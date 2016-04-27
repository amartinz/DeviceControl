/*
 *  Copyright (C) 2013 - 2014 Alexander "Evisceration" Martinz
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
package org.namelessrom.devicecontrol.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.SwitchCompat;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.anjlab.android.iab.v3.BillingProcessor;
import com.anjlab.android.iab.v3.TransactionDetails;

import org.namelessrom.devicecontrol.BuildConfig;
import org.namelessrom.devicecontrol.Constants;
import org.namelessrom.devicecontrol.DeviceConstants;
import org.namelessrom.devicecontrol.R;
import org.namelessrom.devicecontrol.utils.AppHelper;

import butterknife.BindView;
import butterknife.ButterKnife;

public class DonationActivity extends BaseActivity implements BillingProcessor.IBillingHandler,
        View.OnClickListener, RadioGroup.OnCheckedChangeListener {

    @BindView(R.id.bDonateGooglePlay) Button mGooglePlay;
    @BindView(R.id.radioGroupDonation) RadioGroup mRadioGroup;

    private BillingProcessor mBillingProcessor;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_donate);
        ButterKnife.bind(this);

        setupToolbar();

        // get instance of the BillingProcessor
        mBillingProcessor = new BillingProcessor(this, BuildConfig.API_KEY_GOOGLE_PLAY, this);

        // set up the buttons
        if (!TextUtils.isEmpty(BuildConfig.API_KEY_GOOGLE_PLAY) && !TextUtils.equals("---", BuildConfig.API_KEY_GOOGLE_PLAY)) {
            findViewById(R.id.card_donate_google_play).setVisibility(View.VISIBLE);
            mGooglePlay.setText(getString(R.string.donate_via, getString(R.string.google_play)));
            mGooglePlay.setOnClickListener(this);

            // set up radio buttons
            mRadioGroup.setOnCheckedChangeListener(this);
            final String donateValue = getString(R.string.donate_value);
            ((RadioButton) findViewById(R.id.radioDonation1)).setText(String.format(donateValue, "2€"));
            ((RadioButton) findViewById(R.id.radioDonation2)).setText(String.format(donateValue, "5€"));
            ((RadioButton) findViewById(R.id.radioDonation3)).setText(String.format(donateValue, "10€"));
            ((RadioButton) findViewById(R.id.radioDonation4)).setText(String.format(donateValue, "20€"));
            ((RadioButton) findViewById(R.id.radioDonation5)).setText(String.format(donateValue, "50€"));
        } else {
            findViewById(R.id.card_donate_google_play).setVisibility(View.GONE);
        }

        final Button flattr = (Button) findViewById(R.id.bDonateFlattr);
        flattr.setText(getString(R.string.donate_via, getString(R.string.flattr)));
        flattr.setOnClickListener(this);
        final TextView flattrText = (TextView) findViewById(R.id.tvDonateFlattr);
        flattrText.setText(getString(R.string.donate_message, getString(R.string.flattr)));

        final Button payPal = (Button) findViewById(R.id.bDonatePayPal);
        payPal.setText(getString(R.string.donate_via, getString(R.string.paypal)));
        payPal.setOnClickListener(this);
        final TextView payPalText = (TextView) findViewById(R.id.tvDonatePayPal);
        payPalText.setText(getString(R.string.donate_message, getString(R.string.paypal)));
    }

    @Override public void onCheckedChanged(RadioGroup group, int checkedId) {
        mGooglePlay.setEnabled(checkedId != -1);
    }

    @Override public void onClick(View v) {
        final int id = v.getId();
        switch (id) {
            case R.id.bDonateGooglePlay:
                final SwitchCompat cbSub = (SwitchCompat) findViewById(R.id.cbDonationSubscription);
                final boolean useSub = cbSub != null && cbSub.isChecked();

                final int radioButtonId = mRadioGroup.getCheckedRadioButtonId();
                final String productId;
                switch (radioButtonId) {
                    default:
                    case R.id.radioDonation1:
                        productId = useSub ? DeviceConstants.SUB_DONATION_1 : DeviceConstants.SKU_DONATION_1;
                        break;
                    case R.id.radioDonation2:
                        productId = useSub ? DeviceConstants.SUB_DONATION_2 : DeviceConstants.SKU_DONATION_2;
                        break;
                    case R.id.radioDonation3:
                        productId = useSub ? DeviceConstants.SUB_DONATION_3 : DeviceConstants.SKU_DONATION_3;
                        break;
                    case R.id.radioDonation4:
                        productId = useSub ? DeviceConstants.SUB_DONATION_4 : DeviceConstants.SKU_DONATION_4;
                        break;
                    case R.id.radioDonation5:
                        productId = useSub ? DeviceConstants.SUB_DONATION_5 : DeviceConstants.SKU_DONATION_5;
                        break;
                }
                if (useSub) {
                    mBillingProcessor.subscribe(this, productId);
                } else {
                    mBillingProcessor.purchase(this, productId);
                }
                break;
            case R.id.bDonateFlattr:
                AppHelper.viewInBrowser(this, Constants.URL_DONATE_FLATTR);
                break;
            case R.id.bDonatePayPal:
                AppHelper.viewInBrowser(this, Constants.URL_DONATE_PAYPAL);
                break;
        }
    }

    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (mBillingProcessor == null || !mBillingProcessor.handleActivityResult(requestCode, resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override protected void onDestroy() {
        if (mBillingProcessor != null) {
            mBillingProcessor.release();
            mBillingProcessor = null;
        }
        super.onDestroy();
    }

    @Override public void onProductPurchased(String productId, TransactionDetails details) {
        if (details != null && details.productId.contains("donation_sub_")) {
            // we can not consume subscriptions
            return;
        }
        // Consume asap purchased to allow multiple donations
        mBillingProcessor.consumePurchase(productId);
    }

    @Override public void onPurchaseHistoryRestored() {
        // TODO: you know what ;P
    }

    @Override public void onBillingError(int errorCode, Throwable error) {
        // TODO: you know what ;P
    }

    @Override public void onBillingInitialized() {
        // TODO: you know what ;P
    }

}
