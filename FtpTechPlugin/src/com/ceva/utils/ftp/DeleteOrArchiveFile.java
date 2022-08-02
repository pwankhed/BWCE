package com.ceva.utils.ftp;


public class DeleteOrArchiveFile{
/****** START SET/GET METHOD, DO NOT MODIFY *****/
	protected Object FtpConnection = null;
	protected Object FtpPullConfig = null;
	protected String[] SiteCommandAfter = null;
	public Object getFtpConnection() {
		return FtpConnection;
	}
	public void setFtpConnection(Object val) {
		FtpConnection = val;
	}
	public Object getFtpPullConfig() {
		return FtpPullConfig;
	}
	public void setFtpPullConfig(Object val) {
		FtpPullConfig = val;
	}
	public String[] getSiteCommandAfter() {
		return SiteCommandAfter;
	}
	public void setSiteCommandAfter(String[] val) {
		SiteCommandAfter = val;
	}
/****** END SET/GET METHOD, DO NOT MODIFY *****/
	public DeleteOrArchiveFile() {
	}
	public void invoke() throws Exception {
/* Available Variables: DO NOT MODIFY
	In  : Object FtpConnection
	In  : Object FtpPullConfig
	In  : String[] SiteCommandAfter
* Available Variables: DO NOT MODIFY *****/
        if (!(getFtpConnection() instanceof com.ceva.utils.ftp.FtpConnection)) {
            throw new Exception("Invalid FTP connection instance.");
        }
        if (!(getFtpPullConfig() instanceof FtpConnection.PullConfig)) {
            throw new Exception("Invalid FTP Pull Config instance.");
        }

        FtpConnection ftp = (com.ceva.utils.ftp.FtpConnection) getFtpConnection();
        FtpConnection.PullConfig config = (FtpConnection.PullConfig) getFtpPullConfig();
        if (config.delayArchiveDelete) {
            ftp.archiveOrDeleteFileFromServer(config);
        }
        if (SiteCommandAfter != null) {
            for (String command : SiteCommandAfter) {
                ftp.doSiteCommand(command);
            }
        }
    }
}
