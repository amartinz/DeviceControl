/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.namelessrom.devicecontrol.tv.presenter;

import android.support.v17.leanback.widget.ImageCardView;
import android.support.v17.leanback.widget.Presenter;
import android.support.v4.content.ContextCompat;
import android.view.ViewGroup;

import org.namelessrom.devicecontrol.R;

/*
 * A CardPresenter is used to generate Views and bind Objects to them on demand. 
 * It contains an Image CardView
 */
public class CardPresenter extends Presenter {
    private static int CARD_WIDTH = 300;
    private static int CARD_HEIGHT = 80;
    private static int sSelectedBackgroundColor;
    private static int sDefaultBackgroundColor;
    //private Drawable mDefaultCardImage;

    @Override public ViewHolder onCreateViewHolder(ViewGroup parent) {
        sDefaultBackgroundColor = ContextCompat.getColor(parent.getContext(), R.color.cardview_dark_background);
        sSelectedBackgroundColor = ContextCompat.getColor(parent.getContext(), R.color.primary_dark);
        //mDefaultCardImage = parent.getResources().getDrawable(R.drawable.movie);

        ImageCardView cardView = new ImageCardView(parent.getContext()) {
            @Override public void setSelected(boolean selected) {
                updateCardBackgroundColor(this, selected);
                super.setSelected(selected);
            }
        };

        cardView.setFocusable(true);
        cardView.setFocusableInTouchMode(true);
        updateCardBackgroundColor(cardView, false);
        return new ViewHolder(cardView);
    }

    private static void updateCardBackgroundColor(ImageCardView view, boolean selected) {
        int color = selected ? sSelectedBackgroundColor : sDefaultBackgroundColor;
        // Both background colors should be set because the view's background is temporarily visible
        // during animations.
        view.setBackgroundColor(color);
        view.findViewById(R.id.info_field).setBackgroundColor(color);
    }

    @Override public void onBindViewHolder(Presenter.ViewHolder viewHolder, Object item) {
        CardDetail cardDetail = (CardDetail) item;
        ImageCardView cardView = (ImageCardView) viewHolder.view;

        if (cardDetail != null) {
            cardView.setTitleText(cardDetail.title);
            cardView.setContentText(cardDetail.description);
            cardView.setMainImageDimensions(CARD_WIDTH, CARD_HEIGHT);
        }
    }

    @Override public void onUnbindViewHolder(Presenter.ViewHolder viewHolder) {
    }

    public static class CardDetail {
        public String title;
        public String description;

        public CardDetail() {
            this(null);
        }

        public CardDetail(String title) {
            this(title, null);
        }

        public CardDetail(String title, String description) {
            this.title = title;
            this.description = description;
        }
    }
}
