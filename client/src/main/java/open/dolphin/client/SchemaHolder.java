package open.dolphin.client;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import javafx.application.Platform;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.text.Position;
import open.dolphin.helper.ImageHelper;
import open.dolphin.impl.scheam.SchemaEditorImpl;
import open.dolphin.infomodel.SchemaModel;
import open.dolphin.ui.PNSBorderFactory;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 * スタンプのデータを保持するコンポーネントで TextPane に挿入される.
 *
 * @author Kazushi Minagawa, Digital Globe, Inc.
 * @author pns
 */
public final class SchemaHolder extends AbstractComponentHolder {
    private static final long serialVersionUID = 1777560751402251092L;
    private static final Dimension INITIAL_SIZE = new Dimension(192,192);
    private static final Border MY_SELECTED_BORDER = PNSBorderFactory.createSelectedBorder();
    private static final Border MY_CLEAR_BORDER = PNSBorderFactory.createClearBorder();

    private SchemaModel schema;
    private ImageIcon icon;
    private float imgRatio = 1.0f;
    private SchemaEditor editor = null;
    private boolean selected;
    private Position start;
    private Position end;
    private KartePane kartePane;

    private Logger logger;

    public SchemaHolder(KartePane kartePane, SchemaModel schema) {
        logger = Logger.getLogger(SchemaHolder.class);
        logger.setLevel(Level.INFO);
        logger.debug("SchemaHolder constractor");

        this.kartePane = kartePane;
        this.schema = schema;
        this.setImageIcon(schema.getIcon());
    }

    public void setImageIcon(final ImageIcon icon) {
        logger.debug("SchemaHolder setImageIcon");
        // 最初から Border をセットしておく
        // 選択したときに初めて Border が作られると，微妙に schema が動いてしまう
        setVerticalAlignment(SwingConstants.CENTER);
        setHorizontalAlignment(SwingConstants.CENTER);
        setBorder(MY_CLEAR_BORDER);

        this.icon = icon;

        SwingUtilities.invokeLater(() -> {
            setIcon(ImageHelper.adjustImageSize(icon, INITIAL_SIZE));
            imgRatio = getIcon().getIconWidth() / (float) icon.getIconWidth();
            logger.debug("initial img ratio = " + imgRatio);

            JTextPane pane = kartePane.getTextPane();
            if (pane != null) {
                // kartePane の back ground color は invokeLater しないと取れない
                setBackground(pane.getBackground());
            }
        });
    }

    /**
     * Alt-Click で拡大，Alt-Shift-Click で縮小.
     * @param e
     */
    @Override
    public void mousePressed(MouseEvent e) {
        super.mousePressed(e);

        if (e.isAltDown()) {
            Image img = icon.getImage();
            imgRatio = e.isShiftDown()? imgRatio - 0.05f : imgRatio + 0.05f;
            int w = (int) (icon.getIconWidth() * imgRatio);
            int h = (int) (icon.getIconHeight() * imgRatio);
            img = img.getScaledInstance(w, h, Image.SCALE_SMOOTH);
            setIcon(new ImageIcon(img));
        }
    }

    @Override
    public ContentType getContentType() {
        return ContentType.TT_IMAGE;
    }

    @Override
    public KartePane getKartePane() {
        return kartePane;
    }

    public SchemaModel getSchema() {
        return schema;
    }

    @Override
    public boolean isSelected() {
        return selected;
    }

    @Override
    public void enter(ActionMap map) {
        logger.debug("SchemaHolder enter");

        map.get(GUIConst.ACTION_COPY).setEnabled(true);
        map.get(GUIConst.ACTION_CUT).setEnabled(kartePane.getTextPane().isEditable());
        map.get(GUIConst.ACTION_PASTE).setEnabled(false);

        setSelected(true);
    }

    @Override
    public void exit(ActionMap map) {
        logger.debug("SchemaHolder exit");
        setSelected(false);
    }

    @Override
    public JLabel getComponent() {
        return this;
    }

    @Override
    public void maybeShowPopup(MouseEvent e) {
        JPopupMenu popup = new JPopupMenu();
        popup.setFocusable(false);
        ChartMediator mediator = kartePane.getMediator();
        popup.add(mediator.getAction(GUIConst.ACTION_CUT));
        popup.add(mediator.getAction(GUIConst.ACTION_COPY));
        popup.add(mediator.getAction(GUIConst.ACTION_PASTE));
        popup.show(e.getComponent(), e.getX(), e.getY());
    }

    @Override
    public void setSelected(boolean selected) {
        logger.debug("SchemaHolder setSelected " + selected);

        if (selected ^ this.selected) {
            setBorder(selected? MY_SELECTED_BORDER : MY_CLEAR_BORDER);
            this.selected = selected;
        }
    }

    @Override
    public void edit() {
        logger.debug("SchemaHolder edit");

        if (kartePane.getTextPane().isEditable() && this.isEditable()) {

            // JavaFX thread
            Platform.runLater(() -> {
                if (editor == null) {
                    editor = new SchemaEditorImpl();
                }
                editor.setSchema(schema);
                editor.setEditable(kartePane.getTextPane().isEditable());
                editor.addPropertyChangeListener(SchemaHolder.this);
                editor.start();
            });

            // 二重起動の禁止
            this.setEditable(false);
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent e) {
        logger.debug("SchemaHolder propertyChange");

        // 二重起動の解除
        this.setEditable(true);

        SchemaModel newSchema = (SchemaModel) e.getNewValue();
        if (newSchema ==  null) {
            return;
        }

        schema = newSchema;
        setIcon(ImageHelper.adjustImageSize(schema.getIcon(), INITIAL_SIZE));
        // dirty セット
        kartePane.setDirty(true);
    }

    @Override
    public void setEntry(Position start, Position end) {
        this.start = start;
        this.end = end;
    }

    @Override
    public int getStartPos() {
        return start.getOffset();
    }

    @Override
    public int getEndPos() {
        return end.getOffset();
    }
}
