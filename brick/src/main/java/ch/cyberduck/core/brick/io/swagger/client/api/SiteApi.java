package ch.cyberduck.core.brick.io.swagger.client.api;

import ch.cyberduck.core.brick.io.swagger.client.ApiException;
import ch.cyberduck.core.brick.io.swagger.client.ApiClient;
import ch.cyberduck.core.brick.io.swagger.client.ApiResponse;
import ch.cyberduck.core.brick.io.swagger.client.Configuration;
import ch.cyberduck.core.brick.io.swagger.client.Pair;

import javax.ws.rs.core.GenericType;

import ch.cyberduck.core.brick.io.swagger.client.model.ApiKeyEntity;
import org.joda.time.DateTime;
import ch.cyberduck.core.brick.io.swagger.client.model.DnsRecordEntity;
import java.io.File;
import ch.cyberduck.core.brick.io.swagger.client.model.IpAddressEntity;
import ch.cyberduck.core.brick.io.swagger.client.model.SiteEntity;
import ch.cyberduck.core.brick.io.swagger.client.model.StatusEntity;
import ch.cyberduck.core.brick.io.swagger.client.model.UsageSnapshotEntity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaClientCodegen", date = "2021-06-30T21:29:25.490+02:00")
public class SiteApi {
  private ApiClient apiClient;

  public SiteApi() {
    this(Configuration.getDefaultApiClient());
  }

  public SiteApi(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  public ApiClient getApiClient() {
    return apiClient;
  }

  public void setApiClient(ApiClient apiClient) {
    this.apiClient = apiClient;
  }

  /**
   * Show site settings
   * Show site settings
   * @return SiteEntity
   * @throws ApiException if fails to make API call
   */
  public SiteEntity getSite() throws ApiException {
    return getSiteWithHttpInfo().getData();
      }

  /**
   * Show site settings
   * Show site settings
   * @return ApiResponse&lt;SiteEntity&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<SiteEntity> getSiteWithHttpInfo() throws ApiException {
    Object localVarPostBody = null;
    
    // create path and map variables
    String localVarPath = "/site";

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

    GenericType<SiteEntity> localVarReturnType = new GenericType<SiteEntity>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * List Api Keys
   * List Api Keys
   * @param userId User ID.  Provide a value of &#x60;0&#x60; to operate the current session&#39;s user. (optional)
   * @param cursor Used for pagination.  Send a cursor value to resume an existing list from the point at which you left off.  Get a cursor from an existing list via the X-Files-Cursor-Next header. (optional)
   * @param perPage Number of records to show per page.  (Max: 10,000, 1,000 or less is recommended). (optional)
   * @param sortBy If set, sort records by the specified field in either &#39;asc&#39; or &#39;desc&#39; direction (e.g. sort_by[last_login_at]&#x3D;desc). Valid fields are &#x60;expires_at&#x60;. (optional)
   * @param filter If set, return records where the specifiied field is equal to the supplied value. Valid fields are &#x60;expires_at&#x60;. (optional)
   * @param filterGt If set, return records where the specifiied field is greater than the supplied value. Valid fields are &#x60;expires_at&#x60;. (optional)
   * @param filterGteq If set, return records where the specifiied field is greater than or equal to the supplied value. Valid fields are &#x60;expires_at&#x60;. (optional)
   * @param filterLike If set, return records where the specifiied field is equal to the supplied value. Valid fields are &#x60;expires_at&#x60;. (optional)
   * @param filterLt If set, return records where the specifiied field is less than the supplied value. Valid fields are &#x60;expires_at&#x60;. (optional)
   * @param filterLteq If set, return records where the specifiied field is less than or equal to the supplied value. Valid fields are &#x60;expires_at&#x60;. (optional)
   * @return List&lt;ApiKeyEntity&gt;
   * @throws ApiException if fails to make API call
   */
  public List<ApiKeyEntity> getSiteApiKeys(Integer userId, String cursor, Integer perPage, Map<String, String> sortBy, Map<String, String> filter, Map<String, String> filterGt, Map<String, String> filterGteq, Map<String, String> filterLike, Map<String, String> filterLt, Map<String, String> filterLteq) throws ApiException {
    return getSiteApiKeysWithHttpInfo(userId, cursor, perPage, sortBy, filter, filterGt, filterGteq, filterLike, filterLt, filterLteq).getData();
      }

  /**
   * List Api Keys
   * List Api Keys
   * @param userId User ID.  Provide a value of &#x60;0&#x60; to operate the current session&#39;s user. (optional)
   * @param cursor Used for pagination.  Send a cursor value to resume an existing list from the point at which you left off.  Get a cursor from an existing list via the X-Files-Cursor-Next header. (optional)
   * @param perPage Number of records to show per page.  (Max: 10,000, 1,000 or less is recommended). (optional)
   * @param sortBy If set, sort records by the specified field in either &#39;asc&#39; or &#39;desc&#39; direction (e.g. sort_by[last_login_at]&#x3D;desc). Valid fields are &#x60;expires_at&#x60;. (optional)
   * @param filter If set, return records where the specifiied field is equal to the supplied value. Valid fields are &#x60;expires_at&#x60;. (optional)
   * @param filterGt If set, return records where the specifiied field is greater than the supplied value. Valid fields are &#x60;expires_at&#x60;. (optional)
   * @param filterGteq If set, return records where the specifiied field is greater than or equal to the supplied value. Valid fields are &#x60;expires_at&#x60;. (optional)
   * @param filterLike If set, return records where the specifiied field is equal to the supplied value. Valid fields are &#x60;expires_at&#x60;. (optional)
   * @param filterLt If set, return records where the specifiied field is less than the supplied value. Valid fields are &#x60;expires_at&#x60;. (optional)
   * @param filterLteq If set, return records where the specifiied field is less than or equal to the supplied value. Valid fields are &#x60;expires_at&#x60;. (optional)
   * @return ApiResponse&lt;List&lt;ApiKeyEntity&gt;&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<List<ApiKeyEntity>> getSiteApiKeysWithHttpInfo(Integer userId, String cursor, Integer perPage, Map<String, String> sortBy, Map<String, String> filter, Map<String, String> filterGt, Map<String, String> filterGteq, Map<String, String> filterLike, Map<String, String> filterLt, Map<String, String> filterLteq) throws ApiException {
    Object localVarPostBody = null;
    
    // create path and map variables
    String localVarPath = "/site/api_keys";

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    localVarQueryParams.addAll(apiClient.parameterToPairs("", "user_id", userId));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "cursor", cursor));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "per_page", perPage));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "sort_by", sortBy));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "filter", filter));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "filter_gt", filterGt));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "filter_gteq", filterGteq));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "filter_like", filterLike));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "filter_lt", filterLt));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "filter_lteq", filterLteq));

    
    
    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] {  };

    GenericType<List<ApiKeyEntity>> localVarReturnType = new GenericType<List<ApiKeyEntity>>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Show site DNS configuration.
   * Show site DNS configuration.
   * @param cursor Used for pagination.  Send a cursor value to resume an existing list from the point at which you left off.  Get a cursor from an existing list via the X-Files-Cursor-Next header. (optional)
   * @param perPage Number of records to show per page.  (Max: 10,000, 1,000 or less is recommended). (optional)
   * @return List&lt;DnsRecordEntity&gt;
   * @throws ApiException if fails to make API call
   */
  public List<DnsRecordEntity> getSiteDnsRecords(String cursor, Integer perPage) throws ApiException {
    return getSiteDnsRecordsWithHttpInfo(cursor, perPage).getData();
      }

  /**
   * Show site DNS configuration.
   * Show site DNS configuration.
   * @param cursor Used for pagination.  Send a cursor value to resume an existing list from the point at which you left off.  Get a cursor from an existing list via the X-Files-Cursor-Next header. (optional)
   * @param perPage Number of records to show per page.  (Max: 10,000, 1,000 or less is recommended). (optional)
   * @return ApiResponse&lt;List&lt;DnsRecordEntity&gt;&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<List<DnsRecordEntity>> getSiteDnsRecordsWithHttpInfo(String cursor, Integer perPage) throws ApiException {
    Object localVarPostBody = null;
    
    // create path and map variables
    String localVarPath = "/site/dns_records";

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    localVarQueryParams.addAll(apiClient.parameterToPairs("", "cursor", cursor));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "per_page", perPage));

    
    
    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] {  };

    GenericType<List<DnsRecordEntity>> localVarReturnType = new GenericType<List<DnsRecordEntity>>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * List IP Addresses associated with the current site
   * List IP Addresses associated with the current site
   * @param cursor Used for pagination.  Send a cursor value to resume an existing list from the point at which you left off.  Get a cursor from an existing list via the X-Files-Cursor-Next header. (optional)
   * @param perPage Number of records to show per page.  (Max: 10,000, 1,000 or less is recommended). (optional)
   * @return List&lt;IpAddressEntity&gt;
   * @throws ApiException if fails to make API call
   */
  public List<IpAddressEntity> getSiteIpAddresses(String cursor, Integer perPage) throws ApiException {
    return getSiteIpAddressesWithHttpInfo(cursor, perPage).getData();
      }

  /**
   * List IP Addresses associated with the current site
   * List IP Addresses associated with the current site
   * @param cursor Used for pagination.  Send a cursor value to resume an existing list from the point at which you left off.  Get a cursor from an existing list via the X-Files-Cursor-Next header. (optional)
   * @param perPage Number of records to show per page.  (Max: 10,000, 1,000 or less is recommended). (optional)
   * @return ApiResponse&lt;List&lt;IpAddressEntity&gt;&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<List<IpAddressEntity>> getSiteIpAddressesWithHttpInfo(String cursor, Integer perPage) throws ApiException {
    Object localVarPostBody = null;
    
    // create path and map variables
    String localVarPath = "/site/ip_addresses";

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    localVarQueryParams.addAll(apiClient.parameterToPairs("", "cursor", cursor));
    localVarQueryParams.addAll(apiClient.parameterToPairs("", "per_page", perPage));

    
    
    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] {  };

    GenericType<List<IpAddressEntity>> localVarReturnType = new GenericType<List<IpAddressEntity>>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Get the most recent usage snapshot (usage data for billing purposes) for a Site.
   * Get the most recent usage snapshot (usage data for billing purposes) for a Site.
   * @return UsageSnapshotEntity
   * @throws ApiException if fails to make API call
   */
  public UsageSnapshotEntity getSiteUsage() throws ApiException {
    return getSiteUsageWithHttpInfo().getData();
      }

  /**
   * Get the most recent usage snapshot (usage data for billing purposes) for a Site.
   * Get the most recent usage snapshot (usage data for billing purposes) for a Site.
   * @return ApiResponse&lt;UsageSnapshotEntity&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<UsageSnapshotEntity> getSiteUsageWithHttpInfo() throws ApiException {
    Object localVarPostBody = null;
    
    // create path and map variables
    String localVarPath = "/site/usage";

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

    GenericType<UsageSnapshotEntity> localVarReturnType = new GenericType<UsageSnapshotEntity>() {};
    return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Update site settings.
   * Update site settings.
   * @param name Site name (optional)
   * @param subdomain Site subdomain (optional)
   * @param domain Custom domain (optional)
   * @param email Main email for this site (optional)
   * @param replyToEmail Reply-to email for this site (optional)
   * @param allowBundleNames Are manual Bundle names allowed? (optional)
   * @param bundleExpiration Site-wide Bundle expiration in days (optional)
   * @param overageNotify Notify site email of overages? (optional)
   * @param welcomeEmailEnabled Will the welcome email be sent to new users? (optional)
   * @param askAboutOverwrites If false, rename conflicting files instead of asking for overwrite confirmation.  Only applies to web interface. (optional)
   * @param showRequestAccessLink Show request access link for users without access?  Currently unused. (optional)
   * @param welcomeEmailCc Include this email in welcome emails if enabled (optional)
   * @param welcomeCustomText Custom text send in user welcome email (optional)
   * @param language Site default language (optional)
   * @param windowsModeFtp Does FTP user Windows emulation mode? (optional)
   * @param defaultTimeZone Site default time zone (optional)
   * @param desktopApp Is the desktop app enabled? (optional)
   * @param desktopAppSessionIpPinning Is desktop app session IP pinning enabled? (optional)
   * @param desktopAppSessionLifetime Desktop app session lifetime (in hours) (optional)
   * @param folderPermissionsGroupsOnly If true, permissions for this site must be bound to a group (not a user). Otherwise, permissions must be bound to a user. (optional)
   * @param welcomeScreen Does the welcome screen appear? (optional)
   * @param officeIntegrationAvailable Allow users to use Office for the web? (optional)
   * @param sessionExpiry Session expiry in hours (optional)
   * @param sslRequired Is SSL required?  Disabling this is insecure. (optional)
   * @param tlsDisabled Is TLS disabled(site setting)? (optional)
   * @param userLockout Will users be locked out after incorrect login attempts? (optional)
   * @param userLockoutTries Number of login tries within &#x60;user_lockout_within&#x60; hours before users are locked out (optional)
   * @param userLockoutWithin Number of hours for user lockout window (optional)
   * @param userLockoutLockPeriod How many hours to lock user out for failed password? (optional)
   * @param includePasswordInWelcomeEmail Include password in emails to new users? (optional)
   * @param allowedCountries Comma seperated list of allowed Country codes (optional)
   * @param allowedIps List of allowed IP addresses (optional)
   * @param disallowedCountries Comma seperated list of disallowed Country codes (optional)
   * @param daysToRetainBackups Number of days to keep deleted files (optional)
   * @param maxPriorPasswords Number of prior passwords to disallow (optional)
   * @param passwordValidityDays Number of days password is valid (optional)
   * @param passwordMinLength Shortest password length for users (optional)
   * @param passwordRequireLetter Require a letter in passwords? (optional)
   * @param passwordRequireMixed Require lower and upper case letters in passwords? (optional)
   * @param passwordRequireSpecial Require special characters in password? (optional)
   * @param passwordRequireNumber Require a number in passwords? (optional)
   * @param passwordRequireUnbreached Require passwords that have not been previously breached? (see https://haveibeenpwned.com/) (optional)
   * @param sftpUserRootEnabled Use user FTP roots also for SFTP? (optional)
   * @param disablePasswordReset Is password reset disabled? (optional)
   * @param immutableFiles Are files protected from modification? (optional)
   * @param sessionPinnedByIp Are sessions locked to the same IP? (i.e. do users need to log in again if they change IPs?) (optional)
   * @param bundlePasswordRequired Do Bundles require password protection? (optional)
   * @param bundleRequireShareRecipient Do Bundles require recipients for sharing? (optional)
   * @param passwordRequirementsApplyToBundles Require bundles&#39; passwords, and passwords for other items (inboxes, public shares, etc.) to conform to the same requirements as users&#39; passwords? (optional)
   * @param optOutGlobal Use servers in the USA only? (optional)
   * @param useProvidedModifiedAt Allow uploaders to set &#x60;provided_modified_at&#x60; for uploaded files? (optional)
   * @param customNamespace Is this site using a custom namespace for users? (optional)
   * @param disableUsersFromInactivityPeriodDays If greater than zero, users will unable to login if they do not show activity within this number of days. (optional)
   * @param nonSsoGroupsAllowed If true, groups can be manually created / modified / deleted by Site Admins. Otherwise, groups can only be managed via your SSO provider. (optional)
   * @param nonSsoUsersAllowed If true, users can be manually created / modified / deleted by Site Admins. Otherwise, users can only be managed via your SSO provider. (optional)
   * @param sharingEnabled Allow bundle creation (optional)
   * @param userRequestsEnabled Enable User Requests feature (optional)
   * @param allowed2faMethodSms Is SMS two factor authentication allowed? (optional)
   * @param allowed2faMethodU2f Is U2F two factor authentication allowed? (optional)
   * @param allowed2faMethodTotp Is TOTP two factor authentication allowed? (optional)
   * @param allowed2faMethodYubi Is yubikey two factor authentication allowed? (optional)
   * @param require2fa Require two-factor authentication for all users? (optional)
   * @param require2faUserType What type of user is required to use two-factor authentication (when require_2fa is set to &#x60;true&#x60; for this site)? (optional)
   * @param color2Top Top bar background color (optional)
   * @param color2Left Page link and button color (optional)
   * @param color2Link Top bar link color (optional)
   * @param color2Text Page link and button color (optional)
   * @param color2TopText Top bar text color (optional)
   * @param siteHeader Custom site header text (optional)
   * @param siteFooter Custom site footer text (optional)
   * @param loginHelpText Login help text (optional)
   * @param smtpAddress SMTP server hostname or IP (optional)
   * @param smtpAuthentication SMTP server authentication type (optional)
   * @param smtpFrom From address to use when mailing through custom SMTP (optional)
   * @param smtpUsername SMTP server username (optional)
   * @param smtpPort SMTP server port (optional)
   * @param ldapEnabled Main LDAP setting: is LDAP enabled? (optional)
   * @param ldapType LDAP type (optional)
   * @param ldapHost LDAP host (optional)
   * @param ldapHost2 LDAP backup host (optional)
   * @param ldapHost3 LDAP backup host (optional)
   * @param ldapPort LDAP port (optional)
   * @param ldapSecure Use secure LDAP? (optional)
   * @param ldapUsername Username for signing in to LDAP server. (optional)
   * @param ldapUsernameField LDAP username field (optional)
   * @param ldapDomain Domain name that will be appended to usernames (optional)
   * @param ldapUserAction Should we sync users from LDAP server? (optional)
   * @param ldapGroupAction Should we sync groups from LDAP server? (optional)
   * @param ldapUserIncludeGroups Comma or newline separated list of group names (with optional wildcards) - if provided, only users in these groups will be added or synced. (optional)
   * @param ldapGroupExclusion Comma or newline separated list of group names (with optional wildcards) to exclude when syncing. (optional)
   * @param ldapGroupInclusion Comma or newline separated list of group names (with optional wildcards) to include when syncing. (optional)
   * @param ldapBaseDn Base DN for looking up users in LDAP server (optional)
   * @param icon16File  (optional)
   * @param icon16Delete If true, will delete the file stored in icon16 (optional)
   * @param icon32File  (optional)
   * @param icon32Delete If true, will delete the file stored in icon32 (optional)
   * @param icon48File  (optional)
   * @param icon48Delete If true, will delete the file stored in icon48 (optional)
   * @param icon128File  (optional)
   * @param icon128Delete If true, will delete the file stored in icon128 (optional)
   * @param logoFile  (optional)
   * @param logoDelete If true, will delete the file stored in logo (optional)
   * @param disable2faWithDelay If set to true, we will begin the process of disabling 2FA on this site. (optional)
   * @param ldapPasswordChange New LDAP password. (optional)
   * @param ldapPasswordChangeConfirmation Confirm new LDAP password. (optional)
   * @param smtpPassword Password for SMTP server. (optional)
   * @return SiteEntity
   * @throws ApiException if fails to make API call
   */
  public SiteEntity patchSite(String name, String subdomain, String domain, String email, String replyToEmail, Boolean allowBundleNames, Integer bundleExpiration, Boolean overageNotify, Boolean welcomeEmailEnabled, Boolean askAboutOverwrites, Boolean showRequestAccessLink, String welcomeEmailCc, String welcomeCustomText, String language, Boolean windowsModeFtp, String defaultTimeZone, Boolean desktopApp, Boolean desktopAppSessionIpPinning, Integer desktopAppSessionLifetime, Boolean folderPermissionsGroupsOnly, String welcomeScreen, Boolean officeIntegrationAvailable, Double sessionExpiry, Boolean sslRequired, Boolean tlsDisabled, Boolean userLockout, Integer userLockoutTries, Integer userLockoutWithin, Integer userLockoutLockPeriod, Boolean includePasswordInWelcomeEmail, String allowedCountries, String allowedIps, String disallowedCountries, Integer daysToRetainBackups, Integer maxPriorPasswords, Integer passwordValidityDays, Integer passwordMinLength, Boolean passwordRequireLetter, Boolean passwordRequireMixed, Boolean passwordRequireSpecial, Boolean passwordRequireNumber, Boolean passwordRequireUnbreached, Boolean sftpUserRootEnabled, Boolean disablePasswordReset, Boolean immutableFiles, Boolean sessionPinnedByIp, Boolean bundlePasswordRequired, Boolean bundleRequireShareRecipient, Boolean passwordRequirementsApplyToBundles, Boolean optOutGlobal, Boolean useProvidedModifiedAt, Boolean customNamespace, Integer disableUsersFromInactivityPeriodDays, Boolean nonSsoGroupsAllowed, Boolean nonSsoUsersAllowed, Boolean sharingEnabled, Boolean userRequestsEnabled, Boolean allowed2faMethodSms, Boolean allowed2faMethodU2f, Boolean allowed2faMethodTotp, Boolean allowed2faMethodYubi, Boolean require2fa, String require2faUserType, String color2Top, String color2Left, String color2Link, String color2Text, String color2TopText, String siteHeader, String siteFooter, String loginHelpText, String smtpAddress, String smtpAuthentication, String smtpFrom, String smtpUsername, Integer smtpPort, Boolean ldapEnabled, String ldapType, String ldapHost, String ldapHost2, String ldapHost3, Integer ldapPort, Boolean ldapSecure, String ldapUsername, String ldapUsernameField, String ldapDomain, String ldapUserAction, String ldapGroupAction, String ldapUserIncludeGroups, String ldapGroupExclusion, String ldapGroupInclusion, String ldapBaseDn, File icon16File, Boolean icon16Delete, File icon32File, Boolean icon32Delete, File icon48File, Boolean icon48Delete, File icon128File, Boolean icon128Delete, File logoFile, Boolean logoDelete, Boolean disable2faWithDelay, String ldapPasswordChange, String ldapPasswordChangeConfirmation, String smtpPassword) throws ApiException {
    return patchSiteWithHttpInfo(name, subdomain, domain, email, replyToEmail, allowBundleNames, bundleExpiration, overageNotify, welcomeEmailEnabled, askAboutOverwrites, showRequestAccessLink, welcomeEmailCc, welcomeCustomText, language, windowsModeFtp, defaultTimeZone, desktopApp, desktopAppSessionIpPinning, desktopAppSessionLifetime, folderPermissionsGroupsOnly, welcomeScreen, officeIntegrationAvailable, sessionExpiry, sslRequired, tlsDisabled, userLockout, userLockoutTries, userLockoutWithin, userLockoutLockPeriod, includePasswordInWelcomeEmail, allowedCountries, allowedIps, disallowedCountries, daysToRetainBackups, maxPriorPasswords, passwordValidityDays, passwordMinLength, passwordRequireLetter, passwordRequireMixed, passwordRequireSpecial, passwordRequireNumber, passwordRequireUnbreached, sftpUserRootEnabled, disablePasswordReset, immutableFiles, sessionPinnedByIp, bundlePasswordRequired, bundleRequireShareRecipient, passwordRequirementsApplyToBundles, optOutGlobal, useProvidedModifiedAt, customNamespace, disableUsersFromInactivityPeriodDays, nonSsoGroupsAllowed, nonSsoUsersAllowed, sharingEnabled, userRequestsEnabled, allowed2faMethodSms, allowed2faMethodU2f, allowed2faMethodTotp, allowed2faMethodYubi, require2fa, require2faUserType, color2Top, color2Left, color2Link, color2Text, color2TopText, siteHeader, siteFooter, loginHelpText, smtpAddress, smtpAuthentication, smtpFrom, smtpUsername, smtpPort, ldapEnabled, ldapType, ldapHost, ldapHost2, ldapHost3, ldapPort, ldapSecure, ldapUsername, ldapUsernameField, ldapDomain, ldapUserAction, ldapGroupAction, ldapUserIncludeGroups, ldapGroupExclusion, ldapGroupInclusion, ldapBaseDn, icon16File, icon16Delete, icon32File, icon32Delete, icon48File, icon48Delete, icon128File, icon128Delete, logoFile, logoDelete, disable2faWithDelay, ldapPasswordChange, ldapPasswordChangeConfirmation, smtpPassword).getData();
      }

  /**
   * Update site settings.
   * Update site settings.
   * @param name Site name (optional)
   * @param subdomain Site subdomain (optional)
   * @param domain Custom domain (optional)
   * @param email Main email for this site (optional)
   * @param replyToEmail Reply-to email for this site (optional)
   * @param allowBundleNames Are manual Bundle names allowed? (optional)
   * @param bundleExpiration Site-wide Bundle expiration in days (optional)
   * @param overageNotify Notify site email of overages? (optional)
   * @param welcomeEmailEnabled Will the welcome email be sent to new users? (optional)
   * @param askAboutOverwrites If false, rename conflicting files instead of asking for overwrite confirmation.  Only applies to web interface. (optional)
   * @param showRequestAccessLink Show request access link for users without access?  Currently unused. (optional)
   * @param welcomeEmailCc Include this email in welcome emails if enabled (optional)
   * @param welcomeCustomText Custom text send in user welcome email (optional)
   * @param language Site default language (optional)
   * @param windowsModeFtp Does FTP user Windows emulation mode? (optional)
   * @param defaultTimeZone Site default time zone (optional)
   * @param desktopApp Is the desktop app enabled? (optional)
   * @param desktopAppSessionIpPinning Is desktop app session IP pinning enabled? (optional)
   * @param desktopAppSessionLifetime Desktop app session lifetime (in hours) (optional)
   * @param folderPermissionsGroupsOnly If true, permissions for this site must be bound to a group (not a user). Otherwise, permissions must be bound to a user. (optional)
   * @param welcomeScreen Does the welcome screen appear? (optional)
   * @param officeIntegrationAvailable Allow users to use Office for the web? (optional)
   * @param sessionExpiry Session expiry in hours (optional)
   * @param sslRequired Is SSL required?  Disabling this is insecure. (optional)
   * @param tlsDisabled Is TLS disabled(site setting)? (optional)
   * @param userLockout Will users be locked out after incorrect login attempts? (optional)
   * @param userLockoutTries Number of login tries within &#x60;user_lockout_within&#x60; hours before users are locked out (optional)
   * @param userLockoutWithin Number of hours for user lockout window (optional)
   * @param userLockoutLockPeriod How many hours to lock user out for failed password? (optional)
   * @param includePasswordInWelcomeEmail Include password in emails to new users? (optional)
   * @param allowedCountries Comma seperated list of allowed Country codes (optional)
   * @param allowedIps List of allowed IP addresses (optional)
   * @param disallowedCountries Comma seperated list of disallowed Country codes (optional)
   * @param daysToRetainBackups Number of days to keep deleted files (optional)
   * @param maxPriorPasswords Number of prior passwords to disallow (optional)
   * @param passwordValidityDays Number of days password is valid (optional)
   * @param passwordMinLength Shortest password length for users (optional)
   * @param passwordRequireLetter Require a letter in passwords? (optional)
   * @param passwordRequireMixed Require lower and upper case letters in passwords? (optional)
   * @param passwordRequireSpecial Require special characters in password? (optional)
   * @param passwordRequireNumber Require a number in passwords? (optional)
   * @param passwordRequireUnbreached Require passwords that have not been previously breached? (see https://haveibeenpwned.com/) (optional)
   * @param sftpUserRootEnabled Use user FTP roots also for SFTP? (optional)
   * @param disablePasswordReset Is password reset disabled? (optional)
   * @param immutableFiles Are files protected from modification? (optional)
   * @param sessionPinnedByIp Are sessions locked to the same IP? (i.e. do users need to log in again if they change IPs?) (optional)
   * @param bundlePasswordRequired Do Bundles require password protection? (optional)
   * @param bundleRequireShareRecipient Do Bundles require recipients for sharing? (optional)
   * @param passwordRequirementsApplyToBundles Require bundles&#39; passwords, and passwords for other items (inboxes, public shares, etc.) to conform to the same requirements as users&#39; passwords? (optional)
   * @param optOutGlobal Use servers in the USA only? (optional)
   * @param useProvidedModifiedAt Allow uploaders to set &#x60;provided_modified_at&#x60; for uploaded files? (optional)
   * @param customNamespace Is this site using a custom namespace for users? (optional)
   * @param disableUsersFromInactivityPeriodDays If greater than zero, users will unable to login if they do not show activity within this number of days. (optional)
   * @param nonSsoGroupsAllowed If true, groups can be manually created / modified / deleted by Site Admins. Otherwise, groups can only be managed via your SSO provider. (optional)
   * @param nonSsoUsersAllowed If true, users can be manually created / modified / deleted by Site Admins. Otherwise, users can only be managed via your SSO provider. (optional)
   * @param sharingEnabled Allow bundle creation (optional)
   * @param userRequestsEnabled Enable User Requests feature (optional)
   * @param allowed2faMethodSms Is SMS two factor authentication allowed? (optional)
   * @param allowed2faMethodU2f Is U2F two factor authentication allowed? (optional)
   * @param allowed2faMethodTotp Is TOTP two factor authentication allowed? (optional)
   * @param allowed2faMethodYubi Is yubikey two factor authentication allowed? (optional)
   * @param require2fa Require two-factor authentication for all users? (optional)
   * @param require2faUserType What type of user is required to use two-factor authentication (when require_2fa is set to &#x60;true&#x60; for this site)? (optional)
   * @param color2Top Top bar background color (optional)
   * @param color2Left Page link and button color (optional)
   * @param color2Link Top bar link color (optional)
   * @param color2Text Page link and button color (optional)
   * @param color2TopText Top bar text color (optional)
   * @param siteHeader Custom site header text (optional)
   * @param siteFooter Custom site footer text (optional)
   * @param loginHelpText Login help text (optional)
   * @param smtpAddress SMTP server hostname or IP (optional)
   * @param smtpAuthentication SMTP server authentication type (optional)
   * @param smtpFrom From address to use when mailing through custom SMTP (optional)
   * @param smtpUsername SMTP server username (optional)
   * @param smtpPort SMTP server port (optional)
   * @param ldapEnabled Main LDAP setting: is LDAP enabled? (optional)
   * @param ldapType LDAP type (optional)
   * @param ldapHost LDAP host (optional)
   * @param ldapHost2 LDAP backup host (optional)
   * @param ldapHost3 LDAP backup host (optional)
   * @param ldapPort LDAP port (optional)
   * @param ldapSecure Use secure LDAP? (optional)
   * @param ldapUsername Username for signing in to LDAP server. (optional)
   * @param ldapUsernameField LDAP username field (optional)
   * @param ldapDomain Domain name that will be appended to usernames (optional)
   * @param ldapUserAction Should we sync users from LDAP server? (optional)
   * @param ldapGroupAction Should we sync groups from LDAP server? (optional)
   * @param ldapUserIncludeGroups Comma or newline separated list of group names (with optional wildcards) - if provided, only users in these groups will be added or synced. (optional)
   * @param ldapGroupExclusion Comma or newline separated list of group names (with optional wildcards) to exclude when syncing. (optional)
   * @param ldapGroupInclusion Comma or newline separated list of group names (with optional wildcards) to include when syncing. (optional)
   * @param ldapBaseDn Base DN for looking up users in LDAP server (optional)
   * @param icon16File  (optional)
   * @param icon16Delete If true, will delete the file stored in icon16 (optional)
   * @param icon32File  (optional)
   * @param icon32Delete If true, will delete the file stored in icon32 (optional)
   * @param icon48File  (optional)
   * @param icon48Delete If true, will delete the file stored in icon48 (optional)
   * @param icon128File  (optional)
   * @param icon128Delete If true, will delete the file stored in icon128 (optional)
   * @param logoFile  (optional)
   * @param logoDelete If true, will delete the file stored in logo (optional)
   * @param disable2faWithDelay If set to true, we will begin the process of disabling 2FA on this site. (optional)
   * @param ldapPasswordChange New LDAP password. (optional)
   * @param ldapPasswordChangeConfirmation Confirm new LDAP password. (optional)
   * @param smtpPassword Password for SMTP server. (optional)
   * @return ApiResponse&lt;SiteEntity&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<SiteEntity> patchSiteWithHttpInfo(String name, String subdomain, String domain, String email, String replyToEmail, Boolean allowBundleNames, Integer bundleExpiration, Boolean overageNotify, Boolean welcomeEmailEnabled, Boolean askAboutOverwrites, Boolean showRequestAccessLink, String welcomeEmailCc, String welcomeCustomText, String language, Boolean windowsModeFtp, String defaultTimeZone, Boolean desktopApp, Boolean desktopAppSessionIpPinning, Integer desktopAppSessionLifetime, Boolean folderPermissionsGroupsOnly, String welcomeScreen, Boolean officeIntegrationAvailable, Double sessionExpiry, Boolean sslRequired, Boolean tlsDisabled, Boolean userLockout, Integer userLockoutTries, Integer userLockoutWithin, Integer userLockoutLockPeriod, Boolean includePasswordInWelcomeEmail, String allowedCountries, String allowedIps, String disallowedCountries, Integer daysToRetainBackups, Integer maxPriorPasswords, Integer passwordValidityDays, Integer passwordMinLength, Boolean passwordRequireLetter, Boolean passwordRequireMixed, Boolean passwordRequireSpecial, Boolean passwordRequireNumber, Boolean passwordRequireUnbreached, Boolean sftpUserRootEnabled, Boolean disablePasswordReset, Boolean immutableFiles, Boolean sessionPinnedByIp, Boolean bundlePasswordRequired, Boolean bundleRequireShareRecipient, Boolean passwordRequirementsApplyToBundles, Boolean optOutGlobal, Boolean useProvidedModifiedAt, Boolean customNamespace, Integer disableUsersFromInactivityPeriodDays, Boolean nonSsoGroupsAllowed, Boolean nonSsoUsersAllowed, Boolean sharingEnabled, Boolean userRequestsEnabled, Boolean allowed2faMethodSms, Boolean allowed2faMethodU2f, Boolean allowed2faMethodTotp, Boolean allowed2faMethodYubi, Boolean require2fa, String require2faUserType, String color2Top, String color2Left, String color2Link, String color2Text, String color2TopText, String siteHeader, String siteFooter, String loginHelpText, String smtpAddress, String smtpAuthentication, String smtpFrom, String smtpUsername, Integer smtpPort, Boolean ldapEnabled, String ldapType, String ldapHost, String ldapHost2, String ldapHost3, Integer ldapPort, Boolean ldapSecure, String ldapUsername, String ldapUsernameField, String ldapDomain, String ldapUserAction, String ldapGroupAction, String ldapUserIncludeGroups, String ldapGroupExclusion, String ldapGroupInclusion, String ldapBaseDn, File icon16File, Boolean icon16Delete, File icon32File, Boolean icon32Delete, File icon48File, Boolean icon48Delete, File icon128File, Boolean icon128Delete, File logoFile, Boolean logoDelete, Boolean disable2faWithDelay, String ldapPasswordChange, String ldapPasswordChangeConfirmation, String smtpPassword) throws ApiException {
    Object localVarPostBody = null;
    
    // create path and map variables
    String localVarPath = "/site";

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


    
    if (name != null)
      localVarFormParams.put("name", name);
if (subdomain != null)
      localVarFormParams.put("subdomain", subdomain);
if (domain != null)
      localVarFormParams.put("domain", domain);
if (email != null)
      localVarFormParams.put("email", email);
if (replyToEmail != null)
      localVarFormParams.put("reply_to_email", replyToEmail);
if (allowBundleNames != null)
      localVarFormParams.put("allow_bundle_names", allowBundleNames);
if (bundleExpiration != null)
      localVarFormParams.put("bundle_expiration", bundleExpiration);
if (overageNotify != null)
      localVarFormParams.put("overage_notify", overageNotify);
if (welcomeEmailEnabled != null)
      localVarFormParams.put("welcome_email_enabled", welcomeEmailEnabled);
if (askAboutOverwrites != null)
      localVarFormParams.put("ask_about_overwrites", askAboutOverwrites);
if (showRequestAccessLink != null)
      localVarFormParams.put("show_request_access_link", showRequestAccessLink);
if (welcomeEmailCc != null)
      localVarFormParams.put("welcome_email_cc", welcomeEmailCc);
if (welcomeCustomText != null)
      localVarFormParams.put("welcome_custom_text", welcomeCustomText);
if (language != null)
      localVarFormParams.put("language", language);
if (windowsModeFtp != null)
      localVarFormParams.put("windows_mode_ftp", windowsModeFtp);
if (defaultTimeZone != null)
      localVarFormParams.put("default_time_zone", defaultTimeZone);
if (desktopApp != null)
      localVarFormParams.put("desktop_app", desktopApp);
if (desktopAppSessionIpPinning != null)
      localVarFormParams.put("desktop_app_session_ip_pinning", desktopAppSessionIpPinning);
if (desktopAppSessionLifetime != null)
      localVarFormParams.put("desktop_app_session_lifetime", desktopAppSessionLifetime);
if (folderPermissionsGroupsOnly != null)
      localVarFormParams.put("folder_permissions_groups_only", folderPermissionsGroupsOnly);
if (welcomeScreen != null)
      localVarFormParams.put("welcome_screen", welcomeScreen);
if (officeIntegrationAvailable != null)
      localVarFormParams.put("office_integration_available", officeIntegrationAvailable);
if (sessionExpiry != null)
      localVarFormParams.put("session_expiry", sessionExpiry);
if (sslRequired != null)
      localVarFormParams.put("ssl_required", sslRequired);
if (tlsDisabled != null)
      localVarFormParams.put("tls_disabled", tlsDisabled);
if (userLockout != null)
      localVarFormParams.put("user_lockout", userLockout);
if (userLockoutTries != null)
      localVarFormParams.put("user_lockout_tries", userLockoutTries);
if (userLockoutWithin != null)
      localVarFormParams.put("user_lockout_within", userLockoutWithin);
if (userLockoutLockPeriod != null)
      localVarFormParams.put("user_lockout_lock_period", userLockoutLockPeriod);
if (includePasswordInWelcomeEmail != null)
      localVarFormParams.put("include_password_in_welcome_email", includePasswordInWelcomeEmail);
if (allowedCountries != null)
      localVarFormParams.put("allowed_countries", allowedCountries);
if (allowedIps != null)
      localVarFormParams.put("allowed_ips", allowedIps);
if (disallowedCountries != null)
      localVarFormParams.put("disallowed_countries", disallowedCountries);
if (daysToRetainBackups != null)
      localVarFormParams.put("days_to_retain_backups", daysToRetainBackups);
if (maxPriorPasswords != null)
      localVarFormParams.put("max_prior_passwords", maxPriorPasswords);
if (passwordValidityDays != null)
      localVarFormParams.put("password_validity_days", passwordValidityDays);
if (passwordMinLength != null)
      localVarFormParams.put("password_min_length", passwordMinLength);
if (passwordRequireLetter != null)
      localVarFormParams.put("password_require_letter", passwordRequireLetter);
if (passwordRequireMixed != null)
      localVarFormParams.put("password_require_mixed", passwordRequireMixed);
if (passwordRequireSpecial != null)
      localVarFormParams.put("password_require_special", passwordRequireSpecial);
if (passwordRequireNumber != null)
      localVarFormParams.put("password_require_number", passwordRequireNumber);
if (passwordRequireUnbreached != null)
      localVarFormParams.put("password_require_unbreached", passwordRequireUnbreached);
if (sftpUserRootEnabled != null)
      localVarFormParams.put("sftp_user_root_enabled", sftpUserRootEnabled);
if (disablePasswordReset != null)
      localVarFormParams.put("disable_password_reset", disablePasswordReset);
if (immutableFiles != null)
      localVarFormParams.put("immutable_files", immutableFiles);
if (sessionPinnedByIp != null)
      localVarFormParams.put("session_pinned_by_ip", sessionPinnedByIp);
if (bundlePasswordRequired != null)
      localVarFormParams.put("bundle_password_required", bundlePasswordRequired);
if (bundleRequireShareRecipient != null)
      localVarFormParams.put("bundle_require_share_recipient", bundleRequireShareRecipient);
if (passwordRequirementsApplyToBundles != null)
      localVarFormParams.put("password_requirements_apply_to_bundles", passwordRequirementsApplyToBundles);
if (optOutGlobal != null)
      localVarFormParams.put("opt_out_global", optOutGlobal);
if (useProvidedModifiedAt != null)
      localVarFormParams.put("use_provided_modified_at", useProvidedModifiedAt);
if (customNamespace != null)
      localVarFormParams.put("custom_namespace", customNamespace);
if (disableUsersFromInactivityPeriodDays != null)
      localVarFormParams.put("disable_users_from_inactivity_period_days", disableUsersFromInactivityPeriodDays);
if (nonSsoGroupsAllowed != null)
      localVarFormParams.put("non_sso_groups_allowed", nonSsoGroupsAllowed);
if (nonSsoUsersAllowed != null)
      localVarFormParams.put("non_sso_users_allowed", nonSsoUsersAllowed);
if (sharingEnabled != null)
      localVarFormParams.put("sharing_enabled", sharingEnabled);
if (userRequestsEnabled != null)
      localVarFormParams.put("user_requests_enabled", userRequestsEnabled);
if (allowed2faMethodSms != null)
      localVarFormParams.put("allowed_2fa_method_sms", allowed2faMethodSms);
if (allowed2faMethodU2f != null)
      localVarFormParams.put("allowed_2fa_method_u2f", allowed2faMethodU2f);
if (allowed2faMethodTotp != null)
      localVarFormParams.put("allowed_2fa_method_totp", allowed2faMethodTotp);
if (allowed2faMethodYubi != null)
      localVarFormParams.put("allowed_2fa_method_yubi", allowed2faMethodYubi);
if (require2fa != null)
      localVarFormParams.put("require_2fa", require2fa);
if (require2faUserType != null)
      localVarFormParams.put("require_2fa_user_type", require2faUserType);
if (color2Top != null)
      localVarFormParams.put("color2_top", color2Top);
if (color2Left != null)
      localVarFormParams.put("color2_left", color2Left);
if (color2Link != null)
      localVarFormParams.put("color2_link", color2Link);
if (color2Text != null)
      localVarFormParams.put("color2_text", color2Text);
if (color2TopText != null)
      localVarFormParams.put("color2_top_text", color2TopText);
if (siteHeader != null)
      localVarFormParams.put("site_header", siteHeader);
if (siteFooter != null)
      localVarFormParams.put("site_footer", siteFooter);
if (loginHelpText != null)
      localVarFormParams.put("login_help_text", loginHelpText);
if (smtpAddress != null)
      localVarFormParams.put("smtp_address", smtpAddress);
if (smtpAuthentication != null)
      localVarFormParams.put("smtp_authentication", smtpAuthentication);
if (smtpFrom != null)
      localVarFormParams.put("smtp_from", smtpFrom);
if (smtpUsername != null)
      localVarFormParams.put("smtp_username", smtpUsername);
if (smtpPort != null)
      localVarFormParams.put("smtp_port", smtpPort);
if (ldapEnabled != null)
      localVarFormParams.put("ldap_enabled", ldapEnabled);
if (ldapType != null)
      localVarFormParams.put("ldap_type", ldapType);
if (ldapHost != null)
      localVarFormParams.put("ldap_host", ldapHost);
if (ldapHost2 != null)
      localVarFormParams.put("ldap_host_2", ldapHost2);
if (ldapHost3 != null)
      localVarFormParams.put("ldap_host_3", ldapHost3);
if (ldapPort != null)
      localVarFormParams.put("ldap_port", ldapPort);
if (ldapSecure != null)
      localVarFormParams.put("ldap_secure", ldapSecure);
if (ldapUsername != null)
      localVarFormParams.put("ldap_username", ldapUsername);
if (ldapUsernameField != null)
      localVarFormParams.put("ldap_username_field", ldapUsernameField);
if (ldapDomain != null)
      localVarFormParams.put("ldap_domain", ldapDomain);
if (ldapUserAction != null)
      localVarFormParams.put("ldap_user_action", ldapUserAction);
if (ldapGroupAction != null)
      localVarFormParams.put("ldap_group_action", ldapGroupAction);
if (ldapUserIncludeGroups != null)
      localVarFormParams.put("ldap_user_include_groups", ldapUserIncludeGroups);
if (ldapGroupExclusion != null)
      localVarFormParams.put("ldap_group_exclusion", ldapGroupExclusion);
if (ldapGroupInclusion != null)
      localVarFormParams.put("ldap_group_inclusion", ldapGroupInclusion);
if (ldapBaseDn != null)
      localVarFormParams.put("ldap_base_dn", ldapBaseDn);
if (icon16File != null)
      localVarFormParams.put("icon16_file", icon16File);
if (icon16Delete != null)
      localVarFormParams.put("icon16_delete", icon16Delete);
if (icon32File != null)
      localVarFormParams.put("icon32_file", icon32File);
if (icon32Delete != null)
      localVarFormParams.put("icon32_delete", icon32Delete);
if (icon48File != null)
      localVarFormParams.put("icon48_file", icon48File);
if (icon48Delete != null)
      localVarFormParams.put("icon48_delete", icon48Delete);
if (icon128File != null)
      localVarFormParams.put("icon128_file", icon128File);
if (icon128Delete != null)
      localVarFormParams.put("icon128_delete", icon128Delete);
if (logoFile != null)
      localVarFormParams.put("logo_file", logoFile);
if (logoDelete != null)
      localVarFormParams.put("logo_delete", logoDelete);
if (disable2faWithDelay != null)
      localVarFormParams.put("disable_2fa_with_delay", disable2faWithDelay);
if (ldapPasswordChange != null)
      localVarFormParams.put("ldap_password_change", ldapPasswordChange);
if (ldapPasswordChangeConfirmation != null)
      localVarFormParams.put("ldap_password_change_confirmation", ldapPasswordChangeConfirmation);
if (smtpPassword != null)
      localVarFormParams.put("smtp_password", smtpPassword);

    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json", "application/x-www-form-urlencoded", "multipart/form-data"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] {  };

    GenericType<SiteEntity> localVarReturnType = new GenericType<SiteEntity>() {};
    return apiClient.invokeAPI(localVarPath, "PATCH", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Create Api Key
   * Create Api Key
   * @param userId User ID.  Provide a value of &#x60;0&#x60; to operate the current session&#39;s user. (optional)
   * @param name Internal name for the API Key.  For your use. (optional)
   * @param expiresAt API Key expiration date (optional)
   * @param permissionSet Permissions for this API Key.  Keys with the &#x60;desktop_app&#x60; permission set only have the ability to do the functions provided in our Desktop App (File and Share Link operations).  Additional permission sets may become available in the future, such as for a Site Admin to give a key with no administrator privileges.  If you have ideas for permission sets, please let us know. (optional, default to full)
   * @param path Folder path restriction for this api key. (optional)
   * @return ApiKeyEntity
   * @throws ApiException if fails to make API call
   */
  public ApiKeyEntity postSiteApiKeys(Integer userId, String name, DateTime expiresAt, String permissionSet, String path) throws ApiException {
    return postSiteApiKeysWithHttpInfo(userId, name, expiresAt, permissionSet, path).getData();
      }

  /**
   * Create Api Key
   * Create Api Key
   * @param userId User ID.  Provide a value of &#x60;0&#x60; to operate the current session&#39;s user. (optional)
   * @param name Internal name for the API Key.  For your use. (optional)
   * @param expiresAt API Key expiration date (optional)
   * @param permissionSet Permissions for this API Key.  Keys with the &#x60;desktop_app&#x60; permission set only have the ability to do the functions provided in our Desktop App (File and Share Link operations).  Additional permission sets may become available in the future, such as for a Site Admin to give a key with no administrator privileges.  If you have ideas for permission sets, please let us know. (optional, default to full)
   * @param path Folder path restriction for this api key. (optional)
   * @return ApiResponse&lt;ApiKeyEntity&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<ApiKeyEntity> postSiteApiKeysWithHttpInfo(Integer userId, String name, DateTime expiresAt, String permissionSet, String path) throws ApiException {
    Object localVarPostBody = null;
    
    // create path and map variables
    String localVarPath = "/site/api_keys";

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


    
    if (userId != null)
      localVarFormParams.put("user_id", userId);
if (name != null)
      localVarFormParams.put("name", name);
if (expiresAt != null)
      localVarFormParams.put("expires_at", expiresAt);
if (permissionSet != null)
      localVarFormParams.put("permission_set", permissionSet);
if (path != null)
      localVarFormParams.put("path", path);

    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json", "application/x-www-form-urlencoded", "multipart/form-data"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] {  };

    GenericType<ApiKeyEntity> localVarReturnType = new GenericType<ApiKeyEntity>() {};
    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
  /**
   * Test webhook.
   * Test webhook.
   * @param url URL for testing the webhook. (required)
   * @param method HTTP method(GET or POST). (optional)
   * @param encoding HTTP encoding method.  Can be JSON, XML, or RAW (form data). (optional)
   * @param headers Additional request headers. (optional)
   * @param body Additional body parameters. (optional)
   * @param action action for test body (optional)
   * @return StatusEntity
   * @throws ApiException if fails to make API call
   */
  public StatusEntity postSiteTestWebhook(String url, String method, String encoding, Map<String, String> headers, Map<String, String> body, String action) throws ApiException {
    return postSiteTestWebhookWithHttpInfo(url, method, encoding, headers, body, action).getData();
      }

  /**
   * Test webhook.
   * Test webhook.
   * @param url URL for testing the webhook. (required)
   * @param method HTTP method(GET or POST). (optional)
   * @param encoding HTTP encoding method.  Can be JSON, XML, or RAW (form data). (optional)
   * @param headers Additional request headers. (optional)
   * @param body Additional body parameters. (optional)
   * @param action action for test body (optional)
   * @return ApiResponse&lt;StatusEntity&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<StatusEntity> postSiteTestWebhookWithHttpInfo(String url, String method, String encoding, Map<String, String> headers, Map<String, String> body, String action) throws ApiException {
    Object localVarPostBody = null;
    
    // verify the required parameter 'url' is set
    if (url == null) {
      throw new ApiException(400, "Missing the required parameter 'url' when calling postSiteTestWebhook");
    }
    
    // create path and map variables
    String localVarPath = "/site/test-webhook";

    // query params
    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();


    
    if (url != null)
      localVarFormParams.put("url", url);
if (method != null)
      localVarFormParams.put("method", method);
if (encoding != null)
      localVarFormParams.put("encoding", encoding);
if (headers != null)
      localVarFormParams.put("headers", headers);
if (body != null)
      localVarFormParams.put("body", body);
if (action != null)
      localVarFormParams.put("action", action);

    final String[] localVarAccepts = {
      "application/json"
    };
    final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

    final String[] localVarContentTypes = {
      "application/json", "application/x-www-form-urlencoded", "multipart/form-data"
    };
    final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] {  };

    GenericType<StatusEntity> localVarReturnType = new GenericType<StatusEntity>() {};
    return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
      }
}
