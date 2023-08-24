package ch.cyberduck.core.storegate.io.swagger.client.api;

import ch.cyberduck.core.storegate.io.swagger.client.ApiException;
import ch.cyberduck.core.storegate.io.swagger.client.ApiClient;
import ch.cyberduck.core.storegate.io.swagger.client.ApiResponse;
import ch.cyberduck.core.storegate.io.swagger.client.Configuration;
import ch.cyberduck.core.storegate.io.swagger.client.Pair;

import javax.ws.rs.core.GenericType;

import ch.cyberduck.core.storegate.io.swagger.client.model.AlertSettings;
import ch.cyberduck.core.storegate.io.swagger.client.model.AlertSettingsRequest;
import ch.cyberduck.core.storegate.io.swagger.client.model.BackupReportSettings;
import ch.cyberduck.core.storegate.io.swagger.client.model.BackupReportSettingsRequest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaClientCodegen", date = "2023-08-24T11:36:23.792+02:00")
public class NotificationsApi {
  private ApiClient apiClient;

  public NotificationsApi() {
    this(Configuration.getDefaultApiClient());
  }

  public NotificationsApi(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  public ApiClient getApiClient() {
    return apiClient;
  }

  public void setApiClient(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  /**
   * Get alert settings.
   * 
   * @return AlertSettings
   * @throws ApiException if fails to make API call
   */
  public AlertSettings notificationsGetAlerts() throws ApiException {
    return notificationsGetAlertsWithHttpInfo().getData();
      }

  /**
   * Get alert settings.
   * 
   * @return ApiResponse&lt;AlertSettings&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<AlertSettings> notificationsGetAlertsWithHttpInfo() throws ApiException {
    Object localVarPostBody = null;
    
    // create path and map variables
    String localVarPath = "/v4.2/notifications/alerts";

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


    
    
    final String[] localVarAccepts = {
      "application/json", "text/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "oauth2" };

    GenericType<AlertSettings> localVarReturnType = new GenericType<AlertSettings>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Get backup report settings.
   * 
   * @return BackupReportSettings
   * @throws ApiException if fails to make API call
   */
  public BackupReportSettings notificationsGetBackupReports() throws ApiException {
    return notificationsGetBackupReportsWithHttpInfo().getData();
      }

  /**
   * Get backup report settings.
   * 
   * @return ApiResponse&lt;BackupReportSettings&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<BackupReportSettings> notificationsGetBackupReportsWithHttpInfo() throws ApiException {
    Object localVarPostBody = null;
    
    // create path and map variables
    String localVarPath = "/v4.2/notifications/backupreports";

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


    
    
    final String[] localVarAccepts = {
      "application/json", "text/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "oauth2" };

    GenericType<BackupReportSettings> localVarReturnType = new GenericType<BackupReportSettings>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Update alert settings.
   * 
   * @param alertSettings  (required)
   * @throws ApiException if fails to make API call
   */
  public void notificationsUpdateAlerts(AlertSettingsRequest alertSettings) throws ApiException {

    notificationsUpdateAlertsWithHttpInfo(alertSettings);
  }

  /**
   * Update alert settings.
   * 
   * @param alertSettings  (required)
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<Void> notificationsUpdateAlertsWithHttpInfo(AlertSettingsRequest alertSettings) throws ApiException {
    Object localVarPostBody = alertSettings;
    
    // verify the required parameter 'alertSettings' is set
    if (alertSettings == null) {
      throw new ApiException(400, "Missing the required parameter 'alertSettings' when calling notificationsUpdateAlerts");
    }
    
    // create path and map variables
    String localVarPath = "/v4.2/notifications/alerts";

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


    
    
    final String[] localVarAccepts = {
      
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json", "text/json"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "oauth2" };


    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, null);
  }
  /**
   * Update backup report settings.
   * 
   * @param backupReportSettings  (required)
   * @throws ApiException if fails to make API call
   */
  public void notificationsUpdateBackupReports(BackupReportSettingsRequest backupReportSettings) throws ApiException {

    notificationsUpdateBackupReportsWithHttpInfo(backupReportSettings);
  }

  /**
   * Update backup report settings.
   * 
   * @param backupReportSettings  (required)
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<Void> notificationsUpdateBackupReportsWithHttpInfo(BackupReportSettingsRequest backupReportSettings) throws ApiException {
    Object localVarPostBody = backupReportSettings;
    
    // verify the required parameter 'backupReportSettings' is set
    if (backupReportSettings == null) {
      throw new ApiException(400, "Missing the required parameter 'backupReportSettings' when calling notificationsUpdateBackupReports");
    }
    
    // create path and map variables
    String localVarPath = "/v4.2/notifications/backupreports";

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


    
    
    final String[] localVarAccepts = {
      
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json", "text/json"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "oauth2" };


    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, null);
  }
}
