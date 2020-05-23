package open.dolphin.client;

import open.dolphin.delegater.BusinessDelegater;
import open.dolphin.delegater.UserDelegater;
import open.dolphin.event.ProxyDocumentListener;
import open.dolphin.helper.ComponentBoundsManager;
import open.dolphin.helper.HashUtil;
import open.dolphin.helper.PNSTriple;
import open.dolphin.helper.Task;
import open.dolphin.infomodel.*;
import open.dolphin.project.Project;
import open.dolphin.ui.IndentTableCellRenderer;
import open.dolphin.ui.ObjectReflectTableModel;
import open.dolphin.ui.PNSScrollPane;
import open.dolphin.ui.PNSTabbedPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;

/**
 * AddUserPlugin.
 *
 * @author Kazushi Minagawa, Digital Globe, Inc.
 */
public class AddUser extends AbstractMainTool {

    private static final String TITLE = "ユーザ管理";
    private static final String FACILITY_INFO = "施設情報";
    private static final String ADD_USER = "ユーザ登録";
    private static final String LIST_USER = "ユーザリスト";
    private static final String FACILITY_SUCCESS_MSG = "施設情報を更新しました。";
    private static final String ADD_USER_SUCCESS_MSG = "ユーザを登録しました。";
    private static final String DELETE_USER_SUCCESS_MSG = "ユーザを削除しました。";
    private static final String DELETE_OK_USER = "選択したユーザを削除します";
    private static final Point DEFAULT_LOC = new Point(0, 0);
    private static final Dimension DEFAULT_SIZE = new Dimension(600, 370);
    private final Logger logger;
    private JFrame frame;

    public AddUser() {
        setName(TITLE);
        logger = LoggerFactory.getLogger(AddUser.class);
    }

    @Override
    public void start() {

        String title = ClientContext.getFrameTitle(getName());
        frame = new JFrame(title);
        frame.getRootPane().putClientProperty("apple.awt.brushMetalLook", Boolean.TRUE);
        frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                stop();
            }
        });

        // Component を生成する
        AddUserPanel ap = new AddUserPanel();
        FacilityInfoPanel fp = new FacilityInfoPanel();
        UserListPanel mp = new UserListPanel();
        PNSTabbedPane tabbedPane = new PNSTabbedPane();
        tabbedPane.addTab(FACILITY_INFO, fp);
        tabbedPane.addTab(ADD_USER, ap);
        tabbedPane.addTab(LIST_USER, mp);
        fp.get();

        // Frame に加える
        frame.getContentPane().add(tabbedPane, BorderLayout.CENTER);
        frame.pack();

        // putCenter で強制的に中心に置かれる
        ComponentBoundsManager cm = new ComponentBoundsManager(frame, DEFAULT_LOC, DEFAULT_SIZE, this);
        cm.putCenter();

        frame.setVisible(true);
    }

    @Override
    public void stop() {
        frame.setVisible(false);
        frame.dispose();
    }

    public void toFront() {
        if (frame != null) {
            frame.toFront();
        }
    }

    private void constrain(JPanel container, Component cmp, int x, int y,
                           int width, int height, int fill, int anchor) {

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = x;
        c.gridy = y;
        c.gridwidth = width;
        c.gridheight = height;
        c.fill = fill;
        c.anchor = anchor;
        c.insets = new Insets(0, 0, 5, 7);
        ((GridBagLayout) container.getLayout()).setConstraints(cmp, c);
        container.add(cmp);
    }

    /**
     * タイムアウト警告表示を行う.
     */
    private void wraningTimeOut() {
        String message = ClientContext.getString("task.timeoutMsg1") + "\n" +
            ClientContext.getString("task.timeoutMsg1");
        JOptionPane.showMessageDialog(frame,
            message,
            ClientContext.getFrameTitle(getName()),
            JOptionPane.WARNING_MESSAGE);
    }

    /**
     * OSがmacかどうかを返す.
     *
     * @return mac の時 true
     */
    private boolean isMac() {
        String os = System.getProperty("os.name").toLowerCase();
        return os.startsWith("mac");
    }

    /**
     * 施設（医療機関）情報を変更するクラス.
     */
    private class FacilityInfoPanel extends JPanel {
        private static final long serialVersionUID = 1L;

        // 施設情報フィールド
        private JTextField facilityName;
        private JTextField zipField1;
        private JTextField zipField2;
        private JTextField addressField;
        private JTextField areaField;
        private JTextField cityField;
        private JTextField numberField;
        private JTextField urlField;

        // 更新等のボタン
        private JButton updateBtn;
        private JButton clearBtn;
        private JButton closeBtn;
        private boolean hasInitialized;

        public FacilityInfoPanel() {
            initComponents();
        }

        private void initComponents() {
            // GUI生成
            ProxyDocumentListener dl = e -> checkButton();

            facilityName = GUIFactory.createTextField(30, null, null, dl);
            zipField1 = GUIFactory.createTextField(3, null, null, dl);
            zipField2 = GUIFactory.createTextField(3, null, null, dl);
            addressField = GUIFactory.createTextField(30, null, null, dl);
            areaField = GUIFactory.createTextField(3, null, null, dl);
            cityField = GUIFactory.createTextField(3, null, null, dl);
            numberField = GUIFactory.createTextField(3, null, null, dl);
            urlField = GUIFactory.createTextField(30, null, null, dl);

            updateBtn = new JButton("更新");
            updateBtn.setEnabled(false);
            updateBtn.addActionListener(e -> update());

            clearBtn = new JButton("戻す");
            clearBtn.setEnabled(false);
            clearBtn.addActionListener(e -> get());

            closeBtn = new JButton("閉じる");
            closeBtn.addActionListener(e -> stop());

            // レイアウト
            JPanel content = new JPanel(new GridBagLayout());

            int x = 0;
            int y = 0;
            JLabel label = new JLabel("医療機関名:", SwingConstants.RIGHT);
            constrain(content, label, x, y, 1, 1, GridBagConstraints.NONE, GridBagConstraints.EAST);
            constrain(content, facilityName, x + 1, y, 1, 1, GridBagConstraints.NONE, GridBagConstraints.WEST);

            x = 0;
            y += 1;
            label = new JLabel("郵便番号:", SwingConstants.RIGHT);
            constrain(content, label, x, y, 1, 1, GridBagConstraints.NONE, GridBagConstraints.EAST);
            constrain(content, GUIFactory.createZipCodePanel(zipField1, zipField2), x + 1, y, 1, 1, GridBagConstraints.NONE, GridBagConstraints.WEST);

            x = 0;
            y += 1;
            label = new JLabel("住  所:", SwingConstants.RIGHT);
            constrain(content, label, x, y, 1, 1, GridBagConstraints.NONE, GridBagConstraints.EAST);
            constrain(content, addressField, x + 1, y, 1, 1, GridBagConstraints.NONE, GridBagConstraints.WEST);

            x = 0;
            y += 1;
            label = new JLabel("電話番号:", SwingConstants.RIGHT);
            constrain(content, label, x, y, 1, 1, GridBagConstraints.NONE, GridBagConstraints.EAST);
            constrain(content, GUIFactory.createPhonePanel(areaField, cityField, numberField), x + 1, y, 1, 1, GridBagConstraints.NONE, GridBagConstraints.WEST);

            x = 0;
            y += 1;
            label = new JLabel("URL:", SwingConstants.RIGHT);
            constrain(content, label, x, y, 1, 1, GridBagConstraints.NONE, GridBagConstraints.EAST);
            constrain(content, urlField, x + 1, y, 1, 1, GridBagConstraints.NONE, GridBagConstraints.WEST);

            x = 0;
            y += 1;
            label = new JLabel(" ", SwingConstants.RIGHT);
            constrain(content, label, x, y, 2, 1, GridBagConstraints.BOTH, GridBagConstraints.EAST);

            JPanel btnPanel;
            if (isMac()) {
                btnPanel = GUIFactory.createCommandButtonPanel(new JButton[]{clearBtn, closeBtn, updateBtn});
            } else {
                btnPanel = GUIFactory.createCommandButtonPanel(new JButton[]{updateBtn, clearBtn, closeBtn});
            }

            setLayout(new BorderLayout(0, 11));
            add(content, BorderLayout.NORTH);
            add(btnPanel, BorderLayout.SOUTH);
            setBorder(BorderFactory.createEmptyBorder(12, 12, 11, 11));
        }

        public void get() {

            UserModel user = Project.getUserModel();
            FacilityModel facility = user.getFacilityModel();

            if (facility.getFacilityName() != null) {
                facilityName.setText(facility.getFacilityName());
            }

            if (facility.getZipCode() != null) {
                String val = facility.getZipCode();
                try {
                    StringTokenizer st = new StringTokenizer(val, "-");
                    if (st.hasMoreTokens()) {
                        zipField1.setText(st.nextToken());
                        zipField2.setText(st.nextToken());
                    }
                } catch (RuntimeException e) {
                    e.printStackTrace(System.err);
                }
            }

            if (facility.getAddress() != null) {
                addressField.setText(facility.getAddress());
            }

            if (facility.getTelephone() != null) {
                String val = facility.getTelephone();
                try {
                    StringTokenizer st = new StringTokenizer(val, "-");
                    if (st.hasMoreTokens()) {
                        areaField.setText(st.nextToken());
                        cityField.setText(st.nextToken());
                        numberField.setText(st.nextToken());
                    }
                } catch (Exception e) {
                    e.printStackTrace(System.err);
                }
            }

            if (facility.getUrl() != null) {
                urlField.setText(facility.getUrl());
            }

            hasInitialized = true;
        }

        private void checkButton() {

            if (!hasInitialized) {
                return;
            }

            boolean nameEmpty = facilityName.getText().trim().equals("");
            boolean zip1Empty = zipField1.getText().trim().equals("");
            boolean zip2Empty = zipField2.getText().trim().equals("");
            boolean addressEmpty = addressField.getText().trim().equals("");
            boolean areaEmpty = areaField.getText().trim().equals("");
            boolean cityEmpty = cityField.getText().trim().equals("");
            boolean numberEmpty = numberField.getText().trim().equals("");

            if (nameEmpty && zip1Empty && zip2Empty && addressEmpty
                    && areaEmpty && cityEmpty && numberEmpty) {

                if (clearBtn.isEnabled()) {
                    clearBtn.setEnabled(false);
                }
            } else {
                if (!clearBtn.isEnabled()) {
                    clearBtn.setEnabled(true);
                }
            }

            // 施設名フィールドが空の場合
            if (nameEmpty) {
                if (updateBtn.isEnabled()) {
                    updateBtn.setEnabled(false);
                }
                return;
            }

            // 施設名フィールドは空ではない
            if (!updateBtn.isEnabled()) {
                updateBtn.setEnabled(true);
            }
        }

        private void update() {

            final UserModel user = Project.getUserModel();
            // ディタッチオブジェクトが必要である
            FacilityModel facility = user.getFacilityModel();

            // 医療機関コードは変更できない

            // 施設名
            String val = facilityName.getText().trim();
            if (!val.equals("")) {
                facility.setFacilityName(val);
            }

            // 郵便番号
            val = zipField1.getText().trim();
            String val2 = zipField2.getText().trim();
            if ((!val.equals("")) && (!val2.equals(""))) {
                facility.setZipCode(val + "-" + val2);
            }

            // 住所
            val = addressField.getText().trim();
            if (!val.equals("")) {
                facility.setAddress(val);
            }

            // 電話番号
            val = areaField.getText().trim();
            val2 = cityField.getText().trim();
            String val3 = numberField.getText().trim();
            if ((!val.equals("")) && (!val2.equals("")) && (!val3.equals(""))) {
                facility.setTelephone(val + "-" + val2 + "-" + val3);
            }

            // URL
            val = urlField.getText().trim();
            if (!val.equals("")) {
                facility.setUrl(val);
            }

            // 登録日
            // 変更しない

            // タスクを実行する
            final UserDelegater udl = new UserDelegater();

            int maxEstimation = ClientContext.getInt("task.default.maxEstimation");
            int delay = ClientContext.getInt("task.default.delay");
            String updateMsg = ClientContext.getString("task.default.updateMessage");
            String message = null;

            Task<Boolean> task = new Task<Boolean>(frame, message, updateMsg, maxEstimation) {

                @Override
                protected Boolean doInBackground() {
                    logger.debug("updateUser doInBackground");
                    int cnt = udl.updateFacility(user);
                    return (cnt > 0);
                }

                @Override
                protected void succeeded(Boolean result) {
                    logger.debug("updateUser succeeded");
                    if (result) {
                        JOptionPane.showMessageDialog(frame,
                                FACILITY_SUCCESS_MSG,
                                ClientContext.getFrameTitle(getName()),
                                JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(frame,
                                udl.getErrorMessage(),
                                ClientContext.getFrameTitle(getName()),
                                JOptionPane.WARNING_MESSAGE);
                    }
                }
            };
            //task.setMillisToPopup(delay);
            task.execute();
        }
    }

    /**
     * ユーザリストを取得するクラス. 名前がいけない.
     */
    private class UserListPanel extends JPanel {
        private static final long serialVersionUID = 1L;

        private ObjectReflectTableModel<UserModel> tableModel;
        private JTable table;
        private JButton getButton;
        private JButton deleteButton;
        private JButton cancelButton;

        public UserListPanel() {
            initComponents();
        }

        private void initComponents() {

            List<PNSTriple<String, Class<?>, String>> reflectionList = Arrays.asList(
                    new PNSTriple<>("　ユーザID", String.class, "idAsLocal"),
                    new PNSTriple<>("　姓", String.class, "getSirName"),
                    new PNSTriple<>("　名", String.class, "getGivenName"),
                    new PNSTriple<>("　医療資格", LicenseModel.class, "getLicenseModel"),
                    new PNSTriple<>("　診療科", DepartmentModel.class, "getDepartmentModel")
            );

            // ユーザテーブルモデル
            tableModel = new ObjectReflectTableModel<>(reflectionList);

            table = new JTable(tableModel);
            table.putClientProperty("Quaqua.Table.style", "striped");
            table.setDefaultRenderer(Object.class, new IndentTableCellRenderer());
            // Selection を設定する
            table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            table.setRowSelectionAllowed(true);
            table.setToolTipText(DELETE_OK_USER);

            table.getSelectionModel().addListSelectionListener(e -> {
                if (!e.getValueIsAdjusting()) {
                    // 削除ボタンをコントロールする
                    // 医療資格が other 以外は削除できない
                    int index = table.getSelectedRow();
                    if (index == -1) {
                        return;
                    }
                    UserModel entry = tableModel.getObject(index);
                    if (entry != null) {
                        controleDelete(entry);
                    }
                }
            });

            // Layout
            PNSScrollPane scroller = new PNSScrollPane(table,
                    PNSScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                    PNSScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

            getButton = new JButton("ユーザリスト");
            getButton.setEnabled(true);
            getButton.addActionListener(e -> getUsers());

            deleteButton = new JButton("削除");
            deleteButton.setEnabled(false);
            deleteButton.addActionListener(e -> deleteUser());
            deleteButton.setToolTipText(DELETE_OK_USER);

            cancelButton = new JButton("閉じる");
            cancelButton.addActionListener(e -> stop());

            JPanel btnPanel = isMac() ?
                    GUIFactory.createCommandButtonPanel(new JButton[]{deleteButton, cancelButton, getButton}) :
                    GUIFactory.createCommandButtonPanel(new JButton[]{getButton, deleteButton, cancelButton});

            setLayout(new BorderLayout(0, 0));
            add(scroller, BorderLayout.CENTER);
            add(btnPanel, BorderLayout.SOUTH);
            setBorder(BorderFactory.createEmptyBorder());
        }

        /**
         * 医療資格が other 以外は削除できない.
         *
         * @param user UserModel
         */
        private void controleDelete(UserModel user) {
            boolean isMe = user.getId() == Project.getUserModel().getId();
            deleteButton.setEnabled(!isMe);
        }

        /**
         * 施設内の全ユーザを取得する.
         */
        private void getUsers() {

            final UserDelegater udl = new UserDelegater();

            int maxEstimation = ClientContext.getInt("task.default.maxEstimation");
            int delay = ClientContext.getInt("task.default.delay");
            String note = ClientContext.getString("task.default.searchMessage");
            String message = null;

            Task<List<UserModel>> task = new Task<List<UserModel>>(frame, message, note, maxEstimation) {

                @Override
                protected List<UserModel> doInBackground() {
                    logger.debug("getUsers doInBackground");
                    return udl.getAllUser();
                }

                @Override
                protected void succeeded(List<UserModel> results) {
                    logger.debug("getUsers succeeded");
                    if (udl.getErrorCode() == BusinessDelegater.Result.NO_ERROR) {
                        tableModel.setObjectList(results);
                    } else {
                        JOptionPane.showMessageDialog(frame,
                                udl.getErrorMessage(),
                                ClientContext.getFrameTitle(getName()),
                                JOptionPane.WARNING_MESSAGE);
                    }
                }
            };
            //task.setMillisToPopup(delay);
            task.execute();
        }

        /**
         * 選択したユーザを削除する.
         */
        private void deleteUser() {

            int row = table.getSelectedRow();
            UserModel entry = tableModel.getObject(row);
            if (entry == null) {
                return;
            }

            //
            // 作成したドキュメントも削除するかどうかを選ぶ
            //
            boolean deleteDoc = true;
            if (entry.getLicenseModel().getLicense().equals("doctor")) {
                deleteDoc = false;
            }

            final UserDelegater udl = new UserDelegater();

            int maxEstimation = ClientContext.getInt("task.default.maxEstimation");
            int delay = ClientContext.getInt("task.default.delay");
            String note = ClientContext.getString("task.default.deleteMessage");
            String message = null;

            final String deleteId = entry.getUserId();

            Task<List<UserModel>> task = new Task<List<UserModel>>(frame, message, note, maxEstimation) {

                @Override
                protected List<UserModel> doInBackground() {
                    logger.debug("deleteUser doInBackground");
                    List<UserModel> result = null;
                    if (udl.removeUser(deleteId) > 0) {
                        result = udl.getAllUser();
                    }
                    return result;
                }

                @Override
                protected void succeeded(List<UserModel> results) {
                    logger.debug("deleteUser succeeded");
                    if (udl.getErrorCode() == BusinessDelegater.Result.NO_ERROR) {
                        tableModel.setObjectList(results);
                        JOptionPane.showMessageDialog(frame,
                                DELETE_USER_SUCCESS_MSG,
                                ClientContext.getFrameTitle(getName()),
                                JOptionPane.INFORMATION_MESSAGE);

                    } else {
                        JOptionPane.showMessageDialog(frame,
                                udl.getErrorMessage(),
                                ClientContext.getFrameTitle(getName()),
                                JOptionPane.WARNING_MESSAGE);
                    }
                }
            };
            //task.setMillisToPopup(delay);
            task.execute();
        }

        private JTable getTable() {
            return table;
        }
    }

    /**
     * 施設内ユーザ登録クラス.
     */
    private class AddUserPanel extends JPanel {
        private static final long serialVersionUID = 1L;

        private JTextField uid; // 利用者ID
        private JPasswordField userPassword1; // パスワード
        private JPasswordField userPassword2; // パスワード
        private JTextField surName; // 姓
        private JTextField givenName; // 名
        // private String cn; // 氏名(sn & ' ' & givenName)
        private LicenseModel[] licenses; // 職種(MML0026)
        private JComboBox<LicenseModel> licenseCombo;
        private DepartmentModel[] depts; // 診療科(MML0028)
        private JComboBox<DepartmentModel> deptCombo;
        // private String authority; // LASに対する権限(admin:管理者,user:一般利用者)
        private JTextField emailField; // メールアドレス

        // JTextField description;
        private JButton okButton;
        private JButton cancelButton;

        private boolean ok;

        // UserId と Password の長さ
        private int[] userIdLength; // min,max
        private int[] passwordLength; // min,max
        private String idPassPattern;
        private String usersRole; // user に与える role 名

        public AddUserPanel() {
            initComponents();
        }

        private void initComponents() {

            userIdLength = ClientContext.getIntArray("addUser.userId.length");
            passwordLength = ClientContext.getIntArray("addUser.password.length");
            usersRole = ClientContext.getString("addUser.user.roleName");
            idPassPattern = ClientContext.getString("addUser.pattern.idPass");

            // DocumentListener
            ProxyDocumentListener dl = e -> checkButton();

            uid = new JTextField(10);
            uid.setDocument(new RegexConstrainedDocument(idPassPattern));
            uid.getDocument().addDocumentListener(dl);
            uid.addActionListener(e -> userPassword1.requestFocus());

            userPassword1 = new JPasswordField(10);
            userPassword1.setDocument(new RegexConstrainedDocument(idPassPattern));
            userPassword1.getDocument().addDocumentListener(dl);
            userPassword1.addActionListener(e -> userPassword2.requestFocus());

            userPassword2 = new JPasswordField(10);
            userPassword2.setDocument(new RegexConstrainedDocument(idPassPattern));
            userPassword2.getDocument().addDocumentListener(dl);
            userPassword2.addActionListener(e -> surName.requestFocus());

            surName = GUIFactory.createTextField(10, null, null, dl);
            surName.addActionListener(e -> givenName.requestFocus());

            givenName = GUIFactory.createTextField(10, null, null, dl);
            givenName.addActionListener(e -> emailField.requestFocus());

            emailField = GUIFactory.createTextField(15, null, null, dl);
            emailField.addActionListener(e -> uid.requestFocus());

            licenses = ClientContext.getLicenseModel();
            licenseCombo = new JComboBox<>(licenses);

            depts = ClientContext.getDepartmentModel();
            deptCombo = new JComboBox<>(depts);

            okButton = new JButton("追加");
            okButton.addActionListener(e -> addUserEntry());
            okButton.setEnabled(false);

            cancelButton = new JButton("閉じる");
            cancelButton.addActionListener(e -> stop());

            // レイアウト
            JPanel content = new JPanel(new GridBagLayout());

            int x = 0;
            int y = 0;
            JLabel label = new JLabel("ユーザID:", SwingConstants.RIGHT);
            constrain(content, label, x, y, 1, 1, GridBagConstraints.NONE, GridBagConstraints.EAST);
            constrain(content, uid, x + 1, y, 1, 1, GridBagConstraints.NONE, GridBagConstraints.WEST);

            x = 0;
            y += 1;
            label = new JLabel("パスワード:", SwingConstants.RIGHT);
            constrain(content, label, x, y, 1, 1, GridBagConstraints.NONE, GridBagConstraints.EAST);
            constrain(content, userPassword1, x + 1, y, 1, 1, GridBagConstraints.NONE, GridBagConstraints.WEST);
            label = new JLabel("確認:", SwingConstants.RIGHT);
            constrain(content, label, x + 2, y, 1, 1, GridBagConstraints.NONE, GridBagConstraints.EAST);
            constrain(content, userPassword2, x + 3, y, 1, 1, GridBagConstraints.NONE, GridBagConstraints.WEST);

            x = 0;
            y += 1;
            label = new JLabel("姓:", SwingConstants.RIGHT);
            constrain(content, label, x, y, 1, 1, GridBagConstraints.NONE, GridBagConstraints.EAST);
            constrain(content, surName, x + 1, y, 1, 1, GridBagConstraints.NONE, GridBagConstraints.WEST);
            label = new JLabel("名:", SwingConstants.RIGHT);
            constrain(content, label, x + 2, y, 1, 1, GridBagConstraints.NONE, GridBagConstraints.EAST);
            constrain(content, givenName, x + 3, y, 1, 1, GridBagConstraints.NONE, GridBagConstraints.WEST);

            x = 0;
            y += 1;
            label = new JLabel("医療資格:", SwingConstants.RIGHT);
            constrain(content, label, x, y, 1, 1, GridBagConstraints.NONE, GridBagConstraints.EAST);
            constrain(content, licenseCombo, x + 1, y, 1, 1, GridBagConstraints.NONE, GridBagConstraints.WEST);
            label = new JLabel("診療科:", SwingConstants.RIGHT);
            constrain(content, label, x + 2, y, 1, 1, GridBagConstraints.NONE, GridBagConstraints.EAST);
            constrain(content, deptCombo, x + 3, y, 1, 1, GridBagConstraints.NONE, GridBagConstraints.WEST);

            x = 0;
            y += 1;
            label = new JLabel("電子メール:", SwingConstants.RIGHT);
            constrain(content, label, x, y, 1, 1, GridBagConstraints.NONE, GridBagConstraints.EAST);
            constrain(content, emailField, x + 1, y, 1, 1, GridBagConstraints.NONE, GridBagConstraints.WEST);

            x = 0;
            y += 1;
            label = new JLabel(" ", SwingConstants.RIGHT);
            constrain(content, label, x, y, 4, 1, GridBagConstraints.BOTH, GridBagConstraints.EAST);

            x = 0;
            y += 1;
            label = new JLabel("ユーザID - 半角英数記で" + userIdLength[0] + "文字以上" + userIdLength[1] + "文字以内");
            constrain(content, label, x, y, 4, 1, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST);
            x = 0;
            y += 1;
            label = new JLabel("パスワード - 半角英数記で" + passwordLength[0] + "文字以上" + passwordLength[1] + "文字以内");
            constrain(content, label, x, y, 4, 1, GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST);

            JPanel btnPanel = isMac() ?
                    GUIFactory.createCommandButtonPanel(new JButton[]{cancelButton, okButton}) :
                    GUIFactory.createCommandButtonPanel(new JButton[]{okButton, cancelButton});

            setLayout(new BorderLayout(0, 17));
            add(content, BorderLayout.NORTH);
            add(btnPanel, BorderLayout.SOUTH);

            setBorder(BorderFactory.createEmptyBorder(12, 12, 11, 11));
        }

        private void addUserEntry() {

            if (!userIdOk()) {
                return;
            }

            if (!passwordOk()) {
                return;
            }

            String userId = uid.getText().trim();
            String pass = new String(userPassword1.getPassword());
            UserModel loginUser = Project.getUserModel();
            String facilityId = loginUser.getFacilityModel().getFacilityId();

            String hashPass = HashUtil.MD5(pass);
            //String Algorithm = ClientContext.getString("addUser.password.hash.algorithm");
            //String encoding = ClientContext.getString("addUser.password.hash.encoding");
            //String charset = ClientContext.getString("addUser.password.hash.charset");
            //String charset = null;
            //String hashPass = CryptoUtil.createPasswordHash(Algorithm, encoding, charset, userId, pass);
            //pass = null;

            final UserModel user = new UserModel();
            String sb = facilityId + IInfoModel.COMPOSITE_KEY_MAKER + userId;
            user.setUserId(sb);
            user.setPassword(hashPass);
            user.setSirName(surName.getText().trim());
            user.setGivenName(givenName.getText().trim());
            user.setCommonName(user.getSirName() + " " + user.getGivenName());

            // 施設情報
            // 管理者のものを使用する
            user.setFacilityModel(Project.getUserModel().getFacilityModel());

            // 医療資格
            int index = licenseCombo.getSelectedIndex();
            user.setLicenseModel(licenses[index]);

            // 診療科
            index = deptCombo.getSelectedIndex();
            user.setDepartmentModel(depts[index]);

            // MemberType
            // 管理者のものを使用する
            user.setMemberType(Project.getUserModel().getMemberType());

            // RegisteredDate
            if (Project.getUserModel().getMemberType().equals("ASP_TESTER")) {
                user.setRegisteredDate(Project.getUserModel().getRegisteredDate());
            } else {
                user.setRegisteredDate(new Date());
            }

            // Email
            user.setEmail(emailField.getText().trim());

            // Role = user
            RoleModel rm = new RoleModel();
            rm.setRole(usersRole);
            user.addRole(rm);
            rm.setUser(user);
            rm.setUserId(user.getUserId()); // 必要

            // タスクを実行する
            final UserDelegater udl = new UserDelegater();

            int maxEstimation = ClientContext.getInt("task.default.maxEstimation");
            int delay = ClientContext.getInt("task.default.delay");
            String addMsg = ClientContext.getString("task.default.addMessage");
            String message = null;

            Task<Boolean> task = new Task<Boolean>(frame, message, addMsg, maxEstimation) {

                @Override
                protected Boolean doInBackground() {
                    logger.debug("addUserEntry doInBackground");
                    int cnt = udl.putUser(user);
                    return (cnt > 0);
                }

                @Override
                protected void succeeded(Boolean results) {
                    logger.debug("addUserEntry succeeded");
                    if (results) {
                        JOptionPane.showMessageDialog(frame,
                                ADD_USER_SUCCESS_MSG,
                                ClientContext.getFrameTitle(getName()),
                                JOptionPane.INFORMATION_MESSAGE);

                    } else {
                        JOptionPane.showMessageDialog(frame,
                                udl.getErrorMessage(),
                                ClientContext.getFrameTitle(getName()),
                                JOptionPane.WARNING_MESSAGE);
                    }
                }
            };
            //task.setMillisToPopup(delay);
            task.execute();
        }

        private boolean userIdOk() {

            String userId = uid.getText().trim();
            if (userId.equals("")) {
                return false;
            }

            int len = userId.length();
            return (len >= userIdLength[0] && len <= userIdLength[1]);
        }

        private boolean passwordOk() {

            String passwd1 = new String(userPassword1.getPassword());
            String passwd2 = new String(userPassword2.getPassword());

            if (passwd1.equals("") || passwd2.equals("")) {
                return false;
            }

            if ((passwd1.length() < passwordLength[0])
                    || (passwd1.length() > passwordLength[1])) {
                return false;
            }

            if ((passwd2.length() < passwordLength[0])
                    || (passwd2.length() > passwordLength[1])) {
                return false;
            }

            return passwd1.equals(passwd2);
        }

        private void checkButton() {

            boolean userOk = userIdOk();
            boolean passwordOk = passwordOk();
            boolean snOk = !surName.getText().trim().equals("");
            boolean givenOk = !givenName.getText().trim().equals("");
            boolean emailOk = !emailField.getText().trim().equals("");

            boolean newOk = (userOk && passwordOk && snOk && givenOk && emailOk);

            if (ok != newOk) {
                ok = newOk;
                okButton.setEnabled(ok);
            }
        }
    }
}
