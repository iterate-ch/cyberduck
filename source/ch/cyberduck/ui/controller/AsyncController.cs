// 
// Copyright (c) 2010-2011 Yves Langisch. All rights reserved.
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
using ch.cyberduck.core.threading;
using ch.cyberduck.ui;
using Ch.Cyberduck.Ui.Controller.Threading;

namespace Ch.Cyberduck.Ui.Controller
{
    public abstract class AsyncController : AbstractController
    {
        public delegate void AsyncDelegate();

        public delegate void SyncDelegate();

        public virtual IView View { get; set; }

        public void Background(AsyncDelegate del, AsyncDelegate cleanup)
        {
            // Move to background thread
            background(new AsyncDelegateBackgroundAction(del, cleanup));
        }

        public void Background(BackgroundAction backgroundAction)
        {
            // Move to background thread
            background(backgroundAction);
        }

        public void Invoke(AsyncDelegate del, bool wait)
        {
            ControllerMainAction mainAction = new SimpleDefaultMainAction(this, del);
            invoke(mainAction, wait);
        }

        public void Invoke(AsyncDelegate del)
        {
            Invoke(del, false);
        }

        public void Invoke(MainAction mainAction)
        {
            invoke(mainAction);
        }

        public override void invoke(MainAction mainAction, bool wait)
        {
            try
            {
                if (!View.IsHandleCreated)
                {
                    return;
                }

                if (View.InvokeRequired)
                {
                    //currently only sync
                    if (true)
                    {
                        View.Invoke(new AsyncDelegate(mainAction.run), null);
                    }
                    else
                    {
                        View.BeginInvoke(new AsyncDelegate(mainAction.run), null);
                    }
                }
                else
                {
                    mainAction.run();
                }
            }
            catch (ObjectDisposedException)
            {
                //happens because there is no synchronization between the lifecycle of a form and callbacks of background threads.
                //catch silently                
            }
        }

        public override bool isMainThread()
        {
            return !View.InvokeRequired;
        }
    }

    public class AsyncDelegateBackgroundAction : AbstractBackgroundAction
    {
        private readonly AsyncController.AsyncDelegate _background;
        private readonly AsyncController.AsyncDelegate _cleanup;

        public AsyncDelegateBackgroundAction(AsyncController.AsyncDelegate background,
                                             AsyncController.AsyncDelegate cleanup)
        {
            _background = background;
            _cleanup = cleanup;
        }

        public override void run()
        {
            _background();
        }

        public override void cleanup()
        {
            _cleanup();
        }
    }
}