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
using ch.cyberduck.core;
using org.apache.log4j;

namespace Ch.Cyberduck.Ui.Controller
{
    public class EditorFactory
    {
        private static readonly Logger Log = Logger.getLogger(typeof (EditorFactory).Name);

        /// <summary>
        /// 
        /// </summary>
        /// <param name="c"></param>
        /// <param name="path"></param>
        /// <returns>New editor instance for the given file type.</returns>
        public static Editor createEditor(BrowserController c, Path path)
        {
            return new WatchEditor(c, path);
        }
    }
}