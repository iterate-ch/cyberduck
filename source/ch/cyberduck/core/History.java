package ch.cyberduck.core;

/*
 *  Copyright (c) 2002 David Kocher. All rights reserved.
 *  http://icu.unizh.ch/~dkocher/
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

import org.apache.log4j.Logger;
import ch.cyberduck.core.Host;
import java.util.List;
import java.util.ArrayList;

/**
 * Keeps track of recently connected hosts
 * @version $Id$
 */
public class History extends ArrayList {
    private static Logger log = Logger.getLogger(History.class);

    private static History instance;

    private History() {
	//
    }

    public History instance() {
        if(null == instance) {
            String strVendor = System.getProperty("java.vendor");
            if(strVendor.indexOf("Apple") != -1)
                instance = new ch.cyberduck.ui.cocoa.CDHistoryImpl();
            else
                instance = new ch.cyberduck.ui.swing.HistoryImpl();
	}
        return instance;
    }

    /**
	* Ensure persistency.
     */
    public abstract void save();

    /**
	* Read from file into memory.
     */
    public abstract List restore();
}
