// 
// Copyright (c) 2010-2014 Yves Langisch. All rights reserved.
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
using System.IO;
using ch.cyberduck.core;
using ch.cyberduck.core.editor;
using ch.cyberduck.core.local;
using ch.cyberduck.core.pool;
using org.apache.log4j;
using Path = ch.cyberduck.core.Path;

namespace Ch.Cyberduck.Core.Editor
{
    public class SystemWatchEditor : AbstractEditor
    {
        private static readonly Logger Log = Logger.getLogger(typeof(SystemWatchEditor).FullName);
        private FileSystemWatcher _watcher;

        public SystemWatchEditor(Application application, SessionPool session, Path file, ProgressListener listener)
            : base(application, session, file, listener)
        {
        }

        protected override void watch(ch.cyberduck.core.Local file, FileWatcherListener listener)
        {
            _watcher = new FileSystemWatcher();
            _watcher.Path = file.getParent().getAbsolute();
            _watcher.Filter = file.getName();
            _watcher.NotifyFilter = NotifyFilters.LastAccess | NotifyFilters.LastWrite | NotifyFilters.FileName |
                                    NotifyFilters.DirectoryName;
            _watcher.Changed += delegate(object sender, FileSystemEventArgs e)
            {
                Log.debug("HasChanged:" + e.FullPath);
                listener.fileWritten(file);
            };
            _watcher.Renamed += delegate(object sender, RenamedEventArgs e)
            {
                Log.debug(String.Format("HasRenamed: from {0} to {1}", e.OldFullPath, e.FullPath));
                listener.fileWritten(file);
            };
            // Begin watching.
            _watcher.EnableRaisingEvents = true;
        }

        public override void delete()
        {
            _watcher.EnableRaisingEvents = false;
            _watcher.Dispose();
            base.delete();
        }
    }
}