/*
 *  Copyright (C) 2013 - 2014 Alexander "Evisceration" Martinz
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package org.namelessrom.devicecontrol.hardware;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ConfigurationInfo;
import android.opengl.EGL14;
import android.opengl.EGLConfig;
import android.opengl.EGLContext;
import android.opengl.EGLDisplay;
import android.opengl.EGLSurface;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import org.namelessrom.devicecontrol.Application;
import org.namelessrom.devicecontrol.Logger;
import org.namelessrom.devicecontrol.R;
import org.namelessrom.devicecontrol.models.BootupConfig;
import org.namelessrom.devicecontrol.objects.BootupItem;
import org.namelessrom.devicecontrol.utils.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

import static android.opengl.GLES20.GL_EXTENSIONS;
import static android.opengl.GLES20.GL_RENDERER;
import static android.opengl.GLES20.GL_SHADING_LANGUAGE_VERSION;
import static android.opengl.GLES20.GL_VENDOR;
import static android.opengl.GLES20.GL_VERSION;
import static android.opengl.GLES20.glGetString;

public class GpuUtils {
    public static final int[] GL_INFO = new int[]{
            GL_VENDOR,                  // gpu vendor
            GL_RENDERER,                // gpu renderer
            GL_VERSION,                 // opengl version
            GL_EXTENSIONS,              // opengl extensions
            GL_SHADING_LANGUAGE_VERSION // shader language version
    };

    public static final int[] GL_STRINGS = new int[]{
            R.string.gpu_vendor,        // gpu vendor
            R.string.gpu_renderer,      // gpu renderer
            R.string.opengl_version,    // opengl version
            R.string.opengl_extensions, // opengl extensions
            R.string.shader_version     // shader language version
    };

    private static String gpuBasePath = null;
    private static String gpuGovPath = null;
    private static String gpuGovsAvailablePath = null;
    private static String gpuFreqsAvailPath = null;
    private static String gpuFreqMaxPath = null;
    private static String gpuFreqMinPath = null;

    public static final String FILE_3D_SCALING = "/sys/devices/gr3d/enable_3d_scaling";

    public static class Gpu {
        public final String[] available;
        public final String max;
        public final String min;
        public final String governor;

        public Gpu(final String[] availFreqs, final String maxFreq, final String minFreq,
                final String gov) {
            available = availFreqs;
            max = maxFreq;
            min = minFreq;
            governor = gov;
        }
    }

    private static GpuUtils sInstance;

    private GpuUtils() { }

    public static GpuUtils get() {
        if (sInstance == null) {
            sInstance = new GpuUtils();
        }
        return sInstance;
    }

    @Nullable public String getGpuBasePath() {
        if (gpuBasePath == null) {
            final String[] paths = Application.get().getStringArray(R.array.gpu_base);
            for (final String s : paths) {
                if (Utils.fileExists(s)) {
                    gpuBasePath = s;
                    break;
                }
            }
            if (TextUtils.isEmpty(gpuBasePath)) {
                return "";
            }
        }
        return gpuBasePath;
    }

    @Nullable public String getGpuGovPath() {
        if (gpuGovPath == null) {
            final String base = getGpuBasePath();
            final String[] paths = Application.get().getStringArray(R.array.gpu_gov_path);
            for (final String s : paths) {
                if (Utils.fileExists(base + s)) {
                    gpuGovPath = base + s;
                    break;
                }
            }
            if (TextUtils.isEmpty(gpuGovPath)) {
                return "";
            }
        }
        return gpuGovPath;
    }

    @Nullable public String getGpuGovsAvailablePath() {
        if (gpuGovsAvailablePath == null) {
            final String base = getGpuBasePath();
            final String[] paths = Application.get().getStringArray(R.array.gpu_govs_avail_path);
            for (final String s : paths) {
                if (Utils.fileExists(base + s)) {
                    gpuGovsAvailablePath = base + s;
                    break;
                }
            }
            if (TextUtils.isEmpty(gpuGovsAvailablePath)) {
                return null;
            }
        }
        return gpuGovsAvailablePath;
    }

    @Nullable public String getGpuFreqsAvailPath() {
        if (gpuFreqsAvailPath == null) {
            final String base = getGpuBasePath();
            final String[] paths = Application.get().getStringArray(R.array.gpu_freqs_avail);
            for (final String s : paths) {
                if (Utils.fileExists(base + s)) {
                    gpuFreqsAvailPath = base + s;
                    break;
                }
            }
            if (TextUtils.isEmpty(gpuFreqsAvailPath)) {
                return null;
            }
        }
        return gpuFreqsAvailPath;
    }

    @Nullable public String getGpuFreqMaxPath() {
        if (gpuFreqMaxPath == null) {
            final String base = getGpuBasePath();
            final String[] paths = Application.get().getStringArray(R.array.gpu_freqs_max);
            for (final String s : paths) {
                if (Utils.fileExists(base + s)) {
                    gpuFreqMaxPath = base + s;
                    break;
                }
            }
            if (TextUtils.isEmpty(gpuFreqMaxPath)) {
                return null;
            }
        }
        return gpuFreqMaxPath;
    }

    @Nullable public String getGpuFreqMinPath() {
        if (gpuFreqMinPath == null) {
            final String base = getGpuBasePath();
            final String[] paths = Application.get().getStringArray(R.array.gpu_freqs_min);
            for (final String s : paths) {
                if (Utils.fileExists(base + s)) {
                    gpuFreqMinPath = base + s;
                    break;
                }
            }
            if (TextUtils.isEmpty(gpuFreqMinPath)) {
                return null;
            }
        }
        return gpuFreqMinPath;
    }

    @Nullable public String[] getAvailableFrequencies(final boolean sorted) {
        final String freqsRaw = Utils.readOneLine(getGpuFreqsAvailPath());
        if (freqsRaw != null && !freqsRaw.isEmpty()) {
            final String[] freqs = freqsRaw.split(" ");
            if (!sorted) {
                return freqs;
            }
            Arrays.sort(freqs, new Comparator<String>() {
                @Override
                public int compare(String object1, String object2) {
                    return Utils.tryValueOf(object1, 0).compareTo(Utils.tryValueOf(object2, 0));
                }
            });
            Collections.reverse(Arrays.asList(freqs));
            return freqs;
        }
        return null;
    }

    @Nullable public String getMaxFreq() {
        return Utils.readOneLine(getGpuFreqMaxPath());
    }

    @Nullable public String getMinFreq() {
        return Utils.readOneLine(getGpuFreqMinPath());
    }

    @Nullable public String getGovernor() {
        return Utils.readOneLine(getGpuGovPath());
    }

    public Gpu getGpu() {
        return new GpuUtils.Gpu(
                GpuUtils.get().getAvailableFrequencies(true),
                GpuUtils.get().getMaxFreq(),
                GpuUtils.get().getMinFreq(),
                GpuUtils.get().getGovernor());
    }

    public boolean containsGov(final String gov) {
        final String[] governors = GovernorUtils.get().getAvailableGpuGovernors();
        if (governors == null) return false;
        for (final String s : governors) {
            if (gov.toLowerCase().equals(s.toLowerCase())) return true;
        }
        return false;
    }

    @NonNull public String restore(BootupConfig config) {
        final ArrayList<BootupItem> items = config.getItemsByCategory(BootupConfig.CATEGORY_GPU);
        if (items.size() == 0) {
            return "";
        }

        final StringBuilder sbCmd = new StringBuilder();
        for (final BootupItem item : items) {
            if (!item.enabled) {
                continue;
            }
            sbCmd.append(Utils.getWriteCommand(item.filename, item.value));
        }

        return sbCmd.toString();
    }

    @NonNull public static String toMhz(final String mhz) {
        int mhzInt;
        try {
            mhzInt = Utils.parseInt(mhz);
        } catch (Exception exc) {
            Logger.e(GpuUtils.get(), exc.getMessage());
            mhzInt = 0;
        }
        return (String.valueOf(mhzInt / 1000000) + " MHz");
    }

    @NonNull public static String fromMHz(final String mhzString) {
        if (mhzString != null && !mhzString.isEmpty()) {
            try {
                return String.valueOf(Utils.parseInt(mhzString.replace(" MHz", "")) * 1000000);
            } catch (Exception exc) {
                Logger.e(GpuUtils.get(), exc.getMessage());
            }
        }
        return "0";
    }

    @Nullable public static String[] freqsToMhz(final String[] frequencies) {
        if (frequencies == null) return null;
        final String[] names = new String[frequencies.length];

        for (int i = 0; i < frequencies.length; i++) {
            names[i] = toMhz(frequencies[i]);
        }

        return names;
    }

    public static boolean isOpenGLES20Supported() {
        final ActivityManager am = (ActivityManager)
                Application.get().getSystemService(Context.ACTIVITY_SERVICE);
        final ConfigurationInfo info = am.getDeviceConfigurationInfo();
        if (info == null) {
            // we could not get the configuration information, let's return false
            return false;
        }
        final int glEsVersion = ((info.reqGlEsVersion & 0xffff0000) >> 16);
        Logger.v("isOpenGLES20Supported", "glEsVersion: %s (%s)", glEsVersion,
                info.getGlEsVersion());
        return (glEsVersion >= 2);
    }

    @NonNull public static ArrayList<String> getOpenGLESInformation() {
        final ArrayList<String> glesInformation = new ArrayList<>(GL_INFO.length);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            // get a hold of the display and initialize
            final EGLDisplay dpy = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY);
            final int[] vers = new int[2];
            EGL14.eglInitialize(dpy, vers, 0, vers, 1);

            // find a suitable opengl config. since we do not render, we are not that strict
            // about the exact attributes
            final int[] configAttr = {
                    EGL14.EGL_COLOR_BUFFER_TYPE, EGL14.EGL_RGB_BUFFER,
                    EGL14.EGL_LEVEL, 0,
                    EGL14.EGL_RENDERABLE_TYPE, EGL14.EGL_OPENGL_ES2_BIT,
                    EGL14.EGL_SURFACE_TYPE, EGL14.EGL_PBUFFER_BIT,
                    EGL14.EGL_NONE
            };
            final EGLConfig[] configs = new EGLConfig[1];
            final int[] numConfig = new int[1];
            EGL14.eglChooseConfig(dpy, configAttr, 0, configs, 0, 1, numConfig, 0);
            if (numConfig[0] == 0) {
                Logger.w("getOpenGLESInformation", "no config found! PANIC!");
            }
            final EGLConfig config = configs[0];

            // we need a surface for our context, even if we do not render anything
            // so let's create a little offset surface
            final int[] surfAttr = {
                    EGL14.EGL_WIDTH, 64,
                    EGL14.EGL_HEIGHT, 64,
                    EGL14.EGL_NONE
            };
            final EGLSurface surf = EGL14.eglCreatePbufferSurface(dpy, config, surfAttr, 0);

            // finally let's create our context
            final int[] ctxAttrib = { EGL14.EGL_CONTEXT_CLIENT_VERSION, 2, EGL14.EGL_NONE };
            final EGLContext ctx =
                    EGL14.eglCreateContext(dpy, config, EGL14.EGL_NO_CONTEXT, ctxAttrib, 0);

            // set up everything, make the context our current context
            EGL14.eglMakeCurrent(dpy, surf, surf, ctx);

            // get the informations we desire
            for (final int aGL_INFO : GpuUtils.GL_INFO) {
                glesInformation.add(glGetString(aGL_INFO));
            }

            // free and destroy everything
            EGL14.eglMakeCurrent(dpy, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_SURFACE,
                    EGL14.EGL_NO_CONTEXT);
            EGL14.eglDestroySurface(dpy, surf);
            EGL14.eglDestroyContext(dpy, ctx);
            EGL14.eglTerminate(dpy);
        } else {
            // ... no comment
            for (final int aGL_INFO : GpuUtils.GL_INFO) {
                glesInformation.add(glGetString(aGL_INFO));
            }
        }

        return glesInformation;
    }

}
