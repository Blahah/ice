package org.jbei.ice.services.rest;

import org.apache.commons.lang3.StringUtils;
import org.jbei.ice.lib.dto.FeaturedDNASequence;
import org.jbei.ice.lib.dto.entry.AttachmentInfo;
import org.jbei.ice.lib.dto.entry.PartData;
import org.jbei.ice.lib.dto.entry.PartStatistics;
import org.jbei.ice.lib.dto.web.RegistryPartner;
import org.jbei.ice.lib.dto.web.WebEntries;
import org.jbei.ice.lib.entry.EntrySelection;
import org.jbei.ice.lib.net.RemoteContact;
import org.jbei.ice.lib.net.RemoteEntries;
import org.jbei.ice.lib.net.WoRController;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.List;

/**
 * Resource for web of registries requests
 *
 * @author Hector Plahar
 */
@Path("/web")
public class WebResource extends RestResource {

    private final WoRController controller = new WoRController();
    private final RemoteEntries remoteEntries = new RemoteEntries();

    /**
     * Retrieves information on other ice instances that is in a web of registries configuration
     * with this instance; also know as registry partners
     *
     * @param approvedOnly
     *            if true, only instances that have been approved are returned; defaults to true
     * @return wrapper around the list of registry partners
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response query(@DefaultValue("true") @QueryParam("approved_only") boolean approvedOnly) {
        getUserId(); // ensure valid session or auth header
        return super.respond(controller.getRegistryPartners(approvedOnly));
    }

    /**
     * Retrieves entries for specified partner using the specified paging parameters
     *
     * @param partnerId unique identifier for registry partner whose entries are being retrieved
     * @param offset    record retrieve offset paging parameter
     * @param limit     maximum number of entries to retrieve
     * @param sort      field to sort on
     * @param asc       sort order
     *
     * @return Response with public entries from registry partners
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}/entries")
    public Response getWebEntries(
            @PathParam("id") final long partnerId,
            @DefaultValue("0") @QueryParam("offset") final int offset,
            @DefaultValue("15") @QueryParam("limit") final int limit,
            @DefaultValue("created") @QueryParam("sort") final String sort,
            @DefaultValue("false") @QueryParam("asc") final boolean asc) {
        final String userId = getUserId();
        final WebEntries result = remoteEntries.getPublicEntries(userId, partnerId, offset, limit, sort, asc);
        return super.respond(Response.Status.OK, result);
    }

    /**
     * @param partnerId
     * @param partId
     * @return attachment info on a registry partner entry
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}/entries/{entryId}/attachments")
    public List<AttachmentInfo> getAttachments(@PathParam("id") final long partnerId,
            @PathParam("entryId") final long partId) {
        final String userId = getUserId();
        return remoteEntries.getEntryAttachments(userId, partnerId, partId);
    }

    /**
     * @param remoteId
     * @param entrySelection
     * @return Response for success
     */
    @POST
    @Path("/{id}/transfer")
    public Response transferEntries(@PathParam("id") final long remoteId,
            final EntrySelection entrySelection) {
        final String userId = super.getUserId();
        remoteEntries.transferEntries(userId, remoteId, entrySelection);
        return super.respond(Response.Status.OK);
    }

    /**
     * @param partnerId
     * @param entryId
     * @return Response with a specific entry for a registry partner
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}/entries/{entryId}")
    public Response getWebEntry(
            @PathParam("id") final long partnerId,
            @PathParam("entryId") final long entryId) {
        final String userId = super.getUserId();
        final PartData result = remoteEntries.getPublicEntry(userId, partnerId, entryId);
        return super.respond(Response.Status.OK, result);
    }

    /**
     * @param partnerId
     * @param entryId
     * @return Response with a specific entry tooltip for a registry partner
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}/entries/{entryId}/tooltip")
    public Response getWebEntryTooltip(
            @PathParam("id") final long partnerId, @PathParam("entryId") final long entryId) {
        final String userId = super.getUserId();
        final PartData result = remoteEntries.getPublicEntryTooltip(userId, partnerId, entryId);
        return super.respond(Response.Status.OK, result);
    }

    /**
     * @param partnerId
     * @param entryId
     * @return Response with statistics on a specific entry for a registry partner
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}/entries/{entryId}/statistics")
    public Response getStatistics(@PathParam("id") final long partnerId,
            @PathParam("entryId") final long entryId) {
        final String userId = super.getUserId();
        final PartStatistics statistics = remoteEntries.getPublicEntryStatistics(userId, partnerId, entryId);
        return super.respond(statistics);
    }

    /**
     * @param partnerId
     * @param entryId
     * @return Response with a sequence on a registry partner entry
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}/entries/{entryId}/sequence")
    public Response getWebEntrySequence(
            @PathParam("id") final long partnerId, @PathParam("entryId") final long entryId) {
        final String userId = super.getUserId();
        final FeaturedDNASequence result = remoteEntries.getPublicEntrySequence(userId, partnerId, entryId);
        return super.respond(Response.Status.OK, result);
    }

    /**
     * @param partner
     * @return Response with an added registry partner
     */
    @POST
    @Path("/partner")
    // admin function
    public Response addWebPartner(final RegistryPartner partner) {
        final String userId = getUserId();
        final RemoteContact contactRemote = new RemoteContact();
        final RegistryPartner registryPartner = contactRemote.addWebPartner(userId, partner);
        return respond(Response.Status.OK, registryPartner);
    }

    /**
     * @param info
     * @param partnerId
     * @return Response with registry partner info
     */
    @GET
    @Path("/partner/{id}")
    public Response getWebPartner(@Context final UriInfo info, @PathParam("id") final long partnerId) {
        final String userId = getUserId();
        final RegistryPartner partner = controller.getWebPartner(userId, partnerId);
        return super.respond(Response.Status.OK, partner);
    }

    /**
     * @param partner
     * @return Response for success or failure
     */
    @POST
    @Path("/partner/remote")
    public Response remoteWebPartnerRequest(RegistryPartner partner) {
        RemoteContact remoteContact = new RemoteContact();
        return super.respond(remoteContact.handleRemoteAddRequest(partner));
    }

    @DELETE
    @Path("/partner/remote")
    public Response remoteWebPartnerRemoveRequest(
            @HeaderParam(WOR_PARTNER_TOKEN) String worToken,
            @QueryParam("url") String url) {
        RemoteContact remoteContact = new RemoteContact();
        return super.respond(remoteContact.handleRemoteRemoveRequest(worToken, url));
    }

    @GET
    @Path("/partners")
    public Response getWebPartners(@HeaderParam(AUTHENTICATION_PARAM_NAME) String sessionId,
                                   @HeaderParam(WOR_PARTNER_TOKEN) String worToken,
                                   @QueryParam("url") String url) {
        if (StringUtils.isEmpty(sessionId))
            return super.respond(controller.getWebPartners(worToken, url));
        final String userId = getUserId();
        return super.respond(controller.getWebPartners(userId));
    }

    /**
     * @param url
     * @param partner
     * @return Response for success or failure
     */
    @PUT
    @Path("/partner/{url}")
    public Response updateWebPartner(@PathParam("url") final String url,
            final RegistryPartner partner) {
        final String userId = getUserId();
        if (controller.updateWebPartner(userId, url, partner)) {
            return respond(Response.Status.OK);
        }
        return respond(Response.Status.INTERNAL_SERVER_ERROR);
    }

    /**
     * @param url
     * @return Response for success or failure
     */
    @DELETE
    @Path("/partner/{url}")
    public Response removeWebPartner(@PathParam("url") final String url) {
        final String userId = getUserId();
        if (controller.removeWebPartner(userId, url)) {
            return respond(Response.Status.OK);
        }
        return respond(Response.Status.INTERNAL_SERVER_ERROR);
    }
}
