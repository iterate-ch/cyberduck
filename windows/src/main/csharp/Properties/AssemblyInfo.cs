using ikvm.runtime;
using IKVM.Attributes;

[assembly: System.Windows.ThemeInfo(System.Windows.ResourceDictionaryLocation.None, System.Windows.ResourceDictionaryLocation.SourceAssembly)]
[assembly: CustomAssemblyClassLoader(typeof(AppDomainAssemblyClassLoader))]
