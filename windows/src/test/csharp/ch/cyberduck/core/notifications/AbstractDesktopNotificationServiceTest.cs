using ch.cyberduck.core.notification;
using NUnit.Framework;
using System.Linq;

namespace Ch.Cyberduck.Ui.Controller
{
    [TestFixture]
    public class AbstractDesktopNotificationServiceTest
    {
        [Ignore("Doesn't work correctly automated."), Test]
        public void TestNotifyPermutation()
        {
            var valid = new[] { null, string.Empty, "Test" };
            NotificationService notificationService = new DesktopNotificationService();
            notificationService.setup();
            var variants = valid.SelectMany(s1 => valid.SelectMany(s2 => valid.SelectMany(s3 => valid.Select(s4 => new[] { s1, s2, s3, s4 }))));
            foreach (var item in variants)
            {
                Assert.DoesNotThrow(() =>
                {
                    notificationService.notify(item[0], item[1], item[2], item[3]);
                }, "Group: \"{0}\", Identifier: \"{1}\", Title: \"{2}\", Description: \"{3}\"", item);
            }
            notificationService.unregister();
        }
    }
}
