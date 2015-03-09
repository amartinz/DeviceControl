package org.namelessrom.devicecontrol.configuration;

import android.content.Context;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import org.namelessrom.devicecontrol.Logger;
import org.namelessrom.devicecontrol.utils.Utils;

import java.io.File;

public abstract class BaseConfiguration {

    protected abstract String getConfigurationFile();

    protected abstract boolean migrateFromDatabase(Context context);

    public abstract void loadConfiguration(Context context);

    public void saveConfiguration(Context context) {
        final String base = context.getApplicationContext().getFilesDir().getAbsolutePath();
        final File configurationFile = new File(base, getConfigurationFile());

        Logger.d(this, "deleting configuration before writing -> %s", configurationFile.delete());
        final String config = new Gson().toJson(this);
        Logger.d(this, "config -> %s", config);
        Utils.writeToFile(configurationFile, config);
    }

    @Nullable protected Object loadRawConfiguration(Context context, Class clazz) {
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
