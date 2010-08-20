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
import ch.cyberduck.ui.cocoa.foundation.NSCopying;
import ch.cyberduck.ui.cocoa.foundation.NSObject;
import ch.cyberduck.ui.cocoa.foundation.NSZone;

import org.rococoa.ID;
import org.rococoa.ObjCClass;
import org.rococoa.Selector;
import org.rococoa.cocoa.foundation.NSInteger;

public abstract class NSMenu extends NSObject implements NSCopying {
    private static final _Class CLASS = org.rococoa.Rococoa.createClass("NSMenu", _Class.class);

    public static NSMenu menu() {
        return CLASS.alloc().init();
    }

    public static NSMenu menuWithTitle(String title) {
        return CLASS.alloc().initWithTitle(title);
    }

    public interface _Class extends ObjCClass {
        /**
         * Original signature : <code>void setMenuZone(NSZone*)</code><br>
         * <i>native declaration : :41</i>
         */
        void setMenuZone(NSZone aZone);

        /**
         * Original signature : <code>NSZone* menuZone()</code><br>
         * <i>native declaration : :42</i>
         */
        NSZone menuZone();

        /**
         * Original signature : <code>void popUpContextMenu(NSMenu*, NSEvent*, NSView*)</code><br>
         * <i>native declaration : :44</i>
         */
        void popUpContextMenu_withEvent_forView(NSMenu menu, NSEvent event, NSView view);

        /**
         * Original signature : <code>void popUpContextMenu(NSMenu*, NSEvent*, NSView*, NSFont*)</code><br>
         * <i>native declaration : :46</i>
         */
        void popUpContextMenu_withEvent_forView_withFont(NSMenu menu, NSEvent event, NSView view, NSFont font);

        /**
         * Original signature : <code>void setMenuBarVisible(BOOL)</code><br>
         * <i>native declaration : :50</i>
         */
        void setMenuBarVisible(boolean visible);

        /**
         * Original signature : <code>BOOL menuBarVisible()</code><br>
         * <i>native declaration : :51</i>
         */
        boolean menuBarVisible();

        NSMenu alloc();
    }

    public static interface Delegate {
        /**
         * @param menu
         * @return If you return a positive value, the menu is resized by either removing or adding items.
         *         Newly created items are blank. After the menu is resized, your menu:updateItem:atIndex:shouldCancel: method
         *         is called for each item. If you return a negative value, the number of items is left unchanged
         *         and menu:updateItem:atIndex:shouldCancel: is not called. If you can populate the menu quickly,
         *         you can implement menuNeedsUpdate: instead of numberOfItemsInMenu: and menu:updateItem:atIndex:shouldCancel:.
         */
        NSInteger numberOfItemsInMenu(NSMenu menu);

        boolean menu_updateItem_atIndex_shouldCancel(NSMenu menu, NSMenuItem item, NSInteger index, boolean shouldCancel);
    }

    public abstract NSMenu init();

    /**
     * Original signature : <code>id initWithTitle(NSString*)</code><br>
     * <i>native declaration : :54</i>
     */
    public abstract NSMenu initWithTitle(String aTitle);

    /**
     * Original signature : <code>void setTitle(NSString*)</code><br>
     * <i>native declaration : :56</i>
     */
    public abstract void setTitle(String aString);

    /**
     * Original signature : <code>NSString* title()</code><br>
     * <i>native declaration : :57</i>
     */
    public abstract String title();

    /**
     * Original signature : <code>void setSupermenu(NSMenu*)</code><br>
     * <i>native declaration : :59</i>
     */
    public abstract void setSupermenu(NSMenu supermenu);

    /**
     * Original signature : <code>NSMenu* supermenu()</code><br>
     * <i>native declaration : :60</i>
     */
    public abstract NSMenu supermenu();

    /**
     * Original signature : <code>void insertItem(NSMenuItem*, NSInteger)</code><br>
     * <i>native declaration : :63</i>
     */
    public abstract void insertItem_atIndex(NSMenuItem newItem, NSInteger index);

    /**
     * Original signature : <code>void addItem(NSMenuItem*)</code><br>
     * <i>native declaration : :64</i>
     */
    public abstract void addItem(NSMenuItem newItem);

    /**
     * <i>native declaration : :65</i><br>
     * Conversion Error : /// Original signature : <code>NSMenuItem* insertItemWithTitle(NSString*, null, NSString*, NSInteger)</code><br>
     * - (NSMenuItem*)insertItemWithTitle:(NSString*)aString action:(null)aSelector keyEquivalent:(NSString*)charCode atIndex:(NSInteger)index; (Argument aSelector cannot be converted)
     */
    public abstract NSMenuItem insertItemWithTitle_action_keyEquivalent_atIndex(String title, Selector action, String charCode, NSInteger index);

    /**
     * <i>native declaration : :66</i><br>
     * Conversion Error : /// Original signature : <code>NSMenuItem* addItemWithTitle(NSString*, null, NSString*)</code><br>
     * - (NSMenuItem*)addItemWithTitle:(NSString*)aString action:(null)aSelector keyEquivalent:(NSString*)charCode; (Argument aSelector cannot be converted)
     */
    public abstract NSMenuItem addItemWithTitle_action_keyEquivalent(String title, Selector action, String charCode);

    /**
     * Original signature : <code>void removeItemAtIndex(NSInteger)</code><br>
     * <i>native declaration : :67</i>
     */
    public abstract void removeItemAtIndex(NSInteger index);

    /**
     * Original signature : <code>void removeItem(NSMenuItem*)</code><br>
     * <i>native declaration : :68</i>
     */
    public abstract void removeItem(NSMenuItem item);

    /**
     * Original signature : <code>void setSubmenu(NSMenu*, NSMenuItem*)</code><br>
     * <i>native declaration : :69</i>
     */
    public abstract void setSubmenu_forItem(NSMenu aMenu, NSMenuItem anItem);

    /**
     * Original signature : <code>NSArray* itemArray()</code><br>
     * <i>native declaration : :71</i>
     */
    public abstract NSArray itemArray();

    /**
     * Original signature : <code>NSInteger numberOfItems()</code><br>
     * <i>native declaration : :72</i>
     */
    public abstract NSInteger numberOfItems();

    /**
     * Original signature : <code>NSInteger indexOfItem(NSMenuItem*)</code><br>
     * <i>native declaration : :74</i>
     */
    public abstract NSInteger indexOfItem(NSMenuItem index);

    /**
     * Original signature : <code>NSInteger indexOfItemWithTitle(NSString*)</code><br>
     * <i>native declaration : :75</i>
     */
    public abstract NSInteger indexOfItemWithTitle(String aTitle);

    /**
     * Original signature : <code>NSInteger indexOfItemWithTag(NSInteger)</code><br>
     * <i>native declaration : :76</i>
     */
    public abstract NSInteger indexOfItemWithTag(NSInteger aTag);

    /**
     * Original signature : <code>NSInteger indexOfItemWithRepresentedObject(id)</code><br>
     * <i>native declaration : :77</i>
     */
    public abstract NSInteger indexOfItemWithRepresentedObject(String object);

    /**
     * Original signature : <code>NSInteger indexOfItemWithSubmenu(NSMenu*)</code><br>
     * <i>native declaration : :78</i>
     */
    public abstract NSInteger indexOfItemWithSubmenu(NSMenu submenu);
    /**
     * <i>native declaration : :79</i><br>
     * Conversion Error : /// Original signature : <code>NSInteger indexOfItemWithTarget(id, null)</code><br>
     * - (NSInteger)indexOfItemWithTarget:(id)target andAction:(null)actionSelector; (Argument actionSelector cannot be converted)
     */
    /**
     * Original signature : <code>NSMenuItem* itemAtIndex(NSInteger)</code><br>
     * <i>native declaration : :81</i>
     */
    public abstract NSMenuItem itemAtIndex(NSInteger index);

    /**
     * Original signature : <code>NSMenuItem* itemWithTitle(NSString*)</code><br>
     * <i>native declaration : :82</i>
     */
    public abstract NSMenuItem itemWithTitle(String aTitle);

    /**
     * Original signature : <code>NSMenuItem* itemWithTag(NSInteger)</code><br>
     * <i>native declaration : :83</i>
     */
    public abstract NSMenuItem itemWithTag(NSInteger tag);

    /**
     * Original signature : <code>void setAutoenablesItems(BOOL)</code><br>
     * <i>native declaration : :85</i>
     */
    public abstract void setAutoenablesItems(boolean flag);

    /**
     * Original signature : <code>BOOL autoenablesItems()</code><br>
     * <i>native declaration : :86</i>
     */
    public abstract boolean autoenablesItems();

    /**
     * Original signature : <code>BOOL performKeyEquivalent(NSEvent*)</code><br>
     * <i>native declaration : :88</i>
     */
    public abstract boolean performKeyEquivalent(NSEvent event);

    /**
     * Original signature : <code>void update()</code><br>
     * <i>native declaration : :89</i>
     */
    public abstract void update();

    /**
     * Original signature : <code>void setMenuChangedMessagesEnabled(BOOL)</code><br>
     * <i>native declaration : :91</i>
     */
    public abstract void setMenuChangedMessagesEnabled(boolean flag);

    /**
     * Original signature : <code>BOOL menuChangedMessagesEnabled()</code><br>
     * <i>native declaration : :92</i>
     */
    public abstract boolean menuChangedMessagesEnabled();

    /**
     * Original signature : <code>void itemChanged(NSMenuItem*)</code><br>
     * <i>native declaration : :94</i>
     */
    public abstract void itemChanged(NSMenuItem item);

    /**
     * Original signature : <code>void helpRequested(NSEvent*)</code><br>
     * <i>native declaration : :96</i>
     */
    public abstract void helpRequested(NSEvent eventPtr);

    /**
     * Original signature : <code>void setMenuRepresentation(id)</code><br>
     * <i>native declaration : :98</i>
     */
    public abstract void setMenuRepresentation(org.rococoa.ID menuRep);

    /**
     * Original signature : <code>id menuRepresentation()</code><br>
     * <i>native declaration : :99</i>
     */
    public abstract org.rococoa.ID menuRepresentation();

    /**
     * Original signature : <code>void setContextMenuRepresentation(id)</code><br>
     * <i>native declaration : :101</i>
     */
    public abstract void setContextMenuRepresentation(org.rococoa.ID menuRep);

    /**
     * Original signature : <code>id contextMenuRepresentation()</code><br>
     * <i>native declaration : :102</i>
     */
    public abstract org.rococoa.ID contextMenuRepresentation();

    /**
     * Original signature : <code>void setTearOffMenuRepresentation(id)</code><br>
     * <i>native declaration : :104</i>
     */
    public abstract void setTearOffMenuRepresentation(org.rococoa.ID menuRep);

    /**
     * Original signature : <code>id tearOffMenuRepresentation()</code><br>
     * <i>native declaration : :105</i>
     */
    public abstract org.rococoa.ID tearOffMenuRepresentation();

    /**
     * Original signature : <code>BOOL isTornOff()</code><br>
     * <i>native declaration : :107</i>
     */
    public abstract boolean isTornOff();

    /**
     * These methods are platform specific.  They really make little sense on Windows.  Their use is discouraged.<br>
     * Original signature : <code>NSMenu* attachedMenu()</code><br>
     * <i>native declaration : :110</i>
     */
    public abstract NSMenu attachedMenu();

    /**
     * Original signature : <code>BOOL isAttached()</code><br>
     * <i>native declaration : :111</i>
     */
    public abstract boolean isAttached();

    /**
     * Original signature : <code>void sizeToFit()</code><br>
     * <i>native declaration : :112</i>
     */
    public abstract void sizeToFit();

    /**
     * Original signature : <code>locationForSubmenu(NSMenu*)</code><br>
     * <i>native declaration : :113</i>
     */
    public abstract NSObject locationForSubmenu(NSMenu aSubmenu);

    /**
     * Original signature : <code>void performActionForItemAtIndex(NSInteger)</code><br>
     * <i>native declaration : :115</i>
     */
    public abstract void performActionForItemAtIndex(NSInteger index);

    /**
     * Original signature : <code>void setDelegate(id)</code><br>
     * <i>native declaration : :118</i>
     */
    public abstract void setDelegate(org.rococoa.ID anObject);

    /**
     * Original signature : <code>id delegate()</code><br>
     * <i>native declaration : :119</i>
     */
    public abstract org.rococoa.ID delegate();

    /**
     * Original signature : <code>CGFloat menuBarHeight()</code><br>
     * <i>native declaration : :123</i>
     */
    public abstract float menuBarHeight();

    /**
     * Dismisses the menu and ends all menu tracking<br>
     * Original signature : <code>void cancelTracking()</code><br>
     * <i>native declaration : :128</i>
     */
    public abstract void cancelTracking();

    /**
     * Returns the highlighted item in the menu, or nil if no item in the menu is highlighted<br>
     * Original signature : <code>NSMenuItem* highlightedItem()</code><br>
     * <i>native declaration : :131</i>
     */
    public abstract NSMenuItem highlightedItem();

    /**
     * Original signature : <code>void setShowsStateColumn(BOOL)</code><br>
     * <i>native declaration : :133</i>
     */
    public abstract void setShowsStateColumn(boolean showsState);

    /**
     * Original signature : <code>BOOL showsStateColumn()</code><br>
     * <i>native declaration : :134</i>
     */
    public abstract boolean showsStateColumn();

    /**
     * Original signature : <code>void submenuAction(id)</code><br>
     * <i>from NSSubmenuAction native declaration : :140</i>
     */
    public abstract void submenuAction(final ID sender);
}
