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

import com.apple.cocoa.foundation.NSScriptCommand;
import com.apple.cocoa.foundation.NSScriptCommandDescription;

import java.net.URL;

import org.apache.log4j.Logger;

import ch.cyberduck.core.*;

/**
* @version $Id$
 * @author Stuart A. Malone
 */
public class CDGetURLScriptCommand extends NSScriptCommand {
    private static Logger log = Logger.getLogger(CDGetURLScriptCommand.class);

    public CDGetURLScriptCommand(NSScriptCommandDescription commandDescription) {
        super(commandDescription);
    }

    public Object performDefaultImplementation() {
        String arg = (String)this.directParameter();
        log.debug("Received URL from Apple Event: " + arg);
        try {
            URL url = new URL(arg);
            String file = url.getFile();
            log.debug("File:" + file);
            Host h = new Host(url.getProtocol(),
							  url.getHost(),
							  url.getPort(),
							  new Login(url.getHost(), url.getUserInfo(), null),
							  url.getPath());
            if (file.length() > 1) {
                Path p = PathFactory.createPath(SessionFactory.createSession(h), file);
                // we assume a file has an extension
                if (null != p.getExtension()) {
                    log.debug("Assume downloading");
                    Queue queue = new Queue(Queue.KIND_DOWNLOAD);
                    queue.addRoot(p);
                    QueueList.instance().addItem(queue);
                    CDQueueController.instance().startItem(queue);
                    return null;
                }
            }
            CDBrowserController controller = new CDBrowserController();
            controller.mount(h);
        }
        catch (java.net.MalformedURLException e) {
            log.error(e.getMessage());
        }
        return null;
    }
}
