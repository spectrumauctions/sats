/**
 * Copyright by Michael Weiss, weiss.michael@gmx.ch
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package ch.uzh.ifi.ce.mweiss.sats.core.bidfile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map.Entry;

import ch.uzh.ifi.ce.mweiss.sats.core.bidlang.generic.GenericDefinition;
import ch.uzh.ifi.ce.mweiss.sats.core.bidlang.generic.GenericValue;
import ch.uzh.ifi.ce.mweiss.sats.core.model.Good;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import ch.uzh.ifi.ce.mweiss.sats.core.bidlang.generic.GenericLang;
import ch.uzh.ifi.ce.mweiss.sats.core.bidlang.xor.XORLanguage;
import ch.uzh.ifi.ce.mweiss.sats.core.bidlang.xor.XORValue;

/**
 * @author Michael Weiss
 *
 */
public class JsonExporter extends FileWriter{

    public static final boolean ONLY_NONZERO_QUANTITIES = true;
    Gson gson;
    /**
     * @param path
     */
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
    public File writeMultiBidderXOR(Collection<XORLanguage<Good>> valueFunctions, int numberOfBids, String filePrefix)
            throws IOException {
        JsonArray json = new JsonArray();       
        for(XORLanguage<Good> lang : valueFunctions){
            JsonObject thisBidder = new JsonObject();
            thisBidder.addProperty("bidder", lang.getBidder().getId());
            thisBidder.add("bids", singleBidderXOR(lang, numberOfBids, filePrefix));
            json.add(thisBidder);
        }
        return write(json, filePrefix);
    }

    
    private JsonElement singleBidderXOR(XORLanguage<Good> lang, int numberOfBids, String filePrefix) {
        JsonArray result = new JsonArray();
        Iterator<XORValue<Good>> iter = lang.iterator();
        for (int i = 0; i < numberOfBids && iter.hasNext(); i++) {
            JsonObject bid = new JsonObject();
            XORValue<Good> xorValue = iter.next();
            JsonArray licenses = new JsonArray();
            for(Good license : xorValue.getLicenses()){
                licenses.add(license.getId());
            }
            bid.add("licenses", licenses);
            bid.addProperty("value", roundedValue(xorValue.value().doubleValue()));
            result.add(bid);
        }
        return result;
    }

    /* (non-Javadoc)
     * @see FileWriter#writeSingleBidderXOR(XORLanguage, int, java.lang.String)
     */
    @Override
    public File writeSingleBidderXOR(XORLanguage<Good> valueFunction, int numberOfBids, String filePrefix)
            throws IOException {
        JsonElement singleBidder = singleBidderXOR(valueFunction, numberOfBids, filePrefix);
        return write(singleBidder, filePrefix);
    }

    /* (non-Javadoc)
     * @see FileWriter#writeMultiBidderXORQ(java.util.Collection, int, java.lang.String)
     */
    @Override
    public File writeMultiBidderXORQ(Collection<GenericLang<GenericDefinition>> valueFunctions, int numberOfBids,
                                     String filePrefix) throws IOException {
        JsonArray json = new JsonArray();
        for(GenericLang<GenericDefinition> lang : valueFunctions){
            JsonObject thisBidder = new JsonObject();
            thisBidder.addProperty("bidder", lang.getBidder().getId());
            thisBidder.add("bids", singleBidderXORQ(lang, numberOfBids, filePrefix));
            json.add(thisBidder);
        }
        return write(json, filePrefix);
    }

    /* (non-Javadoc)
     * @see FileWriter#writeSingleBidderXORQ(GenericLang, int, java.lang.String)
     */
    @Override
    public File writeSingleBidderXORQ(GenericLang<GenericDefinition> lang, int numberOfBids, String filePrefix)
            throws IOException {
        JsonElement singleBidder = singleBidderXORQ(lang, numberOfBids, filePrefix);
        return write(singleBidder, filePrefix);
    }
    
    private JsonElement singleBidderXORQ(GenericLang<GenericDefinition> lang, int numberOfBids, String filePrefix){
        JsonArray result = new JsonArray();
        Iterator<GenericValue<GenericDefinition>> iter = lang.iterator();
        for (int i = 0; i < numberOfBids && iter.hasNext(); i++) {
            JsonObject bid = new JsonObject();
            GenericValue<GenericDefinition> val = iter.next();
            JsonArray quantities  = new JsonArray();
            for(Entry<GenericDefinition, Integer> quant : val.getQuantities().entrySet()){
                if(quant.getValue() != 0 || !ONLY_NONZERO_QUANTITIES){
                    JsonObject object = new JsonObject();
                    object.add("generic definition", quant.getKey().shortJson());
                    object.addProperty("quantity", quant.getValue());
                    quantities.add(object);
                }
            }
            bid.add("quantities", quantities);
            bid.addProperty("value", roundedValue(val.getValue().doubleValue()));
            result.add(bid);
        }
        return result;
    }

    
    private File write(JsonElement toWrite, String filePrefix) throws IOException{
        Path file = nextNonexistingFile(filePrefix);
        String content = gson.toJson(toWrite);
        Files.write(file, content.getBytes(),  StandardOpenOption.CREATE);
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
