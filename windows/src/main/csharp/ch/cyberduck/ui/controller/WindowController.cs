// 
// Copyright (c) 2010-2016 Yves Langisch. All rights reserved.
// http://cyberduck.io/
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
// feedback@cyberduck.io
// 

using System.Windows.Forms;
using ch.cyberduck.core;
using ch.cyberduck.core.exception;
using ch.cyberduck.core.threading;
using Ch.Cyberduck.Core.TaskDialog;
using Ch.Cyberduck.Ui.Winforms.Threading;
using java.lang;

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
                View.ViewClosingEvent += ViewClosingEvent;
                View.ReleaseWhenClose = !Singleton;
            }
        }

        public bool Invalidated
        {
            get { return _invalidated; }
        }


        private bool IsInvokeAllowed
        {
            get { return !(View.IsDisposed || _invalidated); }
        }

        /// <summary>
        /// A singleton window is not released when closed and the controller is not invalidated
        /// </summary>
        public virtual bool Singleton
        {
            get { return false; }
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
                Invalidate();
                _invalidated = true;
                View.ViewClosingEvent -= ViewClosingEvent;
            }
        }

        public virtual bool ViewShouldClose()
        {
            return true;
        }

        public override void invoke(MainAction mainAction, bool wait)
        {
            if (IsInvokeAllowed)
            {
                base.invoke(mainAction, wait);
            }
        }

        protected virtual void Invalidate()
        {
            invalidate();
        }

        /// <summary>
        /// 
        /// </summary>
        /// <param name="title"></param>
        /// <param name="message"></param>
        /// <param name="detail"></param>
        /// <param name="help"></param>
        /// <returns></returns>
        public TaskDialogResult MessageBox(string title, string message, string detail, string help)
        {
            return View.MessageBox(title, message, detail, null, help, null, delegate { });
        }

        /// <summary>
        /// 
        /// </summary>
        /// <param name="title"></param>
        /// <param name="message"></param>
        /// <param name="detail"></param>
        /// <param name="commandButtons"></param>
        /// <param name="showCancelButton"></param>
        /// <returns></returns>
        public TaskDialogResult QuestionBox(string title, string message, string detail, string commandButtons,
            bool showCancelButton)
        {
            return CommandBox(title, message, detail, commandButtons, showCancelButton, null, TaskDialogIcon.Question,
                delegate { });
        }

        /// <summary>
        /// 
        /// </summary>
        /// <param name="title"></param>
        /// <param name="message"></param>
        /// <param name="detail"></param>
        /// <param name="expanded">Text expandable container</param>
        /// <param name="commandButtons"></param>
        /// <param name="showCancelButton"></param>
        /// <param name="help"></param>
        /// <returns></returns>
        public TaskDialogResult WarningBox(string title, string message, string detail, string expanded,
            string commandButtons, bool showCancelButton, string help, DialogResponseHandler handler)
        {
            return View.CommandBox(title, message, detail, expanded, help, null, commandButtons, showCancelButton,
                TaskDialogIcon.Warning, TaskDialogIcon.Information, handler);
        }

        /// <summary>
        /// 
        /// </summary>
        /// <param name="title"></param>
        /// <param name="message"></param>
        /// <param name="detail"></param>
        /// <param name="commandButtons"></param>
        /// <param name="verificationText"></param>
        /// <param name="showCancelButton"></param>
        /// <param name="mainIcon"></param>
        /// <returns></returns>
        public TaskDialogResult InfoBox(string title, string message, string detail, string commandButtons,
            string verificationText, bool showCancelButton)
        {
            return CommandBox(title, message, detail, commandButtons, showCancelButton, verificationText,
                TaskDialogIcon.Information, null, delegate { });
        }

        /// <summary>
        /// 
        /// </summary>
        /// <param name="title"></param>
        /// <param name="message"></param>
        /// <param name="detail"></param>
        /// <param name="commandButtons"></param>
        /// <param name="showCancelButton"></param>
        /// <param name="verificationText"></param>
        /// <param name="mainIcon"></param>
        /// <param name="handler"></param>
        /// <returns></returns>
        public TaskDialogResult CommandBox(string title, string message, string detail, string commandButtons,
            bool showCancelButton, string verificationText, TaskDialogIcon mainIcon, DialogResponseHandler handler)
        {
            return CommandBox(title, message, detail, commandButtons, showCancelButton, verificationText, mainIcon, null,
                handler);
        }

        /// <summary>
        /// 
        /// </summary>
        /// <param name="title"></param>
        /// <param name="message"></param>
        /// <param name="detail"></param>
        /// <param name="commandButtons"></param>
        /// <param name="showCancelButton"></param>
        /// <param name="verificationText"></param>
        /// <param name="mainIcon"></param>
        /// <param name="help">Help URL</param>
        /// <param name="handler"></param>
        /// <returns></returns>
        public TaskDialogResult CommandBox(string title, string message, string detail, string commandButtons,
            bool showCancelButton, string verificationText, TaskDialogIcon mainIcon, string help,
            DialogResponseHandler handler)
        {
            return View.CommandBox(title, message, detail, null, help, verificationText, commandButtons,
                showCancelButton, mainIcon, TaskDialogIcon.Information, handler);
        }
    }
}