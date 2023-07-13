using Ch.Cyberduck.Core.Refresh.Services;
using Ch.Cyberduck.Core.Refresh.Splat;
using Ch.Cyberduck.Core.Refresh.ViewModels.Dialogs;
using ReactiveUI;
using System;
using System.Reactive.Linq;
using System.Windows;
using System.Windows.Automation;

namespace Ch.Cyberduck.Core.Refresh.Views;

public partial class PromptShareeWindow
{
    public PromptShareeWindow(WpfIconProvider iconProvider)
    {
        InitializeComponent();

        this.WhenActivated(d =>
        {
            d(this.OneWayBind(ViewModel, vm => vm.Items, v => v.sharees.ItemsSource));
            d(this.Bind(ViewModel, vm => vm.SelectedShareeView, v => v.sharees.SelectedItem));

            d(this.OneWayBind(ViewModel, vm => vm.Protocol, v => v.protocolImage.Source, i => iconProvider.GetDisk(i, 64)));
            d(this.WhenAnyValue(x => x.ViewModel.Protocol).Subscribe(x => AutomationProperties.SetName(protocolImage, x.getDescription())));

            d(this.BindCommand(ViewModel, vm => vm.Confirm, v => v.chooseButton));
            d(this.BindCommand(ViewModel, vm => vm.Cancel, v => v.cancelButton));

            d(this.BindInteraction(ViewModel, vm => vm.Close, c =>
            {
                DialogResult = c.Input;
                c.SetOutput(default);
                return Observable.Start(Close, RxApp.MainThreadScheduler);
            }));
        });
    }

    public sealed class Factory : IWindowFactory<PromptShareeViewModel>
    {
        private readonly WpfIconProvider iconProvider;

        public Factory(WpfIconProvider iconProvider)
        {
            this.iconProvider = iconProvider;
        }

        public PromptShareeWindow Create(PromptShareeViewModel viewModel)
        {
            return new PromptShareeWindow(iconProvider)
            {
                ViewModel = viewModel
            };
        }

        Window IWindowFactory<PromptShareeViewModel>.Create(PromptShareeViewModel viewModel) => Create(viewModel);
    }
}

public abstract class PromptShareeWindowBase : ReactiveWindow<PromptShareeViewModel> { }
