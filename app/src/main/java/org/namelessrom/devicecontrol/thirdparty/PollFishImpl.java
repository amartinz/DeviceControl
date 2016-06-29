package org.namelessrom.devicecontrol.thirdparty;

import android.app.Activity;
import android.text.TextUtils;

import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;
import com.pollfish.constants.Position;
import com.pollfish.interfaces.PollfishClosedListener;
import com.pollfish.interfaces.PollfishOpenedListener;
import com.pollfish.interfaces.PollfishSurveyCompletedListener;
import com.pollfish.interfaces.PollfishSurveyNotAvailableListener;
import com.pollfish.interfaces.PollfishSurveyReceivedListener;
import com.pollfish.interfaces.PollfishUserNotEligibleListener;
import com.pollfish.main.PollFish;

import org.namelessrom.devicecontrol.BuildConfig;
import org.namelessrom.devicecontrol.models.DeviceConfig;

import timber.log.Timber;

public class PollFishImpl {
    public static void initPollFish(Activity activity) {
        if (!DeviceConfig.get().showPollfish) {
            Timber.d("PollFish is deactivated!");
            return;
        }
        if (!TextUtils.equals("---", BuildConfig.API_KEY_POLL_FISH)) {
            Timber.d("PollFish.init()");
            final PollFishListener pollFishListener = new PollFishListener();
            final PollFish.ParamsBuilder paramsBuilder = new PollFish.ParamsBuilder(BuildConfig.API_KEY_POLL_FISH)
                    .indicatorPosition(Position.BOTTOM_RIGHT)
                    .indicatorPadding(50)
                    .pollfishSurveyNotAvailableListener(pollFishListener)
                    .pollfishSurveyReceivedListener(pollFishListener)
                    .pollfishSurveyCompletedListener(pollFishListener)
                    .pollfishUserNotEligibleListener(pollFishListener)
                    .pollfishClosedListener(pollFishListener)
                    .pollfishOpenedListener(pollFishListener)
                    .releaseMode(!BuildConfig.DEBUG)
                    .build();
            PollFish.initWith(activity, paramsBuilder);

            if (BuildConfig.DEBUG) {
                PollFish.show();
            }
        } else {
            Timber.w("No proper PollFish api key configured!");
        }
    }

    public static class PollFishListener implements PollfishSurveyNotAvailableListener, PollfishSurveyReceivedListener, PollfishSurveyCompletedListener, PollfishUserNotEligibleListener, PollfishClosedListener, PollfishOpenedListener {

        @Override public void onPollfishSurveyNotAvailable() {
            Answers.getInstance().logCustom(new CustomEvent("pollfish_survey_not_available"));
        }

        @Override public void onPollfishSurveyReceived(boolean playfulSurveys, int surveyPrice) {
            final CustomEvent event = new CustomEvent("pollfish_survey_received");
            event.putCustomAttribute("playful", playfulSurveys ? "true" : "false");
            Answers.getInstance().logCustom(event);
        }

        @Override public void onPollfishSurveyCompleted(boolean playfulSurveys, int surveyPrice) {
            final CustomEvent event = new CustomEvent("pollfish_survey_completed");
            event.putCustomAttribute("playful", playfulSurveys ? "true" : "false");
            Answers.getInstance().logCustom(event);
        }

        @Override public void onUserNotEligible() {
            Answers.getInstance().logCustom(new CustomEvent("pollfish_user_not_eligible"));
        }

        @Override public void onPollfishClosed() {
            Answers.getInstance().logCustom(new CustomEvent("pollfish_closed"));
        }

        @Override public void onPollfishOpened() {
            Answers.getInstance().logCustom(new CustomEvent("pollfish_opened"));
        }
    }
}
