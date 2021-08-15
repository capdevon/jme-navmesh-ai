package com.jme3.recast4j.geom;

import java.util.concurrent.TimeUnit;
import org.recast4j.recast.RecastBuilder.RecastBuilderProgressListener;

/**
 * Listener for build process of tiled builds.
 */
public class NavMeshBuilderProgressListener implements RecastBuilderProgressListener {

    private long time = System.nanoTime();
    private long elapsedTime;
    private long avBuildTime;
    private long estTotalTime;
    private long estTimeRemain;
    private long buildTimeNano;
    private long elapsedTimeHr;
    private long elapsedTimeMin;
    private long elapsedTimeSec;
    private long totalTimeHr;
    private long totalTimeMin;
    private long totalTimeSec;
    private long timeRemainHr;
    private long timeRemainMin;
    private long timeRemainSec;

    @Override
    public void onProgress(int completed, int total) {
        elapsedTime += System.nanoTime() - time;
        avBuildTime = elapsedTime/(long)completed;
        estTotalTime = avBuildTime * (long)total;
        estTimeRemain = estTotalTime - elapsedTime;

        buildTimeNano = TimeUnit.MILLISECONDS.convert(avBuildTime, TimeUnit.NANOSECONDS);
        System.out.printf("Completed %d[%d] Average [%dms] ", completed, total, buildTimeNano);

        elapsedTimeHr = TimeUnit.HOURS.convert(elapsedTime, TimeUnit.NANOSECONDS) % 24;
        elapsedTimeMin = TimeUnit.MINUTES.convert(elapsedTime, TimeUnit.NANOSECONDS) % 60;
        elapsedTimeSec = TimeUnit.SECONDS.convert(elapsedTime, TimeUnit.NANOSECONDS) % 60;
        System.out.printf("Elapsed Time [%02d:%02d:%02d] ", elapsedTimeHr, elapsedTimeMin, elapsedTimeSec);

        totalTimeHr = TimeUnit.HOURS.convert(estTotalTime, TimeUnit.NANOSECONDS) % 24;
        totalTimeMin = TimeUnit.MINUTES.convert(estTotalTime, TimeUnit.NANOSECONDS) % 60;
        totalTimeSec = TimeUnit.SECONDS.convert(estTotalTime, TimeUnit.NANOSECONDS) % 60;
        System.out.printf("Estimated Total [%02d:%02d:%02d] ", totalTimeHr, totalTimeMin, totalTimeSec);

        timeRemainHr = TimeUnit.HOURS.convert(estTimeRemain, TimeUnit.NANOSECONDS) % 24;
        timeRemainMin = TimeUnit.MINUTES.convert(estTimeRemain, TimeUnit.NANOSECONDS) % 60;
        timeRemainSec = TimeUnit.SECONDS.convert(estTimeRemain, TimeUnit.NANOSECONDS) % 60;
        System.out.printf("Remaining Time [%02d:%02d:%02d]%n", timeRemainHr, timeRemainMin, timeRemainSec);

        //reset time
        time = System.nanoTime();
    }

}
