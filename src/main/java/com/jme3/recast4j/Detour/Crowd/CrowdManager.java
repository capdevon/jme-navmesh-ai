package com.jme3.recast4j.Detour.Crowd;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Stream;

public class CrowdManager {

    protected CrowdUpdateType updateType = CrowdUpdateType.SEQUENTIAL;
    protected List<JmeCrowd> crowdList = new ArrayList<>();
    protected ReentrantLock lock = new ReentrantLock();

    public CrowdUpdateType getCrowdUpdateType() {
        return updateType;
    }

    public void setUpdateType(CrowdUpdateType updateType) {
        this.updateType = updateType;
    }

    public void addCrowd(JmeCrowd c) {
        try {
            lock.lock();
            crowdList.add(c);
        } finally {
            lock.unlock();
        }
    }

    public void removeCrowd(JmeCrowd c) {
        try {
            lock.lock();
            crowdList.remove(c);
        } finally {
            lock.unlock();
        }
    }

    public JmeCrowd getCrowd(int idx) {
        try {
            lock.lock();
            return crowdList.get(idx);
        } finally {
            lock.unlock();
        }
    }

    public int getNumberOfCrowds() {
        try {
            lock.lock();
            return crowdList.size();
        } finally {
            lock.unlock();
        }

    }

    public void update(float timePassed) {
        try {
            // Danger here is that someone is blocking the lock and never releasing,
            // freezing a whole application
            lock.lock();
            Stream<JmeCrowd> stream;

            switch (updateType) {
                case SEQUENTIAL:
                    stream = crowdList.stream();
                    break;

                case PARALLEL:
                    stream = crowdList.parallelStream();
                    break;

                default:
                    throw new IllegalArgumentException("Unknown Update Type");
            }

            stream.forEach(c -> c.update(timePassed));
            crowdList.forEach(JmeCrowd::applyMovements);
        } finally {
            lock.unlock();
        }
    }

    /**
     * Determines the way individual crowds are updated while being attached to a
     * Crowd Manager. Note that PARALLEL isn't the magic solution because it only
     * makes sense with many active crowds and can't fix the fact that large crowds
     * are straining the performance
     */
    public enum CrowdUpdateType {
        /**
         * Update one Crowd after another, all in the caller's thread (most probably the
         * game logic thread). You could however also have your own UI/AI Update Thread
         * there.
         */
        SEQUENTIAL,

        /**
         * Update all Crowds in Parallel using the Java 8 Stream API (So the Thread
         * Count is guessed automatically based on available Hardware). Note that this
         * is not always the best solution and in most cases might perform worse than
         * {@link #SEQUENTIAL}
         */
        PARALLEL
    }

}
