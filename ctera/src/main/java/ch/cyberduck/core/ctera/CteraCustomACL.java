package ch.cyberduck.core.ctera;

import ch.cyberduck.core.Acl;
import ch.cyberduck.core.Permission;

import javax.xml.namespace.QName;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.github.sardine.util.SardineUtil;

public class CteraCustomACL {

    public static Acl.Role readpermission = new Acl.Role("readpermission");
    public static Acl.Role writepermission = new Acl.Role("writepermission");


    public static Acl.Role executepermission = new Acl.Role("executepermission"); // Files only
    public static Acl.Role deletepermission = new Acl.Role("deletepermission");


    public static Acl.Role traversepermission = new Acl.Role("traversepermission"); // Directories only


    public static Acl.Role Createfilepermission = new Acl.Role("Createfilepermission"); // Directories only


    public static Acl.Role CreateDirectoriespermission = new Acl.Role("CreateDirectoriespermission");// Directories only


    final static List<Acl.Role> allCteraCustomACLRoles = Collections.unmodifiableList(Arrays.asList(
            readpermission, writepermission, executepermission, deletepermission, traversepermission, Createfilepermission, CreateDirectoriespermission
    ));

    // TODO CTERA-136 support for ctera namespace?
    final static List<QName> allCteraCustomACLQn = Collections.unmodifiableList(allCteraCustomACLRoles.stream().map(CteraCustomACL::toQn).collect(Collectors.toList()));


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
        int perm = 0;
        if(acl.get(new Acl.CanonicalUser()).contains(readpermission)) {
            // r
            perm += 4;
        }
        if(acl.get(new Acl.CanonicalUser()).contains(writepermission)) {
            // w
            perm += 2;
        }
        if(acl.get(new Acl.CanonicalUser()).contains(executepermission) || acl.get(new Acl.CanonicalUser()).contains(traversepermission)) {
            // x
            perm += 1;
        }
        // TODO CTERA-136 only user - what about group/others?
        return new Permission(perm * 100);
    }


}
