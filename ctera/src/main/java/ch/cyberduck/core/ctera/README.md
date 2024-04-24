# CTERA Custom XML fields to support NT-ACL and WORM data

## 2nd line of defense: preflight (Cyberduck)

| local      | Feature     | folder | file | CTERA required permissions                                                                                                                      | preflight |
|------------|-------------|--------|------|-------------------------------------------------------------------------------------------------------------------------------------------------|-----------|
| ls         | ListService | x      |      | `readpermission`                                                                                                                                | x         |
| read       | Read        |        | x    | `readpermission`                                                                                                                                | x         |                      
| write      | Write       |        | x    | `writepermission`                                                                                                                               | x         |
| mv         | Move        | x      |      | source:`deletepermission` AND target:`writepermission` (if directory exists, i.e. overwrite) AND target's parent: `createdirectoriespermission` | x         |
| mv         | Move        |        | x    | source:`deletepermission` AND target:`writepermission` (if file exists, i.e. overwrite) AND (future: target's parent: `createfilepermission`)   | x         |
| cp         | Copy        | x      |      | target:`writepermission` (if directory exists, i.e. overwrite) AND target's parent: `createdirectoriespermission`                               | x         |
| cp         | Copy        |        | x    | target:`writepermission` (if file exists, i.e. overwrite) AND (future: target's parent: `createfilepermission`)                                 | x         |
| touch      | Touch       |        | x    | (future: target's parent `createfilepermission`)                                                                                                | x         |
| mkdir      | Directory   | x      |      | `createdirectoriespermission`                                                                                                                   | x         |
| rm / rmdir | Delete      | x      | x    | `deletepermission`                                                                                                                              | x         |
| exec       | --          |        | x    | --                                                                                                                                              | --        |

N.B. no need to check `readpermission` upon mv/cp.

## 1st line of defense: filesystem (Mountain Duck)

### macOS NFS POSIX (=mode sync)

| folder | file | NFS (POSIX) | affected local operations                                    | implementation (`NfsFileSystemDelegate.getattr`)                                                                                                           |
|--------|------|-------------|--------------------------------------------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------|
|        | x    | `r`         | read                                                         | `r` <-- `Read.preflight` <-- `readpermission`                                                                                                              |
| x      |      | `rx`        | ls                                                           | `rx` <-- `Read.preflight` <-- `readpermission`                                                                                                             |                      
|        | x    | `w`         | write, rm, mv source file, mv target file (if exists)        | `w` <--  (`Write.preflight` OR `Delete.preflight`  <-- (`writepermission` OR `deletepermission`)                                                           |
| x      |      | `w`         | rmdir, mkdir, mv source folder, mv target folder (if exists) | `w` <--  (`Write.preflight` OR `Delete.preflight` OR `Directory.preflight`) <-- (`writepermission` OR `deletepermission` OR `createdirectoriespermission`) |

N.B. `x` on files is only set for POSIX backends, i.e. never for CTERA.

### macOS File Provider Capabilities (=mode integrated)

| folder | file | File Provider capabilities (`DefaultFileProviderItemConverter.toFileProviderItem`)         | affected local operations |
|--------|------|--------------------------------------------------------------------------------------------|---------------------------|
| x      |      | `NSFileProviderFileSystemUserReadable` <-- `ListService.preflight`                         | ls                        |
|        | x    | `NSFileProviderFileSystemUserReadable` <-- `Read.preflight`                                | read                      |
| x      |      | `NSFileProviderFileSystemUserWritable` <-- `Touch.preflight` <-- TRUE for CTERA            | mv, touch, mkdir          |
|        | x    | `NSFileProviderFileSystemUserWritable` <-- `Write.preflight`                               | write, mv                 |
| x      |      | `NSFileProviderFileSystemUserExecutable` <-- `ListService.preflight`                       | ls                        |
|        | x    | `NSFileProviderFileSystemUserExecutable` <-- `permission.isExecutable` <-- FALSE for CTERA | exec                      |

(§) with empty file/directory name

N.B. File Provider sets the `x` flag on all folders independent of `NSFileProviderFileSystemUserExecutable`.

#### Documentation

* https://developer.apple.com/documentation/fileprovider/nsfileproviderfilesystemflags
* https://developer.apple.com/documentation/fileprovider/nsfileprovideritemcapabilities

### Windows (Cloud Files API (=mode integrated) and CBFS (=mode sync))

| folder | file | access right        | affected local operations                                           | implementation (`WindowsAcl.Translate`)                 |
|--------|------|---------------------|---------------------------------------------------------------------|---------------------------------------------------------|
|        | x    | `Read`              | read, exec                                                          | `Read.preflight` <-- `readpermission`                   |                      
| x      |      | `ReadAndExecute`    | ls                                                                  | `ListService.preflight` <-- `readpermission`            |                      
| x      | x    | `Write`             | write, touch, mkdir, mv source file, mv target file (if exists)     | `Write.preflight` <-- `writepermission`                 |
| x      | x    | `Delete`            | rm, rmdir, mv source file/folder, mv target file/folder (if exists) | `Delete.preflight` <-- `deletepermission`               |
| x      |      | `CreateDirectories` | mkdir, mv target folder (if target folder does not exist)           | `Directory.preflight` <-- `createdirectoriespermission` |

N.B. `Write` on folders implies `CreateFiles` (=`WriteData` on files) and `CreateDirectories` (=`AppendData` on files).
N.B. `x` on files is only set for POSIX backends, i.e. never for CTERA.

#### Documentation

* https://learn.microsoft.com/en-us/dotnet/api/system.security.accesscontrol.filesystemrights?view=net-8.0
  