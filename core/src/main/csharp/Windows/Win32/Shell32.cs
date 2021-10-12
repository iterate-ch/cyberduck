using Windows.Win32.Foundation;
using Windows.Win32.Storage.FileSystem;
using Windows.Win32.System.Com;
using Windows.Win32.UI.Shell;
using static System.Runtime.CompilerServices.Unsafe;

namespace Windows.Win32
{
    partial class CorePInvoke
    {
        /// <inheritdoc cref="ILCreateFromPath(PCWSTR)"/>
        public static unsafe ref ITEMIDLIST ILCreateFromPath2(string pszPath)
        {
            fixed (char* pszPathLocal = pszPath)
            {
                ITEMIDLIST* __result = ILCreateFromPath(pszPathLocal);
                return ref *__result;
            }
        }

        /// <inheritdoc cref="SHGetFileInfo(PCWSTR, FILE_FLAGS_AND_ATTRIBUTES, SHFILEINFOW*, uint, SHGFI_FLAGS)"/>
        public static unsafe nuint SHGetFileInfo(string pszPath, FILE_FLAGS_AND_ATTRIBUTES dwFileAttributes, in SHFILEINFOW sfi, SHGFI_FLAGS uFlags)
        {
            var psfi = (SHFILEINFOW*)AsPointer(ref AsRef(sfi));
            fixed (char* pszPathLocal = pszPath)
            {
                return SHGetFileInfo(pszPathLocal, dwFileAttributes, psfi, (uint)SizeOf<SHFILEINFOW>(), uFlags);
            }
        }

        /// <inheritdoc cref="SHParseDisplayName(string, IBindCtx, out ITEMIDLIST*, uint, uint*)"/>
        public static unsafe HRESULT SHParseDisplayName(string pszName, IBindCtx pbc, out ITEMIDLIST ppidl, uint sfgaoIn, out uint psfgaOut)
        {
            fixed (ITEMIDLIST* ppidlLocal = &ppidl)
            fixed (uint* psfgaOutLocal = &psfgaOut)
            fixed (char* pszNameLocal = pszName)
            {
                return SHParseDisplayName(pszNameLocal, pbc, (ITEMIDLIST**)ppidlLocal, sfgaoIn, psfgaOutLocal);
            }
        }
    }
}
