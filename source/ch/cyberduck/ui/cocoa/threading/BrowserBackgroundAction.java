package ch.cyberduck.ui.cocoa.threading;

/*
 * Copyright (c) 2002-2013 David Kocher. All rights reserved.
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
 * Bug fixes, suggestions and comments should be sent to feedback@cyberduck.ch
 */

import ch.cyberduck.core.Session;
import ch.cyberduck.core.exception.ConnectionCanceledException;
import ch.cyberduck.core.threading.AlertCallback;
import ch.cyberduck.core.threading.BackgroundActionRegistry;
import ch.cyberduck.ui.cocoa.BrowserController;
import ch.cyberduck.ui.threading.ControllerBackgroundAction;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @version $Id$
 */
public abstract class BrowserBackgroundAction extends ControllerBackgroundAction {

    private BrowserController controller;

    private BackgroundActionRegistry registry
            = BackgroundActionRegistry.global();

    public BrowserBackgroundAction(final BrowserController controller) {
        this(controller, new PanelAlertCallback(controller));
        this.controller = controller;
    }

    public BrowserBackgroundAction(final BrowserController controller, final AlertCallback alert) {
        super(controller, alert, controller, controller);
        this.controller = controller;
    }

    @Override
    public List<Session<?>> getSessions() {
        final Session<?> session = controller.getSession();
        if(null == session) {
            return Collections.emptyList();
        }
        return new ArrayList<Session<?>>(Collections.singletonList(session));
    }

    @Override
    public void init() {
        // Add to the registry so it will be displayed in the activity window.
        registry.add(this);
        super.init();
    }

    @Override
    public void cleanup() {
        registry.remove(this);
        super.cleanup();
    }

    @Override
    public void prepare() throws ConnectionCanceledException {
        controller.getProgress().startAnimation(null);
        super.prepare();
    }

    @Override
    public void finish() {
        controller.getProgress().stopAnimation(null);
        super.finish();
    }
}