
package com.a2.backend.arvore;

import java.util.ArrayList;
import java.util.List;

public class TreeNode {

    private ItemFormula item;
    private List<TreeNode> filhos;

    public TreeNode(ItemFormula item) {
        this.item = item;
        this.filhos = new ArrayList<>();
    }

    public ItemFormula getItem() {
        return item;
    }

    public List<TreeNode> getFilhos() {
        return filhos;
    }

    public void adicionarFilho(TreeNode filho) {
        filhos.add(filho);
    }
}
