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
using Ch.Cyberduck.Core.Resources;
using Ch.Cyberduck.Ui.Controller;
using StructureMap;

namespace Ch.Cyberduck.windows.src.main.csharp.ch.cyberduck.ui.controller
{
    class PromptPasswordController : PasswordCallback
    {
        private readonly WindowController _browser;

        public PromptPasswordController(WindowController c)
        {
            _browser = c;
        }

        public IPasswordView View { get; set; }

        public void prompt(Credentials credentials, string title, string reason, LoginOptions options)
        {
            View = ObjectFactory.GetInstance<IPasswordView>();
            View.Title = title;
            View.Reason = reason;
            View.PasswordLabel = credentials.getPasswordPlaceholder();
            View.PwdIcon = IconCache.Instance.IconForName(options.icon(), 64);
            View.SavePasswordState = false;
            AsyncController.AsyncDelegate d = delegate
            {
                if (DialogResult.Cancel == View.ShowDialog(_browser.View))
                {
                    throw new LoginCanceledException();
                }
                credentials = new Credentials(null, View.Password);
                credentials.setSaved(View.SavePasswordState);
            };
            _browser.Invoke(d);
        }
    }
}