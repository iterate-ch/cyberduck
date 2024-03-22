package ch.cyberduck.core.ctera;

import ch.cyberduck.core.Acl;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.Permission;
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
import com.github.sardine.util.SardineUtil;

import static ch.cyberduck.core.ctera.CteraAclPermissionFeature.*;

public class CteraAttributesFinderFeature extends DAVAttributesFinderFeature {
    private final DAVSession session;

    public CteraAttributesFinderFeature(final DAVSession session) {
        super(session);
        this.session = session;
    }

    protected List<DavResource> list(final Path file) throws IOException {
        final List<QName> l = new ArrayList<>();
        l.addAll(allCteraCustomACLQn);
        // CTERA-137
        l.add(SardineUtil.createQNameWithCustomNamespace("guid"));
        return session.getClient().list(new DAVPathEncoder().encode(file), 0,
                new HashSet<>(l)
        );
    }


    @Override
    public PathAttributes toAttributes(final DavResource resource) {
        final Map<String, String> customProps = resource.getCustomProps();
        final Acl acl = customPropsToAcl(customProps);
        final Permission p = aclToPermission(acl);
        final PathAttributes attributes = super.toAttributes(resource);
        // CTERA-137
        if(customProps != null && customProps.containsKey("guid")) {
            attributes.withVersionId(customProps.get("guid"));
        }
        return attributes
                .withAcl(acl)
                .withPermission(p);
    }
}
