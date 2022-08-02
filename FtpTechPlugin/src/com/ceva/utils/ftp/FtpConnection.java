package com.ceva.utils.ftp;


/**
 *
 * @author nlou
 */
/**
 *
 * @author nlou
 */
// import com.tibco.pe.core.Document;
import com.ceva.eai.bwce.generic.cfw.model.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Comparator;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;
import org.apache.commons.codec.binary.Base64InputStream;
import org.apache.commons.codec.binary.Base64OutputStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.BOMInputStream;
import org.apache.commons.io.input.ReaderInputStream;
import org.apache.commons.net.ftp.*;
import org.apache.commons.net.io.Util;

public class FtpConnection implements Serializable {

    private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(FtpConnection.class.getName());
    private String user = "";
    private String password = "";
    private String host = "";
    private int port = 21;
    private String transferMode = "PASSIVE";
    private String fileMode = "BINARY";
    private int sessTimeout = 30000;
    private int dataTimeout = 30000;
    private int sessKeepalive = 60;
    private int listPageSize = 100;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd'T'HHmmss");
    private FTPClient ftp = new FTPClient();
    private final FTPSClient ftps = new FTPSClient();
    private String homePath = "";

    public FtpConnection() {
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getConnectTimeout() {
        return sessTimeout;
    }

    public void setConnectTimeout(int timeout) {
        this.sessTimeout = timeout;
    }

    public int getDataTimeout() {
        return dataTimeout;
    }

    public void setDataTimeout(int timeout) {
        this.dataTimeout = timeout;
    }

    public void setSessionKeepalive(int sessKeepalive) {
        this.sessKeepalive = sessKeepalive;
    }

    public int getSessionKeepalive() {
        return sessKeepalive;
    }

    public String getDefaultTransferMode() {
        return transferMode;
    }

    public void setDefaultTransferMode(String transferMode) {
        this.transferMode = transferMode;
    }

    public String getDefaultFileMode() {
        return fileMode;
    }

    public void setDefaultFileMode(String fileMode) {
        this.fileMode = fileMode;
    }

    private String getTimeStamp() {
        return dateFormat.format(new Date());
    }

    public int getListPageSize() {
        return listPageSize;
    }

    public void setListPageSize(int listPageSize) {
        this.listPageSize = listPageSize;
    }

    private String addPathDelimiter(String path) {
        String newPath = path;
        if (newPath != null && newPath.length() > 0) {
            if (!newPath.endsWith("\\") && !newPath.endsWith("/")) {
                String separator;
                if (path.indexOf('/') >= 0) {
                    separator = "/";
                } else {
                    separator = "\\";
                }
                newPath = path + separator;
            }
        }
        return newPath;
    }

    private void checkConnected(String remoteDir, String fileMode, String transferMode) throws Exception {
        if (!isConnected()) {
            logger.debug("Client was disconnected, reconnecting...");
            ftp = new FTPClient();
            connect();
            if (remoteDir != null && remoteDir.length() > 0) {
                changeDir(remoteDir);
            }
            enableFileMode(fileMode);
            enableTransferMode(transferMode);
        } else {
            if (fileMode != null && fileMode.length() > 0 && !fileMode.equalsIgnoreCase(this.fileMode)) {
                enableFileMode(fileMode);
            }
            if (transferMode != null && transferMode.length() > 0 && !transferMode.equalsIgnoreCase(this.transferMode)) {
                enableTransferMode(transferMode);
            }
        }
    }

    public boolean isConnected() {
        boolean isConnected = false;
        logger.debug("Status according to client, connected:" + ftp.isConnected());
        try {
            if (ftp.isConnected()) {
                ftp.pwd();
                isConnected = true;
            }
        } catch (IOException ioe) {
            logger.debug("Error determine connection status:" + ioe);
            isConnected = false;
        }
        logger.debug("Real status, connected:" + isConnected);
        return isConnected;
    }

    public void changeDir(String dir) throws Exception {
        // check if we're already in wanted path
        String curDir = addPathDelimiter(getCurrentDir());
        if (curDir != null && curDir.equals(addPathDelimiter(dir))) {
            logger.debug("Already in " + curDir);
            return;
        }

        if (!ftp.changeWorkingDirectory(dir)) {
            int reply = ftp.getReplyCode();
            if (!FTPReply.isPositiveCompletion(reply)) {
                String info = "Unable to change directory to " + dir + " info: " + ftp.getReplyCode() + ":" + ftp.getReplyString();
                logger.error(info);
                throw new Exception(info);
            }
        } else {
            logger.info("Changed to remote path:" + ftp.printWorkingDirectory());
        }
    }

    private void createDir(String dir) throws Exception {
        if (!ftp.makeDirectory(dir)) {
            int reply = ftp.getReplyCode();
            if (!FTPReply.isPositiveCompletion(reply)) {
                String info = "Unable to create remote directory: " + dir + " info: " + ftp.getReplyCode() + ":" + ftp.getReplyString();
                logger.error(info);
                throw new Exception(info);
            }
        } else {
            logger.info("Create remote directory:" + dir);
        }
    }

    public String getCurrentDir() throws Exception {
        String dir = ftp.printWorkingDirectory();
        if (dir == null) {
            dir = "";
        }
        int reply = ftp.getReplyCode();
        if (!FTPReply.isPositiveCompletion(reply)) {
            String info = "Unable to get directory info: " + ftp.getReplyCode() + ":" + ftp.getReplyString();
            logger.error(info);
            throw new Exception(info);
        } else {
            dir = dir.replace('"', ' ').trim();
        }
        return dir;
    }

    public void resetHome() throws Exception {
        homePath = getCurrentDir();
        logger.info("Reset home directory to:" + homePath);
    }

    public boolean doSpecialSiteCommand(String cmd) throws Exception {
        boolean result = false;
        if (cmd != null) {
            switch (cmd.toLowerCase()) {
                case "resethome":
                    resetHome();
                    result = true;
                    break;
            }
        }
        return result;
    }

    public void doSiteCommand(String cmd) throws Exception {
        if (!doSpecialSiteCommand(cmd)) {
            if (!ftp.sendSiteCommand(cmd)) {
                int reply = ftp.getReplyCode();
                if (!FTPReply.isPositiveCompletion(reply)) {
                    String info = "Unable to execute site cmd: " + cmd + " info: " + ftp.getReplyCode() + ":" + ftp.getReplyString();
                    logger.error(info);
                    throw new Exception(info);
                }
            } else {
                logger.info("Executed site command:" + cmd);
            }
        }
    }

    private void enableTransferMode(String transferMode) throws Exception {
        // set the ftp mode
        String tm = transferMode;
        if (tm == null || tm.length() == 0) {
            tm = getDefaultTransferMode();
        }
        if ("ACTIVE".equalsIgnoreCase(tm)) {
            ftp.enterLocalActiveMode();
        } else {
            ftp.enterLocalPassiveMode();
        }
        logger.debug("Changed local transfermode to " + tm);
    }

    private void enableFileMode(String fileMode) throws Exception {
        String fm = fileMode;
        if (fm == null || fm.length() == 0) {
            fm = getDefaultFileMode();
        }
        if ("ASCII".equalsIgnoreCase(fm)) {
            setAsciiMode();
        } else {
            setBinaryMode();
        }
        logger.debug("Changed local filemode to " + fm);
    }

    private void setBinaryMode() throws Exception {
        if (!ftp.setFileType(FTP.BINARY_FILE_TYPE)) {
            int reply = ftp.getReplyCode();
            if (!FTPReply.isPositiveCompletion(reply)) {
                String info = "Type command failed: " + ftp.getReplyCode() + ":" + ftp.getReplyString();
                logger.error(info);
                throw new Exception(info);
            }
        } else {
            logger.debug("Changed to Binary mode");
        }
    }

    private void setAsciiMode() throws Exception {
        if (!ftp.setFileType(FTP.ASCII_FILE_TYPE)) {
            int reply = ftp.getReplyCode();
            if (!FTPReply.isPositiveCompletion(reply)) {
                String info = "Type command failed: " + ftp.getReplyCode() + ":" + ftp.getReplyString();
                logger.error(info);
                throw new Exception(info);
            }
        } else {
            logger.debug("Changed to Ascii mode");
        }
    }

    private FTPFile[] getRemoteFileList(final String remoteName, final boolean isRegexp, final long stableTimeMSec, final int maxFiles, final long serverOffsetMSec, final boolean allowZeroSize) throws Exception {
        String regexpmask = "^.*$ ";
        String remoteMask = remoteName;
        if (isRegexp) {
            remoteMask = "";
            regexpmask = remoteName;
        }
        final Pattern pmask = Pattern.compile(regexpmask);
        final long currentDT = System.currentTimeMillis();
        FTPListParseEngine pengine;
        if (remoteMask != null && remoteMask.trim().length() > 0) {
            pengine = ftp.initiateListParsing(remoteMask);
        } else {
            pengine = ftp.initiateListParsing();
        }
        int filecount = 0;
        ArrayList<FTPFile> ftpList = new ArrayList<>();
        while (pengine.hasNext()) {
            FTPFile[] files = pengine.getNext(listPageSize);
            if (files != null && files.length > 0) {
                logger.debug("Got first page of " + files.length + " files.");
                for (FTPFile ftpf : files) {
                    boolean isOK = false;
                    if (ftpf != null) {
                        if (maxFiles <= 0 || filecount < maxFiles) {
                            long filestamp = currentDT;
                            if (ftpf.getTimestamp() != null && ftpf.getTimestamp().getTimeInMillis() >= 0) {
                                filestamp = ftpf.getTimestamp().getTimeInMillis() + serverOffsetMSec;
                            }
                            long stableMSM = currentDT - filestamp;
                            long size = ftpf.getSize();
                            logger.debug("File " + ftpf.getName() + " size:" + size + " systemstamp: " + currentDT + ", filestamp:" + filestamp + ", stable for:" + stableMSM + " required:" + stableTimeMSec);
                            isOK = (allowZeroSize || size != 0) && ftpf.isFile() && (stableTimeMSec <= 0 || stableMSM >= stableTimeMSec);
                            if (!isOK) {
                                logger.debug("File " + ftpf.getName() + " not added because of size:" + ftpf.getSize() + " or not file:" + ftpf.isFile() + " or not stable:" + stableMSM + " (ts:" + filestamp + "[offset:" + serverOffsetMSec + "])");
                            }
                            if (isOK && isRegexp) {
                                isOK = pmask.matcher(ftpf.getName()).matches();
                                if (!isOK) {
                                    logger.debug("File " + ftpf.getName() + " not added because name does not match regexp " + remoteName);
                                }
                            }
                        } else {
                            logger.debug("File " + ftpf.getName() + " not added because maxfiles " + maxFiles + " are reached");
                        }
                        if (isOK) {
                            filecount++;
                            ftpList.add(ftpf);
                        }
                        if (maxFiles > 0 && filecount >= maxFiles) {
                            break;
                        }
                    }
                }
                if (maxFiles > 0 && filecount >= maxFiles) {
                    break;
                }
            }
            if (maxFiles > 0 && filecount >= maxFiles) {
                break;
            }
        }
        FTPFile[] ftpFiles = ftpList.toArray(new FTPFile[0]);
        int reply = ftp.getReplyCode();
        if (!FTPReply.isPositiveCompletion(reply)) {
            boolean noFiles = ftp.getReplyCode() == 550 || (ftp.getReplyString() != null && ftp.getReplyString().toLowerCase().contains("no files found"));
            if (!noFiles) {
                String info = "List command failed: " + ftp.getReplyCode() + ":" + ftp.getReplyString();
                logger.error(info);
                throw new Exception(info);
            }
        }

        Arrays.sort(ftpFiles, new Comparator<FTPFile>() {
            @Override
            public int compare(FTPFile f1, FTPFile f2) {
                int result;
                try {
                    long cst = System.currentTimeMillis();
                    long f1lm = cst;
                    if (f1.getTimestamp() != null) {
                        f1lm = f1.getTimestamp().getTimeInMillis();
                    }
                    long f2lm = cst;
                    if (f2.getTimestamp() != null) {
                        f2lm = f2.getTimestamp().getTimeInMillis();
                    }
                    result = Long.valueOf(f1lm).compareTo(f2lm);
                    if (result == 0) {
                        result = f1.getName().compareTo(f2.getName());
                    }
                } catch (Exception ex) {
                    result = 0;
                }
                return result;
            }
        });

        return ftpFiles;
    }

    private void deleteFile(String remoteName) throws Exception {
        if (!ftp.deleteFile(remoteName)) {
            int reply = ftp.getReplyCode();
            if (!FTPReply.isPositiveCompletion(reply)) {
                String info = "Unable to delete remote file " + remoteName + ": " + ftp.getReplyCode() + ":" + ftp.getReplyString();
                logger.error(info);
                throw new Exception(info);
            }
        } else {
            logger.debug("Deleted file: " + remoteName);
        }
    }

    private boolean fileExists(String remoteName) throws Exception {
        boolean exists = false;
        String[] ftpFiles = ftp.listNames(remoteName);
        int reply = ftp.getReplyCode();
        if (!FTPReply.isPositiveCompletion(reply)) {
            boolean cmdOK = (ftp.getReplyCode() == 550 || ftp.getReplyCode() == 450)
                    || (ftp.getReplyString() != null
                    && (ftp.getReplyString().toLowerCase().contains("no files found")
                    || ftp.getReplyString().toLowerCase().contains("no such file")));
            if (!cmdOK) {
                String info = "File exists command failed: " + ftp.getReplyCode() + ":" + ftp.getReplyString();
                logger.error(info);
                throw new Exception(info);
            } else {
                exists = false;
            }
        } else {
            if (ftpFiles != null && ftpFiles.length > 0) {
                exists = (ftpFiles[0].contains(remoteName));
                logger.debug(ftpFiles[0] + ":" + exists);
            }
        }
        return exists;
    }

    private boolean dirExists(String remoteName) throws Exception {
        boolean exists = false;
        FTPFile[] ftpFiles = ftp.listFiles(remoteName, FTPFileFilters.DIRECTORIES);
        int reply = ftp.getReplyCode();
        if (!FTPReply.isPositiveCompletion(reply)) {
            boolean cmdOK = (ftp.getReplyCode() == 550 || ftp.getReplyCode() == 450)
                    || (ftp.getReplyString() != null
                    && (ftp.getReplyString().toLowerCase().contains("no files found")
                    || ftp.getReplyString().toLowerCase().contains("no such file")));
            if (!cmdOK) {
                String info = "Dir exists command failed: " + ftp.getReplyCode() + ":" + ftp.getReplyString();
                logger.error(info);
                throw new Exception(info);
            }
        } else {
            if (ftpFiles != null && ftpFiles.length > 0 && ftpFiles[0].isDirectory()) {
                exists = (ftpFiles[0].getName().contains(remoteName));
                logger.debug(ftpFiles[0].getName() + ":" + exists);
            }
        }
        return exists;
    }

    private FTPFile fileStats(String remoteName, boolean silent) throws Exception {
        FTPFile file = null;// = ftp.mlistFile(remoteName);        
        FTPFile[] ftpFiles = ftp.listFiles(remoteName);
        int reply = ftp.getReplyCode();
        if (!FTPReply.isPositiveCompletion(reply)) {
            boolean cmdOK = (ftp.getReplyCode() == 550 || ftp.getReplyCode() == 450)
                    || (ftp.getReplyString() != null
                    && (ftp.getReplyString().toLowerCase().contains("no files found")
                    || ftp.getReplyString().toLowerCase().contains("no such file")));
            if (!cmdOK) {
                String info = "File stats command failed: " + ftp.getReplyCode() + ":" + ftp.getReplyString();
                logger.error(info);
                if (!silent) {
                    throw new Exception(info);
                }
            }
        } else {
            if (ftpFiles != null && ftpFiles.length > 0 && ftpFiles[0].isFile()) {
                file = ftpFiles[0];
            }
        }
        return file;
    }

    private long fileSize(String remoteName) throws Exception {
        long size = 0;
        FTPFile[] ftpFiles = ftp.listFiles(remoteName);
        int reply = ftp.getReplyCode();
        if (!FTPReply.isPositiveCompletion(reply)) {
            boolean cmdOK = (ftp.getReplyCode() == 550 || ftp.getReplyCode() == 450)
                    || (ftp.getReplyString() != null
                    && (ftp.getReplyString().toLowerCase().contains("no files found")
                    || ftp.getReplyString().toLowerCase().contains("no such file")));
            if (!cmdOK) {
                String info = "File size command failed: " + ftp.getReplyCode() + ":" + ftp.getReplyString();
                logger.error(info);
                throw new Exception(info);
            }
        } else {
            if (ftpFiles != null && ftpFiles.length > 0) {
                FTPFile file = ftpFiles[0];
                if (file.isFile()) {
                    size = file.getSize();
                    logger.debug("size:" + file.getName() + ":" + size);
                }
            }
        }
        return size;
    }

    private void renameFile(String remoteName, String newName) throws Exception {
        if (!ftp.rename(remoteName, newName)) {
            int reply = ftp.getReplyCode();
            if (!FTPReply.isPositiveCompletion(reply)) {
                String info = "Unable to rename remote file " + remoteName + "to:" + newName + ":" + ftp.getReplyCode() + ":" + ftp.getReplyString();
                logger.error(info);
                throw new Exception(info);
            }
        } else {
            logger.debug("Renamed file: " + remoteName + " to " + newName);
        }
    }

    private File checkPath(File file) {
        File parent = file.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }
        return file;
    }

    private String getDefaultEncoding() {
        String encoding = System.getProperty("file.encoding");
        if (encoding == null || encoding.length() == 0) {
            encoding = "UTF-8";
        }
        return encoding;
    }

    private boolean downloadStream(String remoteName, OutputStream outputStream) throws Exception {
        boolean success = false;
        try {
            success = ftp.retrieveFile(remoteName, outputStream);
            outputStream.flush();
        } finally {
            IOUtils.closeQuietly(outputStream);
        }
        if (!success) {
            int reply = ftp.getReplyCode();
            if (!FTPReply.isPositiveCompletion(reply)) {
                String info = "Unable to get remote file " + remoteName + ": " + ftp.getReplyCode() + ":" + ftp.getReplyString();
                logger.error(info);
                throw new Exception(info);
            }
        }
        return success;
    }

    private boolean downloadStreamSkipBOM(String remoteName, OutputStream outputStream) throws Exception {
        boolean success;
        InputStream inputStream = new BOMInputStream(ftp.retrieveFileStream(remoteName), false);
        try {
            try {
                Util.copyStream(inputStream, outputStream, 4096);
                outputStream.flush();
            } finally {
                IOUtils.closeQuietly(outputStream);
            }
        } finally {
            IOUtils.closeQuietly(inputStream);
        }
        success = ftp.completePendingCommand();
        if (!success) {
            String info = "Transfer of  remote file " + remoteName + " failed!";
            logger.error(info);
            throw new Exception(info);
        }
        return success;
    }

    private File downloadFile(String remoteName, String localFullName, String localTmpDir) throws Exception {
        File localFile = checkPath(new File(addPathDelimiter(localTmpDir) + remoteName));
        boolean success = false;
        OutputStream outputStream = new FileOutputStream(localFile);
        try {
            success = ftp.retrieveFile(remoteName, outputStream);
            outputStream.flush();
        } finally {
            IOUtils.closeQuietly(outputStream);
        }
        if (!success) {
            int reply = ftp.getReplyCode();
            if (!FTPReply.isPositiveCompletion(reply)) {
                String info = "Unable to get remote file " + remoteName + ": " + ftp.getReplyCode() + ":" + ftp.getReplyString();
                logger.error(info);
                throw new Exception(info);
            }
        } else {
            if (!localFile.exists()) {
                String info = "Download file " + remoteName + " failed. Local file does not exist.";
                logger.error(info);
                throw new Exception(info);
            }
            File destFile = checkPath(new File(localFullName));
            if (!destFile.equals(localFile)) {
                if (destFile.exists()) {
                    if (!destFile.delete()) {
                        String info = "Unable to delete existing destination file " + destFile.getAbsolutePath();
                        logger.error(info);
                        throw new Exception(info);
                    }
                }
                // move the file to the destination
                try {
                    FileUtils.moveFile(localFile, destFile);
                } catch (IOException e) {
                    String info = "Unable to move local temp file " + localFile.getAbsolutePath() + " to  " + destFile.getAbsolutePath();
                    logger.error(info);
                    throw new Exception(info);
                }
                // remove the temp file
                if (localFile.exists() && !localFile.delete()) {
                    String info = "Unable to delete temporary file " + localFile.getAbsolutePath();
                    logger.error(info);
                    throw new Exception(info);
                }
                localFile = destFile;
                logger.info("Downloaded " + remoteName + " to " + localFile.getAbsolutePath());
            }
        }
        return localFile;
    }

    private void uploadStream(InputStream inputStream, String streamName, String remoteName, boolean append) throws Exception {
        boolean success = false;
        try {
            if (append) {
                success = ftp.appendFile(remoteName, inputStream);
            } else {
                success = ftp.storeFile(remoteName, inputStream);
            }
        } finally {
            IOUtils.closeQuietly(inputStream);
        }
        //ftp.completePendingCommand();
        if (!success) {
            int reply = ftp.getReplyCode();
            if (!FTPReply.isPositiveCompletion(reply)) {
                String info = "Unable to put " + streamName + ": " + ftp.getReplyString();
                logger.error(info);
                throw new Exception(info);
            }
        } else {
            logger.info("Uploaded " + streamName + " to " + remoteName);
        }
    }

    private void uploadFile(String localName, String remoteName, boolean append) throws Exception {
        InputStream inputStream = new FileInputStream(localName);
        try {
            uploadStream(inputStream, localName, remoteName, append);
        } finally {
            IOUtils.closeQuietly(inputStream);
        }
    }

    private String createFlagName(String dataFile, String flagExtension) throws Exception {
        if (dataFile == null || dataFile.length() == 0) {
            throw new Exception("No data file specified as basis for flagfile");
        }

        String myFlagExtension = ".flg";
        if (flagExtension != null && flagExtension.length() > 0) {
            if (flagExtension.startsWith(".")) {
                myFlagExtension = flagExtension;
            } else {
                myFlagExtension = "." + flagExtension;
            }
        }

        String myFlagName;
        if (dataFile.indexOf('.') >= 0) {
            myFlagName = dataFile.substring(0, dataFile.lastIndexOf('.')) + myFlagExtension;
        } else {
            myFlagName = dataFile + myFlagExtension;
        }
        return myFlagName;
    }

    private void uploadFlagFile(String remoteName, String content) throws Exception {
        boolean success = false;
        String myContent = "OK";
        if (content != null && content.length() > 0) {
            myContent = content;
        }
        InputStream inputStream = new ByteArrayInputStream(myContent.getBytes());
        try {
            success = ftp.storeFile(remoteName, inputStream);
        } finally {
            IOUtils.closeQuietly(inputStream);
        }
        if (!success) {
            int reply = ftp.getReplyCode();
            if (!FTPReply.isPositiveCompletion(reply)) {
                String info = "Unable to put flag file " + remoteName + ": " + ftp.getReplyString();
                logger.error(info);
                throw new Exception(info);
            }
        } else {
            logger.info("Uploaded flagfile :" + remoteName);
        }
    }

    public void connect() throws Exception {
        if (isConnected()) {
            logger.info("Connected to :" + getHost() + ":" + getPort());
        } else {
            logger.debug("Trying to connect to " + getHost() + ":" + getPort() + " cTO:" + getConnectTimeout() + ",dTO:" + getDataTimeout());
            ftp.setDataTimeout(getDataTimeout());
            ftp.setDefaultTimeout(getConnectTimeout());
            if (getSessionKeepalive() > 0) {
                ftp.setControlKeepAliveTimeout(getSessionKeepalive());
            }
            ftp.connect(getHost(), getPort());
            int reply = ftp.getReplyCode();
            if (!FTPReply.isPositiveCompletion(reply)) {
                String info = "Connection failed: " + ftp.getReplyCode() + ":" + ftp.getReplyString();
                logger.error(info);
                throw new Exception(info);
            } else {
                if (!ftp.login(getUser(), getPassword())) {
                    reply = ftp.getReplyCode();
                    if (!FTPReply.isPositiveCompletion(reply)) {
                        String info = "Login failed: " + ftp.getReplyCode() + ":" + ftp.getReplyString();
                        logger.error(info);
                        throw new Exception(info);
                    }
                } else {
                    logger.info("Logged into remote server:" + ftp.getRemoteAddress() + ":" + ftp.getRemotePort());
                    ftp.setSoTimeout(getDataTimeout());
                }
            }
            homePath = getCurrentDir();
            logger.info("Current directory:" + homePath);
            enableFileMode(getDefaultFileMode());
            enableTransferMode(getDefaultTransferMode());
            logger.info("FileMode:" + getDefaultFileMode() + ", TransferMode:" + getDefaultTransferMode());
        }
    }

    public void disconnect() {
        disconnect(true);
    }

    public void disconnect(boolean logout) {
        if (ftp.isConnected()) {
            try {
                try {
                    if (logout) {
                        ftp.logout();
                    }
                } finally {
                    ftp.disconnect();
                }
                logger.debug("Disconnected from FTP server.");
            } catch (IOException ioe) {
                logger.debug("Unable to disconnect from FTP server." + ioe, ioe);
            }
        }
    }

    // PULL DATA ===================================
    static public class PullConfig implements Serializable {

        public String remotePath;
        public String remoteName;
        public String remoteFlagName;
        public String encoding;
        public boolean isBinary;
        public boolean delayArchiveDelete;
        public String remoteArchive;
        public String localTmpPath;
        public String localPath;
        public String localName;
        public String filemode;
        public String transfermode;
        public long diskThresholdBytes = 0L;
        public boolean skipBOM = true;

        @Override
        public int hashCode() {
            return Objects.hash(remotePath, remoteName, remoteFlagName, encoding, isBinary, delayArchiveDelete,
                    remoteArchive, localTmpPath, localPath, localName, filemode, transfermode, diskThresholdBytes, skipBOM);

        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final PullConfig other = (PullConfig) obj;
            if (!Objects.equals(this.remotePath, other.remotePath)) {
                return false;
            }
            if (!Objects.equals(this.remoteName, other.remoteName)) {
                return false;
            }
            if (!Objects.equals(this.remoteFlagName, other.remoteFlagName)) {
                return false;
            }
            if (!Objects.equals(this.encoding, other.encoding)) {
                return false;
            }
            if (this.isBinary != other.isBinary) {
                return false;
            }
            if (this.delayArchiveDelete != other.delayArchiveDelete) {
                return false;
            }
            if (!Objects.equals(this.remoteArchive, other.remoteArchive)) {
                return false;
            }
            if (!Objects.equals(this.localTmpPath, other.localTmpPath)) {
                return false;
            }
            if (!Objects.equals(this.localPath, other.localPath)) {
                return false;
            }
            if (!Objects.equals(this.localName, other.localName)) {
                return false;
            }
            if (!Objects.equals(this.filemode, other.filemode)) {
                return false;
            }
            if (!Objects.equals(this.transfermode, other.transfermode)) {
                return false;
            }
            if (this.diskThresholdBytes != other.diskThresholdBytes) {
                return false;
            }
            return this.skipBOM == other.skipBOM;
        }
    }

    static public class DocData implements Serializable {

        public String data;
        public String encoding;
        public boolean isReference;
        public boolean isBinary;
        public String remoteName;
        public String localName;

        @Override
        public int hashCode() {
            return Objects.hash(data, encoding, isReference, isBinary, remoteName, localName);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final DocData other = (DocData) obj;
            if (!Objects.equals(this.data, other.data)) {
                return false;
            }
            if (!Objects.equals(this.encoding, other.encoding)) {
                return false;
            }
            if (this.isReference != other.isReference) {
                return false;
            }
            if (this.isBinary != other.isBinary) {
                return false;
            }
            if (!Objects.equals(this.remoteName, other.remoteName)) {
                return false;
            }
            return Objects.equals(this.localName, other.localName);
        }
    }

    private boolean pullStreamFromServer(final PullConfig config, final OutputStream outputStream) throws Exception {
        checkConnected(config.remotePath, config.filemode, config.transfermode);
        try {
            // get the remote file
            logger.info("Getting file:" + config.remoteName);
            checkConnected(config.remotePath, config.filemode, config.transfermode);
            if (config.skipBOM) {
                logger.debug("Download skipping BOM");
                downloadStreamSkipBOM(config.remoteName, outputStream);
            } else {
                downloadStream(config.remoteName, outputStream);
            }
            // delete or archive
            if (!config.delayArchiveDelete) {
                archiveOrDeleteFileFromServer(config);
            }
        } catch (Exception e) {
            throw e;
        }
        return true;
    }

    private String pullDataFromServer(final PullConfig config) throws Exception {
        String data = "";
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        OutputStream out = baos;
        try {
            if (config.isBinary) {
                logger.debug("Setting up base64 stream since file is marked binary");
                out = new Base64OutputStream(baos, true, 0, new byte[0]);
            }
            pullStreamFromServer(config, out);
            if (config.encoding != null && config.encoding.length() > 0 && !getDefaultEncoding().equalsIgnoreCase(config.encoding)) {
                data = baos.toString(config.encoding);
            } else {
                data = baos.toString();
            }
        } finally {
            IOUtils.closeQuietly(out);
        }
        return data;
    }

    public void archiveOrDeleteFileFromServer(final PullConfig config) throws Exception {
        // delete or archive
        if (config.remoteArchive != null && config.remoteArchive.length() > 0) {
            String archiveName = addPathDelimiter(config.remoteArchive) + config.remoteName + "." + getTimeStamp();
            try {
                checkConnected(config.remotePath, config.filemode, config.transfermode);
                renameFile(config.remoteName, archiveName);
                logger.info("Archived " + config.remoteName + " to " + archiveName);
            } catch (Exception e) {
                checkConnected(config.remotePath, config.filemode, config.transfermode);
                deleteFile(config.remoteName);
                logger.info("Deleted " + config.remoteName);
            }
        } else {
            checkConnected(config.remotePath, config.filemode, config.transfermode);
            deleteFile(config.remoteName);
            logger.info("Deleted " + config.remoteName);
        }
        // if applicable delete flagfile
        if (config.remoteFlagName != null && config.remoteFlagName.length() > 0) {
            checkConnected(config.remotePath, config.filemode, config.transfermode);
            deleteFile(config.remoteFlagName);
        }
    }

    private File pullFileFromServer(final PullConfig config) throws Exception {
        checkConnected(config.remotePath, config.filemode, config.transfermode);

        String myLocalName = config.localName;
        if (myLocalName == null || myLocalName.length() == 0) {
            myLocalName = config.remoteName;
        }
        // check if dest file already exists. If so delete
        File destFile = new File(config.localPath + '/' + myLocalName);
        if (destFile.exists()) {
            if (!destFile.delete()) {
                throw new Exception("Can not delete existing local file:" + destFile.getAbsolutePath());
            }
        }
        if (destFile.getParentFile() != null && !destFile.getParentFile().exists()) {
            destFile.getParentFile().mkdirs();
        }
        // check if tmp file already exists. If so delete
        File tmpFile = new File(config.localTmpPath + '/' + myLocalName);
        if (tmpFile.exists()) {
            if (!tmpFile.delete()) {
                throw new Exception("Can not delete existing local file:" + tmpFile.getAbsolutePath());
            }
        }
        if (tmpFile.getParentFile() != null && !tmpFile.getParentFile().exists()) {
            tmpFile.getParentFile().mkdirs();
        }
        try {
            // get the remote file
            logger.info("Getting file:" + config.remoteName + " to " + myLocalName + " local dir: " + config.localPath + " via:" + config.localTmpPath);
            checkConnected(config.remotePath, config.filemode, config.transfermode);
            downloadFile(config.remoteName, destFile.getAbsolutePath(), config.localTmpPath);
            if (!config.delayArchiveDelete) {
                archiveOrDeleteFileFromServer(config);
            }
        } catch (Exception e) {
            destFile.delete();
            tmpFile.delete();
            throw e;
        }
        return destFile;
    }

    public DocData pullDynamicFromServer(final PullConfig config) throws Exception {
        // 0: data
        // 1: encoding
        // 2: isReference
        // 3: isBinary
        // 4 : remoteName
        // 5 : localName
        DocData result = new DocData();
        checkConnected(config.remotePath, fileMode, transferMode);
        long size = fileSize(config.remoteName);
        logger.debug("Filesize for remote file:" + size);
        if (size >= config.diskThresholdBytes) {
            logger.debug("FileSize large then " + config.diskThresholdBytes + " so pulling as reference");
            File dstFile = pullFileFromServer(config);
            if (dstFile != null) {
                result.data = dstFile.getAbsolutePath();
                result.encoding = (config.encoding != null) ? config.encoding : "UTF-8";
                result.isReference = true;
                result.isBinary = config.isBinary;
                result.remoteName = config.remoteName;
                result.localName = config.localName;
            }
        } else {
            logger.debug("FileSize smaller then " + config.diskThresholdBytes + " so pulling as data");
            result.data = pullDataFromServer(config);
            result.encoding = (config.encoding != null) ? config.encoding : "UTF-8";
            result.isReference = false;
            result.isBinary = config.isBinary;
            result.remoteName = config.remoteName;
            result.localName = config.localName;
        }
        return result;
    }

    // LIST FUNCTIONS ===================================    
    static public class ListConfig implements Serializable {

        public String remotePath;
        public String remoteMask;
        public boolean isRegExp = false;
        public boolean isFlag = false;
        public String dataExtension = "";
        public int maxFiles;
        public long stableTimeMSec = -1;
        public String filemode;
        public String transfermode;
        public long serverOffsetMSec = 0L;
        public boolean allowZeroSize = false;
    }

    public String[] getRemoteFilelistExtendedArray(final ListConfig config) throws Exception {
        List<String> files = getRemoteFilelistExtended(config);
        return files.toArray(new String[0]);
    }

    public List<String> getRemoteFilelistExtended(final ListConfig config) throws Exception {
        List<String> files = new ArrayList<>();
        try {
            checkConnected(config.remotePath, config.filemode, config.transfermode);

            if (config.filemode != null && config.filemode.length() > 0) {
                this.enableFileMode(config.filemode);
            }
            if (config.transfermode != null && config.transfermode.length() > 0) {
                this.enableTransferMode(config.transfermode);
            }

            if (config.remotePath != null && config.remotePath.length() > 0) {
                changeDir(config.remotePath);
            }

            String myDataExtension = "";
            if (config.dataExtension != null) {
                myDataExtension = config.dataExtension;
            }
            String myFlagExtension = ".tmp";
            if (config.isFlag) {
                int i = config.remoteMask.lastIndexOf(".");
                if (i >= 0) {
                    myFlagExtension = config.remoteMask.substring(i).replaceAll("\\*|\\?", "");
                }
                if (myDataExtension.length() > 0 && !myDataExtension.startsWith(".")) {
                    myDataExtension = "." + myDataExtension;
                }
            }

            FTPFile[] list = this.getRemoteFileList(config.remoteMask, config.isRegExp, config.stableTimeMSec, config.maxFiles, config.serverOffsetMSec, config.allowZeroSize);
            if (list != null) {
                int maxNumber = list.length;
                if (config.maxFiles > 0 && maxNumber > config.maxFiles) {
                    maxNumber = config.maxFiles;
                }
                for (int i = 0; i < maxNumber; i++) {
                    try {
                        checkConnected(config.remotePath, config.filemode, config.transfermode);
                        FTPFile lsEntry = list[i];
                        if (lsEntry.getType() != FTPFile.DIRECTORY_TYPE) {
                            String myRemoteName = lsEntry.getName();
                            String myFlagName = "";
                            long mTime = System.currentTimeMillis();
                            if (lsEntry.getTimestamp() != null && lsEntry.getTimestamp().getTimeInMillis() >= 0) {
                                mTime = lsEntry.getTimestamp().getTimeInMillis() + config.serverOffsetMSec;
                            }
                            long size = lsEntry.getSize();
                            // check when using flags / trigger files
                            if (config.isFlag) {
                                myFlagName = lsEntry.getName();
                                myRemoteName = myRemoteName.replaceAll(myFlagExtension, myDataExtension);
                                // check if data file exists
                                checkConnected(config.remotePath, config.filemode, config.transfermode);
                                FTPFile myDataFile = fileStats(myRemoteName, true);
                                if (myDataFile == null) {
                                    logger.warn("Data file:" + myRemoteName + " which belongs to flagfile:" + myFlagName + " does not exist.");
                                    // delete flag file
                                    checkConnected(config.remotePath, config.filemode, config.transfermode);
                                    deleteFile(myFlagName);
                                    // continue with next file
                                    continue;
                                }
                                mTime = System.currentTimeMillis();
                                if (myDataFile.getTimestamp() != null) {
                                    mTime = myDataFile.getTimestamp().getTimeInMillis() + config.serverOffsetMSec;
                                }
                                size = myDataFile.getSize();
                            }

                            StringBuilder sb = new StringBuilder();
                            sb.append(myRemoteName).append("||")
                                    .append(mTime).append("||")
                                    .append(size).append("||");
                            if (config.isFlag) {
                                sb.append(myFlagName);
                            }
                            files.add(sb.toString());
                        }
                    } catch (Exception e) {
                        if (e instanceof IOException) {
                            // is the exception fatal ?
                            if ((e instanceof FTPConnectionClosedException) && !isConnected()) {
                                throw e;
                            } else {
                                logger.warn("IO Warning during file retrieval:" + e.getMessage(), e);
                            }
                        } else {
                            logger.warn("Warning during file retrieval:" + e.getMessage(), e);
                        }
                    }
                }
            }
        } catch (Exception e) {
            if (files.isEmpty()) {
                throw e;
            } else {
                logger.error("Exception was thrown retrieving from " + getHost() + ":" + getPort() + ":" + e, e);
            }
        }
        return files;
    }

    // PUSH DATA ===================================
    static public class PushConfig implements Serializable {

        public String remoteTmpPath;
        public String remotePath;
        public String remoteName;
        public String remoteTmpName;
        public String remoteEncoding;
        public boolean remoteAppend;
        public String filemode;
        public String transfermode;
        public boolean rename;
        public boolean useFlag;
        public String flagExtension;
        public String flagContent;
        public int dataTimeoutMS;
        public boolean forceBinary;
    }

    private void pushStreamToServer(final InputStream stream, final PushConfig config, final String streamName) throws Exception {
        try {
            logger.debug("pushStreamToServer:" + "Name:" + streamName
                    + ",remoteTmpPath:" + config.remoteTmpPath + ",remotePath:" + config.remotePath + ",remoteName:" + config.remoteName
                    + ",filemode:" + config.filemode + ",transfermode:" + config.transfermode + ",rename:" + config.rename
                    + ",useFlag:" + config.useFlag + ",flagExtension:" + config.flagExtension + ",flagContent:" + config.flagContent);

            checkConnected(config.remotePath, config.filemode, config.transfermode);

            if (config.filemode != null && config.filemode.length() > 0) {
                this.enableFileMode(config.filemode);
            }
            if (config.transfermode != null && config.transfermode.length() > 0) {
                this.enableTransferMode(config.transfermode);
            }

            if (config.dataTimeoutMS > 0) {
                setDataTimeout(dataTimeout);
                ftp.setDataTimeout(dataTimeout);
                ftp.setSoTimeout(dataTimeout);
            }

            if (config.remotePath != null && config.remotePath.length() > 0) {
                changeDir(config.remotePath);
            }
            try {
                String rName = config.remoteName;
                if (rName == null || rName.length() <= 0) {
                    rName = streamName;
                }
                String rTmpName = config.remoteTmpName;
                if (rTmpName == null || rTmpName.length() <= 0) {
                    rTmpName = rName;
                }

                logger.debug("Checking if " + rName + " exists.");
                checkConnected(config.remotePath, config.filemode, config.transfermode);
                if (fileExists(rName)) {
                    logger.debug(rName + " exists, trying to delete.");
                    checkConnected(config.remotePath, config.filemode, config.transfermode);
                    deleteFile(rName);
                    logger.debug(rName + " deleted.");
                }

                if (config.remoteTmpPath != null && config.remoteTmpPath.length() > 0 && !config.remoteTmpPath.equals(config.remotePath)) {
                    checkConnected(config.remotePath, config.filemode, config.transfermode);
                    changeDir(config.remoteTmpPath);
                    logger.debug("Changed to remote tmp dir:" + config.remoteTmpPath);
                    if (!config.remoteAppend) {
                        logger.debug("Checking if " + rTmpName + " exists.");
                        checkConnected(config.remoteTmpPath, config.filemode, config.transfermode);
                        if (fileExists(rTmpName)) {
                            logger.debug(rTmpName + " exists, trying to delete.");
                            checkConnected(config.remoteTmpPath, config.filemode, config.transfermode);
                            deleteFile(rTmpName);
                            logger.debug(rTmpName + " deleted.");
                        }
                    }
                    logger.debug("Putting:" + streamName + " to " + config.remoteTmpPath + "/" + rTmpName);
                    checkConnected(config.remoteTmpPath, config.filemode, config.transfermode);
                    uploadStream(stream, streamName, rTmpName, config.remoteAppend);
                    if (!config.remoteAppend) {
                        logger.info("Put:" + streamName + " to " + config.remoteTmpPath + "/" + rTmpName);
                        logger.debug("Renaming:" + rTmpName + " to " + config.remotePath + '/' + rName);
                        checkConnected(config.remoteTmpPath, config.filemode, config.transfermode);
                        renameFile(rTmpName, config.remotePath + '/' + rName);
                        logger.info("Renamed:" + rTmpName + " to " + config.remotePath + '/' + rName);
                    } else {
                        logger.info("Put in append mode (no rename will happen):" + streamName + " to " + config.remoteTmpPath + "/" + rTmpName);
                    }
                    checkConnected(config.remoteTmpPath, config.filemode, config.transfermode);
                    // return to home
                    changeDir(homePath);
                } else {
                    checkConnected(config.remotePath, config.filemode, config.transfermode);
                    changeDir(config.remotePath);
                    logger.debug("Changed dir:" + config.remotePath);
                    if (config.rename && !config.remoteAppend) {
                        if (rTmpName.equals(rName)) {
                            rTmpName = "~" + rName + ".tmp";
                        }
                        logger.debug("Checking if " + rTmpName + " exists.");
                        checkConnected(config.remotePath, config.filemode, config.transfermode);
                        if (fileExists(rTmpName)) {
                            logger.debug(rTmpName + " exists, trying to delete.");
                            checkConnected(config.remotePath, config.filemode, config.transfermode);
                            deleteFile(rTmpName);
                            logger.debug(rTmpName + " deleted.");
                        }
                        logger.debug("Putting:" + streamName + " to " + config.remotePath + "/" + rTmpName);
                        checkConnected(config.remotePath, config.filemode, config.transfermode);
                        uploadStream(stream, streamName, rTmpName, config.remoteAppend);
                        logger.info("Put file:" + rTmpName + " to " + config.remotePath);
                        logger.debug("Renaming:" + rTmpName + " to " + rName);
                        checkConnected(config.remotePath, config.filemode, config.transfermode);
                        renameFile(rTmpName, rName);
                        logger.info("Renamed:" + rTmpName + " to " + rName);
                    } else {
                        logger.debug("Putting:" + streamName + " to " + config.remotePath + "/" + rName);
                        checkConnected(config.remotePath, config.filemode, config.transfermode);
                        uploadStream(stream, streamName, rName, config.remoteAppend);
                        logger.info("Put:" + streamName + " to " + config.remotePath + "/" + rName);
                    }
                }
                if (config.useFlag) {
                    checkConnected(config.remotePath, config.filemode, config.transfermode);
                    changeDir(config.remotePath);
                    String flagName = createFlagName(rName, config.flagExtension);
                    // check if flag exists
                    checkConnected(config.remotePath, config.filemode, config.transfermode);
                    if (fileExists(flagName)) {
                        checkConnected(config.remotePath, config.filemode, config.transfermode);
                        deleteFile(flagName);
                    }
                    checkConnected(config.remotePath, config.filemode, config.transfermode);
                    uploadFlagFile(flagName, config.flagContent);
                }
                // return home
                checkConnected(homePath, config.filemode, config.transfermode);
                changeDir(homePath);

            } catch (Exception e) {
                String error;
                if (e instanceof IOException) {
                    IOException se = (IOException) e;
                    error = "IOException occured during send:" + se.getMessage();
                    logger.error(error, se);
                } else {
                    error = "Exception occured during send:" + e.getClass().getName() + " : " + e.getMessage();
                    logger.error("Exception occured during send:" + e.getClass().getName() + " : " + e.getMessage(), e);
                }
                throw new Exception(error, e);
            }
        } finally {
            IOUtils.closeQuietly(stream);
        }
    }

    public void pushDocumentToServer(final Document doc, final PushConfig config) throws Exception {
        if (doc == null) {
            throw new Exception("No document was assigned for transfer!");
        }
        if (config == null) {
            throw new Exception("No push config was assigned for transfer!");
        }
        InputStream dis;
        if (!config.forceBinary &&  config.remoteEncoding != null && config.remoteEncoding.length() > 0 && !config.remoteEncoding.equalsIgnoreCase(doc.getDocumentEncoding())) {
            dis = new ReaderInputStream(doc.retrieveContentReader(), config.remoteEncoding);
        } else {
            dis = doc.retrieveContentStream();
        }
        try {
            pushStreamToServer(dis, config, doc.getDocumentName());
        } finally {
            IOUtils.closeQuietly(dis);
        }
    }

    public void pushDynamicToServer(final DocData docdata, final PushConfig config) throws Exception {
        if (docdata == null) {
            throw new Exception("No document data was assigned for transfer!");
        }
        if (config == null) {
            throw new Exception("No push config was assigned for transfer!");
        }
        if (docdata.isReference) {
            File localFile = new File(docdata.data);
            if (!localFile.exists()) {
                throw new Exception("Localfile :" + localFile.getCanonicalPath() + " does not exist.");
            }
            InputStream is;
            if (!config.forceBinary && config.remoteEncoding != null && config.remoteEncoding.length() > 0 && !config.remoteEncoding.equalsIgnoreCase(docdata.encoding)) {
                is = new ReaderInputStream(new InputStreamReader(new FileInputStream(localFile), docdata.encoding), config.remoteEncoding);
            } else {
                is = new FileInputStream(localFile);
            }
            try {
                pushStreamToServer(is, config, docdata.remoteName);
            } finally {
                IOUtils.closeQuietly(is);
            }
        } else {
            InputStream is;
            if (docdata.isBinary) {
                is = new Base64InputStream(new ByteArrayInputStream(docdata.data.getBytes(docdata.encoding)), false);
            } else {
                is = new ByteArrayInputStream(docdata.data.getBytes(docdata.encoding));
            }
            if (!config.forceBinary && config.remoteEncoding != null && config.remoteEncoding.length() > 0 && !config.remoteEncoding.equalsIgnoreCase(docdata.encoding)) {
                is = new ReaderInputStream(new InputStreamReader(is, docdata.encoding), config.remoteEncoding);
            }
            try {
                pushStreamToServer(is, config, docdata.remoteName);
            } finally {
                IOUtils.closeQuietly(is);
            }
        }
    }
}
