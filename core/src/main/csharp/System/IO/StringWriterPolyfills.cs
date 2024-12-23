#if NETFRAMEWORK

namespace System.IO;

public static class StringWriterPolyfills
{
    public static void Write(this StringWriter writer, in ReadOnlySpan<char> chars)
    {
        foreach (ref readonly var c in chars)
        {
            writer.Write(c);
        }
    }
}

#endif
