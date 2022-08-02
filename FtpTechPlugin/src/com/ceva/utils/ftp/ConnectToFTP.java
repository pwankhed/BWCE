package com.ceva.utils.ftp;

import java.util.*;

public class ConnectToFTP{
/****** START SET/GET METHOD, DO NOT MODIFY *****/
	protected String Hostname = "";
	protected int Port = 0;
	protected String Username = "";
	protected String Password = "";
	protected String DefaultTransferMode = "";
	protected String DefaultFileMode = "";
	protected int ConnectionTimeoutMSec = 0;
	protected int DefaultDataTimeoutMSec = 0;
	protected String[] SiteCommand = null;
	protected Object FtpConnection = null;
	public String getHostname() {
		return Hostname;
	}
	public void setHostname(String val) {
		Hostname = val;
	}
	public int getPort() {
		return Port;
	}
	public void setPort(int val) {
		Port = val;
	}
	public String getUsername() {
		return Username;
	}
	public void setUsername(String val) {
		Username = val;
	}
	public String getPassword() {
		return Password;
	}
	public void setPassword(String val) {
		Password = val;
	}
	public String getDefaultTransferMode() {
		return DefaultTransferMode;
	}
	public void setDefaultTransferMode(String val) {
		DefaultTransferMode = val;
	}
	public String getDefaultFileMode() {
		return DefaultFileMode;
	}
	public void setDefaultFileMode(String val) {
		DefaultFileMode = val;
	}
	public int getConnectionTimeoutMSec() {
		return ConnectionTimeoutMSec;
	}
	public void setConnectionTimeoutMSec(int val) {
		ConnectionTimeoutMSec = val;
	}
	public int getDefaultDataTimeoutMSec() {
		return DefaultDataTimeoutMSec;
	}
	public void setDefaultDataTimeoutMSec(int val) {
		DefaultDataTimeoutMSec = val;
	}
	public String[] getSiteCommand() {
		return SiteCommand;
	}
	public void setSiteCommand(String[] val) {
		SiteCommand = val;
	}
	public Object getFtpConnection() {
		return FtpConnection;
	}
	public void setFtpConnection(Object val) {
		FtpConnection = val;
	}
/****** END SET/GET METHOD, DO NOT MODIFY *****/
	public ConnectToFTP() {

	}
	
	public ConnectToFTP(String Hostname,int Port,String Username,String Password,String DefaultTransferMode,String DefaultFileMode,int ConnectionTimeoutMSec,int DefaultDataTimeoutMSec) {
		setHostname(Hostname);
		setPort(Port);
		setUsername(Username);
		setPassword(Password);
		setDefaultTransferMode(DefaultTransferMode);
		setDefaultFileMode(DefaultFileMode);
		setConnectionTimeoutMSec(ConnectionTimeoutMSec);
		setDefaultDataTimeoutMSec(DefaultDataTimeoutMSec);
		
	}

	public void invoke() throws Exception {
/* Available Variables: DO NOT MODIFY
	In  : String Hostname
	In  : int Port
	In  : String Username
	In  : String Password
	In  : String DefaultTransferMode
	In  : String DefaultFileMode
	In  : int ConnectionTimeoutMSec
	In  : int DefaultDataTimeoutMSec
	In  : String[] SiteCommand
	Out : Object FtpConnection
* Available Variables: DO NOT MODIFY *****/
            FtpConnection ftp = new com.ceva.utils.ftp.FtpConnection();
            ftp.setHost(getHostname());
            if (getPort() > 0) {
                ftp.setPort(getPort());
            } else {
                ftp.setPort(21);
            }
            if (getUsername() != null && getUsername().length() > 0) {
                ftp.setUser(getUsername());
            }
            if (getPassword() != null && getPassword().length() > 0) {
                ftp.setPassword(getPassword());
            }
            if (getDefaultTransferMode() != null && getDefaultTransferMode().length() > 0) {
                ftp.setDefaultTransferMode(getDefaultTransferMode());
            }
            if (getDefaultFileMode() != null && getDefaultFileMode().length() > 0) {
                ftp.setDefaultFileMode(getDefaultFileMode());
            }
            if (getConnectionTimeoutMSec() > 0) {
                ftp.setConnectTimeout(getConnectionTimeoutMSec());
            }
            if (getDefaultDataTimeoutMSec() > 0) {
                ftp.setDataTimeout(getDefaultDataTimeoutMSec());
            }
            ftp.connect();
            setFtpConnection(ftp);
            if (SiteCommand != null) {
                for (int i = 0; i < SiteCommand.length; i++) {
                    ftp.doSiteCommand(SiteCommand[i]);
                }
            }
}
}
