using ch.cyberduck.core;
using ch.cyberduck.core.local;
using ch.cyberduck.core.profiles;
using ReactiveUI;
using ReactiveUI.Fody.Helpers;
using System.Linq;
using System.Reactive;

namespace Ch.Cyberduck.Core.Refresh.ViewModels.Preferences.Pages
{
    public class ProfileViewModel : ReactiveObject
    {
        public ProfileViewModel(ProfileDescription profile)
        {
            ProfileDescription = profile;
            Profile = (Profile)profile.getProfile().get();
            Installed = profile.isInstalled() && Profile.isEnabled();
            IsEnabled = !(Profile.isBundled() || Utils.ConvertFromJavaList<Host>(BookmarkCollection.defaultCollection()).Any(x => x.getProtocol().Equals(Profile)));

            OpenHelp = ReactiveCommand.Create(() =>
            {
                BrowserLauncherFactory.get().open(ProviderHelpServiceFactory.get().help(Profile));
            });
        }

        public bool IsEnabled { get; }

        public string DefaultHostName => Profile.getDefaultHostname();

        public string Description => Profile.getDescription();

        [Reactive]
        public bool Installed { get; set; }

        public string Name => Profile.getName();

        public ReactiveCommand<Unit, Unit> OpenHelp { get; }

        public Profile Profile { get; }

        public ProfileDescription ProfileDescription { get; }
    }
}
