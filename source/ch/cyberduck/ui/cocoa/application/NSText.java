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

import ch.cyberduck.ui.cocoa.foundation.NSObject;
import ch.cyberduck.ui.cocoa.foundation.NSRange;

import org.rococoa.ID;

// BridgeSupport v 0.017
public abstract class NSText extends NSView {

    /// <i>native declaration : /Users/dkocher/null:15</i>
    public static final int NSEnterCharacter = 3;
    /// <i>native declaration : /Users/dkocher/null:16</i>
    public static final int NSBackspaceCharacter = 8;
    /// <i>native declaration : /Users/dkocher/null:17</i>
    public static final int NSTabCharacter = 9;
    /// <i>native declaration : /Users/dkocher/null:18</i>
    public static final int NSNewlineCharacter = 10;
    /// <i>native declaration : /Users/dkocher/null:19</i>
    public static final int NSFormFeedCharacter = 12;
    /// <i>native declaration : /Users/dkocher/null:20</i>
    public static final int NSCarriageReturnCharacter = 13;
    /// <i>native declaration : /Users/dkocher/null:21</i>
    public static final int NSBackTabCharacter = 25;
    /// <i>native declaration : /Users/dkocher/null:22</i>
    public static final int NSDeleteCharacter = 127;
    /// <i>native declaration : /Users/dkocher/null:23</i>
    public static final int NSLineSeparatorCharacter = 8232;
    /// <i>native declaration : /Users/dkocher/null:24</i>
    public static final int NSParagraphSeparatorCharacter = 8233;
    /**
     * Visually left aligned<br>
     * <i>native declaration : /Users/dkocher/null:29</i>
     */
    public static final int NSLeftTextAlignment = 0;
    /**
     * Visually right aligned<br>
     * <i>native declaration : /Users/dkocher/null:30</i>
     */
    public static final int NSRightTextAlignment = 1;
    /**
     * Visually centered<br>
     * <i>native declaration : /Users/dkocher/null:31</i>
     */
    public static final int NSCenterTextAlignment = 2;
    /**
     * Fully-justified. The last line in a paragraph is natural-aligned.<br>
     * <i>native declaration : /Users/dkocher/null:32</i>
     */
    public static final int NSJustifiedTextAlignment = 3;
    /**
     * Indicates the default alignment for script<br>
     * <i>native declaration : /Users/dkocher/null:33</i>
     */
    public static final int NSNaturalTextAlignment = 4;
    /**
     * Determines direction using the Unicode Bidi Algorithm rules P2 and P3<br>
     * <i>native declaration : /Users/dkocher/null:40</i>
     */
    public static final int NSWritingDirectionNatural = -1;
    /**
     * Left to right writing direction<br>
     * <i>native declaration : /Users/dkocher/null:42</i>
     */
    public static final int NSWritingDirectionLeftToRight = 0;
    /**
     * Right to left writing direction<br>
     * <i>native declaration : /Users/dkocher/null:43</i>
     */
    public static final int NSWritingDirectionRightToLeft = 1;
    /// <i>native declaration : /Users/dkocher/null:50</i>
    public static final int NSIllegalTextMovement = 0;
    /// <i>native declaration : /Users/dkocher/null:51</i>
    public static final int NSReturnTextMovement = 16;
    /// <i>native declaration : /Users/dkocher/null:52</i>
    public static final int NSTabTextMovement = 17;
    /// <i>native declaration : /Users/dkocher/null:53</i>
    public static final int NSBacktabTextMovement = 18;
    /// <i>native declaration : /Users/dkocher/null:54</i>
    public static final int NSLeftTextMovement = 19;
    /// <i>native declaration : /Users/dkocher/null:55</i>
    public static final int NSRightTextMovement = 20;
    /// <i>native declaration : /Users/dkocher/null:56</i>
    public static final int NSUpTextMovement = 21;
    /// <i>native declaration : /Users/dkocher/null:57</i>
    public static final int NSDownTextMovement = 22;
    /// <i>native declaration : /Users/dkocher/null:60</i>
    public static final int NSCancelTextMovement = 23;
    /// <i>native declaration : /Users/dkocher/null:61</i>
    public static final int NSOtherTextMovement = 0;

    public static final String TextDidBeginEditingNotification = "NSTextDidBeginEditingNotification";
    public static final String TextDidEndEditingNotification = "NSTextDidEndEditingNotification";
    public static final String TextDidChangeNotification = "NSTextDidChangeNotification";

    /**
     * Original signature : <code>NSString* string()</code><br>
     * <i>native declaration : /Users/dkocher/null:70</i>
     */
    public abstract String string();

    /**
     * Original signature : <code>void setString(NSString*)</code><br>
     * <i>native declaration : /Users/dkocher/null:71</i>
     */
    public abstract void setString(String string);
    /**
     * <i>native declaration : /Users/dkocher/null:73</i><br>
     * Conversion Error : /// Original signature : <code>void replaceCharactersInRange(null, NSString*)</code><br>
     * - (void)replaceCharactersInRange:(null)range withString:(NSString*)aString; (Argument range cannot be converted)
     */
    /**
     * <i>native declaration : /Users/dkocher/null:74</i><br>
     * Conversion Error : /// Original signature : <code>void replaceCharactersInRange(null, NSData*)</code><br>
     * - (void)replaceCharactersInRange:(null)range withRTF:(NSData*)rtfData; (Argument range cannot be converted)
     */
    /**
     * <i>native declaration : /Users/dkocher/null:75</i><br>
     * Conversion Error : /// Original signature : <code>void replaceCharactersInRange(null, NSData*)</code><br>
     * - (void)replaceCharactersInRange:(null)range withRTFD:(NSData*)rtfdData; (Argument range cannot be converted)
     */
    /**
     * <i>native declaration : /Users/dkocher/null:77</i><br>
     * Conversion Error : /// Original signature : <code>NSData* RTFFromRange(null)</code><br>
     * - (NSData*)RTFFromRange:(null)range; (Argument range cannot be converted)
     */
    /**
     * <i>native declaration : /Users/dkocher/null:78</i><br>
     * Conversion Error : /// Original signature : <code>NSData* RTFDFromRange(null)</code><br>
     * - (NSData*)RTFDFromRange:(null)range; (Argument range cannot be converted)
     */
    /**
     * Original signature : <code>BOOL writeRTFDToFile(NSString*, BOOL)</code><br>
     * <i>native declaration : /Users/dkocher/null:80</i>
     */
    public abstract boolean writeRTFDToFile_atomically(String path, boolean flag);

    /**
     * Original signature : <code>BOOL readRTFDFromFile(NSString*)</code><br>
     * <i>native declaration : /Users/dkocher/null:81</i>
     */
    public abstract boolean readRTFDFromFile(String path);

    /**
     * Original signature : <code>id delegate()</code><br>
     * <i>native declaration : /Users/dkocher/null:83</i>
     */
    public abstract NSObject delegate();

    /**
     * Original signature : <code>void setDelegate(id)</code><br>
     * <i>native declaration : /Users/dkocher/null:84</i>
     */
    public abstract void setDelegate(org.rococoa.ID anObject);

    /**
     * Original signature : <code>BOOL isEditable()</code><br>
     * <i>native declaration : /Users/dkocher/null:86</i>
     */
    public abstract boolean isEditable();

    /**
     * Original signature : <code>void setEditable(BOOL)</code><br>
     * <i>native declaration : /Users/dkocher/null:87</i>
     */
    public abstract void setEditable(boolean flag);

    /**
     * Original signature : <code>BOOL isSelectable()</code><br>
     * <i>native declaration : /Users/dkocher/null:88</i>
     */
    public abstract boolean isSelectable();

    /**
     * Original signature : <code>void setSelectable(BOOL)</code><br>
     * <i>native declaration : /Users/dkocher/null:89</i>
     */
    public abstract void setSelectable(boolean flag);

    /**
     * Original signature : <code>BOOL isRichText()</code><br>
     * <i>native declaration : /Users/dkocher/null:90</i>
     */
    public abstract boolean isRichText();

    /**
     * Original signature : <code>void setRichText(BOOL)</code><br>
     * If NO, also clears setImportsGraphics:<br>
     * <i>native declaration : /Users/dkocher/null:91</i>
     */
    public abstract void setRichText(boolean flag);

    /**
     * Original signature : <code>BOOL importsGraphics()</code><br>
     * <i>native declaration : /Users/dkocher/null:92</i>
     */
    public abstract boolean importsGraphics();

    /**
     * Original signature : <code>void setImportsGraphics(BOOL)</code><br>
     * If YES, also sets setRichText:<br>
     * <i>native declaration : /Users/dkocher/null:93</i>
     */
    public abstract void setImportsGraphics(boolean flag);

    /**
     * Original signature : <code>BOOL isFieldEditor()</code><br>
     * <i>native declaration : /Users/dkocher/null:94</i>
     */
    public abstract boolean isFieldEditor();

    /**
     * Original signature : <code>void setFieldEditor(BOOL)</code><br>
     * Indicates whether to end editing on CR, TAB, etc.<br>
     * <i>native declaration : /Users/dkocher/null:95</i>
     */
    public abstract void setFieldEditor(boolean flag);

    /**
     * Original signature : <code>BOOL usesFontPanel()</code><br>
     * <i>native declaration : /Users/dkocher/null:96</i>
     */
    public abstract boolean usesFontPanel();

    /**
     * Original signature : <code>void setUsesFontPanel(BOOL)</code><br>
     * <i>native declaration : /Users/dkocher/null:97</i>
     */
    public abstract void setUsesFontPanel(boolean flag);

    /**
     * Original signature : <code>BOOL drawsBackground()</code><br>
     * <i>native declaration : /Users/dkocher/null:98</i>
     */
    public abstract boolean drawsBackground();

    /**
     * Original signature : <code>void setDrawsBackground(BOOL)</code><br>
     * <i>native declaration : /Users/dkocher/null:99</i>
     */
    public abstract void setDrawsBackground(boolean flag);

    /**
     * Original signature : <code>NSColor* backgroundColor()</code><br>
     * <i>native declaration : /Users/dkocher/null:100</i>
     */
    public abstract NSColor backgroundColor();

    /**
     * Original signature : <code>void setBackgroundColor(NSColor*)</code><br>
     * <i>native declaration : /Users/dkocher/null:101</i>
     */
    public abstract void setBackgroundColor(NSColor color);

    /**
     * Original signature : <code>BOOL isRulerVisible()</code><br>
     * <i>native declaration : /Users/dkocher/null:103</i>
     */
    public abstract boolean isRulerVisible();

    /**
     * Original signature : <code>selectedRange()</code><br>
     * <i>native declaration : /Users/dkocher/null:105</i>
     */
    public abstract NSObject selectedRange();
    /**
     * <i>native declaration : /Users/dkocher/null:106</i><br>
     * Conversion Error : /// Original signature : <code>void setSelectedRange(null)</code><br>
     * - (void)setSelectedRange:(null)range; (Argument range cannot be converted)
     */
    public abstract void setSelectedRange(NSRange range);
    /**
     * <i>native declaration : /Users/dkocher/null:108</i><br>
     * Conversion Error : /// Original signature : <code>void scrollRangeToVisible(null)</code><br>
     * - (void)scrollRangeToVisible:(null)range; (Argument range cannot be converted)
     */
    public abstract void scrollRangeToVisible(NSRange range);

    /**
     * Original signature : <code>void setFont(NSFont*)</code><br>
     * <i>native declaration : /Users/dkocher/null:110</i>
     */
    public abstract void setFont(NSFont obj);

    /**
     * Original signature : <code>NSFont* font()</code><br>
     * <i>native declaration : /Users/dkocher/null:111</i>
     */
    public abstract NSFont font();

    /**
     * Original signature : <code>void setTextColor(NSColor*)</code><br>
     * <i>native declaration : /Users/dkocher/null:112</i>
     */
    public abstract void setTextColor(NSColor color);

    /**
     * Original signature : <code>NSColor* textColor()</code><br>
     * <i>native declaration : /Users/dkocher/null:113</i>
     */
    public abstract NSColor textColor();

    /**
     * Original signature : <code>NSTextAlignment alignment()</code><br>
     * <i>native declaration : /Users/dkocher/null:114</i>
     */
    public abstract int alignment();

    /**
     * Original signature : <code>void setAlignment(NSTextAlignment)</code><br>
     * <i>native declaration : /Users/dkocher/null:115</i>
     */
    public abstract void setAlignment(int mode);

    /**
     * Original signature : <code>NSWritingDirection baseWritingDirection()</code><br>
     * <i>native declaration : /Users/dkocher/null:117</i>
     */
    public abstract int baseWritingDirection();

    /**
     * Original signature : <code>void setBaseWritingDirection(NSWritingDirection)</code><br>
     * <i>native declaration : /Users/dkocher/null:118</i>
     */
    public abstract void setBaseWritingDirection(int writingDirection);
    /**
     * <i>native declaration : /Users/dkocher/null:121</i><br>
     * Conversion Error : /// Original signature : <code>void setTextColor(NSColor*, null)</code><br>
     * - (void)setTextColor:(NSColor*)color range:(null)range; (Argument range cannot be converted)
     */
    /**
     * <i>native declaration : /Users/dkocher/null:122</i><br>
     * Conversion Error : /// Original signature : <code>void setFont(NSFont*, null)</code><br>
     * - (void)setFont:(NSFont*)font range:(null)range; (Argument range cannot be converted)
     */
    /**
     * Original signature : <code>maxSize()</code><br>
     * <i>native declaration : /Users/dkocher/null:124</i>
     */
    public abstract NSObject maxSize();
    /**
     * <i>native declaration : /Users/dkocher/null:125</i><br>
     * Conversion Error : /// Original signature : <code>void setMaxSize(null)</code><br>
     * - (void)setMaxSize:(null)newMaxSize; (Argument newMaxSize cannot be converted)
     */
    /**
     * Original signature : <code>minSize()</code><br>
     * <i>native declaration : /Users/dkocher/null:126</i>
     */
    public abstract NSObject minSize();
    /**
     * <i>native declaration : /Users/dkocher/null:127</i><br>
     * Conversion Error : /// Original signature : <code>void setMinSize(null)</code><br>
     * - (void)setMinSize:(null)newMinSize; (Argument newMinSize cannot be converted)
     */
    /**
     * Original signature : <code>BOOL isHorizontallyResizable()</code><br>
     * <i>native declaration : /Users/dkocher/null:129</i>
     */
    public abstract boolean isHorizontallyResizable();

    /**
     * Original signature : <code>void setHorizontallyResizable(BOOL)</code><br>
     * <i>native declaration : /Users/dkocher/null:130</i>
     */
    public abstract void setHorizontallyResizable(boolean flag);

    /**
     * Original signature : <code>BOOL isVerticallyResizable()</code><br>
     * <i>native declaration : /Users/dkocher/null:131</i>
     */
    public abstract boolean isVerticallyResizable();

    /**
     * Original signature : <code>void setVerticallyResizable(BOOL)</code><br>
     * <i>native declaration : /Users/dkocher/null:132</i>
     */
    public abstract void setVerticallyResizable(boolean flag);

    /**
     * Original signature : <code>void sizeToFit()</code><br>
     * <i>native declaration : /Users/dkocher/null:134</i>
     */
    public abstract void sizeToFit();

    /**
     * Original signature : <code>void copy(id)</code><br>
     * <i>native declaration : /Users/dkocher/null:136</i>
     */
    public abstract void copy(final ID sender);

    /**
     * Original signature : <code>void copyFont(id)</code><br>
     * <i>native declaration : /Users/dkocher/null:137</i>
     */
    public abstract void copyFont(final ID sender);

    /**
     * Original signature : <code>void copyRuler(id)</code><br>
     * <i>native declaration : /Users/dkocher/null:138</i>
     */
    public abstract void copyRuler(final ID sender);

    /**
     * Original signature : <code>void cut(id)</code><br>
     * <i>native declaration : /Users/dkocher/null:139</i>
     */
    public abstract void cut(final ID sender);

    /**
     * Original signature : <code>void delete(id)</code><br>
     * <i>native declaration : /Users/dkocher/null:140</i>
     */
    public abstract void delete(final ID sender);

    /**
     * Original signature : <code>void paste(id)</code><br>
     * <i>native declaration : /Users/dkocher/null:141</i>
     */
    public abstract void paste(final ID sender);

    /**
     * Original signature : <code>void pasteFont(id)</code><br>
     * <i>native declaration : /Users/dkocher/null:142</i>
     */
    public abstract void pasteFont(final ID sender);

    /**
     * Original signature : <code>void pasteRuler(id)</code><br>
     * <i>native declaration : /Users/dkocher/null:143</i>
     */
    public abstract void pasteRuler(final ID sender);

    /**
     * Original signature : <code>void selectAll(id)</code><br>
     * <i>native declaration : /Users/dkocher/null:144</i>
     */
    public abstract void selectAll(final ID sender);

    /**
     * Original signature : <code>void changeFont(id)</code><br>
     * <i>native declaration : /Users/dkocher/null:145</i>
     */
    public abstract void changeFont(final ID sender);

    /**
     * Original signature : <code>void alignLeft(id)</code><br>
     * <i>native declaration : /Users/dkocher/null:146</i>
     */
    public abstract void alignLeft(final ID sender);

    /**
     * Original signature : <code>void alignRight(id)</code><br>
     * <i>native declaration : /Users/dkocher/null:147</i>
     */
    public abstract void alignRight(final ID sender);

    /**
     * Original signature : <code>void alignCenter(id)</code><br>
     * <i>native declaration : /Users/dkocher/null:148</i>
     */
    public abstract void alignCenter(final ID sender);

    /**
     * Original signature : <code>void subscript(id)</code><br>
     * <i>native declaration : /Users/dkocher/null:149</i>
     */
    public abstract void subscript(final ID sender);

    /**
     * Original signature : <code>void superscript(id)</code><br>
     * <i>native declaration : /Users/dkocher/null:150</i>
     */
    public abstract void superscript(final ID sender);

    /**
     * Original signature : <code>void underline(id)</code><br>
     * <i>native declaration : /Users/dkocher/null:151</i>
     */
    public abstract void underline(final ID sender);

    /**
     * Original signature : <code>void unscript(id)</code><br>
     * <i>native declaration : /Users/dkocher/null:152</i>
     */
    public abstract void unscript(final ID sender);

    /**
     * Original signature : <code>void showGuessPanel(id)</code><br>
     * <i>native declaration : /Users/dkocher/null:153</i>
     */
    public abstract void showGuessPanel(final ID sender);

    /**
     * Original signature : <code>void checkSpelling(id)</code><br>
     * <i>native declaration : /Users/dkocher/null:154</i>
     */
    public abstract void checkSpelling(final ID sender);

    /**
     * Original signature : <code>void toggleRuler(id)</code><br>
     * <i>native declaration : /Users/dkocher/null:155</i>
     */
    public abstract void toggleRuler(final ID sender);

}
