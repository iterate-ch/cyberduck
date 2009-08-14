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
import ch.cyberduck.ui.cocoa.CDController;
import ch.cyberduck.ui.cocoa.foundation.NSURL;

import org.rococoa.ID;

import java.util.ArrayList;
import java.util.List;

/**
 * @version $Id:$
 */
public class QuartzQuickLook extends CDController implements IQuickLook {

    private QLPreviewPanelDataSource model;

    protected QuartzQuickLook() {
        ;
    }

    /**
     *
     */
    private List<QLPreviewItem> items
            = new ArrayList<QLPreviewItem>();

    public void select(final Collection<Local> files) {
        items.clear();
        final QLPreviewPanel panel = QLPreviewPanel.sharedPreviewPanel();
        panel.updateController();
        panel.setDataSource((this.model = new QLPreviewPanelDataSource() {
            @Override
            public int numberOfPreviewItemsInPreviewPanel(QLPreviewPanel panel) {
                return files.size();
            }

            @Override
            public ID previewPanel_previewItemAtIndex(QLPreviewPanel panel, final int index) {
                final QLPreviewItem item = new QLPreviewItem() {
                    @Override
                    public NSURL previewItemURL() {
                        return NSURL.fileURLWithPath(files.get(index).getAbsolute());
                    }

                    @Override
                    public String previewItemTitle() {
                        return files.get(index).getName();
                    }
                };
                items.add(item);
                return item.id();
            }
        }).id());
        panel.reloadData();
    }

    public boolean isAvailable() {
        return null != QLPreviewPanel.sharedPreviewPanel();
    }

    public boolean isOpen() {
        return QLPreviewPanel.sharedPreviewPanelExists()
                && QLPreviewPanel.sharedPreviewPanel().isVisible();
    }

    public void open() {
        QLPreviewPanel.sharedPreviewPanel().makeKeyAndOrderFront(null);
    }

    public void close() {
        final QLPreviewPanel panel = QLPreviewPanel.sharedPreviewPanel();
        panel.orderOut(null);
        panel.setDataSource(null);
    }
}
