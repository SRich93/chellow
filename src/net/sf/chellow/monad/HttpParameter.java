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

package net.sf.chellow.monad;

import net.sf.chellow.monad.types.MonadObject;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;


public class HttpParameter extends MonadObject {
	ParameterName name;

	String[] values;

	public HttpParameter(String name, String value) {
		this(new ParameterName(name), value);
	}
	
	public HttpParameter(ParameterName name, String value) {
		this(name, new String[] {value});
	}

	public HttpParameter(ParameterName name, String[] values) {
		this.name = name;
		this.values = values;
	}

	public Node toXML(Document doc) {
		Element element = doc.createElement("parameter");
		element.setAttributeNode((Attr) name.toXML(doc));
		for (String value : values) {
			Element valueElement = doc.createElement("value");

			valueElement.setTextContent(value);
			element.appendChild(valueElement);
		}
		return element;
	}

	public String getFirstValue() {
		return values[0];
	}

	public ParameterName getName() {
		return name;
	}
}