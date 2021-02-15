package ch.cyberduck.core.onedrive.features.sharepoint;

import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.DescriptiveUrl;
import ch.cyberduck.core.Filter;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.onedrive.AbstractListService;
import ch.cyberduck.core.onedrive.AbstractSharepointSession;

import org.nuxeo.onedrive.client.Sites;
import org.nuxeo.onedrive.client.types.Site;

import java.net.URI;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

public class SitesListService extends AbstractListService<Site.Metadata> {
    private final AbstractSharepointSession session;

    public SitesListService(final AbstractSharepointSession session) {
        this.session = session;
    }

    @Override
    protected Iterator<Site.Metadata> getIterator(final Path directory) throws BackgroundException {
        if(!session.isSingleSite() && directory.getParent().isRoot()) {
            return Sites.getSites(session.getClient(), "*");
        }
        return Sites.getSites(session.getSite(directory.getParent()));
    }

    @Override
    protected boolean isFiltering(final Path directory) {
        return !session.isSingleSite() && directory.getParent().isRoot();
    }

    @Override
    protected boolean filter(final Site.Metadata metadata) {
        return null != metadata.getRoot();
    }

    @Override
    protected Path toPath(final Site.Metadata metadata, final Path directory) {
        final PathAttributes attributes = new PathAttributes();
        attributes.setFileId(metadata.getId());
        attributes.setDisplayname(metadata.getDisplayName());
        attributes.setLink(new DescriptiveUrl(URI.create(metadata.webUrl)));

        return new Path(directory, metadata.getName(),
            EnumSet.of(Path.Type.volume, Path.Type.directory, Path.Type.placeholder), attributes);
    }

    @Override
    protected void postList(final AttributedList<Path> list) {
        final Map<String, Set<Integer>> duplicates = new HashMap<>();
        for(int i = 0; i < list.size(); i++) {
            final Path file = list.get(i);
            final AttributedList<Path> result = list.filter(new Filter<Path>() {
                @Override
                public boolean accept(Path test) {
                    return file != test && file.getName().equals(test.getName());
                }

                @Override
                public Pattern toPattern() {
                    return null;
                }
            });
            if(result.size() > 0) {
                final Set<Integer> set = duplicates.getOrDefault(file.getName(), new HashSet<>());
                set.add(i);
                duplicates.put(file.getName(), set);
            }
        }

        for(Set<Integer> set : duplicates.values()) {
            for(Integer i : set) {
                final Path file = list.get(i);

                final URI webLink = URI.create(file.attributes().getLink().getUrl());
                final String[] path = webLink.getPath().split(String.valueOf(Path.DELIMITER));
                final String suffix = path[path.length - 2];

                final Path rename = new Path(file.getParent(), String.format("%s (%s)", file.getName(), suffix), file.getType(), file.attributes());

                list.set(i, rename);
            }
        }
    }
}
