/*
 * Copyright (C) 2012-2013 Jorrit "Chainfire" Jongma
 * Modifications Copyright (C) 2013 Alexander "Evisceration" Martinz
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package eu.chainfire.libsuperuser;

import android.os.AsyncTask;

import org.namelessrom.devicecontrol.R;

public class Application extends android.app.Application {

    // Switch to your needs
    public static final boolean IS_LOG_DEBUG = true;

    public static boolean IS_SYSTEM_APP = false;
    public static boolean HAS_ROOT = false;

    @Override
    public void onCreate() {
        super.onCreate();

        IS_SYSTEM_APP = getResources().getBoolean(R.bool.is_system_app);
        if (!IS_SYSTEM_APP) {
            new DetectSu().execute();
        }

        try {
            // workaround bug in AsyncTask, can show up (for example) when you toast from a service
            // this makes sure AsyncTask's internal handler is created from the right (main) thread
            Class.forName("android.os.AsyncTask");
        } catch (ClassNotFoundException e) {
        }
    }

    private class DetectSu extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            HAS_ROOT = Shell.SU.available();
            return null;
        }
    }
}
