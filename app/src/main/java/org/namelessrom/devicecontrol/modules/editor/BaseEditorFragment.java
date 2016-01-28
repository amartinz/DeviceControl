/*
 *  Copyright (C) 2013 - 2015 Alexander "Evisceration" Martinz
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
package org.namelessrom.devicecontrol.modules.editor;

import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.SearchView;

import org.namelessrom.devicecontrol.R;
import org.namelessrom.devicecontrol.utils.ShellOutput;
import org.namelessrom.devicecontrol.views.AttachFragment;

public abstract class BaseEditorFragment extends AttachFragment implements AdapterView.OnItemClickListener, ShellOutput.OnShellOutputListener, SearchView.OnQueryTextListener, SearchView.OnCloseListener {

    protected abstract PropAdapter getAdapter();

    @Override public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_editor, menu);

        // setup search
        final MenuItem searchItem = menu.findItem(R.id.action_search);
        final SearchView searchView = searchItem != null
                ? (SearchView) searchItem.getActionView()
                : null;
        if (searchView != null) {
            searchView.setOnQueryTextListener(this);
            searchView.setOnCloseListener(this);
        }

        super.onCreateOptionsMenu(menu, inflater);
    }


    @Override public boolean onQueryTextSubmit(String s) {
        return false;
    }

    @Override public boolean onQueryTextChange(String s) {
        final PropAdapter propAdapter = getAdapter();
        if (propAdapter != null) {
            propAdapter.filter(s);
        }
        return true;
    }

    @Override public boolean onClose() {
        final PropAdapter propAdapter = getAdapter();
        if (propAdapter != null) {
            propAdapter.filter(null);
        }
        return false;
    }

    @Override public boolean showBurger() { return false; }

}
