package ch.cyberduck.core.deepbox.io.swagger.client.api;

import ch.cyberduck.core.deepbox.io.swagger.client.ApiException;
import ch.cyberduck.core.deepbox.io.swagger.client.ApiClient;
import ch.cyberduck.core.deepbox.io.swagger.client.Configuration;
import ch.cyberduck.core.deepbox.io.swagger.client.Pair;

import javax.ws.rs.core.GenericType;

import ch.cyberduck.core.deepbox.io.swagger.client.model.LogEntry;
import ch.cyberduck.core.deepbox.io.swagger.client.model.ShareLinkAdd;
import ch.cyberduck.core.deepbox.io.swagger.client.model.ShareLinkResource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ShareLinkRestControllerApi {
  private ApiClient apiClient;

  public ShareLinkRestControllerApi() {
    this(Configuration.getDefaultApiClient());
  }

  public ShareLinkRestControllerApi(ApiClient apiClient) {
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
   * Create a sharelink which allows accessing a file. To download a file use [/nodes/{nodeId}/downloadUrl](#/download-rest-controller/downloadUrl) or asyc [/downloads](#/download-rest-controller/requestDownload) endpoints instead.
   * @param body  (required)
   * @param acceptMessageIds  (optional)
   * @return ShareLinkResource
   * @throws ApiException if fails to make API call
   */
  public ShareLinkResource createShareLinkResource(ShareLinkAdd body, String acceptMessageIds) throws ApiException {
    Object localVarPostBody = body;
    // verify the required parameter 'body' is set
    if (body == null) {
      throw new ApiException(400, "Missing the required parameter 'body' when calling createShareLinkResource");
    }
    // create path and map variables
    String localVarPath = "/api/v1/sharelinks";

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    localVarQueryParams.addAll(apiClient.parameterToPairs("", "acceptMessageIds", acceptMessageIds));


    final String[] localVarAccepts = {
      "*/*"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "token" };

    GenericType<ShareLinkResource> localVarReturnType = new GenericType<ShareLinkResource>() {};
    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
}
