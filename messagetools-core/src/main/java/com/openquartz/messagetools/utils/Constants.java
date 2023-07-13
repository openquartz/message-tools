package com.openquartz.messagetools.utils;

import java.lang.reflect.Field;
import java.util.Collections;

/**
 * Some useful constants.
 * @author svnee
 **/
public final class Constants {

  /** JVM vendor info. */
  public static final String JVM_VENDOR = System.getProperty("java.vm.vendor");
  public static final String JVM_VERSION = System.getProperty("java.vm.version");
  public static final String JVM_NAME = System.getProperty("java.vm.name");

  /** The value of <tt>System.getProperty("java.version")</tt>. **/
  public static final String JAVA_VERSION = System.getProperty("java.version");

  public static final String OS_ARCH = System.getProperty("os.arch");
  public static final String JAVA_VENDOR = System.getProperty("java.vendor");
  
  public static final boolean JRE_IS_MINIMUM_JAVA7;
  public static final boolean JRE_IS_MINIMUM_JAVA8;
  
  /** True iff running on a 64bit JVM */
  public static final boolean JRE_IS_64BIT;
  
  static {
    boolean is64Bit;
    try {
      final Class<?> unsafeClass = Class.forName("sun.misc.Unsafe");
      final Field unsafeField = unsafeClass.getDeclaredField("theUnsafe");
      unsafeField.setAccessible(true);
      final Object unsafe = unsafeField.get(null);
      final int addressSize = ((Number) unsafeClass.getMethod("addressSize")
        .invoke(unsafe)).intValue();
      is64Bit = addressSize >= 8;
    } catch (Exception e) {
      final String x = System.getProperty("sun.arch.data.model");
      if (x != null) {
        is64Bit = x.contains("64");
      } else {
        is64Bit = OS_ARCH != null && OS_ARCH.contains("64");
      }
    }
    JRE_IS_64BIT = is64Bit;
    
    // this method only exists in Java 7:
    boolean v7 = true;
    try {
      Throwable.class.getMethod("getSuppressed");
    } catch (NoSuchMethodException nsme) {
      v7 = false;
    }
    JRE_IS_MINIMUM_JAVA7 = v7;
    
    if (JRE_IS_MINIMUM_JAVA7) {
      // this method only exists in Java 8:
      boolean v8 = true;
      try {
        Collections.class.getMethod("emptySortedSet");
      } catch (NoSuchMethodException nsme) {
        v8 = false;
      }
      JRE_IS_MINIMUM_JAVA8 = v8;
    } else {
      JRE_IS_MINIMUM_JAVA8 = false;
    }
  }
}
