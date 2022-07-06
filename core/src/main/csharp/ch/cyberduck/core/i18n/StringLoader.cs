using System;
using System.Runtime.InteropServices;
using Windows.Win32;
using static Windows.Win32.CorePInvoke;

namespace Ch.Cyberduck.Core.I18n
{
    public class StringLoader : IDisposable
    {
        private readonly FreeLibrarySafeHandle shell32;
        private bool disposedValue;

        public StringLoader()
        {
            shell32 = LoadLibrary("shell32.dll");
        }

        ~StringLoader()
        {
            Dispose(disposing: false);
        }

        public void Dispose()
        {
            Dispose(disposing: true);
            GC.SuppressFinalize(this);
        }

        public string GetString(uint uiStringId)
        {
            int length = LoadString(shell32, uiStringId, out var lpBuffer);
            if (length == 0)
            {
                Marshal.ThrowExceptionForHR(Marshal.GetHRForLastWin32Error());
            }
            return lpBuffer.ToString(length);
        }

        protected virtual void Dispose(bool disposing)
        {
            if (!disposedValue)
            {
                shell32.Dispose();
                disposedValue = true;
            }
        }
    }
}
