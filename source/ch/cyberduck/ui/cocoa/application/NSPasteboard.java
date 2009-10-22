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
import ch.cyberduck.ui.cocoa.foundation.NSData;
import ch.cyberduck.ui.cocoa.foundation.NSObject;

import org.rococoa.ObjCClass;
import org.rococoa.cocoa.foundation.NSInteger;

/// <i>native declaration : :52</i>
public abstract class NSPasteboard extends NSObject {
    private static final _Class CLASS = org.rococoa.Rococoa.createClass("NSPasteboard", _Class.class);

    public static NSPasteboard generalPasteboard() {
        return CLASS.generalPasteboard();
    }

    public static NSPasteboard pasteboardWithName(String name) {
        return CLASS.pasteboardWithName(name);
    }

    public interface _Class extends ObjCClass {
        /**
         * Original signature : <code>NSPasteboard* generalPasteboard()</code><br>
         * <i>native declaration : :65</i>
         */
        NSPasteboard generalPasteboard();

        /**
         * Original signature : <code>NSPasteboard* pasteboardWithName(NSString*)</code><br>
         * <i>native declaration : :66</i>
         */
        NSPasteboard pasteboardWithName(String name);

        /**
         * Original signature : <code>NSPasteboard* pasteboardWithUniqueName()</code><br>
         * <i>native declaration : :67</i>
         */
        NSPasteboard pasteboardWithUniqueName();

        /**
         * Original signature : <code>NSArray* typesFilterableTo(NSString*)</code><br>
         * <i>native declaration : :69</i>
         */
        NSArray typesFilterableTo(String type);

        /**
         * Original signature : <code>NSPasteboard* pasteboardByFilteringFile(NSString*)</code><br>
         * <i>native declaration : :71</i>
         */
        NSPasteboard pasteboardByFilteringFile(String filename);

        /**
         * Original signature : <code>NSPasteboard* pasteboardByFilteringData(NSData*, NSString*)</code><br>
         * <i>native declaration : :72</i>
         */
        NSPasteboard pasteboardByFilteringData_ofType(NSData data, String type);

        /**
         * Original signature : <code>NSPasteboard* pasteboardByFilteringTypesInPasteboard(NSPasteboard*)</code><br>
         * <i>native declaration : :73</i>
         */
        NSPasteboard pasteboardByFilteringTypesInPasteboard(NSPasteboard pboard);
    }

    /**
     * Original signature : <code>NSString* name()</code><br>
     * <i>native declaration : :75</i>
     */
    public abstract String name();

    /**
     * Original signature : <code>void releaseGlobally()</code><br>
     * <i>native declaration : :77</i>
     */
    public abstract void releaseGlobally();

    public int declareTypes(NSArray newTypes, org.rococoa.ID newOwner) {
        return this.declareTypes_owner(newTypes, newOwner);
    }

    /**
     * Original signature : <code>NSInteger declareTypes(NSArray*, id)</code><br>
     * <i>native declaration : :79</i>
     */
    public abstract int declareTypes_owner(NSArray newTypes, org.rococoa.ID newOwner);

    public NSInteger addTypes(NSArray newTypes, org.rococoa.ID newOwner) {
        return this.addTypes_owner(newTypes, newOwner);
    }

    /**
     * Original signature : <code>NSInteger addTypes(NSArray*, id)</code><br>
     * <i>native declaration : :80</i>
     */
    public abstract NSInteger addTypes_owner(NSArray newTypes, org.rococoa.ID newOwner);

    /**
     * Original signature : <code>NSInteger changeCount()</code><br>
     * <i>native declaration : :81</i>
     */
    public abstract NSInteger changeCount();

    /**
     * Original signature : <code>NSArray* types()</code><br>
     * <i>native declaration : :83</i>
     */
    public abstract NSArray types();

    /**
     * Original signature : <code>NSString* availableTypeFromArray(NSArray*)</code><br>
     * <i>native declaration : :84</i>
     */
    public abstract String availableTypeFromArray(NSArray types);

    /**
     * Original signature : <code>BOOL setData(NSData*, NSString*)</code><br>
     * <i>native declaration : :86</i>
     */
    public abstract boolean setData_forType(NSData data, String dataType);

    /**
     * Original signature : <code>NSData* dataForType(NSString*)</code><br>
     * <i>native declaration : :87</i>
     */
    public abstract NSData dataForType(String dataType);

    /**
     * Original signature : <code>BOOL setPropertyList(id, NSString*)</code><br>
     * <i>native declaration : :89</i>
     */
    public abstract boolean setPropertyList_forType(NSObject plist, String dataType);

    public boolean setPropertyListForType(NSObject plist, String dataType) {
        return this.setPropertyList_forType(plist, dataType);
    }

    /**
     * Original signature : <code>id propertyListForType(NSString*)</code><br>
     * <i>native declaration : :90</i>
     */
    public abstract NSObject propertyListForType(String dataType);

    public boolean setStringForType(String string, String dataType) {
        return this.setString_forType(string, dataType);
    }

    /**
     * Original signature : <code>BOOL setString(NSString*, NSString*)</code><br>
     * <i>native declaration : :92</i>
     */
    public abstract boolean setString_forType(String string, String dataType);

    /**
     * Original signature : <code>NSString* stringForType(NSString*)</code><br>
     * <i>native declaration : :93</i>
     */
    public abstract String stringForType(String dataType);

    /**
     * Original signature : <code>BOOL writeFileContents(NSString*)</code><br>
     * <i>from NSFileContents native declaration : :98</i>
     */
    public abstract boolean writeFileContents(String filename);

    /**
     * Original signature : <code>NSString* readFileContentsType(NSString*, NSString*)</code><br>
     * <i>from NSFileContents native declaration : :99</i>
     */
    public abstract String readFileContentsType_toFile(String type, String filename);

    /**
     * Original signature : <code>BOOL writeFileWrapper(NSFileWrapper*)</code><br>
     * <i>from NSFileContents native declaration : :101</i>
     */
    public abstract boolean writeFileWrapper(com.sun.jna.Pointer wrapper);

    /**
     * Original signature : <code>NSFileWrapper* readFileWrapper()</code><br>
     * <i>from NSFileContents native declaration : :102</i>
     */
    public abstract com.sun.jna.Pointer readFileWrapper();

    public static final String ColorPboardType = "NSColor pasteboard type";
    public static final String FileContentsPboardType = "NXFileContentsPboardType";
    public static final String FilenamesPboardType = "NSFilenamesPboardType";
    public static final String FontPboardType = "NeXT font pasteboard type";
    public static final String PostScriptPboardType = "NeXT Encapsulated PostScript v1.2 pasteboard type";
    public static final String RulerPboardType = "NeXT ruler pasteboard type";
    public static final String RTFPboardType = "NeXT Rich Text Format v1.0 pasteboard type";
    public static final String RTFDPboardType = "NeXT RTFD pasteboard type";
    public static final String PICTPboardType = "Apple PICT pasteboard type";
    public static final String StringPboardType = "NSStringPboardType";
    public static final String TabularTextPboardType = "NeXT tabular text pasteboard type";
    public static final String TIFFPboardType = "NeXT TIFF v4.0 pasteboard type";
    public static final String URLPboardType = "Apple URL pasteboard type";
    public static final String PDFPboardType = "Apple PDF pasteboard type";
    public static final String HTMLPboardType = "Apple HTML pasteboard type";
    public static final String VCardPboardType = "Apple VCard pasteboard type";
    public static final String FilesPromisePboardType = "Apple files promise pasteboard type";
    public static final String GeneralPboard = "Apple CFPasteboard general";
    public static final String FontPboard = "Apple CFPasteboard font";
    public static final String RulerPboard = "Apple CFPasteboard ruler";
    public static final String FindPboard = "Apple CFPasteboard find";
    public static final String DragPboard = "Apple CFPasteboard drag";
}
