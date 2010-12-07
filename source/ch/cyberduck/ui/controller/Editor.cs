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
using ch.cyberduck.core;
using ch.cyberduck.core.editor;
using ch.cyberduck.core.threading;
using Ch.Cyberduck.Ui.Controller.Threading;
using org.apache.log4j;

namespace Ch.Cyberduck.Ui.Controller
{
    public abstract class Editor : AbstractEditor
    {
        private static readonly Logger Log = Logger.getLogger(typeof (Editor).FullName);

        private readonly BrowserController controller;

        /// <summary>
        /// The editor application
        /// </summary>
        protected string BundleIdentifier;

        public Editor(BrowserController controller, string bundleIdentifier, Path path) : base(path)
        {
            this.controller = controller;
            BundleIdentifier = bundleIdentifier;
        }

        protected override void open(BackgroundAction download)
        {
            controller.background(new OpenBackgroundAction(controller, download));
        }

        protected override void save(BackgroundAction upload)
        {
            controller.background(new SaveBackgroundAction(controller, upload));
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