/**
 * Copyright by Michael Weiss, weiss.michael@gmx.ch
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.spectrumauctions.sats.core.api;

import org.spectrumauctions.sats.core.bidlang.generic.BidderSpecificGeneric;
import org.spectrumauctions.sats.core.bidlang.generic.GenericLang;
import org.spectrumauctions.sats.core.bidlang.generic.SizeOrderedPowerset.GenericPowersetDecreasing;
import org.spectrumauctions.sats.core.bidlang.generic.SizeOrderedPowerset.GenericPowersetIncreasing;
import org.spectrumauctions.sats.core.bidlang.xor.BidderSpecificXOR;
import org.spectrumauctions.sats.core.bidlang.xor.DecreasingSizeOrderedXOR;
import org.spectrumauctions.sats.core.bidlang.xor.IncreasingSizeOrderedXOR;
import org.spectrumauctions.sats.core.bidlang.xor.SizeBasedUniqueRandomXOR;
import org.spectrumauctions.sats.core.bidlang.xor.XORLanguage;

/**
 * @author Michael Weiss
 *
 */
public enum BiddingLanguage {

    SIZE_INCREASING, SIZE_DECREASING, RANDOM, BIDDER_SPECIFIC;
    
    @SuppressWarnings("rawtypes")
    public static Class<? extends XORLanguage> getXORLanguage(BiddingLanguage type) throws IllegalConfigException{
        if(type == SIZE_INCREASING){
            return IncreasingSizeOrderedXOR.class;
        }else if(type == SIZE_DECREASING){
            return DecreasingSizeOrderedXOR.class;
        }else if(type == RANDOM){
            return SizeBasedUniqueRandomXOR.class;
        }else if(type == BiddingLanguage.BIDDER_SPECIFIC){
            return BidderSpecificXOR.class;
        }else{
            if(type == null){
                throw new IllegalArgumentException("Language must not be null");
            }
            throw new IllegalConfigException("Illegal Language: " + type);
        }
    }
    
    @SuppressWarnings("rawtypes")
    public static Class<? extends GenericLang> getXORQLanguage(BiddingLanguage type) throws IllegalConfigException{
        if(type == SIZE_INCREASING){
            return GenericPowersetIncreasing.class;
        }else if(type == SIZE_DECREASING){
            return GenericPowersetDecreasing.class;
        }else if(type == RANDOM){
            throw new IllegalConfigException("Random Iterator of generic bids not yet implemented");
        }else if(type == BiddingLanguage.BIDDER_SPECIFIC){
            return BidderSpecificGeneric.class;
        }else{
            if(type == null){
                throw new IllegalArgumentException("Language must not be null");
            }
            throw new IllegalConfigException("Illegal Language: " + type);
        }
    }
}
