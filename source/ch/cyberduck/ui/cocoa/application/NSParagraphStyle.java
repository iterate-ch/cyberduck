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

import org.rococoa.ObjCClass;
import org.rococoa.cocoa.CGFloat;


/// <i>native declaration : :74</i>
public abstract class NSParagraphStyle extends NSObject implements NSCopying {
    private static final _Class CLASS = org.rococoa.Rococoa.createClass("NSParagraphStyle", _Class.class);

    /// <i>native declaration : :12</i>
    public static final int NSLeftTabStopType = 0;
    /// <i>native declaration : :13</i>
    public static final int NSRightTabStopType = 1;
    /// <i>native declaration : :14</i>
    public static final int NSCenterTabStopType = 2;
    /// <i>native declaration : :15</i>
    public static final int NSDecimalTabStopType = 3;
    /**
     * Wrap at word boundaries, default<br>
     * <i>native declaration : :20</i>
     */
    public static final int NSLineBreakByWordWrapping = 0;
    /**
     * Wrap at character boundaries<br>
     * <i>native declaration : :21</i>
     */
    public static final int NSLineBreakByCharWrapping = 1;
    /**
     * Simply clip<br>
     * <i>native declaration : :22</i>
     */
    public static final int NSLineBreakByClipping = 2;
    /**
     * Truncate at head of line: "...wxyz"<br>
     * <i>native declaration : :23</i>
     */
    public static final int NSLineBreakByTruncatingHead = 3;
    /**
     * Truncate at tail of line: "abcd..."<br>
     * <i>native declaration : :24</i>
     */
    public static final int NSLineBreakByTruncatingTail = 4;
    /**
     * Truncate middle of line:  "ab...yz"<br>
     * <i>native declaration : :25</i>
     */
    public static final int NSLineBreakByTruncatingMiddle = 5;

    public static NSParagraphStyle defaultParagraphStyle() {
        return CLASS.defaultParagraphStyle();
    }

    public interface _Class extends ObjCClass {
        /**
         * Original signature : <code>NSParagraphStyle* defaultParagraphStyle()</code><br>
         * <i>native declaration : :100</i>
         */
        NSParagraphStyle defaultParagraphStyle();

        /**
         * Original signature : <code>defaultWritingDirectionForLanguage(NSString*)</code><br>
         * languageName is in ISO lang region format<br>
         * <i>native declaration : :103</i>
         */
        com.sun.jna.Pointer defaultWritingDirectionForLanguage(com.sun.jna.Pointer languageName);
    }

    /**
     * Original signature : <code>CGFloat lineSpacing()</code><br>
     * "Leading": distance between the bottom of one line fragment and top of next (applied between lines in the same container). Can't be negative. This value is included in the line fragment heights in layout manager.<br>
     * <i>native declaration : :106</i>
     */
    public abstract CGFloat lineSpacing();

    /**
     * Original signature : <code>CGpublic abstract float paragraphSpacing()</code><br>
     * Distance between the bottom of this paragraph and top of next (or the beginning of its paragraphSpacingBefore, if any).<br>
     * <i>native declaration : :107</i>
     */
    public abstract float paragraphSpacing();

    /**
     * Original signature : <code>alignment()</code><br>
     * <i>native declaration : :108</i>
     */
    public abstract com.sun.jna.Pointer alignment();

    /**
     * Original signature : <code>CGpublic abstract float headIndent()</code><br>
     * Distance from margin to front edge of paragraph<br>
     * <i>native declaration : :112</i>
     */
    public abstract float headIndent();

    /**
     * Original signature : <code>CGpublic abstract float tailIndent()</code><br>
     * Distance from margin to back edge of paragraph; if negative or 0, from other margin<br>
     * <i>native declaration : :113</i>
     */
    public abstract float tailIndent();

    /**
     * Original signature : <code>CGpublic abstract float firstLineHeadIndent()</code><br>
     * Distance from margin to edge appropriate for text direction<br>
     * <i>native declaration : :114</i>
     */
    public abstract float firstLineHeadIndent();

    /**
     * Original signature : <code>NSArray* tabStops()</code><br>
     * Distance from margin to tab stops<br>
     * <i>native declaration : :115</i>
     */
    public abstract com.sun.jna.Pointer tabStops();

    /**
     * Original signature : <code>CGpublic abstract float minimumLineHeight()</code><br>
     * Line height is the distance from bottom of descenders to top of ascenders; basically the line fragment height. Does not include lineSpacing (which is added after this computation).<br>
     * <i>native declaration : :117</i>
     */
    public abstract float minimumLineHeight();

    /**
     * Original signature : <code>CGpublic abstract float maximumLineHeight()</code><br>
     * 0 implies no maximum.<br>
     * <i>native declaration : :118</i>
     */
    public abstract float maximumLineHeight();

    /**
     * Original signature : <code>NSLineBreakMode lineBreakMode()</code><br>
     * <i>native declaration : :120</i>
     */
    public abstract int lineBreakMode();

    /**
     * Original signature : <code>baseWritingDirection()</code><br>
     * <i>native declaration : :123</i>
     */
    public abstract com.sun.jna.Pointer baseWritingDirection();

    /**
     * Original signature : <code>CGpublic abstract float lineHeightMultiple()</code><br>
     * Natural line height is multiplied by this factor (if positive) before being constrained by minimum and maximum line height.<br>
     * <i>native declaration : :127</i>
     */
    public abstract float lineHeightMultiple();

    /**
     * Original signature : <code>CGpublic abstract float paragraphSpacingBefore()</code><br>
     * Distance between the bottom of the previous paragraph (or the end of its paragraphSpacing, if any) and the top of this paragraph.<br>
     * <i>native declaration : :128</i>
     */
    public abstract float paragraphSpacingBefore();

    /**
     * Original signature : <code>CGpublic abstract float defaultTabInterval()</code><br>
     * Tabs after the last specified in tabStops are placed at integral multiples of this distance (if positive).<br>
     * <i>native declaration : :129</i>
     */
    public abstract float defaultTabInterval();

    /**
     * Original signature : <code>NSArray* textBlocks()</code><br>
     * Array to specify the text blocks containing the paragraph, nested from outermost to innermost.<br>
     * <i>native declaration : :133</i>
     */
    public abstract com.sun.jna.Pointer textBlocks();

    /**
     * Original signature : <code>NSArray* textLists()</code><br>
     * Array to specify the text lists containing the paragraph, nested from outermost to innermost.<br>
     * <i>native declaration : :134</i>
     */
    public abstract com.sun.jna.Pointer textLists();

    /**
     * Specifies the threshold for hyphenation.  Valid values lie between 0.0 and 1.0 inclusive.  Hyphenation will be attempted when the ratio of the text width as broken without hyphenation to the width of the line fragment is less than the hyphenation factor.  When this takes on its default value of 0.0, the layout manager's hyphenation factor is used instead.  When both are 0.0, hyphenation is disabled.<br>
     * Original signature : <code>public abstract float hyphenationFactor()</code><br>
     * <i>native declaration : :138</i>
     */
    public abstract float hyphenationFactor();

    /**
     * Specifies the threshold for using tightening as an alternative to truncation.  When the line break mode specifies truncation, the text system will attempt to tighten inter-character spacing as an alternative to truncation, provided that the ratio of the text width to the line fragment width does not exceed 1.0 + tighteningFactorForTruncation.  Otherwise the text will be truncated at a location determined by the line break mode.  The default value is 0.05.<br>
     * Original signature : <code>public abstract float tighteningFactorForTruncation()</code><br>
     * <i>native declaration : :142</i>
     */
    public abstract float tighteningFactorForTruncation();

    /**
     * Specifies whether the paragraph is to be treated as a header for purposes of HTML generation.  Should be set to 0 (the default value) if the paragraph is not a header, or from 1 through 6 if the paragraph is to be treated as a header.<br>
     * Original signature : <code>NSInteger headerLevel()</code><br>
     * <i>native declaration : :145</i>
     */
    public abstract int headerLevel();
}
