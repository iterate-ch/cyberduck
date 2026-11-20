using ch.cyberduck.core;
using ch.cyberduck.core.exception;
using ch.cyberduck.core.threading;
using Ch.Cyberduck.Core.Refresh.Splat;
using Ch.Cyberduck.Core.Refresh.ViewModels.Dialogs;
using java.util;
using Splat;
using System;
using System.Windows;
using X509Certificate = java.security.cert.X509Certificate;

namespace Ch.Cyberduck.Core.Refresh.Interactivity;

/// <summary>
/// Prompt to select a client certificate in a modal window. Applications create a subclass with a constructor
/// matching <c>CertificateIdentityCallbackFactory</c> that resolves the owner window.
/// </summary>
public class DialogPromptCertificateIdentityCallback : CertificateIdentityCallback
{
    private readonly Controller controller;
    private readonly Func<nint?> owner;

    /// <param name="controller">Controller to run the prompt on the main thread</param>
    /// <param name="owner">Resolves the handle of the owner window. Invoked on the main thread.</param>
    public DialogPromptCertificateIdentityCallback(Controller controller, Func<nint?> owner)
    {
        (this.controller, this.owner) = (controller, owner);
    }

    public X509Certificate prompt(string hostname, List certificates)
    {
        var viewModel = new PromptCertificateIdentityViewModel(hostname, certificates.AsEnumerable<X509Certificate>());
        if (viewModel.Items.Count == 0)
        {
            throw new ConnectionCanceledException();
        }

        var action = new PromptCertificateIdentityAction(viewModel, owner);
        controller.invoke(action, true);
        if (action.Result != true || viewModel.SelectedItem is null)
        {
            throw new ConnectionCanceledException();
        }

        return viewModel.SelectedItem.Certificate;
    }

    private class PromptCertificateIdentityAction : DefaultMainAction
    {
        private readonly Func<nint?> owner;
        private readonly PromptCertificateIdentityViewModel viewModel;

        public bool? Result { get; private set; }

        public PromptCertificateIdentityAction(PromptCertificateIdentityViewModel viewModel, Func<nint?> owner)
        {
            this.owner = owner;
            this.viewModel = viewModel;
        }

        public override void run()
        {
            var factory = Locator.Current.GetService<IWindowFactory<PromptCertificateIdentityViewModel>>();
            var window = factory.Create(viewModel);
            Result = window.ShowWithOwnerDialog(owner?.Invoke());
        }
    }
}
