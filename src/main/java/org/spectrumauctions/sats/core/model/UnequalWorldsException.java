/**
 * Copyright by Michael Weiss, weiss.michael@gmx.ch
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.spectrumauctions.sats.core.model;

/**
 * An unchecked exception, thought to be thrown if goods from different world are inserted in the same bundle
 * 
 * @author Michael Weiss
 *
 */
public class UnequalWorldsException extends RuntimeException {

    public UnequalWorldsException() {
        super();
    }

    public UnequalWorldsException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public UnequalWorldsException(String message, Throwable cause) {
        super(message, cause);
    }

    public UnequalWorldsException(String message) {
        super(message);
    }

    
    public UnequalWorldsException(Throwable cause) {
        super(cause);
    }

}