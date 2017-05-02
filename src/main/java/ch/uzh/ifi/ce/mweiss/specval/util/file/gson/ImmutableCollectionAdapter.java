/**
 * Copyright by Michael Weiss, weiss.michael@gmx.ch
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package ch.uzh.ifi.ce.mweiss.specval.util.file.gson;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.Map.Entry;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.tyler.gson.immutable.TypeAdapters;

/**
 * @author Michael Weiss
 *
 */
public abstract class ImmutableCollectionAdapter {

    public static void registerAllCollectionTypes(GsonBuilder gsonBuilder){
        Map<Type, JsonDeserializer<?>> adapters = TypeAdapters.immutableTypeMap();
        for(Entry<Type, JsonDeserializer<?>> adapter : adapters.entrySet()){
            gsonBuilder.registerTypeAdapter(adapter.getKey(), adapter.getValue());
        }
    }
    
    
}
