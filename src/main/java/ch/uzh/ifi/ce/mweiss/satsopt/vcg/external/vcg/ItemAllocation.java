package ch.uzh.ifi.ce.mweiss.satsopt.vcg.external.vcg;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Map;

import ch.uzh.ifi.ce.mweiss.specval.model.Bidder;
import ch.uzh.ifi.ce.mweiss.specval.model.Bundle;
import ch.uzh.ifi.ce.mweiss.specval.model.Good;
import ch.uzh.ifi.ce.mweiss.specval.model.UnequalWorldsException;
import ch.uzh.ifi.ce.mweiss.specval.model.World;
import ch.uzh.ifi.ce.mweiss.satsopt.model.Allocation;

public final class ItemAllocation<T extends Good> implements Allocation<Bundle<T>> {

	private final World world;
	private final Map<Bidder<T>, Bundle<T>> alloc;
	private final BigDecimal totalValue;

	private ItemAllocation(ItemAllocationBuilder<T> builder) {
		this.world = builder.world;
		this.alloc = builder.alloc;
		this.totalValue = builder.totalValue;
	}

	@Override
	public Collection<Bidder<T>> getBidders() {
		return alloc.keySet();
	}

	@Override
	public Bundle<T> getAllocation(Bidder<?> bidder) {
		Bundle<T> candidate = alloc.get(bidder);
		if (candidate == null) {
			if (bidder.getWorld().equals(world)) {
				return new Bundle<>();
			} else {
				throw new UnequalWorldsException(
						"BidderWorldId: " + bidder.getWorldId() + " AllocationWorldId: " + world.getId());
			}
		}
		return candidate;
	}

	@Override
	public BigDecimal getTotalValue() {
		return totalValue;
	}

	public static final class ItemAllocationBuilder<T extends Good> {

		private World world;
		private Map<Bidder<T>, Bundle<T>> alloc;
		private BigDecimal totalValue;

		public ItemAllocationBuilder<T> withWorld(World world) {
			setWorld(world);
			return this;
		}

		public ItemAllocationBuilder<T> withAllocation(Map<Bidder<T>, Bundle<T>> alloc) {
			setAlloc(alloc);
			return this;
		}

		public ItemAllocationBuilder<T> withTotalValue(BigDecimal totalValue) {
			setTotalValue(totalValue);
			return this;
		}

		public ItemAllocation<T> build() {
			return new ItemAllocation<T>(this);
		}

		private void setWorld(World world) {
			this.world = world;
		}

		private void setAlloc(Map<Bidder<T>, Bundle<T>> alloc) {
			this.alloc = alloc;
		}

		private void setTotalValue(BigDecimal totalValue) {
			this.totalValue = totalValue;
		}
	}
}