/**
 * Copyright by Michael Weiss, weiss.michael@gmx.ch
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.spectrumauctions.sats.core.model.srm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.spectrumauctions.sats.core.model.World;
import com.google.common.collect.ImmutableSet;

import org.spectrumauctions.sats.core.model.Bidder;
import org.spectrumauctions.sats.core.util.random.RNGSupplier;

/**
 * @author Michael Weiss
 *
 */
public final class SRMWorld extends World {
    
    private static final long serialVersionUID = 1766287015715986936L;
    private final Set<SRMBand> bands;
    
    private transient Integer numberOfGoods = null;
    private transient ImmutableSet<SRMLicense> licenses = null;
    
    public SRMWorld(SRMWorldSetup setup, RNGSupplier rngSupplier){
        super("Single-Region Value Model");
        this.bands = Collections.unmodifiableSet(new HashSet<>(SRMBand.createBands(this, setup, rngSupplier)));
        store();
    }
    
    


    /* (non-Javadoc)
     * @see World#getNumberOfGoods()
     */
    @Override
    public int getNumberOfGoods() {
        if(numberOfGoods == null){
            int count = 0;
            for(SRMBand band : bands){
                count += band.getLicenses().size();
            }
            numberOfGoods = count;
        }
        return numberOfGoods;
    }

    /**
     * {@inheritDoc}
     * @return An immutable set containing all licenses.
     */
    @Override
    public ImmutableSet<SRMLicense> getLicenses() {
        if(licenses == null){
            ImmutableSet.Builder<SRMLicense> builder = ImmutableSet.builder();
            for(SRMBand band : bands){
                builder.addAll(band.getLicenses());
            }
            this.licenses = builder.build();
        }
        return licenses;
    }
    
    
    public Set<SRMBand> getBands(){
        return Collections.unmodifiableSet(bands);
    }
    
    public List<SRMBidder> createPopulation(Collection<SRMBidderSetup> bidderSetups, RNGSupplier rngSupplier){
        long population = openNewPopulation();
        long currentId = 0;
        List<SRMBidder> bidders = new ArrayList<>();
       for(SRMBidderSetup setup : bidderSetups){
           for(int i = 0; i < setup.getNumberOfBidders(); i++){
               bidders.add(new SRMBidder(setup, this, currentId++, population, rngSupplier));
           }
       }
       return bidders;
    }
    

    /* (non-Javadoc)
     * @see World#restorePopulation(long)
     */
    @Override
    public Collection<? extends Bidder<SRMLicense>> restorePopulation(long populationId) {
        return super.restorePopulation(SRMBidder.class, populationId);
    }

    /* (non-Javadoc)
     * @see World#refreshFieldBackReferences()
     */
    @Override
    public void refreshFieldBackReferences() {
        for(SRMBand band : bands){
            band.refreshFieldBackReferences(this);
        }
    }
    
    


}
