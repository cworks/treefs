package cworks.treefs.awssp;

import cworks.json.JsonObject;
import cworks.treefs.spi.TreeFile;
import cworks.treefs.spi.TreePathContentType;
import org.apache.log4j.Logger;

public class S3File extends S3Path implements TreeFile {

    /**
     * Logger
     */
    private static final Logger logger = Logger.getLogger(S3File.class);

    /**
     * Must have a JsonObject to create a S3Path instance
     *
     * @param data
     */
    public S3File(JsonObject data) {
        super(data);
    }

    @Override
    public Long size() {
        Long size = jsonObject.getLong("size", -1);
        return size;
    }

    void size(Long size) {
        jsonObject.setNumber("size", size);
    }

    @Override
    public String checksum() {
        String checksum = jsonObject.getString("checksum");
        return checksum;
    }

    @Override
    public TreePathContentType contentType() {
        String ct = jsonObject.getString("contentType");
        return TreePathContentType.valueOf(ct);
    }
}
