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
using ch.cyberduck.core.vault;
using ch.cyberduck.core.exception;
using Ch.Cyberduck.Core;
using StructureMap;
using static Ch.Cyberduck.ImageHelper;
using ch.cyberduck.ui.core;

namespace Ch.Cyberduck.Ui.Controller
{
    public class PromptPasswordController : WindowController<IPasswordPromptView>, PasswordCallback
    {
        private readonly IWindowController _browser;

        public PromptPasswordController(IWindowController c)
        {
            _browser = c;
        }

        public void close(string input)
        {
            View.InputText = input;
            View.Close();
        }

        public Credentials prompt(Host bookmark, string title, string reason, LoginOptions options)
        {
            Credentials credentials = new Credentials().withSaved(options.save());
            AsyncDelegate d = delegate
            {
                View = ObjectFactory.GetInstance<IPasswordPromptView>();
                View.Title = title;
                View.Reason = new StringAppender().append(reason).toString();
                View.OkButtonText = LocaleFactory.localizedString("Continue", "Credentials");
                View.SkipButtonText = LocaleFactory.localizedString("Skip", "Transfer");
                View.IconView = Images.Get(options.icon()).Size(64);
                View.SavePasswordEnabled = options.keychain();
                View.SavePasswordState = credentials.isSaved();
                View.CanSkip = options.anonymous();

                View.ValidateInput += ValidateInputEventHandler;
                switch (View.ShowDialog(_browser.Window))
                {
                    case DialogResult.Cancel:
                        throw new LoginCanceledException();

                    case DialogResult.Ignore:
                        credentials.setPassword(string.Empty);
                        break;

                    default:
                        credentials.setPassword(View.InputText.Trim());
                        credentials.setSaved(View.SavePasswordState);
                        break;
                }
            };
            _browser.Invoke(d);
            return credentials;
        }

        private bool ValidateInputEventHandler()
        {
            return !string.IsNullOrWhiteSpace(View.InputText);
        }
    }
}
