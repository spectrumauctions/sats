/**
 * Copyright by Michael Weiss, weiss.michael@gmx.ch
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package ch.uzh.ifi.ce.mweiss.specval.api;

import java.io.File;

import ch.uzh.ifi.ce.mweiss.specval.bidfile.CatsExporter;
import ch.uzh.ifi.ce.mweiss.specval.bidfile.FileWriter;
import ch.uzh.ifi.ce.mweiss.specval.bidfile.JsonExporter;

/**
 * @author Michael Weiss
 *
 */
public enum FileType {
    
    CATS, JSON;
    
    public static FileWriter getFileWriter(FileType type, File path){
        if(type == CATS){
            return new CatsExporter(path);
        }else if(type == JSON){
            return new JsonExporter(path);
        }else{
            if(type == null){
                throw new IllegalArgumentException("FileType must not be null");
            }
            throw new IllegalArgumentException("Illegal FileType: " + type);
        }
    }
}
