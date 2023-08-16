package ch.cyberduck.ui.cocoa.controller;

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
import ch.cyberduck.binding.WindowController;
import ch.cyberduck.binding.application.*;
import ch.cyberduck.binding.foundation.NSArray;
import ch.cyberduck.binding.foundation.NSAttributedString;
import ch.cyberduck.binding.foundation.NSDictionary;
import ch.cyberduck.binding.foundation.NSMutableAttributedString;
import ch.cyberduck.binding.foundation.NSNotification;
import ch.cyberduck.binding.foundation.NSNotificationCenter;
import ch.cyberduck.binding.foundation.NSObject;
import ch.cyberduck.binding.foundation.NSString;
import ch.cyberduck.core.BookmarkCollection;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.Profile;
import ch.cyberduck.core.Protocol;
import ch.cyberduck.core.ProtocolFactory;
import ch.cyberduck.core.ProviderHelpServiceFactory;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.local.BrowserLauncherFactory;
import ch.cyberduck.core.profiles.LocalProfileDescription;
import ch.cyberduck.core.profiles.ProfileDescription;
import ch.cyberduck.core.profiles.ProfilesFinder;
import ch.cyberduck.core.profiles.ProfilesSynchronizeWorker;
import ch.cyberduck.core.profiles.ProfilesWorkerBackgroundAction;
import ch.cyberduck.core.profiles.SearchProfilePredicate;
import ch.cyberduck.core.resources.IconCacheFactory;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.rococoa.Foundation;
import org.rococoa.ID;
import org.rococoa.cocoa.CGFloat;
import org.rococoa.cocoa.foundation.NSInteger;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class ProfilesPreferencesController extends BundleController {
    private static final Logger log = LogManager.getLogger(ProfilesPreferencesController.class);

    private final ProtocolFactory protocols = ProtocolFactory.get();

    private final NSNotificationCenter notificationCenter = NSNotificationCenter.defaultCenter();

    /**
     * Synchronized ist of available profiles
     */
    private final Map<ProfileDescription, Profile> repository
            = Collections.synchronizedMap(new LinkedHashMap<>());

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
        this.reload();
    }

    private void reload() {
        final String input = searchField.stringValue();
        if(StringUtils.isBlank(input)) {
            this.profilesTableDataSource.withSource(toSorted(repository.keySet()));
        }
        else {
            // Setup search filter
            this.profilesTableDataSource.withSource(toSorted(
                    repository.keySet().stream().filter(new SearchProfilePredicate(input)).collect(Collectors.toSet())));
        }
        // Reload with current cache
        this.profilesTableView.reloadData();
    }

    public void setProfilesTableView(final NSOutlineView profilesTableView) {
        this.profilesTableView = profilesTableView;
        this.profilesTableDataSource = new ProfilesTableDataSource().withSource(toSorted(repository.keySet()));
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

    protected void load(final WindowController controller) {
        if(repository.isEmpty()) {
            try {
                progressIndicator.startAnimation(null);
                this.background(new ProfilesWorkerBackgroundAction(controller,
                        new ProfilesSynchronizeWorker(protocols, ProfilesFinder.Visitor.Prefetch) {
                            @Override
                            public void cleanup(final Set<ProfileDescription> set) {
                                for(ProfileDescription description : set) {
                                    if(description.getProfile().isPresent()) {
                                        repository.put(description, description.getProfile().get());
                                    }
                                }
                                reload();
                                progressIndicator.stopAnimation(null);
                            }
                        })
                );
            }
            catch(BackgroundException e) {
                log.error(String.format("Failure %s retrieving profiles", e));
            }
        }
    }

    public final class ProfilesTableDelegate extends AbstractTableDelegate<Protocol, Void> implements NSOutlineView.Delegate {
        private final Map<ProfileDescription, ProfileTableViewController> controllers = new HashMap<>();

        public ProfilesTableDelegate(final NSTableColumn selectedColumn) {
            super(selectedColumn);
        }

        private ProfileDescription fromChecksum(final NSObject hash) {
            final Optional<ProfileDescription> found = repository.keySet().stream()
                    .filter(description -> description.getChecksum().hash.equals(hash.toString())).findFirst();
            return found.orElse(null);
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
            final ProfileDescription description = this.fromChecksum(item);
            if(null == description) {
                return null;
            }
            return repository.get(description).getDescription();
        }

        @Override
        public void enterKeyPressed(final ID sender) {

        }

        @Override
        public void deleteKeyPressed(final ID sender) {

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
            final ProfileDescription description = this.fromChecksum(item);
            if(null == description) {
                return false;
            }
            return !repository.get(description).isBundled();
        }

        public NSView outlineView_viewForTableColumn_item(final NSOutlineView outlineView, final NSTableColumn tableColumn, final NSObject item) {
            final ProfileDescription description = this.fromChecksum(item);
            if(null == description) {
                return null;
            }
            final Profile profile = repository.get(description);
            if(controllers.containsKey(description)) {
                return controllers.get(description).getCellView();
            }
            final ProfileTableViewController controller = new ProfileTableViewController(description, profile);
            controllers.put(description, controller);
            return controller.getCellView();
        }

        @Override
        public CGFloat outlineView_heightOfRowByItem(NSOutlineView outlineView, NSObject item) {
            if(null == item) {
                return outlineView.rowHeight();
            }
            return new CGFloat(45d);
        }
    }

    /**
     * Data source returning checksums of profiles as items
     */
    public static final class ProfilesTableDataSource extends OutlineDataSource {

        private List<ProfileDescription> profiles;

        public ProfilesTableDataSource withSource(final List<ProfileDescription> source) {
            this.profiles = source;
            return this;
        }

        @Override
        public NSInteger outlineView_numberOfChildrenOfItem(final NSOutlineView view, final NSObject item) {
            if(null == item) {
                return new NSInteger(profiles.size());
            }
            return new NSInteger(0);
        }

        @Override
        public NSObject outlineView_child_ofItem(final NSOutlineView outlineView, final NSInteger index, final NSObject item) {
            if(null == item) {
                // If item is nil, returns the appropriate child item of the root object.
                return NSString.stringWithString(profiles.get(index.intValue()).getChecksum().hash);
            }
            return null;
        }

        @Override
        public NSObject outlineView_objectValueForTableColumn_byItem(final NSOutlineView outlineView, final NSTableColumn tableColumn, final NSObject item) {
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
    }

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

    public final class ProfileTableViewController extends BundleController {
        private final ProfileDescription description;
        private final Profile profile;

        @Outlet
        private NSTableCellView cellView;
        @Outlet
        private NSImageView imageView;
        @Outlet
        private NSTextField textField;
        @Outlet
        private NSButton checkbox;
        @Outlet
        private NSButton helpButton;

        public ProfileTableViewController(final ProfileDescription description, final Profile profile) {
            this.description = description;
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
            final NSMutableAttributedString description = NSMutableAttributedString.create(profile.getDescription(), PRIMARY_FONT_ATTRIBUTES);
            description.appendAttributedString(NSMutableAttributedString.create(String.format("\n%s", profile.getName()), SECONDARY_FONT_ATTRIBUTES));
            this.textField.setAttributedStringValue(description);
        }

        public void setCheckbox(final NSButton checkbox) {
            this.checkbox = checkbox;
            this.checkbox.setState(NSCell.NSOnState);
            this.checkbox.setTarget(this.id());
            this.checkbox.setAction(Foundation.selector("profileCheckboxClicked:"));
            this.checkbox.setEnabled(!profile.isBundled());
            if(BookmarkCollection.defaultCollection().stream().filter(host -> host.getProtocol().equals(profile)).findAny().isPresent()) {
                this.checkbox.setEnabled(false);
            }
            this.checkbox.setState(description.isInstalled() && profile.isEnabled() ? NSCell.NSOnState : NSCell.NSOffState);
        }

        public void setHelpButton(final NSButton helpButton) {
            this.helpButton = helpButton;
            this.helpButton.setTarget(this.id());
            this.helpButton.setAction(Foundation.selector("helpButtonClicked:"));
        }

        @Action
        public void profileCheckboxClicked(final NSButton sender) {
            boolean enabled = sender.state() == NSCell.NSOnState;
            if(enabled) {
                final Optional<Local> file = description.getFile();
                // Update with last version from repository
                file.ifPresent(local -> repository.put(new LocalProfileDescription(protocols, ProtocolFactory.BUNDLED_PROFILE_PREDICATE,
                        protocols.register(local)), profile));
            }
            else {
                final Optional<Profile> profile = description.getProfile();
                // Uninstall profile
                profile.ifPresent(protocols::unregister);
            }
        }

        @Action
        public void helpButtonClicked(final NSButton sender) {
            BrowserLauncherFactory.get().open(ProviderHelpServiceFactory.get().help(profile));
        }

        @Override
        protected String getBundleName() {
            return "Profile";
        }
    }

    /**
     * Sort and map profiles
     *
     * @param profiles Profiles
     * @return List of profile descriptions sorted by value
     */
    private static List<ProfileDescription> toSorted(final Set<ProfileDescription> profiles) {
        return profiles.stream()
                .filter(description -> description.getProfile().isPresent())
                .sorted(Comparator.comparing(o -> o.getProfile().get()))
                .collect(Collectors.toList());
    }
}
