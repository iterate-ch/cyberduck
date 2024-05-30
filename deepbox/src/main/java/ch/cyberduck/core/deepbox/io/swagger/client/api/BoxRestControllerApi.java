package ch.cyberduck.core.deepbox.io.swagger.client.api;

import ch.cyberduck.core.deepbox.io.swagger.client.ApiException;
import ch.cyberduck.core.deepbox.io.swagger.client.ApiClient;
import ch.cyberduck.core.deepbox.io.swagger.client.Configuration;
import ch.cyberduck.core.deepbox.io.swagger.client.Pair;

import javax.ws.rs.core.GenericType;

import ch.cyberduck.core.deepbox.io.swagger.client.model.Box;
import ch.cyberduck.core.deepbox.io.swagger.client.model.BoxNodeIdFilesBody;
import ch.cyberduck.core.deepbox.io.swagger.client.model.Boxes;
import ch.cyberduck.core.deepbox.io.swagger.client.model.DeepBox;
import ch.cyberduck.core.deepbox.io.swagger.client.model.DeepBoxes;
import java.io.File;
import ch.cyberduck.core.deepbox.io.swagger.client.model.FilesNodeIdBody;
import ch.cyberduck.core.deepbox.io.swagger.client.model.Node;
import ch.cyberduck.core.deepbox.io.swagger.client.model.NodeContent;
import java.util.UUID;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class BoxRestControllerApi {
  private ApiClient apiClient;

  public BoxRestControllerApi() {
    this(Configuration.getDefaultApiClient());
  }

  public BoxRestControllerApi(ApiClient apiClient) {
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
   * @param body  (optional)
   * @return List&lt;Node&gt;
   * @throws ApiException if fails to make API call
   */
  public List<Node> addContent(UUID deepBoxNodeId, UUID boxNodeId, BoxNodeIdFilesBody body) throws ApiException {
    Object localVarPostBody = body;
    // verify the required parameter 'deepBoxNodeId' is set
    if (deepBoxNodeId == null) {
      throw new ApiException(400, "Missing the required parameter 'deepBoxNodeId' when calling addContent");
    }
    // verify the required parameter 'boxNodeId' is set
    if (boxNodeId == null) {
      throw new ApiException(400, "Missing the required parameter 'boxNodeId' when calling addContent");
    }
    // create path and map variables
    String localVarPath = "/api/v1/deepBoxes/{deepBoxNodeId}/boxes/{boxNodeId}/files"
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

    GenericType<List<Node>> localVarReturnType = new GenericType<List<Node>>() {};
    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
  /**
   * 
   * 
   * @param deepBoxNodeId  (required)
   * @param boxNodeId  (required)
   * @param nodeId  (required)
   * @param body  (optional)
   * @return List&lt;Node&gt;
   * @throws ApiException if fails to make API call
   */
  public List<Node> addContent1(UUID deepBoxNodeId, UUID boxNodeId, UUID nodeId, FilesNodeIdBody body) throws ApiException {
    Object localVarPostBody = body;
    // verify the required parameter 'deepBoxNodeId' is set
    if (deepBoxNodeId == null) {
      throw new ApiException(400, "Missing the required parameter 'deepBoxNodeId' when calling addContent1");
    }
    // verify the required parameter 'boxNodeId' is set
    if (boxNodeId == null) {
      throw new ApiException(400, "Missing the required parameter 'boxNodeId' when calling addContent1");
    }
    // verify the required parameter 'nodeId' is set
    if (nodeId == null) {
      throw new ApiException(400, "Missing the required parameter 'nodeId' when calling addContent1");
    }
    // create path and map variables
    String localVarPath = "/api/v1/deepBoxes/{deepBoxNodeId}/boxes/{boxNodeId}/files/{nodeId}"
      .replaceAll("\\{" + "deepBoxNodeId" + "\\}", apiClient.escapeString(deepBoxNodeId.toString()))
      .replaceAll("\\{" + "boxNodeId" + "\\}", apiClient.escapeString(boxNodeId.toString()))
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

    GenericType<List<Node>> localVarReturnType = new GenericType<List<Node>>() {};
    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
  /**
   * 
   * 
   * @param files  (required)
   * @param fileAttributes  (required)
   * @param fileTags  (required)
   * @param additionalAnalyzeData  (required)
   * @param deepBoxNodeId  (required)
   * @param boxNodeId  (required)
   * @return List&lt;Node&gt;
   * @throws ApiException if fails to make API call
   */
  public List<Node> addQueue(List<File> files, String fileAttributes, String fileTags, List<File> additionalAnalyzeData, UUID deepBoxNodeId, UUID boxNodeId) throws ApiException {
    Object localVarPostBody = null;
    // verify the required parameter 'files' is set
    if (files == null) {
      throw new ApiException(400, "Missing the required parameter 'files' when calling addQueue");
    }
    // verify the required parameter 'fileAttributes' is set
    if (fileAttributes == null) {
      throw new ApiException(400, "Missing the required parameter 'fileAttributes' when calling addQueue");
    }
    // verify the required parameter 'fileTags' is set
    if (fileTags == null) {
      throw new ApiException(400, "Missing the required parameter 'fileTags' when calling addQueue");
    }
    // verify the required parameter 'additionalAnalyzeData' is set
    if (additionalAnalyzeData == null) {
      throw new ApiException(400, "Missing the required parameter 'additionalAnalyzeData' when calling addQueue");
    }
    // verify the required parameter 'deepBoxNodeId' is set
    if (deepBoxNodeId == null) {
      throw new ApiException(400, "Missing the required parameter 'deepBoxNodeId' when calling addQueue");
    }
    // verify the required parameter 'boxNodeId' is set
    if (boxNodeId == null) {
      throw new ApiException(400, "Missing the required parameter 'boxNodeId' when calling addQueue");
    }
    // create path and map variables
    String localVarPath = "/api/v1/deepBoxes/{deepBoxNodeId}/boxes/{boxNodeId}/queue"
      .replaceAll("\\{" + "deepBoxNodeId" + "\\}", apiClient.escapeString(deepBoxNodeId.toString()))
      .replaceAll("\\{" + "boxNodeId" + "\\}", apiClient.escapeString(boxNodeId.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


    if (files != null)
      localVarFormParams.put("files", files);
    if (fileAttributes != null)
      localVarFormParams.put("fileAttributes", fileAttributes);
    if (fileTags != null)
      localVarFormParams.put("fileTags", fileTags);
    if (additionalAnalyzeData != null)
      localVarFormParams.put("additionalAnalyzeData", additionalAnalyzeData);

    final String[] localVarAccepts = {
      "*/*"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "multipart/form-data"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "token" };

    GenericType<List<Node>> localVarReturnType = new GenericType<List<Node>>() {};
    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
  /**
   * 
   * 
   * @param deepBoxNodeId  (required)
   * @param boxNodeId  (required)
   * @throws ApiException if fails to make API call
   */
  public void emptyTrash(UUID deepBoxNodeId, UUID boxNodeId) throws ApiException {
    Object localVarPostBody = null;
    // verify the required parameter 'deepBoxNodeId' is set
    if (deepBoxNodeId == null) {
      throw new ApiException(400, "Missing the required parameter 'deepBoxNodeId' when calling emptyTrash");
    }
    // verify the required parameter 'boxNodeId' is set
    if (boxNodeId == null) {
      throw new ApiException(400, "Missing the required parameter 'boxNodeId' when calling emptyTrash");
    }
    // create path and map variables
    String localVarPath = "/api/v1/deepBoxes/{deepBoxNodeId}/boxes/{boxNodeId}/trash"
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
   * @return Box
   * @throws ApiException if fails to make API call
   */
  public Box getBox(UUID deepBoxNodeId, UUID boxNodeId) throws ApiException {
    Object localVarPostBody = null;
    // verify the required parameter 'deepBoxNodeId' is set
    if (deepBoxNodeId == null) {
      throw new ApiException(400, "Missing the required parameter 'deepBoxNodeId' when calling getBox");
    }
    // verify the required parameter 'boxNodeId' is set
    if (boxNodeId == null) {
      throw new ApiException(400, "Missing the required parameter 'boxNodeId' when calling getBox");
    }
    // create path and map variables
    String localVarPath = "/api/v1/deepBoxes/{deepBoxNodeId}/boxes/{boxNodeId}"
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

    GenericType<Box> localVarReturnType = new GenericType<Box>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
  /**
   * 
   * 
   * @param deepBoxNodeId  (required)
   * @return DeepBox
   * @throws ApiException if fails to make API call
   */
  public DeepBox getDeepBox(UUID deepBoxNodeId) throws ApiException {
    Object localVarPostBody = null;
    // verify the required parameter 'deepBoxNodeId' is set
    if (deepBoxNodeId == null) {
      throw new ApiException(400, "Missing the required parameter 'deepBoxNodeId' when calling getDeepBox");
    }
    // create path and map variables
    String localVarPath = "/api/v1/deepBoxes/{deepBoxNodeId}"
      .replaceAll("\\{" + "deepBoxNodeId" + "\\}", apiClient.escapeString(deepBoxNodeId.toString()));

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

    GenericType<DeepBox> localVarReturnType = new GenericType<DeepBox>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
  /**
   * 
   * 
   * @param deepBoxNodeId  (required)
   * @param offset  (optional, default to 0)
   * @param limit  (optional, default to 50)
   * @param order name [asc|desc] (optional, default to name)
   * @param q Filter (optional)
   * @return Boxes
   * @throws ApiException if fails to make API call
   */
  public Boxes listBoxes(UUID deepBoxNodeId, Integer offset, Integer limit, String order, String q) throws ApiException {
    Object localVarPostBody = null;
    // verify the required parameter 'deepBoxNodeId' is set
    if (deepBoxNodeId == null) {
      throw new ApiException(400, "Missing the required parameter 'deepBoxNodeId' when calling listBoxes");
    }
    // create path and map variables
    String localVarPath = "/api/v1/deepBoxes/{deepBoxNodeId}/boxes"
      .replaceAll("\\{" + "deepBoxNodeId" + "\\}", apiClient.escapeString(deepBoxNodeId.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    localVarQueryParams.addAll(apiClient.parameterToPairs("", "offset", offset));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "limit", limit));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "order", order));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "q", q));


    final String[] localVarAccepts = {
      "*/*"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "token" };

    GenericType<Boxes> localVarReturnType = new GenericType<Boxes>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
  /**
   * 
   * 
   * @param offset  (optional, default to 0)
   * @param limit  (optional, default to 50)
   * @param order name [asc|desc] (optional, default to name)
   * @param filterBoxType  (optional)
   * @return DeepBoxes
   * @throws ApiException if fails to make API call
   */
  public DeepBoxes listDeepBoxes(Integer offset, Integer limit, String order, String filterBoxType) throws ApiException {
    Object localVarPostBody = null;
    // create path and map variables
    String localVarPath = "/api/v1/deepBoxes";

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    localVarQueryParams.addAll(apiClient.parameterToPairs("", "offset", offset));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "limit", limit));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "order", order));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "filterBoxType", filterBoxType));


    final String[] localVarAccepts = {
      "*/*"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "token" };

    GenericType<DeepBoxes> localVarReturnType = new GenericType<DeepBoxes>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
  /**
   * 
   * 
   * @param deepBoxNodeId  (required)
   * @param boxNodeId  (required)
   * @param offset  (optional, default to 0)
   * @param limit  (optional, default to 50)
   * @param order displayName|modifiedTime|fileSize [asc|desc] (optional, default to displayName)
   * @return NodeContent
   * @throws ApiException if fails to make API call
   */
  public NodeContent listFiles(UUID deepBoxNodeId, UUID boxNodeId, Integer offset, Integer limit, String order) throws ApiException {
    Object localVarPostBody = null;
    // verify the required parameter 'deepBoxNodeId' is set
    if (deepBoxNodeId == null) {
      throw new ApiException(400, "Missing the required parameter 'deepBoxNodeId' when calling listFiles");
    }
    // verify the required parameter 'boxNodeId' is set
    if (boxNodeId == null) {
      throw new ApiException(400, "Missing the required parameter 'boxNodeId' when calling listFiles");
    }
    // create path and map variables
    String localVarPath = "/api/v1/deepBoxes/{deepBoxNodeId}/boxes/{boxNodeId}/files"
      .replaceAll("\\{" + "deepBoxNodeId" + "\\}", apiClient.escapeString(deepBoxNodeId.toString()))
      .replaceAll("\\{" + "boxNodeId" + "\\}", apiClient.escapeString(boxNodeId.toString()));

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
   * @param nodeId  (required)
   * @param offset  (optional, default to 0)
   * @param limit  (optional, default to 50)
   * @param order displayName|modifiedTime|fileSize [asc|desc] (optional, default to displayName)
   * @return NodeContent
   * @throws ApiException if fails to make API call
   */
  public NodeContent listFiles1(UUID deepBoxNodeId, UUID boxNodeId, UUID nodeId, Integer offset, Integer limit, String order) throws ApiException {
    Object localVarPostBody = null;
    // verify the required parameter 'deepBoxNodeId' is set
    if (deepBoxNodeId == null) {
      throw new ApiException(400, "Missing the required parameter 'deepBoxNodeId' when calling listFiles1");
    }
    // verify the required parameter 'boxNodeId' is set
    if (boxNodeId == null) {
      throw new ApiException(400, "Missing the required parameter 'boxNodeId' when calling listFiles1");
    }
    // verify the required parameter 'nodeId' is set
    if (nodeId == null) {
      throw new ApiException(400, "Missing the required parameter 'nodeId' when calling listFiles1");
    }
    // create path and map variables
    String localVarPath = "/api/v1/deepBoxes/{deepBoxNodeId}/boxes/{boxNodeId}/files/{nodeId}"
      .replaceAll("\\{" + "deepBoxNodeId" + "\\}", apiClient.escapeString(deepBoxNodeId.toString()))
      .replaceAll("\\{" + "boxNodeId" + "\\}", apiClient.escapeString(boxNodeId.toString()))
      .replaceAll("\\{" + "nodeId" + "\\}", apiClient.escapeString(nodeId.toString()));

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
   * @param t Tags (optional)
   * @param offset  (optional, default to 0)
   * @param limit  (optional, default to 50)
   * @param order displayName|modifiedTime|fileSize [asc|desc] (optional, default to modifiedTime desc)
   * @return NodeContent
   * @throws ApiException if fails to make API call
   */
  public NodeContent listQueue(UUID deepBoxNodeId, UUID boxNodeId, List<String> t, Integer offset, Integer limit, String order) throws ApiException {
    Object localVarPostBody = null;
    // verify the required parameter 'deepBoxNodeId' is set
    if (deepBoxNodeId == null) {
      throw new ApiException(400, "Missing the required parameter 'deepBoxNodeId' when calling listQueue");
    }
    // verify the required parameter 'boxNodeId' is set
    if (boxNodeId == null) {
      throw new ApiException(400, "Missing the required parameter 'boxNodeId' when calling listQueue");
    }
    // create path and map variables
    String localVarPath = "/api/v1/deepBoxes/{deepBoxNodeId}/boxes/{boxNodeId}/queue"
      .replaceAll("\\{" + "deepBoxNodeId" + "\\}", apiClient.escapeString(deepBoxNodeId.toString()))
      .replaceAll("\\{" + "boxNodeId" + "\\}", apiClient.escapeString(boxNodeId.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    localVarQueryParams.addAll(apiClient.parameterToPairs("multi", "t", t));
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
   * @param offset  (optional, default to 0)
   * @param limit  (optional, default to 50)
   * @param order displayName|modifiedTime|deletedTime|fileSize [asc|desc] (optional, default to displayName)
   * @return NodeContent
   * @throws ApiException if fails to make API call
   */
  public NodeContent listTrash(UUID deepBoxNodeId, UUID boxNodeId, Integer offset, Integer limit, String order) throws ApiException {
    Object localVarPostBody = null;
    // verify the required parameter 'deepBoxNodeId' is set
    if (deepBoxNodeId == null) {
      throw new ApiException(400, "Missing the required parameter 'deepBoxNodeId' when calling listTrash");
    }
    // verify the required parameter 'boxNodeId' is set
    if (boxNodeId == null) {
      throw new ApiException(400, "Missing the required parameter 'boxNodeId' when calling listTrash");
    }
    // create path and map variables
    String localVarPath = "/api/v1/deepBoxes/{deepBoxNodeId}/boxes/{boxNodeId}/trash"
      .replaceAll("\\{" + "deepBoxNodeId" + "\\}", apiClient.escapeString(deepBoxNodeId.toString()))
      .replaceAll("\\{" + "boxNodeId" + "\\}", apiClient.escapeString(boxNodeId.toString()));

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
   * @param nodeId  (required)
   * @param offset  (optional, default to 0)
   * @param limit  (optional, default to 50)
   * @param order displayName|modifiedTime|fileSize [asc|desc] (optional, default to displayName)
   * @return NodeContent
   * @throws ApiException if fails to make API call
   */
  public NodeContent listTrash1(UUID deepBoxNodeId, UUID boxNodeId, UUID nodeId, Integer offset, Integer limit, String order) throws ApiException {
    Object localVarPostBody = null;
    // verify the required parameter 'deepBoxNodeId' is set
    if (deepBoxNodeId == null) {
      throw new ApiException(400, "Missing the required parameter 'deepBoxNodeId' when calling listTrash1");
    }
    // verify the required parameter 'boxNodeId' is set
    if (boxNodeId == null) {
      throw new ApiException(400, "Missing the required parameter 'boxNodeId' when calling listTrash1");
    }
    // verify the required parameter 'nodeId' is set
    if (nodeId == null) {
      throw new ApiException(400, "Missing the required parameter 'nodeId' when calling listTrash1");
    }
    // create path and map variables
    String localVarPath = "/api/v1/deepBoxes/{deepBoxNodeId}/boxes/{boxNodeId}/trash/{nodeId}"
      .replaceAll("\\{" + "deepBoxNodeId" + "\\}", apiClient.escapeString(deepBoxNodeId.toString()))
      .replaceAll("\\{" + "boxNodeId" + "\\}", apiClient.escapeString(boxNodeId.toString()))
      .replaceAll("\\{" + "nodeId" + "\\}", apiClient.escapeString(nodeId.toString()));

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
}
