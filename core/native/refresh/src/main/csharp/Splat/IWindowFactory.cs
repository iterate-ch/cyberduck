using System.Windows;

namespace Ch.Cyberduck.Core.Refresh.Splat;

public interface IWindowFactory<TViewModel> where TViewModel : class
{
    Window Create(TViewModel model);
}
