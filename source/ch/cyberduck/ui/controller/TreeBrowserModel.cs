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
using Ch.Cyberduck.Core;
using Ch.Cyberduck.Ui.Winforms;
using ch.cyberduck.core;
using ch.cyberduck.core.formatter;
using ch.cyberduck.core.local;
using ch.cyberduck.ui.action;
using ch.cyberduck.ui.threading;

namespace Ch.Cyberduck.Ui.Controller
{
    internal class TreeBrowserModel
    {
        private readonly Cache _cache;
        private readonly BrowserController _controller;
        private readonly FileDescriptor _descriptor = FileDescriptorFactory.get();
        private readonly string _unknown = LocaleFactory.localizedString("Unknown");

        public TreeBrowserModel(BrowserController controller, Cache cache)
        {
            _controller = controller;
            _cache = cache;
        }

        public bool CanExpand(object path)
        {
            return ((Path) path).attributes().isDirectory();
        }

        public IEnumerable<Path> ChildrenGetter(object p)
        {
            Path directory = (Path) p;
            AttributedList list;
            if (!_cache.isCached(directory.getReference()))
            {
                // Reloading a workdir that is not cached yet would cause the interface to freeze;
                // Delay until path is cached in the background
                // switch to blocking children fetching
                //path.childs();
                _controller.background(new ListAction(_controller, directory, _cache));
            }
            list = _cache.get(directory.getReference())
                         .filter(_controller.FilenameComparator, _controller.FilenameFilter);
            for (int i = 0; i < list.size(); i++)
            {
                yield return (Path) list.get(i);
            }
        }

        public object GetName(Path path)
        {
            return path.getName();
        }

        public object GetIcon(Path path)
        {
            if (path.attributes().isVolume())
            {
                return IconCache.Instance.VolumeIcon(_controller.Session.getHost().getProtocol(),
                                                     IconCache.IconSize.Small);
            }
            return IconCache.Instance.IconForPath(path, IconCache.IconSize.Small);
        }

        public object GetModified(Path path)
        {
            long modificationDate = path.attributes().getModificationDate();
            if (modificationDate != -1)
            {
                return UserDefaultsDateFormatter.ConvertJavaMillisecondsToDateTime(modificationDate);
            }
            return DateTime.MinValue;
        }

        public string GetModifiedAsString(object value)
        {
            DateTime modificationDate = (DateTime) value;
            if (modificationDate != DateTime.MinValue)
            {
                return UserDateFormatterFactory.get()
                                               .getShortFormat(modificationDate.Ticks/TimeSpan.TicksPerMillisecond,
                                                               Preferences.instance().getBoolean("browser.date.natural"));
            }
            return _unknown;
        }

        public object GetSize(Path path)
        {
            return path.attributes().getSize();
        }

        public string GetSizeAsString(object size)
        {
            return SizeFormatterFactory.get().format((long) size);
        }

        public object GetOwner(Path path)
        {
            return Utils.IsBlank(path.attributes().getOwner())
                       ? LocaleFactory.localizedString("Unknown")
                       : path.attributes().getOwner();
        }

        public object GetGroup(Path path)
        {
            return Utils.IsBlank(path.attributes().getGroup())
                       ? LocaleFactory.localizedString("Unknown")
                       : path.attributes().getGroup();
        }

        public object GetPermission(Path path)
        {
            Permission permission = path.attributes().getPermission();
            if (null == permission)
            {
                return _unknown;
            }
            return permission.toString();
        }

        public object GetKind(Path path)
        {
            return _descriptor.getKind(path);
        }

        public object GetExtension(Path path)
        {
            return path.attributes().isFile()
                       ? Utils.IsNotBlank(path.getExtension())
                             ? path.getExtension()
                             : LocaleFactory.localizedString("None")
                       : LocaleFactory.localizedString("None");
        }

        public object GetRegion(Path path)
        {
            return Utils.IsNotBlank(path.attributes().getRegion())
                       ? path.attributes().getRegion()
                       : LocaleFactory.localizedString("Unknown");
        }

        public bool GetActive(Path path)
        {
            return _controller.IsConnected() && BrowserController.HiddenFilter.accept(path);
        }

        private class ListAction : WorkerBackgroundAction
        {
            public ListAction(BrowserController controller, Path directory, Cache cache)
                : base(controller, controller.Session, new InnerListWorker(controller, directory, cache))
            {
            }

            private class InnerListWorker : SessionListWorker
            {
                private readonly BrowserController _controller;

                public InnerListWorker(BrowserController controller, Path directory, Cache cache)
                    : base(
                        controller.Session, cache, directory,
                        new DialogLimitedListProgressListener(controller))
                {
                    _controller = controller;
                }

                public override void cleanup(object result)
                {
                    _controller.ReloadData(true);
                }
            }
        }
    }
}