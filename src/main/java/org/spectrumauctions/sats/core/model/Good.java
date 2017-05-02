/**
 * Copyright by Michael Weiss, weiss.michael@gmx.ch
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.spectrumauctions.sats.core.model;

import java.io.Serializable;
import java.util.Comparator;

public abstract class Good implements Serializable {

    private final long id;
    
    protected final long worldId;

    protected Good(long id, long worldId) {
        this.id = id;
        this.worldId = worldId;
    }

    public abstract World getWorld();

    private static final long serialVersionUID = 1L;

    public long getId(){
        return id;
    }

    
    public long getWorldId() {
        return worldId;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Good other = (Good) obj;
        if (id != other.id)
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (id ^ (id >>> 32));
        return result;
    }
    
    

    public static class IdComparator implements Comparator<Good>, Serializable {

        private static final long serialVersionUID = -251782333802510799L;

        @Override
        public int compare(Good arg0, Good arg1) {
            return new Long(arg0.getId()).compareTo(new Long(arg1.getId()));
        }
    }

}
