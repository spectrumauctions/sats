package ch.uzh.ifi.ce.mweiss.sats.opt.vcg.external;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Set;

import org.mockito.*;

import ch.uzh.ifi.ce.mweiss.sats.core.bidlang.BiddingLanguage;
import ch.uzh.ifi.ce.mweiss.sats.core.model.Bidder;
import ch.uzh.ifi.ce.mweiss.sats.core.model.BidderSetup;
import ch.uzh.ifi.ce.mweiss.sats.core.model.Bundle;
import ch.uzh.ifi.ce.mweiss.sats.core.model.Good;
import ch.uzh.ifi.ce.mweiss.sats.core.model.UnsupportedBiddingLanguageException;
import ch.uzh.ifi.ce.mweiss.sats.core.model.World;

@SuppressWarnings("serial") //No Serialization
public class MockWorld extends World {
	
	int numberOfGoods;
	int numberOfBidders;
	
	static BidderSetup setup = Mockito.mock(BidderSetup.class);
	
	private static MockWorld INSTANCE = null;
	
	public static MockWorld getInstance(){
		if(INSTANCE == null){
			INSTANCE = new MockWorld();
		}
		return INSTANCE;
	}
	
	public void reset(){
		numberOfGoods = 0;
		numberOfBidders = 0;
	}

	private MockWorld() {
		super("mockWorld");
		Mockito.when(setup.getSetupName()).thenReturn("Mock");
		reset();
	}

	@Override
	public int getNumberOfGoods() {
		return numberOfGoods;
	}
	
	public MockGood createNewGood(){
		return new MockGood(numberOfGoods++, this.getId());
	}
	
	public MockBidder createNewBidder(){
		return new MockBidder(0, numberOfBidders++, this.getId());
	}

	@Override
	public Set<? extends Good> getLicenses() {
		throw new UnsupportedOperationException("Not implemented for MockWorld");
	}

	@Override
	public Collection<? extends Bidder<?>> restorePopulation(long populationId) {
		throw new UnsupportedOperationException("Not implemented for MockWorld");
	}

	@Override
	public void refreshFieldBackReferences() {
	}
	
	public class MockGood extends Good{
		protected MockGood(long id, long worldId) {
			super(id, worldId);
		}

		@Override
		public World getWorld() {
			return MockWorld.this;
		}		
	}
	
	public class MockBidder extends Bidder<MockGood>{

		
		protected MockBidder(long population, long id, long worldId) {
			super(setup, population, id, worldId);
		}

		@Override
		public BigDecimal calculateValue(Bundle<MockGood> bundle) {
			throw new UnsupportedOperationException("Not supported in mock");
		}

		@Override
		public <T extends BiddingLanguage> T getValueFunction(Class<T> type, long seed)
				throws UnsupportedBiddingLanguageException {
			throw new UnsupportedOperationException("Not supported in mock");
		}

		@Override
		public World getWorld() {
			return MockWorld.this;
		}

		@Override
		public void refreshReference(World world) {
			throw new UnsupportedOperationException("Not supported in mock");
			
		}
		
	}
	

}
