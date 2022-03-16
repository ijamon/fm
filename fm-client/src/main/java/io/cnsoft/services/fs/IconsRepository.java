package io.cnsoft.services.fs;

import io.cnsoft.Constants;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileSystemView;
import javax.xml.bind.DatatypeConverter;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.Map;

/**
 * Created by Jamon on 12.05.2016.
 */
public class IconsRepository {

    private FileSystemView view;

    private static Map<String, String> icons;

    public IconsRepository() {
        view = FileSystemView.getFileSystemView();
        icons = new java.util.HashMap();
    }

    public String getIconForPath(String path) {
        if (StringUtils.isEmpty(path)) return Constants.FileBlankIconEncoded;

        File file = new File(path);
        if (!file.exists()) return Constants.FileBlankIconEncoded;

        if (file.isDirectory()) {
            return Constants.DirectoryIconEncoded;
        } else {
            return getIconForFile(new File(path));
        }
    }

    public String getIconForFile(File file) {
        if (file == null) return Constants.FileBlankIconEncoded;

        String extension = FilenameUtils.getExtension(file.getAbsolutePath());

        if (icons.containsKey(extension)) {
            return icons.get(extension);
        } else {

            if(StringUtils.isEmpty(extension)){
                return Constants.FileBlankIconEncoded;
            } else {
                String result = getIconForFileInternal(file);
                icons.put(extension, result);

                return result;
            }
        }

    }

    private String getIconForFileInternal(File file){
        ImageIcon icon = (ImageIcon) view.getSystemIcon(file);
        BufferedImage im = (BufferedImage) icon.getImage();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try {
            ImageIO.write(im, "png", baos);
            String result = DatatypeConverter.printBase64Binary(baos.toByteArray());

            if(StringUtils.isEmpty(result)){
                result = Constants.FileBlankIconEncoded;
            }

            baos.close();
            return result;
        } catch (Exception ex) {
            return Constants.FileBlankIconEncoded;
        }
    }

}




















