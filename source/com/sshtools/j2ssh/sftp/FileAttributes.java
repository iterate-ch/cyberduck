package com.sshtools.j2ssh.sftp;

import com.sshtools.j2ssh.io.UnsignedInteger32;
import com.sshtools.j2ssh.io.UnsignedInteger64;
import com.sshtools.j2ssh.io.ByteArrayWriter;
import com.sshtools.j2ssh.io.ByteArrayReader;

import java.io.IOException;

import java.util.Map;
import java.util.List;
import java.util.Vector;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import java.util.Date;
import java.text.SimpleDateFormat;

public class FileAttributes {

  int version = 3;
  long flags = 0x0000000;               // Version 3 & 4
  int type;                             // Version 4 only
  UnsignedInteger64 size = null;        // Version 3 & 4
  UnsignedInteger32 uid = null;         // Version 3 only
  UnsignedInteger32 gid = null;          // Version 3 only
  String owner = null;                  // Version 4 only
  String group = null;                  // Version 4 only
  UnsignedInteger32 permissions = null; // Version 3 & 4
  UnsignedInteger32 atime = null;       // Version 3 & 4
  UnsignedInteger32 createtime = null;  // Version 4 only
  UnsignedInteger32 mtime = null;       // Version 3 & 4
  List acl = new Vector();              // Version 4 only
  Map extended = new HashMap();         // Version 3 & 4

  // Version 4 types
  public static final int SSH_FILEXFER_TYPE_REGULAR = 1;
  public static final int SSH_FILEXFER_TYPE_DIRECTORY = 2;
  public static final int SSH_FILEXFER_TYPE_SYMLINK = 3;
  public static final int SSH_FILEXFER_TYPE_SPECIAL = 4;
  public static final int SSH_FILEXFER_TYPE_UNKNOWN = 5;

  private static final int SSH_FILEXFER_ATTR_SIZE = 0x0000001;
  private static final int SSH_FILEXFER_ATTR_UIDGID = 0x00000002;
  private static final int SSH_FILEXFER_ATTR_PERMISSIONS = 0x0000004;
  // Also as ACMODTIME for version 3
  private static final int SSH_FILEXFER_ATTR_ACCESSTIME = 0x0000008;
  private static final int SSH_FILEXFER_ATTR_CREATETIME = 0x0000010;
  private static final int SSH_FILEXFER_ATTR_MODIFYTIME = 0x0000020;
  private static final int SSH_FILEXFER_ATTR_ACL = 0x0000040;
  private static final int SSH_FILEXFER_ATTR_OWNERGROUP = 0x0000080;
  private static final int SSH_FILEXFER_ATTR_EXTENDED = 0x8000000;

  // Posix stats
  public static final int S_IFMT = 0xF000;
  public static final int S_IFSOCK = 0xC000;
  public static final int S_IFLNK = 0xA000;
  public static final int S_IFREG = 0x8000;
  public static final int S_IFBLK = 0x6000;
  public static final int S_IFDIR = 0x4000;
  public static final int S_IFCHR = 0x2000;
  public static final int S_IFIFO = 0x1000;

  public final static int S_ISUID = 0x800;
  public final static int S_ISGID = 0x400;

  char[] types = { 'p', 'c', 'd', 'b', '-', 'l', 's', };

  public final static int S_IRUSR = 0x100;
  public final static int S_IWUSR = 0x80;
  public final static int S_IXUSR = 0x40;
  public final static int S_IRGRP = 0x20;
  public final static int S_IWGRP = 0x10;
  public final static int S_IXGRP = 0x08;
  public final static int S_IROTH = 0x04;
  public final static int S_IWOTH = 0x02;
  public final static int S_IXOTH = 0x01;

  public FileAttributes() {

  }

  /*
  public FileAttributes(int type, int version) {
    if(type >= 1 && type <= 5) {
      this.type = type;
      this.version = version;
    }
    else
      throw new IllegalArgumentException("The type must be a valid FileAttribute type");
  }
  */

  public FileAttributes(ByteArrayReader bar) throws IOException {

    flags = bar.readInt();

    /*
    type = bar.readInt();
    */

    if(isFlagSet(SSH_FILEXFER_ATTR_SIZE))
        size = bar.readUINT64();

    if(isFlagSet(SSH_FILEXFER_ATTR_UIDGID)) {
      uid = bar.readUINT32();
      gid = bar.readUINT32();
    }

    /*if(isFlagSet(SSH_FILEXFER_ATTR_OWNERGROUP)) {
        owner = bar.readString();
        group = bar.readString();
    }*/

    if(isFlagSet(SSH_FILEXFER_ATTR_PERMISSIONS))
      permissions = bar.readUINT32();


    if(isFlagSet(SSH_FILEXFER_ATTR_ACCESSTIME)) {
      atime = bar.readUINT32();
      mtime = bar.readUINT32();
    }

    /*
    if(isFlagSet(SSH_FILEXFER_ATTR_CREATETIME))
      createtime = bar.readUINT32();

    if(isFlagSet(SSH_FILEXFER_ATTR_MODIFYTIME))
      mtime = bar.readUINT32();
    */

    /*
    if(isFlagSet(SSH_FILEXFER_ATTR_ACL)) {
      int count = (int)bar.readInt();
      for(int i=0;i<count;i++) {
        acl.add(new ACL(bar));
      }
    }*/

    if(isFlagSet(SSH_FILEXFER_ATTR_EXTENDED)) {
      int count = (int)bar.readInt();
      String type;
      String data;
      for(int i=0;i<count;i++) {
        type = bar.readString();
        data = bar.readString();
        extended.put(type, data);
      }
    }
  }

  public UnsignedInteger32 getUID() {
    return uid;
  }

  public UnsignedInteger32 getGID() {
    return gid;
  }

  public void setSize(UnsignedInteger64 size) {
    this.size = size;
    // Set the flag
    if(size!=null)
      flags |= SSH_FILEXFER_ATTR_SIZE;
    else
      flags ^= SSH_FILEXFER_ATTR_SIZE;
  }

  public UnsignedInteger64 getSize() {
    return size;
  }

  /*
  public void setOwner(String owner) {
    this.owner = owner;
    // Set the flag
    if(group!=null || owner!=null)
      flags |= SSH_FILEXFER_ATTR_OWNERGROUP;
    else
      flags ^= SSH_FILEXFER_ATTR_OWNERGROUP;
  }

  public String getOwner() {
    return owner;
  }*/

  /*
  public void setGroup(String group) {
    this.group = group;
    // Set the flag
    if(group!=null || owner!=null)
      flags |= SSH_FILEXFER_ATTR_OWNERGROUP;
    else
      flags ^= SSH_FILEXFER_ATTR_OWNERGROUP;
  }

  public String getGroup() {
    return group;
  }*/

  public void setPermissions(UnsignedInteger32 permissions) {
    this.permissions = permissions;
    // Set the flag
    if(permissions!=null)
      flags |= SSH_FILEXFER_ATTR_PERMISSIONS;
    else
      flags ^= SSH_FILEXFER_ATTR_PERMISSIONS;
  }

  public UnsignedInteger32 getPermissions() {
    return permissions;
  }

  public void setTimes(UnsignedInteger32 atime, UnsignedInteger32 mtime) {
    this.atime = atime;
    this.mtime = mtime;

    // Set the flag
    if(atime!=null)
      flags |= SSH_FILEXFER_ATTR_ACCESSTIME;
    else
      flags ^= SSH_FILEXFER_ATTR_ACCESSTIME;
  }

  public UnsignedInteger32 getAccessedTime() {
    return atime;
  }

  public UnsignedInteger32 getModifiedTime() {
    return mtime;
  }
  /*
  public void setCreatedTime(UnsignedInteger32 atime) {
    this.createtime = createtime;
    // Set the flag
    if(createtime!=null)
      flags |= SSH_FILEXFER_ATTR_CREATETIME;
    else
      flags ^= SSH_FILEXFER_ATTR_CREATETIME;
  }

  public UnsignedInteger32 getCreatedTime() {
    return createtime;
  }

  public void setModifiedTime(UnsignedInteger32 atime) {
    this.mtime = mtime;
    // Set the flag
    if(mtime!=null)
      flags |= SSH_FILEXFER_ATTR_MODIFYTIME;
    else
      flags ^= SSH_FILEXFER_ATTR_MODIFYTIME;
  }

  public UnsignedInteger32 getModifiedTime() {
    return mtime;
  }*/

  /*
  public List getACL() {
    return acl;
  }*/

  public Map getExtendedAttributes() {
    return extended;
  }

  public boolean isFlagSet(int flag) {
    return ((flags & flag) == flag);
  }

  public byte[] toByteArray() throws IOException {

    ByteArrayWriter baw = new ByteArrayWriter();

    if(extended.size() > 0)
      flags |= SSH_FILEXFER_ATTR_EXTENDED;
    /*
    if(acl.size() > 0)
      flags |= SSH_FILEXFER_ATTR_ACL;
    */
    baw.writeInt(flags);
    /*
    baw.write(type);
    */
    if(isFlagSet(SSH_FILEXFER_ATTR_SIZE))
       baw.writeUINT64(size);

    if(isFlagSet(SSH_FILEXFER_ATTR_UIDGID)) {
      baw.writeUINT32(uid);
      baw.writeUINT32(gid);
    }

    /*
    if(isFlagSet(SSH_FILEXFER_ATTR_OWNERGROUP)) {
       baw.writeString(owner);
       baw.writeString(group);
    }
    */
    if(isFlagSet(SSH_FILEXFER_ATTR_PERMISSIONS))
      baw.writeUINT32(permissions);

    if(isFlagSet(SSH_FILEXFER_ATTR_ACCESSTIME)) {
      baw.writeUINT32(atime);
      baw.writeUINT32(mtime);
    }

    /*
    if(isFlagSet(SSH_FILEXFER_ATTR_CREATETIME))
      baw.writeUINT32(createtime);

    if(isFlagSet(SSH_FILEXFER_ATTR_MODIFYTIME))
      baw.writeUINT32(mtime);

    if(isFlagSet(SSH_FILEXFER_ATTR_ACL)) {
      ByteArrayWriter acls = new ByteArrayWriter();
      acls.writeInt(acl.size());
      Iterator it = acl.iterator();
      while(it.hasNext()) {
        acls.write(((ACL)it.next()).toByteArray());
      }
      baw.writeBinaryString(acls.toByteArray());
      acls = null;
    }*/

    if(isFlagSet(SSH_FILEXFER_ATTR_EXTENDED)) {
      baw.writeInt(extended.size());
      Iterator it = extended.entrySet().iterator();
      Set set;
      while(it.hasNext()) {
        Map.Entry entry = (Map.Entry)it.next();
        baw.writeString((String)entry.getKey());
        baw.writeString((String)entry.getValue());
      }
    }

    return baw.toByteArray();

  }


  private String rwxString(int v, int r) {
      v >>>= r;
      String rwx = ((((v & 0x04) != 0) ? "r" : "-") +
                    (((v & 0x02) != 0) ? "w" : "-"));
      if((r == 6 && ((permissions.intValue() & S_ISUID) == S_ISUID)) ||
         (r == 3 && ((permissions.intValue() & S_ISGID) == S_ISGID))) {
          rwx += (((v & 0x01) != 0) ? "s" : "S");
      } else {
          rwx += (((v & 0x01) != 0) ? "x" : "-");
      }
      return rwx;
  }

  public String getPermissionsString() {
      StringBuffer str = new StringBuffer();
      str.append(types[(permissions.intValue()  & S_IFMT) >>> 13]);
      str.append(rwxString(permissions.intValue(), 6));
      str.append(rwxString(permissions.intValue(), 3));
      str.append(rwxString(permissions.intValue(), 0));
      return str.toString();
  }

  public String getModTimeString() {
      SimpleDateFormat df;
      long mt  = (mtime.longValue() * 1000L);
      long now = System.currentTimeMillis();
      if((now - mt) > (6 * 30 * 24 * 60 * 60 * 1000L)) {
          df = new SimpleDateFormat("MMM dd  yyyy");
      } else {
          df = new SimpleDateFormat("MMM dd hh:mm");
      }
      return df.format(new Date(mt));
  }

}
