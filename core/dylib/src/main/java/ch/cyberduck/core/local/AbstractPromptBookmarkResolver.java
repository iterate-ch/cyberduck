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

import ch.cyberduck.binding.Proxy;
import ch.cyberduck.binding.application.NSOpenPanel;
import ch.cyberduck.binding.application.SheetCallback;
import ch.cyberduck.binding.foundation.NSArray;
import ch.cyberduck.binding.foundation.NSData;
import ch.cyberduck.binding.foundation.NSEnumerator;
import ch.cyberduck.binding.foundation.NSObject;
import ch.cyberduck.binding.foundation.NSURL;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.exception.LocalAccessDeniedException;
import ch.cyberduck.core.preferences.SecurityApplicationGroupSupportDirectoryFinder;
import ch.cyberduck.core.preferences.TemporarySupportDirectoryFinder;
import ch.cyberduck.core.threading.DefaultMainAction;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.rococoa.ObjCObjectByReference;
import org.rococoa.Rococoa;
import org.rococoa.cocoa.foundation.NSError;
import org.rococoa.cocoa.foundation.NSInteger;

import java.text.MessageFormat;
import java.util.concurrent.atomic.AtomicReference;

public abstract class AbstractPromptBookmarkResolver implements FilesystemBookmarkResolver<NSData, NSURL> {
    private static final Logger log = LogManager.getLogger(AbstractPromptBookmarkResolver.class);

    private final int create;
    private final int resolve;

    private final Proxy proxy = new Proxy();

    private static final Local TEMPORARY = new TemporarySupportDirectoryFinder().find();
    private static final Local GROUP_CONTAINER = new SecurityApplicationGroupSupportDirectoryFinder().find();

    /**
     * @param create  Create options
     * @param resolve Resolve options
     */
    public AbstractPromptBookmarkResolver(final int create, final int resolve) {
        this.create = create;
        this.resolve = resolve;
    }

    @Override
    public NSData create(final Local file) throws AccessDeniedException {
        if(skip(file)) {
            return null;
        }
        // Create new security scoped bookmark
        final NSURL url = NSURL.fileURLWithPath(file.getAbsolute());
        log.trace("Resolved file {} to url {}", file, url);
        return this.create(url);
    }

    private NSData create(final NSURL url) throws LocalAccessDeniedException {
        final ObjCObjectByReference error = new ObjCObjectByReference();
        final NSData data = url.bookmarkDataWithOptions_includingResourceValuesForKeys_relativeToURL_error(
                create, null, null, error);
        if(null == data) {
            log.warn("Failure getting bookmark data for file {}", url.path());
            final NSError f = error.getValueAs(NSError.class);
            if(null == f) {
                throw new LocalAccessDeniedException(url.path());
            }
            throw new LocalAccessDeniedException(String.format("%s", f.localizedDescription()));
        }
        log.trace("Created bookmark {} for {}", data.base64Encoding(), url.path());
        return data;
    }

    @Override
    public NSURL resolve(final NSData bookmark) throws AccessDeniedException {
        if(null == bookmark) {
            log.warn("Skip resolving null bookmark");
            return null;
        }
        final ObjCObjectByReference error = new ObjCObjectByReference();
        final NSURL resolved = NSURL.URLByResolvingBookmarkData(bookmark, resolve, error);
        if(null == resolved) {
            final NSError f = error.getValueAs(NSError.class);
            if(null == f) {
                throw new LocalAccessDeniedException();
            }
            log.warn("Error {} resolving bookmark", f);
            throw new LocalAccessDeniedException(String.format("%s", f.localizedDescription()));
        }
        return resolved;
    }

    /**
     * Determine if creating security scoped bookmarks for file should be skipped
     */
    private static boolean skip(final Local file) {
        if(null != TEMPORARY) {
            if(file.isChild(TEMPORARY)) {
                // Skip prompt for file in temporary folder where access is not sandboxed
                return true;
            }
        }
        if(null != GROUP_CONTAINER) {
            if(file.isChild(GROUP_CONTAINER)) {
                // Skip prompt for file in application group folder where access is not sandboxed
                return true;
            }
        }
        return false;
    }

    /**
     * @return Security scoped bookmark
     */
    @Override
    public NSData prompt(final Local file) throws AccessDeniedException {
        if(!file.exists()) {
            log.warn("Skip prompting for non existing file {}", file);
            return null;
        }
        final AtomicReference<NSURL> selected = new AtomicReference<>();
        log.warn("Prompt for file {} to obtain bookmark reference", file);
        final DefaultMainAction action = new DefaultMainAction() {
            @Override
            public void run() {
                final NSOpenPanel panel = NSOpenPanel.openPanel();
                panel.setCanChooseDirectories(file.isDirectory());
                panel.setCanChooseFiles(file.isFile());
                panel.setAllowsMultipleSelection(false);
                panel.setMessage(MessageFormat.format(LocaleFactory.localizedString("Select {0}", "Credentials"),
                        file.getAbbreviatedPath()));
                panel.setPrompt(LocaleFactory.localizedString("Choose"));
                final NSInteger modal = panel.runModal(file.getParent().getAbsolute(), file.getName());
                if(modal.intValue() == SheetCallback.DEFAULT_OPTION) {
                    final NSArray filenames = panel.URLs();
                    final NSEnumerator enumerator = filenames.objectEnumerator();
                    NSObject next;
                    while((next = enumerator.nextObject()) != null) {
                        selected.set(Rococoa.cast(next, NSURL.class));
                    }
                }
                panel.orderOut(null);
            }
        };
        proxy.invoke(action, action.lock(), true);
        if(selected.get() == null) {
            log.warn("Prompt for {} canceled", file);
            return null;
        }
        return this.create(selected.get());
    }
}
