package ch.cyberduck.core.sds.io.swagger.client.api;

/*
 * Copyright (c) 2002-2018 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */

import ch.cyberduck.core.sds.io.swagger.client.ApiClient;
import ch.cyberduck.core.sds.io.swagger.client.ApiException;
import ch.cyberduck.core.sds.io.swagger.client.Configuration;
import ch.cyberduck.core.sds.io.swagger.client.Pair;
import ch.cyberduck.core.sds.io.swagger.client.model.CreateDownloadShareRequest;
import ch.cyberduck.core.sds.io.swagger.client.model.CreateUploadShareRequest;
import ch.cyberduck.core.sds.io.swagger.client.model.DownloadShare;
import ch.cyberduck.core.sds.io.swagger.client.model.DownloadShareList;
import ch.cyberduck.core.sds.io.swagger.client.model.UploadShare;
import ch.cyberduck.core.sds.io.swagger.client.model.UploadShareList;

import javax.ws.rs.core.GenericType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaClientCodegen", date = "2018-05-23T09:31:14.222+02:00")
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
   * ### Functional Description: Create a new Download Share.  ### Precondition: User with _\&quot;manage download share\&quot;_ permissions on target node.  ### Effects: Download Share created.  ### &amp;#9432; Further Information:  * **Access password:** is limited to **150** characters. * **Notes:** are limited to **255** characters. * **Alias names:** are limited to **150** characters.
   * @param body body (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @param xSdsDateFormat Date time format (cf. [RFC 3339](https://www.ietf.org/rfc/rfc3339.txt)): * &#x60;LOCAL&#x60; * &#x60;UTC&#x60; * &#x60;OFFSET&#x60; * &#x60;EPOCH&#x60; (optional)
   * @return DownloadShare
   * @throws ApiException if fails to make API call
   */
  public DownloadShare createDownloadShare(CreateDownloadShareRequest body, String xSdsAuthToken, String xSdsDateFormat) throws ApiException {
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


    if (xSdsAuthToken != null)
      localVarHeaderParams.put("X-Sds-Auth-Token", apiClient.parameterToString(xSdsAuthToken));
if (xSdsDateFormat != null)
      localVarHeaderParams.put("X-Sds-Date-Format", apiClient.parameterToString(xSdsDateFormat));

    
    final String[] localVarAccepts = {
      "application/json;charset=UTF-8"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json;charset=UTF-8"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "DRACOON-OAuth" };

    GenericType<DownloadShare> localVarReturnType = new GenericType<DownloadShare>() {};
    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Create new Upload Share
   * ### Functional Description: Create a new Upload Share (aka Upload Account).  ### Precondition: User has _\&quot;manage upload share\&quot;_ permissions on target container.  ### Effects: Upload Share is created.  ### &amp;#9432; Further Information:  * **Notes:** are limited to **255** characters. * **Alias Names:** are limited to **150** characters. * **Send Mail:**:       * If &#x60;sendMail&#x60; is set to &#x60;false&#x60;: &#x60;mailRecipients&#x60;, &#x60;mailSubject&#x60; and &#x60;mailBody&#x60; are **optional**.       * If &#x60;sendMail&#x60; is set to &#x60;true&#x60;: &#x60;mailRecipients&#x60;, &#x60;mailSubject&#x60; and &#x60;mailBody&#x60; are **mandatory**.
   * @param body body (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @param xSdsDateFormat Date time format (cf. [RFC 3339](https://www.ietf.org/rfc/rfc3339.txt)): * &#x60;LOCAL&#x60; * &#x60;UTC&#x60; * &#x60;OFFSET&#x60; * &#x60;EPOCH&#x60; (optional)
   * @return UploadShare
   * @throws ApiException if fails to make API call
   */
  public UploadShare createUploadShare(CreateUploadShareRequest body, String xSdsAuthToken, String xSdsDateFormat) throws ApiException {
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


    if (xSdsAuthToken != null)
      localVarHeaderParams.put("X-Sds-Auth-Token", apiClient.parameterToString(xSdsAuthToken));
if (xSdsDateFormat != null)
      localVarHeaderParams.put("X-Sds-Date-Format", apiClient.parameterToString(xSdsDateFormat));

    
    final String[] localVarAccepts = {
      "application/json;charset=UTF-8"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json;charset=UTF-8"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "DRACOON-OAuth" };

    GenericType<UploadShare> localVarReturnType = new GenericType<UploadShare>() {};
    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Delete Download Share
   * ### Functional Description: Delete a Download Share.  ### Precondition: None.  ### Effects: Download Share is deleted.  ### &amp;#9432; Further Information: Only the Download Share is removed; the referenced file or container persists.
   * @param shareId Share ID (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @throws ApiException if fails to make API call
   */
  public void deleteDownloadShare(Long shareId, String xSdsAuthToken) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'shareId' is set
    if (shareId == null) {
      throw new ApiException(400, "Missing the required parameter 'shareId' when calling deleteDownloadShare");
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
      "application/json;charset=UTF-8"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "DRACOON-OAuth" };


    apiClient.invokeAPI(localVarPath, "DELETE", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, null);
  }
  /**
   * Delete Upload Share
   * ### Functional Description: Delete an Upload Share (aka Upload Account).  ### Precondition: None.  ### Effects: Upload Share is deleted.  ### &amp;#9432; Further Information: Only the Upload Share is removed; already uploaded files and the target container persist.
   * @param shareId Share ID (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @throws ApiException if fails to make API call
   */
  public void deleteUploadShare(Long shareId, String xSdsAuthToken) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'shareId' is set
    if (shareId == null) {
      throw new ApiException(400, "Missing the required parameter 'shareId' when calling deleteUploadShare");
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
      "application/json;charset=UTF-8"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "DRACOON-OAuth" };


    apiClient.invokeAPI(localVarPath, "DELETE", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, null);
  }
  /**
   * Get Download Share
   * ### Functional Description:   Retrieve detailed information about one Download Share.  ### Precondition: None.  ### Effects: None.  ### &amp;#9432; Further Information: None.
   * @param shareId Share ID (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @param xSdsDateFormat Date time format (cf. [RFC 3339](https://www.ietf.org/rfc/rfc3339.txt)): * &#x60;LOCAL&#x60; * &#x60;UTC&#x60; * &#x60;OFFSET&#x60; * &#x60;EPOCH&#x60; (optional)
   * @return DownloadShare
   * @throws ApiException if fails to make API call
   */
  public DownloadShare getDownloadShare(Long shareId, String xSdsAuthToken, String xSdsDateFormat) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'shareId' is set
    if (shareId == null) {
      throw new ApiException(400, "Missing the required parameter 'shareId' when calling getDownloadShare");
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
if (xSdsDateFormat != null)
      localVarHeaderParams.put("X-Sds-Date-Format", apiClient.parameterToString(xSdsDateFormat));

    
    final String[] localVarAccepts = {
      "application/json;charset=UTF-8"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "DRACOON-OAuth" };

    GenericType<DownloadShare> localVarReturnType = new GenericType<DownloadShare>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Get Download Share via QR Code
   * ### Functional Description:   Retrieve detailed information about one Download Share.  ### Precondition: None.  ### Effects: None.  ### &amp;#9432; Further Information: None.
   * @param shareId Share ID (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @param xSdsDateFormat Date time format (cf. [RFC 3339](https://www.ietf.org/rfc/rfc3339.txt)): * &#x60;LOCAL&#x60; * &#x60;UTC&#x60; * &#x60;OFFSET&#x60; * &#x60;EPOCH&#x60; (optional)
   * @return DownloadShare
   * @throws ApiException if fails to make API call
   */
  public DownloadShare getDownloadShareQr(Long shareId, String xSdsAuthToken, String xSdsDateFormat) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'shareId' is set
    if (shareId == null) {
      throw new ApiException(400, "Missing the required parameter 'shareId' when calling getDownloadShareQr");
    }
    
    // create path and map variables
    String localVarPath = "/v4/shares/downloads/{share_id}/qr"
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
      "application/json;charset=UTF-8"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "DRACOON-OAuth" };

    GenericType<DownloadShare> localVarReturnType = new GenericType<DownloadShare>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Get Download Shares
   * ### Functional Description:   Retrieve a list of Download Shares.  ### Precondition: None.  ### Effects: None.  ### &amp;#9432; Further Information: None.  ### Filtering ### &amp;#9888; All filter fields are connected via logical disjunction (**OR**) Filter string syntax: &#x60;FIELD_NAME:OPERATOR:VALUE[:VALUE...]&#x60;   Example: &gt; &#x60;name:cn:searchString_1|createdBy:cn:searchString_2|nodeId:eq:1&#x60;   Filter by file name contains &#x60;searchString_1&#x60; **OR** creator info (firstname **OR** lastname **OR** login) contains &#x60;searchString_2&#x60; **OR** node ID is equal to &#x60;1&#x60;.  | &#x60;FIELD_NAME&#x60; | Filter Description | &#x60;OPERATOR&#x60; | Operator Description | &#x60;VALUE&#x60; | | :--- | :--- | :--- | :--- | :--- | | **&#x60;name&#x60;** | Alias or node name filter | &#x60;cn&#x60; | Alias or node name contains value. | &#x60;search String&#x60; | | **&#x60;createdBy&#x60;** | Creator info filter | &#x60;cn&#x60; | Creator info (&#x60;firstName&#x60; **OR** &#x60;lastName&#x60; **OR** &#x60;login&#x60;) contains value. | &#x60;search String&#x60; | | **&#x60;accessKey&#x60;** | Share access key filter | &#x60;cn&#x60; | Share access key contains values. | &#x60;search String&#x60; | | **&#x60;nodeId&#x60;** | Source node ID | &#x60;eq&#x60; | Source node (room, folder, file) ID equals value. | &#x60;positive Integer&#x60; | | **&#x60;userId&#x60;** | Creator user ID | &#x60;eq&#x60; | Creator user ID equals value. | &#x60;positive Integer&#x60; |  ### Sorting Sort string syntax: &#x60;FIELD_NAME:ORDER&#x60;   &#x60;ORDER&#x60; can be &#x60;asc&#x60; or &#x60;desc&#x60;.   Multiple sort fields are supported. Example: &gt; &#x60;name:asc|expireAt:desc&#x60;   Sort by &#x60;name&#x60; ascending **AND** by &#x60;expireAt&#x60; descending.  | &#x60;FIELD_NAME&#x60; | Description | | :--- | :--- | | **&#x60;name&#x60;** | Alias or node name | | **&#x60;classification&#x60;** | Classification ID (for files only):&lt;ul&gt;&lt;li&gt;1 - public&lt;/li&gt;&lt;li&gt;2 - for internal use only&lt;/li&gt;&lt;li&gt;3 - confidential&lt;/li&gt;&lt;li&gt;4 - strictly confidential&lt;/li&gt;&lt;/ul&gt; | | **&#x60;notifyCreator&#x60;** | Notify creator on every download | | **&#x60;expireAt&#x60;** | Expiration date | | **&#x60;createdAt&#x60;** | Creation date | | **&#x60;createdBy&#x60;** | Creator info |
   * @param filter Filter string (optional)
   * @param sort Sort string (optional)
   * @param offset Range offset (optional)
   * @param limit Range limit (optional)
   * @param xSdsAuthToken Authentication token (optional)
   * @param xSdsDateFormat Date time format (cf. [RFC 3339](https://www.ietf.org/rfc/rfc3339.txt)): * &#x60;LOCAL&#x60; * &#x60;UTC&#x60; * &#x60;OFFSET&#x60; * &#x60;EPOCH&#x60; (optional)
   * @return DownloadShareList
   * @throws ApiException if fails to make API call
   */
  public DownloadShareList getDownloadShares(String filter, String sort, Integer offset, Integer limit, String xSdsAuthToken, String xSdsDateFormat) throws ApiException {
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

    if (xSdsAuthToken != null)
      localVarHeaderParams.put("X-Sds-Auth-Token", apiClient.parameterToString(xSdsAuthToken));
if (xSdsDateFormat != null)
      localVarHeaderParams.put("X-Sds-Date-Format", apiClient.parameterToString(xSdsDateFormat));

    
    final String[] localVarAccepts = {
      "application/json;charset=UTF-8"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "DRACOON-OAuth" };

    GenericType<DownloadShareList> localVarReturnType = new GenericType<DownloadShareList>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Get Upload Share
   * ### Functional Description:   Retrieve detailed information about one Upload Share (aka Upload Account).  ### Precondition: None.  ### Effects: None.  ### &amp;#9432; Further Information: None.
   * @param shareId Share ID (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @param xSdsDateFormat Date time format (cf. [RFC 3339](https://www.ietf.org/rfc/rfc3339.txt)): * &#x60;LOCAL&#x60; * &#x60;UTC&#x60; * &#x60;OFFSET&#x60; * &#x60;EPOCH&#x60; (optional)
   * @return UploadShare
   * @throws ApiException if fails to make API call
   */
  public UploadShare getUploadShare(Long shareId, String xSdsAuthToken, String xSdsDateFormat) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'shareId' is set
    if (shareId == null) {
      throw new ApiException(400, "Missing the required parameter 'shareId' when calling getUploadShare");
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
if (xSdsDateFormat != null)
      localVarHeaderParams.put("X-Sds-Date-Format", apiClient.parameterToString(xSdsDateFormat));

    
    final String[] localVarAccepts = {
      "application/json;charset=UTF-8"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "DRACOON-OAuth" };

    GenericType<UploadShare> localVarReturnType = new GenericType<UploadShare>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Get Upload Share via QR Code
   * ### Functional Description:   Retrieve detailed information about one Upload Share (aka Upload Account).  ### Precondition: None.  ### Effects: None.  ### &amp;#9432; Further Information: None.
   * @param shareId Share ID (required)
   * @param xSdsAuthToken Authentication token (optional)
   * @param xSdsDateFormat Date time format (cf. [RFC 3339](https://www.ietf.org/rfc/rfc3339.txt)): * &#x60;LOCAL&#x60; * &#x60;UTC&#x60; * &#x60;OFFSET&#x60; * &#x60;EPOCH&#x60; (optional)
   * @return UploadShare
   * @throws ApiException if fails to make API call
   */
  public UploadShare getUploadShareQr(Long shareId, String xSdsAuthToken, String xSdsDateFormat) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'shareId' is set
    if (shareId == null) {
      throw new ApiException(400, "Missing the required parameter 'shareId' when calling getUploadShareQr");
    }
    
    // create path and map variables
    String localVarPath = "/v4/shares/uploads/{share_id}/qr"
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
      "application/json;charset=UTF-8"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "DRACOON-OAuth" };

    GenericType<UploadShare> localVarReturnType = new GenericType<UploadShare>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Get Upload Shares
   * ### Functional Description:   Retrieve a list of Upload Shares (aka Upload Accounts).  ### Precondition: None.  ### Effects: None.  ### &amp;#9432; Further Information: None.  ### Filtering ### &amp;#9888; All filter fields are connected via logical disjunction (**OR**) Filter string syntax: &#x60;FIELD_NAME:OPERATOR:VALUE[:VALUE...]&#x60;   Example: &gt; &#x60;name:cn:searchString_1|createdBy:cn:searchString_2&#x60;   Filter by alias name contains &#x60;searchString_1&#x60; **OR** creator info (firstname **OR** lastname **OR** login) contains &#x60;searchString_2&#x60;.  | &#x60;FIELD_NAME&#x60; | Filter Description | &#x60;OPERATOR&#x60; | Operator Description | &#x60;VALUE&#x60; | | :--- | :--- | :--- | :--- | :--- | | **&#x60;name&#x60;** | Alias name filter | &#x60;cn&#x60; | Alias name contains value. | &#x60;search String&#x60; | | **&#x60;createdBy&#x60;** | Creator info filter | &#x60;cn&#x60; | Creator info (&#x60;firstName&#x60; **OR** &#x60;lastName&#x60; **OR** &#x60;login&#x60;) contains value. | &#x60;search String&#x60; | | **&#x60;accessKey&#x60;** | Share access key filter | &#x60;cn&#x60; | Share access key contains values. | &#x60;search String&#x60; | | **&#x60;targetId&#x60;** | Target node ID | &lt;ul&gt;&lt;li&gt;&#x60;cn&#x60; (**&#x60;DEPRECATED&#x60;**)&lt;/li&gt;&lt;li&gt;&#x60;eq&#x60; (**&#x60;NEW&#x60;**)&lt;/li&gt;&lt;/ul&gt; | Target node (room, folder) ID equals value. | &#x60;positive Integer&#x60; | | **&#x60;userId&#x60;** | Creator user ID | &#x60;eq&#x60; | Creator user ID equals value. | &#x60;positive Integer&#x60; |  ### Sorting Sort string syntax: &#x60;FIELD_NAME:ORDER&#x60;   &#x60;ORDER&#x60; can be &#x60;asc&#x60; or &#x60;desc&#x60;.   Multiple sort fields are supported. Example: &gt; &#x60;name:asc|expireAt:desc&#x60;   Sort by &#x60;name&#x60; ascending **AND** by &#x60;expireAt&#x60; descending.  | &#x60;FIELD_NAME&#x60; | Description | | :--- | :--- | | **&#x60;name&#x60;** | Alias name | | **&#x60;notifyCreator&#x60;** | Notify creator on every upload | | **&#x60;expireAt&#x60;** | Expiration date | | **&#x60;createdAt&#x60;** | Creation date | | **&#x60;createdBy&#x60;** | Creator info |
   * @param filter Filter string (optional)
   * @param sort Sort string (optional)
   * @param offset Range offset (optional)
   * @param limit Range limit (optional)
   * @param xSdsAuthToken Authentication token (optional)
   * @param xSdsDateFormat Date time format (cf. [RFC 3339](https://www.ietf.org/rfc/rfc3339.txt)): * &#x60;LOCAL&#x60; * &#x60;UTC&#x60; * &#x60;OFFSET&#x60; * &#x60;EPOCH&#x60; (optional)
   * @return UploadShareList
   * @throws ApiException if fails to make API call
   */
  public UploadShareList getUploadShares(String filter, String sort, Integer offset, Integer limit, String xSdsAuthToken, String xSdsDateFormat) throws ApiException {
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

    if (xSdsAuthToken != null)
      localVarHeaderParams.put("X-Sds-Auth-Token", apiClient.parameterToString(xSdsAuthToken));
if (xSdsDateFormat != null)
      localVarHeaderParams.put("X-Sds-Date-Format", apiClient.parameterToString(xSdsDateFormat));

    
    final String[] localVarAccepts = {
      "application/json;charset=UTF-8"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] { "DRACOON-OAuth" };

    GenericType<UploadShareList> localVarReturnType = new GenericType<UploadShareList>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
}
