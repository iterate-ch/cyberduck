using System.CommandLine;
using test_utils;
using Windows.Win32.Security.Credentials;

RootCommand command = [];

Command credDeleteCommand = new("cred-delete", "Lists Credentials, and optionally deletes them when matching.");
command.Add(credDeleteCommand);
credDeleteCommand.SetAction(CredDeleteCommand.Invoke);
credDeleteCommand.Add(CredMatch = new("--match")
{
    AllowMultipleArgumentsPerToken = true,
    Arity = ArgumentArity.ZeroOrMore,
    Description = "When specified, Cred-Delete will delete items matching any of the Regex patterns."
});


return command.Parse(args).Invoke();

partial class Program
{
    internal static Option<string[]> CredMatch;

    internal readonly unsafe struct PCREDENTIALW
    {
        private readonly CREDENTIALW* _ptr;

        public readonly ref CREDENTIALW Value => ref *_ptr;
    }
}
