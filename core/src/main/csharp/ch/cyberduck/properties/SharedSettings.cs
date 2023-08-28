using Ch.Cyberduck.Core.Preferences;
using System.Configuration;

namespace Ch.Cyberduck.Properties
{
    [SettingsProvider(typeof(LocalSharedFileSettingsProvider))]
    internal sealed partial class SharedSettings
    {
        private SettingsPropertyValue cdSettings;

        public bool CdSettingsDirty
        {
            get => CdSettingsPropertyValue.IsDirty;
            set => CdSettingsPropertyValue.IsDirty = value;
        }

        private SettingsPropertyValue CdSettingsPropertyValue => cdSettings ??= PropertyValues[nameof(CdSettings)];
    }
}
