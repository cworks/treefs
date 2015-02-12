package cworks.treefs.awssp;

import cworks.json.JsonObject;
import cworks.treefs.common.dt.ISO8601DateParser;
import cworks.treefs.spi.TreePath;
import org.apache.log4j.Logger;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.util.Date;
import java.util.Map;

import static cworks.treefs.common.ObjectUtils.isNullOrEmpty;

public class S3Path implements TreePath {

    /**
     * Logger
     */
    private static final Logger logger = Logger.getLogger(S3Path.class);

    /**
     * Json instance from S3 that contains most of the properties needed to make a SFolder instance
     */
    protected JsonObject jsonObject = null;

    /**
     * Must have a JsonObject to create a S3Path instance
     * @param data
     */
    public S3Path(JsonObject data) {
        this.jsonObject = data;
    }

    @Override
    public String description() {
        String description = this.jsonObject.getString("description", "");
        return description;
    }

    @Override
    public Date lastModifiedTime() {
        Date lastModifiedTime = parseDateTime("lastModifiedTime");
        return lastModifiedTime;
    }

    @Override
    public Date lastAccessedTime() {
        Date lastAccessedTime = parseDateTime("lastModifiedTime");
        return lastAccessedTime;
    }

    @Override
    public Date creationTime() {
        Date creationTime = parseDateTime("creationTime");
        return creationTime;
    }

    @Override
    public Path path() {
        String spath = this.jsonObject.getString("path");
        Path path = Paths.get(spath);
        return path;
    }

    @Override
    public String name() {
        String name = this.jsonObject.getString("name");
        return name;
    }

    @Override
    public boolean hasMetadata() {
        JsonObject meta = this.jsonObject.getObject("metadata");
        if(isNullOrEmpty(meta)) {
            return false;
        }
        return true;
    }

    @Override
    public Map<String, Object> metadata() {
        if(hasMetadata()) {
            JsonObject meta = this.jsonObject.getObject("metadata");
            return meta.toMap();
        }

        return null;
    }

    Date parseDateTime(String field) {
        String dts = this.jsonObject.getString(field);
        if(isNullOrEmpty(dts)) {
            return null;
        }

        try {
            return ISO8601DateParser.parse(dts);
        } catch (ParseException ex) {
            logger.error("Cannot parse " + field + ": " + this.jsonObject.getString(field));
        }

        return null;
    }
}
