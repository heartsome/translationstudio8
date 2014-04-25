
  -dontshrink
  -dontoptimize
  -defaultpackage obClasses
  -allowaccessmodification
  -useuniqueclassmembernames
  -dontusemixedcaseclassnames
  
  -ignorewarnings
  
  -keepattributes LineNumberTable, Signature, Deprecated, InnerClasses
  
  -keep class **.Messages
  
  -keep public class * implements org.eclipse.core.runtime.IPlatformRunnable {public *;}
  