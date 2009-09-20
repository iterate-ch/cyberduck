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
import ch.cyberduck.ui.cocoa.application.NSWindow;
import ch.cyberduck.ui.cocoa.foundation.NSNotification;
import ch.cyberduck.ui.cocoa.foundation.NSNotificationCenter;
import ch.cyberduck.ui.cocoa.foundation.NSURL;
import org.apache.log4j.Logger;
import org.rococoa.Foundation;
import org.rococoa.ID;
import org.rococoa.cocoa.foundation.NSInteger;

import java.util.HashMap;
import java.util.Map;

/**
 * @version $Id$
 */
public class QuartzQuickLook extends AbstractQuickLook {
    private static Logger log = Logger.getLogger(QuartzQuickLook.class);

    private Map<Local, QLPreviewItem> previews = new HashMap<Local, QLPreviewItem>();

    private QLPreviewPanelDataSource model = new QLPreviewPanelDataSource() {

        @Override
        public NSInteger numberOfPreviewItemsInPreviewPanel(QLPreviewPanel panel) {
            return new NSInteger(selected.size());
        }

        @Override
        public ID previewPanel_previewItemAtIndex(QLPreviewPanel panel, final int index) {
            final Local preview = selected.get(index);
            if(!previews.containsKey(preview)) {
                final QLPreviewItem item = new QLPreviewItem() {
                    @Override
                    public NSURL previewItemURL() {
                        return NSURL.fileURLWithPath(preview.getAbsolute());
                    }

                    @Override
                    public String previewItemTitle() {
                        return preview.getName();
                    }
                };
                previews.put(preview, item);
            }
            return previews.get(preview).id();
        }
    };

    protected QuartzQuickLook() {
        ;
    }

    private CDController windowListener = new CDController() {
        public void windowWillClose(NSNotification notification) {
            log.debug("windowWillClose:" + notification);
            previews.clear();
            QuartzQuickLook.super.didEndQuickLook();
        }
    };

    @Override
    public void select(final Collection<Local> files) {
        log.debug("select");
        super.select(files);
        final QLPreviewPanel panel = QLPreviewPanel.sharedPreviewPanel();
        // The Preview Panel automatically updates its controller (by searching the responder
        // chain) whenever the main or key window changes. Invoke updateController if
        // the responder chain changes without explicit notice
        panel.updateController();
        panel.reloadData();
    }

    public boolean isAvailable() {
        return null != QLPreviewPanel.sharedPreviewPanel();
    }

    public boolean isOpen() {
        return QLPreviewPanel.sharedPreviewPanelExists()
                && QLPreviewPanel.sharedPreviewPanel().isVisible();
    }

    @Override
    public void willBeginQuickLook() {
        final QLPreviewPanel panel = QLPreviewPanel.sharedPreviewPanel();
        panel.setDataSource(this.model.id());
        super.willBeginQuickLook();
    }

    public void open() {
        final QLPreviewPanel panel = QLPreviewPanel.sharedPreviewPanel();
        NSNotificationCenter.defaultCenter().addObserver(windowListener.id(),
                Foundation.selector("windowWillClose:"),
                NSWindow.WindowWillCloseNotification,
                panel);
        panel.makeKeyAndOrderFront(null);
    }

    public void close() {
        final QLPreviewPanel panel = QLPreviewPanel.sharedPreviewPanel();
        NSNotificationCenter.defaultCenter().removeObserver(panel.id());
        panel.orderOut(null);
    }

    @Override
    public void didEndQuickLook() {
        final QLPreviewPanel panel = QLPreviewPanel.sharedPreviewPanel();
        panel.setDataSource(null);
    }
}
