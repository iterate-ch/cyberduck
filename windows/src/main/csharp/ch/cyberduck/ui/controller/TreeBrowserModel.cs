// 
// Copyright (c) 2010-2016 Yves Langisch. All rights reserved.
// http://cyberduck.io/
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
// feedback@cyberduck.io
// 

using System;
using System.Collections.Generic;
using System.Text;
using ch.cyberduck.core;
using ch.cyberduck.core.formatter;
using ch.cyberduck.core.local;
using ch.cyberduck.core.preferences;
using ch.cyberduck.ui.browser;
using Ch.Cyberduck.Core;
using Ch.Cyberduck.Ui.Winforms;
using java.util;

namespace Ch.Cyberduck.Ui.Controller
{
    internal class TreeBrowserModel
    {
        private readonly PathCache _cache;
        private readonly BrowserController _controller;
        private readonly FileDescriptor _descriptor = FileDescriptorFactory.get();
        private readonly ListProgressListener _listener;
        private readonly string _unknown = LocaleFactory.localizedString("Unknown");

        public TreeBrowserModel(BrowserController controller, PathCache cache, ListProgressListener listener)
        {
            _controller = controller;
            _cache = cache;
            _listener = listener;
        }

        public bool CanExpand(object path)
        {
            return ((Path) path).isDirectory();
        }

        public IEnumerable<Path> ChildrenGetter(object folder)
        {            
            AttributedList list = _cache.get((Path) folder)
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
            if (path.isVolume())
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
                return
                    UserDateFormatterFactory.get()
                        .getShortFormat(UserDefaultsDateFormatter.ConvertDateTimeToJavaMilliseconds(modificationDate),
                            PreferencesFactory.get().getBoolean("browser.date.natural"));
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
            Acl acl = path.attributes().getAcl();
            if (!Acl.EMPTY.equals(acl))
            {
                StringBuilder s = new StringBuilder();
                Iterator iterator = acl.entrySet().iterator();
                while (iterator.hasNext())
                {
                    Map.Entry entry = (Map.Entry) iterator.next();
                    s.Append(String.Format("{0}{1}:{2}", s.Length == 0 ? "" : ", ",
                        ((Acl.User) entry.getKey()).getDisplayName(), entry.getValue()));
                }
                return s.ToString();
            }
            Permission permission = path.attributes().getPermission();
            return permission.toString();
        }

        public object GetKind(Path path)
        {
            return _descriptor.getKind(path);
        }

        public object GetExtension(Path path)
        {
            return path.isFile()
                ? Utils.IsNotBlank(path.getExtension()) ? path.getExtension() : LocaleFactory.localizedString("None")
                : LocaleFactory.localizedString("None");
        }

        public object GetRegion(Path path)
        {
            return Utils.IsNotBlank(path.attributes().getRegion())
                ? path.attributes().getRegion()
                : LocaleFactory.localizedString("Unknown");
        }

        public object GetVersion(Path path)
        {
            return Utils.IsNotBlank(path.attributes().getVersionId())
                ? path.attributes().getVersionId()
                : LocaleFactory.localizedString("None");
        }

        public bool GetActive(Path path)
        {
            return _controller.IsConnected() && SearchFilterFactory.HIDDEN_FILTER.accept(path);
        }
    }
}