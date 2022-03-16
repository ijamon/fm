package io.cnsoft.notifier.data;

import lombok.Getter;

/**
 * Created by Jamon on 07.03.2016.
 */
public class UpdateProgressData extends BaseData {

    @Getter
    private double percantage;

    public UpdateProgressData(String message){
        super(message);
    }

    public UpdateProgressData(double percantage, String message){
        super(message);

        this.percantage = percantage;
    }
}
