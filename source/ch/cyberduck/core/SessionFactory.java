package ch.cyberduck.core;

/*
 * Copyright (c) 2012 David Kocher. All rights reserved.
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

import ch.cyberduck.core.cf.CFSession;
import ch.cyberduck.core.dav.DAVSession;
import ch.cyberduck.core.ftp.FTPSession;
import ch.cyberduck.core.gstorage.GSSession;
import ch.cyberduck.core.s3.S3Session;
import ch.cyberduck.core.sftp.SFTPSession;

import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

public abstract class SessionFactory {
    private static final Logger log = Logger.getLogger(SessionFactory.class);

    private static final Map<Protocol, SessionFactory> factories
            = new HashMap<Protocol, SessionFactory>() {
        @Override
        public SessionFactory get(final Object key) {
            if(!(key instanceof Protocol)) {
                throw new FactoryException(String.format("No factory for key %s", key));
            }
            final Protocol p = (Protocol) key;
            if(!factories.containsKey(p)) {
                if(log.isDebugEnabled()) {
                    log.debug(String.format("Register protocol %s", p.getIdentifier()));
                }
                switch(p.getType()) {
                    case ftp:
                        factories.put(p, new SessionFactory() {
                            @Override
                            protected Session create(Host h) {
                                return new FTPSession(h);
                            }
                        });
                        break;
                    case sftp:
                        factories.put(p, new SessionFactory() {
                            @Override
                            protected Session create(Host h) {
                                return new SFTPSession(h);
                            }
                        });
                        break;
                    case dav:
                        factories.put(p, new SessionFactory() {
                            @Override
                            protected Session create(Host h) {
                                return new DAVSession(h);
                            }
                        });
                        break;
                    case s3:
                        factories.put(p, new SessionFactory() {
                            @Override
                            protected Session create(Host h) {
                                return new S3Session(h);
                            }
                        });
                        break;
                    case googlestorage:
                        factories.put(p, new SessionFactory() {
                            @Override
                            protected Session create(Host h) {
                                return new GSSession(h);
                            }
                        });
                        break;
                    case swift:
                        factories.put(p, new SessionFactory() {
                            @Override
                            protected Session create(Host h) {
                                return new CFSession(h);
                            }
                        });
                        break;
                    default:
                        throw new FactoryException(String.format("No factory for protocol %s", p));
                }
            }
            return super.get(key);
        }
    };

    protected abstract Session create(Host h);

    public static Session createSession(final Host h) {
        return factories.get(h.getProtocol()).create(h);
    }
}
