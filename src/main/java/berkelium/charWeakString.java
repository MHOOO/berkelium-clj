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

public class charWeakString {
  private long swigCPtr;
  protected boolean swigCMemOwn;

  protected charWeakString(long cPtr, boolean cMemoryOwn) {
    swigCMemOwn = cMemoryOwn;
    swigCPtr = cPtr;
  }

  protected static long getCPtr(charWeakString obj) {
    return (obj == null) ? 0 : obj.swigCPtr;
  }

  protected void finalize() {
    delete();
  }

  public synchronized void delete() {
    if (swigCPtr != 0) {
      if (swigCMemOwn) {
        swigCMemOwn = false;
        BerkeliumCppJNI.delete_charWeakString(swigCPtr);
      }
      swigCPtr = 0;
    }
  }

  public String data() {
    return BerkeliumCppJNI.charWeakString_data(swigCPtr, this);
  }

  public long length() {
    return BerkeliumCppJNI.charWeakString_length(swigCPtr, this);
  }

  public long size() {
    return BerkeliumCppJNI.charWeakString_size(swigCPtr, this);
  }

  public static charWeakString point_to(byte[] byteArray) {
    return new charWeakString(BerkeliumCppJNI.charWeakString_point_to(byteArray), true);
  }

  public static charWeakString empty() {
    return new charWeakString(BerkeliumCppJNI.charWeakString_empty(), true);
  }

  public charWeakString() {
    this(BerkeliumCppJNI.new_charWeakString(), true);
  }

}
