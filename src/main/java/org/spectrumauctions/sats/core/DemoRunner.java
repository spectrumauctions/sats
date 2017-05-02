/**
 * Copyright by Michael Weiss, weiss.michael@gmx.ch
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.spectrumauctions.sats.core;

import java.util.Iterator;

import org.spectrumauctions.sats.core.bidlang.xor.XORValue;
import org.spectrumauctions.sats.core.model.Bidder;
import org.spectrumauctions.sats.core.model.UnsupportedBiddingLanguageException;
import org.spectrumauctions.sats.core.model.mrm.MultiRegionModel;
import org.spectrumauctions.sats.core.bidlang.xor.SizeOrderedXOR;
import org.spectrumauctions.sats.core.bidlang.xor.XORLanguage;
import org.spectrumauctions.sats.core.model.DefaultModel;


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
