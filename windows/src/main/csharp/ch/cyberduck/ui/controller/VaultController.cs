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

using ch.cyberduck.core;
using ch.cyberduck.core.features;
using ch.cyberduck.core.threading;
using ch.cyberduck.core.vault;
using ch.cyberduck.core.worker;
using ch.cyberduck.core.preferences;
using ch.cyberduck.ui.browser;
using Ch.Cyberduck.Core;
using java.util;
using System;
using System.Collections.Generic;
using System.Drawing;
using System.Text;
using System.Windows.Forms;
using static Ch.Cyberduck.ImageHelper;

namespace Ch.Cyberduck.Ui.Controller
{
    class VaultController : FolderController
    {
        private readonly INewVaultPromptView _view;

        public VaultController(INewVaultPromptView view, BrowserController browserController,
            IList<Location.Name> regions, Location.Name defaultRegion) : base(view, browserController, regions, defaultRegion)
        {
            _view = view;
            _view.EnablePassphrase();
            _view.ValidateInput += ValidateInputEventHandler;
        }

        public override Image IconView => Images.Cryptomator.Size(64);

        private bool ValidateInputEventHandler()
        {
            if (Utils.IsBlank(_view.Passphrase))
            {
                return false;
            }
            if (Utils.IsBlank(_view.PassphraseConfirm))
            {
                return false;
            }
            if (!_view.Passphrase.Equals(_view.PassphraseConfirm))
            {
                return false;
            }
            return true;
        }

        public override void Callback(DialogResult result)
        {
            if (DialogResult.OK == result)
            {
                BrowserController.background(new CreateVaultAction(BrowserController,
                    new UploadTargetFinder(Workdir).find(BrowserController.SelectedPath), View.InputText,
                    HasLocation() ? _view.Region : null, _view.Passphrase));
            }
        }

        private class CreateVaultAction : WorkerBackgroundAction
        {
            public CreateVaultAction(BrowserController controller, Path directory, string filename, string region,
                string passphrase)
                : base(
                    controller, controller.Session,
                    new InnerCreateVaultWorker(controller,
                        new Path(directory, filename, EnumSet.of(AbstractPath.Type.directory)), filename, region,
                        passphrase))
            {
            }

            private class InnerCreateVaultWorker : CreateVaultWorker
            {
                private readonly BrowserController _controller;
                private readonly string _filename;
                private readonly Path _folder;

                public InnerCreateVaultWorker(BrowserController controller, Path folder, String filename,
                    String region, String passphrase)
                    : base(region, new VaultCredentials(passphrase), PasswordStoreFactory.get(), VaultFactory.get(folder,
                        new HostPreferences(controller.Session.getHost()).getProperty("cryptomator.vault.masterkey.filename"),
                        new HostPreferences(controller.Session.getHost()).getProperty("cryptomator.vault.config.filename"),
                        Encoding.UTF8.GetBytes(new HostPreferences(controller.Session.getHost()).getProperty("cryptomator.vault.pepper"))))
                {
                    _controller = controller;
                    _folder = folder;
                    _filename = filename;
                }

                public override void cleanup(object result)
                {
                    if (_filename.StartsWith("."))
                    {
                        _controller.ShowHiddenFiles = true;
                    }
                    List<Path> folders = new List<Path>() {_folder};
                    _controller.Reload(_controller.Workdir, folders, folders);
                }
            }
        }
    }
}
