/*
 * See COPYING for license information.
 */ 

package com.mosso.client.cloudfiles;

import org.apache.commons.httpclient.HttpException;
import org.apache.log4j.Logger;

import java.util.List;
import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;

public class FilesContainer
{
    private String name;
    private List<FilesObject> objects = null;
    private FilesClient client = null;
    private static Logger logger = Logger.getLogger(FilesContainer.class);

    /**
     * Create a new container (Note, this does not actually create a container on the server)
     *  
     * @param name    The name of the container
     * @param objs    The objects in that container
     * @param client  The client we are currently using
     */
    public FilesContainer(String name, List<FilesObject> objs, FilesClient client)
    {
        this.name = name;
        this.objects = objs;
        this.client = client;
    }

    /**
     * @param name The name of the container
     * @param client A logged in client
     */
    public FilesContainer(String name, FilesClient client)
    {
        this.name = name;
        this.client = client;
    }

    /**
     * Get the name of the container
     * 
     * @return The name of this container
     */
    public String getName()
    {
        return name;
    }

    /**
     * Set the name of the container
     * 
     * @param name The new name
     */
    public void setName(String name)
    {
        this.name = name;
    }

    /**
     * Returns the contents of this container
     * 
     * @return A list of the contents
     * @throws HttpException There was a problem communicating with the server
     * @throws IOException There was a problem communicating with the server
     */
    public List<FilesObject> getObjects() throws HttpException, IOException
    {
        if (client != null)
        {
            return client.listObjects(this.name);            
        }
        else
        {
            logger.fatal("This Container has no FilesClient defined !");
        }
        return null;
    }

    /**
     * Get useful information on this container
     * 
     * @return The container info
     * @throws HttpException There was a problem communicating with the server
     * @throws IOException There was a problem communicating with the server
     */
    public FilesContainerInfo getInfo() throws HttpException, IOException
    {
        if (client != null)
        {
            return client.getContainerInfo(this.name);
        }
        else
        {
            logger.fatal("This container does not have a valid client !");
        }
        return null;
    }

    /**
     * Returns the instance of the client we're using
     * 
     * @return The FilesClient
     */
    public FilesClient getClient()
    {
        return this.client;                
    }

    /**
     * Adds a new object to the container
     * 
     * @param f    The file for this object
     * @param mimeType It's MIME type
     * @return The return code from the server
     * @throws NoSuchAlgorithmException  The MD5 implementation is not installed in the client
     * @throws IOException
     */
    public boolean addObject (File f, String mimeType) throws NoSuchAlgorithmException, IOException
    {
        FilesObject obj = new FilesObject(f, mimeType, this);

        if (objects != null)
            objects.add(obj);
        else
        {
           objects = getObjects();
           if (objects != null)
              return objects.add (obj);
        }
        logger.fatal("Could not add Object, it seems something is wrong with this Container or FilesClient"); 
        return false;
    }

    /**
     * Creates the container represented by this instance on the server
     *
     * @return Either FilesConstants.CONTAINER_CREATED or FilesConstants.CONTAINER_EXISTED or -1 if the client has not been set 
     * @throws HttpException
     * @throws IOException
     */
    public int createContainer () throws HttpException, IOException
    {
        if (client != null)
        {
        	return client.createContainer(this.name);
        }
        else
            logger.fatal("This Container has no FilesClient defined !");
        return -1;
    }
}
