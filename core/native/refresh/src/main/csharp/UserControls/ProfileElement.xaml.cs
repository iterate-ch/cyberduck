using Ch.Cyberduck.Core.Refresh.Services;
using Ch.Cyberduck.Core.Refresh.ViewModels.Preferences.Pages;
using ReactiveUI;
using System.Windows.Controls;

namespace Ch.Cyberduck.Core.Refresh.UserControls
{
    /// <summary>
    /// Interaktionslogik für ProfileElement.xaml
    /// </summary>
    public partial class ProfileElement
    {
        public ProfileElement(WpfIconProvider wpfIconProvider)
        {
            InitializeComponent();

            this.WhenActivated(d =>
            {
                d(this.OneWayBind(ViewModel, vm => vm.Name, v => v.ProtocolType.Text));
                d(this.OneWayBind(ViewModel, vm => vm.Description, v => v.Description.Text));
                d(this.OneWayBind(ViewModel, vm => vm.Profile, v => v.ProfileIcon.Source, p => wpfIconProvider.GetDisk(p, 32)));
                d(this.OneWayBind(ViewModel, vm => vm.DefaultHostName, v => v.ToolTipEnabled, v => !string.IsNullOrWhiteSpace(v)));
                d(this.OneWayBind(ViewModel, vm => vm.DefaultHostName, v => v.ToolTip));
                d(this.OneWayBind(ViewModel, vm => vm.IsEnabled, v => v.Checked.IsEnabled));
                d(this.BindCommand(ViewModel, vm => vm.OpenHelp, v => v.HelpButton));
                d(this.Bind(ViewModel, vm => vm.Installed, v => v.Checked.IsChecked));
            });
        }

        public bool ToolTipEnabled
        {
            get => ToolTipService.GetIsEnabled(this);
            set => ToolTipService.SetIsEnabled(this, value);
        }
    }

    public abstract class ProfileElementBase : ReactiveUserControl<ProfileViewModel> { }
}
