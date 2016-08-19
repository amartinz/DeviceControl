/*
**
** Copyright 2007, The Android Open Source Project
**
** Licensed under the Apache License, Version 2.0 (the "License");
** you may not use this file except in compliance with the License.
** You may obtain a copy of the License at
**
**     http://www.apache.org/licenses/LICENSE-2.0
**
** Unless required by applicable law or agreed to in writing, software
** distributed under the License is distributed on an "AS IS" BASIS,
** WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
** See the License for the specific language governing permissions and
** limitations under the License.
*/
package org.namelessrom.devicecontrol.modules.appmanager.permissions;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.PermissionGroupInfo;
import android.content.pm.PermissionInfo;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import org.namelessrom.devicecontrol.R;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import timber.log.Timber;

/**
 * This class contains the SecurityPermissions view implementation.
 * Initially the package's advanced or dangerous security permissions
 * are displayed under categorized
 * groups. Clicking on the additional permissions presents
 * extended information consisting of all groups and permissions.
 * To use this view define a LinearLayout or any ViewGroup and add this
 * view by instantiating AppSecurityPermissions and invoking getPermissionsView.
 * <p/>
 */
@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
public class AppSecurityPermissions {
    public static final int WHICH_PERSONAL = 1 << 0;
    public static final int WHICH_DEVICE = 1 << 1;
    public static final int WHICH_NEW = 1 << 2;
    public static final int WHICH_ALL = 0xffff;

    private final Context mContext;
    private final LayoutInflater mInflater;
    private final PackageManager mPm;
    private final Map<String, MyPermissionGroupInfo> mPermGroups = new HashMap<>();
    private final List<MyPermissionGroupInfo> mPermGroupsList = new ArrayList<>();
    private final PermissionGroupInfoComparator mPermGroupComparator =
            new PermissionGroupInfoComparator();
    private final PermissionInfoComparator mPermComparator = new PermissionInfoComparator();

    public static class MyPermissionGroupInfo extends PermissionGroupInfo {
        CharSequence mLabel;

        final ArrayList<MyPermissionInfo> mNewPermissions = new ArrayList<>();
        final ArrayList<MyPermissionInfo> mPersonalPermissions = new ArrayList<>();
        final ArrayList<MyPermissionInfo> mDevicePermissions = new ArrayList<>();
        final ArrayList<MyPermissionInfo> mAllPermissions = new ArrayList<>();

        MyPermissionGroupInfo(PermissionInfo perm) {
            name = perm.packageName;
            packageName = perm.packageName;
        }

        MyPermissionGroupInfo(PermissionGroupInfo info) {
            super(info);
        }

        public Drawable loadGroupIcon(PackageManager pm) {
            if (icon != 0) {
                return loadIcon(pm);
            } else {
                ApplicationInfo appInfo;
                try {
                    appInfo = pm.getApplicationInfo(packageName, 0);
                    return appInfo.loadIcon(pm);
                } catch (NameNotFoundException ignored) { }
            }
            return null;
        }
    }

    public static class MyPermissionInfo extends PermissionInfo {
        CharSequence mLabel;

        /**
         * PackageInfo.requestedPermissionsFlags for the new package being installed.
         */
        int mNewReqFlags;

        /**
         * PackageInfo.requestedPermissionsFlags for the currently installed
         * package, if it is installed.
         */
        int mExistingReqFlags;

        /**
         * True if this should be considered a new permission.
         */
        boolean mNew;

        MyPermissionInfo(PermissionInfo info) {
            super(info);
        }
    }

    private AppSecurityPermissions(Context context) {
        mContext = context;
        mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mPm = mContext.getPackageManager();
    }

    public AppSecurityPermissions(Context context, String packageName) {
        this(context);
        Set<MyPermissionInfo> permSet = new HashSet<>();
        PackageInfo pkgInfo;
        try {
            pkgInfo = mPm.getPackageInfo(packageName, PackageManager.GET_PERMISSIONS);
        } catch (RuntimeException | NameNotFoundException e) {
            Timber.w("Couldn't retrieve permissions for package: %s", packageName);
            return;
        }
        // Extract all user permissions
        if ((pkgInfo.applicationInfo != null) && (pkgInfo.applicationInfo.uid != -1)) {
            getAllUsedPermissions(pkgInfo.applicationInfo.uid, permSet);
        }
        final ArrayList<MyPermissionInfo> permsList = new ArrayList<>();
        permsList.addAll(permSet);
        setPermissions(permsList);
    }

    private void getAllUsedPermissions(int sharedUid, Set<MyPermissionInfo> permSet) {
        final String sharedPkgList[];
        try {
            sharedPkgList = mPm.getPackagesForUid(sharedUid);
        } catch (RuntimeException rex) {
            // we are screwed
            return;
        }
        if (sharedPkgList == null || (sharedPkgList.length == 0)) {
            return;
        }
        for (String sharedPkg : sharedPkgList) {
            getPermissionsForPackage(sharedPkg, permSet);
        }
    }

    private void getPermissionsForPackage(String packageName, Set<MyPermissionInfo> permSet) {
        try {
            PackageInfo pkgInfo = mPm.getPackageInfo(packageName, PackageManager.GET_PERMISSIONS);
            extractPerms(pkgInfo, permSet, pkgInfo);
        } catch (RuntimeException |NameNotFoundException e) {
            Timber.w("Couldn't retrieve permissions for package: %s", packageName);
        }
    }

    private void extractPerms(PackageInfo info, Set<MyPermissionInfo> permSet,
            PackageInfo installedPkgInfo) {
        String[] strList = info.requestedPermissions;
        int[] flagsList = info.requestedPermissionsFlags;
        if ((strList == null) || (strList.length == 0)) {
            return;
        }
        for (int i = 0; i < strList.length; i++) {
            String permName = strList[i];
            // If we are only looking at an existing app, then we only
            // care about permissions that have actually been granted to it.
            if (installedPkgInfo != null && info == installedPkgInfo) {
                if ((flagsList[i] & PackageInfo.REQUESTED_PERMISSION_GRANTED) == 0) {
                    continue;
                }
            }
            try {
                PermissionInfo tmpPermInfo = mPm.getPermissionInfo(permName, 0);
                if (tmpPermInfo == null) {
                    continue;
                }
                int existingIndex = -1;
                if (installedPkgInfo != null && installedPkgInfo.requestedPermissions != null) {
                    for (int j = 0; j < installedPkgInfo.requestedPermissions.length; j++) {
                        if (permName.equals(installedPkgInfo.requestedPermissions[j])) {
                            existingIndex = j;
                            break;
                        }
                    }
                }
                final int existingFlags = installedPkgInfo != null && existingIndex >= 0 ?
                        installedPkgInfo.requestedPermissionsFlags[existingIndex] : 0;
                if (!isDisplayablePermission(tmpPermInfo, flagsList[i], existingFlags)) {
                    // This is not a permission that is interesting for the user
                    // to see, so skip it.
                    continue;
                }
                final String origGroupName = tmpPermInfo.group;
                String groupName = origGroupName;
                if (groupName == null) {
                    groupName = tmpPermInfo.packageName;
                    tmpPermInfo.group = groupName;
                }
                MyPermissionGroupInfo group = mPermGroups.get(groupName);
                if (group == null) {
                    PermissionGroupInfo grp = null;
                    if (origGroupName != null) {
                        grp = mPm.getPermissionGroupInfo(origGroupName, 0);
                    }
                    if (grp != null) {
                        group = new MyPermissionGroupInfo(grp);
                    } else {
                        // We could be here either because the permission
                        // didn't originally specify a group or the group it
                        // gave couldn't be found.  In either case, we consider
                        // its group to be the permission's package name.
                        tmpPermInfo.group = tmpPermInfo.packageName;
                        group = new MyPermissionGroupInfo(tmpPermInfo);
                    }
                    mPermGroups.put(tmpPermInfo.group, group);
                }
                final boolean newPerm = installedPkgInfo != null
                                        && (existingFlags & PackageInfo.REQUESTED_PERMISSION_GRANTED) == 0;
                MyPermissionInfo myPerm = new MyPermissionInfo(tmpPermInfo);
                myPerm.mNewReqFlags = flagsList[i];
                myPerm.mExistingReqFlags = existingFlags;
                // This is a new permission if the app is already installed and
                // doesn't currently hold this permission.
                myPerm.mNew = newPerm;
                permSet.add(myPerm);
            } catch (NameNotFoundException e) {
                Timber.i("Ignoring unknown permission: %s", permName);
            }
        }
    }

    public int getPermissionCount() {
        return getPermissionCount(WHICH_ALL);
    }

    private List<MyPermissionInfo> getPermissionList(MyPermissionGroupInfo grp, int which) {
        if (which == WHICH_NEW) {
            return grp.mNewPermissions;
        } else if (which == WHICH_PERSONAL) {
            return grp.mPersonalPermissions;
        } else if (which == WHICH_DEVICE) {
            return grp.mDevicePermissions;
        } else {
            return grp.mAllPermissions;
        }
    }

    public int getPermissionCount(int which) {
        int N = 0;
        for (int i = 0; i < mPermGroupsList.size(); i++) {
            N += getPermissionList(mPermGroupsList.get(i), which).size();
        }
        return N;
    }

    public View getPermissionsView() {
        LinearLayout permsView = (LinearLayout) mInflater.inflate(R.layout.app_perms_summary, null, false);
        LinearLayout displayList = (LinearLayout) permsView.findViewById(R.id.perms_list);
        View noPermsView = permsView.findViewById(R.id.no_permissions);

        displayPermissions(mPermGroupsList, displayList);
        if (displayList.getChildCount() > 0) {
            noPermsView.setVisibility(View.GONE);
        }

        return permsView;
    }

    /**
     * Utility method that displays permissions from a map containing group name and
     * list of permission descriptions.
     */
    private void displayPermissions(List<MyPermissionGroupInfo> groups, LinearLayout permListView) {
        permListView.removeAllViews();

        int spacing = (int) (8 * mContext.getResources().getDisplayMetrics().density);

        for (int i = 0; i < groups.size(); i++) {
            MyPermissionGroupInfo grp = groups.get(i);
            final List<MyPermissionInfo> perms = getPermissionList(grp, WHICH_ALL);
            for (int j = 0; j < perms.size(); j++) {
                MyPermissionInfo perm = perms.get(j);
                View view = getPermissionItemView(grp, perm, j == 0);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);
                if (j == 0) {
                    lp.topMargin = spacing;
                }
                if (j == grp.mAllPermissions.size() - 1) {
                    lp.bottomMargin = spacing;
                }
                if (permListView.getChildCount() == 0) {
                    lp.topMargin *= 2;
                }
                permListView.addView(view, lp);
            }
        }
    }

    private PermissionItemView getPermissionItemView(MyPermissionGroupInfo grp,
            MyPermissionInfo perm, boolean first) {
        final boolean costsMoney = (perm.flags & PermissionInfo.FLAG_COSTS_MONEY) != 0;
        PermissionItemView permView = (PermissionItemView) mInflater.inflate(
                costsMoney ? R.layout.app_permission_item_money : R.layout.app_permission_item,
                null, false);
        permView.setPermission(grp, perm, first);
        return permView;
    }

    private boolean isDisplayablePermission(PermissionInfo pInfo, int newReqFlags,
            int existingReqFlags) {
        final int base = pInfo.protectionLevel & PermissionInfo.PROTECTION_MASK_BASE;
        final boolean isNormal = (base == PermissionInfo.PROTECTION_NORMAL);
        final boolean isDangerous = (base == PermissionInfo.PROTECTION_DANGEROUS);
        // TODO: recheck
        //final boolean isRequired =
        //        ((newReqFlags & PackageInfo.REQUESTED_PERMISSION_REQUIRED) != 0);
        final boolean isDevelopment =
                ((pInfo.protectionLevel & PermissionInfo.PROTECTION_FLAG_DEVELOPMENT) != 0);
        final boolean wasGranted =
                ((existingReqFlags & PackageInfo.REQUESTED_PERMISSION_GRANTED) != 0);
        final boolean isGranted =
                ((newReqFlags & PackageInfo.REQUESTED_PERMISSION_GRANTED) != 0);

        // Dangerous and normal permissions are always shown to the user if the permission
        // is required, or it was previously granted
        if ((isNormal || isDangerous) && (/*isRequired || */wasGranted || isGranted)) {
            return true;
        }

        // Development permissions are only shown to the user if they are already
        // granted to the app -- if we are installing an app and they are not
        // already granted, they will not be granted as part of the install.
        return isDevelopment && wasGranted;
    }

    private static class PermissionGroupInfoComparator implements Comparator<MyPermissionGroupInfo> {
        private final Collator sCollator = Collator.getInstance();

        PermissionGroupInfoComparator() {
        }

        public final int compare(MyPermissionGroupInfo a, MyPermissionGroupInfo b) {
            if (((a.flags ^ b.flags) & PermissionGroupInfo.FLAG_PERSONAL_INFO) != 0) {
                return ((a.flags & PermissionGroupInfo.FLAG_PERSONAL_INFO) != 0) ? -1 : 1;
            }
            if (a.priority != b.priority) {
                return a.priority > b.priority ? -1 : 1;
            }
            return sCollator.compare(a.mLabel, b.mLabel);
        }
    }

    private static class PermissionInfoComparator implements Comparator<MyPermissionInfo> {
        private final Collator sCollator = Collator.getInstance();

        PermissionInfoComparator() {
        }

        public final int compare(MyPermissionInfo a, MyPermissionInfo b) {
            return sCollator.compare(a.mLabel, b.mLabel);
        }
    }

    private void addPermToList(List<MyPermissionInfo> permList,
            MyPermissionInfo pInfo) {
        if (pInfo.mLabel == null) {
            pInfo.mLabel = pInfo.loadLabel(mPm);
        }
        int idx = Collections.binarySearch(permList, pInfo, mPermComparator);
        if (idx < 0) {
            idx = -idx - 1;
            permList.add(idx, pInfo);
        }
    }

    private void setPermissions(List<MyPermissionInfo> permList) {
        if (permList != null) {
            // First pass to group permissions
            for (MyPermissionInfo pInfo : permList) {
                if (!isDisplayablePermission(pInfo, pInfo.mNewReqFlags, pInfo.mExistingReqFlags)) {
                    continue;
                }
                MyPermissionGroupInfo group = mPermGroups.get(pInfo.group);
                if (group != null) {
                    pInfo.mLabel = pInfo.loadLabel(mPm);
                    addPermToList(group.mAllPermissions, pInfo);
                    if (pInfo.mNew) {
                        addPermToList(group.mNewPermissions, pInfo);
                    }
                    if ((group.flags & PermissionGroupInfo.FLAG_PERSONAL_INFO) != 0) {
                        addPermToList(group.mPersonalPermissions, pInfo);
                    } else {
                        addPermToList(group.mDevicePermissions, pInfo);
                    }
                }
            }
        }

        for (MyPermissionGroupInfo pgrp : mPermGroups.values()) {
            if (pgrp.labelRes != 0 || pgrp.nonLocalizedLabel != null) {
                pgrp.mLabel = pgrp.loadLabel(mPm);
            } else {
                ApplicationInfo app;
                try {
                    app = mPm.getApplicationInfo(pgrp.packageName, 0);
                    pgrp.mLabel = app.loadLabel(mPm);
                    if (pgrp.mLabel == null) {
                        throw new Exception("app.loadLabel returned null!");
                    }
                } catch (Exception e) {
                    pgrp.mLabel = pgrp.loadLabel(mPm);
                }
            }
            if (pgrp.mLabel == null) {
                continue;
            }
            mPermGroupsList.add(pgrp);
        }
        Collections.sort(mPermGroupsList, mPermGroupComparator);
    }
}
