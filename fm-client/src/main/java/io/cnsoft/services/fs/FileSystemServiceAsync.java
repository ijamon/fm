package io.cnsoft.services.fs;

/**
 * Created by Jamon on 01.03.2016.
 */
public interface FileSystemServiceAsync {

    void shiftElement(String sourcePath, String destDirectory, boolean removeSource);

    void shiftElements(String[] sourcePaths, String destDirectory, boolean removeSource);

    void deleteElement(String targetPath);

    void deleteElements(String[] targetPaths);

    void rename(String source, String targetName);
}