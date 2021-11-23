package ch.cyberduck.core.brick.io.swagger.client.api;

import ch.cyberduck.core.brick.io.swagger.client.ApiException;
import ch.cyberduck.core.brick.io.swagger.client.ApiClient;
import ch.cyberduck.core.brick.io.swagger.client.Configuration;
import ch.cyberduck.core.brick.io.swagger.client.Pair;

import javax.ws.rs.core.GenericType;

import ch.cyberduck.core.brick.io.swagger.client.model.FileEntity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.JavaClientCodegen", date = "2021-07-25T22:25:43.390877+02:00[Europe/Paris]")public class FoldersApi {
  private ApiClient apiClient;

  public FoldersApi() {
    this(Configuration.getDefaultApiClient());
  }

  public FoldersApi(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  public ApiClient getApiClient() {
    return apiClient;
  }

  public void setApiClient(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  /**
   * List Folders by path
   * List Folders by path
   * @param path Path to operate on. (required)
   * @param cursor Send cursor to resume an existing list from the point at which you left off.  Get a cursor from an existing list via the X-Files-Cursor header. (optional)
   * @param perPage Number of records to show per page.  (Max: 10,000, 1,000 or less is recommended). (optional)
   * @param filter If specified, will filter folders/files list by this string.  Wildcards of &#x60;*&#x60; and &#x60;?&#x60; are acceptable here. (optional)
   * @param previewSize Request a preview size.  Can be &#x60;small&#x60; (default), &#x60;large&#x60;, &#x60;xlarge&#x60;, or &#x60;pdf&#x60;. (optional)
   * @param search If &#x60;search_all&#x60; is &#x60;true&#x60;, provide the search string here.  Otherwise, this parameter acts like an alias of &#x60;filter&#x60;. (optional)
   * @param searchAll Search entire site? (optional)
   * @param withPreviews Include file previews? (optional)
   * @param withPriorityColor Include file priority color information? (optional)
   * @return List&lt;FileEntity&gt;
   * @throws ApiException if fails to make API call
   */
  public List<FileEntity> foldersListForPath(String path, String cursor, Integer perPage, String filter, String previewSize, String search, Boolean searchAll, Boolean withPreviews, Boolean withPriorityColor) throws ApiException {
    Object localVarPostBody = null;
    // verify the required parameter 'path' is set
    if (path == null) {
      throw new ApiException(400, "Missing the required parameter 'path' when calling foldersListForPath");
    }
    // create path and map variables
    String localVarPath = "/folders/{path}"
      .replaceAll("\\{" + "path" + "\\}", apiClient.escapeString(path.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    localVarQueryParams.addAll(apiClient.parameterToPairs("", "cursor", cursor));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "per_page", perPage));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "filter", filter));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "preview_size", previewSize));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "search", search));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "search_all", searchAll));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "with_previews", withPreviews));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "with_priority_color", withPriorityColor));


    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] {  };

    GenericType<List<FileEntity>> localVarReturnType = new GenericType<List<FileEntity>>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
  /**
   * Create folder
   * Create folder
   * @param path Path to operate on. (required)
   * @return FileEntity
   * @throws ApiException if fails to make API call
   */
  public FileEntity postFoldersPath(String path) throws ApiException {
    Object localVarPostBody = null;
    // verify the required parameter 'path' is set
    if (path == null) {
      throw new ApiException(400, "Missing the required parameter 'path' when calling postFoldersPath");
    }
    // create path and map variables
    String localVarPath = "/folders/{path}"
      .replaceAll("\\{" + "path" + "\\}", apiClient.escapeString(path.toString()));

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

    GenericType<FileEntity> localVarReturnType = new GenericType<FileEntity>() {};
    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
}
