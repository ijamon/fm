package io.cnsoft.services.archive;

import io.cnsoft.Constants;
import io.cnsoft.notifier.progress.NotifierAbstractor;
import io.cnsoft.notifier.progress.ProgressNotifier;
import net.sf.sevenzipjbinding.IOutCreateCallback;
import net.sf.sevenzipjbinding.IOutItemAllFormats;
import net.sf.sevenzipjbinding.ISequentialInStream;
import net.sf.sevenzipjbinding.SevenZipException;
import net.sf.sevenzipjbinding.impl.OutItemFactory;
import net.sf.sevenzipjbinding.util.ByteArrayStream;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Created by Jamon on 07.05.2016.
 */
public class ArchivingCallback implements IOutCreateCallback<IOutItemAllFormats> {

    private ProgressNotifier notifier;

    private NotifierAbstractor notifierAbstractor;

    private List<Path> items;

    private Path parent;

    private Path destArchivePath;

    public ArchivingCallback(ProgressNotifier notifier, List<Path> items, Path parent, Path destArchivePath) {
        this.notifier = notifier;
        this.notifierAbstractor = new NotifierAbstractor(notifier);
        this.items = items;
        this.parent = parent;
        this.destArchivePath = destArchivePath;
    }

    public void setOperationResult(boolean operationResultOk) throws SevenZipException {
    }

    public void setTotal(long total) throws SevenZipException {
        notifierAbstractor.impactNotifier(NotifierAbstractor.CommonNotifierTasks.ARCHIVE_KNOWN_ENDPOINT, total);

        String message = String.format(Constants.ArchivingMessageTemplate, destArchivePath.getFileName());
        notifier.setMessage(message);
    }

    public void setCompleted(long complete) throws SevenZipException {
        notifier.updateProgress(complete);
    }

    public IOutItemAllFormats getItemInformation(int index, OutItemFactory<IOutItemAllFormats> outItemFactory) {
        IOutItemAllFormats item = outItemFactory.createOutItem();

        Path pathItem = items.get(index);
        String filePath = pathItem.toString();

        if (Files.isDirectory(pathItem)) {
            item.setPropertyIsDir(true);
        } else {
            item.setDataSize(FileUtils.sizeOf(new File(filePath)));
        }

        String relPath = parent.relativize(pathItem).toString();
        item.setPropertyPath(relPath);

        return item;
    }

    public ISequentialInStream getStream(int index) throws SevenZipException {
        if (Files.isDirectory(items.get(index))) {
            return null;
        }

        byte[] content = null;

        try {
            content = FileUtils.readFileToByteArray(new File(items.get(index).toString()));
        } catch(Exception ex){
            notifier.sendError(ex, "ArchivingCallback");
        }

        if(content != null){
            return new ByteArrayStream(content, true);
        } else {
            return null;
        }
    }

}
