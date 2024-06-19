using ch.cyberduck.core.preferences;
using Ch.Cyberduck.Core.Preferences;
using org.apache.logging.log4j;
using System;
using System.IO;
using Windows.Win32.UI.Shell;
using static Windows.Win32.CorePInvoke;
using static Windows.Win32.PInvoke;

namespace Ch.Cyberduck.Core.Ui.Preferences;

internal static class PackagedDataMigrator
{
    public static void Migrate()
    {
        // Logger is used for short time.
        // Don't need to keep an instance alive longer than necessary
        var logger = LogManager.getLogger(typeof(PackagedDataMigrator).FullName);
        var preferences = PreferencesFactory.get();
        if (preferences.getBoolean("application.datadir.migrate"))
        {
            if (logger.isDebugEnabled())
            {
                logger.debug("Skipping DataDir migration");
            }

            return;
        }

        if (FindPackagedPath(logger) is { } path && !string.IsNullOrWhiteSpace(path))
        {
            var targetPath = RoamingSupportDirectoryFinder.Local.getAbsolute();
            foreach (var item in new[] { "Bookmarks", "History", "Profiles", "Sessions", "Transfers" })
            {
                DirectoryInfo directory = new(Path.Combine(path, item));
                if (!directory.Exists)
                {
                    continue;
                }

                var copyTarget = Path.Combine(targetPath, item);
                foreach (var file in directory.EnumerateFiles())
                {
                    FileInfo targetFile = new(Path.Combine(copyTarget, file.Name));
                    if (targetFile.Exists && targetFile.LastWriteTime >= file.LastWriteTime)
                    {
                        if (logger.isInfoEnabled())
                        {
                            logger.info($"Skipping {item}/{file.Name} - Exists and target is newer");
                        }

                        continue;
                    }

                    try
                    {
                        file.CopyTo(targetFile.FullName, true);
                    }
                    catch (Exception e)
                    {
                        logger.warn($"Failure migrating {item}/{file.Name}", e);
                    }
                }
            }
        }

        preferences.setProperty("application.datadir.migrate", true);
    }

    private static string FindPackagedPath(Logger logger)
    {
        string path;
        try
        {
            const KNOWN_FOLDER_FLAG FLAGS = KNOWN_FOLDER_FLAG.KF_FLAG_RETURN_FILTER_REDIRECTION_TARGET | KNOWN_FOLDER_FLAG.KF_FLAG_DONT_VERIFY;
            path = SHGetKnownFolderPath(FOLDERID_RoamingAppData, FLAGS, default);
        }
        catch (Exception e)
        {
            logger.warn("Failure getting RoamingAppData packaged folder path", e);
            return null;
        }

        path = Path.Combine(path, EnvironmentInfo.DataFolderName);
        if (!Directory.Exists(path))
        {
            if (logger.isDebugEnabled())
            {
                logger.debug("Skipping migration for non-existent data folder");
            }

            return null;
        }

        return path;
    }
}
