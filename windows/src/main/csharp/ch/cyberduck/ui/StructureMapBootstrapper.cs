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

using Ch.Cyberduck.Ui.Controller;
using Ch.Cyberduck.Ui.Winforms;
using Ch.Cyberduck.Ui.Winforms.Controls;
using StructureMap;

namespace Ch.Cyberduck.Ui
{
    public class StructureMapBootstrapper : IBootstrapper
    {
        private static bool _hasStarted;

        public void BootstrapStructureMap()
        {
            ObjectFactory.Initialize(x =>
            {
                x.For<IBrowserView>().Use<BrowserForm>();
                x.For<IInfoView>().Use<InfoForm>();
                x.For<IActivityView>().Use<ActivityForm>();
                x.For<ILoginView>().Use<LoginForm>();
                x.For<IPasswordView>().Use<PasswordForm>();
                x.For<IBookmarkView>().Use<BookmarkForm>();
                x.For<IConnectionView>().Use<ConnectionForm>();
                x.For<ITransferPromptView>().Use<TransferPromptForm>();
                x.For<IErrorView>().Use<ErrorForm>();
                x.For<INewFolderPromptView>().Use<NewFolderPromptForm>();
                x.For<ICreateFilePromptView>().Use<CreateFilePromptForm>();
                x.For<ICreateSymlinkPromptView>().Use<CreateSymlinkPromptForm>();
                x.For<IGotoPromptView>().Use<GotoPromptForm>();
                x.For<IDuplicateFilePromptView>().Use<DuplicateFilePromptForm>();
                x.For<IDonationView>().Use<DonationForm>();
                x.For<ITransferView>().Use<TransferForm>();
                x.For<IProgressView>().Use<TransferControl>();
                x.For<ICommandView>().Use<CommandForm>();
                x.For<IDonationController>().Use<DonationController>();

                // Singletons
                x.For<IPreferencesView>().Singleton().Use<PreferencesForm>();
            });
        }

        public static void Restart()
        {
            if (_hasStarted)
            {
                ObjectFactory.ResetDefaults();
            }
            else
            {
                Bootstrap();
                _hasStarted = true;
            }
        }

        public static void Bootstrap()
        {
            new StructureMapBootstrapper().BootstrapStructureMap();
        }
    }
}