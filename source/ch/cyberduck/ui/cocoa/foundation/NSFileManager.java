package ch.cyberduck.ui.cocoa.foundation;

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

import org.rococoa.cocoa.foundation.NSUInteger;

/// <i>native declaration : :12</i>
public abstract class NSFileManager implements NSObject {
    private static final _Class CLASS = org.rococoa.Rococoa.createClass("NSFileManager", _Class.class);

    public static NSFileManager defaultManager() {
        return CLASS.defaultManager();
    }

    public static final String NSFileType = "NSFileType";
    public static final String NSFileTypeDirectory = "NSFileTypeDirectory";
    public static final String NSFileTypeRegular = "NSFileTypeRegular";
    public static final String NSFileTypeSymbolicLink = "NSFileTypeSymbolicLink";
    public static final String NSFileTypeSocket = "NSFileTypeSocket";
    public static final String NSFileTypeCharacterSpecial = "NSFileTypeCharacterSpecial";
    public static final String NSFileTypeBlockSpecial = "NSFileTypeBlockSpecial";
    public static final String NSFileTypeUnknown = "NSFileTypeUnknown";
    public static final String NSFileSize = "NSFileSize";
    public static final String NSFileModificationDate = "NSFileModificationDate";
    public static final String NSFileReferenceCount = "NSFileReferenceCount";
    public static final String NSFileDeviceIdentifier = "NSFileDeviceIdentifier";
    public static final String NSFileOwnerAccountName = "NSFileOwnerAccountName";
    public static final String NSFileGroupOwnerAccountName = "NSFileGroupOwnerAccountName";
    public static final String NSFilePosixPermissions = "NSFilePosixPermissions";
    public static final String NSFileSystemNumber = "NSFileSystemNumber";
    public static final String NSFileSystemFileNumber = "NSFileSystemFileNumber";
    public static final String NSFileExtensionHidden = "NSFileExtensionHidden";
    public static final String NSFileHFSCreatorCode = "NSFileHFSCreatorCode";
    public static final String NSFileHFSTypeCode = "NSFileHFSTypeCode";
    public static final String NSFileImmutable = "NSFileImmutable";
    public static final String NSFileAppendOnly = "NSFileAppendOnly";
    public static final String NSFileCreationDate = "NSFileCreationDate";
    public static final String NSFileOwnerAccountID = "NSFileOwnerAccountID";
    public static final String NSFileGroupOwnerAccountID = "NSFileGroupOwnerAccountID";
    public static final String NSFileSystemSize = "NSFileSystemSize";
    public static final String NSFileSystemFreeSize = "NSFileSystemFreeSize";
    public static final String NSFileSystemNodes = "NSFileSystemNodes";
    public static final String NSFileSystemFreeNodes = "NSFileSystemFreeNodes";

    public interface _Class extends org.rococoa.NSClass {
        /**
         * Returns the default singleton CLASS.<br>
         * Original signature : <code>NSFileManager* defaultManager()</code><br>
         * <i>native declaration : :16</i>
         */
        NSFileManager defaultManager();
    }

    /**
     * <i>native declaration : :22</i><br>
     * Conversion Error : /**<br>
     * * CLASSs of NSFileManager may now have delegates. Each CLASS has one delegate, and the delegate is not retained. In versions of Mac OS X prior to 10.5, the behavior of calling [[NSFileManager alloc] init] was undefined. In Mac OS X 10.5 "Leopard" and later, calling [[NSFileManager alloc] init] returns a new CLASS of an NSFileManager.<br>
     * * Original signature : <code>void setDelegate(null)</code><br>
     * * /<br>
     * - (void)setDelegate:(null)delegate; (Argument delegate cannot be converted)
     */
    public abstract void setDelegate(org.rococoa.ID delegate);

    /**
     * Original signature : <code>delegate()</code><br>
     * <i>native declaration : :23</i>
     */
    public abstract org.rococoa.ID delegate();

    /**
     * setAttributes:ofItemAtPath:error: returns YES when the attributes specified in the 'attributes' dictionary are set successfully on the item specified by 'path'. If this method returns NO, a presentable NSError will be provided by-reference in the 'error' parameter. If no error is required, you may pass 'nil' for the error.<br>
     * This method replaces changeFileAttributes:atPath:.<br>
     * Original signature : <code>BOOL setAttributes(NSDictionary*, NSString*, NSError**)</code><br>
     * <i>native declaration : :29</i>
     */
    public abstract boolean setAttributes_ofItemAtPath_error(NSDictionary attributes, String path, com.sun.jna.ptr.PointerByReference error);

    /**
     * createDirectoryAtPath:withIntermediateDirectories:attributes:error: creates a directory at the specified path. If you pass 'NO' for createIntermediates, the directory must not exist at the time this call is made. Passing 'YES' for 'createIntermediates' will create any necessary intermediate directories. This method returns YES if all directories specified in 'path' were created and attributes were set. Directories are created with attributes specified by the dictionary passed to 'attributes'. If no dictionary is supplied, directories are created according to the umask of the process. This method returns NO if a failure occurs at any stage of the operation. If an error parameter was provided, a presentable NSError will be returned by reference.<br>
     * This method replaces createDirectoryAtPath:attributes:<br>
     * Original signature : <code>BOOL createDirectoryAtPath(NSString*, BOOL, NSDictionary*, NSError**)</code><br>
     * <i>native declaration : :35</i>
     */
    public abstract boolean createDirectoryAtPath_withIntermediateDirectories_attributes_error(String path, boolean createIntermediates, NSDictionary attributes, com.sun.jna.ptr.PointerByReference error);

    /**
     * contentsOfDirectoryAtPath:error: returns an NSArray of NSStrings representing the filenames of the items in the directory. If this method returns 'nil', an NSError will be returned by reference in the 'error' parameter. If the directory contains no items, this method will return the empty array.<br>
     * This method replaces directoryContentsAtPath:<br>
     * Original signature : <code>NSArray* contentsOfDirectoryAtPath(NSString*, NSError**)</code><br>
     * <i>native declaration : :41</i>
     */
    public abstract NSArray contentsOfDirectoryAtPath_error(String path, com.sun.jna.ptr.PointerByReference error);

    /**
     * subpathsOfDirectoryAtPath:error: returns an NSArray of NSStrings represeting the filenames of the items in the specified directory and all its subdirectories recursively. If this method returns 'nil', an NSError will be returned by reference in the 'error' parameter. If the directory contains no items, this method will return the empty array.<br>
     * This method replaces subpathsAtPath:<br>
     * Original signature : <code>NSArray* subpathsOfDirectoryAtPath(NSString*, NSError**)</code><br>
     * <i>native declaration : :47</i>
     */
    public abstract NSArray subpathsOfDirectoryAtPath_error(String path, com.sun.jna.ptr.PointerByReference error);

    /**
     * attributesOfItemAtPath:error: returns an NSDictionary of key/value pairs containing the attributes of the item (file, directory, symlink, etc.) at the path in question. If this method returns 'nil', an NSError will be returned by reference in the 'error' parameter. This method does not traverse an initial symlink.<br>
     * This method replaces fileAttributesAtPath:traverseLink:.<br>
     * Original signature : <code>NSDictionary* attributesOfItemAtPath(NSString*, NSError**)</code><br>
     * <i>native declaration : :53</i>
     */
    public abstract NSDictionary attributesOfItemAtPath_error(String path, com.sun.jna.ptr.PointerByReference error);

    /**
     * attributesOfFilesystemForPath:error: returns an NSDictionary of key/value pairs containing the attributes of the filesystem containing the provided path. If this method returns 'nil', an NSError will be returned by reference in the 'error' parameter. This method does not traverse an initial symlink.<br>
     * This method replaces fileSystemAttributesAtPath:.<br>
     * Original signature : <code>NSDictionary* attributesOfFileSystemForPath(NSString*, NSError**)</code><br>
     * <i>native declaration : :59</i>
     */
    public abstract NSDictionary attributesOfFileSystemForPath_error(String path, com.sun.jna.ptr.PointerByReference error);

    /**
     * createSymbolicLinkAtPath:withDestination:error: returns YES if the symbolic link that point at 'destPath' was able to be created at the location specified by 'path'. If this method returns NO, the link was unable to be created and an NSError will be returned by reference in the 'error' parameter. This method does not traverse an initial symlink.<br>
     * This method replaces createSymbolicLinkAtPath:pathContent:<br>
     * Original signature : <code>BOOL createSymbolicLinkAtPath(NSString*, NSString*, NSError**)</code><br>
     * <i>native declaration : :65</i>
     */
    public abstract boolean createSymbolicLinkAtPath_withDestinationPath_error(String path, String destPath, com.sun.jna.ptr.PointerByReference error);

    /**
     * destinationOfSymbolicLinkAtPath:error: returns an NSString containing the path of the item pointed at by the symlink specified by 'path'. If this method returns 'nil', an NSError will be returned by reference in the 'error' parameter. This method does not traverse an initial symlink.<br>
     * This method replaces pathContentOfSymbolicLinkAtPath:<br>
     * Original signature : <code>NSString* destinationOfSymbolicLinkAtPath(NSString*, NSError**)</code><br>
     * <i>native declaration : :71</i>
     */
    public abstract com.sun.jna.Pointer destinationOfSymbolicLinkAtPath_error(String path, com.sun.jna.ptr.PointerByReference error);

    /**
     * These methods replace their non-error returning counterparts below. See the NSFileManagerFileOperationAdditions category below for methods that are dispatched to the NSFileManager CLASS's delegate.<br>
     * Original signature : <code>BOOL copyItemAtPath(NSString*, NSString*, NSError**)</code><br>
     * <i>native declaration : :75</i>
     */
    public abstract boolean copyItemAtPath_toPath_error(String srcPath, String dstPath, com.sun.jna.ptr.PointerByReference error);

    /**
     * Original signature : <code>BOOL moveItemAtPath(NSString*, NSString*, NSError**)</code><br>
     * <i>native declaration : :76</i>
     */
    public abstract boolean moveItemAtPath_toPath_error(String srcPath, String dstPath, com.sun.jna.ptr.PointerByReference error);

    /**
     * Original signature : <code>BOOL linkItemAtPath(NSString*, NSString*, NSError**)</code><br>
     * <i>native declaration : :77</i>
     */
    public abstract boolean linkItemAtPath_toPath_error(String srcPath, String dstPath, com.sun.jna.ptr.PointerByReference error);

    /**
     * Original signature : <code>BOOL removeItemAtPath(NSString*, NSError**)</code><br>
     * <i>native declaration : :78</i>
     */
    public abstract boolean removeItemAtPath_error(String path, com.sun.jna.ptr.PointerByReference error);

    /**
     * The following methods will be deprecated in the next major release of Mac OS X after Leopard. Their error-returning replacements are listed above.<br>
     * Original signature : <code>NSDictionary* fileAttributesAtPath(NSString*, BOOL)</code><br>
     * <i>native declaration : :84</i>
     */
    public abstract NSDictionary fileAttributesAtPath_traverseLink(String path, boolean yorn);

    public NSDictionary fileAttributes(String path) {
        return fileAttributesAtPath_traverseLink(path, true);
    }

    /**
     * Original signature : <code>BOOL changeFileAttributes(NSDictionary*, NSString*)</code><br>
     * <i>native declaration : :85</i>
     */
    public abstract boolean changeFileAttributes_atPath(NSDictionary attributes, String path);

    public boolean changeFileAttributes(NSDictionary attributes, String path) {
        return this.changeFileAttributes_atPath(attributes, path);
    }

    /**
     * Original signature : <code>NSArray* directoryContentsAtPath(NSString*)</code><br>
     * <i>native declaration : :86</i>
     */
    public abstract NSArray directoryContentsAtPath(String path);

    /**
     * Original signature : <code>NSDictionary* fileSystemAttributesAtPath(NSString*)</code><br>
     * <i>native declaration : :87</i>
     */
    public abstract NSDictionary fileSystemAttributesAtPath(String path);

    /**
     * Original signature : <code>NSString* pathContentOfSymbolicLinkAtPath(NSString*)</code><br>
     * <i>native declaration : :88</i>
     */
    public abstract String pathContentOfSymbolicLinkAtPath(String path);

    /**
     * Original signature : <code>BOOL createSymbolicLinkAtPath(NSString*, NSString*)</code><br>
     * <i>native declaration : :89</i>
     */
    public abstract boolean createSymbolicLinkAtPath_pathContent(String path, String otherpath);

    /**
     * Original signature : <code>BOOL createDirectoryAtPath(NSString*, NSDictionary*)</code><br>
     * <i>native declaration : :90</i>
     */
    public abstract boolean createDirectoryAtPath_attributes(String path, NSDictionary attributes);
    /**
     * <i>native declaration : :91</i><br>
     * Conversion Error : /// Original signature : <code>BOOL linkPath(NSString*, NSString*, null)</code><br>
     * - (BOOL)linkPath:(NSString*)src toPath:(NSString*)dest handler:(null)handler; (Argument handler cannot be converted)
     */
    /**
     * <i>native declaration : :92</i><br>
     * Conversion Error : /// Original signature : <code>BOOL copyPath(NSString*, NSString*, null)</code><br>
     * - (BOOL)copyPath:(NSString*)src toPath:(NSString*)dest handler:(null)handler; (Argument handler cannot be converted)
     */
    /**
     * <i>native declaration : :93</i><br>
     * Conversion Error : /// Original signature : <code>BOOL movePath(NSString*, NSString*, null)</code><br>
     * - (BOOL)movePath:(NSString*)src toPath:(NSString*)dest handler:(null)handler; (Argument handler cannot be converted)
     */
    /**
     * <i>native declaration : :94</i><br>
     * Conversion Error : /// Original signature : <code>BOOL removeFileAtPath(NSString*, null)</code><br>
     * - (BOOL)removeFileAtPath:(NSString*)path handler:(null)handler; (Argument handler cannot be converted)
     */
    /**
     * Process working directory management. Despite the fact that these are CLASS methods on NSFileManager, these methods report and change (respectively) the working directory for the entire process. Developers are cautioned that doing so is fraught with peril.<br>
     * Original signature : <code>NSString* currentDirectoryPath()</code><br>
     * <i>native declaration : :98</i>
     */
    public abstract com.sun.jna.Pointer currentDirectoryPath();

    /**
     * Original signature : <code>BOOL changeCurrentDirectoryPath(NSString*)</code><br>
     * <i>native declaration : :99</i>
     */
    public abstract boolean changeCurrentDirectoryPath(String path);

    /**
     * The following methods are of limited utility. Attempting to predicate behavior based on the current state of the filesystem or a particular file on the filesystem is encouraging odd behavior in the face of filesystem race conditions. It's far better to attempt an operation (like loading a file or creating a directory) and handle the error gracefully than it is to try to figure out ahead of time whether the operation will succeed.<br>
     * Original signature : <code>BOOL fileExistsAtPath(NSString*)</code><br>
     * <i>native declaration : :103</i>
     */
    public abstract boolean fileExistsAtPath(String path);

    /**
     * Original signature : <code>BOOL fileExistsAtPath(NSString*, BOOL*)</code><br>
     * <i>native declaration : :104</i>
     */
    public abstract boolean fileExistsAtPath_isDirectory(String path, boolean isDirectory);

    /**
     * Original signature : <code>BOOL isReadableFileAtPath(NSString*)</code><br>
     * <i>native declaration : :105</i>
     */
    public abstract boolean isReadableFileAtPath(String path);

    /**
     * Original signature : <code>BOOL isWritableFileAtPath(NSString*)</code><br>
     * <i>native declaration : :106</i>
     */
    public abstract boolean isWritableFileAtPath(String path);

    /**
     * Original signature : <code>BOOL isExecutableFileAtPath(NSString*)</code><br>
     * <i>native declaration : :107</i>
     */
    public abstract boolean isExecutableFileAtPath(String path);

    /**
     * Original signature : <code>BOOL isDeletableFileAtPath(NSString*)</code><br>
     * <i>native declaration : :108</i>
     */
    public abstract boolean isDeletableFileAtPath(String path);

    /**
     * -contentsEqualAtPath:andPath: does not take into account data stored in the resource fork or filesystem extended attributes.<br>
     * Original signature : <code>BOOL contentsEqualAtPath(NSString*, NSString*)</code><br>
     * <i>native declaration : :112</i>
     */
    public abstract boolean contentsEqualAtPath_andPath(String path1, String path2);

    /**
     * displayNameAtPath: returns an NSString suitable for presentation to the user. For directories which have localization information, this will return the appropriate localized string. This string is not suitable for passing to anything that must interact with the filesystem.<br>
     * Original signature : <code>NSString* displayNameAtPath(NSString*)</code><br>
     * <i>native declaration : :116</i>
     */
    public abstract String displayNameAtPath(String path);

    /**
     * componentsToDisplayForPath: returns an NSArray of display names for the path provided. Localization will occur as in displayNameAtPath: above. This array cannot and should not be reassembled into an usable filesystem path for any kind of access.<br>
     * Original signature : <code>NSArray* componentsToDisplayForPath(NSString*)</code><br>
     * <i>native declaration : :121</i>
     */
    public abstract NSArray componentsToDisplayForPath(String path);

    /**
     * enumeratorAtPath: returns an NSDirectoryEnumerator rooted at the provided path. If the enumerator cannot be created, this returns NULL. Because NSDirectoryEnumerator is a subclass of NSEnumerator, the returned object can be used in the for...in construct.<br>
     * Original signature : <code>NSDirectoryEnumerator* enumeratorAtPath(NSString*)</code><br>
     * <i>native declaration : :126</i>
     */
    public abstract NSDirectoryEnumerator enumeratorAtPath(String path);

    /**
     * subpathsAtPath: returns an NSArray of all contents and subpaths recursively from the provided path. This may be very expensive to compute for deep filesystem hierarchies, and should probably be avoided.<br>
     * Original signature : <code>NSArray* subpathsAtPath(NSString*)</code><br>
     * <i>native declaration : :130</i>
     */
    public abstract com.sun.jna.Pointer subpathsAtPath(String path);

    /**
     * These methods are provided here for compatibility. The corresponding methods on NSData which return NSErrors should be regarded as the primary method of creating a file from an NSData or retrieving the contents of a file as an NSData.<br>
     * Original signature : <code>NSData* contentsAtPath(NSString*)</code><br>
     * <i>native declaration : :134</i>
     */
    public abstract com.sun.jna.Pointer contentsAtPath(String path);

    /**
     * Original signature : <code>BOOL createFileAtPath(NSString*, NSData*, NSDictionary*)</code><br>
     * <i>native declaration : :135</i>
     */
    public abstract boolean createFileAtPath_contents_attributes(String path, NSData data, NSDictionary attr);

    /**
     * fileSystemRepresentationWithPath: returns an array of characters suitable for passing to lower-level POSIX style APIs. The string is provided in the representation most appropriate for the filesystem in question.<br>
     * Original signature : <code>const char* fileSystemRepresentationWithPath(NSString*)</code><br>
     * <i>native declaration : :139</i>
     */
    public abstract com.sun.jna.ptr.ByteByReference fileSystemRepresentationWithPath(String path);

    /**
     * stringWithFileSystemRepresentation:length: returns an NSString created from an array of bytes that are in the filesystem representation.<br>
     * Original signature : <code>NSString* stringWithFileSystemRepresentation(const char*, NSUInteger)</code><br>
     * <i>native declaration : :143</i>
     */
    public abstract String stringWithFileSystemRepresentation_length(java.lang.String str, NSUInteger len);

    public abstract class NSDirectoryEnumerator implements NSObject {
        /**
         * Original signature : <code>NSDictionary* fileAttributes()</code><br>
         * <i>native declaration : :211</i>
         */
        public abstract NSDictionary fileAttributes();

        /**
         * Original signature : <code>NSDictionary* directoryAttributes()</code><br>
         * <i>native declaration : :212</i>
         */
        public abstract NSDictionary directoryAttributes();

        /**
         * Original signature : <code>void skipDescendents()</code><br>
         * <i>native declaration : :213</i>
         */
        public abstract void skipDescendents();
    }
}
