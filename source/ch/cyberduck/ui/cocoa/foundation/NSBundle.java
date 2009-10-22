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

/// <i>native declaration : :12</i>
public abstract class NSBundle extends NSObject {
    private static final _Class CLASS = org.rococoa.Rococoa.createClass("NSBundle", _Class.class);

    /**
     * @param key
     * @param tableName
     * @return
     */
    public static String localizedString(String key, String tableName) {
        return NSBundle.mainBundle().localizedStringForKey_value_table(key, key, tableName);
    }

    private static NSBundle mainBundle = null;

    public static NSBundle mainBundle() {
        if(null == mainBundle) {
            mainBundle = CLASS.mainBundle();
        }
        return mainBundle;
    }

    public static NSArray allBundles() {
        return CLASS.allBundles();
    }

    public static boolean loadNibNamed(String nibName, org.rococoa.ID owner) {
        return CLASS.loadNibNamed_owner(nibName, owner);
    }

    /**
     * This is the primitive that loads the contents of a .nib file.  Context holds key value
     * pairs that can name objects that are referenced by the objects within the nib
     * file (e.g., "NSOwner").  Objects from the nib are allocated in zone.
     *
     * @param fileName
     * @param context
     * @param zone
     * @return
     */
    public static boolean loadNibNamed(String fileName, NSDictionary context, NSZone zone) {
        return CLASS.loadNibFile_externalNameTable_withZone(fileName, context, zone);
    }

    /**
     * @param path
     * @return
     */
    public static NSBundle bundleWithPath(String path) {
        return CLASS.bundleWithPath(path);
    }


    public interface _Class extends ObjCClass {
        /**
         * Original signature : <code>NSBundle* mainBundle)</code><br>
         * <i>native declaration : :24</i>
         */
        NSBundle mainBundle();

        /**
         * Original signature : <code>NSBundle* bundleWithPath(String*)</code><br>
         * <i>native declaration : :25</i>
         */
        NSBundle bundleWithPath(String path1);

        /**
         * Original signature : <code>NSBundle* bundleWithIdentifier(String*)</code><br>
         * <i>native declaration : :29</i>
         */
        NSBundle bundleWithIdentifier(String identifier1);

        /**
         * Original signature : <code>NSArray* allBundles)</code><br>
         * <i>native declaration : :31</i>
         */
        NSArray allBundles();

        /**
         * Original signature : <code>NSArray* allFrameworks)</code><br>
         * <i>native declaration : :32</i>
         */
        NSArray allFrameworks();

        /**
         * In the following methods, bundlePath is an absolute path to a bundle, and may not be nil; subpath is a relative path to a subdirectory inside the relevant global or localized resource directory, and should be nil if the resource file in question is not in a subdirectory.<br>
         * Original signature : <code>String* pathForResource(String*, String*, String*)</code><br>
         * <i>native declaration : :62</i>
         */
        String pathForResource_ofType_inDirectory(String name1, String ext2, String bundlePath3);

        /**
         * Original signature : <code>NSArray* pathsForResourcesOfType(String*, String*)</code><br>
         * <i>native declaration : :67</i>
         */
        NSArray pathsForResourcesOfType_inDirectory(String ext1, String bundlePath2);

        /**
         * Original signature : <code>NSArray* preferredLocalizationsFromArray(NSArray*)</code><br>
         * <i>native declaration : :85</i>
         */
        NSArray preferredLocalizationsFromArray(NSArray localizationsArray1);

        /**
         * Original signature : <code>NSArray* preferredLocalizationsFromArray(NSArray*, NSArray*)</code><br>
         * <i>native declaration : :87</i>
         */
        NSArray preferredLocalizationsFromArray_forPreferences(NSArray localizationsArray1, NSArray preferencesArray2);

        boolean loadNibFile_externalNameTable_withZone(String fileName, NSDictionary context, NSZone zone);

        boolean loadNibNamed_owner(String nibName, org.rococoa.ID owner);
    }

    /**
     * Original signature : <code>id initWithPath(String*)</code><br>
     * <i>native declaration : :26</i>
     */
    public abstract NSBundle initWithPath(String path1);

    /**
     * Original signature : <code>BOOL load)</code><br>
     * <i>native declaration : :34</i>
     */
    public abstract boolean load();

    /**
     * Original signature : <code>BOOL isLoaded)</code><br>
     * <i>native declaration : :36</i>
     */
    public abstract boolean isLoaded();

    /**
     * Original signature : <code>BOOL unload)</code><br>
     * <i>native declaration : :37</i>
     */
    public abstract boolean unload();

    /**
     * Original signature : <code>BOOL preflightAndReturnError(NSError**)</code><br>
     * <i>native declaration : :41</i>
     */
    public abstract boolean preflightAndReturnError(com.sun.jna.ptr.PointerByReference error1);

    /**
     * Original signature : <code>BOOL loadAndReturnError(NSError**)</code><br>
     * <i>native declaration : :42</i>
     */
    public abstract boolean loadAndReturnError(com.sun.jna.ptr.PointerByReference error1);

    /**
     * Original signature : <code>String* bundlePath)</code><br>
     * <i>native declaration : :45</i>
     */
    public abstract String bundlePath();

    /**
     * Original signature : <code>String* resourcePath)</code><br>
     * <i>native declaration : :46</i>
     */
    public abstract String resourcePath();

    /**
     * Original signature : <code>String* executablePath)</code><br>
     * <i>native declaration : :47</i>
     */
    public abstract String executablePath();

    /**
     * Original signature : <code>String* pathForAuxiliaryExecutable(String*)</code><br>
     * <i>native declaration : :48</i>
     */
    public abstract String pathForAuxiliaryExecutable(String executableName1);

    /**
     * Original signature : <code>String* privateFrameworksPath)</code><br>
     * <i>native declaration : :50</i>
     */
    public abstract String privateFrameworksPath();

    /**
     * Original signature : <code>String* sharedFrameworksPath)</code><br>
     * <i>native declaration : :51</i>
     */
    public abstract String sharedFrameworksPath();

    /**
     * Original signature : <code>String* sharedSupportPath)</code><br>
     * <i>native declaration : :52</i>
     */
    public abstract String sharedSupportPath();

    /**
     * Original signature : <code>String* builtInPlugInsPath)</code><br>
     * <i>native declaration : :53</i>
     */
    public abstract String builtInPlugInsPath();

    /**
     * Original signature : <code>String* bundleIdentifier)</code><br>
     * <i>native declaration : :55</i>
     */
    public abstract String bundleIdentifier();

    /**
     * Original signature : <code>String* pathForResource(String*, String*)</code><br>
     * <i>native declaration : :63</i>
     */
    public abstract String pathForResource_ofType(String name1, String ext2);

    /**
     * Original signature : <code>String* pathForResource(String*, String*, String*, String*)</code><br>
     * <i>native declaration : :65</i>
     */
    public abstract String pathForResource_ofType_inDirectory_forLocalization(String name1, String ext2, String subpath3, String localizationName4);

    /**
     * Original signature : <code>NSArray* pathsForResourcesOfType(String*, String*, String*)</code><br>
     * <i>native declaration : :69</i>
     */
    public abstract NSArray pathsForResourcesOfType_inDirectory_forLocalization(String ext1, String subpath2, String localizationName3);

    /**
     * Original signature : <code>String* localizedStringForKey(String*, String*, String*)</code><br>
     * <i>native declaration : :71</i>
     */
    public abstract String localizedStringForKey_value_table(String key1, String value2, String tableName3);

    /**
     * Original signature : <code>NSDictionary* infoDictionary)</code><br>
     * <i>native declaration : :73</i>
     */
    public abstract NSDictionary infoDictionary();

    /**
     * Original signature : <code>NSDictionary* localizedInfoDictionary)</code><br>
     * <i>native declaration : :75</i>
     */
    public abstract NSDictionary localizedInfoDictionary();

    /**
     * Original signature : <code>id objectForInfoDictionaryKey(String*)</code><br>
     * <i>native declaration : :76</i>
     */
    public abstract NSObject objectForInfoDictionaryKey(String key1);

    /**
     * Original signature : <code>NSArray* localizations)</code><br>
     * <i>native declaration : :79</i>
     */
    public abstract NSArray localizations();

    /**
     * Original signature : <code>NSArray* preferredLocalizations)</code><br>
     * <i>native declaration : :80</i>
     */
    public abstract NSArray preferredLocalizations();

    /**
     * Original signature : <code>String* developmentLocalization)</code><br>
     * <i>native declaration : :82</i>
     */
    public abstract String developmentLocalization();
}
