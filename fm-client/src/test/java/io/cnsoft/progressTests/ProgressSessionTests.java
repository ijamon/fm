package io.cnsoft.progressTests;

import io.cnsoft.notifier.progress.ProgressSession;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by Jamon on 13.03.2016.
 */
public class ProgressSessionTests {

    private final double delta = 0.001;

    @Test
    public void progressSessionIncrementPositiveTest1(){
        ProgressSession session = new ProgressSession(ProgressSession.ProgressTypes.KnownEndpoint);
        session.setMaximumProgress(100);
        session.incrementProgress(20);

        Assert.assertEquals(20.0, session.getProgressPercantage(), delta);
    }

    @Test
    public void progressSessionIncrementPositiveTest2(){
        ProgressSession session = new ProgressSession(ProgressSession.ProgressTypes.KnownEndpoint);
        session.setMaximumProgress(250);
        session.incrementProgress(25);

        Assert.assertEquals(10.0, session.getProgressPercantage(), delta);
    }

    @Test
    public void progressSessionSerialIncrementPositiveTest(){
        ProgressSession session = new ProgressSession(ProgressSession.ProgressTypes.KnownEndpoint);
        session.setMaximumProgress(250);
        session.incrementProgress(25);

        Assert.assertEquals(10.0, session.getProgressPercantage(), delta);
        session.incrementProgress(25);
        Assert.assertEquals(20.0, session.getProgressPercantage(), delta);
    }
}
