// 
// Copyright (c) 2010 Yves Langisch. All rights reserved.
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
using System.ComponentModel;
using System.Diagnostics;
using System.IO;
using System.Threading;
using Ch.Cyberduck.Core;
using org.apache.log4j;
using Path = ch.cyberduck.core.Path;

namespace Ch.Cyberduck.Ui.Controller
{
    public class WatchEditor : Editor
    {
        private static readonly Logger Log = Logger.getLogger(typeof (WatchEditor).FullName);
        private readonly string _editor;
        private Timer _atomicSaveTimer;
        private FileSystemWatcher _watcher;

        public WatchEditor(BrowserController controller, Path path, String editor)
            : base(controller, null, path)
        {
            _editor = editor;
        }

        protected override void edit()
        {
            Process process = new Process();

            if (Utils.IsBlank(_editor))
            {
                process.StartInfo.FileName = edited.getLocal().getAbsolute();
            }
            else
            {
                process.StartInfo.FileName = _editor;
                process.StartInfo.Arguments = edited.getLocal().getAbsolute();
            }
            try
            {
                process.Start();
            }
            catch (InvalidOperationException e)
            {
                Log.error(e);
                return;
            }
            catch (Win32Exception e)
            {
                Log.error(e);
                return;
            }
            _watcher = new FileSystemWatcher();
            _watcher.Path = edited.getLocal().getParent().getAbsolute();
            _watcher.Filter = edited.getLocal().getName();
            _watcher.NotifyFilter = NotifyFilters.LastAccess | NotifyFilters.LastWrite
                                    | NotifyFilters.FileName | NotifyFilters.DirectoryName;
            RegisterHandlers();
            _atomicSaveTimer = new Timer(delegate
                                             {
                                                 RemoveHandlers();
                                                 _atomicSaveTimer.Change(Timeout.Infinite, Timeout.Infinite);
                                             }, null, Timeout.Infinite, Timeout.Infinite);
            // Begin watching.
            _watcher.EnableRaisingEvents = true;
        }

        private void FileNeedsToBeUpdated(string path)
        {
            try
            {
                _watcher.EnableRaisingEvents = false;
                save();
            }
            finally
            {
                _watcher.EnableRaisingEvents = true;
            }
        }

        private void HasRenamed(object sender, RenamedEventArgs e)
        {
            Log.info(String.Format("HasRenamed: from {0} to {1}", e.OldFullPath, e.FullPath));
            //prevent removing handlers
            //_atomicSaveTimer.Change(Timeout.Infinite, Timeout.Infinite);
            FileNeedsToBeUpdated(e.FullPath);
        }

        private void HasDeleted(object sender, FileSystemEventArgs e)
        {
            Log.info("HasDeleted:" + e.FullPath);
            //an atomic save must not last longer than 5 seconds. After elapsing the handlers are removed.
            //_atomicSaveTimer.Change(5000, Timeout.Infinite);
        }

        private void HasChanged(object sender, FileSystemEventArgs e)
        {
            Log.info("HasChanged:" + e.FullPath);
            FileNeedsToBeUpdated(e.FullPath);
        }

        protected override void delete()
        {
            _watcher.EnableRaisingEvents = false;
            RemoveHandlers();
            base.delete();
        }

        private void RegisterHandlers()
        {
            _watcher.Changed += HasChanged;
            _watcher.Deleted += HasDeleted;
            _watcher.Renamed += HasRenamed;
        }

        private void RemoveHandlers()
        {
            _watcher.Changed -= HasChanged;
            _watcher.Deleted -= HasDeleted;
            _watcher.Renamed -= HasRenamed;
        }
    }
}