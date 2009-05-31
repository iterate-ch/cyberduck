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

import org.rococoa.cocoa.NSError;

/// <i>native declaration : :11</i>
public interface NSResponder extends NSObject {
    _Class CLASS = org.rococoa.Rococoa.createClass("NSResponder", _Class.class);

    public interface _Class extends org.rococoa.NSClass {
        NSResponder alloc();
    }

    /**
     * Original signature : <code>NSResponder* nextResponder()</code><br>
     * <i>native declaration : :17</i>
     */
    NSResponder NSResponder_nextResponder();

    /**
     * Original signature : <code>void setNextResponder(NSResponder*)</code><br>
     * <i>native declaration : :18</i>
     */
    void setNextResponder(NSResponder aResponder);
    /**
     * <i>native declaration : :19</i><br>
     * Conversion Error : /// Original signature : <code>BOOL tryToPerform(null, id)</code><br>
     * - (BOOL)tryToPerform:(null)anAction with:(id)anObject; (Argument anAction cannot be converted)
     */
    /**
     * Original signature : <code>BOOL performKeyEquivalent(NSEvent*)</code><br>
     * <i>native declaration : :20</i>
     */
    boolean performKeyEquivalent(NSEvent event);

    /**
     * Original signature : <code>id validRequestorForSendType(NSString*, NSString*)</code><br>
     * <i>native declaration : :21</i>
     */
    com.sun.jna.Pointer validRequestorForSendType_returnType(String sendType, String returnType);

    /**
     * Original signature : <code>void mouseDown(NSEvent*)</code><br>
     * <i>native declaration : :22</i>
     */
    void mouseDown(NSEvent event);

    /**
     * Original signature : <code>void rightMouseDown(NSEvent*)</code><br>
     * <i>native declaration : :23</i>
     */
    void rightMouseDown(NSEvent event);

    /**
     * Original signature : <code>void otherMouseDown(NSEvent*)</code><br>
     * <i>native declaration : :24</i>
     */
    void otherMouseDown(NSEvent event);

    /**
     * Original signature : <code>void mouseUp(NSEvent*)</code><br>
     * <i>native declaration : :25</i>
     */
    void mouseUp(NSEvent event);

    /**
     * Original signature : <code>void rightMouseUp(NSEvent*)</code><br>
     * <i>native declaration : :26</i>
     */
    void rightMouseUp(NSEvent event);

    /**
     * Original signature : <code>void otherMouseUp(NSEvent*)</code><br>
     * <i>native declaration : :27</i>
     */
    void otherMouseUp(NSEvent event);

    /**
     * Original signature : <code>void mouseMoved(NSEvent*)</code><br>
     * <i>native declaration : :28</i>
     */
    void mouseMoved(NSEvent event);

    /**
     * Original signature : <code>void mouseDragged(NSEvent*)</code><br>
     * <i>native declaration : :29</i>
     */
    void mouseDragged(NSEvent event);

    /**
     * Original signature : <code>void scrollWheel(NSEvent*)</code><br>
     * <i>native declaration : :30</i>
     */
    void scrollWheel(NSEvent event);

    /**
     * Original signature : <code>void rightMouseDragged(NSEvent*)</code><br>
     * <i>native declaration : :31</i>
     */
    void rightMouseDragged(NSEvent event);

    /**
     * Original signature : <code>void otherMouseDragged(NSEvent*)</code><br>
     * <i>native declaration : :32</i>
     */
    void otherMouseDragged(NSEvent event);

    /**
     * Original signature : <code>void mouseEntered(NSEvent*)</code><br>
     * <i>native declaration : :33</i>
     */
    void mouseEntered(NSEvent event);

    /**
     * Original signature : <code>void mouseExited(NSEvent*)</code><br>
     * <i>native declaration : :34</i>
     */
    void mouseExited(NSEvent event);

    /**
     * Original signature : <code>void keyDown(NSEvent*)</code><br>
     * <i>native declaration : :35</i>
     */
    void keyDown(NSEvent event);

    /**
     * Original signature : <code>void keyUp(NSEvent*)</code><br>
     * <i>native declaration : :36</i>
     */
    void keyUp(NSEvent event);

    /**
     * Original signature : <code>void flagsChanged(NSEvent*)</code><br>
     * <i>native declaration : :37</i>
     */
    void flagsChanged(NSEvent event);

    /**
     * Original signature : <code>void tabletPoint(NSEvent*)</code><br>
     * <i>native declaration : :39</i>
     */
    void tabletPoint(NSEvent event);

    /**
     * Original signature : <code>void tabletProximity(NSEvent*)</code><br>
     * <i>native declaration : :40</i>
     */
    void tabletProximity(NSEvent event);

    /**
     * Original signature : <code>void cursorUpdate(NSEvent*)</code><br>
     * <i>native declaration : :43</i>
     */
    void cursorUpdate(NSEvent event);
    /**
     * <i>native declaration : :45</i><br>
     * Conversion Error : /// Original signature : <code>void noResponderFor(null)</code><br>
     * - (void)noResponderFor:(null)eventSelector; (Argument eventSelector cannot be converted)
     */
    /**
     * Original signature : <code>BOOL acceptsFirstResponder()</code><br>
     * <i>native declaration : :46</i>
     */
    boolean acceptsFirstResponder();

    /**
     * Original signature : <code>BOOL becomeFirstResponder()</code><br>
     * <i>native declaration : :47</i>
     */
    boolean becomeFirstResponder();

    /**
     * Original signature : <code>BOOL resignFirstResponder()</code><br>
     * <i>native declaration : :48</i>
     */
    boolean resignFirstResponder();

    /**
     * Original signature : <code>void interpretKeyEvents(NSArray*)</code><br>
     * <i>native declaration : :50</i>
     */
    void interpretKeyEvents(NSEvent eventArray);

    /**
     * Original signature : <code>void flushBufferedKeyEvents()</code><br>
     * <i>native declaration : :51</i>
     */
    void flushBufferedKeyEvents();

    /**
     * Original signature : <code>void setMenu(NSMenu*)</code><br>
     * <i>native declaration : :53</i>
     */
    void setMenu(NSMenu menu);

    /**
     * Original signature : <code>NSMenu* menu()</code><br>
     * <i>native declaration : :54</i>
     */
    NSMenu menu();

    /**
     * Original signature : <code>void showContextHelp(id)</code><br>
     * <i>native declaration : :56</i>
     */
    void showContextHelp(NSObject sender);

    /**
     * Original signature : <code>void helpRequested(NSEvent*)</code><br>
     * <i>native declaration : :58</i>
     */
    void helpRequested(NSEvent eventPtr);

    /**
     * Original signature : <code>BOOL shouldBeTreatedAsInkEvent(NSEvent*)</code><br>
     * <i>native declaration : :61</i>
     */
    boolean shouldBeTreatedAsInkEvent(NSEvent event);

    /**
     * Original signature : <code>BOOL performMnemonic(NSString*)</code><br>
     * <i>from NSKeyboardUI native declaration : :66</i>
     */
    boolean performMnemonic(String theString);

    /**
     * Original signature : <code>void insertText(id)</code><br>
     * <i>from NSStandardKeyBindingMethods native declaration : :75</i>
     */
    void insertText(NSObject insertString);
    /**
     * <i>from NSStandardKeyBindingMethods native declaration : :78</i><br>
     * Conversion Error : /// Original signature : <code>void doCommandBySelector(null)</code><br>
     * - (void)doCommandBySelector:(null)aSelector; (Argument aSelector cannot be converted)
     */
    /**
     * Original signature : <code>void moveForward(id)</code><br>
     * <i>from NSStandardKeyBindingMethods native declaration : :85</i>
     */
    void moveForward(NSObject sender);

    /**
     * Original signature : <code>void moveRight(id)</code><br>
     * <i>from NSStandardKeyBindingMethods native declaration : :86</i>
     */
    void moveRight(NSObject sender);

    /**
     * Original signature : <code>void moveBackward(id)</code><br>
     * <i>from NSStandardKeyBindingMethods native declaration : :87</i>
     */
    void moveBackward(NSObject sender);

    /**
     * Original signature : <code>void moveLeft(id)</code><br>
     * <i>from NSStandardKeyBindingMethods native declaration : :88</i>
     */
    void moveLeft(NSObject sender);

    /**
     * Original signature : <code>void moveUp(id)</code><br>
     * <i>from NSStandardKeyBindingMethods native declaration : :89</i>
     */
    void moveUp(NSObject sender);

    /**
     * Original signature : <code>void moveDown(id)</code><br>
     * <i>from NSStandardKeyBindingMethods native declaration : :90</i>
     */
    void moveDown(NSObject sender);

    /**
     * Original signature : <code>void moveWordForward(id)</code><br>
     * <i>from NSStandardKeyBindingMethods native declaration : :91</i>
     */
    void moveWordForward(NSObject sender);

    /**
     * Original signature : <code>void moveWordBackward(id)</code><br>
     * <i>from NSStandardKeyBindingMethods native declaration : :92</i>
     */
    void moveWordBackward(NSObject sender);

    /**
     * Original signature : <code>void moveToBeginningOfLine(id)</code><br>
     * <i>from NSStandardKeyBindingMethods native declaration : :93</i>
     */
    void moveToBeginningOfLine(NSObject sender);

    /**
     * Original signature : <code>void moveToEndOfLine(id)</code><br>
     * <i>from NSStandardKeyBindingMethods native declaration : :94</i>
     */
    void moveToEndOfLine(NSObject sender);

    /**
     * Original signature : <code>void moveToBeginningOfParagraph(id)</code><br>
     * <i>from NSStandardKeyBindingMethods native declaration : :95</i>
     */
    void moveToBeginningOfParagraph(NSObject sender);

    /**
     * Original signature : <code>void moveToEndOfParagraph(id)</code><br>
     * <i>from NSStandardKeyBindingMethods native declaration : :96</i>
     */
    void moveToEndOfParagraph(NSObject sender);

    /**
     * Original signature : <code>void moveToEndOfDocument(id)</code><br>
     * <i>from NSStandardKeyBindingMethods native declaration : :97</i>
     */
    void moveToEndOfDocument(NSObject sender);

    /**
     * Original signature : <code>void moveToBeginningOfDocument(id)</code><br>
     * <i>from NSStandardKeyBindingMethods native declaration : :98</i>
     */
    void moveToBeginningOfDocument(NSObject sender);

    /**
     * Original signature : <code>void pageDown(id)</code><br>
     * <i>from NSStandardKeyBindingMethods native declaration : :99</i>
     */
    void pageDown(NSObject sender);

    /**
     * Original signature : <code>void pageUp(id)</code><br>
     * <i>from NSStandardKeyBindingMethods native declaration : :100</i>
     */
    void pageUp(NSObject sender);

    /**
     * Original signature : <code>void centerSelectionInVisibleArea(id)</code><br>
     * <i>from NSStandardKeyBindingMethods native declaration : :101</i>
     */
    void centerSelectionInVisibleArea(NSObject sender);

    /**
     * Original signature : <code>void moveBackwardAndModifySelection(id)</code><br>
     * <i>from NSStandardKeyBindingMethods native declaration : :103</i>
     */
    void moveBackwardAndModifySelection(NSObject sender);

    /**
     * Original signature : <code>void moveForwardAndModifySelection(id)</code><br>
     * <i>from NSStandardKeyBindingMethods native declaration : :104</i>
     */
    void moveForwardAndModifySelection(NSObject sender);

    /**
     * Original signature : <code>void moveWordForwardAndModifySelection(id)</code><br>
     * <i>from NSStandardKeyBindingMethods native declaration : :105</i>
     */
    void moveWordForwardAndModifySelection(NSObject sender);

    /**
     * Original signature : <code>void moveWordBackwardAndModifySelection(id)</code><br>
     * <i>from NSStandardKeyBindingMethods native declaration : :106</i>
     */
    void moveWordBackwardAndModifySelection(NSObject sender);

    /**
     * Original signature : <code>void moveUpAndModifySelection(id)</code><br>
     * <i>from NSStandardKeyBindingMethods native declaration : :107</i>
     */
    void moveUpAndModifySelection(NSObject sender);

    /**
     * Original signature : <code>void moveDownAndModifySelection(id)</code><br>
     * <i>from NSStandardKeyBindingMethods native declaration : :108</i>
     */
    void moveDownAndModifySelection(NSObject sender);

    /**
     * Original signature : <code>void moveWordRight(id)</code><br>
     * <i>from NSStandardKeyBindingMethods native declaration : :111</i>
     */
    void moveWordRight(NSObject sender);

    /**
     * Original signature : <code>void moveWordLeft(id)</code><br>
     * <i>from NSStandardKeyBindingMethods native declaration : :112</i>
     */
    void moveWordLeft(NSObject sender);

    /**
     * Original signature : <code>void moveRightAndModifySelection(id)</code><br>
     * <i>from NSStandardKeyBindingMethods native declaration : :113</i>
     */
    void moveRightAndModifySelection(NSObject sender);

    /**
     * Original signature : <code>void moveLeftAndModifySelection(id)</code><br>
     * <i>from NSStandardKeyBindingMethods native declaration : :114</i>
     */
    void moveLeftAndModifySelection(NSObject sender);

    /**
     * Original signature : <code>void moveWordRightAndModifySelection(id)</code><br>
     * <i>from NSStandardKeyBindingMethods native declaration : :115</i>
     */
    void moveWordRightAndModifySelection(NSObject sender);

    /**
     * Original signature : <code>void moveWordLeftAndModifySelection(id)</code><br>
     * <i>from NSStandardKeyBindingMethods native declaration : :116</i>
     */
    void moveWordLeftAndModifySelection(NSObject sender);

    /**
     * Original signature : <code>void scrollPageUp(id)</code><br>
     * <i>from NSStandardKeyBindingMethods native declaration : :119</i>
     */
    void scrollPageUp(NSObject sender);

    /**
     * Original signature : <code>void scrollPageDown(id)</code><br>
     * <i>from NSStandardKeyBindingMethods native declaration : :120</i>
     */
    void scrollPageDown(NSObject sender);

    /**
     * Original signature : <code>void scrollLineUp(id)</code><br>
     * <i>from NSStandardKeyBindingMethods native declaration : :121</i>
     */
    void scrollLineUp(NSObject sender);

    /**
     * Original signature : <code>void scrollLineDown(id)</code><br>
     * <i>from NSStandardKeyBindingMethods native declaration : :122</i>
     */
    void scrollLineDown(NSObject sender);

    /**
     * Original signature : <code>void transpose(id)</code><br>
     * <i>from NSStandardKeyBindingMethods native declaration : :126</i>
     */
    void transpose(NSObject sender);

    /**
     * Original signature : <code>void transposeWords(id)</code><br>
     * <i>from NSStandardKeyBindingMethods native declaration : :127</i>
     */
    void transposeWords(NSObject sender);

    /**
     * Original signature : <code>void selectAll(id)</code><br>
     * <i>from NSStandardKeyBindingMethods native declaration : :131</i>
     */
    void selectAll(NSObject sender);

    /**
     * Original signature : <code>void selectParagraph(id)</code><br>
     * <i>from NSStandardKeyBindingMethods native declaration : :132</i>
     */
    void selectParagraph(NSObject sender);

    /**
     * Original signature : <code>void selectLine(id)</code><br>
     * <i>from NSStandardKeyBindingMethods native declaration : :133</i>
     */
    void selectLine(NSObject sender);

    /**
     * Original signature : <code>void selectWord(id)</code><br>
     * <i>from NSStandardKeyBindingMethods native declaration : :134</i>
     */
    void selectWord(NSObject sender);

    /**
     * Original signature : <code>void indent(id)</code><br>
     * <i>from NSStandardKeyBindingMethods native declaration : :138</i>
     */
    void indent(NSObject sender);

    /**
     * Original signature : <code>void insertTab(id)</code><br>
     * <i>from NSStandardKeyBindingMethods native declaration : :139</i>
     */
    void insertTab(NSObject sender);

    /**
     * Original signature : <code>void insertBacktab(id)</code><br>
     * <i>from NSStandardKeyBindingMethods native declaration : :140</i>
     */
    void insertBacktab(NSObject sender);

    /**
     * Original signature : <code>void insertNewline(id)</code><br>
     * <i>from NSStandardKeyBindingMethods native declaration : :141</i>
     */
    void insertNewline(NSObject sender);

    /**
     * Original signature : <code>void insertParagraphSeparator(id)</code><br>
     * <i>from NSStandardKeyBindingMethods native declaration : :142</i>
     */
    void insertParagraphSeparator(NSObject sender);

    /**
     * Original signature : <code>void insertNewlineIgnoringFieldEditor(id)</code><br>
     * <i>from NSStandardKeyBindingMethods native declaration : :143</i>
     */
    void insertNewlineIgnoringFieldEditor(NSObject sender);

    /**
     * Original signature : <code>void insertTabIgnoringFieldEditor(id)</code><br>
     * <i>from NSStandardKeyBindingMethods native declaration : :144</i>
     */
    void insertTabIgnoringFieldEditor(NSObject sender);

    /**
     * Original signature : <code>void insertLineBreak(id)</code><br>
     * <i>from NSStandardKeyBindingMethods native declaration : :146</i>
     */
    void insertLineBreak(NSObject sender);

    /**
     * Original signature : <code>void insertContainerBreak(id)</code><br>
     * <i>from NSStandardKeyBindingMethods native declaration : :147</i>
     */
    void insertContainerBreak(NSObject sender);

    /**
     * Original signature : <code>void changeCaseOfLetter(id)</code><br>
     * <i>from NSStandardKeyBindingMethods native declaration : :152</i>
     */
    void changeCaseOfLetter(NSObject sender);

    /**
     * Original signature : <code>void uppercaseWord(id)</code><br>
     * <i>from NSStandardKeyBindingMethods native declaration : :153</i>
     */
    void uppercaseWord(NSObject sender);

    /**
     * Original signature : <code>void lowercaseWord(id)</code><br>
     * <i>from NSStandardKeyBindingMethods native declaration : :154</i>
     */
    void lowercaseWord(NSObject sender);

    /**
     * Original signature : <code>void capitalizeWord(id)</code><br>
     * <i>from NSStandardKeyBindingMethods native declaration : :155</i>
     */
    void capitalizeWord(NSObject sender);

    /**
     * Original signature : <code>void deleteForward(id)</code><br>
     * <i>from NSStandardKeyBindingMethods native declaration : :159</i>
     */
    void deleteForward(NSObject sender);

    /**
     * Original signature : <code>void deleteBackward(id)</code><br>
     * <i>from NSStandardKeyBindingMethods native declaration : :160</i>
     */
    void deleteBackward(NSObject sender);

    /**
     * Original signature : <code>void deleteBackwardByDecomposingPreviousCharacter(id)</code><br>
     * <i>from NSStandardKeyBindingMethods native declaration : :162</i>
     */
    void deleteBackwardByDecomposingPreviousCharacter(NSObject sender);

    /**
     * Original signature : <code>void deleteWordForward(id)</code><br>
     * <i>from NSStandardKeyBindingMethods native declaration : :164</i>
     */
    void deleteWordForward(NSObject sender);

    /**
     * Original signature : <code>void deleteWordBackward(id)</code><br>
     * <i>from NSStandardKeyBindingMethods native declaration : :165</i>
     */
    void deleteWordBackward(NSObject sender);

    /**
     * Original signature : <code>void deleteToBeginningOfLine(id)</code><br>
     * <i>from NSStandardKeyBindingMethods native declaration : :166</i>
     */
    void deleteToBeginningOfLine(NSObject sender);

    /**
     * Original signature : <code>void deleteToEndOfLine(id)</code><br>
     * <i>from NSStandardKeyBindingMethods native declaration : :167</i>
     */
    void deleteToEndOfLine(NSObject sender);

    /**
     * Original signature : <code>void deleteToBeginningOfParagraph(id)</code><br>
     * <i>from NSStandardKeyBindingMethods native declaration : :168</i>
     */
    void deleteToBeginningOfParagraph(NSObject sender);

    /**
     * Original signature : <code>void deleteToEndOfParagraph(id)</code><br>
     * <i>from NSStandardKeyBindingMethods native declaration : :169</i>
     */
    void deleteToEndOfParagraph(NSObject sender);

    /**
     * Original signature : <code>void yank(id)</code><br>
     * <i>from NSStandardKeyBindingMethods native declaration : :171</i>
     */
    void yank(NSObject sender);

    /**
     * Original signature : <code>void complete(id)</code><br>
     * <i>from NSStandardKeyBindingMethods native declaration : :175</i>
     */
    void complete(NSObject sender);

    /**
     * Original signature : <code>void setMark(id)</code><br>
     * <i>from NSStandardKeyBindingMethods native declaration : :179</i>
     */
    void setMark(NSObject sender);

    /**
     * Original signature : <code>void deleteToMark(id)</code><br>
     * <i>from NSStandardKeyBindingMethods native declaration : :180</i>
     */
    void deleteToMark(NSObject sender);

    /**
     * Original signature : <code>void selectToMark(id)</code><br>
     * <i>from NSStandardKeyBindingMethods native declaration : :181</i>
     */
    void selectToMark(NSObject sender);

    /**
     * Original signature : <code>void swapWithMark(id)</code><br>
     * <i>from NSStandardKeyBindingMethods native declaration : :182</i>
     */
    void swapWithMark(NSObject sender);

    /**
     * Original signature : <code>void cancelOperation(id)</code><br>
     * <i>from NSStandardKeyBindingMethods native declaration : :187</i>
     */
    void cancelOperation(NSObject sender);

    /**
     * Original signature : <code>NSUndoManager* undoManager()</code><br>
     * <i>from NSUndoSupport native declaration : :192</i>
     */
    com.sun.jna.Pointer undoManager();
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
    boolean presentError(NSError error);

    /**
     * Given that the receiver is about to present an error (perhaps by just forwarding it to the next responder), return the error that should actually be presented. The default implementation of this method merely returns the passed-in error.<br>
     * You can override this method to customize the presentation of errors by examining the passed-in error and if, for example, its localized description or recovery information is unsuitably generic, returning a more specific one. When you override this method always check the NSError's domain and code to discriminate between errors whose presentation you want to customize and those you don't. For those you don't just return [super willPresentError:error]. Don't make decisions based on the NSError's localized description, recovery suggestion, or recovery options because it's usually not a good idea to try to parse localized text.<br>
     * Original signature : <code>NSError* willPresentError(NSError*)</code><br>
     * <i>from NSErrorPresentation native declaration : :236</i>
     */
    NSError willPresentError(NSError error);
}
