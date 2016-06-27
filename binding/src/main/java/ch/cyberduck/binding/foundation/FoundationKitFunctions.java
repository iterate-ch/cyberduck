package ch.cyberduck.binding.foundation;

/*
 * Copyright (c) 2002-2009 David Kocher. All rights reserved.
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

import org.rococoa.cocoa.foundation.NSPoint;
import org.rococoa.cocoa.foundation.NSRect;
import org.rococoa.cocoa.foundation.NSSize;

import com.sun.jna.Library;

public interface FoundationKitFunctions extends Library {
    /**
     * <i>native declaration : /System/Library/Frameworks/ApplicationServices.framework/Headers/../Frameworks/CoreGraphics.framework/Headers/CGGeometry.h:36</i><br>
     * enum values
     */
    public static interface CGRectEdge {
        public static final int CGRectMinXEdge = 0;
        public static final int CGRectMinYEdge = 1;
        public static final int CGRectMaxXEdge = 2;
        public static final int CGRectMaxYEdge = 3;
    }

    /**
     * <i>native declaration : /System/Library/Frameworks/ApplicationServices.framework/Headers/../Frameworks/CoreGraphics.framework/Headers/CGGeometry.h</i><br>
     * enum values
     */
    public static interface NSRectEdge {
        public static final int NSMinXEdge = 0;
        public static final int NSMinYEdge = 1;
        public static final int NSMaxXEdge = 2;
        public static final int NSMaxYEdge = 3;
    }

    /**
     * <i>native declaration : /System/Library/Frameworks/Foundation.framework/Headers/NSPathUtilities.h</i><br>
     * enum values
     */
    public static interface NSSearchPathDirectory {
        /// supported applications (Applications)
        public static final int NSApplicationDirectory = 1;
        /// unsupported applications, demonstration versions (Demos)
        public static final int NSDemoApplicationDirectory = 2;
        /// developer applications (Developer/Applications). DEPRECATED - there is no one single Developer directory.
        public static final int NSDeveloperApplicationDirectory = 3;
        /// system and network administration applications (Administration)
        public static final int NSAdminApplicationDirectory = 4;
        /// various user-visible documentation, support, and configuration files, resources (Library)
        public static final int NSLibraryDirectory = 5;
        /// developer resources (Developer) DEPRECATED - there is no one single Developer directory.
        public static final int NSDeveloperDirectory = 6;
        /// user home directories (Users)
        public static final int NSUserDirectory = 7;
        /// documentation (Documentation)
        public static final int NSDocumentationDirectory = 8;
        /// documents (Documents)
        public static final int NSDocumentDirectory = 9;
        /// location of CoreServices directory (System/Library/CoreServices)
        public static final int NSCoreServiceDirectory = 10;
        /// location of autosaved documents (Documents/Autosaved)
        public static final int NSAutosavedInformationDirectory = 11;
        /// location of user's desktop
        public static final int NSDesktopDirectory = 12;
        /// location of discardable cache files (Library/Caches)
        public static final int NSCachesDirectory = 13;
        /// location of application support files (plug-ins, etc) (Library/Application Support)
        public static final int NSApplicationSupportDirectory = 14;
        /// location of the user's "Downloads" directory
        public static final int NSDownloadsDirectory = 15;
        /// input methods (Library/Input Methods)
        public static final int NSInputMethodsDirectory = 16;
        /// location of user's Movies directory (~/Movies)
        public static final int NSMoviesDirectory = 17;
        /// location of user's Music directory (~/Music)
        public static final int NSMusicDirectory = 18;
        /// location of user's Pictures directory (~/Pictures)
        public static final int NSPicturesDirectory = 19;
        /// location of system's PPDs directory (Library/Printers/PPDs)
        public static final int NSPrinterDescriptionDirectory = 20;
        /// location of user's Public sharing directory (~/Public)
        public static final int NSSharedPublicDirectory = 21;
        /// location of the PreferencePanes directory for use with System Preferences (Library/PreferencePanes)
        public static final int NSPreferencePanesDirectory = 22;
        /// For use with NSFileManager's URLForDirectory:inDomain:appropriateForURL:create:error:
        public static final int NSItemReplacementDirectory = 99;
        /// all directories where applications can occur
        public static final int NSAllApplicationsDirectory = 100;
        /// all directories where resources can occur
        public static final int NSAllLibrariesDirectory = 101;
    }

    /**
     * <i>native declaration : /System/Library/Frameworks/Foundation.framework/Headers/NSPathUtilities.h</i><br>
     * enum values
     */
    public static interface NSSearchPathDomainMask {
        /// user's home directory --- place to install user's personal items (~)
        public static final int NSUserDomainMask = 1;
        /// local to the current machine --- place to install items available to everyone on this machine (/Library)
        public static final int NSLocalDomainMask = 2;
        /// publically available location in the local area network --- place to install items available on the network (/Network)
        public static final int NSNetworkDomainMask = 4;
        /// provided by Apple, unmodifiable (/System)
        public static final int NSSystemDomainMask = 8;
        /// all domains: all of the above and future items
        public static final int NSAllDomainsMask = 65535;
    }

    /**
     * Original signature : <code>BOOL NSEqualPoints(NSPoint, NSPoint)</code><br>
     * <i>native declaration : /System/Library/Frameworks/ApplicationServices.framework/Headers/../Frameworks/CoreGraphics.framework/Headers/CGGeometry.h:447</i>
     */
    boolean NSEqualPoints(NSPoint aPoint, NSPoint bPoint);

    /**
     * Original signature : <code>BOOL NSEqualSizes(NSSize, NSSize)</code><br>
     * <i>native declaration : /System/Library/Frameworks/ApplicationServices.framework/Headers/../Frameworks/framework/Headers/CGGeometry.h:448</i>
     */
    boolean NSEqualSizes(NSSize aSize, NSSize bSize);

    /**
     * Original signature : <code>BOOL NSEqualRects(NSRect, NSRect)</code><br>
     * <i>native declaration : /System/Library/Frameworks/ApplicationServices.framework/Headers/../Frameworks/framework/Headers/CGGeometry.h:449</i>
     */
    boolean NSEqualRects(NSRect aRect, NSRect bRect);

    /**
     * Original signature : <code>BOOL NSIsEmptyRect(NSRect)</code><br>
     * <i>native declaration : /System/Library/Frameworks/ApplicationServices.framework/Headers/../Frameworks/framework/Headers/CGGeometry.h:450</i>
     */
    boolean NSIsEmptyRect(NSRect aRect);

    /**
     * Original signature : <code>NSRect NSInsetRect(NSRect, CGFloat, CGFloat)</code><br>
     * <i>native declaration : /System/Library/Frameworks/ApplicationServices.framework/Headers/../Frameworks/framework/Headers/CGGeometry.h:452</i>
     */
    NSRect NSInsetRect(NSRect aRect, org.rococoa.cocoa.CGFloat dX, org.rococoa.cocoa.CGFloat dY);

    /**
     * Original signature : <code>NSRect NSIntegralRect(NSRect)</code><br>
     * <i>native declaration : /System/Library/Frameworks/ApplicationServices.framework/Headers/../Frameworks/framework/Headers/CGGeometry.h:453</i>
     */
    NSRect NSIntegralRect(NSRect aRect);

    /**
     * Original signature : <code>NSRect NSUnionRect(NSRect, NSRect)</code><br>
     * <i>native declaration : /System/Library/Frameworks/ApplicationServices.framework/Headers/../Frameworks/framework/Headers/CGGeometry.h:454</i>
     */
    NSRect NSUnionRect(NSRect aRect, NSRect bRect);

    /**
     * Original signature : <code>NSRect NSIntersectionRect(NSRect, NSRect)</code><br>
     * <i>native declaration : /System/Library/Frameworks/ApplicationServices.framework/Headers/../Frameworks/framework/Headers/CGGeometry.h:455</i>
     */
    NSRect NSIntersectionRect(NSRect aRect, NSRect bRect);

    /**
     * Original signature : <code>NSRect NSOffsetRect(NSRect, CGFloat, CGFloat)</code><br>
     * <i>native declaration : /System/Library/Frameworks/ApplicationServices.framework/Headers/../Frameworks/framework/Headers/CGGeometry.h:456</i>
     */
    NSRect NSOffsetRect(NSRect aRect, org.rococoa.cocoa.CGFloat dX, org.rococoa.cocoa.CGFloat dY);

    /**
     * Original signature : <code>void NSDivideRect(NSRect, NSRect*, NSRect*, CGFloat, NSRectEdge)</code><br>
     * <i>native declaration : /System/Library/Frameworks/ApplicationServices.framework/Headers/../Frameworks/framework/Headers/CGGeometry.h:457</i><br>
     *
     * @param edge @see NSRectEdge
     */
    void NSDivideRect(NSRect inRect, NSRect slice, NSRect rem, org.rococoa.cocoa.CGFloat amount, int edge);

    /**
     * Original signature : <code>BOOL NSPointInRect(NSPoint, NSRect)</code><br>
     * <i>native declaration : /System/Library/Frameworks/ApplicationServices.framework/Headers/../Frameworks/framework/Headers/CGGeometry.h:458</i>
     */
    boolean NSPointInRect(NSPoint aPoint, NSRect aRect);

    /**
     * Original signature : <code>BOOL NSMouseInRect(NSPoint, NSRect, BOOL)</code><br>
     * <i>native declaration : /System/Library/Frameworks/ApplicationServices.framework/Headers/../Frameworks/framework/Headers/CGGeometry.h:459</i>
     */
    boolean NSMouseInRect(NSPoint aPoint, NSRect aRect, boolean flipped);

    /**
     * Original signature : <code>BOOL NSContainsRect(NSRect, NSRect)</code><br>
     * <i>native declaration : /System/Library/Frameworks/ApplicationServices.framework/Headers/../Frameworks/framework/Headers/CGGeometry.h:460</i>
     */
    boolean NSContainsRect(NSRect aRect, NSRect bRect);

    /**
     * Original signature : <code>BOOL NSIntersectsRect(NSRect, NSRect)</code><br>
     * <i>native declaration : /System/Library/Frameworks/ApplicationServices.framework/Headers/../Frameworks/framework/Headers/CGGeometry.h:461</i>
     */
    boolean NSIntersectsRect(NSRect aRect, NSRect bRect);

    /**
     * Original signature : <code>NSString* NSStringFromPoint(NSPoint)</code><br>
     * <i>native declaration : /System/Library/Frameworks/ApplicationServices.framework/Headers/../Frameworks/framework/Headers/CGGeometry.h:463</i>
     */
    String NSStringFromPoint(NSPoint aPoint);

    /**
     * Original signature : <code>NSString* NSStringFromSize(NSSize)</code><br>
     * <i>native declaration : /System/Library/Frameworks/ApplicationServices.framework/Headers/../Frameworks/framework/Headers/CGGeometry.h:464</i>
     */
    String NSStringFromSize(NSSize aSize);

    /**
     * Original signature : <code>NSString* NSStringFromRect(NSRect)</code><br>
     * <i>native declaration : /System/Library/Frameworks/ApplicationServices.framework/Headers/../Frameworks/framework/Headers/CGGeometry.h:465</i>
     */
    String NSStringFromRect(NSRect aRect);

    /**
     * Original signature : <code>NSPoint NSPointFromString(NSString*)</code><br>
     * <i>native declaration : /System/Library/Frameworks/ApplicationServices.framework/Headers/../Frameworks/framework/Headers/CGGeometry.h:466</i>
     */
    NSPoint NSPointFromString(String aString);

    /**
     * Original signature : <code>NSSize NSSizeFromString(NSString*)</code><br>
     * <i>native declaration : /System/Library/Frameworks/ApplicationServices.framework/Headers/../Frameworks/framework/Headers/CGGeometry.h:467</i>
     */
    NSSize NSSizeFromString(String aString);

    /**
     * Original signature : <code>NSString* NSUserName()</code><br>
     * <i>native declaration : /System/Library/Frameworks/Foundation.framework/Headers/NSPathUtilities.h:46</i>
     */
    String NSUserName();

    /**
     * Original signature : <code>NSString* NSFullUserName()</code><br>
     * <i>native declaration : /System/Library/Frameworks/Foundation.framework/Headers/NSPathUtilities.h:47</i>
     */
    String NSFullUserName();

    /**
     * Original signature : <code>NSString* NSHomeDirectory()</code><br>
     * <i>native declaration : /System/Library/Frameworks/Foundation.framework/Headers/NSPathUtilities.h:49</i>
     */
    String NSHomeDirectory();

    /**
     * Original signature : <code>NSString* NSHomeDirectoryForUser(String*)</code><br>
     * <i>native declaration : /System/Library/Frameworks/Foundation.framework/Headers/NSPathUtilities.h:50</i>
     */
    String NSHomeDirectoryForUser(String userName);

    /**
     * Original signature : <code>NSString* NSTemporaryDirectory()</code><br>
     * <i>native declaration : /System/Library/Frameworks/Foundation.framework/Headers/NSPathUtilities.h:52</i>
     */
    String NSTemporaryDirectory();

    /**
     * Original signature : <code>NSArray* NSSearchPathForDirectoriesInDomains(NSSearchPathDirectory, NSSearchPathDomainMask, BOOL)</code><br>
     * <i>native declaration : /System/Library/Frameworks/Foundation.framework/Headers/NSPathUtilities.h:106</i><br>
     *
     * @param directory  @see NSSearchPathDirectory<br>
     * @param domainMask @see NSSearchPathDomainMask
     */
    NSArray NSSearchPathForDirectoriesInDomains(int directory, int domainMask, boolean expandTilde);

    /**
     * Logs an error message to the Apple System Log facility.
     *
     * @param format Statement
     */
    void NSLog(String format, String... args);

    CFStringRef CFStringCreateWithCharacters(CFAllocatorRef allocator, char[] chars, CFIndex index);
}

