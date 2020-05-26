package open.dolphin.service;

import jdk.nashorn.internal.objects.annotations.Getter;
import open.dolphin.infomodel.ModuleModel;

import javax.annotation.PostConstruct;
import javax.annotation.security.RolesAllowed;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;

/**
 * @author pns
 */
@Path("pns")
@RolesAllowed("user")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface PnsService {

    @Path("peekKarte")
    @POST
    public List<ModuleModel> peekKarte(Long patientId);

    @Path("getCalendarData")
    @GET
    public String[][] getCalendarData();

    @Path("saveCalendarData")
    @POST
    public void saveCalendarData(String[][] data);

    @Path("makeInitialIndex")
    @POST
    public void makeInitialIndex();
}
