# This is a configuration file for ProGuard.
# http://proguard.sourceforge.net/index.html#manual/usage.html
-dontobfuscate

-dontnote com.android.internal.annotations.**
-dontnote com.google.common.annotations.**
-dontwarn javax.annotation.**

# Restore some source file names and restore approximate line numbers in the stack traces,
# otherwise the stack traces are pretty useless
-keepattributes SourceFile,LineNumberTable

# Also save some stuff from us
-keep class org.namelessrom.devicecontrol.objects.** { *; }
-keep class org.namelessrom.devicecontrol.modules.wizard.** { *; }
-keep class org.namelessrom.devicecontrol.models.** { *; }

# Do not break our reflection voodoo
-keep class android.content.pm.IPackageStatsObserver

# Google Play Services
-keep class * extends java.util.ListResourceBundle {
    protected Object[][] getContents();
}

-keep public class com.google.android.gms.common.internal.safeparcel.SafeParcelable {
    public static final *** NULL;
}

-keepnames @com.google.android.gms.common.annotation.KeepName class *
-keepclassmembernames class * {
    @com.google.android.gms.common.annotation.KeepName *;
}

-keepnames class * implements android.os.Parcelable {
    public static final ** CREATOR;
}

# android support design
-keepclassmembers class android.support.design.widget.FloatingActionButton$Behavior { public <init>(); }

# mpchartlib
-keep public class com.github.mikephil.charting.animation.* {
    public protected *;
}
-dontwarn com.github.mikephil.charting.data.realm.**

# pollfish
-dontwarn com.pollfish.**
-keep class com.pollfish.** { *; }

# my stuff
-keep class alexander.martinz.libs.** { *; }

# RxAndroid
-dontwarn rx.internal.util.**

# Glide
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep public enum com.bumptech.glide.load.resource.bitmap.ImageHeaderParser$** {
  **[] $VALUES;
  public *;
}

##### Sense360 START

## Gson exclusions
-keepattributes Signature
-keep class sun.misc.Unsafe { *; }
-keep class com.google.gson.examples.android.model.** { *; }
## Guava exclusions
-dontwarn sun.misc.Unsafe
-dontwarn com.google.common.collect.MinMaxPriorityQueue
-keepclasseswithmembers public class * {
public static void main(java.lang.String[]);
}
#OKHTTP exclusions
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.squareup.okhttp.** { *; }
-keep interface com.squareup.okhttp.** { *; }
-dontwarn com.squareup.okhttp.**
-dontwarn okio.**
-keep class okio.**

##### Sense360 END
