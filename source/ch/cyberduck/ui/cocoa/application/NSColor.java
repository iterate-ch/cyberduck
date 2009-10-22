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
import ch.cyberduck.ui.cocoa.foundation.NSCopying;
import ch.cyberduck.ui.cocoa.foundation.NSObject;

import org.rococoa.ObjCClass;
import org.rococoa.cocoa.CGFloat;

/// <i>native declaration : :35</i>
public abstract class NSColor extends NSObject implements NSCopying {
    private static final _Class CLASS = org.rococoa.Rococoa.createClass("NSColor", _Class.class);

    public static NSColor whiteColor() {
        return CLASS.whiteColor();
    }

    public static NSColor darkGrayColor() {
        return CLASS.darkGrayColor();
    }

    public static NSColor blueColor() {
        return CLASS.blueColor();
    }

    public static NSColor redColor() {
        return CLASS.redColor();
    }

    /**
     * Original signature : <code>NSColor* controlShadowColor()</code><br>
     * Dark border for controls<br>
     * <i>native declaration : :86</i>
     */
    public static NSColor controlShadowColor() {
        return CLASS.controlShadowColor();
    }

    /**
     * Original signature : <code>NSColor* controlDarkShadowColor()</code><br>
     * Darker border for controls<br>
     * <i>native declaration : :87</i>
     */
    public static NSColor controlDarkShadowColor() {
        return CLASS.controlDarkShadowColor();
    }

    /**
     * Original signature : <code>NSColor* controlColor()</code><br>
     * Control face and old window background color<br>
     * <i>native declaration : :88</i>
     */
    public static NSColor controlColor() {
        return CLASS.controlColor();
    }

    /**
     * Original signature : <code>NSColor* controlHighlightColor()</code><br>
     * Light border for controls<br>
     * <i>native declaration : :89</i>
     */
    public static NSColor controlHighlightColor() {
        return CLASS.controlShadowColor();
    }

    /**
     * Original signature : <code>NSColor* controlLightHighlightColor()</code><br>
     * Lighter border for controls<br>
     * <i>native declaration : :90</i>
     */
    public static NSColor controlLightHighlightColor() {
        return CLASS.controlShadowColor();
    }

    /**
     * Original signature : <code>NSColor* controlTextColor()</code><br>
     * Text on controls<br>
     * <i>native declaration : :91</i>
     */
    public static NSColor controlTextColor() {
        return CLASS.controlTextColor();
    }

    /**
     * Original signature : <code>NSColor* controlBackgroundColor()</code><br>
     * Background of large controls (browser, tableview, clipview, ...)<br>
     * <i>native declaration : :92</i>
     */
    public static NSColor controlBackgroundColor() {
        return CLASS.controlBackgroundColor();
    }

    /**
     * Original signature : <code>NSColor* selectedControlColor()</code><br>
     * Control face for selected controls<br>
     * <i>native declaration : :93</i>
     */
    public static NSColor selectedControlColor() {
        return CLASS.selectedControlColor();
    }

    /**
     * Original signature : <code>NSColor* secondarySelectedControlColor()</code><br>
     * Color for selected controls when control is not active (that is, not focused)<br>
     * <i>native declaration : :94</i>
     */
    public static NSColor secondarySelectedControlColor() {
        return CLASS.secondarySelectedControlColor();
    }

    /**
     * Original signature : <code>NSColor* selectedControlTextColor()</code><br>
     * Text on selected controls<br>
     * <i>native declaration : :95</i>
     */
    public static NSColor selectedControlTextColor() {
        return CLASS.controlShadowColor();
    }

    /**
     * Original signature : <code>NSColor* disabledControlTextColor()</code><br>
     * Text on disabled controls<br>
     * <i>native declaration : :96</i>
     */
    public static NSColor disabledControlTextColor() {
        return CLASS.disabledControlTextColor();
    }

    /**
     * Original signature : <code>NSColor* textColor()</code><br>
     * Document text<br>
     * <i>native declaration : :97</i>
     */
    public static NSColor textColor() {
        return CLASS.textColor();
    }

    /**
     * Original signature : <code>NSColor* textBackgroundColor()</code><br>
     * Document text background<br>
     * <i>native declaration : :98</i>
     */
    public static NSColor textBackgroundColor() {
        return CLASS.textBackgroundColor();
    }

    /**
     * Original signature : <code>NSColor* selectedTextColor()</code><br>
     * Selected document text<br>
     * <i>native declaration : :99</i>
     */
    public static NSColor selectedTextColor() {
        return CLASS.selectedTextColor();
    }

    /**
     * Original signature : <code>NSColor* selectedTextBackgroundColor()</code><br>
     * Selected document text background<br>
     * <i>native declaration : :100</i>
     */
    public static NSColor selectedTextBackgroundColor() {
        return CLASS.selectedTextBackgroundColor();
    }

    /**
     * Original signature : <code>NSColor* gridColor()</code><br>
     * Grids in controls<br>
     * <i>native declaration : :101</i>
     */
    public static NSColor gridColor() {
        return CLASS.gridColor();
    }

    /**
     * Original signature : <code>NSColor* keyboardFocusIndicatorColor()</code><br>
     * Keyboard focus ring around controls<br>
     * <i>native declaration : :102</i>
     */
    public static NSColor keyboardFocusIndicatorColor() {
        return CLASS.keyboardFocusIndicatorColor();
    }

    /**
     * Original signature : <code>NSColor* windowBackgroundColor()</code><br>
     * Background fill for window contents<br>
     * <i>native declaration : :103</i>
     */
    public static NSColor windowBackgroundColor() {
        return CLASS.windowBackgroundColor();
    }

    /**
     * Original signature : <code>NSColor* scrollBarColor()</code><br>
     * Scroll bar slot color<br>
     * <i>native declaration : :105</i>
     */
    public static NSColor scrollBarColor() {
        return CLASS.scrollBarColor();
    }

    /**
     * Original signature : <code>NSColor* knobColor()</code><br>
     * Knob face color for controls<br>
     * <i>native declaration : :106</i>
     */
    public static NSColor knobColor() {
        return CLASS.knobColor();
    }

    /**
     * Original signature : <code>NSColor* selectedKnobColor()</code><br>
     * Knob face color for selected controls<br>
     * <i>native declaration : :107</i>
     */
    public static NSColor selectedKnobColor() {
        return CLASS.selectedKnobColor();
    }

    /**
     * Original signature : <code>NSColor* windowFrameColor()</code><br>
     * Window frames<br>
     * <i>native declaration : :109</i>
     */
    public static NSColor windowFrameColor() {
        return CLASS.windowFrameColor();
    }

    /**
     * Original signature : <code>NSColor* windowFrameTextColor()</code><br>
     * Text on window frames<br>
     * <i>native declaration : :110</i>
     */
    public static NSColor windowFrameTextColor() {
        return CLASS.windowFrameTextColor();
    }

    /**
     * Original signature : <code>NSColor* selectedMenuItemColor()</code><br>
     * Highlight color for menus<br>
     * <i>native declaration : :112</i>
     */
    public static NSColor selectedMenuItemColor() {
        return CLASS.selectedMenuItemColor();
    }

    /**
     * Original signature : <code>NSColor* selectedMenuItemTextColor()</code><br>
     * Highlight color for menu text<br>
     * <i>native declaration : :113</i>
     */
    public static NSColor selectedMenuItemTextColor() {
        return CLASS.selectedMenuItemTextColor();
    }

    /**
     * Original signature : <code>NSColor* highlightColor()</code><br>
     * Highlight color for UI elements (this is abstract and defines the color all highlights tend toward)<br>
     * <i>native declaration : :115</i>
     */
    public static NSColor highlightColor() {
        return CLASS.highlightColor();
    }

    /**
     * Original signature : <code>NSColor* shadowColor()</code><br>
     * Shadow color for UI elements (this is abstract and defines the color all shadows tend toward)<br>
     * <i>native declaration : :116</i>
     */
    public static NSColor shadowColor() {
        return CLASS.shadowColor();
    }

    /**
     * Original signature : <code>NSColor* headerColor()</code><br>
     * Background color for header cells in Table/OutlineView<br>
     * <i>native declaration : :118</i>
     */
    public static NSColor headerColor() {
        return CLASS.headerColor();
    }

    /**
     * Original signature : <code>NSColor* headerTextColor()</code><br>
     * Text color for header cells in Table/OutlineView<br>
     * <i>native declaration : :119</i>
     */
    public static NSColor headerTextColor() {
        return CLASS.headerTextColor();
    }

    /**
     * Original signature : <code>NSColor* alternateSelectedControlColor()</code><br>
     * Similar to selectedControlColor; for use in lists and tables<br>
     * <i>native declaration : :122</i>
     */
    public static NSColor alternateSelectedControlColor() {
        return CLASS.alternateSelectedControlColor();
    }

    /**
     * Original signature : <code>NSColor* alternateSelectedControlTextColor()</code><br>
     * Similar to selectedControlTextColor; see alternateSelectedControlColor<br>
     * <i>native declaration : :123</i>
     */
    public static NSColor alternateSelectedControlTextColor() {
        return CLASS.alternateSelectedControlTextColor();
    }

    public interface _Class extends ObjCClass {
        /**
         * Create NSCalibratedWhiteColorSpace colors.<br>
         * Original signature : <code>NSColor* colorWithCalibratedWhite(CGFloat, CGFloat)</code><br>
         * <i>native declaration : :39</i>
         */
        NSColor colorWithCalibratedWhite_alpha(CGFloat white, CGFloat alpha);

        /**
         * Create NSCalibratedRGBColorSpace colors.<br>
         * Original signature : <code>NSColor* colorWithCalibratedHue(CGFloat, CGFloat, CGFloat, CGFloat)</code><br>
         * <i>native declaration : :44</i>
         */
        NSColor colorWithCalibratedHue_saturation_brightness_alpha(CGFloat hue, CGFloat saturation, CGFloat brightness, CGFloat alpha);

        /**
         * Original signature : <code>NSColor* colorWithCalibratedRed(CGFloat, CGFloat, CGFloat, CGFloat)</code><br>
         * <i>native declaration : :45</i>
         */
        NSColor colorWithCalibratedRed_green_blue_alpha(CGFloat red, CGFloat green, CGFloat blue, CGFloat alpha);

        /**
         * Create colors in various device color spaces. In PostScript these colorspaces correspond directly to the device-dependent operators setgray, sethsbcolor, setrgbcolor, and setcmykcolor.<br>
         * Original signature : <code>NSColor* colorWithDeviceWhite(CGFloat, CGFloat)</code><br>
         * <i>native declaration : :50</i>
         */
        NSColor colorWithDeviceWhite_alpha(CGFloat white, CGFloat alpha);

        /**
         * Original signature : <code>NSColor* colorWithDeviceHue(CGFloat, CGFloat, CGFloat, CGFloat)</code><br>
         * <i>native declaration : :51</i>
         */
        NSColor colorWithDeviceHue_saturation_brightness_alpha(CGFloat hue, CGFloat saturation, CGFloat brightness, CGFloat alpha);

        /**
         * Original signature : <code>NSColor* colorWithDeviceRed(CGCGFloat, CGCGFloat, CGCGFloat, CGCGFloat)</code><br>
         * <i>native declaration : :52</i>
         */
        NSColor colorWithDeviceRed_green_blue_alpha(CGFloat red, CGFloat green, CGFloat blue, CGFloat alpha);

        /**
         * Original signature : <code>NSColor* colorWithDeviceCyan(CGFloat, CGFloat, CGFloat, CGFloat, CGFloat)</code><br>
         * <i>native declaration : :53</i>
         */
        NSColor colorWithDeviceCyan_magenta_yellow_black_alpha(CGFloat cyan, CGFloat magenta, CGFloat yellow, CGFloat black, CGFloat alpha);

        /**
         * Create named colors from standard color catalogs (such as Pantone).<br>
         * Original signature : <code>NSColor* colorWithCatalogName(NSString*, NSString*)</code><br>
         * <i>native declaration : :58</i>
         */
        NSColor colorWithCatalogName_colorName(com.sun.jna.Pointer listName, com.sun.jna.Pointer colorName);

        /**
         * Create colors with arbitrary colorspace. The number of components in the provided array should match the number dictated by the specified colorspace, plus one for alpha (supply 1.0 for opaque colors); otherwise an exception will be raised.  If the colorspace is one which cannot be used with NSColors, nil is returned.<br>
         * Original signature : <code>NSColor* colorWithColorSpace(NSColorSpace*, const CGFloat*, NSInteger)</code><br>
         * <i>native declaration : :64</i>
         */
        NSColor colorWithColorSpace_components_count(com.sun.jna.Pointer space, CGFloat components[], int numberOfComponents);

        /**
         * Create colors with arbitrary colorspace. The number of components in the provided array should match the number dictated by the specified colorspace, plus one for alpha (supply 1.0 for opaque colors); otherwise an exception will be raised.  If the colorspace is one which cannot be used with NSColors, nil is returned.<br>
         * Original signature : <code>NSColor* colorWithColorSpace(NSColorSpace*, const CGFloat*, NSInteger)</code><br>
         * <i>native declaration : :64</i>
         */
        NSColor colorWithColorSpace_components_count(com.sun.jna.Pointer space, java.nio.FloatBuffer components, int numberOfComponents);

        /**
         * Some convenience methods to create colors in the calibrated color spaces...<br>
         * Original signature : <code>NSColor* blackColor()</code><br>
         * 0.0 white<br>
         * <i>native declaration : :70</i>
         */
        NSColor blackColor();

        /**
         * Original signature : <code>NSColor* darkGrayColor()</code><br>
         * 0.333 white<br>
         * <i>native declaration : :71</i>
         */
        NSColor darkGrayColor();

        /**
         * Original signature : <code>NSColor* lightGrayColor()</code><br>
         * 0.667 white<br>
         * <i>native declaration : :72</i>
         */
        NSColor lightGrayColor();

        /**
         * Original signature : <code>NSColor* whiteColor()</code><br>
         * 1.0 white<br>
         * <i>native declaration : :73</i>
         */
        NSColor whiteColor();

        /**
         * Original signature : <code>NSColor* grayColor()</code><br>
         * 0.5 white<br>
         * <i>native declaration : :74</i>
         */
        NSColor grayColor();

        /**
         * Original signature : <code>NSColor* redColor()</code><br>
         * 1.0, 0.0, 0.0 RGB<br>
         * <i>native declaration : :75</i>
         */
        NSColor redColor();

        /**
         * Original signature : <code>NSColor* greenColor()</code><br>
         * 0.0, 1.0, 0.0 RGB<br>
         * <i>native declaration : :76</i>
         */
        NSColor greenColor();

        /**
         * Original signature : <code>NSColor* blueColor()</code><br>
         * 0.0, 0.0, 1.0 RGB<br>
         * <i>native declaration : :77</i>
         */
        NSColor blueColor();

        /**
         * Original signature : <code>NSColor* cyanColor()</code><br>
         * 0.0, 1.0, 1.0 RGB<br>
         * <i>native declaration : :78</i>
         */
        NSColor cyanColor();

        /**
         * Original signature : <code>NSColor* yellowColor()</code><br>
         * 1.0, 1.0, 0.0 RGB<br>
         * <i>native declaration : :79</i>
         */
        NSColor yellowColor();

        /**
         * Original signature : <code>NSColor* magentaColor()</code><br>
         * 1.0, 0.0, 1.0 RGB<br>
         * <i>native declaration : :80</i>
         */
        NSColor magentaColor();

        /**
         * Original signature : <code>NSColor* orangeColor()</code><br>
         * 1.0, 0.5, 0.0 RGB<br>
         * <i>native declaration : :81</i>
         */
        NSColor orangeColor();

        /**
         * Original signature : <code>NSColor* purpleColor()</code><br>
         * 0.5, 0.0, 0.5 RGB<br>
         * <i>native declaration : :82</i>
         */
        NSColor purpleColor();

        /**
         * Original signature : <code>NSColor* brownColor()</code><br>
         * 0.6, 0.4, 0.2 RGB<br>
         * <i>native declaration : :83</i>
         */
        NSColor brownColor();

        /**
         * Original signature : <code>NSColor* clearColor()</code><br>
         * 0.0 white, 0.0 alpha<br>
         * <i>native declaration : :84</i>
         */
        NSColor clearColor();

        /**
         * Original signature : <code>NSColor* controlShadowColor()</code><br>
         * Dark border for controls<br>
         * <i>native declaration : :86</i>
         */
        NSColor controlShadowColor();

        /**
         * Original signature : <code>NSColor* controlDarkShadowColor()</code><br>
         * Darker border for controls<br>
         * <i>native declaration : :87</i>
         */
        NSColor controlDarkShadowColor();

        /**
         * Original signature : <code>NSColor* controlColor()</code><br>
         * Control face and old window background color<br>
         * <i>native declaration : :88</i>
         */
        NSColor controlColor();

        /**
         * Original signature : <code>NSColor* controlHighlightColor()</code><br>
         * Light border for controls<br>
         * <i>native declaration : :89</i>
         */
        NSColor controlHighlightColor();

        /**
         * Original signature : <code>NSColor* controlLightHighlightColor()</code><br>
         * Lighter border for controls<br>
         * <i>native declaration : :90</i>
         */
        NSColor controlLightHighlightColor();

        /**
         * Original signature : <code>NSColor* controlTextColor()</code><br>
         * Text on controls<br>
         * <i>native declaration : :91</i>
         */
        NSColor controlTextColor();

        /**
         * Original signature : <code>NSColor* controlBackgroundColor()</code><br>
         * Background of large controls (browser, tableview, clipview, ...)<br>
         * <i>native declaration : :92</i>
         */
        NSColor controlBackgroundColor();

        /**
         * Original signature : <code>NSColor* selectedControlColor()</code><br>
         * Control face for selected controls<br>
         * <i>native declaration : :93</i>
         */
        NSColor selectedControlColor();

        /**
         * Original signature : <code>NSColor* secondarySelectedControlColor()</code><br>
         * Color for selected controls when control is not active (that is, not focused)<br>
         * <i>native declaration : :94</i>
         */
        NSColor secondarySelectedControlColor();

        /**
         * Original signature : <code>NSColor* selectedControlTextColor()</code><br>
         * Text on selected controls<br>
         * <i>native declaration : :95</i>
         */
        NSColor selectedControlTextColor();

        /**
         * Original signature : <code>NSColor* disabledControlTextColor()</code><br>
         * Text on disabled controls<br>
         * <i>native declaration : :96</i>
         */
        NSColor disabledControlTextColor();

        /**
         * Original signature : <code>NSColor* textColor()</code><br>
         * Document text<br>
         * <i>native declaration : :97</i>
         */
        NSColor textColor();

        /**
         * Original signature : <code>NSColor* textBackgroundColor()</code><br>
         * Document text background<br>
         * <i>native declaration : :98</i>
         */
        NSColor textBackgroundColor();

        /**
         * Original signature : <code>NSColor* selectedTextColor()</code><br>
         * Selected document text<br>
         * <i>native declaration : :99</i>
         */
        NSColor selectedTextColor();

        /**
         * Original signature : <code>NSColor* selectedTextBackgroundColor()</code><br>
         * Selected document text background<br>
         * <i>native declaration : :100</i>
         */
        NSColor selectedTextBackgroundColor();

        /**
         * Original signature : <code>NSColor* gridColor()</code><br>
         * Grids in controls<br>
         * <i>native declaration : :101</i>
         */
        NSColor gridColor();

        /**
         * Original signature : <code>NSColor* keyboardFocusIndicatorColor()</code><br>
         * Keyboard focus ring around controls<br>
         * <i>native declaration : :102</i>
         */
        NSColor keyboardFocusIndicatorColor();

        /**
         * Original signature : <code>NSColor* windowBackgroundColor()</code><br>
         * Background fill for window contents<br>
         * <i>native declaration : :103</i>
         */
        NSColor windowBackgroundColor();

        /**
         * Original signature : <code>NSColor* scrollBarColor()</code><br>
         * Scroll bar slot color<br>
         * <i>native declaration : :105</i>
         */
        NSColor scrollBarColor();

        /**
         * Original signature : <code>NSColor* knobColor()</code><br>
         * Knob face color for controls<br>
         * <i>native declaration : :106</i>
         */
        NSColor knobColor();

        /**
         * Original signature : <code>NSColor* selectedKnobColor()</code><br>
         * Knob face color for selected controls<br>
         * <i>native declaration : :107</i>
         */
        NSColor selectedKnobColor();

        /**
         * Original signature : <code>NSColor* windowFrameColor()</code><br>
         * Window frames<br>
         * <i>native declaration : :109</i>
         */
        NSColor windowFrameColor();

        /**
         * Original signature : <code>NSColor* windowFrameTextColor()</code><br>
         * Text on window frames<br>
         * <i>native declaration : :110</i>
         */
        NSColor windowFrameTextColor();

        /**
         * Original signature : <code>NSColor* selectedMenuItemColor()</code><br>
         * Highlight color for menus<br>
         * <i>native declaration : :112</i>
         */
        NSColor selectedMenuItemColor();

        /**
         * Original signature : <code>NSColor* selectedMenuItemTextColor()</code><br>
         * Highlight color for menu text<br>
         * <i>native declaration : :113</i>
         */
        NSColor selectedMenuItemTextColor();

        /**
         * Original signature : <code>NSColor* highlightColor()</code><br>
         * Highlight color for UI elements (this is abstract and defines the color all highlights tend toward)<br>
         * <i>native declaration : :115</i>
         */
        NSColor highlightColor();

        /**
         * Original signature : <code>NSColor* shadowColor()</code><br>
         * Shadow color for UI elements (this is abstract and defines the color all shadows tend toward)<br>
         * <i>native declaration : :116</i>
         */
        NSColor shadowColor();

        /**
         * Original signature : <code>NSColor* headerColor()</code><br>
         * Background color for header cells in Table/OutlineView<br>
         * <i>native declaration : :118</i>
         */
        NSColor headerColor();

        /**
         * Original signature : <code>NSColor* headerTextColor()</code><br>
         * Text color for header cells in Table/OutlineView<br>
         * <i>native declaration : :119</i>
         */
        NSColor headerTextColor();

        /**
         * Original signature : <code>NSColor* alternateSelectedControlColor()</code><br>
         * Similar to selectedControlColor; for use in lists and tables<br>
         * <i>native declaration : :122</i>
         */
        NSColor alternateSelectedControlColor();

        /**
         * Original signature : <code>NSColor* alternateSelectedControlTextColor()</code><br>
         * Similar to selectedControlTextColor; see alternateSelectedControlColor<br>
         * <i>native declaration : :123</i>
         */
        NSColor alternateSelectedControlTextColor();

        /**
         * Original signature : <code>NSArray* controlAlternatingRowBackgroundColors()</code><br>
         * Standard colors for alternating colored rows in tables and lists (for instance, light blue/white; don't assume just two colors)<br>
         * <i>native declaration : :127</i>
         */
        NSArray controlAlternatingRowBackgroundColors();
        /**
         * <i>native declaration : :133</i><br>
         * Conversion Error : /// Original signature : <code>NSColor* colorForControlTint(null)</code><br>
         * + (NSColor*)colorForControlTint:(null)controlTint; // pass in valid tint to get rough color matching. returns default if invalid tint<br>
         *  (Argument controlTint cannot be converted)
         */
        /**
         * Original signature : <code>currentControlTint()</code><br>
         * returns current system control tint<br>
         * <i>native declaration : :136</i>
         */
        com.sun.jna.Pointer currentControlTint();

        /**
         * Pasteboard methods<br>
         * Original signature : <code>NSColor* colorFromPasteboard(NSPasteboard*)</code><br>
         * <i>native declaration : :243</i>
         */
        NSColor colorFromPasteboard(NSPasteboard pasteBoard);

        /**
         * Pattern methods. Note that colorWithPatternImage: mistakenly returns a non-autoreleased color in 10.1.x and earlier. This has been fixed in (NSAppKitVersionNumber >= NSAppKitVersionNumberWithPatternColorLeakFix), for apps linked post-10.1.x.<br>
         * Original signature : <code>NSColor* colorWithPatternImage(NSImage*)</code><br>
         * <i>native declaration : :248</i>
         */
        NSColor colorWithPatternImage(NSImage image);

        /**
         * Global flag for determining whether an application supports alpha.  This flag is consulted when an application imports alpha (through color dragging, for instance). The value of this flag also determines whether the color panel has an opacity slider. This value is YES by default, indicating that the opacity components of imported colors will be set to 1.0. If an application wants alpha, it can either set the "NSIgnoreAlpha" default to NO or call the set method below.<br>
         * This method provides a global approach to removing alpha which might not always be appropriate. Applications which need to import alpha sometimes should set this flag to NO and explicitly make colors opaque in cases where it matters to them.<br>
         * Original signature : <code>void setIgnoresAlpha(BOOL)</code><br>
         * <i>native declaration : :260</i>
         */
        void setIgnoresAlpha(boolean flag);

        /**
         * Original signature : <code>BOOL ignoresAlpha()</code><br>
         * <i>native declaration : :261</i>
         */
        boolean ignoresAlpha();

        /**
         * Original signature : <code>NSColor* colorWithCIColor(CIColor*)</code><br>
         * <i>from NSQuartzCoreAdditions native declaration : :268</i>
         */
//        NSColor colorWithCIColor(CIColor color);
    }

    /**
     * Original signature : <code>NSColor* highlightWithLevel(CGFloat)</code><br>
     * val = 0 => receiver, val = 1 => highlightColor<br>
     * <i>native declaration : :130</i>
     */
    public abstract NSColor highlightWithLevel(CGFloat val);

    /**
     * Original signature : <code>NSColor* shadowWithLevel(CGFloat)</code><br>
     * val = 0 => receiver, val = 1 => shadowColor<br>
     * <i>native declaration : :131</i>
     */
    public abstract NSColor shadowWithLevel(CGFloat val);

    /**
     * Set the color: Sets the fill and stroke colors in the current drawing context. If the color doesn't know about alpha, it's set to 1.0. Should be implemented by subclassers.<br>
     * Original signature : <code>void set()</code><br>
     * <i>native declaration : :142</i>
     */
    public abstract void set();

    /**
     * Set the fill or stroke colors individually. These should be implemented by subclassers.<br>
     * Original signature : <code>void setFill()</code><br>
     * <i>native declaration : :147</i>
     */
    public abstract void setFill();

    /**
     * Original signature : <code>void setStroke()</code><br>
     * <i>native declaration : :148</i>
     */
    public abstract void setStroke();

    /**
     * Get the color space of the color. Should be implemented by subclassers.<br>
     * Original signature : <code>NSString* colorSpaceName()</code><br>
     * <i>native declaration : :153</i>
     */
    public abstract com.sun.jna.Pointer colorSpaceName();

    /**
     * Convert the color to another colorspace, using a colorspace name. This may return nil if the specified conversion cannot be done. The abstract implementation of this method returns the receiver if the specified colorspace matches that of the receiver; otherwise it returns nil. Subclassers who can convert themselves to other colorspaces override this method to do something better.<br>
     * The version of this method which takes a device description allows the color to specialize itself for the given device.  Device descriptions can be obtained from windows, screens, and printers with the "deviceDescription" method.<br>
     * If device is nil then the current device (as obtained from the currently lockFocus'ed view's window or, if printing, the current printer) is used. The method without the device: argument passes nil for the device.<br>
     * If colorSpace is nil, then the most appropriate color space is used.<br>
     * Original signature : <code>NSColor* colorUsingColorSpaceName(NSString*)</code><br>
     * <i>native declaration : :164</i>
     */
    public abstract NSColor colorUsingColorSpaceName(com.sun.jna.Pointer colorSpace);

    /**
     * Original signature : <code>NSColor* colorUsingColorSpaceName(NSString*, NSDictionary*)</code><br>
     * <i>native declaration : :165</i>
     */
    public abstract NSColor colorUsingColorSpaceName_device(com.sun.jna.Pointer colorSpace, com.sun.jna.Pointer deviceDescription);

    /**
     * colorUsingColorSpace: will convert existing color to a new colorspace and create a new color, which will likely have different component values but look the same. It will return the same color if the colorspace is already the same as the one specified.  Will return nil if conversion is not possible.<br>
     * Original signature : <code>NSColor* colorUsingColorSpace(NSColorSpace*)</code><br>
     * <i>native declaration : :171</i>
     */
    public abstract NSColor colorUsingColorSpace(com.sun.jna.Pointer space);

    /**
     * Blend using the NSCalibratedRGB color space. Both colors are converted into the calibrated RGB color space, and they are blended by taking fraction of color and 1 - fraction of the receiver. The result is in the calibrated RGB color space. If the colors cannot be converted into the calibrated RGB color space the blending fails and nil is returned.<br>
     * Original signature : <code>NSColor* blendedColorWithFraction(CGFloat, NSColor*)</code><br>
     * <i>native declaration : :177</i>
     */
    public abstract NSColor blendedColorWithFraction_ofColor(CGFloat fraction, NSColor color);

    /**
     * Returns a color in the same color space as the receiver with the specified alpha component. The abstract implementation of this method returns the receiver if alpha is 1.0, otherwise it returns nil; subclassers who have explicit opacity components override this method to actually return a color with the specified alpha.<br>
     * Original signature : <code>NSColor* colorWithAlphaComponent(CGFloat)</code><br>
     * <i>native declaration : :182</i>
     */
    public abstract NSColor colorWithAlphaComponent(CGFloat alpha);

    /**
     * Get the catalog and color name of standard colors from catalogs. These colors are special colors which are usually looked up on each device by their name.<br>
     * Original signature : <code>NSString* catalogNameComponent()</code><br>
     * <i>native declaration : :189</i>
     */
    public abstract String catalogNameComponent();

    /**
     * Original signature : <code>NSString* colorNameComponent()</code><br>
     * <i>native declaration : :190</i>
     */
    public abstract String colorNameComponent();

    /**
     * Return localized versions of the above.<br>
     * Original signature : <code>NSString* localizedCatalogNameComponent()</code><br>
     * <i>native declaration : :194</i>
     */
    public abstract String localizedCatalogNameComponent();

    /**
     * Original signature : <code>NSString* localizedColorNameComponent()</code><br>
     * <i>native declaration : :195</i>
     */
    public abstract String localizedColorNameComponent();

    /**
     * Get the red, green, or blue components of NSCalibratedRGB or NSDeviceRGB colors.<br>
     * Original signature : <code>CGFloat redComponent()</code><br>
     * <i>native declaration : :199</i>
     */
    public abstract CGFloat redComponent();

    /**
     * Original signature : <code>CGFloat greenComponent()</code><br>
     * <i>native declaration : :200</i>
     */
    public abstract CGFloat greenComponent();

    /**
     * Original signature : <code>CGFloat blueComponent()</code><br>
     * <i>native declaration : :201</i>
     */
    public abstract CGFloat blueComponent();

    /**
     * Original signature : <code>void getRed(CGFloat*, CGFloat*, CGFloat*, CGFloat*)</code><br>
     * <i>native declaration : :202</i>
     */
    public abstract void getRed_green_blue_alpha(CGFloat red, CGFloat green, CGFloat blue, CGFloat alpha);

    /**
     * Get the components of NSCalibratedRGB or NSDeviceRGB colors as hue, saturation, or brightness.<br>
     * Original signature : <code>CGFloat hueComponent()</code><br>
     * <i>native declaration : :206</i>
     */
    public abstract CGFloat hueComponent();

    /**
     * Original signature : <code>CGFloat saturationComponent()</code><br>
     * <i>native declaration : :207</i>
     */
    public abstract CGFloat saturationComponent();

    /**
     * Original signature : <code>CGFloat brightnessComponent()</code><br>
     * <i>native declaration : :208</i>
     */
    public abstract CGFloat brightnessComponent();

    /**
     * Original signature : <code>void getHue(CGFloat*, CGFloat*, CGFloat*, CGFloat*)</code><br>
     * <i>native declaration : :209</i>
     */
    public abstract void getHue_saturation_brightness_alpha(java.nio.FloatBuffer hue, java.nio.FloatBuffer saturation, java.nio.FloatBuffer brightness, java.nio.FloatBuffer alpha);

    /**
     * Get the white component of NSCalibratedWhite or NSDeviceWhite colors.<br>
     * Original signature : <code>CGFloat whiteComponent()</code><br>
     * <i>native declaration : :214</i>
     */
    public abstract CGFloat whiteComponent();

    /**
     * Original signature : <code>void getWhite(CGFloat*, CGFloat*)</code><br>
     * <i>native declaration : :215</i>
     */
    public abstract void getWhite_alpha(java.nio.FloatBuffer white, java.nio.FloatBuffer alpha);

    /**
     * Get the CMYK components of NSDeviceCMYK colors.<br>
     * Original signature : <code>CGFloat cyanComponent()</code><br>
     * <i>native declaration : :220</i>
     */
    public abstract CGFloat cyanComponent();

    /**
     * Original signature : <code>CGFloat magentaComponent()</code><br>
     * <i>native declaration : :221</i>
     */
    public abstract CGFloat magentaComponent();

    /**
     * Original signature : <code>CGFloat yellowComponent()</code><br>
     * <i>native declaration : :222</i>
     */
    public abstract CGFloat yellowComponent();

    /**
     * Original signature : <code>CGFloat blackComponent()</code><br>
     * <i>native declaration : :223</i>
     */
    public abstract CGFloat blackComponent();

    /**
     * Original signature : <code>void getCyan(CGFloat*, CGFloat*, CGFloat*, CGFloat*, CGFloat*)</code><br>
     * <i>native declaration : :224</i>
     */
    public abstract void getCyan_magenta_yellow_black_alpha(java.nio.FloatBuffer cyan, java.nio.FloatBuffer magenta, java.nio.FloatBuffer yellow, java.nio.FloatBuffer black, java.nio.FloatBuffer alpha);

    /**
     * For colors with custom colorspace; get the colorspace and individual floating point components, including alpha. Note that all these methods will work for other NSColors which have floating point components. They will raise exceptions otherwise, like other existing colorspace-specific methods.<br>
     * Original signature : <code>NSColorSpace* colorSpace()</code><br>
     * <i>native declaration : :230</i>
     */
    public abstract com.sun.jna.Pointer colorSpace();

    /**
     * Original signature : <code>NSInteger numberOfComponents()</code><br>
     * <i>native declaration : :231</i>
     */
    public abstract int numberOfComponents();

    /**
     * Original signature : <code>void getComponents(CGFloat*)</code><br>
     * <i>native declaration : :232</i>
     */
    public abstract void getComponents(java.nio.FloatBuffer components);

    /**
     * Get the alpha component. For colors which do not have alpha components, this will return 1.0 (opaque).<br>
     * Original signature : <code>CGFloat alphaComponent()</code><br>
     * <i>native declaration : :238</i>
     */
    public abstract CGFloat alphaComponent();

    /**
     * Original signature : <code>void writeToPasteboard(NSPasteboard*)</code><br>
     * <i>native declaration : :244</i>
     */
    public abstract void writeToPasteboard(NSPasteboard pasteBoard);

    /**
     * Original signature : <code>NSImage* patternImage()</code><br>
     * <i>native declaration : :249</i>
     */
    public abstract NSImage patternImage();
    /**
     * <i>native declaration : :253</i><br>
     * Conversion Error : /**<br>
     *  * Draws the color and adorns it to indicate the type of color. Used by colorwells, swatches, and other UI objects that need to display colors. Implementation in NSColor simply draws the color (with an indication of transparency if the color isn't fully opaque); subclassers can draw more stuff as they see fit.<br>
     *  * Original signature : <code>void drawSwatchInRect(null)</code><br>
     *  * /<br>
     * - (void)drawSwatchInRect:(null)rect; (Argument rect cannot be converted)
     */
}
