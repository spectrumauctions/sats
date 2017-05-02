package ch.uzh.ifi.ce.mweiss.satsopt.vcg.external.winnerdetermination;

import java.util.HashMap;
import java.util.Map;

import ch.uzh.ifi.ce.mweiss.specval.bidlang.xor.XORValue;
import ch.uzh.ifi.ce.mweiss.specval.model.Bidder;
import ch.uzh.ifi.ce.mweiss.specval.model.Good;
import ch.uzh.ifi.ce.mweiss.satsopt.vcg.external.domain.Auction;
import edu.harvard.econcs.jopt.solver.IMIP;
import edu.harvard.econcs.jopt.solver.mip.CompareType;
import edu.harvard.econcs.jopt.solver.mip.Constraint;
import edu.harvard.econcs.jopt.solver.mip.MIP;
import edu.harvard.econcs.jopt.solver.mip.VarType;
import edu.harvard.econcs.jopt.solver.mip.Variable;

/**
 * Wraps an OR or OR* winner determination
 * 
 * @author Benedikt Buenz
 * 
 */
public class XORWinnerDetermination<T extends Good> extends WinnerDetermination<T>{
	private Map<XORValue<T>, Variable> bidVariables = new HashMap<>();
	private IMIP winnerDeterminationProgram;

	public XORWinnerDetermination(Auction<T> auction) {
		super(auction);
		winnerDeterminationProgram = createWinnerDeterminationMIP(auction);
	}

	private IMIP createWinnerDeterminationMIP(Auction<T> auction) {
		MIP winnerDeterminationProgram = new MIP();
		winnerDeterminationProgram.setObjectiveMax(true);
		// Add decision variables and objective terms:
		for (Bidder<T> bidder : auction.getBidders()) {
			for (XORValue<T> bundleBid : auction.getBid(bidder).getValues()) {
				Variable bidI = new Variable("Bid " + bundleBid.getId(), VarType.BOOLEAN, 0, 1);
				winnerDeterminationProgram.add(bidI);
				winnerDeterminationProgram.addObjectiveTerm(bundleBid.getValue(), bidI);
				bidVariables.put(bundleBid, bidI);
			}
		}
		Map<Good, Constraint> goods = new HashMap<>();

		for (Bidder<T> bidder : auction.getBidders()) {
			Constraint exclusiveBids = new Constraint(CompareType.LEQ, 1);
			for (XORValue<T> bundleBid : auction.getBid(bidder).getValues()) {
				exclusiveBids.addTerm(1, bidVariables.get(bundleBid));
				for (Good good : bundleBid.getLicenses()) {
					Constraint noDoubleAssignment = goods.get(good);
					if (noDoubleAssignment == null) {
						noDoubleAssignment = new Constraint(CompareType.LEQ, 1);
						goods.put(good, noDoubleAssignment);
					}
					noDoubleAssignment.addTerm(1.0, bidVariables.get(bundleBid));
				}
			}
			winnerDeterminationProgram.add(exclusiveBids);
		}
		for (Constraint noDoubleAssignments : goods.values()) {
			winnerDeterminationProgram.add(noDoubleAssignments);
		}

		return winnerDeterminationProgram;
	}

	protected IMIP getMIP() {
		return winnerDeterminationProgram;
	}

	@Override
	protected Variable getBidVariable(XORValue<T> bundleBid) {
		return bidVariables.get(bundleBid);
	}

}
