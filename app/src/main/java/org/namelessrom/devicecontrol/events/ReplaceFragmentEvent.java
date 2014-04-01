package org.namelessrom.devicecontrol.events;

import android.app.Fragment;

public class ReplaceFragmentEvent {

    private final boolean  mAnimate;
    private final Fragment mFragment;

    public ReplaceFragmentEvent(final Fragment f, final boolean anim) {
        mFragment = f;
        mAnimate = anim;
    }

    public Fragment getFragment() { return mFragment; }

    public boolean isAnimate() { return mAnimate; }

}
