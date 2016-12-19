package ch.cyberduck.core.threading;

/*
 * Copyright (c) 2002-2016 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
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
 */

import ch.cyberduck.binding.foundation.NSObject;

import org.rococoa.ObjCClass;
import org.rococoa.Rococoa;

public abstract class NSOperation extends NSObject {
    public static final _Class CLASS = Rococoa.createClass(NSOperation.class.getSimpleName(), _Class.class);

    public interface _Class extends ObjCClass {
        NSOperation alloc();
    }

    public abstract NSOperation init();

    public abstract void start();

    public abstract void main();

    public abstract void cancel();

    public abstract void waitUntilFinished();

    public abstract boolean isCancelled();

    public abstract boolean isExecuting();

    public abstract boolean isFinished();

    public abstract boolean isConcurrent();

    public abstract boolean isReady();
}
