package io.cnsoft.services.archive;

import io.cnsoft.Constants;
import io.cnsoft.domain.FileWrapperFabrique;
import io.cnsoft.helper.FilesHelper;
import io.cnsoft.notifier.bridge.AskUserBus;
import io.cnsoft.notifier.data.AskOverwriteData;
import io.cnsoft.notifier.progress.NotifierAbstractor;
import io.cnsoft.notifier.progress.ProgressNotifier;
import lombok.Getter;
import net.sf.sevenzipjbinding.*;
import net.sf.sevenzipjbinding.impl.RandomAccessFileInStream;
import net.sf.sevenzipjbinding.impl.RandomAccessFileOutStream;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static io.cnsoft.notifier.progress.NotifierAbstractor.CommonNotifierTasks.ARCHIVE_INIT_UNKNOWN_ENDPOINT;
import static io.cnsoft.notifier.progress.ProgressNotifier.OperationTypes.*;

/**
 * Created by Jamon on 07.05.2016.
 */
@SuppressWarnings("ALL")
public class ArchiveServiceAsyncImpl implements ArchiveServiceAsync {

    @Getter
    private CountDownLatch latch;

    private ProgressNotifier notifier;

    private ExecutorService executorService;

    private AskUserBus askUserBus;

    private NotifierAbstractor notifierAbstractor;

    private ArchiveFormat archiveFormat = ArchiveFormat.ZIP;

    private ProgressNotifier.OperationTypes currentOperation = ProgressNotifier.OperationTypes.DEFAULT;

    public ArchiveServiceAsyncImpl(ProgressNotifier notifier, AskUserBus askUserBus){
        this.notifier = notifier;
        this.executorService = Executors.newCachedThreadPool();
        this.askUserBus = askUserBus;
        this.notifierAbstractor = new NotifierAbstractor(notifier);
    }

    @Override
    public void archiveElement(String sourcePath, String destArchivePath) {
        String[] paths = new String[1];
        paths[0] = sourcePath;

        archiveElements(paths, destArchivePath);
    }

    @Override
    public void archiveElements(final String[] sourcePaths, final String destArchivePath) {
        latch = new CountDownLatch(1);
        if (!currentOperation.isDefault() || sourcePaths == null || sourcePaths.length == 0) return;

        executorService.submit(new Runnable() {

            @Override
            public void run() {
                AskOverwriteData.OverwriteOperations overwriteAction = AskOverwriteData.OverwriteOperations.DEFAULT;

                currentOperation = ARCHIVE_INIT;
                notifierAbstractor.impactNotifier(ARCHIVE_INIT_UNKNOWN_ENDPOINT);

                Path targetDestArchivePath = Paths.get(destArchivePath);
                try {
                    if (Files.exists(targetDestArchivePath)) {
                        askUserBus.processArchiveOverwriteAsk(FileWrapperFabrique.getFileWrapper(destArchivePath));
                        notifier.askQuestion(askUserBus.getAskData());

                        askUserBus.awaitCurrentDataLatch();
                        overwriteAction = askUserBus.getAskResult();
                        askUserBus.cleanAskData();
                    }
                } catch (Exception ex) {
                    notifier.sendError(Constants.ParallelExecutionError);
                    overwriteAction = AskOverwriteData.OverwriteOperations.SKIP;
                }

                try {
                    if (overwriteAction.doWriteSingleFile()) {
                        List<Path> paths = new ArrayList<Path>();

                        for (String path : sourcePaths) {
                            paths.addAll(FilesHelper.getAllInternalItems(path));
                        }
                        notifier.finishOperation();

                        currentOperation = ARCHIVE;

                        Path parent = Paths.get(sourcePaths[0]).getParent();

                        ArchivingCallback callback = new ArchivingCallback(notifier, paths, parent, targetDestArchivePath);
                        compress(destArchivePath, paths.size(), archiveFormat, callback);
                    }
                } catch (Exception ex) {
                    notifier.sendError(ex);
                } finally {
                    askUserBus.setCancelled(false);
                    currentOperation = DEFAULT;
                    notifier.finishOperation();

                    latch.countDown();
                }
            }
        });
    }

    @Override
    public void extractElement(final String sourceArchivePath, final String destParentPath) {
        latch = new CountDownLatch(1);
        if (!currentOperation.isDefault() || sourceArchivePath == null || destParentPath == null) return;

        executorService.submit(new Runnable() {

            @Override
            public void run() {
                try {
                    currentOperation = EXTRACT;
                    extract(sourceArchivePath, Paths.get(destParentPath));
                } catch (Exception ex) {
                    notifier.sendError(ex);
                } finally {
                    askUserBus.setCancelled(false);
                    currentOperation = DEFAULT;
                    notifier.finishOperation();

                    latch.countDown();
                }
            }
        });
    }

    private void compress(String destArchivePath, int count, ArchiveFormat archiveFormat, ArchivingCallback callback) {
        RandomAccessFile raf = null;
        IOutCreateArchive<IOutItemAllFormats> outArchive = null;

        try {
            raf = new RandomAccessFile(destArchivePath, "rw");
            outArchive = SevenZip.openOutArchive(archiveFormat);

            if (outArchive instanceof IOutFeatureSetLevel) {
                ((IOutFeatureSetLevel) outArchive).setLevel(9);
            }

            if (outArchive instanceof IOutFeatureSetMultithreading) {
                ((IOutFeatureSetMultithreading) outArchive).setThreadCount(2);
            }

            outArchive.createArchive(new RandomAccessFileOutStream(raf), count, callback);
        } catch (SevenZipException e) {
            notifier.sendError(e, "7z-Error occurs " + e.getSevenZipExceptionMessage());
        } catch (Exception e) {
            notifier.sendError(e, "Error occurs");
        } finally {
            if (outArchive != null) {
                try {
                    outArchive.close();
                } catch (IOException e) {
                    notifier.sendError(e, "Error closing archive");
                }
            }

            if (raf != null) {
                try {
                    raf.close();
                } catch (IOException e) {
                    notifier.sendError(e, "Error closing file");
                }
            }
        }
    }

    private void extract(String archivePath, Path destParent) {
        RandomAccessFile randomAccessFile = null;
        IInArchive inArchive = null;

        try {
            randomAccessFile = new RandomAccessFile(archivePath, "r");
            inArchive = SevenZip.openInArchive(null, new RandomAccessFileInStream(randomAccessFile));

            int count = inArchive.getNumberOfItems();
            List<Integer> itemsToExtract = new ArrayList<Integer>();

            for (int i = 0; i < count; i++) {
                if (!((Boolean) inArchive.getProperty(i, PropID.IS_FOLDER)).booleanValue()) {
                    itemsToExtract.add(Integer.valueOf(i));
                }
            }

            int[] items = new int[itemsToExtract.size()];
            int i = 0;

            for (Integer integer : itemsToExtract) {
                items[i++] = integer.intValue();
            }

            inArchive.extract(items, false, new ExtractingCallback(notifier, askUserBus, inArchive, destParent, Paths.get(archivePath)));
        } catch (Exception ex) {
            notifier.sendError(ex, "extract");
        } finally {
            if (inArchive != null) {
                try {
                    inArchive.close();
                } catch (Exception e) {
                    notifier.sendError(e, "Error closing archive");
                }
            }

            if (randomAccessFile != null) {
                try {
                    randomAccessFile.close();
                } catch (Exception e) {
                    notifier.sendError(e, "Error closing file");
                }
            }
        }

    }

}
