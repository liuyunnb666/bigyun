package com.bigyun.provider.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.bigyun.common.core.web.domain.BaseEntity;

/**
 * Provider元数据实体类
 *
 * 功能说明：
 * 1. 存储枚举定义到数据库，实现枚举与数据库的双向绑定
 * 2. 支持两种元数据类型：
 *    - config_type: 配置类型（如storage、llm、tts等）
 *    - provider_code: 服务商代码（如local、aliyun-oss、openai-gpt等）
 * 3. 应用启动时，ProviderEnumManager会自动同步枚举到此表
 *
 * 使用场景：
 * - 前端动态获取所有支持的配置类型和服务商列表
 * - 配置管理界面的下拉选项数据源
 * - 验证配置的合法性
 *
 * @author BigYun
 * @date 2026-05-21
 */
@TableName("sys_provider_meta")
public class ProviderMeta extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 元数据ID（主键，自增） */
    @TableId(value = "meta_id", type = IdType.AUTO)
    private Long metaId;

    /** 元数据类型：config_type(配置类型) / provider_code(服务商代码) */
    private String metaType;

    /** 元数据代码（如：storage、llm、local、aliyun-oss等） */
    private String metaCode;

    /** 元数据名称（如：对象存储、大语言模型、本地存储、阿里云OSS等） */
    private String metaName;

    /** 元数据描述（详细说明） */
    private String metaDescription;

    /** 父级代码（当meta_type为provider_code时，此字段关联对应的config_type） */
    private String parentCode;

    /** 状态：0=正常 1=停用 */
    private String status;

    /** 排序序号（用于前端展示排序） */
    private Integer sortOrder;

    /** 搜索值（BaseEntity继承字段，数据库表中不存在，需要排除） */
    @TableField(exist = false)
    private String searchValue;

    /** 请求参数（BaseEntity继承字段，数据库表中不存在，需要排除） */
    @TableField(exist = false)
    private java.util.Map<String, Object> params;

    public Long getMetaId() {
        return metaId;
    }

    public void setMetaId(Long metaId) {
        this.metaId = metaId;
    }

    public String getMetaType() {
        return metaType;
    }

    public void setMetaType(String metaType) {
        this.metaType = metaType;
    }

    public String getMetaCode() {
        return metaCode;
    }

    public void setMetaCode(String metaCode) {
        this.metaCode = metaCode;
    }

    public String getMetaName() {
        return metaName;
    }

    public void setMetaName(String metaName) {
        this.metaName = metaName;
    }

    public String getMetaDescription() {
        return metaDescription;
    }

    public void setMetaDescription(String metaDescription) {
        this.metaDescription = metaDescription;
    }

    public String getParentCode() {
        return parentCode;
    }

    public void setParentCode(String parentCode) {
        this.parentCode = parentCode;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }
}
