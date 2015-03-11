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

package org.namelessrom.devicecontrol.configuration;

import android.content.Context;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import org.namelessrom.devicecontrol.Logger;
import org.namelessrom.devicecontrol.utils.Utils;

import java.io.File;

public abstract class BaseConfiguration<T> {

    protected abstract String getConfigurationFile();

    protected abstract boolean migrateFromDatabase(Context context);

    public abstract T loadConfiguration(Context context);

    public abstract T saveConfiguration(Context context);

    protected void saveConfigurationInternal(Context context) {
        final String base = context.getApplicationContext().getFilesDir().getAbsolutePath();
        final File configurationFile = new File(base, getConfigurationFile());

        Logger.d(this, "deleting configuration before writing -> %s", configurationFile.delete());
        final String config = new Gson().toJson(this);
        Logger.d(this, "config -> %s", config);
        Utils.writeToFile(configurationFile, config);
    }

    @Nullable protected T loadRawConfiguration(Context context, Class<T> clazz) {
        final String base = context.getApplicationContext().getFilesDir().getAbsolutePath();
        final File configurationFile = new File(base, getConfigurationFile());

        if (!configurationFile.exists()) {
            Logger.w(this, "config does not exist, return");
            return null;
        }

        final String config = Utils.readFile(configurationFile.getAbsolutePath());
        Logger.d(this, "config -> %s", config);
        if (TextUtils.isEmpty(config)) {
            Logger.w(this, "config is empty or null, return");
            return null;
        }

        try {
            return new Gson().fromJson(config, clazz);
        } catch (JsonSyntaxException jse) {
            Logger.e(this, "could not convert file into configuration", jse);
            return null;
        }
    }

}
