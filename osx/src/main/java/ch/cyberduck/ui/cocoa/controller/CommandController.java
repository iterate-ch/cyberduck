package ch.cyberduck.ui.cocoa.controller;

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

import ch.cyberduck.binding.Action;
import ch.cyberduck.binding.Outlet;
import ch.cyberduck.binding.WindowController;
import ch.cyberduck.binding.application.NSButton;
import ch.cyberduck.binding.application.NSImage;
import ch.cyberduck.binding.application.NSImageView;
import ch.cyberduck.binding.application.NSLayoutManager;
import ch.cyberduck.binding.application.NSProgressIndicator;
import ch.cyberduck.binding.application.NSTextField;
import ch.cyberduck.binding.application.NSTextView;
import ch.cyberduck.binding.application.NSWindow;
import ch.cyberduck.binding.foundation.NSAttributedString;
import ch.cyberduck.binding.foundation.NSObject;
import ch.cyberduck.binding.foundation.NSRange;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.TranscriptListener;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Command;
import ch.cyberduck.core.local.Application;
import ch.cyberduck.core.pool.SessionPool;
import ch.cyberduck.core.resources.IconCacheFactory;
import ch.cyberduck.core.threading.ControllerBackgroundAction;
import ch.cyberduck.core.threading.WindowMainAction;

import org.apache.commons.lang3.StringUtils;
import org.rococoa.cocoa.foundation.NSUInteger;

public class CommandController extends WindowController implements TranscriptListener, NSLayoutManager.Delegate {

    @Outlet
    private NSTextField inputField;
    @Outlet
    private NSTextView responseField;
    @Outlet
    private NSProgressIndicator progress;
    @Outlet
    private NSImageView image;

    private final WindowController parent;

    private final SessionPool session;

    public CommandController(final WindowController parent, final SessionPool session) {
        this.parent = parent;
        this.session = session;
    }

    @Override
    public void setWindow(final NSWindow window) {
        window.setContentMinSize(window.frame().size);
        super.setWindow(window);
    }

    public void setInputField(NSTextField inputField) {
        this.inputField = inputField;
    }

    public void setResponseField(NSTextView responseField) {
        this.responseField = responseField;
        this.responseField.setEditable(false);
        this.responseField.setSelectable(true);
        this.responseField.setUsesFontPanel(false);
        this.responseField.setRichText(false);
        this.responseField.layoutManager().setDelegate(this.id());
    }

    public void setProgress(NSProgressIndicator progress) {
        this.progress = progress;
        this.progress.setDisplayedWhenStopped(false);
    }

    public void setImage(NSImageView image) {
        this.image = image;
        this.image.setImage(IconCacheFactory.<NSImage>get().applicationIcon(new Application("com.apple.Terminal"), 128));
    }

    @Override
    public void layoutManager_didCompleteLayoutForTextContainer_atEnd(NSLayoutManager layoutManager,
                                                                      NSObject textContainer,
                                                                      boolean finished) {
        if(finished && this.responseField.window().isVisible()) {
            this.responseField.scrollRangeToVisible(
                    NSRange.NSMakeRange(this.responseField.textStorage().length(), new NSUInteger(0))
            );
        }
    }

    @Override
    protected String getBundleName() {
        return "Command";
    }

    @Action
    public void sendButtonClicked(final NSButton sender) {
        final String command = this.inputField.stringValue();
        if(StringUtils.isNotBlank(command)) {
            progress.startAnimation(null);
            sender.setEnabled(false);
            parent.background(new ControllerBackgroundAction<Void>(this, session) {
                @Override
                public boolean alert(final BackgroundException e) {
                    return false;
                }

                @Override
                public Void run(final Session<?> session) throws BackgroundException {
                    final Command feature = session.getFeature(Command.class);
                    feature.send(command, this, CommandController.this);
                    return null;
                }

                @Override
                public void cleanup() {
                    super.cleanup();
                    progress.stopAnimation(null);
                    sender.setEnabled(true);
                }

                @Override
                public String getActivity() {
                    return command;
                }
            });
        }
    }

    @Override
    public void log(final Type request, final String message) {
        invoke(new WindowMainAction(this) {
            @Override
            public void run() {
                responseField.textStorage().replaceCharactersInRange_withAttributedString(
                        NSRange.NSMakeRange(responseField.textStorage().length(), new NSUInteger(0)),
                        NSAttributedString.attributedStringWithAttributes(message + "\n", FIXED_WITH_FONT_ATTRIBUTES));
            }
        });
    }

    @Override
    public void invalidate() {
        responseField.layoutManager().setDelegate(null);
        super.invalidate();
    }
}