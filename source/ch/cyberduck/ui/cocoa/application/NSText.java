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

import ch.cyberduck.ui.cocoa.foundation.NSData;
import ch.cyberduck.ui.cocoa.foundation.NSObject;
import ch.cyberduck.ui.cocoa.foundation.NSRange;

import org.rococoa.NSClass;
import org.rococoa.Rococoa;
import org.rococoa.cocoa.NSSize;

// BridgeSupport v 0.017
public interface NSText extends NSView {
    public static final _Class CLASS = Rococoa.createClass("NSText", _Class.class);

    public interface _Class extends NSClass {
    }

    public static final String TextDidBeginEditingNotification = "NSTextDidBeginEditingNotification";
    public static final String TextDidEndEditingNotification = "NSTextDidEndEditingNotification";
    public static final String TextDidChangeNotification = "NSTextDidChangeNotification";
    public static final int LeftTextAlignment = 0;
    public static final int RightTextAlignment = 1;
    public static final int CenterTextAlignment = 2;
    public static final int JustifiedTextAlignment = 3;
    public static final int NaturalTextAlignment = 4;
    public static final int IllegalTextMovement = 0;
    public static final int ReturnTextMovement = 16;
    public static final int TabTextMovement = 17;
    public static final int BacktabTextMovement = 18;
    public static final int LeftTextMovement = 19;
    public static final int RightTextMovement = 20;
    public static final int UpTextMovement = 21;
    public static final int DownTextMovement = 22;
    public static final int CancelTextMovement = 23;
    public static final int OtherTextMovement = 0;
    public static final char ParagraphSeparatorCharacter = 8233;
    public static final char LineSeparatorCharacter = 8232;
    public static final char TabCharacter = 9;
    public static final char FormFeedCharacter = 12;
    public static final char NewlineCharacter = 10;
    public static final char CarriageReturnCharacter = 13;
    public static final char EnterCharacter = 3;
    public static final char BackspaceCharacter = 8;
    public static final char DeleteCharacter = 127;

    NSData RTFDFromRange(NSRange range);

    NSData RTFFromRange(NSRange range);

    void alignCenter(NSObject sender);

    void alignLeft(NSObject sender);

    void alignRight(NSObject sender);

    int alignment();

    NSColor backgroundColor();

    int baseWritingDirection();

    void changeFont(NSObject sender);

    void checkSpelling(NSObject sender);

    void copy(NSObject sender);

    void copyFont(NSObject sender);

    void copyRuler(NSObject sender);

    void cut(NSObject sender);

    NSObject delegate();

    void delete(NSObject sender);

    boolean drawsBackground();

    //	NSFont font();
    boolean importsGraphics();

    boolean isEditable();

    boolean isFieldEditor();

    boolean isHorizontallyResizable();

    boolean isRichText();

    boolean isRulerVisible();

    boolean isSelectable();

    boolean isVerticallyResizable();

    NSSize maxSize();

    NSSize minSize();

    void paste(NSObject sender);

    void pasteFont(NSObject sender);

    void pasteRuler(NSObject sender);

    boolean readRTFDFromFile(String path);

    void replaceCharactersInRange_withRTF(NSRange range, NSData rtfData);

    void replaceCharactersInRange_withRTFD(NSRange range, NSData rtfdData);

    void replaceCharactersInRange_withString(NSRange range, String aString);

    void scrollRangeToVisible(NSRange range);

    void selectAll(NSObject sender);

    NSRange selectedRange();

    void setAlignment(int mode);

    void setBackgroundColor(NSColor color);

    void setBaseWritingDirection(int writingDirection);

    void setDelegate(org.rococoa.ID anObject);

    void setDrawsBackground(boolean flag);

    void setEditable(boolean flag);

    void setFieldEditor(boolean flag);

    void setFont(NSFont obj);

    //	void setFont_range(NSFont font, NSRange range);
    void setHorizontallyResizable(boolean flag);

    void setImportsGraphics(boolean flag);

    void setMaxSize(NSSize newMaxSize);

    void setMinSize(NSSize newMinSize);

    void setRichText(boolean flag);

    void setSelectable(boolean flag);

    void setSelectedRange(NSRange range);

    void setString(String string);

    void setTextColor(NSColor color);

    void setTextColor_range(NSColor color, NSRange range);

    void setUsesFontPanel(boolean flag);

    void setVerticallyResizable(boolean flag);

    void showGuessPanel(NSObject sender);

    void sizeToFit();

    String string();

    void subscript(NSObject sender);

    void superscript(NSObject sender);

    NSColor textColor();

    void toggleRuler(NSObject sender);

    void underline(NSObject sender);

    void unscript(NSObject sender);

    boolean usesFontPanel();

    boolean writeRTFDToFile_atomically(String path, boolean flag);
}
