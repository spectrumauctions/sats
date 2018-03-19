/**
 * Copyright by Michael Weiss, weiss.michael@gmx.ch
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.spectrumauctions.sats.clt;

import joptsimple.OptionSet;
import org.spectrumauctions.sats.core.api.IllegalConfigException;
import org.spectrumauctions.sats.core.api.PathResult;
import org.spectrumauctions.sats.core.api.SRVMModelCreator;
import org.spectrumauctions.sats.core.model.UnsupportedBiddingLanguageException;

import java.io.IOException;

/**
 * @author Michael Weiss
 *
 */
public class SRVMModelOptionParser extends ModelOptionParser {

    public final static String KEY_SMALLBIDDERS = "smallb";
    public final static String KEY_HIGHFREQUENCYBIDDERS = "highb";
    public final static String KEY_PRIMARYBIDDERS = "primaryb";
    public final static String KEY_SECONDARYBIDDERS = "secondaryb";

    /**
     *
     */
    public SRVMModelOptionParser() {
        this.accepts(KEY_SMALLBIDDERS, "The number of Small Bidders in the SRVM")
                .withRequiredArg().ofType(Integer.class);
        this.accepts(KEY_HIGHFREQUENCYBIDDERS, "The number of High Frequency Bidders in the SRVM")
                .withRequiredArg().ofType(Integer.class);
        this.accepts(KEY_PRIMARYBIDDERS, "The number of Primary Bidders in the SRVM")
                .withRequiredArg().ofType(Integer.class);
        this.accepts(KEY_SECONDARYBIDDERS, "The number of Secondary Bidders in the SRVM")
                .withRequiredArg().ofType(Integer.class);
    }

    /* (non-Javadoc)
     * @see ModelOptionParser#getModel()
     */
    @Override
    protected Model getModel() {
        return Model.SRVM;
    }

    /* (non-Javadoc)
     * @see ModelOptionParser#treatResult(java.lang.String[])
     */
    @Override
    public PathResult treatResult(String[] args)
            throws IllegalConfigException, UnsupportedBiddingLanguageException, IOException {
        SRVMModelCreator.Builder builder = new SRVMModelCreator.Builder();
        OptionSet options = this.parse(args);
        if (options.has(KEY_SMALLBIDDERS)) {
            builder.setNumSmallBidders((Integer) options.valueOf(KEY_SMALLBIDDERS));
        }
        if (options.has(KEY_HIGHFREQUENCYBIDDERS)) {
            builder.setNumHighFrequencyBidders((Integer) options.valueOf(KEY_HIGHFREQUENCYBIDDERS));
        }
        if (options.has(KEY_PRIMARYBIDDERS)) {
            builder.setNumPrimaryBidders((Integer) options.valueOf(KEY_PRIMARYBIDDERS));
        }
        if (options.has(KEY_SECONDARYBIDDERS)) {
            builder.setNumSecondaryBidders((Integer) options.valueOf(KEY_SECONDARYBIDDERS));
        }
        return allModelsResultTreating(options, builder);
    }

}
