package com.ceva.utils.ftp;


import com.ceva.utils.ftp.*;
//import com.tibco.pe.core.Document;
//import com.tibco.pe.core.DocumentFactory;
import com.ceva.eai.bwce.generic.cfw.model.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RetrieveFile{

/****** START SET/GET METHOD, DO NOT MODIFY *****/
	protected Object FtpConnection = null;
	protected String RemotePath = "";
	protected String RemoteName = "";
	protected String RemoteFlagName = "";
	protected String Encoding = "";
	protected boolean IsBinary = false;
	protected boolean SkipBOM = false;
	protected String RemoteArchive = "";
	protected String LocalTmpPath = "";
	protected String LocalPath = "";
	protected String LocalName = "";
	protected String FileMode = "";
	protected String TransferMode = "";
	protected long DiskThresholdBytes = 0;
	protected boolean DelayArchiveOrDelete = false;
	protected String[] SiteCommandBefore = null;
	protected String[] SiteCommandAfter = null;
	protected String[] DocumentArgument = null;
	protected Object FtpPullConfig = null;
	protected Object Document = null;
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
	public String getRemoteName() {
		return RemoteName;
	}
	public void setRemoteName(String val) {
		RemoteName = val;
	}
	public String getRemoteFlagName() {
		return RemoteFlagName;
	}
	public void setRemoteFlagName(String val) {
		RemoteFlagName = val;
	}
	public String getEncoding() {
		return Encoding;
	}
	public void setEncoding(String val) {
		Encoding = val;
	}
	public boolean getIsBinary() {
		return IsBinary;
	}
	public void setIsBinary(boolean val) {
		IsBinary = val;
	}
	public boolean getSkipBOM() {
		return SkipBOM;
	}
	public void setSkipBOM(boolean val) {
		SkipBOM = val;
	}
	public String getRemoteArchive() {
		return RemoteArchive;
	}
	public void setRemoteArchive(String val) {
		RemoteArchive = val;
	}
	public String getLocalTmpPath() {
		return LocalTmpPath;
	}
	public void setLocalTmpPath(String val) {
		LocalTmpPath = val;
	}
	public String getLocalPath() {
		return LocalPath;
	}
	public void setLocalPath(String val) {
		LocalPath = val;
	}
	public String getLocalName() {
		return LocalName;
	}
	public void setLocalName(String val) {
		LocalName = val;
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
	public long getDiskThresholdBytes() {
		return DiskThresholdBytes;
	}
	public void setDiskThresholdBytes(long val) {
		DiskThresholdBytes = val;
	}
	public boolean getDelayArchiveOrDelete() {
		return DelayArchiveOrDelete;
	}
	public void setDelayArchiveOrDelete(boolean val) {
		DelayArchiveOrDelete = val;
	}
	public String[] getSiteCommandBefore() {
		return SiteCommandBefore;
	}
	public void setSiteCommandBefore(String[] val) {
		SiteCommandBefore = val;
	}
	public String[] getSiteCommandAfter() {
		return SiteCommandAfter;
	}
	public void setSiteCommandAfter(String[] val) {
		SiteCommandAfter = val;
	}
	public String[] getDocumentArgument() {
		return DocumentArgument;
	}
	public void setDocumentArgument(String[] val) {
		DocumentArgument = val;
	}
	public Object getFtpPullConfig() {
		return FtpPullConfig;
	}
	public void setFtpPullConfig(Object val) {
		FtpPullConfig = val;
	}
	public Object getDocument() {
		return Document;
	}
	public void setDocument(Object val) {
		Document = val;
	}
/****** END SET/GET METHOD, DO NOT MODIFY *****/
    public RetrieveFile() {
    	try {
			GetFile();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }

    public void GetFile() throws Exception {
/* Available Variables: DO NOT MODIFY
	In  : Object FtpConnection
	In  : String RemotePath
	In  : String RemoteName
	In  : String RemoteFlagName
	In  : String Encoding
	In  : boolean IsBinary
	In  : boolean SkipBOM
	In  : String RemoteArchive
	In  : String LocalTmpPath
	In  : String LocalPath
	In  : String LocalName
	In  : String FileMode
	In  : String TransferMode
	In  : long DiskThresholdBytes
	In  : boolean DelayArchiveOrDelete
	In  : String[] SiteCommandBefore
	In  : String[] SiteCommandAfter
	In  : String[] DocumentArgument
	Out : Object FtpPullConfig
	Out : Object Document
* Available Variables: DO NOT MODIFY *****/
        if (!(FtpConnection instanceof com.ceva.utils.ftp.FtpConnection)) {
            throw new Exception("Invalid FTP connection instance.");
        }
        FtpConnection ftp = (com.ceva.utils.ftp.FtpConnection) FtpConnection;
        if (SiteCommandBefore != null) {
            for (String cmd : SiteCommandBefore) {
                ftp.doSiteCommand(cmd);
            }
        }
        FtpConnection.PullConfig config = new FtpConnection.PullConfig();
        config.delayArchiveDelete = getDelayArchiveOrDelete();
        config.diskThresholdBytes = getDiskThresholdBytes();
        config.encoding = getEncoding();
        config.filemode = getFileMode();
        config.isBinary = getIsBinary();
        config.skipBOM = getSkipBOM();
        config.localName = getLocalName();
        config.localPath = getLocalPath();
        config.localTmpPath = getLocalTmpPath();
        config.remoteArchive = getRemoteArchive();
        config.remoteFlagName = getRemoteFlagName();
        config.remoteName = getRemoteName();
        config.remotePath = getRemotePath();
        config.transfermode = getTransferMode();

        FtpConnection.DocData ftpData = ftp.pullDynamicFromServer(config);
        
        List<String> docArgs = new ArrayList<>();
        docArgs.add("Doc__DocumentIsReference==" + Boolean.toString(ftpData.isReference)); 
        docArgs.add("Doc__DocumentIsBinary==" + Boolean.toString(ftpData.isBinary)); 
        docArgs.add("Doc__DocumentEncoding==" + ftpData.encoding);
        docArgs.add("Doc__OriginalFileName==" + ftpData.remoteName);
        docArgs.add("Doc__OriginalFileFolder==" + config.remotePath);

        if (ftpData.isBinary) {
            if (ftpData.isReference) {
                docArgs.add("Doc__DocumentIsBase64==false");
            } else {
                docArgs.add("Doc__DocumentIsBase64==true");
            }
        } else {
            docArgs.add("Doc__DocumentIsBase64==false");
        }
        if (ftpData.localName != null && ftpData.localName.length() > 0) {
            docArgs.add("Doc__DocumentName==" + ftpData.localName);
        } else {
            docArgs.add("Doc__DocumentName==" + ftpData.remoteName);
        }
        docArgs.addAll(Arrays.asList(getDocumentArgument()));

        Document doc = DocumentFactory.createDocument(ftpData.data, docArgs.toArray(new String[0]));
        setFtpPullConfig(config);
        setDocument(doc);
        if (SiteCommandAfter != null) {
            for (String cmd : SiteCommandAfter) {
                ftp.doSiteCommand(cmd);
            }
        }
    }
}
