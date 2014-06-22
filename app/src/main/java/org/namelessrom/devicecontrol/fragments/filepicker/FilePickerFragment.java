package org.namelessrom.devicecontrol.fragments.filepicker;

import android.app.ListFragment;
import android.os.Bundle;
import android.view.View;

import com.squareup.otto.Subscribe;

import org.namelessrom.devicecontrol.events.ShellOutputEvent;
import org.namelessrom.devicecontrol.objects.FlashItem;
import org.namelessrom.devicecontrol.utils.Utils;
import org.namelessrom.devicecontrol.utils.providers.BusProvider;

import java.io.File;
import java.util.ArrayList;

import static org.namelessrom.devicecontrol.Application.logDebug;

/**
 * Created by alex on 22.06.14.
 */
public class FilePickerFragment extends ListFragment {

    private static final int ID_GET_FILES = 100;

    private String root = "/";

    private FileAdapter fileAdapter;

    @Override public void onResume() {
        super.onResume();
        BusProvider.getBus().register(this);
    }

    @Override public void onPause() {
        super.onPause();
        BusProvider.getBus().unregister(this);
    }

    @Override public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        loadFiles();
    }

    private void filePicked(final FlashItem item) {
        BusProvider.getBus().post(item);
    }

    private void loadFiles() {
        Utils.getCommandResult(ID_GET_FILES, String.format("ls %s", root), "", true);
    }

    @Subscribe public void onFile(final File f) {
        root = f.getAbsolutePath() + File.separator;
        logDebug(String.format("onFile(%s)", root));
        loadFiles();
    }

    @Subscribe public void onShellOutputEvent(final ShellOutputEvent event) {
        if (event == null) return;
        final int id = event.getId();
        switch (id) {
            case ID_GET_FILES:
                final String[] output = event.getOutput().split("\n");
                final ArrayList<File> fileList = new ArrayList<File>(output.length);
                for (final String s : output) {
                    fileList.add(new File(root + File.separator + s));
                }
                fileAdapter = new FileAdapter(fileList.toArray(new File[fileList.size()]));
                setListAdapter(fileAdapter);
                break;
        }
    }

}
