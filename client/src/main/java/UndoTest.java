import open.dolphin.helper.StringTool;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.UndoableEditEvent;
import javax.swing.text.AbstractDocument;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import javax.swing.text.StyledEditorKit;
import javax.swing.undo.CompoundEdit;
import javax.swing.undo.UndoManager;
import javax.swing.undo.UndoableEdit;
import java.awt.*;
import java.awt.event.*;
import java.lang.reflect.Field;
import java.text.AttributedCharacterIterator;
import java.text.CharacterIterator;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.regex.Pattern;

public class UndoTest {
    private Logger logger = LoggerFactory.getLogger(UndoTest.class);
    private static KeyStroke CTRL_BACKSPACE = KeyStroke.getKeyStroke("ctrl pressed BACK_SPACE");
    private static KeyStroke KANA = KeyStroke.getKeyStroke("released KATAKANA");
    private static KeyStroke EISU = KeyStroke.getKeyStroke("released ALPHANUMERIC");
    private static Pattern ALPHABET = Pattern.compile("^[a-z,A-Z]*$");

    public class TextComponentUndoManager extends UndoManager {

        private JTextComponent textComponent;
        private Action undoAction;
        private Action redoAction;
        private TextComponentUndoableEdit current = new TextComponentUndoableEdit();
        private Timer timer;
        private int delay = 30;

        public TextComponentUndoManager(JTextComponent c) {
            textComponent = c;
            timer = new Timer(delay, e -> flush());
            AtokListener listener = new AtokListener();
            c.addInputMethodListener(listener);
            c.addKeyListener(listener);

        }

        private class AtokListener implements KeyListener, InputMethodListener {
            // 確定アンドゥのキーが押されたかどうか
            private boolean ctrlBackspace = false;
            // 確定アンドゥ中かどうか
            private boolean isInKakuteiUndo = false;
            // かなキー2度押しされたかどうか
            private boolean doubleKana = false;
            // 英数キー2度押しされたかどうか
            private boolean doubleEisu = false;
            // IME 開始された時間
            private long timeImeTextChanged;
            // KeyPressed があった時間
            private long timeKeyPressed;
            // KeyReleased があった時間
            private long timeKeyReleased;
            // 変換前文字列と変換後文字
            private String textInProcess = "", textCommitted = "";

            @Override
            public void inputMethodTextChanged(InputMethodEvent event) {
                long timeFromImeTextChange = System.currentTimeMillis() - timeImeTextChanged;
                long timeFromKeyPress = System.currentTimeMillis() - timeKeyPressed;

                logger.info("textchanged " + event);
                logger.info("time from key press = " + timeFromKeyPress + ", time from text change = " + timeFromImeTextChange +
                    ", ctrl-backspace = " + ctrlBackspace + ", doubleKana = " + doubleKana);

                // キー押してすぐ入ってきてなかったら無視
                ctrlBackspace = ctrlBackspace && timeFromKeyPress < 300;

                // 確定アンドゥは commit されたら終了
                if (isInKakuteiUndo && event.getCommittedCharacterCount() > 0) {
                    logger.info("Kakutei-undo done: " + event.getCommittedCharacterCount());
                    isInKakuteiUndo = false;
                }

                // 確定アンドゥ 1回目
                if (ctrlBackspace) {
                    logger.info("Kakutei-undo start");
                    undo();
                    ctrlBackspace = false;
                    isInKakuteiUndo = true;

                } else if (isInKakuteiUndo && timeFromImeTextChange < 10) {
                    // 2回目以降の ctrl-backspace キーは ATOK に取られて検出できないが，
                    // timeFromTextChange が非常に短く入ってくるので検出できる
                    logger.info("Kakutei-undo cont'd: " + timeFromImeTextChange);
                    undo();
                }

                // 実行時間記録
                timeImeTextChanged = System.currentTimeMillis();

                // 処理文字列保存
                AttributedCharacterIterator iter = event.getText();
                StringBuilder sb = new StringBuilder();
                int count = event.getCommittedCharacterCount();
                char c = iter.first();
                while (count-- > 0) {
                    sb.append(c);
                    c = iter.next();
                }
                if (!StringUtils.isEmpty(sb.toString())) {
                    textCommitted = sb.toString();
                }
                sb = new StringBuilder();
                while (c != CharacterIterator.DONE) {
                    sb.append(c);
                    c = iter.next();
                }
                if (!StringUtils.isEmpty(sb.toString())) {
                    textInProcess = sb.toString();
                }
            }

            private void processDoubleKey() {
                // ２度打ちは KeyReleased でしか検出できないので, inputMethodTextChanged の後の処理になる
                if (System.currentTimeMillis() - timeImeTextChanged > 300) { return; }

                try {
                    // かなキー２度打ちの処理
                    if (doubleKana) {
                        // 再変換元の文字列の最後を検出
                        int pos = textComponent.getCaretPosition();
                        int end = pos - textInProcess.length();

                        // end から逆にたどって, alphanumeric 以外の文字が出てくるところを検出
                        int start = end;
                        while (start-- > 0) {
                            char c = textComponent.getText(start, 1).charAt(0);
                            logger.info(start + ": " + c);
                            if (!StringTool.isHanakuLower(c) && !StringTool.isHankakuUpper(c)) {
                                break;
                            }
                        }
                        start++;
                        // 変換元の文字列を削除
                        textComponent.getDocument().remove(start, end - start);

                    }
                    // 英数２度打ちの処理. (1)確定前に入ってくる場合と (2)確定直後に入ってくる場合がある.
                    // (1) は何もしなくていい (textCommitted != textInProcess)
                    // (2) は確定終了, 未 flush の状態になっている
                    else if (doubleEisu) {
                        logger.error("double eisu detected: " + textCommitted + "/"+ textInProcess);
                        if (textCommitted.equals(textInProcess)) {
                            // flush すると "tうぃってrtwitter" の状態になる
                            flush();
                            // これを２回 undo すると "twitter" → "tうぃってr" の順に消える
                            undo();
                            undo();
                            // そこに改めて "twitter" を挿入する
                            textComponent.getDocument().insertString(textComponent.getCaretPosition(), textCommitted, null);
                        }
                    }

                } catch (BadLocationException e) {
                    logger.error(e.getMessage());
                }

                textInProcess = "";
                textCommitted = "";
            }

            @Override
            public void keyPressed(KeyEvent e) {
                KeyStroke key = KeyStroke.getKeyStrokeForEvent(e);
                //logger.info("keyevent = " + key);

                if (key.equals(CTRL_BACKSPACE)) {
                    logger.info("CTRL BACK_SPACE PRESSED ================");
                    ctrlBackspace = true;
                }

                // timeKeyPressed 記録
                timeKeyPressed = System.currentTimeMillis();
            }

            @Override
            public void keyReleased(KeyEvent e) {
                // かなキー, 英数キーの2度打ち検出
                KeyStroke key = KeyStroke.getKeyStrokeForEvent(e);
                long lap = System.currentTimeMillis() - timeKeyReleased;

                if (key.equals(KANA)) {
                    if (doubleKana && lap < 300) {
                        processDoubleKey();
                        doubleKana = false;
                    } else {
                        doubleKana = true;
                    }
                } else if (key.equals(EISU)) {
                    if (doubleEisu && lap < 300) {
                        processDoubleKey();
                        doubleEisu = false;
                    } else {
                        doubleEisu = true;
                    }
                } else {
                    doubleKana = false; doubleEisu = false;
                }

                // timeKeyReleased 記録
                timeKeyReleased = System.currentTimeMillis();
            }

            @Override
            public void caretPositionChanged(InputMethodEvent event) {
                logger.info("caretchanged " + event);
            }

            @Override
            public void keyTyped(KeyEvent e) { }
        }

        public void setUndoAction(Action action) {
            undoAction = action;
        }

        public void setRedoAction(Action action) {
            redoAction = action;
        }

        @Override
        public void undoableEditHappened(UndoableEditEvent e) {
//            logger.info("edit = " + e.getEdit());
//            try {
//                AbstractDocument.DefaultDocumentEvent event=(AbstractDocument.DefaultDocumentEvent) e.getEdit();
//                int start = event.getOffset();
//                int len = event.getLength();
//                String text = event.getDocument().getText(start, len);
//                logger.info("edit text = " + text);
//
//            } catch (BadLocationException ex) {
//                logger.error(ex.getMessage());
//            }
//            try {
//                Field field = current.getClass().getSuperclass().getDeclaredField("inProgress");
//                field.setAccessible(true);
//                logger.info("inProgress1 = " + field.get(current));
//                //field.set(this, true);
//
//
//            } catch (NoSuchFieldException | IllegalAccessException ex) {
//                ex.printStackTrace();
//            }

            timer.restart();
            current.addEdit(e.getEdit());
            updateActionStatus();
        }

        private void flush() {
            logger.info("flush");

            timer.stop();
            current.end();
            addEdit(current);
            current = new TextComponentUndoableEdit();
            updateActionStatus();
        }

        /**
         * While timer is running,
         * stop timer, discard current, and then discardAllEdits.
         */
        @Override
        public void discardAllEdits() {
            logger.info("timer running: " + timer.isRunning());
            if (timer.isRunning()) {
                timer.stop();
                current = new TextComponentUndoableEdit();
            }
            super.discardAllEdits();
        }

        /**
         * 指定した UndoableEdit を前の UndoableEdit に merge するかどうか.
         *
         * @param cur TextComponentUndoableEdit contains current DocumentEvent
         * @param last TextComponentUndoableEdit contains last DocumentEvent
         * @return true to merge
         */
        private boolean toMergeEdit(UndoableEdit cur, UndoableEdit last) {
            // last が canUndo でなければ merge しない
            logger.info("last undoable? " + (last ==null? "null" : last.canUndo()));
            if (last == null || !last.canUndo()) { return false; }

            // 両方とも TextComponentUndoableEdit でなければ merge しない
            if (!(cur instanceof TextComponentUndoableEdit)
                || !(last instanceof TextComponentUndoableEdit)) { return false; }

            UndoableEdit curEdit = ((TextComponentUndoableEdit) cur).lastEdit();
            UndoableEdit lastEdit = ((TextComponentUndoableEdit) last).lastEdit();

            // 内容が　DefaultDocumentEvent でなければ merge しない
            if (!(curEdit instanceof AbstractDocument.DefaultDocumentEvent)
                || !(lastEdit instanceof AbstractDocument.DefaultDocumentEvent)) { return false; }

            AbstractDocument.DefaultDocumentEvent curEvent = (AbstractDocument.DefaultDocumentEvent) curEdit;
            AbstractDocument.DefaultDocumentEvent lastEvent = (AbstractDocument.DefaultDocumentEvent) lastEdit;

            try {
                if (curEvent.getType() == DocumentEvent.EventType.REMOVE) {
                    logger.info("curEvent = REMOVE, " + " lastEvent = " + lastEvent.getType());
                    // REMOVE が続いている場合はまとめる
                    if (lastEvent.getType() == DocumentEvent.EventType.REMOVE) { return true; }

                } else {
                    if (lastEvent.getType() == DocumentEvent.EventType.INSERT) {
                        // Alphabet 入力が続いていたらまとめる
                        String curText = curEvent.getDocument().getText(curEvent.getOffset(), curEvent.getLength());
                        String lastText = lastEvent.getDocument().getText(lastEvent.getOffset(), lastEvent.getLength());
                        logger.info("curText = " + curText + " match " + ALPHABET.matcher(curText).matches()
                            + ", lastText = " + lastText + " match " + ALPHABET.matcher(lastText).matches()
                        );
                        if (ALPHABET.matcher(curText).matches() && ALPHABET.matcher(lastText).matches()) { return true; }
                    }
                }
            } catch (BadLocationException ex) {
                logger.error(ex.getMessage());
            }
            return false;
        }

        @Override
        public boolean addEdit(UndoableEdit e) {
            if (toMergeEdit(e, lastEdit())) {
                TextComponentUndoableEdit last = (TextComponentUndoableEdit) lastEdit();
                TextComponentUndoableEdit toMerge = (TextComponentUndoableEdit) e;
                return last.mergeEdit(toMerge.lastEdit());

            } else {
                return super.addEdit(e);
            }
        }

        @Override
        public void undo() {
            logger.info("undo");
            if (canUndo()) { super.undo(); }
            updateActionStatus();
        }

        @Override
        public void redo() {
            if (canRedo()) { super.redo(); }
            updateActionStatus();
        }

        private void updateActionStatus() {
            if (Objects.isNull(undoAction) || Objects.isNull(redoAction)) { return; }
            undoAction.setEnabled(canUndo());
            redoAction.setEnabled(canRedo());
        }
    }

    /**
     * TextComponentUndoManager 用の UndoableEdit.
     */
    private class TextComponentUndoableEdit extends CompoundEdit {
        @Override
        public UndoableEdit lastEdit() {
            return super.lastEdit();
        }

        public int size() {
            return super.edits.size();
        }

        /**
         * この UndoableEdit が保持する DocumentEvent に edit を merge する.
         *
         * @param edit
         * @return
         */
        public boolean mergeEdit(UndoableEdit edit) {
            setInProgress(true);
            boolean ret = addEdit(edit);
            end();
            return ret;
        }

        /**
         * package-private field の inProgress を書き換える.
         *
         * @param inProgress inProgress to set
         */
        public void setInProgress(boolean inProgress) {
            try {
                Field field = getClass().getSuperclass().getDeclaredField("inProgress");
                field.setAccessible(true);
                field.set(this, inProgress);

            } catch (NoSuchFieldException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    private void start() {
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JTextPane pane = new JTextPane();
        TextComponentUndoManager undoManager = new TextComponentUndoManager(pane);
        pane.setEditorKit(new StyledEditorKit());
        pane.getDocument().addUndoableEditListener(undoManager);

        JScrollPane scroll = new JScrollPane(pane);
        frame.getContentPane().add(scroll);

        JButton undoBtn = new JButton();
        Action undo = new AbstractAction("Undo") {
            @Override
            public void actionPerformed(ActionEvent e) { undoManager.undo(); }
        };
        undoBtn.setAction(undo);
        undoManager.setUndoAction(undo);
        undo.setEnabled(false);

        JButton redoBtn = new JButton();
        Action redo = new AbstractAction("Redo") {
            @Override
            public void actionPerformed(ActionEvent e) { undoManager.redo(); }
        };
        redoBtn.setAction(redo);
        undoManager.setRedoAction(redo);
        redo.setEnabled(false);

        ActionMap am = pane.getActionMap();
        am.put("undo", undo);
        am.put("redo", redo);
        InputMap im = pane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        im.put(KeyStroke.getKeyStroke("meta Z"), "undo");
        im.put(KeyStroke.getKeyStroke("shift meta Z"), "redo");

        JToolBar tb = new JToolBar();
        tb.add(undoBtn);
        tb.add(redoBtn);
        frame.getContentPane().add(tb, BorderLayout.NORTH);

        frame.setSize(400, 300);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        //pane.setText("初期文字列");
        //pane.setCaretPosition(pane.getDocument().getLength());
        undoManager.discardAllEdits();
    }

    public static void main(String[] arg) {
        UndoTest app = new UndoTest();
        app.start();
    }
}
