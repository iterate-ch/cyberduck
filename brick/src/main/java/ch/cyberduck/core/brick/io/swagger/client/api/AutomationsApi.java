package ch.cyberduck.core.brick.io.swagger.client.api;

import ch.cyberduck.core.brick.io.swagger.client.ApiException;
import ch.cyberduck.core.brick.io.swagger.client.ApiClient;
import ch.cyberduck.core.brick.io.swagger.client.ApiResponse;
import ch.cyberduck.core.brick.io.swagger.client.Configuration;
import ch.cyberduck.core.brick.io.swagger.client.Pair;

import javax.ws.rs.core.GenericType;

import ch.cyberduck.core.brick.io.swagger.client.model.AutomationEntity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaClientCodegen", date = "2021-06-30T21:29:25.490+02:00")
public class AutomationsApi {
  private ApiClient apiClient;

  public AutomationsApi() {
    this(Configuration.getDefaultApiClient());
  }

  public AutomationsApi(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  public ApiClient getApiClient() {
    return apiClient;
  }

  public void setApiClient(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  /**
   * Delete Automation
   * Delete Automation
   * @param id Automation ID. (required)
   * @throws ApiException if fails to make API call
   */
  public void deleteAutomationsId(Integer id) throws ApiException {

    deleteAutomationsIdWithHttpInfo(id);
  }

  /**
   * Delete Automation
   * Delete Automation
   * @param id Automation ID. (required)
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<Void> deleteAutomationsIdWithHttpInfo(Integer id) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'id' is set
    if (id == null) {
      throw new ApiException(400, "Missing the required parameter 'id' when calling deleteAutomationsId");
    }
    
    // create path and map variables
    String localVarPath = "/automations/{id}"
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


    return apiClient.invokeAPI(localVarPath, "DELETE", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, null);
  }
  /**
   * List Automations
   * List Automations
   * @param cursor Used for pagination.  Send a cursor value to resume an existing list from the point at which you left off.  Get a cursor from an existing list via the X-Files-Cursor-Next header. (optional)
   * @param perPage Number of records to show per page.  (Max: 10,000, 1,000 or less is recommended). (optional)
   * @param sortBy If set, sort records by the specified field in either &#39;asc&#39; or &#39;desc&#39; direction (e.g. sort_by[last_login_at]&#x3D;desc). Valid fields are &#x60;automation&#x60;. (optional)
   * @param filter If set, return records where the specifiied field is equal to the supplied value. Valid fields are &#x60;automation&#x60;. (optional)
   * @param filterGt If set, return records where the specifiied field is greater than the supplied value. Valid fields are &#x60;automation&#x60;. (optional)
   * @param filterGteq If set, return records where the specifiied field is greater than or equal to the supplied value. Valid fields are &#x60;automation&#x60;. (optional)
   * @param filterLike If set, return records where the specifiied field is equal to the supplied value. Valid fields are &#x60;automation&#x60;. (optional)
   * @param filterLt If set, return records where the specifiied field is less than the supplied value. Valid fields are &#x60;automation&#x60;. (optional)
   * @param filterLteq If set, return records where the specifiied field is less than or equal to the supplied value. Valid fields are &#x60;automation&#x60;. (optional)
   * @param automation DEPRECATED: Type of automation to filter by. Use &#x60;filter[automation]&#x60; instead. (optional)
   * @return List&lt;AutomationEntity&gt;
   * @throws ApiException if fails to make API call
   */
  public List<AutomationEntity> getAutomations(String cursor, Integer perPage, Map<String, String> sortBy, Map<String, String> filter, Map<String, String> filterGt, Map<String, String> filterGteq, Map<String, String> filterLike, Map<String, String> filterLt, Map<String, String> filterLteq, String automation) throws ApiException {
    return getAutomationsWithHttpInfo(cursor, perPage, sortBy, filter, filterGt, filterGteq, filterLike, filterLt, filterLteq, automation).getData();
      }

  /**
   * List Automations
   * List Automations
   * @param cursor Used for pagination.  Send a cursor value to resume an existing list from the point at which you left off.  Get a cursor from an existing list via the X-Files-Cursor-Next header. (optional)
   * @param perPage Number of records to show per page.  (Max: 10,000, 1,000 or less is recommended). (optional)
   * @param sortBy If set, sort records by the specified field in either &#39;asc&#39; or &#39;desc&#39; direction (e.g. sort_by[last_login_at]&#x3D;desc). Valid fields are &#x60;automation&#x60;. (optional)
   * @param filter If set, return records where the specifiied field is equal to the supplied value. Valid fields are &#x60;automation&#x60;. (optional)
   * @param filterGt If set, return records where the specifiied field is greater than the supplied value. Valid fields are &#x60;automation&#x60;. (optional)
   * @param filterGteq If set, return records where the specifiied field is greater than or equal to the supplied value. Valid fields are &#x60;automation&#x60;. (optional)
   * @param filterLike If set, return records where the specifiied field is equal to the supplied value. Valid fields are &#x60;automation&#x60;. (optional)
   * @param filterLt If set, return records where the specifiied field is less than the supplied value. Valid fields are &#x60;automation&#x60;. (optional)
   * @param filterLteq If set, return records where the specifiied field is less than or equal to the supplied value. Valid fields are &#x60;automation&#x60;. (optional)
   * @param automation DEPRECATED: Type of automation to filter by. Use &#x60;filter[automation]&#x60; instead. (optional)
   * @return ApiResponse&lt;List&lt;AutomationEntity&gt;&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<List<AutomationEntity>> getAutomationsWithHttpInfo(String cursor, Integer perPage, Map<String, String> sortBy, Map<String, String> filter, Map<String, String> filterGt, Map<String, String> filterGteq, Map<String, String> filterLike, Map<String, String> filterLt, Map<String, String> filterLteq, String automation) throws ApiException {
    Object localVarPostBody = null;
    
    // create path and map variables
    String localVarPath = "/automations";

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    localVarQueryParams.addAll(apiClient.parameterToPairs("", "cursor", cursor));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "per_page", perPage));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "sort_by", sortBy));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "filter", filter));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "filter_gt", filterGt));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "filter_gteq", filterGteq));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "filter_like", filterLike));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "filter_lt", filterLt));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "filter_lteq", filterLteq));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "automation", automation));

    
    
    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] {  };

    GenericType<List<AutomationEntity>> localVarReturnType = new GenericType<List<AutomationEntity>>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Show Automation
   * Show Automation
   * @param id Automation ID. (required)
   * @return AutomationEntity
   * @throws ApiException if fails to make API call
   */
  public AutomationEntity getAutomationsId(Integer id) throws ApiException {
    return getAutomationsIdWithHttpInfo(id).getData();
      }

  /**
   * Show Automation
   * Show Automation
   * @param id Automation ID. (required)
   * @return ApiResponse&lt;AutomationEntity&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<AutomationEntity> getAutomationsIdWithHttpInfo(Integer id) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'id' is set
    if (id == null) {
      throw new ApiException(400, "Missing the required parameter 'id' when calling getAutomationsId");
    }
    
    // create path and map variables
    String localVarPath = "/automations/{id}"
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

    GenericType<AutomationEntity> localVarReturnType = new GenericType<AutomationEntity>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Update Automation
   * Update Automation
   * @param id Automation ID. (required)
   * @param automation Automation type (required)
   * @param source Source Path (optional)
   * @param destination DEPRECATED: Destination Path. Use &#x60;destinations&#x60; instead. (optional)
   * @param destinations A list of String destination paths or Hash of folder_path and optional file_path. (optional)
   * @param destinationReplaceFrom If set, this string in the destination path will be replaced with the value in &#x60;destination_replace_to&#x60;. (optional)
   * @param destinationReplaceTo If set, this string will replace the value &#x60;destination_replace_from&#x60; in the destination filename. You can use special patterns here. (optional)
   * @param interval How often to run this automation? One of: &#x60;day&#x60;, &#x60;week&#x60;, &#x60;week_end&#x60;, &#x60;month&#x60;, &#x60;month_end&#x60;, &#x60;quarter&#x60;, &#x60;quarter_end&#x60;, &#x60;year&#x60;, &#x60;year_end&#x60; (optional)
   * @param path Path on which this Automation runs.  Supports globs. (optional)
   * @param userIds A list of user IDs the automation is associated with. If sent as a string, it should be comma-delimited. (optional)
   * @param groupIds A list of group IDs the automation is associated with. If sent as a string, it should be comma-delimited. (optional)
   * @param schedule Custom schedule for running this automation. (optional)
   * @param trigger How this automation is triggered to run. One of: &#x60;realtime&#x60;, &#x60;daily&#x60;, &#x60;custom_schedule&#x60;, &#x60;webhook&#x60;, &#x60;email&#x60;, or &#x60;action&#x60;. (optional)
   * @param triggerActions If trigger is &#x60;action&#x60;, this is the list of action types on which to trigger the automation. Valid actions are create, read, update, destroy, move, copy (optional)
   * @param triggerActionPath If trigger is &#x60;action&#x60;, this is the path to watch for the specified trigger actions. (optional)
   * @param value A Hash of attributes specific to the automation type. (optional)
   * @return AutomationEntity
   * @throws ApiException if fails to make API call
   */
  public AutomationEntity patchAutomationsId(Integer id, String automation, String source, String destination, List<String> destinations, String destinationReplaceFrom, String destinationReplaceTo, String interval, String path, String userIds, String groupIds, Map<String, String> schedule, String trigger, List<String> triggerActions, String triggerActionPath, Map<String, String> value) throws ApiException {
    return patchAutomationsIdWithHttpInfo(id, automation, source, destination, destinations, destinationReplaceFrom, destinationReplaceTo, interval, path, userIds, groupIds, schedule, trigger, triggerActions, triggerActionPath, value).getData();
      }

  /**
   * Update Automation
   * Update Automation
   * @param id Automation ID. (required)
   * @param automation Automation type (required)
   * @param source Source Path (optional)
   * @param destination DEPRECATED: Destination Path. Use &#x60;destinations&#x60; instead. (optional)
   * @param destinations A list of String destination paths or Hash of folder_path and optional file_path. (optional)
   * @param destinationReplaceFrom If set, this string in the destination path will be replaced with the value in &#x60;destination_replace_to&#x60;. (optional)
   * @param destinationReplaceTo If set, this string will replace the value &#x60;destination_replace_from&#x60; in the destination filename. You can use special patterns here. (optional)
   * @param interval How often to run this automation? One of: &#x60;day&#x60;, &#x60;week&#x60;, &#x60;week_end&#x60;, &#x60;month&#x60;, &#x60;month_end&#x60;, &#x60;quarter&#x60;, &#x60;quarter_end&#x60;, &#x60;year&#x60;, &#x60;year_end&#x60; (optional)
   * @param path Path on which this Automation runs.  Supports globs. (optional)
   * @param userIds A list of user IDs the automation is associated with. If sent as a string, it should be comma-delimited. (optional)
   * @param groupIds A list of group IDs the automation is associated with. If sent as a string, it should be comma-delimited. (optional)
   * @param schedule Custom schedule for running this automation. (optional)
   * @param trigger How this automation is triggered to run. One of: &#x60;realtime&#x60;, &#x60;daily&#x60;, &#x60;custom_schedule&#x60;, &#x60;webhook&#x60;, &#x60;email&#x60;, or &#x60;action&#x60;. (optional)
   * @param triggerActions If trigger is &#x60;action&#x60;, this is the list of action types on which to trigger the automation. Valid actions are create, read, update, destroy, move, copy (optional)
   * @param triggerActionPath If trigger is &#x60;action&#x60;, this is the path to watch for the specified trigger actions. (optional)
   * @param value A Hash of attributes specific to the automation type. (optional)
   * @return ApiResponse&lt;AutomationEntity&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<AutomationEntity> patchAutomationsIdWithHttpInfo(Integer id, String automation, String source, String destination, List<String> destinations, String destinationReplaceFrom, String destinationReplaceTo, String interval, String path, String userIds, String groupIds, Map<String, String> schedule, String trigger, List<String> triggerActions, String triggerActionPath, Map<String, String> value) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'id' is set
    if (id == null) {
      throw new ApiException(400, "Missing the required parameter 'id' when calling patchAutomationsId");
    }
    
    // verify the required parameter 'automation' is set
    if (automation == null) {
      throw new ApiException(400, "Missing the required parameter 'automation' when calling patchAutomationsId");
    }
    
    // create path and map variables
    String localVarPath = "/automations/{id}"
      .replaceAll("\\{" + "id" + "\\}", apiClient.escapeString(id.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


    
    if (automation != null)
      localVarFormParams.put("automation", automation);
if (source != null)
      localVarFormParams.put("source", source);
if (destination != null)
      localVarFormParams.put("destination", destination);
if (destinations != null)
      localVarFormParams.put("destinations", destinations);
if (destinationReplaceFrom != null)
      localVarFormParams.put("destination_replace_from", destinationReplaceFrom);
if (destinationReplaceTo != null)
      localVarFormParams.put("destination_replace_to", destinationReplaceTo);
if (interval != null)
      localVarFormParams.put("interval", interval);
if (path != null)
      localVarFormParams.put("path", path);
if (userIds != null)
      localVarFormParams.put("user_ids", userIds);
if (groupIds != null)
      localVarFormParams.put("group_ids", groupIds);
if (schedule != null)
      localVarFormParams.put("schedule", schedule);
if (trigger != null)
      localVarFormParams.put("trigger", trigger);
if (triggerActions != null)
      localVarFormParams.put("trigger_actions", triggerActions);
if (triggerActionPath != null)
      localVarFormParams.put("trigger_action_path", triggerActionPath);
if (value != null)
      localVarFormParams.put("value", value);

    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json", "application/x-www-form-urlencoded", "multipart/form-data"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] {  };

    GenericType<AutomationEntity> localVarReturnType = new GenericType<AutomationEntity>() {};
    return apiClient.invokeAPI(localVarPath, "PATCH", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Create Automation
   * Create Automation
   * @param automation Automation type (required)
   * @param source Source Path (optional)
   * @param destination DEPRECATED: Destination Path. Use &#x60;destinations&#x60; instead. (optional)
   * @param destinations A list of String destination paths or Hash of folder_path and optional file_path. (optional)
   * @param destinationReplaceFrom If set, this string in the destination path will be replaced with the value in &#x60;destination_replace_to&#x60;. (optional)
   * @param destinationReplaceTo If set, this string will replace the value &#x60;destination_replace_from&#x60; in the destination filename. You can use special patterns here. (optional)
   * @param interval How often to run this automation? One of: &#x60;day&#x60;, &#x60;week&#x60;, &#x60;week_end&#x60;, &#x60;month&#x60;, &#x60;month_end&#x60;, &#x60;quarter&#x60;, &#x60;quarter_end&#x60;, &#x60;year&#x60;, &#x60;year_end&#x60; (optional)
   * @param path Path on which this Automation runs.  Supports globs. (optional)
   * @param userIds A list of user IDs the automation is associated with. If sent as a string, it should be comma-delimited. (optional)
   * @param groupIds A list of group IDs the automation is associated with. If sent as a string, it should be comma-delimited. (optional)
   * @param schedule Custom schedule for running this automation. (optional)
   * @param trigger How this automation is triggered to run. One of: &#x60;realtime&#x60;, &#x60;daily&#x60;, &#x60;custom_schedule&#x60;, &#x60;webhook&#x60;, &#x60;email&#x60;, or &#x60;action&#x60;. (optional)
   * @param triggerActions If trigger is &#x60;action&#x60;, this is the list of action types on which to trigger the automation. Valid actions are create, read, update, destroy, move, copy (optional)
   * @param triggerActionPath If trigger is &#x60;action&#x60;, this is the path to watch for the specified trigger actions. (optional)
   * @param value A Hash of attributes specific to the automation type. (optional)
   * @return AutomationEntity
   * @throws ApiException if fails to make API call
   */
  public AutomationEntity postAutomations(String automation, String source, String destination, List<String> destinations, String destinationReplaceFrom, String destinationReplaceTo, String interval, String path, String userIds, String groupIds, Map<String, String> schedule, String trigger, List<String> triggerActions, String triggerActionPath, Map<String, String> value) throws ApiException {
    return postAutomationsWithHttpInfo(automation, source, destination, destinations, destinationReplaceFrom, destinationReplaceTo, interval, path, userIds, groupIds, schedule, trigger, triggerActions, triggerActionPath, value).getData();
      }

  /**
   * Create Automation
   * Create Automation
   * @param automation Automation type (required)
   * @param source Source Path (optional)
   * @param destination DEPRECATED: Destination Path. Use &#x60;destinations&#x60; instead. (optional)
   * @param destinations A list of String destination paths or Hash of folder_path and optional file_path. (optional)
   * @param destinationReplaceFrom If set, this string in the destination path will be replaced with the value in &#x60;destination_replace_to&#x60;. (optional)
   * @param destinationReplaceTo If set, this string will replace the value &#x60;destination_replace_from&#x60; in the destination filename. You can use special patterns here. (optional)
   * @param interval How often to run this automation? One of: &#x60;day&#x60;, &#x60;week&#x60;, &#x60;week_end&#x60;, &#x60;month&#x60;, &#x60;month_end&#x60;, &#x60;quarter&#x60;, &#x60;quarter_end&#x60;, &#x60;year&#x60;, &#x60;year_end&#x60; (optional)
   * @param path Path on which this Automation runs.  Supports globs. (optional)
   * @param userIds A list of user IDs the automation is associated with. If sent as a string, it should be comma-delimited. (optional)
   * @param groupIds A list of group IDs the automation is associated with. If sent as a string, it should be comma-delimited. (optional)
   * @param schedule Custom schedule for running this automation. (optional)
   * @param trigger How this automation is triggered to run. One of: &#x60;realtime&#x60;, &#x60;daily&#x60;, &#x60;custom_schedule&#x60;, &#x60;webhook&#x60;, &#x60;email&#x60;, or &#x60;action&#x60;. (optional)
   * @param triggerActions If trigger is &#x60;action&#x60;, this is the list of action types on which to trigger the automation. Valid actions are create, read, update, destroy, move, copy (optional)
   * @param triggerActionPath If trigger is &#x60;action&#x60;, this is the path to watch for the specified trigger actions. (optional)
   * @param value A Hash of attributes specific to the automation type. (optional)
   * @return ApiResponse&lt;AutomationEntity&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<AutomationEntity> postAutomationsWithHttpInfo(String automation, String source, String destination, List<String> destinations, String destinationReplaceFrom, String destinationReplaceTo, String interval, String path, String userIds, String groupIds, Map<String, String> schedule, String trigger, List<String> triggerActions, String triggerActionPath, Map<String, String> value) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'automation' is set
    if (automation == null) {
      throw new ApiException(400, "Missing the required parameter 'automation' when calling postAutomations");
    }
    
    // create path and map variables
    String localVarPath = "/automations";

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


    
    if (automation != null)
      localVarFormParams.put("automation", automation);
if (source != null)
      localVarFormParams.put("source", source);
if (destination != null)
      localVarFormParams.put("destination", destination);
if (destinations != null)
      localVarFormParams.put("destinations", destinations);
if (destinationReplaceFrom != null)
      localVarFormParams.put("destination_replace_from", destinationReplaceFrom);
if (destinationReplaceTo != null)
      localVarFormParams.put("destination_replace_to", destinationReplaceTo);
if (interval != null)
      localVarFormParams.put("interval", interval);
if (path != null)
      localVarFormParams.put("path", path);
if (userIds != null)
      localVarFormParams.put("user_ids", userIds);
if (groupIds != null)
      localVarFormParams.put("group_ids", groupIds);
if (schedule != null)
      localVarFormParams.put("schedule", schedule);
if (trigger != null)
      localVarFormParams.put("trigger", trigger);
if (triggerActions != null)
      localVarFormParams.put("trigger_actions", triggerActions);
if (triggerActionPath != null)
      localVarFormParams.put("trigger_action_path", triggerActionPath);
if (value != null)
      localVarFormParams.put("value", value);

    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json", "application/x-www-form-urlencoded", "multipart/form-data"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] {  };

    GenericType<AutomationEntity> localVarReturnType = new GenericType<AutomationEntity>() {};
    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
}
