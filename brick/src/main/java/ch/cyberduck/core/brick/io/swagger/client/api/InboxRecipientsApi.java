package ch.cyberduck.core.brick.io.swagger.client.api;

import ch.cyberduck.core.brick.io.swagger.client.ApiException;
import ch.cyberduck.core.brick.io.swagger.client.ApiClient;
import ch.cyberduck.core.brick.io.swagger.client.ApiResponse;
import ch.cyberduck.core.brick.io.swagger.client.Configuration;
import ch.cyberduck.core.brick.io.swagger.client.Pair;

import javax.ws.rs.core.GenericType;

import ch.cyberduck.core.brick.io.swagger.client.model.InboxRecipientEntity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaClientCodegen", date = "2021-06-30T21:29:25.490+02:00")
public class InboxRecipientsApi {
  private ApiClient apiClient;

  public InboxRecipientsApi() {
    this(Configuration.getDefaultApiClient());
  }

  public InboxRecipientsApi(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  public ApiClient getApiClient() {
    return apiClient;
  }

  public void setApiClient(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  /**
   * List Inbox Recipients
   * List Inbox Recipients
   * @param inboxId List recipients for the inbox with this ID. (required)
   * @param userId User ID.  Provide a value of &#x60;0&#x60; to operate the current session&#39;s user. (optional)
   * @param cursor Used for pagination.  Send a cursor value to resume an existing list from the point at which you left off.  Get a cursor from an existing list via the X-Files-Cursor-Next header. (optional)
   * @param perPage Number of records to show per page.  (Max: 10,000, 1,000 or less is recommended). (optional)
   * @param sortBy If set, sort records by the specified field in either &#39;asc&#39; or &#39;desc&#39; direction (e.g. sort_by[last_login_at]&#x3D;desc). Valid fields are &#x60;has_registrations&#x60;. (optional)
   * @param filter If set, return records where the specifiied field is equal to the supplied value. Valid fields are &#x60;has_registrations&#x60;. (optional)
   * @param filterGt If set, return records where the specifiied field is greater than the supplied value. Valid fields are &#x60;has_registrations&#x60;. (optional)
   * @param filterGteq If set, return records where the specifiied field is greater than or equal to the supplied value. Valid fields are &#x60;has_registrations&#x60;. (optional)
   * @param filterLike If set, return records where the specifiied field is equal to the supplied value. Valid fields are &#x60;has_registrations&#x60;. (optional)
   * @param filterLt If set, return records where the specifiied field is less than the supplied value. Valid fields are &#x60;has_registrations&#x60;. (optional)
   * @param filterLteq If set, return records where the specifiied field is less than or equal to the supplied value. Valid fields are &#x60;has_registrations&#x60;. (optional)
   * @return List&lt;InboxRecipientEntity&gt;
   * @throws ApiException if fails to make API call
   */
  public List<InboxRecipientEntity> getInboxRecipients(Integer inboxId, Integer userId, String cursor, Integer perPage, Map<String, String> sortBy, Map<String, String> filter, Map<String, String> filterGt, Map<String, String> filterGteq, Map<String, String> filterLike, Map<String, String> filterLt, Map<String, String> filterLteq) throws ApiException {
    return getInboxRecipientsWithHttpInfo(inboxId, userId, cursor, perPage, sortBy, filter, filterGt, filterGteq, filterLike, filterLt, filterLteq).getData();
      }

  /**
   * List Inbox Recipients
   * List Inbox Recipients
   * @param inboxId List recipients for the inbox with this ID. (required)
   * @param userId User ID.  Provide a value of &#x60;0&#x60; to operate the current session&#39;s user. (optional)
   * @param cursor Used for pagination.  Send a cursor value to resume an existing list from the point at which you left off.  Get a cursor from an existing list via the X-Files-Cursor-Next header. (optional)
   * @param perPage Number of records to show per page.  (Max: 10,000, 1,000 or less is recommended). (optional)
   * @param sortBy If set, sort records by the specified field in either &#39;asc&#39; or &#39;desc&#39; direction (e.g. sort_by[last_login_at]&#x3D;desc). Valid fields are &#x60;has_registrations&#x60;. (optional)
   * @param filter If set, return records where the specifiied field is equal to the supplied value. Valid fields are &#x60;has_registrations&#x60;. (optional)
   * @param filterGt If set, return records where the specifiied field is greater than the supplied value. Valid fields are &#x60;has_registrations&#x60;. (optional)
   * @param filterGteq If set, return records where the specifiied field is greater than or equal to the supplied value. Valid fields are &#x60;has_registrations&#x60;. (optional)
   * @param filterLike If set, return records where the specifiied field is equal to the supplied value. Valid fields are &#x60;has_registrations&#x60;. (optional)
   * @param filterLt If set, return records where the specifiied field is less than the supplied value. Valid fields are &#x60;has_registrations&#x60;. (optional)
   * @param filterLteq If set, return records where the specifiied field is less than or equal to the supplied value. Valid fields are &#x60;has_registrations&#x60;. (optional)
   * @return ApiResponse&lt;List&lt;InboxRecipientEntity&gt;&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<List<InboxRecipientEntity>> getInboxRecipientsWithHttpInfo(Integer inboxId, Integer userId, String cursor, Integer perPage, Map<String, String> sortBy, Map<String, String> filter, Map<String, String> filterGt, Map<String, String> filterGteq, Map<String, String> filterLike, Map<String, String> filterLt, Map<String, String> filterLteq) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'inboxId' is set
    if (inboxId == null) {
      throw new ApiException(400, "Missing the required parameter 'inboxId' when calling getInboxRecipients");
    }
    
    // create path and map variables
    String localVarPath = "/inbox_recipients";

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
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "inbox_id", inboxId));

    
    
    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] {  };

    GenericType<List<InboxRecipientEntity>> localVarReturnType = new GenericType<List<InboxRecipientEntity>>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Create Inbox Recipient
   * Create Inbox Recipient
   * @param inboxId Inbox to share. (required)
   * @param recipient Email address to share this inbox with. (required)
   * @param userId User ID.  Provide a value of &#x60;0&#x60; to operate the current session&#39;s user. (optional)
   * @param name Name of recipient. (optional)
   * @param company Company of recipient. (optional)
   * @param note Note to include in email. (optional)
   * @param shareAfterCreate Set to true to share the link with the recipient upon creation. (optional)
   * @return InboxRecipientEntity
   * @throws ApiException if fails to make API call
   */
  public InboxRecipientEntity postInboxRecipients(Integer inboxId, String recipient, Integer userId, String name, String company, String note, Boolean shareAfterCreate) throws ApiException {
    return postInboxRecipientsWithHttpInfo(inboxId, recipient, userId, name, company, note, shareAfterCreate).getData();
      }

  /**
   * Create Inbox Recipient
   * Create Inbox Recipient
   * @param inboxId Inbox to share. (required)
   * @param recipient Email address to share this inbox with. (required)
   * @param userId User ID.  Provide a value of &#x60;0&#x60; to operate the current session&#39;s user. (optional)
   * @param name Name of recipient. (optional)
   * @param company Company of recipient. (optional)
   * @param note Note to include in email. (optional)
   * @param shareAfterCreate Set to true to share the link with the recipient upon creation. (optional)
   * @return ApiResponse&lt;InboxRecipientEntity&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<InboxRecipientEntity> postInboxRecipientsWithHttpInfo(Integer inboxId, String recipient, Integer userId, String name, String company, String note, Boolean shareAfterCreate) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'inboxId' is set
    if (inboxId == null) {
      throw new ApiException(400, "Missing the required parameter 'inboxId' when calling postInboxRecipients");
    }
    
    // verify the required parameter 'recipient' is set
    if (recipient == null) {
      throw new ApiException(400, "Missing the required parameter 'recipient' when calling postInboxRecipients");
    }
    
    // create path and map variables
    String localVarPath = "/inbox_recipients";

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


    
    if (userId != null)
      localVarFormParams.put("user_id", userId);
if (inboxId != null)
      localVarFormParams.put("inbox_id", inboxId);
if (recipient != null)
      localVarFormParams.put("recipient", recipient);
if (name != null)
      localVarFormParams.put("name", name);
if (company != null)
      localVarFormParams.put("company", company);
if (note != null)
      localVarFormParams.put("note", note);
if (shareAfterCreate != null)
      localVarFormParams.put("share_after_create", shareAfterCreate);

    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json", "application/x-www-form-urlencoded", "multipart/form-data"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] {  };

    GenericType<InboxRecipientEntity> localVarReturnType = new GenericType<InboxRecipientEntity>() {};
    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
}
