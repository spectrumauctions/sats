/**
 * Copyright by Michael Weiss, weiss.michael@gmx.ch
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.spectrumauctions.sats.core.model.srvm;

import com.google.common.base.Preconditions;

import org.spectrumauctions.sats.core.model.Good;

public final class SRVMLicense extends Good{

    private static final long serialVersionUID = 7672703280459172931L;
    
    private final String bandName;
    private transient SRVMBand band;
    
    /**
     * @param id
     * @param worldId
     */
    SRVMLicense(long id, SRVMBand band) {
        super(id, band.getWorld().getId());
        this.band = band;
        this.bandName = band.getName();
    }
    
    public SRVMBand getBand() {
        return band;
    }


    /* (non-Javadoc)
     * @see Good#getWorld()
     */
    @Override
    public SRVMWorld getWorld() {
        return band.getWorld();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((bandName == null) ? 0 : bandName.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        SRVMLicense other = (SRVMLicense) obj;
        if (bandName == null) {
            if (other.bandName != null)
                return false;
        } else if (!bandName.equals(other.bandName))
            return false;
        return true;
    }

    /**
     * See {@link SRVMWorld#refreshFieldBackReferences()} for purpose of this method
     */
    void refreshFieldBackReferences(SRVMBand band) {
        Preconditions.checkArgument(band.getName().equals(this.bandName));
        this.band = band;
    }
    
    


   
}