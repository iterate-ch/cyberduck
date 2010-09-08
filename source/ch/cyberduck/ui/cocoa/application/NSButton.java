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

import org.rococoa.ObjCClass;
import org.rococoa.cocoa.foundation.NSRect;
import org.rococoa.cocoa.foundation.NSUInteger;

/// <i>native declaration : :15</i>
public abstract class NSButton extends NSControl {
    private static final _Class CLASS = org.rococoa.Rococoa.createClass("NSButton", _Class.class);

    public static NSButton buttonWithFrame(NSRect frameRect) {
        return CLASS.alloc().initWithFrame(frameRect);
    }

    public static final int NSMomentaryLightButton = 0;    // was NSMomentaryPushButton
    public static final int NSMomentaryPushButtonButton = 1;
    public static final int NSToggleButton = 2;
    public static final int NSSwitchButton = 3;
    public static final int NSRadioButton = 4;
    public static final int NSMomentaryChangeButton = 5;
    public static final int NSOnOffButton = 6;
    public static final int NSMomentaryPushInButton = 7;    // was NSMomentaryLight

    public static final int NSRoundedBezelStyle = 1;
    public static final int NSRegularSquareBezelStyle = 2;
    public static final int NSThickSquareBezelStyle = 3;
    public static final int NSThickerSquareBezelStyle = 4;
    public static final int NSDisclosureBezelStyle = 5;
    public static final int NSShadowlessSquareBezelStyle = 6;
    public static final int NSCircularBezelStyle = 7;
    public static final int NSTexturedSquareBezelStyle = 8;
    public static final int NSHelpButtonBezelStyle = 9;
    public static final int NSSmallSquareBezelStyle = 10;
    public static final int NSTexturedRoundedBezelStyle = 11;
    public static final int NSRoundRectBezelStyle = 12;
    public static final int NSRecessedBezelStyle = 13;
    public static final int NSRoundedDisclosureBezelStyle = 14;

    public interface _Class extends ObjCClass {
        NSButton alloc();
    }

    @Override
    public abstract NSButton initWithFrame(NSRect frameRect);

    /**
     * Original signature : <code>NSString* title()</code><br>
     * <i>native declaration : :17</i>
     */
    public abstract String title();

    /**
     * Original signature : <code>void setTitle(NSString*)</code><br>
     * <i>native declaration : :18</i>
     */
    public abstract void setTitle(String aString);

    /**
     * Original signature : <code>NSString* alternateTitle()</code><br>
     * <i>native declaration : :19</i>
     */
    public abstract String alternateTitle();

    /**
     * Original signature : <code>void setAlternateTitle(NSString*)</code><br>
     * <i>native declaration : :20</i>
     */
    public abstract void setAlternateTitle(String aString);

    /**
     * Original signature : <code>NSImage* image()</code><br>
     * <i>native declaration : :21</i>
     */
    public abstract NSImage image();

    /**
     * Original signature : <code>void setImage(NSImage*)</code><br>
     * <i>native declaration : :22</i>
     */
    public abstract void setImage(NSImage image);

    /**
     * Original signature : <code>NSImage* alternateImage()</code><br>
     * <i>native declaration : :23</i>
     */
    public abstract NSImage alternateImage();

    /**
     * Original signature : <code>void setAlternateImage(NSImage*)</code><br>
     * <i>native declaration : :24</i>
     */
    public abstract void setAlternateImage(NSImage image);

    /**
     * Original signature : <code>imagePosition()</code><br>
     * <i>native declaration : :25</i>
     */
    public abstract com.sun.jna.Pointer imagePosition();

    /**
     * <i>native declaration : :26</i><br>
     * Conversion Error : /// Original signature : <code>void setImagePosition(null)</code><br>
     * - (void)setImagePosition:(null)aPosition; (Argument aPosition cannot be converted)
     */
    public abstract void setImagePosition(int position);

    /**
     * <i>native declaration : :27</i><br>
     * Conversion Error : /// Original signature : <code>void setButtonType(null)</code><br>
     * - (void)setButtonType:(null)aType; (Argument aType cannot be converted)
     */
    public abstract void setButtonType(int type);

    /**
     * Original signature : <code>NSInteger state()</code><br>
     * <i>native declaration : :28</i>
     */
    public abstract int state();

    /**
     * Original signature : <code>void setState(NSInteger)</code><br>
     * <i>native declaration : :29</i>
     */
    public abstract void setState(int value);

    /**
     * Original signature : <code>BOOL isBordered()</code><br>
     * <i>native declaration : :30</i>
     */
    public abstract boolean isBordered();

    /**
     * Original signature : <code>void setBordered(BOOL)</code><br>
     * <i>native declaration : :31</i>
     */
    public abstract void setBordered(boolean flag);

    /**
     * Original signature : <code>BOOL isTransparent()</code><br>
     * <i>native declaration : :32</i>
     */
    public abstract boolean isTransparent();

    /**
     * Original signature : <code>void setTransparent(BOOL)</code><br>
     * <i>native declaration : :33</i>
     */
    public abstract void setTransparent(boolean flag);

    /**
     * Original signature : <code>void setPeriodicDelay(float, float)</code><br>
     * <i>native declaration : :34</i>
     */
    public abstract void setPeriodicDelay_interval(float delay, float interval);

    /**
     * Original signature : <code>void getPeriodicDelay(float*, float*)</code><br>
     * <i>native declaration : :35</i>
     */
    public abstract void getPeriodicDelay_interval(java.nio.FloatBuffer delay, java.nio.FloatBuffer interval);

    /**
     * Original signature : <code>NSString* keyEquivalent()</code><br>
     * <i>native declaration : :36</i>
     */
    public abstract String keyEquivalent();

    /**
     * Original signature : <code>void setKeyEquivalent(NSString*)</code><br>
     * <i>native declaration : :37</i>
     */
    public abstract void setKeyEquivalent(String charCode);

    /**
     * Original signature : <code>NSUInteger keyEquivalentModifierMask()</code><br>
     * <i>native declaration : :38</i>
     */
    public abstract NSUInteger keyEquivalentModifierMask();

    /**
     * Original signature : <code>void setKeyEquivalentModifierMask(NSUInteger)</code><br>
     * <i>native declaration : :39</i>
     */
    public abstract void setKeyEquivalentModifierMask(NSUInteger mask);

    /**
     * Original signature : <code>void highlight(BOOL)</code><br>
     * <i>native declaration : :40</i>
     */
    public abstract void highlight(boolean flag);

    /**
     * Original signature : <code>void setTitleWithMnemonic(NSString*)</code><br>
     * <i>from NSKeyboardUI native declaration : :46</i>
     */
    public abstract void setTitleWithMnemonic(String stringWithAmpersand);

    /**
     * Original signature : <code>NSAttributedString* attributedTitle()</code><br>
     * <i>from NSButtonAttributedStringMethods native declaration : :50</i>
     */
    public abstract NSAttributedString attributedTitle();

    /**
     * Original signature : <code>void setAttributedTitle(NSAttributedString*)</code><br>
     * <i>from NSButtonAttributedStringMethods native declaration : :51</i>
     */
    public abstract void setAttributedTitle(NSAttributedString aString);

    /**
     * Original signature : <code>NSAttributedString* attributedAlternateTitle()</code><br>
     * <i>from NSButtonAttributedStringMethods native declaration : :52</i>
     */
    public abstract NSAttributedString attributedAlternateTitle();

    /**
     * Original signature : <code>void setAttributedAlternateTitle(NSAttributedString*)</code><br>
     * <i>from NSButtonAttributedStringMethods native declaration : :53</i>
     */
    public abstract void setAttributedAlternateTitle(NSAttributedString obj);

    /**
     * <i>from NSButtonBezelStyles native declaration : :57</i><br>
     * Conversion Error : /// Original signature : <code>void setBezelStyle(null)</code><br>
     * - (void)setBezelStyle:(null)bezelStyle; (Argument bezelStyle cannot be converted)
     */
    public abstract void setBezelStyle(int style);

    /**
     * Original signature : <code>bezelStyle()</code><br>
     * <i>from NSButtonBezelStyles native declaration : :58</i>
     */
    public abstract int bezelStyle();

    /**
     * Original signature : <code>void setAllowsMixedState(BOOL)</code><br>
     * <i>from NSButtonMixedState native declaration : :62</i>
     */
    public abstract void setAllowsMixedState(boolean flag);

    /**
     * Original signature : <code>BOOL allowsMixedState()</code><br>
     * <i>from NSButtonMixedState native declaration : :63</i>
     */
    public abstract boolean allowsMixedState();

    /**
     * Original signature : <code>void setNextState()</code><br>
     * <i>from NSButtonMixedState native declaration : :64</i>
     */
    public abstract void setNextState();

    /**
     * Original signature : <code>void setShowsBorderOnlyWhileMouseInside(BOOL)</code><br>
     * <i>from NSButtonBorder native declaration : :68</i>
     */
    public abstract void setShowsBorderOnlyWhileMouseInside(boolean show);

    /**
     * Original signature : <code>BOOL showsBorderOnlyWhileMouseInside()</code><br>
     * <i>from NSButtonBorder native declaration : :69</i>
     */
    public abstract boolean showsBorderOnlyWhileMouseInside();

    /**
     * Original signature : <code>void setSound(NSSound*)</code><br>
     * <i>from NSButtonSoundExtensions native declaration : :73</i>
     */
    public abstract void setSound(com.sun.jna.Pointer aSound);

    /**
     * Original signature : <code>NSSound* sound()</code><br>
     * <i>from NSButtonSoundExtensions native declaration : :74</i>
     */
    public abstract com.sun.jna.Pointer sound();
}
