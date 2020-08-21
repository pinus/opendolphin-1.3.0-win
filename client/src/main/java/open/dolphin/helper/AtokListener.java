package open.dolphin.helper;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import java.awt.event.InputMethodEvent;
import java.awt.event.InputMethodListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.text.AttributedCharacterIterator;
import java.text.CharacterIterator;

/**
 * TextComponentUndoManager で使う ATOK のリスナ. Ctrl-Del の確定アンドゥ, かな/英数キー2度打ちに対応.
 *
 * @author pns
 */
public class AtokListener implements KeyListener, InputMethodListener {
    private Logger logger = LoggerFactory.getLogger(AtokListener.class);

    // キーストローク
    private static KeyStroke CTRL_BACKSPACE = KeyStroke.getKeyStroke("ctrl pressed BACK_SPACE");
    private static KeyStroke KANA = KeyStroke.getKeyStroke("released KATAKANA");
    private static KeyStroke EISU = KeyStroke.getKeyStroke("released ALPHANUMERIC");
    // TextComponent
    JTextComponent textComponent;
    // UndoManager
    TextComponentUndoManager undoManager;
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

    public AtokListener(JTextComponent tc, TextComponentUndoManager manager) {
        textComponent = tc;
        undoManager = manager;
    }

    @Override
    public void inputMethodTextChanged(InputMethodEvent event) {
        long timeFromImeTextChange = System.currentTimeMillis() - timeImeTextChanged;
        long timeFromKeyPress = System.currentTimeMillis() - timeKeyPressed;

        // ctrl-backspace は, 押してすぐ入ってこなかったら無視
        ctrlBackspace = ctrlBackspace && timeFromKeyPress < 100;

        // 確定アンドゥは commit されたら終了
        if (isInKakuteiUndo && event.getCommittedCharacterCount() > 0) {
            logger.debug("Kakutei-undo done: " + event.getCommittedCharacterCount());
            isInKakuteiUndo = false;
        }

        // 確定アンドゥ 1回目
        if (ctrlBackspace) {
            logger.debug("Kakutei-undo start");
            undoManager.undo();
            ctrlBackspace = false;
            isInKakuteiUndo = true;

        } else if (isInKakuteiUndo && timeFromImeTextChange < 10) {
            // 2回目以降の ctrl-backspace キーは ATOK に取られて検出できないが，
            // timeFromTextChange が非常に短く入ってくるので検出できる
            logger.debug("Kakutei-undo cont'd: " + timeFromImeTextChange);
            undoManager.undo();
        }

        // timeImeTextChanged 記録
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

    /**
     * かな/英数２度打ちプロセス.
     */
    private void processDoubleKey() {
        // ２度打ちは KeyReleased でしか検出できないので, inputMethodTextChanged の後の処理になる
        if (System.currentTimeMillis() - timeImeTextChanged > 300) { return; }

        try {
            // かなキー２度打ちの処理
            if (doubleKana) {
                // 再変換元の文字列の最後を検出
                int pos = textComponent.getCaretPosition();
                int end = pos - textInProcess.length();

                // end から逆にたどって, alphanumeric or - 以外の文字が出てくるところを検出
                int start = end;
                while (start-- > 0) {
                    char c = textComponent.getText(start, 1).charAt(0);
                    logger.debug(start + ": " + c);
                    if (!StringTool.isHanakuLower(c) && !StringTool.isHankakuUpper(c)
                        && !StringTool.isHankakuNumber(c) && c != '-') {
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
                logger.debug("double eisu detected: " + textCommitted + "/"+ textInProcess);
                if (textCommitted.equals(textInProcess)) {
                    // flush すると "tうぃってrtwitter" の状態になる
                    undoManager.flush();
                    // これを２回 undo すると "twitter" → "tうぃってr" の順に消える
                    undoManager.undo();
                    undoManager.undo();
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
        if (key.equals(CTRL_BACKSPACE)) {
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
    public void caretPositionChanged(InputMethodEvent event) { }

    @Override
    public void keyTyped(KeyEvent e) { }
}
