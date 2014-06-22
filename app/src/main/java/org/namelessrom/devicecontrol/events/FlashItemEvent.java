package org.namelessrom.devicecontrol.events;

import org.namelessrom.devicecontrol.objects.FlashItem;

public class FlashItemEvent {
    private final FlashItem item;

    public FlashItemEvent(final FlashItem item) { this.item = item; }

    public FlashItem getItem() { return this.item; }
}
