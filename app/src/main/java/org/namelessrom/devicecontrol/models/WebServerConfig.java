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

/**
 * Web server configuration which auto serializes itself to a file
 */
public class WebServerConfig {
    protected transient static final String NAME = "WebServerConfig";

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

    public static WebServerConfig get() {
        return Paper.get(NAME, new WebServerConfig());
    }

    public WebServerConfig save() {
        Paper.put(NAME, WebServerConfig.this);
        return this;
    }

}
