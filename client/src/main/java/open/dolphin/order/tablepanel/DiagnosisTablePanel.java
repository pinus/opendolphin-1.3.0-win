package open.dolphin.order.tablepanel;

import open.dolphin.client.ClientContext;
import open.dolphin.client.GUIConst;
import open.dolphin.delegater.OrcaDelegater;
import open.dolphin.dto.OrcaEntry;
import open.dolphin.event.ProxyAction;
import open.dolphin.helper.PNSTriple;
import open.dolphin.helper.Task;
import open.dolphin.infomodel.RegisteredDiagnosisModel;
import open.dolphin.order.IStampEditor;
import open.dolphin.order.MasterItem;
import open.dolphin.ui.Focuser;
import open.dolphin.ui.ObjectReflectTableModel;
import open.dolphin.ui.PNSCellEditor;
import org.apache.commons.lang3.StringUtils;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * ItemTablePanel を extend して作った DiagnosisTablePanel.
 * オリジナルは RegisteredDiagnosisModel ベースだったが，ItemTablePanel に合わせて MasterItem ベースに変更した.
 * setValue で入ってくるときは必ず１個だが，getValue で出て行くときには複数病名になっている可能性があるため，
 * {@code List<RegisteredDiagnosisModel>} で処理する.
 *
 * @author pns
 */
public class DiagnosisTablePanel extends ItemTablePanel {
    private static final long serialVersionUID = 1L;

    // 傷病名の修飾語コード
    private static final String MODIFIER_CODE = "ZZZ";
    // 傷病名手入力時につけるコード
    private static final String HAND_CODE = "0000999";
    // 修飾語付き傷病名表示
    private static final ImageIcon INFO_BUTTON_IMAGE = GUIConst.ICON_INFORMATION_16;
    private static final String LABEL_COMBINED_DIAGNOSIS = "連結した傷病名:";
    private static final String TOOLTIP_COMBINE = "テーブルの行を連結して修飾語付きの傷病名にします";
    // 複合病名を表示するフィールド
    private JTextField combinedDiagnosis;
    // 状態表示ラベル
    private JLabel stateLabel;
    // ItemTableModel のフィールド変数
    private JTable table;
    private ObjectReflectTableModel<MasterItem> tableModel;
    private JButton removeButton;
    private JButton clearButton;

    public DiagnosisTablePanel(IStampEditor parent) {
        super(parent);
        init();
    }

    private void init() {
        // フィールド変数のコピー
        table = getTable();
        table.getInputMap().put(KeyStroke.getKeyStroke("ENTER"), "startEdit");
        table.getActionMap().put("startEdit", new ProxyAction(() -> {
            int row = table.getSelectedRow();
            int col = 2; // エリアス

            if (row >= 0 && table.isCellEditable(row, col)) {
                PNSCellEditor editor = (PNSCellEditor) table.getColumnModel().getColumn(col).getCellEditor();
                Focuser.requestFocus(editor.getComponent());
                table.editCellAt(row, col);
            }
        }));

        tableModel = getTableModel();
        removeButton = getRemoveButton();
        clearButton = getClearButton();
    }

    /**
     * DiagnosisTablePanel の TableModel.
     * 収納されている Object は MasterItem.
     *
     * @return {@code ObjectReflectTableModel<MasterItem>}
     */
    @Override
    public ObjectReflectTableModel<MasterItem> createTableModel() {
        List<PNSTriple<String, Class<?>, String>> reflectList = Arrays.asList(
                new PNSTriple<>(" コード", String.class, "getCode"),
                new PNSTriple<>("　疾患名/修飾語", String.class, "getName"),
                new PNSTriple<>("　エイリアス", String.class, "getDummy")
        );
        setTableColumnWidth(new int[]{90, 200, 200});

        return new ObjectReflectTableModel<MasterItem>(reflectList) {
            private static final long serialVersionUID = 1L;

            @Override
            public boolean isCellEditable(int row, int col) {
                MasterItem model = getObject(row);

                // 疾患名カラムは HAND_CODE の時のみ編集可能
                if (col == 1) {
                    return model != null && HAND_CODE.equals(model.getCode());

                    //エリアスカラムは modifier でなければ編集可能
                } else if (col == 2) {
                    return model != null && (!model.getCode().startsWith(MODIFIER_CODE));
                }
                return false;
            }

            @Override
            public void setValueAt(Object o, int row, int col) {
                if (o == null) {
                    return;
                }
                String value = (String) o;

                MasterItem model = getObject(row);

                if (col == 1) {
                    // 名前コラムに Object が入力されていた場合は HAND_CODE とする
                    if (!value.equals("")) {
                        // 登録されている MasterItem がなければ作成して加える
                        if (model == null) {
                            model = new MasterItem();
                            model.setName(value);
                            model.setCode(HAND_CODE);
                            addRow(model);

                            // 登録されている MasterItem があれば，HAND_CODE に変更する
                        } else {
                            model.setName(value);
                            model.setCode(HAND_CODE);
                            fireTableCellUpdated(row, col);
                        }
                        checkState();
                    }
                } else if (col == 2) {
                    // エリアスコラムは　MasterItem の dummy を間借り
                    if (model != null) {
                        model.setDummy(value);
                    }
                }
            }
        };
    }

    /**
     * テーブル下のコンポネントを作る.
     *
     * @return JPanel
     */
    @Override
    public JPanel createSouthPanel() {
        combinedDiagnosis = new JTextField(20);
        combinedDiagnosis.setMaximumSize(new Dimension(10, 22));
        combinedDiagnosis.setFocusable(false);
        combinedDiagnosis.setEditable(false);
        combinedDiagnosis.setToolTipText(TOOLTIP_COMBINE);
        stateLabel = new JLabel();

        // 南パネルを生成する
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        panel.add(new JLabel(LABEL_COMBINED_DIAGNOSIS));
        panel.add(combinedDiagnosis);
        panel.add(new JLabel(INFO_BUTTON_IMAGE));
        panel.add(stateLabel);
        panel.add(Box.createHorizontalGlue());
        panel.add(getRemoveButton());
        panel.add(getClearButton());

        return panel;
    }

    @Override
    public void clear() {
        super.clear();
        combinedDiagnosis.setText("");
    }

    /**
     * マスタ検索テーブルで選択されたアイテムを編集テーブルへ取り込む.
     *
     * @param mItem MasterItem
     */
    @Override
    public void receiveMaster(MasterItem mItem) {

        if (mItem == null) {
            return;
        }

        // ZZZ コードなら，接頭語（ZZZ1~7）なら頭から挿入
        if (mItem.getCode().matches("^ZZZ[1-7].*")) {
            tableModel.insertRow(0, mItem);
        } else {
            tableModel.addRow(mItem);
        }

        // ボタンコントロールと通知
        checkState();
    }

    /**
     * テーブルをスキャンし，傷病名コンポジットして combinedDiagnosis に表示する.
     */
    public void reconstractDiagnosis() {

        StringBuilder sb = new StringBuilder();
        int count = tableModel.getObjectCount();
        for (int i = 0; i < count; i++) {
            MasterItem diag = tableModel.getObject(i);
            sb.append(diag.getName());
        }
        combinedDiagnosis.setText(sb.toString());
    }

    /**
     * 修飾語をふくんでいるかどうかを返す.
     */
    private boolean hasModifier() {
        boolean hasModifier = false;
        int count = tableModel.getObjectCount();
        for (int i = 0; i < count; i++) {
            MasterItem diag = tableModel.getObject(i);
            if (diag.getCode().startsWith(MODIFIER_CODE)) {
                hasModifier = true;
                break;
            }
        }
        return hasModifier;
    }

    /**
     * diagnosis と alias から "diagnosis,alias" の名前を作る.
     * getValue で使う.
     *
     * @param diag  diagnosis
     * @param alias alias
     * @return diagnosis, alias
     */
    private String getDiagnosisWithAlias(String diag, String alias) {
        if (StringUtils.isEmpty(alias)) {
            return diag;
        } else {
            return String.format("%s,%s", diag, alias);
        }
    }

    /**
     * "diagnosis,alias" の形の病名から Alias を取り出す.
     * setValue で使う.
     *
     * @param name dianogis,alias
     * @return alias
     */
    private String getDiagnosisAlias(String name) {
        String ret = null;
        int idx = name.indexOf(',');
        if (idx > 0) {
            ret = name.substring(idx + 1).trim();
        }
        return ret;
    }

    /**
     * 傷病名テーブルをスキャンし修飾語つきの傷病にして返す.
     * 受けるのは DiagnosisDocument#propertyChange，StampBoxPlugin.EditorValueListener.
     * alias が設定されている場合は，スタンプに登録されたとき alias　がスタンプ名として採用される.
     * see ModuleInfoBean#toString().
     *
     * @return {@code ArrayList<RegisteredDiagnosisModel>}
     */
    @Override
    public Object getValue() {

        if (hasModifier()) {
            return getValue1();
        } else {
            return getValue2();
        }
    }

    /**
     * スタンプから RegisteredDiagnosis を受け取って，MasterItem に変換してセット.
     * ここで受け取る病名は alias を含んでいる可能性がある.
     *
     * @param o RegisteredDiagnosisModel
     */
    @Override
    public void setValue(Object o) {
        if (o == null) {
            return;
        }

        RegisteredDiagnosisModel rd = (RegisteredDiagnosisModel) o;
        // . で区切られたコードを分解してコード配列を作る
        final String[] codes = rd.getDiagnosisCode().split("\\.");
        for (int i = 0; i < codes.length; ++i) {
            // 修飾語は４桁. コードにZZZを追加する.
            if (codes[i].length() == 4) {
                codes[i] = "ZZZ" + codes[i];
            }
        }
        // エリアスを切り出しておく
        final String alias = getDiagnosisAlias(rd.getDiagnosis());

        // 分解したコードのそれぞれについて，不足情報を ORCA に問い合わせる
        String message = "傷病名検索";
        String note = "傷病名を検索しています...";
        Component c = SwingUtilities.getWindowAncestor(this);

        Task<List<OrcaEntry>> task = new Task<List<OrcaEntry>>(c, message, note, 30 * 1000) {
            @Override
            protected List<OrcaEntry> doInBackground() {
                // 傷病名コードからDiseaseEntryを取得
                OrcaDelegater delegater = new OrcaDelegater();
                return delegater.findDiagnosis(Arrays.asList(codes));
            }

            @Override
            protected void succeeded(List<OrcaEntry> result) {
                if (result == null) {
                    return;
                }

                // 取得したDiseaseEntryから MasterItem を作成しテーブルに追加
                // 順番がばらばらで帰ってくるので元の順に並べ替える
                String codeSystem = ClientContext.getString("mml.codeSystem.diseaseMaster");

                for (String code : codes) {
                    for (OrcaEntry entry : result) {
                        if (code.equals(entry.getCode())) {
                            MasterItem model = new MasterItem();
                            model.setName(entry.getName());
                            model.setCode(entry.getCode());
                            model.setMasterTableId(codeSystem);
                            // alias は dummy を間借りする
                            if (!entry.getCode().startsWith(MODIFIER_CODE)) {
                                model.setDummy(alias);
                            }
                            tableModel.addRow(model);
                            break;
                        }
                    }
                }
                // ボタンコントロールと通知
                checkState();
            }
        };
        // task.setMillisToPopup(200);
        task.execute();
    }

    /**
     * 病名修飾のある場合の getValue.
     *
     * @return {@code List<RegisteredDiagnosisMode>}
     */
    private Object getValue1() {

        List<RegisteredDiagnosisModel> ret = new ArrayList<>(1);
        RegisteredDiagnosisModel rd = new RegisteredDiagnosisModel();

        StringBuilder name = new StringBuilder();
        StringBuilder code = new StringBuilder();
        String alias = null;

        // テーブルをスキャンする
        for (Object o : tableModel.getObjectList()) {
            MasterItem mItem = (MasterItem) o;
            String diagCode = mItem.getCode();

            if (diagCode.startsWith(MODIFIER_CODE)) {
                // 修飾語の場合は ZZZ をトリムする （ORCA 実装）
                diagCode = diagCode.substring(MODIFIER_CODE.length());

            } else {
                // 修飾語でないものがみつかったら，基本病名と見なしパラメータを設定する
                // 修飾語がある場合は，基本病名は必ず１つのはず.
                rd.setDiagnosisCodeSystem(mItem.getMasterTableId());
                rd.setCategory(open.dolphin.infomodel.IInfoModel.DEFAULT_DIAGNOSIS_CATEGORY);
                rd.setCategoryDesc(open.dolphin.infomodel.IInfoModel.DEFAULT_DIAGNOSIS_CATEGORY_DESC);
                rd.setCategoryCodeSys(open.dolphin.infomodel.IInfoModel.DEFAULT_DIAGNOSIS_CATEGORY_CODESYS);

                alias = mItem.getDummy(); // alias を保存
            }
            // コードを . で連結する
            if (code.length() > 0) {
                code.append(".");
            }
            code.append(diagCode);
            // 名前を連結する
            name.append(mItem.getName());
        }

        // 名前とコードを設定する
        rd.setDiagnosis(getDiagnosisWithAlias(name.toString(), alias));
        rd.setDiagnosisCode(code.toString());

        ret.add(rd);
        return ret;
    }

    /**
     * 病名修飾のない場合の getValue.
     *
     * @return {@code List<RegisteredDiagnosisModel>}
     */
    private Object getValue2() {
        List<RegisteredDiagnosisModel> ret = new ArrayList<>();

        tableModel.getObjectList().forEach(mItem -> {
            RegisteredDiagnosisModel rd = new RegisteredDiagnosisModel();
            // 診断にエリアスが指定されている場合，dummy に入っている
            rd.setDiagnosis(getDiagnosisWithAlias(mItem.getName(), mItem.getDummy()));
            rd.setDiagnosisCode(mItem.getCode());
            rd.setDiagnosisCodeSystem(mItem.getMasterTableId());
            rd.setCategory(open.dolphin.infomodel.IInfoModel.DEFAULT_DIAGNOSIS_CATEGORY);
            rd.setCategoryDesc(open.dolphin.infomodel.IInfoModel.DEFAULT_DIAGNOSIS_CATEGORY_DESC);
            rd.setCategoryCodeSys(open.dolphin.infomodel.IInfoModel.DEFAULT_DIAGNOSIS_CATEGORY_CODESYS);

            ret.add(rd);
        });
        return ret;
    }

    /**
     * DiagnosisTableModel 独自の checkState.
     */
    @Override
    public void checkState() {
        // empty
        if (tableModel.getObjectCount() == 0) {
            removeButton.setEnabled(false);
            clearButton.setEnabled(false);
            stateLabel.setText("傷病名がありません");
            setValid(false);

        } else {
            removeButton.setEnabled(tableModel.getObject(table.getSelectedRow()) != null);
            clearButton.setEnabled(true);
            setValid(isValidModel());
        }
        reconstractDiagnosis();
    }

    private boolean isValidModel() {
        // 基本病名の数
        int baseDiagnosisCount = 0;
        // 修飾語があるかどうか
        boolean hasModifier = false;

        for (Object o : tableModel.getObjectList()) {
            if (((MasterItem) o).getCode().startsWith("ZZZ")) {
                hasModifier = true;
            } else {
                baseDiagnosisCount++;
            }
        }

        if (baseDiagnosisCount == 0) {
            stateLabel.setText("基本傷病名がありません");
            return false;

        } else if (baseDiagnosisCount == 1 || !hasModifier) {
            stateLabel.setText("有効なデータになっています");
            return true;

        } else {
            stateLabel.setText("修飾語がある場合は、基本傷病名は一つです");
            return false;
        }
    }
}
