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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

public class OAuth2TokenListenerRegistry {
    private static final Logger log = LogManager.getLogger(OAuth2TokenListenerRegistry.class);

    private static final OAuth2TokenListenerRegistry global = new OAuth2TokenListenerRegistry();

    public static OAuth2TokenListenerRegistry get() {
        return global;
    }

    private final Map<String, OAuth2TokenListener> listeners = new HashMap<>();

    public void register(final String state, final OAuth2TokenListener listener) {
        log.debug("Register listener for state {}", state);
        listeners.put(state, listener);
    }

    public void notify(final String state, final String token) {
        final OAuth2TokenListener listener = listeners.get(state);
        if(null == listener) {
            log.error("Missing listener for state {}", state);
            return;
        }
        listeners.remove(state);
        log.debug("Notify listener for state {} with token {}", state, token);
        listener.callback(token);
    }

    public void shutdown() {
        for(OAuth2TokenListener listener : listeners.values()) {
            listener.callback(null);
        }
    }
}
