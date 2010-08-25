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
//using Rhino.Mocks;
using Ch.Cyberduck.Ui.Controller;
using Ch.Cyberduck.Ui.Winforms;
using StructureMap;

namespace Ch.Cyberduck.Core
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
                                             //x.For<IBookmarkManagerView>().Use<BookmarkManagerForm>();
                                             x.For<IBookmarkView>().Use<BookmarkForm>();
                                             x.For<IConnectionView>().Use<ConnectionForm>();
                                             x.For<ITransferPromptView>().Use<TransferPromptForm>();
                                             x.For<IErrorView>().Use<ErrorForm>();
                                             
                                             // Singletons
                                             x.For<INewFolderPromptView>().Singleton().Use<NewFolderPromptForm>();
                                             x.For<ICreateFilePromptView>().Singleton().Use<CreateFilePromptForm>();
                                             x.For<IDuplicateFilePromptView>().Singleton().Use<DuplicateFilePromptForm>();
                                             x.For<IMessageBoxView>().Singleton().Use<MessageBoxDialog>();
                                             x.For<IPreferencesView>().Singleton().Use<PreferencesForm>();
                                             x.For<IDonationView>().Singleton().Use<DonationForm>();

                                             //x.For<ITransferView>().TheDefault.Is.Object(MockRepository.GenerateMock<ITransferView>()); 
                                             //x.For<IProgressView>().TheDefault.Is.Object(MockRepository.GenerateMock<IProgressView>()); 
                                             
                                             x.For<ITransferView>().Use<TransferForm>();
                                             x.For<IProgressView>().Use<TransferControl>();
                                             
                                             /*
                                             x.ForSingletonOf<ICacheHandler>().Use<CacheHandler>();
                                             x.AddRegistry<FactoryRegistry>();
                                             x.AddRegistry<RepositoryRegistry>();
                                             x.AddRegistry<ServiceRegistry>();
                                             */
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