/*
 *  RapidMiner
 *
 *  Copyright (C) 2001-2007 by Rapid-I and the contributors
 *
 *  Complete list of developers available at our web site:
 *
 *       http://rapid-i.com
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU General Public License as 
 *  published by the Free Software Foundation; either version 2 of the
 *  License, or (at your option) any later version. 
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 *  General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 *  USA.
 */
package com.rapidminer.operator;

/**
 * Exception class whose instances are thrown during the creation of operators.
 * 
 * @author Ingo Mierswa
 * @version $Id: OperatorCreationException.java,v 1.6 2006/03/21 15:35:42
 *          ingomierswa Exp $
 */
public class OperatorCreationException extends Exception {

	private static final long serialVersionUID = 805882946295847566L;

	public static final int INSTANTIATION_ERROR = 0;

	public static final int ILLEGAL_ACCESS_ERROR = 1;

	public static final int NO_CONSTRUCTOR_ERROR = 2;

	public static final int CONSTRUCTION_ERROR = 3;

	public static final int NO_DESCRIPTION_ERROR = 4;

	public static final int NO_UNIQUE_DESCRIPTION_ERROR = 5;

	/**
	 * Code must be one of the constants of this class. The classname should
	 * define the operator.
	 */
	public OperatorCreationException(int code, String className, Throwable cause) {
		super(createMessage(code, className, cause));
	}

	private static String createMessage(int code, String className, Throwable cause) {
		switch (code) {
			case INSTANTIATION_ERROR:
				return "Cannot instantiate '" + className + "': " + cause.getMessage();
			case ILLEGAL_ACCESS_ERROR:
				return "Cannot access '" + className + "':" + cause.getMessage();
			case NO_CONSTRUCTOR_ERROR:
				return "No public one-argument constructor for operator descriptions: '" + className + "': " + cause.getMessage();
			case CONSTRUCTION_ERROR:
				return "Operator cannot be constructed: '" + className + "': " + cause.getCause().getMessage();
			case NO_DESCRIPTION_ERROR:
				return "No operator description object given for '" + className + (cause != null ? "': " + cause.getMessage() : "");
			case NO_UNIQUE_DESCRIPTION_ERROR:
				return "No unique operator description object available for class '" + className + "': " + cause.getMessage();
			default:
				return "Error during operator creation of '" + className + "': " + cause.getMessage();
		}
	}
}
