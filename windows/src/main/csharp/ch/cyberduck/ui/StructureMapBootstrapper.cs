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
using ch.cyberduck.core.preferences;
using ch.cyberduck.core.profiles;
using Ch.Cyberduck.Core;
using Ch.Cyberduck.Core.Refresh;
using Ch.Cyberduck.Core.Refresh.Services;
using Ch.Cyberduck.Core.Refresh.UserControls;
using Ch.Cyberduck.Core.Refresh.ViewModels.Preferences.Pages;
using Ch.Cyberduck.Ui.Controller;
using Ch.Cyberduck.Ui.Core.Contracts;
using Ch.Cyberduck.Ui.Winforms;
using Ch.Cyberduck.Ui.Winforms.Controls;
using ReactiveUI;
using Splat;
using StructureMap;
using StructureMap.Configuration.DSL;
using StructureMap.Pipeline;
using System;
using System.Collections.Generic;
using System.Linq;

namespace Ch.Cyberduck.Ui
{
    public static class StructureMapBootstrapper
    {
        public static void Bootstrap()
        {
            ObjectFactory.Initialize(x =>
            {
                x.ForConcreteType<BaseController>();
                x.Forward<BaseController, ch.cyberduck.core.Controller>();

                x.For<Preferences>().Use(() => PreferencesFactory.get());
                x.For<ProtocolFactory>().Use(() => ProtocolFactory.get());

                x.For<IBrowserView>().Use<BrowserForm>();
                x.For<IInfoView>().Use<InfoForm>();
                x.For<IActivityView>().Use<ActivityForm>();
                x.For<ILoginView>().Use<LoginForm>();
                x.For<IPasswordPromptView>().Use<PasswordForm>();
                x.For<IBookmarkView>().Use<BookmarkForm>();
                x.For<IConnectionView>().Use<ConnectionForm>();
                x.For<ITransferPromptView>().Use<TransferPromptForm>();
                x.For<IErrorView>().Use<ErrorForm>();
                x.For<INewFolderPromptView>().Use<NewFolderPromptForm>();
                x.For<INewVaultPromptView>().Use<NewVaultPromptForm>();
                x.For<ICreateFilePromptView>().Use<CreateFilePromptForm>();
                x.For<ICreateSymlinkPromptView>().Use<CreateSymlinkPromptForm>();
                x.For<IGotoPromptView>().Use<GotoPromptForm>();
                x.For<IDuplicateFilePromptView>().Use<DuplicateFilePromptForm>();
                x.For<ITransferView>().Use<TransferForm>();
                x.For<IProgressView>().Use<TransferControl>();
                x.For<ICommandView>().Use<CommandForm>();
                x.For<IDonationController>().Use<DonationController>();
                x.For<LocalProfilesFinder>().Use(() => new LocalProfilesFinder());
                x.For<PeriodicProfilesUpdater>().Use(ctx => new PeriodicProfilesUpdater(ctx.GetInstance<ch.cyberduck.core.Controller>()));

                x.ForSingletonOf<IIconProviderImageSource>().Use<CyberduckImageSource>();

                x.ForConcreteSingleton<Images>();
                x.ForConcreteSingleton<IconCache>();
                x.ForConcreteSingleton<WpfIconProvider>();
                x.ForConcreteSingleton<WinFormsIconProvider>();
                x.ForConcreteSingleton<IconIconProvider>();

                x.For<IViewFor<ProfileViewModel>>().Use<ProfileElement>();

                // Singletons
                x.For<IPreferencesView>().Singleton().Use<PreferencesForm>();

                if (Utils.OSVersion.IsVersion(Utils.OSVersion.Windows10.V1809))
                {
                    x.For<IVirtualDesktopManager>().Use<Core.VirtualDesktop.Windows1809.VirtualDesktopManager>();
                }
                else if (Utils.OSVersion.IsVersion(Utils.OSVersion.Windows10.V1803))
                {
                    x.For<IVirtualDesktopManager>().Use<Core.VirtualDesktop.Windows1803.VirtualDesktopManager>();
                }
                else if (Utils.OSVersion.IsVersion(Utils.OSVersion.Windows10.V1607))
                {
                    x.For<IVirtualDesktopManager>().Use<Core.VirtualDesktop.Windows16071709.VirtualDesktopManager>();
                }
                else
                {
                    x.For<IVirtualDesktopManager>().Use<Core.VirtualDesktop.DisabledVirtualDesktopManager>();
                }
            });
        }

        public static SmartInstance<T> ForConcreteSingleton<T>(this IRegistry registry) => registry.For<T>().Singleton().Add<T>();

        public class SplatDependencyResolver : IDependencyResolver
        {
            public void Dispose()
            {
            }

            public object GetService(Type serviceType, string contract = null)
            {
                return string.IsNullOrEmpty(contract)
                    ? ObjectFactory.GetInstance(serviceType)
                    : ObjectFactory.GetNamedInstance(serviceType, contract);
            }

            public IEnumerable<object> GetServices(Type serviceType, string contract = null) => ObjectFactory.GetAllInstances(serviceType).Cast<object>();

            public bool HasRegistration(Type serviceType, string contract = null) => ObjectFactory.Model.HasImplementationsFor(serviceType);

            public void Register(Func<object> factory, Type serviceType, string contract = null) => ObjectFactory.Configure(configure =>
            {
                var registration = configure.For(serviceType).Use(_ => factory());
                if (!string.IsNullOrWhiteSpace(contract))
                {
                    registration.Named(contract);
                }
            });

            public IDisposable ServiceRegistrationCallback(Type serviceType, string contract, Action<IDisposable> callback) => throw new NotImplementedException();

            public void UnregisterAll(Type serviceType, string contract = null) => ObjectFactory.Model.EjectAndRemove(serviceType);

            public void UnregisterCurrent(Type serviceType, string contract = null) => ObjectFactory.Model.EjectAndRemove(serviceType);
        }
    }
}
