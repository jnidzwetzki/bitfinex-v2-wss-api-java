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
package com.github.jnidzwetzki.bitfinex.v2.test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.junit.Assert;
import org.junit.Test;

import com.github.jnidzwetzki.bitfinex.v2.entity.BitfinexApiKeyPermissions;


public class BitfinexKeyPermissions {
	
	@Test
	public void testAllKey() throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		final Class<BitfinexApiKeyPermissions> keyClass = BitfinexApiKeyPermissions.class;
        final Method methods[] = keyClass.getDeclaredMethods();
        
        for (int i = 0; i < methods.length; i++) {
        		final Method method = methods[i];
        		if(method.getName().startsWith("is")) {
                final Boolean retobj = (Boolean) method.invoke(BitfinexApiKeyPermissions.ALL_PERMISSIONS, new Object[] {});
                Assert.assertEquals(true, retobj);
        		}
        }
	}
	
	@Test
	public void testHashCode() {
		Assert.assertNotEquals(BitfinexApiKeyPermissions.ALL_PERMISSIONS.hashCode(), 
				BitfinexApiKeyPermissions.NO_PERMISSIONS.hashCode());
	}
}
