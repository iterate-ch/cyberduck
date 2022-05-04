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

using ch.cyberduck.core;
using ch.cyberduck.core.editor;
using ch.cyberduck.core.local;
using org.apache.logging.log4j;
using System;
using System.IO;
using Path = ch.cyberduck.core.Path;

namespace Ch.Cyberduck.Core.Editor
{
    public class SystemWatchEditor : AbstractEditor
    {
        private static readonly Logger Log = LogManager.getLogger(typeof(SystemWatchEditor).FullName);

        private readonly FileSystemWatcher _watcher;

        public SystemWatchEditor(Host host, Path file, ProgressListener listener)
            : base(host, file, listener)
        {
            _watcher = new FileSystemWatcher();
        }

        protected override void watch(Application application, ch.cyberduck.core.Local temporary, FileWatcherListener listener, ApplicationQuitCallback quit)
        {
            _watcher.Path = temporary.getParent().getAbsolute();
            _watcher.Filter = temporary.getName();
            _watcher.NotifyFilter = NotifyFilters.LastAccess | NotifyFilters.LastWrite | NotifyFilters.FileName |
                                    NotifyFilters.DirectoryName;
            _watcher.Changed += delegate (object sender, FileSystemEventArgs e)
            {
                Log.debug("HasChanged:" + e.FullPath);
                listener.fileWritten(temporary);
            };
            _watcher.Renamed += delegate (object sender, RenamedEventArgs e)
            {
                Log.debug(String.Format("HasRenamed: from {0} to {1}", e.OldFullPath, e.FullPath));
                listener.fileWritten(temporary);
            };
            // Begin watching.
            _watcher.EnableRaisingEvents = true;
        }

        public override void close()
        {
            _watcher.EnableRaisingEvents = false;
            _watcher.Dispose();
        }
    }
}
