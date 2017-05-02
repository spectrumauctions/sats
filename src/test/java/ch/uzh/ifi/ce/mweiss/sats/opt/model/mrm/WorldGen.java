/**
 * Copyright by Michael Weiss, weiss.michael@gmx.ch
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package ch.uzh.ifi.ce.mweiss.sats.opt.model.mrm;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import ch.uzh.ifi.ce.mweiss.sats.core.model.mrm.MRMGlobalBidderSetup;
import ch.uzh.ifi.ce.mweiss.sats.core.model.mrm.MRMLocalBidderSetup;
import ch.uzh.ifi.ce.mweiss.sats.core.model.mrm.MRMRegionalBidderSetup;
import ch.uzh.ifi.ce.mweiss.sats.core.model.mrm.MRMWorldSetup;
import ch.uzh.ifi.ce.mweiss.sats.core.model.mrm.MRMWorldSetup.BandSetup;
import ch.uzh.ifi.ce.mweiss.sats.core.model.mrm.MRMWorldSetup.MRMWorldSetupBuilder;
import ch.uzh.ifi.ce.mweiss.sats.core.util.math.LinearFunction;
import ch.uzh.ifi.ce.mweiss.sats.core.util.random.DoubleInterval;
import ch.uzh.ifi.ce.mweiss.sats.core.util.random.IntegerInterval;

/**
 * @author Michael Weiss
 *
 */
public abstract class WorldGen {

    public static String BAND_A_NAME ="TEST_A";
    public static String BAND_B_NAME ="TEST_B";


    static MRMWorldSetup getSimpleWorldBuilder(){
        return getSimpleWorldBuilder(5);   
    }
    
    static MRMWorldSetup getSimpleWorldBuilder(int numberOfRegions){
        MRMWorldSetupBuilder builder = new MRMWorldSetupBuilder();
        builder.createGraphRandomly(new IntegerInterval(numberOfRegions), new IntegerInterval(2), 100, 0);
        List<BandSetup> toDelete = new ArrayList<>(builder.bandSetups().values());
        for(BandSetup bandSetup : toDelete){
            builder.removeBandSetup(bandSetup.getName());
        }       
        //Band with 2 lots and base capacity 20 where synergy = quantity
        builder.putBandSetup(new BandSetup(
                BAND_A_NAME, 
                new IntegerInterval(2), 
                new DoubleInterval(20), 
                new LinearFunction(BigDecimal.ONE, BigDecimal.ZERO))); //Syngery = Quantity
        //Band with 6 lots and base capacity 10, where synergy = quantity
        builder.putBandSetup(new BandSetup(
                BAND_B_NAME, 
                new IntegerInterval(6), 
                new DoubleInterval(10), 
                new LinearFunction(BigDecimal.ZERO, BigDecimal.ONE))); //No synergy, i.e., synergy constant 1
        return builder.build();      
    }
    
    
    public static MRMLocalBidderSetup getSimpleLocalBidderSetup(){
        MRMLocalBidderSetup.Builder builder = new MRMLocalBidderSetup.Builder();
        builder.setSetupName("Simple Local Bidder");
        builder.setNumberOfBidders(2);
        builder.setAlphaInterval(new DoubleInterval(0.4));
        builder.setBetaInterval(new DoubleInterval(0.5));
        builder.setNumberOfRegionsInterval(new IntegerInterval(3));
        return builder.build();
    }
    
    static MRMRegionalBidderSetup getSimpleRegionalBidderSetup(){
        MRMRegionalBidderSetup.Builder builder = new MRMRegionalBidderSetup.Builder();
        builder.setSetupName("Simple Regional Bidder");
        builder.setNumberOfBidders(1);
        builder.setAlphaInterval(new DoubleInterval(0.4));
        builder.setBetaInterval(new DoubleInterval(0.5));
        builder.setGammaShape(2, 2);
        return builder.build();
    }
    
    static MRMGlobalBidderSetup getSimpleGlobalBidderSetup(){
        MRMGlobalBidderSetup.Builder builder = new MRMGlobalBidderSetup.Builder();
        builder.setSetupName("Simple Global Bidder");
        builder.setNumberOfBidders(1);
        builder.setAlphaInterval(new DoubleInterval(0.4));
        builder.setBetaInterval(new DoubleInterval(0.5));
        return builder.build();
    }
    
  
       

}
