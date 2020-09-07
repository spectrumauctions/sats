/**
 * Copyright by Michael Weiss, weiss.michael@gmx.ch
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.spectrumauctions.sats.core.util.file.gson;

import com.google.gson.*;
import org.jgrapht.Graph;
import org.marketdesignresearch.mechlib.core.allocationlimits.AllocationLimit;
import org.marketdesignresearch.mechlib.core.allocationlimits.AllocationLimit.NoAllocationLimit;
import org.marketdesignresearch.mechlib.core.allocationlimits.BundleSizeAllocationLimit;
import org.marketdesignresearch.mechlib.core.allocationlimits.BundleSizeAndGoodAllocationLimit;
import org.marketdesignresearch.mechlib.core.allocationlimits.GoodAllocationLimit;
import org.spectrumauctions.sats.core.model.World;
import org.spectrumauctions.sats.core.util.file.FileException;

/**
 * @author Michael Weiss
 *
 */
public class GsonWrapper {

    private static final String IMPLEMENTATION_FIELD = "implementation";
    private static final boolean PRETTY_JSON = true;

    private Gson gson;
    
    private AllocationLimitAdapter allocationLimitAdapter = new AllocationLimitAdapter();

    public GsonWrapper() {
        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(Graph.class, new GraphAdapter());
        builder.registerTypeAdapter(AllocationLimit.class, allocationLimitAdapter);
        builder.registerTypeAdapter(NoAllocationLimit.class, allocationLimitAdapter);
        builder.registerTypeAdapter(BundleSizeAllocationLimit.class, allocationLimitAdapter);
        builder.registerTypeAdapter(BundleSizeAndGoodAllocationLimit.class, allocationLimitAdapter);
        builder.registerTypeAdapter(GoodAllocationLimit.class, allocationLimitAdapter);
        builder.disableHtmlEscaping();
        if (PRETTY_JSON) {
            builder.setPrettyPrinting();
        }
        gson = builder.create();
    }
    
    public Gson getGson() {
        return gson;
    }

    public <T extends Object> T fromJson(Class<T> type, String json) {
        T object = gson.fromJson(json, type);
        return object;
    }


    public String toJson(Object object) {
        JsonElement jsonElement = gson.toJsonTree(object);
        jsonElement.getAsJsonObject().addProperty(IMPLEMENTATION_FIELD, object.getClass().getName());
        return gson.toJson(jsonElement);
    }

    public Class<?> readClass(String json) {
        String typeString = readField(IMPLEMENTATION_FIELD, json);
        try {
            return Class.forName(typeString);
        } catch (ClassNotFoundException e) {
            throw new FileException("Type Unknown", e);
        }
    }

    public String readField(String key, String json) {
        JsonParser parser = new JsonParser();
        JsonObject jsonObject = parser.parse(json).getAsJsonObject();
        return jsonObject.get(key).getAsString();
    }

	public void setWorld(World world) {
		this.allocationLimitAdapter.setWorld(world);
	}

}
