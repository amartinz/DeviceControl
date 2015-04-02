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
package org.namelessrom.devicecontrol.modules.appmanager;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.namelessrom.devicecontrol.Application;
import org.namelessrom.devicecontrol.R;
import org.namelessrom.devicecontrol.objects.AppItem;

import java.util.ArrayList;
import java.util.Iterator;

public class AppListAdapter extends RecyclerView.Adapter<AppListAdapter.ViewHolder> {
    private final Resources res = Application.get().getResources();

    /* package */ final Activity mActivity;
    private final ArrayList<AppItem> mAppList;
    private ArrayList<AppItem> mFiltered;

    private final AppItem.UninstallListener mListener;

    private Toast mToast;

    public AppListAdapter(final Activity activity, final ArrayList<AppItem> appList,
            final AppItem.UninstallListener listener) {
        mActivity = activity;
        mFiltered = appList;
        mListener = listener;

        // save original items
        mAppList = new ArrayList<>(mFiltered);
    }

    public final class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private final View rootView;
        private final View container;

        private final ImageView actionOpen;
        private final ImageView actionUninstall;

        private final ImageView appIcon;
        private final TextView appLabel;
        private final TextView packageName;
        private final TextView appVersion;

        private AppItem mAppItem;

        public ViewHolder(final View v) {
            super(v);
            rootView = v;

            actionOpen = (ImageView) v.findViewById(R.id.app_open);
            actionOpen.setOnClickListener(this);
            actionUninstall = (ImageView) v.findViewById(R.id.app_uninstall);
            actionUninstall.setOnClickListener(this);

            container = v.findViewById(R.id.app_details_container);
            appIcon = (ImageView) v.findViewById(R.id.app_icon);
            appLabel = (TextView) v.findViewById(R.id.app_label);
            packageName = (TextView) v.findViewById(R.id.app_package);
            appVersion = (TextView) v.findViewById(R.id.app_version);

            rootView.setOnClickListener(this);
        }

        public void bind(final AppItem appItem) {
            mAppItem = appItem;

            appIcon.setImageDrawable(appItem.getIcon());
            appLabel.setText(appItem.getLabel());
            packageName.setText(appItem.getPackageName());
            appVersion.setText(appItem.getVersion());

            final int color = Application.get().isDarkTheme() ? Color.WHITE : Color.BLACK;
            appLabel.setTextColor(appItem.isSystemApp()
                    ? res.getColor(R.color.red_middle) : color);
            container.setBackgroundResource(appItem.isEnabled()
                    ? android.R.color.transparent : R.color.darker_gray);

            actionOpen.setVisibility(appItem.isEnabled() ? View.VISIBLE : View.GONE);
        }

        @Override public void onClick(View v) {
            final int id = v.getId();
            switch (id) {
                case R.id.app_open: {
                    final Toast toast = mAppItem.launchActivity(mActivity);
                    if (toast == null) {
                        break;
                    }

                    if (mToast != null) {
                        mToast.cancel();
                    }
                    mToast = toast;
                    mToast.show();
                    break;
                }
                case R.id.app_uninstall: {
                    final String message;
                    if (mAppItem.isSystemApp()) {
                        message = String.format("%s\n\n%s",
                                mActivity.getString(R.string.uninstall_msg, mAppItem.getLabel()),
                                mActivity.getString(R.string.uninstall_msg_system_app));
                    } else {
                        message = mActivity.getString(R.string.uninstall_msg, mAppItem.getLabel());
                    }

                    new AlertDialog.Builder(mActivity)
                            .setMessage(message)
                            .setNegativeButton(android.R.string.cancel,
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                        }
                                    })
                            .setPositiveButton(android.R.string.yes,
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            mAppItem.uninstall(mActivity, mListener);
                                        }
                                    })
                            .show();
                    break;
                }
                default: {
                    final Intent intent = new Intent(mActivity, AppDetailsActivity.class);
                    intent.putExtra(AppDetailsActivity.ARG_FROM_ACTIVITY, true);
                    intent.putExtra(AppDetailsActivity.ARG_PACKAGE_NAME, mAppItem.getPackageName());
                    mActivity.startActivity(intent);
                    break;
                }
            }
        }
    }

    @Override public int getItemCount() { return mFiltered.size(); }

    @Override public ViewHolder onCreateViewHolder(final ViewGroup parent, final int type) {
        final int resId;
        if (Application.get().isDarkTheme()) {
            resId = R.layout.card_app_item_dark;
        } else {
            resId = R.layout.card_app_item_light;
        }
        final View view = LayoutInflater.from(parent.getContext()).inflate(resId, parent, false);
        return new ViewHolder(view);
    }

    @Override public void onBindViewHolder(final ViewHolder viewHolder, final int position) {
        final AppItem appItem = mFiltered.get(position);
        viewHolder.bind(appItem);
    }

    public void filter(String query) {
        mFiltered.clear();
        mFiltered.addAll(mAppList);

        query = (query != null ? query.trim().toLowerCase() : null);
        if (!TextUtils.isEmpty(query)) {
            Iterator<AppItem> iterator = mFiltered.iterator();
            while (iterator.hasNext()) {
                AppItem appItem = iterator.next();
                if (!appItem.getLabel().toLowerCase().contains(query)) {
                    iterator.remove();
                }
            }
        }

        notifyDataSetChanged();
    }

}
