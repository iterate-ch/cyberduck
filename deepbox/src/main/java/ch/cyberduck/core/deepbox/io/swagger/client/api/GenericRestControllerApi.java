package ch.cyberduck.core.deepbox.io.swagger.client.api;

import ch.cyberduck.core.deepbox.io.swagger.client.ApiException;
import ch.cyberduck.core.deepbox.io.swagger.client.ApiClient;
import ch.cyberduck.core.deepbox.io.swagger.client.Configuration;
import ch.cyberduck.core.deepbox.io.swagger.client.Pair;

import javax.ws.rs.core.GenericType;

import ch.cyberduck.core.deepbox.io.swagger.client.model.GenericFolder;
import ch.cyberduck.core.deepbox.io.swagger.client.model.GenericFolderConnect;
import ch.cyberduck.core.deepbox.io.swagger.client.model.GenericsGenericIdBody;
import ch.cyberduck.core.deepbox.io.swagger.client.model.Node;
import ch.cyberduck.core.deepbox.io.swagger.client.model.NodeContent;
import java.util.UUID;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class GenericRestControllerApi {
  private ApiClient apiClient;

  public GenericRestControllerApi() {
    this(Configuration.getDefaultApiClient());
  }

  public GenericRestControllerApi(ApiClient apiClient) {
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
   * @param deepBoxNodeId  (required)
   * @param boxNodeId  (required)
   * @param genericId  (required)
   * @param body  (optional)
   * @return List&lt;Node&gt;
   * @throws ApiException if fails to make API call
   */
  public List<Node> addContentGenerics(UUID deepBoxNodeId, UUID boxNodeId, String genericId, GenericsGenericIdBody body) throws ApiException {
    Object localVarPostBody = body;
    // verify the required parameter 'deepBoxNodeId' is set
    if (deepBoxNodeId == null) {
      throw new ApiException(400, "Missing the required parameter 'deepBoxNodeId' when calling addContentGenerics");
    }
    // verify the required parameter 'boxNodeId' is set
    if (boxNodeId == null) {
      throw new ApiException(400, "Missing the required parameter 'boxNodeId' when calling addContentGenerics");
    }
    // verify the required parameter 'genericId' is set
    if (genericId == null) {
      throw new ApiException(400, "Missing the required parameter 'genericId' when calling addContentGenerics");
    }
    // create path and map variables
    String localVarPath = "/api/v1/deepBoxes/{deepBoxNodeId}/boxes/{boxNodeId}/generics/{genericId}"
      .replaceAll("\\{" + "deepBoxNodeId" + "\\}", apiClient.escapeString(deepBoxNodeId.toString()))
      .replaceAll("\\{" + "boxNodeId" + "\\}", apiClient.escapeString(boxNodeId.toString()))
      .replaceAll("\\{" + "genericId" + "\\}", apiClient.escapeString(genericId.toString()));

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

    GenericType<List<Node>> localVarReturnType = new GenericType<List<Node>>() {};
    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
  /**
   * 
   * Connection to a generic folders. In case of a already existing gerenic folder, it will be disconnected and connected to the new one.
   * @param body  (required)
   * @param deepBoxNodeId  (required)
   * @param boxNodeId  (required)
   * @throws ApiException if fails to make API call
   */
  public void connectGenericFolders(GenericFolderConnect body, UUID deepBoxNodeId, UUID boxNodeId) throws ApiException {
    Object localVarPostBody = body;
    // verify the required parameter 'body' is set
    if (body == null) {
      throw new ApiException(400, "Missing the required parameter 'body' when calling connectGenericFolders");
    }
    // verify the required parameter 'deepBoxNodeId' is set
    if (deepBoxNodeId == null) {
      throw new ApiException(400, "Missing the required parameter 'deepBoxNodeId' when calling connectGenericFolders");
    }
    // verify the required parameter 'boxNodeId' is set
    if (boxNodeId == null) {
      throw new ApiException(400, "Missing the required parameter 'boxNodeId' when calling connectGenericFolders");
    }
    // create path and map variables
    String localVarPath = "/api/v1/admin/deepBoxes/{deepBoxNodeId}/boxes/{boxNodeId}/generics"
      .replaceAll("\\{" + "deepBoxNodeId" + "\\}", apiClient.escapeString(deepBoxNodeId.toString()))
      .replaceAll("\\{" + "boxNodeId" + "\\}", apiClient.escapeString(boxNodeId.toString()));

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

    apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, null);
  }
  /**
   * 
   * Disconnects a generic folder. The folder will not be deleted.
   * @param deepBoxNodeId  (required)
   * @param boxNodeId  (required)
   * @param genericId  (required)
   * @throws ApiException if fails to make API call
   */
  public void disconnectGenericFolder(UUID deepBoxNodeId, UUID boxNodeId, String genericId) throws ApiException {
    Object localVarPostBody = null;
    // verify the required parameter 'deepBoxNodeId' is set
    if (deepBoxNodeId == null) {
      throw new ApiException(400, "Missing the required parameter 'deepBoxNodeId' when calling disconnectGenericFolder");
    }
    // verify the required parameter 'boxNodeId' is set
    if (boxNodeId == null) {
      throw new ApiException(400, "Missing the required parameter 'boxNodeId' when calling disconnectGenericFolder");
    }
    // verify the required parameter 'genericId' is set
    if (genericId == null) {
      throw new ApiException(400, "Missing the required parameter 'genericId' when calling disconnectGenericFolder");
    }
    // create path and map variables
    String localVarPath = "/api/v1/admin/deepBoxes/{deepBoxNodeId}/boxes/{boxNodeId}/generics/{genericId}"
      .replaceAll("\\{" + "deepBoxNodeId" + "\\}", apiClient.escapeString(deepBoxNodeId.toString()))
      .replaceAll("\\{" + "boxNodeId" + "\\}", apiClient.escapeString(boxNodeId.toString()))
      .replaceAll("\\{" + "genericId" + "\\}", apiClient.escapeString(genericId.toString()));

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
   * @param deepBoxNodeId  (required)
   * @param boxNodeId  (required)
   * @return List&lt;GenericFolder&gt;
   * @throws ApiException if fails to make API call
   */
  public List<GenericFolder> getGenericFolders(UUID deepBoxNodeId, UUID boxNodeId) throws ApiException {
    Object localVarPostBody = null;
    // verify the required parameter 'deepBoxNodeId' is set
    if (deepBoxNodeId == null) {
      throw new ApiException(400, "Missing the required parameter 'deepBoxNodeId' when calling getGenericFolders");
    }
    // verify the required parameter 'boxNodeId' is set
    if (boxNodeId == null) {
      throw new ApiException(400, "Missing the required parameter 'boxNodeId' when calling getGenericFolders");
    }
    // create path and map variables
    String localVarPath = "/api/v1/deepBoxes/{deepBoxNodeId}/boxes/{boxNodeId}/generics"
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

    GenericType<List<GenericFolder>> localVarReturnType = new GenericType<List<GenericFolder>>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
  /**
   * 
   * 
   * @param deepBoxNodeId  (required)
   * @param boxNodeId  (required)
   * @param genericId  (required)
   * @param offset  (optional, default to 0)
   * @param limit  (optional, default to 50)
   * @param order displayName|modifiedTime|fileSize [asc|desc] (optional, default to displayName)
   * @return NodeContent
   * @throws ApiException if fails to make API call
   */
  public NodeContent listFilesGenerics(UUID deepBoxNodeId, UUID boxNodeId, String genericId, Integer offset, Integer limit, String order) throws ApiException {
    Object localVarPostBody = null;
    // verify the required parameter 'deepBoxNodeId' is set
    if (deepBoxNodeId == null) {
      throw new ApiException(400, "Missing the required parameter 'deepBoxNodeId' when calling listFilesGenerics");
    }
    // verify the required parameter 'boxNodeId' is set
    if (boxNodeId == null) {
      throw new ApiException(400, "Missing the required parameter 'boxNodeId' when calling listFilesGenerics");
    }
    // verify the required parameter 'genericId' is set
    if (genericId == null) {
      throw new ApiException(400, "Missing the required parameter 'genericId' when calling listFilesGenerics");
    }
    // create path and map variables
    String localVarPath = "/api/v1/deepBoxes/{deepBoxNodeId}/boxes/{boxNodeId}/generics/{genericId}"
      .replaceAll("\\{" + "deepBoxNodeId" + "\\}", apiClient.escapeString(deepBoxNodeId.toString()))
      .replaceAll("\\{" + "boxNodeId" + "\\}", apiClient.escapeString(boxNodeId.toString()))
      .replaceAll("\\{" + "genericId" + "\\}", apiClient.escapeString(genericId.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    localVarQueryParams.addAll(apiClient.parameterToPairs("", "offset", offset));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "limit", limit));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "order", order));


    final String[] localVarAccepts = {
      "*/*"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "token" };

    GenericType<NodeContent> localVarReturnType = new GenericType<NodeContent>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
  /**
   * 
   * 
   * @param deepBoxNodeId  (required)
   * @param boxNodeId  (required)
   * @return List&lt;GenericFolder&gt;
   * @throws ApiException if fails to make API call
   */
  public List<GenericFolder> listGenericFolders(UUID deepBoxNodeId, UUID boxNodeId) throws ApiException {
    Object localVarPostBody = null;
    // verify the required parameter 'deepBoxNodeId' is set
    if (deepBoxNodeId == null) {
      throw new ApiException(400, "Missing the required parameter 'deepBoxNodeId' when calling listGenericFolders");
    }
    // verify the required parameter 'boxNodeId' is set
    if (boxNodeId == null) {
      throw new ApiException(400, "Missing the required parameter 'boxNodeId' when calling listGenericFolders");
    }
    // create path and map variables
    String localVarPath = "/api/v1/admin/deepBoxes/{deepBoxNodeId}/boxes/{boxNodeId}/generics"
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

    GenericType<List<GenericFolder>> localVarReturnType = new GenericType<List<GenericFolder>>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
}
