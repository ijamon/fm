package io.cnsoft.notifier.progress;

import lombok.AllArgsConstructor;

/**
 * Created by Jamon on 02.05.2016.
 */
@AllArgsConstructor
public class NotifierAbstractor {

    private ProgressNotifier notifier;

    public void impactNotifier(CommonNotifierTasks task) {
        switch (task){
            case CALC_SIZE_UNKNOWN_ENDPOINT:
                notifier.initializeNotifier(ProgressNotifier.OperationTypes.CALCULATE_SIZE, ProgressSession.ProgressTypes.UnknownEndpoint);
                break;
            case DELETE_UNKNOWN_ENDPOINT:
                notifier.initializeNotifier(ProgressNotifier.OperationTypes.DELETE, ProgressSession.ProgressTypes.UnknownEndpoint);
                break;
            case ARCHIVE_INIT_UNKNOWN_ENDPOINT:
                notifier.initializeNotifier(ProgressNotifier.OperationTypes.ARCHIVE_INIT, ProgressSession.ProgressTypes.UnknownEndpoint);
                break;
            case RENAME_UNKNOWN_ENDPOINT:
                notifier.initializeNotifier(ProgressNotifier.OperationTypes.RENAME, ProgressSession.ProgressTypes.UnknownEndpoint);
                break;
        }

    }

    public void impactNotifier(CommonNotifierTasks task, long value) {
        switch (task){
            case COPY_KNOWN_ENDPOINT:
                notifier.initializeNotifier(ProgressNotifier.OperationTypes.COPY, ProgressSession.ProgressTypes.KnownEndpoint);
                notifier.setMaximumProgress(value);
                break;
            case MOVE_KNOWN_ENDPOINT:
                notifier.initializeNotifier(ProgressNotifier.OperationTypes.MOVE, ProgressSession.ProgressTypes.KnownEndpoint);
                notifier.setMaximumProgress(value);
                break;
            case ARCHIVE_KNOWN_ENDPOINT:
                notifier.initializeNotifier(ProgressNotifier.OperationTypes.ARCHIVE, ProgressSession.ProgressTypes.KnownEndpoint);
                notifier.setMaximumProgress(value);
                break;
            case EXTRACT_KNOWN_ENDPOINT:
                notifier.initializeNotifier(ProgressNotifier.OperationTypes.EXTRACT, ProgressSession.ProgressTypes.KnownEndpoint);
                notifier.setMaximumProgress(value);
                break;
        }
    }

    public enum CommonNotifierTasks {
        CALC_SIZE_UNKNOWN_ENDPOINT,

        DELETE_UNKNOWN_ENDPOINT,

        COPY_KNOWN_ENDPOINT,

        MOVE_KNOWN_ENDPOINT,

        ARCHIVE_INIT_UNKNOWN_ENDPOINT,

        ARCHIVE_KNOWN_ENDPOINT,

        EXTRACT_KNOWN_ENDPOINT,

        RENAME_UNKNOWN_ENDPOINT,
    }

}
