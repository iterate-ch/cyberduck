using System;
using System.Reactive.Linq;
using System.Windows;
using Ch.Cyberduck.Core.Refresh.Services;
using Ch.Cyberduck.Core.Refresh.ViewModels.Preferences.Pages;
using ReactiveUI;

namespace Ch.Cyberduck.Core.Refresh.Views
{
    public partial class ProfilesPage
    {
        public static readonly DependencyProperty IconProviderProperty = DependencyProperty.Register(nameof(IconProvider), typeof(WpfIconProvider), typeof(ProfilesPage));

        public WpfIconProvider IconProvider
        {
            get { return (WpfIconProvider)GetValue(IconProviderProperty); }
            set { SetValue(IconProviderProperty, value); }
        }

        public ProfilesPage()
        {
            InitializeComponent();

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
