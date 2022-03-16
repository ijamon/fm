package io.cnsoft.services.fs;

import io.cnsoft.Constants;
import io.cnsoft.domain.FileWrapperFabrique;
import io.cnsoft.helper.FilesHelper;
import io.cnsoft.notifier.bridge.AskUserBus;
import io.cnsoft.notifier.data.AskOverwriteData;
import io.cnsoft.notifier.progress.NotifierAbstractor;
import io.cnsoft.notifier.progress.ProgressNotifier;
import io.cnsoft.services.facade.FileManagerImpl;
import lombok.Getter;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.EnumSet;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static io.cnsoft.notifier.progress.NotifierAbstractor.CommonNotifierTasks.*;
import static io.cnsoft.notifier.progress.ProgressNotifier.OperationTypes.*;
import static java.nio.file.FileVisitResult.CONTINUE;
import static java.nio.file.FileVisitResult.TERMINATE;

/**
 * Created by Jamon on 01.03.2016.
 */
public class FileSystemServiceAsyncImpl implements FileSystemServiceAsync {

    @Getter
    private CountDownLatch latch;

    private ProgressNotifier notifier;

    private ExecutorService executorService;

    private FsFileService fsService;

    private AskUserBus askUserBus;

    private NotifierAbstractor notifierAbstractor;


    private ProgressNotifier.OperationTypes currentOperation = ProgressNotifier.OperationTypes.DEFAULT;

    private AskOverwriteData.OverwriteOperations currentOverwriteAction = AskOverwriteData.OverwriteOperations.DEFAULT;


    public FileSystemServiceAsyncImpl(ProgressNotifier notifier, FsFileService fsService, AskUserBus askUserBus){
        this.notifier = notifier;
        this.executorService = Executors.newCachedThreadPool();
        this.fsService = fsService;
        this.askUserBus = askUserBus;
        this.notifierAbstractor = new NotifierAbstractor(notifier);
    }

    @Override
    public void shiftElement(String sourcePath, String destDirectory, boolean removeSource) {
        String[] paths = new String[1];
        paths[0] = sourcePath;

        shiftElements(paths, destDirectory, removeSource);
    }

    @Override
    public void shiftElements(final String[] sourcePaths, final String destDirectory, final boolean removeSource){
        latch = new CountDownLatch(1);
        if (!currentOperation.isDefault()) return;

        executorService.submit(new Runnable() {

            @Override
            public void run() {
                try {
                    Path destDirectoryPath = Paths.get(destDirectory);

                    currentOperation = CALCULATE_SIZE;
                    notifierAbstractor.impactNotifier(CALC_SIZE_UNKNOWN_ENDPOINT);

                    long itemsSize = fsService.calculateItemsSize(sourcePaths);

                    notifier.finishOperation();

                    if (removeSource) {
                        currentOperation = MOVE;
                        notifierAbstractor.impactNotifier(MOVE_KNOWN_ENDPOINT, itemsSize);
                    } else {
                        currentOperation = COPY;
                        notifierAbstractor.impactNotifier(COPY_KNOWN_ENDPOINT, itemsSize);
                    }

                    for (String source : sourcePaths) {
                        if (currentOverwriteAction == AskOverwriteData.OverwriteOperations.SKIP_ALL || askUserBus.isCancelled()) {
                            break;
                        } else {
                            FilesHelper.checkPathsForElementCopy(source, destDirectory);
                            Path sourcePath = Paths.get(source);

                            if (Files.isDirectory(sourcePath)) {
                                shiftDirectory(sourcePath, destDirectoryPath, removeSource);
                            } else {
                                shiftFile(sourcePath, destDirectoryPath, removeSource);
                            }
                        }
                    }
                } catch (Exception ex) {
                    notifier.sendError(ex);
                } finally {
                    askUserBus.setCancelled(false);
                    currentOperation = DEFAULT;
                    currentOverwriteAction = AskOverwriteData.OverwriteOperations.DEFAULT;
                    notifier.finishOperation();

                    latch.countDown();
                }
            }
        });
    }

    @Override
    public void deleteElement(String targetPath) {
        String[] targetPaths = new String[1];
        targetPaths[0] = targetPath;

        deleteElements(targetPaths);
    }

    @Override
    public void deleteElements(final String[] targetPaths) {
        if (!currentOperation.isDefault()) return;

        latch = new CountDownLatch(1);

        executorService.submit(new Runnable() {

            @Override
            public void run() {
                try {
                    currentOperation = CALCULATE_SIZE;
                    notifierAbstractor.impactNotifier(DELETE_UNKNOWN_ENDPOINT);

                    for(String target : targetPaths) {
                        if(askUserBus.isCancelled()){
                            break;
                        } else {
                            Path targetPath = Paths.get(target);

                            if(Files.isDirectory(targetPath)) {
                                fsService.deleteDirectory(targetPath);
                            } else {
                                fsService.deleteFile(targetPath);
                            }
                        }
                    }

                } catch (Exception ex) {
                    notifier.sendError(ex);
                } finally {
                    askUserBus.setCancelled(false);
                    currentOperation = DEFAULT;
                    currentOverwriteAction = AskOverwriteData.OverwriteOperations.DEFAULT;
                    notifier.finishOperation();

                    latch.countDown();
                }
            }
        });
    }

    public void rename(final String source, final String targetName) {
        if (StringUtils.isEmpty(source) || StringUtils.isEmpty(targetName)) return;

        latch = new CountDownLatch(1);

        executorService.submit(new Runnable() {

            @Override
            public void run() {
                try {
                    currentOperation = RENAME;
                    notifierAbstractor.impactNotifier(RENAME_UNKNOWN_ENDPOINT);

                    Path sourcePath = Paths.get(source);
                    String target = sourcePath.getParent().resolve(targetName).toString();

                    if (Files.isDirectory(sourcePath)) {
                        FileUtils.moveDirectory(FileUtils.getFile(sourcePath.toString()), FileUtils.getFile(target));
                    } else {
                        FileUtils.moveFile(FileUtils.getFile(sourcePath.toString()), FileUtils.getFile(target));
                    }
                } catch (Exception ex) {
                    notifier.sendError(ex);
                } finally {
                    currentOperation = DEFAULT;
                    notifier.finishOperation();

                    latch.countDown();
                }
            }
        });
    }

    private void shiftFile(Path sourceFilePath, Path destDirectoryPath, boolean removeSource) {
        final Path destFilePath = destDirectoryPath.resolve(sourceFilePath.getFileName());

        if(Files.exists(destFilePath) && currentOverwriteAction == AskOverwriteData.OverwriteOperations.DEFAULT) {
            try {
                askUserBus.processOverwriteAsk(FileWrapperFabrique.getFileWrapper(sourceFilePath), FileWrapperFabrique.getFileWrapper((destFilePath)));
                notifier.askQuestion(askUserBus.getAskData());

                askUserBus.awaitCurrentDataLatch();
                currentOverwriteAction = askUserBus.getAskResult();
                askUserBus.cleanAskData();
            } catch (Exception ex) {
                notifier.sendError(Constants.ParallelExecutionError);
                currentOverwriteAction = AskOverwriteData.OverwriteOperations.SKIP;
            }
        }

        if(currentOverwriteAction.doWriteSingleFile()) {
            try {
                String message = "";

                if(removeSource) {
                    message = String.format(Constants.MoveMessageTemplate, sourceFilePath.getFileName());
                } else {
                    message = String.format(Constants.CopyMessageTemplate, sourceFilePath.getFileName());
                }

                notifier.setMessage(message);
                fsService.copyFileWithNotification(sourceFilePath, destFilePath);

                if (removeSource && !askUserBus.isCancelled()) {
                    fsService.deleteFile(sourceFilePath);
                }
            } catch (Exception ex) {
                notifier.sendError(ex);
            }
        }

        if(!currentOverwriteAction.isMultipleAction()) {
            currentOverwriteAction = AskOverwriteData.OverwriteOperations.DEFAULT;
        }
    }

    private void shiftDirectory(final Path source, final Path target, final boolean removeSource) {
        try {
            Files.walkFileTree(source, EnumSet.of(FileVisitOption.FOLLOW_LINKS), Integer.MAX_VALUE,
                    new SimpleFileVisitor<Path>() {

                        @Override
                        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                            Path relativeSource = source.getParent().relativize(dir);
                            Path targetdir = target.resolve(relativeSource);

                            try {
                                if (!Files.exists(targetdir)) {
                                    Files.createDirectory(targetdir);
                                }
                            } catch (FileAlreadyExistsException e) {
                                if (!Files.isDirectory(targetdir))
                                    throw e;
                            }

                            if (askUserBus.isCancelled()) {
                                return TERMINATE;
                            } else {
                                return CONTINUE;
                            }
                        }

                        @Override
                        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                            Path relativeSource = source.getParent().relativize(file);
                            Path targetPath = target.resolve(relativeSource);

                            if (currentOverwriteAction == AskOverwriteData.OverwriteOperations.SKIP_ALL || askUserBus.isCancelled()) {
                                return TERMINATE;
                            } else {
                                shiftFile(file, targetPath.getParent(), removeSource);
                                return CONTINUE;
                            }
                        }

                        @Override
                        public FileVisitResult postVisitDirectory(Path dir, IOException ex) throws IOException {
                            if (currentOverwriteAction == AskOverwriteData.OverwriteOperations.SKIP_ALL || askUserBus.isCancelled()) {
                                return TERMINATE;
                            } else {
                                if(removeSource && ex == null){
                                    Files.delete(dir);
                                }

                                return CONTINUE;
                            }
                        }
                    });
        } catch(Exception ex){
            notifier.sendError(ex);
        }
    }

}
