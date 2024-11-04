package ch.cyberduck.core.quicklook;

/*
 * Copyright (c) 2002-2020 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */

import ch.cyberduck.binding.foundation.NSURL;
import ch.cyberduck.binding.quicklook.QLPreviewItem;
import ch.cyberduck.binding.quicklook.QLPreviewPanel;
import ch.cyberduck.binding.quicklook.QLPreviewPanelDataSource;
import ch.cyberduck.core.Local;
import ch.cyberduck.ui.quicklook.QuickLook;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.rococoa.ID;
import org.rococoa.cocoa.foundation.NSInteger;

import java.util.ArrayList;
import java.util.List;

public final class QuartzQuickLook implements QuickLook {
    private static final Logger log = LogManager.getLogger(QuartzQuickLook.class);

    private final List<QLPreviewItem> previews
        = new ArrayList<QLPreviewItem>();

    @Override
    public void select(final List<Local> files) {
        log.debug("Select files for {}", files);
        previews.clear();
        for(final Local selected : files) {
            previews.add(new QLPreviewItem() {
                @Override
                public NSURL previewItemURL() {
                    return NSURL.fileURLWithPath(selected.getAbsolute());
                }

                @Override
                public String previewItemTitle() {
                    return selected.getDisplayName();
                }
            });
        }
    }

    private final QLPreviewPanelDataSource model = new QLPreviewPanelDataSource() {
        @Override
        public NSInteger numberOfPreviewItemsInPreviewPanel(QLPreviewPanel panel) {
            return new NSInteger(previews.size());
        }

        @Override
        public ID previewPanel_previewItemAtIndex(QLPreviewPanel panel, final int index) {
            return previews.get(index).id();
        }
    };

    @Override
    public boolean isOpen() {
        return QLPreviewPanel.sharedPreviewPanelExists()
            && QLPreviewPanel.sharedPreviewPanel().isVisible();
    }

    @Override
    public void open() {
        final QLPreviewPanel panel = QLPreviewPanel.sharedPreviewPanel();
        log.debug("Order front panel {}", panel);
        panel.makeKeyAndOrderFront(null);
        panel.setDataSource(model.id());
        log.debug("Reload data for panel {}", panel);
        panel.reloadData();
    }

    @Override
    public void close() {
        final QLPreviewPanel panel = QLPreviewPanel.sharedPreviewPanel();
        if(null != panel.currentController()) {
            log.debug("Order out panel {}", panel);
            panel.setDataSource(null);
            panel.orderOut(null);
        }
        log.debug("Clear previews");
        previews.clear();
    }
}
