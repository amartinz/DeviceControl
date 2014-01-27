/*
 * Copyright (C) 2014 Alexander "Evisceration" Martinz
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses
 */
package org.namelessrom.devicecontrol.fragments.dialogs;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;

import org.namelessrom.devicecontrol.R;

public class HelpDialog extends Fragment {

    public static final String ARG_TYPE = "arg_type";

    public static HelpDialog newInstance(int typeId) {
        HelpDialog f = new HelpDialog();

        Bundle b = new Bundle();
        b.putInt(HelpDialog.ARG_TYPE, typeId);
        f.setArguments(b);

        return f;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_fragment, container, false);

        final int typeId = getArguments().getInt(HelpDialog.ARG_TYPE);
        String url;
        switch (typeId) {
            default:
            case 0: // About
                url = "file:///android_asset/notice.html";
                break;
            case 1: // Licenses
                url = "file:///android_asset/notice.html";
                break;
            case 2: // Help
                url = "file:///android_asset/notice.html";
                break;
        }

        WebView wv = (WebView) view.findViewById(R.id.dialog_help_webview);
        wv.getSettings().setTextSize(WebSettings.TextSize.SMALLER);
        wv.loadUrl(url);

        return view;
    }
}
