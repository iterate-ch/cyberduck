package ch.cyberduck.core.deepbox.io.swagger.client.api;

import ch.cyberduck.core.deepbox.io.swagger.client.ApiException;
import ch.cyberduck.core.deepbox.io.swagger.client.ApiClient;
import ch.cyberduck.core.deepbox.io.swagger.client.Configuration;
import ch.cyberduck.core.deepbox.io.swagger.client.Pair;

import javax.ws.rs.core.GenericType;

import ch.cyberduck.core.deepbox.io.swagger.client.model.Access;
import ch.cyberduck.core.deepbox.io.swagger.client.model.AccessAdd;
import ch.cyberduck.core.deepbox.io.swagger.client.model.AccessData;
import ch.cyberduck.core.deepbox.io.swagger.client.model.AccessUpdate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class AccessRestControllerApi {
  private ApiClient apiClient;

  public AccessRestControllerApi() {
    this(Configuration.getDefaultApiClient());
  }

  public AccessRestControllerApi(ApiClient apiClient) {
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
   * @param body  (required)
   * @param nodeId  (required)
   * @return List&lt;Access&gt;
   * @throws ApiException if fails to make API call
   */
  public List<Access> addAccesses(AccessAdd body, String nodeId) throws ApiException {
    Object localVarPostBody = body;
    // verify the required parameter 'body' is set
    if (body == null) {
      throw new ApiException(400, "Missing the required parameter 'body' when calling addAccesses");
    }
    // verify the required parameter 'nodeId' is set
    if (nodeId == null) {
      throw new ApiException(400, "Missing the required parameter 'nodeId' when calling addAccesses");
    }
    // create path and map variables
    String localVarPath = "/api/v1/nodes/{nodeId}/accesses"
      .replaceAll("\\{" + "nodeId" + "\\}", apiClient.escapeString(nodeId.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();



    final String[] localVarAccepts = {
      "*/*"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "token" };

    GenericType<List<Access>> localVarReturnType = new GenericType<List<Access>>() {};
    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
  /**
   * 
   * 
   * @param body  (required)
   * @param deepBoxNodeId  (required)
   * @param boxNodeId  (required)
   * @return List&lt;Access&gt;
   * @throws ApiException if fails to make API call
   */
  public List<Access> addBoxAccesses(AccessAdd body, String deepBoxNodeId, String boxNodeId) throws ApiException {
    Object localVarPostBody = body;
    // verify the required parameter 'body' is set
    if (body == null) {
      throw new ApiException(400, "Missing the required parameter 'body' when calling addBoxAccesses");
    }
    // verify the required parameter 'deepBoxNodeId' is set
    if (deepBoxNodeId == null) {
      throw new ApiException(400, "Missing the required parameter 'deepBoxNodeId' when calling addBoxAccesses");
    }
    // verify the required parameter 'boxNodeId' is set
    if (boxNodeId == null) {
      throw new ApiException(400, "Missing the required parameter 'boxNodeId' when calling addBoxAccesses");
    }
    // create path and map variables
    String localVarPath = "/api/v1/deepBoxes/{deepBoxNodeId}/boxes/{boxNodeId}/accesses"
      .replaceAll("\\{" + "deepBoxNodeId" + "\\}", apiClient.escapeString(deepBoxNodeId.toString()))
      .replaceAll("\\{" + "boxNodeId" + "\\}", apiClient.escapeString(boxNodeId.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();



    final String[] localVarAccepts = {
      "*/*"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "token" };

    GenericType<List<Access>> localVarReturnType = new GenericType<List<Access>>() {};
    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
  /**
   * 
   * 
   * @param accessId  (required)
   * @throws ApiException if fails to make API call
   */
  public void deleteAccess(String accessId) throws ApiException {
    Object localVarPostBody = null;
    // verify the required parameter 'accessId' is set
    if (accessId == null) {
      throw new ApiException(400, "Missing the required parameter 'accessId' when calling deleteAccess");
    }
    // create path and map variables
    String localVarPath = "/api/v1/accesses/{accessId}"
      .replaceAll("\\{" + "accessId" + "\\}", apiClient.escapeString(accessId.toString()));

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

    String[] localVarAuthNames = new String[] { "token" };

    apiClient.invokeAPI(localVarPath, "DELETE", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, null);
  }
  /**
   * 
   * 
   * @param nodeId  (required)
   * @return AccessData
   * @throws ApiException if fails to make API call
   */
  public AccessData listAccesses(String nodeId) throws ApiException {
    Object localVarPostBody = null;
    // verify the required parameter 'nodeId' is set
    if (nodeId == null) {
      throw new ApiException(400, "Missing the required parameter 'nodeId' when calling listAccesses");
    }
    // create path and map variables
    String localVarPath = "/api/v1/nodes/{nodeId}/accesses"
      .replaceAll("\\{" + "nodeId" + "\\}", apiClient.escapeString(nodeId.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();



    final String[] localVarAccepts = {
      "*/*"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "token" };

    GenericType<AccessData> localVarReturnType = new GenericType<AccessData>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
  /**
   * 
   * 
   * @param deepBoxNodeId  (required)
   * @param boxNodeId  (required)
   * @return AccessData
   * @throws ApiException if fails to make API call
   */
  public AccessData listAllBoxAccesses(String deepBoxNodeId, String boxNodeId) throws ApiException {
    Object localVarPostBody = null;
    // verify the required parameter 'deepBoxNodeId' is set
    if (deepBoxNodeId == null) {
      throw new ApiException(400, "Missing the required parameter 'deepBoxNodeId' when calling listAllBoxAccesses");
    }
    // verify the required parameter 'boxNodeId' is set
    if (boxNodeId == null) {
      throw new ApiException(400, "Missing the required parameter 'boxNodeId' when calling listAllBoxAccesses");
    }
    // create path and map variables
    String localVarPath = "/api/v1/deepBoxes/{deepBoxNodeId}/boxes/{boxNodeId}/accesses/all"
      .replaceAll("\\{" + "deepBoxNodeId" + "\\}", apiClient.escapeString(deepBoxNodeId.toString()))
      .replaceAll("\\{" + "boxNodeId" + "\\}", apiClient.escapeString(boxNodeId.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();



    final String[] localVarAccepts = {
      "*/*"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "token" };

    GenericType<AccessData> localVarReturnType = new GenericType<AccessData>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
  /**
   * 
   * 
   * @param body  (required)
   * @param nodeId  (required)
   * @param accessId  (required)
   * @throws ApiException if fails to make API call
   */
  public void updateAccess(AccessUpdate body, String nodeId, String accessId) throws ApiException {
    Object localVarPostBody = body;
    // verify the required parameter 'body' is set
    if (body == null) {
      throw new ApiException(400, "Missing the required parameter 'body' when calling updateAccess");
    }
    // verify the required parameter 'nodeId' is set
    if (nodeId == null) {
      throw new ApiException(400, "Missing the required parameter 'nodeId' when calling updateAccess");
    }
    // verify the required parameter 'accessId' is set
    if (accessId == null) {
      throw new ApiException(400, "Missing the required parameter 'accessId' when calling updateAccess");
    }
    // create path and map variables
    String localVarPath = "/api/v1/nodes/{nodeId}/accesses/{accessId}"
      .replaceAll("\\{" + "nodeId" + "\\}", apiClient.escapeString(nodeId.toString()))
      .replaceAll("\\{" + "accessId" + "\\}", apiClient.escapeString(accessId.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();



    final String[] localVarAccepts = {
      
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "token" };

    apiClient.invokeAPI(localVarPath, "PUT", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, null);
  }
  /**
   * 
   * 
   * @param body  (required)
   * @param deepBoxNodeId  (required)
   * @param boxNodeId  (required)
   * @param accessId  (required)
   * @throws ApiException if fails to make API call
   */
  public void updateBoxAccess(AccessUpdate body, String deepBoxNodeId, String boxNodeId, String accessId) throws ApiException {
    Object localVarPostBody = body;
    // verify the required parameter 'body' is set
    if (body == null) {
      throw new ApiException(400, "Missing the required parameter 'body' when calling updateBoxAccess");
    }
    // verify the required parameter 'deepBoxNodeId' is set
    if (deepBoxNodeId == null) {
      throw new ApiException(400, "Missing the required parameter 'deepBoxNodeId' when calling updateBoxAccess");
    }
    // verify the required parameter 'boxNodeId' is set
    if (boxNodeId == null) {
      throw new ApiException(400, "Missing the required parameter 'boxNodeId' when calling updateBoxAccess");
    }
    // verify the required parameter 'accessId' is set
    if (accessId == null) {
      throw new ApiException(400, "Missing the required parameter 'accessId' when calling updateBoxAccess");
    }
    // create path and map variables
    String localVarPath = "/api/v1/deepBoxes/{deepBoxNodeId}/boxes/{boxNodeId}/accesses/{accessId}"
      .replaceAll("\\{" + "deepBoxNodeId" + "\\}", apiClient.escapeString(deepBoxNodeId.toString()))
      .replaceAll("\\{" + "boxNodeId" + "\\}", apiClient.escapeString(boxNodeId.toString()))
      .replaceAll("\\{" + "accessId" + "\\}", apiClient.escapeString(accessId.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();



    final String[] localVarAccepts = {
      
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "token" };

    apiClient.invokeAPI(localVarPath, "PUT", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, null);
  }
}
