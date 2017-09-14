package org.spectrumauctions.sats.core.util.file.gson.immutablegson;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedMap;
import com.google.gson.JsonDeserializer;

/**
 * This class is heavily based on the work of Tyler Russell, https://github.com/acebaggins/gson-serializers
 * It's included in sats-core just until the very helpful repository by Tyler Russell is released on Maven
 * Central - at that point it's simply included as a dependency.
 */
public class TypeAdapters {
	
	public static final Map<Type, JsonDeserializer<?>> immutableTypeMap(){
		final Map<Type, JsonDeserializer<?>> typeMap = new HashMap<Type, JsonDeserializer<?>>(3);
		
		typeMap.put( ImmutableSet.class, new ImmutableSetDeserializer() );
		typeMap.put( ImmutableMap.class, new ImmutableMapDeserializer() );
		typeMap.put( ImmutableSortedMap.class, new ImmutableSortedMapDeserializer() );
		
		return typeMap;
	}
}
