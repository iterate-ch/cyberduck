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

using Ch.Cyberduck.Ui.Winforms.Threading;
using ch.cyberduck.core;
using ch.cyberduck.core.threading;
using ch.cyberduck.ui.threading;
using java.util;

namespace Ch.Cyberduck.Ui.Controller.Threading
{
    public abstract class BrowserBackgroundAction : ControllerBackgroundAction
    {
        private readonly BackgroundActionRegistry _registry = BackgroundActionRegistry.global();

        protected BrowserBackgroundAction(BrowserController controller)
            : base(controller, new DialogAlertCallback(controller), controller, controller)
        {
            BrowserController = controller;
        }

        public BrowserController BrowserController { get; private set; }

        public override List getSessions()
        {
            Session session = BrowserController.getSession();
            if (null == session)
            {
                return Collections.emptyList();
            }
            return Collections.singletonList(session);
        }

        public override void prepare()
        {
            AsyncController.AsyncDelegate mainAction = delegate { BrowserController.View.StartActivityAnimation(); };
            BrowserController.Invoke(mainAction);
            base.prepare();
        }

        public override void finish()
        {
            AsyncController.AsyncDelegate mainAction = delegate { BrowserController.View.StopActivityAnimation(); };
            BrowserController.Invoke(mainAction);
            base.finish();
        }

        protected override void connect(Session session)
        {
            base.connect(session);

            Host bookmark = session.getHost();
            HistoryCollection history = HistoryCollection.defaultCollection();
            history.add(new Host(bookmark.serialize(SerializerFactory.get())));

            // Notify changed bookmark
            if (BookmarkCollection.defaultCollection().contains(bookmark))
            {
                BookmarkCollection.defaultCollection().collectionItemChanged(bookmark);
            }
        }

        public override void init()
        {
            // Add to the registry so it will be displayed in the activity window.
            _registry.add(this);
            base.init();
        }

        public override void cleanup()
        {
            _registry.remove(this);
            base.cleanup();
        }
    }
}