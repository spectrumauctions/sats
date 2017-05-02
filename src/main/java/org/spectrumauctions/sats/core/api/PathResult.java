/**
 * Copyright by Michael Weiss, weiss.michael@gmx.ch
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.spectrumauctions.sats.core.api;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Michael Weiss
 *
 */
public final class PathResult {

    private List<File> valueFiles;
    private boolean areInstancesSerialized;
    private File instanceFolder;
    
    
    public PathResult(boolean areInstancesSerialized, File instanceFolder) {
        super();
        this.valueFiles = new ArrayList<>();
        this.areInstancesSerialized = areInstancesSerialized;
        this.instanceFolder = instanceFolder;
    }
    
    public void addValueFile(File file ){
        valueFiles.add(file);
    }

    public List<File> getValueFiles() {
        return valueFiles;
    }

    public boolean isAreInstancesSerialized() {
        return areInstancesSerialized;
    }

    public void setAreInstancesSerialized(boolean areInstancesSerialized) {
        this.areInstancesSerialized = areInstancesSerialized;
    }

    public File getInstanceFolder() {
        return instanceFolder;
    }

    public void setInstanceFolder(File instanceFolder) {
        this.instanceFolder = instanceFolder;
    }
    
    
    
}
