package io.cnsoft.notifier.progress;

import io.cnsoft.notifier.data.AskData;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Created by Jamon on 01.03.2016.
 */
public interface ProgressNotifier {

    void initializeNotifier(OperationTypes operationType, ProgressSession.ProgressTypes progressType);

    void setMaximumProgress(long maximum);

    void setMessage(String message);

    void updateProgress(long currentProgress);

    void updateProgress(long currentProgress, String message);

    void incrementProgress(long increment);

    void incrementProgress(long increment, String message);

    void finishOperation();

    void sendError(Exception exception);

    void sendError(Exception exception, String message);

    void sendError(String message);

    void askQuestion(AskData data);

    /*
    Возможные операции
     */
    @AllArgsConstructor
    @Getter
    enum OperationTypes {
        DEFAULT("",""),

        COPY ("copy", "Копирование"),

        MOVE ("move", "Перемещение"),

        DELETE ("delete", "Удаление"),

        ARCHIVE ("archive", "Архивирование"),

        EXTRACT ("extract", "Разархивирование"),

        ARCHIVE_INIT ("archiveInit", "Подготовка к архивированию"),

        CALCULATE_SIZE ("calculateSize", "Подсчет размера"),

        RENAME ("rename", "Переименование...");

        private String operationId;

        private String operationView;

        public boolean isDefault(){
            return this.equals(OperationTypes.DEFAULT);
        }
    }

}
















