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
import ch.cyberduck.binding.application.NSPopover;
import ch.cyberduck.binding.application.NSView;
import ch.cyberduck.binding.application.NSViewController;
import ch.cyberduck.binding.application.NSWindow;
import ch.cyberduck.binding.application.SheetCallback;
import ch.cyberduck.binding.application.WindowListener;
import ch.cyberduck.binding.foundation.FoundationKitFunctions;
import ch.cyberduck.binding.foundation.NSNotification;
import ch.cyberduck.binding.foundation.NSObject;
import ch.cyberduck.binding.foundation.NSThread;
import ch.cyberduck.core.AbstractController;
import ch.cyberduck.core.threading.DefaultMainAction;
import ch.cyberduck.core.threading.MainAction;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.rococoa.ID;
import org.rococoa.cocoa.foundation.NSRect;
import org.rococoa.cocoa.foundation.NSSize;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import com.google.common.util.concurrent.Uninterruptibles;

public class ProxyController extends AbstractController {
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

    /**
     * @param sheet  Controller for alert window
     * @param signal Signaling will dismiss sheet without user input
     * @return Selected alert option by user
     */
    public int alert(final SheetController sheet, final CountDownLatch signal) {
        return this.alert(sheet, SheetCallback.noop, signal);
    }

    /**
     * @param sheet    Controller for alert window
     * @param callback Handler invoked after sheet is dismissed with selected option
     * @return Selected alert option by user
     */
    public int alert(final SheetController sheet, final SheetCallback callback) {
        return this.alert(sheet, callback, new CountDownLatch(1));
    }

    /**
     * @param sheet    Controller for alert window
     * @param callback Handler invoked after sheet is dismissed with selected option
     * @param signal   Signaling will dismiss sheet without user input
     * @return Selected alert option by user
     */
    public int alert(final SheetController sheet, final SheetCallback callback, final CountDownLatch signal) {
        return this.alert(sheet, callback, this.alertFor(sheet), signal);
    }

    /**
     * @param sheet  Controller for alert window
     * @param runner Implementation to display alert window
     * @return Selected alert option by user
     */
    public int alert(final SheetController sheet, final AlertRunner runner) {
        return this.alert(sheet, SheetCallback.noop, runner, new CountDownLatch(1));
    }

    /**
     * @param sheet  Controller for alert window
     * @param runner Implementation to display alert window
     * @param signal Signaling will dismiss sheet without user input
     * @return Selected alert option by user
     */
    public int alert(final SheetController sheet, final AlertRunner runner, final CountDownLatch signal) {
        return this.alert(sheet, SheetCallback.noop, runner, signal);
    }

    /**
     * Keep a reference to the sheet to protect it from being deallocated as a weak reference before the callback from
     * the runtime
     */
    protected static final Set<AlertRunner> alerts
            = new HashSet<>();

    /**
     * @param sheet    Controller for alert window
     * @param callback Handler invoked after sheet is dismissed with selected option
     * @param runner   Implementation to display alert window
     * @return Selected alert option by user
     */
    public int alert(final SheetController sheet, final SheetCallback callback, final AlertRunner runner) {
        return this.alert(sheet, callback, runner, new CountDownLatch(1));
    }

    /**
     * Display as sheet attached to window of parent controller
     *
     * @param sheet    Alert window controller
     * @param callback Handler invoked after sheet is dismissed with selected option
     * @param runner   Implementation to display alert window
     * @param signal   Await signal before continuing when on background thread
     * @return Selected alert option by user
     */
    public int alert(final SheetController sheet, final SheetCallback callback, final AlertRunner runner, final CountDownLatch signal) {
        log.debug("Alert with runner {} and callback {}", runner, callback);
        synchronized(alerts) {
            alerts.add(runner);
        }
        final AtomicInteger option = new AtomicInteger(SheetCallback.CANCEL_OPTION);
        final CountDownLatch state = new CountDownLatch(1);
        final SheetCallback.DelegatingSheetCallback chain = new SheetCallback.DelegatingSheetCallback(new SheetCallback.ReturnCodeSheetCallback(option), sheet, callback);
        this.invoke(new DefaultMainAction() {
            @Override
            public void run() {
                log.info("Load bundle for alert {}", sheet);
                sheet.loadBundle();
                runner.alert(sheet.window(), new SheetCallback.DelegatingSheetCallback(new SignalSheetCallback(state),
                        chain, new SignalSheetCallback(signal), (returncode) -> {
                    synchronized(alerts) {
                        alerts.remove(runner);
                    }
                }));
            }
        }, true);
        if(!NSThread.isMainThread()) {
            log.debug("Await sheet dismiss");
            Uninterruptibles.awaitUninterruptibly(signal);
            log.debug("Continue after sheet dismiss");
            if(0 != state.getCount()) {
                log.warn("Invoke callback chain with cancel option after missing signal from sheet dismiss");
                this.invoke(new DefaultMainAction() {
                    @Override
                    public void run() {
                        sheet.closeSheetWithOption(SheetCallback.CANCEL_OPTION);
                    }
                }, true);
            }
        }
        else {
            log.warn("Skip await sheet dismiss");
        }
        log.debug("Return option {}", option);
        return option.get();
    }

    protected AlertRunner alertFor(final SheetController sheet) {
        return new ModalWindowAlertRunner(sheet);
    }

    public static class RegularWindowAlertRunner implements AlertRunner, AlertRunner.CloseHandler {
        private final SheetController controller;
        private final AtomicInteger option = new AtomicInteger(SheetCallback.CANCEL_OPTION);

        public RegularWindowAlertRunner(final SheetController controller) {
            this.controller = controller;
            this.controller.addHandler(this);
        }

        @Override
        public void alert(final NSWindow sheet, final SheetCallback callback) {
            controller.addListener(new SheetCallbackWindowListener(callback, option));
            log.debug("Configure window {}", sheet);
            sheet.makeKeyAndOrderFront(null);
        }

        @Override
        public void closed(final NSWindow sheet, final int returncode) {
            option.set(returncode);
            sheet.performClose(null);
        }

        private static final class SheetCallbackWindowListener implements WindowListener {
            private final SheetCallback callback;
            private final AtomicInteger returncode;

            public SheetCallbackWindowListener(final SheetCallback callback, final AtomicInteger returncode) {
                this.callback = callback;
                this.returncode = returncode;
            }

            @Override
            public void windowWillClose() {
                callback.callback(returncode.get());
            }
        }
    }

    /**
     * Floating window ordered front
     */
    public static class FloatingWindowAlertRunner extends RegularWindowAlertRunner {
        public FloatingWindowAlertRunner(final SheetController controller) {
            super(controller);
        }

        @Override
        public void alert(final NSWindow sheet, final SheetCallback callback) {
            log.debug("Configure window {}", sheet);
            sheet.setHidesOnDeactivate(false);
            // Activate ignoring other applications and move window to floating window level
            sheet.setLevel(NSWindow.NSWindowLevel.NSFloatingWindowLevel);
            super.alert(sheet, callback);
        }
    }

    /**
     * Floating window in modal run loop
     */
    public static class ModalWindowAlertRunner extends FloatingWindowAlertRunner implements AlertRunner.CloseHandler {
        public ModalWindowAlertRunner(final SheetController controller) {
            super(controller);
        }

        @Override
        public void closed(final NSWindow sheet, final int returncode) {
            log.debug("Stop modal with return code {}", returncode);
            // The result code you want returned from the runModalForWindow:
            NSApplication.sharedApplication().stopModalWithCode(returncode);
            // Close window
            sheet.close();
        }

        /**
         * Run modal alert window
         *
         * @param sheet    Alert window
         * @param callback Proxy to send dismiss notification
         * @see SheetCallback
         */
        @Override
        public void alert(final NSWindow sheet, final SheetCallback callback) {
            sheet.setPreventsApplicationTerminationWhenModal(false);
            sheet.setLevel(NSWindow.NSWindowLevel.NSModalPanelWindowLevel);
            // This method runs a modal event loop for the specified window synchronously. It displays the specified window, makes it key,
            // starts the run loop, and processes events for that window.
            log.debug("Run modal for window {} with callback {}", sheet, callback);
            // You do not need to show the window yourself
            callback.callback(NSApplication.sharedApplication().runModalForWindow(sheet).intValue());
        }
    }

    /**
     * Signal count down latch when sheet is dismissed
     */
    public static final class SignalSheetCallback implements SheetCallback {
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

    public static final class PopoverAlertRunner extends Proxy implements AlertRunner, AlertRunner.CloseHandler, WindowListener {
        private final NSPopover popover = NSPopover.create();
        private final NSView positioningView;
        private final NSRect positioningRect;
        private final SheetController controller;
        private final AtomicReference<Proxy> reference = new AtomicReference<>();
        private final AtomicInteger option = new AtomicInteger(SheetCallback.CANCEL_OPTION);
        private final int behaviour;

        public PopoverAlertRunner(final NSView positioningView, final SheetController controller) {
            this(positioningView, positioningView.frame(), controller);
        }

        public PopoverAlertRunner(final NSView positioningView, final SheetController controller, final int behaviour) {
            this(positioningView, positioningView.frame(), controller, behaviour);
        }

        public PopoverAlertRunner(final NSView positioningView, final NSRect positioningRect, final SheetController controller) {
            this(positioningView, positioningRect, controller, NSPopover.NSPopoverBehaviorSemitransient);
        }

        public PopoverAlertRunner(final NSView positioningView, final NSRect positioningRect, final SheetController controller, final int behaviour) {
            this.positioningView = positioningView;
            this.positioningRect = positioningRect;
            this.controller = controller;
            this.controller.addHandler(this);
            this.behaviour = behaviour;
        }

        @Override
        public void alert(final NSWindow sheet, final SheetCallback callback) {
            NSApplication.sharedApplication().activateIgnoringOtherApps(true);
            final Proxy proxy = new PopoverDelegate(controller, option, callback);
            reference.set(proxy);
            popover.setDelegate(proxy.id());
            popover.setAnimates(false);
            popover.setBehavior(behaviour);
            final NSViewController viewController = NSViewController.create();
            viewController.setView(sheet.contentView());
            popover.setContentViewController(viewController);
            popover.showRelativeToRect_ofView_preferredEdge(positioningRect, positioningView,
                    FoundationKitFunctions.NSRectEdge.NSMinYEdge);
            controller.addListener(this);
        }

        @Override
        public void windowDidResize(final NSSize windowFrame) {
            log.debug("Resize popover to {}", windowFrame);
            final NSSize intrinsicContentSize = controller.view().intrinsicContentSize();
            // Adjust height only
            popover.setContentSize(new NSSize(popover.contentSize().width.doubleValue(), intrinsicContentSize.height.doubleValue()));
        }

        @Override
        public void closed(final NSWindow sheet, final int returncode) {
            option.set(returncode);
            popover.performClose(null);
        }
    }

    public static final class PopoverDelegate extends Proxy {
        private final SheetController controller;
        private final AtomicInteger returncode;
        private final SheetCallback callback;

        public PopoverDelegate(final SheetController controller, final AtomicInteger returncode, final SheetCallback callback) {
            this.controller = controller;
            this.returncode = returncode;
            this.callback = callback;
        }

        @Delegate
        public void popoverWillClose(final NSNotification notification) {
            final NSObject popoverCloseReasonValue = notification.userInfo().objectForKey(NSPopover.NSPopoverCloseReasonKey);
            if(popoverCloseReasonValue != null) {
                if(NSPopover.NSPopoverCloseReasonValue.NSPopoverCloseReasonStandard.equals(popoverCloseReasonValue.toString())) {
                    log.debug("Notify {} of return code {}", callback, returncode.get());
                    // No window close notification for popover
                    controller.invalidate();
                    callback.callback(returncode.get());
                }
                else {
                    log.debug("Ignore notification {}", notification);
                }
            }
        }

        @Delegate
        public boolean popoverShouldDetach(final NSPopover popover) {
            return true;
        }
    }

    /**
     * Adjust frame of subview to match the parent view and attach with translating autoresizing mask into constraints
     *
     * @param parent  Container View
     * @param subview Content View
     */
    protected void addSubview(final NSView parent, final NSView subview) {
        subview.setTranslatesAutoresizingMaskIntoConstraints(true);
        subview.setFrame(parent.bounds());
        parent.addSubview(subview);
    }
}
