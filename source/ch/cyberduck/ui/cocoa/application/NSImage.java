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

import ch.cyberduck.ui.cocoa.foundation.*;

import org.rococoa.ObjCClass;
import org.rococoa.cocoa.CGFloat;
import org.rococoa.cocoa.foundation.NSRect;
import org.rococoa.cocoa.foundation.NSSize;


/// <i>native declaration : :41</i>
public abstract class NSImage extends NSObject implements NSCopying {
    private static final _Class CLASS = org.rococoa.Rococoa.createClass("NSImage", _Class.class);

    /// <i>native declaration : :13</i>
    public static final int NSImageLoadStatusCompleted = 0;
    /// <i>native declaration : :14</i>
    public static final int NSImageLoadStatusCancelled = 1;
    /// <i>native declaration : :15</i>
    public static final int NSImageLoadStatusInvalidData = 2;
    /// <i>native declaration : :16</i>
    public static final int NSImageLoadStatusUnexpectedEOF = 3;
    /// <i>native declaration : :17</i>
    public static final int NSImageLoadStatusReadError = 4;
    /**
     * unspecified. use image rep's default<br>
     * <i>native declaration : :22</i>
     */
    public static final int NSImageCacheDefault = 0;
    /**
     * always generate a cache when drawing<br>
     * <i>native declaration : :23</i>
     */
    public static final int NSImageCacheAlways = 1;
    /**
     * cache if cache size is smaller than original data<br>
     * <i>native declaration : :24</i>
     */
    public static final int NSImageCacheBySize = 2;
    /**
     * never cache, always draw direct<br>
     * <i>native declaration : :25</i>
     */
    public static final int NSImageCacheNever = 3;

    public static NSImage imageNamed(String name) {
        return CLASS.imageNamed(name);
    }

    public static NSImage imageWithData(NSData data) {
        return CLASS.alloc().initWithData(data);
    }

    public static NSImage imageWithSize(NSSize size) {
        return CLASS.alloc().initWithSize(size);
    }

    public static NSImage imageWithContentsOfFile(String filename) {
        return CLASS.alloc().initWithContentsOfFile(filename);
    }

    public interface _Class extends ObjCClass {
        /**
         * Original signature : <code>id imageNamed(NSString*)</code><br>
         * If this finds & creates the image, only name is saved when archived<br>
         * <i>native declaration : :73</i>
         */
        NSImage imageNamed(String name);

        /**
         * These return union of all the types registered with NSImageRep.<br>
         * Original signature : <code>NSArray* imageUnfilteredFileTypes()</code><br>
         * <i>native declaration : :138</i>
         */
        NSArray imageUnfilteredFileTypes();

        /**
         * Original signature : <code>NSArray* imageUnfilteredPasteboardTypes()</code><br>
         * <i>native declaration : :139</i>
         */
        NSArray imageUnfilteredPasteboardTypes();

        /**
         * Original signature : <code>NSArray* imageFileTypes()</code><br>
         * <i>native declaration : :140</i>
         */
        NSArray imageFileTypes();

        /**
         * Original signature : <code>NSArray* imagePasteboardTypes()</code><br>
         * <i>native declaration : :141</i>
         */
        NSArray imagePasteboardTypes();

        /**
         * Original signature : <code>NSArray* imageTypes()</code><br>
         * <i>native declaration : :144</i>
         */
        NSArray imageTypes();

        /**
         * Original signature : <code>NSArray* imageUnfilteredTypes()</code><br>
         * <i>native declaration : :145</i>
         */
        NSArray imageUnfilteredTypes();

        /**
         * Original signature : <code>BOOL canInitWithPasteboard(NSPasteboard*)</code><br>
         * <i>native declaration : :148</i>
         */
        boolean canInitWithPasteboard(NSPasteboard pasteBoard);

        NSImage alloc();
    }

    /**
     * <i>native declaration : :75</i><br>
     * Conversion Error : NSSize
     */
    public abstract NSImage initWithSize(NSSize aSize);

    /**
     * Original signature : <code>id initWithData(NSData*)</code><br>
     * When archived, saves contents<br>
     * <i>native declaration : :76</i>
     */
    public abstract NSImage initWithData(NSData data);

    /**
     * Original signature : <code>id initWithContentsOfFile(NSString*)</code><br>
     * When archived, saves contents<br>
     * <i>native declaration : :77</i>
     */
    public abstract NSImage initWithContentsOfFile(String fileName);

    /**
     * Original signature : <code>id initWithContentsOfURL(NSURL*)</code><br>
     * When archived, saves contents<br>
     * <i>native declaration : :78</i>
     */
    public abstract NSImage initWithContentsOfURL(NSURL url);

    /**
     * Original signature : <code>id initByReferencingFile(NSString*)</code><br>
     * When archived, saves fileName<br>
     * <i>native declaration : :79</i>
     */
    public abstract NSImage initByReferencingFile(String fileName);

    /**
     * Original signature : <code>id initByReferencingURL(NSURL*)</code><br>
     * When archived, saves url, supports progressive loading<br>
     * <i>native declaration : :81</i>
     */
    public abstract NSImage initByReferencingURL(NSURL url);
    /**
     * <i>native declaration : :84</i><br>
     * Conversion Error : /// Original signature : <code>id initWithIconRef(null)</code><br>
     * - (id)initWithIconRef:(null)iconRef; (Argument iconRef cannot be converted)
     */
    /**
     * Original signature : <code>id initWithPasteboard(NSPasteboard*)</code><br>
     * <i>native declaration : :86</i>
     */
    public abstract NSImage initWithPasteboard(NSPasteboard pasteboard);

    /**
     * <i>native declaration : :88</i><br>
     * Conversion Error : NSSize
     */
    public abstract void setSize(NSSize aSize);

    /**
     * <i>native declaration : :89</i><br>
     * Conversion Error : NSSize
     */
    public abstract NSSize size();

    /**
     * Original signature : <code>BOOL setName(NSString*)</code><br>
     * <i>native declaration : :90</i>
     */
    public abstract boolean setName(String string);

    /**
     * Original signature : <code>NSString* name()</code><br>
     * <i>native declaration : :91</i>
     */
    public abstract String name();

    /**
     * Original signature : <code>void setScalesWhenResized(BOOL)</code><br>
     * <i>native declaration : :92</i>
     */
    public abstract void setScalesWhenResized(boolean flag);

    /**
     * Original signature : <code>BOOL scalesWhenResized()</code><br>
     * <i>native declaration : :93</i>
     */
    public abstract boolean scalesWhenResized();

    /**
     * Original signature : <code>void setDataRetained(BOOL)</code><br>
     * <i>native declaration : :94</i>
     */
    public abstract void setDataRetained(boolean flag);

    /**
     * Original signature : <code>BOOL isDataRetained()</code><br>
     * <i>native declaration : :95</i>
     */
    public abstract boolean isDataRetained();

    /**
     * Original signature : <code>void setCachedSeparately(BOOL)</code><br>
     * <i>native declaration : :96</i>
     */
    public abstract void setCachedSeparately(boolean flag);

    /**
     * Original signature : <code>BOOL isCachedSeparately()</code><br>
     * <i>native declaration : :97</i>
     */
    public abstract boolean isCachedSeparately();

    /**
     * Original signature : <code>void setCacheDepthMatchesImageDepth(BOOL)</code><br>
     * <i>native declaration : :98</i>
     */
    public abstract void setCacheDepthMatchesImageDepth(boolean flag);

    /**
     * Original signature : <code>BOOL cacheDepthMatchesImageDepth()</code><br>
     * <i>native declaration : :99</i>
     */
    public abstract boolean cacheDepthMatchesImageDepth();

    /**
     * Original signature : <code>public abstract  void setBackgroundColor(NSColor*)</code><br>
     * <i>native declaration : :100</i>
     */
    public abstract void setBackgroundColor(com.sun.jna.Pointer aColor);

    /**
     * Original signature : <code>NSColor* backgroundColor()</code><br>
     * <i>native declaration : :101</i>
     */
    public abstract NSColor backgroundColor();

    /**
     * Original signature : <code>public abstract  void setUsesEPSOnResolutionMismatch(BOOL)</code><br>
     * <i>native declaration : :102</i>
     */
    public abstract void setUsesEPSOnResolutionMismatch(boolean flag);

    /**
     * Original signature : <code>BOOL usesEPSOnResolutionMismatch()</code><br>
     * <i>native declaration : :103</i>
     */
    public abstract boolean usesEPSOnResolutionMismatch();

    /**
     * Original signature : <code>public abstract  void setPrefersColorMatch(BOOL)</code><br>
     * <i>native declaration : :104</i>
     */
    public abstract void setPrefersColorMatch(boolean flag);

    /**
     * Original signature : <code>BOOL prefersColorMatch()</code><br>
     * <i>native declaration : :105</i>
     */
    public abstract boolean prefersColorMatch();

    /**
     * Original signature : <code>public abstract  void setMatchesOnMultipleResolution(BOOL)</code><br>
     * <i>native declaration : :106</i>
     */
    public abstract void setMatchesOnMultipleResolution(boolean flag);

    /**
     * Original signature : <code>BOOL matchesOnMultipleResolution()</code><br>
     * <i>native declaration : :107</i>
     */
    public abstract boolean matchesOnMultipleResolution();
    /**
     * <i>native declaration : :108</i><br>
     * Conversion Error : /// Original signature : <code>public abstract  void dissolveToPoint(null, CGFloat)</code><br>
     * - (void)dissolveToPoint:(null)point fraction:(CGFloat)aFloat; (Argument point cannot be converted)
     */
    /**
     * <i>native declaration : :109</i><br>
     * Conversion Error : /// Original signature : <code>public abstract  void dissolveToPoint(null, null, CGFloat)</code><br>
     * - (void)dissolveToPoint:(null)point fromRect:(null)rect fraction:(CGFloat)aFloat; (Argument point cannot be converted)
     */
    /**
     * <i>native declaration : :110</i><br>
     * Conversion Error : /// Original signature : <code>public abstract  void compositeToPoint(null, null)</code><br>
     * - (void)compositeToPoint:(null)point operation:(null)op; (Argument point cannot be converted)
     */
    /**
     * <i>native declaration : :111</i><br>
     * Conversion Error : /// Original signature : <code>public abstract  void compositeToPoint(null, null, null)</code><br>
     * - (void)compositeToPoint:(null)point fromRect:(null)rect operation:(null)op; (Argument point cannot be converted)
     */
    /**
     * <i>native declaration : :112</i><br>
     * Conversion Error : /// Original signature : <code>public abstract  void compositeToPoint(null, null, CGFloat)</code><br>
     * - (void)compositeToPoint:(null)point operation:(null)op fraction:(CGFloat)delta; (Argument point cannot be converted)
     */
    /**
     * <i>native declaration : :113</i><br>
     * Conversion Error : /// Original signature : <code>public abstract  void compositeToPoint(null, null, null, CGFloat)</code><br>
     * - (void)compositeToPoint:(null)point fromRect:(null)rect operation:(null)op fraction:(CGFloat)delta; (Argument point cannot be converted)
     */
    /**
     * <i>native declaration : :114</i><br>
     * Conversion Error : /// Original signature : <code>public abstract  void drawAtPoint(null, null, null, CGFloat)</code><br>
     * - (void)drawAtPoint:(null)point fromRect:(null)fromRect operation:(null)op fraction:(CGFloat)delta; (Argument point cannot be converted)
     */
    /**
     * <i>native declaration : :115</i><br>
     * Conversion Error : /// Original signature : <code>public abstract  void drawInRect(null, null, null, CGFloat)</code><br>
     * - (void)drawInRect:(null)rect fromRect:(null)fromRect operation:(null)op fraction:(CGFloat)delta; (Argument rect cannot be converted)
     */
    public abstract void drawInRect_fromRect_operation_fraction(NSRect rect, NSRect fromRect, int operation, CGFloat delta);

    public void drawInRect(NSRect rect, NSRect fromRect, int operation, double delta) {
        this.drawInRect_fromRect_operation_fraction(rect, fromRect, operation, new CGFloat(delta));
    }

    /**
     * <i>native declaration : :116</i><br>
     * Conversion Error : /// Original signature : <code>BOOL drawRepresentation(NSImageRep*, null)</code><br>
     * - (BOOL)drawRepresentation:(NSImageRep*)imageRep inRect:(null)rect; (Argument rect cannot be converted)
     */
    /**
     * Original signature : <code>public abstract  void recache()</code><br>
     * <i>native declaration : :117</i>
     */
    public abstract void recache();

    /**
     * Original signature : <code>NSData* TIFFRepresentation()</code><br>
     * <i>native declaration : :118</i>
     */
    public abstract com.sun.jna.Pointer TIFFRepresentation();
    /**
     * <i>native declaration : :119</i><br>
     * Conversion Error : /// Original signature : <code>NSData* TIFFRepresentationUsingCompression(null, float)</code><br>
     * - (NSData*)TIFFRepresentationUsingCompression:(null)comp factor:(float)aFloat; (Argument comp cannot be converted)
     */
    /**
     * Original signature : <code>NSArray* representations()</code><br>
     * <i>native declaration : :121</i>
     */
    public abstract com.sun.jna.Pointer representations();

    /**
     * Original signature : <code>public abstract  void addRepresentations(NSArray*)</code><br>
     * <i>native declaration : :122</i>
     */
    public abstract void addRepresentations(NSArray imageReps);

    /**
     * Original signature : <code>public abstract  void addRepresentation(NSImageRep*)</code><br>
     * <i>native declaration : :123</i>
     */
    public abstract void addRepresentation(com.sun.jna.Pointer imageRep);

    /**
     * Original signature : <code>public abstract  void removeRepresentation(NSImageRep*)</code><br>
     * <i>native declaration : :124</i>
     */
    public abstract void removeRepresentation(com.sun.jna.Pointer imageRep);

    /**
     * Original signature : <code>BOOL isValid()</code><br>
     * <i>native declaration : :126</i>
     */
    public abstract boolean isValid();

    /**
     * Original signature : <code>public abstract  void lockFocus()</code><br>
     * <i>native declaration : :127</i>
     */
    public abstract void lockFocus();

    /**
     * Original signature : <code>public abstract  void lockFocusOnRepresentation(NSImageRep*)</code><br>
     * <i>native declaration : :128</i>
     */
    public abstract void lockFocusOnRepresentation(com.sun.jna.Pointer imageRepresentation);

    /**
     * Original signature : <code>public abstract  void unlockFocus()</code><br>
     * <i>native declaration : :129</i>
     */
    public abstract void unlockFocus();

    /**
     * Original signature : <code>NSImageRep* bestRepresentationForDevice(NSDictionary*)</code><br>
     * <i>native declaration : :131</i>
     */
    public abstract com.sun.jna.Pointer bestRepresentationForDevice(NSDictionary deviceDescription);

    /**
     * Original signature : <code>public abstract  void setDelegate(id)</code><br>
     * <i>native declaration : :133</i>
     */
    public abstract void setDelegate(org.rococoa.ID anObject);

    /**
     * Original signature : <code>id delegate()</code><br>
     * <i>native declaration : :134</i>
     */
    public abstract org.rococoa.ID delegate();

    /**
     * Original signature : <code>public abstract  void setFlipped(BOOL)</code><br>
     * <i>native declaration : :150</i>
     */
    public abstract void setFlipped(boolean flag);

    /**
     * Original signature : <code>BOOL isFlipped()</code><br>
     * <i>native declaration : :151</i>
     */
    public abstract boolean isFlipped();

    /**
     * Original signature : <code>public abstract  void cancelIncrementalLoad()</code><br>
     * <i>native declaration : :154</i>
     */
    public abstract void cancelIncrementalLoad();

    /**
     * Original signature : <code>public abstract  void setCacheMode(NSImageCacheMode)</code><br>
     * <i>native declaration : :156</i>
     */
    public abstract void setCacheMode(int mode);

    /**
     * Original signature : <code>NSImageCacheMode cacheMode()</code><br>
     * <i>native declaration : :157</i>
     */
    public abstract int cacheMode();

    /**
     * The alignmentRect of an image is metadata that a client may use to help determine layout. The bottom of the rect gives the baseline of the image. The other edges give similar information in other directions.<br>
     * A 20x20 image of a phone icon with a glow might specify an alignmentRect of {{2,2},{16,16}} that excludes the glow. NSButtonCell can take advantage of the alignmentRect to place the image in the same visual location as an 16x16 phone icon without the glow. A 5x5 star that should render high when aligned with text might specify a rect of {{0,-7},{5,12}}.<br>
     * The alignmentRect of an image has no effect on methods such as drawInRect:fromRect:operation:Fraction: or drawAtPoint:fromRect:operation:fraction:. It is the client's responsibility to take the alignmentRect into account where applicable.<br>
     * The default alignmentRect of an image is {{0,0},imageSize}. The rect is adjusted when setSize: is called.<br>
     * Original signature : <code>alignmentRect()</code><br>
     * <i>native declaration : :169</i>
     */
    public abstract NSObject alignmentRect();
    /**
     * <i>native declaration : :170</i><br>
     * Conversion Error : /// Original signature : <code>public abstract  void setAlignmentRect(null)</code><br>
     * - (void)setAlignmentRect:(null)rect; (Argument rect cannot be converted)
     */
    /**
     * The 'template' property is metadata that allows clients to be smarter about image processing.  An image should be marked as a template if it is basic glpyh-like black and white art that is intended to be processed into derived images for use on screen.<br>
     * NSButtonCell applies effects to images based on the state of the button.  For example, images are shaded darker when the button is pressed.  If a template image is set on a cell, the cell can apply more sophisticated effects.  For example, it may be processed into an image that looks engraved when drawn into a cell whose interiorBackgroundStyle is NSBackgroundStyleRaised, like on a textured button.<br>
     * Original signature : <code>BOOL isTemplate()</code><br>
     * <i>native declaration : :176</i>
     */
    public abstract boolean isTemplate();

    /**
     * Original signature : <code>public abstract  void setTemplate(BOOL)</code><br>
     * <i>native declaration : :177</i>
     */
    public abstract void setTemplate(boolean isTemplate);
}
