namespace Ch.Cyberduck.Core.Preferences;

public class PropertyStoreFactory<T> : IPropertyStoreFactory
    where T : IPropertyStore, new()
{
    public IPropertyStore New() => new T();
}
