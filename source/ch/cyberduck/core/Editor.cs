// 
// Copyright (c) 2010-2011 Yves Langisch. All rights reserved.
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
using System.Drawing;
using System.IO;
using ch.cyberduck.core;
using Ch.Cyberduck.Core;
using ch.cyberduck.core.editor;
using ch.cyberduck.core.threading;
using Ch.Cyberduck.Ui.Controller.Threading;
using Microsoft.Win32;
using org.apache.log4j;
using Path = ch.cyberduck.core.Path;

namespace Ch.Cyberduck.Ui.Controller
{
    public abstract class Editor : AbstractEditor
    {
        private static readonly List<AvailableEditor> Editors;
        private static readonly Logger Log = Logger.getLogger(typeof (Editor).Name);
        private readonly BrowserController _controller;

        /// <summary>
        /// The editor application
        /// </summary>
        protected string BundleIdentifier;

        static Editor()
        {
            Editors = new List<AvailableEditor>
                          {
                              new Dreamweaver(),
                              new Notepad(),
                              new NotepadPlusPlus(),
                              new TextPad()
                          };
        }

        protected Editor(BrowserController controller, string bundleIdentifier, Path path) : base(path)
        {
            _controller = controller;
            BundleIdentifier = bundleIdentifier;
        }

        protected override void open(BackgroundAction download)
        {
            _controller.background(new OpenBackgroundAction(_controller, download));
        }

        protected override void save(BackgroundAction upload)
        {
            _controller.background(new SaveBackgroundAction(_controller, upload));
        }

        public static IList<AvailableEditor> GetAvailableEditors()
        {
            return Editors;
        }

        public static AvailableEditor DefaultEditor()
        {
            String editorLocation = Preferences.instance().getProperty("editor.bundleIdentifier");
            if (Utils.IsBlank(editorLocation))
            {
                return null;
            }
            IList<AvailableEditor> availableEditors = GetAvailableEditors();
            foreach (AvailableEditor editor in availableEditors)
            {
                if (editorLocation.Equals(editor.Location))
                {
                    return editor;
                }
            }
            try
            {
                if (File.Exists(editorLocation))
                {
                    return new CustomEditor(editorLocation, editorLocation);
                }
            }
            catch
            {
                Log.warn("Default editor not found: " + editorLocation);
            }
            return null;
        }

        public abstract class AvailableEditor
        {
            private static readonly Logger Log = Logger.getLogger(typeof (AvailableEditor).Name);

            public bool Installed
            {
                get { return Location != null; }
            }

            public abstract string Location { get; }

            public abstract string Name { get; }

            public Icon GetIcon(IconCache.IconSize size)
            {
                try
                {
                    if (File.Exists(Location))
                    {
                        return IconCache.Instance.GetFileIconFromExecutable(Location, size);
                    }
                }
                catch
                {
                    Log.warn("Editor location not found: " + Location);
                }
                return null;
            }
        }

        public class CustomEditor : AvailableEditor
        {
            private readonly string _location;
            private readonly string _name;

            public CustomEditor(string name, string location)
            {
                _name = name;
                _location = location;
            }

            public override string Location
            {
                get { return _location; }
            }

            public override string Name
            {
                get { return _name; }
            }
        }

        private class Dreamweaver : AvailableEditor
        {
            private static readonly Logger Log = Logger.getLogger(typeof (Dreamweaver).Name);

            public override string Location
            {
                get
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
                                            return exePath;
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

            public override string Name
            {
                get { return "Dreamweaver"; }
            }
        }

        private class Notepad : AvailableEditor
        {
            public override string Location
            {
                get
                {
                    string windir = Environment.ExpandEnvironmentVariables("%WinDir%");
                    string notepadExe = System.IO.Path.Combine(windir, "system32", "notepad.exe");
                    if (File.Exists(notepadExe))
                    {
                        return notepadExe;
                    }
                    return null;
                }
            }

            public override string Name
            {
                get { return "Notepad"; }
            }
        }

        private class NotepadPlusPlus : AvailableEditor
        {
            private static readonly Logger Log = Logger.getLogger(typeof (NotepadPlusPlus).Name);

            public override string Location
            {
                get
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
                                    return exe;
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

            public override string Name
            {
                get { return "Notepad++"; }
            }
        }

        private class OpenBackgroundAction : BrowserBackgroundAction
        {
            private readonly BackgroundAction _download;

            public OpenBackgroundAction(BrowserController controller, BackgroundAction download) : base(controller)
            {
                _download = download;
            }

            public override void run()
            {
                _download.run();
            }

            public override string getActivity()
            {
                return _download.getActivity();
            }

            public override void cleanup()
            {
                _download.cleanup();
            }
        }

        private class SaveBackgroundAction : BrowserBackgroundAction
        {
            private readonly BackgroundAction _upload;

            public SaveBackgroundAction(BrowserController controller, BackgroundAction upload) : base(controller)
            {
                _upload = upload;
            }

            public override void run()
            {
                _upload.run();
            }

            public override string getActivity()
            {
                return _upload.getActivity();
            }

            public override void cleanup()
            {
                _upload.cleanup();
            }
        }

        private class TextPad : AvailableEditor
        {
            private static readonly Logger Log = Logger.getLogger(typeof (TextPad).Name);

            public override string Location
            {
                get
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
                                            return exePath;
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

            public override string Name
            {
                get { return "TextPad"; }
            }
        }
    }
}