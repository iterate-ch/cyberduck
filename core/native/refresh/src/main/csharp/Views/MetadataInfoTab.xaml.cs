using Ch.Cyberduck.Core.Refresh.ViewModels.Info;
using ReactiveUI;
using System.Windows;

namespace Ch.Cyberduck.Core.Refresh.Views
{
    public partial class MetadataInfoTab
    {
        public MetadataInfoTab()
        {
            InitializeComponent();

            this.WhenActivated(d =>
            {
                d(this.OneWayBind(ViewModel, vm => vm.Busy, v => v.Status.IsBusy));
                d(this.OneWayBind(ViewModel, vm => vm.Metadata, v => v.HeadersGrid.ItemsSource));
                d(this.OneWayBind(ViewModel, vm => vm.MetadataMenuItems, v => v.metadataButtonMenu.ItemsSource));
            });
        }

        private void SplitButton_Opened(object sender, RoutedEventArgs e)
        {
            var element = sender as FrameworkElement;
            if (element == null)
            {
                return;
            }
            if (element.ContextMenu == null)
            {
                return;
            }
            element.ContextMenu.PlacementTarget = element;
            element.ContextMenu.IsOpen = true;
        }
    }

    public abstract class MetadataInfoTabBase : ReactiveUserControl<MetadataViewModel> { }
}
