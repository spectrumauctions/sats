package org.spectrumauctions.sats.core.util.file.gson.immutablegson;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Map;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

/**
 * This class is heavily based on the work of Tyler Russell, https://github.com/acebaggins/gson-serializers
 * It's included in sats-core just until the very helpful repository by Tyler Russell is released on Maven
 * Central - at that point it's simply included as a dependency.
 */
public class ImmutableMapDeserializer implements JsonDeserializer<ImmutableMap<?,?>> {

	public ImmutableMap<?,?> deserialize(final JsonElement json, final Type type,
										 final JsonDeserializationContext context ) throws JsonParseException {

		final Type[] typeArguments = ((ParameterizedType) type).getActualTypeArguments();
		final Type parametrizedType = Types.hashmapOf(typeArguments[0], typeArguments[1]).getType();

		final Map<?, ?> map = context.deserialize(json, parametrizedType);

		return ImmutableMap.copyOf( map );
	}
}