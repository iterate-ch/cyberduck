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
import ch.cyberduck.ui.cocoa.foundation.NSMutableAttributedString;
import ch.cyberduck.ui.cocoa.foundation.NSObject;

import org.rococoa.cocoa.foundation.NSInteger;

public abstract class NSTextStorage extends NSMutableAttributedString {

    /**
     * These methods manage the list of layout managers.<br>
     * Original signature : <code>void addLayoutManager(NSLayoutManager*)</code><br>
     * Retains & calls setTextStorage: on the item<br>
     * <i>native declaration : :45</i>
     */
    public abstract void addLayoutManager(NSLayoutManager obj);

    /**
     * Original signature : <code>void removeLayoutManager(NSLayoutManager*)</code><br>
     * <i>native declaration : :46</i>
     */
    public abstract void removeLayoutManager(NSLayoutManager obj);

    /**
     * Original signature : <code>NSArray* layoutManagers()</code><br>
     * <i>native declaration : :47</i>
     */
    public abstract NSArray layoutManagers();
    /**
     * <i>native declaration : :51</i><br>
     * Conversion Error : NSRange
     */
    /**
     * This is called from edited:range:changeInLength: or endEditing. This method sends out NSTextStorageWillProcessEditing, then fixes the attributes, then sends out NSTextStorageDidProcessEditing, and finally notifies the layout managers of change with the textStorage:edited:range:changeInLength:invalidatedRange: method.<br>
     * Original signature : <code>void processEditing()</code><br>
     * <i>native declaration : :55</i>
     */
    public abstract void processEditing();
    /**
     * <i>native declaration : :58</i><br>
     * Conversion Error : NSRange
     */
    /**
     * <i>native declaration : :61</i><br>
     * Conversion Error : NSRange
     */
    /**
     * Original signature : <code>BOOL fixesAttributesLazily()</code><br>
     * <i>native declaration : :65</i>
     */
    public abstract boolean fixesAttributesLazily();

    /**
     * These methods return information about the editing status. Especially useful when there are outstanding beginEditing calls or during processEditing... editedRange.location will be NSNotFound if nothing has been edited.<br>
     * Original signature : <code>NSUInteger editedMask()</code><br>
     * <i>native declaration : :69</i>
     */
    public abstract int editedMask();
    /**
     * <i>native declaration : :70</i><br>
     * Conversion Error : NSRange
     */
    /**
     * Original signature : <code>NSInteger changeInLength()</code><br>
     * <i>native declaration : :71</i>
     */
    public abstract NSInteger changeInLength();

    /**
     * Set/get the delegate<br>
     * Original signature : <code>void setDelegate(id)</code><br>
     * <i>native declaration : :75</i>
     */
    public abstract void setDelegate(org.rococoa.ID delegate);

    /**
     * Original signature : <code>id delegate()</code><br>
     * <i>native declaration : :76</i>
     */
    public abstract NSObject delegate();
    /// <i>native declaration : :30</i>
}

