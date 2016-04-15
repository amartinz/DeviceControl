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

package org.namelessrom.devicecontrol.modules.wizard;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.balysv.materialmenu.MaterialMenuDrawable;
import com.balysv.materialmenu.extras.toolbar.MaterialMenuIconToolbar;

import org.namelessrom.devicecontrol.R;
import org.namelessrom.devicecontrol.activities.BaseActivity;
import org.namelessrom.devicecontrol.models.TaskerConfig;
import org.namelessrom.devicecontrol.modules.tasker.TaskerItem;
import org.namelessrom.devicecontrol.modules.wizard.setup.AbstractSetupData;
import org.namelessrom.devicecontrol.modules.wizard.setup.Page;
import org.namelessrom.devicecontrol.modules.wizard.setup.PageList;
import org.namelessrom.devicecontrol.modules.wizard.setup.SetupDataCallbacks;
import org.namelessrom.devicecontrol.modules.wizard.setup.TaskerSetupWizardData;
import org.namelessrom.devicecontrol.modules.wizard.ui.StepPagerStrip;

import timber.log.Timber;

public class AddTaskActivity extends BaseActivity implements SetupDataCallbacks {
    public static final String ARG_ITEM = "arg_item";

    private ViewPager mViewPager;
    private StepPagerStrip mStepPagerStrip;
    private CustomPagerAdapter mPagerAdapter;

    private Button mNextButton;
    private Button mPrevButton;

    private PageList mPageList;

    private AbstractSetupData mSetupData;

    private final Handler mHandler = new Handler();

    private static long mBackPressed;
    private Toast mToast;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.wizard_activity);

        // setup action bar
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // setup material menu icon
        final MaterialMenuIconToolbar materialMenu =
                new MaterialMenuIconToolbar(this, Color.WHITE, MaterialMenuDrawable.Stroke.THIN) {
                    @Override public int getToolbarViewId() { return R.id.toolbar; }
                };
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            materialMenu.setNeverDrawTouch(true);
        }
        materialMenu.animateState(MaterialMenuDrawable.IconState.ARROW);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                onBackPressed();
            }
        });

        mSetupData = (AbstractSetupData) getLastNonConfigurationInstance();
        if (mSetupData == null) {
            mSetupData = new TaskerSetupWizardData(this);
        }

        if (savedInstanceState != null) {
            mSetupData.load(savedInstanceState.getBundle("data"));
        }

        final TaskerItem item = (TaskerItem) getIntent().getSerializableExtra(ARG_ITEM);
        Timber.v("TaskerItem: %s", item == null ? "null" : item.toString());
        if (item != null) {
            mSetupData.setSetupData(item);
            toolbar.setTitle(R.string.edit_task);
        }
        if (mSetupData.getSetupData() == null) {
            mSetupData.setSetupData(new TaskerItem());
        }

        mNextButton = (Button) findViewById(R.id.next_button);
        mPrevButton = (Button) findViewById(R.id.prev_button);
        mSetupData.registerListener(this);
        mPagerAdapter = new CustomPagerAdapter(getSupportFragmentManager());
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mPagerAdapter);
        mViewPager.setPageMargin(getResources().getDimensionPixelOffset(R.dimen.app_margin));
        mViewPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                mStepPagerStrip.setCurrentPage(position);

                if (position < mPageList.size()) {
                    onPageLoaded(mPageList.get(position));
                }
            }
        });
        mStepPagerStrip = (StepPagerStrip) findViewById(R.id.strip);
        mStepPagerStrip.setOnPageSelectedListener(new StepPagerStrip.OnPageSelectedListener() {
            @Override
            public void onPageStripSelected(int position) {
                position = Math.min(mPagerAdapter.getCount() - 1, position);
                if (mViewPager.getCurrentItem() != position) {
                    mViewPager.setCurrentItem(position);
                }
            }
        });
        mNextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                doNext();
            }
        });
        mPrevButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                doPrevious();
            }
        });

        onPageTreeChanged();
    }

    @Override protected void onResume() {
        super.onResume();
        onPageTreeChanged();
    }

    @Override protected void onDestroy() {
        mSetupData.unregisterListener(this);
        super.onDestroy();
    }

    @Override protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBundle("data", mSetupData.save());
    }

    @Override public void onBackPressed() {
        doPrevious();
    }

    public void doNext() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                final int currentItem = mViewPager.getCurrentItem();
                final Page currentPage = mPageList.get(currentItem);
                if (currentPage.getId() == R.string.setup_complete) {
                    final TaskerItem item = mSetupData.getSetupData();
                    TaskerConfig.get().deleteItem(item).addItem(item).save();
                    finishSetup();
                } else {
                    mViewPager.setCurrentItem(currentItem + 1, true);
                }
            }
        });
    }

    public void doPrevious() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                final int currentItem = mViewPager.getCurrentItem();
                if (currentItem > 0) {
                    mViewPager.setCurrentItem(currentItem - 1, true);
                } else {
                    shouldExit();
                }
            }
        });
    }

    private void shouldExit() {
        if (mBackPressed + 2000 > System.currentTimeMillis()) {
            if (mToast != null) {
                mToast.cancel();
            }
            finish();
        } else {
            mToast = Toast.makeText(getBaseContext(),
                    getString(R.string.action_press_again), Toast.LENGTH_SHORT);
            mToast.show();
        }
        mBackPressed = System.currentTimeMillis();
    }

    private void updateNextPreviousState() {
        final int position = mViewPager.getCurrentItem();
        mNextButton.setEnabled(position != mPagerAdapter.getCutOffPage());
        mPrevButton.setVisibility(position <= 0 ? View.INVISIBLE : View.VISIBLE);
    }

    @Override public void onPageLoaded(Page page) {
        mNextButton.setText(page.getNextButtonResId());
        if (page.isRequired()) {
            if (recalculateCutOffPage()) {
                mPagerAdapter.notifyDataSetChanged();
            }
        }
        page.refresh();
        updateNextPreviousState();
    }

    @Override public void onPageTreeChanged() {
        mPageList = mSetupData.getPageList();
        recalculateCutOffPage();
        mPagerAdapter.notifyDataSetChanged();
        updateNextPreviousState();

        mStepPagerStrip.setPageCount(mPageList.size());
    }

    @Override public Page getPage(String key) {
        return mSetupData.findPage(key);
    }

    @Override public TaskerItem getSetupData() { return mSetupData.getSetupData(); }

    @Override public void setSetupData(final TaskerItem item) { mSetupData.setSetupData(item); }

    @Override public void onPageFinished(final Page page) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                doNext();
                onPageTreeChanged();
            }
        });
    }

    private boolean recalculateCutOffPage() {
        // Cut off the pager adapter at first required page that isn't completed
        int cutOffPage = mPageList.size();
        Page page;
        for (int i = 0; i < mPageList.size(); i++) {
            page = mPageList.get(i);
            if (page.isRequired() && !page.isCompleted()) {
                cutOffPage = i;
                break;
            }
        }

        if (mPagerAdapter.getCutOffPage() != cutOffPage) {
            mPagerAdapter.setCutOffPage(cutOffPage);
            return true;
        }

        return false;
    }

    private void finishSetup() {
        finish();
    }

    private class CustomPagerAdapter extends FragmentStatePagerAdapter {

        private int mCutOffPage;

        private CustomPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override public Fragment getItem(int i) {
            return mPageList.get(i).createFragment();
        }

        @Override public int getItemPosition(Object object) {
            return POSITION_NONE;
        }

        @Override public int getCount() {
            if (mPageList == null) { return 0; }
            return Math.min(mCutOffPage + 1, mPageList.size());
        }

        public void setCutOffPage(int cutOffPage) {
            if (cutOffPage < 0) {
                cutOffPage = Integer.MAX_VALUE;
            }
            mCutOffPage = cutOffPage;
        }

        public int getCutOffPage() {
            return mCutOffPage;
        }
    }
}
