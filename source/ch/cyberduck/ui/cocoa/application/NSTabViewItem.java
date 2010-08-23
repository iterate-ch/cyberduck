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

import org.rococoa.ObjCClass;
import org.rococoa.Selector;

/// <i>native declaration : :18</i>
public abstract class NSTabViewItem extends NSObject {
    private static final _Class CLASS = org.rococoa.Rococoa.createClass("NSTabViewItem", _Class.class);

    public static NSTabViewItem itemWithIdentifier(String identifier) {
        return CLASS.alloc().initWithIdentifier(identifier);
    }

    public interface _Class extends ObjCClass {
        NSTabViewItem alloc();
    }

    /**
     * Original signature : <code>id initWithIdentifier(id)</code><br>
     * <i>native declaration : :52</i>
     */
    public abstract NSTabViewItem initWithIdentifier(String identifier);

    /**
     * Original signature : <code>id identifier()</code><br>
     * <i>native declaration : :56</i>
     */
    public abstract String identifier();

    /**
     * Original signature : <code>id view()</code><br>
     * <i>native declaration : :57</i>
     */
    public abstract NSView view();

    /**
     * Original signature : <code>id initialFirstResponder()</code><br>
     * <i>native declaration : :58</i>
     */
    public abstract NSView initialFirstResponder();

    /**
     * Original signature : <code>NSString* label()</code><br>
     * <i>native declaration : :59</i>
     */
    public abstract String label();

    /**
     * Original signature : <code>NSColor* color()</code><br>
     * <i>native declaration : :60</i>
     */
    public abstract NSColor color();

    /**
     * Original signature : <code>NSTabState tabState()</code><br>
     * <i>native declaration : :61</i>
     */
    public abstract int tabState();

    /**
     * Original signature : <code>NSTabView* tabView()</code><br>
     * <i>native declaration : :62</i>
     */
    public abstract NSTabView tabView();

    /**
     * Original signature : <code>void setIdentifier(id)</code><br>
     * <i>native declaration : :66</i>
     */
    public abstract void setIdentifier(String identifier);

    /**
     * Original signature : <code>void setLabel(NSString*)</code><br>
     * <i>native declaration : :67</i>
     */
    public abstract void setLabel(String label);

    /**
     * Original signature : <code>void setColor(NSColor*)</code><br>
     * <i>native declaration : :68</i>
     */
    public abstract void setColor(NSColor color);

    /**
     * Original signature : <code>void setView(NSView*)</code><br>
     * <i>native declaration : :69</i>
     */
    public abstract void setView(NSView view);

    /**
     * Original signature : <code>void setInitialFirstResponder(NSView*)</code><br>
     * <i>native declaration : :70</i>
     */
    public abstract void setInitialFirstResponder(NSView view);
    /**
     * <i>native declaration : :76</i><br>
     * Conversion Error : NSRect
     */
    /**
     * <i>native declaration : :80</i><br>
     * Conversion Error : NSSize
     */
}
