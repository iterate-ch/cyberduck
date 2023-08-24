package ch.cyberduck.core.storegate.io.swagger.client.api;

import ch.cyberduck.core.storegate.io.swagger.client.ApiException;
import ch.cyberduck.core.storegate.io.swagger.client.ApiClient;
import ch.cyberduck.core.storegate.io.swagger.client.ApiResponse;
import ch.cyberduck.core.storegate.io.swagger.client.Configuration;
import ch.cyberduck.core.storegate.io.swagger.client.Pair;

import javax.ws.rs.core.GenericType;

import ch.cyberduck.core.storegate.io.swagger.client.model.Client;
import ch.cyberduck.core.storegate.io.swagger.client.model.SyncClient;
import ch.cyberduck.core.storegate.io.swagger.client.model.SyncClients;
import ch.cyberduck.core.storegate.io.swagger.client.model.SyncInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaClientCodegen", date = "2023-08-24T11:36:23.792+02:00")
public class SyncApi {
  private ApiClient apiClient;

  public SyncApi() {
    this(Configuration.getDefaultApiClient());
  }

  public SyncApi(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  public ApiClient getApiClient() {
    return apiClient;
  }

  public void setApiClient(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  /**
   * Deletes a specific sync client (unlinks the client).
   * 
   * @param id The id to the specific sync client (required)
   * @throws ApiException if fails to make API call
   */
  public void syncDeleteSyncClient(String id) throws ApiException {

    syncDeleteSyncClientWithHttpInfo(id);
  }

  /**
   * Deletes a specific sync client (unlinks the client).
   * 
   * @param id The id to the specific sync client (required)
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<Void> syncDeleteSyncClientWithHttpInfo(String id) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'id' is set
    if (id == null) {
      throw new ApiException(400, "Missing the required parameter 'id' when calling syncDeleteSyncClient");
    }
    
    // create path and map variables
    String localVarPath = "/v4.2/sync/clients/{id}"
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
   * Get information about the client
   * 
   * @param id The client (required)
   * @return SyncClient
   * @throws ApiException if fails to make API call
   */
  public SyncClient syncGetClient(String id) throws ApiException {
    return syncGetClientWithHttpInfo(id).getData();
      }

  /**
   * Get information about the client
   * 
   * @param id The client (required)
   * @return ApiResponse&lt;SyncClient&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<SyncClient> syncGetClientWithHttpInfo(String id) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'id' is set
    if (id == null) {
      throw new ApiException(400, "Missing the required parameter 'id' when calling syncGetClient");
    }
    
    // create path and map variables
    String localVarPath = "/v4.2/sync/clients/{id}"
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

    GenericType<SyncClient> localVarReturnType = new GenericType<SyncClient>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * The get client policy
   * 
   * @return SyncInfo
   * @throws ApiException if fails to make API call
   */
  public SyncInfo syncGetInfo() throws ApiException {
    return syncGetInfoWithHttpInfo().getData();
      }

  /**
   * The get client policy
   * 
   * @return ApiResponse&lt;SyncInfo&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<SyncInfo> syncGetInfoWithHttpInfo() throws ApiException {
    Object localVarPostBody = null;
    
    // create path and map variables
    String localVarPath = "/v4.2/sync/info";

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

    GenericType<SyncInfo> localVarReturnType = new GenericType<SyncInfo>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * The get client policy
   * 
   * @return String
   * @throws ApiException if fails to make API call
   */
  public String syncGetPolicy() throws ApiException {
    return syncGetPolicyWithHttpInfo().getData();
      }

  /**
   * The get client policy
   * 
   * @return ApiResponse&lt;String&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<String> syncGetPolicyWithHttpInfo() throws ApiException {
    Object localVarPostBody = null;
    
    // create path and map variables
    String localVarPath = "/v4.2/sync/policy";

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
   * Lists sync clients.
   * 
   * @return SyncClients
   * @throws ApiException if fails to make API call
   */
  public SyncClients syncGetSyncClients() throws ApiException {
    return syncGetSyncClientsWithHttpInfo().getData();
      }

  /**
   * Lists sync clients.
   * 
   * @return ApiResponse&lt;SyncClients&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<SyncClients> syncGetSyncClientsWithHttpInfo() throws ApiException {
    Object localVarPostBody = null;
    
    // create path and map variables
    String localVarPath = "/v4.2/sync/clients";

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

    GenericType<SyncClients> localVarReturnType = new GenericType<SyncClients>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Creates a sync client
   * 
   * @param client The client to register (required)
   * @return SyncClient
   * @throws ApiException if fails to make API call
   */
  public SyncClient syncRegisterClient(Client client) throws ApiException {
    return syncRegisterClientWithHttpInfo(client).getData();
      }

  /**
   * Creates a sync client
   * 
   * @param client The client to register (required)
   * @return ApiResponse&lt;SyncClient&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<SyncClient> syncRegisterClientWithHttpInfo(Client client) throws ApiException {
    Object localVarPostBody = client;
    
    // verify the required parameter 'client' is set
    if (client == null) {
      throw new ApiException(400, "Missing the required parameter 'client' when calling syncRegisterClient");
    }
    
    // create path and map variables
    String localVarPath = "/v4.2/sync/clients";

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

    GenericType<SyncClient> localVarReturnType = new GenericType<SyncClient>() {};
    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
}
