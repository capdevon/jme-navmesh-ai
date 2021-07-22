/*
 * The MIT License
 *
 * Copyright 2019 .
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 *
 * MODELS/DUNE.J3O:
 * Converted from http://quadropolis.us/node/2584 [Public Domain according to the Tags of this Map]
 */

package com.jme3.recast4j.demo;

import java.util.concurrent.TimeUnit;
import org.recast4j.recast.RecastBuilder.RecastBuilderProgressListener;

/**
 * Listener for build process of tiled builds.
 */
public class MyBuilderProgressListener implements RecastBuilderProgressListener {

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
