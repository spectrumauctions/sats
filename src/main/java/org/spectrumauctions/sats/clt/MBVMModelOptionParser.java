/**
 * Copyright by Michael Weiss, weiss.michael@gmx.ch
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.spectrumauctions.sats.clt;

import joptsimple.OptionSet;
import org.spectrumauctions.sats.core.api.IllegalConfigException;
import org.spectrumauctions.sats.core.api.MBVMModelCreator;
import org.spectrumauctions.sats.core.api.PathResult;
import org.spectrumauctions.sats.core.model.UnsupportedBiddingLanguageException;

import java.io.IOException;

/**
 * @author Michael Weiss
 *
 */
public class MBVMModelOptionParser extends ModelOptionParser {

    public final static String KEY_NUMBIDDERS = "bidders";

    public MBVMModelOptionParser() {
        this.accepts(KEY_NUMBIDDERS, "The number of Bidders in the MBVM Model")
                .withRequiredArg().ofType(Integer.class);
    }

    /* (non-Javadoc)
     * @see ModelOptionParser#getModel()
     */
    @Override
    protected Model getModel() {
        return Model.MBVM;
    }

    /* (non-Javadoc)
     * @see ModelOptionParser#treatResult(java.lang.String[])
     */
    @Override
    public PathResult treatResult(String[] args)
            throws IllegalConfigException, UnsupportedBiddingLanguageException, IOException {
        MBVMModelCreator.Builder builder = new MBVMModelCreator.Builder();
        OptionSet options = this.parse(args);
        if (options.has(KEY_NUMBIDDERS)) {
            builder.setNumberOfBidders((Integer) options.valueOf(KEY_NUMBIDDERS));
        }
        return allModelsResultTreating(options, builder);
    }

}
