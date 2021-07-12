using System;
using System.Reactive.Linq;
using Ch.Cyberduck.Core.Refresh.ViewModels.Preferences.Pages;
using ReactiveUI;

namespace Ch.Cyberduck.Core.Refresh.Views
{
    public abstract class ProfilesPageBase : ReactiveUserControl<ProfilesViewModel> { }

    public partial class ProfilesPage
    {
        public ProfilesPage()
        {
            InitializeComponent();

            this.WhenActivated(d =>
            {
                d(this.OneWayBind(ViewModel, vm => vm.Busy, v => v.Status.IsBusy));
                d(this.Bind(ViewModel, vm => vm.FilterText, v => v.Search.Text));
                d(this.OneWayBind(ViewModel, x => x.Profiles, x => x.profilesList.ItemsSource));

                d(ViewModel.LoadProfiles.Execute().Subscribe());
            });
        }
    }
}
