using ch.cyberduck.core.notification;
using DesktopNotifications;
using Microsoft.Toolkit.Uwp.Notifications;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Runtime.InteropServices;
using System.Text;
using System.Threading.Tasks;
using Windows.Data.Xml.Dom;
using Windows.UI.Notifications;

namespace Ch.Cyberduck.Ui.Controller
{
    public class DesktopNotificationService : NotificationService
    {
        public void notify(string title, string description)
        {
            // Construct the visuals of the toast (using Notifications library)
            ToastContent toastContent = new ToastContent()
            {
                Visual = new ToastVisual()
                {
                    BindingGeneric = new ToastBindingGeneric()
                    {
                        Children =
                        {
                            new AdaptiveText()
                            {
                                Text = title
                            },
                            new AdaptiveText()
                            {
                                Text = description
                            }
                        }
                    }
                }
            };

            var doc = new XmlDocument();
            doc.LoadXml(toastContent.GetContent());

            var toast = new ToastNotification(doc);

            DesktopNotificationManagerCompat.CreateToastNotifier().Show(toast);
        }

        public void setup()
        {
            DesktopNotificationManagerCompat.RegisterAumidAndComServer<DesktopNotificationActivator>("iterate.Cyberduck");
            DesktopNotificationManagerCompat.RegisterActivator<DesktopNotificationActivator>();
        }

        public void unregister()
        {
            DesktopNotificationManagerCompat.History.Clear();
        }

        [ClassInterface(ClassInterfaceType.None)]
        [ComSourceInterfaces(typeof(INotificationActivationCallback))]
        [Guid("04FAA434-8D25-4690-AF66-B63B39C1FEDE"), ComVisible(true)]
        public class DesktopNotificationActivator : NotificationActivator
        {
            public override void OnActivated(string arguments, NotificationUserInput userInput, string appUserModelId)
            {
            }
        }
    }
}
