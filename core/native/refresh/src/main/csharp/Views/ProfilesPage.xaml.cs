using Cyberduck.Core.Refresh.ViewModels.Preferences.Pages;
using ReactiveUI;

namespace Cyberduck.Core.Refresh.Views
{
    public partial class ProfilesPage
    {
        public ProfilesPage(ProfilesViewModel viewModel)
        {
            ViewModel = viewModel;

            InitializeComponent();

            this.WhenActivated(d =>
            {
            });
        }
    }
}
