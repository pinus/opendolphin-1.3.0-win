package open.dolphin.order.stampeditor;

import open.dolphin.event.ProxyDocumentListener;
import open.dolphin.event.ValidListener;
import open.dolphin.helper.TextComponentUndoManager;
import open.dolphin.infomodel.IInfoModel;
import open.dolphin.infomodel.ModuleInfoBean;
import open.dolphin.infomodel.ModuleModel;
import open.dolphin.infomodel.TextStampModel;
import open.dolphin.orca.ClaimConst;
import open.dolphin.order.IStampEditor;
import open.dolphin.ui.Focuser;
import open.dolphin.ui.HorizontalPanel;
import open.dolphin.ui.PNSScrollPane;

import javax.swing.*;
import java.awt.*;

/**
 * TextStampEditor.
 *
 * @author pns
 */
public final class TextStampEditor extends JPanel implements IStampEditor<ModuleModel> {
    private static final long serialVersionUID = 1L;
    // ItemTablePanel.java の stampNameField.setBackground と同じ色
    private static final Color STAMP_NAME_FIELD_BACKGROUND = new Color(251, 239, 128);
    private String title; // Dialog に表示されるタイトル
    private ValidListener validListener;
    private JTextPane textPane;
    private JTextField headerField;
    private boolean isValidModel = false;
    private String entity;
    private TextComponentUndoManager paneUndoManager;
    private TextComponentUndoManager fieldUndoManager;

    public TextStampEditor() {
        entity = IInfoModel.ENTITY_TEXT;
    }

    /**
     * エディタを開始する.
     */
    @Override
    public void start() {
        setTitle(ClaimConst.EntityNameMap.get(entity));

        textPane = new JTextPane();
        textPane.setMargin(new Insets(7, 7, 7, 7));
        textPane.getDocument().addDocumentListener((ProxyDocumentListener) e -> checkState());
        headerField = new JTextField();
        headerField.getDocument().addDocumentListener((ProxyDocumentListener) e -> checkState());
        //headerField.setBackground(STAMP_NAME_FIELD_BACKGROUND);
        //headerField.setBorder(PNSBorderFactory.createSelectedGrayBorder());
        headerField.setOpaque(false);

        // Undo
        paneUndoManager = TextComponentUndoManager.createManager(textPane);
        textPane.getDocument().addUndoableEditListener(paneUndoManager);
        fieldUndoManager = TextComponentUndoManager.createManager(headerField);
        headerField.getDocument().addUndoableEditListener(fieldUndoManager);

        HorizontalPanel headerPanel = new HorizontalPanel();
        headerPanel.setPreferredSize(new Dimension(10, 30));

        JLabel label = new JLabel(" スタンプ名：");

        headerPanel.add(label);
        headerPanel.add(headerField);

        PNSScrollPane scroller = new PNSScrollPane(textPane);
        scroller.setVerticalScrollBarPolicy(PNSScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scroller.setHorizontalScrollBarPolicy(PNSScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        this.setLayout(new BorderLayout(0, 0));
        this.add(headerPanel, BorderLayout.NORTH);
        this.add(scroller, BorderLayout.CENTER);
        this.setBorder(BorderFactory.createEmptyBorder());
    }

    private void checkState() {
        String header = headerField.getText();
        String text = textPane.getText();
        setValid(header != null && !header.equals("") && text != null && !text.equals(""));
    }

    /**
     * enter したらフォーカスを取る.
     */
    @Override
    public void enter() {
        Focuser.requestFocus(textPane);
    }

    /**
     * このエディタではとくに dispose すべきリソースはない.
     */
    @Override
    public void dispose() {
    }

    public String getEntity() {
        return entity;
    }

    @Override
    public void setEntity(String entity) {
        this.entity = entity;
    }

    @Override
    public boolean isValidModel() {
        return isValidModel;
    }

    @Override
    public void setValid(boolean valid) {
        boolean old = isValidModel;
        isValidModel = valid;
        if (old != isValidModel) {
            validListener.validity(valid);
        }
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public void addValidListener(ValidListener listener) {
        validListener = listener;
    }

    /**
     * 編集したテキストを返す.
     *
     * @return ModuleModel
     */
    @Override
    public ModuleModel getValue() {
        ModuleModel model = new ModuleModel();
        TextStampModel stamp = new TextStampModel();
        ModuleInfoBean info = new ModuleInfoBean();

        info.setStampName(headerField.getText().trim());
        info.setEntity(entity);
        info.setStampRole(IInfoModel.ROLE_TEXT);
        stamp.setText(textPane.getText());

        model.setModel(stamp);
        model.setModuleInfo(info);

        return model;
    }

    /**
     * 編集するテキストを設定する.
     *
     * @param model ModuleModel
     */
    @Override
    public void setValue(ModuleModel model) {
        // set text
        TextStampModel stamp = (TextStampModel) model.getModel();
        textPane.setText(stamp.getText());
        headerField.setText(model.getModuleInfo().getStampName());

        // reset undo
        paneUndoManager.discardAllEdits();
        fieldUndoManager.discardAllEdits();
        enter();
    }
}
