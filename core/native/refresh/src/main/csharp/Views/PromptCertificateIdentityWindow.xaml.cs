using Ch.Cyberduck.Core.Refresh.Splat;
using Ch.Cyberduck.Core.Refresh.ViewModels.Dialogs;
using ReactiveUI;
using System;
using System.Reactive;
using System.Reactive.Linq;
using System.Security.Cryptography.X509Certificates;
using System.Windows;
using System.Windows.Input;
using System.Windows.Interop;

namespace Ch.Cyberduck.Core.Refresh.Views;

public partial class PromptCertificateIdentityWindow
{
    public PromptCertificateIdentityWindow()
    {
        InitializeComponent();

        this.WhenActivated(d =>
        {
            d(this.OneWayBind(ViewModel, vm => vm.Message, v => v.message.Text));
            d(this.OneWayBind(ViewModel, vm => vm.Items, v => v.certificates.ItemsSource));
            d(this.Bind(ViewModel, vm => vm.SelectedItem, v => v.certificates.SelectedItem));

            d(this.BindCommand(ViewModel, vm => vm.Confirm, v => v.chooseButton));
            d(this.BindCommand(ViewModel, vm => vm.Cancel, v => v.cancelButton));
            d(this.BindCommand(ViewModel, vm => vm.ShowCertificate, v => v.showCertificateButton));

            // Double click on an entry chooses it
            d(Observable.FromEventPattern<MouseButtonEventHandler, MouseButtonEventArgs>(
                    h => certificates.MouseDoubleClick += h,
                    h => certificates.MouseDoubleClick -= h)
                .Where(e => ViewModel?.SelectedItem is not null)
                .Select(_ => Unit.Default)
                .InvokeCommand(this, v => v.ViewModel.Confirm));

            d(this.BindInteraction(ViewModel, vm => vm.Show, c =>
            {
                X509Certificate2UI.DisplayCertificate(c.Input.Native, new WindowInteropHelper(this).Handle);
                c.SetOutput(Unit.Default);
                return Observable.Return(Unit.Default);
            }));

            d(this.BindInteraction(ViewModel, vm => vm.Close, c =>
            {
                DialogResult = c.Input;
                c.SetOutput(default);
                return Observable.Start(Close, RxApp.MainThreadScheduler);
            }));
        });
    }

    public sealed class Factory : IWindowFactory<PromptCertificateIdentityViewModel>
    {
        public PromptCertificateIdentityWindow Create(PromptCertificateIdentityViewModel viewModel)
        {
            return new PromptCertificateIdentityWindow
            {
                ViewModel = viewModel
            };
        }

        Window IWindowFactory<PromptCertificateIdentityViewModel>.Create(PromptCertificateIdentityViewModel viewModel) => Create(viewModel);
    }
}

public abstract class PromptCertificateIdentityWindowBase : ReactiveWindow<PromptCertificateIdentityViewModel> { }
