package open.dolphin.orca;

import open.dolphin.infomodel.*;
import open.dolphin.orca.orcaapi.OrcaApi;
import open.dolphin.orca.orcaapi.bean.*;
import open.dolphin.orca.orcadao.OrcaDao;
import open.dolphin.orca.pushapi.bean.Body;
import open.dolphin.util.ModelUtils;
import org.jboss.logging.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * ORCA PushAPI & ORCA API version PvtBuilder.
 *
 * @author pns
 */
public class PvtBuilder {

    private final Logger logger = Logger.getLogger(PvtBuilder.class);
    private PatientVisitModel pvtModel;

    /**
     * できあがった PatientVisitModel を返す.
     *
     * @return PatientVisitModel
     */
    public PatientVisitModel getProduct() {
        return pvtModel;
    }

    /**
     * Body から PatientVisitModel を構築する.
     *
     * @param body PushApi で返ってきた Body
     */
    public void build(Body body) {

        // Dolphin Models
        pvtModel = new PatientVisitModel();
        PatientModel patientModel = new PatientModel();
        List<PVTHealthInsuranceModel> pvtInsurances = new ArrayList<>();
        AddressModel addressModel = new AddressModel();
        SimpleAddressModel simpleAddressModel = new SimpleAddressModel(); // Embedded in PatientModel
        TelephoneModel[] telephoneModel = {new TelephoneModel(), new TelephoneModel()};

        // body にのってるデータを Dolphin Models に格納する分
        String ptId = body.getPatient_ID(); // 患者番号 002906
        patientModel.setPatientId(ptId);

        String pvtDate = body.getAccept_Date() + "T" + body.getAccept_Time(); // 2019-01-24T14:08:05
        String deptCode = body.getDepartment_Code(); // 19
        String drCode = body.getPhysician_Code(); // 10001
        String insuranceUid = body.getInsurance_Combination_Number(); // 0001, 0002,...

        pvtModel.setPvtDate(pvtDate);
        pvtModel.setInsuranceUid(insuranceUid);

        // 以下は OrcaApi で AcceptlstInformation を取得して Dolphin Models に格納する分
        OrcaApi orcaApi = OrcaApi.getInstance();
        Acceptlstreq acceptlstreq = new Acceptlstreq();
        acceptlstreq.setAcceptance_Date(body.getAccept_Date());
        acceptlstreq.setDepartment_Code(deptCode);
        acceptlstreq.setPhysician_Code(drCode);

        Acceptlstres acceptlstres = orcaApi.post(acceptlstreq, "03"); //  class=03(全受付対象)
        AcceptlstInformation accInfo = Arrays.stream(acceptlstres.getAcceptlst_Information())
                .filter(aInfo -> aInfo.getPatient_Information().getPatient_ID().equals(ptId))
                .findFirst().get(); // 受付が来ているので null にはならない

        //
        // Department "科名,科コード,Dr名,Drコード,JMARIコード", memo "診察"
        //
        String deptName = accInfo.getDepartment_WholeName();
        String drName = accInfo.getPhysician_WholeName();
        String jmari = OrcaHostInfo.getInstance().getJmariCode(); // jmari コード
        pvtModel.setDepartment(String.join(",", deptName, deptCode, drName, drCode, jmari));
        // Medical_Information (診療内容) は 01 などの数値で返る. api ではこれを「診察」などの文字にすることはできない (by サポートセンター)
        String medicalInfoCode = accInfo.getMedical_Information();
        pvtModel.setMemo(OrcaDao.getInstance().getExtraInfo().getKanricd1012().get(medicalInfoCode)); // ので DAO で取っておいたのを使う

        //
        // PatientInformation (Acceptlstres の Patient_Information には住所情報が入っていないので取り直す)
        //
        PatientInformation patInfo = orcaApi.get(ptId).getPatient_Information();
        // 名前
        String wholeName = processString(patInfo.getWholeName());
        patientModel.setFullName(wholeName);
        // カナ名
        String wholeNameInKana = processString(patInfo.getWholeName_inKana());
        patientModel.setKanaName(wholeNameInKana);
        // 名前を姓名に分ける
        String[] name = wholeName.split(" ");
        patientModel.setFamilyName(name[0]);
        patientModel.setGivenName(name[1]);
        String[] kanaName = wholeNameInKana.split(" ");
        patientModel.setKanaFamilyName(kanaName[0]);
        patientModel.setKanaGivenName(kanaName[1]);
        // 生年月日
        String birthday = patInfo.getBirthDate();
        patientModel.setBirthday(birthday);
        // 性別 1/2 → male/female
        String sex = "1".equals(patInfo.getSex()) ? "male" : "female";
        patientModel.setGender(sex);

        //
        // Home Address Information
        //
        HomeAddressInformation addrInfo = patInfo.getHome_Address_Information();
        if (Objects.nonNull(addrInfo)) {
            String zip = addrInfo.getAddress_ZipCode();
            addressModel.setZipCode(zip);
            String address1 = processString(addrInfo.getWholeAddress1());
            String address2 = processString(addrInfo.getWholeAddress2());
            addressModel.setAddress(address1 + address2);
            String telephone1 = addrInfo.getPhoneNumber1();
            String telephone2 = addrInfo.getPhoneNumber2();
            patientModel.setTelephone(telephone1);
            telephoneModel[0].setMemo(telephone1); // 電話番号は memo に入れることになっている.
            telephoneModel[1].setMemo(telephone2);

            // 住所を Embedded に変換する
            String addr = addressModel.getAddress().replace("　", " ") // 全角スペース→半角
                    .replace('ー', '-'); // 全角マイナス→半角
            simpleAddressModel.setAddress(addr);
            simpleAddressModel.setZipCode(zip);
        }

        //
        // HealthInsurance Information
        //
        HealthInsuranceInformation[] healthInfo = patInfo.getHealthInsurance_Information();
        if (Objects.nonNull(healthInfo)) {
            Arrays.stream(patInfo.getHealthInsurance_Information())
                    .filter(hInfo -> hInfo.getInsurance_Combination_Number() != null)
                    .forEach(hInfo -> {
                        PVTHealthInsuranceModel phModel = new PVTHealthInsuranceModel();
                        phModel.setGUID(hInfo.getInsurance_Combination_Number()); // 0001,0002
                        phModel.setInsuranceClassCode(hInfo.getInsuranceProvider_Class()); // 009, 060, etc
                        phModel.setInsuranceClass(hInfo.getInsuranceProvider_WholeName()); // 協会, 国保, etc
                        phModel.setInsuranceNumber(hInfo.getInsuranceProvider_Number()); // 保険者番号 01010016
                        phModel.setClientNumber(hInfo.getHealthInsuredPerson_Number()); // 番号
                        phModel.setClientGroup(hInfo.getHealthInsuredPerson_Symbol()); // 記号
                        String family = hInfo.getRelationToInsuredPerson(); // 本人=1, 家族=2
                        phModel.setFamilyClass(family == null ? null : family.equals("1") ? "true" : "false"); // 本人=true, 家族=false
                        phModel.setStartDate(hInfo.getCertificate_StartDate()); // 開始日
                        phModel.setExpiredDate(hInfo.getCertificate_ExpiredDate()); // 終了日
                        phModel.setPayOutRatio(hInfo.getInsurance_Combination_Rate_Outpatient()); // 外来負担割合
                        phModel.setPayInRatio(hInfo.getInsuranceCombination_Rate_Admission()); // 入院負担割合
                        // 公費
                        List<PVTPublicInsuranceItemModel> pModels = new ArrayList<>();
                        PublicinsuranceInformation[] pInfos = hInfo.getPublicInsurance_Information();
                        if (pInfos != null) {
                            Arrays.stream(pInfos).filter(pInfo -> pInfo.getPublicInsurance_Class() != null)
                                    .forEach(pInfo -> {
                                        PVTPublicInsuranceItemModel pModel = new PVTPublicInsuranceItemModel();
                                        pModel.setPriority("1");
                                        pModel.setProviderName(pInfo.getPublicInsurance_Name()); // 独自乳有
                                        pModel.setProvider(pInfo.getPublicInsurer_Number()); // 92014000
                                        pModel.setRecipient(pInfo.getPublicInsuredPerson_Number()); // 保険番号
                                        pModel.setStartDate(pInfo.getCertificate_IssuedDate()); // 開始日
                                        pModel.setExpiredDate(pInfo.getCertificate_ExpiredDate()); // 終了日
                                        pModel.setPaymentRatio(pInfo.getMoney_Outpatient().trim()); // 580, 0, etc
                                        String rate = pInfo.getRate_Outpatient().trim();
                                        pModel.setPaymentRatioType(rate.equals("0.00") ? "fix" : "ratio"); // fix or ratio
                                        pModels.add(pModel);
                                    });
                        }
                        phModel.setPVTPublicInsuranceItem(pModels.toArray(new PVTPublicInsuranceItemModel[0]));
                        pvtInsurances.add(phModel);
                    });

        }
        // PVTHealthInsuranceModel の beanBytes を HealthInsuranceModel にセットして PatientModel に加える
        pvtInsurances.forEach(p -> {
            HealthInsuranceModel healthInsuranceModel = new HealthInsuranceModel();
            healthInsuranceModel.setBeanBytes(ModelUtils.xmlEncode(p));
            healthInsuranceModel.setPatient(patientModel);
            patientModel.addHealthInsurance(healthInsuranceModel);
        });

        // PatientVisitModel Composition
        patientModel.setAddress(simpleAddressModel);
        patientModel.setAddresses(Arrays.asList(addressModel));
        patientModel.setTelephones(Arrays.asList(telephoneModel));
        patientModel.setPvtHealthInsurances(pvtInsurances);
        pvtModel.setPatient(patientModel);

        //logger.info("pvtModel=" + JsonConverter.toJson(pvtModel));
        //logger.info("pvtHealthInsuranceModel=" + JsonConverter.toJson(pvtModel.getPatient().getPvtHealthInsurances()));
        //logger.info("patientModel=" + JsonConverter.toJson(pvtModel.getPatient()));
        //logger.info("simpleAddressModel=" + JsonConverter.toJson(pvtModel.getPatient().getAddress()));
        //logger.info("collection of addressModel=" + JsonConverter.toJson(pvtModel.getPatient().getAddresses()));
        //logger.info("collection of telephoneModel=" + JsonConverter.toJson(pvtModel.getPatient().getTelephones()));
        //pvtInsurances.stream().map(JsonConverter::toJson).forEach(logger::info);
    }

    /**
     * null を "" に変換.
     * 全角スペースを半角スペースに変換.
     * 全角 "ー" を半角 "-" に変換.
     *
     * @param src ソース文字列
     * @return processed String
     */
    private String processString(String src) {
        return src == null ? "" : src.trim()
                .replace('　', ' ')
                .replace("ー", "-");
    }
}
