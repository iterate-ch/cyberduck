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
import org.rococoa.cocoa.foundation.NSArray;
import org.rococoa.cocoa.foundation.NSInteger;
import org.rococoa.cocoa.foundation.NSUInteger;

public abstract class NSOperationQueue extends NSObject {
    public static final _Class CLASS = Rococoa.createClass(NSOperationQueue.class.getSimpleName(), _Class.class);

    public static final int NSOperationQueueDefaultMaxConcurrentOperationCount = -1;

    public interface _Class extends ObjCClass {
        NSOperationQueue alloc();

        NSOperationQueue currentQueue();

        NSOperationQueue mainQueue();
    }

    public abstract NSOperationQueue init();

    public abstract void addOperation(NSOperation operation);

    public abstract void addOperations_waitUntilFinished(NSArray ops, boolean wait);

    public abstract void cancelAllOperations();

    public abstract boolean isSuspended();

    public abstract NSInteger maxConcurrentOperationCount();

    public abstract String name();

    public abstract NSUInteger operationCount();

    public abstract NSArray operations();

    public abstract void setMaxConcurrentOperationCount(NSInteger count);

    public abstract void setName(String name);

    public abstract void setSuspended(boolean suspend);

    public abstract void waitUntilAllOperationsAreFinished();
}
