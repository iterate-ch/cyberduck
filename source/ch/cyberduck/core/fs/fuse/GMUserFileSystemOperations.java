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
import ch.cyberduck.ui.cocoa.foundation.NSData;
import ch.cyberduck.ui.cocoa.foundation.NSDictionary;

import org.rococoa.ID;

import com.sun.jna.IntegerType;
import com.sun.jna.ptr.PointerByReference;

/**
 * @version $Id:$
 * @category
 * @discussion The core set of file system operations the delegate must implement.
 * Unless otherwise noted, they typically should behave like the NSFileManager
 * equivalent. However, the error codes that they return should correspond to
 * the BSD-equivalent call and be in the NSPOSIXErrorDomain.<br>
 * <p/>
 * For a read-only filesystem, you can typically pick-and-choose which methods
 * to implement.  For example, a minimal read-only filesystem might implement:<ul>
 * - (NSArray *)contentsOfDirectoryAtPath:String path
 * error:(NSError **)error;<br>
 * - (NSDictionary *)attributesOfItemAtPath:String path
 * userData:(id)userData
 * error:(NSError **)error;<br>
 * - (NSData *)contentsAtPath:String path;</ul>
 * For a writeable filesystem, the Finder can be quite picky unless the majority
 * of these methods are implemented. However, you can safely skip hard-links,
 * symbolic links, and extended attributes.
 */
public interface GMUserFileSystemOperations {

    /*!
    * @abstract Returns directory contents at the specified path.
    * @discussion Returns an array of NSString containing the names of files and
    * sub-directories in the specified directory.
    * @seealso man readdir(3)
    * @param path The path to a directory.
    * @param error Should be filled with a POSIX error in case of failure.
    * @result An array of NSString or nil on error.
    */
    NSArray contentsOfDirectoryAtPath_error(String path, PointerByReference error);


    /*!
    * @abstract Returns attributes at the specified path.
    * @discussion
    * Returns a dictionary of attributes at the given path. It is required to
    * return at least the NSFileType attribute. You may omit the NSFileSize
    * attribute if contentsAtPath: is implemented, although this is less efficient.
    * The following keys are currently supported (unknown keys are ignored):<ul>
    *   <li>NSFileType [Required]
    *   <li>NSFileSize [Recommended]
    *   <li>NSFileModificationDate
    *   <li>NSFileReferenceCount
    *   <li>NSFilePosixPermissions
    *   <li>NSFileOwnerAccountID
    *   <li>NSFileGroupOwnerAccountID
    *   <li>NSFileSystemFileNumber             (64-bit on 10.5+)
    *   <li>NSFileCreationDate                 (if supports extended dates)
    *   <li>kGMUserFileSystemFileBackupDateKey (if supports extended dates)
    *   <li>kGMUserFileSystemFileChangeDateKey
    *   <li>kGMUserFileSystemFileAccessDateKey
    *   <li>kGMUserFileSystemFileFlagsKey</ul>
    *
    * If this is the fstat variant and userData was supplied in openFileAtPath: or
    * createFileAtPath: then it will be passed back in this call.
    *
    * @seealso man stat(2), fstat(2)
    * @param path The path to the item.
    * @param userData The userData corresponding to this open file or nil.
    * @param error Should be filled with a POSIX error in case of failure.
    * @result A dictionary of attributes or nil on error.
    */
    NSDictionary attributesOfItemAtPath_userData_error(String path, ID userData, PointerByReference error);

    /*!
    * @abstract Returns file system attributes.
    * @discussion
    * Returns a dictionary of attributes for the file system.
    * The following keys are currently supported (unknown keys are ignored):<ul>
    *   <li>NSFileSystemSize
    *   <li>NSFileSystemFreeSize
    *   <li>NSFileSystemNodes
    *   <li>NSFileSystemFreeNodes
    *   <li>kGMUserFileSystemVolumeSupportsExtendedDatesKey</ul>
    *
    * @seealso man statvfs(3)
    * @param path A path on the file system (it is safe to ignore this).
    * @param error Should be filled with a POSIX error in case of failure.
    * @result A dictionary of attributes for the file system.
    */
    NSDictionary attributesOfFileSystemForPath_error(String path, PointerByReference error);

    /*!
    * @abstract Set attributes at the specified path.
    * @discussion
    * Sets the attributes for the item at the specified path. The following keys
    * may be present (you must ignore unknown keys):<ul>
    *   <li>NSFileSize
    *   <li>NSFileOwnerAccountID
    *   <li>NSFileGroupOwnerAccountID
    *   <li>NSFilePosixPermissions
    *   <li>NSFileModificationDate
    *   <li>NSFileCreationDate                  (if supports extended dates)
    *   <li>kGMUserFileSystemFileBackupDateKey  (if supports extended dates)
    *   <li>kGMUserFileSystemFileChangeDateKey
    *   <li>kGMUserFileSystemFileAccessDateKey
    *   <li>kGMUserFileSystemFileFlagsKey</ul>
    *
    * If this is the f-variant and userData was supplied in openFileAtPath: or
    * createFileAtPath: then it will be passed back in this call.
    *
    * @seealso man truncate(2), chown(2), chmod(2), utimes(2), chflags(2),
    *              ftruncate(2), fchown(2), fchmod(2), futimes(2), fchflags(2)
    * @param attributes The attributes to set.
    * @param path The path to the item.
    * @param userData The userData corresponding to this open file or nil.
    * @param error Should be filled with a POSIX error in case of failure.
    * @result YES if the attributes are successfully set.
    */
    boolean setAttributes_ofItemAtPath_userData_error(NSDictionary attributes, String path, ID userData, PointerByReference error);

// mark File Contents

    /*!
    * @abstract Returns directory contents at the specified path.
    * @discussion Returns the full contents at the given path. Implementation of
    * this delegate method is recommended only by very simple file systems that are
    * not concerned with performance. If contentsAtPath is implemented then you can
    * skip open/release/read.
    * @param path The path to the file.
    * @result The contents of the file or nil if a file does not exist at path.
    */
    NSData contentsAtPath(String path);

    /*!
    * @abstract Opens the file at the given path for read/write.
    * @discussion This will only be called for existing files. If the file needs
    * to be created then createFileAtPath: will be called instead.
    * @seealso man open(2)
    * @param path The path to the file.
    * @param mode The open mode for the file (e.g. O_RDWR, etc.)
    * @param userData Out parameter that can be filled in with arbitrary user data.
    *        The given userData will be retained and passed back in to delegate
    *        methods that are acting on this open file.
    * @param error Should be filled with a POSIX error in case of failure.
    * @result YES if the file was opened successfully.
    */
    boolean openFileAtPath_mode_userData_error(String path, int mode, ID userData, PointerByReference error);

    /*!
    * @abstract Called when an opened file is closed.
    * @discussion If userData was provided in the corresponding openFileAtPath: call
    * then it will be passed in userData and released after this call completes.
    * @seealso man close(2)
    * @param path The path to the file.
    * @param userData The userData corresponding to this open file or nil.
    */
    void releaseFileAtPath_userData(String path, ID userData);

    /*!
    * @abstract Reads data from the open file at the specified path.
    * @discussion Reads data from the file starting at offset into the provided
    * buffer and returns the number of bytes read. If userData was provided in the
    * corresponding openFileAtPath: or createFileAtPath: call then it will be
    * passed in.
    * @seealso man pread(2)
    * @param path The path to the file.
    * @param userData The userData corresponding to this open file or nil.
    * @param buffer Byte buffer to read data from the file into.
    * @param size The size of the provided buffer.
    * @param offset The offset in the file from which to read data.
    * @param error Should be filled with a POSIX error in case of failure.
    * @result The number of bytes read or -1 on error.
    */
    int readFileAtPath_userData_buffer_size_offset_error(String path, ID userData, byte[] buffer, IntegerType size, IntegerType offset, PointerByReference error);

    /*!
    * @abstract Writes data to the open file at the specified path.
    * @discussion Writes data to the file starting at offset from the provided
    * buffer and returns the number of bytes written. If userData was provided in
    * the corresponding openFileAtPath: or createFileAtPath: call then it will be
    * passed in.
    * @seealso man pwrite(2)
    * @param path The path to the file.
    * @param userData The userData corresponding to this open file or nil.
    * @param buffer Byte buffer containing the data to write to the file.
    * @param size The size of the provided buffer.
    * @param offset The offset in the file to write data.
    * @param error Should be filled with a POSIX error in case of failure.
    * @result The number of bytes written or -1 on error.
    */
    int writeFileAtPath_userData_buffer_size_offset_error(String path, ID userData, byte[] buffer, IntegerType size, IntegerType offset, PointerByReference error);

    /*!
    * @abstract Atomically exchanges data between files.
    * @discussion  Called to atomically exchange file data between path1 and path2.
    * @seealso man exchangedata(2)
    * @param path1 The path to the file.
    * @param path2 The path to the other file.
    * @param error Should be filled with a POSIX error in case of failure.
    * @result YES if data was exchanged successfully.
    */
    boolean exchangeDataOfItemAtPath_withItemAtPath_error(String path, String other, PointerByReference error);

// mark Creating an Item

    /*!
    * @abstract Creates a directory at the specified path.
    * @discussion  The attributes may contain keys similar to setAttributes:.
    * @seealso man mkdir(2)
    * @param path The directory path to create.
    * @param attributes Set of attributes to apply to the newly created directory.
    * @param error Should be filled with a POSIX error in case of failure.
    * @result YES if the directory was successfully created.
    */
    boolean createDirectoryAtPath_attributes_error(String path, NSDictionary attributes, PointerByReference error);

    /*!
    * @abstract Creates and opens a file at the specified path.
    * @discussion  This should create and open the file at the same time. The
    * attributes may contain keys similar to setAttributes:.
    * @seealso man creat(2)
    * @param path The path of the file to create.
    * @param attributes Set of attributes to apply to the newly created file.
    * @param userData Out parameter that can be filled in with arbitrary user data.
    *        The given userData will be retained and passed back in to delegate
    *        methods that are acting on this open file.
    * @param error Should be filled with a POSIX error in case of failure.
    * @result YES if the directory was successfully created.
    */
    boolean createFileAtPath_attributes_userData_error(String path, NSDictionary attributes, ID userData, PointerByReference error);

// mark Moving an Item

    /*!
    * @abstract Moves or renames an item.
    * @discussion Move, also known as rename, is one of the more difficult file
    * system methods to implement properly. Care should be taken to handle all
    * error conditions and return proper POSIX error codes.
    * @seealso man rename(2)
    * @param source The source file or directory.
    * @param destination The destination file or directory.
    * @param error Should be filled with a POSIX error in case of failure.
    * @result YES if the move was successful.
    */
    boolean moveItemAtPath_toPath_error(String source, String destination, PointerByReference error);

// mark Removing an Item

    /*!
    * @abstract Remove the directory at the given path.
    * @discussion Unlike NSFileManager, this should not recursively remove
    * subdirectories. If this method is not implemented, then removeItemAtPath
    * will be called even for directories.
    * @seealso man rmdir(2)
    * @param path The directory to remove.
    * @param error Should be filled with a POSIX error in case of failure.
    * @result YES if the directory was successfully removed.
    */
    boolean removeDirectoryAtPath_error(String path, PointerByReference error);

    /*!
    * @abstract Removes the item at the given path.
    * @discussion This should not recursively remove subdirectories. If
    * removeDirectoryAtPath is implemented, then that will be called instead of
    * this selector if the item is a directory.
    * @seealso man unlink(2), rmdir(2)
    * @param path The path to the item to remove.
    * @param error Should be filled with a POSIX error in case of failure.
    * @result YES if the item was successfully removed.
    */
    boolean removeItemAtPath_error(String path, PointerByReference error);

// mark Linking an Item

    /*!
    * @abstract Creates a hard link.
    * @seealso man link(2)
    * @param path The path for the created hard link.
    * @param otherPath The path that is the target of the created hard link.
    * @param error Should be filled with a POSIX error in case of failure.
    * @result YES if the hard link was successfully created.
    */
    boolean linkItemAtPath_toPath_error(String path, String otherPath, PointerByReference error);

// mark Symbolic Links

    /*!
    * @abstract Creates a symbolic link.
    * @seealso man symlink(2)
    * @param path The path for the created symbolic link.
    * @param otherPath The path that is the target of the symbolic link.
    * @param error Should be filled with a POSIX error in case of failure.
    * @result YES if the symbolic link was successfully created.
    */
    boolean createSymbolicLinkAtPath_withDestinationPath_error(String path, String otherPath, PointerByReference error);

    /*!
    * @abstract Reads the destination of a symbolic link.
    * @seealso man readlink(2)
    * @param path The path to the specified symlink.
    * @param error Should be filled with a POSIX error in case of failure.
    * @result The destination path of the symbolic link or nil on error.
    */
    String destinationOfSymbolicLinkAtPath_error(String path, PointerByReference error);

// mark Extended Attributes

    /*!
    * @abstract Returns the names of the extended attributes at the specified path.
    * @discussion If there are no extended attributes at this path, then return an
    * empty array. Return nil only on error.
    * @seealso man listxattr(2)
    * @param path The path to the specified file.
    * @param error Should be filled with a POSIX error in case of failure.
    * @result An NSArray of extended attribute names or nil on error.
    */
    NSArray extendedAttributesOfItemAtPath_error(String path, PointerByReference error);

    /*!
    * @abstract Returns the contents of the extended attribute at the specified path.
    * @seealso man getxattr(2)
    * @param name The name of the extended attribute.
    * @param path The path to the specified file.
    * @param position The offset within the attribute to read from.
    * @param error Should be filled with a POSIX error in case of failure.
    * @result The data corresponding to the attribute or nil on error.
    */
    NSData valueOfExtendedAttribute_ofItemAtPath_position_error(String name, String path, IntegerType position, PointerByReference error);

    /*!
    * @abstract Writes the contents of the extended attribute at the specified path.
    * @seealso man setxattr(2)
    * @param name The name of the extended attribute.
    * @param path The path to the specified file.
    * @param value The data to write.
    * @param position The offset within the attribute to write to
    * @param options Options (see setxattr man page).
    * @param error Should be filled with a POSIX error in case of failure.
    * @result YES if the attribute was successfully written.
    */
    boolean setExtendedAttribute_ofItemAtPath_value_position_options_error(String name, String path, NSData value, IntegerType position, int options, PointerByReference error);

    /*!
    * @abstract Removes the extended attribute at the specified path.
    * @seealso man removexattr(2)
    * @param name The name of the extended attribute.
    * @param path The path to the specified file.
    * @param error Should be filled with a POSIX error in case of failure.
    * @result YES if the attribute was successfully removed.
    */
    boolean removeExtendedAttribute_ofItemAtPath_error(String name, String path, PointerByReference error);

// mark Additional Item Attribute Keys

/*! @group Additional Item Attribute Keys */

    /*! 
    * @abstract File flags.
    * @discussion The value should be an NSNumber* with uint32 value that is the
    * file st_flags (man 2 stat). 
    */
    static final String kGMUserFileSystemFileFlagsKey = "kGMUserFileSystemFileFlagsKey";

    /*! 
    * @abstract File access date.
    * @discussion The value should be an NSDate that is the last file access 
    * time. See st_atimespec (man 2 stat). 
    */
    static final String kGMUserFileSystemFileAccessDateKey = "kGMUserFileSystemFileAccessDateKey";

    /*! 
    * @abstract File status change date.
    * @discussion The value should be an NSDate that is the last file status change 
    * time. See st_ctimespec (man 2 stat). 
    */
    static final String kGMUserFileSystemFileChangeDateKey = "kGMUserFileSystemFileChangeDateKey";

    /*! 
    * @abstract  For file backup date. 
    * @discussion The value should be an NSDate that is the backup date. 
    */
    static final String kGMUserFileSystemFileBackupDateKey = "kGMUserFileSystemFileBackupDateKey";

// mark Additional Volume Attribute Keys

/*! @group Additional Volume Attribute Keys */

    /*! 
    * @abstract Specifies support for extended dates.
    * @discussion The value should be a boolean NSNumber that indicates whether or 
    * not the file system supports extended dates such as creation and backup dates.
    */
    static final String kGMUserFileSystemVolumeSupportsExtendedDatesKey = "kGMUserFileSystemVolumeSupportsExtendedDatesKey";

// mark Additional Finder and Resource Fork keys

/*! @group Additional Finder and Resource Fork Keys */

    /*! 
    * @abstract FinderInfo flags.
    * @discussion The value should contain an NSNumber created by OR'ing together
    * Finder flags (e.g. kHasCustomIcon). See CarbonCore/Finder.h. 
    */
    static final String kGMUserFileSystemFinderFlagsKey = "kGMUserFileSystemFinderFlagsKey";

    /*!
    * @abstract FinderInfo extended flags.
    * @discussion The value should contain an NSNumber created by OR'ing together
    * extended Finder flags. See CarbonCore/Finder.h.
    */
    static final String kGMUserFileSystemFinderExtendedFlagsKey = "kGMUserFileSystemFinderExtendedFlagsKey";

    /*! 
    * @abstract ResourceFork custom icon. 
    * @discussion The value should be NSData for a raw .icns file. 
    */
    static final String kGMUserFileSystemCustomIconDataKey = "kGMUserFileSystemCustomIconDataKey";

    /*! 
    * @abstract ResourceFork webloc.
    * @discussion The value should be an NSURL that is the webloc.
    */
    static final String kGMUserFileSystemWeblocURLKey = "kGMUserFileSystemWeblocURLKey";

}
