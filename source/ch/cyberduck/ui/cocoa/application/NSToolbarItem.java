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

import ch.cyberduck.ui.cocoa.foundation.NSCopying;
import ch.cyberduck.ui.cocoa.foundation.NSObject;

import org.rococoa.ID;
import org.rococoa.ObjCClass;
import org.rococoa.Selector;
import org.rococoa.cocoa.foundation.NSSize;

/// <i>native declaration : :10</i>
public abstract class NSToolbarItem extends NSObject implements NSCopying, NSValidatedUserInterfaceItem {
    private static final _Class CLASS = org.rococoa.Rococoa.createClass("NSToolbarItem", _Class.class);

    public static final String NSToolbarFlexibleItemIdentifier = "NSToolbarFlexibleSpaceItem";
    public static final String NSToolbarSeparatorItemIdentifier = "NSToolbarSeparatorItem";
    public static final String NSToolbarSpaceItemIdentifier = "NSToolbarSpaceItem";
    public static final String NSToolbarFlexibleSpaceItemIdentifier = "NSToolbarFlexibleSpaceItem";
    public static final String NSToolbarShowColorsItemIdentifier = "NSToolbarShowColorsItem";
    public static final String NSToolbarShowFontsItemIdentifier = "NSToolbarShowFontsItem";
    public static final String NSToolbarCustomizeToolbarItemIdentifier = "NSToolbarCustomizeToolbarItem";
    public static final String NSToolbarPrintItemIdentifier = "NSToolbarPrintItem";

    public static final int VisibilityPriorityStandard = 0;
    public static final int VisibilityPriorityLow = -1000;
    public static final int VisibilityPriorityHigh = 1000;
    public static final int VisibilityPriorityUser = 2000;

    public static NSToolbarItem itemWithIdentifier(String itemIdentifier) {
        return CLASS.alloc().initWithItemIdentifier(itemIdentifier);
    }

    public interface _Class extends ObjCClass {
        NSToolbarItem alloc();
    }

    /**
     * Original signature : <code>id initWithItemIdentifier(NSString*)</code><br>
     * <i>native declaration : :62</i>
     */
    public abstract NSToolbarItem initWithItemIdentifier(String itemIdentifier);

    /**
     * Original signature : <code>NSString* itemIdentifier()</code><br>
     * <i>native declaration : :65</i>
     */
    public abstract String itemIdentifier();

    /**
     * Original signature : <code>NSToolbar* toolbar()</code><br>
     * <i>native declaration : :66</i>
     */
    public abstract NSToolbar toolbar();

    /**
     * Original signature : <code>void setLabel(NSString*)</code><br>
     * <i>native declaration : :71</i>
     */
    public abstract void setLabel(String label);

    /**
     * Original signature : <code>NSString* label()</code><br>
     * <i>native declaration : :72</i>
     */
    public abstract String label();

    /**
     * Original signature : <code>void setPaletteLabel(NSString*)</code><br>
     * <i>native declaration : :75</i>
     */
    public abstract void setPaletteLabel(String paletteLabel);

    /**
     * Original signature : <code>NSString* paletteLabel()</code><br>
     * <i>native declaration : :76</i>
     */
    public abstract String paletteLabel();

    /**
     * Original signature : <code>void setToolTip(NSString*)</code><br>
     * <i>native declaration : :79</i>
     */
    public abstract void setToolTip(String toolTip);

    /**
     * Original signature : <code>NSString* toolTip()</code><br>
     * <i>native declaration : :80</i>
     */
    public abstract String toolTip();

    /**
     * Original signature : <code>void setMenuFormRepresentation(NSMenuItem*)</code><br>
     * <i>native declaration : :83</i>
     */
    public abstract void setMenuFormRepresentation(NSMenuItem menuItem);

    /**
     * Original signature : <code>NSMenuItem* menuFormRepresentation()</code><br>
     * <i>native declaration : :84</i>
     */
    public abstract NSMenuItem menuFormRepresentation();

    /**
     * Original signature : <code>void setTag(NSInteger)</code><br>
     * <i>native declaration : :87</i>
     */
    public abstract void setTag(int tag);

    /**
     * Original signature : <code>void setTarget(id)</code><br>
     * <i>native declaration : :91</i>
     */
    public abstract void setTarget(ID target);

    /**
     * Original signature : <code>id target()</code><br>
     * <i>native declaration : :92</i>
     */
    public abstract ID target();

    /**
     * <i>native declaration : :95</i><br>
     * Conversion Error : /// Original signature : <code>void setAction(null)</code><br>
     * - (void)setAction:(null)action; (Argument action cannot be converted)
     */
    public abstract void setAction(Selector action);

    /**
     * Original signature : <code>void setEnabled(BOOL)</code><br>
     * <i>native declaration : :99</i>
     */
    public abstract void setEnabled(boolean enabled);

    /**
     * Original signature : <code>BOOL isEnabled()</code><br>
     * <i>native declaration : :100</i>
     */
    public abstract boolean isEnabled();

    /**
     * Original signature : <code>void setImage(NSImage*)</code><br>
     * <i>native declaration : :103</i>
     */
    public abstract void setImage(NSImage image);

    /**
     * Original signature : <code>NSImage* image()</code><br>
     * <i>native declaration : :104</i>
     */
    public abstract NSImage image();

    /**
     * Original signature : <code>void setView(NSView*)</code><br>
     * <i>native declaration : :107</i>
     */
    public abstract void setView(NSView view);

    /**
     * Original signature : <code>NSView* view()</code><br>
     * <i>native declaration : :108</i>
     */
    public abstract NSView view();

    /**
     * <i>native declaration : :111</i><br>
     * Conversion Error : NSSize
     */
    public abstract void setMinSize(NSSize size);

    /**
     * <i>native declaration : :112</i><br>
     * Conversion Error : NSSize
     */
    public abstract void setMaxSize(NSSize size);
    /**
     * <i>native declaration : :115</i><br>
     * Conversion Error : NSSize
     */
    /**
     * <i>native declaration : :116</i><br>
     * Conversion Error : NSSize
     */
    /// <i>native declaration : :24</i>
    /**
     * Original signature : <code>void setVisibilityPriority(NSInteger)</code><br>
     * <i>native declaration : :123</i>
     */
    public abstract void setVisibilityPriority(int visibilityPriority);

    /**
     * Original signature : <code>NSInteger visibilityPriority()</code><br>
     * <i>native declaration : :124</i>
     */
    public abstract int visibilityPriority();

    /**
     * Original signature : <code>void validate()</code><br>
     * <i>native declaration : :131</i>
     */
    public abstract void validate();

    /**
     * Original signature : <code>void setAutovalidates(BOOL)</code><br>
     * <i>native declaration : :136</i>
     */
    public abstract void setAutovalidates(boolean resistance);

    /**
     * Original signature : <code>BOOL autovalidates()</code><br>
     * <i>native declaration : :137</i>
     */
    public abstract boolean autovalidates();

    /**
     * Original signature : <code>BOOL allowsDuplicatesInToolbar()</code><br>
     * <i>native declaration : :145</i>
     */
    public abstract boolean allowsDuplicatesInToolbar();
}
