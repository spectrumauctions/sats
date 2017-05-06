/**
 * Copyright by Michael Weiss, weiss.michael@gmx.ch
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.spectrumauctions.sats.core.bidfile;

import org.spectrumauctions.sats.core.bidlang.generic.GenericDefinition;
import org.spectrumauctions.sats.core.bidlang.generic.GenericLang;
import org.spectrumauctions.sats.core.bidlang.xor.XORLanguage;
import org.spectrumauctions.sats.core.model.Good;
import org.spectrumauctions.sats.core.util.CacheMap;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;

/**
 * @author Michael Weiss
 *
 */
public abstract class FileWriter {

    public static final int VALUE_STRING_MAXLENGTH = 7;

    public abstract File writeMultiBidderXOR(Collection<XORLanguage<Good>> valueFunctions, int numberOfBids, String filePrefix)
            throws IOException;

    public abstract File writeSingleBidderXOR(XORLanguage<Good> valueFunction, int numberOfBids, String filePrefix) throws IOException;

    public abstract File writeMultiBidderXORQ(Collection<GenericLang<GenericDefinition>> valueFunctions, int numberOfBids, String filePrefix)
            throws IOException;

    public abstract File writeSingleBidderXORQ(GenericLang<GenericDefinition> lang, int numberOfBids, String filePrefix) throws IOException;

    /**
     * The file ending of the generated bid files
     * @return
     */
    protected abstract String filetype();

    protected final File folder;
    private String defaultFilePrefix = "";
    private CacheMap<String, Integer> fileNameCount = new CacheMap<>(30);

    /**
     *
     */
    public FileWriter(File path) {
        super();
        if (!path.isDirectory()) {
            path.mkdir();
        }
        this.folder = path;
    }

    protected String roundedValue(double value) {
        String result = String.valueOf(value);
        if (result.length() > VALUE_STRING_MAXLENGTH) {
            result = result.substring(0, VALUE_STRING_MAXLENGTH);
        }
        return result;
    }

    protected Path nextNonexistingFile(String filePrefix) {
        Integer cashedCount = fileNameCount.get(filePrefix);
        if (cashedCount == null)
            cashedCount = 0;
        boolean searching = true;
        File candidate;
        do {
            candidate = getFile(filePrefix, cashedCount);
            if (candidate.exists() && candidate.isFile()) {
                cashedCount++;
            } else {
                // Found next nonexisting file
                searching = false;
            }
        } while (searching);
        fileNameCount.put(filePrefix, cashedCount + 1);
        return candidate.toPath();
    }

    private File getFile(String filePrefix, int count) {
        String fileName = filePrefix.concat(String.valueOf(count)).concat(".").concat(filetype());
        fileName = folder.getAbsolutePath().concat("/" + fileName);
        return new File(fileName);
    }

    public String getDefaultFilePrefix() {
        return defaultFilePrefix;
    }

    public void setDefaultFilePrefix(String defaultFilePrefix) {
        this.defaultFilePrefix = defaultFilePrefix;
    }

    public File getFolder() {
        return folder;
    }

}