/**
 * Copyright by Michael Weiss, weiss.michael@gmx.ch
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.spectrumauctions.sats.core.model;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.EqualsAndHashCode;
import org.marketdesignresearch.mechlib.domain.Good;

import java.io.Serializable;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

@EqualsAndHashCode
public abstract class GenericGood implements SATSGood, Serializable {

    private final String name;

    protected final long worldId;

    protected GenericGood(String name, long worldId) {
        this.name = name;
        this.worldId = worldId;
    }

    @Override
    public abstract World getWorld();
    public abstract List<? extends Good> containedGoods();

    @Override
    public int available() {
        return containedGoods().size();
    }

    private static final long serialVersionUID = 1345L;

    @Override
    public String getId() {
        return name;
    }

    public long getWorldId() {
        return worldId;
    }

    public JsonElement shortJson() {
        JsonObject json = new JsonObject();
        json.addProperty("good", getId());
        return json;
    }

}
