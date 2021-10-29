package ch.cyberduck.core.box.io.swagger.client.api;

import ch.cyberduck.core.box.io.swagger.client.ApiException;
import ch.cyberduck.core.box.io.swagger.client.ApiClient;
import ch.cyberduck.core.box.io.swagger.client.Configuration;
import ch.cyberduck.core.box.io.swagger.client.Pair;

import javax.ws.rs.core.GenericType;

import ch.cyberduck.core.box.io.swagger.client.model.ClientError;
import ch.cyberduck.core.box.io.swagger.client.model.File;
import ch.cyberduck.core.box.io.swagger.client.model.Files;
import ch.cyberduck.core.box.io.swagger.client.model.FilescontentAttributes;
import ch.cyberduck.core.box.io.swagger.client.model.FilesfileIdcontentAttributes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UploadsApi {
  private ApiClient apiClient;

  public UploadsApi() {
    this(Configuration.getDefaultApiClient());
  }

  public UploadsApi(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  public ApiClient getApiClient() {
    return apiClient;
  }

  public void setApiClient(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  /**
   * Upload file
   * Uploads a small file to Box. For file sizes over 50MB we recommend using the Chunk Upload APIs.  # Request body order  The &#x60;attributes&#x60; part of the body must come **before** the &#x60;file&#x60; part. Requests that do not follow this format when uploading the file will receive a HTTP &#x60;400&#x60; error with a &#x60;metadata_after_file_contents&#x60; error code.
   * @param attributes  (optional)
   * @param file  (optional)
   * @param contentMd5 An optional header containing the SHA1 hash of the file to ensure that the file was not corrupted in transit. (optional)
   * @param fields A comma-separated list of attributes to include in the response. This can be used to request fields that are not normally returned in a standard response.  Be aware that specifying this parameter will have the effect that none of the standard fields are returned in the response unless explicitly specified, instead only fields for the mini representation are returned, additional to the fields requested. (optional)
   * @return Files
   * @throws ApiException if fails to make API call
   */
  public Files postFilesContent(FilescontentAttributes attributes, File file, String contentMd5, List<String> fields) throws ApiException {
    Object localVarPostBody = null;
    // create path and map variables
    String localVarPath = "/files/content";

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    localVarQueryParams.addAll(apiClient.parameterToPairs("csv", "fields", fields));

    if (contentMd5 != null)
      localVarHeaderParams.put("content-md5", apiClient.parameterToString(contentMd5));
    if (attributes != null)
      localVarFormParams.put("attributes", attributes);
    if (file != null)
      localVarFormParams.put("file", file);

    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "multipart/form-data"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] {  };

    GenericType<Files> localVarReturnType = new GenericType<Files>() {};
    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
  /**
   * Upload file version
   * Update a file&#x27;s content. For file sizes over 50MB we recommend using the Chunk Upload APIs.  # Request body order  The &#x60;attributes&#x60; part of the body must come **before** the &#x60;file&#x60; part. Requests that do not follow this format when uploading the file will receive a HTTP &#x60;400&#x60; error with a &#x60;metadata_after_file_contents&#x60; error code.
   * @param fileId The unique identifier that represent a file.  The ID for any file can be determined by visiting a file in the web application and copying the ID from the URL. For example, for the URL &#x60;https://_*.app.box.com/files/123&#x60; the &#x60;file_id&#x60; is &#x60;123&#x60;. (required)
   * @param attributes  (optional)
   * @param file  (optional)
   * @param ifMatch Ensures this item hasn&#x27;t recently changed before making changes.  Pass in the item&#x27;s last observed &#x60;etag&#x60; value into this header and the endpoint will fail with a &#x60;412 Precondition Failed&#x60; if it has changed since. (optional)
   * @param contentMd5 An optional header containing the SHA1 hash of the file to ensure that the file was not corrupted in transit. (optional)
   * @param fields A comma-separated list of attributes to include in the response. This can be used to request fields that are not normally returned in a standard response.  Be aware that specifying this parameter will have the effect that none of the standard fields are returned in the response unless explicitly specified, instead only fields for the mini representation are returned, additional to the fields requested. (optional)
   * @return Files
   * @throws ApiException if fails to make API call
   */
  public Files postFilesIdContent(String fileId, FilesfileIdcontentAttributes attributes, File file, String ifMatch, String contentMd5, List<String> fields) throws ApiException {
    Object localVarPostBody = null;
    // verify the required parameter 'fileId' is set
    if (fileId == null) {
      throw new ApiException(400, "Missing the required parameter 'fileId' when calling postFilesIdContent");
    }
    // create path and map variables
    String localVarPath = "/files/{file_id}/content"
      .replaceAll("\\{" + "file_id" + "\\}", apiClient.escapeString(fileId.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    localVarQueryParams.addAll(apiClient.parameterToPairs("csv", "fields", fields));

    if (ifMatch != null)
      localVarHeaderParams.put("if-match", apiClient.parameterToString(ifMatch));
    if (contentMd5 != null)
      localVarHeaderParams.put("content-md5", apiClient.parameterToString(contentMd5));
    if (attributes != null)
      localVarFormParams.put("attributes", attributes);
    if (file != null)
      localVarFormParams.put("file", file);

    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "multipart/form-data"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] {  };

    GenericType<Files> localVarReturnType = new GenericType<Files>() {};
    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
}
