/*******************************************************************************
 *
 *    Copyright (C) 2015-2018 Jan Kristof Nidzwetzki
 *  
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *  
 *      http://www.apache.org/licenses/LICENSE-2.0
 *  
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License. 
 *    
 *******************************************************************************/
package com.github.jnidzwetzki.bitfinex.v2.test.util;

import java.util.concurrent.TimeUnit;

import org.junit.Assert;
import org.junit.Test;

import com.github.jnidzwetzki.bitfinex.v2.util.EventsInTimeslotManager;

public class EventsInTimeslotTest {

	@Test(expected=IllegalArgumentException.class)
	public void testConstruct1() {
		new EventsInTimeslotManager(-1, 10, TimeUnit.SECONDS);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testConstruct2() {
		new EventsInTimeslotManager(0, 10, TimeUnit.SECONDS);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testConstruct3() {
		new EventsInTimeslotManager(2, -10, TimeUnit.SECONDS);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testConstruct4() {
		new EventsInTimeslotManager(2, 0, TimeUnit.SECONDS);
	}
	
	@Test(timeout=1000)
	public void testNoWait1() throws InterruptedException {
		final EventsInTimeslotManager eventsInTimeslotManager = new EventsInTimeslotManager(2, 10, TimeUnit.SECONDS);
		Assert.assertEquals(0, eventsInTimeslotManager.getNumberOfEventsInTimeslot());
				
		final boolean waited = eventsInTimeslotManager.waitForNewTimeslot();
		Assert.assertFalse(waited);
	}
	
	@Test(timeout=1000)
	public void testNoWait2() throws InterruptedException {
		final EventsInTimeslotManager eventsInTimeslotManager = new EventsInTimeslotManager(2, 10, TimeUnit.SECONDS);
		eventsInTimeslotManager.recordNewEvent();
		final boolean waited = eventsInTimeslotManager.waitForNewTimeslot();
		Assert.assertFalse(waited);
	}
	
	@Test(timeout=1000)
	public void testNoWait3() throws InterruptedException {
		final EventsInTimeslotManager eventsInTimeslotManager = new EventsInTimeslotManager(2, 10, TimeUnit.SECONDS);
		eventsInTimeslotManager.recordNewEvent();
		eventsInTimeslotManager.recordNewEvent();
		
		Assert.assertEquals(2, eventsInTimeslotManager.getNumberOfEventsInTimeslot());

		final boolean waited = eventsInTimeslotManager.waitForNewTimeslot();
		Assert.assertFalse(waited);
	}
	
	@Test(timeout=15000)
	public void testWait1() throws InterruptedException {
		final EventsInTimeslotManager eventsInTimeslotManager = new EventsInTimeslotManager(2, 10, TimeUnit.SECONDS);
		eventsInTimeslotManager.recordNewEvent();
		eventsInTimeslotManager.recordNewEvent();
		eventsInTimeslotManager.recordNewEvent();
		
		Assert.assertEquals(3, eventsInTimeslotManager.getNumberOfEventsInTimeslot());

		final boolean waited = eventsInTimeslotManager.waitForNewTimeslot();
		Assert.assertTrue(waited);
	}
	
	@Test(timeout=15000)
	public void testWait2() throws InterruptedException {
		final EventsInTimeslotManager eventsInTimeslotManager = new EventsInTimeslotManager(2, 10, TimeUnit.SECONDS);
		eventsInTimeslotManager.recordNewEvent();
		eventsInTimeslotManager.recordNewEvent();
		eventsInTimeslotManager.recordNewEvent();
		
		final boolean waited1 = eventsInTimeslotManager.waitForNewTimeslot();
		Assert.assertTrue(waited1);
		
		final boolean waited2 = eventsInTimeslotManager.waitForNewTimeslot();
		Assert.assertFalse(waited2);
		
		Assert.assertEquals(0, eventsInTimeslotManager.getNumberOfEventsInTimeslot());
	}
	
	@Test(timeout=15000)
	public void testWait3() throws InterruptedException {
		final EventsInTimeslotManager eventsInTimeslotManager = new EventsInTimeslotManager(2, 10, TimeUnit.SECONDS);
		
		final boolean waited1 = eventsInTimeslotManager.waitForNewTimeslot();
		Assert.assertFalse(waited1);
		
		eventsInTimeslotManager.recordNewEvent();
		
		final boolean waited2 = eventsInTimeslotManager.waitForNewTimeslot();
		Assert.assertFalse(waited2);
		
		eventsInTimeslotManager.recordNewEvent();
		
		final boolean waited3 = eventsInTimeslotManager.waitForNewTimeslot();
		Assert.assertFalse(waited3);
		
		eventsInTimeslotManager.recordNewEvent();
		
		final boolean waited4 = eventsInTimeslotManager.waitForNewTimeslot();
		Assert.assertTrue(waited4);
		
		final boolean waited5 = eventsInTimeslotManager.waitForNewTimeslot();
		Assert.assertFalse(waited5);
	}
	
	@Test(timeout=15000)
	public void testWait4() throws InterruptedException {
		final EventsInTimeslotManager eventsInTimeslotManager = new EventsInTimeslotManager(2, 10, TimeUnit.SECONDS);
		
		final boolean waited1 = eventsInTimeslotManager.waitForNewTimeslot();
		Assert.assertFalse(waited1);
		
		eventsInTimeslotManager.recordNewEvent();
		Assert.assertEquals(1, eventsInTimeslotManager.getNumberOfEventsInTimeslot());

		Thread.sleep(5000);
		
		final boolean waited2 = eventsInTimeslotManager.waitForNewTimeslot();
		Assert.assertFalse(waited2);
		eventsInTimeslotManager.recordNewEvent();

		Assert.assertEquals(2, eventsInTimeslotManager.getNumberOfEventsInTimeslot());

		Thread.sleep(2000);
		eventsInTimeslotManager.recordNewEvent();
		
		final boolean waited4 = eventsInTimeslotManager.waitForNewTimeslot();
		Assert.assertTrue(waited4);
		Assert.assertEquals(2, eventsInTimeslotManager.getNumberOfEventsInTimeslot());
	}

}
