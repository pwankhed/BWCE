package com.ceva.utils.ftp;


public class ListRemoteFile{
/****** START SET/GET METHOD, DO NOT MODIFY *****/
	protected Object FtpConnection = null;
	protected String RemotePath = "";
	protected String RemoteMask = "";
	protected boolean IsRegExp = false;
	protected boolean IsFlag = false;
	protected String DataExtension = "";
	protected int MaxFiles = 0;
	protected long StableTimeMS = 0;
	protected boolean AllowZerosize = false;
	protected String FileMode = "";
	protected String TransferMode = "";
	protected long SystemOffsetMSec = 0;
	protected String[] SiteCommand = null;
	protected String[] RemoteFiles = null;
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
	public String getRemoteMask() {
		return RemoteMask;
	}
	public void setRemoteMask(String val) {
		RemoteMask = val;
	}
	public boolean getIsRegExp() {
		return IsRegExp;
	}
	public void setIsRegExp(boolean val) {
		IsRegExp = val;
	}
	public boolean getIsFlag() {
		return IsFlag;
	}
	public void setIsFlag(boolean val) {
		IsFlag = val;
	}
	public String getDataExtension() {
		return DataExtension;
	}
	public void setDataExtension(String val) {
		DataExtension = val;
	}
	public int getMaxFiles() {
		return MaxFiles;
	}
	public void setMaxFiles(int val) {
		MaxFiles = val;
	}
	public long getStableTimeMS() {
		return StableTimeMS;
	}
	public void setStableTimeMS(long val) {
		StableTimeMS = val;
	}
	public boolean getAllowZerosize() {
		return AllowZerosize;
	}
	public void setAllowZerosize(boolean val) {
		AllowZerosize = val;
	}
	public String getFileMode() {
		return FileMode;
	}
	public void setFileMode(String val) {
		FileMode = val;
	}
	public String getTransferMode() {
		return TransferMode;
	}
	public void setTransferMode(String val) {
		TransferMode = val;
	}
	public long getSystemOffsetMSec() {
		return SystemOffsetMSec;
	}
	public void setSystemOffsetMSec(long val) {
		SystemOffsetMSec = val;
	}
	public String[] getSiteCommand() {
		return SiteCommand;
	}
	public void setSiteCommand(String[] val) {
		SiteCommand = val;
	}
	public String[] getRemoteFiles() {
		return RemoteFiles;
	}
	public void setRemoteFiles(String[] val) {
		RemoteFiles = val;
	}
/****** END SET/GET METHOD, DO NOT MODIFY *****/
	public ListRemoteFile() {
	}
	public void invoke() throws Exception {
/* Available Variables: DO NOT MODIFY
	In  : Object FtpConnection
	In  : String RemotePath
	In  : String RemoteMask
	In  : boolean IsRegExp
	In  : boolean IsFlag
	In  : String DataExtension
	In  : int MaxFiles
	In  : long StableTimeMS
	In  : boolean AllowZerosize
	In  : String FileMode
	In  : String TransferMode
	In  : long SystemOffsetMSec
	In  : String[] SiteCommand
	Out : String[] RemoteFiles
* Available Variables: DO NOT MODIFY *****/
        if (!(FtpConnection instanceof com.ceva.utils.ftp.FtpConnection)) {
            throw new Exception("Invalid FTP connection instance.");
        }
        FtpConnection ftp = (com.ceva.utils.ftp.FtpConnection) FtpConnection;
        if (SiteCommand != null) {
            for (int i = 0; i < SiteCommand.length; i++) {
                ftp.doSiteCommand(SiteCommand[i]);
            }
        }
        long stableTime = 0;
        if (getStableTimeMS() > 0) {
            stableTime = getStableTimeMS();
        }
        FtpConnection.ListConfig config = new FtpConnection.ListConfig();
        config.dataExtension = getDataExtension();
        config.filemode = getFileMode();
        config.isFlag = getIsFlag();
        config.isRegExp = getIsRegExp();
        config.maxFiles = getMaxFiles();
        config.remoteMask = getRemoteMask();
        config.remotePath = getRemotePath();
        config.serverOffsetMSec = getSystemOffsetMSec();
        config.stableTimeMSec = stableTime;
        config.transfermode = getTransferMode();
        config.allowZeroSize = getAllowZerosize();

        String[] names = ftp.getRemoteFilelistExtendedArray(config);
        setRemoteFiles(names);
    }
}
