package ch.cyberduck.binding.application;

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

import ch.cyberduck.binding.foundation.NSArray;
import ch.cyberduck.binding.foundation.NSDate;
import ch.cyberduck.binding.foundation.NSDictionary;
import ch.cyberduck.binding.foundation.NSNotification;
import ch.cyberduck.binding.foundation.NSObject;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.rococoa.ID;
import org.rococoa.ObjCClass;
import org.rococoa.Rococoa;
import org.rococoa.Selector;
import org.rococoa.cocoa.foundation.NSInteger;
import org.rococoa.cocoa.foundation.NSUInteger;

public abstract class NSApplication extends NSObject {
    private static final _Class CLASS = Rococoa.createClass("NSApplication", _Class.class); //$NON-NLS-1$

    private static final Logger log = LogManager.getLogger(NSApplication.class);

    public interface Delegate {
        /**
         * Tells the delegate to open a single file.
         * <p>
         * Sent directly by theApplication to the delegate. The method should open the file filename, returning YES if
         * the file is successfully opened, and NO otherwise. If the user started up the application by double-clicking
         * a file, the delegate receives the application:openFile: message before receiving
         * applicationDidFinishLaunching:. (applicationWillFinishLaunching: is sent before application:openFile:.)
         *
         * @param app      The application object associated with the delegate.
         * @param filename The name of the file to open.
         * @return YES if the file was successfully opened or NO if it was not.
         */
        boolean application_openFile(NSApplication app, String filename);

        /**
         * Tells the delegate to open a temporary file.
         * <p>
         * <p>
         * Sent directly by theApplication to the delegate. The method should attempt to open the file filename,
         * returning YES if the file is successfully opened, and NO otherwise.
         * <p>
         * By design, a file opened through this method is assumed to be temporary—it’s the application’s responsibility
         * to remove the file at the appropriate time.
         *
         * @param app      The application object associated with the delegate.
         * @param filename The name of the temporary file to open.
         * @return YES if the file was successfully opened or NO if it was not.
         */
        boolean application_openTempFile(NSApplication app, String filename);

        /**
         * Invoked immediately before opening an untitled file.
         * <p>
         * Use this method to decide whether the application should open a new, untitled file. Note that
         * applicationOpenUntitledFile: is invoked if this method returns YES.
         *
         * @param sender The application object associated with the delegate.
         * @return YES if the application should open a new untitled file or NO if it should not.
         */
        boolean applicationShouldOpenUntitledFile(NSApplication sender);

        /**
         * Tells the delegate to open an untitled file.
         * <p>
         * Sent directly by theApplication to the delegate to request that a new, untitled file be opened.
         *
         * @param sender The application object associated with the delegate.
         * @return YES if the file was successfully opened or NO if it was not.
         */
        boolean applicationOpenUntitledFile(NSApplication sender);

        /**
         * Sent by the application to the delegate prior to default behavior to reopen (rapp) AppleEvents.
         * <p>
         * These events are sent whenever the Finder reactivates an already running application because someone
         * double-clicked it again or used the dock to activate it.
         * <p>
         * By default the Application Kit will handle this event by checking whether there are any visible NSWindow (not
         * NSPanel) objects, and, if there are none, it goes through the standard untitled document creation (the same
         * as it does if theApplication is launched without any document to open). For most document-based applications,
         * an untitled document will be created.
         * <p>
         * The application delegate will also get a chance to respond to the normal untitled document delegate methods.
         * If you implement this method in your application delegate, it will be called before any of the default
         * behavior happens. If you return YES, then NSApplication will proceed as normal. If you return NO, then
         * NSApplication will do nothing. So, you can either implement this method with a version that does nothing, and
         * return NO if you do not want anything to happen at all (not recommended), or you can implement this method,
         * handle the event yourself in some custom way, and return NO.
         * <p>
         * Miniaturized windows, windows in the Dock, are considered visible by this method, and cause flag to return
         * YES, despite the fact that miniaturized windows return NO when sent an visible message.
         *
         * @param app                 The application object.
         * @param visibleWindowsFound Indicates whether the NSApplication object found any visible windows in your
         *                            application. You can use this value as an indication of whether the application
         *                            would do anything if you return YES.
         * @return YES if you want the application to perform its normal tasks or NO if you want the application to do
         * nothing.
         */
        boolean applicationShouldHandleReopen_hasVisibleWindows(NSApplication app, boolean visibleWindowsFound);

        /**
         * Sent by the default notification center immediately before the application object is initialized.
         *
         * @param notification A notification named NSApplicationWillFinishLaunchingNotification. Calling the object
         *                     method of this notification returns the NSApplication object itself.
         */
        default void applicationWillFinishLaunching(final NSNotification notification) {
            if(log.isDebugEnabled()) {
                log.debug(notification);
            }
        }

        /**
         * Sent by the default notification center after the application has been launched and initialized but before it
         * has received its first event.
         * <p>
         * Delegates can implement this method to perform further initialization. This method is called after the
         * application’s main run loop has been started but before it has processed any events. If the application was
         * launched by the user opening a file, the delegate’s application:openFile: method is called before this
         * method. If you want to perform initialization before any files are opened, implement the
         * applicationWillFinishLaunching: method in your delegate, which is called before application:openFile:.)
         *
         * @param notification A notification named NSApplicationDidFinishLaunchingNotification. Calling the object
         *                     method of this notification returns the NSApplication object itself.
         */
        default void applicationDidFinishLaunching(final NSNotification notification) {
            if(log.isDebugEnabled()) {
                log.debug(notification);
            }
        }

        /**
         * Sent to notify the delegate that the application is about to terminate.
         * <p>
         * This method is called after the application’s Quit menu item has been selected, or after the terminate:
         * method has been called. Generally, you should return NSTerminateNow to allow the termination to complete, but
         * you can cancel the termination process or delay it somewhat as needed. For example, you might delay
         * termination to finish processing some critical data but then terminate the application as soon as you are
         * done by calling the replyToApplicationShouldTerminate: method.
         *
         * @param app The application object that is about to be terminated.
         * @return One of the values defined in NSApplicationTerminateReply constants indicating whether the application
         * should terminate. For compatibility reasons, a return value of NO is equivalent to NSTerminateCancel, and a
         * return value of YES is equivalent to NSTerminateNow.
         */
        NSUInteger applicationShouldTerminate(NSApplication app);

        default void applicationWillTerminate(final NSNotification notification) {
            if(log.isDebugEnabled()) {
                log.debug(notification);
            }
        }

        /**
         * Invoked when the user closes the last window the application has open.
         * <p>
         * The application sends this message to your delegate when the application’s last window is closed. It sends
         * this message regardless of whether there are still panels open. (A panel in this case is defined as being an
         * instance of NSPanel or one of its subclasses.)
         * <p>
         * If your implementation returns NO, control returns to the main event loop and the application is not
         * terminated. If you return YES, your delegate’s applicationShouldTerminate: method is subsequently invoked to
         * confirm that the application should be terminated.
         *
         * @param app The application object whose last window was closed.
         * @return NO if the application should not be terminated when its last window is closed; otherwise, YES to
         * terminate the application.
         */
        boolean applicationShouldTerminateAfterLastWindowClosed(NSApplication app);

        /**
         * Sent by the default notification center immediately before the application becomes active.
         *
         * @param notification A notification named NSApplicationWillBecomeActiveNotification. Calling the object method
         *                     of this notification returns the NSApplication object itself.
         */
        default void applicationWillBecomeActive(final NSNotification notification) {
            if(log.isDebugEnabled()) {
                log.debug(notification);
            }
        }

        /**
         * Sent by the default notification center immediately after the application becomes active.
         *
         * @param notification A notification named NSApplicationDidBecomeActiveNotification. Calling the object method
         *                     of this notification returns the NSApplication object itself.
         */
        default void applicationDidBecomeActive(final NSNotification notification) {
            if(log.isDebugEnabled()) {
                log.debug(notification);
            }
        }

        /**
         * Sent by the default notification center immediately before the application is deactivated.
         *
         * @param notification A notification named NSApplicationWillResignActiveNotification. Calling the object method
         *                     of this notification returns the NSApplication object itself.
         */
        default void applicationWillResignActive(final NSNotification notification) {
            if(log.isDebugEnabled()) {
                log.debug(notification);
            }
        }

        /**
         * Sent by the default notification center immediately after the application is deactivated.
         *
         * @param notification A notification named NSApplicationDidResignActiveNotification. Calling the object method
         *                     of this notification returns the NSApplication object itself.
         */
        default void applicationDidResignActive(final NSNotification notification) {
            if(log.isDebugEnabled()) {
                log.debug(notification);
            }
        }
    }

    public static final NSUInteger NSTerminateCancel = new NSUInteger(0);
    public static final NSUInteger NSTerminateNow = new NSUInteger(1);
    public static final NSUInteger NSTerminateLater = new NSUInteger(2);

    public interface _Class extends ObjCClass {
        /**
         * Returns the application instance, creating it if it doesn’t exist yet.
         * <p>
         * This method also makes a connection to the window server and completes other initialization. Your program
         * should invoke this method as one of the first statements in main();
         *
         * @return The shared application object.
         */
        NSApplication sharedApplication();
    }

    public static NSApplication sharedApplication() {
        return CLASS.sharedApplication();
    }

    public abstract NSArray windows();

    /**
     * Original signature : <code>void setDelegate(id)</code><br>
     * <i>native declaration : :108</i>
     */
    public abstract void setDelegate(org.rococoa.ID anObject);

    /**
     * Original signature : <code>id delegate()</code><br>
     * <i>native declaration : :109</i>
     */
    public abstract org.rococoa.ID delegate();

    /**
     * Original signature : <code>NSGraphicsContext* context()</code><br>
     * <i>native declaration : :110</i>
     */
    public abstract com.sun.jna.Pointer context();

    /**
     * Original signature : <code>void hide(id)</code><br>
     * <i>native declaration : :111</i>
     */
    public abstract void hide(ID sender);

    /**
     * Original signature : <code>void unhide(id)</code><br>
     * <i>native declaration : :112</i>
     */
    public abstract void unhide(ID sender);

    /**
     * Original signature : <code>void unhideWithoutActivation()</code><br>
     * <i>native declaration : :113</i>
     */
    public abstract void unhideWithoutActivation();

    /**
     * Original signature : <code>NSWindow* windowWithWindowNumber(NSInteger)</code><br>
     * <i>native declaration : :114</i>
     */
    public abstract NSWindow windowWithWindowNumber(int windowNum);

    /**
     * Original signature : <code>NSWindow* mainWindow()</code><br>
     * <i>native declaration : :115</i>
     */
    public abstract NSWindow mainWindow();

    /**
     * Original signature : <code>NSWindow* keyWindow()</code><br>
     * <i>native declaration : :116</i>
     */
    public abstract NSWindow keyWindow();

    /**
     * Original signature : <code>BOOL isActive()</code><br>
     * <i>native declaration : :117</i>
     */
    public abstract boolean isActive();

    /**
     * Original signature : <code>BOOL isHidden()</code><br>
     * <i>native declaration : :118</i>
     */
    public abstract boolean isHidden();

    /**
     * Original signature : <code>BOOL isRunning()</code><br>
     * <i>native declaration : :119</i>
     */
    public abstract boolean isRunning();

    /**
     * Original signature : <code>void deactivate()</code><br>
     * <i>native declaration : :120</i>
     */
    public abstract void deactivate();

    /**
     * Original signature : <code>void activateIgnoringOtherApps(BOOL)</code><br>
     * <i>native declaration : :121</i>
     */
    public abstract void activateIgnoringOtherApps(boolean flag);

    /**
     * Original signature : <code>void hideOtherApplications(id)</code><br>
     * <i>native declaration : :123</i>
     */
    public abstract void hideOtherApplications(ID sender);

    /**
     * Original signature : <code>void unhideAllApplications(id)</code><br>
     * <i>native declaration : :124</i>
     */
    public abstract void unhideAllApplications(ID sender);

    /**
     * Original signature : <code>void finishLaunching()</code><br>
     * <i>native declaration : :126</i>
     */
    public abstract void finishLaunching();

    /**
     * Original signature : <code>void run()</code><br>
     * <i>native declaration : :127</i>
     */
    public abstract void run();

    /**
     * Original signature : <code>NSInteger runModalForWindow(NSWindow*)</code><br>
     * <i>native declaration : :128</i>
     */
    public abstract NSInteger runModalForWindow(NSWindow theWindow);

    /**
     * Original signature : <code>void stop(id)</code><br>
     * <i>native declaration : :129</i>
     */
    public abstract void stop(ID sender);

    /**
     * Original signature : <code>void stopModal()</code><br>
     * <i>native declaration : :130</i>
     */
    public abstract void stopModal();

    /**
     * Original signature : <code>void stopModalWithCode(NSInteger)</code><br>
     * <i>native declaration : :131</i>
     */
    public abstract void stopModalWithCode(int returnCode);

    /**
     * Original signature : <code>void abortModal()</code><br>
     * <i>native declaration : :132</i>
     */
    public abstract void abortModal();

    /**
     * Original signature : <code>NSWindow* modalWindow()</code><br>
     * <i>native declaration : :133</i>
     */
    public abstract NSWindow modalWindow();

    /**
     * Original signature : <code>NSModalSession beginModalSessionForWindow(NSWindow*)</code><br>
     * <i>native declaration : :134</i>
     */
    public abstract com.sun.jna.Pointer beginModalSessionForWindow(NSWindow theWindow);

    /**
     * Original signature : <code>NSInteger runModalSession(NSModalSession)</code><br>
     * <i>native declaration : :135</i>
     */
    public abstract NSInteger runModalSession(com.sun.jna.Pointer session);

    /**
     * Original signature : <code>void endModalSession(NSModalSession)</code><br>
     * <i>native declaration : :136</i>
     */
    public abstract void endModalSession(com.sun.jna.Pointer session);

    /**
     * Original signature : <code>void terminate(id)</code><br>
     * <i>native declaration : :137</i>
     */
    public abstract void terminate(ID sender);

    /**
     * A key value coding compliant get-accessor for the orderedDocuments to-many-relationship declared in Cocoa's
     * definition of the Standard Suite.  Return an array of currently open scriptable documents, in a predictable order
     * that will be meaningful to script writers.  NSApplication's implementation of this method returns pointers to all
     * NSDocuments in the front-to-back order of each document's frontmost window.  NSDocuments that have no associated
     * windows are at the end of the array.<br> Original signature : <code>NSArray* orderedDocuments()</code><br>
     * <i>from NSScripting native declaration : :14</i>
     */
    public abstract NSArray orderedDocuments();

    /**
     * A key value coding compliant get-accessor for the orderedWindows to-many-relationship declared in Cocoa's
     * definition of the Standard Suite.  Return an array of currently open scriptable windows, including hidden
     * windows, but typically not includings things like panels.<br> Original signature : <code>NSArray*
     * orderedWindows()</code><br>
     * <i>from NSScripting native declaration : :17</i>
     */
    public abstract NSArray orderedWindows();
    /**
     * <i>native declaration : :138</i><br>
     * Conversion Error : /**<br>
     *  * inform the user that this application needs attention - call this method only if your application is not already active<br>
     *  * Original signature : <code>NSInteger requestUserAttention(null)</code><br>
     *  * /<br>
     * - (NSInteger)requestUserAttention:(null)requestType; (Argument requestType cannot be converted)
     */
    /**
     * Original signature : <code>void cancelUserAttentionRequest(NSInteger)</code><br>
     * <i>native declaration : :139</i>
     */
    public abstract void cancelUserAttentionRequest(int request);

    /**
     * <i>native declaration : :149</i><br>
     * Conversion Error : /**<br> * *  Present a sheet on the given window.  When the modal session is ended,<br> * *
     * the didEndSelector will be invoked in the modalDelegate.  The didEndSelector<br> * * should have the following
     * signature, and will be invoked when the modal session ends.<br> * * This method should dimiss the sheet using
     * orderOut:<br> * * - (void)sheetDidEnd:(NSWindow *)sheet returnCode:(NSInteger)returnCode contextInfo:(void
     * *)contextInfo;<br> * *<br> * Original signature : <code>void beginSheet(NSWindow*, NSWindow*, id, null,
     * void*)</code><br> * /<br> - (void)beginSheet:(NSWindow*)sheet modalForWindow:(NSWindow*)docWindow
     * modalDelegate:(id)modalDelegate didEndSelector:(null)didEndSelector contextInfo:(void*)contextInfo; (Argument
     * didEndSelector cannot be converted)
     */
    public abstract void beginSheet_modalForWindow_modalDelegate_didEndSelector_contextInfo(NSWindow sheet, NSWindow docWindow, ID modalDelegate, Selector didEndSelector, ID contextInfo);

    public void beginSheet(NSWindow sheet, NSWindow docWindow, ID modalDelegate, Selector didEndSelector, ID contextInfo) {
        this.beginSheet_modalForWindow_modalDelegate_didEndSelector_contextInfo(sheet, docWindow, modalDelegate, didEndSelector, contextInfo);
    }

    /**
     * Original signature : <code>void endSheet(NSWindow*)</code><br>
     * <i>native declaration : :150</i>
     */
    public abstract void endSheet(NSWindow sheet);

    /**
     * Original signature : <code>void endSheet(NSWindow*, NSInteger)</code><br>
     * <i>native declaration : :151</i>
     */
    public abstract void endSheet_returnCode(NSWindow sheet, int returnCode);

    public void endSheet(NSWindow sheet, int returnCode) {
        this.endSheet_returnCode(sheet, returnCode);
    }

    /**
     * * runModalForWindow:relativeToWindow: is deprecated.  <br> * Please use beginSheet:modalForWindow:modalDelegate:didEndSelector:contextInfo:<br>
     * Original signature : <code>NSInteger runModalForWindow(NSWindow*, NSWindow*)</code><br>
     * <i>native declaration : :157</i>
     */
    public abstract NSInteger runModalForWindow_relativeToWindow(NSWindow theWindow, NSWindow docWindow);

    /**
     * * beginModalSessionForWindow:relativeToWindow: is deprecated.<br> * Please use
     * beginSheet:modalForWindow:modalDelegate:didEndSelector:contextInfo:<br> Original signature : <code>NSModalSession
     * beginModalSessionForWindow(NSWindow*, NSWindow*)</code><br>
     * <i>native declaration : :163</i>
     */
    public abstract com.sun.jna.Pointer beginModalSessionForWindow_relativeToWindow(NSWindow theWindow, NSWindow docWindow);

    /**
     * Original signature : <code>NSEvent* nextEventMatchingMask(NSUInteger, NSDate*, NSString*, BOOL)</code><br>
     * <i>native declaration : :164</i>
     */
    public abstract com.sun.jna.Pointer nextEventMatchingMask_untilDate_inMode_dequeue(int mask, NSDate expiration, String mode, boolean deqFlag);

    /**
     * Original signature : <code>void discardEventsMatchingMask(NSUInteger, NSEvent*)</code><br>
     * <i>native declaration : :165</i>
     */
    public abstract void discardEventsMatchingMask_beforeEvent(int mask, com.sun.jna.Pointer lastEvent);

    /**
     * Original signature : <code>void postEvent(NSEvent*, BOOL)</code><br>
     * <i>native declaration : :166</i>
     */
    public abstract void postEvent_atStart(NSEvent event, boolean flag);

    /**
     * Original signature : <code>NSEvent* currentEvent()</code><br>
     * <i>native declaration : :167</i>
     */
    public abstract NSEvent currentEvent();

    /**
     * Original signature : <code>void sendEvent(NSEvent*)</code><br>
     * <i>native declaration : :169</i>
     */
    public abstract void sendEvent(NSEvent event);

    /**
     * Original signature : <code>void preventWindowOrdering()</code><br>
     * <i>native declaration : :170</i>
     */
    public abstract void preventWindowOrdering();
    /**
     * <i>native declaration : :171</i><br>
     * Conversion Error : /// Original signature : <code>NSWindow* makeWindowsPerform(null, BOOL)</code><br>
     * - (NSWindow*)makeWindowsPerform:(null)aSelector inOrder:(BOOL)flag; (Argument aSelector cannot be converted)
     */

    /**
     * Original signature : <code>void setWindowsNeedUpdate(BOOL)</code><br>
     * <i>native declaration : :173</i>
     */
    public abstract void setWindowsNeedUpdate(boolean needUpdate);

    /**
     * Original signature : <code>void updateWindows()</code><br>
     * <i>native declaration : :174</i>
     */
    public abstract void updateWindows();

    /**
     * Original signature : <code>void setMainMenu(NSMenu*)</code><br>
     * <i>native declaration : :176</i>
     */
    public abstract void setMainMenu(NSMenu aMenu);

    /**
     * Original signature : <code>NSMenu* mainMenu()</code><br>
     * <i>native declaration : :177</i>
     */
    public abstract NSMenu mainMenu();

    /**
     * Original signature : <code>void setApplicationIconImage(NSImage*)</code><br>
     * <i>native declaration : :179</i>
     */
    public abstract void setApplicationIconImage(NSImage image);

    /**
     * Original signature : <code>NSImage* applicationIconImage()</code><br>
     * <i>native declaration : :180</i>
     */
    public abstract NSImage applicationIconImage();

    /**
     * Original signature : <code>NSDockTile* dockTile()</code><br>
     * <i>native declaration : :183</i>
     */
    public abstract NSDockTile dockTile();
    /**
     * <i>native declaration : :186</i><br>
     * Conversion Error : /// Original signature : <code>BOOL sendAction(null, id, id)</code><br>
     * - (BOOL)sendAction:(null)theAction to:(id)theTarget from:(id)sender; (Argument theAction cannot be converted)
     */
    /**
     * <i>native declaration : :187</i><br>
     * Conversion Error : /// Original signature : <code>id targetForAction(null)</code><br>
     * - (id)targetForAction:(null)theAction; (Argument theAction cannot be converted)
     */
    /**
     * <i>native declaration : :188</i><br>
     * Conversion Error : /// Original signature : <code>id targetForAction(null, id, id)</code><br>
     * - (id)targetForAction:(null)theAction to:(id)theTarget from:(id)sender; (Argument theAction cannot be converted)
     */
    /**
     * <i>native declaration : :189</i><br>
     * Conversion Error : /// Original signature : <code>BOOL tryToPerform(null, id)</code><br>
     * - (BOOL)tryToPerform:(null)anAction with:(id)anObject; (Argument anAction cannot be converted)
     */
    /**
     * Original signature : <code>id validRequestorForSendType(NSString*, NSString*)</code><br>
     * <i>native declaration : :190</i>
     */
    public abstract ID validRequestorForSendType_returnType(String sendType, String returnType);

    /**
     * Original signature : <code>void reportException(NSException*)</code><br>
     * <i>native declaration : :192</i>
     */
    public abstract void reportException(com.sun.jna.Pointer theException);

    /**
     * If an application delegate returns NSTerminateLater from -applicationShouldTerminate:,
     * -replyToApplicationShouldTerminate: must be called with YES or NO once the application decides if it can
     * terminate<br> Original signature : <code>void replyToApplicationShouldTerminate(BOOL)</code><br>
     * <i>native declaration : :196</i>
     */
    public abstract void replyToApplicationShouldTerminate(boolean shouldTerminate);

    /**
     * <i>native declaration : :200</i><br>
     * Conversion Error : /**<br>
     *  * If an application delegate encounters an error while handling -application:openFiles: or -application:printFiles:, -replyToOpenOrPrint: should be called with NSApplicationDelegateReplyFailure.  If the user cancels the operation, NSApplicationDelegateReplyCancel should be used, and if the operation succeeds, NSApplicationDelegateReplySuccess should be used<br>
     *  * Original signature : <code>void replyToOpenOrPrint(null)</code><br>
     *  * /<br>
     * - (void)replyToOpenOrPrint:(null)reply; (Argument reply cannot be converted)
     */
    /**
     * Opens the character palette<br> Original signature : <code>void orderFrontCharacterPalette(id)</code><br>
     * <i>native declaration : :204</i>
     */
    public abstract void orderFrontCharacterPalette(final ID sender);

    /**
     * Original signature : <code>void setWindowsMenu(NSMenu*)</code><br>
     * <i>from NSWindowsMenu native declaration : :209</i>
     */
    public abstract void setWindowsMenu(NSMenu aMenu);

    /**
     * Original signature : <code>NSMenu* windowsMenu()</code><br>
     * <i>from NSWindowsMenu native declaration : :210</i>
     */
    public abstract NSMenu windowsMenu();

    /**
     * Original signature : <code>void arrangeInFront(id)</code><br>
     * <i>from NSWindowsMenu native declaration : :211</i>
     */
    public abstract void arrangeInFront(final ID sender);

    /**
     * Original signature : <code>void removeWindowsItem(NSWindow*)</code><br>
     * <i>from NSWindowsMenu native declaration : :212</i>
     */
    public abstract void removeWindowsItem(NSWindow win);

    /**
     * Original signature : <code>void addWindowsItem(NSWindow*, NSString*, BOOL)</code><br>
     * <i>from NSWindowsMenu native declaration : :213</i>
     */
    public abstract void addWindowsItem_title_filename(NSWindow win, String aString, boolean isFilename);

    /**
     * Original signature : <code>void changeWindowsItem(NSWindow*, NSString*, BOOL)</code><br>
     * <i>from NSWindowsMenu native declaration : :214</i>
     */
    public abstract void changeWindowsItem_title_filename(NSWindow win, String aString, boolean isFilename);

    /**
     * Original signature : <code>void updateWindowsItem(NSWindow*)</code><br>
     * <i>from NSWindowsMenu native declaration : :215</i>
     */
    public abstract void updateWindowsItem(NSWindow win);

    /**
     * Original signature : <code>void miniaturizeAll(id)</code><br>
     * <i>from NSWindowsMenu native declaration : :216</i>
     */
    public abstract void miniaturizeAll(final ID sender);

    /**
     * Original signature : <code>void setServicesMenu(NSMenu*)</code><br>
     * <i>from NSServicesMenu native declaration : :275</i>
     */
    public abstract void setServicesMenu(NSMenu aMenu);

    /**
     * Original signature : <code>NSMenu* servicesMenu()</code><br>
     * <i>from NSServicesMenu native declaration : :276</i>
     */
    public abstract NSMenu servicesMenu();

    /**
     * Original signature : <code>void registerServicesMenuSendTypes(NSArray*, NSArray*)</code><br>
     * <i>from NSServicesMenu native declaration : :277</i>
     */
    public abstract void registerServicesMenuSendTypes_returnTypes(NSArray sendTypes, NSArray returnTypes);

    /**
     * Original signature : <code>void setServicesProvider(id)</code><br>
     * <i>from NSServicesHandling native declaration : :286</i>
     */
    public abstract void setServicesProvider(final ID provider);

    /**
     * Original signature : <code>id servicesProvider()</code><br>
     * <i>from NSServicesHandling native declaration : :287</i>
     */
    public abstract NSObject servicesProvider();

    /**
     * Original signature : <code>void orderFrontStandardAboutPanel(id)</code><br>
     * <i>from NSStandardAboutPanel native declaration : :291</i>
     */
    public abstract void orderFrontStandardAboutPanel(final ID sender);

    /**
     * Original signature : <code>void orderFrontStandardAboutPanelWithOptions(NSDictionary*)</code><br>
     * <i>from NSStandardAboutPanel native declaration : :292</i>
     */
    public abstract void orderFrontStandardAboutPanelWithOptions(NSDictionary optionsDictionary);

    public abstract void setActivationPolicy(int activationPolicy);

    public abstract int activationPolicy();

    public enum NSApplicationActivationPolicy {
        /* The application is an ordinary app that appears in the Dock and may have a user interface.  This is the
         default for bundled apps, unless overridden in the Info.plist. */
        NSApplicationActivationPolicyRegular,
        /* The application does not appear in the Dock and does not have a menu bar, but it may be activated
        programmatically or by clicking on one of its windows.  This corresponds to LSUIElement=1 in the Info.plist. */
        NSApplicationActivationPolicyAccessory
    }
}
