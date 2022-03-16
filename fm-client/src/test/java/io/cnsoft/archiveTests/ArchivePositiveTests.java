package io.cnsoft.archiveTests;

import io.cnsoft.helper.JsonHelper;
import io.cnsoft.notifier.bridge.NotifierBridge;
import io.cnsoft.notifier.data.AskOverwriteData;
import io.cnsoft.notifier.progress.ProgressNotifierImpl;
import io.cnsoft.services.archive.ArchiveServiceAsyncImpl;
import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import io.cnsoft.helpers.FilesHelper;
import io.cnsoft.stub.AskUserBusStub;
import io.cnsoft.stub.NotifierBridgeStub;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Random;

/**
 * Created by Jamon on 05.05.2016.
 */
public class ArchivePositiveTests {

    private String currentDirPath;

    private String sourceFilePath;

    private String sourceDirPath;

    private String destArchivePath;

    private String destExtractArchiveDir;

    private AskUserBusStub userBus;

    private NotifierBridgeStub bridgeStub;

    private ArchiveServiceAsyncImpl archImpl;

    private final int testFileLenght = 1024 * 1024 * 1;

    private void prepareDirectoryWithInnerFiles() {
        currentDirPath = System.getProperty("user.dir");
        sourceDirPath = currentDirPath + File.separator + FilesHelper.SourceDir;
        destArchivePath = currentDirPath + File.separator + "dest_arc.zip";
        sourceFilePath = currentDirPath + File.separator + "source_1";
        destExtractArchiveDir = currentDirPath + File.separator + FilesHelper.DestDir;

        Path sourceDir = Paths.get(sourceDirPath);

        byte[] randomFile = new byte[testFileLenght];
        new Random().nextBytes(randomFile);

        try {
            clearData();
            Files.createDirectory(sourceDir);

            for (int i = 0; i < 3; i++) {
                Path filePath = sourceDir.resolve(String.valueOf(i));
                FilesHelper.writeBytesToFile(filePath.toString(), randomFile);
            }

            Path innerDir = sourceDir.resolve("inner_dir");
            Files.createDirectory(innerDir);

            for (int i = 3; i < 6; i++) {
                Path filePath = innerDir.resolve(String.valueOf(i));
                FilesHelper.writeBytesToFile(filePath.toString(), randomFile);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void createFakeDestArchive(){
        byte[] randomFile = new byte[testFileLenght];
        new Random().nextBytes(randomFile);

        FilesHelper.writeBytesToFile(destArchivePath, randomFile);
    }

    private void addSourceFile() {
        FilesHelper.writeRandomBytesToFile(sourceFilePath, testFileLenght);
    }

    private void addDestExtractDir() {
        try {
            Files.createDirectory(Paths.get(destExtractArchiveDir));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void clearData() {
        try {
            if (sourceDirPath != null && Files.exists(Paths.get(sourceDirPath))) {
                FileUtils.deleteDirectory(new File(sourceDirPath));
            }

            if (destArchivePath != null && Files.exists(Paths.get(destArchivePath))) {
                FileUtils.deleteQuietly(new File(destArchivePath));
            }

            if (sourceFilePath != null && Files.exists(Paths.get(sourceFilePath))) {
                FileUtils.deleteQuietly(new File(sourceFilePath));
            }

            if (destExtractArchiveDir != null && Files.exists(Paths.get(destExtractArchiveDir))) {
                FileUtils.deleteDirectory(new File(destExtractArchiveDir));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Before
    public void initArchImpl() {
        userBus = new AskUserBusStub();
        bridgeStub = new NotifierBridgeStub();
        ProgressNotifierImpl notifier = new ProgressNotifierImpl(bridgeStub);

        archImpl = new ArchiveServiceAsyncImpl(notifier, userBus);
    }

    @Test
    public void compressOneItemTest() {
        prepareDirectoryWithInnerFiles();

        archImpl.archiveElement(sourceDirPath, destArchivePath);

        try {
            archImpl.getLatch().await();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        NotifierBridgeStub.ExecutionModel model = new NotifierBridgeStub.ExecutionModel(NotifierBridge.Methods.UPDATE_PROGRESS, null);
        boolean containsUpdateMethod = bridgeStub.getExecutionList().containsValue(model);
        Assert.assertTrue(containsUpdateMethod);

        long destSize = FileUtils.sizeOf(new File(destArchivePath));
        Assert.assertTrue(destSize > 0);

        clearData();
    }

    @Test
    public void compressOneItemDestExistActionSkipTest() {
        compressOneItemDestExistOverwriteDependTest(AskOverwriteData.OverwriteOperations.SKIP, false);
    }

    @Test
    public void compressOneItemDestExistActionOverwriteTest() {
        compressOneItemDestExistOverwriteDependTest(AskOverwriteData.OverwriteOperations.OVERWRITE, true);
    }

    public void compressOneItemDestExistOverwriteDependTest(AskOverwriteData.OverwriteOperations overwriteOperation, boolean haveUpdateProgress) {
        prepareDirectoryWithInnerFiles();
        createFakeDestArchive();

        userBus.setFirstAnswer(overwriteOperation);
        archImpl.archiveElement(sourceDirPath, destArchivePath);

        try {
            archImpl.getLatch().await();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        NotifierBridgeStub.ExecutionModel model = new NotifierBridgeStub.ExecutionModel(NotifierBridge.Methods.UPDATE_PROGRESS, null);
        boolean containsUpdateMethod = bridgeStub.getExecutionList().containsValue(model);
        Assert.assertEquals(haveUpdateProgress, containsUpdateMethod);

        long destSize = FileUtils.sizeOf(new File(destArchivePath));
        Assert.assertTrue(destSize > 0);

        clearData();
    }

    @Test
    public void compressMultipleItemsTest() {
        prepareDirectoryWithInnerFiles();
        addSourceFile();

        String[] paths = new String[2];
        paths[0] = sourceDirPath;
        paths[1] = sourceFilePath;

        archImpl.archiveElements(paths, destArchivePath);

        try {
            archImpl.getLatch().await();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        NotifierBridgeStub.ExecutionModel model = new NotifierBridgeStub.ExecutionModel(NotifierBridge.Methods.UPDATE_PROGRESS, null);
        boolean containsUpdateMethod = bridgeStub.getExecutionList().containsValue(model);
        Assert.assertTrue(containsUpdateMethod);

        long destSize = FileUtils.sizeOf(new File(destArchivePath));
        Assert.assertTrue(destSize > 0);

        clearData();
    }

    @Test
    public void extractItemTest() {
        prepareDirectoryWithInnerFiles();
        addDestExtractDir();

        archImpl.archiveElement(sourceDirPath, destArchivePath);

        try {
            archImpl.getLatch().await();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        archImpl.extractElement(destArchivePath, destExtractArchiveDir);

        try {
            archImpl.getLatch().await();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        Assert.assertTrue(FilesHelper.directoryEqualsByCrc(Paths.get(sourceDirPath), Paths.get(destExtractArchiveDir).resolve(FilesHelper.SourceDir)));

        clearData();
    }

    @Test
    public void extractItemDestExistOverwriteTest(){
        extractItemDestExistOverwriteDependTest(AskOverwriteData.OverwriteOperations.OVERWRITE, true);
    }

    @Test
    public void extractItemDestExistOverwriteAllTest(){
        extractItemDestExistOverwriteDependTest(AskOverwriteData.OverwriteOperations.OVERWRITE_ALL, true);
    }

    @Test
    public void extractItemDestExistSkipTest(){
        extractItemDestExistOverwriteDependTest(AskOverwriteData.OverwriteOperations.SKIP, true);
    }

    public void extractItemDestExistOverwriteDependTest(AskOverwriteData.OverwriteOperations overwriteAnswer, boolean expectHaveQuestion) {
        prepareDirectoryWithInnerFiles();
        addDestExtractDir();

        try {
            userBus.setFirstAnswer(overwriteAnswer);

            String destExistSourceDir = destExtractArchiveDir + File.separator + FilesHelper.SourceDir;
            FileUtils.copyDirectory(new File(sourceDirPath), new File(destExistSourceDir));

            archImpl.archiveElement(sourceDirPath, destArchivePath);

            archImpl.getLatch().await();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        archImpl.extractElement(destArchivePath, destExtractArchiveDir);

        try {
            archImpl.getLatch().await();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        Assert.assertTrue(FilesHelper.directoryEqualsByCrc(Paths.get(sourceDirPath), Paths.get(destExtractArchiveDir).resolve(FilesHelper.SourceDir)));

        NotifierBridgeStub.ExecutionModel questionModel = new NotifierBridgeStub.ExecutionModel(NotifierBridge.Methods.ASK_QUESTION, null);
        boolean containsQuestion = bridgeStub.getExecutionList().containsValue(questionModel);
        Assert.assertEquals(expectHaveQuestion, containsQuestion);

        clearData();
    }


}























