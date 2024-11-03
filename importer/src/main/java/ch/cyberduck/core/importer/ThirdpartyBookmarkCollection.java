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
import ch.cyberduck.core.Collection;
import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.PasswordStore;
import ch.cyberduck.core.PasswordStoreFactory;
import ch.cyberduck.core.ProtocolFactory;
import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.LocalAccessDeniedException;
import ch.cyberduck.core.io.Checksum;
import ch.cyberduck.core.io.ChecksumComputeFactory;
import ch.cyberduck.core.io.HashAlgorithm;
import ch.cyberduck.core.preferences.Preferences;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.text.MessageFormat;
import java.util.Iterator;

public abstract class ThirdpartyBookmarkCollection extends Collection<Host> {
    private static final Logger log = LogManager.getLogger(ThirdpartyBookmarkCollection.class);

    private final Preferences preferences = PreferencesFactory.get();
    private final PasswordStore keychain;

    public ThirdpartyBookmarkCollection() {
        this.keychain = PasswordStoreFactory.get();
    }

    public ThirdpartyBookmarkCollection(final PasswordStore keychain) {
        this.keychain = keychain;
    }

    @Override
    public void load() throws AccessDeniedException {
        final Local file = this.getFile();
        if(file.exists()) {
            if(log.isInfoEnabled()) {
                log.info("Found bookmarks file at {}", file);
            }
            Checksum current = Checksum.NONE;
            if(file.isFile()) {
                try {
                    current = ChecksumComputeFactory.get(HashAlgorithm.md5).compute(file.getInputStream(), new TransferStatus());
                    if(log.isDebugEnabled()) {
                        log.debug("Current checksum for {} is {}", file, current);
                    }
                }
                catch(BackgroundException e) {
                    log.warn("Failure obtaining checksum for {}", file);
                }
            }
            if(preferences.getBoolean(this.getConfiguration())) {
                // Previously imported
                final Checksum previous = new Checksum(HashAlgorithm.md5,
                    preferences.getProperty(String.format("%s.checksum", this.getConfiguration())));
                if(log.isDebugEnabled()) {
                    log.debug("Saved previous checksum {} for bookmark {}", previous, file);
                }
                if(StringUtils.isNotBlank(previous.hash)) {
                    if(previous.equals(current)) {
                        if(log.isInfoEnabled()) {
                            log.info("Skip importing bookmarks from {} with previously saved checksum {}", file, previous);
                        }
                    }
                    else {
                        if(log.isInfoEnabled()) {
                            log.info("Checksum changed for bookmarks file at {}", file);
                        }
                        // Should filter existing bookmarks. Skip import
                    }
                }
                else {
                    // Skip flagged
                    if(log.isDebugEnabled()) {
                        log.debug("Skip importing bookmarks from {}", file);
                    }
                }
            }
            else {
                // First import
                this.parse(ProtocolFactory.get(), file);
            }
            // Save last checksum
            if(current != null) {
                preferences.setProperty(String.format("%s.checksum", this.getConfiguration()), current.hash);
            }
        }
        else {
            if(log.isInfoEnabled()) {
                log.info("No bookmarks file at {}", file);
            }
        }
        // Flag as imported
        super.load();
    }

    /**
     * @return Application name
     */
    public abstract String getName();

    public abstract Local getFile();

    protected void parse(Local file) throws AccessDeniedException {
        this.parse(ProtocolFactory.get(), file);
    }

    protected abstract void parse(final ProtocolFactory protocols, Local file) throws AccessDeniedException;

    public boolean isInstalled() {
        return this.getFile().exists();
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
            log.debug("Create new bookmark from import {}", bookmark);
        }
        // Save password if any to Keychain
        final Credentials credentials = bookmark.getCredentials();
        if(StringUtils.isNotBlank(credentials.getPassword())) {
            if(credentials.isPublicKeyAuthentication()) {
                try {
                    keychain.addPassword(bookmark.getHostname(), credentials.getIdentity().getAbbreviatedPath(),
                        credentials.getPassword());
                }
                catch(LocalAccessDeniedException e) {
                    log.error("Failure {} saving credentials for {} in password store", e, bookmark);
                }
            }
            else if(!credentials.isAnonymousLogin()) {
                try {
                    keychain.addPassword(bookmark.getProtocol().getScheme(), bookmark.getPort(),
                        bookmark.getHostname(), credentials.getUsername(), credentials.getPassword());
                }
                catch(LocalAccessDeniedException e) {
                    log.error("Failure {} saving credentials for {} in password store", e, bookmark);
                }
                // Reset password in memory
                credentials.setPassword(null);
            }
        }
        return super.add(bookmark);
    }

    /**
     * Remove all that are contained within the collection passed
     */
    public void filter(final AbstractHostCollection bookmarks) {
        for(Iterator<Host> iter = this.iterator(); iter.hasNext(); ) {
            final Host i = iter.next();
            if(bookmarks.find(new AbstractHostCollection.HostComparePredicate(i)).isPresent()) {
                if(log.isInfoEnabled()) {
                    log.info("Remove {} from import as we found it in bookmarks", i);
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
