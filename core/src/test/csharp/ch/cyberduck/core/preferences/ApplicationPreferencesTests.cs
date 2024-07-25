using java.util;
using NUnit.Framework;

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

    public class TestPreferences : ApplicationPreferences<TestPreferences>
    {
        public TestPreferences() : base(new DefaultLocales(), new PropertyStoreFactory<MemoryPropertyStore>())
        {
        }
    }
}
