package ch.cyberduck.core.onedrive.features.sharepoint;

import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.DescriptiveUrl;
import ch.cyberduck.core.NullFilter;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.onedrive.AbstractListService;
import ch.cyberduck.core.onedrive.AbstractSharepointSession;
import ch.cyberduck.core.onedrive.features.GraphFileIdProvider;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.nuxeo.onedrive.client.ODataQuery;
import org.nuxeo.onedrive.client.Sites;
import org.nuxeo.onedrive.client.types.BaseItem;
import org.nuxeo.onedrive.client.types.SharePointIds;
import org.nuxeo.onedrive.client.types.Site;

import java.net.URI;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class SitesListService extends AbstractListService<Site.Metadata> {
    private static final Logger log = LogManager.getLogger(SitesListService.class);

    private final AbstractSharepointSession session;

    public SitesListService(final AbstractSharepointSession session, final GraphFileIdProvider fileid) {
        super(fileid);
        this.session = session;
    }

    @Override
    protected Iterator<Site.Metadata> getIterator(final Path directory) throws BackgroundException {
        if(log.isDebugEnabled()) {
            log.debug("Return sites for {}", directory);
        }
        final ODataQuery query = new ODataQuery().select(
                BaseItem.Property.Id,
                BaseItem.Property.Name,
                BaseItem.Property.WebUrl,
                Site.Property.DisplayName,
                Site.Property.Root,
                Site.Property.SharepointIds);
        if(!session.isSingleSite() && directory.getParent().isRoot()) {
            // .search() uses OData $search, which doesn't support '*'.
            // But GET sites has query-parameter "search" which does support '*'.
            // (┛ಠ_ಠ)┛彡┻━┻
            return Sites.getSites(session.getClient(), query.set("search", "*"));
        }
        return Sites.getSites(session.getSite(directory.getParent()), query);
    }

    @Override
    protected boolean isFiltering(final Path directory) {
        return !session.isSingleSite() && directory.getParent().isRoot();
    }

    @Override
    protected boolean filter(final Site.Metadata metadata) {
        if(metadata.getRoot() == null) {
            return false;
        }

        final SharePointIds ids = metadata.getSharepointIds();
        if(ids != null) {
            if(isInvalid(ids.getSiteId())) {
                return false;
            }
            if(isInvalid(ids.getWebId())) {
                return false;
            }
        }
        else {
            // fallback for not retrieving sharepoint ids.
            final String[] split = StringUtils.split(metadata.getId(), ',');
            if(split.length != 3) {
                // Sharepoint IDs _must_ be tenant-url,siteId,webId
                return false;
            }
            if(isInvalid(split[1])) {
                return false;
            }
            if(isInvalid(split[2])) {
                return false;
            }
        }

        return true;
    }

    private static boolean isInvalid(final String input) {
        if(input == null || input.length() == 0) {
            // fast fallback, empty strings
            return false;
        }
        try {
            final UUID uuid = UUID.fromString(input);
            if(SharepointID.Invalid.uuid.equals(uuid)) {
                return true;
            }
            return false;
        }
        catch(IllegalArgumentException illegalArgumentException) {
            // invalid UUID, possibly bad.
            return true;
        }
    }

    @Override
    protected Path toPath(final Site.Metadata metadata, final Path directory) {
        final PathAttributes attributes = new PathAttributes();
        attributes.setFileId(metadata.getId());
        attributes.setDisplayname(metadata.getDisplayName());
        attributes.setLink(new DescriptiveUrl(URI.create(metadata.getWebUrl())));

        return new Path(directory, metadata.getName(),
            EnumSet.of(Path.Type.volume, Path.Type.directory, Path.Type.placeholder), attributes);
    }

    @Override
    protected void postList(final AttributedList<Path> list) {
        final Map<String, Set<Integer>> duplicates = new HashMap<>();
        for(int i = 0; i < list.size(); i++) {
            final Path file = list.get(i);
            final AttributedList<Path> result = list.filter(new NullFilter<Path>() {
                @Override
                public boolean accept(Path test) {
                    return file != test && file.getName().equals(test.getName());
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

    enum SharepointID {
        Invalid(0, 0);

        private final UUID uuid;

        SharepointID(long mostSigBits, long leastSigBits) {
            uuid = new UUID(mostSigBits, leastSigBits);
        }

        UUID getUuid() {
            return this.uuid;
        }
    }
}
