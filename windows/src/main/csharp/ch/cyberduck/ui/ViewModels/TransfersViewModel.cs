// Copyright(c) 2002 - 2024 iterate GmbH. All rights reserved.
// https://cyberduck.io/
// 
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
// 
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.

using ch.cyberduck.core.i18n;
using ch.cyberduck.core.local;
using ch.cyberduck.core.preferences;
using ch.cyberduck.core.transfer;
using ch.cyberduck.ui.Model;
using Ch.Cyberduck.Core.Refresh.Services;
using Ch.Cyberduck.Ui.Controller;
using CommunityToolkit.Mvvm.ComponentModel;
using CommunityToolkit.Mvvm.Input;
using CommunityToolkit.Mvvm.Messaging;
using CommunityToolkit.Mvvm.Messaging.Messages;
using DynamicData;
using DynamicData.Aggregation;
using DynamicData.Binding;
using org.apache.logging.log4j;
using System;
using System.Collections.Generic;
using System.Collections.ObjectModel;
using System.Linq;
using System.Reactive.Disposables;
using System.Reactive.Linq;
using System.Threading;
using System.Threading.Tasks;
using System.Windows.Media;
using System.Windows.Shell;
using static Ch.Cyberduck.ImageHelper;

namespace ch.cyberduck.ui.ViewModels;

public sealed partial class TransfersViewModel : ObservableObject, IDisposable
{
    public static readonly Logger Log = LogManager.getLogger(typeof(TransferViewModel).FullName);

    private readonly Dictionary<int, BandwidthViewModel> bandwidthLookup;
    private readonly Dictionary<int, ConnectionViewModel> connectionLookup;
    private readonly WpfIconProvider iconProvider;
    private readonly CompositeDisposable subscriptions = [];
    private readonly Preferences preferences;
    private readonly ReadOnlyObservableCollection<TransferViewModel> selectedTransfers;
    private readonly TransfersStore store;
    private readonly DrawingImage taskbarOverlayCache;
    private readonly ReadOnlyObservableCollection<TransferViewModel> transfers;
    [ObservableProperty, NotifyCanExecuteChangedFor(nameof(OpenCommand))]
    private bool canOpen;
    [ObservableProperty, NotifyCanExecuteChangedFor(nameof(ShowCommand))]
    private bool canShow;
    [ObservableProperty]
    private double globalProgress;
    private BandwidthViewModel selectedBandwidth;
    [ObservableProperty]
    private ConnectionViewModel selectedConnectionLimit;
    [ObservableProperty]
    [NotifyPropertyChangedFor(nameof(BandwidthEnabled))]
    private TransferViewModel selectedTransfer;
    [ObservableProperty]
    private ImageSource selectedTransferFileIcon;
    [ObservableProperty]
    private string selectedTransferLocal;
    [ObservableProperty]
    private string selectedTransferUrl;
    [ObservableProperty]
    private string taskbarDescription;
    [ObservableProperty]
    private DrawingImage taskbarOverlay;
    [ObservableProperty]
    private TaskbarItemProgressState trayProgressState;

    public ObservableCollection<BandwidthViewModel> Bandwidth { get; }

    public bool BandwidthEnabled => SelectedTransfer is not null;

    public ObservableCollection<ConnectionViewModel> Connections { get; }

    public BandwidthViewModel SelectedBandwidth
    {
        get => selectedBandwidth;
        set
        {
            if (SetProperty(ref selectedBandwidth, value))
            {
                OnSelectedBandwidthChanged(value);
            }
        }
    }

    public ReadOnlyObservableCollection<TransferViewModel> SelectedTransfers => selectedTransfers;

    public bool ToolbarCleanup
    {
        get => preferences.getBoolean("transfer.toolbar.cleanup");
        set
        {
            OnPropertyChanging();
            preferences.setProperty("transfer.toolbar.cleanup", value);
            OnPropertyChanged();
            OnPropertyChanged(nameof(ToolbarSeparatorLeft));
        }
    }

    public bool ToolbarOpen
    {
        get => preferences.getBoolean("transfer.toolbar.open");
        set
        {
            OnPropertyChanging();
            preferences.setProperty("transfer.toolbar.open", value);
            OnPropertyChanged();
            OnPropertyChanged(nameof(ToolbarSeparatorRight));
        }
    }

    public bool ToolbarReload
    {
        get => preferences.getBoolean("transfer.toolbar.reload");
        set
        {
            OnPropertyChanging();
            preferences.setProperty("transfer.toolbar.reload", value);
            OnPropertyChanged();
            OnPropertyChanged(nameof(ToolbarSeparatorLeft));
        }
    }

    public bool ToolbarRemove
    {
        get => preferences.getBoolean("transfer.toolbar.remove");
        set
        {
            OnPropertyChanging();
            preferences.setProperty("transfer.toolbar.remove", value);
            OnPropertyChanged();
            OnPropertyChanged(nameof(ToolbarSeparatorLeft));
        }
    }

    public bool ToolbarResume
    {
        get => preferences.getBoolean("transfer.toolbar.resume");
        set
        {
            OnPropertyChanging();
            preferences.setProperty("transfer.toolbar.resume", value);
            OnPropertyChanged();
            OnPropertyChanged(nameof(ToolbarSeparatorLeft));
        }
    }

    public bool ToolbarShow
    {
        get => preferences.getBoolean("transfer.toolbar.show");
        set
        {
            OnPropertyChanging();
            preferences.setProperty("transfer.toolbar.show", value);
            OnPropertyChanged();
            OnPropertyChanged(nameof(ToolbarSeparatorRight));
        }
    }

    public bool ToolbarStop
    {
        get => preferences.getBoolean("transfer.toolbar.stop");
        set
        {
            OnPropertyChanging();
            preferences.setProperty("transfer.toolbar.stop", value);
            OnPropertyChanged();
            OnPropertyChanged(nameof(ToolbarSeparatorLeft));
        }
    }

    public bool ToolbarTrash
    {
        get => preferences.getBoolean("transfer.toolbar.trash");
        set
        {
            OnPropertyChanging();
            preferences.setProperty("transfer.toolbar.trash", value);
            OnPropertyChanged();
            OnPropertyChanged(nameof(ToolbarSeparatorLeft));
        }
    }

    public bool ToolbarSeparatorLeft
    {
        get
        {
            var left = ToolbarRemove | ToolbarTrash | ToolbarCleanup;
            var right = ToolbarResume | ToolbarStop | ToolbarReload;
            return left && right;
        }
    }

    public bool ToolbarSeparatorRight
    {
        get
        {
            return ToolbarOpen | ToolbarShow;
        }
    }

    public ReadOnlyObservableCollection<TransferViewModel> Transfers => transfers;

    private TransferController Controller { get; }

    public TransfersViewModel(
        TransferController controller,
        TransfersStore store,
        Locale locale,
        BandwidthProvider bandwidthProvider,
        ConnectionProvider connectionProvider,
        Preferences preferences,
        WpfIconProvider iconProvider)
    {
        Controller = controller;
        this.store = store;
        this.preferences = preferences;
        this.iconProvider = iconProvider;

        var transfersCache = store.Transfers.Connect().ObserveOnDispatcher()
            .Transform(m => new TransferViewModel(controller, m, locale)).DisposeMany()
            .Bind(out transfers).AsObservableCache().DisposeWith(subscriptions);
        subscriptions.Add(transfersCache.Connect().WhenAnyPropertyChanged().Subscribe(OnTransferItemChanged));
        subscriptions.Add(transfersCache.CountChanged.Subscribe(OnTransfersCountChanged));

        var localTransfers = transfersCache.Connect()
            .AutoRefresh(m => m.IsSelected).Filter(m => m.IsSelected)
            .Bind(out selectedTransfers).AsObservableCache().DisposeWith(subscriptions);
        subscriptions.Add(localTransfers.Connect().WhenAnyPropertyChanged().ObserveOnDispatcher().Subscribe(OnSelectedTransferPropertyChanged));
        subscriptions.Add(localTransfers.CountChanged.Subscribe(OnSelectionCountChanged));

        var progressStream = controller.Progress.Connect();
        subscriptions.Add(Observable.CombineLatest(
            controller.Progress.CountChanged,
            progressStream.Avg(m => m.Progress)
                .InvalidateWhen(progressStream.WhenPropertyChanged(v => v.Progress, false)),
            progressStream.TrueForAll(
                v => v.WhenPropertyChanged(v => v.Progress),
                v => v.Value is not null),
            (count, progress, allRunning) => new ProgressState(allRunning, count, progress)).Subscribe(OnProgressChanged));

        BandwidthViewModel unlimitedBandwidth = new(-1, true, locale.localize("Unlimited Bandwidth", "Preferences"));
        Bandwidth = [
            unlimitedBandwidth,
            ..bandwidthProvider.Bandwidth.Select(m => new BandwidthViewModel(m, true)),
        ];
        bandwidthLookup = Bandwidth.ToDictionary(m => m.Bandwidth);
        selectedBandwidth = unlimitedBandwidth;

        Connections = [
            .. connectionProvider.Connections.Select(m => new ConnectionViewModel(m, true)),
        ];
        connectionLookup = Connections.ToDictionary(m => m.Connections);
        selectedConnectionLimit = FetchConnectionLimit();

        subscriptions.Add(this.WhenValueChanged(v => v.Controller.BadgeLabel, fallbackValue: () => null).Subscribe(v => TaskbarDescription = v));
    }

    public void Dispose()
    {
        subscriptions.Dispose();
    }

    partial void OnSelectedConnectionLimitChanged(ConnectionViewModel value)
    {
        preferences.setProperty("queue.connections.limit", value.Connections);
        TransferQueueFactory.get().resize(value.Connections);
    }

    partial void OnSelectedTransferChanged(TransferViewModel newValue)
    {
        UpdateSelectedBandwidth();
        UpdateSelectionPanel();
        ValidateCommands();
    }

    private static void LogTask(Task task)
    {
        if (Log.isDebugEnabled() && task is { IsFaulted: true, Exception: { } exception })
        {
            Log.debug(exception);
        }
    }

    private ConnectionViewModel FetchConnectionLimit()
    {
        var rate = preferences.getInteger("queue.connections.limit");
        if (!connectionLookup.TryGetValue(rate, out var selection))
        {
            connectionLookup[rate] = selection = new(rate, false, null);
            Connections.Add(selection);
        }

        return selection;
    }

    [RelayCommand(CanExecute = nameof(ValidateCleanCommand))]
    private void OnClean() => store.CleanCompleted();

    [RelayCommand(CanExecute = nameof(CanOpen))]
    private void OnOpen()
    {
        var launcher = ApplicationLauncherFactory.get();
        foreach (var file in SelectedTransfer.Roots)
        {
            if (launcher.open(file.TransferItem.Local))
            {
                return;
            }
        }
    }

    [RelayCommand(AllowConcurrentExecutions = true)]
    private async Task OnOpenCanExecute(CancellationToken cancellationToken)
    {
        CanOpen = false;
        try
        {
            CanOpen = await Task.Run(Run, cancellationToken).ConfigureAwait(true);
        }
        catch { }

        bool Run()
        {
            if (SelectedTransfers.Count != 1)
            {
                return false;
            }

            if (SelectedTransfer?.Local is null || SelectedTransfer.Completed != true)
            {
                return false;
            }

            using var enumerator = SelectedTransfer.Roots.GetEnumerator();
            while (!cancellationToken.IsCancellationRequested && enumerator.MoveNext())
            {
                if (enumerator.Current.TransferItem.Local.exists())
                {
                    return true;
                }
            }

            return false;
        }
    }

    private void OnProgressChanged(ProgressState obj)
    {
        var (running, count, progress) = obj;
        TrayProgressState = count == 0
            ? TaskbarItemProgressState.None
            : running
                ? TaskbarItemProgressState.Normal
                : TaskbarItemProgressState.Indeterminate;
        GlobalProgress = progress / 100;
    }

    [RelayCommand(CanExecute = nameof(ValidateReloadCommand))]
    private void OnReload()
    {
        foreach (var item in SelectedTransfers)
        {
            if (!item.Running)
            {
                TransferOptions options = new TransferOptions().resume(false).reload(true);
                Controller.StartTransfer(item.Transfer.Model, options);
            }
        }
    }

    [RelayCommand(CanExecute = nameof(ValidateRemoveCommand))]
    private void OnRemove()
    {
        using var copy = SelectedTransfers.CopyToPool(true);
        foreach (var transfer in copy.Span)
        {
            if (!transfer.Running)
            {
                store.RemoveTransfer(transfer.Transfer);
            }
        }

        store.Save();
    }

    [RelayCommand(CanExecute = nameof(ValidateResumeCommand))]
    private void OnResume()
    {
        foreach (var item in SelectedTransfers)
        {
            if (!item.Running)
            {
                TransferOptions options = new TransferOptions()
                    .resume(true)
                    .reload(false);
                Controller.StartTransfer(item.Transfer.Model, options);
            }
        }
    }

    private void OnSelectedBandwidthChanged(BandwidthViewModel value)
    {
        foreach (var transfer in SelectedTransfers)
        {
            transfer.Transfer.Model.setBandwidth(value.Bandwidth);
            if (transfer.Running && Controller.Lookup(transfer.Transfer.Model) is { Action: { } action })
            {
                action.getMeter().reset();
            }

            transfer.Transfer.Refresh();
        }
    }

    private void OnSelectedTransferPropertyChanged(TransferViewModel _)
    {
        ValidateCommands();
    }

    private void OnSelectionCountChanged(int _)
    {
        OnSelectedTransferChanged(null);
    }

    [RelayCommand(CanExecute = nameof(CanShow))]
    private void OnShow()
    {
        var reveal = RevealServiceFactory.get();
        foreach (var item in SelectedTransfers)
        {
            if (item.Local != null)
            {
                foreach (var file in item.Roots)
                {
                    reveal.reveal(file.TransferItem.Local);
                }
            }
        }
    }

    [RelayCommand(AllowConcurrentExecutions = true)]
    private async Task OnShowCanExecute(CancellationToken cancellationToken)
    {
        CanShow = false;
        bool __result = false;
        try
        {
            __result = await Task.Run(Run, cancellationToken);
        }
        catch { }
        finally
        {
            if (!cancellationToken.IsCancellationRequested)
            {
                CanShow = __result;
            }
        }

        bool Run()
        {
            foreach (var item in SelectedTransfers)
            {
                if (cancellationToken.IsCancellationRequested)
                {
                    return false;
                }

                if (item.Local is null)
                {
                    continue;
                }

                foreach (var file in item.Roots)
                {
                    if (cancellationToken.IsCancellationRequested)
                    {
                        return false;
                    }
                    if (file.TransferItem.Local.exists())
                    {
                        return true;
                    }
                }
            }

            return false;
        }
    }

    [RelayCommand(CanExecute = nameof(ValidateStopCommand))]
    private void OnStop()
    {
        foreach (var item in selectedTransfers)
        {
            if (!item.Running)
            {
                continue;
            }

            item.Cancel();
        }
    }

    private void OnTransfersCountChanged(int obj)
    {
        CleanCommand.NotifyCanExecuteChanged();
    }

    private void OnTransferItemChanged(TransferViewModel transferViewModel)
    {
        if (SelectedTransfers.Count is 0 && transferViewModel.ProgressState is not null)
        {
            transferViewModel.IsSelected = true;
            WeakReferenceMessenger.Default.Send(new BringIntoViewMessage(transferViewModel));
        }
    }

    [RelayCommand(CanExecute = nameof(ValidateTrashCommand))]
    private void OnTrash()
    {
        foreach (var item in selectedTransfers)
        {
            if (!item.Running)
            {
                foreach (var file in item.Transfer.Roots.Items)
                {
                    file.Delete();
                }
            }
        }
    }

    private void UpdateSelectedBandwidth()
    {
        if (SelectedTransfer is null)
        {
            return;
        }

        if (SelectedBandwidth?.Enabled == false)
        {
            bandwidthLookup.Remove(SelectedBandwidth.Bandwidth);
            Bandwidth.Remove(SelectedBandwidth);
        }

        var rate = (int)SelectedTransfer.Transfer.BandwidthRate;
        if (!bandwidthLookup.TryGetValue(rate, out var selection))
        {
            bandwidthLookup[rate] = selection = new(rate, false, BandwidthProvider.Format(rate));
            Bandwidth.Add(selection);
        }

        selectedBandwidth = selection;
        OnPropertyChanged(nameof(SelectedBandwidth));
    }

    private void UpdateSelectionPanel()
    {
        if (SelectedTransfers.Count == 1 && SelectedTransfer is { Transfer: { Model: { } model } transfer })
        {
            SelectedTransferFileIcon = SelectedTransfer.Roots switch
            {
                { Count: not 1 } => Images.Multiple,
                _ => transfer.Local switch
                {
                    null => iconProvider.GetPath(model.getRoot().remote, 32),
                    _ => iconProvider.GetFileIcon(model.getRoot().local.getAbsolute(), false, true, false),
                },
            };
            SelectedTransferLocal = transfer.Local;
            SelectedTransferUrl = model.getRemote().getUrl();
        }
        else
        {
            SelectedTransferFileIcon = null;
            SelectedTransferLocal = null;
            SelectedTransferUrl = null;
        }
    }

    private bool ValidateCleanCommand() => Transfers.Count > 0;

    private void ValidateCommands()
    {
        CleanCommand.NotifyCanExecuteChanged();
        ReloadCommand.NotifyCanExecuteChanged();
        RemoveCommand.NotifyCanExecuteChanged();
        ResumeCommand.NotifyCanExecuteChanged();
        StopCommand.NotifyCanExecuteChanged();
        TrashCommand.NotifyCanExecuteChanged();

        OpenCanExecuteCommand.ExecuteAsync(null).ContinueWith(LogTask);
        ShowCanExecuteCommand.ExecuteAsync(null).ContinueWith(LogTask);
    }

    private bool ValidateReloadCommand()
    {
        foreach (var item in SelectedTransfers)
        {
            if (item.Transfer.Type.isReloadable())
            {
                return true;
            }
        }

        return false;
    }

    private bool ValidateRemoveCommand() => SelectedTransfer is not null;

    private bool ValidateResumeCommand()
    {
        foreach (var item in SelectedTransfers)
        {
            if (item.Completed == false)
            {
                return true;
            }
        }

        return false;
    }

    private bool ValidateStopCommand()
    {
        foreach (var item in SelectedTransfers)
        {
            if (item.Running)
            {
                return true;
            }
        }

        return false;
    }

    private bool ValidateTrashCommand()
    {
        return SelectedTransfer is not null;
    }

    public class BringIntoViewMessage(TransferViewModel transfer) : ValueChangedMessage<TransferViewModel>(transfer);

    private readonly record struct ProgressState(bool Running, int Count, double Progress);
}
