package open.dolphin.inspector;

import open.dolphin.client.Chart;
import open.dolphin.client.ChartImpl;
import open.dolphin.client.GUIConst;
import open.dolphin.delegater.DocumentDelegater;
import open.dolphin.event.ProxyAction;
import open.dolphin.helper.DBTask;
import open.dolphin.helper.PNSTriple;
import open.dolphin.infomodel.AllergyModel;
import open.dolphin.infomodel.IInfoModel;
import open.dolphin.infomodel.ObservationModel;
import open.dolphin.project.Project;
import open.dolphin.ui.IndentTableCellRenderer;
import open.dolphin.ui.PNSScrollPane;
import open.dolphin.ui.UndoableObjectReflectTableModel;
import open.dolphin.util.ModelUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.*;
import java.util.List;

/**
 * AllergyInspector.
 *
 * @author Kazushi Minagawa, Digital Globe, Inc.
 * @author pns
 */
public class AllergyInspector implements IInspector, TableModelListener {
    public static final InspectorCategory CATEGORY = InspectorCategory.アレルギー;
    private Logger logger = LoggerFactory.getLogger(AllergyInspector.class);

    // Chart
    private final ChartImpl context;
    // TableModel
    private UndoableObjectReflectTableModel<AllergyModel> tableModel;
    // コンテナパネル
    private JPanel view;
    private JTable table;

    /**
     * AllergyInspectorオブジェクトを生成する.
     *
     * @param parent PatientInspector
     */
    public AllergyInspector(PatientInspector parent) {
        context = parent.getContext();
        initComponents();
    }

    /**
     * GUIコンポーネントを初期化する.
     */
    private void initComponents() {

        view = new JPanel(new BorderLayout());
        view.setName(CATEGORY.name());
        view.setPreferredSize(new Dimension(DEFAULT_WIDTH, 110));
        view.setMinimumSize(new Dimension(DEFAULT_WIDTH, 110));

        table = new JTable();
        table.putClientProperty("Quaqua.Table.style", "striped");
        PNSScrollPane scrollPane = new PNSScrollPane();
        scrollPane.putClientProperty("JComponent.sizeVariant", "small");
        scrollPane.setViewportView(table);

        view.add(scrollPane);

        // アレルギーテーブルを設定する
        List<PNSTriple<String, Class<?>, String>> reflectList = Arrays.asList(
                new PNSTriple<>("　要 因", String.class, "getFactor"),
                new PNSTriple<>("　反応程度", String.class, "getSeverity"),
                new PNSTriple<>("　同定日", String.class, "getIdentifiedDate")
        );

        tableModel = new UndoableObjectReflectTableModel<>(reflectList);
        tableModel.addTableModelListener(this);
        table.setModel(tableModel);
        table.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // レンダラを設定する
        table.setDefaultRenderer(Object.class,
                new IndentTableCellRenderer(IndentTableCellRenderer.NARROW, IndentTableCellRenderer.SMALL_FONT));
        // 表の高さ
        table.setRowHeight(GUIConst.DEFAULT_TABLE_ROW_HEIGHT);
        // コラム幅
        table.getColumnModel().getColumn(0).setMinWidth(120);
        table.getColumnModel().getColumn(1).setMaxWidth(3);
        table.getColumnModel().getColumn(1).setPreferredWidth(0);
        table.getColumnModel().getColumn(2).setPreferredWidth(5);

        // 右クリックによる追加削除のメニューを登録する
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    AllergyEditor.show(AllergyInspector.this);
                }
            }

            private void mabeShowPopup(MouseEvent e) {
                //  isReadOnly対応
                if (context.isReadOnly() || !e.isPopupTrigger()) {
                    return;
                }

                JPopupMenu pop = new JPopupMenu();
                // 追加
                JMenuItem item = new JMenuItem("追加");
                item.setIcon(GUIConst.ICON_LIST_ADD_16);
                pop.add(item);
                item.addActionListener(ae -> AllergyEditor.show(AllergyInspector.this));

                // 削除
                final int row = table.rowAtPoint(e.getPoint());
                if (tableModel.getObject(row) != null) {
                    pop.add(new JSeparator());
                    JMenuItem item2 = new JMenuItem("削除");
                    item2.setIcon(GUIConst.ICON_LIST_REMOVE_16);
                    pop.add(item2);
                    item2.addActionListener(ae -> delete(row));
                }
                pop.show(e.getComponent(), e.getX(), e.getY());
            }

            @Override
            public void mousePressed(MouseEvent e) {
                mabeShowPopup(e);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                // Windows
                mabeShowPopup(e);
            }
        });

        InputMap im = table.getInputMap();
        ActionMap am = table.getActionMap();

        // undo
        im.put(META_Z, "undo");
        am.put("undo", new ProxyAction(tableModel::undo));
        im.put(SHIFT_META_Z, "redo");
        am.put("redo", new ProxyAction(tableModel::redo));

        // delete
        im.put(BACK_SPACE, "remove");
        am.put("remove", new ProxyAction(() -> {
            int row = table.getSelectedRow();
            if (row >= 0) { delete(row); }
        }));
    }

    public Chart getContext() { return context; }

    /**
     * レイアウトパネルを返す.
     *
     * @return JPanel
     */
    @Override
    public JPanel getPanel() { return view; }

    @Override
    public String getName() { return CATEGORY.name(); }

    @Override
    public String getTitle() { return CATEGORY.title(); }

    /**
     * 選択された AllergyModel を返す.
     *
     * @return AllergyModel selected or null
     */
    public AllergyModel getSelectedModel() {
        int row = table.getSelectedRow();
        return row >= 0 ? tableModel.getObject(row) : null;
    }

    /**
     * データ部分までスクロールする.
     *
     * @param ascending ascending=true
     */
    private void scroll(boolean ascending) {
        int cnt = tableModel.getObjectCount();
        if (cnt > 0) {
            int row = 0;
            if (ascending) {
                row = cnt - 1;
            }
            Rectangle r = table.getCellRect(row, row, true);
            table.scrollRectToVisible(r);
        }
    }

    /**
     * アレルギー情報を表示する.
     */
    @Override
    public void update() {
        List<AllergyModel> list = context.getKarte().getAllergyEntry();
        if (list != null && !list.isEmpty()) {
            boolean asc = Project.getPreferences().getBoolean(Project.DOC_HISTORY_ASCENDING, false);
            if (asc) {
                list.sort(Comparator.naturalOrder());
            } else {
                list.sort(Comparator.reverseOrder());
            }
            tableModel.setObjectList(list);
            scroll(asc);
        }
    }

    /**
     * アレルギーデータを追加する. AllergyEditor から呼ばれる.
     *
     * @param model AllergyModel
     */
    public void add(final AllergyModel model) {
        // 選択があって add が呼ばれたら即ちそれは置換
        int row = table.getSelectedRow();
        if (row >= 0) {
            delete(row);
            tableModel.undoableInsertRow(row, model);

        } else {
            boolean asc = Project.getPreferences().getBoolean(Project.DOC_HISTORY_ASCENDING, false);
            if (asc) {
                tableModel.undoableAddRow(model);
            } else {
                tableModel.undoableInsertRow(0, model);
            }
            scroll(asc);
        }
    }

    /**
     * テーブルで選択したアレルギーを削除する.
     *
     * @param row 削除行
     */
    public void delete(final int row) {
        tableModel.undoableDeleteRow(row);
    }

    /**
     * TableModel 操作後に呼ばれる. 操作内容をデータベースに記録する.
     *
     * @param e TableModelEvent
     */
    @Override
    public void tableChanged(TableModelEvent e) {

        DocumentDelegater delegater = new DocumentDelegater();

        if (e.getType() == TableModelEvent.INSERT) {
            AllergyModel model = tableModel.getObject(e.getFirstRow());

            // GUI の同定日をTimeStampに変更する
            Date date = ModelUtils.getDateTimeAsObject(model.getIdentifiedDate() + "T00:00:00");

            List<ObservationModel> observations = new ArrayList<>(1);
            ObservationModel observation = new ObservationModel();
            observation.setKarte(context.getKarte());
            observation.setCreator(Project.getUserModel());
            observation.setObservation(IInfoModel.OBSERVATION_ALLERGY);
            observation.setPhenomenon(model.getFactor());
            observation.setCategoryValue(model.getSeverity());
            observation.setConfirmed(date);
            observation.setRecorded(new Date());
            observation.setStarted(date);
            observation.setStatus(IInfoModel.STATUS_FINAL);
            observation.setMemo(model.getMemo());

            observations.add(observation);

            DBTask<Long> task = new DBTask<Long>(context) {
                @Override
                protected Long doInBackground() {
                    List<Long> ids = delegater.addObservations(observations);
                    return ids.get(0);
                }
                @Override
                protected void succeeded(Long id) {
                    model.setObservationId(id);
                }
            };
            task.execute();

        } else if (e.getType() == TableModelEvent.DELETE) {
            List<Long> ids = new ArrayList<>();
            ids.add(tableModel.getLastDeleted().getObservationId());

            DBTask<Void> task = new DBTask<Void>(this.context) {
                @Override
                protected Void doInBackground() {
                    delegater.removeObservations(ids);
                    return null;
                }
            };
            task.execute();
        }
    }
}
