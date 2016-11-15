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

public interface QLPreviewPanelController {

    /**
     * @param panel The Preview Panel looking for a controller.
     * @ Sent to each object in the responder chain to find a controller.
     * @result YES if the receiver accepts to control the panel. You should never call this method directly.
     */
    boolean acceptsPreviewPanelControl(QLPreviewPanel panel);

    /**
     * @param panel The Preview Panel the receiver will control.
     * @ Sent to the object taking control of the Preview Panel.
     * @discussion The receiver should setup the preview panel (data source, delegate, binding, etc.) here. You should never call this method directly.
     */
    void beginPreviewPanelControl(QLPreviewPanel panel);

    /**
     * @param panel The Preview Panel that the receiver will stop controlling.
     * @ Sent to the object in control of the Preview Panel just before stopping its control.
     * @discussion The receiver should unsetup the preview panel (data source, delegate, binding, etc.) here. You should never call this method directly.
     */
    void endPreviewPanelControl(QLPreviewPanel panel);
}
