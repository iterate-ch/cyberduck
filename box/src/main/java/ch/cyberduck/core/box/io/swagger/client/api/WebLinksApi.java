package ch.cyberduck.core.box.io.swagger.client.api;

import ch.cyberduck.core.box.io.swagger.client.ApiException;
import ch.cyberduck.core.box.io.swagger.client.ApiClient;
import ch.cyberduck.core.box.io.swagger.client.Configuration;
import ch.cyberduck.core.box.io.swagger.client.Pair;

import javax.ws.rs.core.GenericType;

import ch.cyberduck.core.box.io.swagger.client.model.Body44;
import ch.cyberduck.core.box.io.swagger.client.model.Body45;
import ch.cyberduck.core.box.io.swagger.client.model.ClientError;
import ch.cyberduck.core.box.io.swagger.client.model.WebLink;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.JavaClientCodegen", date = "2021-01-25T11:35:18.602705+01:00[Europe/Zurich]")public class WebLinksApi {
  private ApiClient apiClient;

  public WebLinksApi() {
    this(Configuration.getDefaultApiClient());
  }

  public WebLinksApi(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  public ApiClient getApiClient() {
    return apiClient;
  }

  public void setApiClient(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  /**
   * Remove web link
   * Deletes a web link.
   * @param webLinkId The ID of the web link. (required)
   * @throws ApiException if fails to make API call
   */
  public void deleteWebLinksId(String webLinkId) throws ApiException {
    Object localVarPostBody = null;
    // verify the required parameter 'webLinkId' is set
    if (webLinkId == null) {
      throw new ApiException(400, "Missing the required parameter 'webLinkId' when calling deleteWebLinksId");
    }
    // create path and map variables
    String localVarPath = "/web_links/{web_link_id}"
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
   * Get web link
   * Retrieve information about a web link.
   * @param webLinkId The ID of the web link. (required)
   * @param boxapi The URL, and optional password, for the shared link of this item.  This header can be used to access items that have not been explicitly shared with a user.  Use the format &#x60;shared_link&#x3D;[link]&#x60; or if a password is required then use &#x60;shared_link&#x3D;[link]&amp;shared_link_password&#x3D;[password]&#x60;.  This header can be used on the file or folder shared, as well as on any files or folders nested within the item. (optional)
   * @return WebLink
   * @throws ApiException if fails to make API call
   */
  public WebLink getWebLinksId(String webLinkId, String boxapi) throws ApiException {
    Object localVarPostBody = null;
    // verify the required parameter 'webLinkId' is set
    if (webLinkId == null) {
      throw new ApiException(400, "Missing the required parameter 'webLinkId' when calling getWebLinksId");
    }
    // create path and map variables
    String localVarPath = "/web_links/{web_link_id}"
      .replaceAll("\\{" + "web_link_id" + "\\}", apiClient.escapeString(webLinkId.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


    if (boxapi != null)
      localVarHeaderParams.put("boxapi", apiClient.parameterToString(boxapi));


    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "OAuth2Security" };

    GenericType<WebLink> localVarReturnType = new GenericType<WebLink>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
  /**
   * Create web link
   * Creates a web link object within a folder.
   * @param body  (optional)
   * @return WebLink
   * @throws ApiException if fails to make API call
   */
  public WebLink postWebLinks(Body44 body) throws ApiException {
    Object localVarPostBody = body;
    // create path and map variables
    String localVarPath = "/web_links";

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();




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
  /**
   * Update web link
   * Updates a web link object.
   * @param webLinkId The ID of the web link. (required)
   * @param body  (optional)
   * @return WebLink
   * @throws ApiException if fails to make API call
   */
  public WebLink putWebLinksId(String webLinkId, Body45 body) throws ApiException {
    Object localVarPostBody = body;
    // verify the required parameter 'webLinkId' is set
    if (webLinkId == null) {
      throw new ApiException(400, "Missing the required parameter 'webLinkId' when calling putWebLinksId");
    }
    // create path and map variables
    String localVarPath = "/web_links/{web_link_id}"
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
      "application/json"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "OAuth2Security" };

    GenericType<WebLink> localVarReturnType = new GenericType<WebLink>() {};
    return apiClient.invokeAPI(localVarPath, "PUT", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
}
