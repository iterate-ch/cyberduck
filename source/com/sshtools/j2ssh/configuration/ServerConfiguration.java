package com.sshtools.j2ssh.configuration;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Iterator;

import java.io.InputStream;
import java.io.IOException;
import java.io.File;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import org.apache.log4j.Logger;

public class ServerConfiguration extends DefaultHandler {

  private Map allowedSubsystems = new HashMap();
  private Map serverHostKeys = new HashMap();
  private List allowedAuthentications = new ArrayList();
  private List requiredAuthentications = new ArrayList();
  private int commandPort = 10091;
  private int port = 22;
  private String listenAddress = "";
  private int maxConnections = 10;
  private String terminalProvider = "";
  private String authorizationFile = "";
  private String userConfigDirectory = "";
  private String authenticationBanner = "";
  private static Logger log = Logger.getLogger(ServerConfiguration.class);
  private String currentElement = null;

  public ServerConfiguration(InputStream in)
              throws SAXException, ParserConfigurationException,
                          IOException {
    reload(in);
  }

  public void reload(InputStream in)
          throws SAXException, ParserConfigurationException,
                          IOException {

    allowedSubsystems.clear();
    serverHostKeys.clear();
    allowedAuthentications.clear();
    requiredAuthentications.clear();
    commandPort = 10091;
    port = 22;
    listenAddress = "";
    maxConnections = 10;
    terminalProvider = "";
    authorizationFile = "";
    userConfigDirectory = "";
    authenticationBanner = "";
    currentElement = null;

    SAXParserFactory saxFactory = SAXParserFactory.newInstance();
    SAXParser saxParser = saxFactory.newSAXParser();
    saxParser.parse(in, this);

  }

  public void startElement(String uri, String localName, String qname,
                             Attributes attrs)
                      throws SAXException {

      if (currentElement==null) {
          if (!qname.equals("ServerConfiguration")) {
              throw new SAXException("Unexpected root element " + qname);
          }
      } else {
          if (currentElement.equals("ServerConfiguration")) {

                if(qname.equals("ServerHostKey")) {

                  String algorithm = attrs.getValue("AlgorithmName");
                  String privateKey = attrs.getValue("PrivateKeyFile");
                  System.out.println(privateKey);
                  if(algorithm==null || privateKey==null)
                    throw new SAXException("Required attributes missing from <ServerHostKey> element");
                  log.debug("ServerHostKey AlgorithmName=" + algorithm + " PrivateKeyFile=" + privateKey);

                  File f = new File(privateKey);
                  if(f.exists())
                    serverHostKeys.put(algorithm, privateKey);
                  else {
                    privateKey = ConfigurationLoader.getConfigurationDirectory() + privateKey;
                    f = new File(privateKey);
                    if(f.exists())
                      serverHostKeys.put(algorithm, privateKey);
                    else
                      log.warn("Private key file '" + privateKey + "' could not be found");
                  }
                } else if(qname.equals("Subsystem")) {

                  String type = attrs.getValue("Type");
                  String name = attrs.getValue("Name");
                  String provider = attrs.getValue("Provider");

                  if(type==null || name==null || provider==null)
                    throw new SAXException("Required attributes missing from <Subsystem> element");
                  log.debug("Subsystem Type=" + type + " Name=" + name + " Provider=" + provider);
                  allowedSubsystems.put(name, new AllowedSubsystem(type, name, provider));

                } else if(!qname.equals("AuthenticationBanner")
                  && !qname.equals("MaxConnections")
                  && !qname.equals("ListenAddress")
                  && !qname.equals("Port")
                  && !qname.equals("CommandPort")
                  && !qname.equals("TerminalProvider")
                  && !qname.equals("AllowedAuthentication")
                  && !qname.equals("RequiredAuthentication")
                  && !qname.equals("AuthorizationFile")
                  && !qname.equals("UserConfigDirectory"))
                    throw new SAXException("Unexpected <" + qname + "> element after SshAPIConfiguration");
          }
      }

      currentElement = qname;
  }


  public void characters(char ch[], int start, int length)
                    throws SAXException {

    String value = new String(ch, start, length);
    if (currentElement!=null) {
      if(currentElement.equals("AuthenticationBanner")) {
        authenticationBanner = value;
        log.debug("AuthenticationBanner=" + authenticationBanner);
      } else if(currentElement.equals("MaxConnections")) {
        maxConnections = Integer.parseInt(value);
        log.debug("MaxConnections=" + value);
      } else if(currentElement.equals("ListenAddress")) {
        listenAddress = value;
        log.debug("ListenAddress=" + listenAddress);
      } else if(currentElement.equals("Port")) {
        port = Integer.parseInt(value);
        log.debug("Port=" + value);
      } else if(currentElement.equals("CommandPort")) {
        commandPort = Integer.parseInt(value);
        log.debug("CommandPort=" + value);
      } else if(currentElement.equals("TerminalProvider")) {
        terminalProvider = value;
        log.debug("TerminalProvider=" + terminalProvider);
      } else if(currentElement.equals("AllowedAuthentication")) {
        if(!allowedAuthentications.contains(value)) {
          allowedAuthentications.add(value);
          log.debug("AllowedAuthentication=" + value);
        }
      } else if(currentElement.equals("RequiredAuthentication")) {
        if(!requiredAuthentications.contains(value)) {
           requiredAuthentications.add(value);
           log.debug("RequiredAuthentication=" + value);
        }
      } else if(currentElement.equals("AuthorizationFile")) {
        authorizationFile = value;
        log.debug("AuthorizationFile=" + authorizationFile);
      } else if(currentElement.equals("UserConfigDirectory")) {
        userConfigDirectory = value;
        log.debug("UserConfigDirectory=" + userConfigDirectory);
      }
    }

  }


  public void endElement(String uri, String localName, String qname)
                    throws SAXException {
     if (currentElement!=null) {
       if (!currentElement.equals(qname)) {
           throw new SAXException("Unexpected end element found <" + qname + ">");
       } else if (currentElement.equals("ServerConfiguration")) {
                currentElement = null;
       } else if(currentElement.equals("AuthenticationBanner")
                  || currentElement.equals("ServerHostKey")
                  || currentElement.equals("Subsystem")
                  || currentElement.equals("MaxConnections")
                  || currentElement.equals("ListenAddress")
                  || currentElement.equals("Port")
                  || currentElement.equals("CommandPort")
                  || currentElement.equals("TerminalProvider")
                  || currentElement.equals("AllowedAuthentication")
                  || currentElement.equals("RequiredAuthentication")
                  || currentElement.equals("AuthorizationFile")
                  || currentElement.equals("UserConfigDirectory"))
        currentElement = "ServerConfiguration";
      } else
        throw new SAXException("Unexpected end element <" + qname + "> found");
  }


  public List getRequiredAuthentications() {
    return requiredAuthentications;
  }

  public List getAllowedAuthentications() {
    return allowedAuthentications;
  }

  public String getAuthenticationBanner() {
    return authenticationBanner;
  }

  public int getCommandPort() {
    return commandPort;
  }

  public String getUserConfigDirectory() {
    return userConfigDirectory;
  }

  public String getAuthorizationFile() {
    return authorizationFile;
  }

  public String getListenAddress() {
    return listenAddress;
  }

  public int getMaxConnections() {
    return maxConnections;
  }

  public int getPort() {
    return port;
  }

  public Map getServerHostKeys() {
    return serverHostKeys;
  }

  public Map getSubsystems() {
    return allowedSubsystems;
  }

  public String getTerminalProvider() {
    return terminalProvider;
  }

  public String toString() {
     String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
     xml += "<!-- Server configuration file - If filenames are not absolute they are assummed to be in the same directory as this configuration file. -->\n";
     xml += "<ServerConfiguration>\n";

     xml+= "   <!-- The available host keys for server authentication -->\n";
     Map.Entry entry;
     Iterator it = serverHostKeys.entrySet().iterator();
     while (it.hasNext()) {
         entry = (Map.Entry)it.next();
         xml += "   <ServerHostKey AlgorithmName=\"" + entry.getKey() + "\" PrivateKeyFile=\"" + entry.getValue() + "\"/>\n";
     }

     xml+= "   <!-- Add any number of subsystem elements here -->\n";
     AllowedSubsystem subsystem;
     it = allowedSubsystems.entrySet().iterator();
     while (it.hasNext()) {
         subsystem = (AllowedSubsystem)((Map.Entry)it.next()).getValue();
         xml += "   <Subsystem Name=\"" + subsystem.getName() + "\" Type=\"" + subsystem.getType() + "\" Provider=\"" + subsystem.getProvider() + "\"/>\n";
     }

     xml+= "   <!-- Display the following authentication banner before authentication -->\n";
     xml +="   <AuthenticationBanner>" + authenticationBanner + "</AuthenticationBanner>\n";
     xml+= "   <!-- The maximum number of connected sessions available -->\n";
     xml +="   <MaxConnections>" + String.valueOf(maxConnections) + "</MaxConnections>\n";
     xml+= "   <!-- Bind to the following address to listen for connections -->\n";
     xml +="   <ListenAddress>" + listenAddress + "</ListenAddress>\n";
     xml+= "   <!-- The port to listen to -->\n";
     xml +="   <Port>" + String.valueOf(port) + "</Port>\n";
     xml+= "   <!-- Listen on the following port (on localhost) for server commands such as stop -->\n";
     xml +="   <CommandPort>" + String.valueOf(commandPort) + "</CommandPort>\n";
     xml+= "   <!-- Specify the executable that provides the default shell -->\n";
     xml +="   <TerminalProvider>" + terminalProvider + "</TerminalProvider>\n";

     xml+= "   <!-- Specify any number of allowed authentications -->\n";
     it = allowedAuthentications.iterator();
     while(it.hasNext()) {
        xml+="   <AllowedAuthentication>" + it.next().toString() + "</AllowedAuthentication>\n";
     }

     xml+= "   <!-- Specify any number of required authentications -->\n";
     it = requiredAuthentications.iterator();
     while(it.hasNext()) {
        xml+="   <RequiredAuthentication>" + it.next().toString() + "</RequiredAuthentication>\n";
     }

     xml+= "   <!-- The users authorizations file -->\n";
     xml +="   <AuthorizationFile>" + authorizationFile + "</AuthorizationFile>\n";
     xml+= "   <!-- The users configuration directory where files such as AuthorizationFile are found. For users home directory specify %D For users name specify %U  -->\n";
     xml +="   <UserConfigDirectory>" + userConfigDirectory + "</UserConfigDirectory>\n";

     xml += "</ServerConfiguration>";

     return xml;
  }

}
