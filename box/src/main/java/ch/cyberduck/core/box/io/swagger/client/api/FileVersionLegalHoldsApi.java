package ch.cyberduck.core.box.io.swagger.client.api;

import ch.cyberduck.core.box.io.swagger.client.ApiException;
import ch.cyberduck.core.box.io.swagger.client.ApiClient;
import ch.cyberduck.core.box.io.swagger.client.Configuration;
import ch.cyberduck.core.box.io.swagger.client.Pair;

import javax.ws.rs.core.GenericType;

import ch.cyberduck.core.box.io.swagger.client.model.ClientError;
import ch.cyberduck.core.box.io.swagger.client.model.FileVersionLegalHold;
import ch.cyberduck.core.box.io.swagger.client.model.FileVersionLegalHolds;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.JavaClientCodegen", date = "2021-01-25T11:35:18.602705+01:00[Europe/Zurich]")public class FileVersionLegalHoldsApi {
  private ApiClient apiClient;

  public FileVersionLegalHoldsApi() {
    this(Configuration.getDefaultApiClient());
  }

  public FileVersionLegalHoldsApi(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  public ApiClient getApiClient() {
    return apiClient;
  }

  public void setApiClient(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  /**
   * List file version legal holds
   * Get a list of file versions on legal hold for a legal hold assignment.  Due to ongoing re-architecture efforts this API might not return all file versions for this policy ID.  Instead, this API will only return file versions held in the legacy architecture. Two new endpoints will available to request any file versions held in the new architecture.  For file versions held in the new architecture, the &#x60;GET /legal_hold_policy_assignments/:id/file_versions_on_hold&#x60; API can be used to return all past file versions available for this policy assignment, and the &#x60;GET /legal_hold_policy_assignments/:id/files_on_hold&#x60; API can be used to return any current (latest) versions of a file under legal hold.  The &#x60;GET /legal_hold_policy_assignments?policy_id&#x3D;{id}&#x60; API can be used to find a list of policy assignments for a given policy ID.  Once the re-architecture is completed this API will be deprecated.
   * @param policyId The ID of the legal hold policy to get the file version legal holds for. (required)
   * @param marker Defines the position marker at which to begin returning results. This is used when paginating using marker-based pagination.  This requires &#x60;usemarker&#x60; to be set to &#x60;true&#x60;. (optional)
   * @param limit The maximum number of items to return per page. (optional)
   * @return FileVersionLegalHolds
   * @throws ApiException if fails to make API call
   */
  public FileVersionLegalHolds getFileVersionLegalHolds(String policyId, String marker, Long limit) throws ApiException {
    Object localVarPostBody = null;
    // verify the required parameter 'policyId' is set
    if (policyId == null) {
      throw new ApiException(400, "Missing the required parameter 'policyId' when calling getFileVersionLegalHolds");
    }
    // create path and map variables
    String localVarPath = "/file_version_legal_holds";

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    localVarQueryParams.addAll(apiClient.parameterToPairs("", "policy_id", policyId));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "marker", marker));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "limit", limit));



    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "OAuth2Security" };

    GenericType<FileVersionLegalHolds> localVarReturnType = new GenericType<FileVersionLegalHolds>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
  /**
   * Get file version legal hold
   * Retrieves information about the legal hold policies assigned to a file version.
   * @param fileVersionLegalHoldId The ID of the file version legal hold (required)
   * @return FileVersionLegalHold
   * @throws ApiException if fails to make API call
   */
  public FileVersionLegalHold getFileVersionLegalHoldsId(String fileVersionLegalHoldId) throws ApiException {
    Object localVarPostBody = null;
    // verify the required parameter 'fileVersionLegalHoldId' is set
    if (fileVersionLegalHoldId == null) {
      throw new ApiException(400, "Missing the required parameter 'fileVersionLegalHoldId' when calling getFileVersionLegalHoldsId");
    }
    // create path and map variables
    String localVarPath = "/file_version_legal_holds/{file_version_legal_hold_id}"
      .replaceAll("\\{" + "file_version_legal_hold_id" + "\\}", apiClient.escapeString(fileVersionLegalHoldId.toString()));

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

    GenericType<FileVersionLegalHold> localVarReturnType = new GenericType<FileVersionLegalHold>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
}
