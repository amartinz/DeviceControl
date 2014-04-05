package org.namelessrom.devicecontrol.fragments.performance;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.text.InputType;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager.LayoutParams;
import android.widget.EditText;
import android.widget.LinearLayout;

import org.namelessrom.devicecontrol.R;
import org.namelessrom.devicecontrol.database.DataItem;
import org.namelessrom.devicecontrol.database.DatabaseHandler;
import org.namelessrom.devicecontrol.preferences.CustomPreference;
import org.namelessrom.devicecontrol.utils.CpuUtils;
import org.namelessrom.devicecontrol.utils.Utils;
import org.namelessrom.devicecontrol.widgets.AttachPreferenceFragment;

import java.io.File;
import java.util.List;

public class CpuGovernorFragment extends AttachPreferenceFragment {

    private static PreferenceCategory mCategory;
    private static Context            mContext;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.governor);

        setHasOptionsMenu(true);

        final PreferenceScreen preferenceScreen = getPreferenceScreen();
        mCategory = (PreferenceCategory) findPreference("key_gov_category");
        mContext = getActivity();

        final String curGov = CpuUtils.getValue(0, CpuUtils.ACTION_GOV);

        final File f = new File("/sys/devices/system/cpu/cpufreq/" + curGov);
        if (f.exists()) {
            mCategory.setTitle(curGov + " Tweakable values");
            new addPreferences().execute(curGov);
        } else {
            preferenceScreen.removeAll();
        }

        isSupported(preferenceScreen, mContext, R.string.no_gov_tweaks_message);
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

    class addPreferences extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... params) {
            if (mCategory.getPreferenceCount() != 0) {
                mCategory.removeAll();
            }
            final String currentGovernor = params[0];
            final File f = new File("/sys/devices/system/cpu/cpufreq/" + currentGovernor);
            if (f.exists()) {
                final File[] files = f.listFiles();
                for (final File file : files) {
                    final String fileName = file.getName();

                    // Do not try to read boostpulse
                    if ("boostpulse".equals(fileName)) {
                        continue;
                    }

                    final String filePath = file.getAbsolutePath();
                    final String fileContent = Utils.readOneLine(filePath).trim()
                            .replaceAll("\n", "");
                    final CustomPreference pref = new CustomPreference(mContext);
                    pref.setTitle(fileName);
                    pref.setSummary(fileContent);
                    pref.setKey(filePath);
                    mCategory.addPreference(pref);
                    pref.setOnPreferenceClickListener(new OnPreferenceClickListener() {

                        @Override
                        public boolean onPreferenceClick(final Preference p) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                            LinearLayout ll = new LinearLayout(mContext);
                            ll.setLayoutParams(new LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.WRAP_CONTENT,
                                    LinearLayout.LayoutParams.MATCH_PARENT));
                            final EditText et = new EditText(mContext);
                            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.MATCH_PARENT,
                                    LinearLayout.LayoutParams.WRAP_CONTENT);
                            params.setMargins(40, 40, 40, 40);
                            params.gravity = Gravity.CENTER;
                            String val = p.getSummary().toString();
                            et.setLayoutParams(params);
                            et.setRawInputType(InputType.TYPE_CLASS_NUMBER);
                            et.setGravity(Gravity.CENTER_HORIZONTAL);
                            et.setText(val);
                            ll.addView(et);
                            builder.setView(ll);
                            builder.setPositiveButton(android.R.string.ok,
                                    new DialogInterface.OnClickListener() {

                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            String value = et.getText().toString();
                                            p.setSummary(value);
                                            Utils.writeValue(p.getKey(), value);
                                            updateBootupListDb(p, value);
                                        }
                                    }
                            );
                            AlertDialog dialog = builder.create();
                            dialog.show();
                            Window window = dialog.getWindow();
                            window.setLayout(800, LayoutParams.WRAP_CONTENT);
                            return true;
                        }

                    });
                }
            }

            return null;
        }

    }

    private static void updateBootupListDb(final Preference p, final String value) {

        class updateListDb extends AsyncTask<String, Void, Void> {

            @Override
            protected Void doInBackground(String... params) {
                final DatabaseHandler db = DatabaseHandler.getInstance(mContext);
                final String name = p.getTitle().toString();
                final String key = p.getKey();

                final List<DataItem> items =
                        db.getAllItems(DatabaseHandler.TABLE_BOOTUP, DataItem.CATEGORY_CPU);
                for (final DataItem item : items) {
                    if (item.getName().equals(name)
                            && item.getCategory().equals(DataItem.CATEGORY_CPU)) {
                        db.deleteItemByName(name, DatabaseHandler.TABLE_BOOTUP);
                    }
                }
                db.addItem(new DataItem(DataItem.CATEGORY_CPU, name, value, key),
                        DatabaseHandler.TABLE_BOOTUP);

                return null;
            }

        }
        new updateListDb().execute();
    }

}
