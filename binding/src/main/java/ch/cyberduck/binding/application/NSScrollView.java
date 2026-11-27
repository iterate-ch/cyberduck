package ch.cyberduck.binding.application;

/*
 * Copyright (c) 2002-2026 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
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
 */

import org.rococoa.ObjCClass;
import org.rococoa.cocoa.CGFloat;
import org.rococoa.cocoa.foundation.NSRect;
import org.rococoa.cocoa.foundation.NSSize;

public abstract class NSScrollView extends NSView {
    private static final _Class CLASS = org.rococoa.Rococoa.createClass("NSScrollView", _Class.class);

    /**
     * NSBorderType
     */
    public static final int NSNoBorder = 0;
    public static final int NSLineBorder = 1;
    public static final int NSBezelBorder = 2;
    public static final int NSGrooveBorder = 3;

    /**
     * NSScrollerStyle
     */
    public static final int NSScrollerStyleLegacy = 0;
    public static final int NSScrollerStyleOverlay = 1;

    public static NSScrollView create() {
        return CLASS.alloc().init();
    }

    public static NSScrollView create(NSRect frameRect) {
        return CLASS.alloc().initWithFrame(frameRect);
    }

    public interface _Class extends ObjCClass {
        NSScrollView alloc();

        /**
         * Original signature : <code>NSSize frameSizeForContentSize:horizontalScrollerClass:verticalScrollerClass:borderType:controlSize:scrollerStyle:</code>
         */
        NSSize contentSizeForFrameSize_horizontalScrollerClass_verticalScrollerClass_borderType_controlSize_scrollerStyle(
            NSSize fSize, ObjCClass horizontalScrollerClass, ObjCClass verticalScrollerClass, int type, int controlSize, int scrollerStyle);

        NSSize frameSizeForContentSize_horizontalScrollerClass_verticalScrollerClass_borderType_controlSize_scrollerStyle(
            NSSize cSize, ObjCClass horizontalScrollerClass, ObjCClass verticalScrollerClass, int type, int controlSize, int scrollerStyle);
    }

    @Override
    public abstract NSScrollView init();

    @Override
    public abstract NSScrollView initWithFrame(NSRect frameRect);

    /**
     * Original signature : <code>NSView* documentView()</code>
     */
    public abstract NSView documentView();

    /**
     * Original signature : <code>void setDocumentView(NSView*)</code>
     */
    public abstract void setDocumentView(NSView view);

    /**
     * Original signature : <code>NSSize contentSize()</code>
     */
    public abstract NSSize contentSize();

    /**
     * Original signature : <code>NSRect documentVisibleRect()</code>
     */
    public abstract NSRect documentVisibleRect();

    /**
     * Original signature : <code>NSBorderType borderType()</code>
     */
    public abstract int borderType();

    /**
     * Original signature : <code>void setBorderType(NSBorderType)</code>
     */
    public abstract void setBorderType(int type);

    /**
     * Original signature : <code>NSColor* backgroundColor()</code>
     */
    public abstract NSColor backgroundColor();

    /**
     * Original signature : <code>void setBackgroundColor(NSColor*)</code>
     */
    public abstract void setBackgroundColor(NSColor color);

    /**
     * Original signature : <code>BOOL drawsBackground()</code>
     */
    public abstract boolean drawsBackground();

    /**
     * Original signature : <code>void setDrawsBackground(BOOL)</code>
     */
    public abstract void setDrawsBackground(boolean flag);

    /**
     * Original signature : <code>BOOL hasVerticalScroller()</code>
     */
    public abstract boolean hasVerticalScroller();

    /**
     * Original signature : <code>void setHasVerticalScroller(BOOL)</code>
     */
    public abstract void setHasVerticalScroller(boolean flag);

    /**
     * Original signature : <code>BOOL hasHorizontalScroller()</code>
     */
    public abstract boolean hasHorizontalScroller();

    /**
     * Original signature : <code>void setHasHorizontalScroller(BOOL)</code>
     */
    public abstract void setHasHorizontalScroller(boolean flag);

    /**
     * Original signature : <code>BOOL autohidesScrollers()</code>
     */
    public abstract boolean autohidesScrollers();

    /**
     * Original signature : <code>void setAutohidesScrollers(BOOL)</code>
     */
    public abstract void setAutohidesScrollers(boolean flag);

    /**
     * Original signature : <code>NSScrollerStyle scrollerStyle()</code>
     */
    public abstract int scrollerStyle();

    /**
     * Original signature : <code>void setScrollerStyle(NSScrollerStyle)</code>
     */
    public abstract void setScrollerStyle(int style);

    /**
     * Original signature : <code>BOOL scrollsDynamically()</code>
     */
    public abstract boolean scrollsDynamically();

    /**
     * Original signature : <code>void setScrollsDynamically(BOOL)</code>
     */
    public abstract void setScrollsDynamically(boolean flag);

    /**
     * Original signature : <code>BOOL usesPredominantAxisScrolling()</code>
     */
    public abstract boolean usesPredominantAxisScrolling();

    /**
     * Original signature : <code>void setUsesPredominantAxisScrolling(BOOL)</code>
     */
    public abstract void setUsesPredominantAxisScrolling(boolean flag);

    /**
     * Original signature : <code>BOOL allowsMagnification()</code>
     */
    public abstract boolean allowsMagnification();

    /**
     * Original signature : <code>void setAllowsMagnification(BOOL)</code>
     */
    public abstract void setAllowsMagnification(boolean flag);

    /**
     * Original signature : <code>CGFloat magnification()</code>
     */
    public abstract CGFloat magnification();

    /**
     * Original signature : <code>void setMagnification(CGFloat)</code>
     */
    public abstract void setMagnification(CGFloat magnification);

    /**
     * Original signature : <code>CGFloat lineScroll()</code>
     */
    public abstract CGFloat lineScroll();

    /**
     * Original signature : <code>void setLineScroll(CGFloat)</code>
     */
    public abstract void setLineScroll(CGFloat value);

    /**
     * Original signature : <code>CGFloat pageScroll()</code>
     */
    public abstract CGFloat pageScroll();

    /**
     * Original signature : <code>void setPageScroll(CGFloat)</code>
     */
    public abstract void setPageScroll(CGFloat value);

    /**
     * Original signature : <code>void tile()</code>
     */
    public abstract void tile();

    /**
     * Original signature : <code>void reflectScrolledClipView(NSClipView*)</code>
     */
    public abstract void reflectScrolledClipView(NSView clipView);
}
