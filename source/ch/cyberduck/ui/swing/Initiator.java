package ch.cyberduck.ui.swing;

/*
 *  Copyright (c) 2003 David Kocher. All rights reserved.
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

/**
* @version $Id$
 * Responsible for loading all application classes and
 * informing the progress model about the current state.
 * @see ch.cyberduck.SplashWindow
 */
public class Initiator extends Thread {
    private static Logger log = Logger.getLogger(Initiator.class);

    private SplashWindow splash;
    private javax.swing.BoundedRangeModel model;

    /**
     * @param s The splash window
     * @param m The progress model of this application loading process
     */
    public Initiator(SplashWindow s, javax.swing.BoundedRangeModel m) {
        log.debug("[Initiator]");
        this.splash = s;
        this.model = m;
    }
    
    public void run() {
        log.debug("[Initiator] run()");
        int pvalue = 0;
        // number of jobs to excecute until loading is complete
        int pcount = 5;
        int pincrement = ( this.model.getMaximum() / pcount );
        // 01 load main frame
        HostFrame main = new HostFrame();
        this.model.setValue( pvalue += pincrement );
        // 02 init actions
        main.initActions();
        this.model.setValue( pvalue += pincrement );
        // 03 init main panels
        main.initPanels();
        this.model.setValue( pvalue += pincrement );
        // 04 init listeners
        main.initListeners();
        this.model.setValue( pvalue += pincrement );
        // 05 loading bookmarks
//        main.initBookmarks();
        this.model.setValue( pvalue += pincrement );
        this.model.setValue( this.model.getMaximum() );
        main.setVisible(true);
        this.splash.enableDismissEvents();
        this.splash.dispose();
    }
}
