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
import ch.cyberduck.ui.cocoa.foundation.NSAttributedString;
import ch.cyberduck.ui.cocoa.foundation.NSObject;

import org.rococoa.ObjCClass;
import org.rococoa.cocoa.CGFloat;

/// <i>native declaration : :71</i>
public abstract class NSLayoutManager extends NSObject {
    private static final _Class CLASS = org.rococoa.Rococoa.createClass("NSLayoutManager", _Class.class);

    public static NSLayoutManager layoutManager() {
        return CLASS.alloc().init();
    }

    /// <i>native declaration : :36</i>
    public static final int NSGlyphAttributeSoft = 0;
    /// <i>native declaration : :37</i>
    public static final int NSGlyphAttributeElastic = 1;
    /// <i>native declaration : :39</i>
    public static final int NSGlyphAttributeBidiLevel = 2;
    /// <i>native declaration : :41</i>
    public static final int NSGlyphAttributeInscribe = 5;
    /// <i>native declaration : :46</i>
    public static final int NSGlyphInscribeBase = 0;
    /// <i>native declaration : :47</i>
    public static final int NSGlyphInscribeBelow = 1;
    /// <i>native declaration : :48</i>
    public static final int NSGlyphInscribeAbove = 2;
    /// <i>native declaration : :49</i>
    public static final int NSGlyphInscribeOverstrike = 3;
    /// <i>native declaration : :50</i>
    public static final int NSGlyphInscribeOverBelow = 4;
    /// <i>native declaration : :57</i>
    public static final int NSTypesetterLatestBehavior = -1;
    /**
     * Mac OS X versions 10.0 and 10.1 (uses NSSimpleHorizontalTypesetter)<br>
     * <i>native declaration : :58</i>
     */
    public static final int NSTypesetterOriginalBehavior = 0;
    /**
     * 10.2 with backward compatibility layout (uses new ATS-based typestter)<br>
     * <i>native declaration : :59</i>
     */
    public static final int NSTypesetterBehavior_10_2_WithCompatibility = 1;
    /// <i>native declaration : :60</i>
    public static final int NSTypesetterBehavior_10_2 = 2;
    /// <i>native declaration : :61</i>
    public static final int NSTypesetterBehavior_10_3 = 3;
    /// <i>native declaration : :64</i>
    public static final int NSTypesetterBehavior_10_4 = 4;

    public static interface Delegate {
        void layoutManager_didCompleteLayoutForTextContainer_atEnd(NSLayoutManager layoutManager,
                                                                   NSObject textContainer,
                                                                   boolean finished);
    }

    public interface _Class extends ObjCClass {
        NSLayoutManager alloc();
    }

    /**
     * Original signature : <code>id init()</code><br>
     * <i>native declaration : :179</i>
     */
    public abstract NSLayoutManager init();

    /**
     * Original signature : <code>NSTextStorage* textStorage()</code><br>
     * <i>native declaration : :184</i>
     */
    public abstract NSTextStorage textStorage();

    /**
     * Original signature : <code>void setTextStorage(NSTextStorage*)</code><br>
     * <i>native declaration : :185</i>
     */
    public abstract void setTextStorage(NSTextStorage textStorage);

    /**
     * Original signature : <code>NSAttributedString* attributedString()</code><br>
     * <i>native declaration : :188</i>
     */
    public abstract NSAttributedString attributedString();

    /**
     * Original signature : <code>void replaceTextStorage(NSTextStorage*)</code><br>
     * <i>native declaration : :191</i>
     */
    public abstract void replaceTextStorage(NSTextStorage newTextStorage);

    /**
     * Original signature : <code>NSGlyphGenerator* glyphGenerator()</code><br>
     * <i>native declaration : :195</i>
     */
    public abstract com.sun.jna.Pointer glyphGenerator();

    /**
     * Original signature : <code>void setGlyphGenerator(NSGlyphGenerator*)</code><br>
     * <i>native declaration : :196</i>
     */
    public abstract void setGlyphGenerator(com.sun.jna.Pointer glyphGenerator);

    /**
     * Original signature : <code>NSTypesetter* typesetter()</code><br>
     * <i>native declaration : :200</i>
     */
    public abstract com.sun.jna.Pointer typesetter();

    /**
     * Original signature : <code>void setTypesetter(NSTypesetter*)</code><br>
     * <i>native declaration : :201</i>
     */
    public abstract void setTypesetter(com.sun.jna.Pointer typesetter);

    /**
     * Original signature : <code>id delegate()</code><br>
     * <i>native declaration : :204</i>
     */
    public abstract org.rococoa.ID delegate();

    /**
     * Original signature : <code>void setDelegate(id)</code><br>
     * <i>native declaration : :205</i>
     */
    public abstract void setDelegate(org.rococoa.ID delegate);

    /**
     * Original signature : <code>NSArray* textContainers()</code><br>
     * <i>native declaration : :210</i>
     */
    public abstract NSArray textContainers();

    /**
     * Original signature : <code>void addTextContainer(NSTextContainer*)</code><br>
     * <i>native declaration : :212</i>
     */
    public abstract void addTextContainer(com.sun.jna.Pointer container);

    /**
     * Add a container to the end of the array.  Must invalidate layout of all glyphs after the previous last container (i.e., glyphs that were not previously laid out because they would not fit anywhere).<br>
     * Original signature : <code>void insertTextContainer(NSTextContainer*, NSUInteger)</code><br>
     * <i>native declaration : :214</i>
     */
    public abstract void insertTextContainer_atIndex(com.sun.jna.Pointer container, int index);

    /**
     * Insert a container into the array before the container at index.  Must invalidate layout of all glyphs in the containers from the one previously at index to the last container.<br>
     * Original signature : <code>void removeTextContainerAtIndex(NSUInteger)</code><br>
     * <i>native declaration : :216</i>
     */
    public abstract void removeTextContainerAtIndex(int index);

    /**
     * Original signature : <code>void textContainerChangedGeometry(NSTextContainer*)</code><br>
     * <i>native declaration : :219</i>
     */
    public abstract void textContainerChangedGeometry(com.sun.jna.Pointer container);

    /**
     * Original signature : <code>void textContainerChangedTextView(NSTextContainer*)</code><br>
     * <i>native declaration : :222</i>
     */
    public abstract void textContainerChangedTextView(com.sun.jna.Pointer container);

    /**
     * Original signature : <code>void setBackgroundLayoutEnabled(BOOL)</code><br>
     * <i>native declaration : :227</i>
     */
    public abstract void setBackgroundLayoutEnabled(boolean flag);

    /**
     * Original signature : <code>BOOL backgroundLayoutEnabled()</code><br>
     * <i>native declaration : :228</i>
     */
    public abstract boolean backgroundLayoutEnabled();

    /**
     * Original signature : <code>void setUsesScreenFonts(BOOL)</code><br>
     * <i>native declaration : :231</i>
     */
    public abstract void setUsesScreenFonts(boolean flag);

    /**
     * Original signature : <code>BOOL usesScreenFonts()</code><br>
     * <i>native declaration : :232</i>
     */
    public abstract boolean usesScreenFonts();

    /**
     * Original signature : <code>void setShowsInvisibleCharacters(BOOL)</code><br>
     * <i>native declaration : :235</i>
     */
    public abstract void setShowsInvisibleCharacters(boolean flag);

    /**
     * Original signature : <code>BOOL showsInvisibleCharacters()</code><br>
     * <i>native declaration : :236</i>
     */
    public abstract boolean showsInvisibleCharacters();

    /**
     * Original signature : <code>void setShowsControlCharacters(BOOL)</code><br>
     * <i>native declaration : :239</i>
     */
    public abstract void setShowsControlCharacters(boolean flag);

    /**
     * Original signature : <code>BOOL showsControlCharacters()</code><br>
     * <i>native declaration : :240</i>
     */
    public abstract boolean showsControlCharacters();

    /**
     * Original signature : <code>void setHyphenationFactor(float)</code><br>
     * <i>native declaration : :243</i>
     */
    public abstract void setHyphenationFactor(float factor);

    /**
     * Original signature : <code>float hyphenationFactor()</code><br>
     * <i>native declaration : :244</i>
     */
    public abstract float hyphenationFactor();
    /**
     * <i>native declaration : :247</i><br>
     * Conversion Error : /// Original signature : <code>void setDefaultAttachmentScaling(null)</code><br>
     * - (void)setDefaultAttachmentScaling:(null)scaling; (Argument scaling cannot be converted)
     */
    /**
     * Original signature : <code>defaultAttachmentScaling()</code><br>
     * <i>native declaration : :248</i>
     */
    public abstract NSObject defaultAttachmentScaling();

    /**
     * Original signature : <code>void setTypesetterBehavior(NSTypesetterBehavior)</code><br>
     * <i>native declaration : :252</i>
     */
    public abstract void setTypesetterBehavior(int theBehavior);

    /**
     * Original signature : <code>NSTypesetterBehavior typesetterBehavior()</code><br>
     * <i>native declaration : :253</i>
     */
    public abstract int typesetterBehavior();

    /**
     * Original signature : <code>NSUInteger layoutOptions()</code><br>
     * <i>native declaration : :258</i>
     */
    public abstract int layoutOptions();

    /**
     * Original signature : <code>void setAllowsNonContiguousLayout(BOOL)</code><br>
     * <i>native declaration : :263</i>
     */
    public abstract void setAllowsNonContiguousLayout(boolean flag);

    /**
     * Original signature : <code>BOOL allowsNonContiguousLayout()</code><br>
     * <i>native declaration : :264</i>
     */
    public abstract boolean allowsNonContiguousLayout();

    /**
     * If YES, then the layout manager may perform glyph generation and layout for a given portion of the text, without having glyphs or layout for preceding portions.  The default is NO.  Turning this setting on will significantly alter which portions of the text will have glyph generation or layout performed when a given generation-causing method is invoked.  It also gives significant performance benefits, especially for large documents.<br>
     * Original signature : <code>BOOL hasNonContiguousLayout()</code><br>
     * <i>native declaration : :266</i>
     */
    public abstract boolean hasNonContiguousLayout();
    /**
     * <i>native declaration : :272</i><br>
     * Conversion Error : NSRange
     */
    /**
     * <i>native declaration : :276</i><br>
     * Conversion Error : NSRange
     */
    /**
     * <i>native declaration : :278</i><br>
     * Conversion Error : NSRange
     */
    /**
     * <i>native declaration : :281</i><br>
     * Conversion Error : NSRange
     */
    /**
     * <i>native declaration : :282</i><br>
     * Conversion Error : NSRange
     */
    /**
     * <i>native declaration : :285</i><br>
     * Conversion Error : NSRange
     */
    /**
     * <i>native declaration : :291</i><br>
     * Conversion Error : NSRange
     */
    /**
     * <i>native declaration : :292</i><br>
     * Conversion Error : NSRange
     */
    /**
     * <i>native declaration : :293</i><br>
     * Conversion Error : NSRange
     */
    /**
     * <i>native declaration : :294</i><br>
     * Conversion Error : NSRange
     */
    /**
     * Original signature : <code>void ensureLayoutForTextContainer(NSTextContainer*)</code><br>
     * <i>native declaration : :295</i>
     */
    public abstract void ensureLayoutForTextContainer(com.sun.jna.Pointer container);
    /**
     * <i>native declaration : :296</i><br>
     * Conversion Error : NSRect
     */
    /**
     * Original signature : <code>void insertGlyphs(const NSGlyph*, NSUInteger, NSUInteger, NSUInteger)</code><br>
     * <i>native declaration : :305</i>
     */
    public abstract void insertGlyphs_length_forStartingGlyphAtIndex_characterIndex(com.sun.jna.Pointer glyphs, int length, int glyphIndex, int charIndex);
    /**
     * <i>native declaration : :309</i><br>
     * Conversion Error : NSGlyph
     */
    /**
     * <i>native declaration : :312</i><br>
     * Conversion Error : NSGlyph
     */
    /**
     * <i>native declaration : :315</i><br>
     * Conversion Error : NSRange
     */
    /**
     * Original signature : <code>void setCharacterIndex(NSUInteger, NSUInteger)</code><br>
     * <i>native declaration : :318</i>
     */
    public abstract void setCharacterIndex_forGlyphAtIndex(int charIndex, int glyphIndex);

    /**
     * Original signature : <code>void setIntAttribute(NSInteger, NSInteger, NSUInteger)</code><br>
     * <i>native declaration : :321</i>
     */
    public abstract void setIntAttribute_value_forGlyphAtIndex(int attributeTag, int val, int glyphIndex);
    /**
     * <i>native declaration : :325</i><br>
     * Conversion Error : NSRange
     */
    /**
     * Original signature : <code>NSUInteger numberOfGlyphs()</code><br>
     * <i>native declaration : :331</i>
     */
    public abstract int numberOfGlyphs();
    /**
     * <i>native declaration : :334</i><br>
     * Conversion Error : NSGlyph
     */
    /**
     * <i>native declaration : :335</i><br>
     * Conversion Error : NSGlyph
     */
    /**
     * Original signature : <code>BOOL isValidGlyphIndex(NSUInteger)</code><br>
     * <i>native declaration : :336</i>
     */
    public abstract boolean isValidGlyphIndex(int glyphIndex);

    /**
     * Original signature : <code>NSUInteger characterIndexForGlyphAtIndex(NSUInteger)</code><br>
     * <i>native declaration : :340</i>
     */
    public abstract int characterIndexForGlyphAtIndex(int glyphIndex);

    /**
     * Original signature : <code>NSUInteger glyphIndexForCharacterAtIndex(NSUInteger)</code><br>
     * <i>native declaration : :344</i>
     */
    public abstract int glyphIndexForCharacterAtIndex(int charIndex);

    /**
     * Original signature : <code>NSInteger intAttribute(NSInteger, NSUInteger)</code><br>
     * <i>native declaration : :348</i>
     */
    public abstract int intAttribute_forGlyphAtIndex(int attributeTag, int glyphIndex);
    /**
     * <i>native declaration : :351</i><br>
     * Conversion Error : NSRange
     */
    /**
     * <i>native declaration : :353</i><br>
     * Conversion Error : NSRange
     */
    /**
     * <i>native declaration : :357</i><br>
     * Conversion Error : NSRange
     */
    /**
     * <i>native declaration : :364</i><br>
     * Conversion Error : NSRange
     */
    /**
     * <i>native declaration : :367</i><br>
     * Conversion Error : NSRect
     */
    /**
     * <i>native declaration : :370</i><br>
     * Conversion Error : NSRect
     */
    /**
     * <i>native declaration : :373</i><br>
     * Conversion Error : NSPoint
     */
    /**
     * <i>native declaration : :377</i><br>
     * Conversion Error : /// Original signature : <code>void setLocations(null, NSUInteger*, NSUInteger, NSRange)</code><br>
     * - (void)setLocations:(null)locations startingGlyphIndexes:(NSUInteger*)glyphIndexes count:(NSUInteger)count forGlyphRange:(NSRange)glyphRange; (Argument locations cannot be converted)
     */
    /**
     * Original signature : <code>void setNotShownAttribute(BOOL, NSUInteger)</code><br>
     * <i>native declaration : :381</i>
     */
    public abstract void setNotShownAttribute_forGlyphAtIndex(boolean flag, int glyphIndex);

    /**
     * Original signature : <code>void setDrawsOutsideLineFragment(BOOL, NSUInteger)</code><br>
     * <i>native declaration : :384</i>
     */
    public abstract void setDrawsOutsideLineFragment_forGlyphAtIndex(boolean flag, int glyphIndex);
    /**
     * <i>native declaration : :387</i><br>
     * Conversion Error : /// Original signature : <code>void setAttachmentSize(null, NSRange)</code><br>
     * - (void)setAttachmentSize:(null)attachmentSize forGlyphRange:(NSRange)glyphRange; (Argument attachmentSize cannot be converted)
     */

    /**
     * Original signature : <code>void getFirstUnlaidCharacterIndex(NSUInteger*, NSUInteger*)</code><br>
     * <i>native declaration : :394</i>
     */
    public abstract void getFirstUnlaidCharacterIndex_glyphIndex(java.nio.IntBuffer charIndex, java.nio.IntBuffer glyphIndex);

    /**
     * Original signature : <code>NSUInteger firstUnlaidCharacterIndex()</code><br>
     * <i>native declaration : :395</i>
     */
    public abstract int firstUnlaidCharacterIndex();

    /**
     * Original signature : <code>NSUInteger firstUnlaidGlyphIndex()</code><br>
     * <i>native declaration : :396</i>
     */
    public abstract int firstUnlaidGlyphIndex();
    /**
     * <i>native declaration : :402</i><br>
     * Conversion Error : /// Original signature : <code>NSTextContainer* textContainerForGlyphAtIndex(NSUInteger, null)</code><br>
     * - (NSTextContainer*)textContainerForGlyphAtIndex:(NSUInteger)glyphIndex effectiveRange:(null)effectiveGlyphRange; (Argument effectiveGlyphRange cannot be converted)
     */
    /**
     * <i>native declaration : :405</i><br>
     * Conversion Error : NSRect
     */
    /**
     * <i>native declaration : :408</i><br>
     * Conversion Error : NSRect
     */
    /**
     * <i>native declaration : :411</i><br>
     * Conversion Error : NSRect
     */
    /**
     * <i>native declaration : :415</i><br>
     * Conversion Error : NSRect
     */
    /**
     * <i>native declaration : :416</i><br>
     * Conversion Error : NSRect
     */
    /**
     * <i>native declaration : :417</i><br>
     * Conversion Error : /// Original signature : <code>NSTextContainer* textContainerForGlyphAtIndex(NSUInteger, null, BOOL)</code><br>
     * - (NSTextContainer*)textContainerForGlyphAtIndex:(NSUInteger)glyphIndex effectiveRange:(null)effectiveGlyphRange withoutAdditionalLayout:(BOOL)flag; (Argument effectiveGlyphRange cannot be converted)
     */
    /**
     * <i>native declaration : :421</i><br>
     * Conversion Error : NSRect
     */
    /**
     * <i>native declaration : :422</i><br>
     * Conversion Error : NSRect
     */
    /**
     * Original signature : <code>NSTextContainer* extraLineFragmentTextContainer()</code><br>
     * <i>native declaration : :423</i>
     */
    public abstract com.sun.jna.Pointer extraLineFragmentTextContainer();
    /**
     * <i>native declaration : :426</i><br>
     * Conversion Error : NSPoint
     */
    /**
     * Original signature : <code>BOOL notShownAttributeForGlyphAtIndex(NSUInteger)</code><br>
     * <i>native declaration : :429</i>
     */
    public abstract boolean notShownAttributeForGlyphAtIndex(int glyphIndex);

    /**
     * Original signature : <code>BOOL drawsOutsideLineFragmentForGlyphAtIndex(NSUInteger)</code><br>
     * <i>native declaration : :432</i>
     */
    public abstract boolean drawsOutsideLineFragmentForGlyphAtIndex(int glyphIndex);

    /**
     * Original signature : <code>attachmentSizeForGlyphAtIndex(NSUInteger)</code><br>
     * <i>native declaration : :435</i>
     */
    public abstract NSObject attachmentSizeForGlyphAtIndex(int glyphIndex);
    /**
     * <i>native declaration : :441</i><br>
     * Conversion Error : NSRect
     */
    /**
     * <i>native declaration : :442</i><br>
     * Conversion Error : NSRect
     */
    /**
     * <i>native declaration : :443</i><br>
     * Conversion Error : NSRect
     */
    /**
     * <i>native declaration : :444</i><br>
     * Conversion Error : NSRect
     */
    /**
     * <i>native declaration : :446</i><br>
     * Conversion Error : NSRect
     */
    /**
     * <i>native declaration : :447</i><br>
     * Conversion Error : NSRect
     */
    /**
     * <i>native declaration : :455</i><br>
     * Conversion Error : NSRange
     */
    /**
     * <i>native declaration : :458</i><br>
     * Conversion Error : NSRange
     */
    /**
     * <i>native declaration : :463</i><br>
     * Conversion Error : NSRange
     */
    /**
     * <i>native declaration : :466</i><br>
     * Conversion Error : NSRange
     */
    /**
     * <i>native declaration : :469</i><br>
     * Conversion Error : NSRange
     */
    /**
     * <i>native declaration : :470</i><br>
     * Conversion Error : NSRange
     */
    /**
     * <i>native declaration : :474</i><br>
     * Conversion Error : NSRect
     */
    /**
     * <i>native declaration : :477</i><br>
     * Conversion Error : NSRange
     */
    /**
     * <i>native declaration : :478</i><br>
     * Conversion Error : NSRange
     */
    /**
     * <i>native declaration : :481</i><br>
     * Conversion Error : NSPoint
     */
    /**
     * <i>native declaration : :482</i><br>
     * Conversion Error : NSPoint
     */
    /**
     * <i>native declaration : :483</i><br>
     * Conversion Error : NSPoint
     */

    /**
     * Original signature : <code>NSUInteger getLineFragmentInsertionPointsForCharacterAtIndex(NSUInteger, BOOL, BOOL, CGFloat*, NSUInteger*)</code><br>
     * <i>native declaration : :488</i>
     */
    public abstract int getLineFragmentInsertionPointsForCharacterAtIndex_alternatePositions_inDisplayOrder_positions_characterIndexes(int charIndex, boolean aFlag, boolean dFlag, java.nio.FloatBuffer positions, java.nio.IntBuffer charIndexes);
    /**
     * <i>native declaration : :494</i><br>
     * Conversion Error : /// Original signature : <code>NSDictionary* temporaryAttributesAtCharacterIndex(NSUInteger, null)</code><br>
     * - (NSDictionary*)temporaryAttributesAtCharacterIndex:(NSUInteger)charIndex effectiveRange:(null)effectiveCharRange; (Argument effectiveCharRange cannot be converted)
     */
    /**
     * <i>native declaration : :495</i><br>
     * Conversion Error : NSRange
     */
    /**
     * <i>native declaration : :496</i><br>
     * Conversion Error : NSRange
     */
    /**
     * <i>native declaration : :497</i><br>
     * Conversion Error : NSRange
     */
    /**
     * <i>native declaration : :501</i><br>
     * Conversion Error : /// Original signature : <code>id temporaryAttribute(NSString*, NSUInteger, null)</code><br>
     * - (id)temporaryAttribute:(NSString*)attrName atCharacterIndex:(NSUInteger)location effectiveRange:(null)range; (Argument range cannot be converted)
     */
    /**
     * <i>native declaration : :502</i><br>
     * Conversion Error : /// Original signature : <code>id temporaryAttribute(NSString*, NSUInteger, null, NSRange)</code><br>
     * - (id)temporaryAttribute:(NSString*)attrName atCharacterIndex:(NSUInteger)location longestEffectiveRange:(null)range inRange:(NSRange)rangeLimit; (Argument range cannot be converted)
     */
    /**
     * <i>native declaration : :503</i><br>
     * Conversion Error : /// Original signature : <code>NSDictionary* temporaryAttributesAtCharacterIndex(NSUInteger, null, NSRange)</code><br>
     * - (NSDictionary*)temporaryAttributesAtCharacterIndex:(NSUInteger)location longestEffectiveRange:(null)range inRange:(NSRange)rangeLimit; (Argument range cannot be converted)
     */
    /**
     * <i>native declaration : :504</i><br>
     * Conversion Error : NSRange
     */
    /**
     * Original signature : <code>NSFont* substituteFontForFont(NSFont*)</code><br>
     * <i>native declaration : :510</i>
     */
    public abstract com.sun.jna.Pointer substituteFontForFont(com.sun.jna.Pointer originalFont);

    /**
     * Original signature : <code>CGFloat defaultLineHeightForFont(NSFont*)</code><br>
     * <i>native declaration : :514</i>
     */
    public abstract CGFloat defaultLineHeightForFont(NSFont theFont);

    /**
     * Returns the default line height specified by the layout manager's typesetter behavior for the given font.<br>
     * Original signature : <code>CGFloat defaultBaselineOffsetForFont(NSFont*)</code><br>
     * <i>native declaration : :516</i>
     */
    public abstract CGFloat defaultBaselineOffsetForFont(NSFont theFont);

    /**
     * Returns the default baseline offset specified by the layout manager's typesetter behavior for the given font.<br>
     * Original signature : <code>BOOL usesFontLeading()</code><br>
     * <i>native declaration : :518</i>
     */
    public abstract boolean usesFontLeading();

    /**
     * Original signature : <code>void setUsesFontLeading(BOOL)</code><br>
     * <i>native declaration : :519</i>
     */
    public abstract void setUsesFontLeading(boolean flag);

    /**
     * Original signature : <code>NSArray* rulerMarkersForTextView(NSTextView*, NSParagraphStyle*, NSRulerView*)</code><br>
     * <i>from NSTextViewSupport native declaration : :529</i>
     */
    public abstract NSArray rulerMarkersForTextView_paragraphStyle_ruler(com.sun.jna.Pointer view, com.sun.jna.Pointer style, com.sun.jna.Pointer ruler);

    /**
     * Original signature : <code>NSView* rulerAccessoryViewForTextView(NSTextView*, NSParagraphStyle*, NSRulerView*, BOOL)</code><br>
     * <i>from NSTextViewSupport native declaration : :530</i>
     */
    public abstract NSView rulerAccessoryViewForTextView_paragraphStyle_ruler_enabled(com.sun.jna.Pointer view, com.sun.jna.Pointer style, com.sun.jna.Pointer ruler, boolean isEnabled);

    /**
     * Original signature : <code>BOOL layoutManagerOwnsFirstResponderInWindow(NSWindow*)</code><br>
     * <i>from NSTextViewSupport native declaration : :535</i>
     */
    public abstract boolean layoutManagerOwnsFirstResponderInWindow(com.sun.jna.Pointer window);

    /**
     * Original signature : <code>NSTextView* firstTextView()</code><br>
     * <i>from NSTextViewSupport native declaration : :538</i>
     */
    public abstract NSTextView firstTextView();

    /**
     * Original signature : <code>NSTextView* textViewForBeginningOfSelection()</code><br>
     * <i>from NSTextViewSupport native declaration : :540</i>
     */
    public abstract NSTextView textViewForBeginningOfSelection();
    /**
     * <i>from NSTextViewSupport native declaration : :545</i><br>
     * Conversion Error : NSRange
     */
    /**
     * <i>from NSTextViewSupport native declaration : :546</i><br>
     * Conversion Error : NSRange
     */
    /**
     * <i>from NSTextViewSupport native declaration : :549</i><br>
     * Conversion Error : NSRange
     */
    /**
     * <i>from NSTextViewSupport native declaration : :552</i><br>
     * Conversion Error : NSRect
     */
    /**
     * <i>from NSTextViewSupport native declaration : :555</i><br>
     * Conversion Error : NSRange
     */
    /**
     * <i>from NSTextViewSupport native declaration : :556</i><br>
     * Conversion Error : NSRange
     */
    /**
     * <i>from NSTextViewSupport native declaration : :560</i><br>
     * Conversion Error : NSRange
     */
    /**
     * <i>from NSTextViewSupport native declaration : :561</i><br>
     * Conversion Error : NSRange
     */
}
