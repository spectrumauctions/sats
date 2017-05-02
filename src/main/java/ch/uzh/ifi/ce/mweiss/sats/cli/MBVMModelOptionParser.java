/**
 * Copyright by Michael Weiss, weiss.michael@gmx.ch
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package ch.uzh.ifi.ce.mweiss.sats.cli;

import java.io.IOException;

import ch.uzh.ifi.ce.mweiss.specval.api.IllegalConfigException;
import ch.uzh.ifi.ce.mweiss.specval.api.MBVMModelCreator;
import ch.uzh.ifi.ce.mweiss.specval.api.PathResult;
import ch.uzh.ifi.ce.mweiss.specval.model.UnsupportedBiddingLanguageException;
import joptsimple.OptionSet;

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
        if(options.has(KEY_NUMBIDDERS)){
            builder.setNumberOfBidders((Integer) options.valueOf(KEY_NUMBIDDERS));
        }
        return allModelsResultTreating(options, builder);
    }

}
