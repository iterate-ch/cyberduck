package com.sshtools.j2ssh.configuration;


import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.parsers.ParserConfigurationException;

import java.io.InputStream;
import java.io.IOException;

import org.apache.log4j.Logger;
/**
 * @author unascribed
 * @version 1.0
 */

public class PlatformConfiguration extends DefaultHandler {

  private String currentElement = null;
  private Map nativeSettings = new HashMap();
  private String nativeProcessProvider = null;
  private String nativeAuthenticationProvider = null;
  private static Logger log = Logger.getLogger(PlatformConfiguration.class);

  protected PlatformConfiguration(InputStream in) throws SAXException,
                                                  ParserConfigurationException,
                                                  IOException {
    reload(in);
  }

  public void reload(InputStream in) throws SAXException,
                                                  ParserConfigurationException,
                                                  IOException {
    SAXParserFactory saxFactory = SAXParserFactory.newInstance();
    SAXParser saxParser = saxFactory.newSAXParser();

    saxParser.parse(in, this);


  }

  public void startElement(String uri, String localName,
                    String qname, Attributes attrs) throws SAXException {

      if(currentElement==null) {
        if(qname.equals("PlatformConfiguration"))
          currentElement = qname;
        nativeProcessProvider = null;
        nativeAuthenticationProvider = null;
        nativeSettings.clear();

      } else {

          if(currentElement.equals("PlatformConfiguration")) {
            if(qname.equals("NativeProcessProvider")
                || qname.equals("NativeAuthenticationProvider")
                || qname.equals("NativeSetting")) {

                currentElement = qname;
            }
            else
              throw new SAXException("Unexpected element " + qname);
          } else
            throw new SAXException("Unexpected element " + qname);

          if(qname.equals("NativeSetting")) {

            String name = attrs.getValue("Name");
            String value = attrs.getValue("Value");

            if(name==null || value==null)
              throw new SAXException("Required attributes missing for NativeSetting element");
            log.debug("NativeSetting " + name + "=" + value);
            nativeSettings.put(name, value);

          }


      }

  }

  public void characters(char ch[], int start, int length)
                                              throws SAXException {

      if(currentElement==null)
        throw new SAXException("Unexpected characters found");

      if(currentElement.equals("NativeAuthenticationProvider")) {
        nativeAuthenticationProvider = new String(ch, start, length).trim();
        log.debug("NativeAuthenticationProvider=" + nativeAuthenticationProvider);
        return;
      }

      if(currentElement.equals("NativeProcessProvider")) {
        nativeProcessProvider = new String(ch, start, length).trim();
        log.debug("NativeProcessProvider=" + nativeProcessProvider);
        return;
      }


  }

  public void endElement(String uri, String localName, String qname)
                                              throws SAXException {



      if(currentElement==null)
        throw new SAXException("Unexpected end element for " + qname);

      if(!currentElement.equals(qname))
        throw new SAXException("Unexpected end element found");

      if(currentElement.equals("PlatformConfiguration")) {
        currentElement=null;
        return;
      }

      if(!currentElement.equals("NativeSetting")
        && !currentElement.equals("NativeAuthenticationProvider")
        && !currentElement.equals("NativeProcessProvider"))
          throw new SAXException("Unexpected end element for " + qname);

      currentElement = "PlatformConfiguration";
  }

  public String getNativeAuthenticationProvider() {
    return nativeAuthenticationProvider;
  }

  public String getNativeProcessProvider() {
    return nativeProcessProvider;
  }

  public Map getNativeSettings() {
    return nativeSettings;
  }

  public static void main(String args[]) {

    try {
      PlatformConfiguration platform = ConfigurationLoader.getPlatformConfiguration();
      System.out.println(platform.toString());
   } catch(Exception e) {
      e.printStackTrace();
    }


  }

  public String toString() {
      String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
      xml+="<!-- Platform Configuration file, Determines the behaviour of platform specific services -->\n<PlatformConfiguration>\n";
      xml+="   <!-- The process provider for executing and redirecting a process -->\n";
      xml+="   <NativeProcessProvider>"+nativeProcessProvider+"</NativeProcessProvider>\n";

      xml+="   <!-- The authentication provider for authenticating users and obtaining user information -->\n";
      xml+="   <NativeAuthenticationProvider>"+nativeAuthenticationProvider+"</NativeAuthenticationProvider>\n";

      xml+="   <!-- Native settings which may be used by the process or authentication provider -->\n";
      Map.Entry entry;
      Iterator it = nativeSettings.entrySet().iterator();
      while(it.hasNext()) {
        entry = (Map.Entry)it.next();
        xml+="   "+"<NativeSetting Name=\"" + entry.getKey().toString() +
                                "\" Value=\"" + entry.getValue().toString()
                                + "\"/>\n";
      }
      xml+="</PlatformConfiguration>";
      return xml;
    }

}
