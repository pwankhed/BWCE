package com.ceva.utils.ftp;

public class DisconnectFromFtp{
/****** START SET/GET METHOD, DO NOT MODIFY *****/
	protected Object FtpConnection = null;
	protected String[] SiteCommand = null;
	public Object getFtpConnection() {
		return FtpConnection;
	}
	public void setFtpConnection(Object val) {
		FtpConnection = val;
	}
	public String[] getSiteCommand() {
		return SiteCommand;
	}
	public void setSiteCommand(String[] val) {
		SiteCommand = val;
	}
/****** END SET/GET METHOD, DO NOT MODIFY *****/
	public DisconnectFromFtp() {
	}
	public void invoke() throws Exception {
/* Available Variables: DO NOT MODIFY
	In  : Object FtpConnection
	In  : String[] SiteCommand
* Available Variables: DO NOT MODIFY *****/
        if ((FtpConnection instanceof FtpConnection)) {
            FtpConnection ftp = (com.ceva.utils.ftp.FtpConnection) FtpConnection;
            try {
                if (SiteCommand != null) {
                    for (int i = 0; i < SiteCommand.length; i++) {
                        ftp.doSiteCommand(SiteCommand[i]);
                    }
                }
            } finally {
                try {
                    ftp.disconnect();
                } catch (Exception e) {
                    // ignore
                }
            }
        }
    }
}
