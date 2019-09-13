package ch.cyberduck.core.sds.io.swagger.client.api;

import ch.cyberduck.core.sds.io.swagger.client.ApiClient;
import ch.cyberduck.core.sds.io.swagger.client.ApiException;
import ch.cyberduck.core.sds.io.swagger.client.ApiResponse;
import ch.cyberduck.core.sds.io.swagger.client.Configuration;
import ch.cyberduck.core.sds.io.swagger.client.Pair;
import ch.cyberduck.core.sds.io.swagger.client.model.CustomerSettingsRequest;
import ch.cyberduck.core.sds.io.swagger.client.model.CustomerSettingsResponse;

import javax.ws.rs.core.GenericType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaClientCodegen", date = "2019-09-13T14:08:20.178+02:00")
public class SettingsApi {
    private ApiClient apiClient;

    public SettingsApi() {
        this(Configuration.getDefaultApiClient());
    }

    public SettingsApi(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public ApiClient getApiClient() {
        return apiClient;
    }

    public void setApiClient(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    /**
     * Get customer settings
     * ### Functional Description:   Retrieve customer related settings.   ### Precondition: Right _\&quot;read config\&quot;_ required.  ### Effects: None.  ### &amp;#9432; Further Information: None.  ### Configurable customer settings  | Setting | Description | Value | | :--- | :--- | :--- | | **&#x60;homeRoomParentName&#x60;** | Name of the container in which all user&#39;s home rooms are located.&lt;br&gt;&#x60;null&#x60; if **&#x60;homeRoomsActive&#x60;** is &#x60;false&#x60;. | &#x60;String&#x60; | | **&#x60;homeRoomQuota&#x60;** | Refers to the quota of each single user&#39;s home room.&lt;br&gt;&#x60;0&#x60; represents no quota.&lt;br&gt;&#x60;null&#x60; if **&#x60;homeRoomsActive&#x60;** is &#x60;false&#x60;. | &#x60;positive Long&#x60; | | **&#x60;homeRoomsActive&#x60;** | If set to &#x60;true&#x60;, every user with an Active Directory account gets a personal homeroom.&lt;br&gt;Once activated, this **CANNOT** be deactivated. | &#x60;true or false&#x60; |
     *
     * @param xSdsAuthToken Authentication token (optional)
     * @return CustomerSettingsResponse
     * @throws ApiException if fails to make API call
     */
    public CustomerSettingsResponse getSettings(String xSdsAuthToken) throws ApiException {
        return getSettingsWithHttpInfo(xSdsAuthToken).getData();
    }

    /**
     * Get customer settings
     * ### Functional Description:   Retrieve customer related settings.   ### Precondition: Right _\&quot;read config\&quot;_ required.  ### Effects: None.  ### &amp;#9432; Further Information: None.  ### Configurable customer settings  | Setting | Description | Value | | :--- | :--- | :--- | | **&#x60;homeRoomParentName&#x60;** | Name of the container in which all user&#39;s home rooms are located.&lt;br&gt;&#x60;null&#x60; if **&#x60;homeRoomsActive&#x60;** is &#x60;false&#x60;. | &#x60;String&#x60; | | **&#x60;homeRoomQuota&#x60;** | Refers to the quota of each single user&#39;s home room.&lt;br&gt;&#x60;0&#x60; represents no quota.&lt;br&gt;&#x60;null&#x60; if **&#x60;homeRoomsActive&#x60;** is &#x60;false&#x60;. | &#x60;positive Long&#x60; | | **&#x60;homeRoomsActive&#x60;** | If set to &#x60;true&#x60;, every user with an Active Directory account gets a personal homeroom.&lt;br&gt;Once activated, this **CANNOT** be deactivated. | &#x60;true or false&#x60; |
     *
     * @param xSdsAuthToken Authentication token (optional)
     * @return ApiResponse&lt;CustomerSettingsResponse&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<CustomerSettingsResponse> getSettingsWithHttpInfo(String xSdsAuthToken) throws ApiException {
        Object localVarPostBody = null;

        // create path and map variables
        String localVarPath = "/v4/settings";

        // query params
        List<Pair> localVarQueryParams = new ArrayList<Pair>();
        Map<String, String> localVarHeaderParams = new HashMap<String, String>();
        Map<String, Object> localVarFormParams = new HashMap<String, Object>();


        if(xSdsAuthToken != null) {
            localVarHeaderParams.put("X-Sds-Auth-Token", apiClient.parameterToString(xSdsAuthToken));
        }


        final String[] localVarAccepts = {
            "application/json;charset=UTF-8"
        };
        final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

        final String[] localVarContentTypes = {

        };
        final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[]{"DRACOON-OAuth"};

        GenericType<CustomerSettingsResponse> localVarReturnType = new GenericType<CustomerSettingsResponse>() {
        };
        return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Set customer settings
     * ### Functional Description:   Set customer related settings.  ### Precondition: Right _\&quot;change config\&quot;_ required.   Role _\&quot;Config Manager\&quot;_.  ### Effects: Home Room configuration is updated.   ### &amp;#9432; Further Information: None.  ### Configurable customer settings  | Setting | Description | Value | | :--- | :--- | :--- | | **&#x60;homeRoomParentName&#x60;** | Name of the container in which all user&#39;s home rooms are located.&lt;br&gt;&#x60;null&#x60; if **&#x60;homeRoomsActive&#x60;** is &#x60;false&#x60;. | &#x60;String&#x60; | | **&#x60;homeRoomQuota&#x60;** | Refers to the quota of each single user&#39;s home room.&lt;br&gt;&#x60;0&#x60; represents no quota.&lt;br&gt;&#x60;null&#x60; if **&#x60;homeRoomsActive&#x60;** is &#x60;false&#x60;. | &#x60;positive Long&#x60; | | **&#x60;homeRoomsActive&#x60;** | If set to &#x60;true&#x60;, every user with an Active Directory account gets a personal homeroom.&lt;br&gt;Once activated, this **CANNOT** be deactivated. | &#x60;true or false&#x60; |  ### Node naming convention  * Node (room, folder, file) names are limited to **150** characters.  * Not allowed names:   &#x60;&#39;CON&#39;, &#39;PRN&#39;, &#39;AUX&#39;, &#39;NUL&#39;, &#39;COM1&#39;, &#39;COM2&#39;, &#39;COM3&#39;, &#39;COM4&#39;, &#39;COM5&#39;, &#39;COM6&#39;, &#39;COM7&#39;, &#39;COM8&#39;, &#39;COM9&#39;, &#39;LPT1&#39;, &#39;LPT2&#39;, &#39;LPT3&#39;, &#39;LPT4&#39;, &#39;LPT5&#39;, &#39;LPT6&#39;, &#39;LPT7&#39;, &#39;LPT8&#39;, &#39;LPT9&#39;, (and any of those with an extension)&#x60;  * Not allowed characters in names:   &#x60;&#39;\\\\&#39;, &#39;&lt;&#39;,&#39;&gt;&#39;, &#39;:&#39;, &#39;\\\&quot;&#39;, &#39;|&#39;, &#39;?&#39;, &#39;*&#39;, &#39;/&#39;, leading &#39;-&#39;, trailing &#39;.&#39; &#x60;
     *
     * @param body          body (required)
     * @param xSdsAuthToken Authentication token (optional)
     * @return CustomerSettingsResponse
     * @throws ApiException if fails to make API call
     */
    public CustomerSettingsResponse setSettings(CustomerSettingsRequest body, String xSdsAuthToken) throws ApiException {
        return setSettingsWithHttpInfo(body, xSdsAuthToken).getData();
    }

    /**
     * Set customer settings
     * ### Functional Description:   Set customer related settings.  ### Precondition: Right _\&quot;change config\&quot;_ required.   Role _\&quot;Config Manager\&quot;_.  ### Effects: Home Room configuration is updated.   ### &amp;#9432; Further Information: None.  ### Configurable customer settings  | Setting | Description | Value | | :--- | :--- | :--- | | **&#x60;homeRoomParentName&#x60;** | Name of the container in which all user&#39;s home rooms are located.&lt;br&gt;&#x60;null&#x60; if **&#x60;homeRoomsActive&#x60;** is &#x60;false&#x60;. | &#x60;String&#x60; | | **&#x60;homeRoomQuota&#x60;** | Refers to the quota of each single user&#39;s home room.&lt;br&gt;&#x60;0&#x60; represents no quota.&lt;br&gt;&#x60;null&#x60; if **&#x60;homeRoomsActive&#x60;** is &#x60;false&#x60;. | &#x60;positive Long&#x60; | | **&#x60;homeRoomsActive&#x60;** | If set to &#x60;true&#x60;, every user with an Active Directory account gets a personal homeroom.&lt;br&gt;Once activated, this **CANNOT** be deactivated. | &#x60;true or false&#x60; |  ### Node naming convention  * Node (room, folder, file) names are limited to **150** characters.  * Not allowed names:   &#x60;&#39;CON&#39;, &#39;PRN&#39;, &#39;AUX&#39;, &#39;NUL&#39;, &#39;COM1&#39;, &#39;COM2&#39;, &#39;COM3&#39;, &#39;COM4&#39;, &#39;COM5&#39;, &#39;COM6&#39;, &#39;COM7&#39;, &#39;COM8&#39;, &#39;COM9&#39;, &#39;LPT1&#39;, &#39;LPT2&#39;, &#39;LPT3&#39;, &#39;LPT4&#39;, &#39;LPT5&#39;, &#39;LPT6&#39;, &#39;LPT7&#39;, &#39;LPT8&#39;, &#39;LPT9&#39;, (and any of those with an extension)&#x60;  * Not allowed characters in names:   &#x60;&#39;\\\\&#39;, &#39;&lt;&#39;,&#39;&gt;&#39;, &#39;:&#39;, &#39;\\\&quot;&#39;, &#39;|&#39;, &#39;?&#39;, &#39;*&#39;, &#39;/&#39;, leading &#39;-&#39;, trailing &#39;.&#39; &#x60;
     *
     * @param body          body (required)
     * @param xSdsAuthToken Authentication token (optional)
     * @return ApiResponse&lt;CustomerSettingsResponse&gt;
     * @throws ApiException if fails to make API call
     */
    public ApiResponse<CustomerSettingsResponse> setSettingsWithHttpInfo(CustomerSettingsRequest body, String xSdsAuthToken) throws ApiException {
        Object localVarPostBody = body;

        // verify the required parameter 'body' is set
        if(body == null) {
            throw new ApiException(400, "Missing the required parameter 'body' when calling setSettings");
        }

        // create path and map variables
        String localVarPath = "/v4/settings";

        // query params
        List<Pair> localVarQueryParams = new ArrayList<Pair>();
        Map<String, String> localVarHeaderParams = new HashMap<String, String>();
        Map<String, Object> localVarFormParams = new HashMap<String, Object>();


        if(xSdsAuthToken != null) {
            localVarHeaderParams.put("X-Sds-Auth-Token", apiClient.parameterToString(xSdsAuthToken));
        }


        final String[] localVarAccepts = {
            "application/json;charset=UTF-8"
        };
        final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

        final String[] localVarContentTypes = {
            "application/json;charset=UTF-8"
        };
        final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[]{"DRACOON-OAuth"};

        GenericType<CustomerSettingsResponse> localVarReturnType = new GenericType<CustomerSettingsResponse>() {
        };
        return apiClient.invokeAPI(localVarPath, "PUT", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }
}
