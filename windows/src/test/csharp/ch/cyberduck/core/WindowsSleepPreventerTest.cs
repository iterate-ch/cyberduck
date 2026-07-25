using ch.cyberduck.core;
using Ch.Cyberduck.Core;
using NUnit.Framework;

namespace Cyberduck.Core.Test
{
    [TestFixture]
    public class WindowsSleepPreventerTest
    {
        [Test]
        public void TestLockAndRelease()
        {
            SleepPreventer preventer = new WindowsSleepPreventer();
            string first = preventer.@lock();
            string second = preventer.@lock();

            if (null != first && null != second)
            {
                Assert.That(first, Is.Not.EqualTo(second));
            }

            Assert.DoesNotThrow(() => preventer.release(second));
            Assert.DoesNotThrow(() => preventer.release(first));
            Assert.DoesNotThrow(() => preventer.release("unknown"));
            Assert.DoesNotThrow(() => preventer.release(null));
        }
    }
}
