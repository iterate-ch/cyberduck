using System;

namespace Ch.Cyberduck.Core.Refresh.Services
{
    public class ProfileListObserver
    {
        public event EventHandler ProfilesChanged;

        public void RaiseProfilesChanged() => ProfilesChanged?.Invoke(this, EventArgs.Empty);
    }
}
