package ch.uzh.ifi.ce.mweiss.satsopt.vcg.external.winnerdetermination;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Sets;

import ch.uzh.ifi.ce.mweiss.specval.bidlang.xor.XORBid;
import ch.uzh.ifi.ce.mweiss.specval.bidlang.xor.XORValue;
import ch.uzh.ifi.ce.mweiss.specval.model.Bidder;
import ch.uzh.ifi.ce.mweiss.specval.model.Bundle;
import ch.uzh.ifi.ce.mweiss.satsopt.model.EfficientAllocator;
import ch.uzh.ifi.ce.mweiss.satsopt.vcg.external.MockWorld;
import ch.uzh.ifi.ce.mweiss.satsopt.vcg.external.MockWorld.MockGood;
import ch.uzh.ifi.ce.mweiss.satsopt.vcg.external.domain.Auction;
import ch.uzh.ifi.ce.mweiss.satsopt.vcg.external.domain.Bids;
import ch.uzh.ifi.ce.mweiss.satsopt.vcg.external.domain.XORAllocation;

public class WinnerDeterminationTest {
	
	private MockGood A;
	private MockGood B;
	private MockGood C;
	private MockGood D;
	private MockGood E;
	
	private Map<Integer, Bidder<MockGood>> bidders;

	@Before
	public void setUp() {
		A = MockWorld.getInstance().createNewGood();
		B = MockWorld.getInstance().createNewGood();
		C = MockWorld.getInstance().createNewGood();
		D = MockWorld.getInstance().createNewGood();
		E = MockWorld.getInstance().createNewGood();
		bidders = new HashMap<>();
		MockWorld.getInstance().reset();
	}
	

	private Bidder<MockGood> bidder(int id){
		Bidder<MockGood> fromMap = bidders.get(id);
		if(fromMap == null){
			Bidder<MockGood> bidder = MockWorld.getInstance().createNewBidder();
			bidders.put((int) bidder.getId(), bidder);
			return bidder(id);
		}
		return fromMap;
	}

	@SuppressWarnings({ "deprecation", "unchecked" }) //Use deprecated double-function
	@Test
	public void testSimpleWinnerDetermination() {
		XORValue<MockGood> bid1 = new XORValue<MockGood>(new Bundle<>(A), 2);
		XORValue<MockGood> bid2 = new XORValue<MockGood>(new Bundle<>(A, B, D), 3);
		XORValue<MockGood> bid3 = new XORValue<MockGood>(new Bundle<>(B, C), 2);
		XORValue<MockGood> bid4 = new XORValue<MockGood>(new Bundle<>(C, D), 1);

		Bids<MockGood> bids = new Bids<>();
		bids.addBid(new XORBid.Builder<MockGood>(bidder(1), Sets.newHashSet(bid1)).build());
		bids.addBid(new XORBid.Builder<MockGood>(bidder(2), Sets.newHashSet(bid2)).build());
		bids.addBid(new XORBid.Builder<MockGood>(bidder(3), Sets.newHashSet(bid3)).build());
		bids.addBid(new XORBid.Builder<MockGood>(bidder(4), Sets.newHashSet(bid4)).build());

		Auction<MockGood>  auction = new Auction<>(bids, Sets.newHashSet(A, B, C, D));
		EfficientAllocator<XORAllocation<MockGood>> wd = new XORWinnerDetermination<>(
				auction);
		XORAllocation<MockGood> result = wd.calculateAllocation();
		assertTrue(result.getTotalValue().compareTo(BigDecimal.valueOf(4)) == 0);
		assertEquals(result.getAllocation(bidder(1)).getTradeValue(),2, 0.0001);
		assertEquals(result.getAllocation(bidder(2)).getTradeValue(),0, 0.01);
		assertEquals(result.getAllocation(bidder(3)).getTradeValue(),2, 0.0001);
		assertEquals(result.getAllocation(bidder(4)).getTradeValue(),0, 0.01);
		assertTrue(result.getAllocation(bidder(2)).getAcceptedBids().isEmpty());
		assertTrue(result.getAllocation(bidder(4)).getAcceptedBids().isEmpty());
	}
	
	@Test
	public void testMediumWinnerDetermination() {
		XORValue<MockGood> bid0 = new XORValue<MockGood>(new Bundle<>(C, D), 1795.51);
		XORValue<MockGood> bid1 = new XORValue<MockGood>(new Bundle<>(D), 894.644);
		XORValue<MockGood> bid2 = new XORValue<MockGood>(new Bundle<>(A, B), 1633.62);
		XORValue<MockGood> bid3 = new XORValue<MockGood>(new Bundle<>(C), 997.064);
		XORValue<MockGood> bid4 = new XORValue<MockGood>(new Bundle<>(B,C), 1751.26);
		XORValue<MockGood> bid5 = new XORValue<MockGood>(new Bundle<>(A,E), 1779.42);
		XORValue<MockGood> bid6 = new XORValue<MockGood>(new Bundle<>(B), 843.716);
		XORValue<MockGood> bid7 = new XORValue<MockGood>(new Bundle<>(E), 762.093);
		XORValue<MockGood> bid8 = new XORValue<MockGood>(new Bundle<>(A), 893.983);
		XORValue<MockGood> bid9 = new XORValue<MockGood>(new Bundle<>(A,C), 1816.69);
		Bids<MockGood> bids = new Bids<>();
		bids.addBid(new XORBid.Builder<MockGood>(bidder(0), Sets.newHashSet(bid0)).build());
		bids.addBid(new XORBid.Builder<MockGood>(bidder(1), Sets.newHashSet(bid1)).build());
		bids.addBid(new XORBid.Builder<MockGood>(bidder(2), Sets.newHashSet(bid2)).build());
		bids.addBid(new XORBid.Builder<MockGood>(bidder(3), Sets.newHashSet(bid3)).build());
		bids.addBid(new XORBid.Builder<MockGood>(bidder(4), Sets.newHashSet(bid4)).build());
		bids.addBid(new XORBid.Builder<MockGood>(bidder(5), Sets.newHashSet(bid5)).build());
		bids.addBid(new XORBid.Builder<MockGood>(bidder(6), Sets.newHashSet(bid6)).build());
		bids.addBid(new XORBid.Builder<MockGood>(bidder(7), Sets.newHashSet(bid7)).build());
		bids.addBid(new XORBid.Builder<MockGood>(bidder(8), Sets.newHashSet(bid8)).build());
		bids.addBid(new XORBid.Builder<MockGood>(bidder(9), Sets.newHashSet(bid9)).build());

		Auction<MockGood> auction = new Auction<>(bids, Sets.newHashSet(A, B, C, D,E));
		EfficientAllocator<XORAllocation<MockGood>> wd = new XORWinnerDetermination<>(
				auction);
		XORAllocation<MockGood> result = wd.calculateAllocation();
		assertEquals(result.getTotalAllocationValue(), 4514.844, 0);
		assertEquals(result.getAllocation(bidder(0)).getTradeValue(),0, 0.0001);
		assertEquals(result.getAllocation(bidder(1)).getTradeValue(),894.644, 0.0001);
		assertEquals(result.getAllocation(bidder(2)).getTradeValue(),0, 0.0001);
		assertEquals(result.getAllocation(bidder(4)).getTradeValue(),0, 0.0001);

	}
}
