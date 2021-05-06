using DynamicData;
using ReactiveUI;

namespace Cyberduck.Core.Refresh.ViewModels.Preferences.Pages
{
    public class ProfilesViewModel : ReactiveObject
    {
        public SourceList<int> Profiles { get; } = new SourceList<int>();

        public ProfilesViewModel()
        {
        }
    }
}
