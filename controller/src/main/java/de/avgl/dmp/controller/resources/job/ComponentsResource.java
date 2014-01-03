package de.avgl.dmp.controller.resources.job;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Provider;
import com.google.inject.servlet.RequestScoped;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;

import de.avgl.dmp.controller.DMPControllerException;
import de.avgl.dmp.controller.resources.ExtendedBasicDMPResource;
import de.avgl.dmp.controller.status.DMPStatus;
import de.avgl.dmp.persistence.model.job.Component;
import de.avgl.dmp.persistence.service.job.ComponentService;

/**
 * A resource (controller service) for {@link Component}s.
 * 
 * @author tgaengler
 */
@RequestScoped
@Api(value = "/components", description = "Operations about components.")
@Path("components")
public class ComponentsResource extends ExtendedBasicDMPResource<ComponentService, Component> {

	/**
	 * Creates a new resource (controller service) for {@link Component}s with the provider of the component persistence service,
	 * the object mapper and metrics registry.
	 * 
	 * @param componentServiceProviderArg the component persistence service provider
	 * @param objectMapperArg an object mapper
	 * @param dmpStatusArg a metrics registry
	 */
	@Inject
	public ComponentsResource(final Provider<ComponentService> componentServiceProviderArg, final ObjectMapper objectMapper, final DMPStatus dmpStatus) {

		super(Component.class, componentServiceProviderArg, objectMapper, dmpStatus);
	}

	/**
	 * This endpoint returns a component as JSON representation for the provided component identifier.
	 * 
	 * @param id a component identifier
	 * @return a JSON representation of a component
	 */
	@ApiOperation(value = "get the component that matches the given id", notes = "Returns the Component object that matches the given id.")
	@GET
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	public Response getObject(@ApiParam(value = "component identifier", required = true) @PathParam("id") final Long id)
			throws DMPControllerException {

		return super.getObject(id);
	}

	/**
	 * This endpoint consumes a component as JSON representation and persists this component in the database.
	 * 
	 * @param jsonObjectString a JSON representation of one component
	 * @return the persisted component as JSON representation
	 * @throws DMPControllerException
	 */
	@ApiOperation(value = "create a new component", notes = "Returns a new Component object.", response = Component.class)
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	public Response createObject(@ApiParam(value = "component (as JSON)", required = true) final String jsonObjectString)
			throws DMPControllerException {

		return super.createObject(jsonObjectString);
	}

	/**
	 * This endpoint returns a list of all components as JSON representation.
	 * 
	 * @return a list of all components as JSON representation
	 * @throws DMPControllerException
	 */
	@ApiOperation(value = "get all components ", notes = "Returns a list of Component objects.")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	public Response getObjects() throws DMPControllerException {

		return super.getObjects();
	}

	/**
	 * {@inheritDoc}<br/>
	 * Updates the name, description, function, parameter mappings, input components and output components of the component.
	 */
	@Override
	protected Component prepareObjectForUpdate(final Component objectFromJSON, final Component object) {

		super.prepareObjectForUpdate(objectFromJSON, object);

		object.setFunction(objectFromJSON.getFunction());
		object.setParameterMappings(objectFromJSON.getParameterMappings());
		object.setInputComponents(objectFromJSON.getInputComponents());
		object.setOutputComponents(objectFromJSON.getOutputComponents());

		return object;
	}
}