using ch.cyberduck.core;

namespace Ch.Cyberduck.Core;

public static class FeatureFactoryExtensions
{
    public static T Feature<T>(this FeatureFactory factory)
    {
        return (T)factory.getFeature(typeof(T));
    }
}
