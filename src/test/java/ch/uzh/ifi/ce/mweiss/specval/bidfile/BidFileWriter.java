/**
 * Copyright by Michael Weiss, weiss.michael@gmx.ch
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package ch.uzh.ifi.ce.mweiss.specval.bidfile;

import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Random;

import ch.uzh.ifi.ce.mweiss.specval.bidlang.generic.GenericDefinition;
import ch.uzh.ifi.ce.mweiss.specval.bidlang.generic.GenericLang;
import ch.uzh.ifi.ce.mweiss.specval.bidlang.generic.FlatSizeIterators.GenericSizeDecreasing;
import ch.uzh.ifi.ce.mweiss.specval.bidlang.generic.FlatSizeIterators.GenericSizeIncreasing;
import ch.uzh.ifi.ce.mweiss.specval.bidlang.xor.SizeBasedUniqueRandomXOR;
import ch.uzh.ifi.ce.mweiss.specval.bidlang.xor.XORLanguage;
import ch.uzh.ifi.ce.mweiss.specval.model.Good;
import ch.uzh.ifi.ce.mweiss.specval.model.UnsupportedBiddingLanguageException;
import ch.uzh.ifi.ce.mweiss.specval.model.bm.BMBidder;
import ch.uzh.ifi.ce.mweiss.specval.model.bm.bvm.BaseValueModel;

/**
 * @author Michael Weiss
 *
 */
public abstract class BidFileWriter {

    protected void testMultiBidderXOR(FileWriter exporter) {
        BaseValueModel bvm = new BaseValueModel();
        Collection<BMBidder> bidders = bvm.createNewPopulation(0L);
        Collection<XORLanguage<Good>> languages = new ArrayList<>();
        int bidsPerBidder = 150;
        for (BMBidder bidder : bidders) {
            try {
                @SuppressWarnings("unchecked")
                SizeBasedUniqueRandomXOR<Good> lang = bidder
                        .getValueFunction(SizeBasedUniqueRandomXOR.class);
                lang.setDistribution(3, 2, bidsPerBidder);
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

    
    protected void testSingleBidderXOR(FileWriter exporter){
        BaseValueModel bvm = new BaseValueModel();
        Collection<BMBidder> bidders = bvm.createNewPopulation(0L);
        int bidsPerBidder = 150;
        for (BMBidder bidder : bidders) {
            try {
                SizeBasedUniqueRandomXOR<Good> lang = bidder
                        .getValueFunction(SizeBasedUniqueRandomXOR.class);
                lang.setDistribution(3, 2, bidsPerBidder);
                File file = exporter.writeSingleBidderXOR(lang, bidsPerBidder, "TestSingleXOR_" + new Random().nextInt());
                System.out.println(file.toPath().toString());
            } catch (UnsupportedBiddingLanguageException e) {
                fail("Unsupported Bidding Iterator");
            } catch (IOException e) {
                e.printStackTrace();
                fail("Error writing file");
            }
        }
    }
    
     
    protected void testMultiBidderXORQ(FileWriter exporter){
        BaseValueModel bvm = new BaseValueModel();
        Collection<BMBidder> bidders = bvm.createNewPopulation(0L);
        Collection<GenericLang<GenericDefinition>> languages = new ArrayList<>();
        int bidsPerBidder = 150;
        for (BMBidder bidder : bidders) {
            try {
                @SuppressWarnings("unchecked")
                GenericSizeDecreasing<GenericDefinition> lang = bidder
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
    
    
    protected void testSingleBidderXORQ(FileWriter exporter){
        BaseValueModel bvm = new BaseValueModel();
        Collection<BMBidder> bidders = bvm.createNewPopulation(0L);
        int bidsPerBidder = 150;
        for (BMBidder bidder : bidders) {
            try {
                GenericSizeIncreasing<GenericDefinition> lang = bidder
                        .getValueFunction(GenericSizeIncreasing.class);
                File file = exporter.writeSingleBidderXORQ(lang, bidsPerBidder, "TestSingleXORQ_" + new Random().nextInt());
                System.out.println(file.toPath().toString());
            } catch (UnsupportedBiddingLanguageException e) {
                fail("Unsupported Bidding Iterator");
            } catch (IOException e) {
                e.printStackTrace();
                fail("Error writing file");
            }
        }
    }
}
