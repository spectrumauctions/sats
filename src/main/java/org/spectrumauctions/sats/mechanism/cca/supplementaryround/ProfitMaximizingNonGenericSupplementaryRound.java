package org.spectrumauctions.sats.mechanism.cca.supplementaryround;

import org.spectrumauctions.sats.core.bidlang.xor.XORValue;
import org.spectrumauctions.sats.core.model.Bidder;
import org.spectrumauctions.sats.core.model.Good;
import org.spectrumauctions.sats.mechanism.cca.NonGenericCCAMechanism;
import org.spectrumauctions.sats.opt.domain.GenericDemandQueryMIP;
import org.spectrumauctions.sats.opt.domain.NonGenericDemandQueryMIP;
import org.spectrumauctions.sats.opt.domain.NonGenericDemandQueryMIPBuilder;
import org.spectrumauctions.sats.opt.domain.NonGenericDemandQueryResult;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class ProfitMaximizingNonGenericSupplementaryRound<T extends Good> implements NonGenericSupplementaryRound<T> {

    private static final int DEFAULT_NUMBER_OF_SUPPLEMENTARY_BIDS = 500;

    private int numberOfSupplementaryBids = DEFAULT_NUMBER_OF_SUPPLEMENTARY_BIDS;
    private boolean useLastDemandedPrices = false;

    @Override
    public List<XORValue<T>> getSupplementaryBids(NonGenericCCAMechanism<T> cca, Bidder<T> bidder) {
        NonGenericDemandQueryMIPBuilder<T> nonGenericDemandQueryMipBuilder = cca.getDemandQueryBuilder();
        Map<T, BigDecimal> finalPrices = cca.getFinalPrices();
        Map<T, BigDecimal> lastPrices = cca.getLastPrices();
        double epsilon = cca.getEpsilon();
        NonGenericDemandQueryMIP<T> demandQueryMIP;

        if (useLastDemandedPrices) {
            demandQueryMIP = nonGenericDemandQueryMipBuilder.getDemandQueryMipFor(bidder, lastPrices, epsilon);
        } else {
            demandQueryMIP = nonGenericDemandQueryMipBuilder.getDemandQueryMipFor(bidder, finalPrices, epsilon);
        }
        List<? extends NonGenericDemandQueryResult<T>> resultSet = demandQueryMIP.getResultPool(numberOfSupplementaryBids);
        return resultSet.stream().map(NonGenericDemandQueryResult::getResultingBundle).collect(Collectors.toList());
    }

    public void setNumberOfSupplementaryBids(int numberOfSupplementaryBids) {
        this.numberOfSupplementaryBids = numberOfSupplementaryBids;
    }

    public ProfitMaximizingNonGenericSupplementaryRound<T> withNumberOfSupplementaryBids(int numberOfSupplementaryBids) {
        this.numberOfSupplementaryBids = numberOfSupplementaryBids;
        return this;
    }

    public void useLastDemandedPrices(boolean useLastDemandedPrices) {
        this.useLastDemandedPrices = useLastDemandedPrices;
    }
}
