using System;
using System.CommandLine;
using System.Linq;
using System.Runtime.InteropServices;
using System.Text.RegularExpressions;
using Windows.Win32;
using Windows.Win32.Security.Credentials;
using static Program;

namespace test_utils;

internal static class CredDeleteCommand
{
    internal static unsafe void Invoke(ParseResult result)
    {
        var patternValues = result.GetValue(Program.CredMatch);
        Regex[] patterns = null;
        if (patternValues is not null)
        {
            patterns = new Regex[patternValues.Length];
            var patternWriter = ((Span<Regex>)patterns).GetEnumerator();
            foreach (var item in patternValues)
            {
                patternWriter.MoveNext();
                patternWriter.Current = new Regex(item, RegexOptions.Compiled);
            }
        }

        CREDENTIALW** credentials = null;
        try
        {
            if (!PInvoke.CredEnumerate(null, out var count, out credentials))
            {
                throw Marshal.GetExceptionForHR(Marshal.GetHRForLastWin32Error());
            }

            foreach (ref readonly var credential in new ReadOnlySpan<PCREDENTIALW>(credentials, (int)count))
            {
                bool? matched;
                if ((matched = patterns?.Any(credential.Value.TargetName.ToString().Match)) is false)
                {
                    continue;
                }

                Console.WriteLine($"{credential.Value.TargetName} ({credential.Value.UserName})");
                if (matched is true)
                {
                    if (!PInvoke.CredDelete(credential.Value.TargetName, credential.Value.Type, 0))
                    {
                        Console.WriteLine($"  Failure deleting: {Marshal.GetLastPInvokeErrorMessage()}");
                    }
                }
            }
        }
        finally
        {
            PInvoke.CredFree(credentials);
        }
    }

    private static bool Match(this string text, Regex pattern) => pattern.IsMatch(text);
}
