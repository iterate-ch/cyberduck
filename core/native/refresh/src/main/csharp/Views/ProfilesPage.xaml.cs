using System;
using System.Reactive.Linq;
using Ch.Cyberduck.Core.Refresh.Services;
using Ch.Cyberduck.Core.Refresh.ViewModels.Preferences.Pages;
using Ch.Cyberduck.Core.Refresh.Views.Services;
using ReactiveUI;

namespace Ch.Cyberduck.Core.Refresh.Views
{
    public partial class ProfilesPage
    {
        public ProfilesPage(WpfIconProvider iconProvider)
        {
            InitializeComponent();
            ((ProfileDescriptionThumbnailConverter)FindResource("ProfileThumbnails")).IconProvider = iconProvider;

            this.WhenActivated(d =>
            {
                d(this.OneWayBind(ViewModel, vm => vm.Busy, v => v.Status.IsBusy));
                d(this.Bind(ViewModel, vm => vm.FilterText, v => v.Search.Text));
                d(this.OneWayBind(ViewModel, x => x.Profiles, x => x.profilesList.ItemsSource));

                d(ViewModel.LoadProfiles.ExecuteIfPossible().Subscribe());
            });
        }
    }

    public abstract class ProfilesPageBase : ReactiveUserControl<ProfilesViewModel> { }
}
