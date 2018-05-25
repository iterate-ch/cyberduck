package ch.cyberduck.core.diagnostics;

/*
 * Copyright (c) 2002-2018 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */

import ch.cyberduck.binding.foundation.NSObject;
import ch.cyberduck.core.library.Native;

import org.rococoa.ObjCClass;
import org.rococoa.Rococoa;

public abstract class CDReachabilityMonitor extends NSObject {

    static {
        Native.load("core");
    }

    private static final _Class CLASS = Rococoa.createClass("CDReachabilityMonitor", _Class.class);

    public interface _Class extends ObjCClass {
        CDReachabilityMonitor alloc();
    }

    public static CDReachabilityMonitor monitorForUrl(final String url) {
        return CLASS.alloc().initWithUrl(url);
    }

    public abstract CDReachabilityMonitor initWithUrl(String url);

    public abstract void diagnoseInteractively();

    public abstract boolean startReachabilityMonitor();

    public abstract boolean stopReachabilityMonitor();

    public abstract boolean isReachable();
}
