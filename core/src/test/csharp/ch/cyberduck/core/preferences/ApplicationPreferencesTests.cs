using java.util;
using NUnit.Framework;
using System.Collections.Generic;

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
            Assert.IsNotEmpty(preferences.getDisplayName(locale));
        }
    }

    [TestCase]
    public void PersistsEmptyValue()
    {
        TestPreferences preferences = new();
        preferences.setDefaults([
            new("test.empty.default", "DONT")
        ]);

        Assert.AreEqual("DONT", preferences.getDefault("test.empty.default"));
        preferences.setProperty("test.empty.default", "");
        Assert.AreEqual("DONT", preferences.getDefault("test.empty.default"));
        Assert.AreEqual("", preferences.getProperty("test.empty.default"));
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

        Assert.IsEmpty(preferences.getProperty("test.core.override"));
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
