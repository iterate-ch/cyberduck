package ch.cyberduck.core.box.io.swagger.client.api;

import ch.cyberduck.core.box.io.swagger.client.ApiException;
import ch.cyberduck.core.box.io.swagger.client.ApiClient;
import ch.cyberduck.core.box.io.swagger.client.Configuration;
import ch.cyberduck.core.box.io.swagger.client.Pair;

import javax.ws.rs.core.GenericType;

import ch.cyberduck.core.box.io.swagger.client.model.ClientError;
import ch.cyberduck.core.box.io.swagger.client.model.ConflictError;
import ch.cyberduck.core.box.io.swagger.client.model.File;
import ch.cyberduck.core.box.io.swagger.client.model.FileIdCopyBody;
import ch.cyberduck.core.box.io.swagger.client.model.FilesContentBody1;
import ch.cyberduck.core.box.io.swagger.client.model.FilesFileIdBody;
import ch.cyberduck.core.box.io.swagger.client.model.UploadUrl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FilesApi {
  private ApiClient apiClient;

  public FilesApi() {
    this(Configuration.getDefaultApiClient());
  }

  public FilesApi(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  public ApiClient getApiClient() {
    return apiClient;
  }

  public void setApiClient(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  /**
   * Delete file
   * Deletes a file, either permanently or by moving it to the trash.  The the enterprise settings determine whether the item will be permanently deleted from Box or moved to the trash.
   * @param fileId The unique identifier that represent a file.  The ID for any file can be determined by visiting a file in the web application and copying the ID from the URL. For example, for the URL &#x60;https://_*.app.box.com/files/123&#x60; the &#x60;file_id&#x60; is &#x60;123&#x60;. (required)
   * @param ifMatch Ensures this item hasn&#x27;t recently changed before making changes.  Pass in the item&#x27;s last observed &#x60;etag&#x60; value into this header and the endpoint will fail with a &#x60;412 Precondition Failed&#x60; if it has changed since. (optional)
   * @throws ApiException if fails to make API call
   */
  public void deleteFilesId(String fileId, String ifMatch) throws ApiException {
    Object localVarPostBody = null;
    // verify the required parameter 'fileId' is set
    if (fileId == null) {
      throw new ApiException(400, "Missing the required parameter 'fileId' when calling deleteFilesId");
    }
    // create path and map variables
    String localVarPath = "/files/{file_id}"
      .replaceAll("\\{" + "file_id" + "\\}", apiClient.escapeString(fileId.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


    if (ifMatch != null)
      localVarHeaderParams.put("if-match", apiClient.parameterToString(ifMatch));

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
   * Get file information
   * Retrieves the details about a file.
   * @param fileId The unique identifier that represent a file.  The ID for any file can be determined by visiting a file in the web application and copying the ID from the URL. For example, for the URL &#x60;https://_*.app.box.com/files/123&#x60; the &#x60;file_id&#x60; is &#x60;123&#x60;. (required)
   * @param xRepHints A header required to request specific &#x60;representations&#x60; of a file. Use this in combination with the &#x60;fields&#x60; query parameter to request a specific file representation.  The general format for these representations is &#x60;X-Rep-Hints: [...]&#x60; where &#x60;[...]&#x60; is one or many hints in the format &#x60;[fileType?query]&#x60;.  For example, to request a &#x60;png&#x60; representation in &#x60;32x32&#x60; as well as &#x60;94x94&#x60; pixel dimensions provide the following hints.  &#x60;X-Rep-Hints: [jpg?dimensions&#x3D;32x32][jpg?dimensions&#x3D;94x94]&#x60;  Additionally, a &#x60;text&#x60; representation is available for all document file types in Box using the &#x60;[extracted_text]&#x60; representation.  &#x60;X-Rep-Hints: [extracted_text]&#x60; (required)
   * @param fields A comma-separated list of attributes to include in the response. This can be used to request fields that are not normally returned in a standard response.  Be aware that specifying this parameter will have the effect that none of the standard fields are returned in the response unless explicitly specified, instead only fields for the mini representation are returned, additional to the fields requested.  Additionally this field can be used to query any metadata applied to the file by specifying the &#x60;metadata&#x60; field as well as the scope and key of the template to retrieve, for example &#x60;?field&#x3D;metadata.enterprise_12345.contractTemplate&#x60;. (optional)
   * @param ifNoneMatch Ensures an item is only returned if it has changed.  Pass in the item&#x27;s last observed &#x60;etag&#x60; value into this header and the endpoint will fail with a &#x60;304 Not Modified&#x60; if the item has not changed since. (optional)
   * @param boxapi The URL, and optional password, for the shared link of this item.  This header can be used to access items that have not been explicitly shared with a user.  Use the format &#x60;shared_link&#x3D;[link]&#x60; or if a password is required then use &#x60;shared_link&#x3D;[link]&amp;shared_link_password&#x3D;[password]&#x60;.  This header can be used on the file or folder shared, as well as on any files or folders nested within the item. (optional)
   * @return File
   * @throws ApiException if fails to make API call
   */
  public File getFilesId(String fileId, String xRepHints, List<String> fields, String ifNoneMatch, String boxapi) throws ApiException {
    Object localVarPostBody = null;
    // verify the required parameter 'fileId' is set
    if (fileId == null) {
      throw new ApiException(400, "Missing the required parameter 'fileId' when calling getFilesId");
    }
    // verify the required parameter 'xRepHints' is set
    if (xRepHints == null) {
      throw new ApiException(400, "Missing the required parameter 'xRepHints' when calling getFilesId");
    }
    // create path and map variables
    String localVarPath = "/files/{file_id}"
      .replaceAll("\\{" + "file_id" + "\\}", apiClient.escapeString(fileId.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    localVarQueryParams.addAll(apiClient.parameterToPairs("csv", "fields", fields));

    if (ifNoneMatch != null)
      localVarHeaderParams.put("if-none-match", apiClient.parameterToString(ifNoneMatch));
    if (boxapi != null)
      localVarHeaderParams.put("boxapi", apiClient.parameterToString(boxapi));
    if (xRepHints != null)
      localVarHeaderParams.put("x-rep-hints", apiClient.parameterToString(xRepHints));

    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] {  };

    GenericType<File> localVarReturnType = new GenericType<File>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
  /**
   * Preflight check before upload
   * Performs a check to verify that a file will be accepted by Box before you upload the entire file.
   * @param body  (optional)
   * @return UploadUrl
   * @throws ApiException if fails to make API call
   */
  public UploadUrl optionsFilesContent(FilesContentBody1 body) throws ApiException {
    Object localVarPostBody = body;
    // create path and map variables
    String localVarPath = "/files/content";

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();



    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] {  };

    GenericType<UploadUrl> localVarReturnType = new GenericType<UploadUrl>() {};
    return apiClient.invokeAPI(localVarPath, "OPTIONS", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
  /**
   * Copy file
   * Creates a copy of a file.
   * @param fileId The unique identifier that represent a file.  The ID for any file can be determined by visiting a file in the web application and copying the ID from the URL. For example, for the URL &#x60;https://_*.app.box.com/files/123&#x60; the &#x60;file_id&#x60; is &#x60;123&#x60;. (required)
   * @param body  (optional)
   * @param ifMatch Ensures this item hasn&#x27;t recently changed before making changes.  Pass in the item&#x27;s last observed &#x60;etag&#x60; value into this header and the endpoint will fail with a &#x60;412 Precondition Failed&#x60; if it has changed since. (optional)
   * @param fields A comma-separated list of attributes to include in the response. This can be used to request fields that are not normally returned in a standard response.  Be aware that specifying this parameter will have the effect that none of the standard fields are returned in the response unless explicitly specified, instead only fields for the mini representation are returned, additional to the fields requested. (optional)
   * @return File
   * @throws ApiException if fails to make API call
   */
  public File postFilesIdCopy(String fileId, FileIdCopyBody body, String ifMatch, List<String> fields) throws ApiException {
    Object localVarPostBody = body;
    // verify the required parameter 'fileId' is set
    if (fileId == null) {
      throw new ApiException(400, "Missing the required parameter 'fileId' when calling postFilesIdCopy");
    }
    // create path and map variables
    String localVarPath = "/files/{file_id}/copy"
      .replaceAll("\\{" + "file_id" + "\\}", apiClient.escapeString(fileId.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    localVarQueryParams.addAll(apiClient.parameterToPairs("csv", "fields", fields));

    if (ifMatch != null)
      localVarHeaderParams.put("if-match", apiClient.parameterToString(ifMatch));

    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] {  };

    GenericType<File> localVarReturnType = new GenericType<File>() {};
    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
  /**
   * Update file
   * Updates a file. This can be used to rename or move a file, create a shared link, or lock a file.
   * @param fileId The unique identifier that represent a file.  The ID for any file can be determined by visiting a file in the web application and copying the ID from the URL. For example, for the URL &#x60;https://_*.app.box.com/files/123&#x60; the &#x60;file_id&#x60; is &#x60;123&#x60;. (required)
   * @param body  (optional)
   * @param ifMatch Ensures this item hasn&#x27;t recently changed before making changes.  Pass in the item&#x27;s last observed &#x60;etag&#x60; value into this header and the endpoint will fail with a &#x60;412 Precondition Failed&#x60; if it has changed since. (optional)
   * @param fields A comma-separated list of attributes to include in the response. This can be used to request fields that are not normally returned in a standard response.  Be aware that specifying this parameter will have the effect that none of the standard fields are returned in the response unless explicitly specified, instead only fields for the mini representation are returned, additional to the fields requested. (optional)
   * @return File
   * @throws ApiException if fails to make API call
   */
  public File putFilesId(String fileId, FilesFileIdBody body, String ifMatch, List<String> fields) throws ApiException {
    Object localVarPostBody = body;
    // verify the required parameter 'fileId' is set
    if (fileId == null) {
      throw new ApiException(400, "Missing the required parameter 'fileId' when calling putFilesId");
    }
    // create path and map variables
    String localVarPath = "/files/{file_id}"
      .replaceAll("\\{" + "file_id" + "\\}", apiClient.escapeString(fileId.toString()));

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    localVarQueryParams.addAll(apiClient.parameterToPairs("csv", "fields", fields));

    if (ifMatch != null)
      localVarHeaderParams.put("if-match", apiClient.parameterToString(ifMatch));

    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] {  };

    GenericType<File> localVarReturnType = new GenericType<File>() {};
    return apiClient.invokeAPI(localVarPath, "PUT", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
  }
}
