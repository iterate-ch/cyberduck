package ch.cyberduck.core.ctera;

import ch.cyberduck.core.Acl;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.dav.DAVAttributesFinderFeature;
import ch.cyberduck.core.dav.DAVPathEncoder;
import ch.cyberduck.core.dav.DAVSession;
import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.exception.BackgroundException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.xml.namespace.QName;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.github.sardine.DavResource;

public class CteraAttributesFinderFeature extends DAVAttributesFinderFeature {
    private static final Logger log = LogManager.getLogger(CteraAttributesFinderFeature.class);
    public static final String CTERA_GUID = "guid";
    public static final String CTERA_NAMESPACE_URI = "http://www.ctera.com/ns";
    public static final String CTERA_NAMESPACE_PREFIX = "ctera";
    /**
     * Read Data: Allows or denies viewing data in files.
     */
    public static final Acl.Role READPERMISSION = new Acl.Role("readpermission");
    /**
     * Write Data: Allows or denies making changes to a file and overwriting existing content.
     */
    public static final Acl.Role WRITEPERMISSION = new Acl.Role("writepermission");
    /**
     * Execute File: Allows or denies running program (executable) files.
     * Files only.
     */
    public static final Acl.Role EXECUTEPERMISSION = new Acl.Role("executepermission");
    /**
     * Allows or denies deleting the file or folder. If you don't have Delete permission on a file or folder,
     * you can still delete it if you have been granted Delete Subfolders and Files on the parent folder.
     */
    public static final Acl.Role DELETEPERMISSION = new Acl.Role("deletepermission");
    /**
     * Create Files: Allows or denies creating files within the folder.
     * Directories only.
     */
    public static final Acl.Role CREATEFILEPERMISSION = new Acl.Role("Createfilepermission");
    /**
     * Create Folders: Allows or denies creating subfolders within the folder.
     * Directories only.
     */
    public static final Acl.Role CREATEDIRECTORIESPERMISSION = new Acl.Role("CreateDirectoriespermission");
    static final List<Acl.Role> allCteraCustomACLRoles = Collections.unmodifiableList(Arrays.asList(
            READPERMISSION, WRITEPERMISSION, EXECUTEPERMISSION, DELETEPERMISSION, CREATEFILEPERMISSION, CREATEDIRECTORIESPERMISSION
    ));
    final static List<QName> allCteraCustomACLQn = Collections.unmodifiableList(allCteraCustomACLRoles.stream().map(CteraAttributesFinderFeature::toQn).collect(Collectors.toList()));

    private final DAVSession session;

    public CteraAttributesFinderFeature(final DAVSession session) {
        super(session);
        this.session = session;
    }

    public static QName toQn(final Acl.Role role) {
        return new QName(CTERA_NAMESPACE_URI, role.getName(), CTERA_NAMESPACE_PREFIX);
    }

    public static String toProp(final QName qn) {
        return qn.getLocalPart();
    }

    public static Acl.Role toRole(final QName qn) {
        return new Acl.Role(toProp(qn));
    }

    public static Acl customPropsToAcl(final Map<String, String> customProps) {
        boolean empty = true;
        final Acl acl = new Acl();
        acl.addAll(new Acl.CanonicalUser());
        for(final QName qn : allCteraCustomACLQn) {
            if(customProps.containsKey(toProp(qn))) {
                empty = false;
                final String val = customProps.get(toProp(qn));
                if(Boolean.parseBoolean(val)) {
                    acl.addAll(new Acl.CanonicalUser(), toRole(qn));
                }
            }
        }
        if(empty) {
            // ignore acl in preflight as none of the CTERA custom ACL roles is found
            return Acl.EMPTY;
        }
        return acl;
    }

    protected static void checkCteraRole(final Path file, final Acl.Role role) throws BackgroundException {
        final Acl acl = file.attributes().getAcl();
        if(acl == Acl.EMPTY) {
            return;
        }
        if(!acl.get(new Acl.CanonicalUser()).contains(role)) {
            final String msg = MessageFormat.format(LocaleFactory.localizedString("Cannot create {0}", "Error"), file.getName());
            if(log.isWarnEnabled()) {
                log.warn(msg);
            }
            throw new AccessDeniedException(msg);
        }
    }

    @Override
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
