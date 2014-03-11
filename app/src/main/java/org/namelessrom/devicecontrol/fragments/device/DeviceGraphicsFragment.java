/*
 *  Copyright (C) 2012 The CyanogenMod Project
 *  Modifications Copyright (C) 2013-2014 Alexander "Evisceration" Martinz
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

package org.namelessrom.devicecontrol.fragments.device;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.android.cards.internal.Card;
import com.android.cards.internal.CardArrayAdapter;
import com.android.cards.internal.CardExpand;
import com.android.cards.internal.CardHeader;
import com.android.cards.view.CardListView;

import org.namelessrom.devicecontrol.R;
import org.namelessrom.devicecontrol.utils.Utils;
import org.namelessrom.devicecontrol.utils.constants.DeviceConstants;
import org.namelessrom.devicecontrol.utils.constants.FileConstants;
import org.namelessrom.devicecontrol.utils.helpers.AlarmHelper;
import org.namelessrom.devicecontrol.utils.helpers.ParseUtils;
import org.namelessrom.devicecontrol.utils.helpers.PreferenceHelper;
import org.namelessrom.devicecontrol.utils.threads.WriteAndForget;
import org.namelessrom.devicecontrol.widgets.SpinnerWidget;
import org.namelessrom.devicecontrol.widgets.SwitchWidget;
import org.namelessrom.devicecontrol.widgets.preferences.PanelColorTemperature;

import java.util.ArrayList;

public class DeviceGraphicsFragment extends Fragment implements DeviceConstants, FileConstants {

    //==============================================================================================
    // Fields
    //==============================================================================================
    public static final String sHasPanelFile = Utils.checkPaths(FILES_PANEL_COLOR_TEMP);
    public static final boolean sHasPanel = !sHasPanelFile.equals("");

    //==============================================================================================
    // Overridden Methods
    //==============================================================================================

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_device, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initializeCards(getActivity(), view);
    }

    private void initializeCards(final Context context, final View view) {
        final ArrayList<Card> cards = new ArrayList<Card>();
        final Card graphicsCard = new Card(context);

        final CardHeader graphicsHeader = new CardHeader(context);
        graphicsHeader.setTitle(getString(R.string.section_title_device_graphics));
        graphicsHeader.setButtonExpandVisible(sHasPanel);
        graphicsCard.addCardHeader(graphicsHeader);

        final GraphicsExpand graphicsExpand = new GraphicsExpand(context);
        graphicsCard.addCardExpand(graphicsExpand);

        graphicsCard.setSwipeable(false);
        cards.add(graphicsCard);

        final CardArrayAdapter mCardArrayAdapter = new CardArrayAdapter(context, cards);

        final CardListView listView = (CardListView) view.findViewById(R.id.cards_list);
        if (listView != null) {
            listView.setAdapter(mCardArrayAdapter);
        }
    }

    private class GraphicsExpand extends CardExpand {

        private SpinnerWidget mPanelColor;

        public GraphicsExpand(Context context) {
            super(context, R.layout.card_layout_device_graphics);
        }

        @Override
        public void setupInnerViewElements(ViewGroup parent, View view) {
            if (view == null) return;

            mPanelColor = (SpinnerWidget) view.findViewById(R.id.panel_color);

            if (sHasPanel) {
                mPanelColor.getSpinner().setSelection(
                        Integer.parseInt(Utils.readOneLine(sHasPanelFile))
                );
                mPanelColor.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                        PreferenceHelper.setInt(PANEL_COLOR, i);
                        new WriteAndForget(sHasPanelFile, String.valueOf(i)).start();
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> adapterView) {
                    }
                });
            } else {
                ((LinearLayout) view).removeView(mPanelColor);
            }
        }
    }

    //==============================================================================================
    // Methods
    //==============================================================================================

    public static boolean isSupported() {
        return (sHasPanel);
    }
}
