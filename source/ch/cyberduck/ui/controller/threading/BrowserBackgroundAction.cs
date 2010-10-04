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
using ch.cyberduck.core.threading;

namespace Ch.Cyberduck.Ui.Controller.Threading
{
    public abstract class BrowserBackgroundAction : AlertRepeatableBackgroundAction
    {
        protected BrowserBackgroundAction(BrowserController controller) : base(controller)
        {
            BrowserController = controller;
        }

        public BrowserController BrowserController { get; private set; }

        protected override Session getSession()
        {
            return BrowserController.getSession();
        }

        public override object @lock()
        {
            return getSession();
        }

        public override void init()
        {
            // Add to the registry so it will be displayed in the activity window.
            BackgroundActionRegistry.instance().add(this);
        }

        public override bool prepare()
        {
            AsyncController.AsyncDelegate mainAction = delegate
            {
                BrowserController.View.StartActivityAnimation();
                BrowserController.UpdateStatusLabel(getActivity());
            };
            BrowserController.Invoke(mainAction);
            return base.prepare();
        }

        public override void finish()
        {
            AsyncController.AsyncDelegate mainAction = delegate
            {
                BrowserController.View.StopActivityAnimation();
                BrowserController.UpdateStatusLabel();
            };
            BrowserController.Invoke(mainAction);
            base.finish();
        }
    }
}