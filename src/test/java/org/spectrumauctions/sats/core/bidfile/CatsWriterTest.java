/**
 * Copyright by Michael Weiss, weiss.michael@gmx.ch
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.spectrumauctions.sats.core.bidfile;

import org.junit.Test;
import org.spectrumauctions.sats.core.bidlang.BiddingLanguage;
import org.spectrumauctions.sats.core.bidlang.xor.CatsXOR;
import org.spectrumauctions.sats.core.model.UnsupportedBiddingLanguageException;
import org.spectrumauctions.sats.core.model.cats.CATSBidder;
import org.spectrumauctions.sats.core.model.cats.CATSRegionModel;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Random;
import java.util.stream.Collectors;

import static org.junit.Assert.fail;

public class CatsWriterTest extends BidFileWriter {

    public static String EXPORT_TEST_FOLDER_NAME = "CATSEXPORT_TESTFILES (AUTODELETED FOLDER)";

    @Test
    public void testSingleBidderCatsFile() {
        CatsExporter exporter = new CatsExporter(new File(EXPORT_TEST_FOLDER_NAME));
        testSingleBidderXOR(exporter);
    }

    @Test
    public void writeMultiBidderCatsFile() {
        CatsExporter exporter = new CatsExporter(new File(EXPORT_TEST_FOLDER_NAME));
        testMultiBidderXOR(exporter);
    }

    @Test
    public void writeSingleBidderCatsModelCatsFile() throws IOException {
        CatsExporter exporter = new CatsExporter(new File(EXPORT_TEST_FOLDER_NAME));
        CATSRegionModel model = new CATSRegionModel();
        model.setNumberOfBidders(25);
        Collection<CATSBidder> bidders = model.createNewWorldAndPopulation(21321468L);
        Collection<BiddingLanguage> langs = bidders.stream().map(b -> {
            try {
                return b.getValueFunction(CatsXOR.class, 0L);
            } catch (UnsupportedBiddingLanguageException e) {
                fail("Unsupported Bidding Iterator");
                return null;
            }
        }).collect(Collectors.toList());

        File file = exporter.writeMultiBidderXOR(langs, 20, "TestSingleXOR_" + new Random().nextInt());
        System.out.println(file.toPath().toString());
    }

}
