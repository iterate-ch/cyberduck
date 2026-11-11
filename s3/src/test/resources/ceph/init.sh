#!/bin/sh
# Single node Ceph cluster (mon, mgr, osd on a file, rgw) for tests. Replaces the unmaintained quay.io/ceph/demo image.
# Requires S3_ACCESS_KEY and S3_SECRET_KEY. Logs "ceph init done" once the S3 gateway accepts requests.
set -e
FSID=$(uuidgen)
mkdir -p /var/lib/ceph/mon/ceph-a /var/lib/ceph/mgr/ceph-x /var/lib/ceph/osd/ceph-0 /var/lib/ceph/radosgw/ceph-rgw /var/log/ceph /var/run/ceph
cat > /etc/ceph/ceph.conf <<CONF
[global]
fsid = $FSID
mon_initial_members = a
mon_host = 127.0.0.1
public_network = 127.0.0.0/8
auth_allow_insecure_global_id_reclaim = false
osd_pool_default_size = 1
osd_pool_default_min_size = 1
osd_pool_default_pg_num = 8
osd_pool_default_pgp_num = 8
mon_allow_pool_size_one = true
mon_warn_on_pool_no_redundancy = false
mon_max_pg_per_osd = 1000
osd_objectstore = bluestore
bluestore_block_size = 4294967296
bluestore_block_db_size = 0
bluestore_block_wal_size = 0
log_to_stderr = false
[client.rgw.localhost]
rgw_dns_name = localhost
rgw_frontends = beast port=8080
rgw_enable_apis = s3, admin
CONF
ceph-authtool --create-keyring /tmp/mon.keyring --gen-key -n mon. --cap mon 'allow *'
ceph-authtool --create-keyring /etc/ceph/ceph.client.admin.keyring --gen-key -n client.admin \
    --cap mon 'allow *' --cap osd 'allow *' --cap mds 'allow *' --cap mgr 'allow *'
ceph-authtool /tmp/mon.keyring --import-keyring /etc/ceph/ceph.client.admin.keyring
monmaptool --create --add a 127.0.0.1 --fsid "$FSID" /tmp/monmap
ceph-mon --mkfs -i a --monmap /tmp/monmap --keyring /tmp/mon.keyring
ceph-mon -i a --public-addr 127.0.0.1
ceph config set mon auth_allow_insecure_global_id_reclaim false
ceph config set global mon_max_pg_per_osd 1000
ceph config set global osd_pool_default_pg_autoscale_mode off
ceph config set global osd_pool_default_pg_num 8
ceph config set global osd_pool_default_pgp_num 8
# Manager
ceph auth get-or-create mgr.x mon 'allow profile mgr' osd 'allow *' mds 'allow *' -o /var/lib/ceph/mgr/ceph-x/keyring
ceph-mgr -i x
# Object storage daemon backed by a sparse file
OSD_UUID=$(uuidgen)
OSD_ID=$(ceph osd new "$OSD_UUID")
truncate -s 4G /var/lib/ceph/osd/ceph-0/block.img
ln -sf /var/lib/ceph/osd/ceph-0/block.img /var/lib/ceph/osd/ceph-0/block
ceph auth get-or-create "osd.$OSD_ID" mon 'allow profile osd' osd 'allow *' mgr 'allow profile osd' -o /var/lib/ceph/osd/ceph-0/keyring
ceph-osd -i "$OSD_ID" --mkfs --osd-uuid "$OSD_UUID" --osd-data /var/lib/ceph/osd/ceph-0 --keyring /var/lib/ceph/osd/ceph-0/keyring
ceph-osd -i "$OSD_ID" --osd-data /var/lib/ceph/osd/ceph-0
# Wait for the object storage daemon
for i in $(seq 1 60); do
    ceph osd stat | grep -q '1 up' && break
    sleep 2
done
# Gateway
ceph auth get-or-create client.rgw.localhost mon 'allow rw' osd 'allow rwx' mgr 'allow rw' -o /var/lib/ceph/radosgw/ceph-rgw/keyring
radosgw -n client.rgw.localhost -k /var/lib/ceph/radosgw/ceph-rgw/keyring
for i in $(seq 1 60); do
    radosgw-admin user list >/dev/null 2>&1 && break
    sleep 2
done
radosgw-admin user create --uid "$S3_ACCESS_KEY" --display-name "$S3_ACCESS_KEY" \
    --access-key "$S3_ACCESS_KEY" --secret-key "$S3_SECRET_KEY" >/dev/null
echo "ceph init done"
