/*
 *  Copyright (C) 2013 - 2015 Alexander "Evisceration" Martinz
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
package org.namelessrom.devicecontrol.modules.bootup;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import org.namelessrom.devicecontrol.R;
import org.namelessrom.devicecontrol.models.BootupConfig;
import org.namelessrom.devicecontrol.hardware.VoltageUtils;
import org.namelessrom.devicecontrol.preferences.CustomPreferenceCategoryMaterial;
import org.namelessrom.devicecontrol.utils.Utils;

import java.util.ArrayList;

import alexander.martinz.libs.materialpreferences.MaterialPreference;
import alexander.martinz.libs.materialpreferences.MaterialSupportPreferenceFragment;

public class BootupItemListFragment extends MaterialSupportPreferenceFragment implements MaterialPreference.MaterialPreferenceChangeListener {

    @Override public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final Activity activity = getActivity();
        final BootupConfig configuration = BootupConfig.get();
        ArrayList<BootupItem> items;
        String path;

        // CPU
        items = configuration.getItemsByCategory(BootupConfig.CATEGORY_CPU);
        addCategory(activity, getString(R.string.cpu), items);

        // GPU
        items = configuration.getItemsByCategory(BootupConfig.CATEGORY_GPU);
        addCategory(activity, getString(R.string.gpu), items);

        // Device
        items = configuration.getItemsByCategory(BootupConfig.CATEGORY_DEVICE);
        addCategory(activity, getString(R.string.device), items);

        // SysCtl
        items = configuration.getItemsByCategory(BootupConfig.CATEGORY_SYSCTL);
        addCategory(activity, getString(R.string.sysctl_vm), items);

        // Voltage
        if (VoltageUtils.isSupported()) {
            items = configuration.getItemsByCategory(BootupConfig.CATEGORY_VOLTAGE);
            addCategory(activity, getString(R.string.voltage), items);
        }

        // intelli plug
        path = Utils.checkPaths(getResources().getStringArray(R.array.directories_intelli_plug));
        if (!TextUtils.isEmpty(path)) {
            items = configuration.getItemsByCategory(BootupConfig.CATEGORY_INTELLI_HOTPLUG);
            addCategory(activity, getString(R.string.intelli_plug), items);
        }

        // mako hotplug
        path = Utils.checkPath(getString(R.string.directory_mako_hotplug));
        if (!TextUtils.isEmpty(path)) {
            items = configuration.getItemsByCategory(BootupConfig.CATEGORY_MAKO_HOTPLUG);
            addCategory(activity, getString(R.string.mako_hotplug), items);
        }

        // Extras
        items = configuration.getItemsByCategory(BootupConfig.CATEGORY_EXTRAS);
        addCategory(activity, getString(R.string.extras), items);
    }

    private CustomPreferenceCategoryMaterial addCategory(Context context, String title,
            ArrayList<BootupItem> items) {
        CustomPreferenceCategoryMaterial category = new CustomPreferenceCategoryMaterial(context);
        category.init(context);
        category.setTitle(title);
        addPreference(category);

        if (items.size() != 0) {
            for (BootupItem item : items) {
                addPreference(context, category, item);
            }
        } else {
            addNoItemsPreference(context, category, getString(R.string.bootup_category_no_items));
        }

        return category;
    }

    private BootupItemPreference addPreference(Context context,
            CustomPreferenceCategoryMaterial category, BootupItem item) {
        BootupItemPreference preference = new BootupItemPreference(context);
        preference.init(context);
        preference.setBootupItem(context, item);
        category.addPreference(preference);
        preference.setOnPreferenceChangeListener(this);
        return preference;
    }

    private MaterialPreference addNoItemsPreference(Context context,
            CustomPreferenceCategoryMaterial category, String title) {
        MaterialPreference preference = new MaterialPreference(context);
        preference.init(context);
        preference.setTitle(title);
        category.addPreference(preference);
        return preference;
    }

    @Override public boolean onPreferenceChanged(MaterialPreference materialPreference, Object o) {
        BootupItemPreference preference = null;

        if (materialPreference instanceof BootupItemPreference) {
            preference = (BootupItemPreference) materialPreference;
        }

        if (preference == null) {
            return false;
        } else {
            BootupConfig configuration = BootupConfig.get();
            BootupItem item = preference.getBootupItem();
            item.enabled = (Boolean) o;
            configuration.addItem(item);
            configuration.save();
            return true;
        }
    }
}
