package org.spectrumauctions.sats.core.util.file.gson;

import java.lang.reflect.Type;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.marketdesignresearch.mechlib.core.Good;
import org.marketdesignresearch.mechlib.core.allocationlimits.AllocationLimit;
import org.marketdesignresearch.mechlib.core.allocationlimits.BundleSizeAllocationLimit;
import org.marketdesignresearch.mechlib.core.allocationlimits.BundleSizeAndGoodAllocationLimit;
import org.marketdesignresearch.mechlib.core.allocationlimits.GoodAllocationLimit;
import org.marketdesignresearch.mechlib.core.allocationlimits.AllocationLimit.NoAllocationLimit;
import org.spectrumauctions.sats.core.model.GenericGood;
import org.spectrumauctions.sats.core.model.GenericWorld;
import org.spectrumauctions.sats.core.model.World;
import org.spectrumauctions.sats.core.model.mrvm.MRVMWorld;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import lombok.Getter;
import lombok.Setter;

public class AllocationLimitAdapter implements JsonSerializer<AllocationLimit>, JsonDeserializer<AllocationLimit>{
	
	private Map<String,Good> goodMap;
	
	private static enum Kind {
		No,
		BundleSizeAllocationLimit,
		BundleSizeAndGoodAllocationLimit,
		GoodAllocationLimit
	}
	
	private static Map<Class<? extends AllocationLimit>,Function<AllocationLimit, SerializedAllocationLimit>> serializers = Map.of(
			NoAllocationLimit.class, AllocationLimitAdapter::serializeNoAllocationLimit,
			BundleSizeAllocationLimit.class, AllocationLimitAdapter::serializeBundleSizeAllocationLimit,
			BundleSizeAndGoodAllocationLimit.class, AllocationLimitAdapter::serializeBundleSizeAndGoodAllocationLimit,
			GoodAllocationLimit.class, AllocationLimitAdapter::serializeGoodAllocationLimit);
	
	private static Map<Kind,BiFunction<SerializedAllocationLimit, Map<String,Good>, AllocationLimit>> deserializers = Map.of(
			Kind.No,AllocationLimitAdapter::deserializeNoAllocationLimit,
			Kind.BundleSizeAllocationLimit,AllocationLimitAdapter::deserializeBundleSizeAllocationLimit,
			Kind.BundleSizeAndGoodAllocationLimit,AllocationLimitAdapter::deserializeBundleSizeAndGoodAllocationLimit,
			Kind.GoodAllocationLimit,AllocationLimitAdapter::deserializeGoodAllocationLimit
			);
			
	
	@Override
	public AllocationLimit deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
			throws JsonParseException {
		SerializedAllocationLimit sal = context.deserialize(json, SerializedAllocationLimit.class);
		return deserializers.get(sal.getKind()).apply(sal, goodMap);
	}

	@Override
	public JsonElement serialize(AllocationLimit src, Type typeOfSrc, JsonSerializationContext context) {
		SerializedAllocationLimit sal = serializers.get(src.getClass()).apply(src);
		return context.serialize(sal);
	}
	
	public static class SerializedAllocationLimit {
		@Getter
		@Setter
		private Kind kind;
		@Getter
		@Setter
		private List<String> good1;
		@Getter
		@Setter
		private List<String> good2;
		@Getter
		@Setter
		private int numberOfItems;
	}
	
	private static SerializedAllocationLimit serializeNoAllocationLimit(AllocationLimit limit) {
		SerializedAllocationLimit sal = new SerializedAllocationLimit();
		sal.setKind(Kind.No);
		return sal;
	}
	
	private static SerializedAllocationLimit serializeBundleSizeAllocationLimit(AllocationLimit limit) {
		BundleSizeAllocationLimit bundleSizeAllocationLimit = (BundleSizeAllocationLimit) limit;
		SerializedAllocationLimit sal = new SerializedAllocationLimit();
		sal.setKind(Kind.BundleSizeAllocationLimit);
		sal.setGood1(bundleSizeAllocationLimit.getGoods().stream().map(Good::getUuid).map(UUID::toString).collect(Collectors.toList()));
		sal.setNumberOfItems(bundleSizeAllocationLimit.getBundleSizeLimit());
		return sal;
	}
	
	private static SerializedAllocationLimit serializeBundleSizeAndGoodAllocationLimit(AllocationLimit limit) {
		BundleSizeAndGoodAllocationLimit bundleSizeAllocationLimit = (BundleSizeAndGoodAllocationLimit) limit;
		SerializedAllocationLimit sal = new SerializedAllocationLimit();
		sal.setKind(Kind.BundleSizeAndGoodAllocationLimit);
		sal.setGood1(bundleSizeAllocationLimit.getDomainGoods().stream().map(Good::getUuid).map(UUID::toString).collect(Collectors.toList()));
		sal.setGood2(bundleSizeAllocationLimit.getLimitedGoods().stream().map(Good::getUuid).map(UUID::toString).collect(Collectors.toList()));
		sal.setNumberOfItems(bundleSizeAllocationLimit.getBundleSizeLimit());
		return sal;
	}
	
	private static SerializedAllocationLimit serializeGoodAllocationLimit(AllocationLimit limit) {
		GoodAllocationLimit bundleSizeAllocationLimit = (GoodAllocationLimit) limit;
		SerializedAllocationLimit sal = new SerializedAllocationLimit();
		sal.setKind(Kind.GoodAllocationLimit);
		sal.setGood1(bundleSizeAllocationLimit.getDomainGoods().stream().map(Good::getUuid).map(UUID::toString).collect(Collectors.toList()));
		sal.setGood2(bundleSizeAllocationLimit.getLimitedGoods().stream().map(Good::getUuid).map(UUID::toString).collect(Collectors.toList()));
		return sal;
	}
	
	private static AllocationLimit deserializeNoAllocationLimit(SerializedAllocationLimit sal, Map<String,Good> goods) {
		return AllocationLimit.NO;
	}
	
	private static AllocationLimit deserializeBundleSizeAllocationLimit(SerializedAllocationLimit sal, Map<String,Good> goods) {
		return new BundleSizeAllocationLimit(sal.getNumberOfItems(), deserializeGoodList(sal.getGood1(), goods));
	}
	
	private static AllocationLimit deserializeBundleSizeAndGoodAllocationLimit(SerializedAllocationLimit sal, Map<String,Good> goods) {
		return new BundleSizeAndGoodAllocationLimit(sal.getNumberOfItems(), deserializeGoodList(sal.getGood1(), goods), deserializeGoodList(sal.getGood2(), goods));
	}

	private static AllocationLimit deserializeGoodAllocationLimit(SerializedAllocationLimit sal, Map<String,Good> goods) {
		return new GoodAllocationLimit(deserializeGoodList(sal.getGood1(), goods), deserializeGoodList(sal.getGood2(), goods));
	}
	
	private static List<? extends Good> deserializeGoodList(List<String> uuids, Map<String,Good> goods) {
		return uuids.stream().map(u -> goods.get(u)).collect(Collectors.toList());
	}

	public void setWorld(World world) {
		goodMap = new LinkedHashMap<>();
		for(Good g : world.getLicenses()) {
			goodMap.put(g.getUuid().toString(), g);
		}
		
		if(world instanceof GenericWorld) {
			for(Good g : ((GenericWorld)world).getAllGenericDefinitions()) {
				goodMap.put(g.getUuid().toString(), g);
			}
		}
	}

}
