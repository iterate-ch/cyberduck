using ch.cyberduck.core;
using ch.cyberduck.core.profiles;
using Ch.Cyberduck.Core.Refresh.Models;
using ReactiveUI;
using ReactiveUI.Fody.Helpers;

namespace Ch.Cyberduck.Core.Refresh.ViewModels.Preferences.Pages
{
    public class ProfileViewModel : ReactiveObject
    {
        public ProfileViewModel(DescribedProfile profile, bool installed)
        {
            Profile = profile.Profile;
            ProfileDescription = profile.Description;
            Installed = installed;

            Description = profile.Profile.getDescription();
            Name = profile.Profile.getName();
            DefaultHostName = profile.Profile.getDefaultHostname();
        }

        public string DefaultHostName { get; }

        public string Description { get; }

        [Reactive]
        public bool Installed { get; set; }

        public bool IsBundled => Profile.isBundled();

        public string Name { get; }

        public Profile Profile { get; }

        public ProfileDescription ProfileDescription { get; }
    }
}
