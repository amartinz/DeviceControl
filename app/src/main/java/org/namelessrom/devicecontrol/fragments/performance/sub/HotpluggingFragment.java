package org.namelessrom.devicecontrol.fragments.performance.sub;

import android.app.Activity;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceGroup;
import android.preference.PreferenceScreen;
import android.view.MenuItem;
import android.view.View;

import com.squareup.otto.Subscribe;

import org.namelessrom.devicecontrol.R;
import org.namelessrom.devicecontrol.activities.MainActivity;
import org.namelessrom.devicecontrol.database.DataItem;
import org.namelessrom.devicecontrol.database.DatabaseHandler;
import org.namelessrom.devicecontrol.events.SectionAttachedEvent;
import org.namelessrom.devicecontrol.events.ShellOutputEvent;
import org.namelessrom.devicecontrol.preferences.CustomCheckBoxPreference;
import org.namelessrom.devicecontrol.providers.BusProvider;
import org.namelessrom.devicecontrol.utils.CpuUtils;
import org.namelessrom.devicecontrol.utils.PreferenceHelper;
import org.namelessrom.devicecontrol.utils.Utils;
import org.namelessrom.devicecontrol.utils.constants.DeviceConstants;
import org.namelessrom.devicecontrol.utils.constants.FileConstants;
import org.namelessrom.devicecontrol.utils.constants.PerformanceConstants;
import org.namelessrom.devicecontrol.widgets.AttachPreferenceFragment;

public class HotpluggingFragment extends AttachPreferenceFragment
        implements DeviceConstants, FileConstants, PerformanceConstants,
        Preference.OnPreferenceChangeListener {

    //----------------------------------------------------------------------------------------------
    private static final int ID_MPDECISION = 200;
    //----------------------------------------------------------------------------------------------
    private PreferenceScreen         mRoot;
    //----------------------------------------------------------------------------------------------
    private CustomCheckBoxPreference mMpDecision;
    private CustomCheckBoxPreference mIntelliPlug;
    private CustomCheckBoxPreference mIntelliPlugEco;

    @Override
    public void onAttach(final Activity activity) {
        super.onAttach(activity, ID_HOTPLUGGING);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        BusProvider.getBus().post(new SectionAttachedEvent(ID_RESTORE_FROM_SUB));
    }

    @Override
    public void onResume() {
        super.onResume();
        BusProvider.getBus().register(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        BusProvider.getBus().unregister(this);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (MainActivity.mSlidingMenu != null && MainActivity.mSlidingMenu.isMenuShowing()) {
            MainActivity.mSlidingMenu.toggle(true);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.extras_hotplugging);
        setHasOptionsMenu(true);

        mRoot = getPreferenceScreen();

        //------------------------------------------------------------------------------------------
        // General
        //------------------------------------------------------------------------------------------
        mMpDecision = (CustomCheckBoxPreference) findPreference("mpdecision");
        if (mMpDecision != null) {
            if (Utils.fileExists(MPDECISION_PATH)) {
                Utils.getCommandResult(ID_MPDECISION, "pgrep mpdecision 2> /dev/null;");
            } else {
                mRoot.removePreference(mMpDecision);
            }
        }

        //------------------------------------------------------------------------------------------
        // Intelli-Plug
        //------------------------------------------------------------------------------------------
        PreferenceCategory category = (PreferenceCategory) findPreference(GROUP_INTELLI_PLUG);
        if (category != null) {
            mIntelliPlug = (CustomCheckBoxPreference) findPreference(KEY_INTELLI_PLUG);
            if (mIntelliPlug != null) {
                if (CpuUtils.hasIntelliPlug()) {
                    mIntelliPlug.setChecked(CpuUtils.getIntelliPlugActive());
                    mIntelliPlug.setOnPreferenceChangeListener(this);
                } else {
                    category.removePreference(mIntelliPlug);
                }
            }

            mIntelliPlugEco = (CustomCheckBoxPreference) findPreference(KEY_INTELLI_PLUG_ECO);
            if (mIntelliPlugEco != null) {
                if (CpuUtils.hasIntelliPlug() && CpuUtils.hasIntelliPlugEcoMode()) {
                    mIntelliPlugEco.setChecked(CpuUtils.getIntelliPlugEcoMode());
                    mIntelliPlugEco.setOnPreferenceChangeListener(this);
                } else {
                    category.removePreference(mIntelliPlugEco);
                }
            }
        }

        removeIfEmpty(category);
    }

    private void removeIfEmpty(final PreferenceGroup preferenceGroup) {
        if (mRoot != null && preferenceGroup.getPreferenceCount() == 0) {
            mRoot.removePreference(preferenceGroup);
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object o) {
        boolean changed = false;

        if (preference == mMpDecision) {
            final boolean value = (Boolean) o;
            Utils.runRootCommand(CpuUtils.enableMpDecision(value));
            PreferenceHelper.setBootup(new DataItem(
                    DatabaseHandler.CATEGORY_EXTRAS, mMpDecision.getKey(),
                    MPDECISION_PATH, value ? "1" : "0"));
            changed = true;
        } else if (preference == mIntelliPlug) {
            final boolean value = (Boolean) o;
            CpuUtils.enableIntelliPlug(value);
            PreferenceHelper.setBootup(new DataItem(
                    DatabaseHandler.CATEGORY_EXTRAS, mIntelliPlug.getKey(),
                    INTELLI_PLUG_PATH, value ? "1" : "0"));
            changed = true;
        } else if (preference == mIntelliPlugEco) {
            final boolean value = (Boolean) o;
            CpuUtils.enableIntelliPlugEcoMode(value);
            PreferenceHelper.setBootup(new DataItem(
                    DatabaseHandler.CATEGORY_EXTRAS, mIntelliPlugEco.getKey(),
                    INTELLI_PLUG_ECO_MODE_PATH, value ? "1" : "0"));
            changed = true;
        }

        return changed;
    }

    @Subscribe
    public void onGetCommandResult(final ShellOutputEvent event) {
        if (event != null) {
            final int id = event.getId();
            final String result = event.getOutput();
            switch (id) {
                case ID_MPDECISION:
                    if (mMpDecision != null) {
                        mMpDecision.setChecked(!result.isEmpty());
                        mMpDecision.setOnPreferenceChangeListener(this);
                    }
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        final int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                final Activity activity = getActivity();
                if (activity != null) {
                    activity.onBackPressed();
                }
                return true;
            default:
                break;
        }

        return false;
    }

}


