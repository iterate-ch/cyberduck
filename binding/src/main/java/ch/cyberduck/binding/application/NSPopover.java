package ch.cyberduck.binding.application;

/*
 * Copyright (c) 2002-2024 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */

import ch.cyberduck.binding.foundation.NSNotification;

import org.rococoa.ID;
import org.rococoa.ObjCClass;
import org.rococoa.cocoa.foundation.NSRect;

/**
 * A means to display additional content related to existing content on the screen.
 */
public abstract class NSPopover extends NSResponder {
    private static final NSPopover._Class CLASS = org.rococoa.Rococoa.createClass("NSPopover", NSPopover._Class.class);

    public interface _Class extends ObjCClass {
        NSPopover alloc();
    }

    @Override
    public abstract NSPopover init();

    public static NSPopover create() {
        return CLASS.alloc().init();
    }

    /**
     * Your application assumes responsibility for closing the popover. AppKit will still close the popover in a
     * limited number of circumstances. For instance, AppKit will attempt to close the popover when the window of
     * its positioningView is closed.  The exact interactions in which AppKit will close the popover are not guaranteed.
     * You may consider implementing -cancel: to close the popover when the escape key is pressed.
     */
    public static final int NSPopoverBehaviorApplicationDefined = 0;

    /**
     * AppKit will close the popover when the user interacts with a user interface element outside the popover.
     * Note that interacting with menus or panels that become key only when needed will not cause a transient
     * popover to close.  The exact interactions that will cause transient popovers to close are not specified.
     */
    public static final int NSPopoverBehaviorTransient = 1;

    /**
     * AppKit will close the popover when the user interacts with user interface elements in the window containing
     * the popover's positioning view.  Semi-transient popovers cannot be shown relative to views in other popovers,
     * nor can they be shown relative to views in child windows.  The exact interactions that cause semi-transient
     * popovers to close are not specified.
     */
    public static final int NSPopoverBehaviorSemitransient = 2;

    /**
     * Specifies the behavior of the popover.
     *
     * @param behavior The default value is NSPopoverBehaviorApplicationDefined. See NSPopoverBehavior for possible value.
     */
    public abstract void setBehavior(int behavior);

    /**
     * Specifies if the popover is to be animated.
     * <p>
     * A popover may be animated when it shows, closes, moves, or appears to transition to a detachable window.
     * This property also controls whether the popover animates when the content view or content size changes.
     *
     * @param animates The default value is true.
     */
    public abstract void setAnimates(boolean animates);

    /**
     * A Boolean value that indicates whether the window created by a popover’s detachment is automatically created.
     *
     * @return When detached is true, the detached window is automatically created. This property does not apply when
     * detaching a popover results in a window returned by detachableWindowForPopover:.
     */
    public abstract boolean isDetached();

    /**
     * The display state of the popover.
     *
     * @return The value is true if the popover is being shown, false otherwise.
     */
    public abstract boolean isShown();

    /**
     * You must set the content view controller of the popover before the popover is shown. Changes to the
     * popover's content view controller while the popover is shown will cause the popover to animate if the animates property is YES.
     *
     * @param controller The view controller that manages the content of the popover.
     */
    public abstract void setContentViewController(NSViewController controller);

    /**
     * Shows the popover anchored to the specified view.
     * <p>
     * This method raises NSInternalInconsistencyException if contentViewController or the view controller’s view is nil.
     * If the popover is already being shown, this method updates the anchored view, rectangle, and preferred edge. If the
     * positioning view is not visible, this method does nothing.
     *
     * @param positioningRect The rectangle within positioningView relative to which the popover should be positioned. Normally
     *                        set to the bounds of positioningView. May be an empty rectangle, which will default to the bounds of positioningView.
     * @param positioningView The view relative to which the popover should be positioned. Causes the method to raise NSInvalidArgumentException if nil.
     * @param preferredEdge   The edge of positioningView the popover should prefer to be anchored to.
     */
    public abstract void showRelativeToRect_ofView_preferredEdge(NSRect positioningRect, NSView positioningView, int preferredEdge);

    /**
     * Shows the popover anchored to the specified toolbar item.
     * <p>
     * Use this method to display a popover relative to a toolbar item. When the item is in the overflow menu,
     * the popover presents itself from another appropriate affordance in the window. ¨
     * See showRelativeToRect:ofView:preferredEdge: for popover behavior.
     * <p>
     * This method raises an NSInvalidArgumentException if it can’t locate the toolbar item. This could
     * occur if the item isn’t in a toolbar, or because the toolbar isn’t in the window.
     * <p>
     * C
     *
     * @param toolbarItem The toolbar item anchoring the popover.
     */
    public abstract void showRelativeToToolbarItem(NSToolbarItem toolbarItem);

    /**
     * Attempts to close the popover.
     * <p>
     * The popover will not be closed if it has a delegate and the delegate implements the returns popoverShouldClose: method
     * returning NO, or if a subclass of the NSPopover class implements popoverShouldClose: and returns NO).
     * <p>
     * The operation will fail if the popover is displaying a nested popover or if it has a child window. A window will
     * attempt to close its popovers when it receives a performClose: message.
     * <p>
     * The popover animates out when closed unless the animates property is set to NO.
     *
     * @param sender The sender of the action message.
     */
    public abstract void performClose(ID sender);

    /**
     * Forces the popover to close without consulting its delegate.
     * <p>
     * Any popovers nested within the popovers will also receive a close message. When a window is closed in
     * response to the close message being sent, all of its popovers are closed. The popover animates out
     * when closed unless the animates property is set to false.
     */
    public abstract void close();

    /**
     * @param delegate The delegate of the popover.
     */
    public abstract void setDelegate(ID delegate);

    /**
     * A set of optional methods that a popover delegate can implement to provide additional or custom functionality.
     */
    public interface NSPopoverDelegate {
        boolean popoverShouldClose(NSPopover popover);

        boolean popoverShouldDetach(NSPopover popover);

        void popoverDidDetach(NSPopover popover);

        void popoverWillShow(NSNotification notification);

        void popoverDidShow(NSNotification notification);

        void popoverWillClose(NSNotification notification);

        void popoverDidClose(NSNotification notification);
    }
}
