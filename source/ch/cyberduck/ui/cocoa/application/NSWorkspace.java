package ch.cyberduck.ui.cocoa.application;

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

import ch.cyberduck.ui.cocoa.foundation.NSArray;
import ch.cyberduck.ui.cocoa.foundation.NSNotificationCenter;
import ch.cyberduck.ui.cocoa.foundation.NSObject;
import ch.cyberduck.ui.cocoa.foundation.NSURL;

import org.rococoa.ObjCClass;
import org.rococoa.cocoa.foundation.NSInteger;
import org.rococoa.cocoa.foundation.NSUInteger;

import com.sun.jna.NativeLong;

/// <i>native declaration : :43</i>
public abstract class NSWorkspace extends NSObject {
    private static final _Class CLASS = org.rococoa.Rococoa.createClass("NSWorkspace", _Class.class);

    public static final String WorkspaceDidLaunchApplicationNotification = "NSWorkspaceDidLaunchApplicationNotification";
    public static final String WorkspaceDidMountNotification = "NSWorkspaceDidMountNotification";
    public static final String WorkspaceDidPerformFileOperationNotification = "NSWorkspaceDidPerformFileOperationNotification";
    public static final String WorkspaceDidTerminateApplicationNotification = "NSWorkspaceDidTerminateApplicationNotification";
    public static final String WorkspaceDidUnmountNotification = "NSWorkspaceDidUnmountNotification";
    public static final String WorkspaceDidWakeNotification = "NSWorkspaceDidWakeNotification";
    public static final String WorkspaceWillLaunchApplicationNotification = "NSWorkspaceWillLaunchApplicationNotification";
    public static final String WorkspaceWillPowerOffNotification = "NSWorkspaceWillPowerOffNotification";
    public static final String WorkspaceWillSleepNotification = "NSWorkspaceWillSleepNotification";
    public static final String WorkspaceWillUnmountNotification = "NSWorkspaceWillUnmountNotification";
    public static final String WorkspaceSessionDidBecomeActiveNotification = "NSWorkspaceSessionDidBecomeActiveNotification";
    public static final String WorkspaceSessionDidResignActiveNotification = "NSWorkspaceSessionDidResignActiveNotification";

    /// <i>native declaration : :13</i>
    public static final int NSWorkspaceLaunchAndPrint = 2;
    /// <i>native declaration : :14</i>
    public static final int NSWorkspaceLaunchInhibitingBackgroundOnly = 128;
    /// <i>native declaration : :15</i>
    public static final int NSWorkspaceLaunchWithoutAddingToRecents = 256;
    /// <i>native declaration : :16</i>
    public static final int NSWorkspaceLaunchWithoutActivation = 512;
    /// <i>native declaration : :17</i>
    public static final int NSWorkspaceLaunchAsync = 65536;
    /// <i>native declaration : :18</i>
    public static final int NSWorkspaceLaunchAllowingClassicStartup = 131072;
    /// <i>native declaration : :19</i>
    public static final int NSWorkspaceLaunchPreferringClassic = 262144;
    /// <i>native declaration : :20</i>
    public static final int NSWorkspaceLaunchNewInstance = 524288;
    /// <i>native declaration : :21</i>
    public static final int NSWorkspaceLaunchAndHide = 1048576;
    /// <i>native declaration : :22</i>
    public static final int NSWorkspaceLaunchAndHideOthers = 2097152;
    /**
     * NSWorkspaceLaunchAndDisplayFailures<br>
     * <i>native declaration : :24</i>
     */
    public static final int NSWorkspaceLaunchDefault = NSWorkspaceLaunchAsync | NSWorkspaceLaunchAllowingClassicStartup;
    /// <i>native declaration : :32</i>
    public static final int NSExcludeQuickDrawElementsIconCreationOption = 1 << 1;
    /// <i>native declaration : :33</i>
    public static final int NSExclude10_4ElementsIconCreationOption = 1 << 2;

    public static final String ApplicationName = "NSApplicationName";
    public static final String DevicePath = "NSDevicePath";
    public static final String OperationNumber = "NSOperationNumber";
    public static final String PlainFileType = "";
    public static final String DirectoryFileType = "NXDirectoryFileType";
    public static final String ApplicationFileType = "app";
    public static final String FilesystemFileType = "NXFilesystemFileType";
    public static final String ShellCommandFileType = "NXShellCommandFileType";
    public static final String MoveOperation = "move";
    public static final String CopyOperation = "copy";
    public static final String LinkOperation = "link";
    public static final String CompressOperation = "compress";
    public static final String DecompressOperation = "decompress";
    public static final String EncryptOperation = "encrypt";
    public static final String DecryptOperation = "decrypt";
    public static final String DestroyOperation = "destroy";
    public static final String RecycleOperation = "recycle";
    public static final String DuplicateOperation = "duplicate";

    public interface _Class extends ObjCClass {
        /**
         * Original signature : <code>NSWorkspace* sharedWorkspace)</code><br>
         * <i>native declaration : :54</i>
         */
        NSWorkspace sharedWorkspace();
    }

    public static NSWorkspace sharedWorkspace() {
        return CLASS.sharedWorkspace();
    }


    /**
     * Original signature : <code>NSNotificationCenter* notificationCenter)</code><br>
     * <i>native declaration : :56</i>
     */
    public abstract NSNotificationCenter notificationCenter();

    /**
     * Original signature : <code>BOOL openFile(NSString*)</code><br>
     * <i>native declaration : :58</i>
     */
    public abstract boolean openFile(String fullPath1);

    /**
     * Original signature : <code>BOOL openFile(NSString*, NSString*)</code><br>
     * <i>native declaration : :59</i>
     */
    public abstract boolean openFile_withApplication(String fullPath1, String appName2);

    public boolean openFile(String fullPath1, String appName2) {
        return this.openFile_withApplication(fullPath1, appName2);
    }

    /**
     * Original signature : <code>BOOL openFile(NSString*, NSString*, BOOL)</code><br>
     * <i>native declaration : :60</i>
     */
    public abstract boolean openFile_withApplication_andDeactivate(String appName2, boolean flag3);

    /**
     * Original signature : <code>BOOL openTempFile(NSString*)</code><br>
     * <i>native declaration : :62</i>
     */
    public abstract boolean openTempFile(String fullPath1);
    /**
     * <i>native declaration : :64</i><br>
     * Conversion Error : NSPoint
     */
    /**
     * Original signature : <code>BOOL openURL(NSURL*)</code><br>
     * <i>native declaration : :66</i>
     */
    public abstract boolean openURL(NSURL url1);

    /**
     * Original signature : <code>BOOL launchApplication(NSString*)</code><br>
     * <i>native declaration : :68</i>
     */
    public abstract boolean launchApplication(String appName1);

    /**
     * Original signature : <code>BOOL launchApplication(NSString*, BOOL, BOOL)</code><br>
     * <i>native declaration : :69</i>
     */
    public abstract boolean launchApplication_showIcon_autolaunch(String appName1, boolean showIcon2, boolean autolaunch3);

    /**
     * Original signature : <code>NSString* fullPathForApplication(NSString*)</code><br>
     * <i>native declaration : :70</i>
     */
    public abstract String fullPathForApplication(String appName1);

    /**
     * Original signature : <code>BOOL selectFile(NSString*, NSString*)</code><br>
     * <i>native declaration : :72</i>
     */
    public abstract boolean selectFile_inFileViewerRootedAtPath(String fullpath, String rootpath);

    public boolean selectFile(String fullpath, String rootpath) {
        return selectFile_inFileViewerRootedAtPath(fullpath, rootpath);
    }

    /**
     * Original signature : <code>void findApplications)</code><br>
     * <i>native declaration : :74</i>
     */
    public abstract void findApplications();

    /**
     * Original signature : <code>public abstract void noteFileSystemChanged)</code><br>
     * <i>native declaration : :76</i>
     */
    public abstract void noteFileSystemChanged();

    /**
     * Original signature : <code>public abstract void noteFileSystemChanged(NSString*)</code><br>
     * <i>native declaration : :77</i>
     */
    public abstract void noteFileSystemChanged(String path1);

    /**
     * Original signature : <code>BOOL fileSystemChanged)</code><br>
     * <i>native declaration : :78</i>
     */
    public abstract boolean fileSystemChanged();

    /**
     * Original signature : <code>public abstract void noteUserDefaultsChanged)</code><br>
     * <i>native declaration : :79</i>
     */
    public abstract void noteUserDefaultsChanged();

    /**
     * Original signature : <code>BOOL userDefaultsChanged)</code><br>
     * <i>native declaration : :80</i>
     */
    public abstract boolean userDefaultsChanged();

    /**
     * Original signature : <code>BOOL getInfoForFile(NSString*, NSString**, NSString**)</code><br>
     * <i>native declaration : :82</i>
     */
    public abstract boolean getInfoForFile_application_type(String fullPath1, com.sun.jna.ptr.PointerByReference appName2, com.sun.jna.ptr.PointerByReference type3);

    /**
     * Original signature : <code>BOOL isFilePackageAtPath(NSString*)</code><br>
     * <i>native declaration : :83</i>
     */
    public abstract boolean isFilePackageAtPath(String fullPath1);

    /**
     * Original signature : <code>NSImage* iconForFile(NSString*)</code><br>
     * <i>native declaration : :85</i>
     */
    public abstract NSImage iconForFile(String fullPath1);

    /**
     * Original signature : <code>NSImage* iconForFiles(NSArray*)</code><br>
     * <i>native declaration : :86</i>
     */
    public abstract NSImage iconForFiles(NSArray fullPaths1);

    /**
     * Original signature : <code>NSImage* iconForFileType(NSString*)</code><br>
     * <i>native declaration : :87</i>
     */
    public abstract NSImage iconForFileType(String fileType1);

    /**
     * Original signature : <code>BOOL setIcon(NSImage*, NSString*, NSWorkspaceIconCreationOptions)</code><br>
     * <i>native declaration : :89</i>
     */
    public abstract boolean setIcon_forFile_options(NSImage image1, String fullPath2, NSUInteger options3);

    /**
     * Original signature : <code>BOOL getFileSystemInfoForPath(NSString*, BOOL*, BOOL*, BOOL*, NSString**, NSString**)</code><br>
     * <i>native declaration : :92</i>
     */
    public abstract boolean getFileSystemInfoForPath_isRemovable_isWritable_isUnmountable_description_type(String fullPath1, boolean removableFlag2, boolean writableFlag3, boolean unmountableFlag4, com.sun.jna.ptr.PointerByReference description5, com.sun.jna.ptr.PointerByReference fileSystemType6);

    /**
     * Original signature : <code>BOOL performFileOperation(NSString*, NSString*, NSString*, NSArray*, NSInteger*)</code><br>
     * Returned tag < 0 on failure, 0 if sync, > 0 if async<br>
     * <i>native declaration : :94</i>
     */
    public abstract boolean performFileOperation_source_destination_files_tag(String operation1, String source2, String destination3, NSArray files4, NSInteger tag5);

    public boolean performFileOperation(String operation, String source, String destination, NSArray files) {
        return this.performFileOperation_source_destination_files_tag(operation, source, destination, files, new NSInteger(0));
    }

    /**
     * Original signature : <code>BOOL unmountAndEjectDeviceAtPath(NSString*)</code><br>
     * <i>native declaration : :96</i>
     */
    public abstract boolean unmountAndEjectDeviceAtPath(String path1);

    /**
     * Original signature : <code>NSInteger extendPowerOffBy(NSInteger)</code><br>
     * <i>native declaration : :97</i>
     */
    public abstract NSInteger extendPowerOffBy(NSInteger requested1);
    /**
     * <i>native declaration : :99</i><br>
     * Conversion Error : NSPoint
     */
    /**
     * Original signature : <code>public abstract void hideOtherApplications)</code><br>
     * <i>native declaration : :101</i>
     */
    public abstract void hideOtherApplications();

    /**
     * Original signature : <code>NSArray* mountedLocalVolumePaths)</code><br>
     * <i>native declaration : :103</i>
     */
    public abstract NSArray mountedLocalVolumePaths();

    /**
     * Original signature : <code>NSArray* mountedRemovableMedia)</code><br>
     * <i>native declaration : :104</i>
     */
    public abstract NSArray mountedRemovableMedia();

    /**
     * Original signature : <code>NSArray* mountNewRemovableMedia)</code><br>
     * <i>native declaration : :105</i>
     */
    public abstract NSArray mountNewRemovableMedia();

    /**
     * Original signature : <code>public abstract void checkForRemovableMedia)</code><br>
     * <i>native declaration : :106</i>
     */
    public abstract void checkForRemovableMedia();

    /**
     * Original signature : <code>NSString* absolutePathForAppBundleWithIdentifier(NSString*)</code><br>
     * <i>native declaration : :110</i>
     */
    public abstract String absolutePathForAppBundleWithIdentifier(String bundleIdentifier1);

    /**
     * Original signature : <code>BOOL launchAppWithBundleIdentifier(NSString*, NSWorkspaceLaunchOptions, NSAppleEventDescriptor*, NSNumber**)</code><br>
     * <i>native declaration : :111</i>
     */
    public abstract boolean launchAppWithBundleIdentifier_options_additionalEventParamDescriptor_launchIdentifier(String bundleIdentifier1, int options2, com.sun.jna.Pointer descriptor3, com.sun.jna.ptr.PointerByReference identifier4);

    /**
     * Original signature : <code>BOOL openURLs(NSArray*, NSString*, NSWorkspaceLaunchOptions, NSAppleEventDescriptor*, NSArray**)</code><br>
     * <i>native declaration : :112</i>
     */
    public abstract boolean openURLs_withAppBundleIdentifier_options_additionalEventParamDescriptor_launchIdentifiers(NSArray urls1, String bundleIdentifier2, int options3, com.sun.jna.Pointer descriptor4, com.sun.jna.ptr.PointerByReference identifiers5);

    /**
     * Original signature : <code>NSArray* launchedApplications)</code><br>
     * Returns an array of dictionaries, one for each running application.<br>
     * <i>native declaration : :127</i>
     */
    public abstract NSArray launchedApplications();

    /**
     * Original signature : <code>NSDictionary* activeApplication)</code><br>
     * Returns a dictionary with information about the current active application.<br>
     * <i>native declaration : :128</i>
     */
    public abstract NSArray activeApplication();

    /**
     * Given an absolute file path, return the uniform type identifier (UTI) of the file, if one can be determined. Otherwise, return nil after setting *outError to an NSError that encapsulates the reason why the file's type could not be determined. If the file at the end of the path is a symbolic link the type of the symbolic link will be returned.<br>
     * You can invoke this method to get the UTI of an existing file.<br>
     * Original signature : <code>NSString* typeOfFile(NSString*, NSError**)</code><br>
     * <i>native declaration : :138</i>
     */
    public abstract String typeOfFile_error(String absoluteFilePath1, com.sun.jna.ptr.PointerByReference outError2);

    /**
     * Given a UTI, return a string that describes the document type and is fit to present to the user, or nil for failure.<br>
     * You can invoke this method to get the name of a type that must be shown to the user, in an alert about your application's inability to handle the type, for instance.<br>
     * Original signature : <code>NSString* localizedDescriptionForType(NSString*)</code><br>
     * <i>native declaration : :144</i>
     */
    public abstract String localizedDescriptionForType(String typeName1);

    /**
     * Given a UTI, return the best file name extension to use when creating a file of that type, or nil for failure.<br>
     * You can invoke this method when your application has only the base name of a file that's being written and it has to append a file name extension so that the file's type can be reliably identified later on.<br>
     * Original signature : <code>NSString* preferredFilenameExtensionForType(NSString*)</code><br>
     * <i>native declaration : :150</i>
     */
    public abstract String preferredFilenameExtensionForType(String typeName1);

    /**
     * Given a file name extension and a UTI, return YES if the file name extension is a valid tag for the identified type, NO otherwise.<br>
     * You can invoke this method when your application needs to check if a file name extension can be used to reliably identify the type later on. For example, NSSavePanel uses this method to validate any extension that the user types in the panel's file name field.<br>
     * Original signature : <code>BOOL filenameExtension(NSString*, NSString*)</code><br>
     * <i>native declaration : :156</i>
     */
    public abstract boolean filenameExtension_isValidForType(String filenameExtension1, String typeName2);

    /**
     * Given two UTIs, return YES if the first "conforms to" to the second in the uniform type identifier hierarchy, NO otherwise. This method will always return YES if the two strings are equal, so you can also use it with other kinds of type name, including those declared in CFBundleTypeName Info.plist entries in apps that don't take advantage of the support for UTIs that was added to Cocoa in Mac OS 10.5.<br>
     * You can invoke this method when your application must determine whether it can handle a file of a known type, returned by -typeOfFile:error: for instance.<br>
     * Use this method instead of merely comparing UTIs for equality.<br>
     * Original signature : <code>BOOL type(NSString*, NSString*)</code><br>
     * <i>native declaration : :164</i>
     */
    public abstract boolean type_conformsToType(String firstTypeName1, String secondTypeName2);
}
