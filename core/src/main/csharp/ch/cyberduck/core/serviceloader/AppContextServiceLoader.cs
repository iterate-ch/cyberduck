using ikvm.runtime;
using java.lang;
using java.util;
using org.apache.logging.log4j;
using System;
using System.IO;
using System.Reflection;

namespace ch.cyberduck.core.serviceloader;

public sealed class AppContextServiceLoader : AutoServiceLoader
{
    /* 
     * ServiceLoader is factory-created, and only lives for a short time.
     * The logger doesn't need to survive for longer than the loader.
     */
    private readonly Logger _logger = LogManager.getLogger(typeof(AppContextServiceLoader));

    public Set load(Class c)
    {
        HashSet set = [];
        var files = Directory.EnumerateFiles(AppContext.BaseDirectory, "*.dll", SearchOption.TopDirectoryOnly);
        foreach (var item in files)
        {
            Assembly assembly;
            try
            {
                assembly = Assembly.LoadFrom(item);
            }
            catch
            {
                /* 
                 * Silently skip native libraries
                 * or otherwise bad files
                 */
                continue;
            }

            try
            {
                foreach (var service in ServiceLoader.load(c, AssemblyClassLoader.getAssemblyClassLoader(assembly)))
                {
                    set.add(service);
                }
            }
            catch (System.Exception e)
            {
                if (_logger.isDebugEnabled())
                {
                    _logger.debug($"Activating {c.getName()} instances in {assembly.FullName}", e);
                }
            }
        }

        return set;
    }
}
