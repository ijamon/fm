package io.cnsoft.services.wrapper;

import io.cnsoft.services.fs.FileSystemServiceAsyncImpl;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.AccessController;
import java.security.PrivilegedAction;

/**
 * Created by Jamon on 11.12.2015.
 */
public class DirIndexerWrapper {

    private static DirIndexerWrapper instance;

    /*
    TODO: make thread-safe
     */
    public static DirIndexerWrapper getInstance() throws Exception{
        if(instance == null) {
            instance = new DirIndexerWrapper();
        }

        return instance;
    }

    private DirIndexerWrapper() throws Exception{
        loadLibrary();
    }

    private void loadLibrary() throws Exception {
        try {
            System.loadLibrary("DirLib");
        } catch (Exception ex) {
            throw ex;
        }
    }

    /*
    Applet impl
     */
    /*
    private void loadLibrary() {
        AccessController.doPrivileged(new PrivilegedAction<String>() {

            @Override
            public String run() {
                try {
                    System.loadLibrary("DirLib");
                } catch (Exception ex) {
                    return ex.getMessage();
                }

                return "";
            }
        });
    }
    */

    /*
    private void loadLibrary() throws Exception {
        try {
            String systemPropertyTmp = System.getProperty("java.io.tmpdir");

            File tmpSubdirFile = new File((new File(systemPropertyTmp)).getAbsolutePath() + File.separator + "crm");

            if(!tmpSubdirFile.exists()){
                Files.createDirectory(Paths.get(tmpSubdirFile.getAbsolutePath()));
            }

            File tmpDirLibDll = new File(tmpSubdirFile.getAbsolutePath() + File.separator + "DirLib.dll");
            File tmpQt5Core = new File(tmpSubdirFile.getAbsolutePath() + File.separator + "Qt5Core.dll");
            File tmpMsvcr100 = new File(tmpSubdirFile.getAbsolutePath() + File.separator + "msvcr100.dll");

            InputStream dirLibOrig = DirIndexerWrapper.class.getResourceAsStream("/DirLib.dll");
            InputStream qt5CoreOrig = DirIndexerWrapper.class.getResourceAsStream("/Qt5Core.dll");
            InputStream msvcr100Orig = DirIndexerWrapper.class.getResourceAsStream("/msvcr100.dll");

            FileUtils.copyInputStreamToFile(dirLibOrig, tmpDirLibDll);
            FileUtils.copyInputStreamToFile(qt5CoreOrig, tmpQt5Core);
            FileUtils.copyInputStreamToFile(msvcr100Orig, tmpMsvcr100);

            dirLibOrig.close();
            qt5CoreOrig.close();
            msvcr100Orig.close();

            System.load(tmpDirLibDll.getAbsolutePath());
        } catch (Exception ex) {
            throw ex;
        }
    }
    */

    public native String getFiles(String path);

}























