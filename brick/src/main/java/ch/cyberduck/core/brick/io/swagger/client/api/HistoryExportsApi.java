package ch.cyberduck.core.brick.io.swagger.client.api;

import ch.cyberduck.core.brick.io.swagger.client.ApiException;
import ch.cyberduck.core.brick.io.swagger.client.ApiClient;
import ch.cyberduck.core.brick.io.swagger.client.ApiResponse;
import ch.cyberduck.core.brick.io.swagger.client.Configuration;
import ch.cyberduck.core.brick.io.swagger.client.Pair;

import javax.ws.rs.core.GenericType;

import org.joda.time.DateTime;
import ch.cyberduck.core.brick.io.swagger.client.model.HistoryExportEntity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaClientCodegen", date = "2021-06-30T21:29:25.490+02:00")
public class HistoryExportsApi {
  private ApiClient apiClient;

  public HistoryExportsApi() {
    this(Configuration.getDefaultApiClient());
  }

  public HistoryExportsApi(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  public ApiClient getApiClient() {
    return apiClient;
  }

  public void setApiClient(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  /**
   * Show History Export
   * Show History Export
   * @param id History Export ID. (required)
   * @return HistoryExportEntity
   * @throws ApiException if fails to make API call
   */
  public HistoryExportEntity getHistoryExportsId(Integer id) throws ApiException {
    return getHistoryExportsIdWithHttpInfo(id).getData();
      }

  /**
   * Show History Export
   * Show History Export
   * @param id History Export ID. (required)
   * @return ApiResponse&lt;HistoryExportEntity&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<HistoryExportEntity> getHistoryExportsIdWithHttpInfo(Integer id) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'id' is set
    if (id == null) {
      throw new ApiException(400, "Missing the required parameter 'id' when calling getHistoryExportsId");
    }
    
    // create path and map variables
    String localVarPath = "/history_exports/{id}"
      .replaceAll("\\{" + "id" + "\\}", apiClient.escapeString(id.toString()));

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

    GenericType<HistoryExportEntity> localVarReturnType = new GenericType<HistoryExportEntity>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Create History Export
   * Create History Export
   * @param userId User ID.  Provide a value of &#x60;0&#x60; to operate the current session&#39;s user. (optional)
   * @param startAt Start date/time of export range. (optional)
   * @param endAt End date/time of export range. (optional)
   * @param queryAction Filter results by this this action type. Valid values: &#x60;create&#x60;, &#x60;read&#x60;, &#x60;update&#x60;, &#x60;destroy&#x60;, &#x60;move&#x60;, &#x60;login&#x60;, &#x60;failedlogin&#x60;, &#x60;copy&#x60;, &#x60;user_create&#x60;, &#x60;user_update&#x60;, &#x60;user_destroy&#x60;, &#x60;group_create&#x60;, &#x60;group_update&#x60;, &#x60;group_destroy&#x60;, &#x60;permission_create&#x60;, &#x60;permission_destroy&#x60;, &#x60;api_key_create&#x60;, &#x60;api_key_update&#x60;, &#x60;api_key_destroy&#x60; (optional)
   * @param queryInterface Filter results by this this interface type. Valid values: &#x60;web&#x60;, &#x60;ftp&#x60;, &#x60;robot&#x60;, &#x60;jsapi&#x60;, &#x60;webdesktopapi&#x60;, &#x60;sftp&#x60;, &#x60;dav&#x60;, &#x60;desktop&#x60;, &#x60;restapi&#x60;, &#x60;scim&#x60;, &#x60;office&#x60; (optional)
   * @param queryUserId Return results that are actions performed by the user indiciated by this User ID (optional)
   * @param queryFileId Return results that are file actions related to the file indicated by this File ID (optional)
   * @param queryParentId Return results that are file actions inside the parent folder specified by this folder ID (optional)
   * @param queryPath Return results that are file actions related to this path. (optional)
   * @param queryFolder Return results that are file actions related to files or folders inside this folder path. (optional)
   * @param querySrc Return results that are file moves originating from this path. (optional)
   * @param queryDestination Return results that are file moves with this path as destination. (optional)
   * @param queryIp Filter results by this IP address. (optional)
   * @param queryUsername Filter results by this username. (optional)
   * @param queryFailureType If searching for Histories about login failures, this parameter restricts results to failures of this specific type.  Valid values: &#x60;expired_trial&#x60;, &#x60;account_overdue&#x60;, &#x60;locked_out&#x60;, &#x60;ip_mismatch&#x60;, &#x60;password_mismatch&#x60;, &#x60;site_mismatch&#x60;, &#x60;username_not_found&#x60;, &#x60;none&#x60;, &#x60;no_ftp_permission&#x60;, &#x60;no_web_permission&#x60;, &#x60;no_directory&#x60;, &#x60;errno_enoent&#x60;, &#x60;no_sftp_permission&#x60;, &#x60;no_dav_permission&#x60;, &#x60;no_restapi_permission&#x60;, &#x60;key_mismatch&#x60;, &#x60;region_mismatch&#x60;, &#x60;expired_access&#x60;, &#x60;desktop_ip_mismatch&#x60;, &#x60;desktop_api_key_not_used_quickly_enough&#x60;, &#x60;disabled&#x60;, &#x60;country_mismatch&#x60; (optional)
   * @param queryTargetId If searching for Histories about specific objects (such as Users, or API Keys), this paremeter restricts results to objects that match this ID. (optional)
   * @param queryTargetName If searching for Histories about Users, Groups or other objects with names, this parameter restricts results to objects with this name/username. (optional)
   * @param queryTargetPermission If searching for Histories about Permisisons, this parameter restricts results to permissions of this level. (optional)
   * @param queryTargetUserId If searching for Histories about API keys, this parameter restricts results to API keys created by/for this user ID. (optional)
   * @param queryTargetUsername If searching for Histories about API keys, this parameter restricts results to API keys created by/for this username. (optional)
   * @param queryTargetPlatform If searching for Histories about API keys, this parameter restricts results to API keys associated with this platform. (optional)
   * @param queryTargetPermissionSet If searching for Histories about API keys, this parameter restricts results to API keys with this permission set. (optional)
   * @return HistoryExportEntity
   * @throws ApiException if fails to make API call
   */
  public HistoryExportEntity postHistoryExports(Integer userId, DateTime startAt, DateTime endAt, String queryAction, String queryInterface, String queryUserId, String queryFileId, String queryParentId, String queryPath, String queryFolder, String querySrc, String queryDestination, String queryIp, String queryUsername, String queryFailureType, String queryTargetId, String queryTargetName, String queryTargetPermission, String queryTargetUserId, String queryTargetUsername, String queryTargetPlatform, String queryTargetPermissionSet) throws ApiException {
    return postHistoryExportsWithHttpInfo(userId, startAt, endAt, queryAction, queryInterface, queryUserId, queryFileId, queryParentId, queryPath, queryFolder, querySrc, queryDestination, queryIp, queryUsername, queryFailureType, queryTargetId, queryTargetName, queryTargetPermission, queryTargetUserId, queryTargetUsername, queryTargetPlatform, queryTargetPermissionSet).getData();
      }

  /**
   * Create History Export
   * Create History Export
   * @param userId User ID.  Provide a value of &#x60;0&#x60; to operate the current session&#39;s user. (optional)
   * @param startAt Start date/time of export range. (optional)
   * @param endAt End date/time of export range. (optional)
   * @param queryAction Filter results by this this action type. Valid values: &#x60;create&#x60;, &#x60;read&#x60;, &#x60;update&#x60;, &#x60;destroy&#x60;, &#x60;move&#x60;, &#x60;login&#x60;, &#x60;failedlogin&#x60;, &#x60;copy&#x60;, &#x60;user_create&#x60;, &#x60;user_update&#x60;, &#x60;user_destroy&#x60;, &#x60;group_create&#x60;, &#x60;group_update&#x60;, &#x60;group_destroy&#x60;, &#x60;permission_create&#x60;, &#x60;permission_destroy&#x60;, &#x60;api_key_create&#x60;, &#x60;api_key_update&#x60;, &#x60;api_key_destroy&#x60; (optional)
   * @param queryInterface Filter results by this this interface type. Valid values: &#x60;web&#x60;, &#x60;ftp&#x60;, &#x60;robot&#x60;, &#x60;jsapi&#x60;, &#x60;webdesktopapi&#x60;, &#x60;sftp&#x60;, &#x60;dav&#x60;, &#x60;desktop&#x60;, &#x60;restapi&#x60;, &#x60;scim&#x60;, &#x60;office&#x60; (optional)
   * @param queryUserId Return results that are actions performed by the user indiciated by this User ID (optional)
   * @param queryFileId Return results that are file actions related to the file indicated by this File ID (optional)
   * @param queryParentId Return results that are file actions inside the parent folder specified by this folder ID (optional)
   * @param queryPath Return results that are file actions related to this path. (optional)
   * @param queryFolder Return results that are file actions related to files or folders inside this folder path. (optional)
   * @param querySrc Return results that are file moves originating from this path. (optional)
   * @param queryDestination Return results that are file moves with this path as destination. (optional)
   * @param queryIp Filter results by this IP address. (optional)
   * @param queryUsername Filter results by this username. (optional)
   * @param queryFailureType If searching for Histories about login failures, this parameter restricts results to failures of this specific type.  Valid values: &#x60;expired_trial&#x60;, &#x60;account_overdue&#x60;, &#x60;locked_out&#x60;, &#x60;ip_mismatch&#x60;, &#x60;password_mismatch&#x60;, &#x60;site_mismatch&#x60;, &#x60;username_not_found&#x60;, &#x60;none&#x60;, &#x60;no_ftp_permission&#x60;, &#x60;no_web_permission&#x60;, &#x60;no_directory&#x60;, &#x60;errno_enoent&#x60;, &#x60;no_sftp_permission&#x60;, &#x60;no_dav_permission&#x60;, &#x60;no_restapi_permission&#x60;, &#x60;key_mismatch&#x60;, &#x60;region_mismatch&#x60;, &#x60;expired_access&#x60;, &#x60;desktop_ip_mismatch&#x60;, &#x60;desktop_api_key_not_used_quickly_enough&#x60;, &#x60;disabled&#x60;, &#x60;country_mismatch&#x60; (optional)
   * @param queryTargetId If searching for Histories about specific objects (such as Users, or API Keys), this paremeter restricts results to objects that match this ID. (optional)
   * @param queryTargetName If searching for Histories about Users, Groups or other objects with names, this parameter restricts results to objects with this name/username. (optional)
   * @param queryTargetPermission If searching for Histories about Permisisons, this parameter restricts results to permissions of this level. (optional)
   * @param queryTargetUserId If searching for Histories about API keys, this parameter restricts results to API keys created by/for this user ID. (optional)
   * @param queryTargetUsername If searching for Histories about API keys, this parameter restricts results to API keys created by/for this username. (optional)
   * @param queryTargetPlatform If searching for Histories about API keys, this parameter restricts results to API keys associated with this platform. (optional)
   * @param queryTargetPermissionSet If searching for Histories about API keys, this parameter restricts results to API keys with this permission set. (optional)
   * @return ApiResponse&lt;HistoryExportEntity&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<HistoryExportEntity> postHistoryExportsWithHttpInfo(Integer userId, DateTime startAt, DateTime endAt, String queryAction, String queryInterface, String queryUserId, String queryFileId, String queryParentId, String queryPath, String queryFolder, String querySrc, String queryDestination, String queryIp, String queryUsername, String queryFailureType, String queryTargetId, String queryTargetName, String queryTargetPermission, String queryTargetUserId, String queryTargetUsername, String queryTargetPlatform, String queryTargetPermissionSet) throws ApiException {
    Object localVarPostBody = null;
    
    // create path and map variables
    String localVarPath = "/history_exports";

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


    
    if (userId != null)
      localVarFormParams.put("user_id", userId);
if (startAt != null)
      localVarFormParams.put("start_at", startAt);
if (endAt != null)
      localVarFormParams.put("end_at", endAt);
if (queryAction != null)
      localVarFormParams.put("query_action", queryAction);
if (queryInterface != null)
      localVarFormParams.put("query_interface", queryInterface);
if (queryUserId != null)
      localVarFormParams.put("query_user_id", queryUserId);
if (queryFileId != null)
      localVarFormParams.put("query_file_id", queryFileId);
if (queryParentId != null)
      localVarFormParams.put("query_parent_id", queryParentId);
if (queryPath != null)
      localVarFormParams.put("query_path", queryPath);
if (queryFolder != null)
      localVarFormParams.put("query_folder", queryFolder);
if (querySrc != null)
      localVarFormParams.put("query_src", querySrc);
if (queryDestination != null)
      localVarFormParams.put("query_destination", queryDestination);
if (queryIp != null)
      localVarFormParams.put("query_ip", queryIp);
if (queryUsername != null)
      localVarFormParams.put("query_username", queryUsername);
if (queryFailureType != null)
      localVarFormParams.put("query_failure_type", queryFailureType);
if (queryTargetId != null)
      localVarFormParams.put("query_target_id", queryTargetId);
if (queryTargetName != null)
      localVarFormParams.put("query_target_name", queryTargetName);
if (queryTargetPermission != null)
      localVarFormParams.put("query_target_permission", queryTargetPermission);
if (queryTargetUserId != null)
      localVarFormParams.put("query_target_user_id", queryTargetUserId);
if (queryTargetUsername != null)
      localVarFormParams.put("query_target_username", queryTargetUsername);
if (queryTargetPlatform != null)
      localVarFormParams.put("query_target_platform", queryTargetPlatform);
if (queryTargetPermissionSet != null)
      localVarFormParams.put("query_target_permission_set", queryTargetPermissionSet);

    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json", "application/x-www-form-urlencoded", "multipart/form-data"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] {  };

    GenericType<HistoryExportEntity> localVarReturnType = new GenericType<HistoryExportEntity>() {};
    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
}
