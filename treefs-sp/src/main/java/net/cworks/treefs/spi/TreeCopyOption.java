package net.cworks.treefs.spi;

import java.nio.file.CopyOption;

public enum TreeCopyOption implements CopyOption {
    /**
     * Copy should be recursive.
     */
    RECURSIVE,
    /**
     * Replace existing resource
     */
    REPLACE_EXISTING,
    /**
     * Copy should copy source wholly into destination
     * cp source target, results in target/source
     */
    INTO;
}
