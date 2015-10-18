package org.namelessrom.devicecontrol.modules.controls;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.namelessrom.devicecontrol.R;
import org.namelessrom.devicecontrol.base.BaseActivity;
import org.namelessrom.devicecontrol.base.BaseFragment;
import org.namelessrom.devicecontrol.modules.controls.sub.IoSchedConfigFragment;
import org.namelessrom.devicecontrol.preferences.AutoListPreference;

import alexander.martinz.libs.hardware.io.IoScheduler;
import alexander.martinz.libs.materialpreferences.MaterialPreference;

// TODO: add support for multiple block devices
public class FileSystemFragment extends BaseFragment implements View.OnClickListener, IoScheduler.IoSchedulerListener, MaterialPreference.MaterialPreferenceChangeListener {
    private AutoListPreference mIoScheduler;
    private MaterialPreference mIoSchedConfig;

    @Nullable @Override public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.prefs_controls_filesystem, container, false);

        mIoScheduler = (AutoListPreference) v.findViewById(R.id.cat_fs_io_scheduler);
        mIoScheduler.setPath(IoScheduler.IO_SCHEDULER_PATH[0]);
        mIoScheduler.setOnPreferenceChangeListener(this);
        mIoScheduler.setEnabled(false);

        mIoSchedConfig = (MaterialPreference) v.findViewById(R.id.cat_fs_io_config);
        mIoSchedConfig.setOnClickListener(this);
        mIoSchedConfig.setEnabled(false);

        IoScheduler.getIoScheduler(this);

        return v;
    }

    @Override public void onIoScheduler(final IoScheduler ioScheduler) {
        final Activity activity = getActivity();
        if (activity != null && ioScheduler != null) {
            if (ioScheduler.available != null && ioScheduler.available.length > 0 && !TextUtils.isEmpty(ioScheduler.current)) {
                mIoScheduler.setAdapter(mIoScheduler.createAdapter(ioScheduler.available, ioScheduler.available));
                mIoScheduler.setValue(ioScheduler.current);
                mIoScheduler.setEnabled(true);

                mIoSchedConfig.setSummary(getString(R.string.io_scheduler_config_summary, ioScheduler.current));
                mIoSchedConfig.setEnabled(true);
            }
        }
    }

    @Override public void onClick(View v) {
        final int id = v.getId();
        switch (id) {
            case R.id.cat_fs_io_config: {
                ((BaseActivity) getActivity()).replaceFragment(new IoSchedConfigFragment(), "", false);
                break;
            }
        }
    }

    @Override public boolean onPreferenceChanged(MaterialPreference preference, Object newValue) {
        if (preference == mIoScheduler) {
            mIoScheduler.setEnabled(false);
            mIoSchedConfig.setEnabled(false);

            mIoScheduler.writeValue(String.valueOf(newValue));

            IoScheduler.getIoScheduler(this);
        }
        return false;
    }
}
