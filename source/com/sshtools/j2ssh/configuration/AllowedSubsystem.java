package com.sshtools.j2ssh.configuration;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author unascribed
 * @version 1.0
 */

public class AllowedSubsystem {

  private String type;
  private String name;
  private String provider;

  protected AllowedSubsystem(String type, String name, String provider) {
    this.type = type;
    this.name = name;
    this.provider = provider;
  }

  public String getType() {
    return type;
  }

  public String getName() {
    return name;
  }

  public String getProvider() {
    return provider;
  }

}
