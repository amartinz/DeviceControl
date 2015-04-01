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

package org.namelessrom.devicecontrol.modules.wizard.setup;

import android.content.Context;

import org.namelessrom.devicecontrol.R;
import org.namelessrom.devicecontrol.modules.wizard.pages.ActionPage;
import org.namelessrom.devicecontrol.modules.wizard.pages.CategoryPage;
import org.namelessrom.devicecontrol.modules.wizard.pages.FinishPage;
import org.namelessrom.devicecontrol.modules.wizard.pages.InformationPage;
import org.namelessrom.devicecontrol.modules.wizard.pages.TriggerPage;
import org.namelessrom.devicecontrol.modules.wizard.pages.ValuePage;

public class TaskerSetupWizardData extends AbstractSetupData {

    public TaskerSetupWizardData(final Context context) { super(context); }

    @Override protected PageList onNewPageList() {
        return new PageList(
                new InformationPage(mContext, this, R.string.setup_welcome),
                new TriggerPage(mContext, this, R.string.setup_trigger),
                new CategoryPage(mContext, this, R.string.setup_category),
                new ActionPage(mContext, this, R.string.setup_action),
                new ValuePage(mContext, this, R.string.setup_value),
                new FinishPage(mContext, this, R.string.setup_complete)
        );
    }


}
