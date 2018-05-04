package ch.cyberduck.core.sds.io.swagger.client.api;

import ch.cyberduck.core.sds.io.swagger.client.ApiException;
import ch.cyberduck.core.sds.io.swagger.client.ApiClient;
import ch.cyberduck.core.sds.io.swagger.client.Configuration;
import ch.cyberduck.core.sds.io.swagger.client.Pair;

import javax.ws.rs.core.GenericType;

import ch.cyberduck.core.sds.io.swagger.client.model.AuditNodeResponse;
import org.joda.time.LocalDate;
import ch.cyberduck.core.sds.io.swagger.client.model.LogEventList;
import ch.cyberduck.core.sds.io.swagger.client.model.LogOperationList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaClientCodegen", date = "2018-05-03T10:55:56.129+02:00")
public class EventlogApi {
  private ApiClient apiClient;

  public EventlogApi() {
    this(Configuration.getDefaultApiClient());
  }

  public EventlogApi(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  public ApiClient getApiClient() {
    return apiClient;
  }

  public void setApiClient(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  /**
   * Get node assigned users with permissions 
   * &lt;div class&#x3D;\&quot;sds\&quot;&gt;&lt;p&gt;&lt;strong&gt;Functional Description:&lt;/strong&gt; &lt;br /&gt;Retrieve a list of all Nodes types ROOM and the room assignment users with permissions.&lt;/p&gt;&lt;p&gt;&lt;strong&gt;Precondition:&lt;/strong&gt; Right \&quot;Read node tree for audit logs\&quot; required.&lt;/p&gt;&lt;p&gt;&lt;strong&gt;Effects:&lt;/strong&gt; None.&lt;/p&gt;&lt;p&gt;&lt;strong&gt;Further Information:&lt;/strong&gt; None.&lt;/p&gt;&lt;/div&gt;&lt;div class&#x3D;\&quot;sds\&quot;&gt;&lt;h4&gt;Filter&lt;/h4&gt;&lt;p&gt;Filter string syntax: &lt;dfn&gt;&amp;lt;FIELD_NAME&amp;gt;:&amp;lt;OPERATOR&amp;gt;:&amp;lt;VALUE&amp;gt;&lt;/dfn&gt;&lt;br/&gt;Multiple fields are supported.&lt;/p&gt;&lt;h5&gt;Filter fields:&lt;/h5&gt;&lt;dl&gt;&lt;dt&gt;nodeId&lt;/dt&gt;&lt;dd&gt;Node ID&lt;br/&gt;OPERATOR: &lt;code&gt;eq&lt;/code&gt; (Node ID equal value)&lt;br/&gt;VALUE: &lt;code&gt;Search string&lt;/code&gt;&lt;/dd&gt;&lt;dt&gt;nodeName&lt;/dt&gt;&lt;dd&gt;Node name (Login)&lt;br/&gt;OPERATOR: &lt;code&gt;cn|eq&lt;/code&gt; (Node name contains value | equal value)&lt;br/&gt;VALUE: &lt;code&gt;Search string&lt;/code&gt;&lt;/dd&gt;&lt;dt&gt;nodeParentId&lt;/dt&gt;&lt;dd&gt;Node parent ID&lt;br/&gt;OPERATOR: &lt;code&gt;eq&lt;/code&gt; (Parent node ID equal value. Parent ID 0 is the root nodes.)&lt;br/&gt;VALUE: &lt;code&gt;Search string&lt;/code&gt;&lt;/dd&gt;&lt;dt&gt;userId&lt;/dt&gt;&lt;dd&gt;User ID&lt;br/&gt;OPERATOR: &lt;code&gt;eq&lt;/code&gt; (User ID equal value)&lt;br/&gt;VALUE: &lt;code&gt;Search string&lt;/code&gt;&lt;/dd&gt;&lt;dt&gt;userName&lt;/dt&gt;&lt;dd&gt;User name (Login)&lt;br/&gt;OPERATOR: &lt;code&gt;cn|eq&lt;/code&gt; (User name contains value | equal value)&lt;br/&gt;VALUE: &lt;code&gt;Search string&lt;/code&gt;&lt;/dd&gt;&lt;dt&gt;userFirstName&lt;/dt&gt;&lt;dd&gt;User first name&lt;br/&gt;OPERATOR: &lt;code&gt;cn|eq&lt;/code&gt; (User first name contains value | equal value)&lt;br/&gt;VALUE: &lt;code&gt;Search string&lt;/code&gt;&lt;/dd&gt;&lt;dt&gt;userLastName&lt;/dt&gt;&lt;dd&gt;User last name&lt;br/&gt;OPERATOR: &lt;code&gt;cn|eq&lt;/code&gt; (User last name contains value | equal value)&lt;br/&gt;VALUE: &lt;code&gt;Search string&lt;/code&gt;&lt;/dd&gt;&lt;dt&gt;permissionsManage&lt;/dt&gt;&lt;dd&gt;&lt;br/&gt;Filter the users that have/don&#39;t have manage right in this room&lt;br/&gt;OPERATOR: &lt;code&gt;eq&lt;/code&gt; (multiple values not allowed)&lt;br/&gt;VALUE: [true|false].&lt;/dd&gt;&lt;dt&gt;nodeIsEncrypted&lt;/dt&gt;&lt;dd&gt;Encrypted node filter&lt;br/&gt;OPERATOR: &lt;code&gt;eq&lt;/code&gt; (multiple values not allowed)&lt;br/&gt;VALUE: [true|false].&lt;/dd&gt;&lt;dt&gt;nodeHasRecycleBin&lt;/dt&gt;&lt;dd&gt;&lt;br/&gt;Recycle bin filter&lt;br/&gt;OPERATOR: &lt;code&gt;eq&lt;/code&gt; (multiple values not allowed)&lt;br/&gt;VALUE: [true|false].&lt;/dd&gt;&lt;/dl&gt;&lt;p&gt;&lt;b&gt;Logical grouping:&lt;/b&gt; filtering according first three fields (login, lastName, firstName)&lt;br&gt;is intrinsically processed by the API as logical &lt;i&gt;OR&lt;/i&gt;.  In opposite, filtering according to&lt;br/&gt;is processed intrinsically as logical &lt;i&gt;AND&lt;/i&gt;.&lt;/p&gt;&lt;p&gt;Example: &lt;samp&gt;userName:cn:searchString_1|userFirstName:cn:searchString_2|nodeId:eq:2 &lt;/samp&gt;&lt;br/&gt;- filter by user login contains searchString_1 or firstName contains searchString_2 and node ID equal 2&lt;/p&gt;&lt;h4&gt;Sort&lt;/h4&gt;&lt;p&gt;Sort string syntax: &lt;dfn&gt;&amp;lt;FIELD_NAME&amp;gt;:&amp;lt;ORDER&amp;gt;&lt;/dfn&gt;&lt;br/&gt;Order can be &lt;code&gt;asc&lt;/code&gt; or &lt;code&gt;desc&lt;/code&gt;&lt;br/&gt;Multiple fields are supported.&lt;/p&gt;&lt;h5&gt;Sort fields:&lt;/h5&gt;&lt;dl&gt;&lt;dt&gt;nodeId&lt;/dt&gt;&lt;dd&gt;Node Id&lt;/dd&gt;&lt;dt&gt;nodeName&lt;/dt&gt;&lt;dd&gt;Node name&lt;/dd&gt;&lt;dt&gt;nodeParentId&lt;/dt&gt;&lt;dd&gt;Node parent ID&lt;/dd&gt;&lt;dt&gt;nodeSize&lt;/dt&gt;&lt;dd&gt;Node size&lt;/dd&gt;&lt;dt&gt;nodeQuota&lt;/dt&gt;&lt;dd&gt;Node quota&lt;/dd&gt;&lt;/dl&gt;&lt;p&gt;Example: &lt;samp&gt;nodeName:asc&lt;/samp&gt;&lt;br/&gt;- sort by nodeName ascending&lt;/p&gt;&lt;/div&gt;
   * @param xSdsAuthToken Authentication token (required)
   * @param xSdsDateFormat DateTimeFormat: LOCAL/UTC/OFFSET/EPOCH (optional)
   * @param filter Filter string (optional)
   * @param sort Sorting string (optional)
   * @return List&lt;AuditNodeResponse&gt;
   * @throws ApiException if fails to make API call
   */
  public List<AuditNodeResponse> getAuditNodeUserData(String xSdsAuthToken, String xSdsDateFormat, String filter, String sort) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'xSdsAuthToken' is set
    if (xSdsAuthToken == null) {
      throw new ApiException(400, "Missing the required parameter 'xSdsAuthToken' when calling getAuditNodeUserData");
    }
    
    // create path and map variables
    String localVarPath = "/eventlog/audits/nodes";

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    localVarQueryParams.addAll(apiClient.parameterToPairs("", "filter", filter));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "sort", sort));

    if (xSdsAuthToken != null)
      localVarHeaderParams.put("X-Sds-Auth-Token", apiClient.parameterToString(xSdsAuthToken));
if (xSdsDateFormat != null)
      localVarHeaderParams.put("X-Sds-Date-Format", apiClient.parameterToString(xSdsDateFormat));

    
    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] {  };

    GenericType<List<AuditNodeResponse>> localVarReturnType = new GenericType<List<AuditNodeResponse>>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Get system events
   * &lt;div class&#x3D;\&quot;sds\&quot;&gt;&lt;p&gt;&lt;strong&gt;Functional Description:&lt;/strong&gt; &lt;br/&gt;Retrieve eventlog (&#x3D; audit log) events.&lt;/p&gt;&lt;p&gt;&lt;strong&gt;Precondition:&lt;/strong&gt; Role \&quot;Log Auditor\&quot; required.&lt;/p&gt;&lt;p&gt;&lt;strong&gt;Effects:&lt;/strong&gt; None.&lt;/p&gt;&lt;p&gt;&lt;strong&gt;Further Information:&lt;/strong&gt; Output may be limited to a certain number of entries. Please use filter criteria and paging.&lt;/p&gt;&lt;/div&gt;
   * @param xSdsAuthToken Authentication token (required)
   * @param xSdsDateFormat DateTimeFormat: LOCAL/UTC/OFFSET/EPOCH (optional)
   * @param offset Range offset (optional)
   * @param limit Range limit (optional)
   * @param dateStart Start date (2015-12-31T23:59:00) (optional)
   * @param dateEnd End date (2015-12-31T23:59:00) (optional)
   * @param type Operation ID: see GET/eventlog/operationstype (optional)
   * @param userId User ID (optional)
   * @param status Operation status: 0 &#x3D; SUCCESS, 2 &#x3D; ERROR (optional)
   * @param userClient User client (optional)
   * @return LogEventList
   * @throws ApiException if fails to make API call
   */
  public LogEventList getLogEvents(String xSdsAuthToken, String xSdsDateFormat, Integer offset, Integer limit, LocalDate dateStart, LocalDate dateEnd, List<Integer> type, List<Integer> userId, List<Integer> status, List<String> userClient) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'xSdsAuthToken' is set
    if (xSdsAuthToken == null) {
      throw new ApiException(400, "Missing the required parameter 'xSdsAuthToken' when calling getLogEvents");
    }
    
    // create path and map variables
    String localVarPath = "/eventlog/events";

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    localVarQueryParams.addAll(apiClient.parameterToPairs("", "offset", offset));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "limit", limit));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "date_start", dateStart));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "date_end", dateEnd));
    localVarQueryParams.addAll(apiClient.parameterToPairs("csv", "type", type));
    localVarQueryParams.addAll(apiClient.parameterToPairs("csv", "user_id", userId));
    localVarQueryParams.addAll(apiClient.parameterToPairs("csv", "status", status));
    localVarQueryParams.addAll(apiClient.parameterToPairs("csv", "user_client", userClient));

    if (xSdsAuthToken != null)
      localVarHeaderParams.put("X-Sds-Auth-Token", apiClient.parameterToString(xSdsAuthToken));
if (xSdsDateFormat != null)
      localVarHeaderParams.put("X-Sds-Date-Format", apiClient.parameterToString(xSdsDateFormat));

    
    final String[] localVarAccepts = {
      "application/json", "text/csv"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] {  };

    GenericType<LogEventList> localVarReturnType = new GenericType<LogEventList>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Get allowed Log Operations
   * &lt;div class&#x3D;\&quot;sds\&quot;&gt;&lt;p&gt;&lt;strong&gt;Functional Description:&lt;/strong&gt; &lt;br/&gt;Retrieve eventlog (&#x3D; audit log) operation IDs and the associated Log Operation Description.&lt;/p&gt;&lt;p&gt;&lt;strong&gt;Precondition:&lt;/strong&gt; Role Log Auditor required.&lt;/p&gt;&lt;p&gt;&lt;strong&gt;Effects:&lt;/strong&gt; None.&lt;/p&gt;&lt;p&gt;&lt;strong&gt;Further Information:&lt;/strong&gt; None.&lt;/p&gt;&lt;/div&gt;
   * @param xSdsAuthToken Authentication token (required)
   * @return LogOperationList
   * @throws ApiException if fails to make API call
   */
  public LogOperationList getLogOperations(String xSdsAuthToken) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'xSdsAuthToken' is set
    if (xSdsAuthToken == null) {
      throw new ApiException(400, "Missing the required parameter 'xSdsAuthToken' when calling getLogOperations");
    }
    
    // create path and map variables
    String localVarPath = "/eventlog/operations";

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


    if (xSdsAuthToken != null)
      localVarHeaderParams.put("X-Sds-Auth-Token", apiClient.parameterToString(xSdsAuthToken));

    
    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] {  };

    GenericType<LogOperationList> localVarReturnType = new GenericType<LogOperationList>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
}
