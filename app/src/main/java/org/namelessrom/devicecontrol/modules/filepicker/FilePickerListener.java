package org.namelessrom.devicecontrol.modules.filepicker;

import org.namelessrom.devicecontrol.objects.FlashItem;

import java.io.File;

public interface FilePickerListener {
    void onFilePicked(final File file);

    void onFlashItemPicked(final FlashItem flashItem);
}
