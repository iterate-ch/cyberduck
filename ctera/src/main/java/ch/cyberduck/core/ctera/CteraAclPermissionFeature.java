package ch.cyberduck.core.ctera;

import ch.cyberduck.core.Acl;
import ch.cyberduck.core.DefaultIOExceptionMappingService;
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.dav.DAVExceptionMappingService;
import ch.cyberduck.core.dav.DAVPathEncoder;
import ch.cyberduck.core.dav.DAVSession;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.shared.DefaultAclFeature;

import org.w3c.dom.Element;

import javax.xml.namespace.QName;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

import com.github.sardine.impl.SardineException;
import com.github.sardine.util.SardineUtil;

import static ch.cyberduck.core.ctera.CteraCustomACL.allCteraCustomACLRoles;
import static ch.cyberduck.core.ctera.CteraCustomACL.toQn;


public class CteraAclPermissionFeature extends DefaultAclFeature {

    private final DAVSession session;

    public CteraAclPermissionFeature(final DAVSession session) {
        this.session = session;
    }

    @Override
    public Acl getPermission(final Path file) throws BackgroundException {
        final PathAttributes attributes = new CteraAttributesFinderFeature(session).find(file, new DisabledListProgressListener());
        return attributes.getAcl();
    }

    @Override
    public void setPermission(final Path file, final Acl acl) throws BackgroundException {
        try {
            List<Element> setProps = this.toCustomProperties(acl);
            session.getClient().patch(new DAVPathEncoder().encode(file), setProps, Collections.emptyList(), Collections.emptyMap());
        }
        catch(SardineException e) {
            throw new DAVExceptionMappingService().map("Failure to write attributes of {0}", e, file);
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map(e, file);
        }
    }

    @Override
    public List<Acl.User> getAvailableAclUsers() {
        return Collections.singletonList(new Acl.CanonicalUser());
    }


    @Override
    public List<Acl.Role> getAvailableAclRoles(final List<Path> files) {
        return allCteraCustomACLRoles;
    }


    protected List<Element> toCustomProperties(final Acl acl) {
        final List<Element> props = new ArrayList<>();
        if(acl == null || acl.get(new Acl.CanonicalUser()) == null) {
            return props;
        }
        for(Acl.Role role : allCteraCustomACLRoles) {
            final QName prop = toQn(role);
            final Element element = SardineUtil.createElement(prop);
            element.setTextContent(Boolean.toString(acl.get(new Acl.CanonicalUser()).contains(role)));
            props.add(element);
        }
        return props;
    }

    @Override
    public Acl getDefault(final EnumSet<Path.Type> type) {
        return Acl.EMPTY;
    }

    @Override
    public Acl getDefault(final Path file, final Local local) throws BackgroundException {
        return Acl.EMPTY;
    }

}
