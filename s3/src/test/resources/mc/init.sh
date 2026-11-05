#!/bin/sh
# Create buckets expected by tests. Executed in the mc container once the server is ready.
# Set BUCKET_REGION to create all buckets in a single region.
set -e
mc alias set local "$S3_ENDPOINT" "$S3_ACCESS_KEY" "$S3_SECRET_KEY"
create() {
    mc mb --ignore-existing --region "${BUCKET_REGION:-$2}" $3 "local/$1"
}
create test-eu-central-1-cyberduck eu-central-1
create versioning-test-eu-central-1-cyberduck eu-central-1 --with-versioning
create test-eu-central-1-objectlock eu-central-1 --with-lock
create test-us-east-1-cyberduck us-east-1
create sse-test-us-east-1-cyberduck us-east-1
create test-eu-west-1-cyberduck eu-west-1
create test-ap-southeast-2-cyberduck ap-southeast-2
create test-acl-us-east-1-cyberduck us-east-1
create test-lifecycle-us-east-1-cyberduck us-east-1
create test-eu-central-1-cyberduck-unsupportedprefix eu-central-1
create test-eu-central-1-cyberduck-ownerenforced eu-central-1
create test-eu-central-1-acl-disabled eu-central-1
echo "buckets init done"
