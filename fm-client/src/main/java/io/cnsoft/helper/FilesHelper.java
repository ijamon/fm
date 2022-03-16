package io.cnsoft.helper;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

/**
 * Created by Jamon on 13.03.2016.
 */
public class FilesHelper {

    private final static int UNIT_SIZE = 1024;

    private static final String[] unit = {"B", "Kb", "Mb", "Gb"};

    public static String lengthWithUnit(long length) {
        double doubleLength = length;
        String res = String.format("%.0f %s", doubleLength, unit[0]);

        for (int i = 1; i < unit.length; i++) {
            if (doubleLength > UNIT_SIZE) {
                doubleLength = doubleLength / UNIT_SIZE;
                res = String.format("%.2f %s", doubleLength, unit[i]);
            } else {
                break;
            }
        }

        return res;
    }

    public static Object checkFilePathsArguments(boolean throwException, String... arguments) {
        if (arguments == null || arguments.length == 0) return null;

        for (String path : arguments) {
            if (path == null || path.isEmpty()) {
                if (throwException) {
                    throw new NullPointerException("Path is required");
                } else {
                    return null;
                }
            }
        }

        return null;
    }

    public static void checkPathsForElementCopy(String sourceFile, String destDirectory) throws IllegalArgumentException {
        if (sourceFile == null || sourceFile.isEmpty()) {
            throw new IllegalArgumentException("The source path is required.");
        } else if (destDirectory == null || destDirectory.isEmpty()) {
            throw new IllegalArgumentException("The destination path is required.");
        }

        final File source = new File(sourceFile);

        if (!source.exists()) {
            throw new IllegalArgumentException("The source file/directory not exists: " + sourceFile);
        }
    }

    public static String getFullPath(final String path, final String fileName) {
        return path.endsWith(File.separator) ? path + fileName : path + File.separator + fileName;
    }

    public static List<Path> getAllInternalItems(String[] sourcePaths){
        List<Path> result = new ArrayList<>();

        for(String sourcePath : sourcePaths){
            List<Path> internalPaths = getAllInternalItems(sourcePath);
            result.addAll(internalPaths);
        }

        return result;
    }

    /*
    TODO: Switch to native
     */
    public static List<Path> getAllInternalItems(String sourcePath) {
        final List<Path> result = new ArrayList<>();

        try {
            Files.walkFileTree(Paths.get(sourcePath), EnumSet.of(FileVisitOption.FOLLOW_LINKS), Integer.MAX_VALUE,

                    new SimpleFileVisitor<Path>() {

                        @Override
                        public FileVisitResult postVisitDirectory(Path dir, IOException ex) throws IOException {
                            result.add(dir);

                            return FileVisitResult.CONTINUE;
                        }

                        @Override
                        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                            result.add(file);

                            return FileVisitResult.CONTINUE;
                        }

                    });
        } catch(Exception ex){
            ex.printStackTrace();
        }

        return result;
    }

}


























