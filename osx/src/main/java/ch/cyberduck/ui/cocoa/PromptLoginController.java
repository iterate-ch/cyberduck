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
import ch.cyberduck.binding.SheetController;
import ch.cyberduck.binding.WindowController;
import ch.cyberduck.binding.application.NSAlert;
import ch.cyberduck.binding.application.NSButton;
import ch.cyberduck.binding.application.NSCell;
import ch.cyberduck.binding.application.NSControl;
import ch.cyberduck.binding.application.NSImage;
import ch.cyberduck.binding.application.NSImageView;
import ch.cyberduck.binding.application.NSMenuItem;
import ch.cyberduck.binding.application.NSOpenPanel;
import ch.cyberduck.binding.application.NSPopUpButton;
import ch.cyberduck.binding.application.NSSecureTextField;
import ch.cyberduck.binding.application.NSTextField;
import ch.cyberduck.binding.application.NSWindow;
import ch.cyberduck.binding.application.SheetCallback;
import ch.cyberduck.binding.foundation.NSAttributedString;
import ch.cyberduck.binding.foundation.NSNotification;
import ch.cyberduck.binding.foundation.NSNotificationCenter;
import ch.cyberduck.binding.foundation.NSObject;
import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.DefaultProviderHelpService;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.HostPasswordStore;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.LocalFactory;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.LoginCallback;
import ch.cyberduck.core.LoginOptions;
import ch.cyberduck.core.PasswordStoreFactory;
import ch.cyberduck.core.Protocol;
import ch.cyberduck.core.Scheme;
import ch.cyberduck.core.StringAppender;
import ch.cyberduck.core.exception.LoginCanceledException;
import ch.cyberduck.core.preferences.Preferences;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.resources.IconCacheFactory;
import ch.cyberduck.core.sftp.openssh.OpenSSHPrivateKeyConfigurator;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.rococoa.Foundation;
import org.rococoa.ID;
import org.rococoa.Selector;

public final class PromptLoginController implements LoginCallback {
    private static final Logger log = Logger.getLogger(PromptLoginController.class);

    private final NSNotificationCenter notificationCenter
            = NSNotificationCenter.defaultCenter();

    private final HostPasswordStore keychain
            = PasswordStoreFactory.get();

    private final Preferences preferences
            = PreferencesFactory.get();

    private final WindowController parent;

    private NSOpenPanel select;

    public PromptLoginController(final WindowController parent) {
        this.parent = parent;
    }

    @Override
    public void warn(final Protocol protocol, final String title, final String message,
                     final String continueButton, final String disconnectButton, final String preference)
            throws LoginCanceledException {
        if(log.isDebugEnabled()) {
            log.debug(String.format("Display insecure connection alert for %s", protocol));
        }
        final NSAlert alert = NSAlert.alert(title, message,
                continueButton, // Default Button
                null, // Alternate button
                disconnectButton // Other
        );
        alert.setShowsHelp(true);
        alert.setShowsSuppressionButton(true);
        alert.suppressionButton().setTitle(LocaleFactory.localizedString("Don't show again", "Credentials"));
        alert.setAlertStyle(NSAlert.NSWarningAlertStyle);
        final StringBuilder site = new StringBuilder(preferences.getProperty("website.help"));
        site.append("/").append(protocol.getScheme().name());
        int option = parent.alert(alert, site.toString());
        if(alert.suppressionButton().state() == NSCell.NSOnState) {
            // Never show again.
            preferences.setProperty(preference, true);
        }
        switch(option) {
            case SheetCallback.CANCEL_OPTION:
                throw new LoginCanceledException();
        }
        //Proceed nevertheless.
    }

    @Override
    public void prompt(final Host bookmark, final Credentials credentials,
                       final String title, final String reason,
                       final LoginOptions options) throws LoginCanceledException {
        if(log.isDebugEnabled()) {
            log.debug(String.format("Prompt for credentials for %s", bookmark));
        }
        final SheetController sheet = new SheetController(parent) {
            @Outlet
            private NSImageView iconView;
            @Outlet
            private NSTextField usernameLabel;
            @Outlet
            private NSTextField passwordLabel;
            @Outlet
            private NSTextField titleField;
            @Outlet
            private NSTextField usernameField;
            @Outlet
            private NSTextField textField;
            @Outlet
            private NSSecureTextField passwordField;
            @Outlet
            private NSButton keychainCheckbox;
            @Outlet
            private NSButton anonymousCheckbox;
            @Outlet
            private NSPopUpButton privateKeyPopup;
            @Outlet
            private NSOpenPanel privateKeyOpenPanel;


            @Override
            protected String getBundleName() {
                return "Login";
            }

            @Override
            public void awakeFromNib() {
                this.update();
                window.makeFirstResponder(usernameField);
                super.awakeFromNib();
            }

            @Override
            public void helpButtonClicked(NSButton sender) {
                new DefaultProviderHelpService().help(bookmark.getProtocol());
            }

            public void setIconView(NSImageView iconView) {
                this.iconView = iconView;
                this.iconView.setImage(IconCacheFactory.<NSImage>get().iconNamed(bookmark.getProtocol().disk()));
            }

            public void setUsernameLabel(NSTextField usernameLabel) {
                this.usernameLabel = usernameLabel;
            }

            public void setPasswordLabel(NSTextField passwordLabel) {
                this.passwordLabel = passwordLabel;
            }

            public void setTitleField(NSTextField titleField) {
                this.titleField = titleField;
                this.updateField(this.titleField, LocaleFactory.localizedString(title, "Credentials"));
            }

            public void setUsernameField(NSTextField usernameField) {
                this.usernameField = usernameField;
                this.updateField(this.usernameField, credentials.getUsername());
                notificationCenter.addObserver(this.id(),
                        Foundation.selector("userFieldTextDidChange:"),
                        NSControl.NSControlTextDidChangeNotification,
                        this.usernameField);
            }

            public void userFieldTextDidChange(NSNotification notification) {
                credentials.setUsername(usernameField.stringValue());
                if(StringUtils.isNotBlank(credentials.getUsername())) {
                    final String password = keychain.getPassword(bookmark.getProtocol().getScheme(), bookmark.getPort(),
                            bookmark.getHostname(), credentials.getUsername());
                    if(StringUtils.isNotBlank(password)) {
                        passwordField.setStringValue(password);
                        this.passFieldTextDidChange(notification);
                    }
                }
                this.update();
            }

            public void setTextField(NSTextField textField) {
                this.textField = textField;
                this.textField.setSelectable(true);
                if(reason.startsWith(Scheme.http.name())) {
                    // For OAuth2
                    this.textField.setAttributedStringValue(HyperlinkAttributedStringFactory.create(reason));
                    this.textField.setAllowsEditingTextAttributes(true);
                    this.textField.setSelectable(true);
                }
                else {
                    this.updateField(this.textField, new StringAppender().append(reason).toString());
                }
            }

            public void setPasswordField(NSSecureTextField passwordField) {
                this.passwordField = passwordField;
                this.updateField(this.passwordField, credentials.getPassword());
                notificationCenter.addObserver(this.id(),
                        Foundation.selector("passFieldTextDidChange:"),
                        NSControl.NSControlTextDidChangeNotification,
                        this.passwordField);
            }

            public void passFieldTextDidChange(NSNotification notification) {
                credentials.setPassword(passwordField.stringValue());
            }

            public void setKeychainCheckbox(NSButton keychainCheckbox) {
                this.keychainCheckbox = keychainCheckbox;
                this.keychainCheckbox.setTarget(this.id());
                this.keychainCheckbox.setAction(Foundation.selector("keychainCheckboxClicked:"));
                this.keychainCheckbox.setState(preferences.getBoolean("connection.login.useKeychain")
                        && preferences.getBoolean("connection.login.addKeychain") ? NSCell.NSOnState : NSCell.NSOffState);
            }

            public void keychainCheckboxClicked(final NSButton sender) {
                final boolean enabled = sender.state() == NSCell.NSOnState;
                preferences.setProperty("connection.login.addKeychain", enabled);
            }

            public void setAnonymousCheckbox(NSButton anonymousCheckbox) {
                this.anonymousCheckbox = anonymousCheckbox;
                this.anonymousCheckbox.setTarget(this.id());
                this.anonymousCheckbox.setAction(Foundation.selector("anonymousCheckboxClicked:"));
            }

            @Action
            public void anonymousCheckboxClicked(final NSButton sender) {
                if(sender.state() == NSCell.NSOnState) {
                    credentials.setUsername(preferences.getProperty("connection.login.anon.name"));
                    credentials.setPassword(preferences.getProperty("connection.login.anon.pass"));
                }
                if(sender.state() == NSCell.NSOffState) {
                    credentials.setUsername(preferences.getProperty("connection.login.name"));
                    credentials.setPassword(null);
                }
                this.updateField(this.usernameField, credentials.getUsername());
                this.updateField(this.passwordField, credentials.getPassword());
                this.update();
            }

            public void setPrivateKeyPopup(final NSPopUpButton button) {
                this.privateKeyPopup = button;
                this.privateKeyPopup.setTarget(this.id());
                final Selector action = Foundation.selector("privateKeyPopupClicked:");
                this.privateKeyPopup.setAction(action);
                this.privateKeyPopup.removeAllItems();
                this.privateKeyPopup.addItemWithTitle(LocaleFactory.localizedString("None"));
                this.privateKeyPopup.lastItem().setRepresentedObject(StringUtils.EMPTY);
                this.privateKeyPopup.menu().addItem(NSMenuItem.separatorItem());
                for(Local certificate : new OpenSSHPrivateKeyConfigurator().list()) {
                    this.privateKeyPopup.addItemWithTitle(certificate.getAbbreviatedPath());
                    this.privateKeyPopup.lastItem().setRepresentedObject(certificate.getAbsolute());
                }
                if(credentials.isPublicKeyAuthentication()) {
                    final Local key = credentials.getIdentity();
                    if(-1 == this.privateKeyPopup.indexOfItemWithRepresentedObject(key.getAbsolute()).intValue()) {
                        this.privateKeyPopup.menu().addItem(NSMenuItem.separatorItem());
                        this.privateKeyPopup.addItemWithTitle(key.getAbbreviatedPath());
                        this.privateKeyPopup.lastItem().setRepresentedObject(key.getAbsolute());
                    }
                }
                // Choose another folder
                this.privateKeyPopup.menu().addItem(NSMenuItem.separatorItem());
                this.privateKeyPopup.addItemWithTitle(String.format("%s…", LocaleFactory.localizedString("Choose")));
            }

            @Action
            public void privateKeyPopupClicked(final NSMenuItem sender) {
                final String selected = sender.representedObject();
                if(null == selected) {
                    privateKeyOpenPanel = NSOpenPanel.openPanel();
                    privateKeyOpenPanel.setCanChooseDirectories(false);
                    privateKeyOpenPanel.setCanChooseFiles(true);
                    privateKeyOpenPanel.setAllowsMultipleSelection(false);
                    privateKeyOpenPanel.setMessage(LocaleFactory.localizedString("Select the private key in PEM or PuTTY format", "Credentials"));
                    privateKeyOpenPanel.setPrompt(String.format("%s…", LocaleFactory.localizedString("Choose")));
                    privateKeyOpenPanel.beginSheetForDirectory(OpenSSHPrivateKeyConfigurator.OPENSSH_CONFIGURATION_DIRECTORY.getAbsolute(), null, this.window(), this.id(),
                            Foundation.selector("privateKeyPanelDidEnd:returnCode:contextInfo:"), null);
                }
                else {
                    credentials.setIdentity(StringUtils.isBlank(selected) ? null : LocalFactory.get(selected));
                    this.update();
                }
            }

            public void privateKeyPanelDidEnd_returnCode_contextInfo(NSOpenPanel sheet, final int returncode, ID contextInfo) {
                switch(returncode) {
                    case SheetCallback.DEFAULT_OPTION:
                        final NSObject selected = privateKeyOpenPanel.filenames().lastObject();
                        if(selected != null) {
                            final Local key = LocalFactory.get(selected.toString());
                            credentials.setIdentity(key);
                        }
                        break;
                    case SheetCallback.ALTERNATE_OPTION:
                        credentials.setIdentity(null);
                        break;
                }
                this.update();
            }

            private void update() {
                this.usernameField.setEnabled(options.user && !credentials.isAnonymousLogin());
                this.usernameField.cell().setPlaceholderString(credentials.getUsernamePlaceholder());

                this.usernameLabel.setAttributedStringValue(NSAttributedString.attributedStringWithAttributes(
                        StringUtils.isNotBlank(credentials.getUsernamePlaceholder()) ? String.format("%s:",
                                credentials.getUsernamePlaceholder()) : StringUtils.EMPTY,
                        LABEL_ATTRIBUTES
                ));

                this.passwordField.setEnabled(options.password && !credentials.isAnonymousLogin());
                this.passwordField.cell().setPlaceholderString(credentials.getPasswordPlaceholder());

                this.passwordLabel.setAttributedStringValue(NSAttributedString.attributedStringWithAttributes(
                        StringUtils.isNotBlank(credentials.getPasswordPlaceholder()) ? String.format("%s:",
                                credentials.getPasswordPlaceholder()) : StringUtils.EMPTY,
                        LABEL_ATTRIBUTES
                ));

                this.keychainCheckbox.setEnabled(options.keychain && !credentials.isAnonymousLogin());
                this.keychainCheckbox.setState(!(!options.keychain || credentials.isAnonymousLogin()) ? NSCell.NSOnState : NSCell.NSOffState);

                this.anonymousCheckbox.setEnabled(options.anonymous);
                this.anonymousCheckbox.setState(options.anonymous && credentials.isAnonymousLogin() ? NSCell.NSOnState : NSCell.NSOffState);

                this.privateKeyPopup.setEnabled(options.publickey);
                if(options.publickey && credentials.isPublicKeyAuthentication()) {
                    privateKeyPopup.selectItemAtIndex(privateKeyPopup.indexOfItemWithRepresentedObject(credentials.getIdentity().getAbsolute()));
                }
                else {
                    this.privateKeyPopup.selectItemWithTitle(LocaleFactory.localizedString("None"));
                }
            }

            @Override
            protected boolean validateInput() {
                credentials.setUsername(usernameField.stringValue());
                credentials.setPassword(passwordField.stringValue());
                return credentials.validate(bookmark.getProtocol(), options);
            }

            @Override
            public void callback(final int returncode) {
                if(returncode == SheetCallback.DEFAULT_OPTION) {
                    this.window().endEditingFor(null);
                    credentials.setSaved(keychainCheckbox.state() == NSCell.NSOnState);
                    credentials.setUsername(usernameField.stringValue());
                    credentials.setPassword(passwordField.stringValue());
                }
            }
        };
        sheet.beginSheet();
        if(sheet.returnCode() == SheetCallback.CANCEL_OPTION) {
            throw new LoginCanceledException();
        }
    }

    public Local select(final Local identity) throws LoginCanceledException {
        final Local selected = this.select(parent, new SheetCallback() {
            @Override
            public void callback(final int returncode) {
                //
            }
        });
        if(null == selected) {
            throw new LoginCanceledException();
        }
        return selected;
    }

    protected Local select(final WindowController parent, final SheetCallback callback) {
        final SheetController sheet = new SheetController(parent) {
            @Override
            public void callback(final int returncode) {
                callback.callback(returncode);
            }

            @Override
            public void beginSheet(final NSWindow window) {
                select = NSOpenPanel.openPanel();
                select.setCanChooseDirectories(false);
                select.setCanChooseFiles(true);
                select.setAllowsMultipleSelection(false);
                select.setMessage(LocaleFactory.localizedString("Select the private key in PEM or PuTTY format", "Credentials"));
                select.setPrompt(LocaleFactory.localizedString("Choose"));
                select.beginSheetForDirectory(LocalFactory.get("~/.ssh").getAbsolute(),
                        null, parent.window(), this.id(), Foundation.selector("sheetDidClose:returnCode:contextInfo:"), null);
            }

            @Override
            public NSWindow window() {
                return select;
            }

            @Override
            public void invalidate() {
                notificationCenter.removeObserver(this.id());
                super.invalidate();
            }
        };
        sheet.beginSheet();
        if(sheet.returnCode() == SheetCallback.DEFAULT_OPTION) {
            final NSObject selected = select.filenames().lastObject();
            if(selected != null) {
                return LocalFactory.get(selected.toString());
            }
        }
        return null;
    }
}
