package ch.cyberduck.core.urlhandler;

/*
 * Copyright (c) 2002-2022 iterate GmbH. All rights reserved.
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
import ch.cyberduck.core.library.Native;

import org.rococoa.ID;
import org.rococoa.ObjCClass;
import org.rococoa.cocoa.foundation.NSError;

public abstract class WorkspaceSchemeHandlerProxy extends NSObject {

    static {
        Native.load("core");
    }

    private static final _Class CLASS = org.rococoa.Rococoa.createClass("WorkspaceSchemeHandlerProxy", _Class.class);

    public interface _Class extends ObjCClass {
        WorkspaceSchemeHandlerProxy alloc();
    }

    public static WorkspaceSchemeHandlerProxy create() {
        return CLASS.alloc().init();
    }

    public abstract WorkspaceSchemeHandlerProxy init();

    /**
     * @param callback A block that the system calls after reconnecting the domain.
     */
    public void setDefaultHandler(NSURL applicationURL, String scheme, ID callback) {
        this.setDefaultApplicationAtURL_toOpenURLsWithScheme_completionHandler(applicationURL, scheme, callback);
    }

    protected abstract void setDefaultApplicationAtURL_toOpenURLsWithScheme_completionHandler(NSURL applicationURL, String scheme, ID callback);

    public interface CompletionHandler {
        /**
         * @param error If an error occurs, this object contains information about the error; otherwise, it’s nil.
         */
        void didFinishWithError(NSError error);
    }
}
