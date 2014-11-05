package ch.cyberduck.core;

/*
 * Copyright (c) 2002-2013 David Kocher. All rights reserved.
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
 * feedback@cyberduck.ch
 */

import ch.cyberduck.core.date.DefaultUserDateFormatter;
import ch.cyberduck.core.local.DefaultLocalTrashFeature;
import ch.cyberduck.core.local.DisabledApplicationBadgeLabeler;
import ch.cyberduck.core.local.DisabledApplicationLauncher;
import ch.cyberduck.core.local.DisabledIconService;
import ch.cyberduck.core.local.DisabledQuarantineService;
import ch.cyberduck.core.local.NullApplicationFinder;
import ch.cyberduck.core.local.NullFileDescriptor;
import ch.cyberduck.core.local.NullLocalSymlinkFeature;
import ch.cyberduck.core.serializer.impl.HostPlistReader;
import ch.cyberduck.core.serializer.impl.PlistDeserializer;
import ch.cyberduck.core.serializer.impl.PlistSerializer;
import ch.cyberduck.core.serializer.impl.PlistWriter;
import ch.cyberduck.core.serializer.impl.ProfilePlistReader;
import ch.cyberduck.core.serializer.impl.TransferPlistReader;
import ch.cyberduck.core.urlhandler.DisabledSchemeHandler;
import ch.cyberduck.ui.growl.DisabledNotificationService;
import ch.cyberduck.ui.resources.DisabledIconCache;

import org.apache.commons.lang3.StringUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @version $Id$
 */
public class MemoryPreferences extends Preferences {

    private Map<String, String> store;

    @Override
    public void setProperty(final String property, final String v) {
        store.put(property, v);
    }

    @Override
    public void setProperty(final String property, final List<String> values) {
        store.put(property, StringUtils.join(values, ","));
    }

    @Override
    public void deleteProperty(final String property) {
        store.remove(property);
    }

    @Override
    public String getProperty(final String property) {
        if(store.containsKey(property)) {
            return store.get(property);
        }
        return defaults.get(property);
    }

    @Override
    public void save() {
        //
    }

    @Override
    protected void setFactories() {
        super.setFactories();

        defaults.put("factory.local.class", Local.class.getName());

        defaults.put("factory.passwordstore.class", DisabledPasswordStore.class.getName());
        defaults.put("factory.certificatestore.class", DisabledCertificateStore.class.getName());
        defaults.put("factory.logincallback.class", DisabledLoginCallback.class.getName());
        defaults.put("factory.hostkeycallback.class", DisabledHostKeyCallback.class.getName());
        defaults.put("factory.proxy.class", DisabledProxyFinder.class.getName());
        defaults.put("factory.sleeppreventer.class", DisabledSleepPreventer.class.getName());

        defaults.put("factory.locale.class", DisabledLocale.class.getName());
        defaults.put("factory.iconcache.class", DisabledIconCache.class.getName());

        defaults.put("factory.serializer.class", PlistSerializer.class.getName());
        defaults.put("factory.deserializer.class", PlistDeserializer.class.getName());
        defaults.put("factory.reader.profile.class", ProfilePlistReader.class.getName());
        defaults.put("factory.writer.profile.class", PlistWriter.class.getName());
        defaults.put("factory.reader.transfer.class", TransferPlistReader.class.getName());
        defaults.put("factory.writer.transfer.class", PlistWriter.class.getName());
        defaults.put("factory.reader.host.class", HostPlistReader.class.getName());
        defaults.put("factory.writer.host.class", PlistWriter.class.getName());

        defaults.put("factory.dateformatter.class", DefaultUserDateFormatter.class.getName());
        defaults.put("factory.rendezvous.class", DisabledRendezvous.class.getName());
        defaults.put("factory.trash.class", DefaultLocalTrashFeature.class.getName());
        defaults.put("factory.quarantine.class", DisabledQuarantineService.class.getName());
        defaults.put("factory.symlink.class", NullLocalSymlinkFeature.class.getName());

        defaults.put("factory.terminalservice.class", DisabledTerminalService.class.getName());
        defaults.put("factory.applicationfinder.class", NullApplicationFinder.class.getName());
        defaults.put("factory.applicationlauncher.class", DisabledApplicationLauncher.class.getName());
        defaults.put("factory.badgelabeler.class", DisabledApplicationBadgeLabeler.class.getName());
        defaults.put("factory.filedescriptor.class", NullFileDescriptor.class.getName());
        defaults.put("factory.iconservice.class", DisabledIconService.class.getName());
        defaults.put("factory.notification.class", DisabledNotificationService.class.getName());
        defaults.put("factory.schemehandler.class", DisabledSchemeHandler.class.getName());
    }

    @Override
    protected void setDefaults() {
        super.setDefaults();

        final String workdir = defaults.get("tmp.dir");

        defaults.put("application.support.path", workdir);
        defaults.put("application.profiles.path", workdir);
        defaults.put("application.receipt.path", workdir);
        defaults.put("application.bookmarks.path", workdir);
        defaults.put("queue.download.folder", workdir);

        defaults.put("application.name", "Cyberduck");
        defaults.put("application.version", Version.getSpecification());
        defaults.put("application.revision", Version.getImplementation());
    }

    @Override
    protected void load() {
        store = new HashMap<String, String>();
    }

    @Override
    public List<String> applicationLocales() {
        return Collections.singletonList("en");
    }

    @Override
    public List<String> systemLocales() {
        return Collections.singletonList("en");
    }

    private static final class Version {
        /**
         * @return The <code>Specification-Version</code> in the JAR manifest.
         */
        public static String getSpecification() {
            Package pkg = Version.class.getPackage();
            return (pkg == null) ? null : pkg.getSpecificationVersion();
        }

        /**
         * @return The <code>Implementation-Version</code> in the JAR manifest.
         */
        public static String getImplementation() {
            Package pkg = Version.class.getPackage();
            return (pkg == null) ? null : pkg.getImplementationVersion();
        }

        /**
         * A simple main method that prints the version and exits
         */
        public static void main(String[] args) {
            System.out.println("Version: " + getSpecification());
            System.out.println("Implementation: " + getImplementation());
        }
    }
}
