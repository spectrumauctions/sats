/**
 * Copyright by Michael Weiss, weiss.michael@gmx.ch
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.spectrumauctions.sats.core.bidfile;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spectrumauctions.sats.core.bidlang.BiddingLanguage;
import org.spectrumauctions.sats.core.bidlang.generic.FlatSizeIterators.GenericSizeDecreasing;
import org.spectrumauctions.sats.core.bidlang.generic.FlatSizeIterators.GenericSizeIncreasing;
import org.spectrumauctions.sats.core.bidlang.xor.SizeBasedUniqueRandomXOR;
import org.spectrumauctions.sats.core.model.UnsupportedBiddingLanguageException;
import org.spectrumauctions.sats.core.model.bvm.BMBidder;
import org.spectrumauctions.sats.core.model.bvm.bvm.BaseValueModel;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Random;

import static org.junit.Assert.fail;

/**
 * @author Michael Weiss
 *
 */
public abstract class BidFileWriter {

    private static final Logger logger = LogManager.getLogger(BidFileWriter.class);

    protected void testMultiBidderXOR(FileWriter exporter) {
        BaseValueModel bvm = new BaseValueModel();
        Collection<BMBidder> bidders = bvm.createNewWorldAndPopulation(0L);
        Collection<BiddingLanguage> languages = new ArrayList<>();
        int bidsPerBidder = 150;
        for (BMBidder bidder : bidders) {
            try {
                @SuppressWarnings("unchecked")
                SizeBasedUniqueRandomXOR lang = bidder
                        .getValueFunction(SizeBasedUniqueRandomXOR.class);
                lang.setDistribution(3, 2);
                lang.setIterations(bidsPerBidder);
                languages.add(lang);
            } catch (UnsupportedBiddingLanguageException e) {
                fail("Unsupported Bidding Iterator");
            }
        }
        try {
            exporter.writeMultiBidderXOR(languages, bidsPerBidder, "TestXOR_" + new Random().nextInt());
        } catch (IOException e) {
            e.printStackTrace();
            fail("Error writing file");
        }
    }


    protected void testSingleBidderXOR(FileWriter exporter) {
        BaseValueModel bvm = new BaseValueModel();
        Collection<BMBidder> bidders = bvm.createNewWorldAndPopulation(0L);
        int bidsPerBidder = 150;
        for (BMBidder bidder : bidders) {
            try {
                SizeBasedUniqueRandomXOR lang = bidder
                        .getValueFunction(SizeBasedUniqueRandomXOR.class);
                lang.setDistribution(3, 2);
                lang.setIterations(bidsPerBidder);
                File file = exporter.writeSingleBidderXOR(lang, bidsPerBidder, "TestSingleXOR_" + new Random().nextInt());
                logger.info(file.toPath().toString());
            } catch (UnsupportedBiddingLanguageException e) {
                fail("Unsupported Bidding Iterator");
            } catch (IOException e) {
                e.printStackTrace();
                fail("Error writing file");
            }
        }
    }


    protected void testMultiBidderXORQ(FileWriter exporter) {
        BaseValueModel bvm = new BaseValueModel();
        Collection<BMBidder> bidders = bvm.createNewWorldAndPopulation(0L);
        Collection<BiddingLanguage> languages = new ArrayList<>();
        int bidsPerBidder = 150;
        for (BMBidder bidder : bidders) {
            try {
                @SuppressWarnings("unchecked")
                GenericSizeDecreasing lang = bidder
                        .getValueFunction(GenericSizeDecreasing.class);
                languages.add(lang);
            } catch (UnsupportedBiddingLanguageException e) {
                fail("Unsupported Bidding Iterator");
            }
        }
        try {
            exporter.writeMultiBidderXORQ(languages, bidsPerBidder, "TestXORQ_" + new Random().nextInt());
        } catch (IOException e) {
            e.printStackTrace();
            fail("Error writing file");
        }
    }


    protected void testSingleBidderXORQ(FileWriter exporter) {
        BaseValueModel bvm = new BaseValueModel();
        Collection<BMBidder> bidders = bvm.createNewWorldAndPopulation(0L);
        int bidsPerBidder = 150;
        for (BMBidder bidder : bidders) {
            try {
                GenericSizeIncreasing lang = bidder
                        .getValueFunction(GenericSizeIncreasing.class);
                File file = exporter.writeSingleBidderXORQ(lang, bidsPerBidder, "TestSingleXORQ_" + new Random().nextInt());
                logger.info(file.toPath().toString());
            } catch (UnsupportedBiddingLanguageException e) {
                fail("Unsupported Bidding Iterator");
            } catch (IOException e) {
                e.printStackTrace();
                fail("Error writing file");
            }
        }
    }
}
