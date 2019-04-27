package open.dolphin.orca;

import open.dolphin.dto.PatientVisitSpec;
import open.dolphin.infomodel.IInfoModel;
import open.dolphin.infomodel.InfoModel;
import open.dolphin.infomodel.PatientModel;
import open.dolphin.infomodel.PatientVisitModel;
import open.dolphin.orca.pushapi.PushApi;
import open.dolphin.orca.pushapi.SubscriptionEvent;
import open.dolphin.orca.pushapi.bean.Body;
import open.dolphin.orca.pushapi.bean.Data;
import open.dolphin.orca.pushapi.bean.Response;
import open.dolphin.service.PatientService;
import open.dolphin.service.PvtService;
import org.apache.log4j.Logger;
import org.jboss.resteasy.spi.ResteasyProviderFactory;
import org.jboss.resteasy.util.Base64;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.security.RunAs;
import javax.ejb.DependsOn;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Inject;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * PvtClient.
 * ORCA PushApi で受付情報を受け取り，PvtBuilder で PatientVisitModel を作って PvtService に渡す.
 *
 * @author pns
 */
@Singleton
@Startup
@DependsOn({"OrcaHostInfo", "OrcaUserInfo"})
@RunAs("user")
public class PvtClient {
    private final Logger logger = Logger.getLogger(PvtClient.class);
    private PushApi pushApi;
    private Response subscriptionRes;
    private PvtBuilder pvtBuilder;
    @Inject
    private PvtService pvtService;

    @Inject
    private PatientService patientService;

    public PvtClient() {
        pvtBuilder = new PvtBuilder();
        pushApi = PushApi.getInstance();
        pushApi.addResponseListener(this::onResponse);
    }

    @PostConstruct
    public void subscribe() {
        pushApi.subscribe(SubscriptionEvent.ALL);
    }

    @PreDestroy
    public void unsubscribe() {
        pushApi.unsubscribe(subscriptionRes);
    }

    private void onResponse(Response res) {
        String command = res.getCommand();

        switch (command) {
            case "subscribed":
                subscriptionRes = res;
            case "unsubscribed":
                logger.info(String.format("command = %s\nreq.id = %s\nsub.id = %s\n\n", command, res.getReqId(), res.getSubId()));
                break;

            case "event":
                Data data = res.getData();
                Body body = data.getBody();
                String event = data.getEvent();
                logger.info(String.format("[%s] event[%s] mode[%s]", body.getPatient_ID(), event, body.getPatient_Mode()));

                // 受付通知: mode = add / modify / delete
                if (event.equals(SubscriptionEvent.ACCEPT.eventName())) {
                    DummyHeader.set();
                    switch (body.getPatient_Mode()) {
                        case "delete":
                            String ptId = body.getPatient_ID(); // 患者番号 002906
                            String pvtTime = body.getAccept_Date() + 'T' + body.getAccept_Time(); // 2016-12-02T16:03:38

                            long removePk = getPvtListToday().stream()
                                    .filter(pvt -> pvt.getPatientId().equals(ptId))
                                    .filter(pvt -> pvt.getPvtDate().equals(pvtTime))
                                    .map(PatientVisitModel::getId).findAny().orElse(0L);
                            if (removePk != 0) {
                                pvtService.removePvt(removePk);
                            }
                            logger.info("pvt removed [" + ptId + "]");
                            break;

                        case "add":
                        case "modify":
                            pvtBuilder.build(body);
                            PatientVisitModel model = pvtBuilder.getProduct();
                            pvtService.addPvt(model);
                            logger.info("addPvt [" + model.getPatient().getPatientId() + "]");
                            break;
                    }

                } else if (event.equals(SubscriptionEvent.INFORMATION.eventName())) {
                    // 患者登録通知 - pvt にある患者情報が orca で書き換えられた場合の対応
                    if ("modify".equals(body.getPatient_Mode())) {
                        DummyHeader.set();
                        String ptId = body.getPatient_ID(); // 患者番号 002906
                        List<PatientVisitModel> pvts = getPvtListToday().stream()
                                .filter(pvt -> pvt.getPatientId().equals(ptId)).collect(Collectors.toList());

                        pvts.stream().forEach(pvt -> {
                            pvtBuilder.build(body);
                            PatientModel patientModel = pvtBuilder.getProduct().getPatient();
                            pvt.setPatient(patientModel);
                            pvtService.addPvt(pvt);
                        });
                        logger.info("modify patient info [" + ptId + "]");
                    }
                }
                break;
        }
    }

    /**
     * 今日の pvt リストを返す.
     *
     * @return List of today's pvt
     */
    private List<PatientVisitModel> getPvtListToday() {
        PatientVisitSpec spec = new PatientVisitSpec();
        spec.setDate(LocalDate.now().format(DateTimeFormatter.ISO_DATE));
        spec.setSkipCount(0);
        return pvtService.getPvtList(spec);
    }

    /**
     * 内部から PvtService を呼ぶためのダミーヘッダ
     */
    private static class DummyHeader implements HttpHeaders {
        private static DummyHeader dummyHeader = new DummyHeader();
        private static String header;

        private DummyHeader() {
        }

        public static void set() {
            String str = IInfoModel.DEFAULT_FACILITY_OID + InfoModel.COMPOSITE_KEY_MAKER
                    + "dummyUser" + InfoModel.PASSWORD_SEPARATOR + "dummyPass";
            header = Base64.encodeBytes(str.getBytes());
            Map<Class<?>, Object> contextDataMap = ResteasyProviderFactory.getContextDataMap();
            contextDataMap.put(HttpHeaders.class, dummyHeader);
        }

        @Override
        public List<String> getRequestHeader(String s) {
            return null;
        }

        @Override
        public String getHeaderString(String s) {
            return header;
        }

        @Override
        public MultivaluedMap<String, String> getRequestHeaders() {
            return null;
        }

        @Override
        public List<MediaType> getAcceptableMediaTypes() {
            return null;
        }

        @Override
        public List<Locale> getAcceptableLanguages() {
            return null;
        }

        @Override
        public MediaType getMediaType() {
            return null;
        }

        @Override
        public Locale getLanguage() {
            return null;
        }

        @Override
        public Map<String, Cookie> getCookies() {
            return null;
        }

        @Override
        public Date getDate() {
            return null;
        }

        @Override
        public int getLength() {
            return 0;
        }
    }
}
