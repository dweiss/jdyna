
# Misc options

-dontusemixedcaseclassnames
-repackageclasses jdyna

-dontoptimize
-dontobfuscate

-dontnote
-dontwarn

# Keep Log4j

-keep class org.apache.log4j.** {
	public protected *;
}
	
# We need to preserve inner class names

-keepattributes InnerClasses
-keepattributes *Annotation*


# Also keep Enumerations. Keep a method that is required in enumeration classes.

-keepclassmembernames class * {
    java.lang.Class class$(java.lang.String);
    java.lang.Class class$(java.lang.String, boolean);
}

-keepclassmembers class * extends java.lang.Enum {
    public **[] values();
}

-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

-keep class org.jdyna.** implements java.io.Serializable {
    static final long serialVersionUID;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

# Keep configuration and all its private stuff. 

-keep class org.jdyna.frontend.swing.Configuration*  {
    public protected *;
}


# Keep classes with native code.

-keepclasseswithmembernames class * {
    native <methods>;
}

# Keep JDyna main.

-keep class org.jdyna.frontend.swing.JDyna {
    public protected *;
}

# keep lwjgl 

-keep class org.lwjgl.** {
    public protected *;
}

# Keep all of JGoodies.

-keep class com.jgoodies.** {
    public protected *;
}

# Keep all of slf4j

-keep class org.slf4j.** {
    public protected *;
}

# Keep all of simplexml

-keep class org.simpleframework.** {
    public protected *;
}
