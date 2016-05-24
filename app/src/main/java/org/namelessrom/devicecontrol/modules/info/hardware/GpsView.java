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
package org.namelessrom.devicecontrol.modules.info.hardware;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Location;
import android.os.Build;
import android.os.Vibrator;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.google.android.gms.location.LocationRequest;

import org.namelessrom.devicecontrol.App;
import org.namelessrom.devicecontrol.BuildConfig;
import org.namelessrom.devicecontrol.R;
import org.namelessrom.devicecontrol.activities.BaseActivity;
import org.namelessrom.devicecontrol.views.CardTitleView;

import java.util.ArrayList;
import java.util.List;

import pl.charmas.android.reactivelocation.ReactiveLocationProvider;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import timber.log.Timber;

public class GpsView extends CardTitleView {
    private Observable<String> addressObservable;
    private Subscription addressSubscription;

    private TextView statusView;
    private String unknownLocation;

    public GpsView(Context context) {
        super(context);
    }

    public GpsView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public GpsView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.M)
    public GpsView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override protected void init(@Nullable AttributeSet attrs) {
        super.init(attrs);
        final Context context = getContext();

        unknownLocation = context.getString(R.string.gps_unknown_location);
        statusView = new TextView(context);

        final FrameLayout content = getContentView();
        content.addView(statusView);
        statusView.setText(R.string.gps_requesting_location);

        final ReactiveLocationProvider provider = new ReactiveLocationProvider(context);

        final LocationRequest locationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setFastestInterval(1000)
                .setInterval(10000);

        // get last known location, to display something quite fast
        subscribe(provider, provider.getLastKnownLocation());

        // then subscribe to get location updates
        addressObservable = subscribe(provider, provider.getUpdatedLocation(locationRequest));
    }

    private Observable<String> subscribe(final ReactiveLocationProvider provider, Observable<Location> locationObservable) {
        return locationObservable
                .flatMap(new LocationObservableFunc1(provider))
                .map(new ListAddressFunc1())
                .map(new StringAddressFunc1(unknownLocation))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public void onResume() {
        final Context context = getContext();
        final boolean location = BaseActivity.isGranted(context, Manifest.permission.ACCESS_COARSE_LOCATION) &&
                                 BaseActivity.isGranted(context, Manifest.permission.ACCESS_FINE_LOCATION);
        if (!location) {
            final Intent intent = new Intent(BaseActivity.ACTION_REQUEST_PERMISSION);
            final ArrayList<String> permissions = new ArrayList<>(2);
            permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION);
            permissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
            intent.putStringArrayListExtra(BaseActivity.EXTRA_PERMISSIONS, permissions);
            LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
        }

        addressSubscription = addressObservable
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<String>() {
                    @Override public void call(String s) {
                        if (BuildConfig.DEBUG) {
                            final Vibrator vibrator = App.get().getVibrator();
                            vibrator.cancel();
                            vibrator.vibrate(75);
                        }
                        statusView.setText(s);
                    }
                }, new ErrorHandler(statusView));
    }

    public void onPause() {
        if (addressSubscription != null) {
            addressSubscription.unsubscribe();
        }
    }

    private static class ErrorHandler implements Action1<Throwable> {
        private final TextView statusView;

        public ErrorHandler(TextView statusView) {
            this.statusView = statusView;
        }

        @Override public void call(Throwable throwable) {
            Timber.d(throwable, "Error occurred when requesting location");
            if (statusView != null) {
                statusView.setText(R.string.gps_requesting_location_error);
            }
        }
    }

    private static class LocationObservableFunc1 implements Func1<Location, Observable<List<Address>>> {
        private final ReactiveLocationProvider provider;

        public LocationObservableFunc1(ReactiveLocationProvider provider) {
            this.provider = provider;
        }

        @Override public Observable<List<Address>> call(Location location) {
            return provider.getReverseGeocodeObservable(location.getLatitude(), location.getLongitude(), 1);
        }
    }

    private static class ListAddressFunc1 implements Func1<List<Address>, Address> {
        @Override public Address call(List<Address> addresses) {
            if (addresses == null || addresses.isEmpty()) {
                return null;
            }
            return addresses.get(0);
        }
    }

    private static class StringAddressFunc1 implements Func1<Address, String> {
        private final String unknownLocation;

        public StringAddressFunc1(String unknownLocation) {
            this.unknownLocation = unknownLocation;
        }

        @Override public String call(Address address) {
            if (address == null) {
                return unknownLocation;
            }

            // collect all address lines
            final StringBuilder sb = new StringBuilder();
            for (int i = 0; i <= address.getMaxAddressLineIndex(); i++) {
                sb.append(address.getAddressLine(i)).append('\n');
            }
            return sb.toString();
        }
    }
}
