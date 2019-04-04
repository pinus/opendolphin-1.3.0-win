package open.dolphin.client;

import open.dolphin.delegater.OrcaDelegater;
import open.dolphin.dto.SubjectivesSpec;
import open.dolphin.event.ProxyAction;
import open.dolphin.helper.StringTool;
import open.dolphin.infomodel.DocumentModel;
import open.dolphin.infomodel.IInfoModel;
import open.dolphin.ui.sheet.JSheet;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static open.dolphin.orca.ClaimConst.SubjectivesCodeMap;

/**
 * KartePanePopupMenu. KartePane から独立.
 *
 * @author pns
 */
public class KartePanePopupMenu extends JPopupMenu {
    private KartePane kartePane;
    private ChartMediator mediator;
    private JTextPane textPane;
    private int modifier;

    /**
     * KartePanePopupMenu.
     *
     * @param kartePane target KartePane
     */
    public KartePanePopupMenu(KartePane kartePane, int modifier) {
        this.kartePane = kartePane;
        this.modifier = modifier;
        this.mediator = kartePane.getMediator();
        this.textPane = kartePane.getTextPane();

        buildPopupMenu();
    }

    private void buildPopupMenu() {

        // cut, copy, paste メニューを追加する
        add(mediator.getAction(GUIConst.ACTION_CUT));
        add(mediator.getAction(GUIConst.ACTION_COPY));
        add(mediator.getAction(GUIConst.ACTION_PASTE));

        // テキストカラーメニューを追加する
        if (textPane.isEditable()) {
            ColorChooserComp ccl = new ColorChooserComp();
            ccl.addPropertyChangeListener(ColorChooserComp.SELECTED_COLOR, e -> {
                Color color = (Color) e.getNewValue();
                mediator.colorAction(color);
                setVisible(false);
            });
            JLabel l = new JLabel("  カラー:");
            JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
            p.add(l);
            p.add(ccl);
            add(p);
        }

        if (textPane.isEditable()) {
            if (kartePane.getMyRole().equals(IInfoModel.ROLE_P)) {
                // PPane の場合はStampMenuを追加する
                //contextMenu.addSeparator();
                mediator.addStampMenu(this, kartePane);
            } else {
                // soaPane の場合は TextMenu を追加する
                mediator.addTextMenu(this);
            }
        }

        String selectedText = textPane.getSelectedText();
        if (!StringTool.isEmpty(selectedText)) {
            // 症状詳記メニュー
            JMenu subjMenu = new JMenu("症状詳記送信");
            // subjMenu に詳記メニューを追加
            SubjectivesCodeMap.entrySet().stream()
                    .sorted(Comparator.comparing(Map.Entry::getValue))
                    .forEach(entry -> subjMenu.add(new JMenuItem(new ProxyAction(entry.getKey(),
                            () -> this.sendSubjectivesDetailRecord("01", entry.getValue(), selectedText)))));
            // 詳記メニュー contextMenu に登録
            add(subjMenu);
        }

        // Option キーで詳記削除メニューを出す
        if ((modifier & KeyEvent.ALT_MASK) != 0) {
            JMenu subjMenu = new JMenu("症状詳記削除");
            // 症状詳記情報を取得してメニューを作る
            OrcaDelegater delegater = new OrcaDelegater();
            SubjectivesSpec spec = prepareSubjectivesSpec();
            List<SubjectivesSpec> res = delegater.getSubjectives(spec);
            res.stream()
                    .filter(s -> Objects.nonNull(s.getRecord()))
                    .forEach(s -> subjMenu.add(new JMenuItem(new ProxyAction(s.getRecord(),
                            () -> this.sendSubjectivesDetailRecord("02", s.getCode(), "")))));
            add(subjMenu);
        }
    }

    /**
     * 症状詳記を ORCA に登録／削除する.
     *
     * @param request 01:登録, 02:削除
     * @param code    詳記区分番号
     * @param record  詳記内容
     */
    private void sendSubjectivesDetailRecord(String request, String code, String record) {

        SubjectivesSpec spec = prepareSubjectivesSpec();
        spec.setRequestNumber(request);
        spec.setCode(code);
        spec.setRecord(record);

        String codeText = SubjectivesCodeMap.entrySet().stream()
                .filter(e -> e.getValue().equals(code)).map(Map.Entry::getKey).findAny().orElse("");

        OrcaDelegater delegater = new OrcaDelegater();
        OrcaDelegater.Result result = delegater.sendSubjectives(spec);

        Window w = SwingUtilities.getWindowAncestor(kartePane.getParent().getUI());
        if (JSheet.isAlreadyShown(w)) {
            w.toFront();
        } else {
            String message;
            int messageType;

            if ("01".equals(request)) { // 登録
                if (result.equals(OrcaDelegater.Result.NO_ERROR)) {
                    message = "病状詳記を ORCA に送信しました";
                    messageType = JOptionPane.PLAIN_MESSAGE;
                } else {
                    message = "病状詳記を送信できませんでした";
                    messageType = JOptionPane.ERROR_MESSAGE;
                }
            } else { // 削除
                if (result.equals(OrcaDelegater.Result.NO_ERROR)) {
                    message = "病状詳記を削除しました";
                    messageType = JOptionPane.PLAIN_MESSAGE;
                } else {
                    message = "病状詳記を削除できませんでした";
                    messageType = JOptionPane.ERROR_MESSAGE;
                }
            }
            JSheet.showMessageDialog(w, message + "\n(" + codeText + ")", "", messageType);
        }
    }

    /**
     * SubjectivesSpec (症状詳記 DTO) のひな形を作る.
     *
     * @return SubjectivesSpec のひな形
     */
    private SubjectivesSpec prepareSubjectivesSpec() {
        // parent は KarteEditor or KarteViewer2
        DocumentModel document = kartePane.getParent().getDocument();

        String ptId = kartePane.getParent().getContext().getPatient().getPatientId();
        String insurance = document.getDocInfo().getHealthInsuranceGUID();
        String dept = document.getDocInfo().getDepartment();

        // firstConfirm が null の場合がある (未保存新規カルテ)
        LocalDate firstConfirmDate = Objects.isNull(document.getDocInfo().getFirstConfirmDate())
                ? LocalDate.now()
                : document.getDocInfo().getFirstConfirmDate()
                    .toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        String date = firstConfirmDate.format(DateTimeFormatter.ofPattern("yyyy-MM"));

        SubjectivesSpec spec = new SubjectivesSpec();
        spec.setPerformDate(date);
        spec.setPatientId(ptId);
        spec.setDepartmentCode(dept);
        spec.setInsuranceCombinationNumber(insurance);

        return spec;
    }
}
