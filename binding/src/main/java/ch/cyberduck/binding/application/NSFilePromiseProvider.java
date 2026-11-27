package ch.cyberduck.binding.application;

/*
 * Copyright (c) 2002-2026 iterate GmbH. All rights reserved.
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
import ch.cyberduck.binding.foundation.NSURL;

import org.rococoa.ID;
import org.rococoa.ObjCClass;
import org.rococoa.Rococoa;

/**
 * An object that provides a promise for the pasteboard. Available in macOS 10.12+
 */
public abstract class NSFilePromiseProvider extends NSObject {
    private static final _Class CLASS = Rococoa.createClass("NSFilePromiseProvider", _Class.class);

    public interface _Class extends ObjCClass {
        NSFilePromiseProvider alloc();
    }

    /**
     * @param fileType UTI of the promised file
     * @param delegate Delegate that writes the promised file. Weak reference only; the caller must retain the delegate.
     * @return Pasteboard writer to return from tableView:pasteboardWriterForRow:
     */
    public static NSFilePromiseProvider create(final String fileType, final ID delegate) {
        return CLASS.alloc().initWithFileType_delegate(fileType, delegate);
    }

    public abstract NSFilePromiseProvider initWithFileType_delegate(String fileType, ID delegate);

    public abstract String fileType();

    public interface Delegate {
        /**
         * @param provider File promise provider
         * @param fileType UTI of the promised file
         * @return Name of the file to write without path
         */
        String filePromiseProvider_fileNameForType(NSFilePromiseProvider provider, String fileType);

        /**
         * Write the contents of the promise to the specified URL. Called on the main queue.
         *
         * @param provider          File promise provider
         * @param url               Destination of the promised file
         * @param completionHandler Block of type void (^)(NSError *errorOrNil) to invoke once the file has been written
         *                          or writing failed. The receiver waits for the completion handler before reading the file.
         */
        void filePromiseProvider_writePromiseToURL_completionHandler(NSFilePromiseProvider provider, NSURL url, ID completionHandler);
    }
}
