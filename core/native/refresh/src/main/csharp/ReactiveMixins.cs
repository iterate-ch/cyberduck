using ReactiveUI;
using System;
using System.Linq;
using System.Reactive.Linq;

namespace Ch.Cyberduck.Core.Refresh
{
    public static class ReactiveMixins
    {
        public static IObservable<bool> ExecuteIfPossible<TParam, TResult>(this ReactiveCommand<TParam, TResult> cmd) =>
            cmd.CanExecute.FirstAsync().Where(can => can).Do(async _ => await cmd.Execute());

        public static IObservable<bool> ExecuteIfPossible<TParam, TResult>(this ReactiveCommand<TParam, TResult> cmd, TParam param) =>
            cmd.CanExecute.FirstAsync().Where(can => can).Do(async _ => await cmd.Execute(param));

        public static bool GetCanExecute<TParam, TResult>(this ReactiveCommand<TParam, TResult> cmd) =>
            cmd.CanExecute.FirstAsync().Wait();
    }
}
