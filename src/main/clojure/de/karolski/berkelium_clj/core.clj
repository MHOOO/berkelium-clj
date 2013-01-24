(ns de.karolski.berkelium-clj.core
  (:use
   (clojure.contrib [logging :only (error warn info)]
                    [error-kit
                     :only (deferror handle with-handler
                             continue-with throw-msg bind-continue
                             continue raise do-not-handle)])
   (clojure [stacktrace :only (print-stack-trace)]
            [pprint :only (cl-format)])
   de.karolski.berkelium-clj.image)
  (:import
   java.nio.ByteBuffer))

(defmacro handle-uncaught-exceptions [& body]
  `(try ~@body
        (catch Throwable t#
          (error (with-out-str (print-stack-trace t#))))))

(defn genstr
  [prefix-string]
  (str prefix-string (str (. clojure.lang.RT (nextID)))))

(deferror system-already-shutdown-error [] []
  {:msg (str "Berkelium system has been shutdown once already. Re-initalizing is not supported by berkelium.")
   :unhandled (throw-msg Exception)})

(defonce ^:private +window+ (ref nil))
(defonce ^:private +window-size+ [512 512])
(defonce ^:private +fn-queue+ (ref nil))
(defonce ^{:private true
    :doc "Stores the clojure Window record."}
  +clojure-window-cache+ (ref nil))
(defonce ^:private +native-window-cache+ (ref nil))
(defonce ^:private ^java.lang.ref.ReferenceQueue +clojure-window-reference-queue+ (java.lang.ref.ReferenceQueue.))
(defonce
  ^{:private true
    :doc "Set to true in case the berkelium update thread is to be stopped."}
  +shutdown?+ (atom false))

(defonce
  ^{:private true
    :doc "Set to true in case berkelium has been initialized."}
  +initialized?+ (atom false))

(defonce
  ^{:private true
    :doc "Set by the update thread to true while it is running. Do not modify manually."}
  +running?+ (atom false))

(defonce
  ^{:private true
    :doc "How much time in msec to wait between internal update calls to berkelium"}
  +update-timeout+ (atom 5))

(defn ^:private queue-ctx-fn
  "Run F inside the berkelium update thread. F should take one
  parameter: the berkelium context (which may be used to create
  windows)."
  [ctx-f]
  (dosync (alter +fn-queue+ #(concat % [ctx-f]))))

(defn queue-fn
  "Run F inside the berkelium update thread. F should not take any
  parameters."
  [f]
  (let [ctx-f (fn context-f [_] (f))]
    (queue-ctx-fn ctx-f)))

(defn call-in-berkelium-thread
  "Call f inside the berkelium thread. Blocks until finished & retuns
  result!"
  [f]
  (let [p (promise)]
   (queue-fn (fn [] (deliver p (f))))
   @p))

(defprotocol AWindow
  (window-refresh [_] "Refresh the window.")
  (window-size-of [_] "Return the size of the window as [WIDTH HEIGHT].")
  (mouse-moved [_ x y] "Inject mouse moved event into window.")
  (mouse-button [_ button-id down?] "Inject mouse button event into window.")
  (mouse-wheel [_ x-scroll y-scroll] "Inject scroll event into window.")
  (window-set-delegate [_ delegate] "Set the DELEGATE of this window. Return updated window.")
  (window-nav-to [_ url] "Navigate the window to the URL. Return updated window.")
  ;; TODO more
    )

(defn ^:private ^berkelium.Window native-window-from-key
  "Return the window for the specified KEY. This +may+ return nil for
  as long as the window has not been created yet. If MAKE-WINDOW has
  been used to create the window, it will usually be avaliable after
  the next berkelium update call (which is dependant on
  +update-timeout+)."
  [key]
  (@+native-window-cache+ key))

(defrecord Window [width height window-key]
  AWindow
  (window-refresh [_] (queue-fn (fn refresh-queued [] (.refresh (native-window-from-key window-key)))))
  (window-size-of [_] [width height])
  (mouse-moved [_ x y] (queue-fn (fn mouse-moved-queued [] (.mouseMoved (native-window-from-key window-key) x y))))
  (mouse-button [_ button-id down?] (queue-fn (fn mouse-button-queued [] (.mouseButton (native-window-from-key window-key) button-id down?))))
  (mouse-wheel [_ x-scroll y-scroll] (queue-fn (fn mouse-wheel-queued [] (.mouseWheel (native-window-from-key window-key) x-scroll y-scroll))))
  (window-set-delegate [self delegate]
                       (queue-fn (fn set-delegate-queued []
                                   (info (str "Setting delegate on window with id " window-key))
                                   (.setDelegate (native-window-from-key window-key) delegate)))
                       self)
  (window-nav-to [self url]
                 (queue-fn (fn nav-to-queued []
                             (info (str "Navigating window with id " window-key " to " url))
                             (.navigateTo (native-window-from-key window-key) url (count url))))
                 self))

;; forward declare ensure-init
(def ensure-init nil)

(defn make-window
  "Create and return berkelium window wrapper class. Implements AWindow."
  [width height & {:keys [transparent?] :or {transparent? nil}}]
  (ensure-init)
  (let [id (genstr "berkelium-window")
        clj-wnd (Window. width height id)]
    (info (str "Creating native window with id " id))
    ;; store the NATIVE window inside +native-window-cache+. We must
    ;; do the creation inside the berkelium update thread hovewer
    (queue-ctx-fn (fn [ctx] (let [wnd (berkelium.Window/create ctx)]
                              (dosync (alter +native-window-cache+ #(assoc % id wnd))))))
    (queue-fn (fn []
                (.resize (native-window-from-key id) width height)
                (when transparent?
                  (info (str "Enabling transparency on window with id " id))
                  (.setTransparent (native-window-from-key id) true))))
    ;; in order to be notified of garbage collection of the CLOJURE
    ;; window (the record) we store a weakref->id inside
    ;; +clojure-window-cache+ once the record has been garbage
    ;; collected, we may then destroy the native window
    (dosync (alter +clojure-window-cache+ #(assoc % (java.lang.ref.WeakReference. clj-wnd +clojure-window-reference-queue+) id)))
    clj-wnd))

(defn destroy-unused-native-windows
  "By polling the +clojure-window-reference-queue+, determine garbage
  collected clojure windows, and destroy their corresponding native
  windows. +MUST+ be called from the berkelium update thread."
  []
  (loop [r (.poll +clojure-window-reference-queue+)]
    (when (not (nil? r))
      (let [id (@+clojure-window-cache+ r)
            n-wnd (native-window-from-key id)]
        (info (str "Destroying native window " n-wnd " with id " id))
        (try
          (.destroy n-wnd)
          (finally
           (dosync
            (alter +clojure-window-cache+ #(dissoc % r))
            (alter +native-window-cache+ #(dissoc % id))))))
      (recur (.poll +clojure-window-reference-queue+)))))

(defn cleanup
  []
  (dosync
   (ref-set +window+ nil)))

(defmethod print-method berkelium.charWeakString
  [^berkelium.charWeakString s writer]
  (print-method (str "<charWeakString: '" (.data s) "'>") writer)) 

(defn null-term-str-byte-array
  [^String s] 
  (let [ba (byte-array (+ (count s) 1))]
    (doseq [[c i] (partition 2 (interleave (.getBytes s) (range)))]
      (aset-byte ba i c))
    (aset-byte ba (count s) 0)
    ba))

(defn weak-str
  [^String s]
  (berkelium.charWeakString/point_to (.getBytes s)))

(defn ucharArray-get-item
  [^berkelium.SWIGTYPE_p_unsigned_char ucharArray ^long index]
  (berkelium.BerkeliumCpp/ucharArray_getitem ucharArray index))

(defn ucharArray->char-seq
  [^berkelium.SWIGTYPE_p_unsigned_char ucharArray ^long size]
  (for [^long i (range size)]
    (ucharArray-get-item ucharArray i)))

(defn ^ByteBuffer berkelium-bgra-buf->rgba-ByteBuffer 
  [^berkelium.SWIGTYPE_p_unsigned_char ucharArray ^long size]
  (let [bb (. ByteBuffer (allocateDirect size))]
    (.position bb 0)
    (dotimes [x (/ size 4)]
      (let [i (bgra-to-rgba
               (int-from-chars (ucharArray-get-item ucharArray (* x 4))
                               (ucharArray-get-item ucharArray (+ (* x 4) 1))
                               (ucharArray-get-item ucharArray (+ (* x 4) 2))
                               (ucharArray-get-item ucharArray (+ (* x 4) 3))))]
        (.putInt bb (unchecked-int i))))
    bb))

(let [no-op (comp vec list)]
 (defn make-window-delegate
   [& {:keys [on-paint] :or {on-paint no-op}}]
   (let [p (proxy [berkelium.WindowDelegate] [] 
             (onAddressBarChanged [win newURL] (println "onAddressBarChanged:" newURL))
             (onStartLoading [win newURL] (println "onStartLoading:" newURL))
             (onLoad [win] (println "onLoad"))
             (onCrashedWorker [win] (println "onCrashedWorker"))
             (onCrashedPlugin [win, pluginName] (println "onCrashedPlugin"))
             (onProvisionalLoadError [win, url, errorCode, isMainFrame]
                                     (println "onProvisionalLoadError:" url errorCode isMainFrame))
             (onConsoleMessage [win, message, sourceId, line_no]
                               (println "onConsoleMessage:" message sourceId line_no) ) ;
             (onScriptAlert [win, message,
                             defaultValue, url,
                             flags, success, value] (println "onScriptAlert") )
             (onNavigationRequested [win, newUrl,
                                     referrer, isNewWindow,
                                     cancelDefaultAction] (println "onNavigationRequested") ) ;
             (onLoadingStateChanged [win, isLoading] (println "onLoadingStateChanged: isLoading?" isLoading) ) ;
             (onTitleChanged [win, title] (println "onTitleChanged:" (.data title)))
             (onTooltipChanged [win, text] (println "onTooltipChanged:" text))
             (onCrashed [win] (println "onCrashed") )   ;
             (onUnresponsive [win] (println "onUnresponsive") ) ;
             (onResponsive [win] (println "onResponsive") )     ;
             (onExternalHost [win, message, origin, target] (println "onExternalHost") ) ;
             (onCreatedWindow [win, newWindow, initialRect] (println "onCreatedWindow") ) ;
             (onPaint [win, sourceBuffer, sourceBufferRect, numCopyRects, copyRects, dx, dy, scrollRect] 
               (handle-uncaught-exceptions 
                (on-paint win sourceBuffer sourceBufferRect numCopyRects copyRects dx dy scrollRect)))
             (onWidgetPaint [win, widget, sourceBuffer, sourceBufferRect, numCopyRects, copyRects, dx, dy, scrollRect] 
               (handle-uncaught-exceptions
                (let [rect (.translate sourceBufferRect (.left (.getRect widget)) (.top (.getRect widget)))]
                  (info "sourceBufferRect: " (.left sourceBufferRect) (.top sourceBufferRect) (.width sourceBufferRect) (.height sourceBufferRect))
                  (info "(.getRect widget): " (.left (.getRect widget)) (.top (.getRect widget)) (.width (.getRect widget)) (.height (.getRect widget)))
                  (info "rect: " (.left rect) (.top rect) (.width rect) (.height rect))
                  (dotimes [i numCopyRects]
                    (let [rect (aget copyRects i)]
                     (println "Rect " i " = " (.left rect) (.top rect) (.width rect) (.height rect)))
                    (aset copyRects i (.translate (aget copyRects i) (.left (.getRect widget)) (.top (.getRect widget)))))
                  (on-paint win sourceBuffer rect numCopyRects copyRects dx dy scrollRect))))
             (onWidgetCreated [win, newWidget, zIndex] (println "onWidgetCreated") ) ;
             (onWidgetDestroyed [win, wid] (println "onWidgetDestroyed"))
             (onWidgetResize [win, wid, newWidth, newHeight] (println "onWidgetResize") )
             ;; this is not being called... why? The swig finalize
             ;; method destroys the underlying class from within the
             ;; gc thread instead of the berkelium thread
             ;; (finalize []
             ;;   (info "WindowDelegateProxy.finalize")
             ;;   (call-in-berkelium-thread
             ;;    (fn []
             ;;      (proxy-super finalize))))
             )] 
    p)))

(defn ^:private
  berkelium-update
  "Do not call manually. This will be called from the berkelium update
  thread."
  [context]
  (Thread/sleep @+update-timeout+)

  (destroy-unused-native-windows)
  
  ;; call any queued fns
  (dosync
   (alter +fn-queue+
          (fn [queue]
            (doseq [f queue]
              (handle-uncaught-exceptions
               (f context)))
            nil))) 
            
  ;; update berkelium state 
  (berkelium.BerkeliumCpp/update))

(defn init
  "Initialize the berkelium instance. Can be called multiple times,
   but will only initialize berkelium +once+. See also ENSURE-INIT
   which is more descriptive. Re-initializing after a call to SHUTDOWN is +not+ supported and will raise SYSTEM-ALREADY-SHUTDOWN-ERROR"
  []
  (let [ ;; path should stay valid for entire runtime
        path (.getAbsolutePath (java.io.File. "./browser/"))
        wpath (weak-str path)
        init-lock (ref nil)]
    (locking init-lock
      (cond
       @+shutdown?+ (raise system-already-shutdown-error)
       (and (not @+initialized?+) (not @+running?+))
       (let [block (promise)]
         (future
           (handle-uncaught-exceptions
            (try
              (info "Initializing berkelium")
              (berkelium.BerkeliumCpp/init wpath wpath)
              (finally
               (reset! +initialized?+ true)
               (deliver block true)))
             
            (let [ctx (berkelium.Context/create)
                  ;; create a single window that is always active. This will
                  ;; work around some memory leak issues with
                  ;; berkelium/chromium apparently
                  win (berkelium.Window/create ctx)]
              (info "Creating default context & window")
              (.resize win (first +window-size+) (second +window-size+))
              (.navigateTo win "http://localhost" (count "http://localhost"))
              ;; the window delegate will not be gc'ed while the initial
              ;; window exists
              ;; (.setDelegate win (make-window-delegate))
               
              (dosync
               (ref-set +window+ win))
               
              (when (not @+running?+)
                ;; update browser
                (reset! +running?+ true)
                (try
                  (info (str "Starting berkelium update thread"))
                  (while (not @+shutdown?+)
                    (handle-uncaught-exceptions
                     (berkelium-update ctx)))
                  (finally
                   (info (str "Stopping berkelium update thread"))
                   (reset! +running?+ false)
                   (when @+shutdown?+
                     (info (str "Shutting down berkelium")) 
                     (try
                       (dosync
                        (ref-set +window+ nil))
                       (berkelium.BerkeliumCpp/destroy)))))))))
         @block)))))

(defn ensure-init
  []
  (init))

(defn shutdown
  []
  (reset! +shutdown?+ true)
  (reset! +initialized?+ false))

(defn partial-update?
  [^berkelium.Rect sbRect [window-width window-height]]
  (if (or (not (== 0 (.left sbRect)))
          (not (== 0 (.top sbRect)))
          (not (== window-width (.right sbRect)))
          (not (== window-height (.bottom sbRect))))
    true
    false))

(defn texture-from-page
  "Generates a texture from the specified URL. The texture will be
  passed to the CALLBACK-FN once it is available or updated. This
  function will return a window object which can be used to pass
  events (like mouse movement) to the window."
  [url callback-fn & {:keys [size] :or {size [512 512]}}]
  (ensure-init)
  (let [[width height] size
        wnd (make-window width height :transparent? true)
        native-wnd-id (:window-key wnd)]
    (queue-fn (fn []
                (let [delegate
                      (make-window-delegate
                       :on-paint
                       (fn [native-win, sourceBuffer, sourceBufferRect, numCopyRects, copyRects, dx, dy, scrollRect]
                         (if-let [[wnd-weak-ref wnd-native-id] (first (filter (fn [[weak-ref native-id]] (= native-wnd-id native-id)) @+clojure-window-cache+))]
                           (if-let [wnd (.get wnd-weak-ref)]
                             (callback-fn wnd sourceBuffer sourceBufferRect numCopyRects copyRects dx dy scrollRect)
                             (throw (Exception. (str "Tried to use a callback fn on window with native-id: " native-wnd-id ". But it has already been garbage collected!"))))
                           (throw (Exception. (str "Unable to find window with native-id: " native-wnd-id " for onPaint event."))))))]
                  (.setDelegate (native-window-from-key native-wnd-id) delegate)
                  (.navigateTo (native-window-from-key native-wnd-id) url (count url))
                  (-> wnd                  
                      ;; these two queue up, but we're already inside the berkelium thread
                      ;; (window-set-delegate delegate)
                      ;; (window-nav-to url)
                      ))
                ))
    
    wnd))

(defn ^ByteBuffer berkelium-image-buf->bytebuffer
  [sourceBuffer byte-count] 
  (berkelium-bgra-buf->rgba-ByteBuffer sourceBuffer byte-count))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Deprecated test
(defn nav-to [url & {:keys [window] :or {window nil}}]
  (ensure-init)
  (queue-fn (fn nav-to-queued [] (.navigateTo (or window @+window+) url (count url)))))


(defn -main []
  (let [url "http://google.com"]
    (info "Trying to access: " url)
    (texture-from-page url
                       (fn [& args]
                         (info args)))
    (info "end of main")))

(gen-class
 :name de.karolski.berkelium_clj.Static
 :prefix genclass-
 :methods [#^{:static true} [TextureFromPage [String clojure.lang.AFn clojure.lang.ISeq] de.karolski.berkelium_clj.core.Window]])

(defn genclass-TextureFromPage [url f size]
  (texture-from-page url f :size size))