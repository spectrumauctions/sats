/**
 * Copyright by Michael Weiss, weiss.michael@gmx.ch
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.spectrumauctions.sats.core.model.srvm;

import com.google.common.base.Preconditions;
import lombok.EqualsAndHashCode;
import org.spectrumauctions.sats.core.model.License;

@EqualsAndHashCode(callSuper = true)
public final class SRVMLicense extends License {

    private static final long serialVersionUID = 7672703280459172931L;

    private final String bandName;
    private transient SRVMBand band;

    SRVMLicense(long id, SRVMBand band) {
        super(id, band.getWorld().getId());
        this.band = band;
        this.bandName = band.getId();
    }

    public SRVMBand getBand() {
        return band;
    }


    /**
     * @see License#getWorld()
     */
    @Override
    public SRVMWorld getWorld() {
        return band.getWorld();
    }

    /**
     * @see SRVMWorld#refreshFieldBackReferences() for purpose of this method
     */
    void refreshFieldBackReferences(SRVMBand band) {
        Preconditions.checkArgument(band.getId().equals(this.bandName));
        this.band = band;
    }


}