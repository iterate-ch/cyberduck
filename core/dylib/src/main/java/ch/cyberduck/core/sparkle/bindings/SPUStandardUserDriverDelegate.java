package ch.cyberduck.core.sparkle.bindings;

/*
 * Copyright (c) 2002-2023 iterate GmbH. All rights reserved.
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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public interface SPUStandardUserDriverDelegate {
    Logger log = LogManager.getLogger(SPUStandardUserDriverDelegate.class);

    /**
     * Called before showing a modal alert window, to give the opportunity to hide attached windows that may
     * get in the way.
     */
    default void standardUserDriverWillShowModalAlert() {
        log.debug("Will show modal alert");
    }

    /**
     * Called after showing a modal alert window, to give the opportunity to hide attached windows that may get in the way.
     */
    default void standardUserDriverDidShowModalAlert() {
        log.debug("Did show modal alert");
    }

    /**
     * Specifies whether the download, extraction, and installing status windows allows to be minimized.
     * <p>
     * By default, the status window showing the current status of the update (download, extraction, ready to
     * install) is allowed to be minimized for regular application bundle updates.
     *
     * @return YES if the status window is allowed to be minimized (default behavior), otherwise NO.
     */
    default boolean standardUserDriverAllowsMinimizableStatusWindow() {
        return true;
    }

    /**
     * Declares whether gentle scheduled update reminders are supported.
     * <p>
     * The delegate may implement scheduled update reminders that are presented in a gentle manner by
     * implementing one or both of: -standardUserDriverWillHandleShowingUpdate:forUpdate:state: and
     * -standardUserDriverShouldHandleShowingScheduledUpdate:andInImmediateFocus:
     *
     * @return YES if gentle scheduled update reminders are implemented by standard user driver delegate, otherwise NO (default).
     */
    default boolean supportsGentleScheduledUpdateReminders() {
        return false;
    }

    /**
     * Specifies if the standard user driver should handle showing a new scheduled update, or if its delegate
     * should handle showing the update instead.
     * <p>
     * If you implement this method and return NO the delegate is then responsible for showing the update, which
     * must be implemented and done in -standardUserDriverWillHandleShowingUpdate:forUpdate:state: The motivation
     * for the delegate being responsible for showing updates is to override Sparkle’s default behavior and add
     * gentle reminders for new updates.
     *
     * @param update         The update the standard user driver should show.
     * @param immediateFocus If immediateFocus is NO the standard user driver may want to defer showing the update
     *                       until the user comes back to the app.
     * @return Returning YES is the default behavior and allows the standard user driver to handle showing the update.
     */
    default boolean standardUserDriverShouldHandleShowingScheduledUpdate_andInImmediateFocus(SUAppcastItem update, boolean immediateFocus) {
        if(log.isDebugEnabled()) {
            log.debug("Should handle showing scheduled update {}", update);
        }
        return true;
    }

    /**
     * If the delegate declared it handles showing the update by returning NO in -standardUserDriverShouldHandleShowingScheduledUpdate:andInImmediateFocus: then
     * the delegate should handle showing update reminders in this method, or at some later point.
     *
     * @param handleShowingUpdate YES if the standard user driver handles showing the update, otherwise NO if the delegate handles showing the update.
     * @param update              The update that will be shown.
     * @param state               The user state of the update which includes if the update check was initiated by the user.
     */
    default void standardUserDriverWillHandleShowingUpdate_forUpdate_state(boolean handleShowingUpdate, SUAppcastItem update, SPUUserUpdateState state) {
        if(log.isDebugEnabled()) {
            log.debug("Will handle showing scheduled update {}", update);
        }
    }

    /**
     * Called when a new update first receives attention from the user.
     * <p>
     * This occurs either when the user first brings the update alert in utmost focus or when the user makes a
     * hoice to install an update or dismiss/skip it.
     *
     * @param update The new update that the user gave attention to.
     */
    default void standardUserDriverDidReceiveUserAttentionForUpdate(SUAppcastItem update) {
        if(log.isDebugEnabled()) {
            log.debug("Did receive user attention for update {}", update);
        }
    }

    /**
     * Called before the standard user driver session will finish its current update session.
     * This may occur after the user has dismissed / skipped a new update or after an update error has occurred.
     */
    default void standardUserDriverWillFinishUpdateSession() {
        if(log.isDebugEnabled()) {
            log.debug("Finish update session");
        }
    }
}
