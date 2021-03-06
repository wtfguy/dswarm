/**
 * Copyright (C) 2013, 2014 SLUB Dresden & Avantgarde Labs GmbH (<code@dswarm.org>)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.dswarm.persistence.model.schema.proxy;

import javax.xml.bind.annotation.XmlRootElement;

import org.dswarm.persistence.model.proxy.ProxyDMPJPAObject;
import org.dswarm.persistence.model.proxy.RetrievalType;
import org.dswarm.persistence.model.schema.AttributePath;

/**
 * A proxy class for {@link AttributePath}s.
 * 
 * @author tgaengler
 */
@XmlRootElement
public class ProxyAttributePath extends ProxyDMPJPAObject<AttributePath> {

	/**
	 *
	 */
	private static final long	serialVersionUID	= 1L;

	/**
	 * Default constructor for handing over a freshly created attribute path, i.e., no updated or already existing attribute path.
	 * 
	 * @param attributePathArg a freshly created attribute path
	 */
	public ProxyAttributePath(final AttributePath attributePathArg) {

		super(attributePathArg);
	}

	/**
	 * Creates a new proxy with the given real attribute path and the type how the attribute path was processed by the attribute
	 * path persistence service, e.g., {@link RetrievalType.CREATED}.
	 * 
	 * @param attributePathArg a attribute path that was processed by the attribute path persistence service
	 * @param typeArg the type how this attribute path was processed by the attribute path persistence service
	 */
	public ProxyAttributePath(final AttributePath attributePathArg, final RetrievalType typeArg) {

		super(attributePathArg, typeArg);
	}
}
