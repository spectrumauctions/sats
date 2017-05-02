/**
 * Copyright by Michael Weiss, weiss.michael@gmx.ch
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package ch.uzh.ifi.ce.mweiss.sats.core;

import java.util.Iterator;

import ch.uzh.ifi.ce.mweiss.sats.core.bidlang.xor.XORValue;
import ch.uzh.ifi.ce.mweiss.sats.core.model.Bidder;
import ch.uzh.ifi.ce.mweiss.sats.core.model.UnsupportedBiddingLanguageException;
import ch.uzh.ifi.ce.mweiss.sats.core.model.mrm.MultiRegionModel;
import ch.uzh.ifi.ce.mweiss.sats.core.bidlang.xor.SizeOrderedXOR;
import ch.uzh.ifi.ce.mweiss.sats.core.bidlang.xor.XORLanguage;
import ch.uzh.ifi.ce.mweiss.sats.core.model.DefaultModel;


/**
 * Simply for illustration purpose - this code is not supposed to run
 * @author Michael Weiss
 *
 */
public class DemoRunner {

    static YourAuction yourAuction;
    
    /**
     * @param args
     * @throws UnsupportedBiddingLanguageException
     */
    public static void main(String[] args) throws UnsupportedBiddingLanguageException {   
       
        
       DefaultModel<?,?> model = new MultiRegionModel();
       for(Bidder<?> bidder : model.createNewPopulation()){
            XORLanguage<?> xorBids = bidder.getValueFunction(SizeOrderedXOR.class);
            Iterator<? extends XORValue<?>> bidsIterator = xorBids.iterator();
            while(bidsIterator.hasNext()){
                XORValue<?> bid = bidsIterator.next();
                yourAuction.add(bid.getLicenses(), bid.getValue());
            }      
       }
      
       
    }
    
    
    
    public static interface YourAuction{
        void add(Object o, Object o2);
    }

}
