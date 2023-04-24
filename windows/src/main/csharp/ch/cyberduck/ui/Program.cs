using ch.cyberduck.core.ctera;
using ch.cyberduck.core.preferences;
using Ch.Cyberduck.Ui.Controller;
using Ch.Cyberduck.Ui.Core.Contracts;
using Ch.Cyberduck.Ui.Core.Preferences;
using StructureMap;
using System;
using System.IO;
using System.ServiceModel;
using System.Threading;
using System.Threading.Tasks;
using System.Web;
using System.Windows.Forms;

namespace Ch.Cyberduck.Ui
{
    internal static class Program
    {
        [STAThread]
        private static void Main(string[] args)
        {
            bool newInstance;
            Mutex mutex = new Mutex(true, "iterate/cyberduck.io", out newInstance);

            StructureMapBootstrapper.Bootstrap();
            PreferencesFactory.set(ObjectFactory.GetInstance<Preferences>());
            var argsTask = Task.Run(async () =>
            {
                using (var channel = new ChannelFactory<ICyberduck>(new NetNamedPipeBinding(), new EndpointAddress("net.pipe://localhost/iterate/cyberduck.io")))
                {
                    ICyberduck proxy = null;

                    // Connect to pipe
                    int retries = 0;
                    bool connected = false;
                    while (!connected && retries < 7) // Timeout after 127 seconds
                    {
                        try
                        {
                            proxy = channel.CreateChannel();
                            proxy.Connect();
                            connected = true;
                        }
                        catch (Exception e)
                        {
                            await Task.Delay(TimeSpan.FromSeconds(Math.Pow(2, retries++)));
                        }
                    }
                    if (!connected)
                    {
                        throw new TimeoutException();
                    }

                    if (!newInstance)
                    {
                        proxy.NewInstance();
                    }

                    foreach (var item in args)
                    {
                        Uri result;
                        if (Uri.TryCreate(item, UriKind.Absolute, out result) && !proxy.OAuth(result))
                        {
                            var scheme = result.Scheme.ToLowerInvariant();
                            if (scheme == "file" && result.IsFile)
                            {
                                var localPath = result.LocalPath;
                                if (File.Exists(localPath))
                                {
                                    switch (Path.GetExtension(localPath).ToLowerInvariant())
                                    {
                                        case ".cyberducklicense":
                                            proxy.RegisterRegistration(localPath);
                                            break;

                                        case ".cyberduckprofile":
                                            proxy.RegisterProfile(localPath);
                                            break;

                                        case ".duck":
                                            proxy.RegisterBookmark(localPath);
                                            break;
                                    }
                                }
                            }
                            else
                            {
                                proxy.QuickConnect(item);
                            }
                        }
                    }
                }
            });

            if (newInstance)
            {
                Application.EnableVisualStyles();
                Application.SetCompatibleTextRenderingDefault(false);

                Application.Run(ObjectFactory.GetInstance<MainController>());
            }
            else
            {
                try
                {
                    argsTask.Wait();
                }
                catch (AggregateException aggregateException)
                {
                    aggregateException.Handle(x =>
                    {
                        if (x is CommunicationObjectFaultedException)
                        {
                            // silent catch this error.
                            return true;
                        }
                        return false;
                    });
                }
            }
            mutex.Close();
        }
    }
}
