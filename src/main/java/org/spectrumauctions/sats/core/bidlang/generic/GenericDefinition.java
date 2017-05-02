package org.spectrumauctions.sats.core.bidlang.generic;

import org.spectrumauctions.sats.core.model.Good;
import com.google.gson.JsonElement;

import java.util.Set;

/**
 * @author Michael Weiss
 */
public interface GenericDefinition {

    boolean isPartOf(Good license);

    /**
     * The number of licenses of this generic type
     *
     * @return
     */
    int numberOfLicenses();

    /**
     * Returns all licenses which are part of this GenericDefinition
     *
     * @return
     */
    Set<? extends Good> allLicenses();

    /**
     * Creates a json representation of self, uniquely defining the definition, without expanding it's members
     *
     * @return
     */
    JsonElement shortJson();
}
