package com.bigyun.demo.domain;

import com.bigyun.common.core.annotation.Excel;
import com.bigyun.common.core.web.domain.BaseEntity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 社区示例数据对象。
 *
 * @author bigyun
 */
public class DemoItem extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long itemId;

    @Excel(name = "Item code")
    private String itemCode;

    @Excel(name = "Item name")
    private String itemName;

    @Excel(name = "Category")
    private String category;

    @Excel(name = "Status")
    private String status;

    public Long getItemId()
    {
        return itemId;
    }

    public void setItemId(Long itemId)
    {
        this.itemId = itemId;
    }

    @NotBlank(message = "示例编码不能为空")
    @Size(max = 64, message = "示例编码长度不能超过64个字符")
    public String getItemCode()
    {
        return itemCode;
    }

    public void setItemCode(String itemCode)
    {
        this.itemCode = itemCode;
    }

    @NotBlank(message = "示例名称不能为空")
    @Size(max = 100, message = "示例名称长度不能超过100个字符")
    public String getItemName()
    {
        return itemName;
    }

    public void setItemName(String itemName)
    {
        this.itemName = itemName;
    }

    @Size(max = 64, message = "分类长度不能超过64个字符")
    public String getCategory()
    {
        return category;
    }

    public void setCategory(String category)
    {
        this.category = category;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }
}

