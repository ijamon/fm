package io.cnsoft.fsTests;

import io.cnsoft.notifier.bridge.AskUserBus;
import io.cnsoft.notifier.bridge.NotifierBridge;
import io.cnsoft.notifier.data.AskOverwriteData;
import io.cnsoft.notifier.progress.ProgressNotifier;
import io.cnsoft.notifier.progress.ProgressNotifierImpl;
import io.cnsoft.services.fs.FileSystemServiceAsyncImpl;
import io.cnsoft.services.fs.FsFileService;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.junit.Assert;
import org.junit.Test;
import io.cnsoft.helpers.FilesHelper;
import io.cnsoft.stub.AskUserBusStub;
import io.cnsoft.stub.NotifierBridgeStub;
import io.cnsoft.stub.ProgressNotifierStub;

import java.io.File;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by Jamon on 25.04.2016.
 */
public class DirectoryIntegrationTests {

    private String sourceDirectoryPath;

    private String destDirectoryPath;

    private String hugeDirectoryPath;

    private final int testFileLenght = 1024 * 1024 * 1;

    private void prepareHugeDirectory(){
        String currentDir = System.getProperty("user.dir");

        hugeDirectoryPath = currentDir + File.separator + FilesHelper.HugeDir;
        Path hugeDirectory = Paths.get(hugeDirectoryPath);

        int targetQuantity = 10;

        try {
            clearDirs();
            Files.createDirectory(hugeDirectory);

            for(int i=0;i<targetQuantity;i++) {
                Files.createFile(hugeDirectory.resolve(String.valueOf(i)));
            }
        } catch(Exception ex){
            ex.printStackTrace();
        }
    }

    private void prepareDirectoryWithDirectories() {
        String dir = System.getProperty("user.dir");

        sourceDirectoryPath = dir + File.separator + FilesHelper.SourceDir;

        Path sourceDir = Paths.get(sourceDirectoryPath);

        try {
            clearDirs();
            Files.createDirectory(sourceDir);

            Path innerDir = sourceDir.resolve("inner_dir.folder");
            Files.createDirectory(innerDir);
        } catch(Exception ex) {
            ex.printStackTrace();
        }
    }

    private void prepareDirectoryWithInnerFiles() {
        String dir = System.getProperty("user.dir");

        sourceDirectoryPath = dir + File.separator + FilesHelper.SourceDir;
        destDirectoryPath = dir + File.separator + FilesHelper.DestDir;

        Path sourceDir = Paths.get(sourceDirectoryPath);
        Path destDirectory = Paths.get(destDirectoryPath);

        try {
            clearDirs();
            Files.createDirectory(sourceDir);
            Files.createDirectory(destDirectory);

            for(int i=0;i<3;i++) {
                Path filePath = sourceDir.resolve(String.valueOf(i));
                FilesHelper.writeRandomBytesToFile(filePath.toString(), testFileLenght);
            }

            Path innerDir = sourceDir.resolve("inner_dir");
            Files.createDirectory(innerDir);

            for(int i=3;i<6;i++) {
                Path filePath = innerDir.resolve(String.valueOf(i));
                FilesHelper.writeRandomBytesToFile(filePath.toString(), testFileLenght);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void prepareFileForCopyDestFileOneExist() {
        prepareDirectoryWithInnerFiles();

        Path destDir = Paths.get(destDirectoryPath);
        Path destInternalDir = destDir.resolve(FilesHelper.SourceDir);

        try {
            Files.createDirectory(destInternalDir);

            Path destInternalFile = destInternalDir.resolve(String.valueOf(2));
            FilesHelper.writeRandomBytesToFile(destInternalFile.toString(), testFileLenght);
        } catch(Exception ex){
            ex.printStackTrace();
        }
    }

    /*
    todo: need refactoring
     */
    private boolean executionListContainsMethod(int repetition, Map<Integer, NotifierBridgeStub.ExecutionModel> data, NotifierBridge.Methods targetMethod) {
        List<String> ar = new ArrayList<>();

        for(Map.Entry<Integer, NotifierBridgeStub.ExecutionModel> item : data.entrySet()){
            if(item.getValue().getMethod() == targetMethod){
                ar.add("");
            }
        }
        /*
        data.forEach((k,v)->{
            if(v.getMethod() == targetMethod) ar.add("");
        });
        */
        return ar.size() == repetition;
    }

    private void prepareFileForCopyDestFileTwoExists() {
        prepareDirectoryWithInnerFiles();

        Path destDir = Paths.get(destDirectoryPath);
        Path destInternalDir = destDir.resolve(FilesHelper.SourceDir);

        try {
            Files.createDirectory(destInternalDir);

            Path destInternalFile1 = destInternalDir.resolve(String.valueOf(1));
            FilesHelper.writeRandomBytesToFile(destInternalFile1.toString(), testFileLenght);

            Path destInternalFile2 = destInternalDir.resolve(String.valueOf(2));
            FilesHelper.writeRandomBytesToFile(destInternalFile2.toString(), testFileLenght);
        } catch(Exception ex){
            ex.printStackTrace();
        }
    }

    private void clearDirs() {
        FsFileService fsService = new FsFileService(new ProgressNotifierStub(), new AskUserBusStub());

        try {
            if(!StringUtils.isEmpty(sourceDirectoryPath) && Files.exists(Paths.get(sourceDirectoryPath))) {
                fsService.deleteDirectory(sourceDirectoryPath);
            }

            if(!StringUtils.isEmpty(destDirectoryPath) && Files.exists(Paths.get(destDirectoryPath))) {
                fsService.deleteDirectory(destDirectoryPath);
            }

            if(!StringUtils.isEmpty(hugeDirectoryPath) && Files.exists(Paths.get(hugeDirectoryPath))) {
                fsService.deleteDirectory(hugeDirectoryPath);
            }
        } catch(Exception ex){
            ex.printStackTrace();
        }
    }

    //One target file exist section
    @Test
    public void copyDirToDirTargetOneExistActionSkipAll(){
        copyDirToDirTargetOneExistsLogic(AskOverwriteData.OverwriteOperations.SKIP_ALL, false);
    }

    @Test
    public void copyDirToDirTargetOneExistActionSkip(){
        copyDirToDirTargetOneExistsLogic(AskOverwriteData.OverwriteOperations.SKIP, false);
    }

    @Test
    public void copyDirToDirTargetOneExistActionOverwriteAll(){
        copyDirToDirTargetOneExistsLogic(AskOverwriteData.OverwriteOperations.OVERWRITE_ALL, true);
    }

    @Test
    public void copyDirToDirTargetOneExistActionOverwrite(){
        copyDirToDirTargetOneExistsLogic(AskOverwriteData.OverwriteOperations.OVERWRITE, true);
    }

    private void copyDirToDirTargetOneExistsLogic(AskOverwriteData.OverwriteOperations overwriteOperation, boolean expectedDirsEquals) {
        prepareFileForCopyDestFileOneExist();

        AskUserBusStub userBus = new AskUserBusStub();
        userBus.setFirstAnswer(overwriteOperation);

        NotifierBridgeStub bridgeStub = new NotifierBridgeStub();
        ProgressNotifier notifier = new ProgressNotifierImpl(bridgeStub);
        FsFileService fsService = new FsFileService(notifier, userBus);

        FileSystemServiceAsyncImpl fileService = new FileSystemServiceAsyncImpl(notifier, fsService, userBus);

        fileService.shiftElement(sourceDirectoryPath, destDirectoryPath, false);

        try {
            fileService.getLatch().await();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        try {
            Path sourcePath = Paths.get(sourceDirectoryPath);
            Path destInnerPath = Paths.get(destDirectoryPath).resolve(FilesHelper.SourceDir);

            boolean dirsEquals = FilesHelper.directoryEqualsByCrc(sourcePath, destInnerPath);
            Assert.assertEquals(expectedDirsEquals, dirsEquals);
        } catch(Exception ex){
            ex.printStackTrace();
        }

        clearDirs();
    }

    //Two target files exists section

    @Test
    public void copyDirToDirTargetTwoExistsOverwrite1Overwrite2(){
        copyDirToDirTargetTwoExistsLogic(AskOverwriteData.OverwriteOperations.OVERWRITE, AskOverwriteData.OverwriteOperations.OVERWRITE, true, true);
    }

    @Test
    public void copyDirToDirTargetTwoExistsOverwrite1Skip2(){
        copyDirToDirTargetTwoExistsLogic(AskOverwriteData.OverwriteOperations.OVERWRITE, AskOverwriteData.OverwriteOperations.SKIP, true, false);
    }

    @Test
    public void copyDirToDirTargetTwoExistsSkip1Overwrite2(){
        copyDirToDirTargetTwoExistsLogic(AskOverwriteData.OverwriteOperations.SKIP, AskOverwriteData.OverwriteOperations.OVERWRITE, false, true);
    }

    @Test
    public void copyDirToDirTargetTwoExistsSkip1Skip2(){
        copyDirToDirTargetTwoExistsLogic(AskOverwriteData.OverwriteOperations.SKIP, AskOverwriteData.OverwriteOperations.SKIP, false, false);
    }

    private void copyDirToDirTargetTwoExistsLogic(AskOverwriteData.OverwriteOperations overwriteOperation1, AskOverwriteData.OverwriteOperations overwriteOperation2,
                                                  boolean expectedFile1Equals, boolean expectedFile2Equals) {
        prepareFileForCopyDestFileTwoExists();

        AskUserBusStub userBus = new AskUserBusStub();
        userBus.setFirstAnswer(overwriteOperation1);
        userBus.setSecondAnswer(overwriteOperation2);

        NotifierBridgeStub bridgeStub = new NotifierBridgeStub();
        ProgressNotifier notifier = new ProgressNotifierImpl(bridgeStub);
        FsFileService fsService = new FsFileService(notifier, userBus);

        FileSystemServiceAsyncImpl fileService = new FileSystemServiceAsyncImpl(notifier, fsService, userBus);
        fileService.shiftElement(sourceDirectoryPath, destDirectoryPath, false);

        try {
            fileService.getLatch().await();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        Path sourceDir = Paths.get(sourceDirectoryPath);

        Path sourceFile1 = sourceDir.resolve(String.valueOf(1));
        Path sourceFile2 = sourceDir.resolve(String.valueOf(2));

        Path destDir = Paths.get(destDirectoryPath);
        Path destInternalDir = destDir.resolve(FilesHelper.SourceDir);

        Path destInternalFile1 = destInternalDir.resolve(String.valueOf(1));
        Path destInternalFile2 = destInternalDir.resolve(String.valueOf(2));

        boolean file1Equals = FilesHelper.filesEquals(sourceFile1, destInternalFile1);
        boolean file2Equals = FilesHelper.filesEquals(sourceFile2, destInternalFile2);

        Assert.assertEquals(expectedFile1Equals, file1Equals);
        Assert.assertEquals(expectedFile2Equals, file2Equals);

        boolean executionListVerified = executionListContainsMethod(2, bridgeStub.getExecutionList(), NotifierBridge.Methods.ASK_QUESTION);
        Assert.assertTrue(executionListVerified);

        clearDirs();
    }

    //Two target files exists, multiple operation section
    @Test
    public void copyDirToDirTargetTwoExistsOverwriteAllTest(){
        copyDirToDirTargetTwoExistsMultipleOperationLogic(AskOverwriteData.OverwriteOperations.OVERWRITE_ALL, true);
    }

    @Test
    public void copyDirToDirTargetTwoExistsSkipAllTest(){
        copyDirToDirTargetTwoExistsMultipleOperationLogic(AskOverwriteData.OverwriteOperations.SKIP_ALL, false);
    }

    private void copyDirToDirTargetTwoExistsMultipleOperationLogic(AskOverwriteData.OverwriteOperations multipleOperation, boolean expectedFilesEquals) {
        if(!multipleOperation.isMultipleAction()) return;

        prepareFileForCopyDestFileTwoExists();

        AskUserBusStub userBus = new AskUserBusStub();
        userBus.setFirstAnswer(multipleOperation);

        NotifierBridgeStub bridgeStub = new NotifierBridgeStub();
        ProgressNotifier notifier = new ProgressNotifierImpl(bridgeStub);
        FsFileService fsService = new FsFileService(notifier, userBus);

        FileSystemServiceAsyncImpl fileService = new FileSystemServiceAsyncImpl(notifier, fsService, userBus);
        fileService.shiftElement(sourceDirectoryPath, destDirectoryPath, false);

        try {
            fileService.getLatch().await();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        Path sourceDir = Paths.get(sourceDirectoryPath);

        Path sourceFile1 = sourceDir.resolve(String.valueOf(1));
        Path sourceFile2 = sourceDir.resolve(String.valueOf(2));

        Path destDir = Paths.get(destDirectoryPath);
        Path destInternalDir = destDir.resolve(FilesHelper.SourceDir);

        Path destInternalFile1 = destInternalDir.resolve(String.valueOf(1));
        Path destInternalFile2 = destInternalDir.resolve(String.valueOf(2));

        boolean file1Equals = FilesHelper.filesEquals(sourceFile1, destInternalFile1);
        boolean file2Equals = FilesHelper.filesEquals(sourceFile2, destInternalFile2);

        Assert.assertEquals(expectedFilesEquals, file1Equals);
        Assert.assertEquals(expectedFilesEquals, file2Equals);

        boolean executionListVerified = executionListContainsMethod(1, bridgeStub.getExecutionList(), NotifierBridge.Methods.ASK_QUESTION);
        Assert.assertTrue(executionListVerified);

        clearDirs();
    }

    //Clean copy - target not exists

    @Test
    public void copyDirToDirTargetCleanTest() {
        prepareDirectoryWithInnerFiles();

        AskUserBus userBus = new AskUserBusStub();
        ProgressNotifier notifier = new ProgressNotifierStub();
        FsFileService fsService = new FsFileService(notifier, userBus);

        FileSystemServiceAsyncImpl fileService = new FileSystemServiceAsyncImpl(notifier, fsService, userBus);

        fileService.shiftElement(sourceDirectoryPath, destDirectoryPath, false);

        try {
            fileService.getLatch().await();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        try {
            Path sourcePath = Paths.get(sourceDirectoryPath);
            Path destInnerPath = Paths.get(destDirectoryPath).resolve(FilesHelper.SourceDir);

            boolean dirsEquals = FilesHelper.directoryEqualsByCrc(sourcePath, destInnerPath);
            Assert.assertTrue(dirsEquals);
        } catch(Exception ex){
            ex.printStackTrace();
        }

        clearDirs();
    }

    //Clean move test
    @Test
    public void moveDirToDirTargetCleanTest() {
        prepareDirectoryWithInnerFiles();

        AskUserBus userBus = new AskUserBusStub();
        ProgressNotifier notifier = new ProgressNotifierStub();
        FsFileService fsService = new FsFileService(notifier, userBus);

        FileSystemServiceAsyncImpl fileService = new FileSystemServiceAsyncImpl(notifier, fsService, userBus);

        fileService.shiftElement(sourceDirectoryPath, destDirectoryPath, true);

        try {
            fileService.getLatch().await();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        try {
            Path sourcePath = Paths.get(sourceDirectoryPath);
            Path destInnerPath = Paths.get(destDirectoryPath).resolve(FilesHelper.SourceDir);

            Assert.assertTrue(Files.notExists(sourcePath));
            Assert.assertTrue(Files.exists(destInnerPath));
        } catch(Exception ex){
            ex.printStackTrace();
        }

        clearDirs();
    }

    //Delete folder test

    @Test
    public void deleteDirTest() {
        prepareDirectoryWithInnerFiles();

        AskUserBus userBus = new AskUserBusStub();
        ProgressNotifier notifier = new ProgressNotifierStub();
        FsFileService fsService = new FsFileService(notifier, userBus);

        FileSystemServiceAsyncImpl fileService = new FileSystemServiceAsyncImpl(notifier, fsService, userBus);

        fileService.deleteElement(sourceDirectoryPath);

        try {
            fileService.getLatch().await();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        Assert.assertTrue(Files.notExists(Paths.get(sourceDirectoryPath)));

        clearDirs();
    }

    //native directory wrapper
    @Test
    public void listDirectoryTest() {
        //prepareHugeDirectory();
        prepareDirectoryWithInnerFiles();

        AskUserBus userBus = new AskUserBusStub();
        ProgressNotifier notifier = new ProgressNotifierStub();
        FsFileService fsService = new FsFileService(notifier, userBus);

        StopWatch sw = new StopWatch();
        sw.start();

        String processedData = fsService.listDirectory(sourceDirectoryPath);

        sw.stop();
        String time = sw.toString();

        Assert.assertFalse(StringUtils.isEmpty(processedData));
        clearDirs();
    }

    @Test
    public void listDirectoryWithDirectoriesTest() {
        prepareDirectoryWithDirectories();

        AskUserBus userBus = new AskUserBusStub();
        ProgressNotifier notifier = new ProgressNotifierStub();
        FsFileService fsService = new FsFileService(notifier, userBus);

        String processedData = fsService.listDirectory(sourceDirectoryPath);

        clearDirs();
    }

    @Test
    public void testExtract(){
        String systemPropertyTmp = System.getProperty("java.io.tmpdir");
        File tmpDirFile = new File(systemPropertyTmp);
        File tmpSubdirFile = new File(tmpDirFile.getAbsolutePath() + File.separator + "crm");

        try {
            if (!tmpSubdirFile.exists()) {
                Files.createDirectory(Paths.get(tmpSubdirFile.getAbsolutePath()));
            }
        } catch(Exception ex){

        }
        String s = "";

    }

}






















