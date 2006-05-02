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

import ch.cyberduck.core.DownloadQueue;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathFactory;
import ch.cyberduck.core.Queue;
import ch.cyberduck.core.SessionFactory;

import com.apple.cocoa.application.NSApplication;
import com.apple.cocoa.foundation.NSScriptCommand;
import com.apple.cocoa.foundation.NSScriptCommandDescription;

import org.apache.log4j.Logger;

import java.io.IOException;

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
        if (null == arg) {
            return ((CDMainController) NSApplication.sharedApplication().delegate()).newDocument();
        }
        log.debug("Received URL from Apple Event:" + arg);
        try {
            final Host h = Host.parse(arg);
            if (h.getDefaultPath().length() > 1) {
                // We have to add this to the end of the main thread; there is some obscure
                // concurrency issue with the rendezvous initialization
                // running in CDMainController.applicationDidFinishLaunching, see ticket #????
                ((CDMainController) NSApplication.sharedApplication().delegate()).invoke(new Runnable() {
                    public void run() {
                        Path p = PathFactory.createPath(SessionFactory.createSession(h), h.getDefaultPath());
                        if(null != p.getExtension()) {
                            CDQueueController.instance().startItem(new DownloadQueue(p));
                        }
                    }
                });
            }
            else {
                CDBrowserController doc = ((CDMainController) NSApplication.sharedApplication().delegate()).newDocument();
                doc.mount(h);
            }
        }
        catch (java.net.MalformedURLException e) {
            log.error(e.getMessage());
        }
        return null;
    }
}
