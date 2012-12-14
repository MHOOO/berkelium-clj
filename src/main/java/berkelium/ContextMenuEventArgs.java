/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
<<<<<<< HEAD
 * Version 2.0.7
=======
 * Version 2.0.8
>>>>>>> b8cebaadde5aead5da24db0db1d434ec68fdb8d1
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package berkelium;

public class ContextMenuEventArgs {
  private long swigCPtr;
  protected boolean swigCMemOwn;

  protected ContextMenuEventArgs(long cPtr, boolean cMemoryOwn) {
    swigCMemOwn = cMemoryOwn;
    swigCPtr = cPtr;
  }

  protected static long getCPtr(ContextMenuEventArgs obj) {
    return (obj == null) ? 0 : obj.swigCPtr;
  }

  protected void finalize() {
    delete();
  }

  public synchronized void delete() {
    if (swigCPtr != 0) {
      if (swigCMemOwn) {
        swigCMemOwn = false;
        BerkeliumCppJNI.delete_ContextMenuEventArgs(swigCPtr);
      }
      swigCPtr = 0;
    }
  }

  public void setMediaType(ContextMenuEventArgs.MediaType value) {
    BerkeliumCppJNI.ContextMenuEventArgs_mediaType_set(swigCPtr, this, value.swigValue());
  }

  public ContextMenuEventArgs.MediaType getMediaType() {
    return ContextMenuEventArgs.MediaType.swigToEnum(BerkeliumCppJNI.ContextMenuEventArgs_mediaType_get(swigCPtr, this));
  }

  public void setMouseX(int value) {
    BerkeliumCppJNI.ContextMenuEventArgs_mouseX_set(swigCPtr, this, value);
  }

  public int getMouseX() {
    return BerkeliumCppJNI.ContextMenuEventArgs_mouseX_get(swigCPtr, this);
  }

  public void setMouseY(int value) {
    BerkeliumCppJNI.ContextMenuEventArgs_mouseY_set(swigCPtr, this, value);
  }

  public int getMouseY() {
    return BerkeliumCppJNI.ContextMenuEventArgs_mouseY_get(swigCPtr, this);
  }

  public void setLinkUrl(charWeakString value) {
    BerkeliumCppJNI.ContextMenuEventArgs_linkUrl_set(swigCPtr, this, charWeakString.getCPtr(value), value);
  }

  public charWeakString getLinkUrl() {
    long cPtr = BerkeliumCppJNI.ContextMenuEventArgs_linkUrl_get(swigCPtr, this);
    return (cPtr == 0) ? null : new charWeakString(cPtr, false);
  }

  public void setSrcUrl(charWeakString value) {
    BerkeliumCppJNI.ContextMenuEventArgs_srcUrl_set(swigCPtr, this, charWeakString.getCPtr(value), value);
  }

  public charWeakString getSrcUrl() {
    long cPtr = BerkeliumCppJNI.ContextMenuEventArgs_srcUrl_get(swigCPtr, this);
    return (cPtr == 0) ? null : new charWeakString(cPtr, false);
  }

  public void setPageUrl(charWeakString value) {
    BerkeliumCppJNI.ContextMenuEventArgs_pageUrl_set(swigCPtr, this, charWeakString.getCPtr(value), value);
  }

  public charWeakString getPageUrl() {
    long cPtr = BerkeliumCppJNI.ContextMenuEventArgs_pageUrl_get(swigCPtr, this);
    return (cPtr == 0) ? null : new charWeakString(cPtr, false);
  }

  public void setFrameUrl(charWeakString value) {
    BerkeliumCppJNI.ContextMenuEventArgs_frameUrl_set(swigCPtr, this, charWeakString.getCPtr(value), value);
  }

  public charWeakString getFrameUrl() {
    long cPtr = BerkeliumCppJNI.ContextMenuEventArgs_frameUrl_get(swigCPtr, this);
    return (cPtr == 0) ? null : new charWeakString(cPtr, false);
  }

  public void setSelectedText(wcharWeakString value) {
    BerkeliumCppJNI.ContextMenuEventArgs_selectedText_set(swigCPtr, this, wcharWeakString.getCPtr(value), value);
  }

  public wcharWeakString getSelectedText() {
    long cPtr = BerkeliumCppJNI.ContextMenuEventArgs_selectedText_get(swigCPtr, this);
    return (cPtr == 0) ? null : new wcharWeakString(cPtr, false);
  }

  public void setIsEditable(boolean value) {
    BerkeliumCppJNI.ContextMenuEventArgs_isEditable_set(swigCPtr, this, value);
  }

  public boolean getIsEditable() {
    return BerkeliumCppJNI.ContextMenuEventArgs_isEditable_get(swigCPtr, this);
  }

  public void setEditFlags(int value) {
    BerkeliumCppJNI.ContextMenuEventArgs_editFlags_set(swigCPtr, this, value);
  }

  public int getEditFlags() {
    return BerkeliumCppJNI.ContextMenuEventArgs_editFlags_get(swigCPtr, this);
  }

  public ContextMenuEventArgs() {
    this(BerkeliumCppJNI.new_ContextMenuEventArgs(), true);
  }

  public final static class MediaType {
    public final static ContextMenuEventArgs.MediaType MediaTypeNone = new ContextMenuEventArgs.MediaType("MediaTypeNone");
    public final static ContextMenuEventArgs.MediaType MediaTypeImage = new ContextMenuEventArgs.MediaType("MediaTypeImage");
    public final static ContextMenuEventArgs.MediaType MediaTypeVideo = new ContextMenuEventArgs.MediaType("MediaTypeVideo");
    public final static ContextMenuEventArgs.MediaType MediaTypeAudio = new ContextMenuEventArgs.MediaType("MediaTypeAudio");

    public final int swigValue() {
      return swigValue;
    }

    public String toString() {
      return swigName;
    }

    public static MediaType swigToEnum(int swigValue) {
      if (swigValue < swigValues.length && swigValue >= 0 && swigValues[swigValue].swigValue == swigValue)
        return swigValues[swigValue];
      for (int i = 0; i < swigValues.length; i++)
        if (swigValues[i].swigValue == swigValue)
          return swigValues[i];
      throw new IllegalArgumentException("No enum " + MediaType.class + " with value " + swigValue);
    }

    private MediaType(String swigName) {
      this.swigName = swigName;
      this.swigValue = swigNext++;
    }

    private MediaType(String swigName, int swigValue) {
      this.swigName = swigName;
      this.swigValue = swigValue;
      swigNext = swigValue+1;
    }

    private MediaType(String swigName, MediaType swigEnum) {
      this.swigName = swigName;
      this.swigValue = swigEnum.swigValue;
      swigNext = this.swigValue+1;
    }

    private static MediaType[] swigValues = { MediaTypeNone, MediaTypeImage, MediaTypeVideo, MediaTypeAudio };
    private static int swigNext = 0;
    private final int swigValue;
    private final String swigName;
  }

  public final static class EditFlags {
    public final static ContextMenuEventArgs.EditFlags CanDoNone = new ContextMenuEventArgs.EditFlags("CanDoNone", BerkeliumCppJNI.ContextMenuEventArgs_CanDoNone_get());
    public final static ContextMenuEventArgs.EditFlags CanUndo = new ContextMenuEventArgs.EditFlags("CanUndo", BerkeliumCppJNI.ContextMenuEventArgs_CanUndo_get());
    public final static ContextMenuEventArgs.EditFlags CanRedo = new ContextMenuEventArgs.EditFlags("CanRedo", BerkeliumCppJNI.ContextMenuEventArgs_CanRedo_get());
    public final static ContextMenuEventArgs.EditFlags CanCut = new ContextMenuEventArgs.EditFlags("CanCut", BerkeliumCppJNI.ContextMenuEventArgs_CanCut_get());
    public final static ContextMenuEventArgs.EditFlags CanCopy = new ContextMenuEventArgs.EditFlags("CanCopy", BerkeliumCppJNI.ContextMenuEventArgs_CanCopy_get());
    public final static ContextMenuEventArgs.EditFlags CanPaste = new ContextMenuEventArgs.EditFlags("CanPaste", BerkeliumCppJNI.ContextMenuEventArgs_CanPaste_get());
    public final static ContextMenuEventArgs.EditFlags CanDelete = new ContextMenuEventArgs.EditFlags("CanDelete", BerkeliumCppJNI.ContextMenuEventArgs_CanDelete_get());
    public final static ContextMenuEventArgs.EditFlags CanSelectAll = new ContextMenuEventArgs.EditFlags("CanSelectAll", BerkeliumCppJNI.ContextMenuEventArgs_CanSelectAll_get());

    public final int swigValue() {
      return swigValue;
    }

    public String toString() {
      return swigName;
    }

    public static EditFlags swigToEnum(int swigValue) {
      if (swigValue < swigValues.length && swigValue >= 0 && swigValues[swigValue].swigValue == swigValue)
        return swigValues[swigValue];
      for (int i = 0; i < swigValues.length; i++)
        if (swigValues[i].swigValue == swigValue)
          return swigValues[i];
      throw new IllegalArgumentException("No enum " + EditFlags.class + " with value " + swigValue);
    }

    private EditFlags(String swigName) {
      this.swigName = swigName;
      this.swigValue = swigNext++;
    }

    private EditFlags(String swigName, int swigValue) {
      this.swigName = swigName;
      this.swigValue = swigValue;
      swigNext = swigValue+1;
    }

    private EditFlags(String swigName, EditFlags swigEnum) {
      this.swigName = swigName;
      this.swigValue = swigEnum.swigValue;
      swigNext = this.swigValue+1;
    }

    private static EditFlags[] swigValues = { CanDoNone, CanUndo, CanRedo, CanCut, CanCopy, CanPaste, CanDelete, CanSelectAll };
    private static int swigNext = 0;
    private final int swigValue;
    private final String swigName;
  }

}
