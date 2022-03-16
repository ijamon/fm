package io.cnsoft.notifier.data;

import io.cnsoft.domain.FileWrapper;
import lombok.*;

/**
 * Created by Jamon on 13.03.2016.
 */
@NoArgsConstructor
public class AskOverwriteData extends AskData<AskOverwriteData.OverwriteOperations> {

    @Getter
    @Setter
    private FileWrapper sourceFile;

    @Getter
    @Setter
    private FileWrapper destinationFile;

    @Getter
    private QuestionViewMode viewMode;

    public AskOverwriteData(FileWrapper sourceFile) {
        setSourceFile(sourceFile);
        setDestinationFile(null);
        setQuestion(Questions.OVERWRITE_CONFIRMATION);

        viewMode = QuestionViewMode.OneFile;
    }

    public AskOverwriteData(FileWrapper sourceFile, FileWrapper destinationFile) {
        setSourceFile(sourceFile);
        setDestinationFile(destinationFile);
        setQuestion(Questions.OVERWRITE_CONFIRMATION);

        viewMode = QuestionViewMode.TwoFiles;
    }

    @AllArgsConstructor
    @Getter
    public enum OverwriteOperations {

        DEFAULT("default"),

        OVERWRITE("overwrite"),

        OVERWRITE_ALL("overwrite_all"),

        SKIP("skip"),

        SKIP_ALL("skip_all"),

        CANCEL("cancel");

        String id;

        public boolean doWriteSingleFile() {
            return this == DEFAULT || this == OVERWRITE || this == OVERWRITE_ALL;
        }

        public boolean isDefault() {
            return this == DEFAULT;
        }

        public boolean isMultipleAction() {
            return this == OVERWRITE_ALL || this == SKIP_ALL;
        }
    }

    @AllArgsConstructor
    @Getter
    public enum QuestionViewMode {

        TwoFiles("twoFiles"),

        OneFile("oneFile");

        String id;
    }
}
