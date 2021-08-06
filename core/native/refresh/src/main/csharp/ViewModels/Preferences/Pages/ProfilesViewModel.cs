using ch.cyberduck.core.profiles;
using ch.cyberduck.core.serializer.impl.dd;
using Ch.Cyberduck.Core.Refresh.Models;
using DynamicData;
using DynamicData.Binding;
using java.util;
using java.util.concurrent;
using org.apache.log4j;
using ReactiveUI;
using ReactiveUI.Fody.Helpers;
using System;
using System.Collections.Concurrent;
using System.Collections.Generic;
using System.ComponentModel;
using System.Linq;
using System.Reactive;
using System.Reactive.Linq;
using System.Reactive.Subjects;
using System.Threading;
using System.Threading.Tasks;

namespace Ch.Cyberduck.Core.Refresh.ViewModels.Preferences.Pages
{
    using ch.cyberduck.core;
    using Ch.Cyberduck.Core.Refresh.Services;

    public class ProfilesViewModel : ReactiveObject
    {
        private static Logger log = Logger.getLogger(typeof(ProfilesViewModel).FullName);

        private readonly Dictionary<ProfileDescription, DescribedProfile> installed = new();

        public ProfilesViewModel(PeriodicProfilesUpdater periodicUpdater, LocalProfilesFinder localFinder, ProtocolFactory protocols, ProfileListObserver profileListObserver)
        {
            List installed = localFinder.find();
            ProfilePlistReader reader = new(protocols);
            Iterator iterator = installed.iterator();
            while (iterator.hasNext())
            {
                ProfileDescription current = (ProfileDescription)iterator.next();
                this.installed[current] = new DescribedProfile(current, (Profile)reader.read(current.getProfile()));
            }

            Func<CancellationToken, Task<IEnumerable<DescribedProfile>>> pass = CreateHandler(periodicUpdater, reader, installed);
            BehaviorSubject<bool> initialized = new(false);
            LoadProfiles = ReactiveCommand.CreateFromTask(async (cancel) =>
            {
                Profiles.Clear();
                try
                {
                    Busy = true;
                    return await pass(cancel);
                }
                finally
                {
                    initialized.OnNext(true);

                    Busy = false;
                }
            }, initialized.Select(x => !x));

            var profiles = LoadProfiles.ToObservableChangeSet()
                .Transform(x => new ProfileViewModel(x, this.installed.ContainsKey(x.Description)))
                .AsObservableList();

            profiles.Connect()
                .Filter(this.WhenAnyValue(v => v.FilterText)
                    .Throttle(TimeSpan.FromMilliseconds(725))
                    .DistinctUntilChanged()
                    .Select(Filter))
                .Sort(SortExpressionComparer<ProfileViewModel>.Ascending(x => x.Description))
                .ObserveOnDispatcher()
                .Bind(Profiles)
                .Subscribe();

            profiles.Connect()
                .WhenPropertyChanged(v => v.Installed, false)
                .Subscribe(p =>
                {
                    if (p.Value)
                    {
                        protocols.register(p.Sender.ProfileDescription.getProfile());
                    }
                    else
                    {
                        protocols.unregister(p.Sender.Profile);
                    }
                    profileListObserver.RaiseProfilesChanged();
                });
        }

        [Reactive]
        public bool Busy { get; set; }

        [Reactive]
        public string FilterText { get; set; }

        public ReactiveCommand<Unit, IEnumerable<DescribedProfile>> LoadProfiles { get; }

        public BindingList<ProfileViewModel> Profiles { get; } = new();

        private static Func<CancellationToken, Task<IEnumerable<DescribedProfile>>> CreateHandler(PeriodicProfilesUpdater updater, ProfilePlistReader reader, List installed)
        {
            Visitor visitor = new(reader);
            return async (cancel) =>
            {
                visitor.Reset();
                Future future = updater.synchronize(installed, visitor);
                using (cancel.Register(() => future.cancel(true)))
                {
                    await Task.Run(future.get, cancel);
                }
                return visitor.List;
            };
        }

        private static Func<ProfileViewModel, bool> Filter(string input)
        {
            if (string.IsNullOrWhiteSpace(input))
            {
                return _ => true;
            }
            var split = input.Split(' ');
            bool Filter(string input) => split.Any(s => input.IndexOf(s, StringComparison.OrdinalIgnoreCase) != -1);
            return p => Filter(p.Profile.getName())
                || Filter(p.Profile.getDescription())
                || Filter(p.Profile.getDefaultHostname())
                || Filter(p.Profile.getProvider());
        }

        private class Visitor : ProfilesFinder.Visitor
        {
            private readonly ProfilePlistReader reader;
            private readonly ConcurrentDictionary<ProfileDescription, DescribedProfile> repository = new();

            public Visitor(ProfilePlistReader reader)
            {
                this.reader = reader;
            }

            public ICollection<DescribedProfile> List => repository.Values;

            public void Reset() => repository.Clear();

            public ProfileDescription visit(ProfileDescription description)
            {
                if (description.isLatest())
                {
                    Local profile = description.getProfile();
                    if (profile != null)
                    {
                        try
                        {
                            repository[description] = new DescribedProfile(description, (Profile)reader.read(profile));
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
