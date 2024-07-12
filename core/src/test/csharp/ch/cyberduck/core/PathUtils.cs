using NUnit.Framework;
using System;
using System.Text;

namespace Ch.Cyberduck.Core;

public static class PathUtils
{
    public static string TestDir
    {
        get
        {
            var context = TestContext.CurrentContext;
            var test = context.Test;
            return $@"{context.WorkDirectory}\{test.ClassName.GetHashCode():X8}\{test.MethodName}";
        }
    }

    public static string LongPath(int minimumLength, string pattern = null)
    {
        pattern ??= System.IO.Path.GetRandomFileName();
        string basePath = $@"{TestDir}\";
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
