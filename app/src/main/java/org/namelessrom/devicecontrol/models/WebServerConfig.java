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

package org.namelessrom.devicecontrol.models;

import io.paperdb.Paper;
import io.paperdb.PaperDbException;
import timber.log.Timber;

/**
 * Web server configuration which auto serializes itself to a file
 */
public class WebServerConfig {
    private transient static final String NAME = "WebServerConfig";

    public transient static final String ROOT = "wfm_root";
    public transient static final String PORT = "wfm_port";

    public transient static final String USE_AUTH = "wfm_auth";
    public transient static final String USERNAME = "wfm_username";
    public transient static final String PASSWORD = "wfm_password";

    public boolean root;
    public int port = 8080;

    public boolean useAuth = true;
    public String username = "root";
    public String password = "toor";

    private transient static WebServerConfig instance;

    private WebServerConfig() { }

    public static WebServerConfig get() {
        if (instance == null) {
            final WebServerConfig config = new WebServerConfig();
            try {
                instance = Paper.book().read(NAME, config);
            } catch (PaperDbException pde) {
                instance = config;
                Timber.e(pde, "Could not read %s", NAME);
            }
        }
        return instance;
    }

    public WebServerConfig save() {
        try {
            Paper.book().write(NAME, WebServerConfig.this);
        } catch (PaperDbException pde) {
            Timber.e(pde, "Could not write %s", NAME);
        }
        return this;
    }

}
