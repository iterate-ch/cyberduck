using java.nio.file;
using NUnit.Framework;
using System;
using System.Runtime.CompilerServices;
using System.Runtime.InteropServices;
using System.Text;
using Windows.Win32.System.Com;
using Windows.Win32.UI.Shell;
using Windows.Win32.UI.Shell.Common;
using static Windows.Win32.CorePInvoke;

namespace Ch.Cyberduck.Core.Local;

[TestFixture]
public class RevealServiceTests
{
    [Test]
    public unsafe void TestSHCreateItemFromPath([Values([false, true])] bool create, [Values([false, true])] bool validate)
    {
        string path = LongPath(1024);
        if (create)
        {
            Paths.get(path).toFile().mkdirs();
        }

        IBindCtx ctx = null;
        if (!validate)
        {
            CreateBindCtx(0, out ctx);
            fixed (char* property = STR_PARSE_PREFER_FOLDER_BROWSING)
            {
                ctx.RegisterObjectParam(property, new());
            }
            fixed (char* property = STR_PARSE_DONT_REQUIRE_VALIDATED_URLS)
            {
                ctx.RegisterObjectParam(property, new());
            }
        }

        try
        {
            SHCreateItemFromParsingName(path, ctx, typeof(IShellItem).GUID, out var ppv).ThrowOnFailure();
        }
        catch when (!create)
        {
            Assert.Pass("SHParseDisplayName won't find non-existent paths");
        }
    }

    [Test]
    public unsafe void TestSHParseDisplayName([Values([false, true])] bool create, [Values([false, true])] bool validate)
    {
        ITEMIDLIST* idlist = null;
        string path = LongPath(1024);
        if (create)
        {
            Paths.get(path).toFile().mkdirs();
        }

        IBindCtx ctx = null;
        if (!validate)
        {
            CreateBindCtx(0, out ctx);
            fixed (char* property = STR_PARSE_PREFER_FOLDER_BROWSING)
            {
                ctx.RegisterObjectParam(property, new());
            }
            fixed (char* property = STR_PARSE_DONT_REQUIRE_VALIDATED_URLS)
            {
                ctx.RegisterObjectParam(property, new());
            }
        }

        try
        {
            uint flags = 0;
            SHParseDisplayName(path, ctx, out idlist, flags, &flags).ThrowOnFailure();
            Assert.That(idlist is not null);
        }
        catch when (!create)
        {
            // Regardless of whether STR_PARSE_DONT_REQUIRE_VALIDATED_URLS is set
            Assert.Pass("SHParseDisplayName won't find non-existent paths");
        }
        finally
        {
            Marshal.FreeCoTaskMem((nint)idlist);
        }
    }

    private static string LongPath(int minimumLength, string pattern = null, [CallerMemberName] string testName = null)
    {
        pattern ??= System.IO.Path.GetRandomFileName();
        string basePath = $@"{TestContext.CurrentContext.WorkDirectory}\{testName}\";
        var startIndex = basePath.Length;
        minimumLength = Math.Max(minimumLength, startIndex + pattern.Length);
        minimumLength += (minimumLength - startIndex) % (pattern.Length + 2) - 1;

        StringBuilder builder = new(basePath)
        {
            Length = minimumLength
        };

        for (int i = basePath.Length, m = 0; i < minimumLength; i++)
        {
            if (m == pattern.Length)
            {
                m = 0;
                builder[i] = '\\';
            }
            else builder[i] = pattern[m++];
        }

        return builder.ToString();
    }
}
