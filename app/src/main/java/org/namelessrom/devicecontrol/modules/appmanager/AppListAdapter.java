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

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.kennyc.bottomsheet.BottomSheet;
import com.kennyc.bottomsheet.BottomSheetListener;

import org.namelessrom.devicecontrol.R;
import org.namelessrom.devicecontrol.utils.AppHelper;
import org.namelessrom.devicecontrol.views.CardView;

import java.util.ArrayList;
import java.util.Iterator;

public class AppListAdapter extends RecyclerView.Adapter<AppListAdapter.ViewHolder> {
    private final Activity mActivity;
    private final BaseAppListFragment mBaseAppListFragment;
    private ArrayList<AppItem> mAppList;
    private ArrayList<AppItem> mFiltered;

    private final AppItem.UninstallListener mUninstallListener;
    private final BaseAppListFragment.AppSelectedListener mAppSelectedListener;

    private final ArrayList<AppItem> mSelectedApps;

    public AppListAdapter(Activity activity, BaseAppListFragment baseAppListFragment, ArrayList<AppItem> appList,
            AppItem.UninstallListener uninstallListener, BaseAppListFragment.AppSelectedListener appSelectedListener) {
        mActivity = activity;
        mBaseAppListFragment = baseAppListFragment;
        mFiltered = appList;
        mUninstallListener = uninstallListener;
        mAppSelectedListener = appSelectedListener;

        // save original items
        mAppList = new ArrayList<>(mFiltered);

        mSelectedApps = new ArrayList<>();
    }

    public void refill(ArrayList<AppItem> appItems) {
        mFiltered = appItems;
        mAppList.clear();
        mAppList.addAll(mFiltered);
        mSelectedApps.clear();

        if (mAppSelectedListener != null) {
            mAppSelectedListener.onAppSelected("refreshing", mSelectedApps);
        }

        notifyDataSetChanged();
    }

    public final class ViewHolder extends RecyclerView.ViewHolder implements CompoundButton.OnCheckedChangeListener, View.OnLongClickListener {
        private final CardView cardView;
        private final LinearLayout container;

        private final AppIconImageView appIcon;
        private final TextView appLabel;
        private final TextView packageName;
        private final TextView appVersion;

        private final CheckBox appSelector;

        private AppItem appItem;

        public ViewHolder(final View v) {
            super(v);
            cardView = (CardView) v.findViewById(R.id.card_view);

            appIcon = (AppIconImageView) v.findViewById(R.id.app_icon);
            appLabel = (TextView) v.findViewById(R.id.app_label);
            packageName = (TextView) v.findViewById(R.id.app_package);
            appVersion = (TextView) v.findViewById(R.id.app_version);

            appSelector = (CheckBox) v.findViewById(R.id.app_selector);
            appSelector.setOnCheckedChangeListener(this);

            container = (LinearLayout) v.findViewById(R.id.app_details_container);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                container.setOnTouchListener(new View.OnTouchListener() {
                    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
                    @Override public boolean onTouch(View v, MotionEvent event) {
                        v.getBackground().setHotspot(event.getX(), event.getY());
                        return false;
                    }
                });
            }
            container.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    if (mSelectedApps.size() != 0) {
                        appSelector.toggle();
                        return;
                    }
                    final Intent intent = new Intent(mActivity, AppDetailsActivity.class);
                    intent.putExtra(AppDetailsActivity.ARG_FROM_ACTIVITY, true);
                    intent.putExtra(AppDetailsActivity.ARG_PACKAGE_NAME, appItem.getPackageName());
                    mActivity.startActivity(intent);
                }
            });
            container.setOnLongClickListener(this);
        }

        public void bind(final AppItem appItem) {
            this.appItem = appItem;

            // do not load the image here, we load it in onViewAttachedToWindow()
            // appIcon.loadImage(appItem, null);

            appLabel.setText(appItem.getLabel());
            packageName.setText(appItem.getPackageName());
            appVersion.setText(appItem.getVersion());

            int color = ContextCompat.getColor(mActivity, R.color.graph_text_color);
            appLabel.setTextColor(appItem.isSystemApp() ? ContextCompat.getColor(mActivity, R.color.red_middle) : color);

            color = appItem.isEnabled() ? android.R.color.transparent : R.color.darker_gray;
            cardView.setForeground(new ColorDrawable(ContextCompat.getColor(mActivity, color)));

            appSelector.setChecked(mSelectedApps.contains(appItem));
        }

        @Override public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            final String packageName = appItem.getPackageName();
            if (isChecked) {
                mSelectedApps.add(appItem);
            } else {
                mSelectedApps.remove(appItem);
            }

            if (mAppSelectedListener != null) {
                mAppSelectedListener.onAppSelected(packageName, mSelectedApps);
            }
        }

        @Override public boolean onLongClick(View v) {
            final BottomSheet.Builder builder = new BottomSheet.Builder(mActivity, R.style.AppManagerBottomSheetStyle);
            builder.setTitle(appItem.getLabel()).setListener(appBottomSheetListener);

            final PopupMenu popupMenu = new PopupMenu(mActivity, null);
            popupMenu.inflate(R.menu.sheet_app_item);

            final Menu menu = popupMenu.getMenu();
            if (appItem.isEnabled()) {
                menu.removeItem(R.id.sheet_enable);
                if (!appItem.isRunning(mActivity)) {
                    menu.removeItem(R.id.sheet_force_stop);
                }
            } else {
                menu.removeItem(R.id.sheet_disable);
                menu.removeItem(R.id.sheet_force_stop);
                menu.removeItem(R.id.sheet_open);
            }

            builder.setMenu(menu).show();
            return true;
        }

        private final BottomSheetListener appBottomSheetListener = new BottomSheetListener() {
            @Override public void onSheetShown(@NonNull BottomSheet bottomSheet) { }

            @Override public void onSheetItemSelected(@NonNull BottomSheet bottomSheet, MenuItem menuItem) {
                final int id = menuItem.getItemId();
                switch (id) {
                    case R.id.sheet_open: {
                        final boolean success = appItem.launchActivity(mActivity);
                        if (!success) {
                            Snackbar.make(container, R.string.could_not_launch_activity, Snackbar.LENGTH_SHORT).show();
                        }
                        break;
                    }
                    case R.id.sheet_force_stop: {
                        AppHelper.killProcess(mActivity, appItem.getPackageName());
                        Snackbar.make(container, R.string.force_stopped_app, Snackbar.LENGTH_SHORT).show();
                        break;
                    }
                    case R.id.sheet_enable: {
                        appItem.enable(mDisableEnableListener);
                        break;
                    }
                    case R.id.sheet_disable: {
                        appItem.disable(mDisableEnableListener);
                        break;
                    }
                    case R.id.sheet_clear_cache: {
                        appItem.clearCache(mActivity);
                        Snackbar.make(container, R.string.cleared_cache, Snackbar.LENGTH_SHORT).show();
                        break;
                    }
                    case R.id.sheet_clear_data: {
                        appItem.clearData(mActivity);
                        Snackbar.make(container, R.string.cleared_data, Snackbar.LENGTH_SHORT).show();
                        break;
                    }
                    case R.id.sheet_uninstall: {
                        final String message;
                        if (appItem.isSystemApp()) {
                            final String label = mActivity.getString(R.string.uninstall_msg, appItem.getLabel());
                            message = String.format("%s\n\n%s", label, mActivity.getString(R.string.uninstall_msg_system_app));
                        } else {
                            message = mActivity.getString(R.string.uninstall_msg, appItem.getLabel());
                        }

                        new AlertDialog.Builder(mActivity)
                                .setMessage(message)
                                .setNegativeButton(android.R.string.cancel,
                                        new DialogInterface.OnClickListener() {
                                            @Override public void onClick(DialogInterface dialog, int which) {
                                                dialog.dismiss();
                                            }
                                        })
                                .setPositiveButton(android.R.string.yes,
                                        new DialogInterface.OnClickListener() {
                                            @Override public void onClick(DialogInterface dialog, int which) {
                                                appItem.uninstall(mActivity, mUninstallListener);
                                            }
                                        })
                                .show();
                        break;
                    }
                }
            }

            @Override public void onSheetDismissed(@NonNull BottomSheet bottomSheet, @DismissEvent int i) { }
        };
    }

    private final AppItem.DisableEnableListener mDisableEnableListener = new AppItem.DisableEnableListener() {
        @Override public void OnDisabledOrEnabled() {
            if (mBaseAppListFragment != null) {
                mBaseAppListFragment.loadApps(true);
            }
        }
    };

    @Override public int getItemCount() { return mFiltered.size(); }

    @Override public void onViewAttachedToWindow(ViewHolder holder) {
        super.onViewAttachedToWindow(holder);
        holder.appIcon.loadImage(holder.appItem);
    }

    @Override public ViewHolder onCreateViewHolder(final ViewGroup parent, final int type) {
        final CardView card = (CardView) LayoutInflater.from(parent.getContext()).inflate(R.layout.card_app_item, parent, false);
        return new ViewHolder(card);
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
            final Iterator<AppItem> iterator = mFiltered.iterator();
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
