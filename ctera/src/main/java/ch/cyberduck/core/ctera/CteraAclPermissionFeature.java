package ch.cyberduck.core.ctera;

import ch.cyberduck.core.Acl;
import ch.cyberduck.core.DefaultIOExceptionMappingService;
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.Permission;
import ch.cyberduck.core.dav.DAVExceptionMappingService;
import ch.cyberduck.core.dav.DAVPathEncoder;
import ch.cyberduck.core.dav.DAVSession;
import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.AclPermission;

import org.w3c.dom.Element;

import javax.xml.namespace.QName;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.github.sardine.impl.SardineException;
import com.github.sardine.util.SardineUtil;


public class CteraAclPermissionFeature implements AclPermission {

    /**
     * Read Data: Allows or denies viewing data in files.
     */
    // TODO CTERA-136 files only or does this control listing folder contents on directories (https://www.ntfs.com/ntfs-permissions-file-advanced.htm).
    public static final Acl.Role READPERMISSION = new Acl.Role("readpermission");

    /**
     * Write Data: Allows or denies making changes to a file and overwriting existing content.
     */
    // TODO CTERA-136 files only or what is the interplay with Createfilepermission/CreateDirectoriespermission?
    public static final Acl.Role WRITEPERMISSION = new Acl.Role("writepermission");

    /**
     * Execute File: Allows or denies running program (executable) files.
     */
    public static final Acl.Role EXECUTEPERMISSION = new Acl.Role("executepermission"); // Files only

    /**
     * Allows or denies deleting the file or folder. If you don't have Delete permission on a file or folder,
     * you can still delete it if you have been granted Delete Subfolders and Files on the parent folder.
     */
    public static final Acl.Role DELETEPERMISSION = new Acl.Role("deletepermission");

    /**
     * Traverse Folder: Allows or denies moving through a restricted folder to reach files and folders
     * beneath the restricted folder in the folder hierarchy. Traverse folder takes effect only when the group or user
     * is not granted the "Bypass traverse checking user" right in the Group Policy snap-in.
     * This permission does not automatically allow running program files.
     */
    public static final Acl.Role TRAVERSEPERMISSION = new Acl.Role("traversepermission"); // Directories only

    /**
     * Create Files: Allows or denies creating files within the folder.
     */
    public static final Acl.Role CREATEFILEPERMISSION = new Acl.Role("Createfilepermission"); // Directories only

    /**
     * Create Folders: Allows or denies creating subfolders within the folder.
     */
    public static final Acl.Role CREATEDIRECTORIESPERMISSION = new Acl.Role("CreateDirectoriespermission");// Directories only

    static final List<Acl.Role> allCteraCustomACLRoles = Collections.unmodifiableList(Arrays.asList(
            READPERMISSION, WRITEPERMISSION, EXECUTEPERMISSION, DELETEPERMISSION, TRAVERSEPERMISSION, CREATEFILEPERMISSION, CREATEDIRECTORIESPERMISSION
    ));

    // TODO CTERA-136 support for ctera namespace?
    final static List<QName> allCteraCustomACLQn = Collections.unmodifiableList(allCteraCustomACLRoles.stream().map(CteraAclPermissionFeature::toQn).collect(Collectors.toList()));

    public static QName toQn(final Acl.Role role) {
        return SardineUtil.createQNameWithCustomNamespace(role.getName());
    }

    public static String toProp(final QName qn) {
        return qn.getLocalPart();
    }

    public static Acl.Role toRole(final QName qn) {
        return new Acl.Role(toProp(qn));
    }

    public static Acl customPropsToAcl(final Map<String, String> customProps) {
        if(customProps.isEmpty()) {
            return Acl.EMPTY;
        }
        final Acl acl = new Acl();
        acl.addAll(new Acl.CanonicalUser());
        for(QName qn : allCteraCustomACLQn) {
            if(customProps.containsKey(toProp(qn))) {
                final String val = customProps.get(toProp(qn));
                if(Boolean.parseBoolean(val)) {
                    acl.addAll(new Acl.CanonicalUser(), toRole(qn));
                }
            }
        }
        return acl;
    }

    public static Permission aclToPermission(final Acl acl) {
        if(Acl.EMPTY.equals(acl)) {
            return new Permission(700);
        }
        Permission.Action action = Permission.Action.none;
        if(acl.get(new Acl.CanonicalUser()).contains(READPERMISSION)) {
            action = action.or(Permission.Action.read);
        }
        if(acl.get(new Acl.CanonicalUser()).contains(WRITEPERMISSION)) {
            action = action.or(Permission.Action.write);
        }
        if(acl.get(new Acl.CanonicalUser()).contains(EXECUTEPERMISSION) || acl.get(new Acl.CanonicalUser()).contains(TRAVERSEPERMISSION)) {
            action = action.or(Permission.Action.execute);
        }
        // TODO CTERA-136 only user - what about group/others?
        return new Permission(action, Permission.Action.none, Permission.Action.none);
    }

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

    protected static void checkCteraRole(final Path file, final Acl.Role role) throws BackgroundException {
        final Acl acl = file.attributes().getAcl();
        if(acl.equals(Acl.EMPTY)) {
            return;
        }
        if(!acl.get(new Acl.CanonicalUser()).contains(role)) {
            throw new AccessDeniedException(
                    String.format("Access to %s failed, requires %s, found %s", file.getAbsolute(), role, acl)
            ).withFile(file);
        }
    }
}
