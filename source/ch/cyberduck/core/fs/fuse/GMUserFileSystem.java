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

import ch.cyberduck.ui.cocoa.foundation.NSArray;

import org.rococoa.ID;
import org.rococoa.ObjCObject;
import org.rococoa.Rococoa;
import org.rococoa.cocoa.foundation.NSObject;

/**
 * @version $Id:$
 */
public abstract class GMUserFileSystem extends NSObject {
    public static final _class_ CLASS = Rococoa.createClass("GMUserFileSystem", _class_.class);

    public static GMUserFileSystem create(ID delegate, boolean isThreadSafe) {
        return CLASS.alloc().initWithDelegate_isThreadSafe(delegate, isThreadSafe);
    }

    public static abstract class _class_ extends NSObject._class_ {
        @Override
        public abstract GMUserFileSystem alloc();
    }

    /*! @abstract Error domain for GMUserFileSystem specific errors */
    public static final String kGMUserFileSystemErrorDomain = "kGMUserFileSystemErrorDomain";

    /*!
    * @abstract Key in notification dictionary for mount path
    * @discussion The value will be an NSString that is the mount path.
    */
    public static final String kGMUserFileSystemMountPathKey = "kGMUserFileSystemMountPathKey";

    /*! @abstract Key in notification dictionary for an error */
    public static final String kGMUserFileSystemErrorKey = "kGMUserFileSystemErrorKey";

    /*!
    * @abstract Notification sent when the mountAtPath operation fails.
    * @discussion The userInfo will contain an kGMUserFileSystemErrorKey with an
    * NSError* that describes the error.
    */
    public static final String kGMUserFileSystemMountFailed = "kGMUserFileSystemMountFailed";

    /*! @abstract Notification sent after the filesystem is successfully mounted. */
    public static final String kGMUserFileSystemDidMount = "kGMUserFileSystemDidMount";

    /*! @abstract Notification sent after the filesystem is successfully unmounted. */
    public static final String kGMUserFileSystemDidUnmount = "kGMUserFileSystemDidUnmount";

    /**
     * @param delegate     The file system delegate; implements the file system logic.<br>
     * @param isThreadSafe Is the file system delegate thread safe?<br>
     * @abstract Initialize the user space file system.<br>
     * @discussion The file system delegate should implement some or all of the<br>
     * GMUserFileSystemOperations informal protocol. You should only specify YES<br>
     * for isThreadSafe if your file system delegate is thread safe with respect to<br>
     * file system operations. That implies that it implements proper file system<br>
     * locking so that multiple operations on the same file can be done safely.<br>
     * @result A GMUserFileSystem instance.<br>
     * Original signature : <code>-(id)initWithDelegate:(id) isThreadSafe:(BOOL)</code><br>
     * <i>native declaration : line 96</i>
     */
    public abstract GMUserFileSystem initWithDelegate_isThreadSafe(ID delegate, boolean isThreadSafe);

    /**
     * Factory method<br>
     *
     * @see #initWithDelegate_isThreadSafe(ID, boolean)
     */
    public static GMUserFileSystem createWithDelegate_isThreadSafe(ID delegate, boolean isThreadSafe) {
        return CLASS.alloc().initWithDelegate_isThreadSafe(delegate, isThreadSafe);
    }

    /**
     * @param delegate The delegate to use from now on for this file system.<br>
     *                 Original signature : <code>-(void)setDelegate:(id)</code><br>
     *                 <i>native declaration : line 102</i>
     * @abstract Set the file system delegate.<br>
     */
    public abstract void setDelegate(ObjCObject delegate);

    /**
     * @abstract Get the file system delegate.<br>
     * @result The file system delegate.<br>
     * Original signature : <code>-(id)delegate</code><br>
     * <i>native declaration : line 108</i>
     */
    public abstract NSObject delegate();

    /**
     * @param mountPath The path to mount on, e.g. /Volumes/MyFileSystem<br>
     * @param options   The set of mount time options to use.<br>
     *                  Original signature : <code>-(void)mountAtPath:(String) withOptions:(NSArray*)</code><br>
     *                  <i>native declaration : line 122</i>
     * @abstract Mount the file system at the given path.<br>
     * @discussion Mounts the file system at mountPath with the given set of options.<br>
     * The set of available options can be found on the <br>
     * <a href="http://code.google.com/p/macfuse/wiki/OPTIONS">options</a> wiki page.<br>
     * For example, to turn on debug output add \@"debug" to the options NSArray.<br>
     * If the mount succeeds, then a kGMUserFileSystemDidMount notification is posted<br>
     * to the default noification center. If the mount fails, then a <br>
     * kGMUserFileSystemMountFailed notification will be posted instead.<br>
     */
    public abstract void mountAtPath_withOptions(String mountPath, NSArray options);

    /**
     * @param mountPath        The path to mount on, e.g. /Volumes/MyFileSystem<br>
     * @param options          The set of mount time options to use.<br>
     * @param shouldForeground Should the file system thread remain foreground rather <br>
     *                         than daemonize? (Recommend: YES)<br>
     * @param detachNewThread  Should the file system run in a new thread rather than<br>
     *                         the current one? (Recommend: YES)<br>
     *                         Original signature : <code>-(void)mountAtPath:(String) withOptions:(NSArray*) shouldForeground:(BOOL) detachNewThread:(BOOL)</code><br>
     *                         <i>native declaration : line 142</i>
     * @abstract Mount the file system at the given path with advanced options.<br>
     * @discussion Mounts the file system at mountPath with the given set of options.<br>
     * This is an advanced version of @link mountAtPath:withOptions: mountAtPath:withOptions @/link<br>
     * You can use this to mount from a command-line program as follows:<ul><br>
     * <li>For an app, use: shouldForeground=YES, detachNewThread=YES<br>
     * <li>For a daemon: shouldForeground=NO, detachNewThread=NO<br>
     * <li>For debug output: shouldForeground=YES, detachNewThread=NO<br>
     * <li>For a daemon+runloop:  shouldForeground=NO, detachNewThread=YES<br>
     * - NOTE: I've never tried daemon+runloop; maybe it doesn't make sense</ul><br>
     */
    public abstract void mountAtPath_withOptions_shouldForeground_detachNewThread(String mountPath, NSArray options, boolean shouldForeground, boolean detachNewThread);

    /**
     * @abstract Unmount the file system.<br>
     * @discussion Unmounts the file system. The kGMUserFileSystemDidUnmount<br>
     * notification will be posted.<br>
     * Original signature : <code>-(void)unmount</code><br>
     * <i>native declaration : line 152</i>
     */
    public abstract void unmount();
}
