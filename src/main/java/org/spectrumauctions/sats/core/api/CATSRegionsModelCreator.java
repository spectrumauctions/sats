package org.spectrumauctions.sats.core.api;

import org.spectrumauctions.sats.core.model.UnsupportedBiddingLanguageException;
import org.spectrumauctions.sats.core.model.cats.CATSBidderSetup;
import org.spectrumauctions.sats.core.model.cats.CATSRegionModel;

import java.io.File;
import java.io.IOException;

/**
 * Created by Michael Weiss on 04.05.2017.
 */
public class CATSRegionsModelCreator extends ModelCreator {

    private final int numberOfBidders;
    private final int numberOfGoods;

    protected CATSRegionsModelCreator(Builder builder) {
        super(builder);
        this.numberOfBidders = builder.getNumberOfBidders();
        this.numberOfGoods = builder.getNumberOfGoods();
    }

    @Override
    public PathResult generateResult(File outputFolder) throws UnsupportedBiddingLanguageException, IOException, IllegalConfigException {
        CATSRegionModel model = new CATSRegionModel();
        model.setNumberOfBidders(numberOfBidders);
        model.setNumberOfGoods(numberOfGoods);
        return appendTopLevelParamsAndSolve(model, outputFolder);
    }

    public static class Builder extends ModelCreator.Builder {

        private int numberOfBidders;
        private int numberOfGoods;

        public Builder() {
            super();
            this.numberOfBidders = new CATSBidderSetup.Builder().getNumberOfBidders();
        }

        @Override
        public CATSRegionsModelCreator build() {
            return new CATSRegionsModelCreator(this);
        }

        public int getNumberOfBidders() {
            return numberOfBidders;
        }

        public void setNumberOfBidders(int numberOfBidders) {
            this.numberOfBidders = numberOfBidders;
        }

        public int getNumberOfGoods() {
            return numberOfGoods;
        }

        public void setNumberOfGoods(int numberOfGoods) {
            this.numberOfGoods = numberOfGoods;
        }
    }
}
