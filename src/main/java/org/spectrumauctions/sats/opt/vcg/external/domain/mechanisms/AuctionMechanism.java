package org.spectrumauctions.sats.opt.vcg.external.domain.mechanisms;

import org.spectrumauctions.sats.opt.model.EfficientAllocator;
import org.spectrumauctions.sats.opt.vcg.external.domain.AuctionResult;

public interface AuctionMechanism extends PaymentMechanism, EfficientAllocator {
    AuctionResult getAuctionResult();
}
