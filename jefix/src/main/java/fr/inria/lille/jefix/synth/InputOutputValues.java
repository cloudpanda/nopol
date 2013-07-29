/*
 * Copyright (C) 2013 INRIA
 *
 * This software is governed by the CeCILL-C License under French law and
 * abiding by the rules of distribution of free software. You can use, modify
 * and/or redistribute the software under the terms of the CeCILL-C license as
 * circulated by CEA, CNRS and INRIA at http://www.cecill.info.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the CeCILL-C License for more details.
 *
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL-C license and that you accept its terms.
 */
package fr.inria.lille.jefix.synth;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;

import com.google.common.base.Supplier;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;

/**
 * @author Favio D. DeMarco
 * 
 */
public class InputOutputValues {

	private enum ListSupplier implements Supplier<List<Object>> {
		INSTANCE;
		@Override
		public List<Object> get() {
			return new ArrayList<>();
		}
	}

	private final Multimap<String, Object> inputValues = Multimaps.newListMultimap(
			new LinkedHashMap<String, Collection<Object>>(), ListSupplier.INSTANCE);

	private final List<Object> outputValues = new ArrayList<>();

	public InputOutputValues addInputValue(final String varName, final Object value) {
		this.inputValues.put(varName, value);
		return this;
	}

	public InputOutputValues addOutputValue(final Object output) {
		this.outputValues.add(output);
		return this;
	}

	/**
	 * @return the inputvalues
	 */
	public Multimap<String, Object> getInputvalues() {
		return this.inputValues;
	}

	/**
	 * @return the outputValues
	 */
	public Iterable<Object> getOutputValues() {
		return this.outputValues;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return String.format("InputOutputValues [inputValues=%s,%noutputValues=%s]", this.inputValues,
				this.outputValues);
	}
}
