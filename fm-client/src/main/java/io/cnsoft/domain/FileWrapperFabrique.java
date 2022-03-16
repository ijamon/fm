package io.cnsoft.domain;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
//import java.util.stream.Collectors;

/**
 * Created by Jamon on 08.05.2016.
 */
public class FileWrapperFabrique {

    public static FileWrapper getFileWrapper(Path filePath) {
        return FileWrapperFabrique.getFileWrapper(filePath.toString());
    }

    public static FileWrapper getFileWrapper(String filePath) {
        return FileWrapperFabrique.getFileWrapper(new File(filePath));
    }

    public static FileWrapper getFileWrapper(File file) {
        if (file == null) {
            throw new NullPointerException("The file is required.");
        }

        return new FileWrapper(file.getPath(), file.length(), file.lastModified(), file.isDirectory());
    }

    public static List<FileWrapper> getFileWrappers(List<File> files) {
        if (files == null || files.isEmpty()) {
            throw new NullPointerException("The file is required.");
        }

        List<FileWrapper> result = new ArrayList(); //files.stream().map(FileWrapperFabrique::getFileWrapper).collect(Collectors.toList());
        for(File file : files){
            FileWrapper wrapper = FileWrapperFabrique.getFileWrapper(file);
            result.add(wrapper);
        }

        return result;
    }

}