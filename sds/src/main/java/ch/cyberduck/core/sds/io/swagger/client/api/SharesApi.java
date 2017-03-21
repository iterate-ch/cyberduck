package ch.cyberduck.core.sds.io.swagger.client.api;

import ch.cyberduck.core.sds.io.swagger.client.ApiException;
import ch.cyberduck.core.sds.io.swagger.client.ApiClient;
import ch.cyberduck.core.sds.io.swagger.client.Configuration;
import ch.cyberduck.core.sds.io.swagger.client.Pair;

import javax.ws.rs.core.GenericType;

import ch.cyberduck.core.sds.io.swagger.client.model.CreateDownloadShareRequest;
import ch.cyberduck.core.sds.io.swagger.client.model.CreateUploadShareRequest;
import ch.cyberduck.core.sds.io.swagger.client.model.DownloadShare;
import ch.cyberduck.core.sds.io.swagger.client.model.DownloadShareList;
import ch.cyberduck.core.sds.io.swagger.client.model.UploadShare;
import ch.cyberduck.core.sds.io.swagger.client.model.UploadShareList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaClientCodegen", date = "2017-03-21T19:15:03.115+01:00")
public class SharesApi {
  private ApiClient apiClient;

  public SharesApi() {
    this(Configuration.getDefaultApiClient());
  }

  public SharesApi(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  public ApiClient getApiClient() {
    return apiClient;
  }

  public void setApiClient(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  /**
   * Create new download share
   * &lt;div class&#x3D;\&quot;sds\&quot;&gt;&lt;p&gt;&lt;strong&gt;Functional Description:&lt;/strong&gt; &lt;br/&gt;Create a new Download Share.&lt;/p&gt;&lt;p&gt;&lt;strong&gt;Precondition:&lt;/strong&gt; Manage download shares permission on target node.&lt;/p&gt;&lt;p&gt;&lt;strong&gt;Effects:&lt;/strong&gt; Download Share created.&lt;/p&gt;&lt;p&gt;&lt;strong&gt;Further Information:&lt;/strong&gt;&lt;ul&gt;&lt;li&gt;&lt;b&gt;Access password:&lt;/b&gt; Passwords are limited to 150 characters.&lt;/li&gt;&lt;li&gt;&lt;b&gt;Notes:&lt;/b&gt; Notes are limited to 255 characters.&lt;/li&gt;&lt;li&gt;&lt;b&gt;Alias Names:&lt;/b&gt; Alias names are limited to 150 characters.&lt;/li&gt;&lt;/ul&gt;&lt;/p&gt;&lt;/div&gt;
   * @param xSdsAuthToken Authentication token (required)
   * @param body  (required)
   * @param xSdsDateFormat DateTimeFormat: LOCAL/UTC/OFFSET/EPOCH (optional)
   * @return DownloadShare
   * @throws ApiException if fails to make API call
   */
  public DownloadShare createDownloadShare(String xSdsAuthToken, CreateDownloadShareRequest body, String xSdsDateFormat) throws ApiException {
    Object localVarPostBody = body;
    
    // verify the required parameter 'xSdsAuthToken' is set
    if (xSdsAuthToken == null) {
      throw new ApiException(400, "Missing the required parameter 'xSdsAuthToken' when calling createDownloadShare");
    }
    
    // verify the required parameter 'body' is set
    if (body == null) {
      throw new ApiException(400, "Missing the required parameter 'body' when calling createDownloadShare");
    }
    
    // create path and map variables
    String localVarPath = "/shares/downloads".replaceAll("\\{format\\}","json");

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


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

    GenericType<DownloadShare> localVarReturnType = new GenericType<DownloadShare>() {};
    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Create new upload share
   * &lt;div class&#x3D;\&quot;sds\&quot;&gt;&lt;p&gt;&lt;strong&gt;Functional Description:&lt;/strong&gt; &lt;br/&gt;Create a new Upload Share (aka Upload Account).&lt;/p&gt;&lt;p&gt;&lt;strong&gt;Precondition:&lt;/strong&gt; Manage Upload Shares permission on target container.&lt;/p&gt;&lt;p&gt;&lt;strong&gt;Effects:&lt;/strong&gt; Upload Share is created.&lt;/p&gt;&lt;p&gt;&lt;strong&gt;Further Information:&lt;/strong&gt;&lt;ul&gt;&lt;li&gt;&lt;b&gt;Notes:&lt;/b&gt; Notes are limited to 255 characters.&lt;/li&gt;&lt;li&gt;&lt;b&gt;Alias Names:&lt;/b&gt; Alias names are limited to 150 characters.&lt;/li&gt;&lt;li&gt;&lt;div&gt;&lt;b&gt;Send Mail:&lt;/b&gt; Following fields are optional, if &lt;dfn&gt;&amp;lt;sendMail&amp;gt;&lt;/dfn&gt; is set to false: &lt;dfn&gt;&amp;lt;mailRecipients&amp;gt; &amp;lt;mailSubject&amp;gt; &amp;lt;mailBody&amp;gt;&lt;/dfn&gt;.&lt;br/&gt;If &lt;dfn&gt;&amp;lt;sendMail&amp;gt;&lt;/dfn&gt; is set to true, these fields are mandatory fields:  &lt;dfn&gt;&amp;lt;mailRecipients&amp;gt; &amp;lt;mailSubject&amp;gt; &amp;lt;mailBody&amp;gt;&lt;/dfn&gt;.&lt;/div&gt;&lt;/li&gt;&lt;/ul&gt;&lt;/p&gt;&lt;/div&gt;
   * @param xSdsAuthToken Authentication token (required)
   * @param body  (required)
   * @param xSdsDateFormat DateTimeFormat: LOCAL/UTC/OFFSET/EPOCH (optional)
   * @return UploadShare
   * @throws ApiException if fails to make API call
   */
  public UploadShare createUploadShare(String xSdsAuthToken, CreateUploadShareRequest body, String xSdsDateFormat) throws ApiException {
    Object localVarPostBody = body;
    
    // verify the required parameter 'xSdsAuthToken' is set
    if (xSdsAuthToken == null) {
      throw new ApiException(400, "Missing the required parameter 'xSdsAuthToken' when calling createUploadShare");
    }
    
    // verify the required parameter 'body' is set
    if (body == null) {
      throw new ApiException(400, "Missing the required parameter 'body' when calling createUploadShare");
    }
    
    // create path and map variables
    String localVarPath = "/shares/uploads".replaceAll("\\{format\\}","json");

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


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

    GenericType<UploadShare> localVarReturnType = new GenericType<UploadShare>() {};
    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Delete download share
   * &lt;div class&#x3D;\&quot;sds\&quot;&gt;&lt;p&gt;&lt;strong&gt;Functional Description:&lt;/strong&gt; &lt;br/&gt;Delete a Download Share.&lt;/p&gt;&lt;p&gt;&lt;strong&gt;Precondition:&lt;/strong&gt; None.&lt;/p&gt;&lt;p&gt;&lt;strong&gt;Effects:&lt;/strong&gt; Download Share is deleted.&lt;/p&gt;&lt;p&gt;&lt;strong&gt;Further Information:&lt;/strong&gt; Only the Download Share is removed; the referenced file or container persists.&lt;/p&gt;&lt;/div&gt;
   * @param xSdsAuthToken Authentication token (required)
   * @param shareId Share Id (required)
   * @throws ApiException if fails to make API call
   */
  public void deleteDownloadShare(String xSdsAuthToken, Long shareId) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'xSdsAuthToken' is set
    if (xSdsAuthToken == null) {
      throw new ApiException(400, "Missing the required parameter 'xSdsAuthToken' when calling deleteDownloadShare");
    }
    
    // verify the required parameter 'shareId' is set
    if (shareId == null) {
      throw new ApiException(400, "Missing the required parameter 'shareId' when calling deleteDownloadShare");
    }
    
    // create path and map variables
    String localVarPath = "/shares/downloads/{share_id}".replaceAll("\\{format\\}","json")
      .replaceAll("\\{" + "share_id" + "\\}", apiClient.escapeString(shareId.toString()));

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


    apiClient.invokeAPI(localVarPath, "DELETE", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, null);
  }
  /**
   * Delete upload share
   * &lt;div class&#x3D;\&quot;sds\&quot;&gt;&lt;p&gt;&lt;strong&gt;Functional Description:&lt;/strong&gt; &lt;br/&gt;Delete an Upload Share (aka Upload Account).&lt;/p&gt;&lt;p&gt;&lt;strong&gt;Precondition:&lt;/strong&gt; None.&lt;/p&gt;&lt;p&gt;&lt;strong&gt;Effects:&lt;/strong&gt; Upload Share is deleted.&lt;/p&gt;&lt;p&gt;&lt;strong&gt;Further Information:&lt;/strong&gt; Only the Upload Share is removed; already uploaded files and the target container persist.&lt;/p&gt;&lt;/div&gt;
   * @param xSdsAuthToken Authentication token (required)
   * @param shareId Share Id (required)
   * @throws ApiException if fails to make API call
   */
  public void deleteUploadShare(String xSdsAuthToken, Long shareId) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'xSdsAuthToken' is set
    if (xSdsAuthToken == null) {
      throw new ApiException(400, "Missing the required parameter 'xSdsAuthToken' when calling deleteUploadShare");
    }
    
    // verify the required parameter 'shareId' is set
    if (shareId == null) {
      throw new ApiException(400, "Missing the required parameter 'shareId' when calling deleteUploadShare");
    }
    
    // create path and map variables
    String localVarPath = "/shares/uploads/{share_id}".replaceAll("\\{format\\}","json")
      .replaceAll("\\{" + "share_id" + "\\}", apiClient.escapeString(shareId.toString()));

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


    apiClient.invokeAPI(localVarPath, "DELETE", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, null);
  }
  /**
   * Get download share
   * &lt;div class&#x3D;\&quot;sds\&quot;&gt;&lt;p&gt;&lt;strong&gt;Functional Description:&lt;/strong&gt; &lt;br/&gt;Retrieve detailed information about one Download Share.&lt;/p&gt;&lt;p&gt;&lt;strong&gt;Precondition:&lt;/strong&gt; None.&lt;/p&gt;&lt;p&gt;&lt;strong&gt;Effects:&lt;/strong&gt; None.&lt;/p&gt;&lt;p&gt;&lt;strong&gt;Further Information:&lt;/strong&gt; None.&lt;/p&gt;&lt;/div&gt;
   * @param xSdsAuthToken Authentication token (required)
   * @param shareId Share Id (required)
   * @param xSdsDateFormat DateTimeFormat: LOCAL/UTC/OFFSET/EPOCH (optional)
   * @return DownloadShare
   * @throws ApiException if fails to make API call
   */
  public DownloadShare getDownloadShare(String xSdsAuthToken, Long shareId, String xSdsDateFormat) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'xSdsAuthToken' is set
    if (xSdsAuthToken == null) {
      throw new ApiException(400, "Missing the required parameter 'xSdsAuthToken' when calling getDownloadShare");
    }
    
    // verify the required parameter 'shareId' is set
    if (shareId == null) {
      throw new ApiException(400, "Missing the required parameter 'shareId' when calling getDownloadShare");
    }
    
    // create path and map variables
    String localVarPath = "/shares/downloads/{share_id}".replaceAll("\\{format\\}","json")
      .replaceAll("\\{" + "share_id" + "\\}", apiClient.escapeString(shareId.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


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

    GenericType<DownloadShare> localVarReturnType = new GenericType<DownloadShare>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Get download shares
   * &lt;div class&#x3D;\&quot;sds\&quot;&gt;&lt;p&gt;&lt;strong&gt;Functional Description:&lt;/strong&gt; &lt;br/&gt;Retrieve a list of Download Shares.&lt;/p&gt;&lt;p&gt;&lt;strong&gt;Precondition:&lt;/strong&gt; None.&lt;/p&gt;&lt;p&gt;&lt;strong&gt;Effects:&lt;/strong&gt; None.&lt;/p&gt;&lt;p&gt;&lt;strong&gt;Further Information:&lt;/strong&gt; None.&lt;/p&gt;&lt;h4&gt;Filters&lt;/h4&gt;&lt;p&gt;Filter string syntax: &lt;dfn&gt;&amp;lt;FIELD_NAME&amp;gt;:&amp;lt;OPERATOR&amp;gt;:&amp;lt;VALUE&amp;gt;&lt;/dfn&gt;&lt;br/&gt;Multiple fields is supported.&lt;/p&gt;&lt;h5&gt;Filter fields:&lt;/h5&gt;&lt;dl&gt;&lt;dt&gt;name&lt;/dt&gt;&lt;dd&gt;Alias Name or File Name&lt;br/&gt;OPERATOR: &lt;code&gt;cn&lt;/code&gt; ( name contains value)&lt;br/&gt;VALUE: &lt;code&gt;Search string&lt;/code&gt;&lt;/dd&gt;&lt;dt&gt;createdBy&lt;/dt&gt;&lt;dd&gt;Creator info&lt;br/&gt;OPERATOR: &lt;code&gt;cn&lt;/code&gt; (Creator info (login, email, firstName, lastName ) contains value)&lt;br/&gt;VALUE: &lt;code&gt;Search string&lt;/code&gt;&lt;/dd&gt;&lt;dt&gt;accessKey&lt;/dt&gt;&lt;dd&gt;Share access key&lt;br/&gt;OPERATOR: &lt;code&gt;cn&lt;/code&gt; (Share access key contains value)&lt;br/&gt;VALUE: &lt;code&gt;Search string&lt;/code&gt;&lt;/dd&gt;&lt;dt&gt;nodeId&lt;/dt&gt;&lt;dd&gt;Source node ID&lt;br/&gt;OPERATOR: &lt;code&gt;eq&lt;/code&gt; (Source file/folder/Room/SubRoom ID)&lt;br/&gt;VALUE: &lt;code&gt;Search node ID&lt;/code&gt;&lt;/dd&gt;&lt;dt&gt;userId&lt;/dt&gt;&lt;dd&gt;Creator user ID&lt;br/&gt;OPERATOR: &lt;code&gt;eq&lt;/code&gt;&lt;br/&gt;VALUE: &lt;code&gt;Search user ID&lt;/code&gt;&lt;/dd&gt;&lt;/dl&gt;&lt;p&gt;Example: &lt;samp&gt;name:cn:searchString_1|createdBy:cn:searchString_2|nodeId:eq:1 &lt;/samp&gt;&lt;br/&gt;- filter by file name contains searchString_1 or creator info (login, email, firstName, lastName ) contains searchString_2 or node ID is equal to 1&lt;/p&gt;&lt;h4&gt;Sorting&lt;/h4&gt;&lt;p&gt;Sort string syntax: &lt;dfn&gt;&amp;lt;FIELD_NAME&amp;gt;:&amp;lt;ORDER&amp;gt;&lt;/dfn&gt;&lt;br/&gt;Order can be &lt;code&gt;asc&lt;/code&gt; or &lt;code&gt;desc&lt;/code&gt;&lt;br/&gt;Multiple fields is supported.&lt;/p&gt;&lt;h5&gt;Sort fields:&lt;/h5&gt;&lt;dl&gt;&lt;dt&gt;name&lt;/dt&gt;&lt;dd&gt;Alias Name or File Name&lt;/dd&gt;&lt;dt&gt;classification&lt;/dt&gt;&lt;dd&gt;File classification&lt;/dd&gt;&lt;dt&gt;notifyCreator&lt;/dt&gt;&lt;dd&gt;Notify creator on every download&lt;/dd&gt;&lt;dt&gt;expireAt&lt;/dt&gt;&lt;dd&gt;Expiration date&lt;/dd&gt;&lt;dt&gt;createdAt&lt;/dt&gt;&lt;dd&gt;Creation date&lt;/dd&gt;&lt;dt&gt;createdBy&lt;/dt&gt;&lt;dd&gt;Creator info&lt;/dd&gt;&lt;/dl&gt;&lt;p&gt;Example: &lt;samp&gt;name:asc|expireAt:desc&lt;/samp&gt;&lt;br/&gt;- sort by name ascending and by expireAt descending&lt;/p&gt;&lt;/div&gt;
   * @param xSdsAuthToken Authentication token (required)
   * @param xSdsDateFormat DateTimeFormat: LOCAL/UTC/OFFSET/EPOCH (optional)
   * @param offset Range offset (optional)
   * @param limit Range limit (optional)
   * @param filter Filter string (optional)
   * @param sort Sort string (optional)
   * @return DownloadShareList
   * @throws ApiException if fails to make API call
   */
  public DownloadShareList getDownloadShares(String xSdsAuthToken, String xSdsDateFormat, Integer offset, Integer limit, String filter, String sort) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'xSdsAuthToken' is set
    if (xSdsAuthToken == null) {
      throw new ApiException(400, "Missing the required parameter 'xSdsAuthToken' when calling getDownloadShares");
    }
    
    // create path and map variables
    String localVarPath = "/shares/downloads".replaceAll("\\{format\\}","json");

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    localVarQueryParams.addAll(apiClient.parameterToPairs("", "offset", offset));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "limit", limit));
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

    GenericType<DownloadShareList> localVarReturnType = new GenericType<DownloadShareList>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Get upload share
   * &lt;div class&#x3D;\&quot;sds\&quot;&gt;&lt;p&gt;&lt;strong&gt;Functional Description:&lt;/strong&gt; &lt;br/&gt;Retrieve detailed information about one Upload Share (aka Upload Account).&lt;/p&gt;&lt;p&gt;&lt;strong&gt;Precondition:&lt;/strong&gt; None.&lt;/p&gt;&lt;p&gt;&lt;strong&gt;Effects:&lt;/strong&gt; None.&lt;/p&gt;&lt;p&gt;&lt;strong&gt;Further Information:&lt;/strong&gt; None.&lt;/p&gt;&lt;/div&gt;
   * @param xSdsAuthToken Authentication token (required)
   * @param shareId Share Id (required)
   * @param xSdsDateFormat DateTimeFormat: LOCAL/UTC/OFFSET/EPOCH (optional)
   * @return UploadShare
   * @throws ApiException if fails to make API call
   */
  public UploadShare getUploadShare(String xSdsAuthToken, Long shareId, String xSdsDateFormat) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'xSdsAuthToken' is set
    if (xSdsAuthToken == null) {
      throw new ApiException(400, "Missing the required parameter 'xSdsAuthToken' when calling getUploadShare");
    }
    
    // verify the required parameter 'shareId' is set
    if (shareId == null) {
      throw new ApiException(400, "Missing the required parameter 'shareId' when calling getUploadShare");
    }
    
    // create path and map variables
    String localVarPath = "/shares/uploads/{share_id}".replaceAll("\\{format\\}","json")
      .replaceAll("\\{" + "share_id" + "\\}", apiClient.escapeString(shareId.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


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

    GenericType<UploadShare> localVarReturnType = new GenericType<UploadShare>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Get upload shares
   * &lt;div class&#x3D;\&quot;sds\&quot;&gt;&lt;p&gt;&lt;strong&gt;Functional Description:&lt;/strong&gt; &lt;br/&gt;Retrieve a list of Upload Shares (aka Upload Accounts).&lt;/p&gt;&lt;p&gt;&lt;strong&gt;Precondition:&lt;/strong&gt; None.&lt;/p&gt;&lt;p&gt;&lt;strong&gt;Effects:&lt;/strong&gt; None.&lt;/p&gt;&lt;p&gt;&lt;strong&gt;Further Information:&lt;/strong&gt; None.&lt;/p&gt;&lt;h4&gt;Filters&lt;/h4&gt;&lt;p&gt;Filter string syntax: &lt;dfn&gt;&amp;lt;FIELD_NAME&amp;gt;:&amp;lt;OPERATOR&amp;gt;:&amp;lt;VALUE&amp;gt;&lt;/dfn&gt;&lt;br/&gt;Multiple fields is supported.&lt;/p&gt;&lt;h5&gt;Filter fields:&lt;/h5&gt;&lt;dl&gt;&lt;dt&gt;name&lt;/dt&gt;&lt;dd&gt;Alias name&lt;br/&gt;OPERATOR: &lt;code&gt;cn&lt;/code&gt; (Alias name contains value)&lt;br/&gt;VALUE: &lt;code&gt;Search string&lt;/code&gt;&lt;/dd&gt;&lt;dt&gt;createdBy&lt;/dt&gt;&lt;dd&gt;Creator info&lt;br/&gt;OPERATOR: &lt;code&gt;cn&lt;/code&gt; (Creator info (login, email, firstName, lastName ) contains value)&lt;br/&gt;VALUE: &lt;code&gt;Search string&lt;/code&gt;&lt;/dd&gt;&lt;dt&gt;accessKey&lt;/dt&gt;&lt;dd&gt;Share access key&lt;br/&gt;OPERATOR: &lt;code&gt;cn&lt;/code&gt; (Share access key contains value)&lt;br/&gt;VALUE: &lt;code&gt;Search string&lt;/code&gt;&lt;/dd&gt;&lt;dt&gt;targetId&lt;/dt&gt;&lt;dd&gt;Target node Room/SubRoom/Folder&lt;br/&gt;OPERATOR: &lt;code&gt;cn&lt;/code&gt; (Target node contains value)&lt;br/&gt;VALUE: &lt;code&gt;Search string&lt;/code&gt;&lt;/dd&gt;&lt;dt&gt;userId&lt;/dt&gt;&lt;dd&gt;Creator user ID&lt;br/&gt;OPERATOR: &lt;code&gt;eq&lt;/code&gt;&lt;br/&gt;VALUE: &lt;code&gt;Search user ID&lt;/code&gt;&lt;/dd&gt;&lt;/dl&gt;&lt;p&gt;Example: &lt;samp&gt;name:cn:searchString_1|createdBy:cn:searchString_2 &lt;/samp&gt;&lt;br/&gt;- filter by alias name contains searchString_1 or creator info (login, email, firstName, lastName ) contains searchString_2&lt;/p&gt;&lt;h4&gt;Sorting&lt;/h4&gt;&lt;p&gt;Sort string syntax: &lt;dfn&gt;&amp;lt;FIELD_NAME&amp;gt;:&amp;lt;ORDER&amp;gt;&lt;/dfn&gt;&lt;br/&gt;Order can be &lt;code&gt;asc&lt;/code&gt; or &lt;code&gt;desc&lt;/code&gt;&lt;br/&gt;Multiple fields is supported.&lt;/p&gt;&lt;h5&gt;Sort fields:&lt;/h5&gt;&lt;dl&gt;&lt;dt&gt;name&lt;/dt&gt;&lt;dd&gt;Alias name&lt;/dd&gt;&lt;dt&gt;expireAt&lt;/dt&gt;&lt;dd&gt;Expiration date&lt;/dd&gt;&lt;dt&gt;notifyCreator&lt;/dt&gt;&lt;dd&gt;Notify creator on every upload.&lt;/dd&gt;&lt;dt&gt;createdAt&lt;/dt&gt;&lt;dd&gt;Creation date&lt;/dd&gt;&lt;dt&gt;createdBy&lt;/dt&gt;&lt;dd&gt;Creator info&lt;/dd&gt;&lt;/dl&gt;&lt;p&gt;Example: &lt;samp&gt;name:asc|expireAt:desc&lt;/samp&gt;&lt;br/&gt;- sort by name ascending and by expireAt descending&lt;/p&gt;&lt;/div&gt;
   * @param xSdsAuthToken Authentication token (required)
   * @param xSdsDateFormat DateTimeFormat: LOCAL/UTC/OFFSET/EPOCH (optional)
   * @param offset Range offset (optional)
   * @param limit Range limit (optional)
   * @param filter Filter string (optional)
   * @param sort Sort string (optional)
   * @return UploadShareList
   * @throws ApiException if fails to make API call
   */
  public UploadShareList getUploadShares(String xSdsAuthToken, String xSdsDateFormat, Integer offset, Integer limit, String filter, String sort) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'xSdsAuthToken' is set
    if (xSdsAuthToken == null) {
      throw new ApiException(400, "Missing the required parameter 'xSdsAuthToken' when calling getUploadShares");
    }
    
    // create path and map variables
    String localVarPath = "/shares/uploads".replaceAll("\\{format\\}","json");

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    localVarQueryParams.addAll(apiClient.parameterToPairs("", "offset", offset));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "limit", limit));
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

    GenericType<UploadShareList> localVarReturnType = new GenericType<UploadShareList>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
}
