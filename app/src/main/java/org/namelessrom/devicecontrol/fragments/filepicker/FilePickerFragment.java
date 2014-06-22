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
package org.namelessrom.devicecontrol.fragments.filepicker;

import android.app.ListFragment;
import android.os.Bundle;
import android.view.View;

import com.squareup.otto.Subscribe;

import org.namelessrom.devicecontrol.events.FlashItemEvent;
import org.namelessrom.devicecontrol.events.ShellOutputEvent;
import org.namelessrom.devicecontrol.events.listeners.OnBackPressedListener;
import org.namelessrom.devicecontrol.objects.FlashItem;
import org.namelessrom.devicecontrol.utils.ContentTypes;
import org.namelessrom.devicecontrol.utils.Utils;
import org.namelessrom.devicecontrol.utils.providers.BusProvider;

import java.io.File;
import java.util.ArrayList;

import static org.namelessrom.devicecontrol.Application.logDebug;

/**
 * A class for picking a file
 */
public class FilePickerFragment extends ListFragment implements OnBackPressedListener {

    public static final String ARG_FILE_TYPE = "arg_file_type";

    private static final int ID_GET_FILES = 100;

    private String root        = "/";
    private String currentPath = "/";

    private FileAdapter fileAdapter;
    private ArrayList<String> breadcrumbs = new ArrayList<String>();

    private String fileType = "";

    @Override public void onResume() {
        super.onResume();
        BusProvider.getBus().register(this);
    }

    @Override public void onPause() {
        super.onPause();
        BusProvider.getBus().unregister(this);
    }

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Bundle bundle = getArguments();
        if (bundle != null) {
            fileType = bundle.getString(ARG_FILE_TYPE, fileType);
        }
    }

    @Override public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // TODO: restore instance state
        loadFiles(root, true);
    }

    private void loadFiles(final String path, final boolean isBreadcrumb) {
        currentPath = path;
        if (isBreadcrumb) {
            breadcrumbs.add(path);
        }
        Utils.getCommandResult(ID_GET_FILES, String.format("ls %s", path), "", true);
    }

    @Subscribe public void onFile(final File f) {
        currentPath = f.getAbsolutePath() + File.separator;
        if (currentPath.endsWith("../")) {
            onBackPressed();
            return;
        }

        logDebug(String.format("onFile(%s)", currentPath));
        loadFiles(currentPath, true);
    }

    @Subscribe public void onFlashItem(final FlashItem item) {
        if (!ContentTypes.isFiletypeMatching(item.getName(), fileType)) return;

        logDebug(String.format("filePicked(%s)", item.getPath()));
        BusProvider.getBus().post(new FlashItemEvent(item));
    }

    @Subscribe public void onShellOutputEvent(final ShellOutputEvent event) {
        if (event == null) return;
        final int id = event.getId();
        switch (id) {
            case ID_GET_FILES:
                final String[] output = event.getOutput().split("\n");
                final ArrayList<File> fileList = new ArrayList<File>(output.length);
                if (!currentPath.equals(root)) {
                    fileList.add(new File(currentPath + File.separator + "../"));
                }
                for (final String s : output) {
                    if (s.isEmpty()) continue;
                    fileList.add(new File(currentPath + File.separator + s));
                }
                fileAdapter = new FileAdapter(fileList);
                fileAdapter.setFileType(fileType);
                setListAdapter(fileAdapter);
                break;
        }
    }

    @Override public boolean onBackPressed() {
        if (!currentPath.equals(root)) {
            if (!breadcrumbs.isEmpty() && breadcrumbs.get(breadcrumbs.size() - 1) != null) {
                breadcrumbs.remove(breadcrumbs.size() - 1);
            }
            loadFiles(breadcrumbs.get(breadcrumbs.size() - 1), false);
            return true;
        }
        return false;
    }
}
