package ch.cyberduck.core.local;

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

import ch.cyberduck.binding.application.NSOpenPanel;
import ch.cyberduck.binding.application.SheetCallback;
import ch.cyberduck.binding.foundation.NSArray;
import ch.cyberduck.binding.foundation.NSData;
import ch.cyberduck.binding.foundation.NSEnumerator;
import ch.cyberduck.binding.foundation.NSObject;
import ch.cyberduck.binding.foundation.NSURL;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.LocalFactory;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.exception.LocalAccessDeniedException;
import ch.cyberduck.core.preferences.Preferences;
import ch.cyberduck.core.preferences.PreferencesFactory;

import org.apache.log4j.Logger;
import org.rococoa.ObjCObjectByReference;
import org.rococoa.cocoa.foundation.NSError;
import org.rococoa.cocoa.foundation.NSInteger;

import java.text.MessageFormat;
import java.util.concurrent.atomic.AtomicReference;

public abstract class AbstractPromptBookmarkResolver implements FilesystemBookmarkResolver<NSURL> {
    private static final Logger log = Logger.getLogger(AbstractPromptBookmarkResolver.class);

    private final Preferences preferences = PreferencesFactory.get();

    private final int create;

    private final int resolve;

    /**
     * @param create  Create options
     * @param resolve Resolve options
     */
    public AbstractPromptBookmarkResolver(final int create, final int resolve) {
        this.create = create;
        this.resolve = resolve;
    }


    @Override
    public String create(final Local file) throws AccessDeniedException {
        final ObjCObjectByReference error = new ObjCObjectByReference();
        // Create new security scoped bookmark
        final NSURL url = NSURL.fileURLWithPath(file.getAbsolute());
        if(log.isTraceEnabled()) {
            log.trace(String.format("Resolved file %s to url %s", file, url));
        }
        final NSData data = url.bookmarkDataWithOptions_includingResourceValuesForKeys_relativeToURL_error(
                create, null, null, error);
        if(null == data) {
            log.warn(String.format("Failure getting bookmark data for file %s", file));
            final NSError f = error.getValueAs(NSError.class);
            if(null == f) {
                throw new LocalAccessDeniedException(file.getAbsolute());
            }
            throw new LocalAccessDeniedException(String.format("%s", f.localizedDescription()));
        }
        final String encoded = data.base64Encoding();
        if(log.isTraceEnabled()) {
            log.trace(String.format("Encoded bookmark for %s as %s", file, encoded));
        }
        return encoded;
    }

    @Override
    public NSURL resolve(final Local file) throws AccessDeniedException {
        final NSData bookmark;
        if(null == file.getBookmark()) {
            if(preferences.getBoolean("local.bookmark.resolve.prompt")) {
                // Prompt user if no bookmark reference is available
                final String reference = this.choose(file);
                file.setBookmark(reference);
                bookmark = NSData.dataWithBase64EncodedString(reference);
            }
            else {
                throw new LocalAccessDeniedException(String.format("No security scoped bookmark for %s", file));
            }
        }
        else {
            bookmark = NSData.dataWithBase64EncodedString(file.getBookmark());
        }
        final ObjCObjectByReference error = new ObjCObjectByReference();
        final NSURL resolved = NSURL.URLByResolvingBookmarkData(bookmark, resolve, error);
        if(null == resolved) {
            log.warn(String.format("Error resolving bookmark for %s to URL", file));
            final NSError f = error.getValueAs(NSError.class);
            if(null == f) {
                throw new LocalAccessDeniedException(file.getAbsolute());
            }
            throw new LocalAccessDeniedException(String.format("%s", f.localizedDescription()));
        }
        return resolved;
    }

    /**
     * @return Security scoped bookmark
     */
    public String choose(final Local file) throws AccessDeniedException {
        final AtomicReference<String> bookmark = new AtomicReference<String>();
        log.warn(String.format("Prompt for file %s to obtain bookmark reference", file));
        final NSOpenPanel panel = NSOpenPanel.openPanel();
        panel.setCanChooseDirectories(file.isDirectory());
        panel.setCanChooseFiles(file.isFile());
        panel.setAllowsMultipleSelection(false);
        panel.setMessage(MessageFormat.format(LocaleFactory.localizedString("Select the file {0}", "Credentials"),
                file.getAbbreviatedPath()));
        panel.setPrompt(LocaleFactory.localizedString("Choose"));
        final NSInteger modal = panel.runModal(file.getParent().getAbsolute(), file.getName());
        if(modal.intValue() == SheetCallback.DEFAULT_OPTION) {
            final NSArray selected = panel.filenames();
            final NSEnumerator enumerator = selected.objectEnumerator();
            NSObject next;
            while((next = enumerator.nextObject()) != null) {
                final Local f = LocalFactory.get(next.toString());
                // Save Base64 encoded scoped reference
                bookmark.set(this.create(f));
            }
        }
        panel.close();
        final String reference = bookmark.get();
        if(reference == null) {
            throw new LocalAccessDeniedException(String.format("Prompt for %s canceled", file));
        }
        return reference;
    }

}
