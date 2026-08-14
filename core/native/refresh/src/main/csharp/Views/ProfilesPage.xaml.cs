using Ch.Cyberduck.Core.Refresh.Services;
using Ch.Cyberduck.Core.Refresh.ViewModels.Preferences.Pages;
using ReactiveUI;
using System;
using System.Reactive.Linq;
using System.Windows.Controls;

namespace Ch.Cyberduck.Core.Refresh.Views
{
    public partial class ProfilesPage
    {
        private WpfIconProvider iconProvider;

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

        private void profilesList_TargetUpdated(object sender, System.Windows.Data.DataTransferEventArgs e)
        {
            //foreach (DataGridColumn column in ((DataGrid)sender).Columns)
            //{
            //}

            ((DataGrid)sender).InvalidateMeasure();
        }
    }

    public abstract class ProfilesPageBase : ReactiveUserControl<ProfilesViewModel> { }
}
