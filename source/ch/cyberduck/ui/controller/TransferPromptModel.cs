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
using System.Drawing;
using System.Windows.Forms;
using Ch.Cyberduck.Ui.Winforms;
using Ch.Cyberduck.Ui.Winforms.Threading;
using ch.cyberduck.core;
using ch.cyberduck.core.formatter;
using ch.cyberduck.core.transfer;
using ch.cyberduck.ui.comparator;
using ch.cyberduck.ui.threading;
using java.util;
using org.apache.log4j;

//using Ch.Cyberduck.ui.winforms.threading;

namespace Ch.Cyberduck.Ui.Controller
{
    internal abstract class TransferPromptModel
    {
        protected static Logger log = Logger.getLogger(typeof (TransferPromptModel).FullName);
        protected readonly Transfer Transfer;

        private readonly string UNKNOWN = LocaleFactory.localizedString("Unknown");
        private readonly TransferPromptController _controller;

        /*
         * Container for all paths currently being listed in the background
         */
        private readonly List<Path> _isLoadingListingInBackground = new List<Path>();
        private readonly List<Path> _roots = new List<Path>();
        protected Bitmap AlertIcon = IconCache.Instance.IconForName("alert");

        protected TransferPromptModel(TransferPromptController controller, Transfer transfer)
        {
            _controller = controller;
            Transfer = transfer;
        }

        public virtual void Add(Path p)
        {
            _roots.Add(p);
        }

        public bool CanExpand(object path)
        {
            return ((Path) path).attributes().isDirectory();
        }

        public IEnumerable<Path> ChildrenGetter(object p)
        {
            Path directory = ((Path) p);
            AttributedList list;


            Cache cache = Transfer.cache();
            if (!cache.isCached(directory.getReference()))
            {
                _controller.Background(new ChildGetterTransferPromptBackgrounAction(_controller, Transfer, directory,
                                                                                    _isLoadingListingInBackground));
            }
            list = cache.get(directory.getReference())
                        .filter(new FilenameComparator(true), Transfer.getRegexFilter());
            for (int i = 0; i < list.size(); i++)
            {
                yield return (Path) list.get(i);
            }
        }

        public object GetName(Path path)
        {
            return path.getName();
        }

        public object GetModified(Path path)
        {
            long modificationDate = path.attributes().getModificationDate();
            if (modificationDate != -1)
            {
                return UserDefaultsDateFormatter.ConvertJavaMillisecondsToDateTime(modificationDate);
            }
            return UNKNOWN;
        }

        public abstract object GetSize(Path path);

        public string GetSizeAsString(object size)
        {
            return SizeFormatterFactory.get().format((long) size);
        }

        public object GetIcon(Path path)
        {
            return IconCache.Instance.IconForPath(path, IconCache.IconSize.Small);
        }

        public CheckState GetCheckState(Object path)
        {
            bool included = !Transfer.isSkipped((Path) path) &&
                            Transfer.isSelected((Path) path) &&
                            !_controller.Action.equals(TransferAction.ACTION_SKIP);
            if (included)
            {
                return CheckState.Checked;
            }
            return CheckState.Unchecked;
        }

        public IEnumerable<Path> GetEnumerator()
        {
            foreach (Path path in _roots)
            {
                yield return path;
            }
        }

        public CheckState SetCheckState(object p, CheckState newValue)
        {
            Path path = (Path) p;
            if (!Transfer.isSkipped(path))
            {
                Transfer.setSelected(path, newValue == CheckState.Checked ? true : false);
                return newValue;
            }
            //simulate enabled=false for the checkbox field
            //see TransferPromptController.java,outlineView_willDisplayCell_forTableColumn_item
            return newValue == CheckState.Checked ? CheckState.Unchecked : CheckState.Checked;
        }

        public abstract object GetWarningImage(Path path);

        public virtual object GetCreateImage(Path path)
        {
            return null;
        }

        public virtual object GetSyncGetter(Path path)
        {
            return null;
        }

        public bool IsActive(Path path)
        {
            return Transfer.isSelected(path);
        }

        private class ChildGetterTransferPromptBackgrounAction : ControllerBackgroundAction
        {
            private readonly TransferPromptController _controller;
            private readonly Path _directory;
            private readonly IList<Path> _isLoadingListingInBackground;
            private readonly Transfer _transfer;

            public ChildGetterTransferPromptBackgrounAction(TransferPromptController controller, Transfer transfer,
                                                            Path directory, IList<Path> isLoadingListingInBackground)
                : base(controller, new DialogAlertCallback(controller), controller, controller)
            {
                _controller = controller;
                _transfer = transfer;
                _directory = directory;
                _isLoadingListingInBackground = isLoadingListingInBackground;
            }

            public override List getSessions()
            {
                return _transfer.getSessions();
            }

            public override object run()
            {
                _transfer.cache().put(_directory.getReference(), _transfer.children(_directory));
                return true;
            }

            public override string getActivity()
            {
                return String.Format(LocaleFactory.localizedString("Listing directory {0}", "Status"),
                                     _directory.getName());
            }

            public override void cleanup()
            {
                base.cleanup();
                _controller.RefreshObject(_directory);
            }
        }
    }
}