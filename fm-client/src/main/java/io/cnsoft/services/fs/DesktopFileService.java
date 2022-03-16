package io.cnsoft.services.fs;

import io.cnsoft.notifier.progress.ProgressNotifier;

import java.awt.*;
import java.io.File;

/**
 * Created by Jamon on 13.03.2016.
 */
public class DesktopFileService {

    private Desktop desktop;

    private ProgressNotifier notifier;

    public DesktopFileService(ProgressNotifier notifier){
        this.desktop = Desktop.getDesktop();
        this.notifier = notifier;
    }

    public boolean open(final String path) {
        return desktopAction(Desktop.Action.OPEN, path);
    }

    public boolean edit(String path) {
        return desktopAction(Desktop.Action.EDIT, path);
    }

    public boolean print(String path) {
        return desktopAction(Desktop.Action.PRINT, path);
    }

    private boolean desktopAction(final Desktop.Action action, final String path) {
        if (path == null) {
            return false;
        }

        try {
            if (!isSupported(action)) return false;

            switch (action) {
                case OPEN:
                    desktop.open(new File(path));
                    break;
                case EDIT:
                    desktop.edit(new File(path));
                    break;
                case PRINT:
                    desktop.print(new File(path));
                    break;
            }

            return true;
        } catch (Exception e) {
            //TODO: add server notification

            notifier.sendError(e, "DesktopFileService");
            return false;
        }
    }

    private boolean isSupported(Desktop.Action action) {
        return Desktop.getDesktop().isSupported(action);
    }
}
