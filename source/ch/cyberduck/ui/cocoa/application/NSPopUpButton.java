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

import org.rococoa.ObjCClass;
import org.rococoa.cocoa.foundation.NSInteger;
import org.rococoa.cocoa.foundation.NSRect;

/// <i>native declaration : :10</i>

public abstract class NSPopUpButton extends NSButton {

    public static final String PopUpButtonWillPopUpNotification = "NSPopUpButtonWillPopUpNotification";

    private static final _Class CLASS = org.rococoa.Rococoa.createClass("NSPopUpButton", _Class.class);

    public static NSPopUpButton buttonWithFrame(NSRect frameRect) {
        return CLASS.alloc().initWithFrame_pullsDown(frameRect, false);
    }

    public interface _Class extends ObjCClass {
        NSPopUpButton alloc();
    }

    public abstract NSPopUpButton initWithFrame_pullsDown(NSRect frameRect, boolean flag);

    /**
     * Behavior settings<br>
     * Original signature : <code>void setPullsDown(BOOL)</code><br>
     * <i>native declaration : :29</i>
     */
    public abstract void setPullsDown(boolean flag);

    /**
     * Original signature : <code>BOOL pullsDown()</code><br>
     * <i>native declaration : :30</i>
     */
    public abstract boolean pullsDown();

    /**
     * Original signature : <code>void setAutoenablesItems(BOOL)</code><br>
     * <i>native declaration : :32</i>
     */
    public abstract void setAutoenablesItems(boolean flag);

    /**
     * Original signature : <code>BOOL autoenablesItems()</code><br>
     * <i>native declaration : :33</i>
     */
    public abstract boolean autoenablesItems();

    /**
     * Adding and removing items<br>
     * Original signature : <code>void addItemWithTitle(NSString*)</code><br>
     * <i>native declaration : :40</i>
     */
    public abstract void addItemWithTitle(String title);

    /**
     * Original signature : <code>void addItemsWithTitles(NSArray*)</code><br>
     * <i>native declaration : :41</i>
     */
    public abstract void addItemsWithTitles(NSArray itemTitles);

    /**
     * Original signature : <code>void insertItemWithTitle(NSString*, NSInteger)</code><br>
     * <i>native declaration : :42</i>
     */
    public abstract void insertItemWithTitle_atIndex(String title, NSInteger index);

    /**
     * Original signature : <code>void removeItemWithTitle(NSString*)</code><br>
     * <i>native declaration : :44</i>
     */
    public abstract void removeItemWithTitle(String title);

    /**
     * Original signature : <code>void removeItemAtIndex(NSInteger)</code><br>
     * <i>native declaration : :45</i>
     */
    public abstract void removeItemAtIndex(NSInteger index);

    /**
     * Original signature : <code>void removeAllItems()</code><br>
     * <i>native declaration : :46</i>
     */
    public abstract void removeAllItems();

    /**
     * Accessing the items<br>
     * Original signature : <code>NSArray* itemArray()</code><br>
     * <i>native declaration : :50</i>
     */
    public abstract NSArray itemArray();

    /**
     * Original signature : <code>NSInteger numberOfItems()</code><br>
     * <i>native declaration : :51</i>
     */
    public abstract NSInteger numberOfItems();

    /**
     * Original signature : <code>NSInteger indexOfItem(NSMenuItem*)</code><br>
     * <i>native declaration : :53</i>
     */
    public abstract NSInteger indexOfItem(NSMenuItem item);

    /**
     * Original signature : <code>NSInteger indexOfItemWithTitle(NSString*)</code><br>
     * <i>native declaration : :54</i>
     */
    public abstract NSInteger indexOfItemWithTitle(String title);

    /**
     * Original signature : <code>NSInteger indexOfItemWithTag(NSInteger)</code><br>
     * <i>native declaration : :55</i>
     */
    public abstract NSInteger indexOfItemWithTag(NSInteger tag);
    /**
     * Original signature : <code>NSInteger indexOfItemWithRepresentedObject(null)</code><br>
     * - (NSInteger)indexOfItemWithRepresentedObject:(null)obj; (Argument obj cannot be converted)
     */
    public abstract NSInteger indexOfItemWithRepresentedObject(String object);
    /**
     * <i>native declaration : :57</i><br>
     * Conversion Error : /// Original signature : <code>NSInteger indexOfItemWithTarget(null, null)</code><br>
     * - (NSInteger)indexOfItemWithTarget:(null)target andAction:(null)actionSelector; (Argument target cannot be converted)
     */
    /**
     * Original signature : <code>NSMenuItem* itemAtIndex(NSInteger)</code><br>
     * <i>native declaration : :59</i>
     */
    public abstract NSMenuItem itemAtIndex(NSInteger index);

    /**
     * Original signature : <code>NSMenuItem* itemWithTitle(NSString*)</code><br>
     * <i>native declaration : :60</i>
     */
    public abstract NSMenuItem itemWithTitle(String title);

    /**
     * Original signature : <code>NSMenuItem* lastItem()</code><br>
     * <i>native declaration : :61</i>
     */
    public abstract NSMenuItem lastItem();

    /**
     * Dealing with selection<br>
     * Original signature : <code>void selectItem(NSMenuItem*)</code><br>
     * <i>native declaration : :65</i>
     */
    public abstract void selectItem(NSMenuItem item);

    /**
     * Original signature : <code>void selectItemAtIndex(NSInteger)</code><br>
     * <i>native declaration : :66</i>
     */
    public abstract void selectItemAtIndex(NSInteger index);

    /**
     * Original signature : <code>void selectItemWithTitle(NSString*)</code><br>
     * <i>native declaration : :67</i>
     */
    public abstract void selectItemWithTitle(String title);

    /**
     * Original signature : <code>BOOL selectItemWithTag(NSInteger)</code><br>
     * <i>native declaration : :69</i>
     */
    public abstract boolean selectItemWithTag(NSInteger tag);

    /**
     * Original signature : <code>NSMenuItem* selectedItem()</code><br>
     * <i>native declaration : :73</i>
     */
    public abstract NSMenuItem selectedItem();

    /**
     * Original signature : <code>NSInteger indexOfSelectedItem()</code><br>
     * <i>native declaration : :74</i>
     */
    public abstract NSInteger indexOfSelectedItem();

    /**
     * Original signature : <code>void synchronizeTitleAndSelectedItem()</code><br>
     * <i>native declaration : :75</i>
     */
    public abstract void synchronizeTitleAndSelectedItem();

    /**
     * Title conveniences<br>
     * Original signature : <code>NSString* itemTitleAtIndex(NSInteger)</code><br>
     * <i>native declaration : :78</i>
     */
    public abstract String itemTitleAtIndex(NSInteger index);

    /**
     * Original signature : <code>NSArray* itemTitles()</code><br>
     * <i>native declaration : :79</i>
     */
    public abstract NSArray itemTitles();

    /**
     * Original signature : <code>NSString* titleOfSelectedItem()</code><br>
     * <i>native declaration : :80</i>
     */
    public abstract String titleOfSelectedItem();
}
