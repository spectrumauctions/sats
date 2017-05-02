/**
 * Copyright by Michael Weiss, weiss.michael@gmx.ch
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.spectrumauctions.sats.core.model;

/**
 * Thrown if an attempt to change an unmodifiable instance is made.
 * 
 * @author Michael Weiss
 *
 */
public class UnmodifiableInstanceException extends RuntimeException {

    public UnmodifiableInstanceException() {
        super();
    }

    public UnmodifiableInstanceException(String message, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public UnmodifiableInstanceException(String message, Throwable cause) {
        super(message, cause);
    }

    public UnmodifiableInstanceException(String message) {
        super(message);
    }

    public UnmodifiableInstanceException(Throwable cause) {
        super(cause);
    }

}
