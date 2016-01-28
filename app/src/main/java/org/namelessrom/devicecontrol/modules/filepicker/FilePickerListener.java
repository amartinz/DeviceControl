package org.namelessrom.devicecontrol.modules.filepicker;

import java.io.File;

public interface FilePickerListener {
    void onFilePicked(final File file);

    void onFlashItemPicked(final FlashItem flashItem);
}
