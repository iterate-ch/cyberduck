package ch.cyberduck.core.box.io.swagger.client.api;

import ch.cyberduck.core.box.io.swagger.client.ApiException;
import ch.cyberduck.core.box.io.swagger.client.ApiClient;
import ch.cyberduck.core.box.io.swagger.client.Configuration;
import ch.cyberduck.core.box.io.swagger.client.Pair;

import javax.ws.rs.core.GenericType;

import ch.cyberduck.core.box.io.swagger.client.model.ClientError;
import ch.cyberduck.core.box.io.swagger.client.model.FileVersionRetention;
import ch.cyberduck.core.box.io.swagger.client.model.FileVersionRetentions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.JavaClientCodegen", date = "2021-01-25T11:35:18.602705+01:00[Europe/Zurich]")public class FileVersionRetentionsApi {
  private ApiClient apiClient;

  public FileVersionRetentionsApi() {
    this(Configuration.getDefaultApiClient());
  }

  public FileVersionRetentionsApi(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  public ApiClient getApiClient() {
    return apiClient;
  }

  public void setApiClient(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  /**
   * List file version retentions
   * Retrieves all file version retentions for the given enterprise.
   * @param fileId Filters results by files with this ID. (optional)
   * @param fileVersionId Filters results by file versions with this ID. (optional)
   * @param policyId Filters results by the retention policy with this ID. (optional)
   * @param dispositionAction Filters results by the retention policy with this disposition action. (optional)
   * @param dispositionBefore Filters results by files that will have their disposition come into effect before this date. (optional)
   * @param dispositionAfter Filters results by files that will have their disposition come into effect after this date. (optional)
   * @param limit The maximum number of items to return per page. (optional)
   * @param marker Defines the position marker at which to begin returning results. This is used when paginating using marker-based pagination.  This requires &#x60;usemarker&#x60; to be set to &#x60;true&#x60;. (optional)
   * @return FileVersionRetentions
   * @throws ApiException if fails to make API call
   */
  public FileVersionRetentions getFileVersionRetentions(String fileId, String fileVersionId, String policyId, String dispositionAction, String dispositionBefore, String dispositionAfter, Long limit, String marker) throws ApiException {
    Object localVarPostBody = null;
    // create path and map variables
    String localVarPath = "/file_version_retentions";

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    localVarQueryParams.addAll(apiClient.parameterToPairs("", "file_id", fileId));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "file_version_id", fileVersionId));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "policy_id", policyId));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "disposition_action", dispositionAction));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "disposition_before", dispositionBefore));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "disposition_after", dispositionAfter));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "limit", limit));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "marker", marker));



    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "OAuth2Security" };

    GenericType<FileVersionRetentions> localVarReturnType = new GenericType<FileVersionRetentions>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
  /**
   * Get retention on file
   * Returns information about a file version retention.
   * @param fileVersionRetentionId The ID of the file version retention (required)
   * @return FileVersionRetention
   * @throws ApiException if fails to make API call
   */
  public FileVersionRetention getFileVersionRetentionsId(String fileVersionRetentionId) throws ApiException {
    Object localVarPostBody = null;
    // verify the required parameter 'fileVersionRetentionId' is set
    if (fileVersionRetentionId == null) {
      throw new ApiException(400, "Missing the required parameter 'fileVersionRetentionId' when calling getFileVersionRetentionsId");
    }
    // create path and map variables
    String localVarPath = "/file_version_retentions/{file_version_retention_id}"
      .replaceAll("\\{" + "file_version_retention_id" + "\\}", apiClient.escapeString(fileVersionRetentionId.toString()));

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

    GenericType<FileVersionRetention> localVarReturnType = new GenericType<FileVersionRetention>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
}
