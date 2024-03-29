/*
 * Box Platform API
 * [Box Platform](https://box.dev) provides functionality to provide access to content stored within [Box](https://box.com). It provides endpoints for basic manipulation of files and folders, management of users within an enterprise, as well as more complex topics such as legal holds and retention policies.
 *
 * OpenAPI spec version: 2.0.0
 * Contact: devrel@box.com
 *
 * NOTE: This class is auto generated by the swagger code generator program.
 * https://github.com/swagger-api/swagger-codegen.git
 * Do not edit the class manually.
 */

package ch.cyberduck.core.box.io.swagger.client.model;

import java.util.Objects;
import java.util.Arrays;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.v3.oas.annotations.media.Schema;
import org.joda.time.DateTime;
/**
 * Defines a lock on an item. This prevents the item from being moved, renamed, or otherwise changed by anyone other than the user who created the lock.  Set this to &#x60;null&#x60; to remove the lock.
 */
@Schema(description = "Defines a lock on an item. This prevents the item from being moved, renamed, or otherwise changed by anyone other than the user who created the lock.  Set this to `null` to remove the lock.")

public class FilesfileIdLock {
  /**
   * The type of this object.
   */
  public enum AccessEnum {
    LOCK("lock");

    private String value;

    AccessEnum(String value) {
      this.value = value;
    }
    @JsonValue
    public String getValue() {
      return value;
    }

    @Override
    public String toString() {
      return String.valueOf(value);
    }
    @JsonCreator
    public static AccessEnum fromValue(String text) {
      for (AccessEnum b : AccessEnum.values()) {
        if (String.valueOf(b.value).equals(text)) {
          return b;
        }
      }
      return null;
    }

  }  @JsonProperty("access")
  private AccessEnum access = null;

  @JsonProperty("expires_at")
  private DateTime expiresAt = null;

  @JsonProperty("is_download_prevented")
  private Boolean isDownloadPrevented = null;

  public FilesfileIdLock access(AccessEnum access) {
    this.access = access;
    return this;
  }

   /**
   * The type of this object.
   * @return access
  **/
  @Schema(example = "lock", description = "The type of this object.")
  public AccessEnum getAccess() {
    return access;
  }

  public void setAccess(AccessEnum access) {
    this.access = access;
  }

  public FilesfileIdLock expiresAt(DateTime expiresAt) {
    this.expiresAt = expiresAt;
    return this;
  }

   /**
   * Defines the time at which the lock expires.
   * @return expiresAt
  **/
  @Schema(example = "2012-12-12T10:53:43-08:00", description = "Defines the time at which the lock expires.")
  public DateTime getExpiresAt() {
    return expiresAt;
  }

  public void setExpiresAt(DateTime expiresAt) {
    this.expiresAt = expiresAt;
  }

  public FilesfileIdLock isDownloadPrevented(Boolean isDownloadPrevented) {
    this.isDownloadPrevented = isDownloadPrevented;
    return this;
  }

   /**
   * Defines if the file can be downloaded while it is locked.
   * @return isDownloadPrevented
  **/
  @Schema(example = "true", description = "Defines if the file can be downloaded while it is locked.")
  public Boolean isIsDownloadPrevented() {
    return isDownloadPrevented;
  }

  public void setIsDownloadPrevented(Boolean isDownloadPrevented) {
    this.isDownloadPrevented = isDownloadPrevented;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    FilesfileIdLock filesfileIdLock = (FilesfileIdLock) o;
    return Objects.equals(this.access, filesfileIdLock.access) &&
        Objects.equals(this.expiresAt, filesfileIdLock.expiresAt) &&
        Objects.equals(this.isDownloadPrevented, filesfileIdLock.isDownloadPrevented);
  }

  @Override
  public int hashCode() {
    return Objects.hash(access, expiresAt, isDownloadPrevented);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class FilesfileIdLock {\n");
    
    sb.append("    access: ").append(toIndentedString(access)).append("\n");
    sb.append("    expiresAt: ").append(toIndentedString(expiresAt)).append("\n");
    sb.append("    isDownloadPrevented: ").append(toIndentedString(isDownloadPrevented)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(java.lang.Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }

}
