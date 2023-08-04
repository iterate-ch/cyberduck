package ch.cyberduck.core.sds.io.swagger.client.api;

import ch.cyberduck.core.sds.io.swagger.client.ApiException;
import ch.cyberduck.core.sds.io.swagger.client.ApiClient;
import ch.cyberduck.core.sds.io.swagger.client.Configuration;
import ch.cyberduck.core.sds.io.swagger.client.Pair;

import javax.ws.rs.core.GenericType;

import ch.cyberduck.core.sds.io.swagger.client.model.CreateDownloadShareRequest;
import ch.cyberduck.core.sds.io.swagger.client.model.CreateUploadShareRequest;
import ch.cyberduck.core.sds.io.swagger.client.model.DeleteDownloadSharesRequest;
import ch.cyberduck.core.sds.io.swagger.client.model.DeleteUploadSharesRequest;
import ch.cyberduck.core.sds.io.swagger.client.model.DownloadShare;
import ch.cyberduck.core.sds.io.swagger.client.model.DownloadShareLinkEmail;
import ch.cyberduck.core.sds.io.swagger.client.model.DownloadShareList;
import ch.cyberduck.core.sds.io.swagger.client.model.ErrorResponse;
import ch.cyberduck.core.sds.io.swagger.client.model.InlineResponse400;
import ch.cyberduck.core.sds.io.swagger.client.model.UpdateDownloadShareRequest;
import ch.cyberduck.core.sds.io.swagger.client.model.UpdateDownloadSharesBulkRequest;
import ch.cyberduck.core.sds.io.swagger.client.model.UpdateUploadShareRequest;
import ch.cyberduck.core.sds.io.swagger.client.model.UpdateUploadSharesBulkRequest;
import ch.cyberduck.core.sds.io.swagger.client.model.UploadShare;
import ch.cyberduck.core.sds.io.swagger.client.model.UploadShareLinkEmail;
import ch.cyberduck.core.sds.io.swagger.client.model.UploadShareList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
   * Create new Download Share
   * ### Description: Create a new Download Share.  ### Precondition: User with &lt;span style&#x3D;&#x27;padding: 3px; background-color: #F6F7F8; border: 1px solid #000; border-radius: 5px; display: inline;&#x27;&gt;&amp;#128275; manage download share&lt;/span&gt; permissions on target node.  ### Postcondition: Download Share is created.  ### Further Information:  If the target node is a room: subordinary rooms are excluded from a Download Share.  * &#x60;name&#x60; is limited to **150** characters. * &#x60;notes&#x60; are limited to **255** characters. * &#x60;password&#x60; is limited to **1024** characters.  Use &#x60;POST /shares/downloads/{share_id}/email&#x60; API for sending emails.    Forbidden characters in passwords: [&#x60;&amp;&#x60;, &#x60;&#x27;&#x60;, &#x60;&lt;&#x60;, &#x60;&gt;&#x60;]  Please keep in mind that due to various restrictions of different telecommunication providers, non-ASCII characters may not be displayed correctly in short messages (SMS).
   * @param body  (required)
   * @param xSdsDateFormat Date time format (cf. [RFC 3339](https://www.ietf.org/rfc/rfc3339.txt) &amp; [leettime.de](http://leettime.de/)) (optional)
   * @param xSdsAuthToken Authentication token (optional)
   * @return DownloadShare
   * @throws ApiException if fails to make API call
   */
  public DownloadShare createDownloadShare(CreateDownloadShareRequest body, String xSdsDateFormat, String xSdsAuthToken) throws ApiException {
    Object localVarPostBody = body;
    // verify the required parameter 'body' is set
    if (body == null) {
      throw new ApiException(400, "Missing the required parameter 'body' when calling createDownloadShare");
    }
    // create path and map variables
    String localVarPath = "/v4/shares/downloads";

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


    if (xSdsDateFormat != null)
      localVarHeaderParams.put("X-Sds-Date-Format", apiClient.parameterToString(xSdsDateFormat));
    if (xSdsAuthToken != null)
      localVarHeaderParams.put("X-Sds-Auth-Token", apiClient.parameterToString(xSdsAuthToken));

    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "oauth2" };

    GenericType<DownloadShare> localVarReturnType = new GenericType<DownloadShare>() {};
    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
  /**
   * Create new Upload Share
   * ### Description: Create a new Upload Share (aka File Request).  ### Precondition: User has &lt;span style&#x3D;&#x27;padding: 3px; background-color: #F6F7F8; border: 1px solid #000; border-radius: 5px; display: inline;&#x27;&gt;&amp;#128275; manage upload share&lt;/span&gt; permissions on target container.  ### Postcondition: Upload Share is created.  ### Further Information:  * &#x60;name&#x60; is limited to **150** characters. * &#x60;notes&#x60; are limited to **255** characters. * &#x60;password&#x60; is limited to **1024** characters.  Forbidden characters in passwords: [&#x60;&amp;&#x60;, &#x60;&#x27;&#x60;, &#x60;&lt;&#x60;, &#x60;&gt;&#x60;]    Use &#x60;POST /shares/uploads/{share_id}/email&#x60; API for sending emails.  Please keep in mind that due to various restrictions of different telecommunication providers, non-ASCII characters may not be displayed correctly in short messages (SMS).
   * @param body  (required)
   * @param xSdsDateFormat Date time format (cf. [RFC 3339](https://www.ietf.org/rfc/rfc3339.txt) &amp; [leettime.de](http://leettime.de/)) (optional)
   * @param xSdsAuthToken Authentication token (optional)
   * @return UploadShare
   * @throws ApiException if fails to make API call
   */
  public UploadShare createUploadShare(CreateUploadShareRequest body, String xSdsDateFormat, String xSdsAuthToken) throws ApiException {
    Object localVarPostBody = body;
    // verify the required parameter 'body' is set
    if (body == null) {
      throw new ApiException(400, "Missing the required parameter 'body' when calling createUploadShare");
    }
    // create path and map variables
    String localVarPath = "/v4/shares/uploads";

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


    if (xSdsDateFormat != null)
      localVarHeaderParams.put("X-Sds-Date-Format", apiClient.parameterToString(xSdsDateFormat));
    if (xSdsAuthToken != null)
      localVarHeaderParams.put("X-Sds-Auth-Token", apiClient.parameterToString(xSdsAuthToken));

    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "oauth2" };

    GenericType<UploadShare> localVarReturnType = new GenericType<UploadShare>() {};
    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
  /**
   * Remove Download Shares
   * &lt;h3 style&#x3D;&#x27;padding: 5px; background-color: #F6F7F8; border: 1px solid #AAA; border-radius: 5px; display: table-cell;&#x27;&gt;&amp;#128640; Since v4.21.0&lt;/h3&gt;  ### Functional Description: Delete multiple Download Shares.  ### Precondition: User with _\&quot;manage download share\&quot;_ permissions on target nodes.  ### Postcondition: Download Shares are deleted.  ### Further Information: Only the Download Shares are removed; the referenced files or containers persists.
   * @param body  (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @throws ApiException if fails to make API call
   */
  public void deleteDownloadShares(DeleteDownloadSharesRequest body, String xSdsAuthToken) throws ApiException {
    Object localVarPostBody = body;
    // verify the required parameter 'body' is set
    if (body == null) {
      throw new ApiException(400, "Missing the required parameter 'body' when calling deleteDownloadShares");
    }
    // create path and map variables
    String localVarPath = "/v4/shares/downloads";

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
      "application/json"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "oauth2" };

    apiClient.invokeAPI(localVarPath, "DELETE", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, null);
  }
  /**
   * Remove Upload Shares
   * &lt;h3 style&#x3D;&#x27;padding: 5px; background-color: #F6F7F8; border: 1px solid #AAA; border-radius: 5px; display: table-cell;&#x27;&gt;&amp;#128640; Since v4.21.0&lt;/h3&gt;  ### Functional Description: Delete multiple Upload Shares (aka Upload Accounts).  ### Precondition: User has _\&quot;manage upload share\&quot;_ permissions on target containers.  ### Postcondition: Upload Shares are deleted.  ### Further Information: Only the Upload Shares are removed; already uploaded files and the target container persist.
   * @param body  (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @throws ApiException if fails to make API call
   */
  public void deleteUploadShares(DeleteUploadSharesRequest body, String xSdsAuthToken) throws ApiException {
    Object localVarPostBody = body;
    // verify the required parameter 'body' is set
    if (body == null) {
      throw new ApiException(400, "Missing the required parameter 'body' when calling deleteUploadShares");
    }
    // create path and map variables
    String localVarPath = "/v4/shares/uploads";

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
      "application/json"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "oauth2" };

    apiClient.invokeAPI(localVarPath, "DELETE", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, null);
  }
  /**
   * Remove Download Share
   * ### Description: Delete a Download Share.  ### Precondition: User with &lt;span style&#x3D;&#x27;padding: 3px; background-color: #F6F7F8; border: 1px solid #000; border-radius: 5px; display: inline;&#x27;&gt;&amp;#128275; manage download share&lt;/span&gt; permissions on target node.  ### Postcondition: Download Share is deleted.  ### Further Information: Only the Download Share is removed; the referenced file or container persists.
   * @param shareId Share ID (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @throws ApiException if fails to make API call
   */
  public void removeDownloadShare(Long shareId, String xSdsAuthToken) throws ApiException {
    Object localVarPostBody = null;
    // verify the required parameter 'shareId' is set
    if (shareId == null) {
      throw new ApiException(400, "Missing the required parameter 'shareId' when calling removeDownloadShare");
    }
    // create path and map variables
    String localVarPath = "/v4/shares/downloads/{share_id}"
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

    String[] localVarAuthNames = new String[] { "oauth2" };

    apiClient.invokeAPI(localVarPath, "DELETE", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, null);
  }
  /**
   * Remove Upload Share
   * ### Description: Delete an Upload Share (aka File Request).  ### Precondition: User has &lt;span style&#x3D;&#x27;padding: 3px; background-color: #F6F7F8; border: 1px solid #000; border-radius: 5px; display: inline;&#x27;&gt;&amp;#128275; manage upload share&lt;/span&gt; permissions on target container.  ### Postcondition: Upload Share is deleted.  ### Further Information: Only the Upload Share is removed; already uploaded files and the target container persist.
   * @param shareId Share ID (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @throws ApiException if fails to make API call
   */
  public void removeUploadShare(Long shareId, String xSdsAuthToken) throws ApiException {
    Object localVarPostBody = null;
    // verify the required parameter 'shareId' is set
    if (shareId == null) {
      throw new ApiException(400, "Missing the required parameter 'shareId' when calling removeUploadShare");
    }
    // create path and map variables
    String localVarPath = "/v4/shares/uploads/{share_id}"
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

    String[] localVarAuthNames = new String[] { "oauth2" };

    apiClient.invokeAPI(localVarPath, "DELETE", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, null);
  }
  /**
   * Request Download Share
   * ### Description:   Retrieve detailed information about one Download Share.  ### Precondition: User with &lt;span style&#x3D;&#x27;padding: 3px; background-color: #F6F7F8; border: 1px solid #000; border-radius: 5px; display: inline;&#x27;&gt;&amp;#128275; manage download share&lt;/span&gt; permissions on target node.  ### Postcondition: Download Share is returned  ### Further Information: None.
   * @param shareId Share ID (required)
   * @param xSdsDateFormat Date time format (cf. [RFC 3339](https://www.ietf.org/rfc/rfc3339.txt) &amp; [leettime.de](http://leettime.de/)) (optional)
   * @param xSdsAuthToken Authentication token (optional)
   * @return DownloadShare
   * @throws ApiException if fails to make API call
   */
  public DownloadShare requestDownloadShare(Long shareId, String xSdsDateFormat, String xSdsAuthToken) throws ApiException {
    Object localVarPostBody = null;
    // verify the required parameter 'shareId' is set
    if (shareId == null) {
      throw new ApiException(400, "Missing the required parameter 'shareId' when calling requestDownloadShare");
    }
    // create path and map variables
    String localVarPath = "/v4/shares/downloads/{share_id}"
      .replaceAll("\\{" + "share_id" + "\\}", apiClient.escapeString(shareId.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


    if (xSdsDateFormat != null)
      localVarHeaderParams.put("X-Sds-Date-Format", apiClient.parameterToString(xSdsDateFormat));
    if (xSdsAuthToken != null)
      localVarHeaderParams.put("X-Sds-Auth-Token", apiClient.parameterToString(xSdsAuthToken));

    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "oauth2" };

    GenericType<DownloadShare> localVarReturnType = new GenericType<DownloadShare>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
  /**
   * Request Download Share via QR Code
   * ### Description:   Retrieve detailed information about one Download Share.  ### Precondition: User with &lt;span style&#x3D;&#x27;padding: 3px; background-color: #F6F7F8; border: 1px solid #000; border-radius: 5px; display: inline;&#x27;&gt;&amp;#128275; manage download share&lt;/span&gt; permissions on target node.  ### Postcondition: Download Share is returned  ### Further Information: None.
   * @param shareId Share ID (required)
   * @param xSdsDateFormat Date time format (cf. [RFC 3339](https://www.ietf.org/rfc/rfc3339.txt) &amp; [leettime.de](http://leettime.de/)) (optional)
   * @param xSdsAuthToken Authentication token (optional)
   * @return DownloadShare
   * @throws ApiException if fails to make API call
   */
  public DownloadShare requestDownloadShareQr(Long shareId, String xSdsDateFormat, String xSdsAuthToken) throws ApiException {
    Object localVarPostBody = null;
    // verify the required parameter 'shareId' is set
    if (shareId == null) {
      throw new ApiException(400, "Missing the required parameter 'shareId' when calling requestDownloadShareQr");
    }
    // create path and map variables
    String localVarPath = "/v4/shares/downloads/{share_id}/qr"
      .replaceAll("\\{" + "share_id" + "\\}", apiClient.escapeString(shareId.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


    if (xSdsDateFormat != null)
      localVarHeaderParams.put("X-Sds-Date-Format", apiClient.parameterToString(xSdsDateFormat));
    if (xSdsAuthToken != null)
      localVarHeaderParams.put("X-Sds-Auth-Token", apiClient.parameterToString(xSdsAuthToken));

    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "oauth2" };

    GenericType<DownloadShare> localVarReturnType = new GenericType<DownloadShare>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
  /**
   * Request list of Download Shares
   * ### Description:   Retrieve a list of Download Shares.  ### Precondition: Authenticated user.  ### Postcondition: List of available Download Shares is returned.  ### Further Information:  ### Filtering: All filter fields are connected via logical (**AND**). createdBy and updatedBy searches several user-related attributes.  Filter string syntax: &#x60;FIELD_NAME:OPERATOR:VALUE[:VALUE...]&#x60;    &lt;details style&#x3D;\&quot;padding-left: 10px\&quot;&gt; &lt;summary style&#x3D;\&quot;cursor: pointer; outline: none\&quot;&gt;&lt;strong&gt;Example&lt;/strong&gt;&lt;/summary&gt;  &#x60;name:cn:searchString_1|createdBy:cn:searchString_2&#x60; Filter by file name contains &#x60;searchString_1&#x60; **AND** creator info (&#x60;firstName&#x60; **OR** &#x60;lastName&#x60; **OR** &#x60;email&#x60; **OR** &#x60;username&#x60;) contains &#x60;searchString_2&#x60;.  &lt;/details&gt;  ### Filtering options: &lt;details style&#x3D;\&quot;padding: 10px; background-color: #F6F7F8; border: 1px solid #AAA; border-radius: 5px;\&quot;&gt; &lt;summary style&#x3D;\&quot;cursor: pointer; outline: none\&quot;&gt;&lt;strong&gt;Expand&lt;/strong&gt;&lt;/summary&gt;  | &#x60;FIELD_NAME&#x60; | Filter Description | &#x60;OPERATOR&#x60; | Operator Description | &#x60;VALUE&#x60; | | :--- | :--- | :--- | :--- | :--- | | &#x60;name&#x60; | Alias or node name filter | &#x60;cn&#x60; | Alias or node name contains value. | &#x60;search String&#x60; | | &#x60;createdAt&#x60; | Creation date filter | &#x60;ge, le&#x60; | Creation date is greater / less equals than value.&lt;br&gt;Multiple operator values are allowed and will be connected via logical conjunction (**AND**).&lt;br&gt;e.g. &#x60;createdAt:ge:2016-12-31&#x60;&amp;#124;&#x60;createdAt:le:2018-01-01&#x60; | &#x60;Date (yyyy-MM-dd)&#x60; | | &#x60;createdBy&#x60; | Creator info filter | &#x60;cn, eq&#x60; | Creator info (&#x60;firstName&#x60; **OR** &#x60;lastName&#x60; **OR** &#x60;email&#x60; **OR** &#x60;username&#x60;) contains value. | &#x60;search String&#x60; | | &#x60;createdById&#x60; | Creator ID filter | &#x60;eq&#x60; | Creator ID equals value. | &#x60;positive Integer&#x60; | | &#x60;accessKey&#x60; | Share access key filter | &#x60;cn&#x60; | Share access key contains values. | &#x60;search String&#x60; | | &#x60;nodeId&#x60; | Source node ID | &#x60;eq&#x60; | Source node (room, folder, file) ID equals value. | &#x60;positive Integer&#x60; | | &#x60;updatedBy&#x60; | Modifier info filter | &#x60;cn, eq&#x60; | Modifier info (&#x60;firstName&#x60; **OR** &#x60;lastName&#x60; **OR** &#x60;email&#x60; **OR** &#x60;username&#x60;) contains value. | &#x60;search String&#x60; | | &#x60;updatedById&#x60; | Modifier ID filter | &#x60;eq&#x60; | Modifier ID equals value. | &#x60;positive Integer&#x60; |  &lt;/details&gt;  ### Deprecated filtering options: &lt;details style&#x3D;\&quot;padding: 10px; background-color: #F6F7F8; border: 1px solid #AAA; border-radius: 5px;\&quot;&gt; &lt;summary style&#x3D;\&quot;cursor: pointer; outline: none\&quot;&gt;&lt;strong&gt;Expand&lt;/strong&gt;&lt;/summary&gt;  | &#x60;FIELD_NAME&#x60; | Filter Description | &#x60;OPERATOR&#x60; | Operator Description | &#x60;VALUE&#x60; | | :--- | :--- | :--- | :--- | :--- | | &lt;del&gt;&#x60;userId&#x60;&lt;/del&gt;  | Creator user ID | &#x60;eq&#x60; | Creator user ID equals value. Use &#x60;createdById&#x60; instead | &#x60;positive Integer&#x60; |  &lt;/details&gt;  ---  ### Sorting: Sort string syntax: &#x60;FIELD_NAME:ORDER&#x60;   &#x60;ORDER&#x60; can be &#x60;asc&#x60; or &#x60;desc&#x60;.   Multiple sort fields are supported.    &lt;details style&#x3D;\&quot;padding-left: 10px\&quot;&gt; &lt;summary style&#x3D;\&quot;cursor: pointer; outline: none\&quot;&gt;&lt;strong&gt;Example&lt;/strong&gt;&lt;/summary&gt;  &#x60;name:asc|expireAt:desc&#x60;   Sort by &#x60;name&#x60; ascending **AND** by &#x60;expireAt&#x60; descending.  &lt;/details&gt;  ### Sorting options: &lt;details style&#x3D;\&quot;padding: 10px; background-color: #F6F7F8; border: 1px solid #AAA; border-radius: 5px;\&quot;&gt; &lt;summary style&#x3D;\&quot;cursor: pointer; outline: none\&quot;&gt;&lt;strong&gt;Expand&lt;/strong&gt;&lt;/summary&gt;  | &#x60;FIELD_NAME&#x60; | Description | | :--- | :--- | | &#x60;name&#x60; | Alias or node name | | &#x60;notifyCreator&#x60; | Notify creator on every download | | &#x60;expireAt&#x60; | Expiration date | | &#x60;createdAt&#x60; | Creation date | | &#x60;createdBy&#x60; | Creator first name, last name | | &#x60;classification&#x60; | Classification ID:&lt;ul&gt;&lt;li&gt;1 - public&lt;/li&gt;&lt;li&gt;2 - internal&lt;/li&gt;&lt;li&gt;3 - confidential&lt;/li&gt;&lt;li&gt;4 - strictly confidential&lt;/li&gt;&lt;/ul&gt; |  &lt;/details&gt; 
   * @param xSdsDateFormat Date time format (cf. [RFC 3339](https://www.ietf.org/rfc/rfc3339.txt) &amp; [leettime.de](http://leettime.de/)) (optional)
   * @param filter Filter string (optional)
   * @param sort Sort string (optional)
   * @param offset Range offset (optional)
   * @param limit Range limit.  Maximum 500.   For more results please use paging (&#x60;offset&#x60; + &#x60;limit&#x60;). (optional)
   * @param xSdsAuthToken Authentication token (optional)
   * @return DownloadShareList
   * @throws ApiException if fails to make API call
   */
  public DownloadShareList requestDownloadShares(String xSdsDateFormat, String filter, String sort, Integer offset, Integer limit, String xSdsAuthToken) throws ApiException {
    Object localVarPostBody = null;
    // create path and map variables
    String localVarPath = "/v4/shares/downloads";

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    localVarQueryParams.addAll(apiClient.parameterToPairs("", "filter", filter));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "sort", sort));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "offset", offset));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "limit", limit));

    if (xSdsDateFormat != null)
      localVarHeaderParams.put("X-Sds-Date-Format", apiClient.parameterToString(xSdsDateFormat));
    if (xSdsAuthToken != null)
      localVarHeaderParams.put("X-Sds-Auth-Token", apiClient.parameterToString(xSdsAuthToken));

    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "oauth2" };

    GenericType<DownloadShareList> localVarReturnType = new GenericType<DownloadShareList>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
  /**
   * Request Upload Share
   * ### Description:   Retrieve detailed information about one Upload Share (aka File Request).  ### Precondition: User has &lt;span style&#x3D;&#x27;padding: 3px; background-color: #F6F7F8; border: 1px solid #000; border-radius: 5px; display: inline;&#x27;&gt;&amp;#128275; manage upload share&lt;/span&gt; permissions on target container.  ### Postcondition: Upload Share is returned.  ### Further Information: None.
   * @param shareId Share ID (required)
   * @param xSdsDateFormat Date time format (cf. [RFC 3339](https://www.ietf.org/rfc/rfc3339.txt) &amp; [leettime.de](http://leettime.de/)) (optional)
   * @param xSdsAuthToken Authentication token (optional)
   * @return UploadShare
   * @throws ApiException if fails to make API call
   */
  public UploadShare requestUploadShare(Long shareId, String xSdsDateFormat, String xSdsAuthToken) throws ApiException {
    Object localVarPostBody = null;
    // verify the required parameter 'shareId' is set
    if (shareId == null) {
      throw new ApiException(400, "Missing the required parameter 'shareId' when calling requestUploadShare");
    }
    // create path and map variables
    String localVarPath = "/v4/shares/uploads/{share_id}"
      .replaceAll("\\{" + "share_id" + "\\}", apiClient.escapeString(shareId.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


    if (xSdsDateFormat != null)
      localVarHeaderParams.put("X-Sds-Date-Format", apiClient.parameterToString(xSdsDateFormat));
    if (xSdsAuthToken != null)
      localVarHeaderParams.put("X-Sds-Auth-Token", apiClient.parameterToString(xSdsAuthToken));

    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "oauth2" };

    GenericType<UploadShare> localVarReturnType = new GenericType<UploadShare>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
  /**
   * Request Upload Share via QR Code
   * ### Description:   Retrieve detailed information about one Upload Share (aka File Request).  ### Precondition: User has &lt;span style&#x3D;&#x27;padding: 3px; background-color: #F6F7F8; border: 1px solid #000; border-radius: 5px; display: inline;&#x27;&gt;&amp;#128275; manage upload share&lt;/span&gt; permissions on target container.  ### Postcondition: Upload Share is returned.  ### Further Information: None.
   * @param shareId Share ID (required)
   * @param xSdsDateFormat Date time format (cf. [RFC 3339](https://www.ietf.org/rfc/rfc3339.txt) &amp; [leettime.de](http://leettime.de/)) (optional)
   * @param xSdsAuthToken Authentication token (optional)
   * @return UploadShare
   * @throws ApiException if fails to make API call
   */
  public UploadShare requestUploadShareQr(Long shareId, String xSdsDateFormat, String xSdsAuthToken) throws ApiException {
    Object localVarPostBody = null;
    // verify the required parameter 'shareId' is set
    if (shareId == null) {
      throw new ApiException(400, "Missing the required parameter 'shareId' when calling requestUploadShareQr");
    }
    // create path and map variables
    String localVarPath = "/v4/shares/uploads/{share_id}/qr"
      .replaceAll("\\{" + "share_id" + "\\}", apiClient.escapeString(shareId.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


    if (xSdsDateFormat != null)
      localVarHeaderParams.put("X-Sds-Date-Format", apiClient.parameterToString(xSdsDateFormat));
    if (xSdsAuthToken != null)
      localVarHeaderParams.put("X-Sds-Auth-Token", apiClient.parameterToString(xSdsAuthToken));

    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "oauth2" };

    GenericType<UploadShare> localVarReturnType = new GenericType<UploadShare>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
  /**
   * Request list of Upload Shares
   * ### Description:   Retrieve a list of Upload Shares (aka File Requests).  ### Precondition: Authenticated user.  ### Postcondition: List of available Upload Shares is returned.  ### Further Information:  ### Filtering: All filter fields are connected via logical (**AND**). createdBy and updatedBy searches several user-related attributes. Filter string syntax: &#x60;FIELD_NAME:OPERATOR:VALUE[:VALUE...]&#x60;    &lt;details style&#x3D;\&quot;padding-left: 10px\&quot;&gt; &lt;summary style&#x3D;\&quot;cursor: pointer; outline: none\&quot;&gt;&lt;strong&gt;Example&lt;/strong&gt;&lt;/summary&gt;  &#x60;name:cn:searchString_1|createdBy:cn:searchString_2&#x60;   Filter by alias name contains &#x60;searchString_1&#x60; **AND** creator info (&#x60;firstName&#x60; **OR** &#x60;lastName&#x60; **OR** &#x60;email&#x60; **OR** &#x60;username&#x60;) contains &#x60;searchString_2&#x60;.  &lt;/details&gt;  ### Filtering options: &lt;details style&#x3D;\&quot;padding: 10px; background-color: #F6F7F8; border: 1px solid #AAA; border-radius: 5px;\&quot;&gt; &lt;summary style&#x3D;\&quot;cursor: pointer; outline: none\&quot;&gt;&lt;strong&gt;Expand&lt;/strong&gt;&lt;/summary&gt;  | &#x60;FIELD_NAME&#x60; | Filter Description | &#x60;OPERATOR&#x60; | Operator Description | &#x60;VALUE&#x60; | | :--- | :--- | :--- | :--- | :--- | | &#x60;name&#x60; | Alias name filter | &#x60;cn&#x60; | Alias name contains value. | &#x60;search String&#x60; | | &#x60;createdAt&#x60; | Creation date filter | &#x60;ge, le&#x60; | Creation date is greater / less equals than value.&lt;br&gt;Multiple operator values are allowed and will be connected via logical conjunction (**AND**).&lt;br&gt;e.g. &#x60;createdAt:ge:2016-12-31&#x60;&amp;#124;&#x60;createdAt:le:2018-01-01&#x60; | &#x60;Date (yyyy-MM-dd)&#x60; | | &#x60;createdBy&#x60; | Creator info filter | &#x60;cn, eq&#x60; | Creator info (&#x60;firstName&#x60; **OR** &#x60;lastName&#x60; **OR** &#x60;email&#x60; **OR** &#x60;username&#x60;) contains value. | &#x60;search String&#x60; | | &#x60;createdById&#x60; | Creator ID filter | &#x60;eq&#x60; | Creator ID equals value. | &#x60;positive Integer&#x60; | | &#x60;accessKey&#x60; | Share access key filter | &#x60;cn&#x60; | Share access key contains values. | &#x60;search String&#x60; | | &#x60;userId&#x60; | Creator user ID | &#x60;eq&#x60; | Creator user ID equals value. | &#x60;positive Integer&#x60; | | &#x60;targetId&#x60; | Target node ID | &#x60;eq&#x60; | Target node (room, folder) ID equals value. | &#x60;positive Integer&#x60; | | &#x60;updatedBy&#x60; | Modifier info filter | &#x60;cn, eq&#x60; | Modifier info (&#x60;firstName&#x60; **OR** &#x60;lastName&#x60; **OR** &#x60;email&#x60; **OR** &#x60;username&#x60;) contains value. | &#x60;search String&#x60; | | &#x60;updatedById&#x60; | Modifier ID filter | &#x60;eq&#x60; | Modifier ID equals value. | &#x60;positive Integer&#x60; |  &lt;/details&gt;  ### Deprecated filtering options: &lt;details style&#x3D;\&quot;padding: 10px; background-color: #F6F7F8; border: 1px solid #AAA; border-radius: 5px;\&quot;&gt; &lt;summary style&#x3D;\&quot;cursor: pointer; outline: none\&quot;&gt;&lt;strong&gt;Expand&lt;/strong&gt;&lt;/summary&gt;  | &#x60;FIELD_NAME&#x60; | Filter Description | &#x60;OPERATOR&#x60; | Operator Description | &#x60;VALUE&#x60; | | :--- | :--- | :--- | :--- | :--- | | &lt;del&gt;&#x60;targetId&#x60;&lt;/del&gt; | Target node ID | &#x60;cn&#x60; | Target node (room, folder) ID equals value. | &#x60;positive Integer&#x60; | | &lt;del&gt;&#x60;userId&#x60; &lt;/del&gt;| Creator user ID | &#x60;eq&#x60; | Creator user ID equals value. Use &#x60;createdById&#x60; instead. | &#x60;positive Integer&#x60; |  &lt;/details&gt;  ---  Sort string syntax: &#x60;FIELD_NAME:ORDER&#x60;   &#x60;ORDER&#x60; can be &#x60;asc&#x60; or &#x60;desc&#x60;.   Multiple sort fields are supported.    &lt;details style&#x3D;\&quot;padding-left: 10px\&quot;&gt; &lt;summary style&#x3D;\&quot;cursor: pointer; outline: none\&quot;&gt;&lt;strong&gt;Example&lt;/strong&gt;&lt;/summary&gt;  &#x60;name:asc|expireAt:desc&#x60;   Sort by &#x60;name&#x60; ascending **AND** by &#x60;expireAt&#x60; descending.  &lt;/details&gt;  ### Sorting options: &lt;details style&#x3D;\&quot;padding: 10px; background-color: #F6F7F8; border: 1px solid #AAA; border-radius: 5px;\&quot;&gt; &lt;summary style&#x3D;\&quot;cursor: pointer; outline: none\&quot;&gt;&lt;strong&gt;Expand&lt;/strong&gt;&lt;/summary&gt;  | &#x60;FIELD_NAME&#x60; | Description | | :--- | :--- | | &#x60;name&#x60; | Alias name | | &#x60;notifyCreator&#x60; | Notify creator on every upload | | &#x60;expireAt&#x60; | Expiration date | | &#x60;createdAt&#x60; | Creation date | | &#x60;createdBy&#x60; | Creator first name, last name |  &lt;/details&gt;
   * @param xSdsDateFormat Date time format (cf. [RFC 3339](https://www.ietf.org/rfc/rfc3339.txt) &amp; [leettime.de](http://leettime.de/)) (optional)
   * @param filter Filter string (optional)
   * @param sort Sort string (optional)
   * @param offset Range offset (optional)
   * @param limit Range limit.  Maximum 500.   For more results please use paging (&#x60;offset&#x60; + &#x60;limit&#x60;). (optional)
   * @param xSdsAuthToken Authentication token (optional)
   * @return UploadShareList
   * @throws ApiException if fails to make API call
   */
  public UploadShareList requestUploadShares(String xSdsDateFormat, String filter, String sort, Integer offset, Integer limit, String xSdsAuthToken) throws ApiException {
    Object localVarPostBody = null;
    // create path and map variables
    String localVarPath = "/v4/shares/uploads";

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    localVarQueryParams.addAll(apiClient.parameterToPairs("", "filter", filter));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "sort", sort));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "offset", offset));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "limit", limit));

    if (xSdsDateFormat != null)
      localVarHeaderParams.put("X-Sds-Date-Format", apiClient.parameterToString(xSdsDateFormat));
    if (xSdsAuthToken != null)
      localVarHeaderParams.put("X-Sds-Auth-Token", apiClient.parameterToString(xSdsAuthToken));

    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "oauth2" };

    GenericType<UploadShareList> localVarReturnType = new GenericType<UploadShareList>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
  /**
   * Send an existing Download Share link via email
   * &lt;h3 style&#x3D;&#x27;padding: 5px; background-color: #F6F7F8; border: 1px solid #AAA; border-radius: 5px; display: table-cell;&#x27;&gt;&amp;#128640; Since v4.11.0&lt;/h3&gt;  ### Description: Send an email to specific recipients for existing Download Share.  ### Precondition: User with &lt;span style&#x3D;&#x27;padding: 3px; background-color: #F6F7F8; border: 1px solid #000; border-radius: 5px; display: inline;&#x27;&gt;&amp;#128275; manage download share&lt;/span&gt; permissions on target node.  ### Postcondition: Download Share link successfully sent.  ### Further Information:  * Forbidden characters in the email body: [&#x60;&lt;&#x60;, &#x60;&gt;&#x60;] 
   * @param body  (required)
   * @param shareId Share ID (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @throws ApiException if fails to make API call
   */
  public void sendDownloadShareLinkViaEmail(DownloadShareLinkEmail body, Long shareId, String xSdsAuthToken) throws ApiException {
    Object localVarPostBody = body;
    // verify the required parameter 'body' is set
    if (body == null) {
      throw new ApiException(400, "Missing the required parameter 'body' when calling sendDownloadShareLinkViaEmail");
    }
    // verify the required parameter 'shareId' is set
    if (shareId == null) {
      throw new ApiException(400, "Missing the required parameter 'shareId' when calling sendDownloadShareLinkViaEmail");
    }
    // create path and map variables
    String localVarPath = "/v4/shares/downloads/{share_id}/email"
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
      "application/json"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "oauth2" };

    apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, null);
  }
  /**
   * Send an existing Upload Share link via email
   * &lt;h3 style&#x3D;&#x27;padding: 5px; background-color: #F6F7F8; border: 1px solid #AAA; border-radius: 5px; display: table-cell;&#x27;&gt;&amp;#128640; Since v4.11.0&lt;/h3&gt;  ### Description: Send an email to specific recipients for existing Upload Share.  ### Precondition: User with &lt;span style&#x3D;&#x27;padding: 3px; background-color: #F6F7F8; border: 1px solid #000; border-radius: 5px; display: inline;&#x27;&gt;&amp;#128275; manage upload share&lt;/span&gt; permissions on target container.  ### Postcondition: Upload Share link successfully sent.  ### Further Information:  * Forbidden characters in the email body: [&#x60;&lt;&#x60;, &#x60;&gt;&#x60;] 
   * @param body  (required)
   * @param shareId Share ID (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @throws ApiException if fails to make API call
   */
  public void sendUploadShareLinkViaEmail(UploadShareLinkEmail body, Long shareId, String xSdsAuthToken) throws ApiException {
    Object localVarPostBody = body;
    // verify the required parameter 'body' is set
    if (body == null) {
      throw new ApiException(400, "Missing the required parameter 'body' when calling sendUploadShareLinkViaEmail");
    }
    // verify the required parameter 'shareId' is set
    if (shareId == null) {
      throw new ApiException(400, "Missing the required parameter 'shareId' when calling sendUploadShareLinkViaEmail");
    }
    // create path and map variables
    String localVarPath = "/v4/shares/uploads/{share_id}/email"
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
      "application/json"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "oauth2" };

    apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, null);
  }
  /**
   * Update Download Share
   * &lt;h3 style&#x3D;&#x27;padding: 5px; background-color: #F6F7F8; border: 1px solid #AAA; border-radius: 5px; display: table-cell;&#x27;&gt;&amp;#128640; Since v4.11.0&lt;/h3&gt;  ### Description: Update an existing Download Share.  ### Precondition: User with &lt;span style&#x3D;&#x27;padding: 3px; background-color: #F6F7F8; border: 1px solid #000; border-radius: 5px; display: inline;&#x27;&gt;&amp;#128275; manage download share&lt;/span&gt; permissions on target node.  ### Postcondition: Download Share is successfully updated.  ### Further Information: * &#x60;name&#x60; is limited to **150** characters. * &#x60;notes&#x60; are limited to **255** characters. * &#x60;password&#x60; is limited to **1024** characters.  Forbidden characters in passwords: [&#x60;&amp;&#x60;, &#x60;&#x27;&#x60;, &#x60;&lt;&#x60;, &#x60;&gt;&#x60;]  Please keep in mind that due to various restrictions of different telecommunication providers, non-ASCII characters may not be displayed correctly in short messages (SMS).
   * @param body  (required)
   * @param shareId Share ID (required)
   * @param xSdsDateFormat Date time format (cf. [RFC 3339](https://www.ietf.org/rfc/rfc3339.txt) &amp; [leettime.de](http://leettime.de/)) (optional)
   * @param xSdsAuthToken Authentication token (optional)
   * @return DownloadShare
   * @throws ApiException if fails to make API call
   */
  public DownloadShare updateDownloadShare(UpdateDownloadShareRequest body, Long shareId, String xSdsDateFormat, String xSdsAuthToken) throws ApiException {
    Object localVarPostBody = body;
    // verify the required parameter 'body' is set
    if (body == null) {
      throw new ApiException(400, "Missing the required parameter 'body' when calling updateDownloadShare");
    }
    // verify the required parameter 'shareId' is set
    if (shareId == null) {
      throw new ApiException(400, "Missing the required parameter 'shareId' when calling updateDownloadShare");
    }
    // create path and map variables
    String localVarPath = "/v4/shares/downloads/{share_id}"
      .replaceAll("\\{" + "share_id" + "\\}", apiClient.escapeString(shareId.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


    if (xSdsDateFormat != null)
      localVarHeaderParams.put("X-Sds-Date-Format", apiClient.parameterToString(xSdsDateFormat));
    if (xSdsAuthToken != null)
      localVarHeaderParams.put("X-Sds-Auth-Token", apiClient.parameterToString(xSdsAuthToken));

    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "oauth2" };

    GenericType<DownloadShare> localVarReturnType = new GenericType<DownloadShare>() {};
    return apiClient.invokeAPI(localVarPath, "PUT", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
  /**
   * Update a list of Download Shares
   * &lt;h3 style&#x3D;&#x27;padding: 5px; background-color: #F6F7F8; border: 1px solid #AAA; border-radius: 5px; display: table-cell;&#x27;&gt;&amp;#128640; Since v4.25.0&lt;/h3&gt;  ### Description: Update a list of existing Download Shares.  ### Precondition: User with &lt;span style&#x3D;&#x27;padding: 3px; background-color: #F6F7F8; border: 1px solid #000; border-radius: 5px; display: inline;&#x27;&gt;&amp;#128275; manage download share&lt;/span&gt; permissions on target node.  ### Postcondition: Download Shares are successfully updated.  ### Further Information: Maximum number of shares is 200
   * @param body  (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @throws ApiException if fails to make API call
   */
  public void updateDownloadShares(UpdateDownloadSharesBulkRequest body, String xSdsAuthToken) throws ApiException {
    Object localVarPostBody = body;
    // verify the required parameter 'body' is set
    if (body == null) {
      throw new ApiException(400, "Missing the required parameter 'body' when calling updateDownloadShares");
    }
    // create path and map variables
    String localVarPath = "/v4/shares/downloads";

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


    if (xSdsAuthToken != null)
      localVarHeaderParams.put("X-Sds-Auth-Token", apiClient.parameterToString(xSdsAuthToken));

    final String[] localVarAccepts = {
      "*/*"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "oauth2" };

    apiClient.invokeAPI(localVarPath, "PUT", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, null);
  }
  /**
   * Update Upload Share
   * &lt;h3 style&#x3D;&#x27;padding: 5px; background-color: #F6F7F8; border: 1px solid #AAA; border-radius: 5px; display: table-cell;&#x27;&gt;&amp;#128640; Since v4.11.0&lt;/h3&gt;  ### Description: Update existing Upload Share (aka File Request).  ### Precondition: User has &lt;span style&#x3D;&#x27;padding: 3px; background-color: #F6F7F8; border: 1px solid #000; border-radius: 5px; display: inline;&#x27;&gt;&amp;#128275; manage upload share&lt;/span&gt; permissions on target container.  ### Postcondition: Upload Share successfully updated.  ### Further Information:  * &#x60;name&#x60; is limited to **150** characters. * &#x60;notes&#x60; are limited to **255** characters. * &#x60;password&#x60; is limited to **1024** characters.  Forbidden characters in passwords: [&#x60;&amp;&#x60;, &#x60;&#x27;&#x60;, &#x60;&lt;&#x60;, &#x60;&gt;&#x60;]  Please keep in mind that due to various restrictions of different telecommunication providers, non-ASCII characters may not be displayed correctly in short messages (SMS).
   * @param body  (required)
   * @param shareId Share ID (required)
   * @param xSdsDateFormat Date time format (cf. [RFC 3339](https://www.ietf.org/rfc/rfc3339.txt) &amp; [leettime.de](http://leettime.de/)) (optional)
   * @param xSdsAuthToken Authentication token (optional)
   * @return UploadShare
   * @throws ApiException if fails to make API call
   */
  public UploadShare updateUploadShare(UpdateUploadShareRequest body, Long shareId, String xSdsDateFormat, String xSdsAuthToken) throws ApiException {
    Object localVarPostBody = body;
    // verify the required parameter 'body' is set
    if (body == null) {
      throw new ApiException(400, "Missing the required parameter 'body' when calling updateUploadShare");
    }
    // verify the required parameter 'shareId' is set
    if (shareId == null) {
      throw new ApiException(400, "Missing the required parameter 'shareId' when calling updateUploadShare");
    }
    // create path and map variables
    String localVarPath = "/v4/shares/uploads/{share_id}"
      .replaceAll("\\{" + "share_id" + "\\}", apiClient.escapeString(shareId.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


    if (xSdsDateFormat != null)
      localVarHeaderParams.put("X-Sds-Date-Format", apiClient.parameterToString(xSdsDateFormat));
    if (xSdsAuthToken != null)
      localVarHeaderParams.put("X-Sds-Auth-Token", apiClient.parameterToString(xSdsAuthToken));

    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "oauth2" };

    GenericType<UploadShare> localVarReturnType = new GenericType<UploadShare>() {};
    return apiClient.invokeAPI(localVarPath, "PUT", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
  /**
   * Update List of Upload Shares
   * &lt;h3 style&#x3D;&#x27;padding: 5px; background-color: #F6F7F8; border: 1px solid #AAA; border-radius: 5px; display: table-cell;&#x27;&gt;&amp;#128640; Since v4.25.0&lt;/h3&gt;  ### Description: Update a list of existing Upload Shares (aka File Request).  ### Precondition: User has &lt;span style&#x3D;&#x27;padding: 3px; background-color: #F6F7F8; border: 1px solid #000; border-radius: 5px; display: inline;&#x27;&gt;&amp;#128275; manage upload share&lt;/span&gt; permissions on target container.  ### Postcondition: Upload Shares successfully updated.  ### Further Information: Maximum number of shares is 200
   * @param body  (required)
   * @param xSdsDateFormat Date time format (cf. [RFC 3339](https://www.ietf.org/rfc/rfc3339.txt) &amp; [leettime.de](http://leettime.de/)) (optional)
   * @param xSdsAuthToken Authentication token (optional)
   * @throws ApiException if fails to make API call
   */
  public void updateUploadShares(UpdateUploadSharesBulkRequest body, String xSdsDateFormat, String xSdsAuthToken) throws ApiException {
    Object localVarPostBody = body;
    // verify the required parameter 'body' is set
    if (body == null) {
      throw new ApiException(400, "Missing the required parameter 'body' when calling updateUploadShares");
    }
    // create path and map variables
    String localVarPath = "/v4/shares/uploads";

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


    if (xSdsDateFormat != null)
      localVarHeaderParams.put("X-Sds-Date-Format", apiClient.parameterToString(xSdsDateFormat));
    if (xSdsAuthToken != null)
      localVarHeaderParams.put("X-Sds-Auth-Token", apiClient.parameterToString(xSdsAuthToken));

    final String[] localVarAccepts = {
      "*/*"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "oauth2" };

    apiClient.invokeAPI(localVarPath, "PUT", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, null);
  }
}
