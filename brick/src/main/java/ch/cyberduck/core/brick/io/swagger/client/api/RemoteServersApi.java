package ch.cyberduck.core.brick.io.swagger.client.api;

import ch.cyberduck.core.brick.io.swagger.client.ApiException;
import ch.cyberduck.core.brick.io.swagger.client.ApiClient;
import ch.cyberduck.core.brick.io.swagger.client.ApiResponse;
import ch.cyberduck.core.brick.io.swagger.client.Configuration;
import ch.cyberduck.core.brick.io.swagger.client.Pair;

import javax.ws.rs.core.GenericType;

import ch.cyberduck.core.brick.io.swagger.client.model.RemoteServerEntity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaClientCodegen", date = "2021-06-30T21:29:25.490+02:00")
public class RemoteServersApi {
  private ApiClient apiClient;

  public RemoteServersApi() {
    this(Configuration.getDefaultApiClient());
  }

  public RemoteServersApi(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  public ApiClient getApiClient() {
    return apiClient;
  }

  public void setApiClient(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  /**
   * Delete Remote Server
   * Delete Remote Server
   * @param id Remote Server ID. (required)
   * @throws ApiException if fails to make API call
   */
  public void deleteRemoteServersId(Integer id) throws ApiException {

    deleteRemoteServersIdWithHttpInfo(id);
  }

  /**
   * Delete Remote Server
   * Delete Remote Server
   * @param id Remote Server ID. (required)
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<Void> deleteRemoteServersIdWithHttpInfo(Integer id) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'id' is set
    if (id == null) {
      throw new ApiException(400, "Missing the required parameter 'id' when calling deleteRemoteServersId");
    }
    
    // create path and map variables
    String localVarPath = "/remote_servers/{id}"
      .replaceAll("\\{" + "id" + "\\}", apiClient.escapeString(id.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


    
    
    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] {  };


    return apiClient.invokeAPI(localVarPath, "DELETE", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, null);
  }
  /**
   * List Remote Servers
   * List Remote Servers
   * @param cursor Used for pagination.  Send a cursor value to resume an existing list from the point at which you left off.  Get a cursor from an existing list via the X-Files-Cursor-Next header. (optional)
   * @param perPage Number of records to show per page.  (Max: 10,000, 1,000 or less is recommended). (optional)
   * @return List&lt;RemoteServerEntity&gt;
   * @throws ApiException if fails to make API call
   */
  public List<RemoteServerEntity> getRemoteServers(String cursor, Integer perPage) throws ApiException {
    return getRemoteServersWithHttpInfo(cursor, perPage).getData();
      }

  /**
   * List Remote Servers
   * List Remote Servers
   * @param cursor Used for pagination.  Send a cursor value to resume an existing list from the point at which you left off.  Get a cursor from an existing list via the X-Files-Cursor-Next header. (optional)
   * @param perPage Number of records to show per page.  (Max: 10,000, 1,000 or less is recommended). (optional)
   * @return ApiResponse&lt;List&lt;RemoteServerEntity&gt;&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<List<RemoteServerEntity>> getRemoteServersWithHttpInfo(String cursor, Integer perPage) throws ApiException {
    Object localVarPostBody = null;
    
    // create path and map variables
    String localVarPath = "/remote_servers";

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    localVarQueryParams.addAll(apiClient.parameterToPairs("", "cursor", cursor));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "per_page", perPage));

    
    
    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] {  };

    GenericType<List<RemoteServerEntity>> localVarReturnType = new GenericType<List<RemoteServerEntity>>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Show Remote Server
   * Show Remote Server
   * @param id Remote Server ID. (required)
   * @return RemoteServerEntity
   * @throws ApiException if fails to make API call
   */
  public RemoteServerEntity getRemoteServersId(Integer id) throws ApiException {
    return getRemoteServersIdWithHttpInfo(id).getData();
      }

  /**
   * Show Remote Server
   * Show Remote Server
   * @param id Remote Server ID. (required)
   * @return ApiResponse&lt;RemoteServerEntity&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<RemoteServerEntity> getRemoteServersIdWithHttpInfo(Integer id) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'id' is set
    if (id == null) {
      throw new ApiException(400, "Missing the required parameter 'id' when calling getRemoteServersId");
    }
    
    // create path and map variables
    String localVarPath = "/remote_servers/{id}"
      .replaceAll("\\{" + "id" + "\\}", apiClient.escapeString(id.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


    
    
    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] {  };

    GenericType<RemoteServerEntity> localVarReturnType = new GenericType<RemoteServerEntity>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Update Remote Server
   * Update Remote Server
   * @param id Remote Server ID. (required)
   * @param awsAccessKey AWS Access Key. (optional)
   * @param awsSecretKey AWS secret key. (optional)
   * @param password Password if needed. (optional)
   * @param privateKey Private key if needed. (optional)
   * @param sslCertificate SSL client certificate. (optional)
   * @param googleCloudStorageCredentialsJson A JSON file that contains the private key. To generate see https://cloud.google.com/storage/docs/json_api/v1/how-tos/authorizing#APIKey (optional)
   * @param wasabiAccessKey Wasabi access key. (optional)
   * @param wasabiSecretKey Wasabi secret key. (optional)
   * @param backblazeB2KeyId Backblaze B2 Cloud Storage keyID. (optional)
   * @param backblazeB2ApplicationKey Backblaze B2 Cloud Storage applicationKey. (optional)
   * @param rackspaceApiKey Rackspace API key from the Rackspace Cloud Control Panel. (optional)
   * @param resetAuthentication Reset authenticated account (optional)
   * @param azureBlobStorageAccessKey Azure Blob Storage secret key. (optional)
   * @param hostname Hostname or IP address (optional)
   * @param name Internal name for your reference (optional)
   * @param maxConnections Max number of parallel connections.  Ignored for S3 connections (we will parallelize these as much as possible). (optional)
   * @param port Port for remote server.  Not needed for S3. (optional)
   * @param s3Bucket S3 bucket name (optional)
   * @param s3Region S3 region (optional)
   * @param serverCertificate Remote server certificate (optional)
   * @param serverHostKey Remote server SSH Host Key. If provided, we will require that the server host key matches the provided key. Uses OpenSSH format similar to what would go into ~/.ssh/known_hosts (optional)
   * @param serverType Remote server type. (optional)
   * @param ssl Should we require SSL? (optional)
   * @param username Remote server username.  Not needed for S3 buckets. (optional)
   * @param googleCloudStorageBucket Google Cloud Storage bucket name (optional)
   * @param googleCloudStorageProjectId Google Cloud Project ID (optional)
   * @param backblazeB2Bucket Backblaze B2 Cloud Storage Bucket name (optional)
   * @param backblazeB2S3Endpoint Backblaze B2 Cloud Storage S3 Endpoint (optional)
   * @param wasabiBucket Wasabi Bucket name (optional)
   * @param wasabiRegion Wasabi region (optional)
   * @param rackspaceUsername Rackspace username used to login to the Rackspace Cloud Control Panel. (optional)
   * @param rackspaceRegion Three letter airport code for Rackspace region. See https://support.rackspace.com/how-to/about-regions/ (optional)
   * @param rackspaceContainer The name of the container (top level directory) where files will sync. (optional)
   * @param oneDriveAccountType Either personal or business_other account types (optional)
   * @param azureBlobStorageAccount Azure Blob Storage Account name (optional)
   * @param azureBlobStorageContainer Azure Blob Storage Container name (optional)
   * @param s3CompatibleBucket S3-compatible Bucket name (optional)
   * @param s3CompatibleRegion S3-compatible Bucket name (optional)
   * @param s3CompatibleEndpoint S3-compatible endpoint (optional)
   * @param s3CompatibleAccessKey S3-compatible access key (optional)
   * @param s3CompatibleSecretKey S3-compatible secret key (optional)
   * @return RemoteServerEntity
   * @throws ApiException if fails to make API call
   */
  public RemoteServerEntity patchRemoteServersId(Integer id, String awsAccessKey, String awsSecretKey, String password, String privateKey, String sslCertificate, String googleCloudStorageCredentialsJson, String wasabiAccessKey, String wasabiSecretKey, String backblazeB2KeyId, String backblazeB2ApplicationKey, String rackspaceApiKey, Boolean resetAuthentication, String azureBlobStorageAccessKey, String hostname, String name, Integer maxConnections, Integer port, String s3Bucket, String s3Region, String serverCertificate, String serverHostKey, String serverType, String ssl, String username, String googleCloudStorageBucket, String googleCloudStorageProjectId, String backblazeB2Bucket, String backblazeB2S3Endpoint, String wasabiBucket, String wasabiRegion, String rackspaceUsername, String rackspaceRegion, String rackspaceContainer, String oneDriveAccountType, String azureBlobStorageAccount, String azureBlobStorageContainer, String s3CompatibleBucket, String s3CompatibleRegion, String s3CompatibleEndpoint, String s3CompatibleAccessKey, String s3CompatibleSecretKey) throws ApiException {
    return patchRemoteServersIdWithHttpInfo(id, awsAccessKey, awsSecretKey, password, privateKey, sslCertificate, googleCloudStorageCredentialsJson, wasabiAccessKey, wasabiSecretKey, backblazeB2KeyId, backblazeB2ApplicationKey, rackspaceApiKey, resetAuthentication, azureBlobStorageAccessKey, hostname, name, maxConnections, port, s3Bucket, s3Region, serverCertificate, serverHostKey, serverType, ssl, username, googleCloudStorageBucket, googleCloudStorageProjectId, backblazeB2Bucket, backblazeB2S3Endpoint, wasabiBucket, wasabiRegion, rackspaceUsername, rackspaceRegion, rackspaceContainer, oneDriveAccountType, azureBlobStorageAccount, azureBlobStorageContainer, s3CompatibleBucket, s3CompatibleRegion, s3CompatibleEndpoint, s3CompatibleAccessKey, s3CompatibleSecretKey).getData();
      }

  /**
   * Update Remote Server
   * Update Remote Server
   * @param id Remote Server ID. (required)
   * @param awsAccessKey AWS Access Key. (optional)
   * @param awsSecretKey AWS secret key. (optional)
   * @param password Password if needed. (optional)
   * @param privateKey Private key if needed. (optional)
   * @param sslCertificate SSL client certificate. (optional)
   * @param googleCloudStorageCredentialsJson A JSON file that contains the private key. To generate see https://cloud.google.com/storage/docs/json_api/v1/how-tos/authorizing#APIKey (optional)
   * @param wasabiAccessKey Wasabi access key. (optional)
   * @param wasabiSecretKey Wasabi secret key. (optional)
   * @param backblazeB2KeyId Backblaze B2 Cloud Storage keyID. (optional)
   * @param backblazeB2ApplicationKey Backblaze B2 Cloud Storage applicationKey. (optional)
   * @param rackspaceApiKey Rackspace API key from the Rackspace Cloud Control Panel. (optional)
   * @param resetAuthentication Reset authenticated account (optional)
   * @param azureBlobStorageAccessKey Azure Blob Storage secret key. (optional)
   * @param hostname Hostname or IP address (optional)
   * @param name Internal name for your reference (optional)
   * @param maxConnections Max number of parallel connections.  Ignored for S3 connections (we will parallelize these as much as possible). (optional)
   * @param port Port for remote server.  Not needed for S3. (optional)
   * @param s3Bucket S3 bucket name (optional)
   * @param s3Region S3 region (optional)
   * @param serverCertificate Remote server certificate (optional)
   * @param serverHostKey Remote server SSH Host Key. If provided, we will require that the server host key matches the provided key. Uses OpenSSH format similar to what would go into ~/.ssh/known_hosts (optional)
   * @param serverType Remote server type. (optional)
   * @param ssl Should we require SSL? (optional)
   * @param username Remote server username.  Not needed for S3 buckets. (optional)
   * @param googleCloudStorageBucket Google Cloud Storage bucket name (optional)
   * @param googleCloudStorageProjectId Google Cloud Project ID (optional)
   * @param backblazeB2Bucket Backblaze B2 Cloud Storage Bucket name (optional)
   * @param backblazeB2S3Endpoint Backblaze B2 Cloud Storage S3 Endpoint (optional)
   * @param wasabiBucket Wasabi Bucket name (optional)
   * @param wasabiRegion Wasabi region (optional)
   * @param rackspaceUsername Rackspace username used to login to the Rackspace Cloud Control Panel. (optional)
   * @param rackspaceRegion Three letter airport code for Rackspace region. See https://support.rackspace.com/how-to/about-regions/ (optional)
   * @param rackspaceContainer The name of the container (top level directory) where files will sync. (optional)
   * @param oneDriveAccountType Either personal or business_other account types (optional)
   * @param azureBlobStorageAccount Azure Blob Storage Account name (optional)
   * @param azureBlobStorageContainer Azure Blob Storage Container name (optional)
   * @param s3CompatibleBucket S3-compatible Bucket name (optional)
   * @param s3CompatibleRegion S3-compatible Bucket name (optional)
   * @param s3CompatibleEndpoint S3-compatible endpoint (optional)
   * @param s3CompatibleAccessKey S3-compatible access key (optional)
   * @param s3CompatibleSecretKey S3-compatible secret key (optional)
   * @return ApiResponse&lt;RemoteServerEntity&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<RemoteServerEntity> patchRemoteServersIdWithHttpInfo(Integer id, String awsAccessKey, String awsSecretKey, String password, String privateKey, String sslCertificate, String googleCloudStorageCredentialsJson, String wasabiAccessKey, String wasabiSecretKey, String backblazeB2KeyId, String backblazeB2ApplicationKey, String rackspaceApiKey, Boolean resetAuthentication, String azureBlobStorageAccessKey, String hostname, String name, Integer maxConnections, Integer port, String s3Bucket, String s3Region, String serverCertificate, String serverHostKey, String serverType, String ssl, String username, String googleCloudStorageBucket, String googleCloudStorageProjectId, String backblazeB2Bucket, String backblazeB2S3Endpoint, String wasabiBucket, String wasabiRegion, String rackspaceUsername, String rackspaceRegion, String rackspaceContainer, String oneDriveAccountType, String azureBlobStorageAccount, String azureBlobStorageContainer, String s3CompatibleBucket, String s3CompatibleRegion, String s3CompatibleEndpoint, String s3CompatibleAccessKey, String s3CompatibleSecretKey) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'id' is set
    if (id == null) {
      throw new ApiException(400, "Missing the required parameter 'id' when calling patchRemoteServersId");
    }
    
    // create path and map variables
    String localVarPath = "/remote_servers/{id}"
      .replaceAll("\\{" + "id" + "\\}", apiClient.escapeString(id.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


    
    if (awsAccessKey != null)
      localVarFormParams.put("aws_access_key", awsAccessKey);
if (awsSecretKey != null)
      localVarFormParams.put("aws_secret_key", awsSecretKey);
if (password != null)
      localVarFormParams.put("password", password);
if (privateKey != null)
      localVarFormParams.put("private_key", privateKey);
if (sslCertificate != null)
      localVarFormParams.put("ssl_certificate", sslCertificate);
if (googleCloudStorageCredentialsJson != null)
      localVarFormParams.put("google_cloud_storage_credentials_json", googleCloudStorageCredentialsJson);
if (wasabiAccessKey != null)
      localVarFormParams.put("wasabi_access_key", wasabiAccessKey);
if (wasabiSecretKey != null)
      localVarFormParams.put("wasabi_secret_key", wasabiSecretKey);
if (backblazeB2KeyId != null)
      localVarFormParams.put("backblaze_b2_key_id", backblazeB2KeyId);
if (backblazeB2ApplicationKey != null)
      localVarFormParams.put("backblaze_b2_application_key", backblazeB2ApplicationKey);
if (rackspaceApiKey != null)
      localVarFormParams.put("rackspace_api_key", rackspaceApiKey);
if (resetAuthentication != null)
      localVarFormParams.put("reset_authentication", resetAuthentication);
if (azureBlobStorageAccessKey != null)
      localVarFormParams.put("azure_blob_storage_access_key", azureBlobStorageAccessKey);
if (hostname != null)
      localVarFormParams.put("hostname", hostname);
if (name != null)
      localVarFormParams.put("name", name);
if (maxConnections != null)
      localVarFormParams.put("max_connections", maxConnections);
if (port != null)
      localVarFormParams.put("port", port);
if (s3Bucket != null)
      localVarFormParams.put("s3_bucket", s3Bucket);
if (s3Region != null)
      localVarFormParams.put("s3_region", s3Region);
if (serverCertificate != null)
      localVarFormParams.put("server_certificate", serverCertificate);
if (serverHostKey != null)
      localVarFormParams.put("server_host_key", serverHostKey);
if (serverType != null)
      localVarFormParams.put("server_type", serverType);
if (ssl != null)
      localVarFormParams.put("ssl", ssl);
if (username != null)
      localVarFormParams.put("username", username);
if (googleCloudStorageBucket != null)
      localVarFormParams.put("google_cloud_storage_bucket", googleCloudStorageBucket);
if (googleCloudStorageProjectId != null)
      localVarFormParams.put("google_cloud_storage_project_id", googleCloudStorageProjectId);
if (backblazeB2Bucket != null)
      localVarFormParams.put("backblaze_b2_bucket", backblazeB2Bucket);
if (backblazeB2S3Endpoint != null)
      localVarFormParams.put("backblaze_b2_s3_endpoint", backblazeB2S3Endpoint);
if (wasabiBucket != null)
      localVarFormParams.put("wasabi_bucket", wasabiBucket);
if (wasabiRegion != null)
      localVarFormParams.put("wasabi_region", wasabiRegion);
if (rackspaceUsername != null)
      localVarFormParams.put("rackspace_username", rackspaceUsername);
if (rackspaceRegion != null)
      localVarFormParams.put("rackspace_region", rackspaceRegion);
if (rackspaceContainer != null)
      localVarFormParams.put("rackspace_container", rackspaceContainer);
if (oneDriveAccountType != null)
      localVarFormParams.put("one_drive_account_type", oneDriveAccountType);
if (azureBlobStorageAccount != null)
      localVarFormParams.put("azure_blob_storage_account", azureBlobStorageAccount);
if (azureBlobStorageContainer != null)
      localVarFormParams.put("azure_blob_storage_container", azureBlobStorageContainer);
if (s3CompatibleBucket != null)
      localVarFormParams.put("s3_compatible_bucket", s3CompatibleBucket);
if (s3CompatibleRegion != null)
      localVarFormParams.put("s3_compatible_region", s3CompatibleRegion);
if (s3CompatibleEndpoint != null)
      localVarFormParams.put("s3_compatible_endpoint", s3CompatibleEndpoint);
if (s3CompatibleAccessKey != null)
      localVarFormParams.put("s3_compatible_access_key", s3CompatibleAccessKey);
if (s3CompatibleSecretKey != null)
      localVarFormParams.put("s3_compatible_secret_key", s3CompatibleSecretKey);

    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json", "application/x-www-form-urlencoded", "multipart/form-data"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] {  };

    GenericType<RemoteServerEntity> localVarReturnType = new GenericType<RemoteServerEntity>() {};
    return apiClient.invokeAPI(localVarPath, "PATCH", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Create Remote Server
   * Create Remote Server
   * @param awsAccessKey AWS Access Key. (optional)
   * @param awsSecretKey AWS secret key. (optional)
   * @param password Password if needed. (optional)
   * @param privateKey Private key if needed. (optional)
   * @param sslCertificate SSL client certificate. (optional)
   * @param googleCloudStorageCredentialsJson A JSON file that contains the private key. To generate see https://cloud.google.com/storage/docs/json_api/v1/how-tos/authorizing#APIKey (optional)
   * @param wasabiAccessKey Wasabi access key. (optional)
   * @param wasabiSecretKey Wasabi secret key. (optional)
   * @param backblazeB2KeyId Backblaze B2 Cloud Storage keyID. (optional)
   * @param backblazeB2ApplicationKey Backblaze B2 Cloud Storage applicationKey. (optional)
   * @param rackspaceApiKey Rackspace API key from the Rackspace Cloud Control Panel. (optional)
   * @param resetAuthentication Reset authenticated account (optional)
   * @param azureBlobStorageAccessKey Azure Blob Storage secret key. (optional)
   * @param hostname Hostname or IP address (optional)
   * @param name Internal name for your reference (optional)
   * @param maxConnections Max number of parallel connections.  Ignored for S3 connections (we will parallelize these as much as possible). (optional)
   * @param port Port for remote server.  Not needed for S3. (optional)
   * @param s3Bucket S3 bucket name (optional)
   * @param s3Region S3 region (optional)
   * @param serverCertificate Remote server certificate (optional)
   * @param serverHostKey Remote server SSH Host Key. If provided, we will require that the server host key matches the provided key. Uses OpenSSH format similar to what would go into ~/.ssh/known_hosts (optional)
   * @param serverType Remote server type. (optional)
   * @param ssl Should we require SSL? (optional)
   * @param username Remote server username.  Not needed for S3 buckets. (optional)
   * @param googleCloudStorageBucket Google Cloud Storage bucket name (optional)
   * @param googleCloudStorageProjectId Google Cloud Project ID (optional)
   * @param backblazeB2Bucket Backblaze B2 Cloud Storage Bucket name (optional)
   * @param backblazeB2S3Endpoint Backblaze B2 Cloud Storage S3 Endpoint (optional)
   * @param wasabiBucket Wasabi Bucket name (optional)
   * @param wasabiRegion Wasabi region (optional)
   * @param rackspaceUsername Rackspace username used to login to the Rackspace Cloud Control Panel. (optional)
   * @param rackspaceRegion Three letter airport code for Rackspace region. See https://support.rackspace.com/how-to/about-regions/ (optional)
   * @param rackspaceContainer The name of the container (top level directory) where files will sync. (optional)
   * @param oneDriveAccountType Either personal or business_other account types (optional)
   * @param azureBlobStorageAccount Azure Blob Storage Account name (optional)
   * @param azureBlobStorageContainer Azure Blob Storage Container name (optional)
   * @param s3CompatibleBucket S3-compatible Bucket name (optional)
   * @param s3CompatibleRegion S3-compatible Bucket name (optional)
   * @param s3CompatibleEndpoint S3-compatible endpoint (optional)
   * @param s3CompatibleAccessKey S3-compatible access key (optional)
   * @param s3CompatibleSecretKey S3-compatible secret key (optional)
   * @return RemoteServerEntity
   * @throws ApiException if fails to make API call
   */
  public RemoteServerEntity postRemoteServers(String awsAccessKey, String awsSecretKey, String password, String privateKey, String sslCertificate, String googleCloudStorageCredentialsJson, String wasabiAccessKey, String wasabiSecretKey, String backblazeB2KeyId, String backblazeB2ApplicationKey, String rackspaceApiKey, Boolean resetAuthentication, String azureBlobStorageAccessKey, String hostname, String name, Integer maxConnections, Integer port, String s3Bucket, String s3Region, String serverCertificate, String serverHostKey, String serverType, String ssl, String username, String googleCloudStorageBucket, String googleCloudStorageProjectId, String backblazeB2Bucket, String backblazeB2S3Endpoint, String wasabiBucket, String wasabiRegion, String rackspaceUsername, String rackspaceRegion, String rackspaceContainer, String oneDriveAccountType, String azureBlobStorageAccount, String azureBlobStorageContainer, String s3CompatibleBucket, String s3CompatibleRegion, String s3CompatibleEndpoint, String s3CompatibleAccessKey, String s3CompatibleSecretKey) throws ApiException {
    return postRemoteServersWithHttpInfo(awsAccessKey, awsSecretKey, password, privateKey, sslCertificate, googleCloudStorageCredentialsJson, wasabiAccessKey, wasabiSecretKey, backblazeB2KeyId, backblazeB2ApplicationKey, rackspaceApiKey, resetAuthentication, azureBlobStorageAccessKey, hostname, name, maxConnections, port, s3Bucket, s3Region, serverCertificate, serverHostKey, serverType, ssl, username, googleCloudStorageBucket, googleCloudStorageProjectId, backblazeB2Bucket, backblazeB2S3Endpoint, wasabiBucket, wasabiRegion, rackspaceUsername, rackspaceRegion, rackspaceContainer, oneDriveAccountType, azureBlobStorageAccount, azureBlobStorageContainer, s3CompatibleBucket, s3CompatibleRegion, s3CompatibleEndpoint, s3CompatibleAccessKey, s3CompatibleSecretKey).getData();
      }

  /**
   * Create Remote Server
   * Create Remote Server
   * @param awsAccessKey AWS Access Key. (optional)
   * @param awsSecretKey AWS secret key. (optional)
   * @param password Password if needed. (optional)
   * @param privateKey Private key if needed. (optional)
   * @param sslCertificate SSL client certificate. (optional)
   * @param googleCloudStorageCredentialsJson A JSON file that contains the private key. To generate see https://cloud.google.com/storage/docs/json_api/v1/how-tos/authorizing#APIKey (optional)
   * @param wasabiAccessKey Wasabi access key. (optional)
   * @param wasabiSecretKey Wasabi secret key. (optional)
   * @param backblazeB2KeyId Backblaze B2 Cloud Storage keyID. (optional)
   * @param backblazeB2ApplicationKey Backblaze B2 Cloud Storage applicationKey. (optional)
   * @param rackspaceApiKey Rackspace API key from the Rackspace Cloud Control Panel. (optional)
   * @param resetAuthentication Reset authenticated account (optional)
   * @param azureBlobStorageAccessKey Azure Blob Storage secret key. (optional)
   * @param hostname Hostname or IP address (optional)
   * @param name Internal name for your reference (optional)
   * @param maxConnections Max number of parallel connections.  Ignored for S3 connections (we will parallelize these as much as possible). (optional)
   * @param port Port for remote server.  Not needed for S3. (optional)
   * @param s3Bucket S3 bucket name (optional)
   * @param s3Region S3 region (optional)
   * @param serverCertificate Remote server certificate (optional)
   * @param serverHostKey Remote server SSH Host Key. If provided, we will require that the server host key matches the provided key. Uses OpenSSH format similar to what would go into ~/.ssh/known_hosts (optional)
   * @param serverType Remote server type. (optional)
   * @param ssl Should we require SSL? (optional)
   * @param username Remote server username.  Not needed for S3 buckets. (optional)
   * @param googleCloudStorageBucket Google Cloud Storage bucket name (optional)
   * @param googleCloudStorageProjectId Google Cloud Project ID (optional)
   * @param backblazeB2Bucket Backblaze B2 Cloud Storage Bucket name (optional)
   * @param backblazeB2S3Endpoint Backblaze B2 Cloud Storage S3 Endpoint (optional)
   * @param wasabiBucket Wasabi Bucket name (optional)
   * @param wasabiRegion Wasabi region (optional)
   * @param rackspaceUsername Rackspace username used to login to the Rackspace Cloud Control Panel. (optional)
   * @param rackspaceRegion Three letter airport code for Rackspace region. See https://support.rackspace.com/how-to/about-regions/ (optional)
   * @param rackspaceContainer The name of the container (top level directory) where files will sync. (optional)
   * @param oneDriveAccountType Either personal or business_other account types (optional)
   * @param azureBlobStorageAccount Azure Blob Storage Account name (optional)
   * @param azureBlobStorageContainer Azure Blob Storage Container name (optional)
   * @param s3CompatibleBucket S3-compatible Bucket name (optional)
   * @param s3CompatibleRegion S3-compatible Bucket name (optional)
   * @param s3CompatibleEndpoint S3-compatible endpoint (optional)
   * @param s3CompatibleAccessKey S3-compatible access key (optional)
   * @param s3CompatibleSecretKey S3-compatible secret key (optional)
   * @return ApiResponse&lt;RemoteServerEntity&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<RemoteServerEntity> postRemoteServersWithHttpInfo(String awsAccessKey, String awsSecretKey, String password, String privateKey, String sslCertificate, String googleCloudStorageCredentialsJson, String wasabiAccessKey, String wasabiSecretKey, String backblazeB2KeyId, String backblazeB2ApplicationKey, String rackspaceApiKey, Boolean resetAuthentication, String azureBlobStorageAccessKey, String hostname, String name, Integer maxConnections, Integer port, String s3Bucket, String s3Region, String serverCertificate, String serverHostKey, String serverType, String ssl, String username, String googleCloudStorageBucket, String googleCloudStorageProjectId, String backblazeB2Bucket, String backblazeB2S3Endpoint, String wasabiBucket, String wasabiRegion, String rackspaceUsername, String rackspaceRegion, String rackspaceContainer, String oneDriveAccountType, String azureBlobStorageAccount, String azureBlobStorageContainer, String s3CompatibleBucket, String s3CompatibleRegion, String s3CompatibleEndpoint, String s3CompatibleAccessKey, String s3CompatibleSecretKey) throws ApiException {
    Object localVarPostBody = null;
    
    // create path and map variables
    String localVarPath = "/remote_servers";

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


    
    if (awsAccessKey != null)
      localVarFormParams.put("aws_access_key", awsAccessKey);
if (awsSecretKey != null)
      localVarFormParams.put("aws_secret_key", awsSecretKey);
if (password != null)
      localVarFormParams.put("password", password);
if (privateKey != null)
      localVarFormParams.put("private_key", privateKey);
if (sslCertificate != null)
      localVarFormParams.put("ssl_certificate", sslCertificate);
if (googleCloudStorageCredentialsJson != null)
      localVarFormParams.put("google_cloud_storage_credentials_json", googleCloudStorageCredentialsJson);
if (wasabiAccessKey != null)
      localVarFormParams.put("wasabi_access_key", wasabiAccessKey);
if (wasabiSecretKey != null)
      localVarFormParams.put("wasabi_secret_key", wasabiSecretKey);
if (backblazeB2KeyId != null)
      localVarFormParams.put("backblaze_b2_key_id", backblazeB2KeyId);
if (backblazeB2ApplicationKey != null)
      localVarFormParams.put("backblaze_b2_application_key", backblazeB2ApplicationKey);
if (rackspaceApiKey != null)
      localVarFormParams.put("rackspace_api_key", rackspaceApiKey);
if (resetAuthentication != null)
      localVarFormParams.put("reset_authentication", resetAuthentication);
if (azureBlobStorageAccessKey != null)
      localVarFormParams.put("azure_blob_storage_access_key", azureBlobStorageAccessKey);
if (hostname != null)
      localVarFormParams.put("hostname", hostname);
if (name != null)
      localVarFormParams.put("name", name);
if (maxConnections != null)
      localVarFormParams.put("max_connections", maxConnections);
if (port != null)
      localVarFormParams.put("port", port);
if (s3Bucket != null)
      localVarFormParams.put("s3_bucket", s3Bucket);
if (s3Region != null)
      localVarFormParams.put("s3_region", s3Region);
if (serverCertificate != null)
      localVarFormParams.put("server_certificate", serverCertificate);
if (serverHostKey != null)
      localVarFormParams.put("server_host_key", serverHostKey);
if (serverType != null)
      localVarFormParams.put("server_type", serverType);
if (ssl != null)
      localVarFormParams.put("ssl", ssl);
if (username != null)
      localVarFormParams.put("username", username);
if (googleCloudStorageBucket != null)
      localVarFormParams.put("google_cloud_storage_bucket", googleCloudStorageBucket);
if (googleCloudStorageProjectId != null)
      localVarFormParams.put("google_cloud_storage_project_id", googleCloudStorageProjectId);
if (backblazeB2Bucket != null)
      localVarFormParams.put("backblaze_b2_bucket", backblazeB2Bucket);
if (backblazeB2S3Endpoint != null)
      localVarFormParams.put("backblaze_b2_s3_endpoint", backblazeB2S3Endpoint);
if (wasabiBucket != null)
      localVarFormParams.put("wasabi_bucket", wasabiBucket);
if (wasabiRegion != null)
      localVarFormParams.put("wasabi_region", wasabiRegion);
if (rackspaceUsername != null)
      localVarFormParams.put("rackspace_username", rackspaceUsername);
if (rackspaceRegion != null)
      localVarFormParams.put("rackspace_region", rackspaceRegion);
if (rackspaceContainer != null)
      localVarFormParams.put("rackspace_container", rackspaceContainer);
if (oneDriveAccountType != null)
      localVarFormParams.put("one_drive_account_type", oneDriveAccountType);
if (azureBlobStorageAccount != null)
      localVarFormParams.put("azure_blob_storage_account", azureBlobStorageAccount);
if (azureBlobStorageContainer != null)
      localVarFormParams.put("azure_blob_storage_container", azureBlobStorageContainer);
if (s3CompatibleBucket != null)
      localVarFormParams.put("s3_compatible_bucket", s3CompatibleBucket);
if (s3CompatibleRegion != null)
      localVarFormParams.put("s3_compatible_region", s3CompatibleRegion);
if (s3CompatibleEndpoint != null)
      localVarFormParams.put("s3_compatible_endpoint", s3CompatibleEndpoint);
if (s3CompatibleAccessKey != null)
      localVarFormParams.put("s3_compatible_access_key", s3CompatibleAccessKey);
if (s3CompatibleSecretKey != null)
      localVarFormParams.put("s3_compatible_secret_key", s3CompatibleSecretKey);

    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json", "application/x-www-form-urlencoded", "multipart/form-data"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] {  };

    GenericType<RemoteServerEntity> localVarReturnType = new GenericType<RemoteServerEntity>() {};
    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
}
