/*
 *  Copyright (C) 2013-2014 Alexander "Evisceration" Martinz
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
package org.namelessrom.devicecontrol.fragments.main;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.cards.internal.Card;
import com.android.cards.internal.CardArrayAdapter;
import com.android.cards.internal.CardExpand;
import com.android.cards.internal.CardHeader;
import com.android.cards.view.CardListView;

import org.namelessrom.devicecontrol.R;
import org.namelessrom.devicecontrol.fragments.parents.AttachFragment;
import org.namelessrom.devicecontrol.utils.constants.DeviceConstants;
import org.namelessrom.devicecontrol.utils.helpers.AlarmHelper;
import org.namelessrom.devicecontrol.utils.helpers.ParseUtils;
import org.namelessrom.devicecontrol.utils.helpers.PreferenceHelper;
import org.namelessrom.devicecontrol.widgets.SpinnerWidget;
import org.namelessrom.devicecontrol.widgets.SwitchWidget;

import java.util.ArrayList;

public class TaskerFragment extends AttachFragment implements DeviceConstants {

    public static final int ID = 3;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity, TaskerFragment.ID);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_tasker, container, false);

        TextView tvHelp = (TextView) view.findViewById(R.id.help_textview);
        tvHelp.setText(R.string.tasker_introduction);

        ImageView ivHelp = (ImageView) view.findViewById(R.id.help_imageview);
        ivHelp.setImageResource(R.mipmap.ic_launcher);

        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initializeCards(getActivity(), view);
    }

    private void initializeCards(final Context context, final View view) {
        final ArrayList<Card> cards = new ArrayList<Card>();
        final Card optimCard = new Card(context);

        final CardHeader optimHeader = new CardHeader(context);
        optimHeader.setTitle(getString(R.string.section_title_tasker_optimizations));
        optimHeader.setButtonExpandVisible(true);
        optimCard.addCardHeader(optimHeader);

        final OptimExpand optimExpand = new OptimExpand(context);
        optimCard.addCardExpand(optimExpand);

        optimCard.setSwipeable(false);
        cards.add(optimCard);

        final CardArrayAdapter mCardArrayAdapter = new CardArrayAdapter(context, cards);

        final CardListView listView = (CardListView) view.findViewById(R.id.cards_list);
        if (listView != null) {
            listView.setAdapter(mCardArrayAdapter);
        }
    }

    private class OptimExpand extends CardExpand {

        private SwitchWidget mFstrim;
        private SpinnerWidget mFstrimInterval;

        public OptimExpand(Context context) {
            super(context, R.layout.card_layout_tasker_optim);
        }

        @Override
        public void setupInnerViewElements(ViewGroup parent, View view) {
            if (view == null) return;

            mFstrim = (SwitchWidget) view.findViewById(R.id.fstrim);
            mFstrim.setChecked(PreferenceHelper.getBoolean(FSTRIM));
            mFstrim.setOnToggleListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean value) {
                    PreferenceHelper.setBoolean(FSTRIM, value);
                    mFstrimInterval.setEnabled(value);
                    if (value) {
                        AlarmHelper.setAlarmFstrim(getActivity(),
                                ParseUtils.parseFstrim(mFstrimInterval.getSelectedPosition())
                        );
                    } else {
                        AlarmHelper.cancelAlarmFstrim(getActivity());
                    }
                }
            });

            mFstrimInterval = (SpinnerWidget) view.findViewById(R.id.fstrim_interval);
            mFstrimInterval.getSpinner().setSelection(ParseUtils.getFstrim());
            mFstrimInterval.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    PreferenceHelper.setInt(FSTRIM_INTERVAL,
                            ParseUtils.parseFstrim(mFstrimInterval.getSelectedPosition())
                    );
                    if (mFstrim.isChecked()) {
                        AlarmHelper.setAlarmFstrim(
                                getActivity(),
                                ParseUtils.parseFstrim(mFstrimInterval.getSelectedPosition())
                        );
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {
                }
            });
            mFstrimInterval.setEnabled(mFstrim.isChecked());
        }
    }
}
