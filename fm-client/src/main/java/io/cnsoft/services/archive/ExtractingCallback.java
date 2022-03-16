package io.cnsoft.services.archive;

import io.cnsoft.Constants;
import io.cnsoft.domain.FileWrapperFabrique;
import io.cnsoft.notifier.bridge.AskUserBus;
import io.cnsoft.notifier.data.AskOverwriteData;
import io.cnsoft.notifier.progress.NotifierAbstractor;
import io.cnsoft.notifier.progress.ProgressNotifier;
import net.sf.sevenzipjbinding.*;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Created by Jamon on 08.05.2016.
 */
public class ExtractingCallback implements IArchiveExtractCallback {

    private ProgressNotifier notifier;

    private NotifierAbstractor notifierAbstractor;

    private IInArchive inArchive;

    private Path destParent;

    private FileOutputStream fso;

    private AskUserBus askUserBus;

    private Path sourceArchivePath;

    private AskOverwriteData.OverwriteOperations currentOverwriteAction = AskOverwriteData.OverwriteOperations.DEFAULT;

    public ExtractingCallback(ProgressNotifier notifier, AskUserBus askUserBus, IInArchive inArchive, Path destParent, Path sourceArchivePath) {
        this.inArchive = inArchive;
        this.destParent = destParent;
        this.notifier = notifier;
        this.notifierAbstractor = new NotifierAbstractor(notifier);
        this.askUserBus = askUserBus;
        this.sourceArchivePath = sourceArchivePath;
    }

    public ISequentialOutStream getStream(int index, ExtractAskMode extractAskMode) throws SevenZipException {
        boolean processArchivedFile;

        if (extractAskMode != ExtractAskMode.EXTRACT) {
            return null;
        }

        try {
            Path fullPath = destParent.resolve((String) inArchive.getProperty(index, PropID.PATH));

            if (Files.exists(fullPath) && currentOverwriteAction == AskOverwriteData.OverwriteOperations.DEFAULT) {
                try {
                    askUserBus.processArchiveOverwriteAsk(FileWrapperFabrique.getFileWrapper(fullPath));
                    notifier.askQuestion(askUserBus.getAskData());

                    askUserBus.awaitCurrentDataLatch();
                    currentOverwriteAction = askUserBus.getAskResult();
                    askUserBus.cleanAskData();
                } catch (Exception ex) {
                    notifier.sendError(Constants.ParallelExecutionError);
                    currentOverwriteAction = AskOverwriteData.OverwriteOperations.SKIP;
                }
            }

            if (currentOverwriteAction.doWriteSingleFile()) {
                processArchivedFile = true;
                Files.createDirectories(fullPath.getParent());

                if(Files.exists(fullPath)) {
                    Files.delete(fullPath);
                }

                Files.createFile(fullPath);

                fso = FileUtils.openOutputStream(new File(fullPath.toString()), true);
            } else {
                processArchivedFile = false;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }

        if (!currentOverwriteAction.isMultipleAction()) {
            currentOverwriteAction = AskOverwriteData.OverwriteOperations.DEFAULT;
        }

        if (processArchivedFile) {
            return new ISequentialOutStream() {

                public int write(byte[] data) throws SevenZipException {
                    try {
                        if (fso != null) {
                            fso.write(data);
                        }
                    } catch (Exception ex) {
                        notifier.sendError(ex, "ExtractingCallback");
                    }

                    return data.length;
                }
            };
        } else {
            return null;
        }
    }

    public void prepareOperation(ExtractAskMode extractAskMode) throws SevenZipException {
    }

    public void setOperationResult(ExtractOperationResult extractOperationResult) throws SevenZipException {
        if (extractOperationResult != ExtractOperationResult.OK) {
            notifier.sendError("Extracting error");
        } else {
            try {
                if (fso != null) {
                    fso.close();
                }
            } catch (Exception ex) {
                notifier.sendError(ex, "ExtractingCallback");
            }
        }
    }

    public void setCompleted(long complete) throws SevenZipException {
        notifier.updateProgress(complete);
    }

    public void setTotal(long total) throws SevenZipException {
        notifierAbstractor.impactNotifier(NotifierAbstractor.CommonNotifierTasks.EXTRACT_KNOWN_ENDPOINT, total);

        String message = String.format(Constants.DearchivingMessageTemplate, sourceArchivePath.getFileName());
        notifier.setMessage(message);
    }

}














