package ch.cyberduck.ui.cocoa.quicklook;

/*
 * Copyright (c) 2002-2009 David Kocher. All rights reserved.
 *
 * http://cyberduck.ch/
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * Bug fixes, suggestions and comments should be sent to:
 * dkocher@cyberduck.ch
 */

import ch.cyberduck.core.Collection;
import ch.cyberduck.core.Local;
import ch.cyberduck.ui.cocoa.foundation.NSURL;

import org.apache.log4j.Logger;
import org.rococoa.ID;
import org.rococoa.cocoa.foundation.NSInteger;

import java.util.ArrayList;
import java.util.List;

/**
 * @version $Id$
 */
public class QuartzQuickLook extends AbstractQuickLook {
    private static Logger log = Logger.getLogger(QuartzQuickLook.class);

    public static void register() {
        if(Factory.VERSION_PLATFORM.matches("10\\.6.*")) {
            QuickLookFactory.addFactory(Factory.VERSION_PLATFORM, new Factory());
        }
    }

    private static class Factory extends QuickLookFactory {
        @Override
        protected QuickLookInterface create() {
            return new QuartzQuickLook();
        }
    }

    private List<QLPreviewItem> previews = new ArrayList<QLPreviewItem>();

    @Override
    public void select(final Collection<Local> files) {
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
        super.select(files);
    }

    private QLPreviewPanelDataSource model = new QLPreviewPanelDataSource() {
        @Override
        public NSInteger numberOfPreviewItemsInPreviewPanel(QLPreviewPanel panel) {
            return new NSInteger(previews.size());
        }

        @Override
        public ID previewPanel_previewItemAtIndex(QLPreviewPanel panel, final int index) {
            return previews.get(index).id();
        }
    };

    final QLPreviewPanel panel;

    private QuartzQuickLook() {
        panel = QLPreviewPanel.sharedPreviewPanel();
    }

    public boolean isAvailable() {
        return null != panel;
    }

    public boolean isOpen() {
        return QLPreviewPanel.sharedPreviewPanelExists() && panel.isVisible();
    }

    @Override
    public void willBeginQuickLook() {
        panel.setDataSource(this.model.id());
        super.willBeginQuickLook();
    }

    public void open() {
        panel.makeKeyAndOrderFront(null);
        if(null == panel.dataSource()) {
            // Do not reload data yet because datasource is not yet setup.
            // Focus has probably changed to another application since
            return;
        }
        panel.reloadData();
    }

    public void close() {
        panel.orderOut(null);
    }

    @Override
    public void didEndQuickLook() {
        panel.setDataSource(null);
        super.didEndQuickLook();
    }
}
