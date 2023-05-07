#!/bin/bash
#
# Copyright (c) 2002-2023 iterate GmbH. All rights reserved.
# https://cyberduck.io/
#
# This program is free software; you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation, either version 3 of the License, or
# (at your option) any later version.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
#

set -e

: "${SMB_USER:=smbuser}"
: "${SMB_PASSWORD:=smbpassword}"
#
#for netdev in /sys/class/net/*; do
#  netdev=${netdev##*/}
#  if [[ "$netdev" != "lo" ]]; then
#    break
#  fi
#done
#subnet=$(ip addr show "$netdev" | sed -n 's/.*inet \([0-9\.]*\/[0-9]*\) .*/\1/p')
#ip_address=${subnet%%/*}

ip_address="127.0.0.1"

# Create DFS links
# - /public -> public share
# - /user -> user share
# - /firstfail-public -> first listed server fails, second -> public share
ln -s "msdfs:${ip_address}\\public" /opt/samba/dfs/public
ln -s "msdfs:${ip_address}\\user" /opt/samba/dfs/user
ln -s "msdfs:192.0.2.1\\notthere,${ip_address}\\public" /opt/samba/dfs/firstfail-public

exec "$@"
