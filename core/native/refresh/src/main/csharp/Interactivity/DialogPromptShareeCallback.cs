using ch.cyberduck.core;
using ch.cyberduck.core.exception;
using ch.cyberduck.core.features;
using ch.cyberduck.core.threading;
using Ch.Cyberduck.Core.Refresh.Splat;
using Ch.Cyberduck.Core.Refresh.ViewModels.Dialogs;
using java.util;
using Splat;
using System.Windows;

namespace Ch.Cyberduck.Core.Refresh.Interactivity;

public class DialogPromptShareeCallback : Share.ShareeCallback
{
    private readonly Controller controller;
    private readonly Host host;
    private readonly nint parent;

    public DialogPromptShareeCallback(Host host, nint parent, Controller controller)
    {
        (
            this.controller,
            this.host,
            this.parent) = (controller, host, parent);
    }

    public Share.Sharee prompt(Share.Type type, Set sharees)
    {
        var viewModel = new PromptShareeViewModel(host.getProtocol(), sharees);
        var action = new PromptShareeAction(viewModel, parent);
        controller.invoke(action, true);
        if (action.Result != true)
        {
            throw new ConnectionCanceledException();
        }

        return viewModel.SelectedSharee;
    }

    private class PromptShareeAction : DefaultMainAction
    {
        private readonly nint parent;
        private readonly PromptShareeViewModel viewModel;

        public bool? Result { get; private set; }

        public PromptShareeAction(PromptShareeViewModel viewModel, nint parent)
        {
            this.parent = parent;
            this.viewModel = viewModel;
        }

        public override void run()
        {
            var factory = Locator.Current.GetService<IWindowFactory<PromptShareeViewModel>>();
            var window = factory.Create(viewModel);
            Result = window.ShowWithOwnerDialog(parent);
        }
    }
}
