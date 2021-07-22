package com.jme3.recast4j.Detour.Crowd;

import java.util.ArrayList;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Stream;

public class CrowdManager {
    CrowdUpdateType updateType;
    ArrayList<Crowd> crowdList;
    ReentrantLock lock;

    public CrowdManager() {
        updateType = CrowdUpdateType.SEQUENTIAL;
        crowdList = new ArrayList<>();
        lock = new ReentrantLock();
    }

    public void setUpdateType(CrowdUpdateType updateType) {
        this.updateType = updateType;
    }

    public void addCrowd(Crowd c) {
        try {
            lock.lock();
            crowdList.add(c);
        } finally {
            lock.unlock();
        }
    }

    public void removeCrowd(Crowd c) {
        try {
            lock.lock();
            crowdList.remove(c);
        } finally {
            lock.unlock();
        }
    }

    public Crowd getCrowd(int idx) {
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
            // Danger here is that someone is blocking the lock and never releasing, freezing a whole application
            lock.lock();
            Stream<Crowd> stream;

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

            crowdList.forEach(c -> c.preUpdate(timePassed));
            stream.forEach(c -> c.update(timePassed));
            crowdList.forEach(Crowd::applyMovements);
        } finally {
            lock.unlock();
        }
    }

}
