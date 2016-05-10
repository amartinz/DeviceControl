/*
 *  Copyright (C) 2013 - 2016 Alexander "Evisceration" Martinz
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
package org.namelessrom.devicecontrol.modules.info.software;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Resources;
import android.os.Build;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.namelessrom.devicecontrol.R;
import org.namelessrom.devicecontrol.views.CardTitleView;

import at.amartinz.hardware.drm.BaseDrmInfo;
import butterknife.ButterKnife;
import hugo.weaving.DebugLog;

public class DrmView extends CardTitleView {
    private BaseDrmInfo drmInfo;

    private TextView supported;
    private LinearLayout extrasContainer;

    private TextView vendor;
    private TextView version;
    private TextView description;

    private TextView systemId;
    private TextView deviceId;

    private TextView algorithms;

    private TextView securityLevel;
    private TextView hdcpLevel;
    private TextView hdcpLevelMax;

    private TextView usageReporting;

    private TextView sessionsMax;
    private TextView sessionsOpen;

    public DrmView(Context context) {
        super(context);
    }

    public DrmView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public DrmView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.M)
    public DrmView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override protected void init(@Nullable AttributeSet attrs) {
        super.init(attrs);
        final FrameLayout content = getContentView();
        final View inflateView = LayoutInflater.from(getContext()).inflate(R.layout.view_drm_info, content, false);
        content.addView(inflateView);

        supported = ButterKnife.findById(content, R.id.drm_supported);
        extrasContainer = ButterKnife.findById(content, R.id.drm_container);
        extrasContainer.setVisibility(View.GONE);

        vendor = ButterKnife.findById(content, R.id.drm_vendor);
        version = ButterKnife.findById(content, R.id.drm_version);
        description = ButterKnife.findById(content, R.id.drm_description);

        systemId = ButterKnife.findById(content, R.id.drm_id_system);
        deviceId = ButterKnife.findById(content, R.id.drm_id_device);

        algorithms = ButterKnife.findById(content, R.id.drm_algorithms);

        securityLevel = ButterKnife.findById(content, R.id.drm_security_level);
        hdcpLevel = ButterKnife.findById(content, R.id.drm_hdcp_level);
        hdcpLevelMax = ButterKnife.findById(content, R.id.drm_hdcp_level_max);

        usageReporting = ButterKnife.findById(content, R.id.drm_supported_usage_reporting);

        sessionsMax = ButterKnife.findById(content, R.id.drm_session_number_max);
        sessionsOpen = ButterKnife.findById(content, R.id.drm_session_number_open);
    }

    @DebugLog public void setDrmInfo(@Nullable final BaseDrmInfo drmInfo) {
        this.drmInfo = drmInfo;
        refreshViews();
    }

    private void refreshViews() {
        final boolean supported = ((drmInfo != null) && drmInfo.isSupported());
        this.supported.setText(supported ? R.string.yes : R.string.no);
        if (!supported) {
            extrasContainer.setVisibility(View.GONE);
            return;
        }
        extrasContainer.setVisibility(View.VISIBLE);

        final Resources res = getResources();

        setValue(vendor, drmInfo.getVendor());
        setValue(version, drmInfo.getVersion());
        setValue(description, drmInfo.getDescription());
        setValue(systemId, drmInfo.getSystemId());
        setValue(deviceId, drmInfo.getDeviceId());
        setValue(algorithms, drmInfo.getAlgorithms());
        setValue(securityLevel, drmInfo.getSecurityLevel());
        setValue(hdcpLevel, drmInfo.getHdcpLevel());
        setValue(hdcpLevelMax, drmInfo.getHdcpLevelMax());
        setValue(usageReporting, drmInfo.isUsageReportingSupported() ? res.getString(R.string.yes) : res.getString(R.string.no));
        setValue(sessionsMax, drmInfo.getSessionNumberMax());
        setValue(sessionsOpen, drmInfo.getSessionNumberOpen());
    }

    private void setValue(TextView textView, String value) {
        if (TextUtils.isEmpty(value)) {
            textView.setText("-");
            return;
        }
        textView.setText(value);
    }

}
