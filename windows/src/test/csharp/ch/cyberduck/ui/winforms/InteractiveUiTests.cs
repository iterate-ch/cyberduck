using NUnit.Framework;
using System.Collections.Generic;
using System.Windows.Forms;

namespace Ch.Cyberduck.Ui.Winforms
{
    [Explicit("Interactive UI tests, run with user validating graphics.")]
    [TestFixture]
    public class InteractiveUiTests
    {
        [OneTimeSetUp]
        public static void Setup()
        {
            Application.EnableVisualStyles();
            Application.SetCompatibleTextRenderingDefault(false);
        }

        [Test]
        public void TestCreateFilePrompt()
        {
            Application.Run(new CreateFilePromptForm());
        }

        [Test]
        public void TestCreateSymlinkPrompt()
        {
            Application.Run(new CreateSymlinkPromptForm());
        }

        [Test]
        public void TestDuplicateFilePrompt()
        {
            Application.Run(new DuplicateFilePromptForm());
        }

        [Test]
        public void TestGoToPromptForm()
        {
            Application.Run(new GotoPromptForm());
        }

        [Test]
        public void TestNewFolderPrompt()
        {
            Application.Run(new NewFolderPromptForm());
        }

        [Test]
        public void TestNewFolderRegionPrompt()
        {
            Application.Run(WithRegions(new NewFolderPromptForm()));
        }

        [Test]
        public void TestNewVaultPrompt()
        {
            var prompt = new NewVaultPromptForm();
            prompt.EnablePassphrase();
            Application.Run(prompt);
        }

        [Test]
        public void TestNewVaultBeforeRegionPrompt()
        {
            var prompt = new NewVaultPromptForm();
            prompt.EnablePassphrase();
            Application.Run(WithRegions(prompt));
        }

        [Test]
        public void TestNewVaultAfterRegionPrompt()
        {
            var prompt = WithRegions(new NewVaultPromptForm());
            prompt.EnablePassphrase();
            Application.Run(prompt);
        }

        [Test]
        public void TestPasswordForm()
        {
            Application.Run(new PasswordForm());
        }

        [Test]
        public void Test14381()
        {
            // Tests https://github.com/iterate-ch/cyberduck/issues/14381
            Application.Run(new PasswordForm()
            {
                Reason = """
                Lorem ipsum dolor sit amet,
                consetetur sadipscing elitr,
                sed diam nonumy eirmod tempor 
                invidunt ut labore et dolore 
                magna aliquyam erat, 
                sed diam voluptua. 
                At vero eos et accusam et justo
                duo dolores et ea rebum.
                Stet clita kasd gubergren,
                no sea takimata sanctus est
                Lorem ipsum dolor sit amet.
                """
            });
        }

        private static T WithRegions<T>(T @this) where T : NewFolderPromptForm
        {
            @this.RegionsEnabled = true;
            @this.Region = "REGION";
            @this.PopulateRegions(new[] { new KeyValuePair<string, string>("REGION", "Test Region") });
            return @this;
        }
    }
}
