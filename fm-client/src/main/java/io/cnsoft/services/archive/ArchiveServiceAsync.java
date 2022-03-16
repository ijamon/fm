package io.cnsoft.services.archive;

/**
 * Created by Jamon on 05.05.2016.
 */
public interface ArchiveServiceAsync {

    void archiveElement(String sourcePath, String destArchivePath);

    void archiveElements(String[] sourcePaths, String destArchivePath);

    void extractElement(String sourceArchivePath, String destParentPath);
}
