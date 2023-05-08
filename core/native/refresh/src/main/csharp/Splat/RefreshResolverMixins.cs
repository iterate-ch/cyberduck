using System;

namespace Splat
{
    public static class RefreshDependencyResolverMixins
    {
        /// <inheritdoc cref="DependencyResolverMixins.GetService{T}(IReadonlyDependencyResolver, string?)"/>
        public static bool GetService<T>(this IReadonlyDependencyResolver resolver, out T service, string? contract = null)
        {
            if (resolver == null)
            {
                throw new ArgumentNullException("resolver");
            }

            return (service = (T)resolver.GetService(typeof(T), contract)) is not null;
        }

    }
}
