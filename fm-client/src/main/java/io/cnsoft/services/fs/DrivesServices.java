package io.cnsoft.services.fs;

import io.cnsoft.Constants;
import io.cnsoft.domain.FileWrapperFabrique;
import io.cnsoft.helper.JsonHelper;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

/**
 * Created by Jamon on 13.03.2016.
 */
public class DrivesServices {

    /**
     * @return all drives list
     */
    public String getAllDrivesNames() {
        return JsonHelper.toJson(FileWrapperFabrique.getFileWrappers(Arrays.asList(File.listRoots())));
    }

    /**
     * @return drive for CRM (signed with SIGN_FILE)
     */
    public String getCrmDriveName() {
        for (File root : File.listRoots()) {
            try {
                Path crmPath = Paths.get(root.getPath()).resolve(Constants.SIGN_FILE);

                if (Files.exists(crmPath)) {
                    return JsonHelper.toJson(FileWrapperFabrique.getFileWrapper(root));
                }
            } catch (Exception e) {
                throw e;
            }
        }

        return null;
    }
}
