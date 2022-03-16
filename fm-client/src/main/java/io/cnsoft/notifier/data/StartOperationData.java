package io.cnsoft.notifier.data;

import io.cnsoft.notifier.progress.ProgressNotifier;
import io.cnsoft.notifier.progress.ProgressSession;
import lombok.Getter;

/**
 * Created by Jamon on 08.03.2016.
 */
public class StartOperationData {

    @Getter
    private ProgressNotifier.OperationTypes operationType;

    @Getter
    private ProgressSession.ProgressTypes progressType;

    public StartOperationData(ProgressNotifier.OperationTypes operationType, ProgressSession.ProgressTypes progressType) {
        this.operationType = operationType;
        this.progressType = progressType;
    }

}
