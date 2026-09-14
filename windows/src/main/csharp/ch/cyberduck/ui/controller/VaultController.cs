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

using System;
using System.Collections.Generic;
using System.Drawing;
using System.Windows.Forms;
using ch.cyberduck.core;
using Ch.Cyberduck.Core;
using ch.cyberduck.core.features;
using ch.cyberduck.core.threading;
using ch.cyberduck.core.vault;
using ch.cyberduck.core.worker;
using ch.cyberduck.ui.browser;
using java.util;
using static Ch.Cyberduck.ImageHelper;

namespace Ch.Cyberduck.Ui.Controller
{
    class VaultController : FolderController
    {
        private readonly INewVaultPromptView _view;
        private readonly VaultVersion _version;

        public VaultController(INewVaultPromptView view, BrowserController browserController,
            IList<Location.Name> regions, Location.Name defaultRegion, VaultVersion version) : base(view,
            browserController, regions, defaultRegion)
        {
            _view = view;
            _version = version;
            if (VaultVersion.Type.V8 == _version.type)
            {
                _view.EnablePassphrase();
                _view.ValidateInput += ValidateInputEventHandler;
            }
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
                VaultCredentials credentials = VaultVersion.Type.V8 == _version.type
                    ? new VaultCredentials(_view.Passphrase).setSaved(false)
                    : new VaultCredentials().setSaved(false);
                BrowserController.background(new CreateVaultAction(BrowserController,
                    new UploadTargetFinder(Workdir).find(BrowserController.SelectedPath), View.InputText,
                    HasLocation() ? _view.Region : null, credentials, _version));
            }
        }

        private class CreateVaultAction : WorkerBackgroundAction
        {
            public CreateVaultAction(BrowserController controller, Path directory, string filename, string region,
                VaultCredentials credentials, VaultVersion version)
                : base(
                    controller, controller.Pool,
                    new InnerCreateVaultWorker(controller,
                        new Path(directory, filename, EnumSet.of(AbstractPath.Type.directory)), filename, region,
                        credentials, version))
            {
            }

            private class InnerCreateVaultWorker : CreateVaultWorker
            {
                private readonly BrowserController _controller;
                private readonly string _filename;
                private readonly Path _folder;

                public InnerCreateVaultWorker(BrowserController controller, Path folder, String filename,
                    String region, VaultCredentials credentials, VaultVersion version)
                    : base(region, folder, credentials, version)
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

                    List<Path> folders = new List<Path>() { _folder };
                    _controller.Reload(_controller.Workdir, folders, folders);
                }
            }
        }
    }
}
