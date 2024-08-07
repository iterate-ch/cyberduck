namespace System.Diagnostics.CodeAnalysis;

#if NETFRAMEWORK

[AttributeUsage(AttributeTargets.Method | AttributeTargets.Parameter | AttributeTargets.Property, AllowMultiple = false, Inherited = false)]
public sealed class UnscopedRefAttribute : Attribute
{
}

#endif
