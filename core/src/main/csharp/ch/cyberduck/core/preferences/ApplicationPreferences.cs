//
// Copyright (c) 2023 iterate GmbH. All rights reserved.
// https://cyberduck.io/
// 
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
// 
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//

using ch.cyberduck.core.i18n;
using ch.cyberduck.core.preferences;
using java.security;
using java.util;
using org.apache.commons.lang3;
using org.apache.logging.log4j;
using org.apache.logging.log4j.core.config;
using sun.security.mscapi;
using System;
using System.Diagnostics;
using System.Globalization;
using System.IO;
using System.Runtime.CompilerServices;
using Windows.Storage;
using JavaSystem = java.lang.System;

namespace Ch.Cyberduck.Core.Preferences;

public class ApplicationPreferences<T> : DefaultPreferences
{
    protected static readonly Logger Log = LogManager.getLogger(typeof(T).FullName);

    private static readonly char[] CultureSeparator = new[] { '_', '-' };
    private readonly Locales locales;
    private readonly IPropertyStore propertyStore;

    public ApplicationPreferences(Locales locales, IPropertyStoreFactory propertyStore)
    {
        this.locales = locales;
        SetEnvironmentInfo();
        this.propertyStore = propertyStore.New();

        JavaSystem.setProperty("jna.boot.library.path", AppContext.BaseDirectory);
    }

    public override List applicationLocales() => locales.applicationLocales();

    public override void deleteProperty(string property)
    {
        propertyStore.DeleteProperty(property);
    }

    public string GetDefaultLanguage()
    {
        List sysLocales = systemLocales();
        List appLocales = applicationLocales();
        for (int i = 0; i < sysLocales.size(); i++)
        {
            string s = (string)sysLocales.get(i);
            string match = TryToMatchLocale(s, appLocales);
            if (null != match)
            {
                if (Log.isDebugEnabled())
                {
                    Log.debug($"Default locale is '{match}' for system locale '{s}'");
                }

                return match;
            }
        }

        //default to english
        if (Log.isDebugEnabled())
        {
            Log.debug("Fallback to locale 'en'");
        }
        return "en";
    }

    public override string getDisplayName(string locale)
    {
        //cy is a special case as it is not available in the framework
        if ("cy".Equals(locale))
        {
            return "Welsh";
        }

        CultureInfo cultureInfo = CultureInfo.GetCultureInfo(locale.Replace('_', '-'));
        return cultureInfo.TextInfo.ToTitleCase(cultureInfo.NativeName);
    }

    public override string getProperty(string property)
    {
        if (propertyStore[property] is not { } value || string.IsNullOrWhiteSpace(value))
        {
            value = getDefault(property);
        }

        return value;
    }

    public override void load()
    {
        propertyStore.Load();
    }

    public override string locale()
    {
        return getProperty("application.language");
    }

    public override void save()
    {
        propertyStore.Save();
    }

    protected virtual void SetEnvironmentInfo()
    {
        EnvironmentInfo.AssemblyInfo<T>();
    }

    public override void setProperty(string property, string value)
    {
        propertyStore[property] = value;
    }

    public override List systemLocales() => locales.systemLocales();

    protected override void configureLogging(string level)
    {
        base.configureLogging(level);
        if (Debugger.IsAttached)
        {
            Configurator.setRootLevel(Level.DEBUG);
        }
    }

    protected override void setDefaults()
    {
        base.setDefaults();

        this.setDefault("os.version", Environment.OSVersion.Version.ToString());

        this.setDefault("oauth.handler.scheme",
            String.Format("x-{0}-action", StringUtils.deleteWhitespace(EnvironmentInfo.ProductName.ToLower())));

        this.setDefault("application.version", EnvironmentInfo.VersionString);
        this.setDefault("application.revision", EnvironmentInfo.Revision);
        this.setDefault("application.language.custom", bool.FalseString);
        this.setDefault("application.localization.enable", bool.TrueString);

        this.setDefault("editor.bundleIdentifier", "shell:openfilewith");

        this.setDefault("update.feed.release", "https://version.cyberduck.io/windows/changelog.rss");
        this.setDefault("update.feed.beta", "https://version.cyberduck.io/windows/beta/changelog.rss");
        this.setDefault("update.feed.nightly", "https://version.cyberduck.io/windows/nightly/changelog.rss");

        // Importers
        this.setDefault("bookmark.import.winscp.location",
            Path.Combine(EnvironmentInfo.AppDataPath, "WinSCP.ini"));
        this.setDefault("bookmark.import.filezilla.location",
            Path.Combine(EnvironmentInfo.AppDataPath, "FileZilla",
                "sitemanager.xml"));
        this.setDefault("bookmark.import.smartftp.location",
            Path.Combine(EnvironmentInfo.AppDataPath, "SmartFTP",
                "Client 2.0", "Favorites"));
        this.setDefault("bookmark.import.totalcommander.location",
            Path.Combine(EnvironmentInfo.AppDataPath, "GHISLER",
                "wcx_ftp.ini"));
        this.setDefault("bookmark.import.flashfxp3.location",
            Path.Combine(EnvironmentInfo.AppDataPath, "FlashFXP", "3",
                "Sites.dat"));
        this.setDefault("bookmark.import.flashfxp4.location",
            Path.Combine(EnvironmentInfo.AppDataPath, "FlashFXP", "4",
                "Sites.dat"));
        this.setDefault("bookmark.import.flashfxp4.common.location",
            Path.Combine(EnvironmentInfo.CommonAppDataPath, "FlashFXP",
                "4",
                "Sites.dat"));
        this.setDefault("bookmark.import.wsftp.location",
            Path.Combine(EnvironmentInfo.AppDataPath, "Ipswitch", "WS_FTP",
                "Sites"));
        this.setDefault("bookmark.import.fireftp.location",
            Path.Combine(EnvironmentInfo.AppDataPath, "Mozilla", "Firefox",
                "Profiles"));
        this.setDefault("bookmark.import.s3browser.location",
            Path.Combine(EnvironmentInfo.AppDataPath, "S3Browser",
                "settings.ini"));
        this.setDefault("bookmark.import.crossftp.location", Path.Combine(EnvironmentInfo.UserProfilePath, ".crossftp", "sites.xml"));
        this.setDefault("bookmark.import.cloudberry.s3.location",
            Path.Combine(EnvironmentInfo.LocalAppDataPath,
                "CloudBerry S3 Explorer for Amazon S3", "settings.list"));
        this.setDefault("bookmark.import.cloudberry.google.location",
            Path.Combine(EnvironmentInfo.LocalAppDataPath,
                "CloudBerry Explorer for Google Storage", "settings.list"));
        this.setDefault("bookmark.import.cloudberry.azure.location",
            Path.Combine(EnvironmentInfo.LocalAppDataPath,
                "CloudBerry Explorer for Azure Blob Storage", "settings.list"));
        this.setDefault("bookmark.import.expandrive3.location",
            Path.Combine(EnvironmentInfo.LocalAppDataPath,
                "ExpanDrive", "favorites.js"));
        this.setDefault("bookmark.import.expandrive4.location",
            Path.Combine(EnvironmentInfo.LocalAppDataPath,
                "ExpanDrive", "expandrive4.favorites.js"));
        this.setDefault("bookmark.import.expandrive5.location",
            Path.Combine(EnvironmentInfo.LocalAppDataPath,
                "ExpanDrive", "expandrive5.favorites.js"));
        this.setDefault("bookmark.import.expandrive6.location",
            Path.Combine(EnvironmentInfo.LocalAppDataPath,
                "ExpanDrive", "expandrive6.favorites.js"));
        this.setDefault("bookmark.import.netdrive2.location",
            Path.Combine(EnvironmentInfo.AppDataPath,
                "NetDrive2", "drives.dat"));

        //disable reminder for protocol handler registration
        this.setDefault("defaulthandler.reminder", bool.FalseString);

        this.setDefault("update.check.privilege", bool.TrueString);

        this.setDefault("queue.download.folder", EnvironmentInfo.DownloadsPath);
        this.setDefault("queue.upload.permissions.default", bool.TrueString);

        this.setDefault("queue.dock.badge", bool.TrueString);

        this.setDefault("ssh.knownhosts",
            Path.Combine(EnvironmentInfo.UserProfilePath, ".ssh", "known_hosts"));
        this.setDefault("browser.enterkey.rename", bool.FalseString);
        this.setDefault("terminal.openssh.enable", bool.TrueString);
        this.setDefault("terminal.windowssubsystemlinux.enable", bool.TrueString);
        this.setDefault("terminal.command.ssh", Path.Combine(EnvironmentInfo.UserProfilePath, "putty.exe"));
        this.setDefault("terminal.command.ssh.args", "-ssh {0} {1}@{2} -t -P {3} -m \"{4}\"");
        this.setDefault("terminal.command.openssh.args", "{1} {0}@{2} -t -p {3} \"cd '{4}'; $SHELL\"");

        this.setDefault("notifications.timeout.milliseconds", "300");

        //default browser toolbar set
        this.setDefault("browser.toolbar", bool.TrueString);
        this.setDefault("browser.toolbar.openconnection", bool.TrueString);
        this.setDefault("browser.toolbar.quickconnect", bool.TrueString);
        this.setDefault("browser.toolbar.action", bool.TrueString);
        this.setDefault("browser.toolbar.info", bool.TrueString);
        this.setDefault("browser.toolbar.refresh", bool.TrueString);
        this.setDefault("browser.toolbar.edit", bool.TrueString);
        this.setDefault("browser.toolbar.openinbrowser", bool.FalseString);
        this.setDefault("browser.toolbar.openinterminal", bool.FalseString);
        this.setDefault("browser.toolbar.newfolder", bool.FalseString);
        this.setDefault("browser.toolbar.delete", bool.FalseString);
        this.setDefault("browser.toolbar.download", bool.FalseString);
        this.setDefault("browser.toolbar.upload", bool.TrueString);
        this.setDefault("browser.toolbar.transfers", bool.TrueString);

        //default transfer toolbar set
        this.setDefault("transfer.toolbar.resume", bool.TrueString);
        this.setDefault("transfer.toolbar.reload", bool.TrueString);
        this.setDefault("transfer.toolbar.stop", bool.TrueString);
        this.setDefault("transfer.toolbar.remove", bool.TrueString);
        this.setDefault("transfer.toolbar.cleanup", bool.FalseString);
        this.setDefault("transfer.toolbar.log", bool.FalseString);
        this.setDefault("transfer.toolbar.open", bool.TrueString);
        this.setDefault("transfer.toolbar.show", bool.TrueString);

        // Resolve symbolic links downloading target file instead. Cannot create symbolic links on FAT.
        this.setDefault("path.symboliclink.resolve", bool.TrueString);
        // Resolve local links uploading target file instead. Currently not supporting shortcuts on Windows.
        this.setDefault("local.symboliclink.resolve", bool.TrueString);

        this.setDefault("local.user.home", EnvironmentInfo.UserProfilePath);
        this.setDefault("local.delimiter", $"{Path.DirectorySeparatorChar}");
        this.setDefault("local.normalize.tilde", bool.FalseString);
        this.setDefault("local.normalize.unicode", bool.FalseString);

        // SSL Keystore
        this.setDefault("connection.ssl.keystore.type", "Windows-MY");
        this.setDefault("connection.ssl.keystore.provider", "SunMSCAPI");

        // Override secure random strong algorithm. Outputs bytes from the Windows CryptGenRandom() API
        this.setDefault("connection.ssl.securerandom.algorithm", "Windows-PRNG");
        this.setDefault("connection.ssl.securerandom.provider", "SunMSCAPI");

        // Enable Integrated Windows Authentication
        this.setDefault("connection.proxy.windows.authentication.enable", bool.TrueString);

        if (getBoolean("webdav.ntlm.environment"))
        {
            // NTLM Windows Domain
            try
            {
                // Gets the network domain name associated with the current user
                this.setDefault("webdav.ntlm.domain", Environment.UserDomainName);
            }
            catch (PlatformNotSupportedException)
            {
                // The operating system does not support retrieving the network domain name.
            }
            catch (InvalidOperationException)
            {
                // The network domain name cannot be retrieved.
            }

            try
            {
                this.setDefault("webdav.ntlm.workstation", Environment.MachineName);
            }
            catch (InvalidOperationException)
            {
                // The name of this computer cannot be obtained.
            }
        }

        if (EnvironmentInfo.Packaged)
        {
            SetUWPDefaults();
        }
    }

    protected override void setFactories()
    {
        base.setFactories();

        setDefault("factory.local.class", typeof(ch.cyberduck.core.Local).FullName);
    }

    private static string TryToMatchLocale(string sysLocale, List appLocales)
    {
        for (int i = 0; i < appLocales.size(); i++)
        {
            string l = (string)appLocales.get(i);
            if (l.Equals(sysLocale))
            {
                //direct match
                return l;
            }
            //remove region
            int m = sysLocale.IndexOfAny(CultureSeparator);
            if (m > 0)
            {
                string country = sysLocale.Substring(0, m);
                if (l.Equals(country))
                {
                    return l;
                }
            }
        }
        return null;
    }

    [MethodImpl(MethodImplOptions.NoInlining)]
    private void SetUWPDefaults()
    {
        this.setDefault("update.check", $"{false}");
        this.setDefault("tmp.dir", ApplicationData.Current.TemporaryFolder.Path);
    }
}
