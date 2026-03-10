// Copyright (c) 2010-2026 Yves Langisch. All rights reserved.
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

using System.Windows.Forms;
using ch.cyberduck.core;
using ch.cyberduck.core.exception;
using ch.cyberduck.ui.core;
using StructureMap;
using static Ch.Cyberduck.ImageHelper;

namespace Ch.Cyberduck.Ui.Controller
{
    public class PromptPasswordController : WindowController<IView>, PasswordCallback
    {
        private readonly IWindowController _browser;
        private IPasswordPromptView _view;

        public PromptPasswordController(IWindowController c)
        {
            _browser = c;
        }

        public void close(string input)
        {
            _view.InputText = input;
            _view.Close();
        }

        public Credentials prompt(Host bookmark, string title, string reason, LoginOptions options)
        {
            Credentials credentials = new Credentials().setSaved(options.save());
            AsyncDelegate d = delegate
            {
                View = ObjectFactory.GetInstance<IPasswordPromptView>();
                _view = (IPasswordPromptView)View;
                _view.Title = title;
                _view.Reason = new StringAppender().append(reason).toString();
                _view.OkButtonText = LocaleFactory.localizedString("Continue", "Credentials");
                _view.SkipButtonText = LocaleFactory.localizedString("Skip", "Transfer");
                _view.IconView = Images.Get(options.icon()).Size(64);
                _view.SavePasswordEnabled = options.keychain();
                _view.SavePasswordState = credentials.isSaved();
                _view.CanSkip = options.anonymous();

                _view.ValidateInput += ValidateInputEventHandler;
                switch (View.ShowDialog(_browser.Window))
                {
                    case DialogResult.Cancel:
                        throw new LoginCanceledException();

                    case DialogResult.Ignore:
                        credentials.setPassword(string.Empty);
                        break;

                    default:
                        credentials.setPassword(_view.InputText.Trim());
                        credentials.setSaved(_view.SavePasswordState);
                        break;
                }
            };
            _browser.Invoke(d, true);
            return credentials;
        }

        private bool ValidateInputEventHandler()
        {
            return !string.IsNullOrWhiteSpace(_view.InputText);
        }
    }
}
