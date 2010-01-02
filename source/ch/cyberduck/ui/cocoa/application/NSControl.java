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

import ch.cyberduck.ui.cocoa.foundation.NSAttributedString;
import ch.cyberduck.ui.cocoa.foundation.NSFormatter;
import ch.cyberduck.ui.cocoa.foundation.NSObject;

import org.rococoa.ID;
import org.rococoa.Selector;
import org.rococoa.cocoa.foundation.NSInteger;
import org.rococoa.cocoa.foundation.NSRect;

/// <i>native declaration : :10</i>
public abstract class NSControl extends NSView {

    /**
     * <i>native declaration : :29</i><br>
     * Conversion Error : /// Original signature : <code>id initWithFrame(null)</code><br>
     * - (id)initWithFrame:(null)frameRect; (Argument frameRect cannot be converted)
     */
    public abstract NSControl initWithFrame(NSRect frameRect);

    /**
     * Original signature : <code>void sizeToFit()</code><br>
     * <i>native declaration : :30</i>
     */
    public abstract void sizeToFit();

    /**
     * Original signature : <code>void calcSize()</code><br>
     * <i>native declaration : :31</i>
     */
    public abstract void calcSize();

    /**
     * Original signature : <code>id cell()</code><br>
     * <i>native declaration : :32</i>
     */
    public abstract NSCell cell();

    /**
     * Original signature : <code>void setCell(NSCell*)</code><br>
     * <i>native declaration : :33</i>
     */
    public abstract void setCell(NSTextFieldCell aCell);

    /**
     * Original signature : <code>id selectedCell()</code><br>
     * <i>native declaration : :34</i>
     */
    public abstract NSCell selectedCell();

    /**
     * Original signature : <code>id target()</code><br>
     * <i>native declaration : :35</i>
     */
    public abstract org.rococoa.ID target();

    /**
     * Original signature : <code>void setTarget(id)</code><br>
     * <i>native declaration : :36</i>
     */
    public abstract void setTarget(org.rococoa.ID anObject);

    /**
     * Original signature : <code>action()</code><br>
     * <i>native declaration : :37</i>
     */
    public abstract Selector action();

    /**
     * <i>native declaration : :38</i><br>
     * Conversion Error : /// Original signature : <code>void setAction(null)</code><br>
     * - (void)setAction:(null)aSelector; (Argument aSelector cannot be converted)
     */
    public abstract void setAction(Selector action);

    /**
     * Original signature : <code>void setTag(NSInteger)</code><br>
     * <i>native declaration : :40</i>
     */
    public abstract void setTag(NSInteger anInt);

    /**
     * Original signature : <code>NSInteger selectedTag()</code><br>
     * <i>native declaration : :41</i>
     */
    public abstract NSInteger selectedTag();

    /**
     * Original signature : <code>void setIgnoresMultiClick(BOOL)</code><br>
     * <i>native declaration : :42</i>
     */
    public abstract void setIgnoresMultiClick(boolean flag);

    /**
     * Original signature : <code>BOOL ignoresMultiClick()</code><br>
     * <i>native declaration : :43</i>
     */
    public abstract boolean ignoresMultiClick();

    /**
     * Original signature : <code>NSInteger sendActionOn(NSInteger)</code><br>
     * <i>native declaration : :44</i>
     */
    public abstract int sendActionOn(int mask);

    /**
     * Original signature : <code>BOOL isContinuous()</code><br>
     * <i>native declaration : :45</i>
     */
    public abstract boolean isContinuous();

    /**
     * Original signature : <code>void setContinuous(BOOL)</code><br>
     * <i>native declaration : :46</i>
     */
    public abstract void setContinuous(boolean flag);

    /**
     * Original signature : <code>BOOL isEnabled()</code><br>
     * <i>native declaration : :47</i>
     */
    public abstract boolean isEnabled();

    /**
     * Original signature : <code>void setEnabled(BOOL)</code><br>
     * <i>native declaration : :48</i>
     */
    public abstract void setEnabled(boolean flag);

    /**
     * Original signature : <code>void setFloatingPointFormat(BOOL, NSUInteger, NSUInteger)</code><br>
     * <i>native declaration : :49</i>
     */
    public abstract void setFloatingPointFormat_left_right(boolean autoRange, int leftDigits, int rightDigits);

    /**
     * Original signature : <code>alignment()</code><br>
     * <i>native declaration : :50</i>
     */
    public abstract NSObject alignment();
    /**
     * <i>native declaration : :51</i><br>
     * Conversion Error : /// Original signature : <code>void setAlignment(null)</code><br>
     * - (void)setAlignment:(null)mode; (Argument mode cannot be converted)
     */
    /**
     * Original signature : <code>NSFont* font()</code><br>
     * <i>native declaration : :52</i>
     */
    public abstract NSFont font();

    /**
     * Original signature : <code>void setFont(NSFont*)</code><br>
     * <i>native declaration : :53</i>
     */
    public abstract void setFont(NSFont fontObj);

    /**
     * Original signature : <code>void setFormatter(NSFormatter*)</code><br>
     * <i>native declaration : :54</i>
     */
    public abstract void setFormatter(NSFormatter newFormatter);

    /**
     * Original signature : <code>id formatter()</code><br>
     * <i>native declaration : :55</i>
     */
    public abstract NSObject formatter();
    /**
     * <i>native declaration : :56</i><br>
     * Conversion Error : id<NSCopying>
     */
    /**
     * Original signature : <code>void setStringValue(NSString*)</code><br>
     * <i>native declaration : :57</i>
     */
    public abstract void setStringValue(String aString);

    /**
     * Original signature : <code>void setIntValue(int)</code><br>
     * <i>native declaration : :58</i>
     */
    public abstract void setIntValue(int anInt);

    /**
     * Original signature : <code>void setFloatValue(float)</code><br>
     * <i>native declaration : :59</i>
     */
    public abstract void setFloatValue(float aFloat);

    /**
     * Original signature : <code>void setDoubleValue(double)</code><br>
     * <i>native declaration : :60</i>
     */
    public abstract void setDoubleValue(double aDouble);

    /**
     * Original signature : <code>id objectValue()</code><br>
     * <i>native declaration : :61</i>
     */
    public abstract NSObject objectValue();

    /**
     * Original signature : <code>NSString* stringValue()</code><br>
     * <i>native declaration : :62</i>
     */
    public abstract String stringValue();

    /**
     * Original signature : <code>int intValue()</code><br>
     * <i>native declaration : :63</i>
     */
    public abstract int intValue();

    /**
     * Original signature : <code>float floatValue()</code><br>
     * <i>native declaration : :64</i>
     */
    public abstract float floatValue();

    /**
     * Original signature : <code>double doubleValue()</code><br>
     * <i>native declaration : :65</i>
     */
    public abstract double doubleValue();

    /**
     * Original signature : <code>void setNeedsDisplay()</code><br>
     * <i>native declaration : :66</i>
     */
    public abstract void setNeedsDisplay();

    /**
     * Original signature : <code>void updateCell(NSCell*)</code><br>
     * <i>native declaration : :67</i>
     */
    public abstract void updateCell(NSCell aCell);

    /**
     * Original signature : <code>void updateCellInside(NSCell*)</code><br>
     * <i>native declaration : :68</i>
     */
    public abstract void updateCellInside(NSCell aCell);

    /**
     * Original signature : <code>void drawCellInside(NSCell*)</code><br>
     * <i>native declaration : :69</i>
     */
    public abstract void drawCellInside(NSCell aCell);

    /**
     * Original signature : <code>void drawCell(NSCell*)</code><br>
     * <i>native declaration : :70</i>
     */
    public abstract void drawCell(NSCell aCell);

    /**
     * Original signature : <code>void selectCell(NSCell*)</code><br>
     * <i>native declaration : :71</i>
     */
    public abstract void selectCell(NSCell aCell);
    /**
     * <i>native declaration : :73</i><br>
     * Conversion Error : /// Original signature : <code>BOOL sendAction(null, id)</code><br>
     * - (BOOL)sendAction:(null)theAction to:(id)theTarget; (Argument theAction cannot be converted)
     */
    /**
     * Original signature : <code>void takeIntValueFrom(id)</code><br>
     * <i>native declaration : :74</i>
     */
    public abstract void takeIntValueFrom(final ID sender);

    /**
     * Original signature : <code>void takeFloatValueFrom(id)</code><br>
     * <i>native declaration : :75</i>
     */
    public abstract void takeFloatValueFrom(final ID sender);

    /**
     * Original signature : <code>void takeDoubleValueFrom(id)</code><br>
     * <i>native declaration : :76</i>
     */
    public abstract void takeDoubleValueFrom(final ID sender);

    /**
     * Original signature : <code>void takeStringValueFrom(id)</code><br>
     * <i>native declaration : :77</i>
     */
    public abstract void takeStringValueFrom(final ID sender);

    /**
     * Original signature : <code>void takeObjectValueFrom(id)</code><br>
     * <i>native declaration : :78</i>
     */
    public abstract void takeObjectValueFrom(final ID sender);

    /**
     * Original signature : <code>NSText* currentEditor()</code><br>
     * <i>native declaration : :79</i>
     */
    public abstract NSText currentEditor();

    /**
     * Original signature : <code>BOOL abortEditing()</code><br>
     * <i>native declaration : :80</i>
     */
    public abstract boolean abortEditing();

    /**
     * Original signature : <code>void validateEditing()</code><br>
     * <i>native declaration : :81</i>
     */
    public abstract void validateEditing();

    /**
     * Original signature : <code>void mouseDown(NSEvent*)</code><br>
     * <i>native declaration : :82</i>
     */
    public abstract void mouseDown(NSEvent event);

    /**
     * Original signature : <code>baseWritingDirection()</code><br>
     * <i>native declaration : :85</i>
     */
    public abstract NSObject baseWritingDirection();
    /**
     * <i>native declaration : :86</i><br>
     * Conversion Error : /// Original signature : <code>void setBaseWritingDirection(null)</code><br>
     * - (void)setBaseWritingDirection:(null)writingDirection; (Argument writingDirection cannot be converted)
     */
    /**
     * Original signature : <code>NSInteger integerValue()</code><br>
     * <i>native declaration : :90</i>
     */
    public abstract int integerValue();

    /**
     * Original signature : <code>void setIntegerValue(NSInteger)</code><br>
     * <i>native declaration : :91</i>
     */
    public abstract void setIntegerValue(int anInteger);

    /**
     * Original signature : <code>void takeIntegerValueFrom(id)</code><br>
     * <i>native declaration : :92</i>
     */
    public abstract void takeIntegerValueFrom(org.rococoa.ID sender);

    /**
     * <i>from NSKeyboardUI native declaration : :98</i><br>
     * Conversion Error : /// Original signature : <code>void performClick(null)</code><br>
     * - (void)performClick:(null)sender; (Argument sender cannot be converted)
     */
    public abstract void performClick(org.rococoa.ID sender);

    /**
     * Original signature : <code>void setRefusesFirstResponder(BOOL)</code><br>
     * <i>from NSKeyboardUI native declaration : :99</i>
     */
    public abstract void setRefusesFirstResponder(boolean flag);

    /**
     * Original signature : <code>BOOL refusesFirstResponder()</code><br>
     * <i>from NSKeyboardUI native declaration : :100</i>
     */
    public abstract boolean refusesFirstResponder();

    /**
     * Original signature : <code>NSAttributedString* attributedStringValue()</code><br>
     * <i>from NSControlAttributedStringMethods native declaration : :135</i>
     */
    public abstract NSAttributedString attributedStringValue();

    /**
     * Original signature : <code>void setAttributedStringValue(NSAttributedString*)</code><br>
     * <i>from NSControlAttributedStringMethods native declaration : :136</i>
     */
    public abstract void setAttributedStringValue(NSAttributedString obj);

    public static final String NSControlTextDidBeginEditingNotification = "NSControlTextDidBeginEditingNotification";
    public static final String NSControlTextDidEndEditingNotification = "NSControlTextDidEndEditingNotification";
    public static final String NSControlTextDidChangeNotification = "NSControlTextDidChangeNotification";

}
