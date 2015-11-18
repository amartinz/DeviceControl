/*
 * Copyright (C) 2013 - 2015 Alexander Martinz
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
 */

package org.namelessrom.devicecontrol.wizard;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import org.namelessrom.devicecontrol.R;
import org.namelessrom.devicecontrol.base.BaseActivity;
import org.namelessrom.devicecontrol.base.BaseFragment;

import java.util.ArrayList;

public class WizardManager<T> extends BaseFragment {
    private BaseActivity mActivity;
    private WizardCallbacks mCallbacks;

    private ArrayList<WizardPage> mWizardPages = new ArrayList<>();
    private int mPageIndex = -1;

    protected Button mNext;
    protected Button mPrevious;

    private boolean mHasSetupStarted;

    public WizardManager() {
        super();
    }

    @Override public void onAttach(Context context) {
        super.onAttach(context);
        mActivity = ((BaseActivity) context);
    }

    public T setCallbacks(@Nullable WizardCallbacks callbacks) {
        mCallbacks = callbacks;
        return (T) this;
    }

    public T setPages(@NonNull ArrayList<WizardPage> wizardPages) {
        mWizardPages = wizardPages;
        return (T) this;
    }

    public T addPage(@NonNull WizardPage wizardPage) {
        mWizardPages.add(wizardPage);
        return (T) this;
    }

    public T addPageAt(@NonNull WizardPage wizardPage, int index) {
        mWizardPages.add(index, wizardPage);
        return (T) this;
    }

    public void start() {
        if (mActivity != null) {
            if (mWizardPages == null || mWizardPages.isEmpty()) {
                // TODO: check for 6.0 when leaving app, toggling permissions and resuming
                throw new RuntimeException("WizardPages must not be null or empty!");
            }
            mPageIndex = -1;
            mWizardCallbacks.onSetupStarted();
            mWizardCallbacks.onNextPage();
        }
    }

    public boolean canDoNext() {
        final int pageIndex = mPageIndex + 1;
        return canDoNextInternal(pageIndex);
    }

    private boolean canDoNextInternal(final int pageIndex) {
        return (pageIndex < mWizardPages.size() && pageIndex > -1);
    }

    public boolean canDoPrevious() {
        final int pageIndex = mPageIndex - 1;
        return canDoPreviousInternal(pageIndex);
    }

    private boolean canDoPreviousInternal(int pageIndex) {
        return (pageIndex > -1);
    }

    public void cancel() {
        mWizardCallbacks.onSetupDone(true);
    }

    @Nullable @Override public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final LinearLayout containerView = (LinearLayout) inflater.inflate(R.layout.wizard_page_container, container, false);

        mPrevious = (Button) containerView.findViewById(R.id.wizard_previous);
        mPrevious.setOnClickListener(v -> {
            if (canDoPrevious()) {
                onPreviousPage();
            } else {
                onSetupDone(true);
            }
        });

        mNext = (Button) containerView.findViewById(R.id.wizard_next);
        mNext.setOnClickListener(v -> onNextPage());

        return containerView;
    }

    @Override public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (!mHasSetupStarted) {
            start();
            mHasSetupStarted = true;
        }
    }

    public void updateButtons() {
        if (canDoNext()) {
            mNext.setText(R.string.wizard_next);
        } else {
            mNext.setText(R.string.wizard_finish);
        }

        if (canDoPrevious()) {
            mPrevious.setText(R.string.wizard_previous);
        } else {
            mPrevious.setText(R.string.wizard_skip);
        }
    }

    public void onSetupStarted() {
        mWizardCallbacks.onSetupStarted();
    }

    public void onNextPage() {
        mWizardCallbacks.onNextPage();
    }

    public void onPreviousPage() {
        mWizardCallbacks.onPreviousPage();
    }

    public void onSetupDone(final boolean isAborted) {
        mWizardCallbacks.onSetupDone(isAborted);
    }

    private final WizardCallbacks mWizardCallbacks = new WizardCallbacks() {
        @Override public void onSetupStarted() {
            if (mCallbacks != null) {
                mCallbacks.onSetupStarted();
            }
        }

        @Override public void onNextPage() {
            mPageIndex++;
            boolean isNext = canDoNextInternal(mPageIndex);

            if (isNext) {
                getChildFragmentManager().beginTransaction()
                        .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_right,
                                R.anim.slide_in_left, R.anim.slide_out_left)
                        .replace(R.id.wizard_page_container, mWizardPages.get(mPageIndex).getWizardFragment())
                        .addToBackStack("")
                        .commit();
                updateButtons();
            }

            if (mCallbacks != null) {
                if (isNext) {
                    mCallbacks.onNextPage();
                } else {
                    onSetupDone(false);
                }
            }
        }

        @Override public void onPreviousPage() {
            mPageIndex--;
            boolean isPrevious = canDoPreviousInternal(mPageIndex);

            // safeguard against users, who like to press back a lot of times
            if (mPageIndex < 0) {
                mPageIndex = 0;
                return;
            }

            if (isPrevious) {
                getChildFragmentManager().popBackStack();
                updateButtons();
            }

            if (mCallbacks != null) {
                if (isPrevious) {
                    mCallbacks.onPreviousPage();
                }
            }
        }

        @Override public void onSetupDone(final boolean isAborted) {
            getChildFragmentManager().popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);

            if (mCallbacks != null) {
                mCallbacks.onSetupDone(isAborted);
            }

            mWizardPages.clear();
            mPageIndex = -1;
            mCallbacks = null;
            mActivity = null;
        }
    };

}
