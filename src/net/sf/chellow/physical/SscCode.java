/*
 
 Copyright 2008 Meniscus Systems Ltd
 
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

package net.sf.chellow.physical;

import java.text.DecimalFormat;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;

import net.sf.chellow.monad.ProgrammerException;
import net.sf.chellow.monad.UserException;
import net.sf.chellow.monad.types.MonadInteger;

public class SscCode extends MonadInteger {
	public SscCode() {
		init();
	}

	public SscCode(int code) throws UserException, ProgrammerException {
		this(null, code);
	}

	public SscCode(String label, int code) throws UserException,
			ProgrammerException {
		init();
		setLabel(label);
		update(code);
	}

	private void init() {
		setTypeName("code");
		setMinimum(0);
		setMaximum(9999);
	}

	public void update(int code) throws UserException, ProgrammerException {
		if (code < 0) {
			throw UserException
					.newInvalidParameter("The SSC can't be negative.");
		}
		super.update(code);
	}

	public Attr toXML(Document doc) {
		Attr attr = doc.createAttribute("code");
		DecimalFormat sscFormat = new DecimalFormat("0000");
		attr.setValue(sscFormat.format(getInteger()));
		return attr;
	}
}