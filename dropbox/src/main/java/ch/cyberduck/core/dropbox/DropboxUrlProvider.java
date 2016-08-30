package ch.cyberduck.core.dropbox;

import ch.cyberduck.core.DescriptiveUrl;
import ch.cyberduck.core.DescriptiveUrlBag;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Scheme;
import ch.cyberduck.core.UrlProvider;

import java.net.URI;
import java.text.MessageFormat;
import java.util.Locale;

import com.dropbox.core.DbxException;
import com.dropbox.core.v2.sharing.RequestedVisibility;
import com.dropbox.core.v2.sharing.SharedLinkMetadata;
import com.dropbox.core.v2.sharing.SharedLinkSettings;

public class DropboxUrlProvider implements UrlProvider {

    private final DropboxSession session;

    public DropboxUrlProvider(final DropboxSession dropboxSession) {
        session = dropboxSession;
    }

    @Override
    public DescriptiveUrlBag toUrl(final Path file) {
        final DescriptiveUrlBag list = new DescriptiveUrlBag();
        if(file.isFile()) {
            try {
                final SharedLinkMetadata shared = session.getClient().sharing().createSharedLinkWithSettings(file.getAbsolute(),
                        new SharedLinkSettings(RequestedVisibility.PUBLIC, null, null));
                list.add(new DescriptiveUrl(URI.create(shared.getUrl()), DescriptiveUrl.Type.http,
                        MessageFormat.format(LocaleFactory.localizedString("{0} URL"), Scheme.https.name().toUpperCase(Locale.ROOT))));
            }
            catch(DbxException e) {
                e.printStackTrace();
            }
        }
        return list;
    }
}
