/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 2.0.7
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package berkelium;

public class Rect {
  private long swigCPtr;
  protected boolean swigCMemOwn;

  protected Rect(long cPtr, boolean cMemoryOwn) {
    swigCMemOwn = cMemoryOwn;
    swigCPtr = cPtr;
  }

  protected static long getCPtr(Rect obj) {
    return (obj == null) ? 0 : obj.swigCPtr;
  }

  protected void finalize() {
    delete();
  }

  public synchronized void delete() {
    if (swigCPtr != 0) {
      if (swigCMemOwn) {
        swigCMemOwn = false;
        BerkeliumCppJNI.delete_Rect(swigCPtr);
      }
      swigCPtr = 0;
    }
  }

  public void setMLeft(int value) {
    BerkeliumCppJNI.Rect_mLeft_set(swigCPtr, this, value);
  }

  public int getMLeft() {
    return BerkeliumCppJNI.Rect_mLeft_get(swigCPtr, this);
  }

  public void setMTop(int value) {
    BerkeliumCppJNI.Rect_mTop_set(swigCPtr, this, value);
  }

  public int getMTop() {
    return BerkeliumCppJNI.Rect_mTop_get(swigCPtr, this);
  }

  public void setMWidth(int value) {
    BerkeliumCppJNI.Rect_mWidth_set(swigCPtr, this, value);
  }

  public int getMWidth() {
    return BerkeliumCppJNI.Rect_mWidth_get(swigCPtr, this);
  }

  public void setMHeight(int value) {
    BerkeliumCppJNI.Rect_mHeight_set(swigCPtr, this, value);
  }

  public int getMHeight() {
    return BerkeliumCppJNI.Rect_mHeight_get(swigCPtr, this);
  }

  public int y() {
    return BerkeliumCppJNI.Rect_y(swigCPtr, this);
  }

  public int x() {
    return BerkeliumCppJNI.Rect_x(swigCPtr, this);
  }

  public int top() {
    return BerkeliumCppJNI.Rect_top(swigCPtr, this);
  }

  public int left() {
    return BerkeliumCppJNI.Rect_left(swigCPtr, this);
  }

  public int width() {
    return BerkeliumCppJNI.Rect_width(swigCPtr, this);
  }

  public int height() {
    return BerkeliumCppJNI.Rect_height(swigCPtr, this);
  }

  public int right() {
    return BerkeliumCppJNI.Rect_right(swigCPtr, this);
  }

  public int bottom() {
    return BerkeliumCppJNI.Rect_bottom(swigCPtr, this);
  }

  public boolean contains(int x, int y) {
    return BerkeliumCppJNI.Rect_contains(swigCPtr, this, x, y);
  }

  public Rect intersect(Rect rect) {
    return new Rect(BerkeliumCppJNI.Rect_intersect(swigCPtr, this, Rect.getCPtr(rect), rect), true);
  }

  public Rect translate(int dx, int dy) {
    return new Rect(BerkeliumCppJNI.Rect_translate(swigCPtr, this, dx, dy), true);
  }

  public Rect() {
    this(BerkeliumCppJNI.new_Rect(), true);
  }

}
