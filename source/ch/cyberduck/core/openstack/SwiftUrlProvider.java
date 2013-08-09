package ch.cyberduck.core.openstack;

import ch.cyberduck.core.DescriptiveUrl;
import ch.cyberduck.core.DescriptiveUrlBag;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.UrlProvider;

import java.net.URI;
import java.text.MessageFormat;
import java.util.Locale;

import ch.iterate.openstack.swift.model.Region;

/**
 * @version $Id$
 */
public class SwiftUrlProvider implements UrlProvider {

    private SwiftSession session;

    private PathContainerService containerService
            = new PathContainerService();

    public SwiftUrlProvider(final SwiftSession session) {
        this.session = session;
    }

    @Override
    public DescriptiveUrlBag get(final Path file) {
        final DescriptiveUrlBag list = new DescriptiveUrlBag();
        final Region region = session.getRegion(containerService.getContainer(file));
        list.add(new DescriptiveUrl(
                URI.create(region.getStorageUrl(containerService.getContainer(file).getName(),
                        containerService.getKey(file)).toString()), DescriptiveUrl.Type.provider,
                MessageFormat.format(LocaleFactory.localizedString("{0} URL"),
                        session.getHost().getProtocol().getScheme().name().toUpperCase(Locale.ENGLISH))
        ));
        return list;
    }
}
