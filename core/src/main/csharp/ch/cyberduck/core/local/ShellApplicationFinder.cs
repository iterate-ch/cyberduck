//
// Copyright (c) 2002-2022 iterate GmbH. All rights reserved.
// http://cyberduck.io/
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU General Public License for more details.
//
// Bug fixes, suggestions and comments should be sent to:
// feedback@cyberduck.io
//

using ch.cyberduck.core.cache;
using ch.cyberduck.core.local;
using Ch.Cyberduck.Core.I18n;
using java.util;
using org.apache.commons.io;
using org.apache.logging.log4j;
using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.IO;
using System.Runtime.InteropServices;
using System.Threading;
using Windows.Win32;
using Windows.Win32.Foundation;
using Windows.Win32.System.Com;
using Windows.Win32.UI.Shell;
using Windows.Win32.UI.Shell.Common;
using static Windows.Win32.CorePInvoke;
using static Windows.Win32.UI.Shell.ASSOC_FILTER;
using static Windows.Win32.UI.Shell.ASSOCIATIONLEVEL;
using static Windows.Win32.UI.Shell.ASSOCIATIONTYPE;
using static Windows.Win32.UI.Shell.ASSOCSTR;
using static Windows.Win32.UI.Shell.OPEN_AS_INFO_FLAGS;

namespace Ch.Cyberduck.Core.Local
{
    public class ShellApplicationFinder : ApplicationFinder
    {
        private static readonly LRUCache assocHandlerCache = LRUCache.build(25);
        private static readonly LRUCache assocHandlerListCache = LRUCache.build(25);
        private static readonly Logger Log = LogManager.getLogger(typeof(ShellApplicationFinder).FullName);

        private interface IInvokeApplication
        {
            int IconIndex { get; }

            string IconPath { get; }
        }

        public static List findAll()
        {
            const string key = "enum.assoc.handler.all";
            if (assocHandlerListCache.get(key) is not List<Application> map)
            {
                map = new List<Application>();
                assocHandlerListCache.put(key, map);
                map.Add(ShellOpenWithApplication.Instance);

                HRESULT result;
                if ((result = SHAssocEnumHandlers(string.Empty, ASSOC_FILTER.ASSOC_FILTER_NONE, out var enumHandlers)).Succeeded)
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
                }
                else
                {
                    Log.warn("findAll: Failure getting IEnumAssocHandler", Marshal.GetExceptionForHR(result.Value));
                }
            }

            return Utils.ConvertToJavaList(map);
        }

        /// <summary>
        /// Finds default associated application.
        /// </summary>
        /// <param name="filename"></param>
        /// <returns></returns>
        public unsafe Application find(string filename)
        {
            int dotIndex = filename.LastIndexOf('.');
            if (dotIndex != -1)
            {
                filename = filename.Substring(dotIndex);
            }
            if (assocHandlerCache.get(filename) is ProgIdApplication shellHandler)
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
                qa.Init(0, defaultQuery, default, default);

                if (!qa.GetString(ASSOCSTR_FRIENDLYAPPNAME, "open", out var friendlyAppName))
                {
                    return Application.notfound;
                }
                if (!qa.GetString(ASSOCSTR_APPICONREFERENCE, "open", out var defaultIcon))
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
                }
                else
                {
                    Log.warn("findAll: Failure getting IEnumAssocHandler", Marshal.GetExceptionForHR(result.Value));
                }
            }

            return Utils.ConvertToJavaList(map);
        }

        public Application getDescription(string filename)
        {
            if (assocHandlerCache.get(filename) is Application app)
            {
                return app;
            }
            if (string.Equals(ShellOpenWithApplication.Key, filename))
            {
                return ShellOpenWithApplication.Instance;
            }
            foreach (Application item in findAll())
            {
                if (string.Equals(item.getIdentifier(), filename))
                {
                    assocHandlerCache.put(filename, item);
                    return item;
                }
            }
            if (!File.Exists(filename))
            {
                return Application.notfound;
            }

            if (FileVersionInfo.GetVersionInfo(filename) is FileVersionInfo info)
            {
                app = new(filename.ToLower(), info.FileDescription);
            }
            else
            {
                // Does not contain version information
                app = new(filename.ToLower(), FilenameUtils.getName(filename));
            }
            assocHandlerCache.put(filename, app);
            return app;
        }

        public bool isInstalled(Application application)
        {
            if (application is IInvokeApplication)
            {
                return true;
            }
            return application != Application.notfound && File.Exists(application.getIdentifier());
        }

        public class ProgIdApplication : Application, IInvokeApplication, WindowsApplicationLauncher.IInvokeApplication
        {
            public ProgIdApplication(string identifier, string name, string defaultIcon) : base(identifier, name)
            {
                PWSTR pszIconFile = defaultIcon;
                IconIndex = PathParseIconLocation(pszIconFile);
                IconPath = pszIconFile.ToString();
            }

            public int IconIndex { get; }

            public string IconPath { get; }

            public unsafe void Launch(ch.cyberduck.core.Local local)
            {
                SHELLEXECUTEINFOW info = new()
                {
                    cbSize = (uint)sizeof(SHELLEXECUTEINFOW),
                    lpClass = getIdentifier(),
                    fMask = SEE_MASK_CLASSNAME | SEE_MASK_NOASYNC,
                    lpVerb = "open",
                    lpFile = local.getAbsolute()
                };
                ShellExecuteEx(ref info);
            }
        }

        public class ShellApplication : Application, IInvokeApplication, WindowsApplicationLauncher.IInvokeApplication
        {
            private readonly IAssocHandler handler;
            private readonly int iconIndex;
            private readonly string iconPath;
            private readonly SynchronizationContext sync;

            public ShellApplication(in IAssocHandler handler) : base(handler.GetName(), handler.GetUIName())
            {
                sync = SynchronizationContext.Current;
                this.handler = handler;
                IsRecommended = handler.IsRecommended().Succeeded;
                iconPath = handler.GetIconLocation(out iconIndex);
            }

            public int IconIndex => iconIndex;

            public string IconPath => iconPath;

            public bool IsRecommended { get; }

            public void Launch(ch.cyberduck.core.Local local)
            {
                if (SynchronizationContext.Current == null)
                {
                    sync.Send(d => Launch((ch.cyberduck.core.Local)d), local);
                    return;
                }

                if (SHCreateItemFromParsingName<IShellItem>(local.getAbsolute(), null, out var ppv) is { Failed: true, Value: { } hr })
                {
                    return;
                }

                try
                {
                    ppv.BindToHandler(null, BHID_DataObject, out IDataObject pdo);
                    handler.Invoke(pdo);
                }
                catch
                {
                    // Catch Silently
                }
            }
        }

        public class ShellOpenWithApplication : Application, IInvokeApplication, WindowsApplicationLauncher.IInvokeApplication
        {
            public const string Key = "shell:openfilewith";
            public static readonly ShellOpenWithApplication Instance;
            public static readonly string Name;
            private static readonly Logger Log = LogManager.getLogger(typeof(ShellOpenWithApplication));

            static ShellOpenWithApplication()
            {
                try
                {
                    using var loader = new StringLoader();
                    Name = loader.GetString(9016);
                }
                catch
                {
                    Log.warn("Unable to retrieve resource id 9016 from Shell32.dll, falling back.");
                    Name = "Open With…";
                }
                Instance = new();
            }

            public ShellOpenWithApplication() : base(Key, Name)
            {
            }

            int IInvokeApplication.IconIndex => throw new NotImplementedException();

            string IInvokeApplication.IconPath => throw new NotImplementedException();

            public void Launch(ch.cyberduck.core.Local local)
            {
                OPENASINFO info = new()
                {
                    oaifInFlags = OAIF_EXEC,
                    pcszFile = local.getAbsolute()
                };
                SHOpenWithDialog(default, info);
            }
        }
    }
}
