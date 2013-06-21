package ch.cyberduck.ui.cocoa.threading;

/*
 * Copyright (c) 2002-2010 David Kocher. All rights reserved.
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

import ch.cyberduck.core.Session;
import ch.cyberduck.core.threading.BackgroundException;
import ch.cyberduck.ui.cocoa.BrowserController;

import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @version $Id$
 */
public abstract class BrowserBackgroundAction extends AlertRepeatableBackgroundAction {
    private static final Logger log = Logger.getLogger(BrowserBackgroundAction.class);

    private BrowserController controller;

    public BrowserBackgroundAction(final BrowserController controller) {
        super(controller);
        this.controller = controller;
    }

    public BrowserController getController() {
        return controller;
    }

    @Override
    public List<Session<?>> getSessions() {
        final Session<?> session = controller.getSession();
        return new ArrayList<Session<?>>(Collections.singletonList(session));
    }

    @Override
    public boolean prepare() {
        controller.invoke(new WindowMainAction(controller) {
            @Override
            public void run() {
                controller.getStatusSpinner().startAnimation(null);
                controller.updateStatusLabel(BrowserBackgroundAction.this.getActivity());
            }
        });
        return super.prepare();
    }

    @Override
    public void cancel() {
        if(this.isRunning()) {
            for(Session s : this.getSessions()) {
                try {
                    s.interrupt();
                }
                catch(BackgroundException e) {
                    this.error(e);
                }
            }
        }
        super.cancel();
    }

    @Override
    public void finish() throws BackgroundException {
        super.finish();
        controller.invoke(new WindowMainAction(controller) {
            @Override
            public void run() {
                controller.getStatusSpinner().stopAnimation(null);
                controller.updateStatusLabel();
            }
        });
    }
}