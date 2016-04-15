package org.namelessrom.devicecontrol.thirdparty;

import android.app.Activity;
import android.text.TextUtils;

import com.pollfish.constants.Position;
import com.pollfish.main.PollFish;

import org.namelessrom.devicecontrol.BuildConfig;
import org.namelessrom.devicecontrol.models.DeviceConfig;

import timber.log.Timber;

/**
 * Created by amartinz on 15.04.16.
 */
public class PollFishImpl {
    public static void initPollFish(Activity activity) {
        if (!DeviceConfig.get().showPollfish) {
            Timber.d("PollFish is deactivated!");
            return;
        }
        if (!TextUtils.equals("---", BuildConfig.API_KEY_POLL_FISH)) {
            Timber.d("PollFish.init()");
            final PollFish.ParamsBuilder paramsBuilder = new PollFish.ParamsBuilder(BuildConfig.API_KEY_POLL_FISH)
                    .indicatorPosition(Position.BOTTOM_RIGHT)
                    .indicatorPadding(50)
                    .build();
            PollFish.initWith(activity, paramsBuilder);
        } else {
            Timber.w("No proper PollFish api key configured!");
        }
    }
}
