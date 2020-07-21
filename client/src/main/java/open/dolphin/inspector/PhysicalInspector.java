package open.dolphin.inspector;

import open.dolphin.client.Chart;
import open.dolphin.client.ChartImpl;
import open.dolphin.client.GUIConst;
import open.dolphin.delegater.DocumentDelegater;
import open.dolphin.event.ProxyAction;
import open.dolphin.helper.DBTask;
import open.dolphin.helper.PNSTriple;
import open.dolphin.infomodel.IInfoModel;
import open.dolphin.infomodel.ObservationModel;
import open.dolphin.infomodel.PhysicalModel;
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
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.*;

/**
 * 身長体重インスペクタクラス.
 *
 * @author Kazushi Minagawa, Digital Globe, Inc.
 * @author pns
 */
public class PhysicalInspector implements IInspector, TableModelListener {
    public static final InspectorCategory CATEGORY = InspectorCategory.身長体重;
    private Logger logger = LoggerFactory.getLogger(PhysicalInspector.class);

    // Chart
    private final ChartImpl context;
    // TableModel
    private UndoableObjectReflectTableModel<PhysicalModel> tableModel;
    // コンテナパネル
    private JPanel view;
    private JTable table;

    /**
     * PhysicalInspectorオブジェクトを生成する.
     *
     * @param parent PatientInspector
     */
    public PhysicalInspector(PatientInspector parent) {
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

        table = new JTable();
        table.putClientProperty("Quaqua.Table.style", "striped");
        PNSScrollPane scrollPane = new PNSScrollPane();
        scrollPane.putClientProperty("JComponent.sizeVariant", "small");
        scrollPane.setViewportView(table);

        view.add(scrollPane);

        // 身長体重テーブルを生成する
        List<PNSTriple<String, Class<?>, String>> reflectList = Arrays.asList(
                new PNSTriple<>(" 身長", String.class, "getHeight"),
                new PNSTriple<>(" 体重", String.class, "getWeight"),
                new PNSTriple<>(" BMI", String.class, "getBmi"),
                new PNSTriple<>(" 測定日", String.class, "getIdentifiedDate")
        );

        tableModel = new UndoableObjectReflectTableModel<>(reflectList);
        tableModel.addTableModelListener(this);
        table.setModel(tableModel);
        table.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // レンダラを設定する
        table.setDefaultRenderer(Object.class, new IndentTableCellRenderer(IndentTableCellRenderer.NARROW));
        table.getColumnModel().getColumn(2).setCellRenderer(new BMIRenderer());

        // 列幅を調整する カット&トライ
        int[] cellWidth = new int[]{50, 50, 50, 110};
        for (int i = 0; i < cellWidth.length; i++) {
            TableColumn column = table.getColumnModel().getColumn(i);
            column.setPreferredWidth(cellWidth[i]);
        }

        // 右クリックによる追加削除のメニューを登録する
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    PhysicalEditor.show(PhysicalInspector.this);
                }
            }

            private void mabeShowPopup(MouseEvent e) {
                // isReadOnly対応
                if (context.isReadOnly() || !e.isPopupTrigger()) {
                    return;
                }

                JPopupMenu pop = new JPopupMenu();
                JMenuItem item = new JMenuItem("追加");
                item.setIcon(GUIConst.ICON_LIST_ADD_16);
                pop.add(item);
                item.addActionListener(ae -> PhysicalEditor.show(PhysicalInspector.this));

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

    public Chart getContext() {
        return context;
    }

    /**
     * レイアウトパネルを返す.
     *
     * @return レイアウトパネル
     */
    @Override
    public JPanel getPanel() {
        return view;
    }

    @Override
    public String getName() {
        return CATEGORY.name();
    }

    @Override
    public String getTitle() {
        return CATEGORY.title();
    }

    /**
     * 選択された PhysicalModel を返す.
     *
     * @return PhysicalModel selected or null
     */
    public PhysicalModel getSelectedModel() {
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
            int row = ascending ? cnt - 1 : 0;
            Rectangle r = table.getCellRect(row, row, true);
            table.scrollRectToVisible(r);
        }
    }

    /**
     * 身長体重データを表示する.
     */
    @Override
    public void update() {
        List<PhysicalModel> list = context.getKarte().getPhysicalEntry();
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
     * 身長体重データを追加する. PhysicalEditor から呼ばれる.
     *
     * @param model PhysicalModel
     */
    public void add(final PhysicalModel model) {
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
     * テーブルで選択した身長体重データを削除する.
     *
     * @param row 削除行
     */
    public void delete(final int row) { tableModel.undoableDeleteRow(row); }

    /**
     * TableModel 操作後に呼ばれる. 操作内容をデータベースに記録する.
     *
     * @param e TableModelEvent
     */
    @Override
    public void tableChanged(TableModelEvent e) {

        DocumentDelegater delegater = new DocumentDelegater();

        if (e.getType() == TableModelEvent.INSERT) {
            PhysicalModel model = tableModel.getObject(e.getFirstRow());

            // GUI の同定日をTimeStampに変更する
            Date confirmed = ModelUtils.getDateTimeAsObject(model.getIdentifiedDate() + "T00:00:00");
            Date recorded = new Date();

            // 身長体重の両方が含まれていると, 身長 → 体重の順に分けてデータベース保存される.
            final List<ObservationModel> observations = new ArrayList<>(2);
            if (model.getHeight() != null) {
                ObservationModel observation = new ObservationModel();
                observation.setKarte(context.getKarte());
                observation.setCreator(Project.getUserModel());
                observation.setObservation(IInfoModel.OBSERVATION_PHYSICAL_EXAM);
                observation.setPhenomenon(IInfoModel.PHENOMENON_BODY_HEIGHT);
                observation.setValue(model.getHeight());
                observation.setUnit(IInfoModel.UNIT_BODY_HEIGHT);
                observation.setConfirmed(confirmed);        // 確定（同定日）
                observation.setStarted(confirmed);          // 適合開始日
                observation.setRecorded(recorded);          // 記録日
                observation.setStatus(IInfoModel.STATUS_FINAL);
                //observation.setMemo(model.getMemo());
                observations.add(observation);
            }

            if (model.getWeight() != null) {

                ObservationModel observation = new ObservationModel();
                observation.setKarte(context.getKarte());
                observation.setCreator(Project.getUserModel());
                observation.setObservation(IInfoModel.OBSERVATION_PHYSICAL_EXAM);
                observation.setPhenomenon(IInfoModel.PHENOMENON_BODY_WEIGHT);
                observation.setValue(model.getWeight());
                observation.setUnit(IInfoModel.UNIT_BODY_WEIGHT);
                observation.setConfirmed(confirmed);        // 確定（同定日）
                observation.setStarted(confirmed);          // 適合開始日
                observation.setRecorded(recorded);          // 記録日
                observation.setStatus(IInfoModel.STATUS_FINAL);
                //observation.setMemo(model.getMemo());
                observations.add(observation);
            }

            DBTask<List<Long>> task = new DBTask<List<Long>>(context) {
                @Override
                protected List<Long> doInBackground() {
                    List<Long> ids = delegater.addObservations(observations);
                    return ids;
                }

                @Override
                protected void succeeded(List<Long> result) {
                    if (model.getHeight() != null && model.getWeight() != null) {
                        model.setHeightId(result.get(0));
                        model.setWeightId(result.get(1));
                    } else if (model.getHeight() != null) {
                        model.setHeightId(result.get(0));
                    } else {
                        model.setWeightId(result.get(0));
                    }
                }
            };
            task.execute();

        } else if (e.getType() == TableModelEvent.DELETE) {
            List<Long> ids = new ArrayList<>();

            PhysicalModel lastDeleted = tableModel.getLastDeleted();
            if (lastDeleted.getHeight() != null) { ids.add(lastDeleted.getHeightId()); }
            if (lastDeleted.getWeight() != null) { ids.add(lastDeleted.getWeightId()); }

            DBTask<Void> task = new DBTask<Void>(context) {
                @Override
                protected Void doInBackground() {
                    delegater.removeObservations(ids);
                    return null;
                }
            };
            task.execute();
        }
    }

    /**
     * BMI値 を表示するレンダラクラス.
     */
    private class BMIRenderer extends DefaultTableCellRenderer {
        private static final long serialVersionUID = 1L;

        public BMIRenderer() {
            super();
        }

        @Override
        public Component getTableCellRendererComponent(JTable table,
                                                       Object value,
                                                       boolean isSelected,
                                                       boolean isFocused,
                                                       int row, int col) {
            Component c = super.getTableCellRendererComponent(table,
                    value,
                    isSelected,
                    isFocused, row, col);

            if (isSelected) {
                if (!table.isFocusOwner()) {
                    c.setForeground(table.getForeground());
                    c.setBackground((Color) table.getClientProperty("JTable.backgroundOffFocus"));
                }
            } else {
                c.setBackground(null);
            }

            PhysicalModel h = tableModel.getObject(row);

            if (h != null && h.getBmi() != null && h.getBmi().compareTo("25") > 0) {
                c.setBackground(Color.RED);
            }

            ((JLabel) c).setText(value == null ? "" : (String) value);

            if (h != null && h.getStandardWeight() != null) {
                setToolTipText("標準体重 = " + h.getStandardWeight());
            }

            return c;
        }
    }
}
