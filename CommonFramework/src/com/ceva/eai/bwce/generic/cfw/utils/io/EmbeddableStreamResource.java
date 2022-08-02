package com.ceva.eai.bwce.generic.cfw.utils.io;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.apache.commons.codec.binary.Base64InputStream;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ceva.eai.bwce.generic.cfw.utils.encoding.FileEncoding;

public class EmbeddableStreamResource implements Serializable {

	private Logger logger = LoggerFactory.getLogger(getClass().getName());
    private final static long serialVersionUID = 1L;
    public static final String COMPRESSION_TYPE_NONE = "NONE";
    public static final String COMPRESSION_TYPE_ZIP = "ZIP";
    public static final String COMPRESSION_TYPE_GZIP = "GZIP";
    public static final String BINARY_CHECK_METHOD_LOOSE = "LOOSE";
    public static final String BINARY_CHECK_METHOD_STRICT = "STRICT";
    public static final String BINARY_CHECK_METHOD_MIME = "MIME";
    public static final String BINARY_CHECK_METHOD_SKIP = "SKIP";
    private boolean base64 = false;
    private boolean compressed = false;
    private String compressionType = "";
    private boolean binary = false;
    private String mimeType = "";
    private File file;
    private byte[] dataArray;
    private long sizeBytes = -1;
    private String binaryCheckMethod = "LOOSE";
    private int binaryCheckLooseMaxNonText = 10;
    private long binaryCheckLooseMaxBytes = 100000L;
    private String binaryOverride;
    private String base64Override;
    private String compressedOverride;
    private String compressionTypeOverride;

    public String getCompressionType() {
        return compressionType;
    }

    public String getMimeType() {
        return mimeType;
    }

    public boolean isBase64() {
        return base64;
    }

    public boolean isBinary() {
        return binary;
    }

    public boolean isCompressed() {
        return compressed;
    }

    public long getSizeBytes() {
        return sizeBytes;
    }

    public String getBase64Override() {
        return base64Override;
    }

    public String getBinaryOverride() {
        return binaryOverride;
    }

    public String getCompressedOverride() {
        return compressedOverride;
    }

    public String getCompressionTypeOverride() {
        return compressionTypeOverride;
    }

    static public class EmbeddableStreamResourceBuilder {

        private final EmbeddableStreamResource esr;

        public static EmbeddableStreamResourceBuilder getBuilder() {
            return new EmbeddableStreamResourceBuilder();
        }

        public EmbeddableStreamResourceBuilder() {
            this.esr = new EmbeddableStreamResource();
        }

        public EmbeddableStreamResourceBuilder setBinaryCheckLooseMaxBytes(long binaryCheckLooseMaxBytes) {
            esr.binaryCheckLooseMaxBytes = binaryCheckLooseMaxBytes;
            return this;
        }

        public EmbeddableStreamResourceBuilder setBinaryCheckLooseMaxNonText(int binaryCheckLooseMaxNonText) {
            esr.binaryCheckLooseMaxNonText = binaryCheckLooseMaxNonText;
            return this;
        }

        public EmbeddableStreamResourceBuilder setBinaryCheckMethod(String binaryCheckMethod) {
            esr.binaryCheckMethod = binaryCheckMethod;
            return this;
        }

        public EmbeddableStreamResourceBuilder setBinaryOverride(String isBinary) {
            esr.binaryOverride = isBinary;
            return this;
        }

        public EmbeddableStreamResourceBuilder setBase64Override(String isBase64) {
            esr.base64Override = isBase64;
            return this;
        }

        public EmbeddableStreamResourceBuilder setCompressedOverride(String isCompressed) {
            esr.compressedOverride = isCompressed;
            return this;
        }

        public EmbeddableStreamResourceBuilder setCompressionTypeOverride(String compressionType) {
            esr.compressionTypeOverride = compressionType;
            return this;
        }

        public EmbeddableStreamResourceBuilder setDataArray(byte[] dataArray) {
            esr.dataArray = dataArray;
            return this;
        }

        public EmbeddableStreamResourceBuilder setString(String str, String charset) {
            try {
                esr.dataArray = (charset != null && charset.length() > 0) ? str.getBytes(charset) : str.getBytes();
            } catch (UnsupportedEncodingException ee) {
                throw new RuntimeException(ee);
            }
            return this;
        }

        public EmbeddableStreamResourceBuilder setFile(File file) {
            esr.file = file;
            return this;
        }

        public EmbeddableStreamResource build() {
            return esr;
        }

    }

    private void checkStreamDocumentBase64(final InputStream instream, boolean close) throws Exception {
        BufferedInputStream bis = new BufferedInputStream(instream);
        try {
            base64 = com.ceva.eai.bwce.generic.cfw.utils.encoding.Base64.isBase64(bis);
        } finally {
            if (close) {
                IOUtils.closeQuietly(bis);
            }
        }
    }
    private final static int ZIP_MAGIC = 0x4b50;

    private String checkCompressionType(BufferedInputStream instream, boolean close) {
        String ctype = COMPRESSION_TYPE_NONE;
        try {
            int magic = 0;
            try {
                instream.mark(2);
                magic = instream.read() & 0xff | ((instream.read() << 8) & 0xff00);
                instream.reset();
            } catch (IOException e) {
                logger.error("Error in checkCompressionType " + e, e);
            }
            if (magic == GZIPInputStream.GZIP_MAGIC) {
                ctype = COMPRESSION_TYPE_GZIP;
            } else if (magic == ZIP_MAGIC) {
                ctype = COMPRESSION_TYPE_ZIP;
            }
        } finally {
            if (close) {
                IOUtils.closeQuietly(instream);
            }
        }
        return ctype;
    }

    private void checkStreamDocumentCompressed(InputStream instream, boolean close) throws Exception {
        BufferedInputStream bis;
        if (base64) {
            bis = new BufferedInputStream(new Base64InputStream(instream));
        } else {
            bis = new BufferedInputStream(instream);
        }
        try {
            compressionType = checkCompressionType(bis, close);
            compressed = !COMPRESSION_TYPE_NONE.equals(compressionType);
        } finally {
            if (close) {
                IOUtils.closeQuietly(bis);
            }
        }
    }

    private long getGzipDecompressedSizeBytes(File file) throws Exception {
        int val = -1;
        RandomAccessFile raf = new RandomAccessFile(file, "r");
        try {
            raf.seek(raf.length() - 4);
            int b4 = raf.read();
            int b3 = raf.read();
            int b2 = raf.read();
            int b1 = raf.read();
            val = (b1 << 24) | (b2 << 16) + (b3 << 8) + b4;
        } finally {
            IOUtils.closeQuietly(raf);
        }
        return val;
    }

    private long getStreamSizeBytes(InputStream instream, boolean close) throws Exception {
        long size = 0;
        try {
            while (instream.available() > 0) {
                byte[] buf = new byte[1024];
                int read = instream.read(buf);
                if (read > 0) {
                    size += read;
                }
            }
        } finally {
            if (close) {
                IOUtils.closeQuietly(instream);
            }
        }
        return size;
    }

    private boolean streamIsBinary(InputStream instream) throws IOException {
        boolean isBinary;
        String checkMethod = binaryCheckMethod;
        if (checkMethod == null || checkMethod.isEmpty()) {
            checkMethod = BINARY_CHECK_METHOD_LOOSE;
        }
        logger.debug("streamIsBinary using method:" + checkMethod);
        switch (checkMethod.toUpperCase()) {
            case BINARY_CHECK_METHOD_LOOSE:
                logger.debug(String.format("check loose method:%s, maxNonText:%d, maxBytes:%d", checkMethod, binaryCheckLooseMaxNonText, binaryCheckLooseMaxBytes));
                isBinary = !FileEncoding.SINGLE_INSTANCE.contentIsTextLoose(instream, true, binaryCheckLooseMaxNonText, binaryCheckLooseMaxBytes);
                break;
            case BINARY_CHECK_METHOD_MIME:
                this.mimeType = FileEncoding.SINGLE_INSTANCE.detectMimeType(instream);
                isBinary = !FileEncoding.SINGLE_INSTANCE.mimeTypeIsText(mimeType);
                break;
            case BINARY_CHECK_METHOD_STRICT:
            default:
                isBinary = !FileEncoding.SINGLE_INSTANCE.contentIsText(instream, true);
                break;
        }
        return isBinary;
    }

    private void checkStreamDocumentBinary(InputStream instream, boolean close) throws Exception {
        try {
            boolean skipBinaryCheck = BINARY_CHECK_METHOD_SKIP.equalsIgnoreCase(binaryCheckMethod);
            if (!skipBinaryCheck) {
                BufferedInputStream bis;
                if (base64) {
                    bis = new BufferedInputStream(new Base64InputStream(instream));
                } else {
                    bis = new BufferedInputStream(instream);
                }
                try {
                    if (compressed && COMPRESSION_TYPE_GZIP.equals(compressionType)) {
                        InputStream gzis = new GZIPInputStream(bis);
                        try {
                            binary = (streamIsBinary(gzis));
                        } finally {
                            if (close) {
                                IOUtils.closeQuietly(gzis);
                            }
                        }
                    } else if (compressed && COMPRESSION_TYPE_ZIP.equalsIgnoreCase(compressionType)) {
                        ZipInputStream zip = new ZipInputStream(bis);
                        try {
                            ZipEntry entry;
                            while ((entry = zip.getNextEntry()) != null) {
                                if (!entry.isDirectory()) {
                                    binary = (streamIsBinary(zip));
                                    if (entry.getSize() > 0) {
                                        sizeBytes = entry.getSize();
                                    }
                                    break;
                                }
                            }
                        } finally {
                            if (close) {
                                IOUtils.closeQuietly(zip);
                            }
                        }

                    } else {
                        binary = (streamIsBinary(bis));
                    }
                } finally {
                    if (close) {
                        IOUtils.closeQuietly(bis);
                    }

                }
            }
            if (mimeType == null || mimeType.length() == 0) {
                if (binary) {
                    mimeType = "application/octet-stream";
                } else {
                    mimeType = "application/xml";
                }
            }
        } finally {
            if (close) {
                IOUtils.closeQuietly(instream);
            }
        }
    }

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append("isBase64:").append(base64).append(',');
        buf.append("isCompressed:").append(compressed).append(',');
        if (compressed) {
            buf.append("compressionType:").append(compressionType).append(',');
        }
        buf.append("isBinary:").append(binary).append(',');
        buf.append("mimeType:").append(mimeType).append(',');
        buf.append("sizeBytes:").append(sizeBytes);

        return buf.toString();

    }

    public InputStream getContentStream() throws Exception {
        InputStream inStream;
        if (file != null) {
            inStream = new FileInputStream(file);
        } else {
            inStream = new ByteArrayInputStream(dataArray);
        }
        if (base64) {
            inStream = new Base64InputStream(inStream);
        }
        if (compressed) {
            if (COMPRESSION_TYPE_ZIP.equalsIgnoreCase(compressionType)) {
                ZipInputStream zip = new ZipInputStream(inStream);
                inStream = zip;
                ZipEntry entry;
                // find first file
                while ((entry = zip.getNextEntry()) != null) {
                    if (!entry.isDirectory()) {
                        break;
                    }
                }
            } else {
                inStream = new GZIPInputStream(inStream);
            }
        }
        return inStream;
    }

    public InputStream getEmbeddableStream() throws Exception {
        InputStream inStream = getContentStream();
        if (binary) {
            inStream = new Base64InputStream(inStream, true);
        }
        return inStream;
    }

    public void analyze() throws Exception {
        if (file != null) {
            analyzeFile();
        } else if (dataArray != null) {
            analyzeByteArray();
        }
    }

    private boolean isEmpty(String str) {
        return (str == null) || (str.length() == 0);
    }

    @SuppressWarnings("resource")
	private void analyzeFile() throws Exception {
        if (file != null && file.exists()) {
            sizeBytes = file.length();
            logger.debug("File:" + file + " original size: " + sizeBytes);
            if (isEmpty(getBase64Override())) {
                checkStreamDocumentBase64(new FileInputStream(file), true);
            } else {
                base64 = Boolean.parseBoolean(getBase64Override());
                logger.debug("Base64Override:" + base64);
            }
            if (base64) {
                sizeBytes = (sizeBytes / 4) * 3;
                logger.debug("File:" + file + " base64 decoded estimated size: " + sizeBytes);
            }
            if (isEmpty(getCompressedOverride())) {
                checkStreamDocumentCompressed(new FileInputStream(file), true);
            } else {
                compressed = Boolean.parseBoolean(getCompressedOverride());
                logger.debug("CompressedOverride:" + compressed);
                if (compressed && isEmpty(getCompressionTypeOverride())) {
                    checkStreamDocumentCompressed(new FileInputStream(file), true);
                } else {
                    compressionType = getCompressionTypeOverride();
                    logger.debug("CompressionTypeOverride:" + compressionType);
                }
            }
            if (compressed && COMPRESSION_TYPE_GZIP.equalsIgnoreCase(compressionType)) {
                long nrOfBytes = getGzipDecompressedSizeBytes(file);
                if (nrOfBytes >= 0) {
                    sizeBytes = nrOfBytes;
                } else {
                    sizeBytes = getStreamSizeBytes(new FileInputStream(file), true);
                }
                logger.debug("File:" + file + " uncompressed estimated size: " + sizeBytes);
            }
            if (isEmpty(getBinaryOverride())) {
                checkStreamDocumentBinary(new FileInputStream(file), true);
            } else {
                binary = Boolean.parseBoolean(getBinaryOverride());
                logger.debug("BinaryOverride:" + binary);
            }
        } else {
            throw new Exception("File " + file + " does not exist");
        }
        logger.debug(toString());
    }

    private void analyzeByteArray() throws Exception {
        if (dataArray != null) {
            sizeBytes = dataArray.length;
            if (isEmpty(getBase64Override())) {
                checkStreamDocumentBase64(new ByteArrayInputStream(dataArray), true);
            } else {
                base64 = Boolean.parseBoolean(getBase64Override());
                logger.debug("Base64Override:" + base64);
            }
            if (base64) {
                sizeBytes = (sizeBytes / 4) * 3;
                logger.debug("Base64 decoded estimated size: " + sizeBytes);
            }
            if (isEmpty(getCompressedOverride())) {
                checkStreamDocumentCompressed(new ByteArrayInputStream(dataArray), true);
            } else {
                compressed = Boolean.parseBoolean(getCompressedOverride());
                logger.debug("CompressedOverride:" + compressed);
                if (compressed && isEmpty(getCompressionTypeOverride())) {
                    checkStreamDocumentCompressed(new ByteArrayInputStream(dataArray), true);
                } else {
                    compressionType = getCompressionTypeOverride();
                    logger.debug("CompressionTypeOverride:" + compressionType);
                }
            }
            if (compressed && COMPRESSION_TYPE_GZIP.equalsIgnoreCase(compressionType)) {
                sizeBytes = getStreamSizeBytes(new ByteArrayInputStream(dataArray), true);
            }
            if (isEmpty(getBinaryOverride())) {
                checkStreamDocumentBinary(new ByteArrayInputStream(dataArray), true);
            } else {
                binary = Boolean.parseBoolean(getBinaryOverride());
                logger.debug("BinaryOverride:" + binary);
            }
        }
    }

}
