package org.namelessrom.devicecontrol.events;

public class DonationStartedEvent {

    public static final String SKU_DONATION_BASE = "sku.donation.";

    private static final String SKU_DONATION_0        = "sku.donation.0";
    private static final String SKU_DONATION_1        = "sku.donation.1";
    private static final String SKU_DONATION_2        = "sku.donation.2";
    private static final String SKU_DONATION_3        = "sku.donation.3";
    private static final String SKU_DONATION_4        = "sku.donation.4";
    private static final String SKU_DONATION_5        = "sku.donation.5";
    private static final String TOKEN_DONATION        = "token_donation";
    private static final int    DONATION_REQUEST_CODE = 13371337;

    private final String mSku;
    private final int    mReqCode;
    private final String mToken;

    public DonationStartedEvent(final String sku) {
        this(sku, DONATION_REQUEST_CODE, TOKEN_DONATION);
    }

    public DonationStartedEvent(final String sku, final int reqCode) {
        this(sku, reqCode, TOKEN_DONATION);
    }

    public DonationStartedEvent(final String sku, final int reqCode, final String token) {
        mSku = sku;
        mReqCode = reqCode;
        mToken = token;
    }

    public String getSku() { return mSku; }

    public int getReqCode() { return mReqCode; }

    public String getToken() { return mToken; }
}
