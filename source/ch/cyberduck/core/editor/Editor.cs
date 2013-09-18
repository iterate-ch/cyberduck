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
using Ch.Cyberduck.Ui.Controller;
using Ch.Cyberduck.Ui.Controller.Threading;
using ch.cyberduck.core;
using ch.cyberduck.core.editor;
using ch.cyberduck.core.exception;
using ch.cyberduck.core.local;
using java.util.concurrent;
using org.apache.log4j;

namespace Ch.Cyberduck.Core.Editor
{
    public abstract class Editor : AbstractEditor
    {
        private static readonly Logger Log = Logger.getLogger(typeof (Editor).Name);
        private readonly BrowserController _controller;

        protected Editor(BrowserController controller, Session session, Application application, Path path)
            : base(application, session, path)
        {
            _controller = controller;
        }

        protected override void open(TransferCallable download)
        {
            if (Log.isDebugEnabled())
            {
                Log.debug(String.Format("Open {0} in {1}", edited.getLocal().getAbsolute(), getApplication()));
            }
            _controller.background(new OpenBackgroundAction(_controller, this, download));
        }

        protected override void save(TransferCallable upload)
        {
            if (Log.isDebugEnabled())
            {
                Log.debug(String.Format("Save changes from {0} for {1}", getApplication().getIdentifier(),
                                        edited.getLocal().getAbsolute()));
            }
            _controller.background(new SaveBackgroundAction(_controller, this, upload));
        }

        private class OpenBackgroundAction : BrowserBackgroundAction
        {
            private readonly Callable _download;
            private readonly AbstractEditor _editor;

            public OpenBackgroundAction(BrowserController controller, AbstractEditor editor, Callable download)
                : base(controller)
            {
                _editor = editor;
                _download = download;
            }

            public override object run()
            {
                try
                {
                    _download.call();
                }
                catch (BackgroundException backgroundException)
                {
                    throw backgroundException;
                }
                catch (Exception e)
                {
                    throw new BackgroundException(e.Message);
                }
                return true;
            }

            public override string getActivity()
            {
                return string.Format(LocaleFactory.localizedString("Downloading {0}", "Status"),
                                     _editor.getEdited().getName());
            }
        }

        private class SaveBackgroundAction : BrowserBackgroundAction
        {
            private readonly AbstractEditor _editor;
            private readonly Callable _upload;

            public SaveBackgroundAction(BrowserController controller, AbstractEditor editor, Callable upload)
                : base(controller)
            {
                _editor = editor;
                _upload = upload;
            }

            public override object run()
            {
                try
                {
                    _upload.call();
                }
                catch (BackgroundException backgroundException)
                {
                    throw backgroundException;
                }
                catch (Exception e)
                {
                    throw new BackgroundException(e.Message);
                }
                return true;
            }

            public override string getActivity()
            {
                return string.Format(LocaleFactory.localizedString("Uploading {0}", "Status"),
                                     _editor.getEdited().getName());
            }
        }
    }
}