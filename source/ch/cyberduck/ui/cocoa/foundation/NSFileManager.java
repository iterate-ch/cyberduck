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

/// <i>native declaration : :12</i>
public abstract class NSFileManager implements NSObject {
    private static final _Class CLASS = org.rococoa.Rococoa.createClass("NSFileManager", _Class.class);

    public static NSFileManager defaultManager() {
        return CLASS.defaultManager();
    }

    public static final class NSFileType {
        public static final class NSFileType_holder extends com.sun.jna.Structure {
            public NSString value;

            NSFileType_holder(com.sun.jna.Pointer pointer) {
                super();
                useMemory(pointer, 0);
                read();
            }
        }

        private static NSFileType_holder NSFileType;

        public static synchronized NSFileType_holder get() {
            if(NSFileType == null) {
                NSFileType = new NSFileType_holder(((com.sun.jna.NativeLibrary) CLASS).getGlobalVariableAddress("NSFileType"));
            }
            return NSFileType;
        }
    }

    public static final class NSFileTypeDirectory {
        public static final class NSFileTypeDirectory_holder extends com.sun.jna.Structure {
            public NSString value;

            NSFileTypeDirectory_holder(com.sun.jna.Pointer pointer) {
                super();
                useMemory(pointer, 0);
                read();
            }
        }

        private static NSFileTypeDirectory_holder NSFileTypeDirectory;

        public static synchronized NSFileTypeDirectory_holder get() {
            if(NSFileTypeDirectory == null) {
                NSFileTypeDirectory = new NSFileTypeDirectory_holder(((com.sun.jna.NativeLibrary) CLASS).getGlobalVariableAddress("NSFileTypeDirectory"));
            }
            return NSFileTypeDirectory;
        }
    }

    public static final class NSFileTypeRegular {
        public static final class NSFileTypeRegular_holder extends com.sun.jna.Structure {
            public NSString value;

            NSFileTypeRegular_holder(com.sun.jna.Pointer pointer) {
                super();
                useMemory(pointer, 0);
                read();
            }
        }

        private static NSFileTypeRegular_holder NSFileTypeRegular;

        public static synchronized NSFileTypeRegular_holder get() {
            if(NSFileTypeRegular == null) {
                NSFileTypeRegular = new NSFileTypeRegular_holder(((com.sun.jna.NativeLibrary) CLASS).getGlobalVariableAddress("NSFileTypeRegular"));
            }
            return NSFileTypeRegular;
        }
    }

    public static final class NSFileTypeSymbolicLink {
        public static final class NSFileTypeSymbolicLink_holder extends com.sun.jna.Structure {
            public NSString value;

            NSFileTypeSymbolicLink_holder(com.sun.jna.Pointer pointer) {
                super();
                useMemory(pointer, 0);
                read();
            }
        }

        private static NSFileTypeSymbolicLink_holder NSFileTypeSymbolicLink;

        public static synchronized NSFileTypeSymbolicLink_holder get() {
            if(NSFileTypeSymbolicLink == null) {
                NSFileTypeSymbolicLink = new NSFileTypeSymbolicLink_holder(((com.sun.jna.NativeLibrary) CLASS).getGlobalVariableAddress("NSFileTypeSymbolicLink"));
            }
            return NSFileTypeSymbolicLink;
        }
    }

    public static final class NSFileTypeSocket {
        public static final class NSFileTypeSocket_holder extends com.sun.jna.Structure {
            public NSString value;

            NSFileTypeSocket_holder(com.sun.jna.Pointer pointer) {
                super();
                useMemory(pointer, 0);
                read();
            }
        }

        private static NSFileTypeSocket_holder NSFileTypeSocket;

        public static synchronized NSFileTypeSocket_holder get() {
            if(NSFileTypeSocket == null) {
                NSFileTypeSocket = new NSFileTypeSocket_holder(((com.sun.jna.NativeLibrary) CLASS).getGlobalVariableAddress("NSFileTypeSocket"));
            }
            return NSFileTypeSocket;
        }
    }

    public static final class NSFileTypeCharacterSpecial {
        public static final class NSFileTypeCharacterSpecial_holder extends com.sun.jna.Structure {
            public NSString value;

            NSFileTypeCharacterSpecial_holder(com.sun.jna.Pointer pointer) {
                super();
                useMemory(pointer, 0);
                read();
            }
        }

        private static NSFileTypeCharacterSpecial_holder NSFileTypeCharacterSpecial;

        public static synchronized NSFileTypeCharacterSpecial_holder get() {
            if(NSFileTypeCharacterSpecial == null) {
                NSFileTypeCharacterSpecial = new NSFileTypeCharacterSpecial_holder(((com.sun.jna.NativeLibrary) CLASS).getGlobalVariableAddress("NSFileTypeCharacterSpecial"));
            }
            return NSFileTypeCharacterSpecial;
        }
    }

    public static final class NSFileTypeBlockSpecial {
        public static final class NSFileTypeBlockSpecial_holder extends com.sun.jna.Structure {
            public NSString value;

            NSFileTypeBlockSpecial_holder(com.sun.jna.Pointer pointer) {
                super();
                useMemory(pointer, 0);
                read();
            }
        }

        private static NSFileTypeBlockSpecial_holder NSFileTypeBlockSpecial;

        public static synchronized NSFileTypeBlockSpecial_holder get() {
            if(NSFileTypeBlockSpecial == null) {
                NSFileTypeBlockSpecial = new NSFileTypeBlockSpecial_holder(((com.sun.jna.NativeLibrary) CLASS).getGlobalVariableAddress("NSFileTypeBlockSpecial"));
            }
            return NSFileTypeBlockSpecial;
        }
    }

    public static final class NSFileTypeUnknown {
        public static final class NSFileTypeUnknown_holder extends com.sun.jna.Structure {
            public NSString value;

            NSFileTypeUnknown_holder(com.sun.jna.Pointer pointer) {
                super();
                useMemory(pointer, 0);
                read();
            }
        }

        private static NSFileTypeUnknown_holder NSFileTypeUnknown;

        public static synchronized NSFileTypeUnknown_holder get() {
            if(NSFileTypeUnknown == null) {
                NSFileTypeUnknown = new NSFileTypeUnknown_holder(((com.sun.jna.NativeLibrary) CLASS).getGlobalVariableAddress("NSFileTypeUnknown"));
            }
            return NSFileTypeUnknown;
        }
    }

    public static final class NSFileSize {
        public static final class NSFileSize_holder extends com.sun.jna.Structure {
            public NSString value;

            NSFileSize_holder(com.sun.jna.Pointer pointer) {
                super();
                useMemory(pointer, 0);
                read();
            }
        }

        private static NSFileSize_holder NSFileSize;

        public static synchronized NSFileSize_holder get() {
            if(NSFileSize == null) {
                NSFileSize = new NSFileSize_holder(((com.sun.jna.NativeLibrary) CLASS).getGlobalVariableAddress("NSFileSize"));
            }
            return NSFileSize;
        }
    }

    public static final class NSFileModificationDate {
        public static final class NSFileModificationDate_holder extends com.sun.jna.Structure {
            public NSString value;

            NSFileModificationDate_holder(com.sun.jna.Pointer pointer) {
                super();
                useMemory(pointer, 0);
                read();
            }
        }

        private static NSFileModificationDate_holder NSFileModificationDate;

        public static synchronized NSFileModificationDate_holder get() {
            if(NSFileModificationDate == null) {
                NSFileModificationDate = new NSFileModificationDate_holder(((com.sun.jna.NativeLibrary) CLASS).getGlobalVariableAddress("NSFileModificationDate"));
            }
            return NSFileModificationDate;
        }
    }

    public static final class NSFileReferenceCount {
        public static final class NSFileReferenceCount_holder extends com.sun.jna.Structure {
            public NSString value;

            NSFileReferenceCount_holder(com.sun.jna.Pointer pointer) {
                super();
                useMemory(pointer, 0);
                read();
            }
        }

        private static NSFileReferenceCount_holder NSFileReferenceCount;

        public static synchronized NSFileReferenceCount_holder get() {
            if(NSFileReferenceCount == null) {
                NSFileReferenceCount = new NSFileReferenceCount_holder(((com.sun.jna.NativeLibrary) CLASS).getGlobalVariableAddress("NSFileReferenceCount"));
            }
            return NSFileReferenceCount;
        }
    }

    public static final class NSFileDeviceIdentifier {
        public static final class NSFileDeviceIdentifier_holder extends com.sun.jna.Structure {
            public NSString value;

            NSFileDeviceIdentifier_holder(com.sun.jna.Pointer pointer) {
                super();
                useMemory(pointer, 0);
                read();
            }
        }

        private static NSFileDeviceIdentifier_holder NSFileDeviceIdentifier;

        public static synchronized NSFileDeviceIdentifier_holder get() {
            if(NSFileDeviceIdentifier == null) {
                NSFileDeviceIdentifier = new NSFileDeviceIdentifier_holder(((com.sun.jna.NativeLibrary) CLASS).getGlobalVariableAddress("NSFileDeviceIdentifier"));
            }
            return NSFileDeviceIdentifier;
        }
    }

    public static final class NSFileOwnerAccountName {
        public static final class NSFileOwnerAccountName_holder extends com.sun.jna.Structure {
            public NSString value;

            NSFileOwnerAccountName_holder(com.sun.jna.Pointer pointer) {
                super();
                useMemory(pointer, 0);
                read();
            }
        }

        private static NSFileOwnerAccountName_holder NSFileOwnerAccountName;

        public static synchronized NSFileOwnerAccountName_holder get() {
            if(NSFileOwnerAccountName == null) {
                NSFileOwnerAccountName = new NSFileOwnerAccountName_holder(((com.sun.jna.NativeLibrary) CLASS).getGlobalVariableAddress("NSFileOwnerAccountName"));
            }
            return NSFileOwnerAccountName;
        }
    }

    public static final class NSFileGroupOwnerAccountName {
        public static final class NSFileGroupOwnerAccountName_holder extends com.sun.jna.Structure {
            public NSString value;

            NSFileGroupOwnerAccountName_holder(com.sun.jna.Pointer pointer) {
                super();
                useMemory(pointer, 0);
                read();
            }
        }

        private static NSFileGroupOwnerAccountName_holder NSFileGroupOwnerAccountName;

        public static synchronized NSFileGroupOwnerAccountName_holder get() {
            if(NSFileGroupOwnerAccountName == null) {
                NSFileGroupOwnerAccountName = new NSFileGroupOwnerAccountName_holder(((com.sun.jna.NativeLibrary) CLASS).getGlobalVariableAddress("NSFileGroupOwnerAccountName"));
            }
            return NSFileGroupOwnerAccountName;
        }
    }

    public static final class NSFilePosixPermissions {
        public static final class NSFilePosixPermissions_holder extends com.sun.jna.Structure {
            public NSString value;

            NSFilePosixPermissions_holder(com.sun.jna.Pointer pointer) {
                super();
                useMemory(pointer, 0);
                read();
            }
        }

        private static NSFilePosixPermissions_holder NSFilePosixPermissions;

        public static synchronized NSFilePosixPermissions_holder get() {
            if(NSFilePosixPermissions == null) {
                NSFilePosixPermissions = new NSFilePosixPermissions_holder(((com.sun.jna.NativeLibrary) CLASS).getGlobalVariableAddress("NSFilePosixPermissions"));
            }
            return NSFilePosixPermissions;
        }
    }

    public static final class NSFileSystemNumber {
        public static final class NSFileSystemNumber_holder extends com.sun.jna.Structure {
            public NSString value;

            NSFileSystemNumber_holder(com.sun.jna.Pointer pointer) {
                super();
                useMemory(pointer, 0);
                read();
            }
        }

        private static NSFileSystemNumber_holder NSFileSystemNumber;

        public static synchronized NSFileSystemNumber_holder get() {
            if(NSFileSystemNumber == null) {
                NSFileSystemNumber = new NSFileSystemNumber_holder(((com.sun.jna.NativeLibrary) CLASS).getGlobalVariableAddress("NSFileSystemNumber"));
            }
            return NSFileSystemNumber;
        }
    }

    public static final class NSFileSystemFileNumber {
        public static final class NSFileSystemFileNumber_holder extends com.sun.jna.Structure {
            public NSString value;

            NSFileSystemFileNumber_holder(com.sun.jna.Pointer pointer) {
                super();
                useMemory(pointer, 0);
                read();
            }
        }

        private static NSFileSystemFileNumber_holder NSFileSystemFileNumber;

        public static synchronized NSFileSystemFileNumber_holder get() {
            if(NSFileSystemFileNumber == null) {
                NSFileSystemFileNumber = new NSFileSystemFileNumber_holder(((com.sun.jna.NativeLibrary) CLASS).getGlobalVariableAddress("NSFileSystemFileNumber"));
            }
            return NSFileSystemFileNumber;
        }
    }

    public static final class NSFileExtensionHidden {
        public static final class NSFileExtensionHidden_holder extends com.sun.jna.Structure {
            public NSString value;

            NSFileExtensionHidden_holder(com.sun.jna.Pointer pointer) {
                super();
                useMemory(pointer, 0);
                read();
            }
        }

        private static NSFileExtensionHidden_holder NSFileExtensionHidden;

        public static synchronized NSFileExtensionHidden_holder get() {
            if(NSFileExtensionHidden == null) {
                NSFileExtensionHidden = new NSFileExtensionHidden_holder(((com.sun.jna.NativeLibrary) CLASS).getGlobalVariableAddress("NSFileExtensionHidden"));
            }
            return NSFileExtensionHidden;
        }
    }

    public static final class NSFileHFSCreatorCode {
        public static final class NSFileHFSCreatorCode_holder extends com.sun.jna.Structure {
            public NSString value;

            NSFileHFSCreatorCode_holder(com.sun.jna.Pointer pointer) {
                super();
                useMemory(pointer, 0);
                read();
            }
        }

        private static NSFileHFSCreatorCode_holder NSFileHFSCreatorCode;

        public static synchronized NSFileHFSCreatorCode_holder get() {
            if(NSFileHFSCreatorCode == null) {
                NSFileHFSCreatorCode = new NSFileHFSCreatorCode_holder(((com.sun.jna.NativeLibrary) CLASS).getGlobalVariableAddress("NSFileHFSCreatorCode"));
            }
            return NSFileHFSCreatorCode;
        }
    }

    public static final class NSFileHFSTypeCode {
        public static final class NSFileHFSTypeCode_holder extends com.sun.jna.Structure {
            public NSString value;

            NSFileHFSTypeCode_holder(com.sun.jna.Pointer pointer) {
                super();
                useMemory(pointer, 0);
                read();
            }
        }

        private static NSFileHFSTypeCode_holder NSFileHFSTypeCode;

        public static synchronized NSFileHFSTypeCode_holder get() {
            if(NSFileHFSTypeCode == null) {
                NSFileHFSTypeCode = new NSFileHFSTypeCode_holder(((com.sun.jna.NativeLibrary) CLASS).getGlobalVariableAddress("NSFileHFSTypeCode"));
            }
            return NSFileHFSTypeCode;
        }
    }

    public static final class NSFileImmutable {
        public static final class NSFileImmutable_holder extends com.sun.jna.Structure {
            public NSString value;

            NSFileImmutable_holder(com.sun.jna.Pointer pointer) {
                super();
                useMemory(pointer, 0);
                read();
            }
        }

        private static NSFileImmutable_holder NSFileImmutable;

        public static synchronized NSFileImmutable_holder get() {
            if(NSFileImmutable == null) {
                NSFileImmutable = new NSFileImmutable_holder(((com.sun.jna.NativeLibrary) CLASS).getGlobalVariableAddress("NSFileImmutable"));
            }
            return NSFileImmutable;
        }
    }

    public static final class NSFileAppendOnly {
        public static final class NSFileAppendOnly_holder extends com.sun.jna.Structure {
            public NSString value;

            NSFileAppendOnly_holder(com.sun.jna.Pointer pointer) {
                super();
                useMemory(pointer, 0);
                read();
            }
        }

        private static NSFileAppendOnly_holder NSFileAppendOnly;

        public static synchronized NSFileAppendOnly_holder get() {
            if(NSFileAppendOnly == null) {
                NSFileAppendOnly = new NSFileAppendOnly_holder(((com.sun.jna.NativeLibrary) CLASS).getGlobalVariableAddress("NSFileAppendOnly"));
            }
            return NSFileAppendOnly;
        }
    }

    public static final class NSFileCreationDate {
        public static final class NSFileCreationDate_holder extends com.sun.jna.Structure {
            public NSString value;

            NSFileCreationDate_holder(com.sun.jna.Pointer pointer) {
                super();
                useMemory(pointer, 0);
                read();
            }
        }

        private static NSFileCreationDate_holder NSFileCreationDate;

        public static synchronized NSFileCreationDate_holder get() {
            if(NSFileCreationDate == null) {
                NSFileCreationDate = new NSFileCreationDate_holder(((com.sun.jna.NativeLibrary) CLASS).getGlobalVariableAddress("NSFileCreationDate"));
            }
            return NSFileCreationDate;
        }
    }

    public static final class NSFileOwnerAccountID {
        public static final class NSFileOwnerAccountID_holder extends com.sun.jna.Structure {
            public NSString value;

            NSFileOwnerAccountID_holder(com.sun.jna.Pointer pointer) {
                super();
                useMemory(pointer, 0);
                read();
            }
        }

        private static NSFileOwnerAccountID_holder NSFileOwnerAccountID;

        public static synchronized NSFileOwnerAccountID_holder get() {
            if(NSFileOwnerAccountID == null) {
                NSFileOwnerAccountID = new NSFileOwnerAccountID_holder(((com.sun.jna.NativeLibrary) CLASS).getGlobalVariableAddress("NSFileOwnerAccountID"));
            }
            return NSFileOwnerAccountID;
        }
    }

    public static final class NSFileGroupOwnerAccountID {
        public static final class NSFileGroupOwnerAccountID_holder extends com.sun.jna.Structure {
            public NSString value;

            NSFileGroupOwnerAccountID_holder(com.sun.jna.Pointer pointer) {
                super();
                useMemory(pointer, 0);
                read();
            }
        }

        private static NSFileGroupOwnerAccountID_holder NSFileGroupOwnerAccountID;

        public static synchronized NSFileGroupOwnerAccountID_holder get() {
            if(NSFileGroupOwnerAccountID == null) {
                NSFileGroupOwnerAccountID = new NSFileGroupOwnerAccountID_holder(((com.sun.jna.NativeLibrary) CLASS).getGlobalVariableAddress("NSFileGroupOwnerAccountID"));
            }
            return NSFileGroupOwnerAccountID;
        }
    }

    public static final class NSFileBusy {
        public static final class NSFileBusy_holder extends com.sun.jna.Structure {
            public NSString value;

            NSFileBusy_holder(com.sun.jna.Pointer pointer) {
                super();
                useMemory(pointer, 0);
                read();
            }
        }

        private static NSFileBusy_holder NSFileBusy;

        public static synchronized NSFileBusy_holder get() {
            if(NSFileBusy == null) {
                NSFileBusy = new NSFileBusy_holder(((com.sun.jna.NativeLibrary) CLASS).getGlobalVariableAddress("NSFileBusy"));
            }
            return NSFileBusy;
        }
    }

    public static final class NSFileSystemSize {
        public static final class NSFileSystemSize_holder extends com.sun.jna.Structure {
            public NSString value;

            NSFileSystemSize_holder(com.sun.jna.Pointer pointer) {
                super();
                useMemory(pointer, 0);
                read();
            }
        }

        private static NSFileSystemSize_holder NSFileSystemSize;

        public static synchronized NSFileSystemSize_holder get() {
            if(NSFileSystemSize == null) {
                NSFileSystemSize = new NSFileSystemSize_holder(((com.sun.jna.NativeLibrary) CLASS).getGlobalVariableAddress("NSFileSystemSize"));
            }
            return NSFileSystemSize;
        }
    }

    public static final class NSFileSystemFreeSize {
        public static final class NSFileSystemFreeSize_holder extends com.sun.jna.Structure {
            public NSString value;

            NSFileSystemFreeSize_holder(com.sun.jna.Pointer pointer) {
                super();
                useMemory(pointer, 0);
                read();
            }
        }

        private static NSFileSystemFreeSize_holder NSFileSystemFreeSize;

        public static synchronized NSFileSystemFreeSize_holder get() {
            if(NSFileSystemFreeSize == null) {
                NSFileSystemFreeSize = new NSFileSystemFreeSize_holder(((com.sun.jna.NativeLibrary) CLASS).getGlobalVariableAddress("NSFileSystemFreeSize"));
            }
            return NSFileSystemFreeSize;
        }
    }

    public static final class NSFileSystemNodes {
        public static final class NSFileSystemNodes_holder extends com.sun.jna.Structure {
            public NSString value;

            NSFileSystemNodes_holder(com.sun.jna.Pointer pointer) {
                super();
                useMemory(pointer, 0);
                read();
            }
        }

        private static NSFileSystemNodes_holder NSFileSystemNodes;

        public static synchronized NSFileSystemNodes_holder get() {
            if(NSFileSystemNodes == null) {
                NSFileSystemNodes = new NSFileSystemNodes_holder(((com.sun.jna.NativeLibrary) CLASS).getGlobalVariableAddress("NSFileSystemNodes"));
            }
            return NSFileSystemNodes;
        }
    }

    public static final class NSFileSystemFreeNodes {
        public static final class NSFileSystemFreeNodes_holder extends com.sun.jna.Structure {
            public NSString value;

            NSFileSystemFreeNodes_holder(com.sun.jna.Pointer pointer) {
                super();
                useMemory(pointer, 0);
                read();
            }
        }

        private static NSFileSystemFreeNodes_holder NSFileSystemFreeNodes;

        public static synchronized NSFileSystemFreeNodes_holder get() {
            if(NSFileSystemFreeNodes == null) {
                NSFileSystemFreeNodes = new NSFileSystemFreeNodes_holder(((com.sun.jna.NativeLibrary) CLASS).getGlobalVariableAddress("NSFileSystemFreeNodes"));
            }
            return NSFileSystemFreeNodes;
        }
    }

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
     *  * CLASSs of NSFileManager may now have delegates. Each CLASS has one delegate, and the delegate is not retained. In versions of Mac OS X prior to 10.5, the behavior of calling [[NSFileManager alloc] init] was undefined. In Mac OS X 10.5 "Leopard" and later, calling [[NSFileManager alloc] init] returns a new CLASS of an NSFileManager.<br>
     *  * Original signature : <code>void setDelegate(null)</code><br>
     *  * /<br>
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
    public abstract com.sun.jna.Pointer contentsOfDirectoryAtPath_error(String path, com.sun.jna.ptr.PointerByReference error);

    /**
     * subpathsOfDirectoryAtPath:error: returns an NSArray of NSStrings represeting the filenames of the items in the specified directory and all its subdirectories recursively. If this method returns 'nil', an NSError will be returned by reference in the 'error' parameter. If the directory contains no items, this method will return the empty array.<br>
     * This method replaces subpathsAtPath:<br>
     * Original signature : <code>NSArray* subpathsOfDirectoryAtPath(NSString*, NSError**)</code><br>
     * <i>native declaration : :47</i>
     */
    public abstract com.sun.jna.Pointer subpathsOfDirectoryAtPath_error(String path, com.sun.jna.ptr.PointerByReference error);

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
    public abstract boolean createSymbolicLinkAtPath_withDestinationPath_error(String path, com.sun.jna.Pointer destPath, com.sun.jna.ptr.PointerByReference error);

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
    public abstract boolean copyItemAtPath_toPath_error(com.sun.jna.Pointer srcPath, com.sun.jna.Pointer dstPath, com.sun.jna.ptr.PointerByReference error);

    /**
     * Original signature : <code>BOOL moveItemAtPath(NSString*, NSString*, NSError**)</code><br>
     * <i>native declaration : :76</i>
     */
    public abstract boolean moveItemAtPath_toPath_error(com.sun.jna.Pointer srcPath, com.sun.jna.Pointer dstPath, com.sun.jna.ptr.PointerByReference error);

    /**
     * Original signature : <code>BOOL linkItemAtPath(NSString*, NSString*, NSError**)</code><br>
     * <i>native declaration : :77</i>
     */
    public abstract boolean linkItemAtPath_toPath_error(com.sun.jna.Pointer srcPath, com.sun.jna.Pointer dstPath, com.sun.jna.ptr.PointerByReference error);

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
    public abstract boolean createSymbolicLinkAtPath_pathContent(String path, com.sun.jna.Pointer otherpath);

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
    public abstract boolean createFileAtPath_contents_attributes(String path, com.sun.jna.Pointer data, NSDictionary attr);

    /**
     * fileSystemRepresentationWithPath: returns an array of characters suitable for passing to lower-level POSIX style APIs. The string is provided in the representation most appropriate for the filesystem in question.<br>
     * Original signature : <code>const char* fileSystemRepresentationWithPath(NSString*)</code><br>
     * <i>native declaration : :139</i>
     */
    public abstract com.sun.jna.ptr.ByteByReference fileSystemRepresentationWithPath(String path);

    /**
     * stringWithFileSystemRepresentation:length: returns an NSString created from an array of bytes that are in the filesystem representation.<br>
     * Original signature : <code>NSString* stringWithFileSystemRepresentation(const char*, NSUInteger)</code><br>
     * <i>native declaration : :143</i><br>
     *
     * @deprecated use the safer method {@link #stringWithFileSystemRepresentation_length(java.lang.String, int)} instead
     */
    @java.lang.Deprecated
    public abstract com.sun.jna.Pointer stringWithFileSystemRepresentation_length(com.sun.jna.ptr.ByteByReference str, int len);

    /**
     * stringWithFileSystemRepresentation:length: returns an NSString created from an array of bytes that are in the filesystem representation.<br>
     * Original signature : <code>NSString* stringWithFileSystemRepresentation(const char*, NSUInteger)</code><br>
     * <i>native declaration : :143</i>
     */
    public abstract com.sun.jna.Pointer stringWithFileSystemRepresentation_length(java.lang.String str, int len);

    public interface NSDirectoryEnumerator extends NSObject {
        _Class CLASS = org.rococoa.Rococoa.createClass("NSDirectoryEnumerator", _Class.class);

        public interface _Class extends org.rococoa.NSClass {
            NSDirectoryEnumerator alloc();
        }

        /**
         * Original signature : <code>NSDictionary* fileAttributes()</code><br>
         * <i>native declaration : :211</i>
         */
        NSDictionary fileAttributes();

        /**
         * Original signature : <code>NSDictionary* directoryAttributes()</code><br>
         * <i>native declaration : :212</i>
         */
        NSDictionary directoryAttributes();

        /**
         * Original signature : <code>void skipDescendents()</code><br>
         * <i>native declaration : :213</i>
         */
        void skipDescendents();
    }
}
