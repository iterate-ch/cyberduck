package ch.cyberduck.core.ctera;

import ch.cyberduck.core.Acl;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.dav.DAVAttributesFinderFeature;
import ch.cyberduck.core.dav.DAVPathEncoder;
import ch.cyberduck.core.dav.DAVSession;

import javax.xml.namespace.QName;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import com.github.sardine.DavResource;

import static ch.cyberduck.core.ctera.CteraAclPermissionFeature.*;

public class CteraAttributesFinderFeature extends DAVAttributesFinderFeature {
    public static final String CTERA_GUID = "guid";
    private final DAVSession session;

    public CteraAttributesFinderFeature(final DAVSession session) {
        super(session);
        this.session = session;
    }

    protected List<DavResource> list(final Path file) throws IOException {
        final List<QName> l = new ArrayList<>();
        l.addAll(allCteraCustomACLQn);
        l.add(new QName(CTERA_NAMESPACE_URI, CTERA_GUID, CTERA_NAMESPACE_PREFIX));
        return session.getClient().list(new DAVPathEncoder().encode(file), 0,
                new HashSet<>(l)
        );
    }

    @Override
    public PathAttributes toAttributes(final DavResource resource) {
        final Map<String, String> customProps = resource.getCustomProps();
        final Acl acl = customPropsToAcl(customProps);
        final PathAttributes attributes = super.toAttributes(resource);
        if(customProps != null && customProps.containsKey(CTERA_GUID)) {
            attributes.setFileId(customProps.get(CTERA_GUID));
        }
        return attributes
                .withAcl(acl);
    }
}
