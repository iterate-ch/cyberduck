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

import com.apple.cocoa.foundation.*;
import com.apple.cocoa.application.*;

import org.apache.log4j.Logger;
import java.net.URL;

import ch.cyberduck.core.*;
import ch.cyberduck.core.ftp.FTPPath;
import ch.cyberduck.core.ftp.FTPSession;
import ch.cyberduck.ui.cocoa.CDBrowserController;
import ch.cyberduck.ui.cocoa.CDTransferController;

/**
* @author Stuart A. Malone
 */
public class CDGetURLScriptCommand extends NSScriptCommand {
    private static Logger log = Logger.getLogger(CDGetURLScriptCommand.class);

    public CDGetURLScriptCommand(NSScriptCommandDescription commandDescription) {
	super(commandDescription);
    }

    // @todo support other protocols than ftp
    public Object performDefaultImplementation() {
	String arg = (String)this.directParameter();
	log.debug("Received URL from Apple Event: "+arg);
	try {
	    URL url = new URL(arg);
	    Host h = new Host(url.getProtocol(), url.getHost(), url.getPort(), new Login(url.getUserInfo()));
	    String file = url.getFile();
	    if(file.length() > 1) {
		Path p = new FTPPath((FTPSession)h.getSession(), file);
		log.debug("Assuming download");
		CDTransferController controller = new CDTransferController(p.getSession(), p, Queue.KIND_DOWNLOAD);
		controller.transfer();
	    }
	    else {
		log.debug("Assuming browser");
		CDBrowserController controller = new CDBrowserController();
		controller.mount(h);
	    }
	}
	catch(java.net.MalformedURLException e) {
	    log.error(e.getMessage());
	}
	log.debug("return");
	return null;
    }
}
