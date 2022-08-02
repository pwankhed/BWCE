package com.ceva.utils.ftp;


public class SetRemotePath{
/****** START SET/GET METHOD, DO NOT MODIFY *****/
	protected Object FtpConnection = null;
	protected String RemotePath = "";
	protected String[] SiteCommand = null;
	public Object getFtpConnection() {
		return FtpConnection;
	}
	public void setFtpConnection(Object val) {
		FtpConnection = val;
	}
	public String getRemotePath() {
		return RemotePath;
	}
	public void setRemotePath(String val) {
		RemotePath = val;
	}
	public String[] getSiteCommand() {
		return SiteCommand;
	}
	public void setSiteCommand(String[] val) {
		SiteCommand = val;
	}
/****** END SET/GET METHOD, DO NOT MODIFY *****/
	public SetRemotePath() {
	}
	public void invoke() throws Exception {
/* Available Variables: DO NOT MODIFY
	In  : Object FtpConnection
	In  : String RemotePath
	In  : String[] SiteCommand
	Out : String RemotePath
* Available Variables: DO NOT MODIFY *****/
        if (!(FtpConnection instanceof  com.ceva.utils.ftp.FtpConnection)) {
            throw new Exception("Invalid FTP connection instance.");
        }
        FtpConnection ftp = ( com.ceva.utils.ftp.FtpConnection) FtpConnection;
        if (SiteCommand != null) {
            for (int i = 0; i < SiteCommand.length; i++) {
                ftp.doSiteCommand(SiteCommand[i]);
            }
        }
        ftp.changeDir(getRemotePath());
        setRemotePath(ftp.getCurrentDir());
    }
}
