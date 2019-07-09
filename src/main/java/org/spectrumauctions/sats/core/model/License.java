/**
 * Copyright by Michael Weiss, weiss.michael@gmx.ch
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.spectrumauctions.sats.core.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.io.Serializable;
import java.util.Comparator;
import java.util.UUID;

@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public abstract class License implements SATSGood, Serializable {

    @Getter
    private final UUID uuid = UUID.randomUUID();

    @EqualsAndHashCode.Include
    private final long longId;
    @Getter
    private final String name;

    protected final long worldId;

    protected License(long longId, long worldId) {
        this.longId = longId;
        this.name = toAlphabetic(longId);
        this.worldId = worldId;
    }

    @Override
    public abstract World getWorld();

    private static final long serialVersionUID = 1L;

    public long getLongId() {
        return longId;
    }

    public long getWorldId() {
        return worldId;
    }


    public static class IdComparator implements Comparator<License>, Serializable {

        private static final long serialVersionUID = -251782333802510799L;

        private static Comparator<License> comparator = Comparator.comparingLong(License::getLongId);

        @Override
        public int compare(License arg0, License arg1) {
            return comparator.compare(arg0, arg1);
        }
    }

    private static String toAlphabetic(long i) {
        if (i < 0) {
            return "-" + toAlphabetic(-i - 1);
        }

        long quot = i / 26L;
        long rem = i % 26;
        char letter = (char) ((int) 'A' + rem);
        if (quot == 0) {
            return "" + letter;
        } else {
            return toAlphabetic(quot - 1) + letter;
        }
    }

}
