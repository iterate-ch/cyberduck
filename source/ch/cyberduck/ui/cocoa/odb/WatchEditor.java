package ch.cyberduck.ui.cocoa.odb;

/*
 *  Copyright (c) 2007 David Kocher. All rights reserved.
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

import ch.cyberduck.core.Local;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.TransferAction;
import ch.cyberduck.ui.cocoa.BrowserController;
import ch.cyberduck.ui.cocoa.application.NSWorkspace;
import ch.cyberduck.ui.cocoa.io.FileWatcher;
import ch.cyberduck.ui.cocoa.io.FileWatcherListener;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * An editor listing for file system notifications on a particular folder
 *
 * @version $Id$
 */
public class WatchEditor extends Editor implements FileWatcherListener {
    private static Logger log = Logger.getLogger(WatchEditor.class);

    private static final Map<String, String> SUPPORTED_KQUEUE_EDITORS = new HashMap<String, String>();
    private static final Map<String, String> INSTALLED_KQUEUE_EDITORS = new HashMap<String, String>();

    static {
        SUPPORTED_KQUEUE_EDITORS.put("TextEdit", "com.apple.TextEdit");
        SUPPORTED_KQUEUE_EDITORS.put("Xcode", "com.apple.Xcode");

        Iterator<String> editorNames = SUPPORTED_KQUEUE_EDITORS.keySet().iterator();
        Iterator<String> editorIdentifiers = SUPPORTED_KQUEUE_EDITORS.values().iterator();
        while(editorNames.hasNext()) {
            String editor = editorNames.next();
            String identifier = editorIdentifiers.next();
            final String path = NSWorkspace.sharedWorkspace().absolutePathForAppBundleWithIdentifier(identifier);
            if(StringUtils.isEmpty(path)) {
                continue;
            }
            addInstalledEditor(editor, identifier);
        }
    }

    public static void addInstalledEditor(String name, String identifier) {
        SUPPORTED_KQUEUE_EDITORS.put(name, identifier);
        INSTALLED_KQUEUE_EDITORS.put(name, identifier);
    }

    public static Map<String, String> getSupportedEditors() {
        return SUPPORTED_KQUEUE_EDITORS;
    }

    public static Map<String, String> getInstalledEditors() {
        return INSTALLED_KQUEUE_EDITORS;
    }

    private FileWatcher monitor;

    /**
     * @param c
     */
    public WatchEditor(BrowserController c, Path path) {
        this(c, path.getLocal().getDefaultApplication(), path);
    }

    /**
     * @param c
     * @param bundleIdentifier
     */
    public WatchEditor(BrowserController c, String bundleIdentifier, Path path) {
        super(c, bundleIdentifier, path);
    }

    @Override
    protected TransferAction getAction() {
        return TransferAction.ACTION_RENAME;
    }

    /**
     * Edit and watch the file for changes
     */
    @Override
    public void edit() {
        NSWorkspace.sharedWorkspace().openFile(edited.getLocal().getAbsolute(),
                NSWorkspace.sharedWorkspace().absolutePathForAppBundleWithIdentifier(bundleIdentifier));
        this.watch();
    }

    /**
     * Watch the file for changes
     */
    public void watch() {
        monitor = new FileWatcher(edited.getLocal());
        monitor.register();
        monitor.addListener(this);
    }

    @Override
    protected void delete() {
        monitor.removeListener(this);
        super.delete();
    }

    @Override
    protected void setDeferredDelete(boolean deferredDelete) {
        if(!this.isOpen()) {
            this.delete();
        }
        super.setDeferredDelete(deferredDelete);
    }

    public void fileWritten(Local file) {
        log.info("fileWritten:" + file);
        this.save();
    }

    public void fileRenamed(Local file) {
        log.info("fileRenamed:" + file);
    }

    public void fileDeleted(Local file) {
        log.info("fileDeleted:" + file);
        monitor.removeListener(this);
    }
}
