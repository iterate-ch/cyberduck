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
import org.rococoa.internal.RococoaTypeMapper;

import java.util.Collections;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.platform.mac.CoreFoundation;

public interface FoundationKitFunctions extends Library {
    FoundationKitFunctions library = Native.load(
        "Foundation", FoundationKitFunctions.class, Collections.singletonMap(Library.OPTION_TYPE_MAPPER, new RococoaTypeMapper()));

    /**
     * <i>native declaration : /System/Library/Frameworks/ApplicationServices.framework/Headers/../Frameworks/CoreGraphics.framework/Headers/CGGeometry.h:36</i><br>
     * enum values
     */
    interface CGRectEdge {
        int CGRectMinXEdge = 0;
        int CGRectMinYEdge = 1;
        int CGRectMaxXEdge = 2;
        int CGRectMaxYEdge = 3;
    }

    /**
     * <i>native declaration : /System/Library/Frameworks/ApplicationServices.framework/Headers/../Frameworks/CoreGraphics.framework/Headers/CGGeometry.h</i><br>
     * enum values
     */
    interface NSRectEdge {
        int NSMinXEdge = 0;
        int NSMinYEdge = 1;
        int NSMaxXEdge = 2;
        int NSMaxYEdge = 3;
    }

    /**
     * <i>native declaration : /System/Library/Frameworks/Foundation.framework/Headers/NSPathUtilities.h</i><br>
     * enum values
     */
    interface NSSearchPathDirectory {
        /// supported applications (Applications)
        int NSApplicationDirectory = 1;
        /// unsupported applications, demonstration versions (Demos)
        int NSDemoApplicationDirectory = 2;
        /// developer applications (Developer/Applications). DEPRECATED - there is no one single Developer directory.
        int NSDeveloperApplicationDirectory = 3;
        /// system and network administration applications (Administration)
        int NSAdminApplicationDirectory = 4;
        /// various user-visible documentation, support, and configuration files, resources (Library)
        int NSLibraryDirectory = 5;
        /// developer resources (Developer) DEPRECATED - there is no one single Developer directory.
        int NSDeveloperDirectory = 6;
        /// user home directories (Users)
        int NSUserDirectory = 7;
        /// documentation (Documentation)
        int NSDocumentationDirectory = 8;
        /// documents (Documents)
        int NSDocumentDirectory = 9;
        /// location of CoreServices directory (System/Library/CoreServices)
        int NSCoreServiceDirectory = 10;
        /// location of autosaved documents (Documents/Autosaved)
        int NSAutosavedInformationDirectory = 11;
        /// location of user's desktop
        int NSDesktopDirectory = 12;
        /// location of discardable cache files (Library/Caches)
        int NSCachesDirectory = 13;
        /// location of application support files (plug-ins, etc) (Library/Application Support)
        int NSApplicationSupportDirectory = 14;
        /// location of the user's "Downloads" directory
        int NSDownloadsDirectory = 15;
        /// input methods (Library/Input Methods)
        int NSInputMethodsDirectory = 16;
        /// location of user's Movies directory (~/Movies)
        int NSMoviesDirectory = 17;
        /// location of user's Music directory (~/Music)
        int NSMusicDirectory = 18;
        /// location of user's Pictures directory (~/Pictures)
        int NSPicturesDirectory = 19;
        /// location of system's PPDs directory (Library/Printers/PPDs)
        int NSPrinterDescriptionDirectory = 20;
        /// location of user's Public sharing directory (~/Public)
        int NSSharedPublicDirectory = 21;
        /// location of the PreferencePanes directory for use with System Preferences (Library/PreferencePanes)
        int NSPreferencePanesDirectory = 22;
        /// For use with NSFileManager's URLForDirectory:inDomain:appropriateForURL:create:error:
        int NSItemReplacementDirectory = 99;
        /// all directories where applications can occur
        int NSAllApplicationsDirectory = 100;
        /// all directories where resources can occur
        int NSAllLibrariesDirectory = 101;
    }

    /**
     * <i>native declaration : /System/Library/Frameworks/Foundation.framework/Headers/NSPathUtilities.h</i><br>
     * enum values
     */
    interface NSSearchPathDomainMask {
        /// user's home directory --- place to install user's personal items (~)
        int NSUserDomainMask = 1;
        /// local to the current machine --- place to install items available to everyone on this machine (/Library)
        int NSLocalDomainMask = 2;
        /// publicly available location in the local area network --- place to install items available on the network (/Network)
        int NSNetworkDomainMask = 4;
        /// provided by Apple, unmodifiable (/System)
        int NSSystemDomainMask = 8;
        /// all domains: all of the above and future items
        int NSAllDomainsMask = 65535;
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
     * Original signature : <code>NSArray* NSSearchPathForDirectoriesInDomains(NSSearchPathDirectory,
     * NSSearchPathDomainMask, BOOL)</code><br>
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

    /**
     * Releases a Core Foundation object. If the retain count of cf becomes zero the memory allocated to the object is
     * deallocated and the object is destroyed. If you create, copy, or explicitly retain (see the CFRetain function) a
     * Core Foundation object, you are responsible for releasing it when you no longer need it (see Memory Management
     * Programming Guide for Core Foundation).
     *
     * @param ref A CFType object to release. This value must not be NULL.
     */
    void CFRelease(CoreFoundation.CFTypeRef ref);
}

