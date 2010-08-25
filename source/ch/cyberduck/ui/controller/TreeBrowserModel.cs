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
using System.Collections.Generic;
using ch.cyberduck.core;
using Ch.Cyberduck.Core;
using ch.cyberduck.core.i18n;
using Ch.Cyberduck.Ui.Controller.Threading;
using Ch.Cyberduck.Ui.Winforms;

namespace Ch.Cyberduck.Ui.Controller
{
    internal class TreeBrowserModel
    {
        private readonly string _unknown = Locale.localizedString("Unknown");
        private readonly BrowserController _controller;
        private readonly List<Path> _isLoadingListingInBackground = new List<Path>();

        public TreeBrowserModel(BrowserController controller)
        {
            _controller = controller;
        }

        public bool CanExpand(object reference)
        {
            Path path = ((TreePathReference) reference).Unique;
            return path.attributes().isDirectory();
        }

        public IEnumerable<TreePathReference> ChildrenGetter(object reference)
        {
            Path path = ((TreePathReference) reference).Unique;
            AttributedList list;
            lock (_isLoadingListingInBackground)
            {
                if (!_isLoadingListingInBackground.Contains(path))
                {
                    if (path.isCached())
                    {
                        list = path.cache().get(path.getReference(), _controller.FilenameComparator,
                                                _controller.FilenameFilter);
                        for (int i = 0; i < list.size(); i++)
                        {
                            yield return new TreePathReference((Path) list.get(i));
                        }
                        yield break;
                    }
                }
                _isLoadingListingInBackground.Add(path);
                // Reloading a workdir that is not cached yet would cause the interface to freeze;
                // Delay until path is cached in the background


                // switch to blocking children fetching
                //path.childs();

                _controller.Background(new ChildGetterBrowserBackgrounAction(_controller, path,
                                                                             _isLoadingListingInBackground));
            }
            list = path.cache().get(path.getReference(), _controller.FilenameComparator, _controller.FilenameFilter);
            for (int i = 0; i < list.size(); i++)
            {
                yield return new TreePathReference((Path) list.get(i));
            }
            yield break;
        }

        public IEnumerable<TreePathReference> GetEnumerator()
        {
            if (null == _controller.Workdir)
                yield break;

            AttributedList list = _controller.Workdir.children(_controller.FilenameFilter);
            for (int i = 0; i < list.size(); i++)
            {
                yield return new TreePathReference((Path) list.get(i));
            }
        }

        public object GetName(TreePathReference path)
        {
            return path.Unique.getName();
        }

        public object GetIcon(TreePathReference path)
        {
            return IconCache.Instance.IconForPath(path.Unique, IconCache.IconSize.Small);
        }

        public object GetModified(TreePathReference path)
        {
            long modificationDate = path.Unique.attributes().getModificationDate();
            if (modificationDate != -1)
            {
                return UserDefaultsDateFormatter.ConvertJavaMiliSecondToDateTime(modificationDate);
            }
            return _unknown;
        }

        public object GetSize(TreePathReference path)
        {
            return path.Unique.attributes().getSize();
        }

        public string GetSizeAsString(object size)
        {
            return Status.getSizeAsString((long) size);
        }

        public object GetOwner(TreePathReference path)
        {
            return path.Unique.attributes().getOwner();
        }

        public object GetGroup(TreePathReference path)
        {
            return path.Unique.attributes().getGroup();
        }

        public object GetPermission(TreePathReference path)
        {
            Path p = path.Unique;
            Permission permission = p.attributes().getPermission();
            if (null == permission)
            {
                return _unknown;
            }
            return permission.toString();
        }

        public object GetKind(TreePathReference path)
        {
            return path.Unique.kind();
        }

        public bool GetActive(TreePathReference reference)
        {
            return _controller.IsConnected() && BrowserController.HiddenFilter.accept(reference.Unique);
        }

        private class ChildGetterBrowserBackgrounAction : BrowserBackgroundAction
        {
            private readonly BrowserController _controller;
            private readonly List<Path> _isLoadingListingInBackground;
            private readonly Path _path;

            public ChildGetterBrowserBackgrounAction(BrowserController controller,
                                                     Path path,
                                                     List<Path> isLoadingListingInBackground)
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

                    if (_isLoadingListingInBackground.Count == 0)
                    {
                        _controller.ReloadData(true);
                        //_controller.RefreshObject(_path);
                    }
                }
                base.cleanup();
            }
        }
    }
}