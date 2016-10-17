package ch.cyberduck.binding.application;

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

import ch.cyberduck.binding.foundation.NSArray;
import ch.cyberduck.binding.foundation.NSCopying;
import ch.cyberduck.binding.foundation.NSDictionary;
import ch.cyberduck.binding.foundation.NSObject;

import org.rococoa.ObjCClass;
import org.rococoa.cocoa.CGFloat;
import org.rococoa.cocoa.foundation.NSUInteger;

/// <i>native declaration : :45</i>
public abstract class NSFont extends NSObject implements NSCopying {
    private static final _Class CLASS = org.rococoa.Rococoa.createClass("NSFont", _Class.class);

    public static final double NSFontWeightUltraLight = -0.80d;
    public static final double NSFontWeightThin = -0.60d;
    public static final double NSFontWeightLight = -0.40d;
    public static final double NSFontWeightRegular = 0.00d;
    public static final double NSFontWeightMedium = 0.23d;
    public static final double NSFontWeightSemibold = 0.30d;
    public static final double NSFontWeightBold = 0.40d;
    public static final double NSFontWeightHeavy = 0.56d;
    public static final double NSFontWeightBlack = 0.62d;

    /**
     * User font settings<br>
     * Original signature : <code>NSFont* userFontOfSize(CGFloat)</code><br>
     * Aqua Application font<br>
     * <i>native declaration : :77</i>
     */
    public static NSFont userFontOfSize(double fontSize) {
        return CLASS.userFontOfSize(new CGFloat(fontSize));
    }

    /**
     * Original signature : <code>NSFont* userFixedPitchFontOfSize(CGFloat)</code><br>
     * Aqua fixed-pitch font<br>
     * <i>native declaration : :78</i>
     */
    public static NSFont userFixedPitchFontOfSize(double fontSize) {
        return CLASS.userFixedPitchFontOfSize(new CGFloat(fontSize));
    }

    /**
     * UI font settings<br>
     * Original signature : <code>NSFont* systemFontOfSize(CGFloat)</code><br>
     * Aqua System font<br>
     * <i>native declaration : :84</i>
     */
    public static NSFont systemFontOfSize(double fontSize) {
        return CLASS.systemFontOfSize(new CGFloat(fontSize));
    }

    /**
     * Original signature : <code>NSFont* boldSystemFontOfSize(CGFloat)</code><br>
     * Aqua System font (emphasized)<br>
     * <i>native declaration : :85</i>
     */
    public static NSFont boldSystemFontOfSize(double fontSize) {
        return CLASS.boldSystemFontOfSize(new CGFloat(fontSize));
    }

    public static NSFont monospacedDigitSystemFontOfSize(double fontSize) {
        return CLASS.monospacedDigitSystemFontOfSize_weight(new CGFloat(fontSize), new CGFloat(NSFontWeightRegular));
    }

    public static double smallSystemFontSize() {
        return CLASS.smallSystemFontSize().doubleValue();
    }

    public static double systemFontSize() {
        return CLASS.systemFontSize().doubleValue();
    }

    public static double labelFontSize() {
        return CLASS.labelFontSize().doubleValue();
    }

    public interface _Class extends ObjCClass {
        /**
         * Factory ********<br>
         * Original signature : <code>NSFont* fontWithName(NSString*, CGFloat)</code><br>
         * <i>native declaration : :62</i>
         */
        NSFont fontWithName_size(String fontName, CGFloat fontSize);

        /**
         * Original signature : <code>NSFont* fontWithName(NSString*, const CGFloat*)</code><br>
         * <i>native declaration : :63</i>
         */
        NSFont fontWithName_matrix(String fontName, CGFloat fontMatrix[]);

        /**
         * Original signature : <code>NSFont* fontWithName(NSString*, const CGFloat*)</code><br>
         * <i>native declaration : :63</i>
         */
        NSFont fontWithName_matrix(String fontName, CGFloat fontMatrix);

        /**
         * Instantiates an NSFont object matching fontDescriptor. If fontSize is greater than 0.0, it has precedence over NSFontSizeAttribute in fontDescriptor.<br>
         * Original signature : <code>NSFont* fontWithDescriptor(NSFontDescriptor*, CGFloat)</code><br>
         * <i>native declaration : :67</i>
         */
        NSFont fontWithDescriptor_size(com.sun.jna.Pointer fontDescriptor, CGFloat fontSize);

        /**
         * Instantiates an NSFont object matching fontDescriptor. If textTransform is non-nil, it has precedence over NSFontMatrixAttribute in fontDescriptor.<br>
         * Original signature : <code>NSFont* fontWithDescriptor(NSFontDescriptor*, NSAffineTransform*)</code><br>
         * <i>native declaration : :71</i>
         */
        NSFont fontWithDescriptor_textTransform(com.sun.jna.Pointer fontDescriptor, com.sun.jna.Pointer textTransform);

        /**
         * User font settings<br>
         * Original signature : <code>NSFont* userFontOfSize(CGFloat)</code><br>
         * Aqua Application font<br>
         * <i>native declaration : :77</i>
         */
        NSFont userFontOfSize(CGFloat fontSize);

        /**
         * Original signature : <code>NSFont* userFixedPitchFontOfSize(CGFloat)</code><br>
         * Aqua fixed-pitch font<br>
         * <i>native declaration : :78</i>
         */
        NSFont userFixedPitchFontOfSize(CGFloat fontSize);

        /**
         * Original signature : <code>void setUserFont(NSFont*)</code><br>
         * set preference for Application font.<br>
         * <i>native declaration : :79</i>
         */
        void setUserFont(NSFont aFont);

        /**
         * Original signature : <code>void setUserFixedPitchFont(NSFont*)</code><br>
         * set preference for fixed-pitch.<br>
         * <i>native declaration : :80</i>
         */
        void setUserFixedPitchFont(NSFont aFont);

        /**
         * UI font settings<br>
         * Original signature : <code>NSFont* systemFontOfSize(CGFloat)</code><br>
         * Aqua System font<br>
         * <i>native declaration : :84</i>
         */
        NSFont systemFontOfSize(CGFloat fontSize);

        /**
         * Original signature : <code>NSFont* boldSystemFontOfSize(CGFloat)</code><br>
         * Aqua System font (emphasized)<br>
         * <i>native declaration : :85</i>
         */
        NSFont boldSystemFontOfSize(CGFloat fontSize);

        NSFont monospacedDigitSystemFontOfSize_weight(CGFloat fontSize, CGFloat fontWeight);

        /**
         * Original signature : <code>NSFont* labelFontOfSize(CGFloat)</code><br>
         * Aqua label font<br>
         * <i>native declaration : :86</i>
         */
        NSFont labelFontOfSize(CGFloat fontSize);

        /**
         * Original signature : <code>NSFont* titleBarFontOfSize(CGFloat)</code><br>
         * <i>native declaration : :88</i>
         */
        NSFont titleBarFontOfSize(CGFloat fontSize);

        /**
         * Original signature : <code>NSFont* menuFontOfSize(CGFloat)</code><br>
         * <i>native declaration : :89</i>
         */
        NSFont menuFontOfSize(CGFloat fontSize);

        /**
         * Original signature : <code>NSFont* menuBarFontOfSize(CGFloat)</code><br>
         * <i>native declaration : :91</i>
         */
        NSFont menuBarFontOfSize(CGFloat fontSize);

        /**
         * Original signature : <code>NSFont* messageFontOfSize(CGFloat)</code><br>
         * <i>native declaration : :93</i>
         */
        NSFont messageFontOfSize(CGFloat fontSize);

        /**
         * Original signature : <code>NSFont* paletteFontOfSize(CGFloat)</code><br>
         * <i>native declaration : :94</i>
         */
        NSFont paletteFontOfSize(CGFloat fontSize);

        /**
         * Original signature : <code>NSFont* toolTipsFontOfSize(CGFloat)</code><br>
         * <i>native declaration : :95</i>
         */
        NSFont toolTipsFontOfSize(CGFloat fontSize);

        /**
         * Original signature : <code>NSFont* controlContentFontOfSize(CGFloat)</code><br>
         * <i>native declaration : :96</i>
         */
        NSFont controlContentFontOfSize(CGFloat fontSize);

        /**
         * UI font size settings<br>
         * Original signature : <code>CGFloat systemFontSize()</code><br>
         * size of the standard System font.<br>
         * <i>native declaration : :100</i>
         */
        CGFloat systemFontSize();

        /**
         * Original signature : <code>CGFloat smallSystemFontSize()</code><br>
         * size of standard small System font.<br>
         * <i>native declaration : :101</i>
         */
        CGFloat smallSystemFontSize();

        /**
         * Original signature : <code>CGFloat labelFontSize()</code><br>
         * size of the standard Label Font.<br>
         * <i>native declaration : :102</i>
         */
        CGFloat labelFontSize();
        /**
         * <i>native declaration : :105</i><br>
         * Conversion Error : /// Original signature : <code>CGFloat systemFontSizeForControlSize(null)</code><br>
         * + (CGFloat)systemFontSizeForControlSize:(null)controlSize; (Argument controlSize cannot be converted)
         */
        /**
         * Original signature : <code>void useFont(NSString*)</code><br>
         * This is now automatically handled by Quartz.<br>
         * <i>from NSFontDeprecated native declaration : :211</i>
         */
        void useFont(NSFont fontName);

        /**
         * Original signature : <code>NSArray* preferredFontNames()</code><br>
         * NSFontCascadeListAttribute offers more powerful font substitution management<br>
         * <i>from NSFontDeprecated native declaration : :217</i>
         */
        NSArray preferredFontNames();

        /**
         * Original signature : <code>void setPreferredFontNames(NSArray*)</code><br>
         * <i>from NSFontDeprecated native declaration : :218</i>
         */
        void setPreferredFontNames(String fontNameArray);
    }

    /**
     * Core font attribute ********<br>
     * Original signature : <code>NSString* fontName()</code><br>
     * <i>native declaration : :109</i>
     */
    public abstract String fontName();

    /**
     * Original signature : <code>CGFloat pointSize()</code><br>
     * <i>native declaration : :110</i>
     */
    public abstract CGFloat pointSize();

    /**
     * Original signature : <code>const CGFloat* matrix()</code><br>
     * <i>native declaration : :111</i>
     */
    public abstract com.sun.jna.ptr.FloatByReference matrix();

    /**
     * Original signature : <code>NSString* familyName()</code><br>
     * <i>native declaration : :112</i>
     */
    public abstract String familyName();

    /**
     * Original signature : <code>NSString* displayName()</code><br>
     * <i>native declaration : :113</i>
     */
    public abstract String displayName();

    /**
     * Original signature : <code>NSFontDescriptor* fontDescriptor()</code><br>
     * <i>native declaration : :115</i>
     */
    public abstract com.sun.jna.Pointer fontDescriptor();

    /**
     * Original signature : <code>NSAffineTransform* textTransform()</code><br>
     * <i>native declaration : :118</i>
     */
    public abstract com.sun.jna.Pointer textTransform();

    /**
     * Glyph coverage ********<br>
     * Original signature : <code>NSUInteger numberOfGlyphs()</code><br>
     * <i>native declaration : :122</i>
     */
    public abstract NSUInteger numberOfGlyphs();

    /**
     * Original signature : <code>mostCompatibleStringEncoding()</code><br>
     * <i>native declaration : :123</i>
     */
    public abstract com.sun.jna.Pointer mostCompatibleStringEncoding();

    /**
     * Original signature : <code>NSGlyph glyphWithName(NSString*)</code><br>
     * <i>native declaration : :124</i>
     */
    public abstract int glyphWithName(String aName);

    /**
     * Original signature : <code>NSCharacterSet* coveredCharacterSet()</code><br>
     * <i>native declaration : :126</i>
     */
    public abstract com.sun.jna.Pointer coveredCharacterSet();

    /**
     * These methods return scaled numbers.  If the font was created with a matrix, the matrix is applied automatically; otherwise the coordinates are multiplied by size.<br>
     * Original signature : <code>boundingRectForFont()</code><br>
     * <i>native declaration : :132</i>
     */
    public abstract com.sun.jna.Pointer boundingRectForFont();

    /**
     * Original signature : <code>maximumAdvancement()</code><br>
     * <i>native declaration : :133</i>
     */
    public abstract com.sun.jna.Pointer maximumAdvancement();

    /**
     * Original signature : <code>CGFloat ascender()</code><br>
     * <i>native declaration : :135</i>
     */
    public abstract CGFloat ascender();

    /**
     * Original signature : <code>CGFloat descender()</code><br>
     * <i>native declaration : :136</i>
     */
    public abstract CGFloat descender();

    /**
     * Original signature : <code>CGFloat leading()</code><br>
     * <i>native declaration : :138</i>
     */
    public abstract CGFloat leading();

    /**
     * Original signature : <code>CGFloat underlinePosition()</code><br>
     * <i>native declaration : :141</i>
     */
    public abstract CGFloat underlinePosition();

    /**
     * Original signature : <code>CGFloat underlineThickness()</code><br>
     * <i>native declaration : :142</i>
     */
    public abstract CGFloat underlineThickness();

    /**
     * Original signature : <code>CGFloat italicAngle()</code><br>
     * <i>native declaration : :143</i>
     */
    public abstract CGFloat italicAngle();

    /**
     * Original signature : <code>CGFloat capHeight()</code><br>
     * <i>native declaration : :144</i>
     */
    public abstract CGFloat capHeight();

    /**
     * Original signature : <code>CGFloat xHeight()</code><br>
     * <i>native declaration : :145</i>
     */
    public abstract CGFloat xHeight();

    /**
     * Original signature : <code>BOOL isFixedPitch()</code><br>
     * <i>native declaration : :146</i>
     */
    public abstract boolean isFixedPitch();

    /**
     * Glyph metrics ********<br>
     * Original signature : <code>boundingRectForGlyph(NSGlyph)</code><br>
     * <i>native declaration : :149</i>
     */
    public abstract com.sun.jna.Pointer boundingRectForGlyph(int aGlyph);

    /**
     * Original signature : <code>advancementForGlyph(NSGlyph)</code><br>
     * <i>native declaration : :150</i>
     */
    public abstract com.sun.jna.Pointer advancementForGlyph(int ag);
    /**
     * <i>native declaration : :154</i><br>
     * Conversion Error : /// Original signature : <code>void getBoundingRects(null, const NSGlyph*, NSUInteger)</code><br>
     * - (void)getBoundingRects:(null)bounds forGlyphs:(const NSGlyph*)glyphs count:(NSUInteger)glyphCount; (Argument bounds cannot be converted)
     */
    /**
     * <i>native declaration : :155</i><br>
     * Conversion Error : /// Original signature : <code>void getAdvancements(null, const NSGlyph*, NSUInteger)</code><br>
     * - (void)getAdvancements:(null)advancements forGlyphs:(const NSGlyph*)glyphs count:(NSUInteger)glyphCount; (Argument advancements cannot be converted)
     */
    /**
     * <i>native declaration : :156</i><br>
     * Conversion Error : /// Original signature : <code>void getAdvancements(null, const void*, NSUInteger)</code><br>
     * - (void)getAdvancements:(null)advancements forPackedGlyphs:(const void*)packedGlyphs length:(NSUInteger)length; // only supports NSNativeShortGlyphPacking<br>
     *  (Argument advancements cannot be converted)
     */
    /**
     * NSGraphicsContext-related ********<br>
     * Original signature : <code>void set()</code><br>
     * <i>native declaration : :160</i>
     */
    public abstract void set();

    /**
     * Original signature : <code>void setInContext(NSGraphicsContext*)</code><br>
     * <i>native declaration : :162</i>
     */
    public abstract void setInContext(com.sun.jna.Pointer graphicsContext);

    /**
     * Rendering mode ********<br>
     * Original signature : <code>NSFont* printerFont()</code><br>
     * <i>native declaration : :166</i>
     */
    public abstract NSFont printerFont();

    /**
     * Original signature : <code>NSFont* screenFont()</code><br>
     * Same as screenFontWithRenderingMode:NSFontDefaultRenderingMode<br>
     * <i>native declaration : :167</i>
     */
    public abstract NSFont screenFont();

    /**
     * Original signature : <code>NSFont* screenFontWithRenderingMode(NSFontRenderingMode)</code><br>
     * <i>native declaration : :169</i>
     */
    public abstract NSFont screenFontWithRenderingMode(int renderingMode);

    /**
     * Original signature : <code>NSFontRenderingMode renderingMode()</code><br>
     * <i>native declaration : :170</i>
     */
    public abstract int renderingMode();

    /**
     * Original signature : <code>CGFloat widthOfString(NSString*)</code><br>
     * This API never returns correct value. Use NSStringDrawing API instead.<br>
     * <i>from NSFontDeprecated native declaration : :212</i>
     */
    public abstract CGFloat widthOfString(com.sun.jna.Pointer string);

    /**
     * Original signature : <code>BOOL isBaseFont()</code><br>
     * <i>from NSFontDeprecated native declaration : :213</i>
     */
    public abstract boolean isBaseFont();

    /**
     * Original signature : <code>NSDictionary* afmDictionary()</code><br>
     * <i>from NSFontDeprecated native declaration : :214</i>
     */
    public abstract NSDictionary afmDictionary();

    /**
     * Original signature : <code>BOOL glyphIsEncoded(NSGlyph)</code><br>
     * Can be deduced by aGlyph < [NSFont numberOfGlyphs] since only NSNativeShortGlyphPacking is supported.<br>
     * <i>from NSFontDeprecated native declaration : :215</i>
     */
    public abstract boolean glyphIsEncoded(int aGlyph);

    /**
     * Original signature : <code>CGFloat defaultLineHeightForFont()</code><br>
     * Use -[NSLayoutManager defaultLineHeightForFont:] instead.<br>
     * <i>from NSFontDeprecated native declaration : :216</i>
     */
    public abstract CGFloat defaultLineHeightForFont();

    /**
     * Original signature : <code>NSString* encodingScheme()</code><br>
     * <i>from NSFontDeprecated native declaration : :219</i>
     */
    public abstract String encodingScheme();

    /**
     * Original signature : <code>NSMultibyteGlyphPacking glyphPacking()</code><br>
     * <i>from NSFontDeprecated native declaration : :220</i>
     */
    public abstract int glyphPacking();

    /**
     * The context-sensitive inter-glyph spacing is now performed at the typesetting stage.<br>
     * Original signature : <code>positionOfGlyph(NSGlyph, NSGlyph, BOOL*)</code><br>
     * <i>from NSFontDeprecated native declaration : :223</i>
     */
    public abstract com.sun.jna.Pointer positionOfGlyph_precededByGlyph_isNominal(int curGlyph, int prevGlyph, boolean nominal);
    /**
     * <i>from NSFontDeprecated native declaration : :224</i><br>
     * Conversion Error : /// Original signature : <code>NSInteger positionsForCompositeSequence(NSGlyph*, NSInteger, null)</code><br>
     * - (NSInteger)positionsForCompositeSequence:(NSGlyph*)someGlyphs numberOfGlyphs:(NSInteger)numGlyphs pointArray:(null)points; (Argument points cannot be converted)
     */
    /**
     * Original signature : <code>positionOfGlyph(NSGlyph, NSGlyph, BOOL*)</code><br>
     * <i>from NSFontDeprecated native declaration : :225</i>
     */
    public abstract com.sun.jna.Pointer positionOfGlyph_struckOverGlyph_metricsExist(int curGlyph, int prevGlyph, boolean exist);
    /**
     * <i>from NSFontDeprecated native declaration : :226</i><br>
     * Conversion Error : /// Original signature : <code>positionOfGlyph(NSGlyph, null, BOOL*)</code><br>
     * - (null)positionOfGlyph:(NSGlyph)aGlyph struckOverRect:(null)aRect metricsExist:(BOOL*)exist; (Argument aRect cannot be converted)
     */
    /**
     * <i>from NSFontDeprecated native declaration : :227</i><br>
     * Conversion Error : /// Original signature : <code>positionOfGlyph(NSGlyph, unichar, null)</code><br>
     * - (null)positionOfGlyph:(NSGlyph)aGlyph forCharacter:(unichar)aChar struckOverRect:(null)aRect; (Argument aRect cannot be converted)
     */
    /**
     * <i>from NSFontDeprecated native declaration : :228</i><br>
     * Conversion Error : /// Original signature : <code>positionOfGlyph(NSGlyph, NSGlyphRelation, NSGlyph, null, BOOL*)</code><br>
     * - (null)positionOfGlyph:(NSGlyph)thisGlyph withRelation:(NSGlyphRelation)rel toBaseGlyph:(NSGlyph)baseGlyph totalAdvancement:(null)adv metricsExist:(BOOL*)exist; (Argument adv cannot be converted)
     */
}
