package org.spectrumauctions.sats.core.api;

import org.spectrumauctions.sats.core.model.UnsupportedBiddingLanguageException;
import org.spectrumauctions.sats.core.model.gsvm.GSVMNationalBidderSetup;
import org.spectrumauctions.sats.core.model.gsvm.GSVMRegionalBidderSetup;
import org.spectrumauctions.sats.core.model.gsvm.GlobalSynergyValueModel;

import java.io.File;
import java.io.IOException;

/**
 * Created by Michael Weiss on 04.05.2017.
 */
public class GSVMModelCreator extends ModelCreator {

    private final int numberOfNationalBidders;
    private final int numberOfReginalBidders;

    protected GSVMModelCreator(Builder builder) {
        super(builder);
        this.numberOfNationalBidders = builder.getNumberOfNationalBidders();
        this.numberOfReginalBidders = builder.getNumberOfRegionalBidders();
    }

    @Override
    public PathResult generateResult(File outputFolder) throws UnsupportedBiddingLanguageException, IOException, IllegalConfigException {
        GlobalSynergyValueModel model = new GlobalSynergyValueModel();
        model.setNumberOfNationalBidders(numberOfNationalBidders);
        model.setNumberOfRegionalBidders(numberOfReginalBidders);
        return appendTopLevelParamsAndSolve(model, outputFolder);
    }

    public static class Builder extends ModelCreator.Builder {

        private int numberOfNationalBidders;
        private int numberOfRegionalBidders;

        public Builder() {
            this.numberOfNationalBidders = new GSVMNationalBidderSetup.Builder().getNumberOfBidders();
            this.numberOfRegionalBidders = new GSVMRegionalBidderSetup.Builder().getNumberOfBidders();
        }

        @Override
        public GSVMModelCreator build() {
            return new GSVMModelCreator(this);
        }

        public int getNumberOfNationalBidders() {
            return numberOfNationalBidders;
        }

        public int getNumberOfRegionalBidders() {
            return numberOfRegionalBidders;
        }

        public void setNumberOfNationalBidders(int numberOfNationalBidders) {
            this.numberOfNationalBidders = numberOfNationalBidders;
        }

        public void setNumberOfRegionalBidders(int numberOfRegionalBidders) {
            this.numberOfRegionalBidders = numberOfRegionalBidders;
        }
    }
}
