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
using Ch.Cyberduck.Core;
using Ch.Cyberduck.Ui.Winforms;
using ch.cyberduck.core;
using ch.cyberduck.core.formatter;
using ch.cyberduck.core.transfer;
using ch.cyberduck.ui.action;
using ch.cyberduck.ui.threading;
using java.util;
using org.apache.log4j;

namespace Ch.Cyberduck.Ui.Controller
{
    internal abstract class TransferPromptModel
    {
        protected static Logger log = Logger.getLogger(typeof (TransferPromptModel).FullName);
        protected readonly Transfer Transfer;

        private readonly string UNKNOWN = LocaleFactory.localizedString("Unknown");
        private readonly Cache _cache = new Cache(int.MaxValue);
        private readonly TransferPromptController _controller;

        private readonly List<Path> _roots = new List<Path>();

        /**
         * Selection status map in the prompt
         */
        private readonly IDictionary<Path, CheckState> _selected = new Dictionary<Path, CheckState>();
        private readonly Session _session;

        /**
          * Transfer status determined by filters
          */
        protected Bitmap AlertIcon = IconCache.Instance.IconForName("alert");
        private TransferAction _action;
        protected IDictionary<Path, TransferStatus> _status = new Dictionary<Path, TransferStatus>();


        protected TransferPromptModel(TransferPromptController controller, Session session, Transfer transfer)
        {
            _controller = controller;
            _session = session;
            Transfer = transfer;
            _action =
                TransferAction.forName(
                    Preferences.instance()
                               .getProperty(String.Format("queue.prompt.{0}.action.default", transfer.getType().name())));
        }

        public virtual void Add(Path p)
        {
            _roots.Add(p);
        }

        public bool CanExpand(object path)
        {
            return ((Path) path).isDirectory();
        }

        public IEnumerable<Path> ChildrenGetter(object p)
        {
            Path directory = ((Path) p);
            if (null == directory)
            {
                // Root
                if (!_cache.isCached(null))
                {
                    _cache.put(null, new AttributedList(Transfer.getRoots()));
                    Filter();
                }
            }
            else if (!_cache.isCached(directory.getReference()))
            {
                _controller.Background(new TransferPromptListAction(this, _controller, _session, directory, Transfer,
                                                                    _cache));
            }
            // Return list with filtered files included
            AttributedList list = _cache.get(null == directory ? null : directory.getReference());
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

        public object GetSize(Path path)
        {
            TransferStatus status = GetStatus(path);
            return status.getLength();
        }

        public TransferStatus GetStatus(Path path)
        {
            if(!_status.ContainsKey(path)) {
                // Transfer filter background task has not yet finished
                return new TransferStatus();
            }
            TransferStatus status = _status[path];
            return status;
        }

        public string GetSizeAsString(object size)
        {
            return SizeFormatterFactory.get().format((long) size);
        }

        public object GetIcon(Path path)
        {
            return IconCache.Instance.IconForPath(path, IconCache.IconSize.Small);
        }

        private bool IsFiltered(Path p)
        {
            return !_status.ContainsKey(p);
        }

        public bool IsSelected(Path p)
        {
            if (_selected.ContainsKey(p))
            {
                return _selected[p] == CheckState.Checked;
            }
            return true;
        }

        public CheckState GetCheckState(Object p)
        {
            Path path = (Path) p;
            if (IsFiltered(path))
            {
                return CheckState.Unchecked;
            }
            return IsSelected(path) ? CheckState.Checked : CheckState.Unchecked;
        }

        public CheckState SetCheckState(object p, CheckState newValue)
        {
            _selected[(Path) p] = newValue;
            return newValue;
        }

        public object GetWarningImage(Path path)
        {
            TransferStatus status = GetStatus(path);
            if (path.attributes().isFile())
            {
                if (status.getLength() == 0)
                {
                    return AlertIcon;
                }
            }
            return null;
        }

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
            return _status.ContainsKey(path);
        }

        /// <summary>
        /// Change transfer action and reload list of files
        /// </summary>
        /// <param name="action">Transfer action</param>
        public void SetAction(TransferAction action)
        {
            _action = action;
            Filter();
        }

        private void Filter()
        {
            _controller.background(new FilterAction(this, _controller, _session, Transfer, _action, _cache));
        }

        private class FilterAction : WorkerBackgroundAction
        {
            public FilterAction(TransferPromptModel model, TransferPromptController controller, Session session,
                                Transfer transfer, TransferAction action, Cache cache)
                : base(
                    controller, session,
                    new InnerTransferPromptFilterWorker(model, controller, session, transfer, action, cache))
            {
            }

            private class InnerTransferPromptFilterWorker : TransferPromptFilterWorker
            {
                private readonly TransferPromptController _controller;
                private readonly TransferPromptModel _model;

                public InnerTransferPromptFilterWorker(TransferPromptModel model, TransferPromptController controller,
                                                       Session session, Transfer transfer, TransferAction action,
                                                       Cache cache) : base(session, transfer, action, cache)
                {
                    _model = model;
                    _controller = controller;
                }

                public override void cleanup(object result)
                {
                    IDictionary<Path, TransferStatus> map = Utils.ConvertFromJavaMap<Path, TransferStatus>((Map) result);
                    _model._status = map;
                    _controller.ReloadData(_model._roots);
                }
            }
        }


        private class TransferPromptListAction : WorkerBackgroundAction
        {
            public TransferPromptListAction(TransferPromptModel model, TransferPromptController controller,
                                            Session session, Path directory, Transfer transfer, Cache cache)
                : base(
                    controller, session, new InnerTransferPromptListWorker(model, session, transfer, directory, cache))
            {
            }

            private class InnerTransferPromptListWorker : TransferPromptListWorker
            {
                private readonly Cache _cache;
                private readonly Path _directory;
                private readonly TransferPromptModel _model;

                public InnerTransferPromptListWorker(TransferPromptModel model, Session session, Transfer transfer,
                                                     Path directory, Cache cache) : base(session, transfer, directory)
                {
                    _model = model;
                    _directory = directory;
                    _cache = cache;
                }

                public override void cleanup(object list)
                {
                    _cache.put(_directory.getReference(), (AttributedList) list);
                    _model.Filter();
                }
            }
        }
    }
}