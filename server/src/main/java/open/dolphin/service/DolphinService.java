package open.dolphin.service;

import open.dolphin.infomodel.InfoModel;
import org.jboss.resteasy.util.Encode;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import java.io.IOException;

/**
 * DolphinService.
 *
 * @author pns
 */
public abstract class DolphinService {
    @PersistenceContext(unitName = "DolphinPU")
    protected EntityManager em;

    @Context
    private HttpHeaders headers;

    /**
     * Header 情報から CallerId (FacilityId:username) 部分を取り出す.
     *
     * @return FacilityId:username
     */
    private String getCallerId() {
        String encoded = headers.getHeaderString("Authorization");
        String usernameAndPassword = Encode.decode(encoded);
        String[] split = usernameAndPassword.split(InfoModel.PASSWORD_SEPARATOR);
        return split[0];
    }

    /**
     * CallerId (FacilityId:username) からFacilityId 部分を切り出す.
     *
     * @param checkId CallerId for check
     * @return FacilityId
     */
    private String getFacilityId(String checkId) {
        String[] split = checkId.split(InfoModel.COMPOSITE_KEY_MAKER);
        return split[0];
    }

    /**
     * Caller の FacilityId 部分を切り出す.
     *
     * @return FacilityId
     */
    protected String getCallersFacilityId() {
        return getFacilityId(getCallerId());
    }


    /**
     * Caller と CheckId の FacilityId を比較して，一致していなければ SecurityException を出す.
     *
     * @param checkId CallerId for check
     * @return checkId or SecurityException
     */
    protected String checkFacility(String checkId) {
        String callerKey = getCallersFacilityId();
        String requestKey = getFacilityId(checkId);
        if (!callerKey.equals(requestKey)) {
            throw new SecurityException(requestKey);
        }
        return checkId;
    }
}
