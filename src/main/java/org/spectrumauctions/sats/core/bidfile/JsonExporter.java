/**
 * Copyright by Michael Weiss, weiss.michael@gmx.ch
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.spectrumauctions.sats.core.bidfile;

import com.google.gson.*;
import org.marketdesignresearch.mechlib.core.BundleEntry;
import org.marketdesignresearch.mechlib.core.Good;
import org.marketdesignresearch.mechlib.core.bidder.valuefunction.BundleValue;
import org.spectrumauctions.sats.core.bidlang.BiddingLanguage;
import org.spectrumauctions.sats.core.model.GenericGood;
import org.spectrumauctions.sats.core.model.License;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Collection;
import java.util.Iterator;

/**
 * @author Michael Weiss
 *
 */
public class JsonExporter extends FileWriter {

    public static final boolean ONLY_NONZERO_QUANTITIES = true;
    Gson gson;

    public JsonExporter(File path) {
        super(path);
        GsonBuilder builder = new GsonBuilder();
        builder.setPrettyPrinting();
        gson = builder.create();
    }

    /* (non-Javadoc)
     * @see FileWriter#writeMultiBidderXOR(java.util.Collection, int, java.lang.String)
     */
    @Override
    public File writeMultiBidderXOR(Collection<BiddingLanguage> valueFunctions, int numberOfBids, String filePrefix)
            throws IOException {
        JsonArray json = new JsonArray();
        for (BiddingLanguage lang : valueFunctions) {
            JsonObject thisBidder = new JsonObject();
            thisBidder.addProperty("bidder", lang.getBidder().getLongId());
            thisBidder.add("bids", singleBidderXOR(lang, numberOfBids, filePrefix));
            json.add(thisBidder);
        }
        return write(json, filePrefix);
    }


    private JsonElement singleBidderXOR(BiddingLanguage lang, int numberOfBids, String filePrefix) {
        JsonArray result = new JsonArray();
        Iterator<BundleValue> iter = lang.iterator();
        for (int i = 0; i < numberOfBids && iter.hasNext(); i++) {
            JsonObject bid = new JsonObject();
            BundleValue xorValue = iter.next();
            JsonArray licenses = new JsonArray();
            for (Good license : xorValue.getBundle().getSingleQuantityGoods()) {
                License l = (License) license;
                licenses.add(l.getLongId());
            }
            bid.add("licenses", licenses);
            bid.addProperty("value", xorValue.getAmount().setScale(ROUNDING_SCALE, BigDecimal.ROUND_HALF_UP).toString());
            result.add(bid);
        }
        return result;
    }

    /* (non-Javadoc)
     * @see FileWriter#writeSingleBidderXOR(XORLanguage, int, java.lang.String)
     */
    @Override
    public File writeSingleBidderXOR(BiddingLanguage valueFunction, int numberOfBids, String filePrefix)
            throws IOException {
        JsonElement singleBidder = singleBidderXOR(valueFunction, numberOfBids, filePrefix);
        return write(singleBidder, filePrefix);
    }

    /* (non-Javadoc)
     * @see FileWriter#writeMultiBidderXORQ(java.util.Collection, int, java.lang.String)
     */
    @Override
    public File writeMultiBidderXORQ(Collection<BiddingLanguage> valueFunctions, int numberOfBids,
                                     String filePrefix) throws IOException {
        JsonArray json = new JsonArray();
        for (BiddingLanguage lang : valueFunctions) {
            JsonObject thisBidder = new JsonObject();
            thisBidder.addProperty("bidder", lang.getBidder().getLongId());
            thisBidder.add("bids", singleBidderXORQ(lang, numberOfBids, filePrefix));
            json.add(thisBidder);
        }
        return write(json, filePrefix);
    }

    /* (non-Javadoc)
     * @see FileWriter#writeSingleBidderXORQ(GenericLang, int, java.lang.String)
     */
    @Override
    public File writeSingleBidderXORQ(BiddingLanguage lang, int numberOfBids, String filePrefix)
            throws IOException {
        JsonElement singleBidder = singleBidderXORQ(lang, numberOfBids, filePrefix);
        return write(singleBidder, filePrefix);
    }

    private JsonElement singleBidderXORQ(BiddingLanguage lang, int numberOfBids, String filePrefix) {
        JsonArray result = new JsonArray();
        Iterator<BundleValue> iter = lang.iterator();
        for (int i = 0; i < numberOfBids && iter.hasNext(); i++) {
            JsonObject bid = new JsonObject();
            BundleValue val = iter.next();
            JsonArray quantities = new JsonArray();
            for (BundleEntry quant : val.getBundle().getBundleEntries()) {
                if (quant.getAmount() != 0 || !ONLY_NONZERO_QUANTITIES) {
                    GenericGood good = (GenericGood) quant.getGood();
                    JsonObject object = new JsonObject();
                    object.add("generic definition", good.shortJson());
                    object.addProperty("quantity", quant.getAmount());
                    quantities.add(object);
                }
            }
            bid.add("quantities", quantities);
            bid.addProperty("value", val.getAmount().setScale(ROUNDING_SCALE, BigDecimal.ROUND_HALF_UP).toString());
            result.add(bid);
        }
        return result;
    }


    private File write(JsonElement toWrite, String filePrefix) throws IOException {
        Path file = nextNonexistingFile(filePrefix);
        String content = gson.toJson(toWrite);
        Files.write(file, content.getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE);
        return file.toFile();
    }

    /* (non-Javadoc)
     * @see FileWriter#filetype()
     */
    @Override
    protected String filetype() {
        return "json";
    }
}
