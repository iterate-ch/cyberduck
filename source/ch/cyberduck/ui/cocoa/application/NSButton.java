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

import org.rococoa.Rococoa;
import org.rococoa.cocoa.NSRect;

/// <i>native declaration : :15</i>
public interface NSButton extends NSControl {
    _Class CLASS = org.rococoa.Rococoa.createClass("NSButton", _Class.class);

    public static class Factory {
        public static NSButton create(NSRect frameRect) {
            return Rococoa.cast(CLASS.alloc().initWithFrame(frameRect).autorelease(), NSButton.class);
        }
    }

    int NSMomentaryLightButton = 0;    // was NSMomentaryPushButton
    int NSMomentaryPushButtonButton = 1;
    int NSToggleButton = 2;
    int NSSwitchButton = 3;
    int NSRadioButton = 4;
    int NSMomentaryChangeButton = 5;
    int NSOnOffButton = 6;
    int NSMomentaryPushInButton = 7;    // was NSMomentaryLight

    int NSRoundedBezelStyle = 1;
    int NSRegularSquareBezelStyle = 2;
    int NSThickSquareBezelStyle = 3;
    int NSThickerSquareBezelStyle = 4;
    int NSDisclosureBezelStyle = 5;
    int NSShadowlessSquareBezelStyle = 6;
    int NSCircularBezelStyle = 7;
    int NSTexturedSquareBezelStyle = 8;
    int NSHelpButtonBezelStyle = 9;
    int NSSmallSquareBezelStyle = 10;
    int NSTexturedRoundedBezelStyle = 11;
    int NSRoundRectBezelStyle = 12;
    int NSRecessedBezelStyle = 13;
    int NSRoundedDisclosureBezelStyle = 14;

    public interface _Class extends org.rococoa.NSClass {
        NSButton alloc();
    }

    /**
     * Original signature : <code>NSString* title()</code><br>
     * <i>native declaration : :17</i>
     */
    String title();

    /**
     * Original signature : <code>void setTitle(NSString*)</code><br>
     * <i>native declaration : :18</i>
     */
    void setTitle(String aString);

    /**
     * Original signature : <code>NSString* alternateTitle()</code><br>
     * <i>native declaration : :19</i>
     */
    String alternateTitle();

    /**
     * Original signature : <code>void setAlternateTitle(NSString*)</code><br>
     * <i>native declaration : :20</i>
     */
    void setAlternateTitle(String aString);

    /**
     * Original signature : <code>NSImage* image()</code><br>
     * <i>native declaration : :21</i>
     */
    NSImage image();

    /**
     * Original signature : <code>void setImage(NSImage*)</code><br>
     * <i>native declaration : :22</i>
     */
    void setImage(NSImage image);

    /**
     * Original signature : <code>NSImage* alternateImage()</code><br>
     * <i>native declaration : :23</i>
     */
    NSImage alternateImage();

    /**
     * Original signature : <code>void setAlternateImage(NSImage*)</code><br>
     * <i>native declaration : :24</i>
     */
    void setAlternateImage(NSImage image);

    /**
     * Original signature : <code>imagePosition()</code><br>
     * <i>native declaration : :25</i>
     */
    com.sun.jna.Pointer imagePosition();

    /**
     * <i>native declaration : :26</i><br>
     * Conversion Error : /// Original signature : <code>void setImagePosition(null)</code><br>
     * - (void)setImagePosition:(null)aPosition; (Argument aPosition cannot be converted)
     */
    public void setImagePosition(int position);

    /**
     * <i>native declaration : :27</i><br>
     * Conversion Error : /// Original signature : <code>void setButtonType(null)</code><br>
     * - (void)setButtonType:(null)aType; (Argument aType cannot be converted)
     */
    void setButtonType(int type);

    /**
     * Original signature : <code>NSInteger state()</code><br>
     * <i>native declaration : :28</i>
     */
    int state();

    /**
     * Original signature : <code>void setState(NSInteger)</code><br>
     * <i>native declaration : :29</i>
     */
    void setState(int value);

    /**
     * Original signature : <code>BOOL isBordered()</code><br>
     * <i>native declaration : :30</i>
     */
    boolean isBordered();

    /**
     * Original signature : <code>void setBordered(BOOL)</code><br>
     * <i>native declaration : :31</i>
     */
    void setBordered(boolean flag);

    /**
     * Original signature : <code>BOOL isTransparent()</code><br>
     * <i>native declaration : :32</i>
     */
    boolean isTransparent();

    /**
     * Original signature : <code>void setTransparent(BOOL)</code><br>
     * <i>native declaration : :33</i>
     */
    void setTransparent(boolean flag);

    /**
     * Original signature : <code>void setPeriodicDelay(float, float)</code><br>
     * <i>native declaration : :34</i>
     */
    void setPeriodicDelay_interval(float delay, float interval);

    /**
     * Original signature : <code>void getPeriodicDelay(float*, float*)</code><br>
     * <i>native declaration : :35</i><br>
     *
     * @deprecated use the safer method {@link #getPeriodicDelay_interval(java.nio.FloatBuffer, java.nio.FloatBuffer)} instead
     */
    @java.lang.Deprecated
    void getPeriodicDelay_interval(com.sun.jna.ptr.FloatByReference delay, com.sun.jna.ptr.FloatByReference interval);

    /**
     * Original signature : <code>void getPeriodicDelay(float*, float*)</code><br>
     * <i>native declaration : :35</i>
     */
    void getPeriodicDelay_interval(java.nio.FloatBuffer delay, java.nio.FloatBuffer interval);

    /**
     * Original signature : <code>NSString* keyEquivalent()</code><br>
     * <i>native declaration : :36</i>
     */
    String keyEquivalent();

    /**
     * Original signature : <code>void setKeyEquivalent(NSString*)</code><br>
     * <i>native declaration : :37</i>
     */
    void setKeyEquivalent(String charCode);

    /**
     * Original signature : <code>NSUInteger keyEquivalentModifierMask()</code><br>
     * <i>native declaration : :38</i>
     */
    int keyEquivalentModifierMask();

    /**
     * Original signature : <code>void setKeyEquivalentModifierMask(NSUInteger)</code><br>
     * <i>native declaration : :39</i>
     */
    void setKeyEquivalentModifierMask(int mask);

    /**
     * Original signature : <code>void highlight(BOOL)</code><br>
     * <i>native declaration : :40</i>
     */
    void highlight(boolean flag);

    /**
     * Original signature : <code>BOOL performKeyEquivalent(NSEvent*)</code><br>
     * <i>native declaration : :41</i>
     */
    boolean performKeyEquivalent(NSEvent key);

    /**
     * Original signature : <code>void setTitleWithMnemonic(NSString*)</code><br>
     * <i>from NSKeyboardUI native declaration : :46</i>
     */
    void setTitleWithMnemonic(String stringWithAmpersand);

    /**
     * Original signature : <code>NSAttributedString* attributedTitle()</code><br>
     * <i>from NSButtonAttributedStringMethods native declaration : :50</i>
     */
    NSAttributedString attributedTitle();

    /**
     * Original signature : <code>void setAttributedTitle(NSAttributedString*)</code><br>
     * <i>from NSButtonAttributedStringMethods native declaration : :51</i>
     */
    void setAttributedTitle(NSAttributedString aString);

    /**
     * Original signature : <code>NSAttributedString* attributedAlternateTitle()</code><br>
     * <i>from NSButtonAttributedStringMethods native declaration : :52</i>
     */
    NSAttributedString attributedAlternateTitle();

    /**
     * Original signature : <code>void setAttributedAlternateTitle(NSAttributedString*)</code><br>
     * <i>from NSButtonAttributedStringMethods native declaration : :53</i>
     */
    void setAttributedAlternateTitle(NSAttributedString obj);

    /**
     * <i>from NSButtonBezelStyles native declaration : :57</i><br>
     * Conversion Error : /// Original signature : <code>void setBezelStyle(null)</code><br>
     * - (void)setBezelStyle:(null)bezelStyle; (Argument bezelStyle cannot be converted)
     */
    void setBezelStyle(int style);

    /**
     * Original signature : <code>bezelStyle()</code><br>
     * <i>from NSButtonBezelStyles native declaration : :58</i>
     */
    int bezelStyle();

    /**
     * Original signature : <code>void setAllowsMixedState(BOOL)</code><br>
     * <i>from NSButtonMixedState native declaration : :62</i>
     */
    void setAllowsMixedState(boolean flag);

    /**
     * Original signature : <code>BOOL allowsMixedState()</code><br>
     * <i>from NSButtonMixedState native declaration : :63</i>
     */
    boolean allowsMixedState();

    /**
     * Original signature : <code>void setNextState()</code><br>
     * <i>from NSButtonMixedState native declaration : :64</i>
     */
    void setNextState();

    /**
     * Original signature : <code>void setShowsBorderOnlyWhileMouseInside(BOOL)</code><br>
     * <i>from NSButtonBorder native declaration : :68</i>
     */
    void setShowsBorderOnlyWhileMouseInside(boolean show);

    /**
     * Original signature : <code>BOOL showsBorderOnlyWhileMouseInside()</code><br>
     * <i>from NSButtonBorder native declaration : :69</i>
     */
    boolean showsBorderOnlyWhileMouseInside();

    /**
     * Original signature : <code>void setSound(NSSound*)</code><br>
     * <i>from NSButtonSoundExtensions native declaration : :73</i>
     */
    void setSound(com.sun.jna.Pointer aSound);

    /**
     * Original signature : <code>NSSound* sound()</code><br>
     * <i>from NSButtonSoundExtensions native declaration : :74</i>
     */
    com.sun.jna.Pointer sound();
}
