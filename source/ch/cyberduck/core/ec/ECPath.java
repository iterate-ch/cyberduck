package ch.cyberduck.core.ec;

/*
 *  Copyright (c) 2008 David Kocher. All rights reserved.
 *  http://cyberduck.ch/
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  Bug fixes, suggestions and comments should be sent to:
 *  dkocher@cyberduck.ch
 */

import ch.cyberduck.core.*;
import ch.cyberduck.core.cloud.Distribution;
import ch.cyberduck.core.s3.S3Path;
import ch.cyberduck.ui.cocoa.foundation.NSDictionary;

import org.jets3t.service.S3ServiceException;
import org.jets3t.service.model.S3Object;

/**
 * @version $Id$
 */
public class ECPath extends S3Path {

    static {
        PathFactory.addFactory(Protocol.EUCALYPTUS, new Factory());
    }

    private static class Factory extends PathFactory {
        protected Path create(Session session, String path, int type) {
            return new ECPath((ECSession) session, path, type);
        }

        protected Path create(Session session, String parent, String name, int type) {
            return new ECPath((ECSession) session, parent, name, type);
        }

        protected Path create(Session session, String path, Local file) {
            return new ECPath((ECSession) session, path, file);
        }

        protected Path create(Session session, NSDictionary dict) {
            return new ECPath((ECSession) session, dict);
        }
    }

    protected ECPath(ECSession s, String parent, String name, int type) {
        super(s, parent, name, type);
    }

    protected ECPath(ECSession s, String path, int type) {
        super(s, path, type);
    }

    protected ECPath(ECSession s, String parent, Local file) {
        super(s, parent, file);
    }

    protected ECPath(ECSession s, NSDictionary dict) {
        super(s, dict);
    }

    @Override
    protected S3Object getDetails() throws S3ServiceException {
        return null;
    }

    /**
     * @return
     */
    @Override
    public Distribution readDistribution() {
        throw new UnsupportedOperationException();
    }

    /**
     * Amazon CloudFront Extension
     *
     * @param enabled
     * @param cnames
     * @param logging
     */
    @Override
    public void writeDistribution(final boolean enabled, final String[] cnames, boolean logging) {
        throw new UnsupportedOperationException();
    }
}