package open.dolphin.orca.orcaapi;

/**
 * Orca Api url 一覧.
 * https://www.orca.med.or.jp/receipt/tec/api/overview.html#ver480
 *
 * @author pns
 */
public class OrcaApiUrl {
    /**
     * 患者基本情報の取得 (GET): id=? 患者基本情報取得 (URL変更).
     */
    public static final String PATIENTGETV2 = "/api01rv2/patientgetv2";

    /**
     * 予約の登録、取り消し (POST): class=01 予約受付, class=02 予約取消 (URL変更).
     */
    public static final String APPOINTMODV2 = "/orca14/appointmodv2";

    /**
     * 中途終了データ作成 (POST): class=01 中途データ登録, class=02 中途データ削除, class=03 中途データ変更 (URL変更).
     */
    public static final String MEDICALMODV2 = "/api21/medicalmodv2";

    /**
     * 指定された患者の受付、取り消し (POST): class=01 受付登録, class=02 受付取消 (URL変更).
     */
    public static final String ACCEPTMODV2 = "/orca11/acceptmodv2";

    /**
     * 指定された日付の受付一覧返却 (POST): class=01 受付中取得, class=02 受付済み取得, class=03 全受付取得 (URL変更).
     */
    public static final String ACCEPTLSTV2 = "/api01rv2/acceptlstv2";

    /**
     * 指定された日付の予約一覧返却 (POST): class=01 予約一覧取得 (URL変更).
     */
    public static final String APPOINTLSTV2 = "/api01rv2/appointlstv2";

    /**
     * 点数マスタ情報登録 (POST): class=01 登録, class=02 削除, class=03 終了日設定, class=04 期間変更 (URL変更).
     */
    public static final String MEDICATONMODV2 = "/orca102/medicatonmodv2";

    /**
     * 患者番号一覧の取得 (POST): class=01 新規・更新対象, class=02 新規対象 (URL変更).
     */
    public static final String PATIENTLST1V2 = "/api01rv2/patientlst1v2";

    /**
     * 複数の患者情報取得 (POST): class=01 指定患者情報取得 (URL変更).
     */
    public static final String PATIENTLST2V2 = "/api01rv2/patientlst2v2";

    /**
     * 患者情報取得(氏名検索) (POST): class=01 指定患者情報取得 (URL変更).
     */
    public static final String PATIENTLST3V2 = "/api01rv2/patientlst3v2";

    /**
     * システム管理情報の取得 (POST): class=01 診療科対象, class=02 ドクター対象, class=03 ドクター以外の職員対象, class=04 医療機関基本情報 (URL変更).
     */
    public static final String SYSTEM01LSTV2 = "/api01rv2/system01lstv2";

    /**
     * 診療情報の返却 (POST): class=01 受診履歴取得, class=02 受診履歴診療行為内容, class=03 診療月診療行為取得, class=04 診療区分別剤点数 (URL変更).
     */
    public static final String MEDICALGETV2 = "/api01rv2/medicalgetv2";

    /**
     * 患者病名情報の返却 (POST): class=01 患者病名情報の取得 (URL変更).
     */
    public static final String DISEASEGETV2 = "/api01rv2/diseasegetv2";

    /**
     * 患者登録 (POST): class=01 患者登録, class=02 患者情報更新, class=03 患者情報削除, class=04 保険情報追加 (URL変更).
     */
    public static final String PATIENTMODV2 = "/orca12/patientmodv2";

    /**
     * 患者予約情報 (POST): class=01 患者予約情報取得 (Ver.4.7.0).
     */
    public static final String APPOINTLST2V2 = "/api01rv2/appointlst2v2";

    /**
     * 請求金額返却 (POST): class=01 請求金額シミュレーション (Ver.4.7.02013/01).
     */
    public static final String ACSIMULATEV2 = "/api01rv2/acsimulatev2";

    /**
     * 症状詳記 (POST): class=01 症状詳記登録, class=02 症状詳記削除 (Ver.4.7.0).
     */
    public static final String SUBJECTIVESV2 = "/orca25/subjectivesv2";

    /**
     * 来院患者一覧 (POST): 引数なし, 来院日の受診履歴返却 (Ver.4.7.02013/09).
     */
    public static final String VISITPTLSTV2 = "/api01rv2/visitptlstv2";

    /**
     * 入院基本情報 (POST): 引数なし, 入院基本情報設定内容返却 (Ver.4.7.02013/09).
     */
    public static final String HSCONFBASEV2 = "/api01rv2/hsconfbasev2";

    /**
     * 病棟・病室情報 (POST): 引数なし, 病棟情報返却 (Ver.4.7.02013/09).
     */
    public static final String HSCONFWARDV2 = "/api01rv2/hsconfwardv2";

    /**
     * 中途終了患者情報一覧 (POST): 引数なし, 中途終了患者情報一覧返却 (Ver.4.7.02013/10).
     */
    public static final String TMEDICALGETV2 = "/api01rv2/tmedicalgetv2";

    /**
     * 保険者一覧情報 (POST): 引数なし, 保険者一覧返却 (Ver.4.7.02013/10).
     */
    public static final String INSPROGETV2 = "/api01rv2/insprogetv2";

    /**
     * 入院患者食事等情報 (POST): 引数なし, 入院患者食事等情報返却 (Ver.4.7.02013/10).
     */
    public static final String HSMEALV2 = "/api01rv2/hsmealv2";

    /**
     * 入院患者医療区分・ADL点数情報 (POST): 引数なし, 入院患者医療区分・ADL点数情報返却 (Ver.4.7.02013/10).
     */
    public static final String HSPTEVALV2 = "/api01rv2/hsptevalv2";

    /**
     * 入院患者基本情報 (POST): 引数なし, 入院患者基本情報返却 (Ver.4.7.02013/10).
     */
    public static final String HSPTINFV2 = "/api01rv2/hsptinfv2";

    /**
     * 仮計算情報 (POST): 引数なし, 仮計算情報返却 (Ver.4.7.02013/11 ).
     */
    public static final String HSACSIMULATEV2 = "/api01rv2/hsacsimulatev2";

    /**
     * 収納情報返却 (POST): 引数なし, 収納情報返却 (Ver.4.7.02013/12 ).
     */
    public static final String INCOMEINFV2 = "/api01rv2/incomeinfv2";

    /**
     * システム情報の取得 (POST): 引数なし, システム情報の返却 (Ver.4.7.02014/10).
     */
    public static final String SYSTEMINFV2 = "/api01rv2/systeminfv2";

    /**
     * 入院登録 (POST): 引数なし, 入院登録 (Ver.4.7.02015/03).
     */
    public static final String HSPTINFMODV2 = "/orca31/hsptinfmodv2";

    /**
     * 外泊等登録 (POST): 引数なし, 外泊等登録 (Ver.4.7.02015/03 ).
     */
    public static final String HSACCTMODV2 = "/orca31/hsacctmodv2";

    /**
     * 入院患者医療区分・ADL点数登録 (POST): 引数なし, 入院患者医療区分・ADL点数登録 (Ver.4.7.02015/03 ).
     */
    public static final String HSPTEVALMODV2 = "/orca32/hsptevalmodv2";

    /**
     * ユーザー管理情報 (POST): 引数なし, ユーザー一覧 (Ver.4.8.02015/09).
     */
    public static final String MANAGEUSERSV2 = "/orca101/manageusersv2";

    /**
     * セット登録 (POST): 引数なし, 新規登録 (Ver.4.8.02015/12 ).
     */
    public static final String MEDICALSETV2 = "/orca21/medicalsetv2";

    /**
     * 出産育児一時金 (POST): 引数なし, 照会 (Ver.4.8.02016/01 ).
     */
    public static final String BIRTHDELIVERYV2 = "/orca31/birthdeliveryv2";

    /**
     * 全保険組合せ一覧取得 (POST): 引数なし, 全保険組合せ一覧取得 (Ver.4.8.02017/05).
     */
    public static final String PATIENTLST6V2 = "/api01rv2/patientlst6v2";

    /**
     * 患者病名登録 (POST): 引数なし, 患者病名登録 (Ver.5.0.02017/06).
     */
    public static final String DISEASEV2 = "/orca22/diseasev2";

    /**
     * 患者病名登録２ (POST): 引数なし, 患者病名登録２ (Ver.5.0.02017/09).
     */
    public static final String DISEASEV3 = "/orca22/diseasev3";

    /**
     * 入院会計作成 (POST): 引数なし, 入院会計作成 (Ver.4.8.02017/11).
     */
    // public static final String HSACCTMODV2 = "/orca31/hsacctmodv2";

    /**
     * 入院会計未作成チェック (POST): 引数なし, 入院会計未作成チェック (Ver.4.8.02017/11).
     */
    public static final String HSPMMV2 = "/orca31/hspmmv2";

    /**
     * 室料差額登録 (POST): 引数なし, 室料差額登録 (Ver.5.0.02017/12).
     */
    // public static final String HSACCTMODV2 = "/orca31/hsacctmodv2";

    /**
     * Ver.5.0.02018/03 (PUSH通知一括取得): json ※１ (POST).
     */
    public static final String PUSHEVENTGETV2 = "/api01rv2/pusheventgetv2";

    /**
     * マスタデータ最終更新日取得 (POST): 引数なし, マスタデータ最終更新日取得 (Ver.5.0.0).
     */
    public static final String MASTERLASTUPDATEV3 = "/orca51/masterlastupdatev3";

    /**
     * 基本情報取得 (POST): 引数なし, 基本情報取得 (Ver.5.0.0).
     */
    public static final String SYSTEM01DAILYV2 = "/api01rv2/system01dailyv2";

    /**
     * 患者メモ取得 (POST): 引数なし, 患者メモ取得 (Ver.5.0.0).
     */
    public static final String PATIENTLST7V2 = "/api01rv2/patientlst7v2";

    /**
     * 初診算定日登録 (POST): 引数なし, 初診算定日登録 (Ver.5.0.0).
     */
    public static final String MEDICALMODV23 = "/api21/medicalmodv23";

    /**
     * 入院患者照会 (POST): 引数なし, 入院患者照会 (Ver.5.0.0).
     */
    public static final String HSFINDV3 = "/orca36/hsfindv3";

    /**
     * 薬剤併用禁忌チェック (POST): 引数なし, 薬剤併用禁忌チェック (Ver.5.0.0).
     */
    public static final String CONTRAINDICATIONCHECKV2 = "/api01rv2/contraindicationcheckv2";

    /**
     * 保険・公費一覧取得 (POST): 引数なし, 保険・公費一覧取得 (Ver.5.0.0).
     */
    public static final String INSURANCEINF1V2 = "/api01rv2/insuranceinf1v2";

    /**
     * 症状詳記情報取得 (POST): 引数なし, 症状詳記情報取得 (Ver.5.0.0).
     */
    public static final String SUBJECTIVESLSTV2 = "/api01rv2/subjectiveslstv2";

    /**
     * 旧姓履歴情報取得 (POST): 引数なし, 旧姓履歴情報取得 (Ver.5.0.0).
     */
    public static final String PATIENTLST8V2 = "/api01rv2/patientlst8v2";
}
