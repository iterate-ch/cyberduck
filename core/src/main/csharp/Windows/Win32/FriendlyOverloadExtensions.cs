#pragma warning disable IDE0001,IDE0002,IDE0005,CS1591,CS1573,CS0465,CS0649,CS8019,CS1570,CS1584,CS1658,CS0436

namespace Windows.Win32
{
    using global::System;
    using global::System.Diagnostics;
    using global::System.Runtime.CompilerServices;
    using global::System.Runtime.InteropServices;
    using winmdroot = global::Windows.Win32;

    public static partial class FriendlyOverloadExtensions
    {
        /// <inheritdoc cref="winmdroot.UI.Shell.IShellItem.BindToHandler(winmdroot.System.Com.IBindCtx, global::System.Guid*, global::System.Guid*, out object)"/>
		public static unsafe void BindToHandler<T>(this winmdroot.UI.Shell.IShellItem @this, winmdroot.System.Com.IBindCtx pbc, in global::System.Guid bhid, out T ppv)
        {
            global::System.Guid riid = typeof(T).GUID;
            fixed (global::System.Guid* bhidLocal = &bhid)
            {
                @this.BindToHandler(pbc, bhidLocal, &riid, out object ppvLocal);
                ppv = (T)ppvLocal;
            }
        }

        /// <inheritdoc cref="winmdroot.UI.Shell.IExtractIconW.Extract(winmdroot.Foundation.PCWSTR, uint, winmdroot.UI.WindowsAndMessaging.HICON*, winmdroot.UI.WindowsAndMessaging.HICON*, uint)"/>
		public static unsafe void Extract(this winmdroot.UI.Shell.IExtractIconW @this, string pszFile, uint nIconIndex, in winmdroot.UI.WindowsAndMessaging.HICON phiconLarge, in winmdroot.UI.WindowsAndMessaging.HICON phiconSmall, uint nIconSize)
        {
            fixed (char* pszFileLocal = pszFile)
            {
                @this.Extract(pszFileLocal, nIconIndex, (winmdroot.UI.WindowsAndMessaging.HICON*)phiconLarge.Value, (winmdroot.UI.WindowsAndMessaging.HICON*)phiconSmall.Value, nIconSize);
            }
        }

        /// <inheritdoc cref="winmdroot.UI.Shell.IAssocHandler.GetIconLocation(Foundation.PWSTR*, int*)"/>
		public static unsafe string GetIconLocation(this winmdroot.UI.Shell.IAssocHandler @this, out int pIndex)
        {
            fixed (int* pIndexLocal = &pIndex)
            {
                winmdroot.Foundation.PWSTR ppszLocal = new();
                @this.GetIconLocation(&ppszLocal, pIndexLocal);
                return ppszLocal.ToString();
            }
        }

        /// <inheritdoc cref="winmdroot.UI.Shell.IAssocHandler.GetName(winmdroot.Foundation.PWSTR*)"/>
		public static unsafe string GetName(this winmdroot.UI.Shell.IAssocHandler @this)
        {
            winmdroot.Foundation.PWSTR ppszLocal = new();
            @this.GetName(&ppszLocal);
            return ppszLocal.ToString();
        }

        /// <inheritdoc cref="winmdroot.UI.Shell.IQueryAssociations.GetString(uint, UI.Shell.ASSOCSTR, Foundation.PCWSTR, Foundation.PWSTR, uint*)"
        public static unsafe bool GetString(this winmdroot.UI.Shell.IQueryAssociations @this, winmdroot.UI.Shell.ASSOCSTR str, string pszExtra, out string pszOut)
        {
            var pool = global::System.Buffers.ArrayPool<char>.Shared;
            uint length = 0;
            try
            {
                @this.GetString(winmdroot.CorePInvoke.ASSOCF_NOTRUNCATE, str, pszExtra, default, ref length);
                char[] buffer = null;
                try
                {
                    buffer = pool.Rent((int)length);
                    length = (uint)buffer.Length;
                    fixed (char* bufferLocal = buffer)
                    {
                        @this.GetString(winmdroot.CorePInvoke.ASSOCF_NOTRUNCATE, str, pszExtra, bufferLocal, ref length);

                        pszOut = ((winmdroot.Foundation.PCWSTR)bufferLocal).ToString();
                        return true;
                    }
                }
                finally
                {
                    pool.Return(buffer);
                }
            }
            catch { }
            pszOut = default;
            return false;
        }

        /// <inheritdoc cref="winmdroot.UI.Shell.IAssocHandler.GetUIName(winmdroot.Foundation.PWSTR*)"/>
		public static unsafe string GetUIName(this winmdroot.UI.Shell.IAssocHandler @this)
        {
            winmdroot.Foundation.PWSTR ppszLocal = new();
            @this.GetUIName(&ppszLocal);
            return ppszLocal.ToString();
        }

        /// <inheritdoc cref="winmdroot.UI.Shell.IEnumAssocHandlers.Next(uint, winmdroot.UI.Shell.IAssocHandler[], uint*)"/>
		public static unsafe int Next(this winmdroot.UI.Shell.IEnumAssocHandlers @this, winmdroot.UI.Shell.IAssocHandler[] rgelt)
        {
            uint pceltFetched = 0;
            @this.Next((uint)rgelt.Length, rgelt, &pceltFetched);
            return (int)pceltFetched;
        }
    }
}
