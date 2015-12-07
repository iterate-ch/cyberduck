// 
// Copyright (c) 2010-2012 Yves Langisch. All rights reserved.
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
using System.Drawing;
using System.IO;
using ch.cyberduck.core.formatter;
using java.text;
using java.util;
using Path = ch.cyberduck.core.Path;

namespace Ch.Cyberduck.Ui.Controller
{
    public abstract class BaseItem
    {
        public Path Path;
        private string _date = String.Empty;
        private string _path = String.Empty;
        private string _size = String.Empty;


        public string ItemPath
        {
            get { return _path; }
            set { _path = value; }
        }

        public Image Icon { get; set; }

        public virtual string Size
        {
            get { return _size; }
            set { _size = value; }
        }

        public virtual string Date
        {
            get { return _date; }
            set { _date = value; }
        }

        public abstract string Name { get; set; }

        public BaseItem Parent { get; set; }
    }

    public class RootItem : BaseItem
    {
        public RootItem(string name)
        {
            ItemPath = name;
        }

        public override string Name
        {
            get { return ItemPath; }
            set { }
        }
    }

    public class FolderItem : BaseItem
    {
        public FolderItem(string name, BaseItem parent)
        {
            ItemPath = name;
            Parent = parent;
        }

        public override string Name
        {
            get { return System.IO.Path.GetFileName(ItemPath); }
            set
            {
                string dir = System.IO.Path.GetDirectoryName(ItemPath);
                string destination = System.IO.Path.Combine(dir, value);
                Directory.Move(ItemPath, destination);
                ItemPath = destination;
            }
        }
    }

    public class FileItem : BaseItem
    {
        public FileItem(string name, BaseItem parent)
        {
            ItemPath = name;
            Parent = parent;
        }

        public override string Name
        {
            get { return System.IO.Path.GetFileName(ItemPath); }
            set
            {
                string dir = System.IO.Path.GetDirectoryName(ItemPath);
                string destination = System.IO.Path.Combine(dir, value);
                File.Move(ItemPath, destination);
                ItemPath = destination;
            }
        }
    }

    public class CDItem : BaseItem
    {
        public CDItem(Path path, BaseItem parent)
        {
            Path = path;
            Parent = parent;
        }

        public override string Size
        {
            get { return SizeFormatterFactory.get().format(Path.attributes().getSize()); }
            set { }
        }

        public override string Date
        {
            get
            {
                DateFormat df = DateFormat.getDateInstance();
                return df.format(new Date(Path.attributes().getModificationDate()));
            }
            set { }
        }

        public override string Name
        {
            get { return Path.getName(); }
            set { }
        }
    }

    public class CDRootItem : CDItem
    {
        public CDRootItem(Path path) : base(path, null)
        {
        }

        public override string Size
        {
            get { return SizeFormatterFactory.get().format(Path.attributes().getSize()); }
            set { }
        }

        public override string Date
        {
            get
            {
                DateFormat df = DateFormat.getDateInstance();
                return df.format(new Date(Path.attributes().getModificationDate()));
            }
            set { }
        }

        public override string Name
        {
            get { return Path.getName(); }
            set { }
        }
    }
}