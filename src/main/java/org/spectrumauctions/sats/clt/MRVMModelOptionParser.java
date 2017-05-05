/**
 * Copyright by Michael Weiss, weiss.michael@gmx.ch
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.spectrumauctions.sats.clt;

import joptsimple.OptionSet;
import org.spectrumauctions.sats.core.api.IllegalConfigException;
import org.spectrumauctions.sats.core.api.MRVMModelCreator;
import org.spectrumauctions.sats.core.api.PathResult;
import org.spectrumauctions.sats.core.model.UnsupportedBiddingLanguageException;

import java.io.IOException;

/**
 * @author Michael Weiss
 *
 */
public class MRVMModelOptionParser extends ModelOptionParser {

    public final static String KEY_LOCALBIDDERS = "localbidders";
    public final static String KEY_NATIONALBIDDERS = "nationalbidders";
    public final static String KEY_REGIONALBIDDERS = "regionalbidders";

    public MRVMModelOptionParser() {
        this.accepts(KEY_LOCALBIDDERS, "The number of Local Bidders in the MRVM Model")
                .withRequiredArg().ofType(Integer.class);
        this.accepts(KEY_NATIONALBIDDERS, "The number of National Bidders in the MRVM Model")
                .withRequiredArg().ofType(Integer.class);
        this.accepts(KEY_REGIONALBIDDERS, "The number of Regional Bidders in the MRVM Model")
                .withRequiredArg().ofType(Integer.class);
    }

    /* (non-Javadoc)
     * @see ModelOptionParser#getModel()
     */
    @Override
    protected Model getModel() {
        return Model.MRVM;
    }

    /* (non-Javadoc)
     * @see ModelOptionParser#treatResult(java.lang.String[])
     */
    @Override
    public PathResult treatResult(String[] args)
            throws IllegalConfigException, UnsupportedBiddingLanguageException, IOException {
        MRVMModelCreator.Builder builder = new MRVMModelCreator.Builder();
        OptionSet options = this.parse(args);
        if (options.has(KEY_NATIONALBIDDERS)) {
            builder.setNumberOfNationalBidders((Integer) options.valueOf(KEY_NATIONALBIDDERS));
        }
        if (options.has(KEY_REGIONALBIDDERS)) {
            builder.setNumberOfRegionalBidders((Integer) options.valueOf(KEY_REGIONALBIDDERS));
        }
        if (options.has(KEY_NATIONALBIDDERS)) {
            builder.setNumberOfLocalBidders((Integer) options.valueOf(KEY_LOCALBIDDERS));
        }
        return allModelsResultTreating(options, builder);
    }

}
