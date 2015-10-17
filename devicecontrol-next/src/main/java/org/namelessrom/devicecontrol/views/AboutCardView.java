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
import android.content.Intent;
import android.net.Uri;
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

import org.namelessrom.devicecontrol.Constants;
import org.namelessrom.devicecontrol.R;
import org.namelessrom.devicecontrol.utils.AppHelper;

public class AboutCardView extends FrameLayout {

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

        final TextView email = (TextView) findViewById(R.id.about_card_email);
        email.setText(context.getString(R.string.about_contact_email, context.getString(R.string.email)));
        Linkify.addLinks(email, Linkify.EMAIL_ADDRESSES);
        stripUnderlines(email);

        final TextView phone = (TextView) findViewById(R.id.about_card_phone);
        phone.setText(context.getString(R.string.about_contact_phone, context.getString(R.string.phone)));
        Linkify.addLinks(phone, Linkify.PHONE_NUMBERS);
        stripUnderlines(phone);
    }

    public void setupWithActivity(@NonNull final Activity activity) {
        final OnClickListener onClickListener = new OnClickListener() {
            @Override public void onClick(View v) {
                final int id = v.getId();
                switch (id) {
                    case R.id.about_card_link_email: {
                        final Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
                        emailIntent.setData(Uri.fromParts("mailto", Constants.EMAIL, null));
                        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Device Control [root]");
                        emailIntent.putExtra(Intent.EXTRA_TEXT, "");
                        emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{ Constants.EMAIL });
                        activity.startActivity(Intent.createChooser(emailIntent, activity.getString(R.string.send_email)));
                        break;
                    }
                    case R.id.about_card_link_github: {
                        AppHelper.launchUrlViaTabs(activity, Constants.GITHUB_BASE_URL);
                        break;
                    }
                    case R.id.about_card_link_googleplus: {
                        AppHelper.launchUrlViaTabs(activity, Constants.GOOGLE_PLUS_URL);
                        break;
                    }
                    case R.id.about_card_link_linkedin: {
                        AppHelper.launchUrlViaTabs(activity, Constants.LINKEDIN_URL);
                        break;
                    }
                }
            }
        };

        findViewById(R.id.about_card_link_email).setOnClickListener(onClickListener);
        findViewById(R.id.about_card_link_github).setOnClickListener(onClickListener);
        findViewById(R.id.about_card_link_googleplus).setOnClickListener(onClickListener);
        findViewById(R.id.about_card_link_linkedin).setOnClickListener(onClickListener);
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
