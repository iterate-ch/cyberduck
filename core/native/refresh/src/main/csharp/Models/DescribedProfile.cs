using ch.cyberduck.core;
using ch.cyberduck.core.profiles;

namespace Ch.Cyberduck.Core.Refresh.Models
{
    public class DescribedProfile
    {
        public DescribedProfile(ProfileDescription description)
        {
            Description = description;
            Profile = (Profile)description.getProfile().get();
        }

        public ProfileDescription Description { get; }

        public Profile Profile { get; }
    }
}
