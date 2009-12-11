package ch.cyberduck.ui.cocoa;

/*
 *  Copyright (c) 2005 David Kocher. All rights reserved.
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

import ch.cyberduck.core.*;
import ch.cyberduck.ui.cocoa.application.NSTextField;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

/**
 * @version $Id$
 */
public class DownloadController extends SheetController {
    private static Logger log = Logger.getLogger(DownloadController.class);

    @Outlet
    private NSTextField urlField;

    public void setUrlField(NSTextField urlField) {
        this.urlField = urlField;
        this.updateField(this.urlField, url);
    }

    private String url;

    public DownloadController(final WindowController parent) {
        super(parent);
    }

    public DownloadController(final WindowController parent, final String url) {
        this(parent);
        this.url = url;
    }

    @Override
    protected String getBundleName() {
        return "Download";
    }

    public void callback(final int returncode) {
        if(returncode == DEFAULT_OPTION) {
            Host host = Host.parse(urlField.stringValue());
            final Transfer transfer = new DownloadTransfer(
                    PathFactory.createPath(SessionFactory.createSession(host),
                            host.getDefaultPath(), Path.FILE_TYPE)
            );
            TransferController.instance().startTransfer(transfer);
        }
    }

    @Override
    protected boolean validateInput() {
        Host host = Host.parse(urlField.stringValue());
        return StringUtils.isNotBlank(host.getDefaultPath());
    }
}