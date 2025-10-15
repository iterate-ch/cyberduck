#! /bin/bash -e

echo "Waiting for iRODS catalog database to be ready"
catalog_db_hostname=irods-catalog
until pg_isready -h ${catalog_db_hostname} -d ICAT -U irods -q; do
    sleep 1
done
echo "iRODS catalog database is ready"

unattended_install_file=/unattended_install.json
if [ -f "${unattended_install_file}" ]; then
    echo "Running iRODS setup"

    # Add generated hostname as a recognizable alias.
    sed -i "s/CONTAINER_HOSTNAME_ALIAS/${HOSTNAME}/g" ${unattended_install_file}
    python3 /var/lib/irods/scripts/setup_irods.py --json_configuration_file ${unattended_install_file}

    # Move the input file used to configure the server out of the way so
    # the container is restartable.
    mv ${unattended_install_file} ${unattended_install_file}.processed
fi

echo "Starting server"
su - irods -c 'irodsServer --stdout'
