package org.namelessrom.devicecontrol.modules.controls.sub;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.namelessrom.devicecontrol.Constants;
import org.namelessrom.devicecontrol.R;
import org.namelessrom.devicecontrol.base.BaseFragment;
import org.namelessrom.devicecontrol.preferences.AutoEditTextPreference;

import java.util.List;

import alexander.martinz.libs.hardware.utils.IoUtils;
import alexander.martinz.libs.materialpreferences.MaterialPreference;
import alexander.martinz.libs.materialpreferences.MaterialPreferenceCategory;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

// TODO: add support for multiple block devices
public class IoSchedConfigFragment extends BaseFragment {
    private MaterialPreferenceCategory mIoConfigCategory;

    @Nullable @Override public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.prefs_controls_filesystem_ioconfig, container, false);

        mIoConfigCategory = (MaterialPreferenceCategory) v.findViewById(R.id.cat_fs_io_config);

        return v;
    }

    @Override public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Observable.create(new Observable.OnSubscribe<List<String>>() {
            @Override public void call(final Subscriber<? super List<String>> subscriber) {
                if (subscriber.isUnsubscribed()) {
                    return;
                }

                final List<String> listedFiles = IoUtils.listFiles(Constants.IO_SCHED_CONFIG_DIR);
                subscriber.onNext(listedFiles);

                if (!subscriber.isUnsubscribed()) {
                    subscriber.onCompleted();
                }
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Action1<List<String>>() {
            @Override public void call(List<String> listedFiles) {
                final Context context = getContext();
                if (listedFiles.size() == 0) {
                    final MaterialPreference preference = new MaterialPreference(context);
                    preference.setTitle(getString(R.string.no_tweaks_available));
                    preference.setSummary(getString(R.string.no_io_tweaks_message));
                    mIoConfigCategory.addPreference(preference);
                    return;
                }
                for (final String file : listedFiles) {
                    final AutoEditTextPreference preference = new AutoEditTextPreference(context);
                    preference.init(context, null);
                    preference.setTitle(file);
                    preference.setPath(Constants.IO_SCHED_CONFIG_DIR + file);
                    mIoConfigCategory.addPreference(preference);
                    preference.initValue();
                    preference.handleSelf(true);
                }
            }
        });
    }
}
