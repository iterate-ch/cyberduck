namespace Cyberduck.Core.Refresh
{
    public interface IViewFor
    {
    }

    public interface IViewFor<T> : IViewFor
    {
        T ViewModel { get; }
    }
}
