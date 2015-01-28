package ch.cyberduck.core.local;

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

import ch.cyberduck.binding.foundation.NSData;
import ch.cyberduck.binding.foundation.NSURL;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.preferences.Preferences;
import ch.cyberduck.core.preferences.PreferencesFactory;

import org.apache.log4j.Logger;
import org.rococoa.ObjCObjectByReference;
import org.rococoa.cocoa.foundation.NSError;

import java.util.concurrent.atomic.AtomicReference;

/**
 * @version $Id$
 */
public class PanelSandboxBookmarkResolver implements SandboxBookmarkResolver<NSURL> {
    private static final Logger log = Logger.getLogger(PanelSandboxBookmarkResolver.class);

    private final Preferences preferences = PreferencesFactory.get();

    @Override
    public NSURL resolve(final Local file) throws AccessDeniedException {
        final NSData bookmark;
        if(null == file.getBookmark()) {
            if(preferences.getBoolean("local.bookmark.resolve.prompt")) {
                // Prompt user if no bookmark reference is available
                bookmark = NSData.dataWithBase64EncodedString(this.choose(file));
            }
            else {
                throw new AccessDeniedException(String.format("No security scoped bookmark for %s", file));
            }
        }
        else {
            bookmark = NSData.dataWithBase64EncodedString(file.getBookmark());
        }
        final ObjCObjectByReference error = new ObjCObjectByReference();
        final NSURL resolved = NSURL.URLByResolvingBookmarkData(bookmark, error);
        if(null == resolved) {
            final NSError f = error.getValueAs(NSError.class);
            log.error(String.format("Error resolving bookmark for %s to URL %s", file, f));
            if(null == f) {
                throw new AccessDeniedException(file.getAbsolute());
            }
            throw new AccessDeniedException(String.format("%s", f.localizedDescription()));
        }
        return resolved;
    }

    /**
     * @return Security scoped bookmark
     */

    private String choose(final Local file) throws AccessDeniedException {
        final AtomicReference<String> bookmark = new AtomicReference<String>();
        log.warn(String.format("Prompt for file %s to obtain bookmark reference", file));
//        this.invoke(new DefaultMainAction() {
//            @Override
//            public void run() {
//                final NSOpenPanel panel = NSOpenPanel.openPanel();
//                panel.setCanChooseDirectories(file.isDirectory());
//                panel.setCanChooseFiles(file.isFile());
//                panel.setAllowsMultipleSelection(false);
//                panel.setMessage(MessageFormat.format(LocaleFactory.localizedString("Select the file {0}", "Credentials"), file.getAbsolute()));
//                panel.setPrompt(LocaleFactory.localizedString("Choose"));
//                final NSInteger modal = panel.runModal(file.getParent().getAbsolute(), file.getName());
//                if(modal.intValue() == SheetCallback.DEFAULT_OPTION) {
//                    final NSArray selected = panel.filenames();
//                    final NSEnumerator enumerator = selected.objectEnumerator();
//                    NSObject next;
//                    while((next = enumerator.nextObject()) != null) {
//                        final Local f = LocalFactory.get(next.toString());
//                        bookmark.set(f.getBookmark());
//                    }
//                }
//                panel.close();
//            }
//        }, true);
        final String data = bookmark.get();
        if(data == null) {
            throw new AccessDeniedException(String.format("Prompt for %s canceled", file));
        }
        file.setBookmark(data);
        return data;
    }

    @Override
    public String create(final Local file) throws AccessDeniedException {
        if(file.exists()) {
            // Create new security scoped bookmark
            final ObjCObjectByReference error = new ObjCObjectByReference();
            final NSData data = NSURL.fileURLWithPath(file.getAbsolute()).bookmarkDataWithOptions_includingResourceValuesForKeys_relativeToURL_error(
                    NSURL.NSURLBookmarkCreationOptions.NSURLBookmarkCreationWithSecurityScope, null, null, error);
            if(null == data) {
                final NSError f = error.getValueAs(NSError.class);
                log.warn(String.format("Failure getting bookmark data for file %s %s", file, f));
                if(null == f) {
                    throw new AccessDeniedException(file.getAbsolute());
                }
                throw new AccessDeniedException(String.format("%s", f.localizedDescription()));
            }
            final String encoded = data.base64Encoding();
            if(log.isDebugEnabled()) {
                log.debug(String.format("Encoded bookmark for %s as %s", file, encoded));
            }
            return encoded;
        }
        else {
            log.warn(String.format("Skip creating bookmark for file not found %s", file));
            return null;
        }
    }
}
