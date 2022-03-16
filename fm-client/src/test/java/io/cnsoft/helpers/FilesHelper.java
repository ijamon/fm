package io.cnsoft.helpers;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.zip.CRC32;

/**
 * Created by Jamon on 30.04.2016.
 */
public class FilesHelper {

    private static SecureRandom rand = new SecureRandom();

    public static final String SourceDir = "source_dir";

    public static final String DestDir = "dest_dir";

    public static final String HugeDir = "huge_dir";


    public static final String SourceFile = "source.file";

    public static final String DestFile = "dest.file";


    public static final String SourceItem = "source.item";

    public static final String DestItem = "dest.item";

    public static long getLengthOfFile(String path) {
        long result = 0;

        try {
            RandomAccessFile file = new RandomAccessFile(path, "r");
            result = file.length();
        } catch (Exception ex){

        }

        return result;
    }

    public static byte[] readBytesFromFile(String path) {
        byte[] result = null;

        try {
            FileInputStream stream = new FileInputStream(path);
            result = IOUtils.toByteArray(stream);
            stream.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            return result;
        }
    }

    public static void writeRandomBytesToFile(String path, int fileLength){
        try {
            FileOutputStream out = new FileOutputStream(path);

            byte[] buffer = new byte[fileLength];
            rand.nextBytes(buffer);
            IOUtils.write(buffer, out);

            out.close();
        } catch(Exception ex){
            ex.printStackTrace();
        }
    }

    public static void writeBytesToFile(String path, byte[] bytes){
        try {
            FileOutputStream out = new FileOutputStream(path);

            IOUtils.write(bytes, out);

            out.close();
        } catch(Exception ex){
            ex.printStackTrace();
        }
    }

    public static boolean filesEquals(String source, String dest) {
        boolean result = false;

        try {
            if(!Files.exists(Paths.get(source)) || !Files.exists(Paths.get(dest))) return false;

            FileInputStream sourceStream = new FileInputStream(source);
            FileInputStream destStream = new FileInputStream(dest);

            result = IOUtils.contentEquals(sourceStream, destStream);

            sourceStream.close();
            destStream.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return result;
    }

    public static boolean filesEquals(Path source, Path dest){
        return filesEquals(source.toString(), dest.toString());
    }

    public static boolean directoryEqualsByCrc(Path dir1Path, Path dir2Path){
        long dir1Crc = calculateDirectoryCrc(dir1Path);
        long dir2Crc = calculateDirectoryCrc(dir2Path);

        return dir1Crc == dir2Crc;
    }

    public static long calculateDirectoryCrc(final Path path){
        final List<Byte> directoryBytesList = new ArrayList<Byte>();

        try {
            Files.walkFileTree(path, new SimpleFileVisitor<Path>() {

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                    try {
                        FileInputStream stream = new FileInputStream(file.toString());
                        byte[] fileBytes = IOUtils.toByteArray(stream);
                        directoryBytesList.addAll(Arrays.asList(ArrayUtils.toObject(fileBytes)));
                        stream.close();
                    } catch(Exception ex){
                        ex.printStackTrace();
                    }

                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFileFailed(Path file, IOException exc) {
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) {
                    String folderName = path.relativize(dir).toString();
                    byte[] folderNameBytes = folderName.getBytes();

                    directoryBytesList.addAll(Arrays.asList(ArrayUtils.toObject(folderNameBytes)));
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            throw new AssertionError("walkFileTree will not throw IOException if the FileVisitor does not");
        }

        byte[] directoryBytes = ArrayUtils.toPrimitive(directoryBytesList.toArray(new Byte[directoryBytesList.size()]));

        CRC32 result = new CRC32();
        result.update(directoryBytes);

        return result.getValue();
    }

}



















