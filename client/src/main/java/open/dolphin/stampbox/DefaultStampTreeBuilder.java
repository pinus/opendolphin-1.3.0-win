package open.dolphin.stampbox;

import open.dolphin.infomodel.IInfoModel;
import open.dolphin.infomodel.ModuleInfoBean;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * StampTree Builder クラス.
 *
 * @author Kazushi Minagawa, Digital Globe, Inc.
 */
public class DefaultStampTreeBuilder extends AbstractStampTreeBuilder {
    /**
     * エディタから発行のスタンプ名
     */
    protected static final String FROM_EDITOR = "エディタから発行...";
    /**
     * rootノードの名前
     */
    private String rootName;
    /**
     * エディタから発行があったかどうかのフラグ
     */
    private boolean hasEditor;
    /**
     * StampTree のルートノード
     */
    private StampTreeNode rootNode;
    /**
     * StampTree のノード
     */
    private StampTreeNode node;
    /**
     * ノードの UserObject になる StampInfo
     */
    private ModuleInfoBean info;
    /**
     * 制御用のリスト
     */
    private LinkedList<StampTreeNode> linkedList;
    /**
     * 生成物
     */
    private List<StampTree> products;
    /**
     * Logger
     */
    private Logger logger;

    public DefaultStampTreeBuilder() {
    }

    /**
     * Returns the product of this builder.
     *
     * @return vector that contains StampTree instances
     */
    @Override
    public List<StampTree> getProduct() {
        return products;
    }

    /**
     * build を開始する.
     */
    @Override
    public void buildStart() {
        products = new ArrayList<>();
        if (logger != null) {
            logger.debug("Build StampTree start");
        }
    }

    /**
     * Root を生成する.
     *
     * @param name   root名
     * @param entity Stamptree の Entity
     */
    @Override
    public void buildRoot(String name, String entity) {

        if (logger != null) {
            logger.debug("Root=" + name);
        }
        linkedList = new LinkedList<>();

        // TreeInfo を 生成し rootNode に保存する
        TreeInfo treeInfo = new TreeInfo();
        treeInfo.setName(name);
        treeInfo.setEntity(entity);
        rootNode = new StampTreeNode(treeInfo);

        hasEditor = false;
        rootName = name;
        linkedList.addFirst(rootNode);
    }

    /**
     * ノードを生成する.
     *
     * @param name ノード名
     */
    @Override
    public void buildNode(String name) {

        if (logger != null) {
            logger.debug("Node=" + name);
        }

        // Node を生成し現在のノードに加える
        node = new StampTreeNode(toXmlText(name));
        getCurrentNode().add(node);

        // このノードを first に加える
        linkedList.addFirst(node);
    }

    /**
     * StampInfo を UserObject にするノードを生成する.
     *
     * @param name     ノード名
     * @param role     role
     * @param entity   エンティティ
     * @param editable 編集可能かどうかのフラグ
     * @param memo     メモ
     * @param id       DB key
     */
    @Override
    public void buildStampInfo(String name,
                               String role,
                               String entity,
                               String editable,
                               String memo,
                               String id) {

        if (logger != null) {
            String sb = name + "," + role + "," + entity + "," + editable + "," + memo + "," + id;
            logger.debug(sb);
        }

        // StampInfo を生成する
        info = new ModuleInfoBean();
        info.setStampName(toXmlText(name));
        info.setStampRole(role);
        info.setEntity(entity);
        if (editable != null) {
            info.setEditable(Boolean.parseBoolean(editable));
        }
        if (memo != null) {
            info.setStampMemo(toXmlText(memo));
        }
        if (id != null) {
            info.setStampId(id);
        }

        // StampInfo から TreeNode を生成し現在のノードへ追加する
        node = new StampTreeNode(info);
        getCurrentNode().add(node);

        // エディタから発行を持っているか
        if (info.getStampName().equals(FROM_EDITOR) && (!info.isSerialized())) {
            hasEditor = true;
            info.setEditable(false);
        }
    }

    /**
     * Node の生成を終了する.
     */
    @Override
    public void buildNodeEnd() {
        if (logger != null) {
            logger.debug("End node");
        }
        linkedList.removeFirst();
    }

    /**
     * Root Node の生成を終了する.
     */
    @Override
    public void buildRootEnd() {

        // エディタから発行...を削除された場合に追加する処置
        if ((!hasEditor) && (getEntity(rootName) != null)) {

            if (getEntity(rootName).equals(IInfoModel.ENTITY_TEXT) || getEntity(rootName).equals(IInfoModel.ENTITY_PATH)) {
                // テキストスタンプとパススタンプにはエディタから発行...はなし
            } else {
                ModuleInfoBean si = new ModuleInfoBean();
                si.setStampName(FROM_EDITOR);
                si.setStampRole(IInfoModel.ROLE_P);
                si.setEntity(getEntity(rootName));
                si.setEditable(false);
                StampTreeNode sn = new StampTreeNode(si);
                rootNode.add(sn);
            }
        }

        // StampTree を生成しプロダクトリストへ加える
        StampTree tree = new StampTree(new StampTreeModel(rootNode));
        products.add(tree);

        if (logger != null) {
            int pCount = products.size();
            logger.debug("End root " + "count=" + pCount);
        }
    }

    /**
     * build を終了する.
     */
    @Override
    public void buildEnd() {

        if (logger != null) {
            logger.debug("Build end");
        }

        // ORCAセットを加える
        boolean hasOrca = false;
        for (StampTree st : products) {
            String entity = st.getTreeInfo().getEntity();
            if (entity.equals(IInfoModel.ENTITY_ORCA)) {
                hasOrca = true;
            }
        }

        if (!hasOrca) {
            TreeInfo treeInfo = new TreeInfo();
            treeInfo.setName(IInfoModel.TABNAME_ORCA);
            treeInfo.setEntity(IInfoModel.ENTITY_ORCA);
            rootNode = new StampTreeNode(treeInfo);
            OrcaTree tree = new OrcaTree(new StampTreeModel(rootNode));
            // products.add(IInfoModel.TAB_INDEX_ORCA, tree);
            products.add(tree); // UserStampBox でソートすることにしたので，どこに入れてもいい
            tree.fetchOrcaInputCd(); // code helper で orca stamp も使うために，build 時にフェッチさせる
            if (logger != null) {
                logger.debug("ORCAセットを加えました");
            }
        }
    }

    /**
     * リストから先頭の StampTreeNode を取り出す.
     */
    private StampTreeNode getCurrentNode() {
        return linkedList.getFirst();
    }
}
