using ch.cyberduck.core;
using ch.cyberduck.core.profiles;
using Ch.Cyberduck.Core.Refresh.Models;
using ReactiveUI;

namespace Ch.Cyberduck.Core.Refresh.ViewModels.Preferences.Pages
{
    public class ProfileViewModel : ReactiveObject
    {
        private readonly ProfileDescription profileDescription;

        public ProfileViewModel(DescribedProfile profile)
        {
            Profile = profile.Profile;
            profileDescription = profile.Description;

            Description = profile.Profile.getDescription();
            Name = profile.Profile.getName();
        }

        public string Description { get; }

        public string Name { get; }

        public Profile Profile { get; }
    }
}
