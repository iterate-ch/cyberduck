package com.sshtools.j2ssh.configuration;

public class ExtensionAlgorithm {

  private String name;
  private String implClass;

  public ExtensionAlgorithm() {
  }

  public String getAlgorithmName() {
    return name;
  }

  public String getImplementationClass() {
    return implClass;
  }

  public void setAlgorithmName(String name) {
    this.name = name;
  }

  public void setImplementationClass(String implClass) {
    this.implClass = implClass;
  }
}
