package ch.cyberduck.core.box.io.swagger.client.api;

import ch.cyberduck.core.box.io.swagger.client.ApiException;
import ch.cyberduck.core.box.io.swagger.client.ApiClient;
import ch.cyberduck.core.box.io.swagger.client.Configuration;
import ch.cyberduck.core.box.io.swagger.client.Pair;

import javax.ws.rs.core.GenericType;

import ch.cyberduck.core.box.io.swagger.client.model.Body46;
import ch.cyberduck.core.box.io.swagger.client.model.ClientError;
import ch.cyberduck.core.box.io.swagger.client.model.Folder;
import ch.cyberduck.core.box.io.swagger.client.model.WebLink;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.JavaClientCodegen", date = "2021-01-25T11:35:18.602705+01:00[Europe/Zurich]")public class TrashedWebLinksApi {
  private ApiClient apiClient;

  public TrashedWebLinksApi() {
    this(Configuration.getDefaultApiClient());
  }

  public TrashedWebLinksApi(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  public ApiClient getApiClient() {
    return apiClient;
  }

  public void setApiClient(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  /**
   * Permanently remove web link
   * Permanently deletes a web link that is in the trash. This action cannot be undone.
   * @param webLinkId The ID of the web link. (required)
   * @throws ApiException if fails to make API call
   */
  public void deleteWebLinksIdTrash(String webLinkId) throws ApiException {
    Object localVarPostBody = null;
    // verify the required parameter 'webLinkId' is set
    if (webLinkId == null) {
      throw new ApiException(400, "Missing the required parameter 'webLinkId' when calling deleteWebLinksIdTrash");
    }
    // create path and map variables
    String localVarPath = "/web_links/{web_link_id}/trash"
      .replaceAll("\\{" + "web_link_id" + "\\}", apiClient.escapeString(webLinkId.toString()));

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

    String[] localVarAuthNames = new String[] { "OAuth2Security" };

    apiClient.invokeAPI(localVarPath, "DELETE", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, null);
  }
  /**
   * Get trashed web link
   * Retrieves a web link that has been moved to the trash.
   * @param webLinkId The ID of the web link. (required)
   * @param fields A comma-separated list of attributes to include in the response. This can be used to request fields that are not normally returned in a standard response.  Be aware that specifying this parameter will have the effect that none of the standard fields are returned in the response unless explicitly specified, instead only fields for the mini representation are returned, additional to the fields requested. (optional)
   * @return Folder
   * @throws ApiException if fails to make API call
   */
  public Folder getWebLinksIdTrash(String webLinkId, List<String> fields) throws ApiException {
    Object localVarPostBody = null;
    // verify the required parameter 'webLinkId' is set
    if (webLinkId == null) {
      throw new ApiException(400, "Missing the required parameter 'webLinkId' when calling getWebLinksIdTrash");
    }
    // create path and map variables
    String localVarPath = "/web_links/{web_link_id}/trash"
      .replaceAll("\\{" + "web_link_id" + "\\}", apiClient.escapeString(webLinkId.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    localVarQueryParams.addAll(apiClient.parameterToPairs("csv", "fields", fields));



    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "OAuth2Security" };

    GenericType<Folder> localVarReturnType = new GenericType<Folder>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
  /**
   * Restore web link
   * Restores a web link that has been moved to the trash.  An optional new parent ID can be provided to restore the  web link to in case the original folder has been deleted.
   * @param webLinkId The ID of the web link. (required)
   * @param body  (optional)
   * @param fields A comma-separated list of attributes to include in the response. This can be used to request fields that are not normally returned in a standard response.  Be aware that specifying this parameter will have the effect that none of the standard fields are returned in the response unless explicitly specified, instead only fields for the mini representation are returned, additional to the fields requested. (optional)
   * @return WebLink
   * @throws ApiException if fails to make API call
   */
  public WebLink postWebLinksId(String webLinkId, Body46 body, List<String> fields) throws ApiException {
    Object localVarPostBody = body;
    // verify the required parameter 'webLinkId' is set
    if (webLinkId == null) {
      throw new ApiException(400, "Missing the required parameter 'webLinkId' when calling postWebLinksId");
    }
    // create path and map variables
    String localVarPath = "/web_links/{web_link_id}"
      .replaceAll("\\{" + "web_link_id" + "\\}", apiClient.escapeString(webLinkId.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    localVarQueryParams.addAll(apiClient.parameterToPairs("csv", "fields", fields));



    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "OAuth2Security" };

    GenericType<WebLink> localVarReturnType = new GenericType<WebLink>() {};
    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
}
