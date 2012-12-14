%module(directors="1") BerkeliumCpp
%{
#include "berkelium/Platform.hpp"
#include "berkelium/Berkelium.hpp"
#include "berkelium/Context.hpp"
#include "berkelium/Window.hpp"
#include "berkelium/Widget.hpp"
#include "berkelium/WindowDelegate.hpp"
#include "berkelium/WeakString.hpp"
    using namespace Berkelium;
%}


// Manually have the client of the library load the library
/* %pragma(java) jniclasscode=%{ */
/*   static { */
/*     try { */
/*         System.loadLibrary("BerkeliumCppJavaWrap"); */
/*     } catch (UnsatisfiedLinkError e) { */
/*       System.err.println("Native code library failed to load. \n" + e); */
/*       System.exit(1); */
/*     } */
/*   } */
/* %} */


bool init(FileString homeDirectory, FileString subprocessDirectory, unsigned int extra_argc = 0, const char* extra_argv[] = NULL);
void destroy();
void update();

#if defined(SWIGJAVA)
// FIXME: make sure calling GetByteArrayElements does not result in a memory leak
%typemap(in)     (const char * BYTE, size_t LENGTH) {
/* Functions from jni.h */
$1 = (char *) JCALL2(GetByteArrayElements, jenv, $input, 0);
$2 = (size_t) JCALL1(GetArrayLength, jenv, $input);
}
%typemap(jni)    (const char * BYTE, size_t LENGTH) "jbyteArray";
%typemap(jtype)  (const char * BYTE, size_t LENGTH) "byte[]";
%typemap(jstype) (const char * BYTE, size_t LENGTH) "byte[]";
%typemap(javain) (const char * BYTE, size_t LENGTH) "$javainput";

/* Specify signature of method to handle */
%apply (const char * BYTE, size_t LENGTH)   { (const char * byteArray, size_t len) };

#else
%apply (const char * STRING, size_t LENGTH) { (const char * byteArray, size_t len) };
#endif

template <class CharType>
struct WeakString {
    inline const CharType* data() const;
    inline size_t length() const;
    inline size_t size() const;
    /* template <class StrType> */
    /* inline StrType& get(StrType& ret) const; */
    /* template <class StrType> */
    /* inline StrType get() const; */
    /* template <class StrType> */
    /* inline static WeakString<CharType> point_to(const StrType&input); */
    inline static WeakString<CharType> point_to(const CharType *byteArray, size_t len);
    //inline static WeakString<CharType> point_to(const CharType *byteArray);
    inline static WeakString<CharType> empty();
};
%template(charWeakString) WeakString<char>;
%template(wcharWeakString) WeakString<wchar_t>;
/// TODO: this should be a wchar_t on windows
typedef WeakString<char> FileString;
typedef WeakString<char> URLString;
typedef WeakString<wchar_t> WideString;

%include "arrays_java.i";

%typemap(jni) const Rect *copyRects "jobjectArray";
%typemap(jtype) const Rect *copyRects "Rect[]";
%typemap(jstype) const Rect *copyRects "Rect[]";
%typemap(javain) const Rect *copyRects "$javainput";
%typemap(javaout) const Rect *copyRects "$jnicall";
//%typemap(javadirectorin) (const Rect *copyRects, size_t numCopyRects) "($jniinput == null)? null : Rect.cArrayWrap($jniinput, true)";
%typemap(javadirectorin) const Rect *copyRects "$jniinput";
%typemap(javadirectorout) const Rect *copyRects "Rect.cArrayUnwrap($javainput)";
%typemap(directorin, descriptor="[Lberkelium/Rect;", noblock=1) const Rect *copyRects
{
    
    /// Rect* -> berkelium.Rect
    int i;
    if (!$1) {
        SWIG_JavaThrowException(jenv, SWIG_JavaNullPointerException, "null array");
        return $null;
    }

    jclass theClass = jenv->FindClass("berkelium/Rect");
    if (theClass == NULL) {
        SWIG_JavaThrowException(jenv, SWIG_JavaNullPointerException, "Class not found: berkelium/Rect");
        return $null;
    }
    $input = jenv->NewObjectArray(numCopyRects, theClass, NULL);
    if (!$input) {
        SWIG_JavaThrowException(jenv, SWIG_JavaOutOfMemoryError, "array memory allocation failed for class berkelium/Rect");
        return $null;
    }
    jmethodID ctor = jenv->GetMethodID(theClass, "<init>", "(JZ)V");
    if (ctor == NULL) {
        SWIG_JavaThrowException(jenv, SWIG_JavaRuntimeException, "Cannot find <init> method for class berkelium/Rect");
        return $null;
    }
    for (i=0; i<numCopyRects; i++) {
        jobject obj = jenv->NewObject(theClass, ctor, &($1[i]), false);
        jenv->SetObjectArrayElement($input, i, obj);
    }
    
 };

/* Specify signature of method to handle */
//JAVA_ARRAYSOFCLASSES(Rect);

//%apply Rect [] {(const Rect *copyRects, size_t numCopyRects)};


struct Rect {
    int mLeft;
    int mTop;
    int mWidth;
    int mHeight;

    inline int y() const;
    inline int x() const;
    inline int top() const;
    inline int left() const;
    inline int width() const;
    inline int height() const;
    inline int right() const;
    inline int bottom() const;

    inline bool contains(int x, int y) const;
    Rect intersect(const Rect &rect) const;

    Rect translate(int dx, int dy) const ;
};

%nodefaultctor Context;
/* %newobject *::create; */
/* %newobject *::clone; */
//%delobject *::destroy;
/* %typemap(javafinalize) Context %{ */
/*   protected void finalize() { */
/*     destroy(); delete(); */
/*   } */
/* %} */
//%typemap(newfree) Context* "$1->destroy();";
//%typemap(newfree) Window* "$1->destroy();";
class Context {
public:
    static Context* create();
    void destroy();
    virtual ~Context();
    virtual Context* clone() const = 0;
    virtual ContextImpl* getImpl()= 0;
};


%typemap(javain) WindowDelegate *delegate "getCPtrAndAddReference($javainput)";
%typemap(javacode) Window %{
  // Ensure that the GC doesn't collect any element set from Java
  // as the underlying C++ class stores a shallow copy
  private WindowDelegate wndDelegateReference;
  private long getCPtrAndAddReference(WindowDelegate w) {
    wndDelegateReference = w;
    return WindowDelegate.getCPtr(w);
  }
%}
class Window {
protected:
    Window ();
    Window (const Context *otherContext);

public:
    typedef WidgetList::const_iterator BackToFrontIter;
    typedef WidgetList::const_reverse_iterator FrontToBackIter;

    static Window* create(const Context * context);
    void destroy();
    virtual ~Window();
    virtual void refresh();
    virtual Widget* getWidget() const=0;
    inline Context* getContext() const;
    void setDelegate(WindowDelegate *delegate);
    BackToFrontIter backIter() const;
    BackToFrontIter backEnd() const;
    FrontToBackIter frontIter() const;
    FrontToBackIter frontEnd() const;
    virtual int getId() const = 0;
    virtual void setTransparent(bool istrans) = 0;
    virtual void focus() = 0;
    virtual void unfocus() = 0;
    virtual void mouseMoved(int xPos, int yPos) = 0;
    virtual void mouseButton(unsigned int buttonID, bool down) = 0;
    virtual void mouseWheel(int xScroll, int yScroll) = 0;
    virtual void textEvent(const wchar_t *evt, size_t evtLength) = 0;
    virtual void keyEvent(bool pressed, int mods, int vk_code, int scancode) = 0;
    virtual void resize(int width, int height) = 0;
    virtual void adjustZoom (int mode) = 0;
    virtual void executeJavascript (WideString javascript) = 0;
    virtual void insertCSS (WideString css, WideString elementId) = 0;
    virtual bool navigateTo(URLString url) = 0;
    inline bool navigateTo(const char *url, size_t url_length);
};

class Widget {
public:
    virtual ~Widget() {}

    void destroy(); // defined in src/RenderWidget.cpp

    virtual int getId() const = 0;

    virtual void focus()=0;
    virtual void unfocus()=0;
    virtual bool hasFocus() const = 0;

    virtual void mouseMoved(int xPos, int yPos)=0;
    virtual void mouseButton(unsigned int buttonID, bool down, int clickCount = 1)=0;
    virtual void mouseWheel(int xScroll, int yScroll)=0;

    virtual void textEvent(const wchar_t* evt, size_t evtLength)=0;
    virtual void keyEvent(bool pressed, int mods, int vk_code, int scancode)=0;

    virtual Rect getRect() const=0;
    virtual void setPos(int x, int y)=0;

    virtual void textEvent(WideString text)=0;
};

struct ContextMenuEventArgs {
  enum MediaType {
      MediaTypeNone,
      MediaTypeImage,
      MediaTypeVideo,
      MediaTypeAudio,
  };
  enum EditFlags {
      CanDoNone = 0x0,
      CanUndo = 0x1,
      CanRedo = 0x2,
      CanCut = 0x4,
      CanCopy = 0x8,
      CanPaste = 0x10,
      CanDelete = 0x20,
      CanSelectAll = 0x40,
  };

  MediaType mediaType;

  int mouseX, mouseY;

  URLString linkUrl, srcUrl, pageUrl, frameUrl;
  WideString selectedText;

  bool isEditable;

  int editFlags;
};

enum ScriptAlertType {
	JavascriptAlert = 0,
	JavascriptConfirm = 1,
	JavascriptPrompt = 2
};

enum FileChooserType {
    FileOpen = 0,
    FileOpenMultiple = 1,
    FileOpenFolder = 2,
    FileSaveAs = 3
};

/* %include "arrays_java.i" */
/* JAVA_ARRAYSOFCLASSES(Rect) */
/* %apply Rect[] { const Rect *copyRects }; */

%include "carrays.i";
%array_functions(unsigned char, ucharArray);

%typemap(jni) char* javaNativeData "jobject"
%typemap(jtype) char* javaNativeData "java.nio.ByteBuffer"
%typemap(jstype) char* javaNativeData "java.nio.ByteBuffer"
%typemap(javain) char* javaNativeData "$javainput"
%typemap(javaout) char* javaNativeData {
    return $jnicall;
}
%typemap(in) char* javaNativeData {
  $1 = (char*)(*jenv).GetDirectBufferAddress($input);
  if ($1 == NULL) {
    SWIG_JavaThrowException(jenv, SWIG_JavaRuntimeException, 
       "Unable to get address of direct buffer. Buffer must be allocated direct.");
  }
}
// for some reason swig treats the javaNativeData as a string and
// wants to deallocate it. Remove that behavior.
%typemap(freearg) char* javaNativeData {}

%inline %{
    // Copy data from native array to nio.ByteBuffer
    static void ucharArray_nio_memcopy(char* javaNativeData, unsigned char* nativeData, unsigned int bytes ) {
        memcpy(javaNativeData, nativeData, bytes);
    }
%}

//%apply Rect[] { Rect* copyRects };


%feature("director") WindowDelegate;
class WindowDelegate {
public:
    WindowDelegate();
    virtual ~WindowDelegate();
    virtual void onAddressBarChanged(Window *win, URLString newURL);
    virtual void onStartLoading(Window *win, URLString newURL);
    virtual void onLoad(Window *win);
    virtual void onCrashedWorker(Window *win);
    virtual void onCrashedPlugin(Window *win, WideString pluginName);
    virtual void onProvisionalLoadError(Window *win, URLString url,
                                        int errorCode, bool isMainFrame);
    virtual void onConsoleMessage(Window *win, WideString message,
                                  WideString sourceId, int line_no);
    virtual void onScriptAlert(Window *win, WideString message,
                              WideString defaultValue, URLString url,
                               int flags, bool &success, WideString &value);
    virtual void freeLastScriptAlert(WideString lastValue);
    virtual void onNavigationRequested(Window *win, URLString newUrl,
                                       URLString referrer, bool isNewWindow,
                                       bool &cancelDefaultAction);
    virtual void onLoadingStateChanged(Window *win, bool isLoading);
    virtual void onTitleChanged(Window *win, WideString title);
    virtual void onTooltipChanged(Window *win, WideString text);
    virtual void onCrashed(Window *win);
    virtual void onUnresponsive(Window *win);
    virtual void onResponsive(Window *win);
    virtual void onExternalHost(
        Window *win,
        WideString message,
        URLString origin,
        URLString target);
    virtual void onCreatedWindow(Window *win, Window *newWindow,
                                 const Rect &initialRect);
    virtual void onPaint(
        Window *win,
        const unsigned char *sourceBuffer,
        const Rect &sourceBufferRect,
        size_t numCopyRects,
        const Rect *copyRects,
        int dx, int dy,
        const Rect &scrollRect);
    virtual void onWidgetCreated(Window *win, Widget *newWidget, int zIndex);
    virtual void onWidgetDestroyed(Window *win, Widget *wid);
    virtual void onWidgetResize(
        Window *win,
        Widget *wid,
        int newWidth,
        int newHeight);
    virtual void onWidgetMove(
        Window *win,
        Widget *wid,
        int newX,
        int newY);
    virtual void onWidgetPaint(
        Window *win,
        Widget *wid,
        const unsigned char *sourceBuffer,
        const Rect &sourceBufferRect,
        size_t numCopyRects,
        const Rect *copyRects,
        int dx, int dy,
        const Rect &scrollRect);
    virtual void onCursorUpdated(Window *win, const Cursor& newCursor);
    virtual void onShowContextMenu(Window *win,
                                   const ContextMenuEventArgs& args);
    virtual void onJavascriptCallback(Window *win, void* replyMsg, URLString origin, WideString funcName, Script::Variant *args, size_t numArgs);
    virtual void onRunFileChooser(Window *win, int mode, WideString title, FileString defaultFile);
};
