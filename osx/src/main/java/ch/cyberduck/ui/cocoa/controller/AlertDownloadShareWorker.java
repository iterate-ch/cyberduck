/*
 * Copyright (c) 2016 iterate GmbH. All rights reserved.
 */

package ch.cyberduck.ui.cocoa.controller;

import ch.cyberduck.binding.AlertController;
import ch.cyberduck.binding.ProxyController;
import ch.cyberduck.binding.SystemAlertController;
import ch.cyberduck.binding.application.NSAlert;
import ch.cyberduck.binding.application.NSTextField;
import ch.cyberduck.binding.application.NSView;
import ch.cyberduck.binding.application.SheetCallback;
import ch.cyberduck.binding.foundation.NSAttributedString;
import ch.cyberduck.core.DescriptiveUrl;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.PasswordCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.features.Share;
import ch.cyberduck.core.worker.DownloadShareWorker;
import ch.cyberduck.ui.pasteboard.PasteboardService;
import ch.cyberduck.ui.pasteboard.PasteboardServiceFactory;

import java.text.MessageFormat;

public final class AlertDownloadShareWorker<Options> extends DownloadShareWorker<Options> {

    private final ProxyController controller;
    private final Path file;

    public AlertDownloadShareWorker(final ProxyController controller, final Path file, final Options options, final PasswordCallback password, final Share.ShareeCallback sharee) {
        super(file, options, password, sharee);
        this.file = file;
        this.controller = controller;
    }

    @Override
    public void cleanup(final DescriptiveUrl url) {
        if(null != url) {
            final AlertController alert = new SystemAlertController(NSAlert.alert(LocaleFactory.localizedString("Create Download Share", "Share"),
                    MessageFormat.format(LocaleFactory.localizedString("You have successfully created a share link for {0}.", "SDS"), file.getName()),
                    LocaleFactory.localizedString("Continue", "Credentials"),
                    DescriptiveUrl.EMPTY != url ? LocaleFactory.localizedString("Copy", "Main") : null,
                    null)) {
                @Override
                public NSView getAccessoryView(final NSAlert alert) {
                    if(DescriptiveUrl.EMPTY == url) {
                        return null;
                    }
                    final NSTextField field = NSTextField.textFieldWithString(url.getUrl());
                    field.setEditable(false);
                    field.setSelectable(true);
                    field.cell().setWraps(false);
                    field.setAttributedStringValue(NSAttributedString.attributedStringWithAttributes(url.getUrl(), TRUNCATE_MIDDLE_ATTRIBUTES));
                    return field;
                }
            };
            controller.alert(alert, new SheetCallback() {
                @Override
                public void callback(final int returncode) {
                    switch(returncode) {
                        case SheetCallback.CANCEL_OPTION:
                            PasteboardServiceFactory.get().add(PasteboardService.Type.url, url.getUrl());
                    }
                }
            });
        }
    }
}
