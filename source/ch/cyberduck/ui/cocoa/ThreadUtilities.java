package ch.cyberduck.ui.cocoa;

/*
 *  Copyright (c) 2004 David Kocher. All rights reserved.
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

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

public class ThreadUtilities {
    private static Logger log = Logger.getLogger(ThreadUtilities.class);

    private static ThreadUtilities instance;

    private List queue;

    private ThreadUtilities() {
        this.queue = new ArrayList();
    }

    public static ThreadUtilities instance() {
        if (null == instance) {
            instance = new ThreadUtilities();
        }
        return instance;
    }

    public synchronized void invokeLater(Runnable thread) {
        //log.debug("invokeLater:"+thread);
        this.queue.add(thread);
    }

    public synchronized Runnable next() {
        //log.debug("next");
//		if(log.isDebugEnabled())
//			log.debug("Size of thread queue:"+queue.size()+" objects waiting for removal");
        if (this.queue.isEmpty()) {
            return null;
        }
        return (Runnable)this.queue.remove(0);
    }
}