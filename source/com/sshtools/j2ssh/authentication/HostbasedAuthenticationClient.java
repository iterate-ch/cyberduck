/*
 *  SSHTools - Java SSH2 API
 *
 *  Copyright (C) 2002-2003 Lee David Painter and Contributors.
 *
 *  Contributions made by:
 *
 *  Brett Smith
 *  Richard Pernavas
 *  Erwin Bolwidt
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Library General Public License
 *  as published by the Free Software Foundation; either version 2 of
 *  the License, or (at your option) any later version.
 *
 *  You may also distribute it and/or modify it under the terms of the
 *  Apache style J2SSH Software License. A copy of which should have
 *  been provided with the distribution.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  License document supplied with your distribution for more details.
 *
 */
package com.sshtools.j2ssh.authentication;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.sshtools.j2ssh.io.ByteArrayWriter;
import com.sshtools.j2ssh.transport.publickey.SshPrivateKey;
import com.sshtools.j2ssh.transport.publickey.SshPublicKey;


/**
 * @author $author$
 * @version $Revision$
 */
public class HostbasedAuthenticationClient extends SshAuthenticationClient {
	private static Log log = LogFactory.getLog(HostbasedAuthenticationClient.class);

	/**  */
	protected SshPrivateKey key;
	private String privateKeyFile = null;
	private String passphrase = null;
	private String clientUser = null;

	/**
	 * Creates a new HostbasedAuthenticationClient object.
	 */
	public HostbasedAuthenticationClient() {
	}

	/**
	 * @param key
	 */
	public void setKey(SshPrivateKey key) {
		this.key = key;
	}

	/**
	 *
	 */
	public void reset() {
		privateKeyFile = null;
		passphrase = null;
		clientUser = null;
	}

	/**
	 * @param clientUser
	 */
	public void setClientUsername(String clientUser) {
		this.clientUser = clientUser;
	}

	/**
	 * @return
	 */
	public String getMethodName() {
		return "hostbased";
	}

	/**
	 * @param authentication
	 * @param serviceToStart
	 * @throws IOException
	 * @throws TerminatedStateException
	 * @throws AuthenticationProtocolException
	 *
	 */
	public void authenticate(AuthenticationProtocolClient authentication,
	                         String serviceToStart) throws IOException, TerminatedStateException {
		if((getUsername() == null) || (key == null)) {
			throw new AuthenticationProtocolException("You must supply a username and a key");
		}

		ByteArrayWriter baw = new ByteArrayWriter();
		log.info("Generating data to sign");

		SshPublicKey pub = key.getPublicKey();
		InetAddress addr = InetAddress.getLocalHost();
		String hostname = addr.getHostName();
		log.info("Preparing hostbased authentication request for "+hostname);

		// Now prepare and send the message
		baw.writeString(pub.getAlgorithmName());
		baw.writeBinaryString(pub.getEncoded());
		baw.writeString(hostname);

		if(clientUser != null) {
			baw.writeString(clientUser);
		}
		else {
			baw.writeString(getUsername());
		}

		// Create the signature data
		ByteArrayWriter data = new ByteArrayWriter();
		data.writeBinaryString(authentication.getSessionIdentifier());
		data.write(SshMsgUserAuthRequest.SSH_MSG_USERAUTH_REQUEST);
		data.writeString(getUsername());
		data.writeString(serviceToStart);
		data.writeString(getMethodName());
		data.writeString(pub.getAlgorithmName());
		data.writeBinaryString(pub.getEncoded());
		data.writeString(hostname);

		if(clientUser != null) {
			data.writeString(clientUser);
		}
		else {
			data.writeString(getUsername());
		}

		// Generate the signature
		baw.writeBinaryString(key.generateSignature(data.toByteArray()));

		SshMsgUserAuthRequest msg = new SshMsgUserAuthRequest(getUsername(),
		    serviceToStart, getMethodName(), baw.toByteArray());
		authentication.sendMessage(msg);
	}

	/* public boolean showAuthenticationDialog(Component parent) {
	    SshPrivateKeyFile pkf = null;
	    if (privateKeyFile == null) {
	 JFileChooser chooser = new JFileChooser();
	 chooser.setFileHidingEnabled(false);
	 chooser.setDialogTitle("Select Private Key File For Authentication");
	 if (chooser.showOpenDialog(parent) == JFileChooser.APPROVE_OPTION) {
	   privateKeyFile = chooser.getSelectedFile().getAbsolutePath();
	 }
	 else {
	   return false;
	 }
	    }
	    FileInputStream in = null;
	    try {
	 pkf = SshPrivateKeyFile.parse(new File(privateKeyFile));
	    }
	    catch (InvalidSshKeyException iske) {
	 JOptionPane.showMessageDialog(parent, iske.getMessage());
	 return false;
	    }
	    catch (IOException ioe) {
	 JOptionPane.showMessageDialog(parent, ioe.getMessage());
	    }
	    // Now see if its passphrase protected
	    if (pkf.isPassphraseProtected()) {
	 if (passphrase == null) {
	   // Show the passphrase dialog
	   Window w = (Window) SwingUtilities.getAncestorOfClass(Window.class,
	       parent);
	   PassphraseDialog dialog = null;
	   if (w instanceof Frame) {
	     dialog = new PassphraseDialog( (Frame) w);
	   }
	   else if (w instanceof Dialog) {
	     dialog = new PassphraseDialog( (Dialog) w);
	   }
	   else {
	     dialog = new PassphraseDialog();
	   }
	   do {
	     dialog.setVisible(true);
	     if (dialog.isCancelled()) {
	       return false;
	     }
	     passphrase = new String(dialog.getPassphrase());
	     try {
	       key = pkf.toPrivateKey(passphrase);
	       break;
	     }
	     catch (InvalidSshKeyException ihke) {
	       dialog.setMessage("Passphrase Invalid! Try again");
	       dialog.setMessageForeground(Color.red);
	     }
	   }
	   while (true);
	 }
	 else {
	   try {
	     key = pkf.toPrivateKey(passphrase);
	   }
	   catch (InvalidSshKeyException ihke) {
	     return false;
	   }
	 }
	    }
	    else {
	 try {
	   key = pkf.toPrivateKey(null);
	 }
	 catch (InvalidSshKeyException ihke) {
	   JOptionPane.showMessageDialog(parent, ihke.getMessage());
	   return false;
	 }
	    }
	    return true;
	  }*/
	public Properties getPersistableProperties() {
		Properties properties = new Properties();

		if(getUsername() != null) {
			properties.setProperty("Username", getUsername());
		}

		if(privateKeyFile != null) {
			properties.setProperty("PrivateKey", privateKeyFile);
		}

		return properties;
	}

	/**
	 * @param properties
	 */
	public void setPersistableProperties(Properties properties) {
		setUsername(properties.getProperty("Username"));

		if(properties.getProperty("PrivateKey") != null) {
			privateKeyFile = properties.getProperty("PrivateKey");
		}

		if(properties.getProperty("Passphrase") != null) {
			passphrase = properties.getProperty("Passphrase");
		}
	}

	/**
	 * @return
	 */
	public boolean canAuthenticate() {
		return ((getUsername() != null) && (key != null));
	}
}
