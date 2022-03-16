package io.cnsoft.fsTests;

import io.cnsoft.services.fs.IconsRepository;
import org.apache.commons.lang3.StringUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

/**
 * Created by Jamon on 12.05.2016.
 */
public class IconsTests {

    private String sourceFilePath;

    private IconsRepository iconsRepository;

    @Before
    public void prepareTest() {
        String dir = System.getProperty("user.dir");

        sourceFilePath = dir + File.separator + "file.exe";
        File sourceFile = new File(sourceFilePath);

        iconsRepository = new IconsRepository();

        try {
            sourceFile.createNewFile();
        } catch(Exception ex){
            ex.printStackTrace();
        }

    }

    @After
    public void clearAfterTest() {
        if (!StringUtils.isEmpty(sourceFilePath)) {
            try {
                File file = new File(sourceFilePath);
                file.delete();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    @Test
    public void iconTest(){
        String iconBase64 = iconsRepository.getIconForPath(sourceFilePath);
        Assert.assertFalse(StringUtils.isEmpty(iconBase64));
    }

}
