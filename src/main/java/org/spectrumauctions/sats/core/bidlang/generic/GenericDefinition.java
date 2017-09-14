package org.spectrumauctions.sats.core.bidlang.generic;

import com.google.gson.JsonElement;
import org.spectrumauctions.sats.core.model.Good;

import java.util.Set;

/**
 * @author Michael Weiss
 */
public interface GenericDefinition {

    boolean isPartOf(Good license);

    /**
     * @return the number of licenses of this generic type
     */
    int numberOfLicenses();

    /**
     * @return all licenses which are part of this GenericDefinition
     */
    Set<? extends Good> allLicenses();

    /**
     * @return a json representation of self, uniquely defining the definition, without expanding its members
     */
    JsonElement shortJson();
}
