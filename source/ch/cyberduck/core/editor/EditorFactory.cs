// 
// Copyright (c) 2010-2013 Yves Langisch. All rights reserved.
// http://cyberduck.ch/
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
// yves@cyberduck.ch
// 

using System;
using System.Collections.Generic;
using System.IO;
using Ch.Cyberduck.Ui.Controller;
using Microsoft.Win32;
using ch.cyberduck.core.local;
using java.util;
using org.apache.log4j;
using Controller = ch.cyberduck.ui.Controller;
using Path = ch.cyberduck.core.Path;

namespace Ch.Cyberduck.Core.Editor
{
    public class EditorFactory : ch.cyberduck.core.editor.EditorFactory
    {
        private static readonly Logger Log = Logger.getLogger(typeof (EditorFactory).Name);

        private readonly IList<Application> _registeredEditors = new List<Application>();

        public EditorFactory()
        {
            _registeredEditors.Add(new Dreamweaver());
            _registeredEditors.Add(new Notepad());
            _registeredEditors.Add(new NotepadPlusPlus());
            _registeredEditors.Add(new TextPad());
        }

        public static void Register()
        {
            addFactory(NATIVE_PLATFORM, new EditorFactory());
        }

        protected override List getConfigured()
        {
            return Utils.ConvertToJavaList(_registeredEditors);
        }

        public override ch.cyberduck.core.editor.Editor create(Controller c, Application application, Path path)
        {
            return new WatchEditor((BrowserController) c, application, path);
        }

        protected override object create()
        {
            throw new NotImplementedException();
        }

        private class Dreamweaver : Application
        {
            public Dreamweaver()
                : base(Identifier(), "Dreamweaver")
            {
            }

            private static string Identifier()
            {
                try
                {
                    using (var uc = Registry.LocalMachine.OpenSubKey(@"Software\Adobe\Dreamweaver\"))
                    {
                        if (null != uc)
                        {
                            string[] subKeyNames = uc.GetSubKeyNames();
                            foreach (string keyName in subKeyNames)
                            {
                                RegistryKey versionSubtree = uc.OpenSubKey(keyName + "\\Installation");
                                if (versionSubtree != null)
                                {
                                    string exePath = (string) versionSubtree.GetValue(null);
                                    if (null != exePath)
                                    {
                                        return exePath.ToLower();
                                    }
                                }
                            }
                        }
                    }
                }
                catch (Exception e)
                {
                    Log.error(e.Message, e);
                }
                return null;
            }
        }

        /**
         * Default Editor if none selected
         */

        public class Notepad : Application
        {
            public Notepad()
                : base(Identifier(), "Notepad")
            {
            }

            private static string Identifier()
            {
                string windir = Environment.ExpandEnvironmentVariables("%WinDir%");
                string notepadExe = System.IO.Path.Combine(windir, "system32", "notepad.exe");
                if (File.Exists(notepadExe))
                {
                    return notepadExe.ToLower();
                }
                return null;
            }
        }

        private class NotepadPlusPlus : Application
        {
            public NotepadPlusPlus()
                : base(Identifier(), "Notepad++")
            {
            }

            private static string Identifier()
            {
                try
                {
                    using (
                        var uc =
                            Registry.LocalMachine.OpenSubKey(
                                @"Software\Microsoft\Windows\CurrentVersion\Uninstall\Notepad++"))
                    {
                        if (null != uc)
                        {
                            string exe = (string) uc.GetValue("DisplayIcon");
                            if (null != exe)
                            {
                                return exe.ToLower();
                            }
                        }
                    }
                }
                catch (Exception e)
                {
                    Log.error(e.Message, e);
                }
                return null;
            }
        }

        private class TextPad : Application
        {
            public TextPad()
                : base(Identifier(), "TextPad")
            {
            }

            private static string Identifier()
            {
                try
                {
                    using (var uc = Registry.LocalMachine.OpenSubKey(@"Software\Helios\TextPad\"))
                    {
                        if (null != uc)
                        {
                            string version = (string) uc.GetValue("CurrentVersion");
                            if (null != version)
                            {
                                RegistryKey versionSubtree = uc.OpenSubKey(version);
                                if (versionSubtree != null)
                                {
                                    string exePath = (string) versionSubtree.GetValue("ExePath");
                                    if (null != exePath)
                                    {
                                        return exePath.ToLower();
                                    }
                                }
                            }
                        }
                    }
                }
                catch (Exception e)
                {
                    Log.error(e.Message, e);
                }
                return null;
            }
        }
    }
}