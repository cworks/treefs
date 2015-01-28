package net.cworks.treefs.syssp;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import net.cworks.treefs.spi.TreeFile;
import net.cworks.treefs.spi.TreePathContentType;

import java.nio.file.Path;

public class SystemFile extends SystemPath implements TreeFile {

    private static final String PROPERTY_SIZE = "size";

    private static final String PROPERTY_CHECKSUM = "checksum";

    private static final String PROPERTY_CONTENT_TYPE = "contentType";

    private Long size = 0L;

    private String checksum = null;

    private TreePathContentType contentType = null;

    SystemFile() {
        super(null);
        type("file");
    }

    SystemFile(Path root) {
        super(root);
        type("file");
    }

    @Override
    @JsonProperty(PROPERTY_SIZE)
    public Long size() {
        return this.size;
    }

    @JsonProperty(PROPERTY_SIZE)
    void size(Long size) {
        this.size = size;
    }

    @Override
    @JsonProperty(PROPERTY_CHECKSUM)
    public String checksum() {
        return this.checksum;
    }

    @JsonProperty(PROPERTY_CHECKSUM)
    void checksum(String checksum) {
        this.checksum = checksum;
    }

    @Override
    @JsonIgnore
    public TreePathContentType contentType() {
        return this.contentType;
    }

    @JsonIgnore
    void contentType(TreePathContentType contentType) {
        this.contentType = contentType;
    }

    @JsonProperty(PROPERTY_CONTENT_TYPE)
    public String contentTypeString() {
        TreePathContentType pct = contentType();
        if(pct != null) {
            return pct.contentType;
        }
        return null;
    }

    @JsonProperty(PROPERTY_CONTENT_TYPE)
    void contentType(String contentType) {
        contentType(TreePathContentType.valueOf(contentType));
    }
}
