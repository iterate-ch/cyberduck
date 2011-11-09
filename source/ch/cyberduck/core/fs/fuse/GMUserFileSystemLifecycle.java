package ch.cyberduck.core.fs.fuse;

/*
 * Copyright (c) 2002-2011 David Kocher. All rights reserved.
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

import org.rococoa.ObjCObject;

/**
 * @version $Id:$
 */
public interface GMUserFileSystemLifecycle extends ObjCObject {
// The GMUserFileSystem's delegate can implement any of the below protocols.
// In most cases you can selectively choose which methods of a protocol to 
// implement.

    /*! @abstract Called just before the mount of the file system occurs. */
    void willMount();

    /*! @abstract Called just before an unmount of the file system will occur. */
    void willUnmount();

}