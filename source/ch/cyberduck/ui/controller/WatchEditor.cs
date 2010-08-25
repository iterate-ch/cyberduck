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
using org.apache.log4j;
using Path = ch.cyberduck.core.Path;

namespace Ch.Cyberduck.Ui.Controller
{
    public class WatchEditor : Editor
    {
        private static readonly Logger Log = Logger.getLogger(typeof (WatchEditor).Name);
        private DateTime _lastWriteTime;
        private FileSystemWatcher _watcher;

        public WatchEditor(BrowserController controller, Path path)
            : base(controller, null, path)
        {
        }

        public bool Open { get; set; }

        protected override void edit()
        {
            Process process = new Process();
            process.StartInfo.FileName = edited.getLocal().getAbsolute();
            //process.StartInfo.Verb = "Edit";            
            //process.StartInfo.Verb = "Open"; // open with dialog does not come up for unknown file types if Verb = Open            
            try
            {
                process.Start();
                // workaround because of process re-use
                // see http://stackoverflow.com/questions/303339/c-process-start-how-to-prevent-re-use-of-existing-application
                if (process.WaitForInputIdle(10000) && !process.HasExited)
                {
                    Log.debug("Attach exit handler");
                    process.Exited += EditorExited;
                    process.EnableRaisingEvents = true;
                }
                else
                {
                    Log.debug("No exit handler could be attached");
                }
            }
            catch (InvalidOperationException)
            {
                // catch silently 
            }
            catch (Win32Exception e)
            {
                Log.error(e);
            }

            Open = true;

            _watcher = new FileSystemWatcher();
            _watcher.Path = edited.getLocal().getParent().getAbsolute();
            _watcher.Filter = edited.getLocal().getName();
            _watcher.NotifyFilter = NotifyFilters.LastAccess | NotifyFilters.LastWrite
                                    | NotifyFilters.FileName | NotifyFilters.DirectoryName;

            RegisterHandlers();

            // Begin watching.
            _watcher.EnableRaisingEvents = true;
        }

        private void EditorExited(object sender, EventArgs e)
        {
            Open = false;
            if (!edited.status().isComplete())
            {
                setDeferredDelete(true);
            }
            else
            {
                delete();
            }
        }

        private void HasRenamed(object sender, RenamedEventArgs e)
        {
            Log.info("HasRenamed:" + e.FullPath);
        }

        private void HasDeleted(object sender, FileSystemEventArgs e)
        {
            Log.info("HasDeleted:" + e.FullPath);
            _watcher.EnableRaisingEvents = false;
            RemoveHandlers();
        }

        private void HasChanged(object sender, FileSystemEventArgs e)
        {
            Log.info("HasChanged:" + e.FullPath);
            try
            {
                _watcher.EnableRaisingEvents = false;
                DateTime lastWriteTime = File.GetLastWriteTime(e.FullPath);
                if (lastWriteTime.Equals(_lastWriteTime)) return;
                //workaround: http://stackoverflow.com/questions/1764809/filesystemwatcher-changed-event-is-raised-twice                                
                save();
                _lastWriteTime = lastWriteTime;
            }
            finally
            {
                _watcher.EnableRaisingEvents = true;
            }
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

        protected override void setDeferredDelete(Boolean deferredDelete)
        {
            if (!isOpen())
            {
                delete();
            }
            base.setDeferredDelete(deferredDelete);
        }

        public override bool isOpen()
        {
            return Open;
        }
    }
}