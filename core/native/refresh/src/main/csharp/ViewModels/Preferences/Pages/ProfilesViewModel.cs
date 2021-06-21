using ch.cyberduck.core;
using ch.cyberduck.core.profiles;
using ch.cyberduck.core.serializer.impl.dd;
using DynamicData;
using java.util;
using java.util.concurrent;
using org.apache.log4j;
using ReactiveUI;
using System;
using System.Collections.ObjectModel;
using System.Reactive;
using System.Reactive.Linq;
using System.Threading.Tasks;
using System.Windows.Threading;

namespace Ch.Cyberduck.Core.Refresh.ViewModels.Preferences.Pages
{
    using ch.cyberduck.core;

    public class ProfilesViewModel : ReactiveObject
    {
        private static Logger log = Logger.getLogger(typeof(ProfilesViewModel).FullName);

        private readonly SourceCache<DescribedProfile, ProfileDescription> installed = new SourceCache<DescribedProfile, ProfileDescription>(x => x.Description);
        private readonly ReadOnlyObservableCollection<ViewModel> profiles;
        private readonly SourceCache<DescribedProfile, ProfileDescription> repository = new SourceCache<DescribedProfile, ProfileDescription>(x => x.Description);

        public ProfilesViewModel(PeriodicProfilesUpdater periodicUpdater, LocalProfilesFinder localFinder, ProtocolFactory protocols)
        {
            List installed = localFinder.find();
            ProfilePlistReader reader = new ProfilePlistReader(protocols);
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
            repository.Connect().Transform(x => new ViewModel(x)).ObserveOnDispatcher(DispatcherPriority.Background).Bind(out profiles).Subscribe();
        }

        public ReactiveCommand<Unit, Unit> LoadProfiles { get; }
        public ReadOnlyObservableCollection<ViewModel> Profiles => profiles;

        private static Func<List, Future> CreateHandler(PeriodicProfilesUpdater updater, ProfilesViewModel model, ProfilePlistReader reader)
        {
            Visitor visitor = new Visitor(model, reader);
            return l => updater.synchronize(l, visitor);
        }

        public class DescribedProfile
        {
            public DescribedProfile(ProfileDescription description, Profile profile)
            {
                Profile = profile;
                Description = description;
            }

            public ProfileDescription Description { get; }
            public Profile Profile { get; }
        }

        public class ViewModel : ReactiveObject
        {
            private readonly Profile profile;
            private readonly ProfileDescription profileDescription;

            public ViewModel(DescribedProfile profile)
            {
                this.profile = profile.Profile;
                profileDescription = profile.Description;

                Description = profile.Profile.getDescription();
                Name = profile.Profile.getName();
            }

            public string Description { get; }
            public string Name { get; }
        }

        private class Visitor : ProfilesFinder.Visitor
        {
            private readonly Dispatcher dispatcher = Dispatcher.CurrentDispatcher;
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
