/* 
 * polymap.org
 * Copyright 2012, Polymap GmbH. All rights reserved.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 */
package org.polymap.core.runtime.cache;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.polymap.core.runtime.Timer;
import org.polymap.core.runtime.cache.ConcurrentMapCache.CacheEntry;

/**
 * In-memory cache manager. The caches are backed by {@link ConcurrentHashMap}s.
 * Memory usage is periodically checked by the {@link MemoryChecker} thread. If
 * memory is low then the LRU entries ({@link #DEFAULT_EVICTION_SIZE}) from all
 * caches are evicted. The check interval is calculated from the amount of free
 * memory.
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
final class ConcurrentMapCacheManager
        extends CacheManager {

    private static Log log = LogFactory.getLog( ConcurrentMapCacheManager.class );
    
    private static final int                        DEFAULT_EVICTION_SIZE = 1000;
    
    private static final ConcurrentMapCacheManager  instance = new ConcurrentMapCacheManager();
    
    
    public static ConcurrentMapCacheManager instance() {
        return instance;
    }
    
    
    // instance *******************************************
    
    private Thread                          checkerThread;

    /**
     * Keep the currentPeriod and caches stable while checker runs. Use fair policy
     * so that subsequent read operations don't prevent checkr/eviction to run.
     */
    private ReadWriteLock                   checkerLock = new ReentrantReadWriteLock( true );
    
    private List<ConcurrentMapCache>        caches;
    
    private AccessPeriod                    currentPeriod;
    
    /* Modified in the checker thread -> not synchronization needed */
    private LinkedList<AccessPeriod>        periods;
    
    private volatile int                    periodCount = 1;
    

    protected ConcurrentMapCacheManager() {
        // start thread
        checkerThread = new Thread( new MemoryChecker(), "CacheMemoryChecker" );
        checkerThread.setPriority( Thread.MAX_PRIORITY );
        checkerThread.start();
    
        caches = Collections.synchronizedList( new ArrayList( 64 ) );

        currentPeriod = new AccessPeriod();
        periods = new LinkedList();
    }
    

    public <K, V> Cache<K, V> newCache( String name, CacheConfig config ) {
        ConcurrentMapCache result = new ConcurrentMapCache( this, name, config );
        caches.add( result );
        return result;
    }

    
    void disposeCache( ConcurrentMapCache cache ) {
        caches.remove( cache );
    }

    
    void countAccess( CacheEntry entry ) {
        if (entry != null) {
            currentPeriod.countAccess( entry );
        }
    }
    

    /*
     * 
     */
    final class AccessPeriod {
        
        protected final int         started = periodCount++;
        
        /** Number of elements that were 'last accessed' in this period. */
        private volatile int        count = 0;
        
        
        public void countAccess( CacheEntry entry ) {
            if (entry.accessPeriod == this) {
                return;
            }
            try {
                checkerLock.readLock().lock();
                synchronized (entry) {
                    if (entry.accessPeriod != null) {
                        entry.accessPeriod.countRemove( entry );
                    }
                    entry.accessPeriod = this;
                    ++count;
                }
            }
            finally {
                checkerLock.readLock().unlock();
            }
        }
        
        public void countRemove( CacheEntry entry ) {
            try {
                checkerLock.readLock().lock();
                synchronized (entry) {
                    assert entry.accessPeriod == this;                
                    entry.accessPeriod = null;

                    if (--count < 0) {
                        log.warn( "Cache: AccessPeriod.count < 0: " + count );
                    }
                }    
            }
            finally {
                checkerLock.readLock().unlock();
            }
        }
    }
    
    
    protected void evict() {
        Timer timer = new Timer();
        
        int evictionTime = -1;
        int candidates = 0;
        int periodsChecked = 0;

        // find evictionTime for the last DEFAULT_EVICTION_SIZE elements
        Iterator<AccessPeriod> it = periods.iterator();
        while (it.hasNext() && candidates < DEFAULT_EVICTION_SIZE) {
            AccessPeriod period = it.next();
            log.info( "        checking period: count=" + period.count + ", started=" + period.started );
            assert evictionTime < period.started;
            evictionTime = period.started;
            candidates += period.count;
            periodsChecked++;
        }

        // empty periods are removed periodically by the checker

        // total evicted elements
        int evicted = 0;
        // older periods tend to contain low number elements after some
        // eviction cycles; if more than 75% of all periods are to be evicted
        // then evict all elements of that periods (instead of just DEFAULT)
        int target = DEFAULT_EVICTION_SIZE;
        if (periodsChecked > (periods.size()*0.75)) {
            target = candidates;
            log.info( "    FULL eviction of " + candidates + " elements..." );
        }
        
        for (final ConcurrentMapCache cache : caches) {

            Iterator<CacheEntry> it2 = cache.cacheEntries().iterator();
            while (it2.hasNext() && evicted < target) {
                CacheEntry entry = it2.next();
                if (entry.accessPeriod != null
                        && entry.accessPeriod.started <= evictionTime) {

                    it2.remove();
                    entry.accessPeriod.countRemove( entry );
                    evicted++;
                }
            }

            if (evicted >= target) {
                break;
            }
        }
        if (evicted == 0) {
            System.gc();
        }
        log.info( "    EVICTED: elements: " + evicted + "; eviction time: " + evictionTime + " (" + timer.elapsedTime() + "ms)" );
    }
    
    
    /*
     * 
     */
    class MemoryChecker
            implements Runnable {

        private MemoryMXBean                    memBean;
        
        
        public MemoryChecker() {
            memBean = ManagementFactory.getMemoryMXBean() ;
        }

        public void run() {
            while (true) {
                long nextSleep = 0;
                try {
                    checkerLock.writeLock().lock();
                    checkCurrentPeriod();
                    checkEmptyPeriods();
                    nextSleep = checkMemory();
                }
                catch (Throwable e) {
                    log.warn( e );
                }
                finally {
                    checkerLock.writeLock().unlock();
                }
                
                try {
                    if (nextSleep > 0) {
                        Thread.sleep( nextSleep );
                    }
                }
                catch (InterruptedException e) {
                }
            }
        }

        protected void checkCurrentPeriod() {
            if (currentPeriod.count > DEFAULT_EVICTION_SIZE) {
                periods.addLast( currentPeriod );
                currentPeriod = new AccessPeriod();
                log.info( "New AccessPeriod: " + currentPeriod.started + "; periods: " + periods.size() );
            }
        }

        protected void checkEmptyPeriods() {
            // remove 'empty' periods
            // XXX merge periods so that num of periods does not exceeds limit?
            for (Iterator<AccessPeriod> it = periods.iterator(); it.hasNext(); ) {
                AccessPeriod period = it.next();
                if (period.count <= 0) {
                    it.remove();
                    log.info( "    Empty period removed: " + period.started + ", periods:" + periods.size() );
                }
            }
        }
        
        protected long checkMemory() {
            Timer timer = new Timer();
            MemoryUsage heap = memBean.getHeapMemoryUsage();
            MemoryUsage nonHeap = memBean.getNonHeapMemoryUsage();

            long memUsedGoal = (long)(heap.getMax() * 0.80);
            long maxFree = heap.getMax() - memUsedGoal;
            long free = heap.getMax() - heap.getUsed();
            float ratio = (float)free / (float)maxFree;
            //log.info( "    memory free ratio: " + ratio );
            // sleep no longer than 100ms
            long sleep = (long)Math.min( 1000, 1000*ratio );

            if (sleep < 500) {
                System.gc();
                sleep = 0;
            }
            
            if (heap.getUsed() > memUsedGoal) {
                log.info( "Starting eviction..." );
                log.info( String.format( "    Heap: used: %d, max: %d", heap.getUsed(), heap.getMax() ) );
                evict();
            }
            return sleep;
        }

        
//        private void fullSortEviction() {
//            Timer timer = new Timer();
//            
//            // XXX memory allocation!?
//            SortedSet<EvictionCandidate> evictionSet = new TreeSet();
//            int accessThreshold = 0;  //Integer.MAX_VALUE;
//            
//            for (ConcurrentMapCache cache : caches) {
//                
//                Iterable<Map.Entry<Object,CacheEntry>> entries = cache.entries();
//                for (Map.Entry<Object,CacheEntry> entry : entries) {
//
//                    if (evictionSet.size() < DEFAULT_EVICTION_SIZE
//                            || entry.getValue().accessed() < accessThreshold) {
//                        
//                        // find last entry and remove
//                        EvictionCandidate last = null;
//                        if (evictionSet.size() >= DEFAULT_EVICTION_SIZE) {
//                            last = evictionSet.last();
//                            evictionSet.remove( last );
//                            
//                            accessThreshold = last.entry.accessed();
//                        }
//                        else {
//                            accessThreshold = Math.max( accessThreshold, entry.getValue().accessed() );
//                        }
//                        
//                        evictionSet.add( last != null
//                                ? last.reUse( cache, entry.getValue(), entry.getKey() )
//                                : new EvictionCandidate( cache, entry.getValue(), entry.getKey() ) );
//                    }
//                }
//            }
//            
//            for (EvictionCandidate candidate : evictionSet) {
//                // remove from cache
//                candidate.cache.remove( candidate.key );
//                // fire eviction event
//                candidate.cache.fireEvictionEvent( candidate.key, candidate.entry.value() );
//            }
//            
//            //System.gc();
//            
//            log.info( "    Evicted: " + evictionSet.size() + ", accessThreshold: " + accessThreshold + " (" + timer.elapsedTime() + "ms)" );
//        }
        
    }
    
    
//    /*
//     * 
//     */
//    class EvictionCandidate
//            implements Comparable {
//       
//        ConcurrentMapCache          cache;
//        
//        CacheEntry                  entry;
//        
//        Object                      key;
//
//        
//        EvictionCandidate( ConcurrentMapCache cache, CacheEntry entry, Object key ) {
//            this.cache = cache;
//            this.entry = entry;
//            this.key = key;
//        }
//
//        @SuppressWarnings("hiding")
//        public EvictionCandidate reUse( ConcurrentMapCache cache, CacheEntry entry, Object key ) {
//            this.cache = cache;
//            this.entry = entry;
//            this.key = key;
//            return this;
//        }
//
//        public void clear() {
//            cache = null;
//            entry = null;
//            key = null;
//        }
//        
//        public int compareTo( Object obj ) {
//            EvictionCandidate other = (EvictionCandidate)obj;
//            return other.entry != null
//                    ? entry.accessed() - other.entry.accessed()
//                    : 0;
//        }
//
////        public int hashCode() {
////            final int prime = 31;
////            int result = 1;
////            result = prime * result + ((cache == null) ? 0 : cache.hashCode());
////            result = prime * result + ((key == null) ? 0 : key.hashCode());
////            return result;
////        }
//
//        public boolean equals( Object obj ) {
//            if (obj == this) {
//                return true;
//            }
//            if (obj instanceof EvictionCandidate) {
//                EvictionCandidate other = (EvictionCandidate)obj;
//                return cache == other.cache
//                        && key.equals( other.key );
//            }
//            return false;
//        }
//
//    }

}
