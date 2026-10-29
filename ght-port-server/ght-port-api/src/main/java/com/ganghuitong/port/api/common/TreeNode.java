package com.ganghuitong.port.api.common;

import java.util.List;

/**
 * 树形节点约定，用于通用树构建工具
 *
 * @param <T> 节点类型
 * @author yangxueyong
 */
public interface TreeNode<T> {

    /**
     * 获取节点ID
     *
     * @return 节点ID
     */
    String getId();

    /**
     * 获取父节点ID
     *
     * @return 父节点ID
     */
    String getParentId();

    /**
     * 设置子节点列表
     *
     * @param children 子节点列表
     */
    void setChildren(List<T> children);
}
