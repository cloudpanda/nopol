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
package fr.inria.lille.commons.trace;

import java.util.Collection;

import spoon.processing.AbstractProcessor;
import spoon.reflect.code.CtAssignment;
import spoon.reflect.code.CtBlock;
import spoon.reflect.code.CtCodeElement;
import spoon.reflect.code.CtCodeSnippetStatement;
import spoon.reflect.code.CtStatement;
import spoon.reflect.cu.SourcePosition;
import spoon.reflect.declaration.CtField;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtVariable;
import spoon.reflect.visitor.Filter;
import spoon.reflect.visitor.filter.CompositeFilter;
import spoon.reflect.visitor.filter.FilteringOperator;
import fr.inria.lille.commons.collections.ListLibrary;
import fr.inria.lille.commons.collections.SetLibrary;
import fr.inria.lille.commons.spoon.BeforeLocationFilter;
import fr.inria.lille.commons.spoon.InsertBeforeStrategy;
import fr.inria.lille.commons.spoon.InsertionStrategy;
import fr.inria.lille.commons.spoon.ReachableVariableVisitor;
import fr.inria.lille.commons.spoon.SpoonLibrary;
import fr.inria.lille.commons.spoon.VariableAssignmentFilter;

public class RuntimeValuesProcessor extends AbstractProcessor<CtCodeElement> {
	
	public RuntimeValuesProcessor() {
		this(new InsertBeforeStrategy());
	}
	
	public RuntimeValuesProcessor(InsertionStrategy insertionStrategy) {
		this.insertionStrategy = insertionStrategy;
	}
	
	@Override
	public void process(CtCodeElement codeElement) {
		CtStatement statement = SpoonLibrary.statementOf(codeElement);
		Collection<String> variableNames = reachableVariableNames(statement);
		if (! variableNames.isEmpty()) {
			Collection<CtCodeSnippetStatement> newStatements = valueColectingStatements(statement, variableNames);
			insertionStrategy().insertNewStatements(statement, newStatements);
		}
	}

	private Collection<CtCodeSnippetStatement> valueColectingStatements(CtStatement statement, Collection<String> variableNames) {
		Collection<CtCodeSnippetStatement> newStatements = ListLibrary.newLinkedList();
		for (String variableName : variableNames) {
			String invocation = RuntimeValues.collectValueInvocation(variableName);
			CtCodeSnippetStatement newStatement = SpoonLibrary.statementFrom(invocation, statement.getParent());
			newStatements.add(newStatement);
		}
		return newStatements;
	}
	
	public Collection<String> reachableVariableNames(CtStatement statement) {
		Collection<CtVariable<?>> reachableVariables = reachableVariables(statement);
		return variableNames(reachableVariables);
	}
	
	private Collection<CtVariable<?>> reachableVariables(CtStatement statement) {
		ReachableVariableVisitor variableVisitor = new ReachableVariableVisitor(statement);
		Collection<CtVariable<?>> reachedVariables = variableVisitor.reachedVariables();
		return initializedVariablesBefore(statement.getPosition(), reachedVariables);
	}
	
	private Collection<String> variableNames(Collection<CtVariable<?>> reachedVariables) {
		Collection<String> names = SetLibrary.newHashSet();
		for (CtVariable<?> variable : reachedVariables) {
			names.add(nameFor(variable));
		}
		return names;
	}
	
	private String nameFor(CtVariable<?> variable) {
		String simpleName = variable.getSimpleName();
		if (SpoonLibrary.isField(variable)) {
			if (SpoonLibrary.hasStaticModifier(variable)) {
				simpleName = ((CtField<?>) variable).getDeclaringType().getSimpleName() + "." + simpleName;
			} else {
				simpleName = "this." + simpleName;
			}
		}
		return simpleName;
	}
	
	private Collection<CtVariable<?>> initializedVariablesBefore(SourcePosition position, Collection<CtVariable<?>> reachedVariables) {
		Collection<CtVariable<?>> initializedVariables = SetLibrary.newHashSet();
		for (CtVariable<?> variable : reachedVariables) {
			if (! SpoonLibrary.isLocalVariable(variable) || wasInitializedBefore(position, variable)) {
				initializedVariables.add(variable);
			}
		}
		return initializedVariables;
	}
	
	private boolean wasInitializedBefore(SourcePosition position, CtVariable<?> variable) {
		if (variable.getDefaultExpression() == null) {
			CtBlock block = variable.getParent(CtMethod.class).getBody();
			Filter<CtAssignment> filter = compositeFilterFor(variable, position);
			return ! block.getElements(filter).isEmpty();
		}
		return true;
	}

	private Filter<CtAssignment> compositeFilterFor(CtVariable<?> variable, SourcePosition position) {
		VariableAssignmentFilter variableAssignment = new VariableAssignmentFilter(variable);
		BeforeLocationFilter beforeLocation = new BeforeLocationFilter<CtAssignment>(CtAssignment.class, position);
		return new CompositeFilter(FilteringOperator.INTERSECTION, variableAssignment, beforeLocation);
	}
	
	protected InsertionStrategy insertionStrategy() {
		return insertionStrategy;
	}
	
	private InsertionStrategy insertionStrategy;
}
