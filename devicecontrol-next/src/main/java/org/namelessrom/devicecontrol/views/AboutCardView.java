/*
 * Copyright (C) 2013 - 2015 Alexander Martinz
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
 */

package org.namelessrom.devicecontrol.views;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import android.text.Spannable;
import android.text.TextPaint;
import android.text.style.URLSpan;
import android.text.util.Linkify;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.android.volley.toolbox.NetworkImageView;

import org.namelessrom.devicecontrol.Application;
import org.namelessrom.devicecontrol.R;
import org.namelessrom.devicecontrol.utils.AppHelper;

public class AboutCardView extends FrameLayout {
    private static final String URL_GRAVATAR = "https://secure.gravatar.com/avatar/46b7972df351d022d2b6ae7865f7d4d5?s=500";
    private static final String URL_GITHUB = "https://github.com/Evisceration";

    private NetworkImageView mNetworkImageView;

    public AboutCardView(Context context) {
        super(context);
        init(context);
    }

    public AboutCardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public AboutCardView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public AboutCardView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    private void init(Context context) {
        final LayoutInflater inflater = LayoutInflater.from(context);
        inflater.inflate(R.layout.view_about_card, this, true);

        mNetworkImageView = (NetworkImageView) findViewById(R.id.about_card_image);
        if (!isInEditMode()) {
            final Application application = (Application) context.getApplicationContext();
            mNetworkImageView.setImageUrl(URL_GRAVATAR, application.getImageLoader());
        }

        final TextView email = (TextView) findViewById(R.id.about_card_email);
        email.setText(context.getString(R.string.about_contact_email, context.getString(R.string.email)));
        Linkify.addLinks(email, Linkify.EMAIL_ADDRESSES);
        stripUnderlines(email);

        final TextView phone = (TextView) findViewById(R.id.about_card_phone);
        phone.setText(context.getString(R.string.about_contact_phone, context.getString(R.string.phone)));
        Linkify.addLinks(phone, Linkify.PHONE_NUMBERS);
        stripUnderlines(phone);
    }

    public void setupAboutImageClickListener(@NonNull final Activity activity) {
        mNetworkImageView.setOnClickListener(new OnClickListener() {
            @Override public void onClick(View v) {
                AppHelper.launchUrlViaTabs(activity, URL_GITHUB);
            }
        });
    }

    private void stripUnderlines(@NonNull final TextView textView) {
        final Spannable s = (Spannable) textView.getText();
        final URLSpan[] spans = s.getSpans(0, s.length(), URLSpan.class);
        for (URLSpan span : spans) {
            final int start = s.getSpanStart(span);
            final int end = s.getSpanEnd(span);
            s.removeSpan(span);
            span = new URLSpanNoUnderline(span.getURL());
            s.setSpan(span, start, end, 0);
        }
        textView.setText(s);
    }

    private class URLSpanNoUnderline extends URLSpan {
        public URLSpanNoUnderline(String url) {
            super(url);
        }

        @Override public void updateDrawState(@NonNull TextPaint ds) {
            super.updateDrawState(ds);
            ds.setUnderlineText(false);
        }
    }
}
