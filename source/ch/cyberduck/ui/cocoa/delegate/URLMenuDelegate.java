package ch.cyberduck.ui.cocoa.delegate;

/*
 * Copyright (c) 2002-2010 David Kocher. All rights reserved.
 *
 * http://cyberduck.ch/
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
 *
 * Bug fixes, suggestions and comments should be sent to:
 * dkocher@cyberduck.ch
 */

import ch.cyberduck.core.Path;
import ch.cyberduck.core.cloud.Distribution;
import ch.cyberduck.core.i18n.Locale;
import ch.cyberduck.core.s3.S3Path;
import ch.cyberduck.core.s3.S3Session;
import ch.cyberduck.ui.cocoa.Action;
import ch.cyberduck.ui.cocoa.IconCache;
import ch.cyberduck.ui.cocoa.TableCellAttributes;
import ch.cyberduck.ui.cocoa.application.*;
import ch.cyberduck.ui.cocoa.foundation.NSArray;
import ch.cyberduck.ui.cocoa.foundation.NSAttributedString;
import ch.cyberduck.ui.cocoa.foundation.NSDictionary;
import ch.cyberduck.ui.cocoa.foundation.NSString;

import org.rococoa.Foundation;
import org.rococoa.ID;
import org.rococoa.Selector;
import org.rococoa.cocoa.foundation.NSInteger;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.text.MessageFormat;

/**
 * @version $Id:$
 */
public abstract class URLMenuDelegate extends AbstractMenuDelegate {
    private static Logger log = Logger.getLogger(URLMenuDelegate.class);

    protected static final NSDictionary URL_FONT_ATTRIBUTES = NSDictionary.dictionaryWithObjectsForKeys(
            NSArray.arrayWithObjects(NSFont.userFontOfSize(NSFont.smallSystemFontSize()), NSColor.darkGrayColor(),
                    TableCellAttributes.PARAGRAPH_STYLE_LEFT_ALIGNMENT_TRUNCATE_MIDDLE),
            NSArray.arrayWithObjects(NSAttributedString.FontAttributeName, NSAttributedString.ForegroundColorAttributeName,
                    NSAttributedString.ParagraphStyleAttributeName)
    );

    protected abstract Path getSelectedFile();

    public NSInteger numberOfItemsInMenu(NSMenu menu) {
        Path path = this.getSelectedFile();
        if(path instanceof S3Path) {
            S3Session session = ((S3Path) path).getSession();
            int count = 8;
            for(Distribution.Method method : session.getSupportedDistributionMethods()) {
                Distribution distribution = session.getDistribution(path.getContainerName(), method);
                if(null == distribution) {
                    continue;
                }
                count++;
                count++;
            }
            return new NSInteger(count);
        }
        return new NSInteger(4);
    }

    @Override
    public boolean menuUpdateItemAtIndex(NSMenu menu, NSMenuItem item, NSInteger index, boolean cancel) {
        item.setTarget(this.id());
        Path path = this.getSelectedFile();
        item.setEnabled(path != null);
        item.setTitle(Locale.localizedString("Unknown"));
        item.setKeyEquivalent("");
        if(index.intValue() == 0) {
            item.setTitle(MessageFormat.format(Locale.localizedString("Copy {0} URL"), path != null ? path.getHost().getProtocol().getName() : ""));
            item.setAction(Foundation.selector("copyURLClicked:"));
            item.setKeyEquivalent("c");
            item.setKeyEquivalentModifierMask(NSEvent.NSCommandKeyMask | NSEvent.NSShiftKeyMask);
        }
        else if(index.intValue() == 1) {
            item.setEnabled(false);
            if(path != null) {
                String url = path.toURL();
                item.setAttributedTitle(NSAttributedString.attributedStringWithAttributes(url, URL_FONT_ATTRIBUTES));
                item.setImage(IconCache.iconNamed("site", 16));
            }
            else {
                item.setAttributedTitle(NSAttributedString.attributedStringWithAttributes(Locale.localizedString("Unknown"), URL_FONT_ATTRIBUTES));
            }
        }

        // Possible HTTP URL
        else if(index.intValue() == 2) {
            item.setTitle(MessageFormat.format(Locale.localizedString("Copy {0} URL"), Locale.localizedString("Web")));
            item.setAction(Foundation.selector("copyWebURLClicked:"));
        }
        else if(index.intValue() == 3) {
            item.setAttributedTitle(NSAttributedString.attributedStringWithAttributes(Locale.localizedString("Unknown"), URL_FONT_ATTRIBUTES));
            item.setEnabled(false);
            if(path != null) {
                String url = path.toHttpURL().getUrl();
                item.setAttributedTitle(NSAttributedString.attributedStringWithAttributes(url, URL_FONT_ATTRIBUTES));
                item.setImage(IconCache.iconNamed("site", 16));
            }
        }

        // Temporary signed URL for AWS
        else if(index.intValue() == 4) {
            item.setTitle(Locale.localizedString("Copy Signed URL"));
            item.setAction(Foundation.selector("copySignedClicked:"));
        }
        else if(index.intValue() == 5) {
            item.setAttributedTitle(NSAttributedString.attributedStringWithAttributes(Locale.localizedString("Unknown"), URL_FONT_ATTRIBUTES));
            if(path != null) {
                if(path instanceof S3Path) {
                    S3Path.DescriptiveUrl description = ((S3Path) path).toSignedUrl();
                    String url = description.getUrl();
                    if(StringUtils.isNotBlank(url)) {
                        item.setAttributedTitle(NSAttributedString.attributedStringWithAttributes(url, URL_FONT_ATTRIBUTES));
                        item.setImage(IconCache.iconNamed("site", 16));
                    }
                }
            }
            item.setEnabled(false);
        }

        // Authenticated URL for Google Storage
        else if(index.intValue() == 6) {
            item.setTitle(MessageFormat.format(Locale.localizedString("Copy {0} URL"), Locale.localizedString("Authenticated")));
            item.setAction(Foundation.selector("copyAuthenticatedClicked:"));
        }
        else if(index.intValue() == 7) {
            item.setAttributedTitle(NSAttributedString.attributedStringWithAttributes(Locale.localizedString("Unknown"), URL_FONT_ATTRIBUTES));
            item.setEnabled(false);
            if(path != null) {
                if(path instanceof S3Path) {
                    S3Path.DescriptiveUrl description = ((S3Path) path).toAuthenticatedUrl();
                    String url = description.getUrl();
                    if(StringUtils.isNotBlank(url)) {
                        item.setAttributedTitle(NSAttributedString.attributedStringWithAttributes(url, URL_FONT_ATTRIBUTES));
                        item.setImage(IconCache.iconNamed("site", 16));
                    }
                }
            }
        }

        // Distributions
        else if(index.intValue() == 8) {
            item.setTitle(MessageFormat.format(Locale.localizedString("Copy {0} URL"), Locale.localizedString(Distribution.DOWNLOAD.toString(), "S3")));
            item.setAction(Foundation.selector("copyDowloadDistributionClicked:"));
        }
        else if(index.intValue() == 9) {
            item.setAttributedTitle(NSAttributedString.attributedStringWithAttributes(Locale.localizedString("Unknown"), URL_FONT_ATTRIBUTES));
            item.setEnabled(false);
            if(path != null) {
                if(path instanceof S3Path) {
                    S3Session session = ((S3Path) path).getSession();
                    Distribution distribution = session.getDistribution(path.getContainerName(), Distribution.DOWNLOAD);
                    if(null != distribution) {
                        String url = distribution.getCnameUrl(((S3Path) path).getKey());
                        if(StringUtils.isNotBlank(url)) {
                            item.setAttributedTitle(NSAttributedString.attributedStringWithAttributes(url, URL_FONT_ATTRIBUTES));
                            item.setImage(IconCache.iconNamed("site", 16));
                        }
                    }
                }
            }
        }
        else if(index.intValue() == 9) {
            item.setTitle(MessageFormat.format(Locale.localizedString("Copy {0} URL"), Locale.localizedString(Distribution.STREAMING.toString(), "S3")));
            item.setAction(Foundation.selector("copyDowloadDistributionClicked:"));
        }
        else if(index.intValue() == 10) {
            item.setAttributedTitle(NSAttributedString.attributedStringWithAttributes(Locale.localizedString("Unknown"), URL_FONT_ATTRIBUTES));
            item.setEnabled(false);
            if(path != null) {
                if(path instanceof S3Path) {
                    S3Session session = ((S3Path) path).getSession();
                    Distribution distribution = session.getDistribution(path.getContainerName(), Distribution.STREAMING);
                    if(null != distribution) {
                        String url = distribution.getCnameUrl(((S3Path) path).getKey());
                        if(StringUtils.isNotBlank(url)) {
                            item.setAttributedTitle(NSAttributedString.attributedStringWithAttributes(url, URL_FONT_ATTRIBUTES));
                            item.setImage(IconCache.iconNamed("site", 16));
                        }
                    }
                }
            }
        }
        return super.menuUpdateItemAtIndex(menu, item, index, cancel);
    }

    @Action
    public void copyURLClicked(final ID sender) {
        copy(this.getSelectedFile().toURL());
    }

    @Action
    public void copyWebURLClicked(final ID sender) {
        this.copy(this.getSelectedFile().toHttpURL().getUrl());
    }

    @Action
    public void copySignedClicked(final ID sender) {
        this.copy(((S3Path) this.getSelectedFile()).toSignedUrl().getUrl());
    }

    @Action
    public void copyAuthenticatedClicked(final ID sender) {
        this.copy(((S3Path) this.getSelectedFile()).toAuthenticatedUrl().getUrl());
    }

    @Action
    public void copyDowloadDistributionClicked(final ID sender) {
        this.copy(((S3Path) this.getSelectedFile()).toAuthenticatedUrl().getUrl());
    }

    private void copy(String url) {
        if(StringUtils.isNotBlank(url)) {
            NSPasteboard pboard = NSPasteboard.generalPasteboard();
            pboard.declareTypes(NSArray.arrayWithObject(NSString.stringWithString(NSPasteboard.StringPboardType)), null);
            if(!pboard.setStringForType(url, NSPasteboard.StringPboardType)) {
                log.error("Error writing URL to NSPasteboard.StringPboardType.");
            }
        }
        else {
            AppKitFunctions.instance.NSBeep();
        }
    }

    public boolean validateMenuItem(NSMenuItem item) {
        if(null == this.getSelectedFile()) {
            return false;
        }
        final Selector action = item.action();
        if(action.equals(Foundation.selector("copySignedClicked:"))) {
            return StringUtils.isNotBlank(((S3Path) this.getSelectedFile()).toSignedUrl().getUrl());
        }
        if(action.equals(Foundation.selector("copyAuthenticatedClicked:"))) {
            return StringUtils.isNotBlank(((S3Path) this.getSelectedFile()).toAuthenticatedUrl().getUrl());
        }
        return true;
    }
}