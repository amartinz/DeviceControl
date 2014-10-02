package org.namelessrom.devicecontrol.cards;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.namelessrom.devicecontrol.R;
import org.namelessrom.devicecontrol.events.RefreshEvent;
import org.namelessrom.devicecontrol.objects.FlashItem;
import org.namelessrom.devicecontrol.utils.providers.BusProvider;

import it.gmariotti.cardslib.library.internal.Card;

/**
 * A simple card for displaying informations
 */
public class FlasherCard extends Card {

    private final FlashItem item;

    public FlasherCard(final Context context, final FlashItem flashItem) {
        this(context, R.layout.card_flasher_inner_content, flashItem);
    }

    public FlasherCard(final Context context, final int innerLayout, final FlashItem flashItem) {
        super(context, innerLayout);
        item = flashItem;

        setClickable(false);
        setSwipeable(true);
        setOnSwipeListener(new OnSwipeListener() {
            @Override public void onSwipe(Card card) {
                BusProvider.getBus().post(new RefreshEvent());
            }
        });
    }

    @Override public void setupInnerViewElements(final ViewGroup parent, final View view) {
        final TextView name = (TextView) view.findViewById(R.id.flash_name);
        final TextView path = (TextView) view.findViewById(R.id.flash_path);

        name.setText(item.getName());
        path.setText(item.getPath());
    }

    public FlashItem getItem() { return item; }

}
