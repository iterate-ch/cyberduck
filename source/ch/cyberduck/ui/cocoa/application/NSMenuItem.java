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
import ch.cyberduck.ui.cocoa.foundation.NSCopying;
import ch.cyberduck.ui.cocoa.foundation.NSObject;

import org.rococoa.ObjCClass;
import org.rococoa.Selector;


/// <i>native declaration : :14</i>
public abstract class NSMenuItem extends NSObject implements NSCopying, NSValidatedUserInterfaceItem {
    private static final _Class CLASS = org.rococoa.Rococoa.createClass("NSMenuItem", _Class.class);

    public static NSMenuItem separatorItem() {
        return CLASS.separatorItem();
    }

    public static NSMenuItem itemWithTitle(String title, Selector selector, String charCode) {
        return CLASS.alloc().initWithTitle_action_keyEquivalent(title, selector, charCode);
    }

    public interface _Class extends ObjCClass {
        /**
         * Original signature : <code>void setUsesUserKeyEquivalents(BOOL)</code><br>
         * <i>native declaration : :44</i>
         */
        void setUsesUserKeyEquivalents(boolean flag);

        /**
         * Original signature : <code>BOOL usesUserKeyEquivalents()</code><br>
         * <i>native declaration : :45</i>
         */
        boolean usesUserKeyEquivalents();

        /**
         * Original signature : <code>NSMenuItem* separatorItem()</code><br>
         * <i>native declaration : :47</i>
         */
        NSMenuItem separatorItem();

        NSMenuItem alloc();
    }

    /**
     * Original signature : <code>id initWithTitle(NSString*, SEL, NSString*)</code><br>
     * <i>native declaration : :49</i>
     */
    public abstract NSMenuItem initWithTitle_action_keyEquivalent(String aString, org.rococoa.Selector aSelector, String charCode);

    /**
     * Original signature : <code>void setMenu(NSMenu*)</code><br>
     * <i>native declaration : :51</i>
     */
    public abstract void setMenu(NSMenu menu);

    /**
     * Original signature : <code>NSMenu* menu()</code><br>
     * <i>native declaration : :52</i>
     */
    public abstract NSMenu menu();

    /**
     * Original signature : <code>BOOL hasSubmenu()</code><br>
     * <i>native declaration : :56</i>
     */
    public abstract boolean hasSubmenu();

    /**
     * Original signature : <code>void setSubmenu(NSMenu*)</code><br>
     * <i>native declaration : :57</i>
     */
    public abstract void setSubmenu(NSMenu submenu);

    /**
     * Original signature : <code>NSMenu* submenu()</code><br>
     * <i>native declaration : :58</i>
     */
    public abstract NSMenu submenu();

    /**
     * Original signature : <code>void setTitle(NSString*)</code><br>
     * <i>native declaration : :60</i>
     */
    public abstract void setTitle(String aString);

    /**
     * Original signature : <code>NSString* title()</code><br>
     * <i>native declaration : :61</i>
     */
    public abstract String title();

    /**
     * Original signature : <code>void setAttributedTitle(NSAttributedString*)</code><br>
     * <i>native declaration : :63</i>
     */
    public abstract void setAttributedTitle(NSAttributedString string);

    /**
     * Original signature : <code>NSAttributedString* attributedTitle()</code><br>
     * <i>native declaration : :64</i>
     */
    public abstract NSAttributedString attributedTitle();

    /**
     * Original signature : <code>BOOL isSeparatorItem()</code><br>
     * <i>native declaration : :67</i>
     */
    public abstract boolean isSeparatorItem();

    /**
     * Original signature : <code>void setKeyEquivalent(NSString*)</code><br>
     * <i>native declaration : :69</i>
     */
    public abstract void setKeyEquivalent(String aKeyEquivalent);

    /**
     * Original signature : <code>NSString* keyEquivalent()</code><br>
     * <i>native declaration : :70</i>
     */
    public abstract String keyEquivalent();

    /**
     * Original signature : <code>void setKeyEquivalentModifierMask(NSUInteger)</code><br>
     * <i>native declaration : :71</i>
     */
    public abstract void setKeyEquivalentModifierMask(int mask);

    /**
     * Original signature : <code>NSUInteger keyEquivalentModifierMask()</code><br>
     * <i>native declaration : :72</i>
     */
    public abstract int keyEquivalentModifierMask();

    /**
     * Original signature : <code>NSString* userKeyEquivalent()</code><br>
     * <i>native declaration : :74</i>
     */
    public abstract String userKeyEquivalent();

    /**
     * Original signature : <code>void setMnemonicLocation(NSUInteger)</code><br>
     * <i>native declaration : :76</i>
     */
    public abstract void setMnemonicLocation(int location);

    /**
     * Original signature : <code>NSUInteger mnemonicLocation()</code><br>
     * <i>native declaration : :77</i>
     */
    public abstract int mnemonicLocation();

    /**
     * Original signature : <code>NSString* mnemonic()</code><br>
     * <i>native declaration : :78</i>
     */
    public abstract String mnemonic();

    /**
     * Original signature : <code>void setTitleWithMnemonic(NSString*)</code><br>
     * <i>native declaration : :79</i>
     */
    public abstract void setTitleWithMnemonic(String stringWithAmpersand);

    /**
     * Original signature : <code>void setImage(NSImage*)</code><br>
     * <i>native declaration : :81</i>
     */
    public abstract void setImage(NSImage menuImage);

    /**
     * Original signature : <code>NSImage* image()</code><br>
     * <i>native declaration : :82</i>
     */
    public abstract NSImage image();

    /**
     * Original signature : <code>void setState(NSInteger)</code><br>
     * <i>native declaration : :84</i>
     */
    public abstract void setState(int state);

    /**
     * Original signature : <code>NSInteger state()</code><br>
     * <i>native declaration : :85</i>
     */
    public abstract int state();

    /**
     * Original signature : <code>void setOnStateImage(NSImage*)</code><br>
     * checkmark by default<br>
     * <i>native declaration : :86</i>
     */
    public abstract void setOnStateImage(NSImage image);

    /**
     * Original signature : <code>NSImage* onStateImage()</code><br>
     * <i>native declaration : :87</i>
     */
    public abstract NSImage onStateImage();

    /**
     * Original signature : <code>void setOffStateImage(NSImage*)</code><br>
     * none by default<br>
     * <i>native declaration : :88</i>
     */
    public abstract void setOffStateImage(NSImage image);

    /**
     * Original signature : <code>NSImage* offStateImage()</code><br>
     * <i>native declaration : :89</i>
     */
    public abstract NSImage offStateImage();

    /**
     * Original signature : <code>void setMixedStateImage(NSImage*)</code><br>
     * horizontal line by default?<br>
     * <i>native declaration : :90</i>
     */
    public abstract void setMixedStateImage(NSImage image);

    /**
     * Original signature : <code>NSImage* mixedStateImage()</code><br>
     * <i>native declaration : :91</i>
     */
    public abstract NSImage mixedStateImage();

    /**
     * Original signature : <code>void setEnabled(BOOL)</code><br>
     * <i>native declaration : :93</i>
     */
    public abstract void setEnabled(boolean flag);

    /**
     * Original signature : <code>BOOL isEnabled()</code><br>
     * <i>native declaration : :94</i>
     */
    public abstract boolean isEnabled();

    /**
     * Original signature : <code>void setAlternate(BOOL)</code><br>
     * <i>native declaration : :98</i>
     */
    public abstract void setAlternate(boolean isAlternate);

    /**
     * Original signature : <code>BOOL isAlternate()</code><br>
     * <i>native declaration : :99</i>
     */
    public abstract boolean isAlternate();

    /**
     * Original signature : <code>void setIndentationLevel(NSInteger)</code><br>
     * <i>native declaration : :101</i>
     */
    public abstract void setIndentationLevel(int indentationLevel);

    /**
     * Original signature : <code>NSInteger indentationLevel()</code><br>
     * <i>native declaration : :102</i>
     */
    public abstract int indentationLevel();

    /**
     * Original signature : <code>void setTarget(id)</code><br>
     * <i>native declaration : :105</i>
     */
    public abstract void setTarget(org.rococoa.ID anObject);

    /**
     * Original signature : <code>id target()</code><br>
     * <i>native declaration : :106</i>
     */
    public abstract org.rococoa.ID target();

    /**
     * Original signature : <code>void setAction(SEL)</code><br>
     * <i>native declaration : :107</i>
     */
    public abstract void setAction(org.rococoa.Selector aSelector);

    /**
     * Original signature : <code>void setTag(NSInteger)</code><br>
     * <i>native declaration : :110</i>
     */
    public abstract void setTag(int anInt);

    /**
     * Original signature : <code>void setRepresentedObject(id)</code><br>
     * <i>native declaration : :113</i>
     */
    public abstract void setRepresentedObject(String anObject);

    /**
     * Original signature : <code>id representedObject()</code><br>
     * <i>native declaration : :114</i>
     */
    public abstract String representedObject();

    /**
     * Set (and get) the view for a menu item.  By default, a menu item has a nil view.<br>
     * A menu item with a view does not draw its title, state, font, or other standard drawing attributes, and assigns drawing responsibility entirely to the view.  Keyboard equivalents and type-select continue to use the key equivalent and title as normal.<br>
     * A menu item with a view sizes itself according to the view's frame, and the width of the other menu items.  The menu item will always be at least as wide as its view, but it may be wider.  If you want your view to auto-expand to fill the menu item, then make sure that its autoresizing mask has NSViewWidthSizable set; in that case, the view's width at the time setView: is called will be treated as the minimum width for the view.  A menu will resize itself as its containing views change frame size.  Changes to the view's frame during tracking are reflected immediately in the menu.<br>
     * A view in a menu item will receive mouse and keyboard events normally.  During non-sticky menu tracking (manipulating menus with the mouse button held down), a view in a menu item will receive mouseDragged: events.<br>
     * Animation is possible via the usual mechanism (set a timer to call setNeedsDisplay: or display), but because menu tracking occurs in the NSEventTrackingRunLoopMode, you must add the timer to the run loop in that mode.<br>
     * When the menu is opened, the view is added to a window; when the menu is closed the view is removed from the window.  Override viewDidMoveToWindow in your view for a convenient place to start/stop animations, reset tracking rects, etc., but do not attempt to move or otherwise modify the window.<br>
     * When a menu item is copied via NSCopying, any attached view is copied via archiving/unarchiving.  Menu item views are not supported in the Dock menu.<br>
     * Original signature : <code>void setView(NSView*)</code><br>
     * <i>native declaration : :124</i>
     */
    public abstract void setView(NSView view);

    /**
     * Original signature : <code>NSView* view()</code><br>
     * <i>native declaration : :125</i>
     */
    public abstract NSView view();

    /**
     * Indicates whether the menu item should be drawn highlighted or not.<br>
     * Original signature : <code>BOOL isHighlighted()</code><br>
     * <i>native declaration : :128</i>
     */
    public abstract boolean isHighlighted();

    /**
     * Set (and get) the visibility of a menu item.  Hidden menu items (or items with a hidden superitem) do not appear in a menu and do not participate in command key matching.  isHiddenOrHasHiddenAncestor returns YES if the item is hidden or any of its superitems are hidden.<br>
     * Original signature : <code>void setHidden(BOOL)</code><br>
     * <i>native declaration : :131</i>
     */
    public abstract void setHidden(boolean hidden);

    /**
     * Original signature : <code>BOOL isHidden()</code><br>
     * <i>native declaration : :132</i>
     */
    public abstract boolean isHidden();

    /**
     * Original signature : <code>BOOL isHiddenOrHasHiddenAncestor()</code><br>
     * <i>native declaration : :133</i>
     */
    public abstract boolean isHiddenOrHasHiddenAncestor();

    /**
     * Original signature : <code>void setToolTip(NSString*)</code><br>
     * <i>native declaration : :138</i>
     */
    public abstract void setToolTip(String toolTip);

    /**
     * Original signature : <code>NSString* toolTip()</code><br>
     * <i>native declaration : :139</i>
     */
    public abstract String toolTip();
}
