package org.namelessrom.devicecontrol.objects;

import android.os.Build;
import android.text.TextUtils;

import com.google.gson.annotations.SerializedName;

import org.namelessrom.devicecontrol.utils.Utils;

/**
 * Created by alex on 20.10.14.
 */
public class Device {
    @SerializedName("platform_version") public final String platformVersion;
    @SerializedName("platform_id") public final String platformId;
    @SerializedName("platform_type") public final String platformType;
    @SerializedName("platform_tags") public final String platformTags;
    @SerializedName("platform_build_date") public final String platformBuildType;

    @SerializedName("vm_library") public final String vmLibrary;
    @SerializedName("vm_version") public final String vmVersion;

    @SerializedName("android_id") public final String androidId;
    @SerializedName("device_manufacturer") public final String manufacturer;
    @SerializedName("device_model") public final String model;
    @SerializedName("device_product") public final String product;
    @SerializedName("device_board") public final String board;
    @SerializedName("device_bootloader") public final String bootloader;
    @SerializedName("device_radio_version") public final String radio;

    private static Device sInstance;

    private Device() {
        platformVersion = Build.VERSION.RELEASE;
        platformId = Build.DISPLAY;
        platformType = Build.VERSION.CODENAME + " " + Build.TYPE;
        platformTags = Build.TAGS;
        platformBuildType = Utils.getDate(Build.TIME);

        vmLibrary = getRuntime();
        vmVersion = System.getProperty("java.vm.version", "-");

        androidId = Utils.getAndroidId();
        manufacturer = Build.MANUFACTURER;
        model = Build.MODEL;
        product = Build.PRODUCT;
        board = Build.BOARD;
        bootloader = Build.BOOTLOADER;
        radio = Build.getRadioVersion();
    }

    public static Device get() {
        if (sInstance == null) {
            sInstance = new Device();
        }
        return sInstance;
    }

    private String getRuntime() {
        String tmp = Utils.getCommandResult("getprop persist.sys.dalvik.vm.lib", "-");
        if (!TextUtils.equals(tmp, "-")) {
            final String runtime = TextUtils.equals(tmp, "libdvm.so")
                    ? "Dalvik" : TextUtils.equals(tmp, "libart.so") ? "ART" : "-";
            tmp = String.format("%s (%s)", runtime, tmp);
        }
        return tmp;
    }
}
