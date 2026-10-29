package com.ganghuitong.port.api.vo.sys;

import com.ganghuitong.port.api.common.TreeNode;
import com.ganghuitong.port.api.entity.sys.SysMenu;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 菜单树节点
 *
 * @author yangxueyong
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class MenuVO extends SysMenu implements TreeNode<MenuVO> {

    /**
     * 子菜单
     */
    private List<MenuVO> children;
}
