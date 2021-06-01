using Cyberduck.Core.Refresh.ViewModels.Preferences.Pages;
using ReactiveUI;
using System;
using System.Reactive.Linq;

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
                d(this.OneWayBind(ViewModel, x => x.Profiles, x => x.profilesList.ItemsSource));

                d(this.WhenAnyValue(x => x.ViewModel.LoadProfiles).Select(x => x.Execute()).Subscribe());
            });
        }
    }
}
