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
using ch.cyberduck.core.transfer;
using DynamicData;
using DynamicData.Binding;
using java.util.function;

namespace ch.cyberduck.ui.Model
{
    public class TransfersStore
    {
        private readonly TransferCollection transfers;

        public IObservableCache<TransferModel, Transfer> Transfers { get; }

        public TransfersStore()
        {
            transfers = TransferCollection.defaultCollection();
            Transfers = transfers
                .ToObservableChangeSet((Transfer data, TransferModel model) =>
                {
                    model ??= new(data);
                    model.Refresh();
                    return model;
                }, true).AddKey(x => x.Model).AsObservableCache();
        }

        public void CleanCompleted()
        {
            transfers.removeIf(new CleanPredicate());
        }

        public void RemoveTransfer(TransferModel transfer)
        {
            transfers.remove(transfer.Model);
        }

        public void Save()
        {
            transfers.save();
        }

        private sealed class CleanPredicate : Predicate
        {
            public bool test(object t) => ((Transfer)t).isComplete();

            Predicate Predicate.and(Predicate other) => Predicate.__DefaultMethods.and(this, other);

            Predicate Predicate.negate() => Predicate.__DefaultMethods.negate(this);

            Predicate Predicate.or(Predicate other) => Predicate.__DefaultMethods.or(this, other);
        }
    }
}
