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
using Ch.Cyberduck.Ui.Controller.Threading;
using Ch.Cyberduck.Ui.Winforms;
using ch.cyberduck.core;
using ch.cyberduck.core.date;
using ch.cyberduck.core.formatter;
using ch.cyberduck.core.i18n;
using ch.cyberduck.core.local;

namespace Ch.Cyberduck.Ui.Controller
{
    internal class TreeBrowserModel
    {
        private readonly BrowserController _controller;
        private readonly List<AbstractPath> _isLoadingListingInBackground = new List<AbstractPath>();
        private readonly string _unknown = Locale.localizedString("Unknown");
        private readonly FileDescriptor descriptor = FileDescriptorFactory.get();

        public TreeBrowserModel(BrowserController controller)
        {
            _controller = controller;
        }

        public bool CanExpand(object path)
        {
            return ((Path) path).attributes().isDirectory();
        }

        public IEnumerable<Path> ChildrenGetter(object path)
        {
            Path p = (Path) path;
            AttributedList list;
            lock (_isLoadingListingInBackground)
            {
                Cache cache = _controller.getSession().cache();
                if (!_isLoadingListingInBackground.Contains(p))
                {
                    if (cache.isCached(p.getReference()))
                    {
                        list = cache.get(p.getReference()).filter(_controller.FilenameComparator,
                                                                  _controller.FilenameFilter);
                        for (int i = 0; i < list.size(); i++)
                        {
                            yield return (Path) list.get(i);
                        }
                        yield break;
                    }
                    _isLoadingListingInBackground.Add(p);
                    // Reloading a workdir that is not cached yet would cause the interface to freeze;
                    // Delay until path is cached in the background
                    // switch to blocking children fetching
                    //path.childs();
                    _controller.Background(new ChildGetterBrowserBackgrounAction(_controller, p,
                                                                                 _isLoadingListingInBackground));
                }
                list = cache.get(p.getReference()).filter(_controller.FilenameComparator, _controller.FilenameFilter);
//                if (list.size() == 0)
//                {
//                    yield return
//                        new TreePathReference(PathFactory.createPath(_controller.getSession(), "Loading...",
//                                                                     AbstractPath.FILE_TYPE));
//                }
//                else
//                {
                for (int i = 0; i < list.size(); i++)
                {
                    yield return (Path) list.get(i);
                }
//                }
                yield break;
            }
        }

        public IEnumerable<Path> GetEnumerator()
        {
            if (null == _controller.Workdir)
                yield break;

            AttributedList list = _controller.Workdir.children(_controller.FilenameFilter);
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
                                               .getShortFormat(modificationDate.Ticks,
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
            return path.attributes().getOwner();
        }

        public object GetGroup(Path path)
        {
            return path.attributes().getGroup();
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
            return descriptor.getKind(path);
        }

        public bool GetActive(Path path)
        {
            return _controller.IsConnected() && BrowserController.HiddenFilter.accept(path);
        }

        private class ChildGetterBrowserBackgrounAction : BrowserBackgroundAction
        {
            private readonly BrowserController _controller;
            private readonly List<AbstractPath> _isLoadingListingInBackground;
            private readonly Path _path;

            public ChildGetterBrowserBackgrounAction(BrowserController controller,
                                                     Path path,
                                                     List<AbstractPath> isLoadingListingInBackground)
                : base(controller)
            {
                _controller = controller;
                _path = path;
                _isLoadingListingInBackground = isLoadingListingInBackground;
            }

            public override void run()
            {
                _path.children();
            }

            public override string getActivity()
            {
                return String.Format(Locale.localizedString("Listing directory {0}", "Status"), _path.getName());
            }

            public override void cleanup()
            {
                lock (_isLoadingListingInBackground)
                {
                    _isLoadingListingInBackground.Remove(_path);
                    _controller.RefreshObject(_path, true);
                }
                base.cleanup();
            }
        }
    }
}