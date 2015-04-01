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

package org.namelessrom.devicecontrol.modules.wizard;

/*import android.content.Context;

import org.namelessrom.devicecontrol.Application;
import org.namelessrom.devicecontrol.R;
import org.namelessrom.devicecontrol.actions.ActionProcessor;
import org.namelessrom.devicecontrol.modules.tasker.TaskerItem;
import org.namelessrom.devicecontrol.modules.cpu.CpuUtils;
import org.namelessrom.devicecontrol.hardware.GovernorUtils;
import org.namelessrom.devicecontrol.hardware.GpuUtils;
import org.namelessrom.devicecontrol.hardware.IoSchedulerUtils;
import org.namelessrom.devicecontrol.utils.constants.Constants;
import org.namelessrom.devicecontrol.modules.wizard.model.AbstractWizardModel;
import org.namelessrom.devicecontrol.modules.wizard.model.BranchPage;
import org.namelessrom.devicecontrol.modules.wizard.model.Page;
import org.namelessrom.devicecontrol.modules.wizard.model.PageList;
import org.namelessrom.devicecontrol.modules.wizard.model.SingleFixedChoicePage;*/

public class TaskerWizardModel /*extends AbstractWizardModel implements Constants*/ {
    /*
    private TaskerItem mItem;
    private String mCategory = "";
    private String mAction   = "";
    private String mValue    = "";

    public TaskerWizardModel(final Context context) {
        this(context, null);
    }

    public TaskerWizardModel(final Context context, final TaskerItem item) {
        super(context);

        mItem = item;
        if (mItem != null) {
            mCategory = mItem.category;
            mAction = mItem.name;
            mValue = mItem.value;
        }

        setRootPageList(getRootPageList());
    }

    public TaskerItem getItem() { return mItem; }

    private Page getCategoryPage() {
        return new SingleFixedChoicePage(this,
                "1) " + getString(R.string.category))
                .setChoices(ActionProcessor.CATEGORIES)
                .setValue(mCategory)
                .setRequired(true);
    }

    private Page getActionPage() {
        final BranchPage actionPage = new BranchPage(this, "2) " + getString(R.string.action));

        for (final String s : ActionProcessor.getActions()) {
            actionPage.addBranch(s, getValuePage(s));
        }

        actionPage.setValue(mAction);
        actionPage.setRequired(true);

        return actionPage;
    }

    private Page getValuePage(final String action) {
        final String[] choices;
        //------------------------------------------------------------------------------------------
        // CPU
        //------------------------------------------------------------------------------------------
        if (ActionProcessor.ACTION_CPU_FREQUENCY_MAX.equals(action)
                || ActionProcessor.ACTION_CPU_FREQUENCY_MIN.equals(action)) {
            choices = CpuUtils.get().getAvailableFrequencies();
        } else if (ActionProcessor.ACTION_CPU_GOVERNOR.equals(action)) {
            choices = GovernorUtils.get().getAvailableGovernors();
        }
        //------------------------------------------------------------------------------------------
        // General Actions
        //------------------------------------------------------------------------------------------
        else if (ActionProcessor.ACTION_IO_SCHEDULER.equals(action)) {
            choices = IoSchedulerUtils.get().getAvailableIoSchedulers();
        } else if (ActionProcessor.ACTION_KSM_ENABLED.equals(action)
                || ActionProcessor.ACTION_KSM_DEFERRED.equals(action)) {
            choices = getBooleanChoices();
        } else if (ActionProcessor.ACTION_KSM_PAGES.equals(action)) {
            choices = new String[]{"32", "64", "128", "256", "512", "1024"};
        } else if (ActionProcessor.ACTION_KSM_SLEEP.equals(action)) {
            choices = new String[]{"100", "250", "500", "1000", "2000", "3000", "4000", "5000"};
        }
        //------------------------------------------------------------------------------------------
        // GPU
        //------------------------------------------------------------------------------------------
        else if (ActionProcessor.ACTION_GPU_FREQUENCY_MAX.equals(action)) {
            choices = GpuUtils.get().getAvailableFrequencies();
        } else if (ActionProcessor.ACTION_GPU_GOVERNOR.equals(action)) {
            choices = GPU_GOVS;
        } else if (ActionProcessor.ACTION_3D_SCALING.equals(action)) {
            choices = getBooleanChoices();
        } else { choices = null; }

        return new SingleFixedChoicePage(this, "3) " + getString(R.string.value))
                .setChoices(choices)
                .setValue(mValue)
                .setRequired(true);
    }

    private String[] getBooleanChoices() { return new String[]{"0", "1"}; }

    private String getString(final int id) { return Application.get().getString(id); }

    private PageList getRootPageList() {
        PageList pageList;

        if (mItem == null) {
            pageList = new PageList(getCategoryPage(), getActionPage());
        } else {
            pageList = new PageList(
                    new BranchPage(this, "0) " + getString(R.string.edit))
                            .addBranch(getString(R.string.edit_task),
                                    getCategoryPage(),
                                    getActionPage())
                            .addBranch(getString(R.string.delete_task))
                            .setRequired(true)
            );
        }

        return pageList;
    }
*/
}
