/**
 * Copyright by Michael Weiss, weiss.michael@gmx.ch
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package ch.uzh.ifi.ce.mweiss.specval.model.bm.bvm;

import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Preconditions;

import ch.uzh.ifi.ce.mweiss.specval.model.DefaultModel;
import ch.uzh.ifi.ce.mweiss.specval.model.bm.BMBidder;
import ch.uzh.ifi.ce.mweiss.specval.model.bm.BMBidderSetup;
import ch.uzh.ifi.ce.mweiss.specval.model.bm.BMWorld;
import ch.uzh.ifi.ce.mweiss.specval.model.bm.BMWorldSetup;
import ch.uzh.ifi.ce.mweiss.specval.model.bm.bvm.BVMBidderSetup.BVMBidderSetupBuilder;
import ch.uzh.ifi.ce.mweiss.specval.model.bm.bvm.BVMWorldSetup.BVMWorldSetupBuilder;
import ch.uzh.ifi.ce.mweiss.specval.util.random.RNGSupplier;

/**
 * @author Michael Weiss
 *
 */
public class BaseValueModel extends DefaultModel<BMWorld, BMBidder> {
    
    BVMWorldSetupBuilder worldSetupBuilder;
    BVMBidderSetupBuilder bidderSetupBuilder;
    
    
    /**
     * Creates a new QuickAccessor to the Base Value Model in its default configuration.
     * Note: Since the orginal model definition does not specify the number of bidders, 
     * it is recommended to use {@link BaseValueModel#BaseValueModel(int)} for construction. 
     * Otherwise, 5 bidders are created by default.
     */
    public BaseValueModel(){
        this(5);
    }
    
    
    /**
     * Creates a new QuickAccessor to the Base Value Model in its default configuration.
     * @param numberOfBidders specifies how many bidders there will be created in each population
     */
    public BaseValueModel(int numberOfBidders){
        this.worldSetupBuilder = new BVMWorldSetupBuilder(BVMWorldSetup.DEFAULT_SETUP_NAME);
        this.bidderSetupBuilder = new BVMBidderSetupBuilder(BVMBidderSetup.DEFAULT_SETUP_NAME, numberOfBidders);      
    }

    /* (non-Javadoc)
     * @see ch.uzh.ifi.ce.mweiss.specval.model.QuickDefaultAccess#createWorld(ch.uzh.ifi.ce.mweiss.specval.util.random.RNGSupplier)
     */
    @Override
    public BMWorld createWorld(RNGSupplier rngSupplier) {
        BMWorldSetup setup = BVMWorldSetup.getDefaultSetup();
        return new BMWorld(setup, rngSupplier);
    }

    /* (non-Javadoc)
     * @see ch.uzh.ifi.ce.mweiss.specval.model.QuickDefaultAccess#createPopulation(ch.uzh.ifi.ce.mweiss.specval.model.World, ch.uzh.ifi.ce.mweiss.specval.util.random.RNGSupplier)
     */
    @Override
    public List<BMBidder> createPopulation(BMWorld world, RNGSupplier populationRNG) {
        List<BMBidderSetup> setupset = new ArrayList<>();
        setupset.add(bidderSetupBuilder.build());
        return world.createPopulation(setupset, populationRNG);
    }

    /**
     * @return The number of bidders to be created
     */
    public int getNumberOfBidders() {
        return bidderSetupBuilder.getNumberOfBidders();
    }

    /**
     * Set the number of bidders to be created
     * @param numberOfBidders
     */
    public void setNumberOfBidders(int numberOfBidders) {
        Preconditions.checkArgument(numberOfBidders > 0);
        bidderSetupBuilder.setNumberOfBidders(numberOfBidders);
    }
    
    /**
     * Call this method when worldBuilder is changed (away from default config) 
     * to ensure the {@link BMWorldSetup#getSetupName()} is changed
     */
    protected void modifyWorldBuilder(){
        worldSetupBuilder.setSetupName("MODIFIED_MBVM");
    }
    
    /**
     * Call this method when bidderBuilder is changed (away from default config) 
     * to ensure the {@link BMBidderSetup#getSetupName()} is changed
     */
    protected void modifyBidderBuilder(){
        bidderSetupBuilder.setSetupName("MODIFIED_MBVM");
    }
    

}
