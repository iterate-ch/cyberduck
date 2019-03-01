package ch.cyberduck.ui.cocoa.callback;

/*
 * Copyright (c) 2002-2017 iterate GmbH. All rights reserved.
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

import ch.cyberduck.binding.WindowController;
import ch.cyberduck.binding.application.SheetCallback;
import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.LoginOptions;
import ch.cyberduck.core.PasswordCallback;
import ch.cyberduck.core.exception.LoginCanceledException;
import ch.cyberduck.core.oauth.OAuth2TokenListener;
import ch.cyberduck.core.oauth.OAuth2TokenListenerRegistry;
import ch.cyberduck.ui.cocoa.controller.PasswordController;

import org.apache.log4j.Logger;

public class PromptPasswordCallback implements PasswordCallback {
    private static final Logger log = Logger.getLogger(PromptPasswordCallback.class);

    private final WindowController parent;

    private boolean suppressed;

    public PromptPasswordCallback(final WindowController parent) {
        this.parent = parent;
    }

    @Override
    public Credentials prompt(final Host bookmark, final String title, final String reason, final LoginOptions options) throws LoginCanceledException {
        if(suppressed) {
            throw new LoginCanceledException();
        }
        final Credentials credentials = new Credentials().withSaved(options.save);
        final PasswordController controller = new PasswordController(bookmark, credentials, title, reason, options);
        if(options.oauth) {
            final OAuth2TokenListenerRegistry registry = OAuth2TokenListenerRegistry.get();
            registry.register(new OAuth2TokenListener() {
                @Override
                public void callback(final String param) {
                    log.warn(String.format("Callback with code %s", param));
                    credentials.setPassword(param);
                    controller.closeSheet(SheetCallback.DEFAULT_OPTION);
                }
            });
        }
        final int option = controller.beginSheet(parent);
        if(option == SheetCallback.CANCEL_OPTION) {
            if(controller.isSuppressed()) {
                suppressed = true;
            }
            throw new LoginCanceledException();
        }
        return credentials;
    }
}
