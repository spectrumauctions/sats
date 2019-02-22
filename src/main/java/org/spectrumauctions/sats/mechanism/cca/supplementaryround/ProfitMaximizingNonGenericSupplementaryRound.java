package org.spectrumauctions.sats.mechanism.cca.supplementaryround;

import com.google.common.base.Preconditions;
import org.spectrumauctions.sats.core.bidlang.xor.XORValue;
import org.spectrumauctions.sats.core.model.Bidder;
import org.spectrumauctions.sats.core.model.Good;
import org.spectrumauctions.sats.mechanism.cca.NonGenericCCAMechanism;
import org.spectrumauctions.sats.opt.domain.NonGenericDemandQueryMIP;
import org.spectrumauctions.sats.opt.domain.NonGenericDemandQueryMIPBuilder;
import org.spectrumauctions.sats.opt.domain.NonGenericDemandQueryResult;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ProfitMaximizingNonGenericSupplementaryRound<T extends Good> implements NonGenericSupplementaryRound<T> {

    private static final int DEFAULT_NUMBER_OF_SUPPLEMENTARY_BIDS = 500;

    private int numberOfSupplementaryBids = DEFAULT_NUMBER_OF_SUPPLEMENTARY_BIDS;
    private boolean useLastDemandedPrices = false;
    private boolean useZeroPrices = false;

    @Override
    public List<XORValue<T>> getSupplementaryBids(NonGenericCCAMechanism<T> cca, Bidder<T> bidder) {
        Preconditions.checkArgument(!(useLastDemandedPrices && useZeroPrices));
        NonGenericDemandQueryMIPBuilder<T> nonGenericDemandQueryMipBuilder = cca.getDemandQueryBuilder();
        Map<T, BigDecimal> finalPrices = cca.getFinalPrices();
        Map<T, BigDecimal> lastPrices = cca.getLastPrices();
        double epsilon = cca.getEpsilon();
        NonGenericDemandQueryMIP<T> demandQueryMIP;

        if (useLastDemandedPrices) {
            demandQueryMIP = nonGenericDemandQueryMipBuilder.getDemandQueryMipFor(bidder, lastPrices, epsilon);
        } else if (useZeroPrices) {
            Map<T, BigDecimal> zeroPrices = new HashMap<>();
            bidder.getWorld().getLicenses().forEach(l -> zeroPrices.put((T) l, BigDecimal.ZERO));
            demandQueryMIP = nonGenericDemandQueryMipBuilder.getDemandQueryMipFor(bidder, zeroPrices, epsilon);
        } else {
            demandQueryMIP = nonGenericDemandQueryMipBuilder.getDemandQueryMipFor(bidder, finalPrices, epsilon);
        }
        demandQueryMIP.setTimeLimit(cca.getTimeLimit());
        demandQueryMIP.setRelativeResultPoolTolerance(cca.getRelativeResultPoolTolerance());
        demandQueryMIP.setAbsoluteResultPoolTolerance(cca.getAbsoluteResultPoolTolerance());
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

    public void useZeroPrices(boolean useZeroPrices) {
        this.useZeroPrices = useZeroPrices;
    }

}
