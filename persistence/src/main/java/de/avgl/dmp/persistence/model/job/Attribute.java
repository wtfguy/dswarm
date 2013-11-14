package de.avgl.dmp.persistence.model.job;

import java.util.List;
import java.util.Set;

import javax.xml.bind.annotation.XmlRootElement;

import org.hamcrest.Matchers;

import ch.lambdaj.Lambda;

import com.google.common.collect.Sets;

/**
 * @author tgaengler
 */
@XmlRootElement
//@Entity
// @Cacheable(true)
// @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
//@Table(name = "ATTRIBUTE")
public class Attribute extends BasicDMPObject {

	private static final org.apache.log4j.Logger	LOG					= org.apache.log4j.Logger.getLogger(Attribute.class);

	/**
	 * All attribute paths that utilise this attribute
	 */
	//@ManyToMany(fetch = FetchType.LAZY, cascade = { CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH })
	//@JoinTable(name = "ATTRIBUTE_PATHS_ATTRIBUTES", joinColumns = { @JoinColumn(name = "ATTRIBUTE_ID", referencedColumnName = "ID") }, inverseJoinColumns = { @JoinColumn(name = "ATTRIBUTE_PATH_ID", referencedColumnName = "ID") })
	// private Set<AttributePath>						attributePaths		= null;

	/**
	 * 
	 */
	private static final long						serialVersionUID	= 1L;

	public Attribute() {

		super(null);
	}

	public Attribute(final String id) {

		super(id);
	}

	public Attribute(final String id, final String name) {

		super(id);
		setName(name);
	}

//	public Set<AttributePath> getAttributePaths() {
//
//		return attributePaths;
//	}
//
//	public void setAttributePaths(final Set<AttributePath> attributePathsArg) {
//
//		if (attributePathsArg == null && attributePaths != null) {
//
//			// remove configuration from resources, if configuration, will be prepared for removal
//
//			for (final AttributePath attributePath : attributePaths) {
//
//				attributePath.removeAttribute(this);
//			}
//		}
//
//		attributePaths = attributePathsArg;
//
//		if (attributePathsArg != null) {
//
//			for (final AttributePath attributePath : attributePathsArg) {
//
//				attributePath.addAttribute(this);
//			}
//		}
//	}
//
//	public AttributePath getAttributePath(final String id) {
//
//		if (id == null) {
//
//			return null;
//		}
//
//		if (attributePaths == null || attributePaths.isEmpty()) {
//
//			return null;
//		}
//
//		final List<AttributePath> attributePathsFiltered = Lambda.filter(Lambda.having(Lambda.on(AttributePath.class).getId(), Matchers.equalTo(id)),
//				attributePaths);
//
//		if (attributePathsFiltered == null || attributePathsFiltered.isEmpty()) {
//
//			return null;
//		}
//
//		return attributePathsFiltered.get(0);
//	}
//
//	/**
//	 * Adds a new resource to the collection of resources of this configuration.<br>
//	 * Created by: tgaengler
//	 * 
//	 * @param resource a new export definition revision
//	 */
//	public void addAttributePath(final AttributePath attributePath) {
//
//		if (attributePath != null) {
//
//			if (attributePaths == null) {
//
//				attributePaths = Sets.newLinkedHashSet();
//			}
//
//			if (!attributePaths.contains(attributePath)) {
//
//				attributePaths.add(attributePath);
//				attributePath.addAttribute(this);
//			}
//		}
//	}
//
//	/**
//	 * Adds a new resource to the collection of resources of this configuration.<br>
//	 * Created by: tgaengler
//	 * 
//	 * @param resource a new export definition revision
//	 */
//	protected void addAttributePath(final AttributePath attributePath, final int attributeIndex) {
//
//		if (attributePath != null) {
//
//			if (attributePaths == null) {
//
//				attributePaths = Sets.newLinkedHashSet();
//			}
//
//			if (!attributePaths.contains(attributePath)) {
//
//				attributePaths.add(attributePath);
//				attributePath.addAttribute(this, attributeIndex);
//			}
//		}
//	}
//
//	/**
//	 * Removes an existing resource from the collection of resources of this configuration.<br>
//	 * Created by: tgaengler
//	 * 
//	 * @param resource an existing resource that should be removed
//	 */
//	public void removeAttributePath(final AttributePath attributePath) {
//
//		if (attributePaths != null && attributePath != null && attributePaths.contains(attributePath)) {
//
//			attributePaths.remove(attributePath);
//
//			attributePath.removeAttribute(this);
//		}
//	}

	@Override
	public boolean equals(final Object obj) {

		if (!Attribute.class.isInstance(obj)) {

			Attribute.LOG.debug("object is not an instance of Attribute, but " + obj.getClass().getName());

			return false;
		}

		final boolean result = super.equals(obj);

		if (!result) {

			Attribute.LOG.debug("attribute objects do not match. attribute id = '" + getId() + "' to compare attribute id = '"
					+ ((Attribute) obj).getId() + "'");
		}

		return result;
	}
}
