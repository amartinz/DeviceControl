package org.namelessrom.devicecontrol.ui.views;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;

import org.namelessrom.devicecontrol.Logger;

public class CustomRecyclerView extends RecyclerView {
    public CustomRecyclerView(Context context) {
        super(context);
    }

    public CustomRecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomRecyclerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override public void scrollTo(int x, int y) {
        Logger.e(this, "RecyclerView does not support scrolling to an absolute position.");
    }
}
