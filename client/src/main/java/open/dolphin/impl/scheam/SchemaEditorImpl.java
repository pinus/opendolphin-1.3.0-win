package open.dolphin.impl.scheam;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.InputEvent;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import open.dolphin.client.SchemaEditor;
import open.dolphin.impl.scheam.helper.SchemaUtils;
import open.dolphin.impl.scheam.shapeholder.ImageHolder;
import open.dolphin.impl.scheam.widget.PnsStage;
import open.dolphin.infomodel.SchemaModel;

import javax.swing.*;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Arrays;
import java.util.stream.Stream;

/**
 * SchemaEditorImpl.
 * <p>
 * +-- DraftLayer
 * |                +- DrawLayer - Holder
 * |                |
 * ContentPane -+-- CanvasPane --+- DrawLayer - Holder
 * |                |
 * |                +- DrawLayer - Holder
 * +-- BaseLayer
 *
 * @author pns
 */
public final class SchemaEditorImpl implements SchemaEditor {
    public static final String DEFAULT_TITLE = "参考画像";
    public static final String DEFAULT_ROLE = "参考図";
    private static SchemaEditorProperties properties;
    /**
     * SchemaEditor のウインドウ (stage)
     */
    private final PnsStage canvasStage;
    /**
     * base image を載せる layer
     */
    private final SchemaLayer baseLayer;
    /**
     * 実際に描画する Layers を入れる StackPane: 中に入る Layer は StateManager で作られる
     */
    private final StackPane canvasPane;
    /**
     * mouse event を受けて途中経過を描画する Layer
     */
    private final SchemaLayer draftLayer;
    /**
     * StateManager
     */
    private final StateManager stateManager;
    /**
     * UndoManager
     */
    private final UndoManager undoManager;
    private final Button okButton;
    private final Button cancelButton;
    /**
     * setEditable で Listener を付けたり取ったりするための Property
     */
    private final BooleanProperty editableProperty = new SimpleBooleanProperty();
    private SchemaModel model;
    // KartePane との通信用
    private PropertyChangeSupport boundSupport;

    public SchemaEditorImpl() {
        properties = new SchemaEditorProperties();
        properties.load();

        baseLayer = new SchemaLayer();
        draftLayer = new SchemaLayer();
        // 実際に描画する Layer を載せるパネル
        canvasPane = new StackPane();

        canvasStage = new PnsStage();
        canvasStage.setTitle("Schema Editor");
        canvasStage.getScene().getStylesheets().add("css/schemaeditorimpl.css");

        undoManager = new UndoManager(this); // should be instantiated before StateManager
        stateManager = new StateManager(this);

        // 大きさはみんな BaseLayer に合わせる
        draftLayer.widthProperty().bind(baseLayer.widthProperty());
        draftLayer.heightProperty().bind(baseLayer.heightProperty());
        canvasPane.prefWidthProperty().bind(baseLayer.widthProperty());
        canvasPane.prefHeightProperty().bind(baseLayer.heightProperty());

        // カルテに展開して終了する
        okButton = new Button("カルテに展開");
        okButton.setFocusTraversable(false);
        okButton.setDefaultButton(true);
        okButton.setOnAction(e -> {
            SchemaLayer output = new SchemaLayer();
            GraphicsContext outputGc = output.getGraphicsContext2D();
            output.setWidth(baseLayer.getWidth());
            output.setHeight(baseLayer.getHeight());

            ShapeHolder baseHolder = baseLayer.getHolder();
            baseHolder.setGraphicsContext(outputGc);
            baseHolder.draw();

            canvasPane.getChildren().forEach(node -> {
                ShapeHolder holder = ((SchemaLayer) node).getHolder();
                holder.setGraphicsContext(outputGc);
                holder.draw();
            });

            SnapshotParameters parameters = new SnapshotParameters();
            parameters.setFill(Color.WHITE);

            Image image = output.snapshot(parameters, null);

            firePropertyChange(image);
            canvasStage.hide();
            close();
        });
        // 破棄して終了する
        cancelButton = new Button("破棄");
        cancelButton.setFocusTraversable(false);
        cancelButton.setOnAction(e -> {
            firePropertyChange(null);
            canvasStage.hide();
            close();
        });

        // レイアウト
        HBox buttonPane = new HBox();
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox.setMargin(okButton, new Insets(8, 8, 4, 0));
        HBox.setMargin(cancelButton, new Insets(8, 8, 4, 0));
        buttonPane.getChildren().addAll(spacer, cancelButton, okButton);

        StackPane contentPane = new StackPane();
        StackPane.setAlignment(baseLayer, Pos.CENTER);
        StackPane.setAlignment(canvasPane, Pos.CENTER);
        StackPane.setAlignment(draftLayer, Pos.CENTER);
        contentPane.getChildren().addAll(baseLayer, canvasPane, draftLayer);

        canvasStage.addTool(new ToolPane(this));
        canvasStage.addContent(contentPane);
        canvasStage.addFooter(buttonPane);

        // MouseListeners
        draftLayer.setOnMousePressed(stateManager::mousePressed);
        draftLayer.setOnMouseDragged(stateManager::mouseDragged);
        draftLayer.setOnMouseMoved(stateManager::mouseMoved);
        draftLayer.setOnMouseReleased(stateManager::mouseReleased);
        // KeyListeners
        canvasStage.getScene().setOnKeyPressed(stateManager::keyPressed);
        canvasStage.getScene().setOnKeyReleased(stateManager::keyReleased);

        // Editable 処理
        editableProperty.set(true); // 初期値
        editableProperty.addListener(new ChangeListener<Boolean>() {
            private final EventHandler<InputEvent> consumer = Event::consume;

            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                if (newValue) {
                    draftLayer.removeEventFilter(MouseEvent.ANY, consumer);
                    canvasStage.getScene().removeEventFilter(KeyEvent.ANY, consumer);
                } else {
                    draftLayer.addEventFilter(MouseEvent.ANY, consumer);
                    canvasStage.getScene().addEventFilter(KeyEvent.ANY, consumer);
                }
            }
        });

        // 終了処理
        canvasStage.setOnHidden(e -> {
            for (PropertyChangeListener l : boundSupport.getPropertyChangeListeners()) {
                boundSupport.removePropertyChangeListener(l);
            }
            // TranslateEditor で選択したまま終了する可能性がある
            stateManager.stateProperty().get().end();
            // 再利用できる可能性もある
            canvasPane.getChildren().clear();
            properties.save();
        });
    }

    /**
     * Properties を返す.
     *
     * @return properties
     */
    public static SchemaEditorProperties getProperties() {
        return properties;
    }

    /**
     * CanvasStage を返す.
     *
     * @return canvasStage
     */
    public PnsStage getCanvasStage() {
        return canvasStage;
    }

    /**
     * DrawLayers を載せる Pane.
     *
     * @return canvasPane
     */
    public StackPane getCanvasPane() {
        return canvasPane;
    }

    /**
     * CanvasStage の ContentPane を返す.
     * BaseLayer, CanvasPane, DraftLayer を載せる.
     *
     * @return ContentPane
     */
    public StackPane getContentPane() {
        return canvasStage.getContentPane();
    }

    /**
     * Mouse Event を受け取って途中経過を描く DraftLayer を返す.
     *
     * @return draftLayer
     */
    public SchemaLayer getDraftLayer() {
        return draftLayer;
    }

    /**
     * Base 画像を表示する BaseLayer を返す.
     *
     * @return baseLayer
     */
    public SchemaLayer getBaseLayer() {
        return baseLayer;
    }

    /**
     * StateManager を返す.
     *
     * @return stateManager
     */
    public StateManager getStateManager() {
        return stateManager;
    }

    /**
     * UndoManager を返す.
     *
     * @return undoManager
     */
    public UndoManager getUndoManager() {
        return undoManager;
    }

    /**
     * プログラムの入り口.
     */
    @Override
    public void start() {
        Image image = SchemaUtils.toFXImage(model.getIcon().getImage());
        ImageHolder imageHolder = new ImageHolder();
        imageHolder.setNode(new ImageView(image));
        baseLayer.setHolder(imageHolder);
        double w = image.getWidth();
        double h = image.getHeight();
        baseLayer.setWidth(w);
        baseLayer.setHeight(h);

        baseLayer.draw();
        canvasStage.show();

        // Tool が隠れない大きさを維持
        canvasStage.setMinWidth(canvasStage.getWidth());
        // undo をクリア
        undoManager.clearQueue();
    }

    /**
     * このリスナは fire するとKartePane or SchemaHolder の propertyChanged を呼び出す.
     *
     * @param listener PropertyChangeListener
     */
    @Override
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        if (boundSupport == null) {
            boundSupport = new PropertyChangeSupport(this);
        }
        if (!Arrays.asList(boundSupport.getPropertyChangeListeners()).contains(listener)) {
            boundSupport.addPropertyChangeListener(listener);
        }
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener l) {
        if (boundSupport == null) {
            boundSupport = new PropertyChangeSupport(this);
        }
        boundSupport.removePropertyChangeListener(l);
    }

    /**
     * KartePane に SchemaModel を返す.
     * FXImage → SwingImage 変換する.
     * 　「カルテに展開」ボタン：　createImage で作った BufferedImage を持ってくる
     * 　「破棄」ボタン　　　　：　null を持ってくる
     *
     * @param image Image
     */
    public void firePropertyChange(Image image) {
        // カルテに展開
        if (image != null) {
            ImageIcon icon = new ImageIcon(SwingFXUtils.fromFXImage(image, null));
            model.setIcon(icon);
            model.getExtRef().setTitle(DEFAULT_TITLE);
            model.getExtRef().setMedicalRole(DEFAULT_ROLE);
            boundSupport.firePropertyChange(IMAGE_PROP, null, model);

            // キャンセル
        } else {
            boundSupport.firePropertyChange(IMAGE_PROP, model, null);
        }
    }

    @Override
    public void setEditable(boolean b) {
        editableProperty.set(b);
    }

    @Override
    public void setSchema(SchemaModel model) {
        this.model = model;
    }

    private void close() {
        Stream.of(boundSupport.getPropertyChangeListeners()).
            forEach(boundSupport::removePropertyChangeListener);
    }
}
