package ch.cyberduck.core.storegate.io.swagger.client.api;

import ch.cyberduck.core.storegate.io.swagger.client.ApiException;
import ch.cyberduck.core.storegate.io.swagger.client.ApiClient;
import ch.cyberduck.core.storegate.io.swagger.client.ApiResponse;
import ch.cyberduck.core.storegate.io.swagger.client.Configuration;
import ch.cyberduck.core.storegate.io.swagger.client.Pair;

import javax.ws.rs.core.GenericType;

import ch.cyberduck.core.storegate.io.swagger.client.model.RecycleBinContents;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaClientCodegen", date = "2019-12-02T20:20:31.369+01:00")
public class RecycleBinApi {
  private ApiClient apiClient;

  public RecycleBinApi() {
    this(Configuration.getDefaultApiClient());
  }

  public RecycleBinApi(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  public ApiClient getApiClient() {
    return apiClient;
  }

  public void setApiClient(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  /**
   * Permanently removes item from the recycle bin
   * 
   * @param id The recycle bin item to remove (required)
   * @throws ApiException if fails to make API call
   */
  public void recycleBinDeleteRecycleBinItem(String id) throws ApiException {

    recycleBinDeleteRecycleBinItemWithHttpInfo(id);
  }

  /**
   * Permanently removes item from the recycle bin
   * 
   * @param id The recycle bin item to remove (required)
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<Void> recycleBinDeleteRecycleBinItemWithHttpInfo(String id) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'id' is set
    if (id == null) {
      throw new ApiException(400, "Missing the required parameter 'id' when calling recycleBinDeleteRecycleBinItem");
    }
    
    // create path and map variables
    String localVarPath = "/v4/recyclebin/{id}"
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
   * Empty the recycle bin for the current user
   * 
   * @throws ApiException if fails to make API call
   */
  public void recycleBinEmptyRecycleBin() throws ApiException {

    recycleBinEmptyRecycleBinWithHttpInfo();
  }

  /**
   * Empty the recycle bin for the current user
   * 
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<Void> recycleBinEmptyRecycleBinWithHttpInfo() throws ApiException {
    Object localVarPostBody = null;
    
    // create path and map variables
    String localVarPath = "/v4/recyclebin";

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
   * Gets the recycle bin content
   * 
   * @param pageIndex Index of page (required)
   * @param pageSize Max rows per page (required)
   * @param sortExpression OriginalName, ModifiedDate, Size, DeletedDate, OriginalLocation (desc/asc) (optional)
   * @return RecycleBinContents
   * @throws ApiException if fails to make API call
   */
  public RecycleBinContents recycleBinGetRecycleBinContents(Integer pageIndex, Integer pageSize, String sortExpression) throws ApiException {
    return recycleBinGetRecycleBinContentsWithHttpInfo(pageIndex, pageSize, sortExpression).getData();
      }

  /**
   * Gets the recycle bin content
   * 
   * @param pageIndex Index of page (required)
   * @param pageSize Max rows per page (required)
   * @param sortExpression OriginalName, ModifiedDate, Size, DeletedDate, OriginalLocation (desc/asc) (optional)
   * @return ApiResponse&lt;RecycleBinContents&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<RecycleBinContents> recycleBinGetRecycleBinContentsWithHttpInfo(Integer pageIndex, Integer pageSize, String sortExpression) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'pageIndex' is set
    if (pageIndex == null) {
      throw new ApiException(400, "Missing the required parameter 'pageIndex' when calling recycleBinGetRecycleBinContents");
    }
    
    // verify the required parameter 'pageSize' is set
    if (pageSize == null) {
      throw new ApiException(400, "Missing the required parameter 'pageSize' when calling recycleBinGetRecycleBinContents");
    }
    
    // create path and map variables
    String localVarPath = "/v4/recyclebin";

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    localVarQueryParams.addAll(apiClient.parameterToPairs("", "pageIndex", pageIndex));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "pageSize", pageSize));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "sortExpression", sortExpression));

    
    
    final String[] localVarAccepts = {
      "application/json", "text/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "oauth2" };

    GenericType<RecycleBinContents> localVarReturnType = new GenericType<RecycleBinContents>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Restores (moves) the recycle bin item back to the original location
   * 
   * @param id The recycle bin item to remove (required)
   * @param mode 0 &#x3D; None (returns error on conflict), 1 &#x3D; Overwrite (deletes conflict on target), 2 &#x3D; KeepBoth (renames the moved resource) (required)
   * @throws ApiException if fails to make API call
   */
  public void recycleBinRestoreRecycleBinItem(String id, Integer mode) throws ApiException {

    recycleBinRestoreRecycleBinItemWithHttpInfo(id, mode);
  }

  /**
   * Restores (moves) the recycle bin item back to the original location
   * 
   * @param id The recycle bin item to remove (required)
   * @param mode 0 &#x3D; None (returns error on conflict), 1 &#x3D; Overwrite (deletes conflict on target), 2 &#x3D; KeepBoth (renames the moved resource) (required)
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<Void> recycleBinRestoreRecycleBinItemWithHttpInfo(String id, Integer mode) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'id' is set
    if (id == null) {
      throw new ApiException(400, "Missing the required parameter 'id' when calling recycleBinRestoreRecycleBinItem");
    }
    
    // verify the required parameter 'mode' is set
    if (mode == null) {
      throw new ApiException(400, "Missing the required parameter 'mode' when calling recycleBinRestoreRecycleBinItem");
    }
    
    // create path and map variables
    String localVarPath = "/v4/recyclebin/{id}/restore"
      .replaceAll("\\{" + "id" + "\\}", apiClient.escapeString(id.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    localVarQueryParams.addAll(apiClient.parameterToPairs("", "mode", mode));

    
    
    final String[] localVarAccepts = {
      
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "oauth2" };


    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, null);
  }
}
