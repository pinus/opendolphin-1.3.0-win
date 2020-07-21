package open.dolphin.order;

import open.dolphin.event.ValidListener;
import open.dolphin.infomodel.IInfoModel;
import open.dolphin.order.stampeditor.StampEditor;
import open.dolphin.order.stampeditor.TextStampEditor;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;

/**
 * 全ての StampEditor を保持可能なパネル.
 * valid data かどうかを確認するリスナを登録必要.
 *
 * @author pns
 */
public class StampEditorProxyPanel extends JPanel {
    private static final long serialVersionUID = 1L;

    private CardLayout cardLayout;
    // entity のエディタをロードするローダー
    private StampEditorLoader loader;
    // 現在選択されているエディタ
    private IStampEditor curEditor;
    // Validity listener
    private ValidListener validListener;

    public StampEditorProxyPanel() {
        initComponents();
    }

    private void initComponents() {
        cardLayout = new CardLayout();
        setLayout(cardLayout);
        loader = new StampEditorLoader(this::setValid, this);
    }

    public void addValidListener(ValidListener listener) {
        validListener = listener;
    }

    /**
     * editor の enter 処理を行う.
     */
    public void enter() {
        curEditor.enter();
    }

    /**
     * entity のエディタを表示する.
     *
     * @param entity
     */
    public void show(String entity) {
        // load した際に editorCardPanel にも追加される
        curEditor = loader.load(entity);
        cardLayout.show(this, entity);
    }

    /**
     * リソースの解放.
     */
    public void close() {
        loader.disposeAll();
    }

    /**
     * エディタにセットされているデータを取り出す.
     *
     * @return
     */
    public Object getValue() {
        return curEditor.getValue();
    }

    /**
     * エディタにデータをセットする.
     *
     * @param value
     */
    public void setValue(Object value) {
        curEditor.setValue(value);
    }

    /**
     * valid を通知する.
     *
     * @param valid
     */
    public void setValid(boolean valid) {
        validListener.validity(valid);
    }

    /**
     * 現在選択されている Editor を返す.
     * @return active StampEditor
     */
    public IStampEditor getEditor() {
        return curEditor;
    }

    /**
     * StampEditor をロードするための loader.
     */
    private class StampEditorLoader {
        // load した editor を保持する HashMap
        private final HashMap<String, IStampEditor> editorMap = new HashMap<>(14);
        // load した editor につける ValidityListener
        private final ValidListener listener;
        // load した editor を格納する panel (CardLayout)
        private final JPanel panel;

        public StampEditorLoader(ValidListener listener, JPanel panel) {
            this.listener = listener;
            this.panel = panel;
        }

        /**
         * entity に対応する StampEditor を作成する
         *
         * @param entity Entiry
         * @return StampEditor
         */
        public IStampEditor load(String entity) {
            IStampEditor editor = editorMap.get(entity);

            if (editor == null) {
                if (IInfoModel.ENTITY_TEXT.equals(entity)) {
                    editor = new TextStampEditor();
                } else {
                    editor = new StampEditor(entity);
                }
                editor.addValidListener(listener);
                editor.start();
                editor.setEntity(entity);
                editorMap.put(entity, editor);
                panel.add(entity, (Component) editor);
            }
            editor.enter();
            return editor;
        }

        /**
         * 作成された全ての StampEditor を dispose する.
         */
        public void disposeAll() {
            editorMap.keySet().forEach(key -> {
                IStampEditor e = editorMap.get(key);
                if (e != null) {
                    e.dispose();
                }
            });
        }
    }
}
