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

import org.rococoa.ObjCClass;
import org.rococoa.cocoa.foundation.NSUInteger;

import com.sun.jna.ptr.PointerByReference;

/// <i>native declaration : :12</i>
public abstract class NSFileManager extends NSObject {
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

    public interface _Class extends ObjCClass {
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
     * -mountedVolumeURLsIncludingResourceValuesForKeys:options: returns an NSArray of NSURLs locating the mounted volumes available on the computer. The property keys that can be requested are available in <Foundation/NSURL.h>.<br>
     * Original signature : <code>-(NSArray*)mountedVolumeURLsIncludingResourceValuesForKeys:(NSArray*) options:(NSVolumeEnumerationOptions)</code><br>
     * <i>native declaration : NSFileManager.h:69</i>
     */
    public abstract NSArray mountedVolumeURLsIncludingResourceValuesForKeys_options(NSArray propertyKeys, NSUInteger options);
    /**
     * -contentsOfDirectoryAtURL:includingPropertiesForKeys:options:error: returns an NSArray of NSURLs identifying the the directory entries. If this method returns nil, an NSError will be returned by reference in the 'error' parameter. If the directory contains no entries, this method will return the empty array. When an array is specified for the 'keys' parameter, the specified property values will be pre-fetched and cached with each enumerated URL.<br>
     * This method always does a shallow enumeration of the specified directory (i.e. it always acts as if NSDirectoryEnumerationSkipsSubdirectoryDescendants has been specified). If you need to perform a deep enumeration, use +[NSFileManager enumeratorAtURL:includingPropertiesForKeys:options:errorHandler:].<br>
     * If you wish to only receive the URLs and no other attributes, then pass '0' for 'options' and an empty NSArray ('[NSArray array]') for 'keys'. If you wish to have the property caches of the vended URLs pre-populated with a default set of attributes, then pass '0' for 'options' and 'nil' for 'keys'.<br>
     * Original signature : <code>-(NSArray*)contentsOfDirectoryAtURL:(NSURL*) includingPropertiesForKeys:(NSArray*) options:(NSDirectoryEnumerationOptions) error:(NSError**)</code><br>
     * <i>native declaration : NSFileManager.h:77</i>
     */
    public abstract NSArray contentsOfDirectoryAtURL_includingPropertiesForKeys_options_error(NSURL url, NSArray keys, NSUInteger mask, PointerByReference error);
    /**
     * <i>native declaration : NSFileManager.h:82</i><br>
     * Conversion Error : /**<br>
     *  * -URLsForDirectory:inDomains: is analogous to NSSearchPathForDirectoriesInDomains(), but returns an array of NSURL instances for use with URL-taking APIs. This API is suitable when you need to search for a file or files which may live in one of a variety of locations in the domains specified.<br>
     *  * Original signature : <code>-(NSArray*)URLsForDirectory:() inDomains:()</code><br>
     *  * /<br>
     * - (NSArray*)URLsForDirectory:(null)directory inDomains:(null)domainMask; (Argument directory cannot be converted)
     */
    /**
     * <i>native declaration : NSFileManager.h:88</i><br>
     * Conversion Error : /**<br>
     *  * -URLForDirectory:inDomain:appropriateForURL:create:error: is a URL-based replacement for FSFindFolder(). It allows for the specification and (optional) creation of a specific directory for a particular purpose (e.g. the replacement of a particular item on disk, or a particular Library directory.<br>
     *  * You may pass only one of the values from the NSSearchPathDomainMask enumeration, and you may not pass NSAllDomainsMask.<br>
     *  * Original signature : <code>-(NSURL*)URLForDirectory:() inDomain:() appropriateForURL:(NSURL*) create:(BOOL) error:(NSError**)</code><br>
     *  * /<br>
     * - (NSURL*)URLForDirectory:(null)directory inDomain:(null)domain appropriateForURL:(NSURL*)url create:(BOOL)shouldCreate error:(NSError**)error; (Argument directory cannot be converted)
     */
    /**
     * Instances of NSFileManager may now have delegates. Each instance has one delegate, and the delegate is not retained. In versions of Mac OS X prior to 10.5, the behavior of calling [[NSFileManager alloc] init] was undefined. In Mac OS X 10.5 "Leopard" and later, calling [[NSFileManager alloc] init] returns a new instance of an NSFileManager.<br>
     * Original signature : <code>-(void)setDelegate:(id)</code><br>
     * <i>native declaration : NSFileManager.h:94</i>
     */
    public abstract void setDelegate(org.rococoa.ObjCObject delegate);

    /**
     * setAttributes:ofItemAtPath:error: returns YES when the attributes specified in the 'attributes' dictionary are set successfully on the item specified by 'path'. If this method returns NO, a presentable NSError will be provided by-reference in the 'error' parameter. If no error is required, you may pass 'nil' for the error.<br>
     * This method replaces changeFileAttributes:atPath:.<br>
     * Original signature : <code>-(BOOL)setAttributes:(NSDictionary*) ofItemAtPath:(String*) error:(NSError**)</code><br>
     * <i>native declaration : NSFileManager.h:101</i>
     */
    public abstract boolean setAttributes_ofItemAtPath_error(NSDictionary attributes, String path, PointerByReference error);
    /**
     * createDirectoryAtPath:withIntermediateDirectories:attributes:error: creates a directory at the specified path. If you pass 'NO' for createIntermediates, the directory must not exist at the time this call is made. Passing 'YES' for 'createIntermediates' will create any necessary intermediate directories. This method returns YES if all directories specified in 'path' were created and attributes were set. Directories are created with attributes specified by the dictionary passed to 'attributes'. If no dictionary is supplied, directories are created according to the umask of the process. This method returns NO if a failure occurs at any stage of the operation. If an error parameter was provided, a presentable NSError will be returned by reference.<br>
     * This method replaces createDirectoryAtPath:attributes:<br>
     * Original signature : <code>-(BOOL)createDirectoryAtPath:(String*) withIntermediateDirectories:(BOOL) attributes:(NSDictionary*) error:(NSError**)</code><br>
     * <i>native declaration : NSFileManager.h:107</i>
     */
    public abstract boolean createDirectoryAtPath_withIntermediateDirectories_attributes_error(String path, boolean createIntermediates, NSDictionary attributes, PointerByReference error);
    /**
     * contentsOfDirectoryAtPath:error: returns an NSArray of Strings representing the filenames of the items in the directory. If this method returns 'nil', an NSError will be returned by reference in the 'error' parameter. If the directory contains no items, this method will return the empty array.<br>
     * This method replaces directoryContentsAtPath:<br>
     * Original signature : <code>-(NSArray*)contentsOfDirectoryAtPath:(String*) error:(NSError**)</code><br>
     * <i>native declaration : NSFileManager.h:113</i>
     */
    public abstract NSArray contentsOfDirectoryAtPath_error(String path, PointerByReference error);
    /**
     * subpathsOfDirectoryAtPath:error: returns an NSArray of Strings represeting the filenames of the items in the specified directory and all its subdirectories recursively. If this method returns 'nil', an NSError will be returned by reference in the 'error' parameter. If the directory contains no items, this method will return the empty array.<br>
     * This method replaces subpathsAtPath:<br>
     * Original signature : <code>-(NSArray*)subpathsOfDirectoryAtPath:(String*) error:(NSError**)</code><br>
     * <i>native declaration : NSFileManager.h:119</i>
     */
    public abstract NSArray subpathsOfDirectoryAtPath_error(String path, PointerByReference error);
    /**
     * attributesOfItemAtPath:error: returns an NSDictionary of key/value pairs containing the attributes of the item (file, directory, symlink, etc.) at the path in question. If this method returns 'nil', an NSError will be returned by reference in the 'error' parameter. This method does not traverse a terminal symlink.<br>
     * This method replaces fileAttributesAtPath:traverseLink:.<br>
     * Original signature : <code>-(NSDictionary*)attributesOfItemAtPath:(String*) error:(NSError**)</code><br>
     * <i>native declaration : NSFileManager.h:125</i>
     */
    public abstract NSDictionary attributesOfItemAtPath_error(String path, PointerByReference error);
    /**
     * attributesOfFileSystemForPath:error: returns an NSDictionary of key/value pairs containing the attributes of the filesystem containing the provided path. If this method returns 'nil', an NSError will be returned by reference in the 'error' parameter. This method does not traverse a terminal symlink.<br>
     * This method replaces fileSystemAttributesAtPath:.<br>
     * Original signature : <code>-(NSDictionary*)attributesOfFileSystemForPath:(String*) error:(NSError**)</code><br>
     * <i>native declaration : NSFileManager.h:131</i>
     */
    public abstract NSDictionary attributesOfFileSystemForPath_error(String path, PointerByReference error);
    /**
     * createSymbolicLinkAtPath:withDestination:error: returns YES if the symbolic link that point at 'destPath' was able to be created at the location specified by 'path'. If this method returns NO, the link was unable to be created and an NSError will be returned by reference in the 'error' parameter. This method does not traverse a terminal symlink.<br>
     * This method replaces createSymbolicLinkAtPath:pathContent:<br>
     * Original signature : <code>-(BOOL)createSymbolicLinkAtPath:(String*) withDestinationPath:(String*) error:(NSError**)</code><br>
     * <i>native declaration : NSFileManager.h:137</i>
     */
    public abstract boolean createSymbolicLinkAtPath_withDestinationPath_error(String path, String destPath, PointerByReference error);
    /**
     * destinationOfSymbolicLinkAtPath:error: returns an String containing the path of the item pointed at by the symlink specified by 'path'. If this method returns 'nil', an NSError will be returned by reference in the 'error' parameter.<br>
     * This method replaces pathContentOfSymbolicLinkAtPath:<br>
     * Original signature : <code>-(String*)destinationOfSymbolicLinkAtPath:(String*) error:(NSError**)</code><br>
     * <i>native declaration : NSFileManager.h:143</i>
     */
    public abstract String destinationOfSymbolicLinkAtPath_error(String path, PointerByReference error);
    /**
     * These methods replace their non-error returning counterparts below. See the NSFileManagerFileOperationAdditions category below for methods that are dispatched to the NSFileManager instance's delegate.<br>
     * Original signature : <code>-(BOOL)copyItemAtPath:(String*) toPath:(String*) error:(NSError**)</code><br>
     * <i>native declaration : NSFileManager.h:147</i>
     */
    public abstract boolean copyItemAtPath_toPath_error(String srcPath, String dstPath, PointerByReference error);
    /**
     * Original signature : <code>-(BOOL)moveItemAtPath:(String*) toPath:(String*) error:(NSError**)</code><br>
     * <i>native declaration : NSFileManager.h:148</i>
     */
    public abstract boolean moveItemAtPath_toPath_error(String srcPath, String dstPath, PointerByReference error);
    /**
     * Original signature : <code>-(BOOL)linkItemAtPath:(String*) toPath:(String*) error:(NSError**)</code><br>
     * <i>native declaration : NSFileManager.h:149</i>
     */
    public abstract boolean linkItemAtPath_toPath_error(String srcPath, String dstPath, PointerByReference error);
    /**
     * Original signature : <code>-(BOOL)removeItemAtPath:(String*) error:(NSError**)</code><br>
     * <i>native declaration : NSFileManager.h:150</i>
     */
    public abstract boolean removeItemAtPath_error(String path, PointerByReference error);
    /**
     * These methods are URL-taking equivalents of the four methods above. Their delegate methods are defined in the NSFileManagerFileOperationAdditions category below.<br>
     * Original signature : <code>-(BOOL)copyItemAtURL:(NSURL*) toURL:(NSURL*) error:(NSError**)</code><br>
     * <i>native declaration : NSFileManager.h:156</i>
     */
    public abstract boolean copyItemAtURL_toURL_error(NSURL srcURL, NSURL dstURL, PointerByReference error);
    /**
     * Original signature : <code>-(BOOL)moveItemAtURL:(NSURL*) toURL:(NSURL*) error:(NSError**)</code><br>
     * <i>native declaration : NSFileManager.h:157</i>
     */
    public abstract boolean moveItemAtURL_toURL_error(NSURL srcURL, NSURL dstURL, PointerByReference error);
    /**
     * Original signature : <code>-(BOOL)linkItemAtURL:(NSURL*) toURL:(NSURL*) error:(NSError**)</code><br>
     * <i>native declaration : NSFileManager.h:158</i>
     */
    public abstract boolean linkItemAtURL_toURL_error(NSURL srcURL, NSURL dstURL, PointerByReference error);
    /**
     * Original signature : <code>-(BOOL)removeItemAtURL:(NSURL*) error:(NSError**)</code><br>
     * <i>native declaration : NSFileManager.h:159</i>
     */
    public abstract boolean removeItemAtURL_error(NSURL URL, PointerByReference error);
    /**
     * The following methods are deprecated on Mac OS X 10.5. Their URL-based and/or error-returning replacements are listed above.<br>
     * Original signature : <code>-(NSDictionary*)fileAttributesAtPath:(String*) traverseLink:(BOOL)</code><br>
     * <i>native declaration : NSFileManager.h:163</i>
     */
    public abstract NSDictionary fileAttributesAtPath_traverseLink(String path, boolean yorn);
    /**
     * Original signature : <code>-(BOOL)changeFileAttributes:(NSDictionary*) atPath:(String*)</code><br>
     * <i>native declaration : NSFileManager.h:164</i>
     */
    public abstract boolean changeFileAttributes_atPath(NSDictionary attributes, String path);
    /**
     * Original signature : <code>-(NSArray*)directoryContentsAtPath:(String*)</code><br>
     * <i>native declaration : NSFileManager.h:165</i>
     */
    public abstract NSArray directoryContentsAtPath(String path);
    /**
     * Original signature : <code>-(NSDictionary*)fileSystemAttributesAtPath:(String*)</code><br>
     * <i>native declaration : NSFileManager.h:166</i>
     */
    public abstract NSDictionary fileSystemAttributesAtPath(String path);
    /**
     * Original signature : <code>-(String*)pathContentOfSymbolicLinkAtPath:(String*)</code><br>
     * <i>native declaration : NSFileManager.h:167</i>
     */
    public abstract String pathContentOfSymbolicLinkAtPath(String path);
    /**
     * Original signature : <code>-(BOOL)createSymbolicLinkAtPath:(String*) pathContent:(String*)</code><br>
     * <i>native declaration : NSFileManager.h:168</i>
     */
    public abstract boolean createSymbolicLinkAtPath_pathContent(String path, String otherpath);
    /**
     * Original signature : <code>-(BOOL)createDirectoryAtPath:(String*) attributes:(NSDictionary*)</code><br>
     * <i>native declaration : NSFileManager.h:169</i>
     */
    public abstract boolean createDirectoryAtPath_attributes(String path, NSDictionary attributes);
    /**
     * Original signature : <code>-(BOOL)linkPath:(String*) toPath:(String*) handler:(id)</code><br>
     * <i>native declaration : NSFileManager.h:172</i>
     */
    public abstract boolean linkPath_toPath_handler(String src, String dest, org.rococoa.ObjCObject handler);
    /**
     * Original signature : <code>-(BOOL)copyPath:(String*) toPath:(String*) handler:(id)</code><br>
     * <i>native declaration : NSFileManager.h:173</i>
     */
    public abstract boolean copyPath_toPath_handler(String src, String dest, org.rococoa.ObjCObject handler);
    /**
     * Original signature : <code>-(BOOL)movePath:(String*) toPath:(String*) handler:(id)</code><br>
     * <i>native declaration : NSFileManager.h:174</i>
     */
    public abstract boolean movePath_toPath_handler(String src, String dest, org.rococoa.ObjCObject handler);
    /**
     * Original signature : <code>-(BOOL)removeFileAtPath:(String*) handler:(id)</code><br>
     * <i>native declaration : NSFileManager.h:175</i>
     */
    public abstract boolean removeFileAtPath_handler(String path, org.rococoa.ObjCObject handler);
    /**
     * Process working directory management. Despite the fact that these are instance methods on NSFileManager, these methods report and change (respectively) the working directory for the entire process. Developers are cautioned that doing so is fraught with peril.<br>
     * Original signature : <code>-(String*)currentDirectoryPath</code><br>
     * <i>native declaration : NSFileManager.h:180</i>
     */
    public abstract String currentDirectoryPath();
    /**
     * Original signature : <code>-(BOOL)changeCurrentDirectoryPath:(String*)</code><br>
     * <i>native declaration : NSFileManager.h:181</i>
     */
    public abstract boolean changeCurrentDirectoryPath(String path);
    /**
     * The following methods are of limited utility. Attempting to predicate behavior based on the current state of the filesystem or a particular file on the filesystem is encouraging odd behavior in the face of filesystem race conditions. It's far better to attempt an operation (like loading a file or creating a directory) and handle the error gracefully than it is to try to figure out ahead of time whether the operation will succeed.<br>
     * Original signature : <code>-(BOOL)fileExistsAtPath:(String*)</code><br>
     * <i>native declaration : NSFileManager.h:185</i>
     */
    public abstract boolean fileExistsAtPath(String path);
    /**
     * Original signature : <code>-(BOOL)fileExistsAtPath:(String*) isDirectory:(BOOL*)</code><br>
     * <i>native declaration : NSFileManager.h:186</i>
     */
    public abstract boolean fileExistsAtPath_isDirectory(String path, boolean isDirectory);
    /**
     * Original signature : <code>-(BOOL)isReadableFileAtPath:(String*)</code><br>
     * <i>native declaration : NSFileManager.h:187</i>
     */
    public abstract boolean isReadableFileAtPath(String path);
    /**
     * Original signature : <code>-(BOOL)isWritableFileAtPath:(String*)</code><br>
     * <i>native declaration : NSFileManager.h:188</i>
     */
    public abstract boolean isWritableFileAtPath(String path);
    /**
     * Original signature : <code>-(BOOL)isExecutableFileAtPath:(String*)</code><br>
     * <i>native declaration : NSFileManager.h:189</i>
     */
    public abstract boolean isExecutableFileAtPath(String path);
    /**
     * Original signature : <code>-(BOOL)isDeletableFileAtPath:(String*)</code><br>
     * <i>native declaration : NSFileManager.h:190</i>
     */
    public abstract boolean isDeletableFileAtPath(String path);
    /**
     * -contentsEqualAtPath:andPath: does not take into account data stored in the resource fork or filesystem extended attributes.<br>
     * Original signature : <code>-(BOOL)contentsEqualAtPath:(String*) andPath:(String*)</code><br>
     * <i>native declaration : NSFileManager.h:194</i>
     */
    public abstract boolean contentsEqualAtPath_andPath(String path1, String path2);
    /**
     * displayNameAtPath: returns an String suitable for presentation to the user. For directories which have localization information, this will return the appropriate localized string. This string is not suitable for passing to anything that must interact with the filesystem.<br>
     * Original signature : <code>-(String*)displayNameAtPath:(String*)</code><br>
     * <i>native declaration : NSFileManager.h:198</i>
     */
    public abstract String displayNameAtPath(String path);
    /**
     * componentsToDisplayForPath: returns an NSArray of display names for the path provided. Localization will occur as in displayNameAtPath: above. This array cannot and should not be reassembled into an usable filesystem path for any kind of access.<br>
     * Original signature : <code>-(NSArray*)componentsToDisplayForPath:(String*)</code><br>
     * <i>native declaration : NSFileManager.h:203</i>
     */
    public abstract NSArray componentsToDisplayForPath(String path);
    /**
     * subpathsAtPath: returns an NSArray of all contents and subpaths recursively from the provided path. This may be very expensive to compute for deep filesystem hierarchies, and should probably be avoided.<br>
     * Original signature : <code>-(NSArray*)subpathsAtPath:(String*)</code><br>
     * <i>native declaration : NSFileManager.h:220</i>
     */
    public abstract NSArray subpathsAtPath(String path);
    /**
     * These methods are provided here for compatibility. The corresponding methods on NSData which return NSErrors should be regarded as the primary method of creating a file from an NSData or retrieving the contents of a file as an NSData.<br>
     * Original signature : <code>-(NSData*)contentsAtPath:(String*)</code><br>
     * <i>native declaration : NSFileManager.h:224</i>
     */
    public abstract NSData contentsAtPath(String path);
    /**
     * Original signature : <code>-(BOOL)createFileAtPath:(String*) contents:(NSData*) attributes:(NSDictionary*)</code><br>
     * <i>native declaration : NSFileManager.h:225</i>
     */
    public abstract boolean createFileAtPath_contents_attributes(String path, NSData data, NSDictionary attr);
    /**
     * fileSystemRepresentationWithPath: returns an array of characters suitable for passing to lower-level POSIX style APIs. The string is provided in the representation most appropriate for the filesystem in question.<br>
     * Original signature : <code>-(const char*)fileSystemRepresentationWithPath:(String*)</code><br>
     * <i>native declaration : NSFileManager.h:229</i>
     */
    public abstract String fileSystemRepresentationWithPath(String path);
    /**
     * stringWithFileSystemRepresentation:length: returns an String created from an array of bytes that are in the filesystem representation.<br>
     * Original signature : <code>-(String*)stringWithFileSystemRepresentation:(const char*) length:(NSUInteger)</code><br>
     * <i>native declaration : NSFileManager.h:233</i>
     */
    public abstract String stringWithFileSystemRepresentation_length(String str, NSUInteger len);
    /**
     * -replaceItemAtURL:withItemAtURL:backupItemName:options:resultingItemURL:error: is for developers who wish to perform a safe-save without using the full NSDocument machinery that is available in the AppKit.<br>
     * The `originalItemURL` is the item being replaced.<br>
     * `newItemURL` is the item which will replace the original item. This item should be placed in a temporary directory as provided by the OS, or in a uniquely named directory placed in the same directory as the original item if the temporary directory is not available.<br>
     * If `backupItemName` is provided, that name will be used to create a backup of the original item. The backup is placed in the same directory as the original item. If an error occurs during the creation of the backup item, the operation will fail. If there is already an item with the same name as the backup item, that item will be removed. The backup item will be removed in the event of success unless the `NSFileManagerItemReplacementWithoutDeletingBackupItem` option is provided in `options`.<br>
     * For `options`, pass `0` to get the default behavior, which uses only the metadata from the new item while adjusting some properties using values from the original item. Pass `NSFileManagerItemReplacementUsingNewMetadataOnly` in order to use all possible metadata from the new item.<br>
     * Original signature : <code>-(BOOL)replaceItemAtURL:(NSURL*) withItemAtURL:(NSURL*) backupItemName:(String*) options:(NSFileManagerItemReplacementOptions) resultingItemURL:(NSURL**) error:(NSError**)</code><br>
     * <i>native declaration : NSFileManager.h:242</i>
     */
    public abstract boolean replaceItemAtURL_withItemAtURL_backupItemName_options_resultingItemURL_error(NSURL originalItemURL, NSURL newItemURL, String backupItemName, NSUInteger options, PointerByReference resultingURL, PointerByReference error);
    /// <i>native declaration : NSFileManager.h</i>
}
