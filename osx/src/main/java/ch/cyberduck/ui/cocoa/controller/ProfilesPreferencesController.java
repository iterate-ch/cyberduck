package ch.cyberduck.ui.cocoa.controller;/*
 * Copyright (c) 2002-2021 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */

/*
 * Copyright (c) 2002-2021 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */

import ch.cyberduck.binding.AbstractTableDelegate;
import ch.cyberduck.binding.Action;
import ch.cyberduck.binding.BundleController;
import ch.cyberduck.binding.Delegate;
import ch.cyberduck.binding.Outlet;
import ch.cyberduck.binding.OutlineDataSource;
import ch.cyberduck.binding.application.*;
import ch.cyberduck.binding.foundation.NSArray;
import ch.cyberduck.binding.foundation.NSAttributedString;
import ch.cyberduck.binding.foundation.NSDictionary;
import ch.cyberduck.binding.foundation.NSMutableAttributedString;
import ch.cyberduck.binding.foundation.NSNotification;
import ch.cyberduck.binding.foundation.NSNotificationCenter;
import ch.cyberduck.binding.foundation.NSObject;
import ch.cyberduck.core.DeserializerFactory;
import ch.cyberduck.core.HostParser;
import ch.cyberduck.core.Profile;
import ch.cyberduck.core.Protocol;
import ch.cyberduck.core.ProtocolFactory;
import ch.cyberduck.core.SearchProtocolPredicate;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.SessionPoolFactory;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.HostParserException;
import ch.cyberduck.core.preferences.Preferences;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.profiles.ProfileMatcher;
import ch.cyberduck.core.profiles.ProfilesFinder;
import ch.cyberduck.core.profiles.RemoteProfilesFinder;
import ch.cyberduck.core.resources.IconCacheFactory;
import ch.cyberduck.core.serializer.ProfileDictionary;
import ch.cyberduck.core.serializer.impl.jna.PlistDeserializer;
import ch.cyberduck.core.serializer.impl.jna.PlistSerializer;
import ch.cyberduck.core.threading.WorkerBackgroundAction;
import ch.cyberduck.core.worker.Worker;

import org.apache.log4j.Logger;
import org.rococoa.Foundation;
import org.rococoa.ID;
import org.rococoa.Rococoa;
import org.rococoa.cocoa.CGFloat;
import org.rococoa.cocoa.foundation.NSInteger;
import org.rococoa.cocoa.foundation.NSPoint;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ProfilesPreferencesController extends BundleController {
    private static final Logger log = Logger.getLogger(ProfilesPreferencesController.class);

    private final NSNotificationCenter notificationCenter = NSNotificationCenter.defaultCenter();
    private final Preferences preferences = PreferencesFactory.get();
    private final List<Protocol> profiles = new ArrayList<>();

    @Delegate
    private ProfilesTableDataSource profilesTableDataSource;
    @Delegate
    private ProfilesTableDelegate profilesTableDelegate;

    @Override
    protected String getBundleName() {
        return "Profiles";
    }

    @Outlet
    private NSView panelProfiles;
    @Outlet
    private NSSearchField searchField;
    @Outlet
    private NSOutlineView profilesTableView;
    @Outlet
    private NSProgressIndicator progressIndicator;

    public void setPanelProfiles(final NSView panelProfiles) {
        this.panelProfiles = panelProfiles;
    }

    public void setSearchField(final NSSearchField searchField) {
        this.searchField = searchField;
        if(this.searchField.respondsToSelector(Foundation.selector("setSendsWholeSearchString:"))) {
            // calls its search action method when the user clicks the search button (or presses Return)
            this.searchField.setSendsWholeSearchString(false);
        }
        if(this.searchField.respondsToSelector(Foundation.selector("setSendsSearchStringImmediately:"))) {
            this.searchField.setSendsSearchStringImmediately(false);
        }
        // Make sure action is not sent twice.
        this.searchField.cell().setSendsActionOnEndEditing(false);
        this.searchField.setTarget(this.id());
        this.searchField.setAction(Foundation.selector("searchFieldTextDidChange:"));
        this.notificationCenter.addObserver(this.id(),
            Foundation.selector("searchFieldTextDidEndEditing:"),
            NSControl.NSControlTextDidEndEditingNotification,
            this.searchField.id());
    }

    @Action
    public void searchFieldTextDidChange(final NSNotification notification) {
        this.searchFieldTextDidEndEditing(notification);
    }

    @Action
    public void searchFieldTextDidEndEditing(final NSNotification notification) {
        final String input = searchField.stringValue();
        // Setup search filter
        final List<Protocol> filtered = profiles.stream().filter(new SearchProtocolPredicate(input)).collect(Collectors.toList());
        filtered.sort(Comparator.comparing(Protocol::getType));
        this.profilesTableDataSource.setSource(filtered);
        // Reload with current cache
        this.profilesTableView.reloadData();
    }

    public void setProfilesTableView(final NSOutlineView profilesTableView) {
        this.profilesTableView = profilesTableView;
        this.profilesTableDataSource = new ProfilesTableDataSource(profiles);
        this.profilesTableView.setDataSource(profilesTableDataSource.id());
        this.profilesTableDelegate = new ProfilesTableDelegate(profilesTableView.tableColumnWithIdentifier("Default"));
        this.profilesTableView.setDelegate(profilesTableDelegate.id());
    }

    public void setProgressIndicator(final NSProgressIndicator progressIndicator) {
        this.progressIndicator = progressIndicator;
        this.progressIndicator.setDisplayedWhenStopped(false);
    }

    public NSView getPanel() {
        return panelProfiles;
    }

    @Override
    public void awakeFromNib() {
        progressIndicator.startAnimation(null);
        try {
            this.background(new WorkerBackgroundAction<>(this, SessionPoolFactory.create(this,
                HostParser.parse(PreferencesFactory.get().getProperty("profiles.discovery.updater.url"))), new Worker<Void>() {
                @Override
                public Void run(final Session<?> session) throws BackgroundException {
                    final Stream<ProfilesFinder.ProfileDescription> stream = new RemoteProfilesFinder(session).find();
                    stream.forEach(description -> profiles.add(description.getProfile().get()));
                    profiles.sort(Comparator.comparing(Protocol::getType));
                    return null;
                }

                @Override
                public void cleanup(final Void result) {
                    progressIndicator.stopAnimation(null);
                    profilesTableView.reloadData();
                }
            }));
        }
        catch(HostParserException e) {
            log.warn(String.format("Failure %s parsing profiles service disovery URL", e));
        }
        super.awakeFromNib();
    }

    public static final class ProfilesTableDelegate extends AbstractTableDelegate<Protocol, Void> implements NSOutlineView.Delegate {
        private final Map<Protocol, ProfileTableViewController> controllers = new HashMap<>();

        private static Profile toProfile(final NSDictionary dict) {
            return new ProfileDictionary(new DeserializerFactory<NSDictionary, PlistDeserializer>(PlistDeserializer.class)).deserialize(dict);
        }

        public ProfilesTableDelegate(final NSTableColumn selectedColumn) {
            super(selectedColumn);
        }

        @Override
        public void tableColumnClicked(final NSTableView view, final NSTableColumn tableColumn) {

        }

        @Override
        public void tableRowDoubleClicked(final ID sender) {

        }

        @Override
        public void selectionDidChange(final NSNotification notification) {

        }

        @Override
        protected boolean isTypeSelectSupported() {
            return true;
        }

        @Override
        public String outlineView_typeSelectStringForTableColumn_item(final NSOutlineView view, final NSTableColumn tableColumn, final NSObject item) {
            if(item.isKindOfClass(NSDictionary.CLASS)) {
                final NSDictionary dict = Rococoa.cast(item, NSDictionary.class);
                final Profile profile = toProfile(dict);
                return profile.getName();
            }
            return null;
        }

        @Override
        public void enterKeyPressed(final ID sender) {

        }

        @Override
        public void deleteKeyPressed(final ID sender) {

        }

        @Override
        public String outlineView_toolTipForCell_rect_tableColumn_item_mouseLocation(NSOutlineView t, NSCell cell, ID rect, NSTableColumn c, NSObject item, NSPoint mouseLocation) {
            if(item.isKindOfClass(NSDictionary.CLASS)) {
                final NSDictionary dict = Rococoa.cast(item, NSDictionary.class);
                final Profile profile = toProfile(dict);
                return profile.getDefaultHostname();
            }
            return null;
        }

        @Override
        public String tooltip(final Protocol item, final Void unused) {
            return item.getDescription();
        }

        @Override
        public void outlineView_willDisplayCell_forTableColumn_item(final NSOutlineView view, final NSTextFieldCell cell, final NSTableColumn tableColumn, final NSObject item) {
            //
        }

        @Override
        public boolean outlineView_shouldExpandItem(final NSOutlineView view, final NSObject item) {
            return false;
        }

        @Override
        public void outlineViewItemWillExpand(final NSNotification notification) {

        }

        @Override
        public void outlineViewItemDidExpand(final NSNotification notification) {

        }

        @Override
        public void outlineViewItemWillCollapse(final NSNotification notification) {

        }

        @Override
        public void outlineViewItemDidCollapse(final NSNotification notification) {

        }

        @Override
        public boolean outlineView_isGroupItem(final NSOutlineView view, final NSObject item) {
            return false;
        }

        @Override
        public boolean outlineView_shouldSelectItem(final NSOutlineView view, final NSObject item) {
            if(item.isKindOfClass(NSDictionary.CLASS)) {
                final NSDictionary dict = Rococoa.cast(item, NSDictionary.class);
                final Profile profile = toProfile(dict);
                return !profile.isBundled();
            }
            return false;
        }

        public NSView outlineView_viewForTableColumn_item(final NSOutlineView outlineView, final NSTableColumn tableColumn, final NSObject item) {
            log.debug(String.format("outlineView_viewForTableColumn_item:%s", item));
            // We only have a single column
            if(item.isKindOfClass(NSDictionary.CLASS)) {
                final NSDictionary dict = Rococoa.cast(item, NSDictionary.class);
                final Profile profile = toProfile(dict);
                if(controllers.containsKey(profile)) {
                    return controllers.get(profile).getCellView();
                }
                final ProfileTableViewController controller = new ProfileTableViewController(profile);
                controllers.put(profile, controller);
                return controller.getCellView();
            }
            return null;
        }
    }

    public static final class ProfilesTableDataSource extends OutlineDataSource {

        private List<Protocol> profiles;

        public ProfilesTableDataSource(final List<Protocol> profiles) {
            this.profiles = profiles;
        }

        public void setSource(final List<Protocol> source) {
            this.profiles = source;
        }

        @Override
        public NSInteger outlineView_numberOfChildrenOfItem(final NSOutlineView view, final NSObject item) {
            log.debug(String.format("outlineView_numberOfChildrenOfItem:%s", item));
            if(null == item) {
                return new NSInteger(profiles.size());
            }
            return new NSInteger(0);
        }

        @Override
        public NSObject outlineView_child_ofItem(final NSOutlineView outlineView, final NSInteger index, final NSObject item) {
            log.debug(String.format("outlineView_child_ofItem:%s", index));
            if(null == item) {
                // If item is nil, returns the appropriate child item of the root object.
                final Protocol profile = profiles.get(index.intValue());
                return profile.serialize(new PlistSerializer());
            }
            return null;
        }

        @Override
        public NSObject outlineView_objectValueForTableColumn_byItem(final NSOutlineView outlineView, final NSTableColumn tableColumn, final NSObject item) {
            log.debug(String.format("outlineView_objectValueForTableColumn_byItem:%s", item));
            if(null == item) {
                return null;
            }
            return item;
        }

        @Override
        public boolean outlineView_isItemExpandable(final NSOutlineView view, final NSObject item) {
            if(null == item) {
                return true;
            }
            return false;
        }

        public CGFloat outlineView_heightOfRowByItem(NSOutlineView outlineView, NSObject item) {
            if(null == item) {
                return outlineView.rowHeight();
            }
            return new CGFloat(45d);
        }
    }

    public static final class ProfileTableViewController extends BundleController {
        private static final NSDictionary PRIMARY_FONT_ATTRIBUTES = NSDictionary.dictionaryWithObjectsForKeys(
            NSArray.arrayWithObjects(
                NSFont.boldSystemFontOfSize(NSFont.systemFontSize()),
                NSColor.controlTextColor(),
                BundleController.PARAGRAPH_STYLE_LEFT_ALIGNMENT_TRUNCATE_TAIL),
            NSArray.arrayWithObjects(
                NSAttributedString.FontAttributeName,
                NSAttributedString.ForegroundColorAttributeName,
                NSAttributedString.ParagraphStyleAttributeName)
        );

        private static final NSDictionary SECONDARY_FONT_ATTRIBUTES = NSDictionary.dictionaryWithObjectsForKeys(
            NSArray.arrayWithObjects(
                NSFont.systemFontOfSize(NSFont.systemFontSize()),
                NSColor.secondaryLabelColor(),
                BundleController.PARAGRAPH_STYLE_LEFT_ALIGNMENT_TRUNCATE_TAIL),
            NSArray.arrayWithObjects(
                NSAttributedString.FontAttributeName,
                NSAttributedString.ForegroundColorAttributeName,
                NSAttributedString.ParagraphStyleAttributeName)
        );

        private static final ProtocolFactory registry = ProtocolFactory.get();

        private final Profile profile;

        @Outlet
        private NSTableCellView cellView;
        @Outlet
        private NSImageView imageView;
        @Outlet
        private NSTextField textField;
        @Outlet
        private NSButton checkbox;

        public ProfileTableViewController(final Profile profile) {
            this.profile = profile;
            this.loadBundle();
        }

        public void setCellView(final NSTableCellView cellView) {
            this.cellView = cellView;
        }

        public NSTableCellView getCellView() {
            return cellView;
        }

        public void setImageView(final NSImageView imageView) {
            this.imageView = imageView;
            this.imageView.setImage(IconCacheFactory.<NSImage>get().iconNamed(profile.icon(), 32));
        }

        public void setTextField(final NSTextField textField) {
            this.textField = textField;
            final NSMutableAttributedString description = NSMutableAttributedString.create(profile.getName(), PRIMARY_FONT_ATTRIBUTES);
            description.appendAttributedString(NSMutableAttributedString.create(String.format("\n%s", profile.getDescription()), SECONDARY_FONT_ATTRIBUTES));
            this.textField.setAttributedStringValue(description);
        }

        public void setCheckbox(final NSButton checkbox) {
            this.checkbox = checkbox;
            this.checkbox.setState(NSCell.NSOnState);
            this.checkbox.setTarget(this.id());
            this.checkbox.setAction(Foundation.selector("profileCheckboxClicked:"));
            final Optional<Protocol> installed = registry.find().stream().filter(new ProfileMatcher.IdentifierProtocolPredicate(profile)).findFirst();
            this.checkbox.setState(installed.isPresent() ? NSCell.NSOnState : NSCell.NSOffState);
            if(installed.isPresent()) {
                this.checkbox.setEnabled(!installed.get().isBundled());
            }
            else {
                this.checkbox.setEnabled(true);
            }
        }

        @Action
        public void profileCheckboxClicked(final NSButton sender) {
            boolean enabled = sender.state() == NSCell.NSOnState;
            if(enabled) {
                // Install profile
                registry.register(profile);
            }
            else {
                // Uninstall profile
                registry.unregister(profile);
            }
        }

        @Override
        protected String getBundleName() {
            return "Profile";
        }
    }
}
