package ch.cyberduck.core.deepbox.io.swagger.client.api;

import ch.cyberduck.core.deepbox.io.swagger.client.ApiException;
import ch.cyberduck.core.deepbox.io.swagger.client.ApiClient;
import ch.cyberduck.core.deepbox.io.swagger.client.Configuration;
import ch.cyberduck.core.deepbox.io.swagger.client.Pair;

import javax.ws.rs.core.GenericType;

import java.io.File;
import java.util.UUID;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class MetaRestControllerApi {
  private ApiClient apiClient;

  public MetaRestControllerApi() {
    this(Configuration.getDefaultApiClient());
  }

  public MetaRestControllerApi(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  public ApiClient getApiClient() {
    return apiClient;
  }

  public void setApiClient(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  /**
   * 
   * 
   * @param deepBoxAppKey  (required)
   * @param nodeId  (required)
   * @param name  (required)
   * @return File
   * @throws ApiException if fails to make API call
   * @deprecated
   */
  @Deprecated
  public File deprecatedReadNodeBin(UUID deepBoxAppKey, UUID nodeId, String name) throws ApiException {
    Object localVarPostBody = null;
    // verify the required parameter 'deepBoxAppKey' is set
    if (deepBoxAppKey == null) {
      throw new ApiException(400, "Missing the required parameter 'deepBoxAppKey' when calling deprecatedReadNodeBin");
    }
    // verify the required parameter 'nodeId' is set
    if (nodeId == null) {
      throw new ApiException(400, "Missing the required parameter 'nodeId' when calling deprecatedReadNodeBin");
    }
    // verify the required parameter 'name' is set
    if (name == null) {
      throw new ApiException(400, "Missing the required parameter 'name' when calling deprecatedReadNodeBin");
    }
    // create path and map variables
    String localVarPath = "/api/v1/nodes/{nodeId}/bin/{name}"
      .replaceAll("\\{" + "nodeId" + "\\}", apiClient.escapeString(nodeId.toString()))
      .replaceAll("\\{" + "name" + "\\}", apiClient.escapeString(name.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


    if (deepBoxAppKey != null)
      localVarHeaderParams.put("DeepBox-App-Key", apiClient.parameterToString(deepBoxAppKey));

    final String[] localVarAccepts = {
      "*/*"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "token" };

    GenericType<File> localVarReturnType = new GenericType<File>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
  /**
   * 
   * 
   * @param deepBoxAppKey  (required)
   * @param nodeId  (required)
   * @param name  (required)
   * @return String
   * @throws ApiException if fails to make API call
   */
  public String readNodeBin(UUID deepBoxAppKey, UUID nodeId, String name) throws ApiException {
    Object localVarPostBody = null;
    // verify the required parameter 'deepBoxAppKey' is set
    if (deepBoxAppKey == null) {
      throw new ApiException(400, "Missing the required parameter 'deepBoxAppKey' when calling readNodeBin");
    }
    // verify the required parameter 'nodeId' is set
    if (nodeId == null) {
      throw new ApiException(400, "Missing the required parameter 'nodeId' when calling readNodeBin");
    }
    // verify the required parameter 'name' is set
    if (name == null) {
      throw new ApiException(400, "Missing the required parameter 'name' when calling readNodeBin");
    }
    // create path and map variables
    String localVarPath = "/api/v1/nodes/{nodeId}/bin-url/{name}"
      .replaceAll("\\{" + "nodeId" + "\\}", apiClient.escapeString(nodeId.toString()))
      .replaceAll("\\{" + "name" + "\\}", apiClient.escapeString(name.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


    if (deepBoxAppKey != null)
      localVarHeaderParams.put("DeepBox-App-Key", apiClient.parameterToString(deepBoxAppKey));

    final String[] localVarAccepts = {
      "*/*"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "token" };

    GenericType<String> localVarReturnType = new GenericType<String>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
  /**
   * 
   * 
   * @param deepBoxAppKey  (required)
   * @param nodeId  (required)
   * @param keys  (required)
   * @return Map&lt;String, String&gt;
   * @throws ApiException if fails to make API call
   */
  public Map<String, String> readNodeMeta(UUID deepBoxAppKey, UUID nodeId, List<String> keys) throws ApiException {
    Object localVarPostBody = null;
    // verify the required parameter 'deepBoxAppKey' is set
    if (deepBoxAppKey == null) {
      throw new ApiException(400, "Missing the required parameter 'deepBoxAppKey' when calling readNodeMeta");
    }
    // verify the required parameter 'nodeId' is set
    if (nodeId == null) {
      throw new ApiException(400, "Missing the required parameter 'nodeId' when calling readNodeMeta");
    }
    // verify the required parameter 'keys' is set
    if (keys == null) {
      throw new ApiException(400, "Missing the required parameter 'keys' when calling readNodeMeta");
    }
    // create path and map variables
    String localVarPath = "/api/v1/nodes/{nodeId}/meta"
      .replaceAll("\\{" + "nodeId" + "\\}", apiClient.escapeString(nodeId.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    localVarQueryParams.addAll(apiClient.parameterToPairs("multi", "keys", keys));

    if (deepBoxAppKey != null)
      localVarHeaderParams.put("DeepBox-App-Key", apiClient.parameterToString(deepBoxAppKey));

    final String[] localVarAccepts = {
      "*/*"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "token" };

    GenericType<Map<String, String>> localVarReturnType = new GenericType<Map<String, String>>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
  /**
   * 
   * 
   * @param deepBoxAppKey  (required)
   * @param nodeId  (required)
   * @param files  (required)
   * @throws ApiException if fails to make API call
   */
  public void writeNodeBin(UUID deepBoxAppKey, UUID nodeId, List<File> files) throws ApiException {
    Object localVarPostBody = null;
    // verify the required parameter 'deepBoxAppKey' is set
    if (deepBoxAppKey == null) {
      throw new ApiException(400, "Missing the required parameter 'deepBoxAppKey' when calling writeNodeBin");
    }
    // verify the required parameter 'nodeId' is set
    if (nodeId == null) {
      throw new ApiException(400, "Missing the required parameter 'nodeId' when calling writeNodeBin");
    }
    // verify the required parameter 'files' is set
    if (files == null) {
      throw new ApiException(400, "Missing the required parameter 'files' when calling writeNodeBin");
    }
    // create path and map variables
    String localVarPath = "/api/v1/nodes/{nodeId}/bin"
      .replaceAll("\\{" + "nodeId" + "\\}", apiClient.escapeString(nodeId.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    localVarQueryParams.addAll(apiClient.parameterToPairs("multi", "files", files));

    if (deepBoxAppKey != null)
      localVarHeaderParams.put("DeepBox-App-Key", apiClient.parameterToString(deepBoxAppKey));

    final String[] localVarAccepts = {
      
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "token" };

    apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, null);
  }
  /**
   * 
   * Set or update meta values
   * @param body  (required)
   * @param deepBoxAppKey  (required)
   * @param nodeId  (required)
   * @throws ApiException if fails to make API call
   */
  public void writeNodeMeta(Map<String, String> body, UUID deepBoxAppKey, UUID nodeId) throws ApiException {
    Object localVarPostBody = body;
    // verify the required parameter 'body' is set
    if (body == null) {
      throw new ApiException(400, "Missing the required parameter 'body' when calling writeNodeMeta");
    }
    // verify the required parameter 'deepBoxAppKey' is set
    if (deepBoxAppKey == null) {
      throw new ApiException(400, "Missing the required parameter 'deepBoxAppKey' when calling writeNodeMeta");
    }
    // verify the required parameter 'nodeId' is set
    if (nodeId == null) {
      throw new ApiException(400, "Missing the required parameter 'nodeId' when calling writeNodeMeta");
    }
    // create path and map variables
    String localVarPath = "/api/v1/nodes/{nodeId}/meta"
      .replaceAll("\\{" + "nodeId" + "\\}", apiClient.escapeString(nodeId.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


    if (deepBoxAppKey != null)
      localVarHeaderParams.put("DeepBox-App-Key", apiClient.parameterToString(deepBoxAppKey));

    final String[] localVarAccepts = {
      
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "token" };

    apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, null);
  }
}
