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

using ch.cyberduck.core;
using ch.cyberduck.core.date;
using ch.cyberduck.core.formatter;
using ch.cyberduck.core.i18n;
using ch.cyberduck.core.transfer;
using ch.cyberduck.ui.Model;
using Ch.Cyberduck.Ui.Controller;
using CommunityToolkit.Mvvm.ComponentModel;
using DynamicData;
using DynamicData.Binding;
using System;
using System.Collections.ObjectModel;
using System.ComponentModel;
using System.Globalization;
using System.Reactive.Disposables;
using System.Reactive.Linq;
using System.Text;

namespace ch.cyberduck.ui.ViewModels
{
    public partial class TransferViewModel : SynchronizedObservableObject, IDisposable
    {
        private static readonly UserDateFormatter dateFormatter = UserDateFormatterFactory.get();
        private static readonly SizeFormatter sizeFormatter = SizeFormatterFactory.get(false);
        private readonly Locale locale;
        private readonly SerialDisposable progressStateNotifications = new();
        private readonly ReadOnlyObservableCollection<TransferItemViewModel> roots;
        private readonly string transferTypeName;
        [ObservableProperty]
        private bool isSelected;
        [ObservableProperty]
        private double progress;
        [ObservableProperty]
        private bool progressPending;
        [ObservableProperty]
        [NotifyPropertyChangedFor(nameof(MessageText))]
        [NotifyPropertyChangedFor(nameof(ProgressText))]
        [NotifyPropertyChangedFor(nameof(Summary))]
        private TransferProgressModel progressState;
        [ObservableProperty]
        [NotifyPropertyChangedFor(nameof(Summary))]
        private bool running;
        [ObservableProperty]
        private TransferItemViewModel selectedTransferItem;
        [ObservableProperty]
        private int selectedTransferItemIndex;

        public bool Completed => Transfer.Completed;

        public string Local => Transfer.Local;

        public string MessageText => ProgressState switch
        {
            { } state => state.Message,
            _ => Transfer.Timestamp switch
            {
                { } date => dateFormatter.getLongFormat(date.getTime(), false),
                _ => null,
            },
        };

        public string Name => Transfer.Name;

        public string ProgressText => ProgressState switch
        {
            { } state => state.Text,
            _ => string.Format(
                locale.localize("{0} of {1}", "Localizable"),
                sizeFormatter.format(Transfer.Transferred),
                sizeFormatter.format(Transfer.Size)),
        };

        public string StatusText => Transfer.Completed
            ? locale.localize(string.Format("{0} complete", transferTypeName), "Status")
            : locale.localize("Transfer incomplete", "Status");

        public string Summary
        {
            get
            {
                StringBuilder builder = new();

                if (Running)
                {
                    builder.AppendLine(locale.localize("Transfer in progress", "Localizable"));
                }
                else
                {
                    builder.AppendLine(StatusText);
                    builder.AppendLine(ProgressText);
                }

                return builder.ToString();
            }
        }

        public TransferModel Transfer { get; }

        public TransferDirection TransferDirection { get; }

        public ReadOnlyObservableCollection<TransferItemViewModel> Roots => roots;

        public TransferViewModel(TransferController controller, TransferModel transfer, Locale locale)
        {
            this.locale = locale;
            Transfer = transfer;

            transferTypeName = CultureInfo.CurrentCulture.TextInfo.ToTitleCase(Transfer.Model.getType().name());

            var progressState = Observable.Create<TransferProgressModel>(observer =>
            {
                bool emitEmpty = true;
                var subscription = controller.Progress.Watch(transfer.Model)
                    .Subscribe(next =>
                    {
                        if (next.Reason == ChangeReason.Remove)
                        {
                            observer.OnNext(default);
                        }
                        else
                        {
                            observer.OnNext(next.Current);
                        }

                        emitEmpty = false;
                    }, observer.OnError, observer.OnCompleted);

                if (emitEmpty)
                {
                    observer.OnNext(null);
                    if (subscription == Disposable.Empty)
                    {
                        observer.OnCompleted();
                    }
                }

                return subscription;
            }).Subscribe(v => ProgressState = v);

            Transfer.Roots.Connect().ObserveOnDispatcher()
                .Transform(m => new TransferItemViewModel(m))
                .DisposeMany().Bind(out roots).Subscribe();

#pragma warning disable CS8509 
            TransferDirection = transfer.Model switch
#pragma warning restore CS8509 
            {
                DownloadTransfer => TransferDirection.Download,
                UploadTransfer or CopyTransfer => TransferDirection.Upload,
                SyncTransfer => TransferDirection.Sync,
            };

            transfer.PropertyChanged += OnTransferChanged;
        }

        public void Cancel()
        {
            ProgressState?.Cancel();
        }

        void IDisposable.Dispose()
        {
            Transfer.PropertyChanged -= OnTransferChanged;
            progressStateNotifications.Dispose();
        }

        partial void OnProgressStateChanged(TransferProgressModel value)
        {
            if (value is null)
            {
                Running = false;
            }
            else
            {
                Running = true;
                (ProgressPending, Progress) = value.Progress switch
                {
                    null => (true, 0),
                    double progress => (false, progress)
                };

                OnPropertyChanged(nameof(MessageText));
                OnPropertyChanged(nameof(ProgressText));
            }
        }

        partial void OnProgressStateChanged(TransferProgressModel old, TransferProgressModel value)
        {
            progressStateNotifications.Disposable = value?.WhenAnyPropertyChanged().Subscribe(OnProgressStateChanged);
        }

        private void OnTransferChanged(object sender, PropertyChangedEventArgs e)
        {
            switch (e.PropertyName)
            {
                case nameof(TransferModel.Completed):
                    OnPropertyChanged(nameof(Completed));
                    OnPropertyChanged(nameof(StatusText));
                    OnPropertyChanged(nameof(Summary));
                    break;

                case nameof(TransferModel.Timestamp):
                    OnPropertyChanged(nameof(MessageText));
                    break;

                case nameof(TransferModel.Size):
                case nameof(TransferModel.Transferred):
                    OnPropertyChanged(nameof(ProgressText));
                    OnPropertyChanged(nameof(Summary));
                    break;

                case nameof(TransferModel.Name):
                case nameof(TransferModel.Local):
                    OnPropertyChanged(e);
                    break;
            }
        }
    }

    public enum TransferDirection
    {
        Download,
        Upload,
        Sync,
    }
}
