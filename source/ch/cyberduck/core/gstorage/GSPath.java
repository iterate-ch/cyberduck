package ch.cyberduck.core.gstorage;

/*
 * Copyright (c) 2002-2010 David Kocher. All rights reserved.
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

import ch.cyberduck.core.Local;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathFactory;
import ch.cyberduck.core.Permission;
import ch.cyberduck.core.i18n.Locale;
import ch.cyberduck.core.s3.S3Path;
import ch.cyberduck.core.s3.S3Session;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.jets3t.service.acl.AccessControlList;
import org.jets3t.service.acl.CanonicalGrantee;
import org.jets3t.service.acl.GroupGrantee;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.Set;

/**
 * @version $Id$
 */
public class GSPath extends S3Path {
    private static Logger log = Logger.getLogger(GSPath.class);

    private static class Factory extends PathFactory<GSSession> {
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
    }

    public static PathFactory factory() {
        return new Factory();
    }

    protected GSPath(S3Session s, String parent, String name, int type) {
        super(s, parent, name, type);
    }

    protected GSPath(S3Session s, String path, int type) {
        super(s, path, type);
    }

    protected GSPath(S3Session s, String parent, Local file) {
        super(s, parent, file);
    }

    protected <T> GSPath(S3Session s, T dict) {
        super(s, dict);
    }

    /**
     * This creates an URL that uses Cookie-based Authentication. The ACLs for the given Google user account
     * has to be setup first.
     * <p/>
     * Google Storage lets you provide browser-based authenticated downloads to users who do not have
     * Google Storage accounts. To do this, you apply Google account-based ACLs to the object and then
     * you provide users with a URL that is scoped to the object.
     *
     * @return
     */
    @Override
    public DescriptiveUrl toAuthenticatedUrl() {
        if(this.attributes().isFile()) {
            // Authenticated browser download using cookie-based Google account authentication in conjunction with ACL
            return new DescriptiveUrl("https://sandbox.google.com/storage" + this.getAbsolute());
        }
        return new DescriptiveUrl(null, null);
    }

    @Override
    public DescriptiveUrl toSignedUrl() {
        return new DescriptiveUrl(null, null);
    }

    /**
     * Torrent links are not supported.
     *
     * @return Always null.
     */
    @Override
    public DescriptiveUrl toTorrentUrl() {
        return new DescriptiveUrl(null, null);
    }

    @Override
    public Set<DescriptiveUrl> getHttpURLs() {
        Set<DescriptiveUrl> urls = super.getHttpURLs();
        DescriptiveUrl url = this.toAuthenticatedUrl();
        if(StringUtils.isNotBlank(url.getUrl())) {
            urls.add(new DescriptiveUrl(url.getUrl(),
                    MessageFormat.format(Locale.localizedString("{0} URL"), Locale.localizedString("Authenticated"))));
        }
        return urls;

    }
}