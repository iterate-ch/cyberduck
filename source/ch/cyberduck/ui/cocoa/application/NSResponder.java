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

import org.rococoa.ID;
import org.rococoa.cocoa.foundation.NSError;

/// <i>native declaration : :11</i>
public abstract class NSResponder extends NSObject {

    /**
     * Original signature : <code>NSResponder* nextResponder()</code><br>
     * <i>native declaration : :17</i>
     */
    public abstract NSResponder NSResponder_nextResponder();

    /**
     * Original signature : <code>void setNextResponder(NSResponder*)</code><br>
     * <i>native declaration : :18</i>
     */
    public abstract void setNextResponder(NSResponder aResponder);
    /**
     * <i>native declaration : :19</i><br>
     * Conversion Error : /// Original signature : <code>BOOL tryToPerform(null, id)</code><br>
     * - (BOOL)tryToPerform:(null)anAction with:(id)anObject; (Argument anAction cannot be converted)
     */
    /**
     * Original signature : <code>BOOL performKeyEquivalent(NSEvent*)</code><br>
     * <i>native declaration : :20</i>
     */
    public abstract boolean performKeyEquivalent(NSEvent event);

    /**
     * Original signature : <code>id validRequestorForSendType(NSString*, NSString*)</code><br>
     * <i>native declaration : :21</i>
     */
    public abstract org.rococoa.ID validRequestorForSendType_returnType(String sendType, String returnType);

    /**
     * Original signature : <code>void mouseDown(NSEvent*)</code><br>
     * <i>native declaration : :22</i>
     */
    public abstract void mouseDown(NSEvent event);

    /**
     * Original signature : <code>void rightMouseDown(NSEvent*)</code><br>
     * <i>native declaration : :23</i>
     */
    public abstract void rightMouseDown(NSEvent event);

    /**
     * Original signature : <code>void otherMouseDown(NSEvent*)</code><br>
     * <i>native declaration : :24</i>
     */
    public abstract void otherMouseDown(NSEvent event);

    /**
     * Original signature : <code>void mouseUp(NSEvent*)</code><br>
     * <i>native declaration : :25</i>
     */
    public abstract void mouseUp(NSEvent event);

    /**
     * Original signature : <code>void rightMouseUp(NSEvent*)</code><br>
     * <i>native declaration : :26</i>
     */
    public abstract void rightMouseUp(NSEvent event);

    /**
     * Original signature : <code>void otherMouseUp(NSEvent*)</code><br>
     * <i>native declaration : :27</i>
     */
    public abstract void otherMouseUp(NSEvent event);

    /**
     * Original signature : <code>void mouseMoved(NSEvent*)</code><br>
     * <i>native declaration : :28</i>
     */
    public abstract void mouseMoved(NSEvent event);

    /**
     * Original signature : <code>void mouseDragged(NSEvent*)</code><br>
     * <i>native declaration : :29</i>
     */
    public abstract void mouseDragged(NSEvent event);

    /**
     * Original signature : <code>void scrollWheel(NSEvent*)</code><br>
     * <i>native declaration : :30</i>
     */
    public abstract void scrollWheel(NSEvent event);

    /**
     * Original signature : <code>void rightMouseDragged(NSEvent*)</code><br>
     * <i>native declaration : :31</i>
     */
    public abstract void rightMouseDragged(NSEvent event);

    /**
     * Original signature : <code>void otherMouseDragged(NSEvent*)</code><br>
     * <i>native declaration : :32</i>
     */
    public abstract void otherMouseDragged(NSEvent event);

    /**
     * Original signature : <code>void mouseEntered(NSEvent*)</code><br>
     * <i>native declaration : :33</i>
     */
    public abstract void mouseEntered(NSEvent event);

    /**
     * Original signature : <code>void mouseExited(NSEvent*)</code><br>
     * <i>native declaration : :34</i>
     */
    public abstract void mouseExited(NSEvent event);

    /**
     * Original signature : <code>void keyDown(NSEvent*)</code><br>
     * <i>native declaration : :35</i>
     */
    public abstract void keyDown(NSEvent event);

    /**
     * Original signature : <code>void keyUp(NSEvent*)</code><br>
     * <i>native declaration : :36</i>
     */
    public abstract void keyUp(NSEvent event);

    /**
     * Original signature : <code>void flagsChanged(NSEvent*)</code><br>
     * <i>native declaration : :37</i>
     */
    public abstract void flagsChanged(NSEvent event);

    /**
     * Original signature : <code>void tabletPoint(NSEvent*)</code><br>
     * <i>native declaration : :39</i>
     */
    public abstract void tabletPoint(NSEvent event);

    /**
     * Original signature : <code>void tabletProximity(NSEvent*)</code><br>
     * <i>native declaration : :40</i>
     */
    public abstract void tabletProximity(NSEvent event);

    /**
     * Original signature : <code>void cursorUpdate(NSEvent*)</code><br>
     * <i>native declaration : :43</i>
     */
    public abstract void cursorUpdate(NSEvent event);
    /**
     * <i>native declaration : :45</i><br>
     * Conversion Error : /// Original signature : <code>void noResponderFor(null)</code><br>
     * - (void)noResponderFor:(null)eventSelector; (Argument eventSelector cannot be converted)
     */
    /**
     * Original signature : <code>BOOL acceptsFirstResponder()</code><br>
     * <i>native declaration : :46</i>
     */
    public abstract boolean acceptsFirstResponder();

    /**
     * Original signature : <code>BOOL becomeFirstResponder()</code><br>
     * <i>native declaration : :47</i>
     */
    public abstract boolean becomeFirstResponder();

    /**
     * Original signature : <code>BOOL resignFirstResponder()</code><br>
     * <i>native declaration : :48</i>
     */
    public abstract boolean resignFirstResponder();

    /**
     * Original signature : <code>void interpretKeyEvents(NSArray*)</code><br>
     * <i>native declaration : :50</i>
     */
    public abstract void interpretKeyEvents(NSEvent eventArray);

    /**
     * Original signature : <code>void flushBufferedKeyEvents()</code><br>
     * <i>native declaration : :51</i>
     */
    public abstract void flushBufferedKeyEvents();

    /**
     * Original signature : <code>void setMenu(NSMenu*)</code><br>
     * <i>native declaration : :53</i>
     */
    public abstract void setMenu(NSMenu menu);

    /**
     * Original signature : <code>NSMenu* menu()</code><br>
     * <i>native declaration : :54</i>
     */
    public abstract NSMenu menu();

    /**
     * Original signature : <code>void showContextHelp(id)</code><br>
     * <i>native declaration : :56</i>
     */
    public abstract void showContextHelp(final ID sender);

    /**
     * Original signature : <code>void helpRequested(NSEvent*)</code><br>
     * <i>native declaration : :58</i>
     */
    public abstract void helpRequested(NSEvent eventPtr);

    /**
     * Original signature : <code>BOOL shouldBeTreatedAsInkEvent(NSEvent*)</code><br>
     * <i>native declaration : :61</i>
     */
    public abstract boolean shouldBeTreatedAsInkEvent(NSEvent event);

    /**
     * Original signature : <code>BOOL performMnemonic(NSString*)</code><br>
     * <i>from NSKeyboardUI native declaration : :66</i>
     */
    public abstract boolean performMnemonic(String theString);

    /**
     * Original signature : <code>void insertText(id)</code><br>
     * <i>from NSStandardKeyBindingMethods native declaration : :75</i>
     */
    public abstract void insertText(NSObject insertString);
    /**
     * <i>from NSStandardKeyBindingMethods native declaration : :78</i><br>
     * Conversion Error : /// Original signature : <code>void doCommandBySelector(null)</code><br>
     * - (void)doCommandBySelector:(null)aSelector; (Argument aSelector cannot be converted)
     */
    /**
     * Original signature : <code>void moveForward(id)</code><br>
     * <i>from NSStandardKeyBindingMethods native declaration : :85</i>
     */
    public abstract void moveForward(final ID sender);

    /**
     * Original signature : <code>void moveRight(id)</code><br>
     * <i>from NSStandardKeyBindingMethods native declaration : :86</i>
     */
    public abstract void moveRight(final ID sender);

    /**
     * Original signature : <code>void moveBackward(id)</code><br>
     * <i>from NSStandardKeyBindingMethods native declaration : :87</i>
     */
    public abstract void moveBackward(final ID sender);

    /**
     * Original signature : <code>void moveLeft(id)</code><br>
     * <i>from NSStandardKeyBindingMethods native declaration : :88</i>
     */
    public abstract void moveLeft(final ID sender);

    /**
     * Original signature : <code>void moveUp(id)</code><br>
     * <i>from NSStandardKeyBindingMethods native declaration : :89</i>
     */
    public abstract void moveUp(final ID sender);

    /**
     * Original signature : <code>void moveDown(id)</code><br>
     * <i>from NSStandardKeyBindingMethods native declaration : :90</i>
     */
    public abstract void moveDown(final ID sender);

    /**
     * Original signature : <code>void moveWordForward(id)</code><br>
     * <i>from NSStandardKeyBindingMethods native declaration : :91</i>
     */
    public abstract void moveWordForward(final ID sender);

    /**
     * Original signature : <code>void moveWordBackward(id)</code><br>
     * <i>from NSStandardKeyBindingMethods native declaration : :92</i>
     */
    public abstract void moveWordBackward(final ID sender);

    /**
     * Original signature : <code>void moveToBeginningOfLine(id)</code><br>
     * <i>from NSStandardKeyBindingMethods native declaration : :93</i>
     */
    public abstract void moveToBeginningOfLine(final ID sender);

    /**
     * Original signature : <code>void moveToEndOfLine(id)</code><br>
     * <i>from NSStandardKeyBindingMethods native declaration : :94</i>
     */
    public abstract void moveToEndOfLine(final ID sender);

    /**
     * Original signature : <code>void moveToBeginningOfParagraph(id)</code><br>
     * <i>from NSStandardKeyBindingMethods native declaration : :95</i>
     */
    public abstract void moveToBeginningOfParagraph(final ID sender);

    /**
     * Original signature : <code>void moveToEndOfParagraph(id)</code><br>
     * <i>from NSStandardKeyBindingMethods native declaration : :96</i>
     */
    public abstract void moveToEndOfParagraph(final ID sender);

    /**
     * Original signature : <code>void moveToEndOfDocument(id)</code><br>
     * <i>from NSStandardKeyBindingMethods native declaration : :97</i>
     */
    public abstract void moveToEndOfDocument(final ID sender);

    /**
     * Original signature : <code>void moveToBeginningOfDocument(id)</code><br>
     * <i>from NSStandardKeyBindingMethods native declaration : :98</i>
     */
    public abstract void moveToBeginningOfDocument(final ID sender);

    /**
     * Original signature : <code>void pageDown(id)</code><br>
     * <i>from NSStandardKeyBindingMethods native declaration : :99</i>
     */
    public abstract void pageDown(final ID sender);

    /**
     * Original signature : <code>void pageUp(id)</code><br>
     * <i>from NSStandardKeyBindingMethods native declaration : :100</i>
     */
    public abstract void pageUp(final ID sender);

    /**
     * Original signature : <code>void centerSelectionInVisibleArea(id)</code><br>
     * <i>from NSStandardKeyBindingMethods native declaration : :101</i>
     */
    public abstract void centerSelectionInVisibleArea(final ID sender);

    /**
     * Original signature : <code>void moveBackwardAndModifySelection(id)</code><br>
     * <i>from NSStandardKeyBindingMethods native declaration : :103</i>
     */
    public abstract void moveBackwardAndModifySelection(final ID sender);

    /**
     * Original signature : <code>void moveForwardAndModifySelection(id)</code><br>
     * <i>from NSStandardKeyBindingMethods native declaration : :104</i>
     */
    public abstract void moveForwardAndModifySelection(final ID sender);

    /**
     * Original signature : <code>void moveWordForwardAndModifySelection(id)</code><br>
     * <i>from NSStandardKeyBindingMethods native declaration : :105</i>
     */
    public abstract void moveWordForwardAndModifySelection(final ID sender);

    /**
     * Original signature : <code>void moveWordBackwardAndModifySelection(id)</code><br>
     * <i>from NSStandardKeyBindingMethods native declaration : :106</i>
     */
    public abstract void moveWordBackwardAndModifySelection(final ID sender);

    /**
     * Original signature : <code>void moveUpAndModifySelection(id)</code><br>
     * <i>from NSStandardKeyBindingMethods native declaration : :107</i>
     */
    public abstract void moveUpAndModifySelection(final ID sender);

    /**
     * Original signature : <code>void moveDownAndModifySelection(id)</code><br>
     * <i>from NSStandardKeyBindingMethods native declaration : :108</i>
     */
    public abstract void moveDownAndModifySelection(final ID sender);

    /**
     * Original signature : <code>void moveWordRight(id)</code><br>
     * <i>from NSStandardKeyBindingMethods native declaration : :111</i>
     */
    public abstract void moveWordRight(final ID sender);

    /**
     * Original signature : <code>void moveWordLeft(id)</code><br>
     * <i>from NSStandardKeyBindingMethods native declaration : :112</i>
     */
    public abstract void moveWordLeft(final ID sender);

    /**
     * Original signature : <code>void moveRightAndModifySelection(id)</code><br>
     * <i>from NSStandardKeyBindingMethods native declaration : :113</i>
     */
    public abstract void moveRightAndModifySelection(final ID sender);

    /**
     * Original signature : <code>void moveLeftAndModifySelection(id)</code><br>
     * <i>from NSStandardKeyBindingMethods native declaration : :114</i>
     */
    public abstract void moveLeftAndModifySelection(final ID sender);

    /**
     * Original signature : <code>void moveWordRightAndModifySelection(id)</code><br>
     * <i>from NSStandardKeyBindingMethods native declaration : :115</i>
     */
    public abstract void moveWordRightAndModifySelection(final ID sender);

    /**
     * Original signature : <code>void moveWordLeftAndModifySelection(id)</code><br>
     * <i>from NSStandardKeyBindingMethods native declaration : :116</i>
     */
    public abstract void moveWordLeftAndModifySelection(final ID sender);

    /**
     * Original signature : <code>void scrollPageUp(id)</code><br>
     * <i>from NSStandardKeyBindingMethods native declaration : :119</i>
     */
    public abstract void scrollPageUp(final ID sender);

    /**
     * Original signature : <code>void scrollPageDown(id)</code><br>
     * <i>from NSStandardKeyBindingMethods native declaration : :120</i>
     */
    public abstract void scrollPageDown(final ID sender);

    /**
     * Original signature : <code>void scrollLineUp(id)</code><br>
     * <i>from NSStandardKeyBindingMethods native declaration : :121</i>
     */
    public abstract void scrollLineUp(final ID sender);

    /**
     * Original signature : <code>void scrollLineDown(id)</code><br>
     * <i>from NSStandardKeyBindingMethods native declaration : :122</i>
     */
    public abstract void scrollLineDown(final ID sender);

    /**
     * Original signature : <code>void transpose(id)</code><br>
     * <i>from NSStandardKeyBindingMethods native declaration : :126</i>
     */
    public abstract void transpose(final ID sender);

    /**
     * Original signature : <code>void transposeWords(id)</code><br>
     * <i>from NSStandardKeyBindingMethods native declaration : :127</i>
     */
    public abstract void transposeWords(final ID sender);

    /**
     * Original signature : <code>void selectAll(id)</code><br>
     * <i>from NSStandardKeyBindingMethods native declaration : :131</i>
     */
    public abstract void selectAll(final ID sender);

    /**
     * Original signature : <code>void selectParagraph(id)</code><br>
     * <i>from NSStandardKeyBindingMethods native declaration : :132</i>
     */
    public abstract void selectParagraph(final ID sender);

    /**
     * Original signature : <code>void selectLine(id)</code><br>
     * <i>from NSStandardKeyBindingMethods native declaration : :133</i>
     */
    public abstract void selectLine(final ID sender);

    /**
     * Original signature : <code>void selectWord(id)</code><br>
     * <i>from NSStandardKeyBindingMethods native declaration : :134</i>
     */
    public abstract void selectWord(final ID sender);

    /**
     * Original signature : <code>void indent(id)</code><br>
     * <i>from NSStandardKeyBindingMethods native declaration : :138</i>
     */
    public abstract void indent(final ID sender);

    /**
     * Original signature : <code>void insertTab(id)</code><br>
     * <i>from NSStandardKeyBindingMethods native declaration : :139</i>
     */
    public abstract void insertTab(final ID sender);

    /**
     * Original signature : <code>void insertBacktab(id)</code><br>
     * <i>from NSStandardKeyBindingMethods native declaration : :140</i>
     */
    public abstract void insertBacktab(final ID sender);

    /**
     * Original signature : <code>void insertNewline(id)</code><br>
     * <i>from NSStandardKeyBindingMethods native declaration : :141</i>
     */
    public abstract void insertNewline(final ID sender);

    /**
     * Original signature : <code>void insertParagraphSeparator(id)</code><br>
     * <i>from NSStandardKeyBindingMethods native declaration : :142</i>
     */
    public abstract void insertParagraphSeparator(final ID sender);

    /**
     * Original signature : <code>void insertNewlineIgnoringFieldEditor(id)</code><br>
     * <i>from NSStandardKeyBindingMethods native declaration : :143</i>
     */
    public abstract void insertNewlineIgnoringFieldEditor(final ID sender);

    /**
     * Original signature : <code>void insertTabIgnoringFieldEditor(id)</code><br>
     * <i>from NSStandardKeyBindingMethods native declaration : :144</i>
     */
    public abstract void insertTabIgnoringFieldEditor(final ID sender);

    /**
     * Original signature : <code>void insertLineBreak(id)</code><br>
     * <i>from NSStandardKeyBindingMethods native declaration : :146</i>
     */
    public abstract void insertLineBreak(final ID sender);

    /**
     * Original signature : <code>void insertContainerBreak(id)</code><br>
     * <i>from NSStandardKeyBindingMethods native declaration : :147</i>
     */
    public abstract void insertContainerBreak(final ID sender);

    /**
     * Original signature : <code>void changeCaseOfLetter(id)</code><br>
     * <i>from NSStandardKeyBindingMethods native declaration : :152</i>
     */
    public abstract void changeCaseOfLetter(final ID sender);

    /**
     * Original signature : <code>void uppercaseWord(id)</code><br>
     * <i>from NSStandardKeyBindingMethods native declaration : :153</i>
     */
    public abstract void uppercaseWord(final ID sender);

    /**
     * Original signature : <code>void lowercaseWord(id)</code><br>
     * <i>from NSStandardKeyBindingMethods native declaration : :154</i>
     */
    public abstract void lowercaseWord(final ID sender);

    /**
     * Original signature : <code>void capitalizeWord(id)</code><br>
     * <i>from NSStandardKeyBindingMethods native declaration : :155</i>
     */
    public abstract void capitalizeWord(final ID sender);

    /**
     * Original signature : <code>void deleteForward(id)</code><br>
     * <i>from NSStandardKeyBindingMethods native declaration : :159</i>
     */
    public abstract void deleteForward(final ID sender);

    /**
     * Original signature : <code>void deleteBackward(id)</code><br>
     * <i>from NSStandardKeyBindingMethods native declaration : :160</i>
     */
    public abstract void deleteBackward(final ID sender);

    /**
     * Original signature : <code>void deleteBackwardByDecomposingPreviousCharacter(id)</code><br>
     * <i>from NSStandardKeyBindingMethods native declaration : :162</i>
     */
    public abstract void deleteBackwardByDecomposingPreviousCharacter(final ID sender);

    /**
     * Original signature : <code>void deleteWordForward(id)</code><br>
     * <i>from NSStandardKeyBindingMethods native declaration : :164</i>
     */
    public abstract void deleteWordForward(final ID sender);

    /**
     * Original signature : <code>void deleteWordBackward(id)</code><br>
     * <i>from NSStandardKeyBindingMethods native declaration : :165</i>
     */
    public abstract void deleteWordBackward(final ID sender);

    /**
     * Original signature : <code>void deleteToBeginningOfLine(id)</code><br>
     * <i>from NSStandardKeyBindingMethods native declaration : :166</i>
     */
    public abstract void deleteToBeginningOfLine(final ID sender);

    /**
     * Original signature : <code>void deleteToEndOfLine(id)</code><br>
     * <i>from NSStandardKeyBindingMethods native declaration : :167</i>
     */
    public abstract void deleteToEndOfLine(final ID sender);

    /**
     * Original signature : <code>void deleteToBeginningOfParagraph(id)</code><br>
     * <i>from NSStandardKeyBindingMethods native declaration : :168</i>
     */
    public abstract void deleteToBeginningOfParagraph(final ID sender);

    /**
     * Original signature : <code>void deleteToEndOfParagraph(id)</code><br>
     * <i>from NSStandardKeyBindingMethods native declaration : :169</i>
     */
    public abstract void deleteToEndOfParagraph(final ID sender);

    /**
     * Original signature : <code>void yank(id)</code><br>
     * <i>from NSStandardKeyBindingMethods native declaration : :171</i>
     */
    public abstract void yank(final ID sender);

    /**
     * Original signature : <code>void complete(id)</code><br>
     * <i>from NSStandardKeyBindingMethods native declaration : :175</i>
     */
    public abstract void complete(final ID sender);

    /**
     * Original signature : <code>void setMark(id)</code><br>
     * <i>from NSStandardKeyBindingMethods native declaration : :179</i>
     */
    public abstract void setMark(final ID sender);

    /**
     * Original signature : <code>void deleteToMark(id)</code><br>
     * <i>from NSStandardKeyBindingMethods native declaration : :180</i>
     */
    public abstract void deleteToMark(final ID sender);

    /**
     * Original signature : <code>void selectToMark(id)</code><br>
     * <i>from NSStandardKeyBindingMethods native declaration : :181</i>
     */
    public abstract void selectToMark(final ID sender);

    /**
     * Original signature : <code>void swapWithMark(id)</code><br>
     * <i>from NSStandardKeyBindingMethods native declaration : :182</i>
     */
    public abstract void swapWithMark(final ID sender);

    /**
     * Original signature : <code>void cancelOperation(id)</code><br>
     * <i>from NSStandardKeyBindingMethods native declaration : :187</i>
     */
    public abstract void cancelOperation(final ID sender);

    /**
     * Original signature : <code>NSUndoManager* undoManager()</code><br>
     * <i>from NSUndoSupport native declaration : :192</i>
     */
    public abstract com.sun.jna.Pointer undoManager();
    /**
     * <i>from NSErrorPresentation native declaration : :222</i><br>
     * Conversion Error : /**<br>
     *  * Present an error alert to the user, as a document-modal panel. When the user has dismissed the alert and any recovery possible for the error and chosen by the user has been attempted, send the selected message to the specified delegate. The method selected by didPresentSelector must have the same signature as:<br>
     *  * - (void)didPresentErrorWithRecovery:(BOOL)didRecover contextInfo:(void *)contextInfo;<br>
     *  * The default implementation of this method always invokes [self willPresentError:error] to give subclassers an opportunity to customize error presentation. It then forwards the message, passing the customized error, to the next responder or, if there is no next responder, NSApp. NSApplication's override of this method invokes [[NSAlert alertWithError:theErrorToPresent] beginSheetModalForWindow:window modalDelegate:self didEndSelector:selectorForAPrivateMethod contextInfo:privateContextInfo]. When the user has dismissed the alert, the error's recovery attempter is sent an -attemptRecoveryFromError:optionIndex:delegate:didRecoverSelector:contextInfo: message, if the error had recovery options and a recovery delegate.<br>
     *  * Errors for which ([[error domain] isEqualToString:NSCocoaErrorDomain] && [error code]==NSUserCancelledError) are a special case,  because they do not actually represent errors and should not be presented as such to the user. NSApplication's override of this method does not present an alert to the user for these kinds of errors. Instead it merely invokes the delegate specifying didRecover==NO.<br>
     *  * Between the responder chain in a typical application and various overrides of this method in AppKit classes, objects are given the opportunity to present errors in orders like these:<br>
     *  * For windows owned by documents:<br>
     *  * view -> superviews -> window -> window controller -> document -> document controller -> application<br>
     *  * For windows that have window controllers but aren't associated with documents:<br>
     *  * view -> superviews -> window -> window controller -> application<br>
     *  * For windows that have no window controller at all:<br>
     *  * view -> superviews -> window -> application<br>
     *  * You can invoke this method to present error alert sheets. For example, Cocoa's own -[NSDocument saveToURL:ofType:forSaveOperation:delegate:didSaveSelector:contextInfo:] invokes this method when it's just invoked -saveToURL:ofType:forSaveOperation:error: and that method has returned NO.<br>
     *  * You probably shouldn't override this method, because you have no way of reliably predicting whether this method vs. -presentError will be invoked for any particular error. You should instead override the -willPresentError: method described below.<br>
     *  * Original signature : <code>void presentError(NSError*, NSWindow*, id, null, void*)</code><br>
     *  * /<br>
     * - (void)presentError:(NSError*)error modalForWindow:(NSWindow*)window delegate:(id)delegate didPresentSelector:(null)didPresentSelector contextInfo:(void*)contextInfo; (Argument didPresentSelector cannot be converted)
     */
    /**
     * Present an error alert to the user, as an application-modal panel, and return YES if error recovery was done, NO otherwise. This method behaves much like the previous one except it does not return until the user has dismissed the alert and, if the error had recovery options and a recovery delegate, the error's recovery delegate has been sent an -attemptRecoveryFromError:optionIndex: message.<br>
     * You can invoke this method to present error alert dialog boxes. For example, Cocoa's own [NSDocumentController openDocument:] invokes this method when it's just invoked -openDocumentWithContentsOfURL:display:error: and that method has returned nil.<br>
     * You probably shouldn't override this method, because you have no way of reliably predicting whether this method vs. -presentError:modalForWindow:delegate:didPresentSelector:contextInfo: will be invoked for any particular error. You should instead override the -willPresentError: method described below.<br>
     * Original signature : <code>BOOL presentError(NSError*)</code><br>
     * <i>from NSErrorPresentation native declaration : :230</i>
     */
    public abstract boolean presentError(NSError error);

    /**
     * Given that the receiver is about to present an error (perhaps by just forwarding it to the next responder), return the error that should actually be presented. The default implementation of this method merely returns the passed-in error.<br>
     * You can override this method to customize the presentation of errors by examining the passed-in error and if, for example, its localized description or recovery information is unsuitably generic, returning a more specific one. When you override this method always check the NSError's domain and code to discriminate between errors whose presentation you want to customize and those you don't. For those you don't just return [super willPresentError:error]. Don't make decisions based on the NSError's localized description, recovery suggestion, or recovery options because it's usually not a good idea to try to parse localized text.<br>
     * Original signature : <code>NSError* willPresentError(NSError*)</code><br>
     * <i>from NSErrorPresentation native declaration : :236</i>
     */
    public abstract NSError willPresentError(NSError error);
}
