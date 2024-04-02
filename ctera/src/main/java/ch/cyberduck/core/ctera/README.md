# CTERA Custom XML fields to support NT-ACL and WORM data

## 2nd line of defense: preflight (Cyberduck)

| local      | Feature     | folder | file | CTERA required permissions                                                                                                                      | preflight |
|------------|-------------|--------|------|-------------------------------------------------------------------------------------------------------------------------------------------------|-----------|
| ls         | ListService | x      |      | `readpermission`                                                                                                                                | --        |
| read       | Read        |        | x    | `readpermission`                                                                                                                                | x         |                      
| write      | Write       |        | x    | `writepermission`                                                                                                                               | x         |
| mv         | Move        | x      |      | source:`deletepermission` AND target:`writepermission` (if directory exists, i.e. overwrite) AND target's parent: `createdirectoriespermission` | x         |
| mv         | Move        |        | x    | source:`deletepermission` AND target:`writepermission` (if file exists, i.e. overwrite) AND (future: target's parent: `createfilepermission`)   | x         |
| cp         | Copy        | x      |      | target:`writepermission` (if directory exists, i.e. overwrite) AND target's parent: `createdirectoriespermission`                               | x         |
| cp         | Copy        |        | x    | target:`writepermission` (if file exists, i.e. overwrite) AND (future: target's parent: `createfilepermission`)                                 | x         |
| touch      | Touch       |        | x    | (future: target's parent `createfilepermission`)                                                                                                | x         |
| mkdir      | Directory   | x      |      | `createdirectoriespermission`                                                                                                                   | x         |
| rm / rmdir | Delete      | x      | x    | `deletepermission`                                                                                                                              | x         |
| exec       | --          |        | x    | `executepermission` on file                                                                                                                     | --        |

N.B. no need to check `readpermission` upon mv/cp.

## 1st line of defense: filesystem (Mountain Duck)

### macOS NFS POSIX

| folder | file | NFS (POSIX) | affected local operations                                    | implementation (`NfsFileSystemDelegate.getattr`)                                                                                                           |
|--------|------|-------------|--------------------------------------------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------|
|        | x    | `r`         | read                                                         | `r` <-- `Read.preflight` <-- `readpermission`                                                                                                              |
|        | x    | `x`         | exec                                                         | `x` <-- TRUE                                                                                                                                               |
| x      |      | `rx`        | ls                                                           | `rx` <-- `Read.preflight` <-- `readpermission`                                                                                                             |                      
|        | x    | `w`         | write, rm, mv source file, mv target file (if exists)        | `w` <--  (`Write.preflight` OR `Delete.preflight`  <-- (`writepermission` OR `deletepermission`)                                                           |
| x      |      | `w`         | rmdir, mkdir, mv source folder, mv target folder (if exists) | `w` <--  (`Write.preflight` OR `Delete.preflight` OR `Directory.preflight`) <-- (`writepermission` OR `deletepermission` OR `createdirectoriespermission`) |

N.B. we use `Read` feature for `readpermission` on directories, as well.

### macOS File Provider Capabilities

| folder | file | File Provider capabilities (`DefaultFileProviderItemConverter.toFileProviderItem`)                                                                                                            | affected local operations |
|--------|------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|---------------------------|
| x      | x    | `NSFileProviderFileSystemUserReadable` <-- TRUE                                                                                                                                               | read, ls                  |
| x      | x    | `NSFileProviderFileSystemUserWritable` <-- TRUE                                                                                                                                               | write, mv, touch, mkdir   |
| x      | x    | `NSFileProviderFileSystemUserExecutable` <-- TRUE                                                                                                                                             | exec                      |
| x      |      | `NSFileProviderItemCapabilitiesAllowsContentEnumerating` <-- `Read.preflight` <-- `readpermission`                                                                                            | ls                        |
|        | x    | `NSFileProviderItemCapabilitiesAllowsReading` <-- `Read.preflight` <-- `readpermission`                                                                                                       | read                      |                      
|        | x    | `NSFileProviderItemCapabilitiesAllowsWriting` <--  `Write.preflight`  <-- `writepermission`                                                                                                   | write                     |
| x      |      | `NSFileProviderItemCapabilitiesAllowsAddingSubItems` <-- (`Touch.preflight` (§) OR `Directory.preflight` (§)) <-- (`createdirectoriespermission` OR (future: `createfilepermission`)) == TRUE | mv, touch, mkdir          |
| x      | x    | `NSFileProviderItemCapabilitiesAllowsDeleting`  <-- `Delete.preflight` <-- `deletepermission`                                                                                                 | rm, rmdir, mv             |

(§) with random file/directory name

