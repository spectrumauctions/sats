package org.spectrumauctions.sats.core.util.file.gson.immutablegson;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.google.common.base.Optional;
import com.google.common.reflect.TypeParameter;
import com.google.common.reflect.TypeToken;

@SuppressWarnings ( {"unchecked", "serial"} ) 
public class Types {

	/**
	 * This class is heavily based on the work of Tyler Russell, https://github.com/acebaggins/gson-serializers
	 * It's included in sats-core just until the very helpful repository by Tyler Russell is released on Maven
	 * Central - at that point it's simply included as a dependency.
	 */
	public static <K,V> TypeToken<HashMap<K,V>> hashmapOf(final Type key, final Type value) {
		return new TypeToken<HashMap<K,V>>() {}
		.where( newTypeParameter(), typeTokenOf( key ))		
		.where( newTypeParameter(), typeTokenOf( value ));   
	}
	
	public static <E> TypeToken<Collection<E>> collectionOf(final Type type){
		return new TypeToken<Collection<E>>(){}
			.where( newTypeParameter(), typeTokenOf( type ));
	}
	
	private static <E> TypeParameter<E> newTypeParameter(){
		return new TypeParameter<E>(){};
	}
	
	private static <E> TypeToken<E> typeTokenOf(final Type type){
		return (TypeToken<E>) TypeToken.of( type );
	}
}
