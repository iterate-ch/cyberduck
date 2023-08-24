package ch.cyberduck.core.storegate.io.swagger.client.api;

import ch.cyberduck.core.storegate.io.swagger.client.ApiException;
import ch.cyberduck.core.storegate.io.swagger.client.ApiClient;
import ch.cyberduck.core.storegate.io.swagger.client.ApiResponse;
import ch.cyberduck.core.storegate.io.swagger.client.Configuration;
import ch.cyberduck.core.storegate.io.swagger.client.Pair;

import javax.ws.rs.core.GenericType;

import ch.cyberduck.core.storegate.io.swagger.client.model.BackupClient;
import ch.cyberduck.core.storegate.io.swagger.client.model.BackupClientSettings;
import ch.cyberduck.core.storegate.io.swagger.client.model.BackupClients;
import ch.cyberduck.core.storegate.io.swagger.client.model.Client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaClientCodegen", date = "2023-08-24T11:36:23.792+02:00")
public class BackupApi {
  private ApiClient apiClient;

  public BackupApi() {
    this(Configuration.getDefaultApiClient());
  }

  public BackupApi(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  public ApiClient getApiClient() {
    return apiClient;
  }

  public void setApiClient(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  /**
   * Deletes a specific backup client (unlinks the client and deletes the files).
   * 
   * @param id The id to the specific backup client (required)
   * @throws ApiException if fails to make API call
   */
  public void backupDeleteBackupClient(String id) throws ApiException {

    backupDeleteBackupClientWithHttpInfo(id);
  }

  /**
   * Deletes a specific backup client (unlinks the client and deletes the files).
   * 
   * @param id The id to the specific backup client (required)
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<Void> backupDeleteBackupClientWithHttpInfo(String id) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'id' is set
    if (id == null) {
      throw new ApiException(400, "Missing the required parameter 'id' when calling backupDeleteBackupClient");
    }
    
    // create path and map variables
    String localVarPath = "/v4.2/backup/clients/{id}"
      .replaceAll("\\{" + "id" + "\\}", apiClient.escapeString(id.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


    
    
    final String[] localVarAccepts = {
      
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "oauth2" };


    return apiClient.invokeAPI(localVarPath, "DELETE", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, null);
  }
  /**
   * Lists backup clients.
   * 
   * @return BackupClients
   * @throws ApiException if fails to make API call
   */
  public BackupClients backupGetBackupClients() throws ApiException {
    return backupGetBackupClientsWithHttpInfo().getData();
      }

  /**
   * Lists backup clients.
   * 
   * @return ApiResponse&lt;BackupClients&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<BackupClients> backupGetBackupClientsWithHttpInfo() throws ApiException {
    Object localVarPostBody = null;
    
    // create path and map variables
    String localVarPath = "/v4.2/backup/clients";

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


    
    
    final String[] localVarAccepts = {
      "application/json", "text/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "oauth2" };

    GenericType<BackupClients> localVarReturnType = new GenericType<BackupClients>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Get backup size
   * 
   * @param id  (required)
   * @return Long
   * @throws ApiException if fails to make API call
   */
  public Long backupGetBackupSize(String id) throws ApiException {
    return backupGetBackupSizeWithHttpInfo(id).getData();
      }

  /**
   * Get backup size
   * 
   * @param id  (required)
   * @return ApiResponse&lt;Long&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<Long> backupGetBackupSizeWithHttpInfo(String id) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'id' is set
    if (id == null) {
      throw new ApiException(400, "Missing the required parameter 'id' when calling backupGetBackupSize");
    }
    
    // create path and map variables
    String localVarPath = "/v4.2/backup/clients/{id}/size"
      .replaceAll("\\{" + "id" + "\\}", apiClient.escapeString(id.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


    
    
    final String[] localVarAccepts = {
      "application/json", "text/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "oauth2" };

    GenericType<Long> localVarReturnType = new GenericType<Long>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Get information about the client
   * 
   * @param id The client (required)
   * @return BackupClient
   * @throws ApiException if fails to make API call
   */
  public BackupClient backupGetClient(String id) throws ApiException {
    return backupGetClientWithHttpInfo(id).getData();
      }

  /**
   * Get information about the client
   * 
   * @param id The client (required)
   * @return ApiResponse&lt;BackupClient&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<BackupClient> backupGetClientWithHttpInfo(String id) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'id' is set
    if (id == null) {
      throw new ApiException(400, "Missing the required parameter 'id' when calling backupGetClient");
    }
    
    // create path and map variables
    String localVarPath = "/v4.2/backup/clients/{id}"
      .replaceAll("\\{" + "id" + "\\}", apiClient.escapeString(id.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


    
    
    final String[] localVarAccepts = {
      "application/json", "text/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "oauth2" };

    GenericType<BackupClient> localVarReturnType = new GenericType<BackupClient>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * The get client policy
   * 
   * @return String
   * @throws ApiException if fails to make API call
   */
  public String backupGetPolicy() throws ApiException {
    return backupGetPolicyWithHttpInfo().getData();
      }

  /**
   * The get client policy
   * 
   * @return ApiResponse&lt;String&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<String> backupGetPolicyWithHttpInfo() throws ApiException {
    Object localVarPostBody = null;
    
    // create path and map variables
    String localVarPath = "/v4.2/backup/policy";

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


    
    
    final String[] localVarAccepts = {
      "application/json", "text/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "oauth2" };

    GenericType<String> localVarReturnType = new GenericType<String>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Creates a backup client
   * 
   * @param client The client to register (required)
   * @return BackupClient
   * @throws ApiException if fails to make API call
   */
  public BackupClient backupRegisterClient(Client client) throws ApiException {
    return backupRegisterClientWithHttpInfo(client).getData();
      }

  /**
   * Creates a backup client
   * 
   * @param client The client to register (required)
   * @return ApiResponse&lt;BackupClient&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<BackupClient> backupRegisterClientWithHttpInfo(Client client) throws ApiException {
    Object localVarPostBody = client;
    
    // verify the required parameter 'client' is set
    if (client == null) {
      throw new ApiException(400, "Missing the required parameter 'client' when calling backupRegisterClient");
    }
    
    // create path and map variables
    String localVarPath = "/v4.2/backup/clients";

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


    
    
    final String[] localVarAccepts = {
      "application/json", "text/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json", "text/json"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "oauth2" };

    GenericType<BackupClient> localVarReturnType = new GenericType<BackupClient>() {};
    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Update the client with the current queue size
   * 
   * @param id The client to update (required)
   * @param size The queue size in bytes (required)
   * @throws ApiException if fails to make API call
   */
  public void backupSetQueueSize(String id, Long size) throws ApiException {

    backupSetQueueSizeWithHttpInfo(id, size);
  }

  /**
   * Update the client with the current queue size
   * 
   * @param id The client to update (required)
   * @param size The queue size in bytes (required)
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<Void> backupSetQueueSizeWithHttpInfo(String id, Long size) throws ApiException {
    Object localVarPostBody = size;
    
    // verify the required parameter 'id' is set
    if (id == null) {
      throw new ApiException(400, "Missing the required parameter 'id' when calling backupSetQueueSize");
    }
    
    // verify the required parameter 'size' is set
    if (size == null) {
      throw new ApiException(400, "Missing the required parameter 'size' when calling backupSetQueueSize");
    }
    
    // create path and map variables
    String localVarPath = "/v4.2/backup/clients/{id}/queuesize"
      .replaceAll("\\{" + "id" + "\\}", apiClient.escapeString(id.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


    
    
    final String[] localVarAccepts = {
      
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json", "text/json"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "oauth2" };


    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, null);
  }
  /**
   * Updates settings for a backup client
   * 
   * @param id The id to the specific backup client (required)
   * @param settings The settings (required)
   * @throws ApiException if fails to make API call
   */
  public void backupUpdateBackupClient(String id, BackupClientSettings settings) throws ApiException {

    backupUpdateBackupClientWithHttpInfo(id, settings);
  }

  /**
   * Updates settings for a backup client
   * 
   * @param id The id to the specific backup client (required)
   * @param settings The settings (required)
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<Void> backupUpdateBackupClientWithHttpInfo(String id, BackupClientSettings settings) throws ApiException {
    Object localVarPostBody = settings;
    
    // verify the required parameter 'id' is set
    if (id == null) {
      throw new ApiException(400, "Missing the required parameter 'id' when calling backupUpdateBackupClient");
    }
    
    // verify the required parameter 'settings' is set
    if (settings == null) {
      throw new ApiException(400, "Missing the required parameter 'settings' when calling backupUpdateBackupClient");
    }
    
    // create path and map variables
    String localVarPath = "/v4.2/backup/clients/{id}/settings"
      .replaceAll("\\{" + "id" + "\\}", apiClient.escapeString(id.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


    
    
    final String[] localVarAccepts = {
      
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json", "text/json"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "oauth2" };


    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, null);
  }
}
