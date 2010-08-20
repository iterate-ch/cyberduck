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

import org.rococoa.ObjCClass;
import org.rococoa.cocoa.CGFloat;

/// <i>native declaration : :72</i>

public abstract class NSTextView extends NSText {
    private static final _Class CLASS = org.rococoa.Rococoa.createClass("NSTextView", _Class.class);

    public static NSTextView create() {
        return CLASS.alloc().init();
    }

    public interface _Class extends ObjCClass {
        NSTextView alloc();
    }

    public abstract NSTextView init();

    /**
     * <i>native declaration : :80</i><br>
     * Conversion Error : /// Original signature : <code>initWithFrame(null, NSTextContainer*)</code><br>
     * - (null)initWithFrame:(null)frameRect textContainer:(NSTextContainer*)container; (Argument frameRect cannot be converted)
     */
    /**
     * <i>native declaration : :83</i><br>
     * Conversion Error : /// Original signature : <code>initWithFrame(null)</code><br>
     * - (null)initWithFrame:(null)frameRect; (Argument frameRect cannot be converted)
     */
    /**
     * Original signature : <code>NSTextContainer* textContainer()</code><br>
     * <i>native declaration : :88</i>
     */
    public abstract com.sun.jna.Pointer textContainer();

    /**
     * Original signature : <code>void setTextContainer(NSTextContainer*)</code><br>
     * <i>native declaration : :89</i>
     */
    public abstract void setTextContainer(com.sun.jna.Pointer container);

    /**
     * Original signature : <code>void replaceTextContainer(NSTextContainer*)</code><br>
     * <i>native declaration : :92</i>
     */
    public abstract void replaceTextContainer(com.sun.jna.Pointer newContainer);
    /**
     * <i>native declaration : :95</i><br>
     * Conversion Error : /// Original signature : <code>void setTextContainerInset(null)</code><br>
     * - (void)setTextContainerInset:(null)inset; (Argument inset cannot be converted)
     */
    /**
     * Original signature : <code>textContainerInset()</code><br>
     * <i>native declaration : :96</i>
     */
    public abstract NSObject textContainerInset();

    /**
     * Original signature : <code>textContainerOrigin()</code><br>
     * <i>native declaration : :99</i>
     */
    public abstract NSObject textContainerOrigin();

    /**
     * Original signature : <code>void invalidateTextContainerOrigin()</code><br>
     * <i>native declaration : :100</i>
     */
    public abstract void invalidateTextContainerOrigin();

    /**
     * Original signature : <code>NSLayoutManager* layoutManager()</code><br>
     * <i>native declaration : :103</i>
     */
    public abstract NSLayoutManager layoutManager();

    /**
     * Original signature : <code>NSTextStorage* textStorage()</code><br>
     * <i>native declaration : :104</i>
     */
    public abstract NSTextStorage textStorage();
    /**
     * <i>native declaration : :109</i><br>
     * Conversion Error : /// Original signature : <code>void insertText(null)</code><br>
     * - (void)insertText:(null)insertString; (Argument insertString cannot be converted)
     */
    /**
     * <i>native declaration : :114</i><br>
     * Conversion Error : /// Original signature : <code>void setConstrainedFrameSize(null)</code><br>
     * - (void)setConstrainedFrameSize:(null)desiredSize; (Argument desiredSize cannot be converted)
     */
    /**
     * <i>native declaration : :120</i><br>
     * Conversion Error : /**<br>
     *  * These two complete the set of range: type set methods. to be equivalent to the set of non-range taking varieties.<br>
     *  * Original signature : <code>void setAlignment(null, null)</code><br>
     *  * /<br>
     * - (void)setAlignment:(null)alignment range:(null)range; (Argument alignment cannot be converted)
     */
    /**
     * <i>native declaration : :122</i><br>
     * Conversion Error : /// Original signature : <code>void setBaseWritingDirection(null, null)</code><br>
     * - (void)setBaseWritingDirection:(null)writingDirection range:(null)range; (Argument writingDirection cannot be converted)
     */
    /**
     * <i>native declaration : :127</i><br>
     * Conversion Error : /// Original signature : <code>void turnOffKerning(null)</code><br>
     * - (void)turnOffKerning:(null)sender; (Argument sender cannot be converted)
     */
    /**
     * <i>native declaration : :128</i><br>
     * Conversion Error : /// Original signature : <code>void tightenKerning(null)</code><br>
     * - (void)tightenKerning:(null)sender; (Argument sender cannot be converted)
     */
    /**
     * <i>native declaration : :129</i><br>
     * Conversion Error : /// Original signature : <code>void loosenKerning(null)</code><br>
     * - (void)loosenKerning:(null)sender; (Argument sender cannot be converted)
     */
    /**
     * <i>native declaration : :130</i><br>
     * Conversion Error : /// Original signature : <code>void useStandardKerning(null)</code><br>
     * - (void)useStandardKerning:(null)sender; (Argument sender cannot be converted)
     */
    /**
     * <i>native declaration : :131</i><br>
     * Conversion Error : /// Original signature : <code>void turnOffLigatures(null)</code><br>
     * - (void)turnOffLigatures:(null)sender; (Argument sender cannot be converted)
     */
    /**
     * <i>native declaration : :132</i><br>
     * Conversion Error : /// Original signature : <code>void useStandardLigatures(null)</code><br>
     * - (void)useStandardLigatures:(null)sender; (Argument sender cannot be converted)
     */
    /**
     * <i>native declaration : :133</i><br>
     * Conversion Error : /// Original signature : <code>void useAllLigatures(null)</code><br>
     * - (void)useAllLigatures:(null)sender; (Argument sender cannot be converted)
     */
    /**
     * <i>native declaration : :134</i><br>
     * Conversion Error : /// Original signature : <code>void raiseBaseline(null)</code><br>
     * - (void)raiseBaseline:(null)sender; (Argument sender cannot be converted)
     */
    /**
     * <i>native declaration : :135</i><br>
     * Conversion Error : /// Original signature : <code>void lowerBaseline(null)</code><br>
     * - (void)lowerBaseline:(null)sender; (Argument sender cannot be converted)
     */
    /**
     * <i>native declaration : :136</i><br>
     * Conversion Error : /// Original signature : <code>void toggleTraditionalCharacterShape(null)</code><br>
     * - (void)toggleTraditionalCharacterShape:(null)sender; (Argument sender cannot be converted)
     */
    /**
     * <i>native declaration : :138</i><br>
     * Conversion Error : /// Original signature : <code>void outline(null)</code><br>
     * - (void)outline:(null)sender; (Argument sender cannot be converted)
     */
    /**
     * <i>native declaration : :144</i><br>
     * Conversion Error : /// Original signature : <code>void performFindPanelAction(null)</code><br>
     * - (void)performFindPanelAction:(null)sender; (Argument sender cannot be converted)
     */
    /**
     * <i>native declaration : :150</i><br>
     * Conversion Error : /// Original signature : <code>void alignJustified(null)</code><br>
     * - (void)alignJustified:(null)sender; (Argument sender cannot be converted)
     */
    /**
     * <i>native declaration : :151</i><br>
     * Conversion Error : /// Original signature : <code>void changeColor(null)</code><br>
     * - (void)changeColor:(null)sender; (Argument sender cannot be converted)
     */
    /**
     * <i>native declaration : :153</i><br>
     * Conversion Error : /// Original signature : <code>void changeAttributes(null)</code><br>
     * - (void)changeAttributes:(null)sender; (Argument sender cannot be converted)
     */
    /**
     * <i>native declaration : :154</i><br>
     * Conversion Error : /// Original signature : <code>void changeDocumentBackgroundColor(null)</code><br>
     * - (void)changeDocumentBackgroundColor:(null)sender; (Argument sender cannot be converted)
     */
    /**
     * <i>native declaration : :155</i><br>
     * Conversion Error : /// Original signature : <code>void toggleBaseWritingDirection(null)</code><br>
     * - (void)toggleBaseWritingDirection:(null)sender; (Argument sender cannot be converted)
     */
    /**
     * <i>native declaration : :158</i><br>
     * Conversion Error : /// Original signature : <code>void orderFrontSpacingPanel(null)</code><br>
     * - (void)orderFrontSpacingPanel:(null)sender; (Argument sender cannot be converted)
     */
    /**
     * <i>native declaration : :159</i><br>
     * Conversion Error : /// Original signature : <code>void orderFrontLinkPanel(null)</code><br>
     * - (void)orderFrontLinkPanel:(null)sender; (Argument sender cannot be converted)
     */
    /**
     * <i>native declaration : :160</i><br>
     * Conversion Error : /// Original signature : <code>void orderFrontListPanel(null)</code><br>
     * - (void)orderFrontListPanel:(null)sender; (Argument sender cannot be converted)
     */
    /**
     * <i>native declaration : :161</i><br>
     * Conversion Error : /// Original signature : <code>void orderFrontTablePanel(null)</code><br>
     * - (void)orderFrontTablePanel:(null)sender; (Argument sender cannot be converted)
     */
    /**
     * Original signature : <code>void rulerView(NSRulerView*, NSRulerMarker*)</code><br>
     * <i>native declaration : :166</i>
     */
    public abstract void rulerView_didMoveMarker(com.sun.jna.Pointer ruler, com.sun.jna.Pointer marker);

    /**
     * Original signature : <code>void rulerView(NSRulerView*, NSRulerMarker*)</code><br>
     * <i>native declaration : :167</i>
     */
    public abstract void rulerView_didRemoveMarker(com.sun.jna.Pointer ruler, com.sun.jna.Pointer marker);

    /**
     * Original signature : <code>void rulerView(NSRulerView*, NSRulerMarker*)</code><br>
     * <i>native declaration : :168</i>
     */
    public abstract void rulerView_didAddMarker(com.sun.jna.Pointer ruler, com.sun.jna.Pointer marker);

    /**
     * Original signature : <code>BOOL rulerView(NSRulerView*, NSRulerMarker*)</code><br>
     * <i>native declaration : :169</i>
     */
    public abstract boolean rulerView_shouldMoveMarker(com.sun.jna.Pointer ruler, com.sun.jna.Pointer marker);

    /**
     * Original signature : <code>BOOL rulerView(NSRulerView*, NSRulerMarker*)</code><br>
     * <i>native declaration : :170</i>
     */
    public abstract boolean rulerView_shouldAddMarker(com.sun.jna.Pointer ruler, com.sun.jna.Pointer marker);

    /**
     * Original signature : <code>CGFloat rulerView(NSRulerView*, NSRulerMarker*, CGFloat)</code><br>
     * <i>native declaration : :171</i>
     */
    public abstract CGFloat rulerView_willMoveMarker_toLocation(com.sun.jna.Pointer ruler, com.sun.jna.Pointer marker, CGFloat location);

    /**
     * Original signature : <code>BOOL rulerView(NSRulerView*, NSRulerMarker*)</code><br>
     * <i>native declaration : :172</i>
     */
    public abstract boolean rulerView_shouldRemoveMarker(com.sun.jna.Pointer ruler, com.sun.jna.Pointer marker);

    /**
     * Original signature : <code>CGFloat rulerView(NSRulerView*, NSRulerMarker*, CGFloat)</code><br>
     * <i>native declaration : :173</i>
     */
    public abstract CGFloat rulerView_willAddMarker_atLocation(com.sun.jna.Pointer ruler, com.sun.jna.Pointer marker, CGFloat location);

    /**
     * Original signature : <code>void rulerView(NSRulerView*, NSEvent*)</code><br>
     * <i>native declaration : :174</i>
     */
    public abstract void rulerView_handleMouseDown(com.sun.jna.Pointer ruler, NSEvent event);
    /**
     * <i>native declaration : :178</i><br>
     * Conversion Error : /// Original signature : <code>void setNeedsDisplayInRect(null, BOOL)</code><br>
     * - (void)setNeedsDisplayInRect:(null)rect avoidAdditionalLayout:(BOOL)flag; (Argument rect cannot be converted)
     */
    /**
     * Original signature : <code>BOOL shouldDrawInsertionPoint()</code><br>
     * <i>native declaration : :181</i>
     */
    public abstract boolean shouldDrawInsertionPoint();
    /**
     * <i>native declaration : :182</i><br>
     * Conversion Error : /// Original signature : <code>void drawInsertionPointInRect(null, NSColor*, BOOL)</code><br>
     * - (void)drawInsertionPointInRect:(null)rect color:(NSColor*)color turnedOn:(BOOL)flag; (Argument rect cannot be converted)
     */
    /**
     * <i>native declaration : :185</i><br>
     * Conversion Error : /// Original signature : <code>void drawViewBackgroundInRect(null)</code><br>
     * - (void)drawViewBackgroundInRect:(null)rect; (Argument rect cannot be converted)
     */
    /**
     * Original signature : <code>void updateRuler()</code><br>
     * <i>native declaration : :191</i>
     */
    public abstract void updateRuler();

    /**
     * Original signature : <code>void updateFontPanel()</code><br>
     * <i>native declaration : :192</i>
     */
    public abstract void updateFontPanel();

    /**
     * Original signature : <code>void updateDragTypeRegistration()</code><br>
     * <i>native declaration : :194</i>
     */
    public abstract void updateDragTypeRegistration();
    /**
     * <i>native declaration : :196</i><br>
     * Conversion Error : /// Original signature : <code>selectionRangeForProposedRange(null, NSSelectionGranularity)</code><br>
     * - (null)selectionRangeForProposedRange:(null)proposedCharRange granularity:(NSSelectionGranularity)granularity; (Argument proposedCharRange cannot be converted)
     */
    /**
     * <i>native declaration : :200</i><br>
     * Conversion Error : /// Original signature : <code>void clickedOnLink(null, NSUInteger)</code><br>
     * - (void)clickedOnLink:(null)link atIndex:(NSUInteger)charIndex; (Argument link cannot be converted)
     */
    /**
     * <i>native declaration : :205</i><br>
     * Conversion Error : /// Original signature : <code>void startSpeaking(null)</code><br>
     * - (void)startSpeaking:(null)sender; (Argument sender cannot be converted)
     */
    /**
     * <i>native declaration : :206</i><br>
     * Conversion Error : /// Original signature : <code>void stopSpeaking(null)</code><br>
     * - (void)stopSpeaking:(null)sender; (Argument sender cannot be converted)
     */
    /**
     * <i>native declaration : :211</i><br>
     * Conversion Error : /// Original signature : <code>NSUInteger characterIndexForInsertionAtPoint(null)</code><br>
     * - (NSUInteger)characterIndexForInsertionAtPoint:(null)point; (Argument point cannot be converted)
     */
    /**
     * <i>from NSCompletion native declaration : :222</i><br>
     * Conversion Error : /// Original signature : <code>void complete(null)</code><br>
     * - (void)complete:(null)sender; (Argument sender cannot be converted)
     */
    /**
     * Original signature : <code>rangeForUserCompletion()</code><br>
     * <i>from NSCompletion native declaration : :225</i>
     */
    public abstract NSObject rangeForUserCompletion();
    /**
     * <i>from NSCompletion native declaration : :228</i><br>
     * Conversion Error : /// Original signature : <code>NSArray* completionsForPartialWordRange(null, NSInteger*)</code><br>
     * - (NSArray*)completionsForPartialWordRange:(null)charRange indexOfSelectedItem:(NSInteger*)index; (Argument charRange cannot be converted)
     */
    /**
     * <i>from NSCompletion native declaration : :231</i><br>
     * Conversion Error : /// Original signature : <code>void insertCompletion(NSString*, null, NSInteger, BOOL)</code><br>
     * - (void)insertCompletion:(NSString*)word forPartialWordRange:(null)charRange movement:(NSInteger)movement isFinal:(BOOL)flag; (Argument charRange cannot be converted)
     */
    /**
     * Original signature : <code>NSArray* writablePasteboardTypes()</code><br>
     * <i>from NSPasteboard native declaration : :248</i>
     */
    public abstract com.sun.jna.Pointer writablePasteboardTypes();

    /**
     * Original signature : <code>BOOL writeSelectionToPasteboard(NSPasteboard*, NSString*)</code><br>
     * <i>from NSPasteboard native declaration : :251</i>
     */
    public abstract boolean writeSelectionToPasteboard_type(com.sun.jna.Pointer pboard, com.sun.jna.Pointer type);

    /**
     * Original signature : <code>BOOL writeSelectionToPasteboard(NSPasteboard*, NSArray*)</code><br>
     * <i>from NSPasteboard native declaration : :254</i>
     */
    public abstract boolean writeSelectionToPasteboard_types(com.sun.jna.Pointer pboard, com.sun.jna.Pointer types);

    /**
     * Original signature : <code>NSArray* readablePasteboardTypes()</code><br>
     * <i>from NSPasteboard native declaration : :258</i>
     */
    public abstract NSArray readablePasteboardTypes();

    /**
     * Original signature : <code>NSString* preferredPasteboardTypeFromArray(NSArray*, NSArray*)</code><br>
     * <i>from NSPasteboard native declaration : :261</i>
     */
    public abstract String preferredPasteboardTypeFromArray_restrictedToTypesFromArray(NSArray availableTypes, NSArray allowedTypes);

    /**
     * Original signature : <code>BOOL readSelectionFromPasteboard(NSPasteboard*, NSString*)</code><br>
     * <i>from NSPasteboard native declaration : :264</i>
     */
    public abstract boolean readSelectionFromPasteboard_type(NSPasteboard pboard, String type);

    /**
     * Original signature : <code>BOOL readSelectionFromPasteboard(NSPasteboard*)</code><br>
     * <i>from NSPasteboard native declaration : :267</i>
     */
    public abstract boolean readSelectionFromPasteboard(com.sun.jna.Pointer pboard);

    /**
     * Original signature : <code>validRequestorForSendType(NSString*, NSString*)</code><br>
     * <i>from NSPasteboard native declaration : :273</i>
     */
    /**
     * <i>from NSPasteboard native declaration : :276</i><br>
     * Conversion Error : /// Original signature : <code>void pasteAsPlainText(null)</code><br>
     * - (void)pasteAsPlainText:(null)sender; (Argument sender cannot be converted)
     */
    /**
     * <i>from NSPasteboard native declaration : :277</i><br>
     * Conversion Error : /// Original signature : <code>void pasteAsRichText(null)</code><br>
     * - (void)pasteAsRichText:(null)sender; (Argument sender cannot be converted)
     */
    /**
     * <i>from NSDragging native declaration : :284</i><br>
     * Conversion Error : /// Original signature : <code>BOOL dragSelectionWithEvent(NSEvent*, null, BOOL)</code><br>
     * - (BOOL)dragSelectionWithEvent:(NSEvent*)event offset:(null)mouseOffset slideBack:(BOOL)slideBack; (Argument mouseOffset cannot be converted)
     */
    /**
     * <i>from NSDragging native declaration : :287</i><br>
     * Conversion Error : /// Original signature : <code>NSImage* dragImageForSelectionWithEvent(NSEvent*, null)</code><br>
     * - (NSImage*)dragImageForSelectionWithEvent:(NSEvent*)event origin:(null)origin; (Argument origin cannot be converted)
     */
    /**
     * Original signature : <code>NSArray* acceptableDragTypes()</code><br>
     * <i>from NSDragging native declaration : :290</i>
     */
    public abstract NSArray acceptableDragTypes();
    /**
     * <i>from NSDragging native declaration : :293</i><br>
     * Conversion Error : id<NSDraggingInfo>
     */
    /**
     * Original signature : <code>void cleanUpAfterDragOperation()</code><br>
     * <i>from NSDragging native declaration : :296</i>
     */
    public abstract void cleanUpAfterDragOperation();

    /**
     * Original signature : <code>NSArray* selectedRanges()</code><br>
     * <i>from NSSharing native declaration : :308</i>
     */
    public abstract NSArray selectedRanges();

    /**
     * Original signature : <code>void setSelectedRanges(NSArray*, NSSelectionAffinity, BOOL)</code><br>
     * <i>from NSSharing native declaration : :309</i>
     */
    public abstract void setSelectedRanges_affinity_stillSelecting(com.sun.jna.Pointer ranges, int affinity, boolean stillSelectingFlag);

    /**
     * Original signature : <code>void setSelectedRanges(NSArray*)</code><br>
     * <i>from NSSharing native declaration : :310</i>
     */
    public abstract void setSelectedRanges(NSArray ranges);
    /**
     * <i>from NSSharing native declaration : :314</i><br>
     * Conversion Error : /// Original signature : <code>void setSelectedRange(null, NSSelectionAffinity, BOOL)</code><br>
     * - (void)setSelectedRange:(null)charRange affinity:(NSSelectionAffinity)affinity stillSelecting:(BOOL)stillSelectingFlag; (Argument charRange cannot be converted)
     */
    /**
     * Original signature : <code>NSSelectionAffinity selectionAffinity()</code><br>
     * <i>from NSSharing native declaration : :315</i>
     */
    public abstract int selectionAffinity();

    /**
     * Original signature : <code>NSSelectionGranularity selectionGranularity()</code><br>
     * <i>from NSSharing native declaration : :316</i>
     */
    public abstract int selectionGranularity();

    /**
     * Original signature : <code>void setSelectionGranularity(NSSelectionGranularity)</code><br>
     * <i>from NSSharing native declaration : :317</i>
     */
    public abstract void setSelectionGranularity(int granularity);

    /**
     * Original signature : <code>void setSelectedTextAttributes(NSDictionary*)</code><br>
     * <i>from NSSharing native declaration : :319</i>
     */
    public abstract void setSelectedTextAttributes(NSDictionary attributeDictionary);

    /**
     * Original signature : <code>NSDictionary* selectedTextAttributes()</code><br>
     * <i>from NSSharing native declaration : :320</i>
     */
    public abstract com.sun.jna.Pointer selectedTextAttributes();

    /**
     * Original signature : <code>void setInsertionPointColor(NSColor*)</code><br>
     * <i>from NSSharing native declaration : :323</i>
     */
    public abstract void setInsertionPointColor(com.sun.jna.Pointer color);

    /**
     * Original signature : <code>NSColor* insertionPointColor()</code><br>
     * <i>from NSSharing native declaration : :324</i>
     */
    public abstract com.sun.jna.Pointer insertionPointColor();

    /**
     * Original signature : <code>void updateInsertionPointStateAndRestartTimer(BOOL)</code><br>
     * <i>from NSSharing native declaration : :326</i>
     */
    public abstract void updateInsertionPointStateAndRestartTimer(boolean restartFlag);

    /**
     * Original signature : <code>void setMarkedTextAttributes(NSDictionary*)</code><br>
     * <i>from NSSharing native declaration : :328</i>
     */
    public abstract void setMarkedTextAttributes(com.sun.jna.Pointer attributeDictionary);

    /**
     * Original signature : <code>NSDictionary* markedTextAttributes()</code><br>
     * <i>from NSSharing native declaration : :329</i>
     */
    public abstract com.sun.jna.Pointer markedTextAttributes();

    /**
     * Original signature : <code>void setLinkTextAttributes(NSDictionary*)</code><br>
     * <i>from NSSharing native declaration : :333</i>
     */
    public abstract void setLinkTextAttributes(com.sun.jna.Pointer attributeDictionary);

    /**
     * Original signature : <code>NSDictionary* linkTextAttributes()</code><br>
     * <i>from NSSharing native declaration : :334</i>
     */
    public abstract com.sun.jna.Pointer linkTextAttributes();

    /**
     * Original signature : <code>BOOL displaysLinkToolTips()</code><br>
     * <i>from NSSharing native declaration : :339</i>
     */
    public abstract boolean displaysLinkToolTips();

    /**
     * Original signature : <code>void setDisplaysLinkToolTips(BOOL)</code><br>
     * <i>from NSSharing native declaration : :340</i>
     */
    public abstract void setDisplaysLinkToolTips(boolean flag);

    /**
     * Original signature : <code>BOOL acceptsGlyphInfo()</code><br>
     * <i>from NSSharing native declaration : :347</i>
     */
    public abstract boolean acceptsGlyphInfo();

    /**
     * Original signature : <code>void setAcceptsGlyphInfo(BOOL)</code><br>
     * <i>from NSSharing native declaration : :348</i>
     */
    public abstract void setAcceptsGlyphInfo(boolean flag);

    /**
     * Original signature : <code>void setRulerVisible(BOOL)</code><br>
     * <i>from NSSharing native declaration : :353</i>
     */
    public abstract void setRulerVisible(boolean flag);

    /**
     * Original signature : <code>BOOL usesRuler()</code><br>
     * <i>from NSSharing native declaration : :354</i>
     */
    public abstract boolean usesRuler();

    /**
     * Original signature : <code>void setUsesRuler(BOOL)</code><br>
     * <i>from NSSharing native declaration : :355</i>
     */
    public abstract void setUsesRuler(boolean flag);

    /**
     * Original signature : <code>void setContinuousSpellCheckingEnabled(BOOL)</code><br>
     * <i>from NSSharing native declaration : :357</i>
     */
    public abstract void setContinuousSpellCheckingEnabled(boolean flag);

    /**
     * Original signature : <code>BOOL isContinuousSpellCheckingEnabled()</code><br>
     * <i>from NSSharing native declaration : :358</i>
     */
    public abstract boolean isContinuousSpellCheckingEnabled();
    /**
     * <i>from NSSharing native declaration : :359</i><br>
     * Conversion Error : /// Original signature : <code>void toggleContinuousSpellChecking(null)</code><br>
     * - (void)toggleContinuousSpellChecking:(null)sender; (Argument sender cannot be converted)
     */
    /**
     * Original signature : <code>NSInteger spellCheckerDocumentTag()</code><br>
     * <i>from NSSharing native declaration : :361</i>
     */
    public abstract int spellCheckerDocumentTag();

    /**
     * Original signature : <code>void setGrammarCheckingEnabled(BOOL)</code><br>
     * <i>from NSSharing native declaration : :364</i>
     */
    public abstract void setGrammarCheckingEnabled(boolean flag);

    /**
     * Original signature : <code>BOOL isGrammarCheckingEnabled()</code><br>
     * <i>from NSSharing native declaration : :365</i>
     */
    public abstract boolean isGrammarCheckingEnabled();
    /**
     * <i>from NSSharing native declaration : :366</i><br>
     * Conversion Error : /// Original signature : <code>void toggleGrammarChecking(null)</code><br>
     * - (void)toggleGrammarChecking:(null)sender; (Argument sender cannot be converted)
     */
    /**
     * <i>from NSSharing native declaration : :369</i><br>
     * Conversion Error : /// Original signature : <code>void setSpellingState(NSInteger, null)</code><br>
     * - (void)setSpellingState:(NSInteger)value range:(null)charRange; (Argument charRange cannot be converted)
     */
    /**
     * Original signature : <code>NSDictionary* typingAttributes()</code><br>
     * <i>from NSSharing native declaration : :373</i>
     */
    public abstract com.sun.jna.Pointer typingAttributes();

    /**
     * Original signature : <code>void setTypingAttributes(NSDictionary*)</code><br>
     * <i>from NSSharing native declaration : :374</i>
     */
    public abstract void setTypingAttributes(com.sun.jna.Pointer attrs);

    /**
     * These multiple-range methods supersede the corresponding single-range methods.  For the first method, the affectedRanges argument obeys the same restrictions as the argument to setSelectedRanges:, and the replacementStrings array should either be nil (for attribute-only changes) or have the same number of elements as affectedRanges.  For the remaining three methods, the return values obey the same restrictions as that for selectedRanges, except that they will be nil if the corresponding change is not permitted, where the corresponding single-range methods return (NSNotFound, 0).<br>
     * Original signature : <code>BOOL shouldChangeTextInRanges(NSArray*, NSArray*)</code><br>
     * <i>from NSSharing native declaration : :378</i>
     */
    public abstract boolean shouldChangeTextInRanges_replacementStrings(com.sun.jna.Pointer affectedRanges, com.sun.jna.Pointer replacementStrings);

    /**
     * Original signature : <code>NSArray* rangesForUserTextChange()</code><br>
     * <i>from NSSharing native declaration : :379</i>
     */
    public abstract com.sun.jna.Pointer rangesForUserTextChange();

    /**
     * Original signature : <code>NSArray* rangesForUserCharacterAttributeChange()</code><br>
     * <i>from NSSharing native declaration : :380</i>
     */
    public abstract com.sun.jna.Pointer rangesForUserCharacterAttributeChange();

    /**
     * Original signature : <code>NSArray* rangesForUserParagraphAttributeChange()</code><br>
     * <i>from NSSharing native declaration : :381</i>
     */
    public abstract com.sun.jna.Pointer rangesForUserParagraphAttributeChange();
    /**
     * <i>from NSSharing native declaration : :384</i><br>
     * Conversion Error : /// Original signature : <code>BOOL shouldChangeTextInRange(null, NSString*)</code><br>
     * - (BOOL)shouldChangeTextInRange:(null)affectedCharRange replacementString:(NSString*)replacementString; (Argument affectedCharRange cannot be converted)
     */
    /**
     * Original signature : <code>void didChangeText()</code><br>
     * <i>from NSSharing native declaration : :385</i>
     */
    public abstract void didChangeText();

    /**
     * Original signature : <code>rangeForUserTextChange()</code><br>
     * <i>from NSSharing native declaration : :387</i>
     */
    public abstract NSObject rangeForUserTextChange();

    /**
     * Original signature : <code>rangeForUserCharacterAttributeChange()</code><br>
     * <i>from NSSharing native declaration : :388</i>
     */
    public abstract NSObject rangeForUserCharacterAttributeChange();

    /**
     * Original signature : <code>rangeForUserParagraphAttributeChange()</code><br>
     * <i>from NSSharing native declaration : :389</i>
     */
    public abstract NSObject rangeForUserParagraphAttributeChange();

    /**
     * Original signature : <code>void setUsesFindPanel(BOOL)</code><br>
     * <i>from NSSharing native declaration : :392</i>
     */
    public abstract void setUsesFindPanel(boolean flag);

    /**
     * Original signature : <code>BOOL usesFindPanel()</code><br>
     * <i>from NSSharing native declaration : :393</i>
     */
    public abstract boolean usesFindPanel();

    /**
     * Original signature : <code>void setAllowsDocumentBackgroundColorChange(BOOL)</code><br>
     * <i>from NSSharing native declaration : :395</i>
     */
    public abstract void setAllowsDocumentBackgroundColorChange(boolean flag);

    /**
     * Original signature : <code>BOOL allowsDocumentBackgroundColorChange()</code><br>
     * <i>from NSSharing native declaration : :396</i>
     */
    public abstract boolean allowsDocumentBackgroundColorChange();

    /**
     * Original signature : <code>void setDefaultParagraphStyle(NSParagraphStyle*)</code><br>
     * <i>from NSSharing native declaration : :398</i>
     */
    public abstract void setDefaultParagraphStyle(com.sun.jna.Pointer paragraphStyle);

    /**
     * Original signature : <code>NSParagraphStyle* defaultParagraphStyle()</code><br>
     * <i>from NSSharing native declaration : :399</i>
     */
    public abstract com.sun.jna.Pointer defaultParagraphStyle();

    /**
     * Original signature : <code>void setAllowsUndo(BOOL)</code><br>
     * <i>from NSSharing native declaration : :402</i>
     */
    public abstract void setAllowsUndo(boolean flag);

    /**
     * Original signature : <code>BOOL allowsUndo()</code><br>
     * <i>from NSSharing native declaration : :403</i>
     */
    public abstract boolean allowsUndo();

    /**
     * Original signature : <code>void breakUndoCoalescing()</code><br>
     * <i>from NSSharing native declaration : :406</i>
     */
    public abstract void breakUndoCoalescing();

    /**
     * Original signature : <code>BOOL allowsImageEditing()</code><br>
     * <i>from NSSharing native declaration : :411</i>
     */
    public abstract boolean allowsImageEditing();

    /**
     * Original signature : <code>void setAllowsImageEditing(BOOL)</code><br>
     * <i>from NSSharing native declaration : :412</i>
     */
    public abstract void setAllowsImageEditing(boolean flag);
    /**
     * <i>from NSSharing native declaration : :415</i><br>
     * Conversion Error : /// Original signature : <code>void showFindIndicatorForRange(null)</code><br>
     * - (void)showFindIndicatorForRange:(null)charRange; (Argument charRange cannot be converted)
     */
    /**
     * <i>from NSSharing native declaration : :440</i><br>
     * Conversion Error : /// Original signature : <code>void setSelectedRange(null)</code><br>
     * - (void)setSelectedRange:(null)charRange; (Argument charRange cannot be converted)
     */
    /**
     * Original signature : <code>BOOL smartInsertDeleteEnabled()</code><br>
     * <i>from NSSharing native declaration : :445</i>
     */
    public abstract boolean smartInsertDeleteEnabled();

    /**
     * Original signature : <code>void setSmartInsertDeleteEnabled(BOOL)</code><br>
     * <i>from NSSharing native declaration : :446</i>
     */
    public abstract void setSmartInsertDeleteEnabled(boolean flag);
    /**
     * <i>from NSSharing native declaration : :447</i><br>
     * Conversion Error : /// Original signature : <code>smartDeleteRangeForProposedRange(null)</code><br>
     * - (null)smartDeleteRangeForProposedRange:(null)proposedCharRange; (Argument proposedCharRange cannot be converted)
     */
    /**
     * <i>from NSSharing native declaration : :449</i><br>
     * Conversion Error : /// Original signature : <code>void toggleSmartInsertDelete(null)</code><br>
     * - (void)toggleSmartInsertDelete:(null)sender; (Argument sender cannot be converted)
     */
    /**
     * <i>from NSSharing native declaration : :452</i><br>
     * Conversion Error : /// Original signature : <code>void smartInsertForString(NSString*, null, NSString**, NSString**)</code><br>
     * - (void)smartInsertForString:(NSString*)pasteString replacingRange:(null)charRangeToReplace beforeString:(NSString**)beforeString afterString:(NSString**)afterString; (Argument charRangeToReplace cannot be converted)
     */
    /**
     * <i>from NSSharing native declaration : :453</i><br>
     * Conversion Error : /// Original signature : <code>NSString* smartInsertBeforeStringForString(NSString*, null)</code><br>
     * - (NSString*)smartInsertBeforeStringForString:(NSString*)pasteString replacingRange:(null)charRangeToReplace; (Argument charRangeToReplace cannot be converted)
     */
    /**
     * <i>from NSSharing native declaration : :454</i><br>
     * Conversion Error : /// Original signature : <code>NSString* smartInsertAfterStringForString(NSString*, null)</code><br>
     * - (NSString*)smartInsertAfterStringForString:(NSString*)pasteString replacingRange:(null)charRangeToReplace; (Argument charRangeToReplace cannot be converted)
     */
    /**
     * Original signature : <code>void setAutomaticQuoteSubstitutionEnabled(BOOL)</code><br>
     * <i>from NSSharing native declaration : :458</i>
     */
    public abstract void setAutomaticQuoteSubstitutionEnabled(boolean flag);

    /**
     * Original signature : <code>BOOL isAutomaticQuoteSubstitutionEnabled()</code><br>
     * <i>from NSSharing native declaration : :459</i>
     */
    public abstract boolean isAutomaticQuoteSubstitutionEnabled();
    /**
     * <i>from NSSharing native declaration : :460</i><br>
     * Conversion Error : /// Original signature : <code>void toggleAutomaticQuoteSubstitution(null)</code><br>
     * - (void)toggleAutomaticQuoteSubstitution:(null)sender; (Argument sender cannot be converted)
     */
    /**
     * Original signature : <code>void setAutomaticLinkDetectionEnabled(BOOL)</code><br>
     * <i>from NSSharing native declaration : :461</i>
     */
    public abstract void setAutomaticLinkDetectionEnabled(boolean flag);

    /**
     * Original signature : <code>BOOL isAutomaticLinkDetectionEnabled()</code><br>
     * <i>from NSSharing native declaration : :462</i>
     */
    public abstract boolean isAutomaticLinkDetectionEnabled();
    /**
     * <i>from NSSharing native declaration : :463</i><br>
     * Conversion Error : /// Original signature : <code>void toggleAutomaticLinkDetection(null)</code><br>
     * - (void)toggleAutomaticLinkDetection:(null)sender; (Argument sender cannot be converted)
     */
    /**
     * Returns an array of locale identifiers representing input sources allowed to be enabled when the receiver has the keyboard focus.<br>
     * Original signature : <code>NSArray* allowedInputSourceLocales()</code><br>
     * <i>from NSSharing native declaration : :470</i>
     */
    public abstract NSArray allowedInputSourceLocales();

    /**
     * Original signature : <code>void setAllowedInputSourceLocales(NSArray*)</code><br>
     * <i>from NSSharing native declaration : :471</i>
     */
    public abstract void setAllowedInputSourceLocales(NSArray localeIdentifiers);
}
