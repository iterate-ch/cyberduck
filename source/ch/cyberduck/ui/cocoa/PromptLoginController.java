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

import ch.cyberduck.core.*;
import ch.cyberduck.core.i18n.Locale;
import ch.cyberduck.ui.cocoa.application.*;
import ch.cyberduck.ui.cocoa.foundation.*;

import org.rococoa.Foundation;
import org.rococoa.ID;

import org.apache.log4j.Logger;

/**
 * @version $Id$
 */
public class PromptLoginController extends AbstractLoginController {
    private static Logger log = Logger.getLogger(PromptLoginController.class);

    private WindowController parent;

    public PromptLoginController(final WindowController parent) {
        this.parent = parent;
    }

    @Override
    public void prompt(final Protocol protocol, final Credentials credentials,
                       final String title, final String reason,
                       final boolean enableKeychain, final boolean enablePublicKey) throws LoginCanceledException {
        SheetController c = new SheetController(parent) {
            @Override
            protected String getBundleName() {
                return "Login";
            }

            @Override
            public void awakeFromNib() {
                this.update();
                super.awakeFromNib();
            }

            @Outlet
            protected NSImageView iconView;

            public void setIconView(NSImageView iconView) {
                this.iconView = iconView;
                this.iconView.setImage(IconCache.iconNamed(protocol.disk()));
            }

            @Outlet
            private NSTextField userLabel;

            public void setUserLabel(NSTextField userLabel) {
                this.userLabel = userLabel;
            }

            @Outlet
            private NSTextField passwordLabel;

            public void setPasswordLabel(NSTextField passwordLabel) {
                this.passwordLabel = passwordLabel;
            }

            @Outlet
            private NSTextField titleField;

            public void setTitleField(NSTextField titleField) {
                this.titleField = titleField;
                this.updateField(this.titleField, Locale.localizedString(title, "Credentials"));
            }

            @Outlet
            private NSTextField usernameField;

            public void setUsernameField(NSTextField usernameField) {
                this.usernameField = usernameField;
                this.updateField(this.usernameField, credentials.getUsername());
                this.usernameField.cell().setPlaceholderString(credentials.getUsernamePlaceholder());
                NSNotificationCenter.defaultCenter().addObserver(this.id(),
                        Foundation.selector("userFieldTextDidChange:"),
                        NSControl.NSControlTextDidChangeNotification,
                        this.usernameField);
            }

            public void userFieldTextDidChange(NSNotification notification) {
                credentials.setUsername(usernameField.stringValue());
                this.update();
            }

            @Outlet
            private NSTextField textField;

            public void setTextField(NSTextField textField) {
                this.textField = textField;
                this.updateField(this.textField, Locale.localizedString(reason, "Credentials"));
            }

            @Outlet
            private NSSecureTextField passwordField;

            public void setPasswordField(NSSecureTextField passwordField) {
                this.passwordField = passwordField;
                this.updateField(this.passwordField, credentials.getPassword());
                this.passwordField.cell().setPlaceholderString(credentials.getPasswordPlaceholder());
                NSNotificationCenter.defaultCenter().addObserver(this.id(),
                        Foundation.selector("passFieldTextDidChange:"),
                        NSControl.NSControlTextDidChangeNotification,
                        this.passwordField);
            }

            public void passFieldTextDidChange(NSNotification notification) {
                credentials.setPassword(passwordField.stringValue());
            }

            @Outlet
            private NSButton keychainCheckbox;

            public void setKeychainCheckbox(NSButton keychainCheckbox) {
                this.keychainCheckbox = keychainCheckbox;
                this.keychainCheckbox.setTarget(this.id());
                this.keychainCheckbox.setAction(Foundation.selector("keychainCheckboxClicked:"));
                this.keychainCheckbox.setEnabled(Preferences.instance().getBoolean("connection.login.useKeychain"));
                this.keychainCheckbox.setState(Preferences.instance().getBoolean("connection.login.useKeychain")
                        && Preferences.instance().getBoolean("connection.login.addKeychain") ? NSCell.NSOnState : NSCell.NSOffState);
            }

            public void keychainCheckboxClicked(final NSButton sender) {
                final boolean enabled = sender.state() == NSCell.NSOnState;
                credentials.setUseKeychain(enabled);
                Preferences.instance().setProperty("connection.login.addKeychain", enabled);
            }

            @Outlet
            private NSButton anonymousCheckbox;

            public void setAnonymousCheckbox(NSButton anonymousCheckbox) {
                this.anonymousCheckbox = anonymousCheckbox;
                this.anonymousCheckbox.setTarget(this.id());
                this.anonymousCheckbox.setAction(Foundation.selector("anonymousCheckboxClicked:"));
            }

            @Action
            public void anonymousCheckboxClicked(final NSButton sender) {
                if(sender.state() == NSCell.NSOnState) {
                    credentials.setUsername(Preferences.instance().getProperty("connection.login.anon.name"));
                    credentials.setPassword(Preferences.instance().getProperty("connection.login.anon.pass"));
                }
                if(sender.state() == NSCell.NSOffState) {
                    credentials.setUsername(Preferences.instance().getProperty("connection.login.name"));
                    credentials.setPassword(null);
                }
                this.updateField(this.usernameField, credentials.getUsername());
                this.updateField(this.passwordField, credentials.getPassword());
                this.update();
            }

            @Outlet
            private NSTextField pkLabel;

            public void setPkLabel(NSTextField pkLabel) {
                this.pkLabel = pkLabel;
            }

            @Outlet
            private NSButton pkCheckbox;

            public void setPkCheckbox(NSButton pkCheckbox) {
                this.pkCheckbox = pkCheckbox;
                this.pkCheckbox.setTarget(this.id());
                this.pkCheckbox.setAction(Foundation.selector("pkCheckboxSelectionChanged:"));
            }

            private NSOpenPanel publicKeyPanel;

            @Action
            public void pkCheckboxSelectionChanged(final NSButton sender) {
                if(sender.state() == NSCell.NSOnState) {
                    publicKeyPanel = NSOpenPanel.openPanel();
                    publicKeyPanel.setCanChooseDirectories(false);
                    publicKeyPanel.setCanChooseFiles(true);
                    publicKeyPanel.setAllowsMultipleSelection(false);
                    publicKeyPanel.setMessage(Locale.localizedString("Select the private key in PEM format", "Credentials"));
                    publicKeyPanel.setPrompt(Locale.localizedString("Choose"));
                    publicKeyPanel.beginSheetForDirectory(LocalFactory.createLocal("~/.ssh").getAbsolute(),
                            null, this.window(), this.id(),
                            Foundation.selector("pkSelectionPanelDidEnd:returnCode:contextInfo:"), null);
                }
                else {
                    this.pkSelectionPanelDidEnd_returnCode_contextInfo(publicKeyPanel, NSPanel.NSCancelButton, null);
                }
            }

            public void pkSelectionPanelDidEnd_returnCode_contextInfo(NSOpenPanel sheet, int returncode, ID contextInfo) {
                if(returncode == NSPanel.NSOKButton) {
                    NSArray selected = sheet.filenames();
                    final NSEnumerator enumerator = selected.objectEnumerator();
                    NSObject next;
                    while((next = enumerator.nextObject()) != null) {
                        credentials.setIdentity(LocalFactory.createLocal(next.toString()));
                    }
                }
                if(returncode == NSPanel.NSCancelButton) {
                    credentials.setIdentity(null);
                }
                update();
            }

            private void update() {
                this.usernameField.setEnabled(!credentials.isAnonymousLogin());
                this.passwordField.setEnabled(!credentials.isAnonymousLogin());
                {
                    boolean enable = enableKeychain && !credentials.isAnonymousLogin();
                    this.keychainCheckbox.setEnabled(enable);
                    if(!enable) {
                        this.keychainCheckbox.setState(NSCell.NSOffState);
                    }
                }
                this.anonymousCheckbox.setState(credentials.isAnonymousLogin() ? NSCell.NSOnState : NSCell.NSOffState);
                this.pkCheckbox.setEnabled(enablePublicKey);
                if(credentials.isPublicKeyAuthentication()) {
                    this.pkCheckbox.setState(NSCell.NSOnState);
                    this.updateField(this.pkLabel, credentials.getIdentity().toURL());
                    this.pkLabel.setTextColor(NSColor.textColor());
                }
                else {
                    this.pkCheckbox.setState(NSCell.NSOffState);
                    this.pkLabel.setStringValue(Locale.localizedString("No private key selected"));
                    this.pkLabel.setTextColor(NSColor.disabledControlTextColor());
                }
            }

            @Override
            protected boolean validateInput() {
                return credentials.validate(protocol);
            }

            public void callback(final int returncode) {
                if(returncode == SheetCallback.DEFAULT_OPTION) {
                    this.window().endEditingFor(null);
                    credentials.setUsername(usernameField.stringValue());
                    credentials.setPassword(passwordField.stringValue());
                }
            }
        };
        c.beginSheet();

        if(c.returnCode() == SheetCallback.ALTERNATE_OPTION) {
            throw new LoginCanceledException();
        }
    }
}
