package io.cnsoft.fsTests;

import io.cnsoft.notifier.bridge.AskUserBus;
import io.cnsoft.notifier.progress.ProgressNotifier;
import io.cnsoft.services.fs.FileSystemServiceAsyncImpl;
import io.cnsoft.services.fs.FsFileService;
import org.junit.Assert;
import org.junit.Test;
import io.cnsoft.helpers.FilesHelper;
import io.cnsoft.stub.AskUserBusStub;
import io.cnsoft.stub.ProgressNotifierStub;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created by Jamon on 02.05.2016.
 */
public class MultipleOperationTests {

    private Path sourceFilePath1;

    private Path sourceFilePath2;

    private Path destDirectoryPath;

    private final int testFileLenght = 1024 * 1024 * 2;

    private void prepareFilesForCopy() {
        Path currentDirPath = Paths.get(System.getProperty("user.dir"));

        sourceFilePath1 = currentDirPath.resolve("source.file.1");
        sourceFilePath2 = currentDirPath.resolve("source.file.2");
        destDirectoryPath = currentDirPath.resolve("dest_dir");

        try {
            clearFilesForCopy();

            io.cnsoft.helpers.FilesHelper.writeRandomBytesToFile(sourceFilePath1.toString(), testFileLenght);
            io.cnsoft.helpers.FilesHelper.writeRandomBytesToFile(sourceFilePath2.toString(), testFileLenght);

            Files.createDirectory(destDirectoryPath);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void clearFilesForCopy() {
        try {
            if (sourceFilePath1 != null && Files.exists(sourceFilePath1)) {
                Files.delete(sourceFilePath1);
            }

            if (sourceFilePath2 != null && Files.exists(sourceFilePath2)) {
                Files.delete(sourceFilePath2);
            }

            if (Files.exists(destDirectoryPath)) {
                try (DirectoryStream<Path> stream = Files.newDirectoryStream(destDirectoryPath)) {
                    for (Path path : stream) {
                        Files.delete(path);
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                Files.delete(destDirectoryPath);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Test
    public void multipleFilesCopyTest(){
        prepareFilesForCopy();

        AskUserBus userBus = new AskUserBusStub();
        ProgressNotifier notifier = new ProgressNotifierStub();
        FsFileService fsService = new FsFileService(notifier, userBus);

        FileSystemServiceAsyncImpl fileService = new FileSystemServiceAsyncImpl(notifier, fsService, userBus);

        String[] source = new String[2];
        source[0] = sourceFilePath1.toString();
        source[1] = sourceFilePath2.toString();

        fileService.shiftElements(source, destDirectoryPath.toString(), false);

        try {
            fileService.getLatch().await();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        Path destFilePath1 = destDirectoryPath.resolve(sourceFilePath1.getFileName());
        Path destFilePath2 = destDirectoryPath.resolve(sourceFilePath2.getFileName());

        boolean file1equals = FilesHelper.filesEquals(sourceFilePath1, destFilePath1);
        boolean file2equals = FilesHelper.filesEquals(sourceFilePath2, destFilePath2);

        Assert.assertTrue(file1equals);
        Assert.assertTrue(file2equals);

        clearFilesForCopy();
    }

    @Test
    public void deleteFilesTest(){
        prepareFilesForCopy();

        AskUserBus userBus = new AskUserBusStub();
        ProgressNotifier notifier = new ProgressNotifierStub();
        FsFileService fsService = new FsFileService(notifier, userBus);

        FileSystemServiceAsyncImpl fileService = new FileSystemServiceAsyncImpl(notifier, fsService, userBus);

        String[] source = new String[2];
        source[0] = sourceFilePath1.toString();
        source[1] = sourceFilePath2.toString();

        fileService.deleteElements(source);

        try {
            fileService.getLatch().await();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        Assert.assertTrue(Files.notExists(sourceFilePath1));
        Assert.assertTrue(Files.notExists(sourceFilePath2));

        clearFilesForCopy();
    }

}






























