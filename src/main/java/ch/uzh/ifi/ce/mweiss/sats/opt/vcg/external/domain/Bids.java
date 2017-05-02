package ch.uzh.ifi.ce.mweiss.sats.opt.vcg.external.domain;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import ch.uzh.ifi.ce.mweiss.sats.core.bidlang.xor.XORBid;
import ch.uzh.ifi.ce.mweiss.sats.core.model.Bidder;
import ch.uzh.ifi.ce.mweiss.sats.core.model.Good;
import ch.uzh.ifi.ce.mweiss.sats.core.model.UnequalWorldsException;
import ch.uzh.ifi.ce.mweiss.sats.core.model.World;

/**
 * This class represents the aggregated bids of the auction, one Bid per Bidder.
 *
 */
public class Bids<T extends Good> implements Iterable<XORBid<T>> {
	private final Map<Bidder<T>, XORBid<T>> bids;


	public Bids(){
		super();
		bids = new HashMap<>();
	}
	
	
	/**
	 * Creates a new non-empty bids instance
	 * @param bids the bids to be added
	 * @throws UnequalWorldsException if not all passed bids are from the same world
	 */
	public Bids(Collection<XORBid<T>> bids) {
		this();
		for(XORBid<T> bid : bids){
			this.bids.put(bid.getBidder(), bid);
		}
		validateAll();
	}



	/**
	 * @return An unmodifiable map of all {@link XORBid} of this, with their {@link XORBid#getBidder()} as key.
	 */
	public Map<Bidder<T>, XORBid<T>> getBidMap() {
		return Collections.unmodifiableMap(bids);
	}
	
	/**
	 * Adds a new {@link XORBid} to this instance
	 * @param bid The bid to add
	 * @return true iff there was no bid previously stored for this bidder
	 * @throws UnequalWorldsException if the bid is not from the same world as the previously added ones
	 */
	public boolean addBid(XORBid<T> bid){
		validate(bid);
		return bids.put(bid.getBidder(), bid) == null;
	}
	
	/**
	 * Validates that the passed bid is from the same world as a (potentially) already stored {@link XORBid}
	 * @throws UnequalWorldsException if validation fails
	 */
	private void validate(XORBid<T> bid){
		if(!bids.isEmpty()){
			if(!bids.keySet().iterator().next().getWorld()
					.equals(bid.getBidder().getWorld())){
				throw new UnequalWorldsException();
			}
		}
		
	}
	/**
	 * Validates that all stored bids are from the same world
	 * @throws UnequalWorldsException if validation fails
	 */
	private void validateAll(){
		World world = null;
		for(Bidder<T> bidder : bids.keySet()){
			if(world == null){
				world = bidder.getWorld();
			}else{
				if(!world.equals(bidder.getWorld())){
					throw new UnequalWorldsException();
				}
			}
		}
	}
	
	@Override
	public Iterator<XORBid<T>> iterator() {
		return bids.values().iterator();
	}

	public Set<Bidder<T>> getBidders() {
		return bids.keySet();
	}

	public Collection<XORBid<T>> getBids() {
		return bids.values();
	}

	/**
	 * Creates a copy of this instance, excluding the bid form a specific {@link Bidder}
	 * @param bidder The bidder to be excluded
	 */
	public Bids<T> without(Bidder<T> bidder) {
		Map<Bidder<T>,XORBid<T>> newBidderBidMap = new HashMap<>(bids);
		newBidderBidMap.remove(bidder);
		return new Bids<T>(newBidderBidMap.values());
	}

	public XORBid<T> getBid(Bidder<T> bidder) {
		return bids.get(bidder);
	}
	
	public boolean contains(Bidder<T> bidder){
		return bids.containsKey(bidder);
	}
}
