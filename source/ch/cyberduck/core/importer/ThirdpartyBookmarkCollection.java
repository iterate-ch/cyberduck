package ch.cyberduck.core.importer;

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

import ch.cyberduck.core.AbstractHostCollection;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.KeychainFactory;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.i18n.Locale;
import ch.cyberduck.ui.cocoa.odb.EditorFactory;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.text.MessageFormat;

/**
 * @version $Id:$
 */
public abstract class ThirdpartyBookmarkCollection extends AbstractHostCollection {
    private static Logger log = Logger.getLogger(ThirdpartyBookmarkCollection.class);

    /**
     * Parse the bookmark file.
     *
     * @param file Local path.
     */
    protected void load(Local file) {
        if(file.exists()) {
            if(log.isInfoEnabled()) {
                log.info("Found Bookmarks file: " + file.getAbsolute());
            }
            this.parse(file);
        }
    }

    @Override
    public void load() {
        this.load(this.getFile());
        super.load();
    }

    public abstract Local getFile();

    protected abstract void parse(Local file);

    @Override
    public String getName() {
        return EditorFactory.getApplicationName(this.getBundleIdentifier());
    }

    public boolean isInstalled() {
        return StringUtils.isNotBlank(this.getName());
    }

    public abstract String getBundleIdentifier();

    public String getConfiguration() {
        return "bookmark.import." + this.getBundleIdentifier();
    }

    @Override
    public boolean add(Host bookmark) {
        if(null == bookmark) {
            log.warn("Parsing bookmark failed.");
            return false;
        }
        StringBuilder comment = new StringBuilder();
        if(StringUtils.isNotBlank(bookmark.getComment())) {
            comment.append(bookmark.getComment());
            if(!comment.toString().endsWith(".")) {
                comment.append(".");
            }
            comment.append(" ");
        }
        comment.append(MessageFormat.format(Locale.localizedString("Imported from {0}", "Configuration"),
                this.getName()));
        bookmark.setComment(comment.toString());
        if(log.isDebugEnabled()) {
            log.debug("Create new bookmark from import: " + bookmark);
        }
        // Save password if any to Keychain
        KeychainFactory.instance().save(bookmark);
        // Reset password in memory
        bookmark.getCredentials().setPassword(null);
        return super.add(bookmark);
    }
}
