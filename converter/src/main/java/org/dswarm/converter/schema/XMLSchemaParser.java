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
package org.dswarm.converter.schema;

import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.io.ByteSource;
import com.google.common.io.Resources;
import com.google.inject.Inject;
import com.google.inject.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import org.dswarm.graph.json.Resource;
import org.dswarm.graph.json.ResourceNode;
import org.dswarm.persistence.DMPPersistenceException;
import org.dswarm.persistence.model.internal.helper.AttributePathHelper;
import org.dswarm.persistence.model.internal.helper.AttributePathHelperHelper;
import org.dswarm.persistence.model.schema.Schema;
import org.dswarm.persistence.model.schema.proxy.ProxySchema;
import org.dswarm.persistence.model.schema.utils.SchemaUtils;
import org.dswarm.persistence.service.schema.AttributePathService;
import org.dswarm.persistence.service.schema.AttributeService;
import org.dswarm.persistence.service.schema.ClaszService;
import org.dswarm.persistence.service.schema.SchemaService;
import org.dswarm.persistence.util.DMPPersistenceUtil;
import org.dswarm.persistence.util.GDMUtil;
import org.dswarm.xsd2jsonschema.JsonSchemaParser;
import org.dswarm.xsd2jsonschema.model.JSRoot;

/**
 * @author tgaengler
 */
public class XMLSchemaParser {

	private static final Logger LOG = LoggerFactory.getLogger(XMLSchemaParser.class);

	private final Provider<SchemaService> schemaServiceProvider;

	private final Provider<ClaszService> classServiceProvider;

	private final Provider<AttributePathService> attributePathServiceProvider;

	private final Provider<AttributeService> attributeServiceProvider;

	private final Provider<ObjectMapper> objectMapperProvider;

	private static final String ROOT_NODE_IDENTIFIER               = "__ROOT_NODE__";
	private static final String UNKNOWN_JSON_SCHEMA_ATTRIBUTE_TYPE = "__UNKNOWN__";
	private static final String STRING_JSON_SCHEMA_ATTRIBUTE_TYPE  = "string";
	private static final String OBJECT_JSON_SCHEMA_ATTRIBUTE_TYPE  = "object";

	private static final String JSON_SCHEMA_PROPERTIES_IDENTIFIER = "properties";
	private static final String JSON_SCHEMA_TYPE_IDENTIFIER       = "type";
	private static final String JSON_SCHEMA_MIXED_IDENTIFIER      = "mixed";

	@Inject
	public XMLSchemaParser(final Provider<SchemaService> schemaServiceProviderArg,
			final Provider<ClaszService> classServiceProviderArg, final Provider<AttributePathService> attributePathServiceProviderArg,
			final Provider<AttributeService> attributeServiceProviderArg, final Provider<ObjectMapper> objectMapperProviderArg) {

		schemaServiceProvider = schemaServiceProviderArg;
		classServiceProvider = classServiceProviderArg;
		attributePathServiceProvider = attributePathServiceProviderArg;
		attributeServiceProvider = attributeServiceProviderArg;
		objectMapperProvider = objectMapperProviderArg;
	}

	public Optional<Schema> parse(final String xmlSchemaFilePath, final String recordTag, final String schemaName) throws DMPPersistenceException {

		final Optional<List<JsonNode>> optionalRecordTags = getRecordTagNodes(xmlSchemaFilePath, recordTag);

		if(!optionalRecordTags.isPresent()) {

			return Optional.absent();
		}

		final List<JsonNode> recordTagNodes = optionalRecordTags.get();

		final Optional<Schema> optionalSchema = createSchema();

		if(!optionalSchema.isPresent()) {

			return Optional.absent();
		}

		final Schema schema = optionalSchema.get();

		if(schemaName != null) {

			schema.setName(schemaName);
		}

		final Optional<String> optionalRecordTagAttribute = getAttribute(recordTagNodes.get(0));

		if(optionalRecordTagAttribute.isPresent()) {

			final String recordTagAttribute = optionalRecordTagAttribute.get();

			final String recordTagAttributeURI = SchemaUtils.mintSchemaTermURI(recordTagAttribute, schema.getId());

			final String recordClassUri = recordTagAttributeURI + "Type";

			SchemaUtils.addRecordClass(schema, recordClassUri, classServiceProvider);
		}

		final Set<AttributePathHelper> attributePaths = parseAttributePaths(recordTagNodes);

		SchemaUtils.addAttributePaths(schema, attributePaths, attributePathServiceProvider, attributeServiceProvider);

		final Schema updatedSchema = SchemaUtils.updateSchema(schema, schemaServiceProvider);

		return Optional.fromNullable(updatedSchema);
	}

	public Optional<Set<AttributePathHelper>> parseAttributePaths(final String xmlSchemaFilePath, final String recordTag) {

		final Optional<List<JsonNode>> optionalRecordTags = getRecordTagNodes(xmlSchemaFilePath, recordTag);

		if(!optionalRecordTags.isPresent()) {

			return Optional.absent();
		}

		final List<JsonNode> recordTagNodes = optionalRecordTags.get();

		final Set<AttributePathHelper> attributePaths = parseAttributePaths(recordTagNodes);

		return Optional.fromNullable(attributePaths);
	}

	private Set<AttributePathHelper> parseAttributePaths(final List<JsonNode> recordTagNodes) {

		final Set<AttributePathHelper> attributePaths = Sets.newCopyOnWriteArraySet();

		// attribute path retrieval from all records
		for (final JsonNode recordTagNode : recordTagNodes) {

			Set<AttributePathHelper> recordTagNodeAttributePaths = Sets.newLinkedHashSet();

			recordTagNodeAttributePaths = determineAttributePaths(recordTagNode, recordTagNodeAttributePaths, new AttributePathHelper(), false);

			if (recordTagNodeAttributePaths != null && !recordTagNodeAttributePaths.isEmpty()) {

				attributePaths.addAll(recordTagNodeAttributePaths);
			}
		}

		return attributePaths;
	}

	private Optional<List<JsonNode>> getRecordTagNodes(final String xmlSchemaFilePath, final String recordTag) {

		final Optional<ObjectNode> jsonSchemaOptional = getJSONSchema(xmlSchemaFilePath);

		if (!jsonSchemaOptional.isPresent()) {

			return Optional.absent();
		}

		final List<JsonNode> recordTagNodes = getRecordTagNodes(jsonSchemaOptional.get(), recordTag);

		return Optional.fromNullable(recordTagNodes);
	}

	private List<JsonNode> getRecordTagNodes(final ObjectNode jsonSchema, final String recordTag) {

		final List<JsonNode> recordTagNodes = Lists.newCopyOnWriteArrayList();

		getRecordTagNodes(jsonSchema, recordTag, recordTagNodes);

		return recordTagNodes;
	}

	private void getRecordTagNodes(final JsonNode currentJSONSchemaNode, final String recordTag, final List<JsonNode> recordTagNodes) {

		final JsonNode currentJSONSchemaNodeProperties = currentJSONSchemaNode.get(XMLSchemaParser.JSON_SCHEMA_PROPERTIES_IDENTIFIER);

		if (currentJSONSchemaNodeProperties == null || currentJSONSchemaNodeProperties.size() <= 0) {

			return;
		}

		final Iterator<Map.Entry<String, JsonNode>> iter = currentJSONSchemaNodeProperties.fields();

		while (iter.hasNext()) {

			final Map.Entry<String, JsonNode> currentJSONSchemaNodePropertiesEntry = iter.next();

			if (currentJSONSchemaNodePropertiesEntry.getKey().endsWith(recordTag)) {

				final ObjectNode recordTagNode = objectMapperProvider.get().createObjectNode();
				recordTagNode.put(currentJSONSchemaNodePropertiesEntry.getKey(), currentJSONSchemaNodePropertiesEntry.getValue());

				recordTagNodes.add(recordTagNode);

				break;
			}

			getRecordTagNodes(currentJSONSchemaNodePropertiesEntry.getValue(), recordTag, recordTagNodes);
		}
	}

	private Optional<ObjectNode> getJSONSchema(final String xmlSchemaFilePath) {

		final JsonSchemaParser schemaParser = new JsonSchemaParser();

		final URL resourceURL = Resources.getResource(xmlSchemaFilePath);
		final ByteSource byteSource = Resources.asByteSource(resourceURL);

		try {

			schemaParser.parse(byteSource.openStream());
		} catch (final SAXException e) {

			LOG.error("couldn't parse XML schema '{}'", xmlSchemaFilePath, e);

			return Optional.absent();
		} catch (final IOException e) {

			LOG.error("couldn't read XML schema '{}'", xmlSchemaFilePath, e);

			return Optional.absent();
		}
		final JSRoot root;

		try {

			root = schemaParser.apply(XMLSchemaParser.ROOT_NODE_IDENTIFIER);
		} catch (final SAXException e) {

			LOG.error("couldn't convert XSD to JSON schema for '{}'", xmlSchemaFilePath, e);

			return Optional.absent();
		}

		final ObjectNode json;

		try {

			json = root.toJson(objectMapperProvider.get());
		} catch (final IOException e) {

			LOG.error("couldn't serialize JSON schema for '{}'", xmlSchemaFilePath, e);

			return Optional.absent();
		}

		return Optional.fromNullable(json);
	}

	private Set<AttributePathHelper> determineAttributePaths(final JsonNode jsonSchemaAttributeNode, final Set<AttributePathHelper> attributePaths,
			final AttributePathHelper attributePath, final boolean addRootAttribute) {

		final Optional<String> optionalAttribute = getAttribute(jsonSchemaAttributeNode);

		if (!optionalAttribute.isPresent()) {

			return attributePaths;
		}

		final String attribute = optionalAttribute.get();

		final boolean isXMLAttribute;

		if (attribute.startsWith("@")) {

			isXMLAttribute = true;
		} else {

			isXMLAttribute = false;
		}

		final String finalAttribute;

		if (isXMLAttribute) {

			finalAttribute = attribute.substring(1, attribute.length());
		} else {

			finalAttribute = attribute;
		}

		final AttributePathHelper finalAttributePathHelper;

		if (addRootAttribute) {

			finalAttributePathHelper = AttributePathHelperHelper.addAttributePath(finalAttribute, attributePaths, attributePath);
		} else {

			finalAttributePathHelper = attributePath;
		}

		final JsonNode jsonSchemaAttributeContentNode = jsonSchemaAttributeNode.get(attribute);

		final JsonNode typeNode = jsonSchemaAttributeContentNode.get(XMLSchemaParser.JSON_SCHEMA_TYPE_IDENTIFIER);

		final String type;

		if (typeNode != null) {

			type = typeNode.asText();
		} else {

			type = XMLSchemaParser.UNKNOWN_JSON_SCHEMA_ATTRIBUTE_TYPE;
		}

		if ((type.equals(XMLSchemaParser.STRING_JSON_SCHEMA_ATTRIBUTE_TYPE) || type.equals(OBJECT_JSON_SCHEMA_ATTRIBUTE_TYPE)) && !isXMLAttribute) {

			// add rdf:type attribute
			AttributePathHelperHelper.addAttributePath(GDMUtil.RDF_type, attributePaths, finalAttributePathHelper);
		}

		final JsonNode jsonSchemaAttributePropertiesNode = jsonSchemaAttributeContentNode.get(XMLSchemaParser.JSON_SCHEMA_PROPERTIES_IDENTIFIER);

		final boolean noProperties;

		noProperties = jsonSchemaAttributePropertiesNode == null || jsonSchemaAttributePropertiesNode.size() <= 0;

		final JsonNode mixedNode = jsonSchemaAttributeContentNode.get(XMLSchemaParser.JSON_SCHEMA_MIXED_IDENTIFIER);

		final boolean isMixed;

		isMixed = mixedNode != null && mixedNode.asBoolean();

		final boolean addRDFValueAttributePath;

		addRDFValueAttributePath =
				(type.equals(XMLSchemaParser.STRING_JSON_SCHEMA_ATTRIBUTE_TYPE) || (type.equals(OBJECT_JSON_SCHEMA_ATTRIBUTE_TYPE) && isMixed))
						&& !isXMLAttribute;

		if (noProperties) {

			addRDFValueAttributePath(addRDFValueAttributePath, attributePaths, finalAttributePathHelper);

			return attributePaths;
		}

		final Iterator<Map.Entry<String, JsonNode>> iter = jsonSchemaAttributePropertiesNode.fields();

		final List<JsonNode> newAttributeNodes = Lists.newArrayList();
		final List<JsonNode> newElementNodes = Lists.newArrayList();

		while (iter.hasNext()) {

			final Map.Entry<String, JsonNode> entry = iter.next();

			final ObjectNode newJSONSchemaAttributeNode = objectMapperProvider.get().createObjectNode();

			final String newAttribute = entry.getKey();

			newJSONSchemaAttributeNode.put(newAttribute, entry.getValue());

			if (newAttribute.startsWith("@")) {

				newAttributeNodes.add(newJSONSchemaAttributeNode);
			} else {

				newElementNodes.add(newJSONSchemaAttributeNode);
			}
		}

		determineAttributePaths(attributePaths, finalAttributePathHelper, newAttributeNodes);
		addRDFValueAttributePath(addRDFValueAttributePath, attributePaths, finalAttributePathHelper);
		determineAttributePaths(attributePaths, finalAttributePathHelper, newElementNodes);

		return attributePaths;
	}

	private void determineAttributePaths(final Set<AttributePathHelper> attributePaths, final AttributePathHelper finalAttributePathHelper,
			final List<JsonNode> newJSONSchemaAttributeNodes) {

		for (final JsonNode newJSONSchemaAttributeNode : newJSONSchemaAttributeNodes) {

			determineAttributePaths(newJSONSchemaAttributeNode, attributePaths, finalAttributePathHelper, true);
		}
	}

	private Optional<String> getAttribute(final JsonNode jsonSchemaAttributeNode) {

		if (jsonSchemaAttributeNode == null || !jsonSchemaAttributeNode.fieldNames().hasNext()) {

			return Optional.absent();
		}

		final String attribute = jsonSchemaAttributeNode.fieldNames().next();

		return Optional.fromNullable(attribute);
	}

	private void addRDFValueAttributePath(final boolean addRDFValueAttributePath, final Set<AttributePathHelper> attributePaths,
			final AttributePathHelper attributePath) {

		if (addRDFValueAttributePath) {

			// add rdf:value attribute
			AttributePathHelperHelper.addAttributePath(GDMUtil.RDF_value, attributePaths, attributePath);
		}
	}

	private Optional<Schema> createSchema() throws DMPPersistenceException {

		// create new schema
		final ProxySchema proxySchema = schemaServiceProvider.get().createObjectTransactional();

		final Schema schema;

		if (proxySchema != null) {

			schema = proxySchema.getObject();
		} else {

			schema = null;
		}

		return Optional.fromNullable(schema);
	}

}
