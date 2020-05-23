package open.dolphin.stampbox;

import open.dolphin.infomodel.ModuleInfoBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * AspStampTreeBuilder.
 *
 * @author Kazushi Minagawa, Digital Globe, Inc.
 */
public class AspStampTreeBuilder extends AbstractStampTreeBuilder {

    private final Logger logger;
    /**
     * Control staffs
     */
    private StampTreeNode rootNode;
    private StampTreeNode node;
    private ModuleInfoBean info;
    private LinkedList<StampTreeNode> linkedList;
    private List<StampTree> products;

    public AspStampTreeBuilder() {
        logger = LoggerFactory.getLogger(AspStampTreeBuilder.class);
    }

    /**
     * Returns the product of this builder
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

    @Override
    public void buildRoot(String name, String entity) {
        // New root
        if (logger != null) {
            logger.debug("Root=" + name);
        }
        linkedList = new LinkedList<>();

        // TreeInfo を rootNode に保存する
        TreeInfo treeInfo = new TreeInfo();
        treeInfo.setName(name);
        treeInfo.setEntity(entity);
        rootNode = new StampTreeNode(treeInfo);
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

        // ASP Tree なのでエディタから発行を無視する
        if (name.equals("エディタから発行...") && (id == null) && (role.equals("p"))) {
            return;
        }

        info = new ModuleInfoBean();
        info.setStampName(name);
        info.setStampRole(role);
        info.setEntity(entity);
        if (editable != null) {
            info.setEditable(Boolean.parseBoolean(editable));
        }
        if (memo != null) {
            info.setStampMemo(memo);
        }
        if (id != null) {
            info.setStampId(id);
        }

        // StampInfo から TreeNode を生成し現在のノードへ追加する
        node = new StampTreeNode(info);
        getCurrentNode().add(node);
    }

    @Override
    public void buildRootEnd() {

        StampTree tree = new StampTree(new StampTreeModel(rootNode));
        products.add(tree);

        if (logger != null) {
            int pCount = products.size();
            logger.debug("End root " + "count=" + pCount);
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

    @Override
    public void buildEnd() {
        if (logger != null) {
            logger.debug("Build end");
        }
    }

    private StampTreeNode getCurrentNode() {
        return linkedList.getFirst();
    }
}
