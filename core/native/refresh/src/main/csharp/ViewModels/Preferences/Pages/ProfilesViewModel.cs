using ch.cyberduck.core;
using ch.cyberduck.core.profiles;
using ch.cyberduck.core.serializer.impl.dd;
using DynamicData;
using java.util;
using java.util.concurrent;
using ReactiveUI;
using System;
using System.Reactive;
using System.Threading.Tasks;

namespace Cyberduck.Core.Refresh.ViewModels.Preferences.Pages
{
    public class ProfilesViewModel : ReactiveObject
    {
        public ProfilesViewModel(PeriodicProfilesUpdater periodicUpdater, LocalProfilesFinder localFinder, ProtocolFactory protocols)
        {
            List installed = localFinder.find();
            ProfilePlistReader reader = new ProfilePlistReader(protocols);
            Iterator iterator = installed.iterator();
            while (iterator.hasNext())
            {
                ProfileDescription current = (ProfileDescription)iterator.next();
                Installed.AddOrUpdate(new ViewModel(current, (Profile)reader.read(current.getProfile())));
            }

            Func<List, Future> pass = CreateHandler(periodicUpdater, this, reader);
            LoadProfiles = ReactiveCommand.CreateFromTask(() =>
            {
                Repository.Clear();
                Future future = pass(installed);
                return (Task)Task.Run(future.get);
            });
        }

        public SourceCache<ViewModel, ProfileDescription> Installed { get; } = new SourceCache<ViewModel, ProfileDescription>(x => x.Description);
        public ReactiveCommand<Unit, Unit> LoadProfiles { get; }
        public SourceCache<ViewModel, ProfileDescription> Repository { get; } = new SourceCache<ViewModel, ProfileDescription>(x => x.Description);

        private static Func<List, Future> CreateHandler(PeriodicProfilesUpdater updater, ProfilesViewModel model, ProfilePlistReader reader)
        {
            Visitor visitor = new Visitor(model, reader);
            return l => updater.synchronize(l, visitor);
        }

        public class ViewModel
        {
            public ViewModel(ProfileDescription description, Profile profile)
            {
                Profile = profile;
                Description = description;
            }

            public ProfileDescription Description { get; }
            public Profile Profile { get; }
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
                        parent.Repository.AddOrUpdate(new ViewModel(description, (Profile)reader.read(profile)));
                    }
                }

                return description;
            }
        }
    }
}
