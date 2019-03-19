using ch.cyberduck.core;
using ch.cyberduck.core.notification;
using ch.cyberduck.core.pool;
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
        private DesktopNotificationHistoryCompat history;

        protected abstract string AumID { get; }

        public void notify(string group, string identifier, string title, string description)
        {
            var toastContent = new ToastContent
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
            if (!string.IsNullOrWhiteSpace(identifier))
            {
                toast.Tag = identifier;
                toast.SuppressPopup = history.GetHistory().Any(GetToastComparer(toast));
            }

            DesktopNotificationManagerCompat.CreateToastNotifier().Show(toast);
        }

        private static Func<ToastNotification, bool> GetToastComparer(ToastNotification original)
        {
            return other =>
            {
                if (!Equals(other.Group, original.Group))
                {
                    return false;
                }
                if (!Equals(other.Tag, original.Tag))
                {
                    return false;
                }
                return true;
            };
        }

        NotificationService NotificationService.setup()
        {
            DesktopNotificationManagerCompat.RegisterAumidAndComServer<TActivator>(AumID);
            DesktopNotificationManagerCompat.RegisterActivator<TActivator>();
            history = DesktopNotificationManagerCompat.History;

            return this;
        }

        void NotificationService.unregister()
        {
            history.Clear();
        }
    }
}
