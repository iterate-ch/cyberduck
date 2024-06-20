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

using Ch.Cyberduck.Properties;
using org.apache.logging.log4j;
using System;
using System.Collections;
using System.Collections.Specialized;
using System.Configuration;

namespace Ch.Cyberduck.Core.Preferences;

public class ApplicationSettingsPropertyStore : IPropertyStore
{
    private static readonly Logger Log = LogManager.getLogger(typeof(ApplicationSettingsPropertyStore).FullName);

    private SettingsDictionary Shared
    {
        get => SharedSettings.CdSettings;
        set => SharedSettings.CdSettings = value;
    }

    private SharedSettings SharedSettings { get; } = SharedSettings.Default;

    private SettingsDictionary User
    {
        get => UserSettings.CdSettings;
        set => UserSettings.CdSettings = value;
    }

    private Settings UserSettings { get; } = Settings.Default;

    public string this[string property]
    {
        get => Shared[property];
        set
        {
            Shared[property] = value;
            SharedSettings.CdSettingsDirty = true;
        }
    }

    public void DeleteProperty(string property)
    {
        Shared.Remove(property);
    }

    public void Load()
    {
        Shared ??= [];

        MigrateConfig();
    }

    public void Save()
    {
        Save(SharedSettings);
    }

    protected virtual void OnUpgradeUserSettings()
    {
        try
        {
            UserSettings.Upgrade();
            Save(UserSettings);
        }
        catch
        {
            // Don't care about failures saving old user config.
        }
    }

    protected virtual void Save(ApplicationSettingsBase settings)
    {
        try
        {
            settings.Save();
        }
        catch (Exception ex)
        {
            Log.error("Failure saving preferences", ex);
        }
    }

    protected void UpgradeUserSettings()
    {
        if (!UserSettings.UpgradeSettings)
        {
            return;
        }

        UserSettings.UpgradeSettings = false;
        OnUpgradeUserSettings();
    }

    private StringDictionary LoadUserConfig()
    {
        UpgradeUserSettings();
        return User;
    }

    private void MigrateConfig()
    {
        if (!SharedSettings.Migrate)
        {
            return;
        }

        SharedSettings.Migrate = false;
        StringDictionary userConfig = default;
        try
        {
            userConfig = LoadUserConfig();
        }
        catch
        {
            // ignore
        }

        if (userConfig is null or { Count: 0 })
        {
            return;
        }

        SharedSettings.CdSettingsDirty = true;

        foreach (DictionaryEntry item in userConfig)
        {
            this[(string)item.Key] = (string)item.Value;
        }

        Save();
    }
}
