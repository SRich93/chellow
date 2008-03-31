/*
 
 Copyright 2005 Meniscus Systems Ltd
 
 This file is part of Chellow.

 Chellow is free software; you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation; either version 2 of the License, or
 (at your option) any later version.

 Chellow is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with Chellow; if not, write to the Free Software
 Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA

 */

package net.sf.chellow.monad.types;

import net.sf.chellow.monad.ProgrammerException;
import net.sf.chellow.monad.UserException;
import net.sf.chellow.monad.VFMessage;
import net.sf.chellow.monad.VFParameter;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

public class MonadLong extends MonadObject {
	private Long longValue;

	private Long min = null;

	private Long max = null;

	public MonadLong(String name, String longValue) throws UserException,
			ProgrammerException {
		super("Long", name);

		try {
			setLong(new Long(longValue));
		} catch (NumberFormatException e) {
			throw UserException.newInvalidParameter(new VFMessage(
					"malformed_integer",
					new VFParameter("note", e.getMessage())));
		}
	}

	protected MonadLong(String name, long longValue) throws UserException,
			ProgrammerException {
		super("Long", name);
		setLong(new Long(longValue));
	}

	protected void setMax(Long max) {
		this.max = max;
	}

	protected void setMin(Long min) {
		this.min = min;
	}

	public MonadLong(String stringValue) throws UserException,
			ProgrammerException {
		this(null, stringValue);
	}

	public MonadLong(long longValue) throws UserException, ProgrammerException {
		this(null, longValue);
	}

	public Long getLong() {
		return longValue;
	}

	public void setLong(Long longValue) throws UserException,
			ProgrammerException {

		if ((min != null) && (longValue.longValue() < min.longValue())) {
			throw UserException.newInvalidParameter(new VFMessage(
					VFMessage.NUMBER_TOO_SMALL, new VFParameter[] {
							new VFParameter("number", toString()),
							new VFParameter("min", min.toString()) }));
		}
		if ((max != null) && (longValue.longValue() < max.longValue())) {
			throw UserException.newInvalidParameter(new VFMessage(
					VFMessage.NUMBER_TOO_BIG, new VFParameter[] {
							new VFParameter("number", toString()),
							new VFParameter("max", max.toString()) }));
		}
		this.longValue = longValue;
	}

	public Node toXML(Document doc) {
		Node node = doc.createAttribute(getLabel());

		node.setNodeValue(longValue.toString());
		return node;
	}

	public String toString() {
		return longValue.toString();
	}
}