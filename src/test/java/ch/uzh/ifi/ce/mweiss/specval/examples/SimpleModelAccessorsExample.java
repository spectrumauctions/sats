package ch.uzh.ifi.ce.mweiss.specval.examples;

import ch.uzh.ifi.ce.mweiss.specval.model.*;
import ch.uzh.ifi.ce.mweiss.specval.model.bm.bvm.BaseValueModel;
import ch.uzh.ifi.ce.mweiss.specval.model.bm.mbvm.MultiBandValueModel;
import ch.uzh.ifi.ce.mweiss.specval.model.mrm.MRMWorld;
import ch.uzh.ifi.ce.mweiss.specval.model.mrm.MultiRegionModel;
import ch.uzh.ifi.ce.mweiss.specval.model.srm.SRMBidder;
import ch.uzh.ifi.ce.mweiss.specval.model.srm.SRMWorld;
import ch.uzh.ifi.ce.mweiss.specval.model.srm.SRMWorldSetup;
import ch.uzh.ifi.ce.mweiss.specval.model.srm.SingleRegionModel;
import ch.uzh.ifi.ce.mweiss.specval.util.random.JavaUtilRNGSupplier;
import ch.uzh.ifi.ce.mweiss.specval.util.random.RNGSupplier;
import org.junit.Test;
import sun.security.krb5.internal.LastReq;
import sun.security.krb5.internal.tools.Klist;

import java.math.BigDecimal;
import java.util.*;

/**
 * <p>This shows how to use the simple accessors provided for the models. </p>
 * <p>Each model provides such an easy accessor ({@link DefaultModel}), eg. {@link MultiRegionModel}, {@link SingleRegionModel}, {@link BaseValueModel}, {@link
 * MultiBandValueModel}</p> and so forth.</p>
 * <p> Using these accessors allow the most simple and straight forward use of sats, however, more complex parameters can not be done using the {@link DefaultModel}
 */
public class SimpleModelAccessorsExample {

    /**
     * In this example we create a new world for the <i>Single Region Value Model</i>, as well as a set of bidders for this world.
     */
    @Test
    public void exampleOne(){
        SingleRegionModel singleRegionModel = new SingleRegionModel();
        SRMWorld world = singleRegionModel.createWorld();
        List<SRMBidder> bidders = singleRegionModel.createPopulation(world);
        treatBidders(bidders);
    }


    /**
     * Now we want to to the same as in {@link #exampleOne()}, but modify the number of bidders
     */
    @Test
    public void exampleTwo(){
        SingleRegionModel singleRegionModel = new SingleRegionModel();
        SRMWorld world = singleRegionModel.createWorld();
        singleRegionModel.setNumberOfHighFrequencyBidders(1);
        singleRegionModel.setNumberOfPrimaryBidders(2);
        singleRegionModel.setNumberOfSecondaryBidders(0);
        singleRegionModel.setNumberOfSmallBidders(0);
        List<SRMBidder> bidders = singleRegionModel.createPopulation(world);
        treatBidders(bidders);
    }

    /**
     * In this example, we do again the same as in {@link #exampleOne()}, but we want to set a seed to allow reproduction
     */
    @Test
    public void exampleThree(){
        SingleRegionModel singleRegionModel = new SingleRegionModel();
        RNGSupplier rngSupplier1 = new JavaUtilRNGSupplier("MY SEED".hashCode());
        SRMWorld world = singleRegionModel.createWorld(rngSupplier1);
        RNGSupplier rngSupplier2 = new JavaUtilRNGSupplier("ANOTHER SEED".hashCode());
        List<SRMBidder> bidders = singleRegionModel.createPopulation(world, rngSupplier2); //Note that we could also continue to use rngSupplier1
        treatBidders(bidders);
    }

    /**
     * <p>This example shows how you actually don't have to care about model details, you can just use {@link DefaultModel} and set it to the
     * model type you want, everything stays the same. Of course, model specific actions (as shown in {@link #exampleTwo()} are then not possible anymore.
     * <p>This example is a copy of {@link #exampleOne()}</p>, but adapted to be model independent
     */
    @Test
    public void exampleGeneric(){
        DefaultModel anyModel;
        // In the following switch statement, we pick any model at random
        int randomNumber = new Random().nextInt(4);
        switch (randomNumber){
            case(0): anyModel = new MultiRegionModel(); break;
            case(1): anyModel = new SingleRegionModel(); break;
            case(2): anyModel = new BaseValueModel(); break;
            case(3): anyModel = new MultiBandValueModel(); break;
            default: anyModel = new SingleRegionModel();
        }
        World world = anyModel.createWorld();
        List<Bidder> bidders = anyModel.createPopulation(world);
        treatBidders(bidders);
    }

    /**
     * Writes, for every passed bidder, his value for getting all licenses to the console.
     */
    public static void treatBidders(Collection<? extends Bidder> bidders){
        Optional<? extends Bidder> anyBidder = bidders.stream().findAny();
        if(anyBidder.isPresent()) {
            World world = anyBidder.get().getWorld();
            Bundle fullBundle = new Bundle<>(world.getLicenses());
            for (Bidder bidder : bidders) {
                BigDecimal val = bidder.calculateValue(fullBundle);
                System.out.println("bidder " + bidder.getId() + "has the following value for getting all licenses: " + val.toString());
            }
        }else{
            System.out.println("No bidder created");
        }
    }


}
