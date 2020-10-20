package ch.cyberduck.core.sds.io.swagger.client.api;

import ch.cyberduck.core.sds.io.swagger.client.ApiException;
import ch.cyberduck.core.sds.io.swagger.client.ApiClient;
import ch.cyberduck.core.sds.io.swagger.client.Configuration;
import ch.cyberduck.core.sds.io.swagger.client.Pair;

import javax.ws.rs.core.GenericType;

import ch.cyberduck.core.sds.io.swagger.client.model.ErrorResponse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.JavaClientCodegen", date = "2020-10-15T15:35:23.522373+02:00[Europe/Zurich]")public class JobsApi {
  private ApiClient apiClient;

  public JobsApi() {
    this(Configuration.getDefaultApiClient());
  }

  public JobsApi(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  public ApiClient getApiClient() {
    return apiClient;
  }

  public void setApiClient(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  /**
   * Trigger a database cleanup job
   * ### Available Database Cleanup Jobs: &lt;details open style&#x3D;\&quot;padding: 10px; background-color: #F6F7F8; border: 1px solid #AAA; border-radius: 5px;\&quot;&gt; &lt;summary style&#x3D;\&quot;cursor: pointer; outline: none\&quot;&gt;&lt;strong&gt;Expand&lt;/strong&gt;&lt;/summary&gt;  | Name | Description | | :--- | :--- | | &#x60;authTokensCleanupJob&#x60; | Deletes expired authentication tokens (&#x60;X-Sds-Auth-Token&#x60;). | | &#x60;openIdStatesCleanupJob&#x60; | Deletes expired OpenId Connect states. | | &#x60;oAuthCodesCleanupJob&#x60; | Deletes expired OAuth codes. | | &#x60;oAuthAuthorizationsCleanupJob&#x60; | Deletes expired OAuth authorizations. | | &#x60;oAuthApprovalsCleanupJob&#x60;&lt;sup style&#x3D;\&quot;color:green;\&quot;&gt;&lt;b&gt; * new&lt;/b&gt;&lt;/sup&gt; | Deletes expired OAuth client approvals. | | &#x60;expiredSharesCleanupJob&#x60; | Deletes expired Up-/Download Shares. | | &#x60;expiredUsersCleanupJob&#x60; | Deletes expired users. | | &#x60;expiredGroupsCleanupJob&#x60; | Deletes expired groups. | | &#x60;expiredFilesCleanupJob&#x60; | Deletes expired files. | | &#x60;syslogCleanupJob&#x60; | Deletes log events according to configured retention period value. | | &#x60;uploadChannelsCleanupJob&#x60; | Deletes canceled upload channels. | | &#x60;emptyRecyclebinJob&#x60; | Deletes recycled files after they have been removed from recycle bin. | | &#x60;deletedCustomerCleanupJob&#x60; | Deletes customers which were marked for deletion. | | &#x60;loginPasswordExpirationJob&#x60; | Expires login passwords if password expiration is enabled and last password change date is before current date minus &#x60;maxPasswordAge&#x60;. |  | &#x60;s3FailedLocalDeleteCleanupJob&#x60; | Tries to delete files on NFS with status &#x60;LOCAL_DELETE_FAILED (4)&#x60;. | | &#x60;expiredWebhooksCleanupJob&#x60; | Deletes expired customer and tenant webhooks. |  &lt;/details&gt; 
   * @param jobName Name of the job (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @throws ApiException if fails to make API call
   */
  public void startCleanupJob(String jobName, String xSdsAuthToken) throws ApiException {
    Object localVarPostBody = null;
    // verify the required parameter 'jobName' is set
    if (jobName == null) {
      throw new ApiException(400, "Missing the required parameter 'jobName' when calling startCleanupJob");
    }
    // create path and map variables
    String localVarPath = "/v4/system/jobs/cleanup/start";

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    localVarQueryParams.addAll(apiClient.parameterToPairs("", "jobName", jobName));

    if (xSdsAuthToken != null)
      localVarHeaderParams.put("X-Sds-Auth-Token", apiClient.parameterToString(xSdsAuthToken));


    final String[] localVarAccepts = {
      "*/*"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "oauth2" };

    apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, null);
  }
  /**
   * Trigger a database job
   * ### Available Database Jobs: &lt;details open style&#x3D;\&quot;padding: 10px; background-color: #F6F7F8; border: 1px solid #AAA; border-radius: 5px;\&quot;&gt; &lt;summary style&#x3D;\&quot;cursor: pointer; outline: none\&quot;&gt;&lt;strong&gt;Expand&lt;/strong&gt;&lt;/summary&gt;  | Name | Description | | :--- | :--- | | &#x60;monthlyNotificationJob&#x60; | Sends email containing information (user licences, quota, trial days etc.) about all customers on all tenants. | | &#x60;fileHashJob&#x60; | Calculates hash value for files. | | &#x60;filePathMigrationJob&#x60; | Starts migration of file path for affected files. | | &#x60;fileSystemCleanupJob&#x60; | Deletes all **NOT** referenced files from system. | | &#x60;s3MigrationJob&#x60; | Uploads files to configured S3 storage. | | &#x60;s3TagsStatusUpdateJob&#x60; | Sets S3 tags status for files | | &#x60;s3SetTagsJob&#x60; | Creates S3 tags for affected files on configured S3 storage. | | &#x60;s3CopyJob&#x60; | Copies file with their tags on S3 storage. | | &#x60;defaultUserAvatarJob&#x60; | Creates default avatars for users who do not have one yet. | | &#x60;expiringWebhooksWarningJob&#x60; | Sends a message to the queue before a webhook is about to expire. | | &#x60;userNotificationMigrationJob&#x60; | Sends a message to the queue to create initial notification configs. |  &lt;/details&gt;
   * @param jobName Name of the job (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @throws ApiException if fails to make API call
   */
  public void startJob(String jobName, String xSdsAuthToken) throws ApiException {
    Object localVarPostBody = null;
    // verify the required parameter 'jobName' is set
    if (jobName == null) {
      throw new ApiException(400, "Missing the required parameter 'jobName' when calling startJob");
    }
    // create path and map variables
    String localVarPath = "/v4/system/jobs/start";

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    localVarQueryParams.addAll(apiClient.parameterToPairs("", "jobName", jobName));

    if (xSdsAuthToken != null)
      localVarHeaderParams.put("X-Sds-Auth-Token", apiClient.parameterToString(xSdsAuthToken));


    final String[] localVarAccepts = {
      "*/*"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "oauth2" };

    apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, null);
  }
}
