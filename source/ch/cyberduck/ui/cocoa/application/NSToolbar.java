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
import ch.cyberduck.ui.cocoa.foundation.NSDictionary;
import ch.cyberduck.ui.cocoa.foundation.NSObject;

import org.rococoa.ID;
import org.rococoa.ObjCClass;
import org.rococoa.cocoa.foundation.NSInteger;
import org.rococoa.cocoa.foundation.NSUInteger;

/// <i>native declaration : :17</i>
public abstract class NSToolbar extends NSObject {
    private static final _Class CLASS = org.rococoa.Rococoa.createClass("NSToolbar", _Class.class);

    /// <i>native declaration : :12</i>
    public static final NSUInteger NSToolbarDisplayModeDefault = new NSUInteger(0);
    /// <i>native declaration : :12</i>
    public static final NSUInteger NSToolbarDisplayModeIconAndLabel = new NSUInteger(1);
    /// <i>native declaration : :12</i>
    public static final NSUInteger NSToolbarDisplayModeIconOnly = new NSUInteger(2);
    /// <i>native declaration : :12</i>
    public static final NSUInteger NSToolbarDisplayModeLabelOnly = new NSUInteger(3);
    /// <i>native declaration : :15</i>
    public static final NSUInteger NSToolbarSizeModeDefault = new NSUInteger(0);
    /// <i>native declaration : :15</i>
    public static final NSUInteger NSToolbarSizeModeRegular = new NSUInteger(1);
    /// <i>native declaration : :15</i>
    public static final NSUInteger NSToolbarSizeModeSmall = new NSUInteger(2);

    public static NSToolbar toolbarWithIdentifier(String identifier) {
        return CLASS.alloc().initWithIdentifier(identifier);
    }

    public interface _Class extends ObjCClass {
        NSToolbar alloc();
    }

    public static interface Delegate {
        boolean validateToolbarItem(final NSToolbarItem item);

        /**
         * Original signature : <code>NSToolbarItem* toolbar(NSToolbar*, NSString*, BOOL)</code><br>
         * <i>native declaration : :149</i>
         */
        NSToolbarItem toolbar_itemForItemIdentifier_willBeInsertedIntoToolbar(NSToolbar toolbar, String itemIdentifier, boolean flag);

        /**
         * Original signature : <code>NSArray* toolbarDefaultItemIdentifiers(NSToolbar*)</code><br>
         * <i>native declaration : :152</i>
         */
        NSArray toolbarDefaultItemIdentifiers(NSToolbar toolbar);

        /**
         * Original signature : <code>NSArray* toolbarAllowedItemIdentifiers(NSToolbar*)</code><br>
         * <i>native declaration : :155</i>
         */
        NSArray toolbarAllowedItemIdentifiers(NSToolbar toolbar);

        /**
         * Original signature : <code>NSArray* toolbarSelectableItemIdentifiers(NSToolbar*)</code><br>
         * <i>native declaration : :159</i>
         */
        NSArray toolbarSelectableItemIdentifiers(NSToolbar toolbar);
    }

    /**
     * Original signature : <code>id initWithIdentifier(NSString*)</code><br>
     * <i>native declaration : :68</i>
     */
    public abstract NSToolbar initWithIdentifier(String identifier);

    /**
     * Original signature : <code>void insertItemWithItemIdentifier(NSString*, NSInteger)</code><br>
     * <i>native declaration : :71</i>
     */
    public abstract void insertItemWithItemIdentifier_atIndex(String itemIdentifier, NSInteger index);

    /**
     * Original signature : <code>void removeItemAtIndex(NSInteger)</code><br>
     * <i>native declaration : :72</i>
     */
    public abstract void removeItemAtIndex(NSInteger index);

    /**
     * Original signature : <code>void setDelegate(id)</code><br>
     * <i>native declaration : :75</i>
     */
    public abstract void setDelegate(org.rococoa.ID delegate);

    /**
     * Original signature : <code>id delegate()</code><br>
     * <i>native declaration : :76</i>
     */
    public abstract org.rococoa.ID delegate();

    /**
     * Original signature : <code>void setVisible(BOOL)</code><br>
     * <i>native declaration : :79</i>
     */
    public abstract void setVisible(boolean shown);

    /**
     * Original signature : <code>BOOL isVisible()</code><br>
     * <i>native declaration : :80</i>
     */
    public abstract boolean isVisible();

    /**
     * Original signature : <code>void runCustomizationPalette(id)</code><br>
     * <i>native declaration : :83</i>
     */
    public abstract void runCustomizationPalette(final ID sender);

    /**
     * Original signature : <code>BOOL customizationPaletteIsRunning()</code><br>
     * <i>native declaration : :84</i>
     */
    public abstract boolean customizationPaletteIsRunning();

    /**
     * Original signature : <code>void setDisplayMode(NSToolbarDisplayMode)</code><br>
     * <i>native declaration : :90</i>
     */
    public abstract void setDisplayMode(NSUInteger displayMode);

    /**
     * Original signature : <code>NSToolbarDisplayMode displayMode()</code><br>
     * <i>native declaration : :91</i>
     */
    public abstract NSUInteger displayMode();

    /**
     * Original signature : <code>void setSelectedItemIdentifier(NSString*)</code><br>
     * <i>native declaration : :94</i>
     */
    public abstract void setSelectedItemIdentifier(String itemIdentifier);

    /**
     * Original signature : <code>NSString* selectedItemIdentifier()</code><br>
     * <i>native declaration : :95</i>
     */
    public abstract String selectedItemIdentifier();

    /**
     * Original signature : <code>void setSizeMode(NSToolbarSizeMode)</code><br>
     * <i>native declaration : :100</i>
     */
    public abstract void setSizeMode(NSUInteger sizeMode);

    /**
     * Original signature : <code>NSToolbarSizeMode sizeMode()</code><br>
     * <i>native declaration : :101</i>
     */
    public abstract NSUInteger sizeMode();

    /**
     * Use this API to hide the baseline NSToolbar draws between itself and the main window contents.  The default is YES.  This method should only be used before the toolbar is attached to its window (-[NSWindow setToolbar:]).<br>
     * Original signature : <code>void setShowsBaselineSeparator(BOOL)</code><br>
     * <i>native declaration : :107</i>
     */
    public abstract void setShowsBaselineSeparator(boolean flag);

    /**
     * Original signature : <code>BOOL showsBaselineSeparator()</code><br>
     * <i>native declaration : :108</i>
     */
    public abstract boolean showsBaselineSeparator();

    /**
     * Original signature : <code>void setAllowsUserCustomization(BOOL)</code><br>
     * <i>native declaration : :111</i>
     */
    public abstract void setAllowsUserCustomization(boolean allowCustomization);

    /**
     * Original signature : <code>BOOL allowsUserCustomization()</code><br>
     * <i>native declaration : :112</i>
     */
    public abstract boolean allowsUserCustomization();

    /**
     * Original signature : <code>NSString* identifier()</code><br>
     * <i>native declaration : :118</i>
     */
    public abstract String identifier();

    /**
     * Original signature : <code>NSArray* items()</code><br>
     * <i>native declaration : :121</i>
     */
    public abstract NSArray items();

    /**
     * Original signature : <code>NSArray* visibleItems()</code><br>
     * <i>native declaration : :124</i>
     */
    public abstract NSArray visibleItems();

    /**
     * Original signature : <code>void setAutosavesConfiguration(BOOL)</code><br>
     * <i>native declaration : :130</i>
     */
    public abstract void setAutosavesConfiguration(boolean flag);

    /**
     * Original signature : <code>BOOL autosavesConfiguration()</code><br>
     * <i>native declaration : :131</i>
     */
    public abstract boolean autosavesConfiguration();

    /**
     * Original signature : <code>void setConfigurationFromDictionary(NSDictionary*)</code><br>
     * <i>native declaration : :134</i>
     */
    public abstract void setConfigurationFromDictionary(NSDictionary configDict);

    /**
     * Original signature : <code>NSDictionary* configurationDictionary()</code><br>
     * <i>native declaration : :135</i>
     */
    public abstract NSDictionary configurationDictionary();

    /**
     * Original signature : <code>void validateVisibleItems()</code><br>
     * <i>native declaration : :141</i>
     */
    public abstract void validateVisibleItems();
}
