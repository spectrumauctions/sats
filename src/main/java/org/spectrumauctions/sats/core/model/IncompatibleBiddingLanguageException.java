/**
 * Copyright by Michael Weiss, weiss.michael@gmx.ch
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.spectrumauctions.sats.core.model;

public class IncompatibleBiddingLanguageException extends UnsupportedBiddingLanguageException {

    private static final long serialVersionUID = -8225895134824650191L;

    public IncompatibleBiddingLanguageException() {
        super();
    }

    public IncompatibleBiddingLanguageException(String message, Throwable cause, boolean enableSuppression,
                                                boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public IncompatibleBiddingLanguageException(String message, Throwable cause) {
        super(message, cause);
    }

    public IncompatibleBiddingLanguageException(String message) {
        super(message);
    }

    public IncompatibleBiddingLanguageException(Throwable cause) {
        super(cause);
    }

}
