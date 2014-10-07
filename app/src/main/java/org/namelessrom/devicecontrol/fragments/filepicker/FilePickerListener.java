package org.namelessrom.devicecontrol.fragments.filepicker;

import org.namelessrom.devicecontrol.objects.FlashItem;

import java.io.File;

/**
 * Created by alex on 07.10.14.
 */
public interface FilePickerListener {
    public void onFilePicked(final File file);

    public void onFlashItemPicked(final FlashItem flashItem);
}
