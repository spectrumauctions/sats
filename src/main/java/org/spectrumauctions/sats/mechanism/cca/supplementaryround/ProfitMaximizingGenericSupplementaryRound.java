package org.spectrumauctions.sats.mechanism.cca.supplementaryround;

import org.spectrumauctions.sats.core.bidlang.generic.GenericDefinition;
import org.spectrumauctions.sats.core.bidlang.generic.GenericValue;
import org.spectrumauctions.sats.core.model.Bidder;
import org.spectrumauctions.sats.core.model.Good;
import org.spectrumauctions.sats.mechanism.cca.GenericCCAMechanism;
import org.spectrumauctions.sats.opt.domain.GenericDemandQueryMIP;
import org.spectrumauctions.sats.opt.domain.GenericDemandQueryMIPBuilder;
import org.spectrumauctions.sats.opt.domain.GenericDemandQueryResult;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class ProfitMaximizingGenericSupplementaryRound<G extends GenericDefinition<T>, T extends Good> implements GenericSupplementaryRound<G, T> {

    private static final int DEFAULT_NUMBER_OF_SUPPLEMENTARY_BIDS = 500;

    private int numberOfSupplementaryBids = DEFAULT_NUMBER_OF_SUPPLEMENTARY_BIDS;
    private boolean useLastDemandedPrices = false;

    @Override
    public List<GenericValue<G, T>> getSupplementaryBids(GenericCCAMechanism<G, T> cca, Bidder<T> bidder) {
        GenericDemandQueryMIPBuilder<G, T> genericDemandQueryMIPBuilder = cca.getDemandQueryBuilder();
        Map<G, BigDecimal> finalPrices = cca.getFinalPrices();
        Map<G, BigDecimal> lastPrices = cca.getLastPrices();
        double epsilon = cca.getEpsilon();
        GenericDemandQueryMIP<G, T> demandQueryMIP;
        if (useLastDemandedPrices) {
            demandQueryMIP =  genericDemandQueryMIPBuilder.getDemandQueryMipFor(bidder, lastPrices, epsilon);
        } else {
            demandQueryMIP =  genericDemandQueryMIPBuilder.getDemandQueryMipFor(bidder, finalPrices, epsilon);
        }
        List<? extends GenericDemandQueryResult<G, T>> resultSet = demandQueryMIP.getResultPool(numberOfSupplementaryBids);
        return resultSet.stream().map(GenericDemandQueryResult::getResultingBundle).collect(Collectors.toList());
    }

    public void setNumberOfSupplementaryBids(int numberOfSupplementaryBids) {
        this.numberOfSupplementaryBids = numberOfSupplementaryBids;
    }

    public ProfitMaximizingGenericSupplementaryRound<G, T> withNumberOfSupplementaryBids(int numberOfSupplementaryBids) {
        this.numberOfSupplementaryBids = numberOfSupplementaryBids;
        return this;
    }

    public void useLastDemandedPrices(boolean useLastDemandedPrices) {
        this.useLastDemandedPrices = useLastDemandedPrices;
    }
}
