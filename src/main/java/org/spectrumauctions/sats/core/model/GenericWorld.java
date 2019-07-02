package org.spectrumauctions.sats.core.model;

import java.util.List;
import java.util.Set;

public interface GenericWorld {
    List<? extends GenericGood> getAllGenericDefinitions();

    default GenericGood getGenericDefinitionOf(License license) {
        for (GenericGood generic : getAllGenericDefinitions()) {
            if (generic.containedGoods().contains(license)) return generic;
        }
        throw new WrongConfigException();
    }
}
