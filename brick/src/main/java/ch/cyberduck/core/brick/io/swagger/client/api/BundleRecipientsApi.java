package ch.cyberduck.core.brick.io.swagger.client.api;

import ch.cyberduck.core.brick.io.swagger.client.ApiException;
import ch.cyberduck.core.brick.io.swagger.client.ApiClient;
import ch.cyberduck.core.brick.io.swagger.client.Configuration;
import ch.cyberduck.core.brick.io.swagger.client.Pair;

import javax.ws.rs.core.GenericType;

import ch.cyberduck.core.brick.io.swagger.client.model.BundleRecipientEntity;
import ch.cyberduck.core.brick.io.swagger.client.model.BundleRecipientsBody;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.JavaClientCodegen", date = "2021-07-25T22:25:43.390877+02:00[Europe/Paris]")public class BundleRecipientsApi {
  private ApiClient apiClient;

  public BundleRecipientsApi() {
    this(Configuration.getDefaultApiClient());
  }

  public BundleRecipientsApi(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  public ApiClient getApiClient() {
    return apiClient;
  }

  public void setApiClient(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  /**
   * List Bundle Recipients
   * List Bundle Recipients
   * @param bundleId List recipients for the bundle with this ID. (required)
   * @param userId User ID.  Provide a value of &#x60;0&#x60; to operate the current session&#x27;s user. (optional)
   * @param cursor Used for pagination.  Send a cursor value to resume an existing list from the point at which you left off.  Get a cursor from an existing list via the X-Files-Cursor-Next header. (optional)
   * @param perPage Number of records to show per page.  (Max: 10,000, 1,000 or less is recommended). (optional)
   * @param sortBy If set, sort records by the specified field in either &#x27;asc&#x27; or &#x27;desc&#x27; direction (e.g. sort_by[last_login_at]&#x3D;desc). Valid fields are &#x60;has_registrations&#x60;. (optional)
   * @param filter If set, return records where the specifiied field is equal to the supplied value. Valid fields are &#x60;has_registrations&#x60;. (optional)
   * @param filterGt If set, return records where the specifiied field is greater than the supplied value. Valid fields are &#x60;has_registrations&#x60;. (optional)
   * @param filterGteq If set, return records where the specifiied field is greater than or equal to the supplied value. Valid fields are &#x60;has_registrations&#x60;. (optional)
   * @param filterLike If set, return records where the specifiied field is equal to the supplied value. Valid fields are &#x60;has_registrations&#x60;. (optional)
   * @param filterLt If set, return records where the specifiied field is less than the supplied value. Valid fields are &#x60;has_registrations&#x60;. (optional)
   * @param filterLteq If set, return records where the specifiied field is less than or equal to the supplied value. Valid fields are &#x60;has_registrations&#x60;. (optional)
   * @return List&lt;BundleRecipientEntity&gt;
   * @throws ApiException if fails to make API call
   */
  public List<BundleRecipientEntity> getBundleRecipients(Integer bundleId, Integer userId, String cursor, Integer perPage, Object sortBy, Object filter, Object filterGt, Object filterGteq, Object filterLike, Object filterLt, Object filterLteq) throws ApiException {
    Object localVarPostBody = null;
    // verify the required parameter 'bundleId' is set
    if (bundleId == null) {
      throw new ApiException(400, "Missing the required parameter 'bundleId' when calling getBundleRecipients");
    }
    // create path and map variables
    String localVarPath = "/bundle_recipients";

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    localVarQueryParams.addAll(apiClient.parameterToPairs("", "user_id", userId));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "cursor", cursor));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "per_page", perPage));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "sort_by", sortBy));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "filter", filter));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "filter_gt", filterGt));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "filter_gteq", filterGteq));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "filter_like", filterLike));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "filter_lt", filterLt));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "filter_lteq", filterLteq));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "bundle_id", bundleId));


    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] {  };

    GenericType<List<BundleRecipientEntity>> localVarReturnType = new GenericType<List<BundleRecipientEntity>>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
  /**
   * Create Bundle Recipient
   * Create Bundle Recipient
   * @param body  (required)
   * @return BundleRecipientEntity
   * @throws ApiException if fails to make API call
   */
  public BundleRecipientEntity postBundleRecipients(BundleRecipientsBody body) throws ApiException {
    Object localVarPostBody = body;
    // verify the required parameter 'body' is set
    if (body == null) {
      throw new ApiException(400, "Missing the required parameter 'body' when calling postBundleRecipients");
    }
    // create path and map variables
    String localVarPath = "/bundle_recipients";

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

    String[] localVarAuthNames = new String[] {  };

    GenericType<BundleRecipientEntity> localVarReturnType = new GenericType<BundleRecipientEntity>() {};
    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
}
