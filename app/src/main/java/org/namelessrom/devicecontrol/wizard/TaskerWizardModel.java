/*
 * Copyright 2013 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.namelessrom.devicecontrol.wizard;

import android.content.Context;

import org.namelessrom.devicecontrol.R;
import org.namelessrom.devicecontrol.database.TaskerItem;
import org.namelessrom.devicecontrol.objects.Action;
import org.namelessrom.devicecontrol.objects.Category;
import org.namelessrom.devicecontrol.utils.ActionProcessor;
import org.namelessrom.devicecontrol.utils.CpuUtils;
import org.namelessrom.devicecontrol.utils.GpuUtils;
import org.namelessrom.devicecontrol.utils.Utils;
import org.namelessrom.devicecontrol.utils.constants.PerformanceConstants;
import org.namelessrom.devicecontrol.wizard.model.AbstractWizardModel;
import org.namelessrom.devicecontrol.wizard.model.BranchPage;
import org.namelessrom.devicecontrol.wizard.model.Page;
import org.namelessrom.devicecontrol.wizard.model.PageList;
import org.namelessrom.devicecontrol.wizard.model.SingleFixedChoicePage;

import java.util.ArrayList;
import java.util.List;

import static org.namelessrom.devicecontrol.Application.logDebug;

public class TaskerWizardModel extends AbstractWizardModel implements PerformanceConstants {
    private TaskerItem mItem;
    private String mCategory = "";
    private String mAction   = "";
    private String mValue    = "";

    public TaskerWizardModel(final Context context) { this(context, null); }

    public TaskerWizardModel(final Context context, final TaskerItem item) {
        super(context);

        mItem = item;
        if (mItem != null) {
            mCategory = mItem.getCategory();
            mAction = mItem.getName();
            mValue = mItem.getValue();
        }

        setRootPageList(getRootPageList());
    }

    public TaskerItem getItem() { return mItem; }

    private Page getCategoryPage() {
        final List<Category> categories = ActionProcessor.getCategories();
        final List<String> values = new ArrayList<String>();
        final List<String> display = new ArrayList<String>();

        for (final Category c : categories) {
            values.add(c.mValue);
            display.add(c.mDisplay);
        }

        return new SingleFixedChoicePage(this,
                "1) " + Utils.getString(R.string.category))
                .setChoices(display.toArray(new String[display.size()]))
                .setValues(values.toArray(new String[values.size()]))
                .setValue(mCategory)
                .setRequired(true);
    }

    private Page getActionPage() {
        final BranchPage actionPage = new BranchPage(this,
                "2) " + Utils.getString(R.string.action));

        final List<Action> actions = ActionProcessor.getActions();
        final List<String> values = new ArrayList<String>();
        final List<String> display = new ArrayList<String>();
        for (final Action a : actions) {
            actionPage.addBranch(a.mDisplay, getValuePage(a.mValue));
            values.add(a.mValue);
            display.add(a.mDisplay);
            logDebug("Action.mDisplay: " + a.mDisplay + " | Action.mValue: " + a.mValue);
        }

        logDebug("getActionPage().setValue(" + mAction + ')');
        actionPage.setChoices(display.toArray(new String[display.size()]));
        actionPage.setValues(values.toArray(new String[values.size()]));
        actionPage.setValue(mAction);
        actionPage.setRequired(true);

        return actionPage;
    }

    private Page getValuePage(final String action) {
        final String[] choices;
        //------------------------------------------------------------------------------------------
        // General Actions
        //------------------------------------------------------------------------------------------
        if (ActionProcessor.ACTION_CPU_FREQUENCY_MAX.equals(action)
                || ActionProcessor.ACTION_CPU_FREQUENCY_MIN.equals(action)) {
            choices = CpuUtils.getAvailableFrequencies();
        } else if (ActionProcessor.ACTION_CPU_GOVERNOR.equals(action)) {
            choices = CpuUtils.getAvailableGovernors();
        } else if (ActionProcessor.ACTION_IO_SCHEDULER.equals(action)) {
            choices = CpuUtils.getAvailableIOSchedulers();
        }
        //------------------------------------------------------------------------------------------
        // GPU
        //------------------------------------------------------------------------------------------
        else if (ActionProcessor.ACTION_GPU_FREQUENCY_MAX.equals(action)) {
            choices = GpuUtils.getAvailableFrequencies();
        } else if (ActionProcessor.ACTION_GPU_GOVERNOR.equals(action)) {
            choices = GPU_GOVS;
        } else if (ActionProcessor.ACTION_3D_SCALING.equals(action)) {
            choices = getBooleanChoices();
        } else { choices = null; }

        return new SingleFixedChoicePage(this, "3) " + Utils.getString(R.string.value))
                .setChoices(choices)
                .setValues(choices)
                .setValue(mValue)
                .setRequired(true);
    }

    private String[] getBooleanChoices() { return new String[]{"0", "1"}; }

    private PageList getRootPageList() {
        final PageList pageList;

        if (mItem == null) {
            pageList = new PageList(getCategoryPage(), getActionPage());
        } else {
            pageList = new PageList(
                    new BranchPage(this, "0) " + Utils.getString(R.string.edit))
                            .addBranch(Utils.getString(R.string.edit_task),
                                    getCategoryPage(),
                                    getActionPage())
                            .addBranch(Utils.getString(R.string.delete_task))
                            .setRequired(true)
            );
        }

        return pageList;
    }
}
