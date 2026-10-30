using java.util;
using NUnit.Framework;
using System.Collections.Generic;
using System.IO;

namespace Ch.Cyberduck.Core.Preferences;

[TestFixture]
public class ApplicationPreferencesTests
{
    [TestCase]
    public void LocaleDisplayNames()
    {
        TestPreferences preferences = new();
        preferences.load();

        foreach (string locale in preferences.applicationLocales())
        {
            Assert.That(preferences.getDisplayName(locale), Is.Not.Empty);
        }
    }

    [TestCase]
    public void PersistsEmptyValue()
    {
        TestPreferences preferences = new();
        preferences.setDefaults([
            new("test.empty.default", "DONT")
        ]);

        Assert.That("DONT", Is.EqualTo(preferences.getDefault("test.empty.default")));
        preferences.setProperty("test.empty.default", "");
        Assert.That("DONT", Is.EqualTo(preferences.getDefault("test.empty.default")));
        Assert.That("", Is.EqualTo(preferences.getProperty("test.empty.default")));
    }

    [TestCase]
    public void OverrideDefaultsDefault()
    {
        TestPreferences preferences = new();
        preferences.setDefaults([
            /* Mimick Cyberduck Defaults */
            new("test.core.override", "CORE"),
            /* Override in downstream Defaults */
            new("test.core.override", "")
        ]);

        Assert.That(preferences.getProperty("test.core.override"), Is.Empty);
    }

    [TestCase]
    public void CommonSupportDirectoryInProgramData()
    {
        // Initializes EnvironmentInfo.DataFolderName before the finder caches its location
        _ = new TestPreferences();
        Assert.That(CommonSupportDirectoryFinder.Local.getAbsolute(),
            Is.EqualTo(Path.Combine(EnvironmentInfo.CommonAppDataPath, EnvironmentInfo.DataFolderName)));
    }

    public class TestPreferences : ApplicationPreferences<TestPreferences>
    {
        public TestPreferences() : base(new DefaultLocales(), new PropertyStoreFactory<MemoryPropertyStore>())
        {
        }

        public void setDefaults(params KeyValuePair<string, string>[] testDefaults)
        {
            foreach (var item in testDefaults)
            {
                setDefault(item.Key, item.Value);
            }
        }
    }
}
