package org.spectrumauctions.sats.core.bidlang.generic;

import com.google.gson.JsonElement;
import org.spectrumauctions.sats.core.model.Good;

import java.util.Set;

/**
 * @author Michael Weiss
 */
public interface GenericDefinition<T extends Good> {

    boolean isPartOf(T license);

    /**
     * @return the number of licenses of this generic type
     */
    int numberOfLicenses();

    /**
     * @return all licenses which are part of this GenericDefinition
     */
    Set<T> allLicenses();

    /**
     * @return a json representation of self, uniquely defining the definition, without expanding its members
     */
    JsonElement shortJson();
}
