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

/// <i>native declaration : :18</i>
public interface NSTabViewItem extends NSObject {
    _Class CLASS = org.rococoa.Rococoa.createClass("NSTabViewItem", _Class.class);

    public interface _Class extends org.rococoa.NSClass {
        NSTabViewItem alloc();
    }

    /**
     * Original signature : <code>id initWithIdentifier(id)</code><br>
     * <i>native declaration : :52</i>
     */
    NSTabViewItem initWithIdentifier(String identifier);

    /**
     * Original signature : <code>id identifier()</code><br>
     * <i>native declaration : :56</i>
     */
    String identifier();

    /**
     * Original signature : <code>id view()</code><br>
     * <i>native declaration : :57</i>
     */
    NSView view();

    /**
     * Original signature : <code>id initialFirstResponder()</code><br>
     * <i>native declaration : :58</i>
     */
    NSView initialFirstResponder();

    /**
     * Original signature : <code>NSString* label()</code><br>
     * <i>native declaration : :59</i>
     */
    String label();

    /**
     * Original signature : <code>NSColor* color()</code><br>
     * <i>native declaration : :60</i>
     */
    NSColor color();

    /**
     * Original signature : <code>NSTabState tabState()</code><br>
     * <i>native declaration : :61</i>
     */
    int tabState();

    /**
     * Original signature : <code>NSTabView* tabView()</code><br>
     * <i>native declaration : :62</i>
     */
    NSTabView tabView();

    /**
     * Original signature : <code>void setIdentifier(id)</code><br>
     * <i>native declaration : :66</i>
     */
    void setIdentifier(String identifier);

    /**
     * Original signature : <code>void setLabel(NSString*)</code><br>
     * <i>native declaration : :67</i>
     */
    void setLabel(String label);

    /**
     * Original signature : <code>void setColor(NSColor*)</code><br>
     * <i>native declaration : :68</i>
     */
    void setColor(NSColor color);

    /**
     * Original signature : <code>void setView(NSView*)</code><br>
     * <i>native declaration : :69</i>
     */
    void setView(NSView view);

    /**
     * Original signature : <code>void setInitialFirstResponder(NSView*)</code><br>
     * <i>native declaration : :70</i>
     */
    void setInitialFirstResponder(NSView view);
    /**
     * <i>native declaration : :76</i><br>
     * Conversion Error : NSRect
     */
    /**
     * <i>native declaration : :80</i><br>
     * Conversion Error : NSSize
     */
}
