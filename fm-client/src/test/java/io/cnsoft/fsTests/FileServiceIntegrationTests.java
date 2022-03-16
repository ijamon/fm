package io.cnsoft.fsTests;

import io.cnsoft.notifier.bridge.NodeBridgeImpl;
import io.cnsoft.notifier.bridge.NotifierBridge;
import io.cnsoft.notifier.data.AskOverwriteData;
import io.cnsoft.notifier.progress.ProgressNotifierImpl;
import io.cnsoft.services.fs.FileSystemServiceAsyncImpl;
import io.cnsoft.services.fs.FsFileService;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Test;
import io.cnsoft.helpers.FilesHelper;
import io.cnsoft.stub.AskUserBusStub;
import io.cnsoft.stub.NotifierBridgeStub;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created by Jamon on 13.03.2016.
 */
public class FileServiceIntegrationTests {

    private String sourceItemPath;

    private String destItemPath;

    private final int testFileLenght = 1024 * 1024 * 20;

    private void prepareSourceItem(boolean isDir) {
        String dir = System.getProperty("user.dir");
        sourceItemPath = dir + File.separator + FilesHelper.SourceItem;
        destItemPath = dir + File.separator + FilesHelper.DestItem;

        File sourceFile = new File(sourceItemPath);

        try {
            if (isDir) {
                sourceFile.mkdir();
            } else {
                sourceFile.createNewFile();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void clearFilesAfterRename() {
        if(!StringUtils.isEmpty(sourceItemPath) && Files.exists(Paths.get(sourceItemPath))){
            FileUtils.deleteQuietly(new File(sourceItemPath));
        }

        if(!StringUtils.isEmpty(destItemPath) && Files.exists(Paths.get(destItemPath))){
            FileUtils.deleteQuietly(new File(destItemPath));
        }
    }

    private void prepareFileForCopy() {
        String dir = System.getProperty("user.dir");

        sourceItemPath = dir + File.separator + FilesHelper.SourceFile;
        destItemPath = dir + File.separator + "dest_dir";

        File sourceFile = new File(sourceItemPath);
        File destDirectory = new File(destItemPath);

        try {
            clearFilesForCopy(sourceFile, destDirectory);

            sourceFile.createNewFile();
            destDirectory.mkdirs();

            io.cnsoft.helpers.FilesHelper.writeRandomBytesToFile(sourceFile.toString(), testFileLenght);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void prepareFileForCopyDestExist() {
        String dir = System.getProperty("user.dir");

        sourceItemPath = dir + File.separator + FilesHelper.SourceFile;
        destItemPath = dir + File.separator + "dest_dir";
        String destFilePath = destItemPath + File.separator + FilesHelper.SourceFile;

        File sourceFile = new File(sourceItemPath);
        File destDirectory = new File(destItemPath);
        File destFile = new File(destFilePath);

        try {
            clearFilesForCopy(sourceFile, destDirectory);

            destDirectory.mkdirs();

            sourceFile.createNewFile();
            destFile.createNewFile();

            io.cnsoft.helpers.FilesHelper.writeRandomBytesToFile(sourceFile.toString(), testFileLenght);
            io.cnsoft.helpers.FilesHelper.writeRandomBytesToFile(destFile.toString(), testFileLenght);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void clearFilesForCopy(File sourceFile, File destDirectory) {
        try {
            if (sourceFile.exists()) {
                sourceFile.delete();
            }

            if (destDirectory.exists()) {
                File[] files = destDirectory.listFiles();

                for(File destFile : files){
                    destFile.delete();
                }

                destDirectory.delete();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Test
    public void copyFileCleanTest() {
        prepareFileForCopy();

        AskUserBusStub userBus = new AskUserBusStub();
        NotifierBridgeStub bridgeStub = new NotifierBridgeStub();
        ProgressNotifierImpl notifier = new ProgressNotifierImpl(bridgeStub);

        FsFileService fsService = new FsFileService(notifier, userBus);
        FileSystemServiceAsyncImpl fileService = new FileSystemServiceAsyncImpl(notifier, fsService, userBus);

        fileService.shiftElement(sourceItemPath, destItemPath, false);

        try {
            fileService.getLatch().await();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        Path source = Paths.get(sourceItemPath);
        Path dest = Paths.get(destItemPath).resolve(source.getFileName());
        boolean filesMatches = io.cnsoft.helpers.FilesHelper.filesEquals(source, dest);
        Assert.assertEquals(true, filesMatches);
        clearFilesForCopy(new File(sourceItemPath), new File(destItemPath));
    }

    @Test
    public void moveFileCleanTest() {
        prepareFileForCopy();

        AskUserBusStub userBus = new AskUserBusStub();
        NotifierBridgeStub bridgeStub = new NotifierBridgeStub();
        ProgressNotifierImpl notifier = new ProgressNotifierImpl(bridgeStub);

        FsFileService fsService = new FsFileService(notifier, userBus);
        FileSystemServiceAsyncImpl fileService = new FileSystemServiceAsyncImpl(notifier, fsService, userBus);

        fileService.shiftElement(sourceItemPath, destItemPath, true);

        try {
            fileService.getLatch().await();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        Path source = Paths.get(sourceItemPath);
        Path dest = Paths.get(destItemPath).resolve(source.getFileName());

        Assert.assertTrue(Files.notExists(source));
        Assert.assertTrue(Files.exists(dest));

        clearFilesForCopy(new File(sourceItemPath), new File(destItemPath));
    }

    @Test
    public void deleteFileTest(){
        NodeBridgeImpl bi = new NodeBridgeImpl();

        String message = "some error message";
        bi.execute(NotifierBridge.Methods.SEND_ERROR, message);

        prepareFileForCopy();

        AskUserBusStub userBus = new AskUserBusStub();
        NotifierBridgeStub bridgeStub = new NotifierBridgeStub();
        ProgressNotifierImpl notifier = new ProgressNotifierImpl(bridgeStub);

        FsFileService fsService = new FsFileService(notifier, userBus);
        FileSystemServiceAsyncImpl fileService = new FileSystemServiceAsyncImpl(notifier, fsService, userBus);

        fileService.deleteElement(sourceItemPath);

        try {
            fileService.getLatch().await();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        Path source = Paths.get(sourceItemPath);
        Assert.assertTrue(Files.notExists(source));

        clearFilesForCopy(new File(sourceItemPath), new File(destItemPath));
    }

    @Test
    public void copyFileDestExistActionOverwriteAllTest() {
        copyFileDestExistActionDependentTest(AskOverwriteData.OverwriteOperations.OVERWRITE_ALL, true);
    }

    @Test
    public void copyFileDestExistActionOverwriteTest() {
        copyFileDestExistActionDependentTest(AskOverwriteData.OverwriteOperations.OVERWRITE, true);
    }

    @Test
    public void copyFileDestExistActionSkipTest() {
        copyFileDestExistActionDependentTest(AskOverwriteData.OverwriteOperations.SKIP, false);
    }

    @Test
    public void copyFileDestExistActionSkipAllTest() {
        copyFileDestExistActionDependentTest(AskOverwriteData.OverwriteOperations.SKIP_ALL, false);
    }

    private void copyFileDestExistActionDependentTest(AskOverwriteData.OverwriteOperations overwriteOperation, boolean expectedTargetOverwrited) {
        prepareFileForCopyDestExist();

        AskUserBusStub userBus = new AskUserBusStub();
        NotifierBridgeStub bridgeStub = new NotifierBridgeStub();
        ProgressNotifierImpl notifier = new ProgressNotifierImpl(bridgeStub);

        FsFileService fsService = new FsFileService(notifier, userBus);
        FileSystemServiceAsyncImpl fileService = new FileSystemServiceAsyncImpl(notifier, fsService, userBus);

        userBus.setFirstAnswer(overwriteOperation);
        fileService.shiftElement(sourceItemPath, destItemPath, false);

        try {
            fileService.getLatch().await();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        NotifierBridgeStub.ExecutionModel model = new NotifierBridgeStub.ExecutionModel(NotifierBridge.Methods.UPDATE_PROGRESS, null);
        boolean containsUpdateMethod = bridgeStub.getExecutionList().containsValue(model);
        Assert.assertEquals(expectedTargetOverwrited, containsUpdateMethod);

        Path source = Paths.get(sourceItemPath);
        Path dest = Paths.get(destItemPath).resolve(source.getFileName());
        boolean filesMatches = io.cnsoft.helpers.FilesHelper.filesEquals(source, dest);
        Assert.assertEquals(expectedTargetOverwrited, filesMatches);

        clearFilesForCopy(new File(sourceItemPath), new File(destItemPath));
    }

    //rename test
    @Test
    public void renameFileTest(){
        prepareSourceItem(false);

        AskUserBusStub userBus = new AskUserBusStub();
        NotifierBridgeStub bridgeStub = new NotifierBridgeStub();
        ProgressNotifierImpl notifier = new ProgressNotifierImpl(bridgeStub);

        FsFileService fsService = new FsFileService(notifier, userBus);
        FileSystemServiceAsyncImpl fileService = new FileSystemServiceAsyncImpl(notifier, fsService, userBus);

        fileService.rename(sourceItemPath, FilesHelper.DestItem);

        try {
            fileService.getLatch().await();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        NotifierBridgeStub.ExecutionModel model = new NotifierBridgeStub.ExecutionModel(NotifierBridge.Methods.FINISH_OPERATION, null);
        boolean containsFinishMethod = bridgeStub.getExecutionList().containsValue(model);
        Assert.assertEquals(true, containsFinishMethod);

        Path sourceFile = Paths.get(sourceItemPath);
        Assert.assertFalse(Files.exists(sourceFile));

        Path destFile = sourceFile.getParent().resolve(FilesHelper.DestItem);
        Assert.assertTrue(Files.exists(destFile));
        Assert.assertFalse(Files.isDirectory(destFile));

        clearFilesAfterRename();
    }

    @Test
    public void renameDirectoryTest() {
        prepareSourceItem(true);

        AskUserBusStub userBus = new AskUserBusStub();
        NotifierBridgeStub bridgeStub = new NotifierBridgeStub();
        ProgressNotifierImpl notifier = new ProgressNotifierImpl(bridgeStub);

        FsFileService fsService = new FsFileService(notifier, userBus);
        FileSystemServiceAsyncImpl fileService = new FileSystemServiceAsyncImpl(notifier, fsService, userBus);

        fileService.rename(sourceItemPath, FilesHelper.DestItem);

        try {
            fileService.getLatch().await();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        NotifierBridgeStub.ExecutionModel model = new NotifierBridgeStub.ExecutionModel(NotifierBridge.Methods.FINISH_OPERATION, null);
        boolean containsFinishMethod = bridgeStub.getExecutionList().containsValue(model);
        Assert.assertEquals(true, containsFinishMethod);

        Path sourceFile = Paths.get(sourceItemPath);
        Assert.assertFalse(Files.exists(sourceFile));

        Path destFile = sourceFile.getParent().resolve(FilesHelper.DestItem);
        Assert.assertTrue(Files.exists(destFile));
        Assert.assertTrue(Files.isDirectory(destFile));

        clearFilesAfterRename();
    }

}


















