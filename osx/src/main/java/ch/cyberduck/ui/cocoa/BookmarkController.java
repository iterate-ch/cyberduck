package ch.cyberduck.ui.cocoa;

/*
 *  Copyright (c) 2005 David Kocher. All rights reserved.
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

import ch.cyberduck.binding.Action;
import ch.cyberduck.binding.HyperlinkAttributedStringFactory;
import ch.cyberduck.binding.Outlet;
import ch.cyberduck.binding.WindowController;
import ch.cyberduck.binding.application.NSButton;
import ch.cyberduck.binding.application.NSCell;
import ch.cyberduck.binding.application.NSControl;
import ch.cyberduck.binding.application.NSImage;
import ch.cyberduck.binding.application.NSMenuItem;
import ch.cyberduck.binding.application.NSOpenPanel;
import ch.cyberduck.binding.application.NSPopUpButton;
import ch.cyberduck.binding.application.NSTextField;
import ch.cyberduck.binding.application.NSTextFieldCell;
import ch.cyberduck.binding.application.NSWindow;
import ch.cyberduck.binding.application.SheetCallback;
import ch.cyberduck.binding.foundation.NSAttributedString;
import ch.cyberduck.binding.foundation.NSData;
import ch.cyberduck.binding.foundation.NSNotification;
import ch.cyberduck.binding.foundation.NSNotificationCenter;
import ch.cyberduck.binding.foundation.NSObject;
import ch.cyberduck.binding.foundation.NSURL;
import ch.cyberduck.core.AbstractCollectionListener;
import ch.cyberduck.core.BookmarkCollection;
import ch.cyberduck.core.BookmarkNameProvider;
import ch.cyberduck.core.DefaultCharsetProvider;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.HostParser;
import ch.cyberduck.core.HostUrlProvider;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.LocalFactory;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.Protocol;
import ch.cyberduck.core.ProtocolFactory;
import ch.cyberduck.core.Scheme;
import ch.cyberduck.core.diagnostics.ReachabilityFactory;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.local.BrowserLauncherFactory;
import ch.cyberduck.core.preferences.Preferences;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.resources.IconCacheFactory;
import ch.cyberduck.core.sftp.openssh.OpenSSHPrivateKeyConfigurator;
import ch.cyberduck.core.ssl.KeychainX509KeyManager;
import ch.cyberduck.core.threading.AbstractBackgroundAction;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.rococoa.Foundation;
import org.rococoa.ID;
import org.rococoa.Selector;
import org.rococoa.cocoa.foundation.NSPoint;
import org.rococoa.cocoa.foundation.NSSize;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.TimeZone;

public class BookmarkController extends WindowController {
    private static final Logger log = Logger.getLogger(BookmarkController.class);

    private static final TimeZone UTC = TimeZone.getTimeZone("UTC");

    private static final String TIMEZONE_CONTINENT_PREFIXES =
            "^(Africa|America|Asia|Atlantic|Australia|Europe|Indian|Pacific)/.*";

    protected static final String CHOOSE = LocaleFactory.localizedString("Choose") + "…";

    private static NSPoint cascade = new NSPoint(0, 0);

    protected final Preferences preferences = PreferencesFactory.get();

    protected final NSNotificationCenter notificationCenter = NSNotificationCenter.defaultCenter();

    protected BookmarkCollection collection;
    /**
     * The bookmark
     */
    protected final Host bookmark;

    private final AbstractCollectionListener<Host> bookmarkCollectionListener = new AbstractCollectionListener<Host>() {
        @Override
        public void collectionItemRemoved(Host item) {
            if(item.equals(bookmark)) {
                final NSWindow window = window();
                if(null != window) {
                    window.close();
                }
            }
        }
    };
    @Outlet
    private NSPopUpButton protocolPopup;
    @Outlet
    private NSPopUpButton encodingPopup;
    @Outlet
    private NSTextField nicknameField;
    @Outlet
    private NSTextField hostField;
    @Outlet
    private NSButton alertIcon;
    @Outlet
    private NSTextField portField;
    @Outlet
    private NSTextField pathField;
    @Outlet
    private NSTextField urlField;
    @Outlet
    private NSTextField usernameField;
    @Outlet
    private NSTextField usernameLabel;
    @Outlet
    private NSButton anonymousCheckbox;
    @Outlet
    private NSTextField webURLField;
    @Outlet
    private NSButton webUrlImage;
    @Outlet
    private NSImage favicon;
    @Outlet
    private NSPopUpButton timezonePopup;
    @Outlet
    private NSPopUpButton certificatePopup;
    @Outlet
    private NSButton toggleOptionsButton;
    @Outlet
    private NSPopUpButton privateKeyPopup;
    @Outlet
    private NSOpenPanel privateKeyOpenPanel;

    /**
     * @param bookmark The bookmark to edit
     */
    public BookmarkController(final Host bookmark) {
        this(BookmarkCollection.defaultCollection(), bookmark);
    }

    public BookmarkController(final BookmarkCollection collection, Host bookmark) {
        this.bookmark = bookmark;
        this.collection = collection;
        // Register for bookmark delete event. Will close this window.
        this.collection.addListener(bookmarkCollectionListener);
    }

    public void setProtocolPopup(final NSPopUpButton button) {
        this.protocolPopup = button;
        this.protocolPopup.setEnabled(true);
        this.protocolPopup.setTarget(this.id());
        this.protocolPopup.setAction(Foundation.selector("protocolSelectionChanged:"));
        this.protocolPopup.removeAllItems();
        for(Protocol protocol : ProtocolFactory.getEnabledProtocols()) {
            final String title = protocol.getDescription();
            this.protocolPopup.addItemWithTitle(title);
            this.protocolPopup.lastItem().setRepresentedObject(String.valueOf(protocol.hashCode()));
            this.protocolPopup.lastItem().setImage(IconCacheFactory.<NSImage>get().iconNamed(protocol.icon(), 16));
        }
    }

    @Action
    public void protocolSelectionChanged(final NSPopUpButton sender) {
        final Protocol selected = ProtocolFactory.forName(protocolPopup.selectedItem().representedObject());
        if(log.isDebugEnabled()) {
            log.debug(String.format("Protocol selection changed to %s", selected));
        }
        bookmark.setPort(selected.getDefaultPort());
        if(!bookmark.getProtocol().isHostnameConfigurable()) {
            // Previously selected protocol had a default hostname. Change to default
            // of newly selected protocol.
            bookmark.setHostname(selected.getDefaultHostname());
        }
        if(!selected.isHostnameConfigurable()) {
            // Hostname of newly selected protocol is not configurable. Change to default.
            bookmark.setHostname(selected.getDefaultHostname());
        }
        if(StringUtils.isNotBlank(selected.getDefaultHostname())) {
            // Prefill with default hostname
            bookmark.setHostname(selected.getDefaultHostname());
        }
        bookmark.setProtocol(selected);
        this.itemChanged();
        this.init();
        this.reachable();
    }

    public void setEncodingPopup(final NSPopUpButton button) {
        this.encodingPopup = button;
        this.encodingPopup.setTarget(this.id());
        final Selector action = Foundation.selector("encodingSelectionChanged:");
        this.encodingPopup.setAction(action);
        this.encodingPopup.removeAllItems();
        this.encodingPopup.addItemWithTitle(DEFAULT);
        this.encodingPopup.menu().addItem(NSMenuItem.separatorItem());
        for(String encoding : new DefaultCharsetProvider().availableCharsets()) {
            this.encodingPopup.addItemWithTitle(encoding);
            this.encodingPopup.lastItem().setRepresentedObject(encoding);
        }
    }

    @Action
    public void encodingSelectionChanged(final NSPopUpButton sender) {
        if(sender.selectedItem().title().equals(DEFAULT)) {
            bookmark.setEncoding(null);
        }
        else {
            bookmark.setEncoding(sender.selectedItem().title());
        }
        this.itemChanged();
    }

    public void setNicknameField(final NSTextField field) {
        this.nicknameField = field;
        notificationCenter.addObserver(this.id(),
                Foundation.selector("nicknameInputDidChange:"),
                NSControl.NSControlTextDidChangeNotification,
                this.nicknameField);
    }

    public void setHostField(final NSTextField field) {
        this.hostField = field;
        notificationCenter.addObserver(this.id(),
                Foundation.selector("hostFieldDidChange:"),
                NSControl.NSControlTextDidChangeNotification,
                field);
    }

    public void setAlertIcon(final NSButton button) {
        this.alertIcon = button;
        this.alertIcon.setEnabled(false);
        this.alertIcon.setImage(null);
        this.alertIcon.setTarget(this.id());
        this.alertIcon.setAction(Foundation.selector("launchNetworkAssistant:"));
    }

    @Action
    public void launchNetworkAssistant(final NSButton sender) {
        ReachabilityFactory.get().diagnose(bookmark);
    }

    public void setPortField(final NSTextField field) {
        this.portField = field;
        notificationCenter.addObserver(this.id(),
                Foundation.selector("portInputDidEndEditing:"),
                NSControl.NSControlTextDidChangeNotification,
                this.portField);
    }

    public void setPathField(NSTextField field) {
        this.pathField = field;
        notificationCenter.addObserver(this.id(),
                Foundation.selector("pathInputDidChange:"),
                NSControl.NSControlTextDidChangeNotification,
                this.pathField);
    }

    public void setUrlField(final NSTextField field) {
        this.urlField = field;
        this.urlField.setAllowsEditingTextAttributes(true);
        this.urlField.setSelectable(true);
    }

    public void setUsernameField(final NSTextField field) {
        this.usernameField = field;
        notificationCenter.addObserver(this.id(),
                Foundation.selector("usernameInputDidChange:"),
                NSControl.NSControlTextDidChangeNotification,
                this.usernameField);
    }

    public void setUsernameLabel(final NSTextField usernameLabel) {
        this.usernameLabel = usernameLabel;
    }

    public void setCertificatePopup(final NSPopUpButton button) {
        this.certificatePopup = button;
        this.certificatePopup.setTarget(this.id());
        final Selector action = Foundation.selector("certificateSelectionChanged:");
        this.certificatePopup.setAction(action);
        this.certificatePopup.removeAllItems();
        this.certificatePopup.addItemWithTitle(LocaleFactory.localizedString("None"));
        this.certificatePopup.menu().addItem(NSMenuItem.separatorItem());
        for(String certificate : new KeychainX509KeyManager(bookmark).list()) {
            this.certificatePopup.addItemWithTitle(certificate);
            this.certificatePopup.lastItem().setRepresentedObject(certificate);
        }
    }

    @Action
    public void certificateSelectionChanged(final NSPopUpButton sender) {
        bookmark.getCredentials().setCertificate(sender.selectedItem().representedObject());
        this.itemChanged();
    }

    public void setAnonymousCheckbox(final NSButton button) {
        this.anonymousCheckbox = button;
        this.anonymousCheckbox.setTarget(this.id());
        this.anonymousCheckbox.setAction(Foundation.selector("anonymousCheckboxClicked:"));
        this.anonymousCheckbox.setState(NSCell.NSOffState);
    }

    public void setWebURLField(final NSTextField field) {
        this.webURLField = field;
        final NSTextFieldCell cell = this.webURLField.cell();
        cell.setPlaceholderString(bookmark.getDefaultWebURL());
        notificationCenter.addObserver(this.id(),
                Foundation.selector("webURLInputDidChange:"),
                NSControl.NSControlTextDidChangeNotification,
                this.webURLField);
    }

    public void setWebUrlImage(final NSButton button) {
        this.webUrlImage = button;
        this.webUrlImage.setTarget(this.id());
        this.webUrlImage.setAction(Foundation.selector("openWebUrl:"));
        this.webUrlImage.setImage(IconCacheFactory.<NSImage>get().iconNamed("site.tiff", 16));
    }

    /**
     *
     */
    private void updateFavicon() {
        if(preferences.getBoolean("bookmark.favicon.download")) {
            this.background(new AbstractBackgroundAction<Void>() {
                @Override
                public Void run() throws BackgroundException {
                    final String f = bookmark.getProtocol().favicon();
                    if(StringUtils.isNotBlank(f)) {
                        favicon = IconCacheFactory.<NSImage>get().iconNamed(f, 16);
                    }
                    else {
                        String url = bookmark.getWebURL() + "/favicon.ico";
                        // Default favicon location
                        final NSData data = NSData.dataWithContentsOfURL(NSURL.URLWithString(url));
                        if(null == data) {
                            return null;
                        }
                        favicon = NSImage.imageWithData(data);
                    }
                    if(null != favicon) {
                        favicon.setSize(new NSSize(16, 16));
                    }
                    return null;
                }

                @Override
                public void cleanup() {
                    if(null != favicon) {
                        webUrlImage.setImage(favicon);
                    }
                }

                @Override
                public Object lock() {
                    return bookmark;
                }
            });
        }
    }

    @Action
    public void openWebUrl(final NSButton sender) {
        BrowserLauncherFactory.get().open(bookmark.getWebURL());
    }

    public void setTimezonePopup(final NSPopUpButton button) {
        this.timezonePopup = button;
        this.timezonePopup.setTarget(this.id());
        this.timezonePopup.setAction(Foundation.selector("timezonePopupClicked:"));
        this.timezonePopup.removeAllItems();
        final List<String> timezones = Arrays.asList(TimeZone.getAvailableIDs());
        this.timezonePopup.addItemWithTitle(UTC.getID());
        this.timezonePopup.lastItem().setRepresentedObject(UTC.getID());
        this.timezonePopup.menu().addItem(NSMenuItem.separatorItem());
        Collections.sort(timezones, new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                return TimeZone.getTimeZone(o1).getID().compareTo(TimeZone.getTimeZone(o2).getID());
            }
        });
        for(String tz : timezones) {
            if(tz.matches(TIMEZONE_CONTINENT_PREFIXES)) {
                this.timezonePopup.addItemWithTitle(String.format("%s", tz));
                this.timezonePopup.lastItem().setRepresentedObject(tz);
            }
        }
    }

    @Action
    public void timezonePopupClicked(final NSPopUpButton sender) {
        String selected = sender.selectedItem().representedObject();
        String[] ids = TimeZone.getAvailableIDs();
        for(String id : ids) {
            TimeZone tz;
            if((tz = TimeZone.getTimeZone(id)).getID().equals(selected)) {
                bookmark.setTimezone(tz);
                break;
            }
        }
        this.itemChanged();
    }

    public void setToggleOptionsButton(final NSButton toggleOptionsButton) {
        this.toggleOptionsButton = toggleOptionsButton;
    }

    @Override
    public void invalidate() {
        preferences.setProperty("bookmark.toggle.options", this.toggleOptionsButton.state());
        collection.removeListener(bookmarkCollectionListener);
        notificationCenter.removeObserver(this.id());
        super.invalidate();
    }

    @Override
    protected String getBundleName() {
        return "Bookmark";
    }

    @Override
    public void awakeFromNib() {
        this.init();
        this.setState(this.toggleOptionsButton, preferences.getBoolean("bookmark.toggle.options"));
        this.reachable();
        this.updateFavicon();
        window.makeFirstResponder(hostField);

        super.awakeFromNib();
    }

    @Override
    public void setWindow(final NSWindow window) {
        window.setContentMinSize(window.frame().size);
        window.setContentMaxSize(new NSSize(600, window.frame().size.height.doubleValue()));
        super.setWindow(window);
        cascade = this.cascade(cascade);
    }

    @Override
    public void windowWillClose(final NSNotification notification) {
        cascade = new NSPoint(this.window().frame().origin.x.doubleValue(),
                this.window().frame().origin.y.doubleValue() + this.window().frame().size.height.doubleValue());
        super.windowWillClose(notification);
    }

    public void setPrivateKeyPopup(final NSPopUpButton button) {
        this.privateKeyPopup = button;
        this.privateKeyPopup.setTarget(this.id());
        final Selector action = Foundation.selector("privateKeyPopupClicked:");
        this.privateKeyPopup.setAction(action);
        this.privateKeyPopup.removeAllItems();
        this.privateKeyPopup.addItemWithTitle(LocaleFactory.localizedString("None"));
        this.privateKeyPopup.menu().addItem(NSMenuItem.separatorItem());
        for(Local certificate : new OpenSSHPrivateKeyConfigurator().list()) {
            this.privateKeyPopup.addItemWithTitle(certificate.getAbbreviatedPath());
            this.privateKeyPopup.lastItem().setRepresentedObject(certificate.getAbsolute());
        }
        if(bookmark.getCredentials().isPublicKeyAuthentication()) {
            final Local key = bookmark.getCredentials().getIdentity();
            if(-1 == this.privateKeyPopup.indexOfItemWithRepresentedObject(key.getAbsolute()).intValue()) {
                this.privateKeyPopup.menu().addItem(NSMenuItem.separatorItem());
                this.privateKeyPopup.addItemWithTitle(key.getAbbreviatedPath());
                this.privateKeyPopup.lastItem().setRepresentedObject(key.getAbsolute());
            }
        }
        // Choose another folder
        this.privateKeyPopup.menu().addItem(NSMenuItem.separatorItem());
        this.privateKeyPopup.menu().addItemWithTitle_action_keyEquivalent(CHOOSE, action, StringUtils.EMPTY);
        this.privateKeyPopup.lastItem().setTarget(this.id());
    }

    @Action
    public void privateKeyPopupClicked(final NSMenuItem sender) {
        if(sender.title().equals(CHOOSE)) {
            privateKeyOpenPanel = NSOpenPanel.openPanel();
            privateKeyOpenPanel.setCanChooseDirectories(false);
            privateKeyOpenPanel.setCanChooseFiles(true);
            privateKeyOpenPanel.setAllowsMultipleSelection(false);
            privateKeyOpenPanel.setMessage(LocaleFactory.localizedString("Select the private key in PEM or PuTTY format", "Credentials"));
            privateKeyOpenPanel.setPrompt(CHOOSE);
            privateKeyOpenPanel.beginSheetForDirectory(OpenSSHPrivateKeyConfigurator.OPENSSH_CONFIGURATION_DIRECTORY.getAbsolute(), null, this.window(), this.id(),
                    Foundation.selector("privateKeyPanelDidEnd:returnCode:contextInfo:"), null);
        }
        else {
            bookmark.getCredentials().setIdentity(LocalFactory.get(sender.representedObject()));
        }
    }

    public void privateKeyPanelDidEnd_returnCode_contextInfo(NSOpenPanel sheet, final int returncode, ID contextInfo) {
        switch(returncode) {
            case SheetCallback.DEFAULT_OPTION:
                final NSObject selected = privateKeyOpenPanel.filenames().lastObject();
                if(selected != null) {
                    final Local key = LocalFactory.get(selected.toString());
                    bookmark.getCredentials().setIdentity(key);
                }
                break;
            case SheetCallback.ALTERNATE_OPTION:
                bookmark.getCredentials().setIdentity(null);
                break;
        }
        this.init();
        this.itemChanged();
    }

    @Action
    public void hostFieldDidChange(final NSNotification sender) {
        final String input = hostField.stringValue();
        if(Scheme.isURL(input)) {
            final Host parsed = HostParser.parse(input);
            bookmark.setHostname(parsed.getHostname());
            bookmark.setProtocol(parsed.getProtocol());
            bookmark.setPort(parsed.getPort());
            bookmark.setDefaultPath(parsed.getDefaultPath());
        }
        else {
            bookmark.setHostname(input);
        }
        this.itemChanged();
        this.init();
        this.reachable();
    }

    private void reachable() {
        if(StringUtils.isNotBlank(bookmark.getHostname())) {
            this.background(new AbstractBackgroundAction<Boolean>() {
                boolean reachable = false;

                @Override
                public Boolean run() throws BackgroundException {
                    if(!preferences.getBoolean("connection.hostname.check")) {
                        return reachable = true;
                    }
                    return reachable = ReachabilityFactory.get().isReachable(bookmark);
                }

                @Override
                public void cleanup() {
                    alertIcon.setEnabled(!reachable);
                    alertIcon.setImage(reachable ? null : IconCacheFactory.<NSImage>get().iconNamed("alert.tiff"));
                }
            });
        }
        else {
            alertIcon.setImage(IconCacheFactory.<NSImage>get().iconNamed("alert.tiff"));
            alertIcon.setEnabled(false);
        }
    }

    @Action
    public void portInputDidEndEditing(final NSNotification sender) {
        try {
            bookmark.setPort(Integer.valueOf(portField.stringValue()));
        }
        catch(NumberFormatException e) {
            bookmark.setPort(-1);
        }
        this.itemChanged();
        this.init();
        this.reachable();
    }

    @Action
    public void pathInputDidChange(final NSNotification sender) {
        bookmark.setDefaultPath(pathField.stringValue());
        this.itemChanged();
        this.init();
    }

    @Action
    public void nicknameInputDidChange(final NSNotification sender) {
        bookmark.setNickname(nicknameField.stringValue());
        this.itemChanged();
        this.init();
    }

    @Action
    public void usernameInputDidChange(final NSNotification sender) {
        bookmark.getCredentials().setUsername(usernameField.stringValue());
        this.itemChanged();
        this.init();
    }

    @Action
    public void anonymousCheckboxClicked(final NSButton sender) {
        if(sender.state() == NSCell.NSOnState) {
            bookmark.getCredentials().setUsername(preferences.getProperty("connection.login.anon.name"));
        }
        if(sender.state() == NSCell.NSOffState) {
            if(preferences.getProperty("connection.login.name").equals(
                    preferences.getProperty("connection.login.anon.name"))) {
                bookmark.getCredentials().setUsername(StringUtils.EMPTY);
            }
            else {
                bookmark.getCredentials().setUsername(preferences.getProperty("connection.login.name"));
            }
        }
        this.itemChanged();
        this.init();
    }

    @Action
    public void webURLInputDidChange(final NSNotification sender) {
        bookmark.setWebURL(webURLField.stringValue());
        this.updateFavicon();
        this.itemChanged();
    }

    /**
     * Updates the window title and url label with the properties of this bookmark
     * Propagates all fields with the properties of this bookmark
     */
    protected void itemChanged() {
        collection.collectionItemChanged(bookmark);
    }

    protected void init() {
        window.setTitle(BookmarkNameProvider.toString(bookmark));
        this.updateField(hostField, bookmark.getHostname());
        hostField.setEnabled(bookmark.getProtocol().isHostnameConfigurable());
        hostField.cell().setPlaceholderString(bookmark.getProtocol().getDefaultHostname());
        this.updateField(nicknameField, BookmarkNameProvider.toString(bookmark));
        urlField.setAttributedStringValue(HyperlinkAttributedStringFactory.create(new HostUrlProvider(true, true).get(bookmark)));
        this.updateField(portField, String.valueOf(bookmark.getPort()));
        portField.setEnabled(bookmark.getProtocol().isPortConfigurable());
        this.updateField(pathField, bookmark.getDefaultPath());
        this.updateField(usernameField, bookmark.getCredentials().getUsername());
        usernameField.cell().setPlaceholderString(bookmark.getProtocol().getUsernamePlaceholder());
        usernameField.setEnabled(!bookmark.getCredentials().isAnonymousLogin());
        usernameLabel.setAttributedStringValue(NSAttributedString.attributedStringWithAttributes(
                StringUtils.isNotBlank(bookmark.getCredentials().getUsernamePlaceholder()) ? String.format("%s:",
                        bookmark.getCredentials().getUsernamePlaceholder()) : StringUtils.EMPTY,
                LABEL_ATTRIBUTES
        ));
        anonymousCheckbox.setEnabled(bookmark.getProtocol().isAnonymousConfigurable());
        anonymousCheckbox.setState(bookmark.getCredentials().isAnonymousLogin() ? NSCell.NSOnState : NSCell.NSOffState);
        protocolPopup.selectItemAtIndex(protocolPopup.indexOfItemWithRepresentedObject(String.valueOf(bookmark.getProtocol().hashCode())));
        encodingPopup.setEnabled(bookmark.getProtocol().isEncodingConfigurable());
        if(null == bookmark.getEncoding()) {
            encodingPopup.selectItemWithTitle(DEFAULT);
        }
        else {
            encodingPopup.selectItemAtIndex(encodingPopup.indexOfItemWithRepresentedObject(bookmark.getEncoding()));
        }
        certificatePopup.setEnabled(bookmark.getProtocol().getScheme() == Scheme.https);
        if(bookmark.getCredentials().isCertificateAuthentication()) {
            certificatePopup.selectItemAtIndex(certificatePopup.indexOfItemWithRepresentedObject(bookmark.getCredentials().getCertificate()));
        }
        else {
            certificatePopup.selectItemWithTitle(LocaleFactory.localizedString("None"));
        }
        privateKeyPopup.setEnabled(bookmark.getProtocol().getType() == Protocol.Type.sftp);
        if(bookmark.getCredentials().isPublicKeyAuthentication()) {
            privateKeyPopup.selectItemAtIndex(privateKeyPopup.indexOfItemWithRepresentedObject(bookmark.getCredentials().getIdentity().getAbsolute()));
        }
        else {
            privateKeyPopup.selectItemWithTitle(LocaleFactory.localizedString("None"));
        }
        final String webURL = bookmark.getWebURL();
        webUrlImage.setToolTip(webURL);
        this.updateField(webURLField, bookmark.getDefaultWebURL().equals(webURL) ? null : webURL);
        timezonePopup.setEnabled(!bookmark.getProtocol().isUTCTimezone());
        if(null == bookmark.getTimezone()) {
            if(bookmark.getProtocol().isUTCTimezone()) {
                timezonePopup.setTitle(UTC.getID());
            }
            else {
                timezonePopup.setTitle(TimeZone.getTimeZone(preferences.getProperty("ftp.timezone.default")).getID());
            }
        }
        else {
            timezonePopup.setTitle(bookmark.getTimezone().getID());
        }
    }

    @Override
    @Action
    public void helpButtonClicked(final NSButton sender) {
        final StringBuilder site = new StringBuilder(preferences.getProperty("website.help"));
        site.append("/howto/bookmarks");
        BrowserLauncherFactory.get().open(site.toString());
    }
}
