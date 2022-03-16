package io.cnsoft.services.fs;

import io.cnsoft.Constants;
import io.cnsoft.domain.FileWrapper;
import io.cnsoft.domain.FileWrapperDto;
import io.cnsoft.domain.FileWrapperFabrique;
import io.cnsoft.helper.FilesHelper;
import io.cnsoft.helper.JsonHelper;
import io.cnsoft.notifier.bridge.AskUserBus;
import io.cnsoft.notifier.progress.ProgressNotifier;
import io.cnsoft.services.facade.FileManagerImpl;
import io.cnsoft.services.wrapper.DirIndexerWrapper;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by Jamon on 13.03.2016.
 */
public class FsFileService {

    private DirIndexerWrapper nativeFsWrapper;

    private ProgressNotifier notifier;

    private AskUserBus userBus;

    public FsFileService(ProgressNotifier notifier, AskUserBus userBus) {
        this.notifier = notifier;
        this.userBus = userBus;

        try {
            this.nativeFsWrapper = DirIndexerWrapper.getInstance();
        } catch (Exception ex) {
            notifier.sendError(ex);
        }
    }

    /*
    JDK implementation
     */

    /*
    public String listDirectory(String path) {
        try {
            if (path == null || path.isEmpty()) {
                return null;
            }

            File pathTest = new File(path);

            if (pathTest.isDirectory()) {
                File[] innerFiles = pathTest.listFiles();
                List<FileWrapper> wrappers = new ArrayList<>();

                for(File innerFile : innerFiles){
                    wrappers.add(FileWrapperFabrique.getFileWrapper(innerFile));
                }
                //String rawData = nativeFsWrapper.getFiles(path);
                //FileWrapperDto dto = JsonHelper.gson.fromJson(rawData, FileWrapperDto.class);

                List<FileWrapper> fileWrappers = new ArrayList<>();

                if (wrappers != null && wrappers.size() > 0) {
                    for (FileWrapper fileWrapper : wrappers) {
                        if (fileWrapper.getPath().endsWith(Constants.DOUBLE_DOT) || fileWrapper.getPath().endsWith(Constants.SINGLE_DOT)) {
                            continue;
                        } else {
                            fileWrapper.calculateParameters();
                            fileWrapper.setPath(fileWrapper.getPath().replace('/', '\\'));

                            if (fileWrapper.isDirectory()) {
                                fileWrapper.setParent(path);
                            }

                            fileWrappers.add(fileWrapper);
                        }
                    }
                }

                return fileWrappers.toString();
            } else {
                return null;
            }

        } catch (Exception e) {
            notifier.sendError(e);
        }

        return null;
    }
    */

    /*
    Native impl
     */
    public String listDirectory(String path) {
        try {
            if (path == null || path.isEmpty()) {
                return null;
            }

            File pathTest = new File(path);

            if (pathTest.isDirectory()) {
                String rawData = nativeFsWrapper.getFiles(path);
                FileWrapperDto dto = JsonHelper.gson.fromJson(rawData, FileWrapperDto.class);

                List<FileWrapper> fileWrappers = new ArrayList<>();

                if (dto.data != null && dto.data.length > 0) {
                    for (FileWrapper fileWrapper : dto.data) {
                        if (fileWrapper.getPath().endsWith(Constants.DOUBLE_DOT) || fileWrapper.getPath().endsWith(Constants.SINGLE_DOT)) {
                            continue;
                        } else {
                            fileWrapper.calculateParameters();
                            fileWrapper.setPath(fileWrapper.getPath().replace('/', '\\'));

                            if (fileWrapper.isDirectory()) {
                                fileWrapper.setParent(path);
                            }

                            fileWrappers.add(fileWrapper);
                        }
                    }
                }

                return fileWrappers.toString();
            } else {
                return null;
            }

        } catch (Exception e) {
            notifier.sendError(e);
            return "";
        }
    }

    public void copyFileWithNotification(Path source, Path dest) throws Exception {
        copyFileWithNotification(source.toString(), dest.toString());
    }

    public void copyFileWithNotification(String source, String dest) throws Exception {
        InputStream in;
        OutputStream out;

        try {
            in = new FileInputStream(source);
            out = new FileOutputStream(dest);

            byte[] buf = new byte[Constants.BUFFER_LENGTH];
            int length;

            boolean cancelled = false;
            int i =0;
            while ((length = in.read(buf)) > 0 && !cancelled) {
                out.write(buf, 0, length);
                notifier.incrementProgress(length);

                cancelled = userBus.isCancelled();
            }

            in.close();
            out.close();

            BasicFileAttributes sourceAttr = Files.getFileAttributeView(Paths.get(source), BasicFileAttributeView.class).readAttributes();
            BasicFileAttributeView destAttr = Files.getFileAttributeView(Paths.get(dest), BasicFileAttributeView.class);

            destAttr.setTimes(sourceAttr.lastModifiedTime(), sourceAttr.lastAccessTime(), sourceAttr.creationTime());
        } catch (Exception e) {
            throw e;
        }
    }

    public long calculateItemsSize(String[] paths) throws Exception {
        final AtomicLong size = new AtomicLong(0);

        for (String path : paths) {
            long pathSize = calculateItemSize(Paths.get(path));
            size.addAndGet(pathSize);

            if (userBus.isCancelled()) {
                break;
            }
        }

        return size.get();
    }

    /*
    Calculate size both if path is file or directory
     */
    public long calculateItemSize(final Path path) throws Exception {
        final AtomicLong size = new AtomicLong(0);

        try {
            Files.walkFileTree(path, new SimpleFileVisitor<Path>() {

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                    size.addAndGet(attrs.size());

                    String message = String.format(Constants.CalculateSizeMessageTemplate, path.getFileName(), FilesHelper.lengthWithUnit(size.get()));
                    notifier.setMessage(message);

                    return ifCancelledResult();
                }

                @Override
                public FileVisitResult visitFileFailed(Path file, IOException exc) {
                    return ifCancelledResult();
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) {
                    return ifCancelledResult();
                }

                private FileVisitResult ifCancelledResult() {
                    if (userBus.isCancelled()) {
                        return FileVisitResult.TERMINATE;
                    } else {
                        return FileVisitResult.CONTINUE;
                    }
                }
            });
        } catch (IOException e) {
            throw new AssertionError("walkFileTree will not throw IOException if the FileVisitor does not");
        }

        return size.get();
    }

    public void deleteDirectory(Path path) throws Exception {
        deleteDirectory(path.toString());
    }

    public void deleteDirectory(String path) throws Exception {
        Path target = Paths.get(path);

        try {
            Files.walkFileTree(target, new SimpleFileVisitor<Path>() {

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    deleteFile(file.toString());

                    return ifCancelledResult();
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException ex) throws IOException {
                    if (ex == null) {
                        Files.delete(dir);

                        return ifCancelledResult();
                    } else {
                        throw ex;
                    }
                }

                private FileVisitResult ifCancelledResult() {
                    if (userBus.isCancelled()) {
                        return FileVisitResult.TERMINATE;
                    } else {
                        return FileVisitResult.CONTINUE;
                    }
                }
            });
        } catch (Exception ex) {
            throw ex;
        }
    }

    public void deleteFile(Path path) throws IOException {
        deleteFile(path.toString());
    }

    public void deleteFile(String path) throws IOException {
        try {
            Path targetPath = Paths.get(path);
            notifier.setMessage(String.format(Constants.DeleteMessageTemplate, targetPath.getFileName()));
            Files.delete(targetPath);
        } catch (IOException ex) {
            throw ex;
        }
    }

    public String mkDir(String parent, String dirName) {
        Path fullPath = Paths.get(parent).resolve(dirName);

        try {
            Files.createDirectory(fullPath);
            return fullPath.toString();
        } catch (Exception ex) {
            notifier.sendError(ex, "fsFileService");
        }

        return null;
    }

    public String getFile(String path){
        return JsonHelper.toJson(FileWrapperFabrique.getFileWrapper(path));
    }

}
















