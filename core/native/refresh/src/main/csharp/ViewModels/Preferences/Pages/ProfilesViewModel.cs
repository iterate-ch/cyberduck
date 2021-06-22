using ch.cyberduck.core.profiles;
using ch.cyberduck.core.serializer.impl.dd;
using DynamicData;
using java.util;
using java.util.concurrent;
using org.apache.log4j;
using ReactiveUI;
using System;
using System.Reactive;
using System.Reactive.Linq;
using System.Threading.Tasks;

namespace Ch.Cyberduck.Core.Refresh.ViewModels.Preferences.Pages
{
    using ch.cyberduck.core;
    using Ch.Cyberduck.Core.Refresh.Models;
    using Ch.Cyberduck.Core.Refresh.Services;
    using System.ComponentModel;

    public class ProfilesViewModel : ReactiveObject
    {
        private static Logger log = Logger.getLogger(typeof(ProfilesViewModel).FullName);

        private readonly SourceCache<DescribedProfile, ProfileDescription> installed = new(x => x.Description);
        private readonly SourceCache<DescribedProfile, ProfileDescription> repository = new(x => x.Description);

        public ProfilesViewModel(PeriodicProfilesUpdater periodicUpdater, LocalProfilesFinder localFinder, ProtocolFactory protocols, WpfIconProvider iconProvider)
        {
            List installed = localFinder.find();
            ProfilePlistReader reader = new(protocols);
            Iterator iterator = installed.iterator();
            while (iterator.hasNext())
            {
                ProfileDescription current = (ProfileDescription)iterator.next();
                this.installed.AddOrUpdate(new DescribedProfile(current, (Profile)reader.read(current.getProfile())));
            }

            Func<List, Future> pass = CreateHandler(periodicUpdater, this, reader);
            LoadProfiles = ReactiveCommand.CreateFromTask(() =>
            {
                repository.Clear();
                Future future = pass(installed);
                return (Task)Task.Run(future.get);
            });
            repository.Connect().Transform(x => new ProfileViewModel(x)).ObserveOnDispatcher().Bind(Profiles).Subscribe();
        }

        public ReactiveCommand<Unit, Unit> LoadProfiles { get; }

        public BindingList<ProfileViewModel> Profiles { get; } = new();

        private static Func<List, Future> CreateHandler(PeriodicProfilesUpdater updater, ProfilesViewModel model, ProfilePlistReader reader)
        {
            Visitor visitor = new(model, reader);
            return l => updater.synchronize(l, visitor);
        }

        private class Visitor : ProfilesFinder.Visitor
        {
            private readonly ProfilesViewModel parent;
            private readonly ProfilePlistReader reader;

            public Visitor(ProfilesViewModel parent, ProfilePlistReader reader)
            {
                this.parent = parent;
                this.reader = reader;
            }

            public ProfileDescription visit(ProfileDescription description)
            {
                if (description.isLatest())
                {
                    Local profile = description.getProfile();
                    if (profile != null)
                    {
                        try
                        {
                            parent.repository.AddOrUpdate(new DescribedProfile(description, (Profile)reader.read(profile)));
                        }
                        catch (Exception e)
                        {
                            log.warn(string.Format("Failure {0} reading profile {1}", e, description));
                        }
                    }
                }

                return description;
            }
        }
    }
}
