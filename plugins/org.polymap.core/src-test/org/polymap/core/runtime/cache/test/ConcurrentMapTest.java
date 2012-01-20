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
package org.polymap.core.runtime.cache.test;

import java.util.Random;

import org.polymap.core.runtime.Timer;
import org.polymap.core.runtime.cache.Cache;
import org.polymap.core.runtime.cache.CacheConfig;
import org.polymap.core.runtime.cache.CacheLoader;
import org.polymap.core.runtime.cache.CacheManager;

import junit.framework.TestCase;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class ConcurrentMapTest
        extends TestCase {

    Cache<Integer,byte[]>        cache;
    

    protected void setUp() throws Exception {
        cache = CacheManager.instance().newCache( "Test", CacheConfig.DEFAULT );    
    }

    
    protected void tearDown() throws Exception {
        cache.dispose();
    }

    
    public void tstEviction() throws InterruptedException {
        int count = 0;
        while (true) {
            System.out.println( "adding 1000 to " + cache.size() );
            for (int i=0; i<1000; i++) {
                cache.putIfAbsent( new Integer(count++), new byte[1024] );
            }
            Thread.sleep( 100 );
        }            
    }
    
    
    public void tstCreation() throws Exception {
        int count = 0;
        while (true) {
            System.out.println( "adding 1000 to " + cache.size() );
            for (int i=0; i<1000; i++) {
                cache.get( new Integer(count++), new CacheLoader<Integer,byte[]>() {
                    public byte[] load( Integer key ) throws Exception {
                        return new byte[1024];
                    }
                    public int size() throws Exception {
                        return 1024;
                    }
                });
            }
            Thread.sleep( 200 );
        }        
    }
    
    
    public void testRandom() throws Exception {
        
        for (int t=0; t<3; t++) {

            Thread thread = new Thread( "" + t ) {
                public void run() {
                    try {
                        Random random = new Random();
                        while (true) {
                            System.out.println( Thread.currentThread().getName() + ": processing 1000 to " + cache.size() );
                            Timer timer = new Timer();

                            for (int i=0; i<1000; i++) {
                                Integer key = (int)(random.nextGaussian() * 50000);

                                if (random.nextBoolean()) {
                                    // access
                                    cache.get( key, new CacheLoader<Integer,byte[]>() {
                                        public byte[] load( Integer _key ) throws Exception {
                                            return new byte[1024];
                                        }
                                        public int size() throws Exception {
                                            return 1024;
                                        }
                                    });
                                }
                                else {
                                    // remove
                                    cache.remove( key );
                                }
                            }
                            System.out.println( Thread.currentThread().getName() + "    time: " + timer.elapsedTime() + "ms" );
                            Thread.sleep( random.nextInt( 100 ) + 100 );
                        }
                    }
                    catch (Exception e) {
                        throw new RuntimeException( e );
                    }
                }
            };
            thread.start();
        }
        Thread.sleep( 1000*1000 );
    }

}
