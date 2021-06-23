using Ch.Cyberduck.Core.Refresh.Services;
using Ch.Cyberduck.Core.Refresh.ViewModels.Preferences.Pages;
using ReactiveUI;

namespace Ch.Cyberduck.Core.Refresh.UserControls
{
    public abstract class ProfileElementBase : ReactiveUserControl<ProfileViewModel> { }

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
            });
        }
    }
}
