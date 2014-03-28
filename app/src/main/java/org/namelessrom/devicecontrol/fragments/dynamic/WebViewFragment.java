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
package org.namelessrom.devicecontrol.fragments.dynamic;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;

import org.namelessrom.devicecontrol.R;
import org.namelessrom.devicecontrol.activities.MainActivity;
import org.namelessrom.devicecontrol.widgets.AttachFragment;

public class WebViewFragment extends AttachFragment {

    public static final String ARG_TYPE = "arg_type";
    public static final int    ID       = 9999;

    public static final int TYPE_ABOUT    = 0;
    public static final int TYPE_LICENSES = 1;
    public static final int TYPE_HELP     = 2;

    public static WebViewFragment newInstance(int typeId) {
        WebViewFragment f = new WebViewFragment();

        Bundle b = new Bundle();
        b.putInt(WebViewFragment.ARG_TYPE, typeId);
        f.setArguments(b);

        return f;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity, WebViewFragment.ID);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.dialog_fragment, container, false);

        final int typeId = getArguments().getInt(WebViewFragment.ARG_TYPE);
        String url;
        switch (typeId) {
            default:
            case TYPE_ABOUT: // About
                url = "file:///android_asset/notice.html";
                break;
            case TYPE_LICENSES: // Licenses
                url = "file:///android_asset/notice.html";
                break;
            case TYPE_HELP: // Help
                url = "file:///android_asset/notice.html";
                break;
        }

        final WebView wv = (WebView) view.findViewById(R.id.dialog_help_webview);
        wv.getSettings().setTextSize(WebSettings.TextSize.SMALLER);
        wv.loadUrl(url);

        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (MainActivity.mSlidingMenu != null && MainActivity.mSlidingMenu.isMenuShowing()) {
            MainActivity.mSlidingMenu.toggle(true);
        }
    }
}
