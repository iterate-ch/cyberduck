/*
 *  Copyright (c) 2003 Regents of The University of Michigan.
 *  Copyright (c) 2004 David Kocher. All rights reserved.
 *  http://cyberduck.ch/
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  Bug fixes, suggestions and comments should be sent to:
 *  dkocher@cyberduck.ch
 */

#include <syslog.h>
#include <errno.h>
#include <stdlib.h>
#include <string.h>

#include <Login.h>
#include <Security/SecBase.h>
#include <Security/SecKeychain.h>

char *getPwdFromKeychain(const char *service, const char *account, OSStatus *error);
void addPwdToKeychain(const char *service, const char *account, const char *password);

