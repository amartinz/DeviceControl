package org.namelessrom.devicecontrol.widgets.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.stericson.roottools.RootTools;
import com.stericson.roottools.execution.CommandCapture;

import org.namelessrom.devicecontrol.Application;
import org.namelessrom.devicecontrol.R;
import org.namelessrom.devicecontrol.events.ShellOutputEvent;
import org.namelessrom.devicecontrol.objects.AppItem;
import org.namelessrom.devicecontrol.utils.SortHelper;
import org.namelessrom.devicecontrol.utils.providers.BusProvider;

import java.util.ArrayList;
import java.util.List;

public class AppListAdapter extends BaseAdapter {

    private final Context       mContext;
    private       List<AppItem> mAppList;

    public AppListAdapter(final Context context) {
        mContext = context;
        loadItems();
    }

    private void loadItems() {
        List<AppItem> tmpList = new ArrayList<AppItem>();
        final PackageManager packageManager = Application.applicationContext.getPackageManager();

        if (packageManager != null) {
            final List<ApplicationInfo> appInfos = packageManager.getInstalledApplications(0);

            Drawable icon;
            String label;
            for (final ApplicationInfo appInfo : appInfos) {
                icon = appInfo.loadIcon(packageManager);
                label = String.valueOf(appInfo.loadLabel(packageManager));
                tmpList.add(new AppItem(icon, label, appInfo));
            }
        }

        mAppList = SortHelper.sortApplications(tmpList);
    }

    @Override
    public void notifyDataSetChanged() {
        loadItems();
        super.notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mAppList.size();
    }

    @Override
    public Object getItem(int position) {
        return mAppList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    private static class ViewHolder {
        private ImageView appIcon;
        private TextView  appLabel;
        private TextView  packageName;
        private View      layer;
    }

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {

        final ViewHolder viewHolder;

        if (convertView == null) {
            convertView = ((LayoutInflater) Application.applicationContext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE))
                    .inflate(R.layout.item_app, parent, false);
            viewHolder = new ViewHolder();

            viewHolder.appIcon = (ImageView) convertView.findViewById(R.id.app_icon);
            viewHolder.appLabel = (TextView) convertView.findViewById(R.id.app_label);
            viewHolder.packageName = (TextView) convertView.findViewById(R.id.app_package);
            viewHolder.layer = convertView.findViewById(R.id.app_layer);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        final AppItem appItem = mAppList.get(position);
        viewHolder.appIcon.setImageDrawable(appItem.getIcon());
        viewHolder.appLabel.setText(appItem.getLabel());
        viewHolder.packageName.setText(appItem.getPackageName());

        final Resources res = Application.applicationContext.getResources();
        if (appItem.isSystemApp()) {
            viewHolder.appLabel.setTextColor(res.getColor(R.color.red_middle));
        } else {
            viewHolder.appLabel.setTextColor(res.getColor(android.R.color.white));
        }

        if (appItem.isEnabled()) {
            viewHolder.layer.setVisibility(View.INVISIBLE);
        } else {
            viewHolder.layer.setVisibility(View.VISIBLE);
        }

        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog(appItem);
            }
        });

        return convertView;
    }

    private Button mDisabler;

    private void showDialog(final AppItem appItem) {
        if (appItem == null) return;

        final View v = ((LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE))
                .inflate(R.layout.dialog_app, null, false);

        final TextView status = (TextView) v.findViewById(R.id.app_status);
        resetAppStatus(status, appItem);

        mDisabler = (Button) v.findViewById(R.id.app_disabler);
        if (appItem.getPackageName().contains("org.namelessrom")) {
            mDisabler.setEnabled(false);
        }
        mDisabler.setText(appItem.isEnabled() ? R.string.disable : R.string.enable);
        mDisabler.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showConfirmationDialog(appItem);
            }
        });

        final AlertDialog.Builder builder = new AlertDialog.Builder(mContext);

        builder.setIcon(appItem.getIcon());
        builder.setTitle(appItem.getLabel());
        builder.setView(v);

        builder.setNeutralButton(R.string.close, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builder.setCancelable(false);
        builder.show();
    }

    private void showConfirmationDialog(final AppItem appItem) {
        if (appItem == null) return;

        final AlertDialog.Builder builder = new AlertDialog.Builder(mContext);

        builder.setMessage(mContext.getString(appItem.isEnabled()
                ? R.string.disable_msg : R.string.enable_msg, appItem.getLabel()));

        builder.setPositiveButton(appItem.isEnabled() ? R.string.disable : R.string.enable,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        disable(appItem);
                    }
                }
        );

        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builder.show();
    }

    private void disable(final AppItem appItem) {
        if (appItem == null) return;

        if (mDisabler != null) {
            mDisabler.setEnabled(false);
        }

        String cmd;
        if (appItem.isEnabled()) {
            cmd = "pm disable " + appItem.getPackageName() + " 2> /dev/null";
        } else {
            cmd = "pm enable " + appItem.getPackageName() + " 2> /dev/null";
        }

        final CommandCapture commandCapture = new CommandCapture(new DisableHandler(appItem), cmd) {
            @Override
            public void commandCompleted(int id, int exitcode) {
                super.commandCompleted(id, exitcode);
            }

            @Override
            public void commandTerminated(int id, String reason) {
                BusProvider.getBus().post(new ShellOutputEvent(-1, "", ""));
            }
        };

        try {
            RootTools.getShell(true).add(commandCapture);
        } catch (Exception ignored) { /* ignored */ }
    }

    private void resetAppStatus(final TextView tv, final AppItem appItem) {
        tv.setText(Html.fromHtml(mContext.getString(appItem.isSystemApp()
                ? R.string.app_system : R.string.app_user, appItem.getLabel())));
    }

    private class DisableHandler extends Handler {
        private static final int COMMAND_OUTPUT     = 0x01;
        private static final int COMMAND_COMPLETED  = 0x02;
        private static final int COMMAND_TERMINATED = 0x03;

        private final AppItem appItem;

        public DisableHandler(final AppItem appItem) {
            this.appItem = appItem;
        }

        @Override
        public void handleMessage(final Message msg) {
            final Bundle data = msg.getData();
            final int action;
            if (data != null) {
                action = msg.getData().getInt("action");
            } else {
                action = 0x00;
            }
            switch (action) {
                case COMMAND_COMPLETED:
                case COMMAND_TERMINATED:
                    if (mDisabler != null) {
                        appItem.setEnabled(!appItem.isEnabled());
                        mDisabler.setEnabled(true);
                        mDisabler.setText(appItem.isEnabled()
                                ? R.string.disable : R.string.enable);
                    }
                    notifyDataSetChanged();
                    break;
                default:
                case COMMAND_OUTPUT:
                    break;
            }
        }
    }

}
