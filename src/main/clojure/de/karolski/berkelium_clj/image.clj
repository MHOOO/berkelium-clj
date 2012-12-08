(ns de.karolski.berkelium-clj.image
  (:use (clojure.contrib [logging :only (error warn info with-logs)])
        (clojure [pprint :only (cl-format)]))
  (:import
   java.awt.image.MemoryImageSource
   java.awt.image.BufferedImage
   java.awt.image.ComponentColorModel
   java.awt.color.ColorSpace
   java.awt.image.DataBuffer
   java.io.ByteArrayInputStream
   javax.imageio.ImageIO 
   java.nio.ByteBuffer
   ))

(defn bgra-to-rgba 
  "Incoming value should be:
   0xBBGGRRAA"
  ^long
  [^long c]
  (reduce
   bit-or
   [(bit-shift-left (bit-and (bit-shift-right c 8) 0xFF) 24)
    (bit-shift-left (bit-and (bit-shift-right c 16) 0xFF) 16)
    (bit-shift-left (bit-and (bit-shift-right c 24) 0xFF) 8)
    (bit-shift-left (bit-and c 0xFF) 0)]))

(defn int-from-chars
  {:tag long
   :static true}
  ([^long b3 ^long b2 ^long b1 ^long b0]
     (bit-or
      (bit-or
       (bit-or
        (bit-shift-left b3 24)
        (bit-shift-left b2 16))
       (bit-shift-left b1 8))
      b0)))

(defn image-pixel-of
  "Return the pixel (as an integer) at the specified X,Y position
  within the image."
  ^long [image ^long x ^long y]
  {:pre [(== 1 (count (.getData image)))]}
  (let [w (.getWidth image)
        bpp (/ (.getBitsPerPixel (.getFormat image)) 8)
        pix (byte-array bpp)]
    (.position (.getData image 0) (+ (* bpp x) (* y (* bpp w))))
    (.get (.getData image 0) pix) 
    (apply int-from-chars (seq pix))))

(defn byte-array-to-buffered-image
  [byte-array width height]
  (let [model (ComponentColorModel. (ColorSpace/getInstance ColorSpace/CS_sRGB)
                                    (int-array [8 8 8 8])
                                    true ;; hasAlpha
                                    false ;; isAlphaPremultiplied
                                    java.awt.Transparency/OPAQUE ;; transparency
                                    DataBuffer/TYPE_BYTE ;; transfertype
                                    )
        raster (.createCompatibleWritableRaster model width height)
        b-image (BufferedImage. model raster false nil)
        byte-data (.getData (.getDataBuffer raster))]
    ;; copy over contents
    (.setDataElements raster 0 0 width height byte-array)
    b-image))

(defn int-array-to-awt-image
  "Takes an INT[] array and returns an Image for it.
  TODO: Test"
  [array width height]
  (.createImage (java.awt.Toolkit/getDefaultToolkit)
                (MemoryImageSource. width
                                    height
                                    ;; the color space (we use RGBA, not simple RGB)
                                    (ComponentColorModel. (ColorSpace/getInstance ColorSpace/CS_sRGB)
                                                          (int-array [8 8 8 8])
                                                          true ;; hasAlpha
                                                          false ;; isAlphaPremultiplied
                                                          java.awt.Transparency/OPAQUE;; transparency
                                                          DataBuffer/TYPE_INT ;; transfertype
                                                          )
                                    ;; the array data
                                    array
                                    0
                                    width)))

(defn awt-image-to-buffered-image
  "TODO: Test"
  [image]
  {:pre [(isa? (type image) java.awt.Image)]}
  (let [buffered-image (BufferedImage. (.getWidth image nil) (.getHeight image nil) BufferedImage/TYPE_INT_RGB)
        g2d (doto (.createGraphics buffered-image)
              (.drawImage image nil nil))]
    buffered-image))

(defn image-array-to-buffered-image
  "Takes an image as a byte array of the specified format FORMAT-NAME
  and returns a bufferedimage for it. TODO: Test"
  [byte-array width height & {:keys [format-name] :or {format-name "bmp"}}]
  (let [in (ByteArrayInputStream. byte-array)
        iis (ImageIO/createImageInputStream in)
        ;; the reader is necessary for reading different kinds of formats
        reader (doto (.next (ImageIO/getImageReadersByFormatName format-name))
                 (.setInput iis true))
        ;; the image params
        param (.getDefaultReadParam reader)
        ;; create the image
        image (.read reader 0 param)]
    (awt-image-to-buffered-image image)))


(defn char-seq->int-seq
  [char-seq]
  (map #(apply int-from-chars %)
       (partition 4 char-seq)))

(defn process-image-data
  [data-seq components-per-element
   & {:keys [status-fn pixel-format-converter-fn]
      :or {status-fn (fn [percent] nil)
           pixel-format-converter-fn identity}}]
  (let [len (count data-seq)
        processed-data (. ByteBuffer (allocateDirect (* len components-per-element)))
        update-offset (int (/ len 100))
        processed (atom 1)]
    (letfn [(proc-default [^long c]
                          (.putInt processed-data (pixel-format-converter-fn c)))
            (proc-with-status [c]
              (when (== 0 (mod @processed update-offset))
                (status-fn (* 100.0 (/ (float @processed) len))))
              (proc-default c)
              (swap! processed inc))]
      (let [proc-fn (if (and status-fn (> update-offset 0)) proc-with-status proc-default)] 
        (loop [[c & rest] (seq data-seq)]
          (when c
            (proc-fn c)
            (recur rest)))
        processed-data))))
