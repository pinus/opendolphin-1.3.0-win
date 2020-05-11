package open.dolphin.client;

import open.dolphin.event.ProxyAction;
import open.dolphin.infomodel.ClaimItem;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * StampHolderPopupMenu 用の，外用部位選択パネル.
 *
 * @author pns
 */
public class RegionView extends javax.swing.JDialog {
    private static final long serialVersionUID = 1L;
    /**
     * ORCA 自作マスタの部位部分 001000800 - 001000999
     * getValue() で返す ClaimItem[] はこの順番にソートされる
     */
    private static String[] MASTER = {
            "001000001", "混合",
            "001000801", "部位",
            // 頭頚部
            "001000802", "頭",
            "001000803", "",
            "001000804", "",
            "001000805", "顔",
            "001000806", "額",
            "001000807", "眉毛部（まゆげ）",
            "001000808", "まぶた",
            "001000809", "眼囲",
            "001000810", "目の回り",
            "001000811", "目",
            "001000812", "口囲",
            "001000813", "口角",
            "001000814", "口唇",
            "001000815", "唇",
            "001000816", "顎（あご）",
            "001000817", "耳",
            "001000818", "耳介",
            "001000819", "頬",
            "001000820", "鼻",
            "001000821", "口内炎に貼付",
            "001000822", "口内炎",
            "001000823", "口内",
            "001000824", "",
            "001000825", "項部",
            "001000826", "頚部",
            "001000827", "首",
            "001000828", "",
            "001000829", "",
            // 躯幹
            "001000830", "体",
            "001000831", "肩",
            "001000832", "腋窩（わき）",
            "001000833", "胸",
            "001000834", "背中",
            "001000835", "腹",
            "001000836", "おへそ",
            "001000837", "腰",
            "001000838", "股",
            "001000839", "おしり",
            "001000868", "肛囲",
            "001000869", "陰部",
            "001000870", "亀頭部",
            "001000841", "",
            "001000842", "",
            "001000843", "",
            "001000844", "",
            // 上肢
            "001000845", "四肢",
            "001000855", "手足",
            "001000846", "腕",
            "001000847", "肘",
            "001000848", "手首",
            "001000856", "手",
            "001000857", "手指",
            // 下肢
            "001000849", "下肢",
            "001000850", "下腿部（すね）",
            "001000851", "大腿部（太もも）",
            "001000852", "膝",
            "001000853", "",
            "001000854", "",
            "001000858", "足",
            "001000859", "足裏",
            "001000860", "足趾（足のゆび）",
            "001000861", "踵（かかと）",
            "001000862", "爪",
            // その他
            "001000863", "タコ",
            "001000864", "イボ",
            "001000865", "ウオノメ",
            "001000866", "",
            "001000867", "",
            "001000871", "",
            "001000872", "",
            "001000873", "",
            "001000874", "",
            "001000875", "全身",
            "001000876", "顔にはつけないで下さい",
            "001000877", "",
            "001000878", "",
            "001000879", "",
            "001000880", "脱毛部",
            "001000881", "潰瘍部",
            "001000882", "痒いところ",
            "001000883", "ひどいところ",
            "001000884", "かるいとき",
            "001000885", "かるいところ",
            "001000886", "かゆいとき",
            "001000887", "かゆいところ",
            "001000888", "水虫に",
            "001000889", "傷に",
            "001000890", "化膿止め",
            "001000891", "しっしんに",
            "001000892", "虫さされに",
            "001000893", "おむつかぶれに",
            "001000894", "おできに",
            "001000895", "とびひに",
            "001000896", "カサカサしたところ",
            "001000897", "ジクジクしたところ",
            "001000898", "ニキビに",
            "001000899", "乾燥したところ",
            "001000900", "やけどに",
            "001000901", "熱傷に",
            "001000902", "火傷に",
            "001000903", "赤いところに",
            "001000904", "ハンドクリームとして",
            "001000905", "あせもに",
            "001000906", "湿疹",
            "001000907", "帯状疱疹",
            "001000908", "水疱（みずぶくれ）に",
            "001000909", "痛いところに",
            "001000910", "腫れた（はれた）ところに",
            "001000911", "患部に",
            "001000912", "おむつ交換時に",
            "001000913", "悪化時に",
            "001000914", "落ち着いている時に",
            "001000915", "保湿",

            "001000607", "１番目に外用",
            "001000608", "２番目に外用",

            "001000840", "３０ｇ×２個",
            "001000002", "医師の指示通りに",
    };
    /**
     * code → name の map
     */
    private final HashMap<String, String> nameMap = new HashMap<>();
    /**
     * code → JRadioButton の map
     */
    private final HashMap<String, JRadioButton> buttonMap = new HashMap<>();
    private boolean cancelled = true;
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JRadioButton abdomen;
    private javax.swing.JRadioButton acne;
    private javax.swing.JRadioButton angle;
    private javax.swing.JRadioButton arm;
    private javax.swing.JRadioButton axilla;
    private javax.swing.JRadioButton back;
    private javax.swing.JPanel backgroundPanel;
    private javax.swing.JRadioButton body;
    private javax.swing.JRadioButton buttock;
    private javax.swing.JButton cancel;
    private javax.swing.JRadioButton chest;
    private javax.swing.JButton clear;
    private javax.swing.JRadioButton dryskin;
    private javax.swing.JRadioButton ear;
    private javax.swing.JRadioButton eczema;
    private javax.swing.JRadioButton elbow;
    private javax.swing.JRadioButton extremities;
    private javax.swing.JRadioButton eyebrow;
    private javax.swing.JRadioButton eyelid;
    private javax.swing.JRadioButton face;
    private javax.swing.JRadioButton finger;
    private javax.swing.JRadioButton first;
    private javax.swing.JRadioButton foot;
    private javax.swing.JRadioButton forehead;
    private javax.swing.JRadioButton genital;
    private javax.swing.JRadioButton groin;
    private javax.swing.JRadioButton hand;
    private javax.swing.JRadioButton handAndFoot;
    private javax.swing.JRadioButton head;
    private javax.swing.JRadioButton heel;
    private javax.swing.JRadioButton indication;
    private javax.swing.JButton input;
    private javax.swing.JRadioButton insect;
    private javax.swing.JRadioButton itchy;
    private javax.swing.JRadioButton knee;
    private javax.swing.JRadioButton leg;
    private javax.swing.JRadioButton limb;
    private javax.swing.JRadioButton lip;
    private javax.swing.JRadioButton milliaria;
    private javax.swing.JPanel miscPanel;
    private javax.swing.JRadioButton mix;
    private javax.swing.JRadioButton moisture;
    private javax.swing.JRadioButton nabel;
    private javax.swing.JRadioButton nail;
    private javax.swing.JRadioButton neck;
    private javax.swing.JRadioButton nose;
    private javax.swing.JRadioButton perianal;
    private javax.swing.JRadioButton perioral;
    private javax.swing.JRadioButton periorbital;
    private javax.swing.JRadioButton reddish;
    private javax.swing.JRadioButton second;
    private javax.swing.JRadioButton shoulder;
    private javax.swing.JRadioButton thigh;
    private javax.swing.JRadioButton waist;
    private javax.swing.JRadioButton wart;
    private javax.swing.JRadioButton wound;
    private javax.swing.JRadioButton wrist;
    public RegionView(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        getRootPane().putClientProperty("apple.awt.brushMetalLook", Boolean.TRUE);

        initComponents();
        connect();

        // マッピング作成
        generateMap();
        buttonMap.keySet().forEach(key -> buttonMap.get(key).setFocusable(false));

        input.addActionListener(e -> {
            cancelled = false;
            setVisible(false);
            dispose();
        });
        cancel.addActionListener(e -> {
            cancelled = true;
            setVisible(false);
            dispose();
        });
        clear.addActionListener(e -> {
            clearAllButtons();
            repaint();
        });

        SwingUtilities.invokeLater(() -> {
            cancel.requestFocusInWindow();
            input.setSelected(true);
        });
    }

    private void connect() {
        // short-cut
        InputMap im = getRootPane().getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        ActionMap am = getRootPane().getActionMap();
        // return で入力
        im.put(KeyStroke.getKeyStroke("ENTER"), "input");
        am.put("input", new ProxyAction(input::doClick));

        // ESC でキャンセル
        im.put(KeyStroke.getKeyStroke("ESCAPE"), "cancel");
        am.put("cancel", new ProxyAction(cancel::doClick));

        // backspace でクリア
        im.put(KeyStroke.getKeyStroke("BACK_SPACE"), "clear");
        am.put("clear", new ProxyAction(clear::doClick));

        // default button
        getRootPane().setDefaultButton(input);
    }

    /**
     * This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        miscPanel = new javax.swing.JPanel();
        itchy = new javax.swing.JRadioButton();
        eczema = new javax.swing.JRadioButton();
        moisture = new javax.swing.JRadioButton();
        milliaria = new javax.swing.JRadioButton();
        insect = new javax.swing.JRadioButton();
        wart = new javax.swing.JRadioButton();
        acne = new javax.swing.JRadioButton();
        reddish = new javax.swing.JRadioButton();
        first = new javax.swing.JRadioButton();
        second = new javax.swing.JRadioButton();
        indication = new javax.swing.JRadioButton();
        mix = new javax.swing.JRadioButton();
        dryskin = new javax.swing.JRadioButton();
        wound = new javax.swing.JRadioButton();
        input = new javax.swing.JButton();
        cancel = new javax.swing.JButton();
        clear = new javax.swing.JButton();
        backgroundPanel = new BackgroundPanel();
        head = new javax.swing.JRadioButton();
        ear = new javax.swing.JRadioButton();
        forehead = new javax.swing.JRadioButton();
        face = new javax.swing.JRadioButton();
        nose = new javax.swing.JRadioButton();
        eyebrow = new javax.swing.JRadioButton();
        perioral = new javax.swing.JRadioButton();
        angle = new javax.swing.JRadioButton();
        lip = new javax.swing.JRadioButton();
        eyelid = new javax.swing.JRadioButton();
        periorbital = new javax.swing.JRadioButton();
        neck = new javax.swing.JRadioButton();
        body = new javax.swing.JRadioButton();
        extremities = new javax.swing.JRadioButton();
        waist = new javax.swing.JRadioButton();
        buttock = new javax.swing.JRadioButton();
        shoulder = new javax.swing.JRadioButton();
        back = new javax.swing.JRadioButton();
        perianal = new javax.swing.JRadioButton();
        nabel = new javax.swing.JRadioButton();
        abdomen = new javax.swing.JRadioButton();
        chest = new javax.swing.JRadioButton();
        genital = new javax.swing.JRadioButton();
        axilla = new javax.swing.JRadioButton();
        groin = new javax.swing.JRadioButton();
        handAndFoot = new javax.swing.JRadioButton();
        hand = new javax.swing.JRadioButton();
        foot = new javax.swing.JRadioButton();
        limb = new javax.swing.JRadioButton();
        knee = new javax.swing.JRadioButton();
        elbow = new javax.swing.JRadioButton();
        wrist = new javax.swing.JRadioButton();
        finger = new javax.swing.JRadioButton();
        nail = new javax.swing.JRadioButton();
        leg = new javax.swing.JRadioButton();
        arm = new javax.swing.JRadioButton();
        thigh = new javax.swing.JRadioButton();
        heel = new javax.swing.JRadioButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("部位入力パネル");
        setResizable(false);

        miscPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(" その他 "));

        itchy.setText("痒いところ");

        eczema.setText("湿疹");

        moisture.setText("保湿");

        milliaria.setText("あせも");

        insect.setText("虫さされ");

        wart.setText("イボ");

        acne.setText("ニキビ");

        reddish.setText("赤いところ");

        first.setText("１番目に外用");

        second.setText("２番目に外用");

        indication.setText("医師の指示通り");

        mix.setText("混合");

        dryskin.setText("乾燥したところ");

        wound.setText("傷に");

        javax.swing.GroupLayout miscPanelLayout = new javax.swing.GroupLayout(miscPanel);
        miscPanel.setLayout(miscPanelLayout);
        miscPanelLayout.setHorizontalGroup(
                miscPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(itchy)
                        .addComponent(reddish)
                        .addComponent(eczema)
                        .addComponent(dryskin)
                        .addComponent(moisture)
                        .addComponent(acne)
                        .addComponent(milliaria)
                        .addComponent(wound)
                        .addComponent(wart)
                        .addComponent(insect)
                        .addComponent(first)
                        .addComponent(second)
                        .addComponent(indication)
                        .addComponent(mix)
        );
        miscPanelLayout.setVerticalGroup(
                miscPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(miscPanelLayout.createSequentialGroup()
                                .addComponent(itchy)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(reddish)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(eczema)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(dryskin)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(moisture)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(acne)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(milliaria)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(wound)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(wart, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(insect)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(first)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(second)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(indication)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(mix))
        );

        input.setText("入力");

        cancel.setText("キャンセル");

        clear.setText("全てクリア");

        backgroundPanel.setMaximumSize(new java.awt.Dimension(384, 420));
        backgroundPanel.setMinimumSize(new java.awt.Dimension(384, 420));
        backgroundPanel.setPreferredSize(new java.awt.Dimension(384, 420));
        backgroundPanel.setSize(new java.awt.Dimension(384, 420));
        backgroundPanel.setLayout(null);

        head.setText("頭");
        head.setBorder(javax.swing.BorderFactory.createEtchedBorder(java.awt.Color.white, java.awt.Color.lightGray));
        head.setBorderPainted(true);
        head.setOpaque(true);
        backgroundPanel.add(head);
        head.setBounds(280, 30, 39, 22);

        ear.setText("耳");
        ear.setBorder(javax.swing.BorderFactory.createEtchedBorder(java.awt.Color.white, java.awt.Color.lightGray));
        ear.setBorderPainted(true);
        ear.setOpaque(true);
        backgroundPanel.add(ear);
        ear.setBounds(220, 50, 39, 22);

        forehead.setText("前額部");
        forehead.setBorder(javax.swing.BorderFactory.createEtchedBorder(java.awt.Color.white, java.awt.Color.lightGray));
        forehead.setBorderPainted(true);
        forehead.setOpaque(true);
        backgroundPanel.add(forehead);
        forehead.setBounds(90, 20, 65, 22);

        face.setText("顔");
        face.setBorder(javax.swing.BorderFactory.createEtchedBorder(java.awt.Color.white, java.awt.Color.lightGray));
        face.setBorderPainted(true);
        face.setOpaque(true);
        backgroundPanel.add(face);
        face.setBounds(10, 0, 39, 22);

        nose.setText("鼻");
        nose.setBorder(javax.swing.BorderFactory.createEtchedBorder(java.awt.Color.white, java.awt.Color.lightGray));
        nose.setBorderPainted(true);
        nose.setOpaque(true);
        backgroundPanel.add(nose);
        nose.setBounds(90, 60, 39, 22);

        eyebrow.setText("眉毛部");
        eyebrow.setBorder(javax.swing.BorderFactory.createEtchedBorder(java.awt.Color.white, java.awt.Color.lightGray));
        eyebrow.setBorderPainted(true);
        eyebrow.setOpaque(true);
        backgroundPanel.add(eyebrow);
        eyebrow.setBounds(10, 20, 65, 22);

        perioral.setText("口囲");
        perioral.setBorder(javax.swing.BorderFactory.createEtchedBorder(java.awt.Color.white, java.awt.Color.lightGray));
        perioral.setBorderPainted(true);
        perioral.setOpaque(true);
        backgroundPanel.add(perioral);
        perioral.setBounds(130, 60, 52, 22);

        angle.setText("口角");
        angle.setBorder(javax.swing.BorderFactory.createEtchedBorder(java.awt.Color.white, java.awt.Color.lightGray));
        angle.setBorderPainted(true);
        angle.setOpaque(true);
        backgroundPanel.add(angle);
        angle.setBounds(130, 80, 52, 22);

        lip.setText("口唇");
        lip.setBorder(javax.swing.BorderFactory.createEtchedBorder(java.awt.Color.white, java.awt.Color.lightGray));
        lip.setBorderPainted(true);
        lip.setOpaque(true);
        backgroundPanel.add(lip);
        lip.setBounds(130, 100, 52, 22);

        eyelid.setText("まぶた");
        eyelid.setBorder(javax.swing.BorderFactory.createEtchedBorder(java.awt.Color.white, java.awt.Color.lightGray));
        eyelid.setBorderPainted(true);
        eyelid.setOpaque(true);
        backgroundPanel.add(eyelid);
        eyelid.setBounds(10, 40, 65, 22);

        periorbital.setText("目の周り");
        periorbital.setBorder(javax.swing.BorderFactory.createEtchedBorder(java.awt.Color.white, java.awt.Color.lightGray));
        periorbital.setBorderPainted(true);
        periorbital.setOpaque(true);
        backgroundPanel.add(periorbital);
        periorbital.setBounds(10, 60, 78, 22);

        neck.setText("頚部");
        neck.setBorder(javax.swing.BorderFactory.createEtchedBorder(java.awt.Color.white, java.awt.Color.lightGray));
        neck.setBorderPainted(true);
        neck.setOpaque(true);
        backgroundPanel.add(neck);
        neck.setBounds(280, 80, 52, 22);

        body.setText("体");
        body.setBorder(javax.swing.BorderFactory.createEtchedBorder(java.awt.Color.white, java.awt.Color.lightGray));
        body.setBorderPainted(true);
        body.setOpaque(true);
        backgroundPanel.add(body);
        body.setBounds(10, 120, 58, 22);

        extremities.setText("四肢");
        extremities.setBorder(javax.swing.BorderFactory.createEtchedBorder(java.awt.Color.white, java.awt.Color.lightGray));
        extremities.setBorderPainted(true);
        extremities.setOpaque(true);
        backgroundPanel.add(extremities);
        extremities.setBounds(10, 330, 52, 22);

        waist.setText("腰部");
        waist.setBorder(javax.swing.BorderFactory.createEtchedBorder(java.awt.Color.white, java.awt.Color.lightGray));
        waist.setBorderPainted(true);
        waist.setOpaque(true);
        backgroundPanel.add(waist);
        waist.setBounds(280, 200, 52, 22);

        buttock.setText("おしり");
        buttock.setBorder(javax.swing.BorderFactory.createEtchedBorder(java.awt.Color.white, java.awt.Color.lightGray));
        buttock.setBorderPainted(true);
        buttock.setOpaque(true);
        backgroundPanel.add(buttock);
        buttock.setBounds(280, 230, 65, 22);

        shoulder.setText("肩部");
        shoulder.setBorder(javax.swing.BorderFactory.createEtchedBorder(java.awt.Color.white, java.awt.Color.lightGray));
        shoulder.setBorderPainted(true);
        shoulder.setOpaque(true);
        backgroundPanel.add(shoulder);
        shoulder.setBounds(230, 110, 52, 22);

        back.setText("背中");
        back.setBorder(javax.swing.BorderFactory.createEtchedBorder(java.awt.Color.white, java.awt.Color.lightGray));
        back.setBorderPainted(true);
        back.setOpaque(true);
        backgroundPanel.add(back);
        back.setBounds(280, 150, 52, 22);

        perianal.setText("肛囲");
        perianal.setBorder(javax.swing.BorderFactory.createEtchedBorder(java.awt.Color.white, java.awt.Color.lightGray));
        perianal.setBorderPainted(true);
        perianal.setOpaque(true);
        backgroundPanel.add(perianal);
        perianal.setBounds(280, 250, 52, 22);

        nabel.setText("へそ");
        nabel.setBorder(javax.swing.BorderFactory.createEtchedBorder(java.awt.Color.white, java.awt.Color.lightGray));
        nabel.setBorderPainted(true);
        nabel.setOpaque(true);
        backgroundPanel.add(nabel);
        nabel.setBounds(90, 210, 52, 22);

        abdomen.setText("腹部");
        abdomen.setBorder(javax.swing.BorderFactory.createEtchedBorder(java.awt.Color.white, java.awt.Color.lightGray));
        abdomen.setBorderPainted(true);
        abdomen.setOpaque(true);
        backgroundPanel.add(abdomen);
        abdomen.setBounds(90, 190, 52, 22);

        chest.setText("胸部");
        chest.setBorder(javax.swing.BorderFactory.createEtchedBorder(java.awt.Color.white, java.awt.Color.lightGray));
        chest.setBorderPainted(true);
        chest.setOpaque(true);
        backgroundPanel.add(chest);
        chest.setBounds(90, 150, 52, 22);

        genital.setText("陰部");
        genital.setBorder(javax.swing.BorderFactory.createEtchedBorder(java.awt.Color.white, java.awt.Color.lightGray));
        genital.setBorderPainted(true);
        genital.setOpaque(true);
        backgroundPanel.add(genital);
        genital.setBounds(90, 260, 52, 22);

        axilla.setText("腋窩");
        axilla.setBorder(javax.swing.BorderFactory.createEtchedBorder(java.awt.Color.white, java.awt.Color.lightGray));
        axilla.setBorderPainted(true);
        axilla.setOpaque(true);
        backgroundPanel.add(axilla);
        axilla.setBounds(130, 130, 52, 22);

        groin.setText("股部");
        groin.setBorder(javax.swing.BorderFactory.createEtchedBorder(java.awt.Color.white, java.awt.Color.lightGray));
        groin.setBorderPainted(true);
        groin.setOpaque(true);
        backgroundPanel.add(groin);
        groin.setBounds(70, 240, 52, 22);

        handAndFoot.setText("手足");
        handAndFoot.setBorder(javax.swing.BorderFactory.createEtchedBorder(java.awt.Color.white, java.awt.Color.lightGray));
        handAndFoot.setBorderPainted(true);
        handAndFoot.setOpaque(true);
        backgroundPanel.add(handAndFoot);
        handAndFoot.setBounds(180, 330, 52, 22);

        hand.setText("手");
        hand.setBorder(javax.swing.BorderFactory.createEtchedBorder(java.awt.Color.white, java.awt.Color.lightGray));
        hand.setBorderPainted(true);
        hand.setOpaque(true);
        backgroundPanel.add(hand);
        hand.setBounds(200, 230, 39, 22);

        foot.setText("足");
        foot.setBorder(javax.swing.BorderFactory.createEtchedBorder(java.awt.Color.white, java.awt.Color.lightGray));
        foot.setBorderPainted(true);
        foot.setOpaque(true);
        backgroundPanel.add(foot);
        foot.setBounds(110, 390, 39, 22);

        limb.setText("下肢");
        limb.setBorder(javax.swing.BorderFactory.createEtchedBorder(java.awt.Color.white, java.awt.Color.lightGray));
        limb.setBorderPainted(true);
        limb.setOpaque(true);
        backgroundPanel.add(limb);
        limb.setBounds(10, 300, 52, 22);

        knee.setText("膝");
        knee.setBorder(javax.swing.BorderFactory.createEtchedBorder(java.awt.Color.white, java.awt.Color.lightGray));
        knee.setBorderPainted(true);
        knee.setOpaque(true);
        backgroundPanel.add(knee);
        knee.setBounds(110, 330, 39, 22);

        elbow.setText("肘");
        elbow.setBorder(javax.swing.BorderFactory.createEtchedBorder(java.awt.Color.white, java.awt.Color.lightGray));
        elbow.setBorderPainted(true);
        elbow.setOpaque(true);
        backgroundPanel.add(elbow);
        elbow.setBounds(210, 170, 39, 22);

        wrist.setText("手首");
        wrist.setBorder(javax.swing.BorderFactory.createEtchedBorder(java.awt.Color.white, java.awt.Color.lightGray));
        wrist.setBorderPainted(true);
        wrist.setOpaque(true);
        backgroundPanel.add(wrist);
        wrist.setBounds(200, 210, 52, 22);

        finger.setText("手指");
        finger.setBorder(javax.swing.BorderFactory.createEtchedBorder(java.awt.Color.white, java.awt.Color.lightGray));
        finger.setBorderPainted(true);
        finger.setOpaque(true);
        backgroundPanel.add(finger);
        finger.setBounds(200, 250, 52, 22);

        nail.setText("爪");
        nail.setBorder(javax.swing.BorderFactory.createEtchedBorder(java.awt.Color.white, java.awt.Color.lightGray));
        nail.setBorderPainted(true);
        nail.setOpaque(true);
        backgroundPanel.add(nail);
        nail.setBounds(200, 280, 39, 22);

        leg.setText("下腿");
        leg.setBorder(javax.swing.BorderFactory.createEtchedBorder(java.awt.Color.white, java.awt.Color.lightGray));
        leg.setBorderPainted(true);
        leg.setOpaque(true);
        backgroundPanel.add(leg);
        leg.setBounds(110, 360, 52, 22);

        arm.setText("腕");
        arm.setBorder(javax.swing.BorderFactory.createEtchedBorder(java.awt.Color.white, java.awt.Color.lightGray));
        arm.setBorderPainted(true);
        arm.setOpaque(true);
        backgroundPanel.add(arm);
        arm.setBounds(30, 170, 39, 22);

        thigh.setText("大腿");
        thigh.setBorder(javax.swing.BorderFactory.createEtchedBorder(java.awt.Color.white, java.awt.Color.lightGray));
        thigh.setBorderPainted(true);
        thigh.setOpaque(true);
        backgroundPanel.add(thigh);
        thigh.setBounds(110, 290, 52, 22);

        heel.setText("踵");
        heel.setBorder(javax.swing.BorderFactory.createEtchedBorder(java.awt.Color.white, java.awt.Color.lightGray));
        heel.setBorderPainted(true);
        heel.setOpaque(true);
        backgroundPanel.add(heel);
        heel.setBounds(250, 390, 39, 22);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addComponent(backgroundPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(miscPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(0, 0, Short.MAX_VALUE))
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(clear)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(cancel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(input, javax.swing.GroupLayout.PREFERRED_SIZE, 102, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addContainerGap())
        );
        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(backgroundPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(miscPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(clear)
                                        .addComponent(cancel)
                                        .addComponent(input)))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void headActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_headActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_headActionPerformed

    private void chestActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chestActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_chestActionPerformed
    // End of variables declaration//GEN-END:variables

    public boolean isCancelled() {
        return cancelled;
    }

    /**
     * ボタンの状態に応じて ClaimItem を作って返す
     *
     * @return List of ClaimItem
     */
    public List<ClaimItem> getValue() {
        List<ClaimItem> list = new ArrayList<>();

        for (int i = 0; i < MASTER.length; i += 2) {
            String key = MASTER[i];
            JRadioButton button = buttonMap.get(key);

            if (button != null && button.isSelected()) {
                // 部位をリストの最後に加える
                ClaimItem c = new ClaimItem();
                c.setClassCode("2");
                c.setClassCodeSystem("Claim003");
                c.setCode(key);
                c.setName(nameMap.get(key));
                c.setNumber(".");
                c.setNumberCode("10");
                c.setNumberCodeSystem("Claim004");
                list.add(c);
            }
        }
        return list;
    }

    /**
     * ClaimItem にボタンに対応するコードが入っていれば，ボタンをセットする
     *
     * @param items ClaimItem[]
     */
    public void setValue(ClaimItem[] items) {
        clearAllButtons();

        for (ClaimItem c : items) {
            JRadioButton b = buttonMap.get(c.getCode());
            if (b != null) {
                b.setSelected(true);
            }
        }
    }

    /**
     * すべてのボタンをクリア
     */
    private void clearAllButtons() {
        buttonMap.keySet().forEach((key) -> buttonMap.get(key).setSelected(false));
    }

    /**
     * code → 部位名　および　code → ラジオボタン　マッピング作成
     */
    private void generateMap() {
        // code → 部位
        for (int i = 0; i < MASTER.length; i += 2) {
            nameMap.put(MASTER[i], MASTER[i + 1]);
        }
        // code → JRadioButton
        buttonMap.put("001000835", abdomen);
        buttonMap.put("001000898", acne);
        buttonMap.put("001000813", angle);
        buttonMap.put("001000846", arm);
        buttonMap.put("001000832", axilla);
        buttonMap.put("001000834", back);
        buttonMap.put("001000830", body);
        buttonMap.put("001000839", buttock);
        buttonMap.put("001000833", chest);
        buttonMap.put("001000817", ear);
        buttonMap.put("001000906", eczema);
        buttonMap.put("001000847", elbow);
        buttonMap.put("001000845", extremities);
        buttonMap.put("001000807", eyebrow);
        buttonMap.put("001000808", eyelid);
        buttonMap.put("001000805", face);
        buttonMap.put("001000857", finger);
        buttonMap.put("001000858", foot);
        buttonMap.put("001000806", forehead);
        buttonMap.put("001000869", genital);
        buttonMap.put("001000838", groin);
        buttonMap.put("001000856", hand);
        buttonMap.put("001000855", handAndFoot);
        buttonMap.put("001000802", head);
        buttonMap.put("001000861", heel);
        buttonMap.put("001000892", insect);
        buttonMap.put("001000882", itchy);
        buttonMap.put("001000852", knee);
        buttonMap.put("001000850", leg);
        buttonMap.put("001000849", limb);
        buttonMap.put("001000814", lip);
        buttonMap.put("001000905", milliaria);
        buttonMap.put("001000915", moisture);
        buttonMap.put("001000836", nabel);
        buttonMap.put("001000862", nail);
        buttonMap.put("001000827", neck);
        buttonMap.put("001000820", nose);
        buttonMap.put("001000868", perianal);
        buttonMap.put("001000812", perioral);
        buttonMap.put("001000810", periorbital);
        buttonMap.put("001000903", reddish);
        buttonMap.put("001000831", shoulder);
        buttonMap.put("001000851", thigh);
        buttonMap.put("001000837", waist);
        buttonMap.put("001000864", wart);
        buttonMap.put("001000848", wrist);

        buttonMap.put("001000607", first);
        buttonMap.put("001000608", second);
        buttonMap.put("001000001", mix);
        buttonMap.put("001000002", indication);
        buttonMap.put("001000899", dryskin);
        buttonMap.put("001000889", wound);
    }

    /**
     * バックグランド付きパネル
     */
    private class BackgroundPanel extends JPanel {
        private static final long serialVersionUID = 1L;
        private final int width, height;


        public BackgroundPanel() {
            width = GUIConst.IMAGE_BODY.getWidth();
            height = GUIConst.IMAGE_BODY.getHeight();
            setOpaque(false);
        }

        @Override
        public void paint(Graphics g) {
            g.drawImage(GUIConst.IMAGE_BODY, 0, 0, width, height, null);
            super.paint(g);
        }
    }

    public static void main(String[] args) {
        // ClientContext を生成する
        ClientContextStub stub = new ClientContextStub();
        ClientContext.setClientContextStub(stub);
        java.awt.EventQueue.invokeLater(() -> {
            RegionView dialog = new RegionView(new JFrame(), true);
            dialog.addWindowListener(new java.awt.event.WindowAdapter() {

                public void windowClosing(java.awt.event.WindowEvent e) {
                    System.exit(0);
                }
            });
            dialog.setVisible(true);
        });
    }

}
