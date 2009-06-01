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

import org.rococoa.ID;
import org.rococoa.cocoa.NSSize;

/// <i>native declaration : :31</i>
public interface NSDrawer extends NSObject {
    static final _Class CLASS = org.rococoa.Rococoa.createClass("NSDrawer", _Class.class);

    public interface _Class extends org.rococoa.NSClass {
        NSDrawer alloc();
    }
    /**
     * <i>native declaration : :57</i><br>
     * Conversion Error : /// Original signature : <code>id initWithContentSize(null, NSRectEdge)</code><br>
     * - (id)initWithContentSize:(null)contentSize preferredEdge:(NSRectEdge)edge; (Argument contentSize cannot be converted)
     */
    /**
     * Original signature : <code>void setParentWindow(NSWindow*)</code><br>
     * <i>native declaration : :59</i>
     */
    void setParentWindow(NSWindow parent);

    /**
     * Original signature : <code>NSWindow* parentWindow()</code><br>
     * <i>native declaration : :60</i>
     */
    NSWindow parentWindow();

    /**
     * Original signature : <code>void setContentView(NSView*)</code><br>
     * <i>native declaration : :61</i>
     */
    void setContentView(NSView aView);

    /**
     * Original signature : <code>NSView* contentView()</code><br>
     * <i>native declaration : :62</i>
     */
    NSView contentView();
    /**
     * <i>native declaration : :63</i><br>
     * Conversion Error : NSRectEdge
     */
    /**
     * <i>native declaration : :64</i><br>
     * Conversion Error : NSRectEdge
     */
    /**
     * Original signature : <code>void setDelegate(id)</code><br>
     * <i>native declaration : :65</i>
     */
    void setDelegate(org.rococoa.ID anObject);

    /**
     * Original signature : <code>id delegate()</code><br>
     * <i>native declaration : :66</i>
     */
    NSObject delegate();

    /**
     * Original signature : <code>void open()</code><br>
     * <i>native declaration : :68</i>
     */
    void open();
    /**
     * <i>native declaration : :69</i><br>
     * Conversion Error : NSRectEdge
     */
    /**
     * Original signature : <code>void close()</code><br>
     * <i>native declaration : :70</i>
     */
    void close();

    /**
     * Original signature : <code>void open(id)</code><br>
     * <i>native declaration : :72</i>
     */
    void open(ID sender);

    /**
     * Original signature : <code>void close(id)</code><br>
     * <i>native declaration : :73</i>
     */
    void close(ID sender);

    /**
     * Original signature : <code>void toggle(id)</code><br>
     * <i>native declaration : :74</i>
     */
    void toggle(ID sender);

    /**
     * Original signature : <code>NSInteger state()</code><br>
     * <i>native declaration : :76</i>
     */
    int state();
    /**
     * <i>native declaration : :77</i><br>
     * Conversion Error : NSRectEdge
     */
    /**
     * <i>native declaration : :79</i><br>
     * Conversion Error : /// Original signature : <code>void setContentSize(null)</code><br>
     * - (void)setContentSize:(null)size; (Argument size cannot be converted)
     */
    void setContentSize(NSSize size);

    /**
     * Original signature : <code>contentSize()</code><br>
     * <i>native declaration : :80</i>
     */
    NSSize contentSize();
    /**
     * <i>native declaration : :81</i><br>
     * Conversion Error : /// Original signature : <code>void setMinContentSize(null)</code><br>
     * - (void)setMinContentSize:(null)size; (Argument size cannot be converted)
     */
    /**
     * Original signature : <code>minContentSize()</code><br>
     * <i>native declaration : :82</i>
     */
    NSSize minContentSize();
    /**
     * <i>native declaration : :83</i><br>
     * Conversion Error : /// Original signature : <code>void setMaxContentSize(null)</code><br>
     * - (void)setMaxContentSize:(null)size; (Argument size cannot be converted)
     */
    /**
     * Original signature : <code>maxContentSize()</code><br>
     * <i>native declaration : :84</i>
     */
    NSSize maxContentSize();

    /**
     * Original signature : <code>void setLeadingOffset(CGFloat)</code><br>
     * <i>native declaration : :86</i>
     */
    void setLeadingOffset(float offset);

    /**
     * Original signature : <code>CGFloat leadingOffset()</code><br>
     * <i>native declaration : :87</i>
     */
    float leadingOffset();

    /**
     * Original signature : <code>void setTrailingOffset(CGFloat)</code><br>
     * <i>native declaration : :88</i>
     */
    void setTrailingOffset(float offset);

    /**
     * Original signature : <code>CGFloat trailingOffset()</code><br>
     * <i>native declaration : :89</i>
     */
    float trailingOffset();

    public static final int NSDrawerClosedState = 0;
    public static final int NSDrawerOpeningState = 1;
    public static final int NSDrawerOpenState = 2;
    public static final int NSDrawerClosingState = 3;
    public static final int ClosedState = 0;
    public static final int OpeningState = 1;
    public static final int OpenState = 2;
    public static final int ClosingState = 3;
    public static final String DrawerWillOpenNotification = "NSDrawerWillOpenNotification";
    public static final String DrawerDidOpenNotification = "NSDrawerDidOpenNotification";
    public static final String DrawerWillCloseNotification = "NSDrawerWillCloseNotification";
    public static final String DrawerDidCloseNotification = "NSDrawerDidCloseNotification";

}
