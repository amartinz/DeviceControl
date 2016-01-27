package org.namelessrom.devicecontrol.modules.performance.sub;

import android.os.Bundle;

import org.namelessrom.devicecontrol.DeviceConstants;
import org.namelessrom.devicecontrol.R;
import org.namelessrom.devicecontrol.preferences.CustomPreferenceCategory;
import org.namelessrom.devicecontrol.views.AttachPreferenceFragment;
import org.namelessrom.devicecontrol.utils.PreferenceUtils;
import org.namelessrom.devicecontrol.utils.Utils;

public class IoSchedConfigFragment extends AttachPreferenceFragment {
    @Override protected int getFragmentId() {
        return DeviceConstants.ID_IOSCHED_TUNING;
    }

    @Override public void onCreate(Bundle paramBundle) {
        super.onCreate(paramBundle);
        addPreferencesFromResource(R.xml.extras_io_config);

        final CustomPreferenceCategory ioCat =
                (CustomPreferenceCategory) findPreference("cat_io_config");

        final String path = "/sys/block/mmcblk0/queue/iosched/";
        final String[] files = Utils.listFiles(path, true);
        if (files == null || files.length == 0) {
            getPreferenceScreen().removePreference(ioCat);
        } else {
            for (final String file : files) {
                final int type = PreferenceUtils.getType(file);
                if (PreferenceUtils.TYPE_EDITTEXT == type) {
                    PreferenceUtils.addAwesomeEditTextPreference(getActivity(), "io_conf_",
                            "extras", path, file, ioCat, this);
                }
            }
        }

        isSupported(getPreferenceScreen(), getActivity(), R.string.no_io_tweaks_message);
    }
}
