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

using Ch.Cyberduck.Ui.Controller;
using Ch.Cyberduck.Ui.Controller.Threading;
using ch.cyberduck.core;
using ch.cyberduck.core.editor;
using ch.cyberduck.core.local;
using ch.cyberduck.core.threading;
using org.apache.log4j;

namespace Ch.Cyberduck.Core.Editor
{
    public abstract class Editor : AbstractEditor
    {
        private static readonly Logger Log = Logger.getLogger(typeof (Editor).Name);
        private readonly BrowserController _controller;

        protected Editor(BrowserController controller, Application application, Path path) : base(application, path)
        {
            _controller = controller;
        }

        protected override void open(BackgroundAction download)
        {
            _controller.background(new OpenBackgroundAction(_controller, download));
        }

        protected override void save(BackgroundAction upload)
        {
            _controller.background(new SaveBackgroundAction(_controller, upload));
        }

        private class OpenBackgroundAction : BrowserBackgroundAction
        {
            private readonly BackgroundAction _download;

            public OpenBackgroundAction(BrowserController controller, BackgroundAction download) : base(controller)
            {
                _download = download;
            }

            public override void run()
            {
                _download.run();
            }

            public override string getActivity()
            {
                return _download.getActivity();
            }

            public override void cleanup()
            {
                _download.cleanup();
            }
        }

        private class SaveBackgroundAction : BrowserBackgroundAction
        {
            private readonly BackgroundAction _upload;

            public SaveBackgroundAction(BrowserController controller, BackgroundAction upload) : base(controller)
            {
                _upload = upload;
            }

            public override void run()
            {
                _upload.run();
            }

            public override string getActivity()
            {
                return _upload.getActivity();
            }

            public override void cleanup()
            {
                _upload.cleanup();
            }
        }
    }
}