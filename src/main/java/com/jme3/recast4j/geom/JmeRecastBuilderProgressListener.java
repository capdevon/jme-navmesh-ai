package com.jme3.recast4j.geom;

import java.util.concurrent.TimeUnit;
import org.recast4j.recast.RecastBuilder.RecastBuilderProgressListener;

/**
 * Listener for build process of tiled builds.
 */
public class JmeRecastBuilderProgressListener implements RecastBuilderProgressListener {

    private long time = System.nanoTime();
    private long elapsedTime;

    @Override
    public void onProgress(int completed, int total) {
        elapsedTime += System.nanoTime() - time;
        long avBuildTime = elapsedTime / completed;
        long estTotalTime = avBuildTime * total;
        long estTimeRemain = estTotalTime - elapsedTime;

        long buildTimeNano = TimeUnit.MILLISECONDS.convert(avBuildTime, TimeUnit.NANOSECONDS);
        System.out.printf("Completed %d[%d] Average [%dms] ", completed, total, buildTimeNano);

        long elapsedTimeHr = TimeUnit.HOURS.convert(elapsedTime, TimeUnit.NANOSECONDS) % 24;
        long elapsedTimeMin = TimeUnit.MINUTES.convert(elapsedTime, TimeUnit.NANOSECONDS) % 60;
        long elapsedTimeSec = TimeUnit.SECONDS.convert(elapsedTime, TimeUnit.NANOSECONDS) % 60;
        System.out.printf("Elapsed Time [%02d:%02d:%02d] ", elapsedTimeHr, elapsedTimeMin, elapsedTimeSec);

        long totalTimeHr = TimeUnit.HOURS.convert(estTotalTime, TimeUnit.NANOSECONDS) % 24;
        long totalTimeMin = TimeUnit.MINUTES.convert(estTotalTime, TimeUnit.NANOSECONDS) % 60;
        long totalTimeSec = TimeUnit.SECONDS.convert(estTotalTime, TimeUnit.NANOSECONDS) % 60;
        System.out.printf("Estimated Total [%02d:%02d:%02d] ", totalTimeHr, totalTimeMin, totalTimeSec);

        long timeRemainHr = TimeUnit.HOURS.convert(estTimeRemain, TimeUnit.NANOSECONDS) % 24;
        long timeRemainMin = TimeUnit.MINUTES.convert(estTimeRemain, TimeUnit.NANOSECONDS) % 60;
        long timeRemainSec = TimeUnit.SECONDS.convert(estTimeRemain, TimeUnit.NANOSECONDS) % 60;
        System.out.printf("Remaining Time [%02d:%02d:%02d]%n", timeRemainHr, timeRemainMin, timeRemainSec);

        // reset time
        time = System.nanoTime();
    }

}
