package ch.cyberduck.ui.cocoa;

/*
 *  Copyright (c) 2003 Stuart A. Malone. All rights reserved.
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

import com.apple.cocoa.application.NSApplication;
import com.apple.cocoa.foundation.NSScriptCommand;
import com.apple.cocoa.foundation.NSScriptCommandDescription;

import ch.cyberduck.core.*;
import ch.cyberduck.ui.cocoa.threading.DefaultMainAction;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

/**
 * @version $Id$
 */
public class CDApplescriptabilityController extends NSScriptCommand {
    private static Logger log = Logger.getLogger(CDApplescriptabilityController.class);

    public CDApplescriptabilityController(NSScriptCommandDescription commandDescription) {
        super(commandDescription);
    }

    public Object performDefaultImplementation() {
        log.debug("performDefaultImplementation");
        String arg = (String) this.directParameter();
        if(null == arg) {
            return ((CDMainController) NSApplication.sharedApplication().delegate()).newDocument();
        }
        log.debug("Received URL from Apple Event:" + arg);
        final Host h = Host.parse(arg);
        if(StringUtils.isNotEmpty(h.getDefaultPath())) {
            if(!h.getDefaultPath().endsWith(Path.DELIMITER)) {
                final Session s = SessionFactory.createSession(h);
                final Path p = PathFactory.createPath(s, h.getDefaultPath(), Path.FILE_TYPE);
//            final AttributedList<AbstractPath> list = p.list();
//            // Determine if listable directory
//            if(!list.attributes().isReadable()) {
                if(StringUtils.isNotBlank(p.getExtension())) {
                    CDMainApplication.invoke(new DefaultMainAction() {
                        public void run() {
                            CDTransferController.instance().startTransfer(new DownloadTransfer(p));
                        }
                    });
                    return null;
                }
//            }
            }
        }
        CDBrowserController doc = ((CDMainController) NSApplication.sharedApplication().delegate()).newDocument();
        doc.mount(h);
        return null;
    }
}
