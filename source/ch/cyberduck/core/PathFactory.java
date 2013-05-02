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

import ch.cyberduck.core.cf.CFPath;
import ch.cyberduck.core.cf.CFSession;
import ch.cyberduck.core.dav.DAVPath;
import ch.cyberduck.core.dav.DAVSession;
import ch.cyberduck.core.ftp.FTPPath;
import ch.cyberduck.core.ftp.FTPSession;
import ch.cyberduck.core.gstorage.GSPath;
import ch.cyberduck.core.gstorage.GSSession;
import ch.cyberduck.core.local.Local;
import ch.cyberduck.core.s3.S3Path;
import ch.cyberduck.core.s3.S3Session;
import ch.cyberduck.core.sftp.SFTPPath;
import ch.cyberduck.core.sftp.SFTPSession;

import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

public abstract class PathFactory<S extends Session> {
    private static final Logger log = Logger.getLogger(PathFactory.class);

    public static void register(Protocol protocol, PathFactory factory) {
        factories.put(protocol, factory);
    }

    /**
     * Registered factories
     */
    private static final Map<Protocol, PathFactory> factories = new HashMap<Protocol, PathFactory>() {
        @Override
        public PathFactory get(final Object key) {
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
                        register(p, new PathFactory<FTPSession>() {
                            @Override
                            protected Path create(FTPSession session, String path, int type) {
                                return new FTPPath(session, path, type);
                            }

                            @Override
                            protected Path create(FTPSession session, String parent, String name, int type) {
                                return new FTPPath(session, parent, name, type);
                            }

                            @Override
                            protected Path create(FTPSession session, String parent, Local file) {
                                return new FTPPath(session, parent, file);
                            }

                            @Override
                            protected <T> Path create(FTPSession session, T dict) {
                                return new FTPPath(session, dict);
                            }
                        });
                        break;
                    case sftp:
                        register(p, new PathFactory<SFTPSession>() {
                            @Override
                            protected Path create(SFTPSession session, String path, int type) {
                                return new SFTPPath(session, path, type);
                            }

                            @Override
                            protected Path create(SFTPSession session, String parent, String name, int type) {
                                return new SFTPPath(session, parent, name, type);
                            }

                            @Override
                            protected Path create(SFTPSession session, String parent, Local file) {
                                return new SFTPPath(session, parent, file);
                            }

                            @Override
                            protected <T> Path create(SFTPSession session, T dict) {
                                return new SFTPPath(session, dict);
                            }
                        });
                        break;
                    case dav:
                        register(p, new PathFactory<DAVSession>() {
                            @Override
                            protected Path create(DAVSession session, String path, int type) {
                                return new DAVPath(session, path, type);
                            }

                            @Override
                            protected Path create(DAVSession session, String parent, String name, int type) {
                                return new DAVPath(session, parent, name, type);
                            }

                            @Override
                            protected Path create(DAVSession session, String parent, Local file) {
                                return new DAVPath(session, parent, file);
                            }

                            @Override
                            protected <T> Path create(DAVSession session, T dict) {
                                return new DAVPath(session, dict);
                            }
                        });
                        break;
                    case s3:
                        register(p, new PathFactory<S3Session>() {
                            @Override
                            protected Path create(S3Session session, String path, int type) {
                                return new S3Path(session, path, type);
                            }

                            @Override
                            protected Path create(S3Session session, String parent, String name, int type) {
                                return new S3Path(session, parent, name, type);
                            }

                            @Override
                            protected Path create(S3Session session, String parent, Local file) {
                                return new S3Path(session, parent, file);
                            }

                            @Override
                            protected <T> Path create(S3Session session, T dict) {
                                return new S3Path(session, dict);
                            }
                        });
                        break;
                    case googlestorage:
                        register(p, new PathFactory<GSSession>() {
                            @Override
                            protected Path create(GSSession session, String path, int type) {
                                return new GSPath(session, path, type);
                            }

                            @Override
                            protected Path create(GSSession session, String parent, String name, int type) {
                                return new GSPath(session, parent, name, type);
                            }

                            @Override
                            protected Path create(GSSession session, String parent, Local file) {
                                return new GSPath(session, parent, file);
                            }

                            @Override
                            protected <T> Path create(GSSession session, T dict) {
                                return new GSPath(session, dict);
                            }
                        });
                        break;
                    case swift:
                        register(p, new PathFactory<CFSession>() {
                            @Override
                            protected Path create(CFSession session, String path, int type) {
                                return new CFPath(session, path, type);
                            }

                            @Override
                            protected Path create(CFSession session, String parent, String name, int type) {
                                return new CFPath(session, parent, name, type);
                            }

                            @Override
                            protected Path create(CFSession session, String parent, Local file) {
                                return new CFPath(session, parent, file);
                            }

                            @Override
                            protected <T> Path create(CFSession session, T dict) {
                                return new CFPath(session, dict);
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

    protected abstract Path create(S session, String path, int type);

    protected abstract Path create(S session, String parent, String name, int type);

    protected abstract Path create(S session, String parent, Local file);

    protected abstract <T> Path create(S session, T dict);

    public static <S extends Session> Path createPath(S session, String parent, String name, int type) {
        return factories.get(session.getHost().getProtocol()).create(session, parent, name, type);
    }

    public static <S extends Session> Path createPath(S session, String path, int type) {
        return factories.get(session.getHost().getProtocol()).create(session, path, type);
    }

    public static <S extends Session> Path createPath(S session, String parent, Local file) {
        return factories.get(session.getHost().getProtocol()).create(session, parent, file);
    }

    public static <S extends Session, T> Path createPath(S session, T dict) {
        return factories.get(session.getHost().getProtocol()).create(session, dict);
    }
}