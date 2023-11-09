using java.util;
using NUnit.Framework;
using System.Configuration;

namespace Ch.Cyberduck.Core.Preferences;

[TestFixture]
public class ApplicationPreferencesTests
{
    [TestCase]
    public void LoadPreferences()
    {
        TestPreferences preferences = new();
        preferences.SetUserSettings(new SettingsDictionary());
        preferences.load();
    }

    [TestCase]
    public void NullOldPreferences()
    {
        TestPreferences preferences = new();
        preferences.SetUserSettings(null);
        preferences.load();
    }

    [TestCase]
    public void LocaleDisplayNames()
    {
        TestPreferences preferences = new();
        preferences.load();

        foreach (string locale in preferences.applicationLocales())
        {
            Assert.IsNotEmpty(preferences.getDisplayName(locale));
        }
    }

    public class TestPreferences : ApplicationPreferences
    {
        public TestPreferences() : this(new TestRuntime())
        {
        }

        public TestPreferences(IRuntime runtime) : base(new DefaultLocales(), runtime)
        {
        }

        public void SetUserSettings(SettingsDictionary userSettings)
        {
            UpgradeUserSettings();
            User = userSettings;
        }

        protected override void OnUpgradeUserSettings()
        {
            // nop
        }

        protected override void Save(ApplicationSettingsBase settings)
        {
            // Dont want to persist any test preferences
            foreach (SettingsPropertyValue property in settings.PropertyValues)
            {
                property.IsDirty = false;
            }
        }

        public record class TestRuntime(
            string CompanyName,
            string Location,
            bool Packaged,
            string ProductName,
            string ResourcesLocation,
            System.Version Version) : IRuntime
        {
            public string CompanyName { get; } = CompanyName;

            public string DataFolderName { get; } = "Cyberduck";

            public string Location { get; } = Location;

            public string ProductName { get; } = ProductName;

            public string ResourcesLocation { get; } = ResourcesLocation;

            public bool Packaged { get; } = Packaged;

            public string Revision { get; } = Version.Revision.ToString();

            public System.Version Version { get; } = Version;

            public string VersionString { get; } = Version.ToString(3);

            public TestRuntime() : this(Runtime.CreateDefault<TestRuntime>())
            {
            }

            public TestRuntime(in Runtime.ValueRuntime runtime)
                : this(
                    runtime.CompanyName,
                    runtime.Location,
                    runtime.Packaged,
                    runtime.ProductName,
                    runtime.ResourcesLocation,
                    runtime.Version)
            {
            }
        }
    }
}
