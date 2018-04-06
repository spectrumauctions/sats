package org.spectrumauctions.sats.mechanism.ccg;

import com.google.common.collect.Sets;
import edu.harvard.econcs.jopt.solver.IMIPResult;
import edu.harvard.econcs.jopt.solver.mip.*;
import org.spectrumauctions.sats.core.model.Bidder;
import org.spectrumauctions.sats.core.model.Good;
import org.spectrumauctions.sats.mechanism.domain.MechanismResult;
import org.spectrumauctions.sats.mechanism.domain.Payment;
import org.spectrumauctions.sats.mechanism.domain.mechanisms.AuctionMechanism;
import org.spectrumauctions.sats.mechanism.vcg.VCGMechanism;
import org.spectrumauctions.sats.opt.domain.Allocation;
import org.spectrumauctions.sats.opt.domain.WinnerDeterminator;

import java.util.HashMap;
import java.util.Map;

public class CCGMechanism<T extends Good> implements AuctionMechanism<T> {

    private WinnerDeterminator<T> baseWD;
    private MechanismResult<T> result;


    public CCGMechanism(WinnerDeterminator<T> wdp) {
        this.baseWD = wdp;
    }

    @Override
    public MechanismResult<T> getMechanismResult() {
        if (result == null) {
            result = calculateCCGPayments();
        }
        return result;
    }

    @Override
    public Payment<T> getPayment() {
        return getMechanismResult().getPayment();
    }

    @Override
    public WinnerDeterminator<T> getWdWithoutBidder(Bidder<T> bidder) {
        return baseWD.getWdWithoutBidder(bidder);
    }

    @Override
    public Allocation<T> calculateAllocation() {
        return getMechanismResult().getAllocation();
    }

    @Override
    public WinnerDeterminator<T> copyOf() {
        return baseWD.copyOf();
    }

    @Override
    public void adjustPayoffs(Map<Bidder<T>, Double> payoffs) {
        // Stub
    }

    private MechanismResult<T> calculateCCGPayments() {
        MechanismResult<T> vcgResult = new VCGMechanism<>(baseWD).getMechanismResult();
        Allocation<T> originalAllocation = vcgResult.getAllocation();

        Payment<T> payment = vcgResult.getPayment();

        Map<Bidder<T>, Variable> paymentVariables = createPaymentVariables(originalAllocation, payment);
        MIP l1Mip = new MIP();
        paymentVariables.values().forEach(l1Mip::add);
        paymentVariables.values().forEach(v -> l1Mip.addObjectiveTerm(1, v));

        double oldBlockingCoalitionValue = -1;
        boolean caughtInLoopDueToRoundingErrors = false;
        while (true) {
            Map<Bidder<T>, Double> payoffs = computePayoffs(originalAllocation, payment);
            WinnerDeterminator<T> blockingCoalitionMip = baseWD.copyOf();
            blockingCoalitionMip.adjustPayoffs(payoffs);

            Allocation<T> blockingCoalition = blockingCoalitionMip.calculateAllocation();

//             FIXME
//            double traitorPayments = blockingCoalition.getWinners().stream().mapToDouble(w -> payment.paymentOf(w).getAmount()).sum();
//
//            double blockingCoalitionValue = blockingCoalition.getTotalValue().doubleValue() - /* TODO: Check if this is correct */ traitorPayments;
//            if (oldBlockingCoalitionValue == blockingCoalitionValue) {
//                // LOGGER.warn("Detected endless loop due to rounding inaccuracies. Problem is actually solved.");
//                caughtInLoopDueToRoundingErrors = true;
//            } else {
//                oldBlockingCoalitionValue = blockingCoalitionValue;
//            }
//            double payments = payment.getTotalPayments();
//            if (caughtInLoopDueToRoundingErrors ||
//                    blockingCoalitionValue <= payments + 1e-6) {
//                // LOGGER.debug("Final CCG payments are {}", payment);
//                return new MechanismResult<>(payment, originalAllocation);
//            } else {
//                double coalitionValue = blockingCoalitionValue - traitorPayments;
//                Constraint constraint = new Constraint(CompareType.GEQ, coalitionValue);
//
//                for (Bidder<T> nonTraitor : Sets.difference(originalAllocation.getWinners(), blockingCoalition.getWinners())) {
//                    Variable paymentVariable = paymentVariables.get(nonTraitor);
//                    constraint.addTerm(1, paymentVariable);
//                }
//
//                l1Mip.add(constraint);
//
//                // L1 Norm
//                IMIPResult l1Result = solverClient.solve(l1Mip);
//                numberOfLPs++;
//
//                // L2 Norm
//                MIP l2Mip = new MIP();
//                l1Mip.getVars().values().forEach(l2Mip::add);
//                l1Mip.getConstraints().forEach(l2Mip::add);
//
//                double totalPayments = l1Result.getObjectiveValue();
//                Constraint fixPayments = new Constraint(CompareType.EQ, totalPayments);
//                paymentVariables.values().forEach(v -> fixPayments.addTerm(1, v));
//                l2Mip.add(fixPayments);
//
//                for (Map.Entry<CellularCompany, Variable> entry : paymentVariables.entrySet()) {
//                    Variable winnerVariable = entry.getValue();
//                    l2Mip.addObjectiveTerm(1, winnerVariable, winnerVariable);
//                    l2Mip.addObjectiveTerm(-2 * vcgPayment.paymentOf(entry.getKey()), winnerVariable);
//                }
//
//                IMIPResult l2Result = solverClient.solve(l2Mip);
//                numberOfLPs++;
//
//                Map<CellularCompany, Double> paymentMap = new HashMap<>(allocation.getWinners().size());
//                for (CellularCompany company : allocation.getWinners()) {
//                    double doublePayment = l2Result.getValue(paymentVariables.get(company));
//                    paymentMap.put(company, doublePayment);
//                }
//                payment = new Payment(paymentMap/*, System.currentTimeMillis() - start, payment.getNumberOfMIPs() + 1, payment.getNumberOfLPs() + 1*/);
//                LOGGER.debug("Adjusted payments to {}", payment);
//            }
            return null;
        }

    }

    private Map<Bidder<T>, Double> computePayoffs(Allocation<T> allocation, Payment<T> payment) {
        Map<Bidder<T>, Double> payoffs = new HashMap<>(allocation.getWinners().size());
        for (Bidder<T> company : allocation.getWinners()) {
            payoffs.put(company, allocation.getTradeValue(company).doubleValue() - payment.paymentOf(company).getAmount());
        }
        return payoffs;
    }

    private Map<Bidder<T>, Variable> createPaymentVariables(Allocation<T> originalAllocation, Payment<T> payment) {
        Map<Bidder<T>, Variable> winnerVariables = new HashMap<>(originalAllocation.getWinners().size());
        for (Bidder<T> winner : originalAllocation.getWinners()) {

            double winnerPayment = payment.paymentOf(winner).getAmount();
            double winnerValue = originalAllocation.getTradeValue(winner).doubleValue();
            Variable winnerVariable = new Variable(String.valueOf(winner.getId()), VarType.DOUBLE, winnerPayment, winnerValue);

            winnerVariables.put(winner, winnerVariable);
        }
        return winnerVariables;
    }

}
