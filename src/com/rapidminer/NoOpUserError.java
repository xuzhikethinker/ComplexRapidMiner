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
package com.rapidminer;

import com.rapidminer.operator.UserError;
import com.rapidminer.tools.Tools;


/**
 * An exception caused outside an operator which is <i>not</i> a bug, but
 * caused by the user.
 * 
 * Unfortunately, this class doubles most of the code of {@link UserError}.
 * 
 * @author Simon Fischer, Ingo Mierswa
 * @version $Id: NoOpUserError.java,v 1.1 2007/05/27 22:02:43 ingomierswa Exp $
 */
public class NoOpUserError extends Exception implements NoBugError {

	private static final long serialVersionUID = -686838060355434724L;
	
	private int code;

	/**
	 * Creates a new NoOpUserError.
	 * 
	 * @param cause
	 *            The exception that caused the user error. May be null. Using
	 *            this makes debugging a lot easier.
	 * @param code
	 *            The error code referring to a message in the file
	 *            <code>UserErrorMessages.properties</code>
	 * @param arguments
	 *            Arguments for the short message.
	 */
	public NoOpUserError(Throwable cause, int code, Object[] arguments) {
		super(UserError.getErrorMessage(code, arguments), cause);
		this.code = code;
	}

	/** Convenience constructor for messages with no arguments and cause. */
	public NoOpUserError(Throwable cause, int code) {
		this(code, new Object[0], cause);
	}

	public NoOpUserError(int code, Object[] arguments) {
		this(null, code, arguments);
	}

	/** Convenience constructor for messages with no arguments. */
	public NoOpUserError(int code) {
		this(null, code, new Object[0]);
	}

	/** Convenience constructor for messages with exactly one argument. */
	public NoOpUserError(int code, Object argument1) {
		this(null, code, new Object[] { argument1 });
	}

	/**
	 * Convenience constructor for messages with exactly one arguments and
	 * cause.
	 */
	public NoOpUserError(Throwable cause, int code, Object argument1) {
		this(cause, code, new Object[] { argument1 });
	}

	/** Convenience constructor for messages with exactly two arguments. */
	public NoOpUserError(int code, Object argument1, Object argument2) {
		this(null, code, new Object[] { argument1, argument2 });
	}

	public String getDetails() {
		return UserError.getResourceString(code, "long", "Description missing.");
	}

	public String getErrorName() {
		return UserError.getResourceString(code, "name", "Unnamed error.");
	}

	public int getCode() {
		return code;
	}

	public String getHTMLMessage() {
		return "<html>Error occured:<br>" + Tools.escapeXML(getMessage()) + "<hr>" + Tools.escapeXML(getDetails()) + "</html>";
	}
}
