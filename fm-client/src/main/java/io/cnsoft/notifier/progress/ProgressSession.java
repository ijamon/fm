package io.cnsoft.notifier.progress;

import lombok.*;
import org.apache.commons.math3.util.Precision;

/**
 * Created by Jamon on 01.03.2016.
 */
public class ProgressSession {

    @Getter
    private ProgressTypes type;

    @Getter
    @Setter
    private long maximumProgress;

    @Getter
    private long progess;

    @Getter
    private double progressPercantage;

    public ProgressSession(ProgressTypes type) {
        this.type = type;
    }

    @AllArgsConstructor
    @Getter
    public enum ProgressTypes {
        KnownEndpoint ("knownEndpoint"),

        UnknownEndpoint ("unknownEndpoint");

        private String id;;
    }

    public void incrementProgress(long increment) {
        progess += increment;

        recalculateProgress();
    }

    public void updateProgress(long currentProgress){
        progess = currentProgress;

        recalculateProgress();
    }

    private void recalculateProgress() {
        progressPercantage = 100.0 * ((double) progess / maximumProgress);
        progressPercantage = Precision.round(progressPercantage, 2);
    }

}