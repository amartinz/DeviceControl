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
package org.namelessrom.devicecontrol.actions;

import android.graphics.drawable.Drawable;
import android.text.TextUtils;

import org.namelessrom.devicecontrol.App;
import org.namelessrom.devicecontrol.R;
import org.namelessrom.devicecontrol.actions.cpu.CpuFreqMaxAction;
import org.namelessrom.devicecontrol.actions.cpu.CpuFreqMinAction;
import org.namelessrom.devicecontrol.actions.cpu.CpuGovAction;
import org.namelessrom.devicecontrol.actions.extras.MpDecisionAction;
import org.namelessrom.devicecontrol.actions.extras.ksm.KsmDeferredAction;
import org.namelessrom.devicecontrol.actions.extras.ksm.KsmEnableAction;
import org.namelessrom.devicecontrol.actions.extras.ksm.KsmPagesAction;
import org.namelessrom.devicecontrol.actions.extras.ksm.KsmSleepAction;
import org.namelessrom.devicecontrol.actions.extras.uksm.UksmEnableAction;
import org.namelessrom.devicecontrol.actions.extras.uksm.UksmGovernorAction;
import org.namelessrom.devicecontrol.actions.extras.uksm.UksmSleepAction;
import org.namelessrom.devicecontrol.actions.fs.IoSchedulerAction;
import org.namelessrom.devicecontrol.actions.fs.ReadAheadAction;
import org.namelessrom.devicecontrol.actions.gpu.Gpu3dScalingAction;
import org.namelessrom.devicecontrol.actions.gpu.GpuFreqMaxAction;
import org.namelessrom.devicecontrol.actions.gpu.GpuFreqMinAction;
import org.namelessrom.devicecontrol.actions.gpu.GpuGovAction;
import org.namelessrom.devicecontrol.hardware.GovernorUtils;
import org.namelessrom.devicecontrol.hardware.GpuUtils;
import org.namelessrom.devicecontrol.hardware.IoUtils;
import org.namelessrom.devicecontrol.hardware.KsmUtils;
import org.namelessrom.devicecontrol.utils.DrawableHelper;
import org.namelessrom.devicecontrol.utils.Utils;

import java.util.ArrayList;
import java.util.List;

import at.amartinz.hardware.cpu.CpuInformation;
import at.amartinz.hardware.cpu.CpuReader;

public class ActionProcessor {

    public static final String CATEGORY_CPU = "cpu";
    public static final String CATEGORY_GPU = "gpu";
    public static final String CATEGORY_EXTRAS = "extras";
    public static final String CATEGORY_FS = "fs";

    public static final String TRIGGER_SCREEN_ON = "screen_on";
    public static final String TRIGGER_SCREEN_OFF = "screen_off";

    //----------------------------------------------------------------------------------------------
    // CPU
    //----------------------------------------------------------------------------------------------
    public static final String ACTION_CPU_FREQUENCY_MAX = CpuFreqMaxAction.NAME;
    public static final String ACTION_CPU_FREQUENCY_MIN = CpuFreqMinAction.NAME;
    public static final String ACTION_CPU_GOVERNOR = CpuGovAction.NAME;

    //----------------------------------------------------------------------------------------------
    // GPU
    //----------------------------------------------------------------------------------------------
    public static final String ACTION_GPU_FREQUENCY_MAX = GpuFreqMaxAction.NAME;
    public static final String ACTION_GPU_FREQUENCY_MIN = GpuFreqMinAction.NAME;
    public static final String ACTION_GPU_GOVERNOR = GpuGovAction.NAME;

    //----------------------------------------------------------------------------------------------
    public static final String ACTION_3D_SCALING = Gpu3dScalingAction.NAME;

    //----------------------------------------------------------------------------------------------
    // Extras
    //----------------------------------------------------------------------------------------------
    // KSM
    public static final String ACTION_KSM_ENABLED = KsmEnableAction.NAME;
    public static final String ACTION_KSM_DEFERRED = KsmDeferredAction.NAME;
    public static final String ACTION_KSM_PAGES = KsmPagesAction.NAME;
    public static final String ACTION_KSM_SLEEP = KsmSleepAction.NAME;
    // UKSM
    public static final String ACTION_UKSM_ENABLED = UksmEnableAction.NAME;
    public static final String ACTION_UKSM_SLEEP = UksmSleepAction.NAME;
    public static final String ACTION_UKSM_GOVERNOR = UksmGovernorAction.NAME;
    //----------------------------------------------------------------------------------------------
    public static final String ACTION_MPDECISION = MpDecisionAction.NAME;

    //----------------------------------------------------------------------------------------------
    // Filesystem
    //----------------------------------------------------------------------------------------------
    public static final String ACTION_IO_SCHEDULER = IoSchedulerAction.NAME;
    public static final String ACTION_READ_AHEAD = ReadAheadAction.NAME;

    public static Drawable getImageForCategory(final String category) {
        if (TextUtils.equals(CATEGORY_CPU, category)) {
            return DrawableHelper.applyAccentColorFilter(R.drawable.ic_memory_black_24dp);
        }
        if (TextUtils.equals(CATEGORY_GPU, category)) {
            return DrawableHelper.applyAccentColorFilter(R.drawable.ic_dvr_black_24dp);
        }
        if (TextUtils.equals(CATEGORY_EXTRAS, category)) {
            return DrawableHelper.applyAccentColorFilter(R.drawable.ic_developer_mode_black_24dp);
        }
        if (TextUtils.equals(CATEGORY_FS, category)) {
            return DrawableHelper.applyAccentColorFilter(R.drawable.ic_storage_black_24dp);
        }

        // return the extras one by default
        return DrawableHelper.applyAccentColorFilter(R.drawable.ic_developer_mode_black_24dp);
    }

    public static class Entry {
        public final String name;
        public final String value;

        public Entry(final String name, final String value) {
            this.name = name;
            this.value = value;
        }
    }

    public static ArrayList<Entry> getTriggers() {
        final ArrayList<Entry> triggers = new ArrayList<>();

        Entry e = new Entry(App.get().getString(R.string.screen_off), TRIGGER_SCREEN_OFF);
        triggers.add(e);
        e = new Entry(App.get().getString(R.string.screen_on), TRIGGER_SCREEN_ON);
        triggers.add(e);

        return triggers;
    }

    public static ArrayList<Entry> getCategories() {
        final ArrayList<Entry> categories = new ArrayList<>();

        // CPU
        categories.add(new Entry(App.get().getString(R.string.cpu), CATEGORY_CPU));
        // GPU
        categories.add(new Entry(App.get().getString(R.string.gpu), CATEGORY_GPU));
        // Extras
        categories.add(new Entry(App.get().getString(R.string.extras), CATEGORY_EXTRAS));
        // Filesystem
        categories.add(new Entry(App.get().getString(R.string.file_system), CATEGORY_FS));

        return categories;
    }

    public static ArrayList<Entry> getActions(final String category) {
        final ArrayList<Entry> actions = new ArrayList<>();
        if (TextUtils.isEmpty(category)) return actions;

        // CPU
        if (TextUtils.equals(CATEGORY_CPU, category)) {
            actions.add(new Entry(App.get().getString(R.string.frequency_max),
                    ACTION_CPU_FREQUENCY_MAX));
            actions.add(new Entry(App.get().getString(R.string.frequency_min),
                    ACTION_CPU_FREQUENCY_MIN));
            actions.add(new Entry(App.get().getString(R.string.governor),
                    ACTION_CPU_GOVERNOR));
        }
        // GPU
        if (TextUtils.equals(CATEGORY_GPU, category)) {
            if (Utils.fileExists(GpuUtils.get().getGpuFreqMaxPath())) {
                actions.add(new Entry(App.get().getString(R.string.frequency_max),
                        ACTION_GPU_FREQUENCY_MAX));
            }
            if (Utils.fileExists(GpuUtils.get().getGpuFreqMinPath())) {
                actions.add(new Entry(App.get().getString(R.string.frequency_min),
                        ACTION_GPU_FREQUENCY_MIN));
            }
            if (Utils.fileExists(GpuUtils.get().getGpuGovPath())) {
                actions.add(new Entry(App.get().getString(R.string.governor),
                        ACTION_GPU_GOVERNOR));
            }
            if (Utils.fileExists(GpuUtils.FILE_3D_SCALING)) {
                actions.add(new Entry(App.get().getString(R.string.gpu_3d_scaling),
                        ACTION_3D_SCALING));
            }
        }
        // Extras
        if (TextUtils.equals(CATEGORY_EXTRAS, category)) {
            if (Utils.fileExists(KsmUtils.KSM_PATH)) {
                if (Utils.fileExists(App.get().getString(R.string.file_ksm_run))) {
                    actions.add(new Entry(App.get().getString(R.string.enable_ksm),
                            ACTION_KSM_ENABLED));
                }
                if (Utils.fileExists(App.get().getString(R.string.file_ksm_deferred))) {
                    actions.add(new Entry(App.get().getString(R.string.deferred_timer),
                            ACTION_KSM_DEFERRED));
                }
                if (Utils.fileExists(KsmUtils.KSM_PAGES_TO_SCAN)) {
                    actions.add(new Entry(App.get().getString(R.string.pages_to_scan),
                            ACTION_KSM_PAGES));
                }
                if (Utils.fileExists(KsmUtils.KSM_SLEEP)) {
                    actions.add(new Entry(App.get().getString(R.string.sleep_between_scans),
                            ACTION_KSM_SLEEP));
                }
            }
            if (Utils.fileExists(MpDecisionAction.MPDECISION_PATH)) {
                actions.add(new Entry(App.get().getString(R.string.mpdecision),
                        ACTION_MPDECISION));
            }
        }
        // Filesystem
        if (TextUtils.equals(CATEGORY_FS, category)) {
            actions.add(new Entry(App.get().getString(R.string.io), ACTION_IO_SCHEDULER));
            actions.add(new Entry(App.get().getString(R.string.read_ahead),
                    ACTION_READ_AHEAD));
        }

        return actions;
    }

    public static ArrayList<Entry> getValues(final String action) {
        final ArrayList<Entry> values = new ArrayList<>();
        if (TextUtils.isEmpty(action)) return values;

        // CPU frequencies
        if (TextUtils.equals(ACTION_CPU_FREQUENCY_MAX, action) || TextUtils.equals(ACTION_CPU_FREQUENCY_MIN, action)) {
            final List<Integer> freqList = CpuReader.readFreqAvail(0);
            for (final int value : freqList) {
                final String s = String.valueOf(value);
                values.add(new Entry(CpuInformation.toMhz(s), s));
            }
        } else

        // CPU governor
        if (TextUtils.equals(ACTION_CPU_GOVERNOR, action)) {
            final List<String> governors = CpuReader.readGovAvail(0);
            for (final String s : governors) {
                values.add(new Entry(s, s));
            }
        } else

        // GPU frequencies
        if (TextUtils.equals(ACTION_GPU_FREQUENCY_MAX, action)
                || TextUtils.equals(ACTION_GPU_FREQUENCY_MIN, action)) {
            final String[] freqs = GpuUtils.get().getAvailableFrequencies(true);
            if (freqs == null) return values;

            for (final String s : freqs) {
                values.add(new Entry(GpuUtils.toMhz(s), s));
            }
        } else

        // GPU governor
        if (TextUtils.equals(ACTION_GPU_GOVERNOR, action)) {
            final String[] governors = GovernorUtils.get().getAvailableGpuGovernors();
            if (governors == null) return values;

            for (final String s : governors) {
                values.add(new Entry(s, s));
            }
        } else

        // GPU 3D scaling
        if (TextUtils.equals(ACTION_3D_SCALING, action)) {
            addValuesOnOff(values);
        } else

        // Filesystem
        if (TextUtils.equals(ACTION_IO_SCHEDULER, action)) {
            final String[] scheds = IoUtils.get().getAvailableIoSchedulers();
            if (scheds == null) return values;

            for (final String s : scheds) {
                values.add(new Entry(s, s));
            }
        }else
        if (TextUtils.equals(ACTION_READ_AHEAD, action)) {
            final String[] entries = App.get().getStringArray(R.array.read_ahead_entries);
            final String[] vals = App.get().getStringArray(R.array.read_ahead_values);
            for (int i = 0; i < entries.length; i++) {
                values.add(new Entry(entries[i], vals[i]));
            }
        }else
        // Extras
        if (TextUtils.equals(ACTION_KSM_ENABLED, action)
                || TextUtils.equals(ACTION_KSM_DEFERRED, action)) {
            addValuesOnOff(values);
        }else
        if (TextUtils.equals(ACTION_KSM_PAGES, action)) {
            final String[] vals = { "32", "64", "128", "256", "512", "1024" };
            for (final String s : vals) {
                values.add(new Entry(s, s));
            }
        }else
        if (TextUtils.equals(ACTION_KSM_SLEEP, action)) {
            final String[] vals = { "100", "250", "500", "1000", "2000", "3000", "4000", "5000" };
            for (final String s : vals) {
                values.add(new Entry(s, s));
            }
        }else
        if (TextUtils.equals(ACTION_MPDECISION, action)) {
            addValuesOnOff(values);
        }

        return values;
    }

    public static void getProcessAction(final String action, final String value,
            final boolean boot) {
        if (action == null || action.isEmpty() || value == null || value.isEmpty()) {
            return;
        }

        //------------------------------------------------------------------------------------------
        // CPU
        //------------------------------------------------------------------------------------------
        switch (action) {
            case ACTION_CPU_FREQUENCY_MAX:
                new CpuFreqMaxAction(value, boot).triggerAction();
                break;
            case ACTION_CPU_FREQUENCY_MIN:
                new CpuFreqMinAction(value, boot).triggerAction();
                break;
            case ACTION_CPU_GOVERNOR:
                new CpuGovAction(value, boot).triggerAction();
                break;
            //------------------------------------------------------------------------------------------
            // GPU
            //------------------------------------------------------------------------------------------
            case ACTION_GPU_FREQUENCY_MAX:
                new GpuFreqMaxAction(value, boot).triggerAction();
                break;
            case ACTION_GPU_FREQUENCY_MIN:
                new GpuFreqMinAction(value, boot).triggerAction();
                break;
            case ACTION_GPU_GOVERNOR:
                new GpuGovAction(value, boot).triggerAction();
                break;
            case ACTION_3D_SCALING:
                new Gpu3dScalingAction(value, boot).triggerAction();
                break;
            //------------------------------------------------------------------------------------------
            // Filesystem
            //------------------------------------------------------------------------------------------
            case ACTION_IO_SCHEDULER:
                new IoSchedulerAction(value, boot).triggerAction();
                break;
            // Read Ahead ------------------------------------------------------------------------------
            case ACTION_READ_AHEAD:
                new ReadAheadAction(value, boot).triggerAction();
                break;
            //------------------------------------------------------------------------------------------
            // Extras
            //------------------------------------------------------------------------------------------
            case ACTION_KSM_ENABLED:
                new KsmEnableAction(value, boot).triggerAction();
                break;
            case ACTION_KSM_DEFERRED:
                new KsmDeferredAction(value, boot).triggerAction();
                break;
            case ACTION_KSM_PAGES:
                new KsmPagesAction(value, boot).triggerAction();
                break;
            case ACTION_KSM_SLEEP:
                new KsmSleepAction(value, boot).triggerAction();
                break;
            case ACTION_UKSM_SLEEP:
                new UksmSleepAction(value, boot).triggerAction();
                break;
            case ACTION_UKSM_ENABLED:
                new UksmEnableAction(value, boot).triggerAction();
                break;
            case ACTION_UKSM_GOVERNOR:
                new UksmGovernorAction(value, boot).triggerAction();
                break;
        }
    }

    private static void addValuesOnOff(final ArrayList<Entry> values) {
        values.add(new Entry(App.get().getString(R.string.on), "1"));
        values.add(new Entry(App.get().getString(R.string.off), "0"));
    }

    public static void processAction(final String action, final String value) {
        processAction(action, value, false);
    }

    public static void processAction(final String action, final String value, final boolean boot) {
        getProcessAction(action, value, boot);
    }

}
