using ch.cyberduck.core.cache;
using ch.cyberduck.core.local;
using java.util;
using org.apache.logging.log4j;
using System;
using System.Buffers;
using System.Collections.Generic;
using System.IO;
using System.Runtime.InteropServices;
using Windows.Win32;
using Windows.Win32.Foundation;
using Windows.Win32.UI.Shell;
using static Windows.Win32.CorePInvoke;
using static Windows.Win32.UI.Shell.ASSOC_FILTER;
using static Windows.Win32.UI.Shell.ASSOCIATIONTYPE;
using static Windows.Win32.UI.Shell.ASSOCIATIONLEVEL;
using static Windows.Win32.UI.Shell.ASSOCSTR;

namespace Ch.Cyberduck.Core.Local
{
    public class ShellApplicationFinder : ApplicationFinder
    {
        private static readonly Logger Log = LogManager.getLogger(typeof(ShellApplicationFinder).FullName);

        private readonly LRUCache assocHandlerCache = LRUCache.build(25);
        private readonly LRUCache assocHandlerListCache = LRUCache.build(25);

        /// <summary>
        /// Finds default associated application.
        /// </summary>
        /// <param name="filename"></param>
        /// <returns></returns>
        public unsafe Application find(string filename)
        {
            filename = Path.GetExtension(filename);
            if (assocHandlerCache.get(filename) is ShellApplication shellHandler)
            {
                return shellHandler;
            }

            if (SHCreateAssociationRegistration(out IApplicationAssociationRegistration reg).Failed)
            {
                return Application.notfound;
            }

            PWSTR defaultQuery = default;
            try
            {
                try
                {
                    reg.QueryCurrentDefault(filename, AT_FILEEXTENSION, AL_EFFECTIVE, out defaultQuery);
                }
                catch
                {
                    return Application.notfound;
                }

                var qa = (IQueryAssociations)Activator.CreateInstance(Type.GetTypeFromCLSID(CLSID_QueryAssociations));
                qa.Init(0, filename, default, default);
                if (!GetString(qa, ASSOCSTR_FRIENDLYAPPNAME, out var friendlyAppName))
                {
                    return Application.notfound;
                }
                if (!GetString(qa, ASSOCSTR_DEFAULTICON, out var defaultIcon))
                {
                    return Application.notfound;
                }

                ProgIdApplication app = new(defaultQuery.ToString(), friendlyAppName, defaultIcon);
                assocHandlerCache.put(filename, app);
                return app;
            }
            catch
            {
                return Application.notfound;
            }
            finally
            {
                CoTaskMemFree(defaultQuery.Value);
            }
        }

        public List findAll(string filename)
        {
            filename = Path.GetExtension(filename);
            if (assocHandlerListCache.get(filename) is not List<ShellApplication> map)
            {
                map = new List<ShellApplication>();
                assocHandlerListCache.put(filename, map);

                HRESULT result;
                if ((result = SHAssocEnumHandlers(filename, ASSOC_FILTER_RECOMMENDED, out var enumHandlers)).Succeeded)
                {
                    IAssocHandler[] passocHandler = new IAssocHandler[1];
                    ref IAssocHandler assocHandler = ref passocHandler[0];
                    try
                    {
                        while (enumHandlers.Next(passocHandler) > 0)
                        {
                            map.Add(new ShellApplication(assocHandler));
                        }
                    }
                    catch (Exception e)
                    {
                        Log.warn("findAll: Failure enumerating IEnumAssocHandler", e);
                    }
                    map.Sort();
                }
                else
                {
                    Log.warn("findAll: Failure getting IEnumAssocHandler", Marshal.GetExceptionForHR(result.Value));
                }
            }

            return Utils.ConvertToJavaList(map);
        }

        public Application getDescription(string filename) => Application.notfound;

        public bool isInstalled(Application application) => application is ShellApplication;

        private static unsafe bool GetString(IQueryAssociations @this, ASSOCSTR part, out string value)
        {
            var pool = ArrayPool<char>.Shared;
            uint length = 0;
            try
            {
                @this.GetString(ASSOCF_NOTRUNCATE, part, "edit", default, ref length);
                char[] buffer = null;
                try
                {
                    buffer = pool.Rent((int)length);
                    length = (uint)buffer.Length;
                    fixed (char* bufferLocal = buffer)
                    {
                        @this.GetString(ASSOCF_NOTRUNCATE, part, "edit", bufferLocal, ref length);

                        value = ((PCWSTR)bufferLocal).ToString();
                        return true;
                    }
                }
                finally
                {
                    pool.Return(buffer);
                }
            }
            catch { }
            value = default;
            return false;
        }

        public class ProgIdApplication : Application
        {
            private readonly string defaultIcon;

            public ProgIdApplication(string identifier, string name, string defaultIcon) : base(identifier, name)
            {
                this.defaultIcon = defaultIcon;
            }
        }

        public class ShellApplication : Application, IComparable<ShellApplication>
        {
            private readonly int cachedImageIndex;
            private readonly IAssocHandler handler;

            public ShellApplication(in IAssocHandler handler) : base(handler.GetName(), handler.GetUIName())
            {
                this.handler = handler;
                IsRecommended = handler.IsRecommended().Succeeded;
                var path = handler.GetIconLocation(out var index);
                cachedImageIndex = Shell_GetCachedImageIndex(path, index, 0);
            }

            public bool IsRecommended { get; }

            public int CompareTo(ShellApplication other) => IsRecommended == other.IsRecommended ? 0 : other.IsRecommended ? 1 : -1;
        }
    }
}
