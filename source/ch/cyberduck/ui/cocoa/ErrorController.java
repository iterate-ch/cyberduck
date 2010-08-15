package ch.cyberduck.ui.cocoa;

/*
 *  Copyright (c) 2008 David Kocher. All rights reserved.
 *  http://cyberduck.ch/
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  Bug fixes, suggestions and comments should be sent to:
 *  dkocher@cyberduck.ch
 */

import ch.cyberduck.core.threading.BackgroundException;
import ch.cyberduck.ui.cocoa.application.NSTextField;
import ch.cyberduck.ui.cocoa.application.NSView;
import ch.cyberduck.ui.cocoa.foundation.NSAttributedString;

import org.apache.log4j.Logger;

/**
 * @version $Id:$
 */
public class ErrorController extends BundleController {
    private static Logger log = Logger.getLogger(TaskController.class);

    @Outlet
    private NSTextField hostField;

    public void setHostField(NSTextField hostField) {
        this.hostField = hostField;
        if(null == failure.getPath()) {
            this.hostField.setAttributedStringValue(
                    NSAttributedString.attributedStringWithAttributes(failure.getSession().getHost().toURL(), FIXED_WITH_FONT_ATTRIBUTES));
        }
        else {
            this.hostField.setAttributedStringValue(
                    NSAttributedString.attributedStringWithAttributes(failure.getPath().getAbsolute(), FIXED_WITH_FONT_ATTRIBUTES));
        }
    }

    @Outlet
    private NSTextField descriptionField;

    public void setDescriptionField(NSTextField descriptionField) {
        this.descriptionField = descriptionField;
        this.descriptionField.setSelectable(true);
        this.descriptionField.setAttributedStringValue(
                NSAttributedString.attributedStringWithAttributes(failure.getDetailedCauseMessage(), TRUNCATE_MIDDLE_ATTRIBUTES));
    }

    @Outlet
    private NSTextField errorField;

    public void setErrorField(NSTextField errorField) {
        this.errorField = errorField;
        this.errorField.setSelectable(true);
        this.errorField.setAttributedStringValue(
                NSAttributedString.attributedStringWithAttributes(failure.getReadableTitle() + ": " + failure.getMessage(), TRUNCATE_MIDDLE_ATTRIBUTES));
    }

    @Outlet
    private NSView view;

    public void setView(NSView view) {
        this.view = view;
    }

    @Override
    public NSView view() {
        return view;
    }

    private BackgroundException failure;

    public ErrorController(BackgroundException e) {
        this.failure = e;
        this.loadBundle();
    }

    /**
     * @return
     */
    public String getTooltip() {
        return failure.getReadableTitle();
    }

    @Override
    protected String getBundleName() {
        return "Error";
    }
}
