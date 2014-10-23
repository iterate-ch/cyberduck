package ch.cyberduck.core.importer;

/*
 * Copyright (c) 2002-2014 David Kocher. All rights reserved.
 * http://cyberduck.io/
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
 * feedback@cyberduck.io
 */

import ch.cyberduck.core.AbstractHostCollection;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.PasswordStore;
import ch.cyberduck.core.PasswordStoreFactory;
import ch.cyberduck.core.Preferences;
import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.io.MD5ChecksumCompute;
import ch.cyberduck.core.local.Application;
import ch.cyberduck.core.local.ApplicationFinderFactory;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.text.MessageFormat;
import java.util.Iterator;

/**
 * @version $Id$
 */
public abstract class ThirdpartyBookmarkCollection extends AbstractHostCollection {
    private static final Logger log = Logger.getLogger(ThirdpartyBookmarkCollection.class);

    private static final long serialVersionUID = -4582425984484543617L;

    private PasswordStore keychain = PasswordStoreFactory.get();

    @Override
    public String getName() {
        final Application application = ApplicationFinderFactory.get().getDescription(this.getBundleIdentifier());
        if(null == application) {
            return LocaleFactory.localizedString("Unknown");
        }
        return application.getName();
    }

    @Override
    public void load() throws AccessDeniedException {
        final Local file = this.getFile();
        if(file.exists()) {
            if(log.isInfoEnabled()) {
                log.info(String.format("Found bookmarks file at %s", file));
            }
            String current = null;
            try {
                current = new MD5ChecksumCompute().compute(file.getInputStream());
                if(log.isDebugEnabled()) {
                    log.debug(String.format("Current checksum for %s is %s", file, current));
                }
            }
            catch(BackgroundException e) {
                log.warn(String.format("Failure obtaining checksum for %s", file));
            }
            if(Preferences.instance().getBoolean(this.getConfiguration())) {
                // Previously imported
                final String previous = Preferences.instance().getProperty(String.format("%s.checksum", this.getConfiguration()));
                if(log.isDebugEnabled()) {
                    log.debug(String.format("Saved previous checksum %s for bookmark %s", previous, file));
                }
                if(StringUtils.isNotBlank(previous)) {
                    if(previous.equals(current)) {
                        if(log.isInfoEnabled()) {
                            log.info(String.format("Skip importing bookmarks from %s with previously saved checksum %s", file, previous));
                        }
                    }
                    else {
                        if(log.isInfoEnabled()) {
                            log.info(String.format("Checksum changed for bookmarks file at %s", file));
                        }
                        // Should filter existing bookmarks
                        this.parse(file);
                    }
                }
                else {
                    // Skip flagged
                    if(log.isDebugEnabled()) {
                        log.debug(String.format("Skip importing bookmarks from %s", file));
                    }
                }
            }
            else {
                // First import
                this.parse(file);
            }
            // Save last checksum
            if(StringUtils.isNotBlank(current)) {
                Preferences.instance().setProperty(String.format("%s.checksum", this.getConfiguration()), current);
            }
        }
        else {
            if(log.isInfoEnabled()) {
                log.info(String.format("No bookmarks file at %s", file));
            }
        }
        // Flag as imported
        super.load();
    }

    public abstract Local getFile();

    protected abstract void parse(Local file) throws AccessDeniedException;

    public boolean isInstalled() {
        return StringUtils.isNotBlank(this.getName());
    }

    public abstract String getBundleIdentifier();

    public String getConfiguration() {
        return String.format("bookmark.import.%s", this.getBundleIdentifier());
    }

    @Override
    public boolean add(final Host bookmark) {
        if(null == bookmark) {
            log.warn("Parsing bookmark failed.");
            return false;
        }
        final StringBuilder comment = new StringBuilder();
        if(StringUtils.isNotBlank(bookmark.getComment())) {
            comment.append(bookmark.getComment());
            if(!comment.toString().endsWith(".")) {
                comment.append(".");
            }
            comment.append(" ");
        }
        comment.append(MessageFormat.format(LocaleFactory.localizedString("Imported from {0}", "Configuration"),
                this.getName()));
        bookmark.setComment(comment.toString());
        if(log.isDebugEnabled()) {
            log.debug(String.format("Create new bookmark from import %s", bookmark));
        }
        // Save password if any to Keychain
        if(StringUtils.isNotBlank(bookmark.getCredentials().getPassword())) {
            keychain.addPassword(bookmark.getProtocol().getScheme(), bookmark.getPort(),
                    bookmark.getHostname(), bookmark.getCredentials().getUsername(), bookmark.getCredentials().getPassword());
            // Reset password in memory
            bookmark.getCredentials().setPassword(null);
        }
        return super.add(bookmark);
    }

    /**
     * Remove all that are contained within the collection passed
     */
    public void filter(final AbstractHostCollection bookmarks) {
        for(Iterator<Host> iter = this.iterator(); iter.hasNext(); ) {
            final Host i = iter.next();
            if(bookmarks.find(i)) {
                if(log.isInfoEnabled()) {
                    log.info(String.format("Remove %s from import as we found it in bookmarks", i));
                }
                iter.remove();
            }
        }
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ThirdpartyBookmarkCollection{");
        sb.append("file=").append(this.getFile());
        sb.append('}');
        return sb.toString();
    }
}
