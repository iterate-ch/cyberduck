package ch.cyberduck.binding;

/*
 * Copyright (c) 2002-2016 iterate GmbH. All rights reserved.
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

import ch.cyberduck.binding.application.NSAlert;
import ch.cyberduck.binding.application.NSApplication;
import ch.cyberduck.binding.application.NSWindow;
import ch.cyberduck.binding.application.SheetCallback;
import ch.cyberduck.binding.foundation.NSThread;
import ch.cyberduck.core.AbstractController;
import ch.cyberduck.core.threading.DefaultMainAction;
import ch.cyberduck.core.threading.MainAction;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.rococoa.Foundation;
import org.rococoa.ID;
import org.rococoa.Selector;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import com.google.common.util.concurrent.Uninterruptibles;

public class ProxyController extends AbstractController implements AlertSheetRunner {
    private static final Logger log = LogManager.getLogger(ProxyController.class);

    private final Proxy proxy = new Proxy(this);

    public ID id() {
        return proxy.id();
    }

    /**
     * Free all locked resources by this controller; also remove me from all observables;
     * marks this controller to be garbage collected as soon as needed
     */
    public void invalidate() {
        log.debug("Invalidate controller {}", this);
        proxy.invalidate();
    }

    /**
     * You can use this method to deliver messages to the main thread of your application. The main thread
     * encompasses the application’s main run loop, and is where the NSApplication object receives
     * events. The message in this case is a method of the current object that you want to execute
     * on the thread.
     * <p>
     * Execute the passed <code>Runnable</code> on the main thread also known as NSRunLoop.DefaultRunLoopMode
     *
     * @param runnable The <code>Runnable</code> to run
     * @param wait     Block until execution on main thread exits. A Boolean that specifies whether the current
     *                 thread blocks until after the specified selector is performed on the receiver on the main thread.
     *                 Specify YES to block this thread; otherwise, specify NO to have this method return immediately.
     *                 If the current thread is also the main thread, and you specify YES for this parameter,
     *                 the message is delivered and processed immediately.
     */
    @Override
    public void invoke(final MainAction runnable, final boolean wait) {
        if(!runnable.isValid()) {
            return;
        }
        // The Application Kit creates an autorelease pool on the main thread at the beginning of every cycle
        // of the event loop, and drains it at the end, thereby releasing any autoreleased objects generated
        // while processing an event. If you use the Application Kit, you therefore typically don’t have to
        // create your own pools.
        proxy.invoke(runnable, runnable.lock(), wait);
    }

    public int alert(final NSAlert alert) {
        return this.alert(new SystemAlertController(alert), SheetCallback.noop);
    }

    /**
     * Display alert as sheet to the window of this controller
     *
     * @param alert    Sheet
     * @param callback Dismissed notification
     */
    public int alert(final NSAlert alert, final SheetCallback callback) {
        return this.alert(new SystemAlertController(alert), callback);
    }

    public int alert(final SheetController sheet) {
        return this.alert(sheet, SheetCallback.noop);
    }

    public int alert(final SheetController sheet, final CountDownLatch signal) {
        return this.alert(sheet, this, SheetCallback.noop, signal);
    }

    public int alert(final SheetController sheet, final SheetCallback callback) {
        return this.alert(sheet, this, callback, new CountDownLatch(1));
    }

    public int alert(final SheetController sheet, final AlertSheetRunner invoker) {
        return this.alert(sheet, invoker, SheetCallback.noop, new CountDownLatch(1));
    }

    /**
     * Display as sheet attached to window of parent controller
     *
     * @param sheet    Alert window controller
     * @param callback Handler invoked after sheet is dismissed
     * @param signal   Signal to stop waiting for selection from user
     * @return Return code from selected option
     */
    public int alert(final SheetController sheet, final AlertSheetRunner invoker, final SheetCallback callback, final CountDownLatch signal) {
        final AtomicInteger option = new AtomicInteger(SheetCallback.CANCEL_OPTION);
        final SheetDidCloseReturnCodeDelegate proxy = new SheetDidCloseReturnCodeDelegate(new SheetCallback.DelegatingSheetCallback(
                new SheetCallback.ReturnCodeSheetCallback(option), callback, sheet, new SignalSheetCallback(signal)));
        this.invoke(new DefaultMainAction() {
            @Override
            public void run() {
                log.info("Load bundle for alert {}", sheet);
                sheet.loadBundle();
                final NSWindow sheetWindow = sheet.window();
                log.debug("Configure window {}", sheetWindow);
                sheetWindow.setHidesOnDeactivate(false);
                sheetWindow.setPreventsApplicationTerminationWhenModal(false);
                // Activate ignoring other applications and move window to floating window level
                sheetWindow.setLevel(NSWindow.NSWindowLevel.NSFloatingWindowLevel);
                NSApplication.sharedApplication().activateIgnoringOtherApps(true);
                // Maybe null
                final NSWindow parentWindow = window();
                invoker.alert(parentWindow, sheetWindow, proxy);
            }
        }, true);
        if(!NSThread.isMainThread()) {
            log.debug("Await sheet dismiss");
            Uninterruptibles.awaitUninterruptibly(signal);
            log.debug("Continue after sheet dismiss");
        }
        else {
            log.warn("Skip await sheet dismiss");
        }
        log.debug("Return option {}", option);
        return option.get();
    }

    /**
     * Run modal alert window
     *
     * @param parentWindow Parent window or null
     * @param sheetWindow  Alert window
     * @param delegate     Proxy to send dismiss notification
     * @see SheetDidCloseReturnCodeDelegate
     * @see SheetCallback
     */
    @Override
    public void alert(final NSWindow parentWindow, final NSWindow sheetWindow, final SheetDidCloseReturnCodeDelegate delegate) {
        // This method runs a modal event loop for the specified window synchronously. It displays the specified window, makes it key,
        // starts the run loop, and processes events for that window.
        delegate.sheetDidClose_returnCode_contextInfo(sheetWindow,
                NSApplication.sharedApplication().runModalForWindow(sheetWindow).intValue(), null);
    }


    public static final class SheetDidCloseReturnCodeDelegate extends Proxy {

        /**
         * Keep a reference to the sheet to protect it from being deallocated as a weak reference before the callback from
         * the runtime
         */
        private static final Set<SheetDidCloseReturnCodeDelegate> registry
                = new HashSet<>();

        public static final Selector selector = Foundation.selector("sheetDidClose:returnCode:contextInfo:");

        private final SheetCallback callback;

        public SheetDidCloseReturnCodeDelegate(final SheetCallback callback) {
            this.callback = callback;
            registry.add(this);
        }

        /**
         * Called by the runtime after a sheet has been dismissed. Ends any modal session and sends the returncode to the
         * callback implementation. Also invalidates this controller to be garbage collected and notifies the lock object
         *
         * @param sheetWindow Sheet window
         * @param returncode  Identifier for the button clicked by the user
         * @param contextInfo Not used
         */
        @Delegate
        public void sheetDidClose_returnCode_contextInfo(final NSWindow sheetWindow, final int returncode, final ID contextInfo) {
            log.debug("Close with return code {}", returncode);
            callback.callback(returncode);
            registry.remove(this);
        }
    }

    /**
     * Signal count down latch when sheet is dismissed
     */
    static final class SignalSheetCallback implements SheetCallback {
        private final CountDownLatch signal;

        public SignalSheetCallback(final CountDownLatch signal) {
            this.signal = signal;
        }

        @Override
        public void callback(final int returncode) {
            log.debug("Signal {} after closing sheet with code {}", signal, returncode);
            signal.countDown();
        }
    }

    public NSWindow window() {
        return null;
    }
}
