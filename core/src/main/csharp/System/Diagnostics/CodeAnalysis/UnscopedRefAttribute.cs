namespace System.Diagnostics.CodeAnalysis;

[AttributeUsage(AttributeTargets.Method | AttributeTargets.Parameter | AttributeTargets.Property, AllowMultiple = false, Inherited = false)]
public sealed class UnscopedRefAttribute : Attribute
{
}
