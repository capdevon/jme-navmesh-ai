/*
recast4j copyright (c) 2021 Piotr Piastucki piotr@jtilia.org

This software is provided 'as-is', without any express or implied
warranty.  In no event will the authors be held liable for any damages
arising from the use of this software.
Permission is granted to anyone to use this software for any purpose,
including commercial applications, and to alter it and redistribute it
freely, subject to the following restrictions:
1. The origin of this software must not be misrepresented; you must not
 claim that you wrote the original software. If you use this software
 in a product, an acknowledgment in the product documentation would be
 appreciated but is not required.
2. Altered source versions must be plainly marked as such, and must not be
 misrepresented as being the original software.
3. This notice may not be removed or altered from any source distribution.
 */
package com.jme3.recast4j.geom;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.recast4j.recast.Context;

/**
 *
 * @author capdevon
 */
public class Telemetry extends Context {

    private final ThreadLocal<Map<String, AtomicLong>> timerStart = ThreadLocal.withInitial(HashMap::new);
    private final Map<String, AtomicLong> timerAccum = new ConcurrentHashMap<>();

    @Override
    public void startTimer(String name) {
        timerStart.get().put(name, new AtomicLong(System.nanoTime()));
    }

    @Override
    public void stopTimer(String name) {
        if (timerStart.get().get(name) != null) {
            long delta = System.nanoTime() - timerStart.get().get(name).get();
            timerAccum.computeIfAbsent(name, __ -> new AtomicLong()).addAndGet(delta);
        }
    }

    @Override
    public void warn(String string) {
        System.err.println(string);
    }

    public void print() {
        timerAccum.forEach((n, v) -> System.out.println(n + ": " + v.get() / 1000000));
    }

}
