using ch.cyberduck.core.notification;
using DesktopNotifications;
using Microsoft.Toolkit.Uwp.Notifications;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using Windows.Data.Xml.Dom;
using Windows.UI.Notifications;

namespace Ch.Cyberduck.Core.Notifications
{
    public abstract class AbstractDesktopNotificationService<TActivator> : NotificationService where TActivator : NotificationActivator
    {
        protected abstract string AumID { get; }

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

        NotificationService NotificationService.setup()
        {
            DesktopNotificationManagerCompat.RegisterAumidAndComServer<TActivator>(AumID);
            DesktopNotificationManagerCompat.RegisterActivator<TActivator>();

            return this;
        }

        void NotificationService.unregister()
        {
            throw new NotImplementedException();
        }
    }
}
