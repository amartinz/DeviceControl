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
package org.namelessrom.devicecontrol.modules.home;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.anjlab.android.iab.v3.BillingProcessor;
import com.anjlab.android.iab.v3.TransactionDetails;

import org.namelessrom.devicecontrol.R;
import org.namelessrom.devicecontrol.base.BaseActivity;
import org.namelessrom.devicecontrol.utils.AppHelper;
import org.namelessrom.proprietary.Configuration;

public class DonationActivity extends BaseActivity implements BillingProcessor.IBillingHandler, View.OnClickListener, RadioGroup.OnCheckedChangeListener {
    /** Donation  2€ **/
    public static final String SKU_DONATION_1 = "donation_1";
    /** Donation  5€ **/
    public static final String SKU_DONATION_2 = "donation_2";
    /** Donation 10€ **/
    public static final String SKU_DONATION_3 = "donation_3";
    /** Donation 20€ **/
    public static final String SKU_DONATION_4 = "donation_4";
    /** Donation 50€ **/
    public static final String SKU_DONATION_5 = "donation_5";

    /** Donation, subscription  2€ **/
    public static final String SUB_DONATION_1 = "donation_sub_1";
    /** Donation, subscription  5€ **/
    public static final String SUB_DONATION_2 = "donation_sub_2";
    /** Donation, subscription 10€ **/
    public static final String SUB_DONATION_3 = "donation_sub_3";
    /** Donation, subscription 20€ **/
    public static final String SUB_DONATION_4 = "donation_sub_4";
    /** Donation, subscription 50€ **/
    public static final String SUB_DONATION_5 = "donation_sub_5";

    private Button mGooglePlay;
    private RadioGroup mRadioGroup;

    private BillingProcessor mBillingProcessor;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_donate);

        // get instance of the BillingProcessor
        mBillingProcessor = new BillingProcessor(this, Configuration.getGooglePlayApiKeyDc(), this);

        // setup action bar
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final ActionBar actionBar = getSupportActionBar();
        assert (actionBar != null);
        actionBar.setDisplayHomeAsUpEnabled(true);

        // set up the buttons
        mGooglePlay = (Button) findViewById(R.id.bDonateGooglePlay);
        mGooglePlay.setText(getString(R.string.donate_via, getString(R.string.google_play)));
        mGooglePlay.setOnClickListener(this);

        final Button payPal = (Button) findViewById(R.id.bDonatePayPal);
        payPal.setText(getString(R.string.donate_via, getString(R.string.paypal)));
        payPal.setOnClickListener(this);

        // set up radio buttons
        mRadioGroup = (RadioGroup) findViewById(R.id.radioGroupDonation);
        mRadioGroup.setOnCheckedChangeListener(this);
        final String donateValue = getString(R.string.donate_value);
        ((RadioButton) findViewById(R.id.radioDonation1)).setText(String.format(donateValue, "2€"));
        ((RadioButton) findViewById(R.id.radioDonation2)).setText(String.format(donateValue, "5€"));
        ((RadioButton) findViewById(R.id.radioDonation3)).setText(String.format(donateValue, "10€"));
        ((RadioButton) findViewById(R.id.radioDonation4)).setText(String.format(donateValue, "20€"));
        ((RadioButton) findViewById(R.id.radioDonation5)).setText(String.format(donateValue, "50€"));
    }

    @Override public boolean onOptionsItemSelected(MenuItem item) {
        final int id = item.getItemId();
        switch (id) {
            case android.R.id.home: {
                finish();
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override public void onCheckedChanged(RadioGroup group, int checkedId) {
        mGooglePlay.setEnabled(checkedId != -1);
    }

    @Override public void onClick(View v) {
        final int id = v.getId();
        switch (id) {
            case R.id.bDonateGooglePlay:
                final CheckBox cbSub = (CheckBox) findViewById(R.id.cbDonationSubscription);
                final boolean useSub = cbSub != null && cbSub.isChecked();

                final int radioButtonId = mRadioGroup.getCheckedRadioButtonId();
                final String productId;
                switch (radioButtonId) {
                    default:
                    case R.id.radioDonation1:
                        productId = useSub ? SUB_DONATION_1 : SKU_DONATION_1;
                        break;
                    case R.id.radioDonation2:
                        productId = useSub ? SUB_DONATION_2 : SKU_DONATION_2;
                        break;
                    case R.id.radioDonation3:
                        productId = useSub ? SUB_DONATION_3 : SKU_DONATION_3;
                        break;
                    case R.id.radioDonation4:
                        productId = useSub ? SUB_DONATION_4 : SKU_DONATION_4;
                        break;
                    case R.id.radioDonation5:
                        productId = useSub ? SUB_DONATION_5 : SKU_DONATION_5;
                        break;
                }
                if (useSub) {
                    mBillingProcessor.subscribe(this, productId);
                } else {
                    mBillingProcessor.purchase(this, productId);
                }
                break;
            case R.id.bDonatePayPal:
                AppHelper.startExternalDonation(this);
                break;
        }
    }

    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (mBillingProcessor == null ||
            !mBillingProcessor.handleActivityResult(requestCode, resultCode, data)) {
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
