/**
 * Copyright by Michael Weiss, weiss.michael@gmx.ch
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package ch.uzh.ifi.ce.mweiss.specval.api;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import com.google.common.base.Preconditions;

import ch.uzh.ifi.ce.mweiss.specval.bidfile.FileWriter;
import ch.uzh.ifi.ce.mweiss.specval.bidlang.generic.GenericDefinition;
import ch.uzh.ifi.ce.mweiss.specval.bidlang.generic.GenericLang;
import ch.uzh.ifi.ce.mweiss.specval.bidlang.xor.XORLanguage;
import ch.uzh.ifi.ce.mweiss.specval.model.Bidder;
import ch.uzh.ifi.ce.mweiss.specval.model.DefaultModel;
import ch.uzh.ifi.ce.mweiss.specval.model.Good;
import ch.uzh.ifi.ce.mweiss.specval.model.UnsupportedBiddingLanguageException;

/**
 * @author Michael Weiss
 *
 */
public abstract class ModelCreator {
        
    private final boolean oneFile;
    private final FileType fileType;
    private final boolean generic;
    private final int bidsPerBidder;
    private final long worldSeed;
    private final long populationSeed;
    private final BiddingLanguage lang;

    private final boolean storeWorldSerialization;
    private SeedType seedType;
    private long superSeed;

    protected ModelCreator(Builder builder) {
        super();
        this.oneFile = builder.oneFile;
        this.fileType = builder.fileType;
        this.generic = builder.generic;
        this.bidsPerBidder = builder.bidsPerBidder;
        this.seedType = builder.seedType;
        this.superSeed = builder.superSeed;
        this.worldSeed = builder.worldSeed;
        this.populationSeed = builder.populationSeed;
        this.storeWorldSerialization = builder.storeWorldSerialization;
        this.lang = builder.lang;
    }

    public boolean isOneFile() {
        return oneFile;
    }

    public FileType getFileType() {
        return fileType;
    }

    public boolean isGeneric() {
        return generic;
    }

    public int getBidsPerBidder() {
        return bidsPerBidder;
    }

    public long getWorldSeed() {
        return worldSeed;
    }

   
    public long getPopulationSeed() {
        return populationSeed;
    }

    public boolean isStoreWorldSerialization() {
        return storeWorldSerialization;
    }

    public abstract PathResult generateResult(File outputFolder) throws UnsupportedBiddingLanguageException, IOException, IllegalConfigException;
    
    protected PathResult appendTopLevelParamsAndSolve(DefaultModel<?, ?> model, File outputFolder) throws UnsupportedBiddingLanguageException, IOException, IllegalConfigException{
        
        Collection<? extends Bidder<? extends Good>> bidders;
        if(seedType == SeedType.INDIVIDUALSEED){
            bidders = model.createNewPopulation(worldSeed, populationSeed);
        }else if(seedType == SeedType.NOSEED){
            bidders = model.createNewPopulation();
        }else if(seedType == SeedType.SUPERSEED){
            bidders = model.createNewPopulation(superSeed);
        }else{
            throw new IllegalConfigException("Seed type unknown");
        }
        FileWriter writer = FileType.getFileWriter(fileType, outputFolder);
       
        if(generic){
            @SuppressWarnings("unchecked")
            Class<? extends GenericLang<GenericDefinition>> langClass = (Class<? extends GenericLang<GenericDefinition>>) BiddingLanguage.getXORQLanguage(lang);
            if(oneFile){
                Collection<GenericLang<GenericDefinition>> languages = new ArrayList<>();
                for(Bidder<? extends Good> bidder : bidders){
                    languages.add(bidder.getValueFunction(langClass));
                }
                File valueFile =  writer.writeMultiBidderXORQ(languages, bidsPerBidder, "satsvalue");
                PathResult result = new PathResult(storeWorldSerialization, null);
                result.addValueFile(valueFile);
                return result;
            }else{
                throw new UnsupportedOperationException("Not yet implemented"); //TODO
            }
        }else{
            @SuppressWarnings("unchecked")
            Class<? extends XORLanguage<Good>> langClass = (Class<? extends XORLanguage<Good>>) BiddingLanguage.getXORLanguage(lang);
            if(oneFile){
                Collection<XORLanguage<Good>> languages = new ArrayList<>();
                for(Bidder<? extends Good> bidder : bidders){
                    XORLanguage<Good> language = bidder.getValueFunction(langClass);
                    languages.add(language);
                }
                File valueFile =  writer.writeMultiBidderXOR(languages, bidsPerBidder, "satsvalue");
                PathResult result = new PathResult(storeWorldSerialization, null);
                result.addValueFile(valueFile);
                return result;
            }else{
                throw new UnsupportedOperationException("Not yet implemented"); //TODO
            }
        }
    }
    
    
    public static abstract class Builder {

        private  BiddingLanguage lang;
        private boolean storeWorldSerialization;
        private long populationSeed;
        private long superSeed;
        private long worldSeed;
        private SeedType seedType;
        private int bidsPerBidder;
        private boolean generic;
        private FileType fileType;
        private boolean oneFile;

        public Builder(BiddingLanguage lang) {
            this.lang = lang;
            storeWorldSerialization = false;
            seedType = SeedType.NOSEED;
            bidsPerBidder = 100;
            generic = false;
            fileType = FileType.CATS;
            oneFile = true;
        }
        
        public abstract ModelCreator build();

        public boolean isStoreWorldSerialization() {
            return storeWorldSerialization;
        }

        public void setStoreWorldSerialization(boolean storeWorldSerialization) {
            if(storeWorldSerialization){
                throw new UnsupportedOperationException("Storing through API not yet supported");
            }
            this.storeWorldSerialization = storeWorldSerialization;
        }

        public long getPopulationSeed() {
            return populationSeed;
        }

        public void setPopulationSeed(long populationSeed) {
            this.populationSeed = populationSeed;
        }

        public long getWorldSeed() {
            return worldSeed;
        }

        public void setWorldSeed(long worldSeed) {
            this.worldSeed = worldSeed;
        }

        public int getBidsPerBidder() {
            return bidsPerBidder;
        }

        public void setSuperSeed(long superSeed) {
            this.superSeed = superSeed;
        }

        public void setSeedType(SeedType seedType) {
            this.seedType = seedType;
        }

        public void setLang(BiddingLanguage lang) {
            this.lang = lang;
        }

        public void setBidsPerBidder(int bidsPerBidder) throws IllegalConfigException {
            try{
                Preconditions.checkArgument(bidsPerBidder > 0, "%s is not a valid number of bids per bidder", bidsPerBidder);
            }catch (IllegalArgumentException e) {
                throw new IllegalConfigException(e.getMessage());
            }
            this.bidsPerBidder = bidsPerBidder;
        }

        public boolean isGeneric() {
            return generic;
        }

        public void setGeneric(boolean generic) {
            this.generic = generic;
        }

        public FileType getFileType() {
            return fileType;
        }

        public void setFileType(FileType fileType) {
            this.fileType = fileType;
        }

        public boolean isOneFile() {
            return oneFile;
        }

        public void setOneFile(boolean oneFile) {
            this.oneFile = oneFile;
        }

    }
}
