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
using System.Windows.Forms;
using ch.cyberduck.core.threading;
using Ch.Cyberduck.Ui.Winforms.Taskdialog;

namespace Ch.Cyberduck.Ui.Controller
{
    public abstract class WindowController<T> : WindowController where T : IView
    {
        private T _view;

        public new T View
        {
            get { return _view; }
            set
            {
                _view = value;
                base.View = _view;
            }
        }
    }

    public abstract class WindowController : AsyncController
    {
        private bool _invalidated;



        /// <summary>
        /// 
        /// </summary>
        /// <value>True if the controller window is on screen.</value>
        public bool Visible
        {
            get { return View.Visible; }
        }

        public override IView View
        {
            get { return base.View; }
            set
            {
                base.View = value;
                //if (!Singleton)
                {
                    View.ViewClosingEvent += ViewClosingEvent;                    
                    /*
                    View.ViewClosedEvent += delegate
                                                {
                                                    Invalidate();
                                                    _invalidated = true;
                                                    Console.WriteLine("Invalidated!");
                                                };
                     */
                    
                    View.ViewDisposedEvent += delegate
                                                  {
                                                      //todo should be deregister delegate? wie oben? braucht wohl sowieso noch refactoring wegen threading issues
                                                      Invalidate();
                                                      _invalidated = true;
                                                  };

                }
                View.ReleaseWhenClose = !Singleton;
            }
        }

        private void ViewClosingEvent(object sender, FormClosingEventArgs args)
        {            
            if (View.ModalResult == DialogResult.Cancel)
            {
                args.Cancel = false;
                return;
            }

            bool shouldClose = ViewShouldClose();            
            args.Cancel = !shouldClose;
            if (shouldClose)
            {
                //Invalidate();
                //_invalidated = true;
                View.ViewClosingEvent -= ViewClosingEvent;
                //Console.WriteLine("Invalidated!");
            }
        }

        protected void ForceCloseView()
        {
            Invalidate();
            _invalidated = true;
            View.ViewClosingEvent -= ViewClosingEvent;
            View.Close();
        }

        public virtual bool ViewShouldClose()
        {
            return true;
        }

        public bool Invalidated
        {
            get { return _invalidated; }
        }

        private bool IsInvokeAllowed
        {
            get { return !_invalidated || Singleton; }
        }

        /// <summary>
        /// A singleton window is not released when closed and the controller is not invalidated
        /// </summary>
        public virtual bool Singleton
        {
            get { return false; }
        }

        public override void invoke(MainAction mainAction, bool wait)
        {
            //Make sure that we can call invoke on the view
            //todo klappt nicht immer, weil threaded und der zustand nach dem if (im falle von true) noch ändern kann :(
            if (IsInvokeAllowed)
            {                
                base.invoke(mainAction, wait);
            }
        }

        protected virtual void Invalidate()
        {
            ;
        }

        public DialogResult MessageBox(string title, string message, string content, eTaskDialogButtons buttons,
                                       eSysIcons icon)
        {
            return View.MessageBox(title, message, content, buttons, icon);
        }
    }
}