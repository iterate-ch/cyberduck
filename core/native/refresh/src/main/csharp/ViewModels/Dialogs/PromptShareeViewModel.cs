using ch.cyberduck.core;
using ch.cyberduck.core.features;
using DynamicData;
using DynamicData.Binding;
using DynamicData.Kernel;
using java.util;
using ReactiveUI;
using System;
using System.Collections;
using System.Collections.Generic;
using System.Collections.ObjectModel;
using System.Linq;
using System.Reactive;
using System.Reactive.Linq;

namespace Ch.Cyberduck.Core.Refresh.ViewModels.Dialogs;

public class PromptShareeViewModel : ReactiveObject
{
    private readonly ObservableAsPropertyHelper<ShareeItemViewModel> selectedShareeView;
    private Share.Sharee selectedSharee;

    public ReactiveCommand<Unit, Unit> Cancel { get; }

    public Interaction<bool, Unit> Close { get; } = new();

    public ReactiveCommand<Unit, Unit> Confirm { get; }

    public ReadOnlyCollection<ShareeItemViewModel> Items { get; }

    public Protocol Protocol { get; }

    public Share.Sharee SelectedSharee
    {
        get => selectedSharee;
        set => this.RaiseAndSetIfChanged(ref selectedSharee, value);
    }

    public ShareeItemViewModel SelectedShareeView
    {
        get => selectedShareeView.Value;
        set => SelectedSharee = value.Sharee;
    }

    public PromptShareeViewModel(Protocol protocol, Set sharees)
    {
        Protocol = protocol;

        Cancel = ReactiveCommand.CreateFromObservable(OnCancel);
        Confirm = ReactiveCommand.CreateFromObservable(OnConfirm);

        var worldSharee = new ShareeItemViewModel(Share.Sharee.world);
        var shareesSet = sharees
            .AsEnumerable<Share.Sharee>()
            .Where(s => !Share.Sharee.world.equals(s))
            .ToDictionary(sharee => sharee, model => new ShareeItemViewModel(model));

        List<ShareeItemViewModel> viewModels = new() { worldSharee };
        viewModels.AddRange(
            shareesSet.OrderBy(x => x.Key.getDescription()).Select(x => x.Value));
        shareesSet[Share.Sharee.world] = worldSharee;
        Items = viewModels.AsReadOnly();

        selectedSharee = worldSharee.Sharee;
        selectedShareeView = this.WhenValueChanged(x => x.SelectedSharee)
            .Select((Func<Share.Sharee, ShareeItemViewModel>)shareesSet.Lookup)
            .ToProperty(this, nameof(SelectedShareeView));
    }

    private IObservable<Unit> OnCancel() => OnClose(false);

    private IObservable<Unit> OnClose(bool result) => Close.Handle(result);

    private IObservable<Unit> OnConfirm() => OnClose(true);

    public class ShareeItemViewModel : ReactiveObject
    {
        public string Description { get; }

        public Share.Sharee Sharee { get; }

        public ShareeItemViewModel(Share.Sharee sharee)
        {
            Sharee = sharee;
            Description = sharee.getDescription();
        }

        public override string ToString() => Description;
    }
}
