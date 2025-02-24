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
import ch.cyberduck.core.threading.DefaultMainAction;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.rococoa.ObjCObjectByReference;
import org.rococoa.Rococoa;
import org.rococoa.cocoa.foundation.NSError;
import org.rococoa.cocoa.foundation.NSInteger;

import java.text.MessageFormat;
import java.util.concurrent.atomic.AtomicReference;

public class NSURLPromptBookmarkResolver implements FilesystemBookmarkResolver<NSURL> {
    private static final Logger log = LogManager.getLogger(NSURLPromptBookmarkResolver.class);

    /**
     * NSURLBookmarkCreationOptions
     */
    private final int create;
    /**
     * NSURLBookmarkResolutionOptions
     */
    private final int resolve;

    /**
     * @param create  Create options from NSURLBookmarkCreationOptions
     * @param resolve Resolve options from NSURLBookmarkResolutionOptions
     */
    public NSURLPromptBookmarkResolver(final int create, final int resolve) {
        this.create = create;
        this.resolve = resolve;
    }

    @Override
    public String create(final Local file, final boolean prompt) {
        // Create new security scoped bookmark
        final NSURL url = NSURL.fileURLWithPath(file.getAbsolute());
        log.trace("Resolved file {} to url {}", file, url);
        try {
            return this.create(url);
        }
        catch(LocalAccessDeniedException e) {
            log.warn("Failure {} creating bookmark for {}", e, url);
            if(prompt) {
                try {
                    return this.prompt(file);
                }
                catch(LocalAccessDeniedException f) {
                    // Prompt canceled by user
                    log.warn("Failure {} creating bookmark for {}", f, url);
                }
            }
            return null;
        }
    }

    private String create(final NSURL url) throws LocalAccessDeniedException {
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
        return data.base64Encoding();
    }

    @Override
    public NSURL resolve(final String bookmark) throws AccessDeniedException {
        if(null == bookmark) {
            log.warn("Skip resolving null bookmark");
            return null;
        }
        final ObjCObjectByReference error = new ObjCObjectByReference();
        final NSURL resolved = NSURL.URLByResolvingBookmarkData(NSData.dataWithBase64EncodedString(bookmark), resolve, error);
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

    private String prompt(final Local file) throws LocalAccessDeniedException {
        if(!file.exists()) {
            log.warn("Skip prompting for non existing file {}", file);
            return null;
        }
        log.warn("Prompt for file {} to obtain bookmark reference", file);
        final Proxy proxy = new Proxy();
        final AtomicReference<NSURL> selected = new AtomicReference<>();
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
