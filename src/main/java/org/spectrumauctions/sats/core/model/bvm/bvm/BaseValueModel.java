/**
 * Copyright by Michael Weiss, weiss.michael@gmx.ch
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.spectrumauctions.sats.core.model.bvm.bvm;

import java.util.ArrayList;
import java.util.List;

import org.spectrumauctions.sats.core.model.bvm.BMBidder;
import org.spectrumauctions.sats.core.model.bvm.BMBidderSetup;
import org.spectrumauctions.sats.core.model.bvm.BMWorld;
import org.spectrumauctions.sats.core.model.bvm.BMWorldSetup;
import com.google.common.base.Preconditions;

import org.spectrumauctions.sats.core.model.DefaultModel;
import org.spectrumauctions.sats.core.util.random.RNGSupplier;

/**
 * @author Michael Weiss
 *
 */
public class BaseValueModel extends DefaultModel<BMWorld, BMBidder> {
    
    BVMWorldSetup.BVMWorldSetupBuilder worldSetupBuilder;
    BVMBidderSetup.BVMBidderSetupBuilder bidderSetupBuilder;
    
    
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
        this.worldSetupBuilder = new BVMWorldSetup.BVMWorldSetupBuilder(BVMWorldSetup.DEFAULT_SETUP_NAME);
        this.bidderSetupBuilder = new BVMBidderSetup.BVMBidderSetupBuilder(BVMBidderSetup.DEFAULT_SETUP_NAME, numberOfBidders);
    }

    /* (non-Javadoc)
     * @see org.spectrumauctions.sats.core.model.QuickDefaultAccess#createWorld(RNGSupplier)
     */
    @Override
    public BMWorld createWorld(RNGSupplier rngSupplier) {
        BMWorldSetup setup = BVMWorldSetup.getDefaultSetup();
        return new BMWorld(setup, rngSupplier);
    }

    /* (non-Javadoc)
     * @see org.spectrumauctions.sats.core.model.QuickDefaultAccess#createPopulation(World, RNGSupplier)
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
