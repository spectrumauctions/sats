package org.spectrumauctions.sats.core.model;

import org.spectrumauctions.sats.core.bidlang.generic.GenericDefinition;

import java.util.Set;

public interface GenericWorld<T extends Good> {
    Set<? extends GenericDefinition<T>> getAllGenericDefinitions();

    GenericDefinition<T> getGenericDefinitionOf(T license);
}
