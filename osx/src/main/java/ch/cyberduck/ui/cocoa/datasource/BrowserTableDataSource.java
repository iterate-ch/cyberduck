package ch.cyberduck.ui.cocoa.datasource;

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

import ch.cyberduck.binding.ProxyController;
import ch.cyberduck.binding.application.NSApplication;
import ch.cyberduck.binding.application.NSDraggingInfo;
import ch.cyberduck.binding.application.NSDraggingSource;
import ch.cyberduck.binding.application.NSEvent;
import ch.cyberduck.binding.application.NSImage;
import ch.cyberduck.binding.application.NSPasteboard;
import ch.cyberduck.binding.application.NSTableView;
import ch.cyberduck.binding.foundation.NSArray;
import ch.cyberduck.binding.foundation.NSAttributedString;
import ch.cyberduck.binding.foundation.NSFileManager;
import ch.cyberduck.binding.foundation.NSMutableArray;
import ch.cyberduck.binding.foundation.NSObject;
import ch.cyberduck.binding.foundation.NSString;
import ch.cyberduck.binding.foundation.NSURL;
import ch.cyberduck.core.Acl;
import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.Cache;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.HostParser;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.LocalFactory;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Permission;
import ch.cyberduck.core.Scheme;
import ch.cyberduck.core.SessionFactory;
import ch.cyberduck.core.UserDateFormatterFactory;
import ch.cyberduck.core.date.AbstractUserDateFormatter;
import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.features.Move;
import ch.cyberduck.core.features.Touch;
import ch.cyberduck.core.formatter.SizeFormatter;
import ch.cyberduck.core.formatter.SizeFormatterFactory;
import ch.cyberduck.core.local.FileDescriptor;
import ch.cyberduck.core.local.FileDescriptorFactory;
import ch.cyberduck.core.local.IconServiceFactory;
import ch.cyberduck.core.local.LocalTouchFactory;
import ch.cyberduck.core.pasteboard.PathPasteboard;
import ch.cyberduck.core.pasteboard.PathPasteboardFactory;
import ch.cyberduck.core.preferences.Preferences;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.resources.IconCache;
import ch.cyberduck.core.resources.IconCacheFactory;
import ch.cyberduck.core.transfer.CopyTransfer;
import ch.cyberduck.core.transfer.DownloadTransfer;
import ch.cyberduck.core.transfer.TransferItem;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.core.transfer.UploadTransfer;
import ch.cyberduck.ui.browser.Column;
import ch.cyberduck.ui.cocoa.controller.BrowserController;
import ch.cyberduck.ui.cocoa.controller.DeleteController;
import ch.cyberduck.ui.cocoa.controller.MoveController;

import org.apache.commons.collections4.map.LRUMap;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.rococoa.Rococoa;
import org.rococoa.cocoa.foundation.NSInteger;
import org.rococoa.cocoa.foundation.NSPoint;
import org.rococoa.cocoa.foundation.NSRect;
import org.rococoa.cocoa.foundation.NSSize;
import org.rococoa.cocoa.foundation.NSUInteger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class BrowserTableDataSource extends ProxyController implements NSDraggingSource {
    private static final Logger log = Logger.getLogger(BrowserTableDataSource.class);

    private final SizeFormatter sizeFormatter = SizeFormatterFactory.get();

    private final AbstractUserDateFormatter dateFormatter = UserDateFormatterFactory.get();

    private final IconCache<NSImage> icons = IconCacheFactory.get();

    private final FileDescriptor descriptor = FileDescriptorFactory.get();

    private final Preferences preferences = PreferencesFactory.get();

    private final Map<Item, NSAttributedString> attributed = new LRUMap<Item, NSAttributedString>(
            preferences.getInteger("browser.model.cache.size")
    );

    protected final BrowserController controller;

    protected final Cache<Path> cache;

    private static final class Item {
        private final Path file;
        private final String column;

        public Item(final Path file, final String column) {
            this.file = file;
            this.column = column;
        }

        @Override
        public boolean equals(final Object o) {
            if(this == o) {
                return true;
            }
            if(o == null || getClass() != o.getClass()) {
                return false;
            }
            final Item item = (Item) o;
            if(column != null ? !column.equals(item.column) : item.column != null) {
                return false;
            }
            if(file != null ? !file.equals(item.file) : item.file != null) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            int result = file != null ? file.hashCode() : 0;
            result = 31 * result + (column != null ? column.hashCode() : 0);
            return result;
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("Item{");
            sb.append("file=").append(file);
            sb.append(", column='").append(column).append('\'');
            sb.append('}');
            return sb.toString();
        }
    }

    protected BrowserTableDataSource(final BrowserController controller, final Cache<Path> cache) {
        this.controller = controller;
        this.cache = cache;
    }

    /**
     * Tell the browser view to reload the data
     *
     * @param folders Changed files
     */
    public void render(final NSTableView view, final List<Path> folders) {
        attributed.clear();
    }

    public AttributedList<Path> get(final Path directory) {
        return cache.get(directory).filter(controller.getComparator(), controller.getFilter());
    }

    public int indexOf(NSTableView view, Path file) {
        return this.get(controller.workdir()).indexOf(file);
    }

    protected void setObjectValueForItem(final Path item, final NSObject value, final String identifier) {
        if(log.isDebugEnabled()) {
            log.debug(String.format("Set new value %s for item %s", value, item));
        }
        if(identifier.equals(Column.filename.name())) {
            if(StringUtils.isNotBlank(value.toString()) && !item.getName().equals(value.toString())) {
                final Path renamed = new Path(
                        item.getParent(), value.toString(), item.getType());
                new MoveController(controller).rename(item, renamed);
            }
        }
    }

    public NSImage iconForPath(final Path item) {
        if(item.isVolume()) {
            return icons.volumeIcon(controller.getSession().getHost().getProtocol(), 16);
        }
        return icons.fileIcon(item, 16);
    }

    protected NSObject objectValueForItem(final Path item, final String identifier) {
        if(null == item) {
            return null;
        }
        if(log.isTraceEnabled()) {
            log.trace("objectValueForItem:" + item.getAbsolute());
        }
        if(identifier.equals(Column.icon.name())) {
            return this.iconForPath(item);
        }
        final Item key = new Item(item, identifier);
        // Query second level cache with view items
        NSAttributedString value = attributed.get(key);
        if(null != value) {
            return value;
        }
        if(log.isTraceEnabled()) {
            log.trace(String.format("Lookup failed for %s in cache", key));
        }
        if(identifier.equals(Column.filename.name())) {
            value = NSAttributedString.attributedStringWithAttributes(
                    item.getName(),
                    TableCellAttributes.browserFontLeftAlignment());
        }
        else if(identifier.equals(Column.size.name())) {
            value = NSAttributedString.attributedStringWithAttributes(
                    sizeFormatter.format(item.attributes().getSize()),
                    TableCellAttributes.browserFontRightAlignment());
        }
        else if(identifier.equals(Column.modified.name())) {
            value = NSAttributedString.attributedStringWithAttributes(
                    dateFormatter.getShortFormat(item.attributes().getModificationDate(),
                            preferences.getBoolean("browser.date.natural")),
                    TableCellAttributes.browserFontLeftAlignment()
            );
        }
        else if(identifier.equals(Column.owner.name())) {
            value = NSAttributedString.attributedStringWithAttributes(
                    StringUtils.isBlank(item.attributes().getOwner()) ?
                            LocaleFactory.localizedString("Unknown") : item.attributes().getOwner(),
                    TableCellAttributes.browserFontLeftAlignment());
        }
        else if(identifier.equals(Column.group.name())) {
            value = NSAttributedString.attributedStringWithAttributes(
                    StringUtils.isBlank(item.attributes().getGroup()) ?
                            LocaleFactory.localizedString("Unknown") : item.attributes().getGroup(),
                    TableCellAttributes.browserFontLeftAlignment());
        }
        else if(identifier.equals(Column.permission.name())) {
            final Acl acl = item.attributes().getAcl();
            if(!Acl.EMPTY.equals(acl)) {
                final StringBuilder s = new StringBuilder();
                for(Map.Entry<Acl.User, Set<Acl.Role>> entry : acl.entrySet()) {
                    s.append(String.format("%s%s:%s", s.length() == 0 ? StringUtils.EMPTY : ", ",
                            entry.getKey().getDisplayName(), entry.getValue()));
                }
                value = NSAttributedString.attributedStringWithAttributes(s.toString(),
                        TableCellAttributes.browserFontLeftAlignment());
            }
            else {
                final Permission permission = item.attributes().getPermission();
                value = NSAttributedString.attributedStringWithAttributes(
                        permission.toString(),
                        TableCellAttributes.browserFontLeftAlignment());
            }
        }
        else if(identifier.equals(Column.kind.name())) {
            value = NSAttributedString.attributedStringWithAttributes(
                    descriptor.getKind(item),
                    TableCellAttributes.browserFontLeftAlignment());
        }
        else if(identifier.equals(Column.extension.name())) {
            value = NSAttributedString.attributedStringWithAttributes(
                    item.isFile() ? StringUtils.isNotBlank(item.getExtension()) ? item.getExtension() :
                            LocaleFactory.localizedString("None") : LocaleFactory.localizedString("None"),
                    TableCellAttributes.browserFontLeftAlignment());
        }
        else if(identifier.equals(Column.region.name())) {
            value = NSAttributedString.attributedStringWithAttributes(
                    StringUtils.isNotBlank(item.attributes().getRegion()) ? item.attributes().getRegion() :
                            LocaleFactory.localizedString("Unknown"),
                    TableCellAttributes.browserFontLeftAlignment());
        }
        else if(identifier.equals(Column.version.name())) {
            value = NSAttributedString.attributedStringWithAttributes(
                    StringUtils.isNotBlank(item.attributes().getVersionId()) ? item.attributes().getVersionId() :
                            LocaleFactory.localizedString("None"),
                    TableCellAttributes.browserFontLeftAlignment());
        }
        else {
            throw new IllegalArgumentException(String.format("Unknown identifier %s", identifier));
        }
        attributed.put(key, value);
        return value;
    }

    /**
     * Sets whether the use of modifier keys should have an effect on the type of operation performed.
     *
     * @return Always false
     * @see NSDraggingSource
     */
    @Override
    public boolean ignoreModifierKeysWhileDragging() {
        // If this method is not implemented or returns false, the user can tailor the drag operation by
        // holding down a modifier key during the drag.
        return false;
    }

    /**
     * @param local indicates that the candidate destination object (the window or view over which the dragged
     *              image is currently poised) is in the same application as the source, while a NO value indicates that
     *              the destination object is in a different application
     * @return A mask, created by combining the dragging operations listed in the NSDragOperation section of
     * NSDraggingInfo protocol reference using the C bitwise OR operator.If the source does not permit
     * any dragging operations, it should return NSDragOperationNone.
     * @see NSDraggingSource
     */
    @Override
    public NSUInteger draggingSourceOperationMaskForLocal(final boolean local) {
        if(log.isDebugEnabled()) {
            log.debug(String.format("Request dragging source operation mask for %s", local));
        }
        if(local) {
            // Move or copy within the browser
            return new NSUInteger(NSDraggingInfo.NSDragOperationMove.intValue() | NSDraggingInfo.NSDragOperationCopy.intValue());
        }
        // Copy to a thirdparty application or drag to trash to delete
        return new NSUInteger(NSDraggingInfo.NSDragOperationCopy.intValue() | NSDraggingInfo.NSDragOperationDelete.intValue());
    }

    /**
     * @param view        Table
     * @param destination A directory or null to mount an URL
     * @param info        Dragging pasteboard
     * @return True if accepted
     */
    public boolean acceptDrop(final NSTableView view, final Path destination, final NSDraggingInfo info) {
        if(log.isDebugEnabled()) {
            log.debug(String.format("Accept drop for destination %s", destination));
        }
        if(info.draggingPasteboard().availableTypeFromArray(NSArray.arrayWithObject(NSPasteboard.URLPboardType)) != null) {
            final NSObject o = info.draggingPasteboard().propertyListForType(NSPasteboard.URLPboardType);
            // Mount .webloc URLs dragged to browser window
            if(o != null) {
                if(o.isKindOfClass(Rococoa.createClass("NSArray", NSArray._Class.class))) {
                    final NSArray elements = Rococoa.cast(o, NSArray.class);
                    for(int i = 0; i < elements.count().intValue(); i++) {
                        if(Scheme.isURL(elements.objectAtIndex(new NSUInteger(i)).toString())) {
                            controller.mount(HostParser.parse(elements.objectAtIndex(new NSUInteger(i)).toString()));
                            return true;
                        }
                    }
                }
            }
        }
        if(controller.isMounted()) {
            if(info.draggingPasteboard().availableTypeFromArray(NSArray.arrayWithObject(NSPasteboard.FilenamesPboardType)) != null) {
                final NSObject o = info.draggingPasteboard().propertyListForType(NSPasteboard.FilenamesPboardType);
                // A file drag has been received by another application; upload to the dragged directory
                if(o != null) {
                    if(o.isKindOfClass(Rococoa.createClass("NSArray", NSArray._Class.class))) {
                        final NSArray elements = Rococoa.cast(o, NSArray.class);
                        final List<TransferItem> roots = new ArrayList<TransferItem>();
                        for(int i = 0; i < elements.count().intValue(); i++) {
                            final Local local = LocalFactory.get(elements.objectAtIndex(new NSUInteger(i)).toString());
                            roots.add(new TransferItem(new Path(destination, local.getName(),
                                    local.isDirectory() ? EnumSet.of(Path.Type.directory) : EnumSet.of(Path.Type.file)), local));
                        }
                        controller.transfer(new UploadTransfer(controller.getSession().getHost(), roots));
                        return true;
                    }
                }
                return false;
            }
            final List<PathPasteboard> pasteboards = PathPasteboardFactory.allPasteboards();
            for(PathPasteboard pasteboard : pasteboards) {
                // A file dragged within the browser has been received
                if(pasteboard.isEmpty()) {
                    continue;
                }
                if(info.draggingSourceOperationMask().intValue() == NSDraggingInfo.NSDragOperationCopy.intValue()
                        || pasteboard.getBookmark().compareTo(controller.getSession().getHost()) != 0) {
                    // Drag to browser windows with different session or explicit copy requested by user.
                    final Map<Path, Path> files = new HashMap<Path, Path>();
                    for(Path file : pasteboard) {
                        files.put(file, new Path(destination, file.getName(), file.getType()));
                    }
                    final Host target = controller.getSession().getHost();
                    controller.transfer(new CopyTransfer(pasteboard.getBookmark(),
                                    SessionFactory.create(target),
                                    files),
                            new ArrayList<Path>(files.values()), false);
                }
                else {
                    // The file should be renamed
                    final Map<Path, Path> files = new HashMap<Path, Path>();
                    for(Path next : pasteboard) {
                        Path renamed = new Path(
                                destination, next.getName(), next.getType());
                        files.put(next, renamed);
                    }
                    new MoveController(controller).rename(files);
                }
                pasteboard.clear();
            }
            return true;
        }
        return false;
    }

    /**
     * @param view        Table
     * @param destination A directory or null to mount an URL
     * @param row         Index
     * @param info        Dragging pasteboard
     * @return Drag operation
     */
    public NSUInteger validateDrop(final NSTableView view, final Path destination, final NSInteger row, final NSDraggingInfo info) {
        if(log.isDebugEnabled()) {
            log.debug(String.format("Validate drop for destination %s", destination));
        }
        if(info.draggingPasteboard().availableTypeFromArray(NSArray.arrayWithObject(NSPasteboard.URLPboardType)) != null) {
            // Dragging URLs to mount new session
            final NSObject o = info.draggingPasteboard().propertyListForType(NSPasteboard.URLPboardType);
            if(o != null) {
                if(o.isKindOfClass(Rococoa.createClass("NSArray", NSArray._Class.class))) {
                    final NSArray elements = Rococoa.cast(o, NSArray.class);
                    for(int i = 0; i < elements.count().intValue(); i++) {
                        // Validate if .webloc URLs dragged to browser window have a known protocol
                        if(Scheme.isURL(elements.objectAtIndex(new NSUInteger(i)).toString())) {
                            // Passing a value of –1 for row, and NSTableViewDropOn as the operation causes the
                            // entire table view to be highlighted rather than a specific row.
                            view.setDropRow(new NSInteger(-1), NSTableView.NSTableViewDropOn);
                            return NSDraggingInfo.NSDragOperationCopy;
                        }
                        else {
                            log.warn(String.format("Protocol not supported for URL %s", elements.objectAtIndex(new NSUInteger(i)).toString()));
                        }
                    }
                }
            }
            else {
                log.warn("URL dragging pasteboard is empty.");
            }
        }
        if(controller.isMounted()) {
            if(null == destination) {
                log.warn("Dragging destination is null.");
                return NSDraggingInfo.NSDragOperationNone;
            }
            final Touch feature = controller.getSession().getFeature(Touch.class);
            if(!feature.isSupported(destination)) {
                // Target file system does not support creating files. Creating files is not supported
                // for example in root of cloud storage accounts.
                return NSDraggingInfo.NSDragOperationNone;
            }
            // Files dragged form other application
            if(info.draggingPasteboard().availableTypeFromArray(NSArray.arrayWithObject(NSPasteboard.FilenamesPboardType)) != null) {
                this.setDropRowAndDropOperation(view, destination, row);
                return NSDraggingInfo.NSDragOperationCopy;
            }
            // Files dragged from browser
            for(Path next : controller.getPasteboard()) {
                if(destination.equals(next)) {
                    // Do not allow dragging onto myself
                    return NSDraggingInfo.NSDragOperationNone;
                }
                if(next.isDirectory() && destination.isChild(next)) {
                    // Do not allow dragging a directory into its own containing items
                    return NSDraggingInfo.NSDragOperationNone;
                }
                if(next.isFile() && next.getParent().equals(destination)) {
                    // Moving a file to the same destination makes no sense
                    return NSDraggingInfo.NSDragOperationNone;
                }
            }
            if(log.isDebugEnabled()) {
                log.debug(String.format("Drag operation mask is %d", info.draggingSourceOperationMask().intValue()));
            }
            this.setDropRowAndDropOperation(view, destination, row);
            final List<PathPasteboard> pasteboards = PathPasteboardFactory.allPasteboards();
            for(PathPasteboard pasteboard : pasteboards) {
                if(pasteboard.isEmpty()) {
                    continue;
                }
                if(pasteboard.getBookmark().compareTo(controller.getSession().getHost()) == 0) {
                    if(info.draggingSourceOperationMask().intValue() == NSDraggingInfo.NSDragOperationCopy.intValue()) {
                        // Explicit copy requested if drag operation is already NSDragOperationCopy. User is pressing the option key.
                        return NSDraggingInfo.NSDragOperationCopy;
                    }
                    for(Path file : pasteboard) {
                        if(!controller.getSession().getFeature(Move.class).isSupported(file)) {
                            return NSDraggingInfo.NSDragOperationNone;
                        }
                    }
                    // Defaulting to move for same session
                    return NSDraggingInfo.NSDragOperationMove;
                }
                else {
                    // If copying between sessions is supported
                    return NSDraggingInfo.NSDragOperationCopy;
                }
            }
        }
        return NSDraggingInfo.NSDragOperationNone;
    }

    private void setDropRowAndDropOperation(final NSTableView view, final Path destination, final NSInteger row) {
        if(destination.equals(controller.workdir())) {
            log.debug("setDropRowAndDropOperation:-1");
            // Passing a value of –1 for row, and NSTableViewDropOn as the operation causes the
            // entire table view to be highlighted rather than a specific row.
            view.setDropRow(new NSInteger(-1), NSTableView.NSTableViewDropOn);
        }
        else if(destination.isDirectory()) {
            log.debug("setDropRowAndDropOperation:" + row.intValue());
            view.setDropRow(row, NSTableView.NSTableViewDropOn);
        }
    }

    public boolean writeItemsToPasteBoard(final NSTableView view, final List<Path> selected, final NSPasteboard pboard) {
        if(log.isDebugEnabled()) {
            log.debug(String.format("Write items to pasteboard %s", pboard));
        }
        if(controller.isMounted()) {
            if(selected.size() > 0) {
                // The fileTypes argument is the list of fileTypes being promised.
                // The array elements can consist of file extensions and HFS types encoded
                // with the NSHFSFileTypes method fileTypeForHFSTypeCode. If promising a directory
                // of files, only include the top directory in the array.
                final NSMutableArray fileTypes = NSMutableArray.array();
                final PathPasteboard pasteboard = controller.getPasteboard();
                for(final Path f : selected) {
                    if(f.isFile()) {
                        if(StringUtils.isNotEmpty(f.getExtension())) {
                            fileTypes.addObject(NSString.stringWithString(f.getExtension()));
                        }
                        else {
                            fileTypes.addObject(NSString.stringWithString(NSFileManager.NSFileTypeRegular));
                        }
                    }
                    else if(f.isDirectory()) {
                        fileTypes.addObject(NSString.stringWithString("'fldr'")); //NSFileTypeForHFSTypeCode('fldr')
                    }
                    else {
                        fileTypes.addObject(NSString.stringWithString(NSFileManager.NSFileTypeUnknown));
                    }
                    // Writing data for private use when the item gets dragged to the transfer queue.
                    pasteboard.add(f);
                }
                NSEvent event = NSApplication.sharedApplication().currentEvent();
                if(event != null) {
                    NSPoint dragPosition = view.convertPoint_fromView(event.locationInWindow(), null);
                    NSRect imageRect = new NSRect(new NSPoint(dragPosition.x.doubleValue() - 16, dragPosition.y.doubleValue() - 16), new NSSize(32, 32));
                    view.dragPromisedFilesOfTypes(fileTypes, imageRect, this.id(), true, event);
                    // @see http://www.cocoabuilder.com/archive/message/cocoa/2003/5/15/81424
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void draggedImage_beganAt(final NSImage image, final NSPoint point) {
        if(log.isTraceEnabled()) {
            log.trace("draggedImage_beganAt:" + point);
        }
    }

    /**
     * See http://www.cocoabuilder.com/archive/message/2005/10/5/118857
     */
    @Override
    public void draggedImage_endedAt_operation(final NSImage image, final NSPoint point, final NSUInteger operation) {
        if(log.isTraceEnabled()) {
            log.trace("draggedImage_endedAt_operation:" + operation);
        }
        final PathPasteboard pasteboard = controller.getPasteboard();
        if(NSDraggingInfo.NSDragOperationDelete.intValue() == operation.intValue()) {
            new DeleteController(controller).delete(pasteboard);
        }
        pasteboard.clear();
    }

    @Override
    public void draggedImage_movedTo(final NSImage image, final NSPoint point) {
        if(log.isTraceEnabled()) {
            log.trace("draggedImage_movedTo:" + point);
        }
    }

    /**
     * @return the names (not full paths) of the files that the receiver promises to create at dropDestination.
     * This method is invoked when the drop has been accepted by the destination and the destination, in the case of another
     * Cocoa application, invokes the NSDraggingInfo method namesOfPromisedFilesDroppedAtDestination. For long operations,
     * you can cache dropDestination and defer the creation of the files until the finishedDraggingImage method to avoid
     * blocking the destination application.
     */
    @Override
    public NSArray namesOfPromisedFilesDroppedAtDestination(final NSURL url) {
        if(log.isDebugEnabled()) {
            log.debug(String.format("Return names of promised files dropped at %s", url));
        }
        NSMutableArray promisedDragNames = NSMutableArray.array();
        if(null != url) {
            final Local destination = LocalFactory.get(url.path());
            final PathPasteboard pasteboard = controller.getPasteboard();
            final List<TransferItem> downloads = new ArrayList<TransferItem>();
            for(Path p : pasteboard) {
                downloads.add(new TransferItem(p, LocalFactory.get(destination, p.getName())));
                // Add to returned path names
                promisedDragNames.addObject(NSString.stringWithString(p.getName()));
            }
            if(downloads.size() == 1) {
                if(downloads.iterator().next().remote.isFile()) {
                    final Local file = downloads.iterator().next().local;
                    if(!file.exists()) {
                        try {
                            LocalTouchFactory.get().touch(file);
                            IconServiceFactory.get().set(file, new TransferStatus());
                        }
                        catch(AccessDeniedException e) {
                            log.warn(String.format("Failure creating file %s %s", file, e.getMessage()));
                        }
                    }
                }
                if(downloads.iterator().next().remote.isDirectory()) {
                    final Local file = downloads.iterator().next().local;
                    if(!file.exists()) {
                        try {
                            file.mkdir();
                        }
                        catch(AccessDeniedException e) {
                            log.warn(e.getMessage());
                        }
                    }
                }
            }
            // kTemporaryFolderType
            final boolean dock = destination.equals(LocalFactory.get("~/Library/Caches/TemporaryItems"));
            if(dock) {
                for(Path p : pasteboard) {
                    // Drag to application icon in dock.
                    controller.edit(p);
                }
            }
            else {
                final DownloadTransfer transfer = new DownloadTransfer(controller.getSession().getHost(), downloads);
                controller.transfer(transfer, Collections.emptyList());
            }
            pasteboard.clear();
        }
        // Filenames
        return promisedDragNames;
    }
}
