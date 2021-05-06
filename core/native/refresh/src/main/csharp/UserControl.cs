using System.Windows.Controls;

namespace Cyberduck.Core.Refresh
{
    public abstract class UserControl<T> : UserControl, IViewFor<T>
    {
        public UserControl(T viewModel)
        {
            ViewModel = viewModel;
        }

        public T ViewModel
        {
            get => (T)GetValue(DataContextProperty);
            set => SetValue(DataContextProperty, value);
        }
    }
}
