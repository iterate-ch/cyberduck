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

/// <i>native declaration : /Users/dkocher/Desktop/null:12</i>
public abstract class NSBundle implements NSObject {
    private static final _Class CLASS = org.rococoa.Rococoa.createClass("NSBundle", _Class.class);

    /**
     * @param key
     * @return
     */
    public static String localizedString(String key) {
        return NSBundle.mainBundle().localizedStringForKey_value_table(key, key, "Localizable");
    }

    /**
     * @param key
     * @param tableName
     * @return
     */
    public static String localizedString(String key, String tableName) {
        return NSBundle.mainBundle().localizedStringForKey_value_table(key, key, tableName);
    }

    /**
     * @param path
     * @return
     */
    public static NSBundle bundleWithPath(String path) {
        return CLASS.bundleWithPath(path);
    }


    public interface _Class extends org.rococoa.NSClass {
        /**
         * Original signature : <code>NSBundle* mainBundle)</code><br>
         * <i>native declaration : /Users/dkocher/Desktop/null:24</i>
         */
        NSBundle mainBundle();

        /**
         * Original signature : <code>NSBundle* bundleWithPath(String*)</code><br>
         * <i>native declaration : /Users/dkocher/Desktop/null:25</i>
         */
        NSBundle bundleWithPath(String path1);

        /**
         * Original signature : <code>NSBundle* bundleForClass(Class)</code><br>
         * <i>native declaration : /Users/dkocher/Desktop/null:28</i>
         */
        NSBundle bundleForClass(org.rococoa.NSClass aClass1);

        /**
         * Original signature : <code>NSBundle* bundleWithIdentifier(String*)</code><br>
         * <i>native declaration : /Users/dkocher/Desktop/null:29</i>
         */
        NSBundle bundleWithIdentifier(String identifier1);

        /**
         * Original signature : <code>NSArray* allBundles)</code><br>
         * <i>native declaration : /Users/dkocher/Desktop/null:31</i>
         */
        NSArray allBundles();

        /**
         * Original signature : <code>NSArray* allFrameworks)</code><br>
         * <i>native declaration : /Users/dkocher/Desktop/null:32</i>
         */
        NSArray allFrameworks();

        /**
         * In the following methods, bundlePath is an absolute path to a bundle, and may not be nil; subpath is a relative path to a subdirectory inside the relevant global or localized resource directory, and should be nil if the resource file in question is not in a subdirectory.<br>
         * Original signature : <code>String* pathForResource(String*, String*, String*)</code><br>
         * <i>native declaration : /Users/dkocher/Desktop/null:62</i>
         */
        String pathForResource_ofType_inDirectory(String name1, String ext2, String bundlePath3);

        /**
         * Original signature : <code>NSArray* pathsForResourcesOfType(String*, String*)</code><br>
         * <i>native declaration : /Users/dkocher/Desktop/null:67</i>
         */
        NSArray pathsForResourcesOfType_inDirectory(String ext1, String bundlePath2);

        /**
         * Original signature : <code>NSArray* preferredLocalizationsFromArray(NSArray*)</code><br>
         * <i>native declaration : /Users/dkocher/Desktop/null:85</i>
         */
        NSArray preferredLocalizationsFromArray(NSArray localizationsArray1);

        /**
         * Original signature : <code>NSArray* preferredLocalizationsFromArray(NSArray*, NSArray*)</code><br>
         * <i>native declaration : /Users/dkocher/Desktop/null:87</i>
         */
        NSArray preferredLocalizationsFromArray_forPreferences(NSArray localizationsArray1, NSArray preferencesArray2);

        boolean loadNibFile_externalNameTable_withZone(String fileName, NSDictionary context, NSObject zone);

        boolean loadNibNamed_owner(String nibName, NSObject owner);
    }

    public static NSBundle mainBundle() {
        return CLASS.mainBundle();
    }

    public static NSArray allBundles() {
        return CLASS.allBundles();
    }

    public static boolean loadNibNamed(String nibName, NSObject owner) {
        return CLASS.loadNibNamed_owner(nibName, owner);
    }


    /**
     * Original signature : <code>id initWithPath(String*)</code><br>
     * <i>native declaration : /Users/dkocher/Desktop/null:26</i>
     */
    public abstract NSBundle initWithPath(String path1);

    /**
     * Original signature : <code>BOOL load)</code><br>
     * <i>native declaration : /Users/dkocher/Desktop/null:34</i>
     */
    public abstract boolean load();

    /**
     * Original signature : <code>BOOL isLoaded)</code><br>
     * <i>native declaration : /Users/dkocher/Desktop/null:36</i>
     */
    public abstract boolean isLoaded();

    /**
     * Original signature : <code>BOOL unload)</code><br>
     * <i>native declaration : /Users/dkocher/Desktop/null:37</i>
     */
    public abstract boolean unload();

    /**
     * Original signature : <code>BOOL preflightAndReturnError(NSError**)</code><br>
     * <i>native declaration : /Users/dkocher/Desktop/null:41</i>
     */
    public abstract boolean preflightAndReturnError(com.sun.jna.ptr.PointerByReference error1);

    /**
     * Original signature : <code>BOOL loadAndReturnError(NSError**)</code><br>
     * <i>native declaration : /Users/dkocher/Desktop/null:42</i>
     */
    public abstract boolean loadAndReturnError(com.sun.jna.ptr.PointerByReference error1);

    /**
     * Original signature : <code>String* bundlePath)</code><br>
     * <i>native declaration : /Users/dkocher/Desktop/null:45</i>
     */
    public abstract String bundlePath();

    /**
     * Original signature : <code>String* resourcePath)</code><br>
     * <i>native declaration : /Users/dkocher/Desktop/null:46</i>
     */
    public abstract String resourcePath();

    /**
     * Original signature : <code>String* executablePath)</code><br>
     * <i>native declaration : /Users/dkocher/Desktop/null:47</i>
     */
    public abstract String executablePath();

    /**
     * Original signature : <code>String* pathForAuxiliaryExecutable(String*)</code><br>
     * <i>native declaration : /Users/dkocher/Desktop/null:48</i>
     */
    public abstract String pathForAuxiliaryExecutable(String executableName1);

    /**
     * Original signature : <code>String* privateFrameworksPath)</code><br>
     * <i>native declaration : /Users/dkocher/Desktop/null:50</i>
     */
    public abstract String privateFrameworksPath();

    /**
     * Original signature : <code>String* sharedFrameworksPath)</code><br>
     * <i>native declaration : /Users/dkocher/Desktop/null:51</i>
     */
    public abstract String sharedFrameworksPath();

    /**
     * Original signature : <code>String* sharedSupportPath)</code><br>
     * <i>native declaration : /Users/dkocher/Desktop/null:52</i>
     */
    public abstract String sharedSupportPath();

    /**
     * Original signature : <code>String* builtInPlugInsPath)</code><br>
     * <i>native declaration : /Users/dkocher/Desktop/null:53</i>
     */
    public abstract String builtInPlugInsPath();

    /**
     * Original signature : <code>String* bundleIdentifier)</code><br>
     * <i>native declaration : /Users/dkocher/Desktop/null:55</i>
     */
    public abstract String bundleIdentifier();

    /**
     * Original signature : <code>Class classNamed(String*)</code><br>
     * <i>native declaration : /Users/dkocher/Desktop/null:57</i>
     */
    public abstract org.rococoa.NSClass classNamed(String className1);

    /**
     * Original signature : <code>Class principalClass)</code><br>
     * <i>native declaration : /Users/dkocher/Desktop/null:59</i>
     */
    public abstract org.rococoa.NSClass principalClass();

    /**
     * Original signature : <code>String* pathForResource(String*, String*)</code><br>
     * <i>native declaration : /Users/dkocher/Desktop/null:63</i>
     */
    public abstract String pathForResource_ofType(String name1, String ext2);

    /**
     * Original signature : <code>String* pathForResource(String*, String*, String*, String*)</code><br>
     * <i>native declaration : /Users/dkocher/Desktop/null:65</i>
     */
    public abstract String pathForResource_ofType_inDirectory_forLocalization(String name1, String ext2, String subpath3, String localizationName4);

    /**
     * Original signature : <code>NSArray* pathsForResourcesOfType(String*, String*, String*)</code><br>
     * <i>native declaration : /Users/dkocher/Desktop/null:69</i>
     */
    public abstract NSArray pathsForResourcesOfType_inDirectory_forLocalization(String ext1, String subpath2, String localizationName3);

    /**
     * Original signature : <code>String* localizedStringForKey(String*, String*, String*)</code><br>
     * <i>native declaration : /Users/dkocher/Desktop/null:71</i>
     */
    public abstract String localizedStringForKey_value_table(String key1, String value2, String tableName3);

    /**
     * Original signature : <code>NSDictionary* infoDictionary)</code><br>
     * <i>native declaration : /Users/dkocher/Desktop/null:73</i>
     */
    public abstract NSDictionary infoDictionary();

    /**
     * Original signature : <code>NSDictionary* localizedInfoDictionary)</code><br>
     * <i>native declaration : /Users/dkocher/Desktop/null:75</i>
     */
    public abstract NSDictionary localizedInfoDictionary();

    /**
     * Original signature : <code>id objectForInfoDictionaryKey(String*)</code><br>
     * <i>native declaration : /Users/dkocher/Desktop/null:76</i>
     */
    public abstract NSObject objectForInfoDictionaryKey(String key1);

    /**
     * Original signature : <code>NSArray* localizations)</code><br>
     * <i>native declaration : /Users/dkocher/Desktop/null:79</i>
     */
    public abstract NSArray localizations();

    /**
     * Original signature : <code>NSArray* preferredLocalizations)</code><br>
     * <i>native declaration : /Users/dkocher/Desktop/null:80</i>
     */
    public abstract NSArray preferredLocalizations();

    /**
     * Original signature : <code>String* developmentLocalization)</code><br>
     * <i>native declaration : /Users/dkocher/Desktop/null:82</i>
     */
    public abstract String developmentLocalization();
}
