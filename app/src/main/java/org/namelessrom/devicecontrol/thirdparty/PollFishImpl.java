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
        } else {
            Timber.w("No proper PollFish api key configured!");
        }
    }

    private static final class PollFishListener implements PollfishSurveyNotAvailableListener, PollfishSurveyReceivedListener, PollfishSurveyCompletedListener, PollfishUserNotEligibleListener, PollfishClosedListener, PollfishOpenedListener {
        private static final String NOT_AVAILABLE = "pollfish_survey_not_available";
        private static final String RECEIVED = "pollfish_survey_received";
        private static final String COMPLETED = "pollfish_survey_completed";

        private static final String NOT_ELIGIBLE = "pollfish_user_not_eligible";
        private static final String CLOSED = "pollfish_closed";
        private static final String OPENED = "pollfish_opened";

        @Override public void onPollfishSurveyNotAvailable() {
            Timber.d(NOT_AVAILABLE);
            Answers.getInstance().logCustom(new CustomEvent(NOT_AVAILABLE));
        }

        @Override public void onPollfishSurveyReceived(boolean playfulSurveys, int surveyPrice) {
            Timber.d(RECEIVED);
            final CustomEvent event = new CustomEvent(RECEIVED);
            event.putCustomAttribute("playful", playfulSurveys ? "true" : "false");
            Answers.getInstance().logCustom(event);
        }

        @Override public void onPollfishSurveyCompleted(boolean playfulSurveys, int surveyPrice) {
            Timber.d(COMPLETED);
            final CustomEvent event = new CustomEvent(COMPLETED);
            event.putCustomAttribute("playful", playfulSurveys ? "true" : "false");
            Answers.getInstance().logCustom(event);
        }

        @Override public void onUserNotEligible() {
            Timber.d(NOT_ELIGIBLE);
            Answers.getInstance().logCustom(new CustomEvent(NOT_ELIGIBLE));
        }

        @Override public void onPollfishClosed() {
            Timber.d(CLOSED);
            Answers.getInstance().logCustom(new CustomEvent(CLOSED));
        }

        @Override public void onPollfishOpened() {
            Timber.d(OPENED);
            Answers.getInstance().logCustom(new CustomEvent(OPENED));
        }
    }
}
