package open.dolphin.impl.labrcv;

import open.dolphin.client.ClientContext;
import open.dolphin.delegater.LaboDelegater;
import open.dolphin.infomodel.LaboImportSummary;

import javax.swing.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * LaboTestFileTransferHandler.
 *
 * @author kazushi Minagawa
 */
class LaboTestFileTransferHandler extends TransferHandler {
    private static final long serialVersionUID = 2942768324728994019L;

    private final DataFlavor fileFlavor;
    private final LaboTestImporter context;
    private final LinkedList<List<File>> queue;

    public LaboTestFileTransferHandler(LaboTestImporter ctx) {
        fileFlavor = DataFlavor.javaFileListFlavor;
        context = ctx;
        queue = new LinkedList<>();
        start();
    }

    private void start() {
        ImportThread importThread = new ImportThread();
        importThread.start();
    }

    @Override
    public boolean importData(JComponent c, Transferable t) {

        if (!canImport(c, t.getTransferDataFlavors())) {
            return false;
        }

        try {
            if (hasFileFlavor(t.getTransferDataFlavors())) {
                List<File> files = (List<File>) t.getTransferData(fileFlavor);
                List<File> xmlFiles = new ArrayList<>(files.size());

                files.stream().filter(file -> !file.isDirectory() && file.getName().endsWith(".xml")).forEach(xmlFiles::add);
                if (!xmlFiles.isEmpty()) {
                    addFiles(xmlFiles);
                }

                return true;
            }

        } catch (UnsupportedFlavorException | IOException ex) {
            ex.printStackTrace(System.err);
        }
        return false;
    }

    @Override
    public boolean canImport(JComponent c, DataFlavor[] flavors) {
        return hasFileFlavor(flavors);
    }

    private boolean hasFileFlavor(DataFlavor[] flavors) {
        for (DataFlavor flavor : flavors) {
            if (fileFlavor.equals(flavor)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Queueへドロップされたファイルを加える.
     *
     * @param xmlFiles ドロップされたファイルのリスト
     */
    public synchronized void addFiles(List<File> xmlFiles) {
        queue.addLast(xmlFiles);
        notify(); // wait() 解除
    }

    /**
     * Queueからファイルリストを取り出す.
     *
     * @return ドロップされたファイルのリスト
     */
    public synchronized List<File> getFiles() {

        while (queue.isEmpty()) {
            try {
                wait();
            } catch (Exception e) {
            }
        }
        return queue.removeFirst(); // last in first out
    }

    /**
     * ファイルをパースしデータベースへ登録するコンシューマスレッドクラス.
     */
    private class ImportThread extends Thread {

        @Override
        public void run() {
            while (!interrupted()) {
                try {
                    List<File> files = getFiles(); // ここで wait()

                    SwingUtilities.invokeLater(() -> context.getProgressBar().setIndeterminate(true));

                    LaboModuleBuilder builder = new LaboModuleBuilder();
                    builder.setEncoding(ClientContext.getString("laboTestImport.mmlFile.encoding")); // UTF-8
                    builder.setLaboDelegater(new LaboDelegater());

                    final List<LaboImportSummary> result = builder.build(files);
                    SwingUtilities.invokeLater(() -> {
                        context.getProgressBar().setIndeterminate(false);
                        context.getProgressBar().setValue(0);
                        context.getTableModel().addRows(result);
                        context.updateCount();
                    });

                } catch (Exception e) {
                    e.printStackTrace(System.err);
                }
            }
        }
    }
}
