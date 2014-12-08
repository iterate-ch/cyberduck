package ch.cyberduck.core.test;

/*
 * Copyright (c) 2002-2014 David Kocher. All rights reserved.
 * http://cyberduck.io/
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
 * feedback@cyberduck.io
 */

import ch.cyberduck.core.Factory;

import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;

/**
 * @version $Id$
 */
public final class PlatformAwareClassRunner extends BlockJUnit4ClassRunner {
    private Depends depends;

    public PlatformAwareClassRunner(final Class<?> klass) throws InitializationError {
        super(klass);
        depends = klass.getAnnotation(Depends.class);
    }

    private static Factory.Platform.Name p = Factory.Platform.getDefault();

    @Override
    protected void runChild(FrameworkMethod method, RunNotifier notifier) {
        if(depends == null) {
            super.runChild(method, notifier);
        }
        if(p.equals(depends.platform())) {
            super.runChild(method, notifier);
        }
        else {
            notifier.fireTestIgnored(describeChild(method));
        }
    }
}
