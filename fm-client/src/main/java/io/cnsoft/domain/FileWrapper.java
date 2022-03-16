package io.cnsoft.domain;

import io.cnsoft.helper.FilesHelper;
import io.cnsoft.helper.JsonHelper;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * Created by maxim on 29.11.15.
 */

public class FileWrapper {

    @Getter
    private boolean isDirectory;

    @Getter
    private long lastModified;

    @Getter
    @Setter
    private String path;

    @Getter
    @Setter
    private String parent;

    @Getter
    private String extension;

    @Getter
    private String lengthView;

    @Getter
    private long lengthBytes;

    public FileWrapper(String path, long lengthBytes, long lastModified, boolean isDirectory) {
        if(StringUtils.isEmpty(path)) {
            throw new NullPointerException("The path is required.");
        }

        this.path = path;
        this.lengthBytes = lengthBytes;
        this.lastModified = lastModified;
        this.isDirectory = isDirectory;

        calculateParameters();
    }

    public void calculateParameters(){
        this.extension = calcExtension();
        this.lengthView = FilesHelper.lengthWithUnit(lengthBytes);
    }

    private String calcExtension() {
        if (isDirectory) {
            return "folder";
        } else {
            return FilenameUtils.getExtension(path);
        }
    }

    @Override
    public String toString() {
        return JsonHelper.toJson(this);
    }

}
