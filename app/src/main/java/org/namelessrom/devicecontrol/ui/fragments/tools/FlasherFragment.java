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
package org.namelessrom.devicecontrol.ui.fragments.tools;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;

import org.namelessrom.devicecontrol.R;
import org.namelessrom.devicecontrol.ui.cards.Card;
import org.namelessrom.devicecontrol.ui.cards.FlashCard;
import org.namelessrom.devicecontrol.ui.views.AttachFragment;
import org.namelessrom.devicecontrol.utils.RebootHelper;
import org.namelessrom.devicecontrol.utils.RecoveryHelper;
import org.namelessrom.devicecontrol.utils.constants.DeviceConstants;

public class FlasherFragment extends AttachFragment implements DeviceConstants {
    private LinearLayout mCardsLayout;
    private FlashCard    mFlashCard;

    @Override protected int getFragmentId() { return ID_TOOLS_FLASHER; }

    @Override public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
            final Bundle savedInstanceState) {
        setHasOptionsMenu(true);

        final View v = inflater.inflate(R.layout.fragment_flasher, container, false);

        mCardsLayout = (LinearLayout) v.findViewById(R.id.cards_layout);

        return v;
    }

    @Override public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mFlashCard = new FlashCard(getActivity(), null,
                new RebootHelper(new RecoveryHelper(getActivity())), savedInstanceState);
        addCards(new Card[]{mFlashCard}, true, true);
    }

    public void addCards(final Card[] cards, boolean animate, boolean remove) {
        mCardsLayout.clearAnimation();
        if (remove) {
            mCardsLayout.removeAllViews();
        }
        if (animate) {
            mCardsLayout.setAnimation(
                    AnimationUtils.loadAnimation(getActivity(), R.anim.up_from_bottom));
        }
        for (Card card : cards) {
            mCardsLayout.addView(card);
        }
    }

}
