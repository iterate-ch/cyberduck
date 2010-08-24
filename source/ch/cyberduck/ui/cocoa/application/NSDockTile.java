package ch.cyberduck.ui.cocoa.application;

/*
 * Copyright (c) 2002-2010 David Kocher. All rights reserved.
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
import org.rococoa.cocoa.foundation.NSSize;

public abstract class NSDockTile extends NSObject {
    private static final _Class CLASS = org.rococoa.Rococoa.createClass("NSDockTile", _Class.class);

    /// <i>native declaration : NSDockTile.h</i>
    public static NSDockTile alloc() {
        return CLASS.alloc();
    }

    /// <i>native declaration : NSDockTile.h</i>
    public static NSDockTile create() {
        return CLASS.create();
    }

    public interface _Class extends ObjCClass {
        /// <i>native declaration : NSDockTile.h</i>
        public abstract NSDockTile alloc();
        /// <i>native declaration : NSDockTile.h</i>
        public abstract NSDockTile create();
    }

	/**
	 * get the size of the dock tile, in screen coordinates<br>
	 * Original signature : <code>-(NSSize)size</code><br>
	 * <i>native declaration : NSDockTile.h:37</i>
	 */
	public abstract NSSize.ByValue size();
	/**
	 * set the content view to view.  view should be height and width resizable.  In order to initiate drawing in view, you must call -[NSDockTile display].<br>
	 * Original signature : <code>-(void)setContentView:(NSView*)</code><br>
	 * <i>native declaration : NSDockTile.h:41</i>
	 */
	public abstract void setContentView(NSView view);
	/**
	 * Original signature : <code>-(NSView*)contentView</code><br>
	 * <i>native declaration : NSDockTile.h:42</i>
	 */
	public abstract NSView contentView();
	/**
	 * cause the dock tile to be redrawn.  The contentView and any subviews will be sent drawRect: messages.<br>
	 * Original signature : <code>-(void)display</code><br>
	 * <i>native declaration : NSDockTile.h:46</i>
	 */
	public abstract void display();
	/**
	 * setShowsApplicationBadge: sets whether or not the dock tile should be badged with the application icon.  Default is YES for NSWindow dock tiles, NO for the NSApplication dock tile.<br>
	 * Original signature : <code>-(void)setShowsApplicationBadge:(BOOL)</code><br>
	 * <i>native declaration : NSDockTile.h:50</i>
	 */
	public abstract void setShowsApplicationBadge(boolean flag);
	/**
	 * Original signature : <code>-(BOOL)showsApplicationBadge</code><br>
	 * <i>native declaration : NSDockTile.h:51</i>
	 */
	public abstract boolean showsApplicationBadge();
	/**
	 * Badge the dock icon with a localized string.  The badge appearance is system defined.  This is often used to show an unread count in the application dock icon.<br>
	 * Original signature : <code>-(void)setBadgeLabel:(NSString*)</code><br>
	 * <i>native declaration : NSDockTile.h:55</i>
	 */
	public abstract void setBadgeLabel(String string);
	/**
	 * Original signature : <code>-(NSString*)badgeLabel</code><br>
	 * <i>native declaration : NSDockTile.h:56</i>
	 */
	public abstract String badgeLabel();
	/**
	 * -owner will return NSApp for the application dock tile, or the NSWindow for a mini window dock tile.<br>
	 * Original signature : <code>-(id)owner</code><br>
	 * <i>native declaration : NSDockTile.h:60</i>
	 */
	public abstract NSObject owner();
}
