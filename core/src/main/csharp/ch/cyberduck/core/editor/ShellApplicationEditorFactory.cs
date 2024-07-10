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
using Ch.Cyberduck.Core.Local;
using java.util;
using CoreEditor = ch.cyberduck.core.editor.Editor;

namespace Ch.Cyberduck.Core.Editor
{
    public class ShellApplicationEditorFactory : EditorFactory
    {
        public override CoreEditor create(Host host, Path file, ProgressListener listener)
        {
            return new DefaultWatchEditor(host, file, listener);
        }

        protected override List getConfigured() => ShellApplicationFinder.findAll();
    }
}
