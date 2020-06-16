package open.dolphin.order.stampeditor;

import open.dolphin.client.ClientContext;
import open.dolphin.client.GUIConst;
import open.dolphin.event.ValidListener;
import open.dolphin.infomodel.IInfoModel;
import open.dolphin.orca.ClaimConst;
import open.dolphin.order.IStampEditor;
import open.dolphin.order.MasterSearchPanel;
import open.dolphin.order.tablepanel.DiagnosisTablePanel;
import open.dolphin.order.tablepanel.ItemTablePanel;
import open.dolphin.order.tablepanel.RadiologyTablePanel;
import open.dolphin.order.tablepanel.RecipeTablePanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.util.Objects;

/**
 * StampEditor.
 * StampEditorProxyPanel では，これを必要なだけインスタンス化して保持する.
 *
 * @author pns
 */
public class StampEditor extends JPanel implements IStampEditor {
    private static final long serialVersionUID = 1L;
    public static final String MASTER_SEARCH_FIELD = "masterSearchField";
    public static final String MASTER_TABLE = "masterTable";
    public static final String ITEM_TABLE = "itemTable";

    private ValidListener validListener;
    private boolean isValidModel;
    private String title;
    private ItemTablePanel tablePanel;
    private MasterSearchPanel masterSearchPanel;
    private String entity;
    private Logger logger = LoggerFactory.getLogger(StampEditor.class);

    public StampEditor(String entity) {
        this.entity = entity;

        // フォーカス処理: tab で search field -> search panel -> table panel の順番にフォーカス移動する
        setFocusTraversalPolicyProvider(true);
        setFocusTraversalPolicy(new FocusTraversalPolicy() {
            @Override
            public Component getComponentAfter(Container aContainer, Component aComponent) {
                switch (Objects.isNull(aComponent.getName()) ? "" : aComponent.getName()) {
                    case StampEditor.MASTER_SEARCH_FIELD:
                        getMasterSearchPanel().requestFocusOnTable();
                        break;

                    case StampEditor.MASTER_TABLE:
                        getTablePanel().requestFocusOnTable();
                        break;

                    default:
                        enter();
                        break;
                }
                return null;
            }

            @Override
            public Component getComponentBefore(Container aContainer, Component aComponent) {
                switch (Objects.isNull(aComponent.getName()) ? "" : aComponent.getName()) {
                    case StampEditor.MASTER_SEARCH_FIELD:
                        getTablePanel().requestFocusOnTable();
                        break;

                    case StampEditor.ITEM_TABLE:
                        getMasterSearchPanel().requestFocusOnTable();
                        break;

                    default:
                        enter();
                        break;
                }
                return null;
            }

            @Override
            public Component getFirstComponent(Container aContainer) { return null; }

            @Override
            public Component getLastComponent(Container aContainer) { return null; }

            @Override
            public Component getDefaultComponent(Container aContainer) { return null; }
        });
    }

    @Override
    public void start() {

        // タイトル設定
        this.setTitle(ClaimConst.EntityNameMap.get(entity));

        switch (entity) {
            case IInfoModel.ENTITY_MED_ORDER:
                tablePanel = new RecipeTablePanel(this);
                break;
            case IInfoModel.ENTITY_RADIOLOGY_ORDER:
                tablePanel = new RadiologyTablePanel(this);
                break;
            case IInfoModel.ENTITY_DIAGNOSIS:
                tablePanel = new DiagnosisTablePanel(this);
                break;
            default:
                tablePanel = new ItemTablePanel(this);
                break;
        }

        // MasterSearchPanel 作成
        masterSearchPanel = new MasterSearchPanel(entity);
        masterSearchPanel.addOrderListener(tablePanel::receiveMaster);

        // CLAIM パラメータを設定する
        tablePanel.setOrderName(ClaimConst.EntityNameMap.get(entity));
        tablePanel.setClassCode(ClaimConst.ClaimClassCodeMap.get(entity));
        tablePanel.setClassCodeId(ClaimConst.CLASS_CODE_ID);
        tablePanel.setSubClassCodeId(ClaimConst.SUBCLASS_CODE_ID);

        // 上にスタンプのセットパネル，下にマスタのセットパネルを配置する
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        add(tablePanel);
        add(masterSearchPanel);
        setPreferredSize(GUIConst.DEFAULT_STAMP_EDITOR_SIZE);
    }

    /**
     * StampEditorProxyPanel を show した際に呼ばれる.
     */
    @Override
    public void enter() {
        tablePanel.checkState(); // enter したときの 右ボタンの制御
        masterSearchPanel.requestFocusOnTextField();
    }

    @Override
    public void dispose() {
    }

    /**
     * ItemTablePanel を返す.
     *
     * @return ItemTablePanel
     */
    public ItemTablePanel getTablePanel() {
        return tablePanel;
    }

    /**
     * ItemTablePanel をセットする.
     *
     * @param tablePanel ItemTablePanel
     */
    public void setTablePanel(ItemTablePanel tablePanel) {
        this.tablePanel = tablePanel;
    }

    /**
     * MasterSearchPanel を返す.
     *
     * @return MasterSearchPanel
     */
    public MasterSearchPanel getMasterSearchPanel() { return masterSearchPanel; }

    /**
     * MasterSearchPanel をセットする.
     *
     * @param panel MasterSearchPanel
     */
    public void setMasterSearchPanel(MasterSearchPanel panel) { masterSearchPanel = panel; }

    /**
     * セットされたエンティティーを返す.
     *
     * @return Entity
     */
    public String getEntity() {
        return entity;
    }

    /**
     * Entity を TablePanel に伝える.
     *
     * @param entity Entity
     */
    @Override
    public void setEntity(String entity) {
        this.entity = entity;
        tablePanel.setEntity(entity);
    }

    /**
     * TablePanel からデータを取り出す.
     *
     * @return TablePanel のデータ
     */
    @Override
    public Object getValue() {
        return tablePanel.getValue();
    }

    /**
     * TablePanel にデータをセットする.
     *
     * @param val TablePanel にセットするデータ
     */
    @Override
    public void setValue(Object val) {
        tablePanel.setValue(val);
    }

    /**
     * StampEditorDialog の OK ボタンを制御するためのリスナ.
     *
     * @param listener Validity listener
     */
    @Override
    public void addValidListener(ValidListener listener) {
        validListener = listener;
    }

    @Override
    public boolean isValidModel() {
        return isValidModel;
    }

    @Override
    public void setValid(boolean valid) {
        isValidModel = valid;
        validListener.validity(valid);
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public void setTitle(String title) {
        String buf = title +
            ClientContext.getString("application.title.editorText") +
            ClientContext.getString("application.title.separator") +
            ClientContext.getString("application.title");
        this.title = buf;
    }
}
