/*
 
 Copyright 2005-2008 Meniscus Systems Ltd
 
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

import net.sf.chellow.monad.InternalException;
import net.sf.chellow.monad.HttpException;
import net.sf.chellow.monad.UserException;
import net.sf.chellow.monad.types.MonadInteger;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;

public class LlfcCode extends MonadInteger {
	public LlfcCode() {
	}

	public LlfcCode(String code) throws HttpException, InternalException {
		update(code);
	}

	public LlfcCode(int code) throws HttpException, InternalException {
		update(code);
	}
	
	public LlfcCode(String label, String code) throws HttpException,
			InternalException {
		setLabel(label);
		update(code);
	}

	public void update(int code) throws InternalException, UserException {
		setMinimum(0);
		setMaximum(999);
		super.update(code);
	}

	
	public void update(String code) throws InternalException, UserException {
		update(Integer.parseInt(code.trim()));
	}

	public boolean hasDso() {
		return !((getInteger() > 499 && getInteger() < 510) || (getInteger() > 799 && getInteger() < 1000));
	}

	public String toString() {
		DecimalFormat mtcFormat = new DecimalFormat("000");
		return mtcFormat.format(getInteger());
	}

	public Attr toXml(Document doc) {
		Attr attr = doc.createAttribute("code");
		attr.setValue(toString());
		return attr;
	}
}