package ch.cyberduck.core.ctera;

import ch.cyberduck.core.Acl;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.Permission;
import ch.cyberduck.core.dav.DAVAttributesFinderFeature;
import ch.cyberduck.core.dav.DAVPathEncoder;
import ch.cyberduck.core.dav.DAVSession;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import com.github.sardine.DavResource;

import static ch.cyberduck.core.ctera.CteraAclPermissionFeature.*;

public class CteraAttributesFinderFeature extends DAVAttributesFinderFeature {
    private final DAVSession session;

    public CteraAttributesFinderFeature(final DAVSession session) {
        super(session);
        this.session = session;
    }

    protected List<DavResource> list(final Path file) throws IOException {
        return session.getClient().list(new DAVPathEncoder().encode(file), 0,
                new HashSet<>(allCteraCustomACLQn)
        );
    }


    @Override
    public PathAttributes toAttributes(final DavResource resource) {
        final Map<String, String> customProps = resource.getCustomProps();
        final Acl acl = customPropsToAcl(customProps);
        final Permission p = aclToPermission(acl);
        return super.toAttributes(resource)
                .withAcl(acl)
                .withPermission(p);
    }
}
