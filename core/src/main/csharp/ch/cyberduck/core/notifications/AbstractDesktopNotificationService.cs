//
// Copyright (c) 2002-2022 iterate GmbH. All rights reserved.
// http://cyberduck.io/
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU General Public License for more details.
//
// Bug fixes, suggestions and comments should be sent to:
// feedback@cyberduck.io
//

using ch.cyberduck.core.notification;
using Microsoft.Toolkit.Uwp.Notifications;
using System;
using System.Collections.Generic;
using System.Linq;
using Windows.Data.Xml.Dom;
using Windows.UI.Notifications;

namespace Ch.Cyberduck.Core.Notifications
{
    public abstract class AbstractDesktopNotificationService : NotificationService
    {
        private readonly NotificationFilterService notificationFilter;

        private ToastNotificationHistory history;

        private Action<string> listeners;

        public AbstractDesktopNotificationService()
        {
            notificationFilter = NotificationFilterService.Factory.get();
        }

        protected abstract string AumID { get; }

        public void addListener(NotificationService.Listener listener)
        {
            listeners += listener.callback;
        }

        public void notify(string group, string identifier, string title, string description, string action)
        {
            var toastContent = GetToast(title, description);

            toastContent.Actions = new ToastActionsCustom()
            {
                Buttons =
                {
                    new ToastButton(action, string.Empty)
                }
            };

            Toast(toastContent, identifier, true);
        }

        public void notify(string group, string identifier, string title, string description)
        {
            var toastContent = GetToast(title, description);

            Toast(toastContent, identifier, false);
        }

        NotificationService NotificationService.setup()
        {
            history = ToastNotificationManager.History;

            return this;
        }

        void NotificationService.unregister()
        {
            if (Utils.IsRunningAsUWP)
            {
                history.Clear();
            }
            else
            {
                history.Clear(AumID);
            }
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

        private ToastContent GetToast(string title, string description)
        {
            return new ToastContent
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
        }

        private bool ShouldSuppressPopup(ToastNotification toast)
        {
            IReadOnlyList<ToastNotification> toasts;
            if (Utils.IsRunningAsUWP)
            {
                toasts = history.GetHistory();
            }
            else
            {
                toasts = history.GetHistory(AumID);
            }

            if (toasts.Any(GetToastComparer(toast)))
            {
                return true;
            }

            return notificationFilter.shouldSuppress();
        }

        private void Toast(ToastContent toastContent, string identifier, bool handleActivated)
        {
            var doc = new XmlDocument();
            doc.LoadXml(toastContent.GetContent());

            var toast = new ToastNotification(doc);
            if (!string.IsNullOrWhiteSpace(identifier))
            {
                toast.Tag = identifier.GetHashCode().ToString("X");

                toast.Data = new NotificationData(new Dictionary<string, string>()
                {
                    ["identifier"] = identifier
                });

                toast.SuppressPopup = ShouldSuppressPopup(toast);
            }

            if (handleActivated)
            {
                toast.Activated += Toast_Activated;
            }

            ToastNotifier notifier;
            if (Utils.IsRunningAsUWP)
            {
                notifier = ToastNotificationManager.CreateToastNotifier();
            }
            else
            {
                notifier = ToastNotificationManager.CreateToastNotifier(AumID);
            }

            notifier.Show(toast);
        }

        private void Toast_Activated(ToastNotification sender, object args)
        {
            string tag = string.Empty;
            if (sender.Data?.Values.TryGetValue("identifier", out tag) != true)
            {
                tag = sender.Tag;
            }
            listeners(tag);
        }
    }
}
