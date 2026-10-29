package com.ganghuitong.port.api.util;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.ganghuitong.port.api.common.TreeNode;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 通用树形结构构建工具
 *
 * @author yangxueyong
 */
public final class TreeUtil {

    /**
     * 私有构造，禁止实例化
     */
    private TreeUtil() {
    }

    /**
     * 将平铺列表构建为树形结构（保持原列表顺序）
     *
     * @param nodes  平铺节点列表
     * @param rootId 根节点的父ID
     * @param <T>    节点类型
     * @return 顶层节点列表（子节点已挂载）
     */
    public static <T extends TreeNode<T>> List<T> build(List<T> nodes, String rootId) {
        List<T> roots = new ArrayList<>();
        if (CollUtil.isEmpty(nodes)) {
            return roots;
        }
        Map<String, List<T>> childrenMap = new LinkedHashMap<>();
        for (T node : nodes) {
            childrenMap.computeIfAbsent(StrUtil.nullToEmpty(node.getParentId()), k -> new ArrayList<>()).add(node);
        }
        for (T node : nodes) {
            node.setChildren(childrenMap.getOrDefault(node.getId(), new ArrayList<>()));
            if (StrUtil.equals(rootId, node.getParentId())) {
                roots.add(node);
            }
        }
        return roots;
    }
}
