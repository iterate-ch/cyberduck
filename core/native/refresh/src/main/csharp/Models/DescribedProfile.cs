using ch.cyberduck.core;
using ch.cyberduck.core.profiles;

namespace Ch.Cyberduck.Core.Refresh.Models
{
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
}
