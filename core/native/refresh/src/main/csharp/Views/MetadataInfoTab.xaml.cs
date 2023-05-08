using ch.cyberduck.core.i18n;
using Ch.Cyberduck.Core.Refresh.ViewModels.Info;
using ReactiveUI;
using Splat;
using System.Windows;
using System.Windows.Automation;
using System.Windows.Controls;
using System.Windows.Controls.Primitives;

namespace Ch.Cyberduck.Core.Refresh.Views
{
    public partial class MetadataInfoTab
    {
        private readonly Locale locale;

        public MetadataInfoTab()
        {
            InitializeComponent();

            Locator.Current.GetService(out locale);

            this.WhenActivated(d =>
            {
                d(this.OneWayBind(ViewModel, vm => vm.Busy, v => v.Status.IsBusy));
                d(this.OneWayBind(ViewModel, vm => vm.Metadata, v => v.HeadersGrid.ItemsSource));
                d(this.OneWayBind(ViewModel, vm => vm.MetadataMenuItems, v => v.metadataButtonMenu.ItemsSource));
            });
        }

        public override void OnApplyTemplate()
        {
            AddNewMetadataButton.ApplyTemplate();
            var splitButtonTemplate = AddNewMetadataButton.Template;
            var actionButton = (Button)splitButtonTemplate.FindName("PART_ActionButton", AddNewMetadataButton);
            var dropdownButton = (ToggleButton)splitButtonTemplate.FindName("PART_ToggleButton", AddNewMetadataButton);
            var title = locale.localize("Custom Header", "S3");

            AutomationProperties.SetName(actionButton, title);
            AutomationProperties.SetLabeledBy(dropdownButton, actionButton);
            AutomationProperties.SetName(metadataButtonMenu, title);
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
            element.ContextMenu.Placement = PlacementMode.Bottom;
            element.ContextMenu.PlacementTarget = element;
            element.ContextMenu.IsOpen = true;
        }
    }

    public abstract class MetadataInfoTabBase : ReactiveUserControl<MetadataViewModel> { }
}
