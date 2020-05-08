package ch.cyberduck.core.notification;

/*
 * Copyright (c) 2002-2014 David Kocher. All rights reserved.
 * http://cyberduck.io/
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
 * feedback@cyberduck.io
 */

import ch.cyberduck.binding.ProxyController;
import ch.cyberduck.binding.application.NSImage;
import ch.cyberduck.binding.foundation.NSUserNotification;
import ch.cyberduck.binding.foundation.NSUserNotificationCenter;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.resources.IconCacheFactory;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.rococoa.Foundation;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class NotificationCenter extends ProxyController implements NotificationService, NSUserNotificationCenter.Delegate {
    private static final Logger log = Logger.getLogger(NotificationCenter.class);

    private final NSUserNotificationCenter center
        = NSUserNotificationCenter.defaultUserNotificationCenter();

    private final Set<NotificationService.Listener> listeners =
        Collections.synchronizedSet(new HashSet<NotificationService.Listener>());

    private final NotificationFilterService filter
        = NotificationFilterService.Factory.get();

    @Override
    public NotificationService setup() {
        center.setDelegate(this.id());
        return this;
    }

    @Override
    public void unregister() {
        if(center.respondsToSelector(Foundation.selector("removeAllDeliveredNotifications"))) {
            center.removeAllDeliveredNotifications();
        }
        listeners.clear();
    }

    @Override
    public void addListener(final Listener listener) {
        listeners.add(listener);
    }

    @Override
    public void notify(final String group, final String identifier, final String title, final String description) {
        if (filter.shouldSuppress()) {
            return;
        }
        final NSUserNotification notification = NSUserNotification.notification();
        if(StringUtils.isNotBlank(identifier)) {
            if(notification.respondsToSelector(Foundation.selector("setIdentifier:"))) {
                notification.setIdentifier(identifier);
            }
            if(StringUtils.isNotBlank(FilenameUtils.getExtension(identifier))) {
                notification.setContentImage(IconCacheFactory.<NSImage>get().documentIcon(FilenameUtils.getExtension(identifier), 32));
            }
        }
        notification.setTitle(LocaleFactory.localizedString(title, "Status"));
        notification.setInformativeText(description);
        notification.setHasActionButton(false);
        center.scheduleNotification(notification);
    }

    @Override
    public void notify(final String group, final String identifier, final String title, final String description, final String action) {
        if (filter.shouldSuppress()) {
            return;
        }
        final NSUserNotification notification = NSUserNotification.notification();
        if(StringUtils.isNotBlank(identifier)) {
            if(notification.respondsToSelector(Foundation.selector("setIdentifier:"))) {
                notification.setIdentifier(identifier);
            }
            if(StringUtils.isNotBlank(FilenameUtils.getExtension(identifier))) {
                notification.setContentImage(IconCacheFactory.<NSImage>get().documentIcon(FilenameUtils.getExtension(identifier), 32));
            }
        }
        notification.setTitle(LocaleFactory.localizedString(title, "Status"));
        notification.setInformativeText(description);
        notification.setHasActionButton(true);
        notification.setActionButtonTitle(action);
        center.scheduleNotification(notification);
    }

    @Override
    public void userNotificationCenter_didActivateNotification(final NSUserNotificationCenter center, final NSUserNotification notification) {
        if(log.isDebugEnabled()) {
            log.debug(String.format("Did close notification %s with type %s", notification, notification.activationType()));
        }
        for(Listener listener : listeners) {
            listener.callback(notification.identifier());
        }
    }
}
