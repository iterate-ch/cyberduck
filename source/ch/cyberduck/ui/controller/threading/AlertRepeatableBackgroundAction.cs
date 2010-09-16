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
using System.Collections.Generic;
using System.Windows.Forms;
using ch.cyberduck.core;
using Ch.Cyberduck.Core;
using ch.cyberduck.core.threading;
using StructureMap;

namespace Ch.Cyberduck.Ui.Controller.Threading
{
    public abstract class AlertRepeatableBackgroundAction : RepeatableBackgroundAction
    {
        private readonly WindowController _controller;

        protected AlertRepeatableBackgroundAction(WindowController controller)
        {
            _controller = controller;
        }

        public WindowController Controller
        {
            get { return _controller; }
        }

        public override void finish()
        {
            base.finish();
            // If there was any failure, display the summary now
            if (hasFailed() && !isCanceled())
            {
                // Display alert if the action was not canceled intentionally
                Alert();
            }
        }

        /// <summary>
        /// Display an alert dialog with a summary of all failed tasks
        /// </summary>
        protected void Alert()
        {
            _controller.Invoke(delegate
                                   {
                                       ICollection<BackgroundException> backgroundExceptions =
                                           Utils.ConvertFromJavaList<BackgroundException>(getExceptions());

                                       ErrorController errorController =
                                           new ErrorController(ObjectFactory.GetInstance<IErrorView>(),
                                                               backgroundExceptions, getTranscript());
                                       DialogResult result = errorController.View.ShowDialog(_controller.View);

                                       if (DialogResult.OK == result)
                                       {
                                           foreach (BackgroundException e in backgroundExceptions)
                                           {
                                               Path workdir = e.getPath();
                                               if (null == workdir)
                                               {
                                                   continue;
                                               }
                                               workdir.invalidate();
                                           }
                                           reset();
                                           // Re-run the action with the previous lock used
                                           _controller.background(this);
                                       }
                                   }, true);
        }
    }
}