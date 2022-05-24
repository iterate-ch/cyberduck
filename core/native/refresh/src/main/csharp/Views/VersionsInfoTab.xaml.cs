using Ch.Cyberduck.Core.Refresh.ViewModels.Info;
using ReactiveUI;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Data;
using System.Windows.Documents;
using System.Windows.Input;
using System.Windows.Media;
using System.Windows.Media.Imaging;
using System.Windows.Navigation;
using System.Windows.Shapes;

namespace Ch.Cyberduck.Core.Refresh.Views
{
    /// <summary>
    /// Interaktionslogik für VersionsInfoTab.xaml
    /// </summary>
    public partial class VersionsInfoTab
    {
        public VersionsInfoTab()
        {
            InitializeComponent();

            this.WhenActivated(d =>
            {
                d(this.OneWayBind(ViewModel, vm => vm.Busy, v => v.Status.IsBusy));
                d(this.OneWayBind(ViewModel, vm => vm.Versions, v => v.VersionGrid.ItemsSource));
                d(this.Bind(ViewModel, vm => vm.SelectedVersion, v => v.VersionGrid.SelectedItem));
                d(this.BindCommand(ViewModel, vm => vm.Open, v => v.OpenButton));
                d(this.BindCommand(ViewModel, vm => vm.Remove, v => v.RemoveButton));
                d(this.BindCommand(ViewModel, vm => vm.Revert, v => v.RevertButton));
            });
        }
    }

    public abstract class VersionsInfoTabBase : ReactiveUserControl<VersionsViewModel> { }
}
