package io.cnsoft.services;

/**
 * Created by mtsygan on 14.04.15.
 */
public interface FileManagerFacade {

    /**
     * Desktop actions
     */
    void open(String path);

    void edit(String path);

    /**
     * DrivesServices
     */
    String getAllDrivesNames();

    /**
     * @return drive for CRM (signed with file-label)
     */
    String getCrmDriveName();

    /**
     * FsFileService
     */

    /**
     * @return content of directory (files and dirs) with relevant properties
     */
    String listDirectory(String path);

    /**
     * @return make directory with name "name" in directory "path"
     */
    String mkDir(String path, String name);

    String getIcon(String path);

    String getFile(String path);

    /**
     * FileManagerFacade
     */
    void shiftElement(String sourcePath, String destDirectory, boolean removeSource);

    void shiftElements(String[] sourcePaths, String destDirectory, boolean removeSource);

    void deleteElement(String targetPath);

    void deleteElements(String[] targetPaths);

    void rename(String sourcePath, String name);

    /**
     * AskUserBus
     */
    void answerCurrentQuestion(String data);

    void cancelCurrentOperation();

    /**
     * ArchiveService
     */
    void archiveElement(String sourcePath, String destArchivePath);

    void archiveElements(String[] sourcePaths, String destArchivePath);

    void extractElement(String sourceArchivePath, String destParentPath);

}
