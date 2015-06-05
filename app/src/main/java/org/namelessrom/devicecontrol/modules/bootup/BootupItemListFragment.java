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
import android.view.View;

import org.namelessrom.devicecontrol.R;
import org.namelessrom.devicecontrol.configuration.BootupConfiguration;
import org.namelessrom.devicecontrol.configuration.ConfigConstants;
import org.namelessrom.devicecontrol.hardware.VoltageUtils;
import org.namelessrom.devicecontrol.objects.BootupItem;
import org.namelessrom.devicecontrol.ui.preferences.CustomPreferenceCategoryMaterial;

import java.util.ArrayList;

import alexander.martinz.libs.materialpreferences.MaterialPreference;
import alexander.martinz.libs.materialpreferences.MaterialSupportPreferenceFragment;

public class BootupItemListFragment extends MaterialSupportPreferenceFragment implements MaterialPreference.MaterialPreferenceChangeListener {

    @Override public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final Activity activity = getActivity();
        final BootupConfiguration configuration = BootupConfiguration.get(activity);
        ArrayList<BootupItem> items;

        // CPU
        items = configuration.getItemsByCategory(ConfigConstants.CATEGORY_CPU);
        addCategory(activity, getString(R.string.cpu), items);

        // GPU
        items = configuration.getItemsByCategory(ConfigConstants.CATEGORY_GPU);
        addCategory(activity, getString(R.string.gpu), items);

        // Device
        items = configuration.getItemsByCategory(ConfigConstants.CATEGORY_DEVICE);
        addCategory(activity, getString(R.string.device), items);

        // SysCtl
        items = configuration.getItemsByCategory(ConfigConstants.CATEGORY_SYSCTL);
        addCategory(activity, getString(R.string.sysctl_vm), items);

        // Extras
        items = configuration.getItemsByCategory(ConfigConstants.CATEGORY_EXTRAS);
        addCategory(activity, getString(R.string.extras), items);

        // Voltage
        if (VoltageUtils.isSupported()) {
            items = configuration.getItemsByCategory(ConfigConstants.CATEGORY_VOLTAGE);
            addCategory(activity, getString(R.string.voltage), items);
        }
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
            BootupConfiguration configuration = BootupConfiguration.get(getActivity());
            BootupItem item = preference.getBootupItem();
            item.enabled = (Boolean) o;
            configuration.addItem(item);
            configuration.saveConfiguration(getActivity());
            return true;
        }
    }
}
