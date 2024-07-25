using NUnit.Framework;
using System;

[SetUpFixture]
public class JavaSetup
{
    [OneTimeSetUp]
    public void SetupJna()
    {
        java.lang.System.setProperty("jna.boot.library.path", AppContext.BaseDirectory);
    }
}
