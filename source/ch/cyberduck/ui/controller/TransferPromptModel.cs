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
using System.Drawing;
using System.Windows.Forms;
using ch.cyberduck.core;
using Ch.Cyberduck.Core;
using ch.cyberduck.core.i18n;
using ch.cyberduck.core.threading;
using Ch.Cyberduck.Ui.Winforms;
using org.apache.log4j;

namespace Ch.Cyberduck.Ui.Controller
{
    internal abstract class TransferPromptModel
    {
        protected static Logger log = Logger.getLogger(typeof (TransferPromptModel));
        protected readonly Transfer Transfer;

        private readonly string UNKNOWN = Locale.localizedString("Unknown");
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

        public abstract PathFilter Filter();

        public void Add(Path p)
        {
            _roots.Add(p);
        }

        public bool CanExpand(object reference)
        {
            Path path = ((TreePathReference) reference).Unique;            
            return path.attributes().isDirectory();
        }

        public IEnumerable<TreePathReference> ChildrenGetter(object reference)
        {
            Path path = ((TreePathReference) reference).Unique;

            Console.WriteLine("ChildrenGetter: " + path.toString());

            AttributedList list;
            lock (_isLoadingListingInBackground)
            {
                // Check first if it hasn't been already requested so we don't spawn
                // a multitude of unecessary threads
                if (!_isLoadingListingInBackground.Contains(path))
                {
                    if (Transfer.cache().containsKey(path.getReference()))
                    {
                        list = Transfer.cache().get(path.getReference(), new NullComparator(), Filter());
                        for (int i = 0; i < list.size(); i++)
                        {
                            yield return new TreePathReference((Path) list.get(i));
                        }
                        yield break;
                    }
                    _isLoadingListingInBackground.Add(path);

                    // Reloading a workdir that is not cached yet would cause the interface to freeze;
                    // Delay until path is cached in the background
                    _controller.Background(new ChildGetterTransferPromptBackgrounAction(_controller, Transfer, path,
                                                                                        _isLoadingListingInBackground));
                }
            }
            list = Transfer.cache().get(path.getReference(), new NullComparator(), Filter());
            for (int i = 0; i < list.size(); i++)
            {
                yield return new TreePathReference((Path) list.get(i));
            }
            yield break;
        }

        public object GetName(object reference)
        {
            return ((TreePathReference) reference).Unique.getName();
        }

        public object GetModified(object reference)
        {
            long modificationDate = ((TreePathReference) reference).Unique.attributes().getModificationDate();
            if (modificationDate != -1)
            {
                return UserDefaultsDateFormatter.ConvertJavaMiliSecondToDateTime(modificationDate);
            }
            return UNKNOWN;
        }

        public abstract object GetSize(object path);

        public string GetSizeAsString(object size)
        {
            return Status.getSizeAsString((long) size);
        }

        public object GetIcon(object rowobject)
        {
            return IconCache.Instance.IconForPath(((TreePathReference) rowobject).Unique, IconCache.IconSize.Small);
        }

        public CheckState GetCheckState(Object reference)
        {
            Path path = ((TreePathReference) reference).Unique;
            bool included = Transfer.isIncluded(path) && !_controller.Action.equals(TransferAction.ACTION_SKIP);
            if (included)
            {
                return CheckState.Checked;
            }
            return CheckState.Unchecked;
        }

        public IEnumerable<TreePathReference> GetEnumerator()
        {
            foreach (Path path in _roots)
            {
                yield return new TreePathReference(path);
            }
        }

        public CheckState SetCheckState(object reference, CheckState newValue)
        {
            Path path = ((TreePathReference) reference).Unique;
            if (!path.status().isSkipped())
            {
                Transfer.setSelected(path, newValue == CheckState.Checked ? true : false);
                return newValue;
            }
            //simulate enabled=false for the checkbox field
            //see TransferPromptController.java,outlineView_willDisplayCell_forTableColumn_item
            return newValue == CheckState.Checked ? CheckState.Unchecked : CheckState.Checked;
        }

        public abstract object GetWarningImage(object reference);

        public virtual object GetCreateImage(object reference)
        {
            return null;
        }

        public virtual object GetSyncGetter(object reference)
        {
            return null;
        }

        public bool IsActive(TreePathReference reference)
        {
            return Transfer.isIncluded(reference.Unique);
        }

        private class ChildGetterTransferPromptBackgrounAction : AbstractBackgroundAction
        {
            private readonly TransferPromptController _controller;
            private readonly List<Path> _isLoadingListingInBackground;
            private readonly Path _path;
            private readonly Transfer _transfer;

            public ChildGetterTransferPromptBackgrounAction(TransferPromptController controller,
                                                            Transfer transfer,
                                                            Path path,
                                                            List<Path> isLoadingListingInBackground)
            {
                _controller = controller;
                _transfer = transfer;
                _path = path;
                _isLoadingListingInBackground = isLoadingListingInBackground;
            }

            public override object @lock()
            {
                return _transfer.getSession();
            }

            public override bool prepare()
            {
                AsyncController.AsyncDelegate mainAction = () => _controller.View.StartActivityAnimation();
                _controller.Invoke(mainAction);
                return base.prepare();
            }

            public override void run()
            {
                _transfer.children(_path);
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
                        //_controller.ReloadData();
                        Console.WriteLine("Und jetzt refresh: " +_path);
                        _controller.RefreshObject(_path);
                    }
                }
            }

            public override void finish()
            {
                AsyncController.AsyncDelegate mainAction = delegate
                                                               {
                                                                   _controller.View.StopActivityAnimation();
                                                                   _controller.UpdateStatusLabel();
                                                               };
                Console.WriteLine("und schliesslich finish:" +_path);
                _controller.Invoke(mainAction);
            }
        }
    }

    internal abstract class PromptFilter : PathFilter
    {
        public virtual bool accept(AbstractPath ap)
        {
            Path file = (Path) ap;
            if (file.exists())
            {
                if (file.attributes().getSize() == -1)
                {
                    file.readSize();
                }
                if (file.attributes().getModificationDate() == -1)
                {
                    file.readTimestamp();
                }
            }
            return true;
        }
    }
}