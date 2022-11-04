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
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Scheme;
import ch.cyberduck.core.resources.IconCacheFactory;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.rococoa.Foundation;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class NotificationCenter extends ProxyController implements NotificationService, NSUserNotificationCenter.Delegate {
    private static final Logger log = LogManager.getLogger(NotificationCenter.class);

    /**
     * Methods involve talking to a server process, so calling them repeatedly can have a negative effect on
     * performance.
     */
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
        if(filter.shouldSuppress()) {
            if(log.isWarnEnabled()) {
                log.warn(String.format("Suppressing notification for %s, %s, %s, %s", group, identifier, title, description));
            }
            return;
        }
        final NSUserNotification notification = NSUserNotification.notification();
        if(StringUtils.isNotBlank(identifier)) {
            if(notification.respondsToSelector(Foundation.selector("setIdentifier:"))) {
                notification.setIdentifier(identifier);
            }
            if(StringUtils.isNotBlank(Path.getExtension(identifier))) {
                notification.setContentImage(IconCacheFactory.<NSImage>get().documentIcon(Path.getExtension(identifier), 32));
            }
        }
        notification.setTitle(LocaleFactory.localizedString(title, "Status"));
        notification.setInformativeText(description);
        notification.setHasActionButton(false);
        if(log.isDebugEnabled()) {
            log.debug(String.format("Schedule notification %s", notification));
        }
        center.scheduleNotification(notification);
    }

    @Override
    public void notify(final String group, final String identifier, final String title, final String description, final String action) {
        if(filter.shouldSuppress()) {
            if(log.isWarnEnabled()) {
                log.warn(String.format("Suppressing notification for %s, %s, %s, %s", group, identifier, title, description));
            }
            return;
        }
        final NSUserNotification notification = NSUserNotification.notification();
        if(StringUtils.isNotBlank(identifier)) {
            if(notification.respondsToSelector(Foundation.selector("setIdentifier:"))) {
                notification.setIdentifier(identifier);
            }
            if(!Scheme.isURL(identifier)) {
                if(StringUtils.isNotBlank(Path.getExtension(identifier))) {
                    notification.setContentImage(IconCacheFactory.<NSImage>get().documentIcon(Path.getExtension(identifier), 32));
                }
            }
        }
        notification.setTitle(LocaleFactory.localizedString(title, "Status"));
        notification.setInformativeText(description);
        notification.setHasActionButton(true);
        notification.setActionButtonTitle(action);
        if(log.isDebugEnabled()) {
            log.debug(String.format("Schedule notification %s", notification));
        }
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

    @Override
    public boolean userNotificationCenter_shouldPresentNotification(final NSUserNotificationCenter center, final NSUserNotification notification) {
        log.warn(String.format("Discarded notification %s without presenting", notification));
        return false;
    }
}
