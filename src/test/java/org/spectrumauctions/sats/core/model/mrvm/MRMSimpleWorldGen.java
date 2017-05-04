/**
 * Copyright by Michael Weiss, weiss.michael@gmx.ch
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.spectrumauctions.sats.core.model.mrvm;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.spectrumauctions.sats.core.util.math.LinearFunction;
import org.spectrumauctions.sats.core.util.random.DoubleInterval;
import org.spectrumauctions.sats.core.util.random.IntegerInterval;

/**
 * Provides accessors to some very basic simple world and bidder setups 
 * and contains a couple of trivial test methods to check their validity.
 * 
 * @author Michael Weiss
 *
 */
public class MRMSimpleWorldGen {

    static MRVMWorldSetup getSimpleWorldBuilder(){
        MRVMWorldSetup.MRMWorldSetupBuilder builder = new MRVMWorldSetup.MRMWorldSetupBuilder();
        builder.createGraphRandomly(new IntegerInterval(5), new IntegerInterval(2), 100, 0);
        List<MRVMWorldSetup.BandSetup> setups = new ArrayList<>(builder.bandSetups().values());
        for(MRVMWorldSetup.BandSetup bandSetup : setups ){
            builder.removeBandSetup(bandSetup.getName());
        }       
        //Band with 2 lots and base capacity 20, no synergies
        builder.putBandSetup(new MRVMWorldSetup.BandSetup(
                "TEST_A", 
                new IntegerInterval(2), 
                new DoubleInterval(20),
                new LinearFunction(BigDecimal.ONE, BigDecimal.ZERO))); //Syngery = Quantity
        //Band with 6 lots and base capacity 10, no synergies
        builder.putBandSetup(new MRVMWorldSetup.BandSetup(
                "TEST_B", 
                new IntegerInterval(6), 
                new DoubleInterval(10), 
                new LinearFunction(BigDecimal.ZERO, BigDecimal.ONE))); //No synergy, i.e., synergy constant 1
        return builder.build();      
    }
    
  
       
    static MRVMLocalBidderSetup getSimpleLocalBidderSetup(){
        MRVMLocalBidderSetup.Builder builder = new MRVMLocalBidderSetup.Builder();
        builder.setSetupName("Simple Local Bidder");
        builder.setNumberOfBidders(1);
        builder.setAlphaInterval(new DoubleInterval(0.4));
        builder.setBetaInterval(new DoubleInterval(0.5));
        builder.setNumberOfRegionsInterval(new IntegerInterval(3));
        return builder.build();
    }
    
    static MRVMRegionalBidderSetup getSimpleRegionalBidderSetup(){
        MRVMRegionalBidderSetup.Builder builder = new MRVMRegionalBidderSetup.Builder();
        builder.setSetupName("Simple Regional Bidder");
        builder.setNumberOfBidders(1);
        builder.setAlphaInterval(new DoubleInterval(0.4));
        builder.setBetaInterval(new DoubleInterval(0.5));
        builder.setGammaShape(2, 2);
        return builder.build();
    }
    
    static MRVMNationalBidderSetup getSimpleGlobalBidderSetup(){
        MRVMNationalBidderSetup.Builder builder = new MRVMNationalBidderSetup.Builder();
        builder.setSetupName("Simple Global Bidder");
        builder.setNumberOfBidders(1);
        builder.setAlphaInterval(new DoubleInterval(0.4));
        builder.setBetaInterval(new DoubleInterval(0.5));
        return builder.build();
    }
}
