/*******************************************************************************
 * Copyright (c) 2010 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/ 
package org.eclipse.cdt.core.dom.ast.cpp;

import org.eclipse.cdt.core.dom.ast.IEnumeration;
import org.eclipse.cdt.core.dom.ast.IType;

/**
 * C++ specific version of enumerations.
 * 
 * @since 5.2
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface ICPPEnumeration extends IEnumeration, ICPPBinding {
    
    /**
     * Returns whether this enumeration is scoped.
     * An enumeration can only be scoped in C++.
     */
    boolean isScoped();
    
    /**
     * Returns the underlying type of the enumeration if it is fixed, or <code>null</code> otherwise.
     * The underlying type can only be fixed in C++.
     */
    IType getFixedType();

	/**
	 * Returns the scope containing the enumerators.
	 * By the standard the scope is only defined for scoped enums, however it will be returned 
	 * for any enum. In case the enum has no definition (just opaque declarations) an empty scope 
	 * will be returned.
	 */
	ICPPScope asScope();
}
