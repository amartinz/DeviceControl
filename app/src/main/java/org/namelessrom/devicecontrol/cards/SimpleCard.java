package org.namelessrom.devicecontrol.cards;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.namelessrom.devicecontrol.R;

import it.gmariotti.cardslib.library.internal.Card;

import static butterknife.ButterKnife.findById;

/**
 * A simple card for displaying informations
 */
public class SimpleCard extends Card {

    private String mTitle;

    public SimpleCard(final Context context) {
        this(context, R.layout.card_flasher_inner_content);
    }

    public SimpleCard(final Context context, final int innerLayout) {
        super(context, innerLayout);
        mTitle = "i am the title";
        setSwipeable(true);
    }

    @Override
    public void setupInnerViewElements(final ViewGroup parent, final View view) {
        final TextView title = findById(view, R.id.flash_name);
        final TextView body = findById(view, R.id.flash_path);

        title.setText(mTitle);
        body.setText("i am the body");
    }

}
