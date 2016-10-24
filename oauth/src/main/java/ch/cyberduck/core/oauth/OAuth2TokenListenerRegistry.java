package ch.cyberduck.core.oauth;

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

import ch.cyberduck.core.exception.ConnectionCanceledException;
import ch.cyberduck.core.threading.CancelCallback;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class OAuth2TokenListenerRegistry {

    private static final OAuth2TokenListenerRegistry global = new OAuth2TokenListenerRegistry();

    public static OAuth2TokenListenerRegistry get() {
        return global;
    }

    private final Set<OAuth2TokenListener> listeners = new HashSet<>();

    public void register(final OAuth2TokenListener listener, final CancelCallback cancel) throws ConnectionCanceledException {
        listeners.add(listener);
        synchronized(global) {
            try {
                while(listeners.contains(listener)) {
                    // Not yet notified
                    cancel.verify();
                    global.wait(500L);
                }
            }
            catch(InterruptedException e) {
                throw new OAuthInterruptedException();
            }
        }
    }

    public void notify(final String token) {
        for(Iterator<OAuth2TokenListener> iter = listeners.iterator(); iter.hasNext(); ) {
            iter.next().callback(token);
            iter.remove();
        }
        synchronized(global) {
            global.notifyAll();
        }
    }
}
