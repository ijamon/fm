package io.cnsoft.services.facade;

import io.cnsoft.domain.ExceptionStub;
import io.cnsoft.notifier.bridge.AskUserBus;
import io.cnsoft.notifier.bridge.AskUserBusImpl;
import io.cnsoft.notifier.progress.ProgressNotifier;
import io.cnsoft.notifier.progress.ProgressNotifierImpl;
import io.cnsoft.services.FileManagerFacade;
import io.cnsoft.services.archive.ArchiveServiceAsync;
import io.cnsoft.services.archive.ArchiveServiceAsyncImpl;
import io.cnsoft.services.fs.*;
import io.cnsoft.services.fs.IconsRepository;

import java.io.InputStream;

/**
 * Created by Jamon on 09.05.2016.
 */
public class FileManagerImpl implements FileManagerFacade {

    private ProgressNotifier notifier;
    private AskUserBus askUserBus;

    private DrivesServices drivesFsService;
    private DesktopFileService desktopFsService;
    private FsFileService fsFileService;
    private FileSystemServiceAsync fileSystemServiceAsync;
    private IconsRepository iconsRepository;
    private ArchiveServiceAsync archiveService;

    public FileManagerImpl(){
        notifier = new ProgressNotifierImpl();
        askUserBus = new AskUserBusImpl();

        drivesFsService = new DrivesServices();
        desktopFsService = new DesktopFileService(notifier);
        fsFileService = new FsFileService(notifier, askUserBus);
        fileSystemServiceAsync = new FileSystemServiceAsyncImpl(notifier, fsFileService, askUserBus);
        iconsRepository = new IconsRepository();
        archiveService = new ArchiveServiceAsyncImpl(notifier, askUserBus);
    }

    @Override
    public void open(String path) {
        desktopFsService.open(path);
    }

    @Override
    public void edit(String path) {
        desktopFsService.edit(path);
    }

    @Override
    public String getAllDrivesNames() {
        return drivesFsService.getAllDrivesNames();
    }

    @Override
    public String getCrmDriveName() {
        return drivesFsService.getCrmDriveName();
    }

    @Override
    public String listDirectory(String path) {
        return fsFileService.listDirectory(path);
    }

    @Override
    public String mkDir(String path, String name) {
        return fsFileService.mkDir(path, name);
    }

    @Override
    public String getIcon(String path) {
        return iconsRepository.getIconForPath(path);
    }

    @Override
    public String getFile(String path) {
        return fsFileService.getFile(path);
    }

    @Override
    public void shiftElement(String sourcePath, String destDirectory, boolean removeSource) {
        fileSystemServiceAsync.shiftElement(sourcePath, destDirectory, removeSource);
    }

    @Override
    public void shiftElements(String[] sourcePaths, String destDirectory, boolean removeSource) {
        fileSystemServiceAsync.shiftElements(sourcePaths, destDirectory, removeSource);
    }

    @Override
    public void deleteElement(String targetPath) {
        fileSystemServiceAsync.deleteElement(targetPath);
    }

    @Override
    public void deleteElements(String[] targetPaths) {
        fileSystemServiceAsync.deleteElements(targetPaths);
    }

    @Override
    public void rename(String sourcePath, String name) {
        fileSystemServiceAsync.rename(sourcePath, name);
    }

    @Override
    public void answerCurrentQuestion(String data) {
        askUserBus.answerCurrentQuestion(data);
    }

    @Override
    public void cancelCurrentOperation() {
        askUserBus.setCancelled(true);
    }

    @Override
    public void archiveElement(String sourcePath, String destArchivePath) {
        archiveService.archiveElement(sourcePath, destArchivePath);
    }

    @Override
    public void archiveElements(String[] sourcePaths, String destArchivePath) {
        archiveService.archiveElements(sourcePaths, destArchivePath);
    }

    @Override
    public void extractElement(String sourceArchivePath, String destParentPath) {
        archiveService.extractElement(sourceArchivePath, destParentPath);
    }

}
