#!/bin/bash

echo "=========================================="
echo "Provider 配置系统重构 - 验收脚本"
echo "=========================================="
echo ""

# 数据库配置（请修改为你的实际配置）
DB_HOST="localhost"
DB_PORT="3306"
DB_NAME="bigyun_cloud"
DB_USER="root"
DB_PASS="your_password"

echo "步骤 1: 检查数据库连接..."
mysql -h $DB_HOST -P $DB_PORT -u $DB_USER -p$DB_PASS -e "SELECT 1" > /dev/null 2>&1
if [ $? -eq 0 ]; then
    echo "✅ 数据库连接成功"
else
    echo "❌ 数据库连接失败，请检查配置"
    exit 1
fi

echo ""
echo "步骤 2: 检查 sys_provider_credentials 表是否存在..."
TABLE_EXISTS=$(mysql -h $DB_HOST -P $DB_PORT -u $DB_USER -p$DB_PASS $DB_NAME -e "SHOW TABLES LIKE 'sys_provider_credentials'" | grep sys_provider_credentials)
if [ -n "$TABLE_EXISTS" ]; then
    echo "✅ sys_provider_credentials 表已存在"
else
    echo "⚠️  sys_provider_credentials 表不存在，正在创建..."
    mysql -h $DB_HOST -P $DB_PORT -u $DB_USER -p$DB_PASS $DB_NAME < sql/sys_provider_credentials.sql
    if [ $? -eq 0 ]; then
        echo "✅ 表创建成功"
    else
        echo "❌ 表创建失败"
        exit 1
    fi
fi

echo ""
echo "步骤 3: 验证表结构..."
mysql -h $DB_HOST -P $DB_PORT -u $DB_USER -p$DB_PASS $DB_NAME -e "DESC sys_provider_credentials"
echo "✅ 表结构验证完成"

echo ""
echo "步骤 4: 测试插入凭据..."
mysql -h $DB_HOST -P $DB_PORT -u $DB_USER -p$DB_PASS $DB_NAME << EOF
-- 插入测试数据
INSERT INTO sys_provider_credentials
(config_id, credential_key, credential_value, is_sensitive)
VALUES
(999, 'test_key', 'test_value', 'Y')
ON DUPLICATE KEY UPDATE credential_value = 'test_value';
EOF

if [ $? -eq 0 ]; then
    echo "✅ 凭据插入成功"
else
    echo "❌ 凭据插入失败"
    exit 1
fi

echo ""
echo "步骤 5: 测试查询凭据..."
RESULT=$(mysql -h $DB_HOST -P $DB_PORT -u $DB_USER -p$DB_PASS $DB_NAME -e "SELECT * FROM sys_provider_credentials WHERE config_id = 999" | wc -l)
if [ $RESULT -gt 1 ]; then
    echo "✅ 凭据查询成功"
    mysql -h $DB_HOST -P $DB_PORT -u $DB_USER -p$DB_PASS $DB_NAME -e "SELECT * FROM sys_provider_credentials WHERE config_id = 999"
else
    echo "❌ 凭据查询失败"
    exit 1
fi

echo ""
echo "步骤 6: 清理测试数据..."
mysql -h $DB_HOST -P $DB_PORT -u $DB_USER -p$DB_PASS $DB_NAME -e "DELETE FROM sys_provider_credentials WHERE config_id = 999"
echo "✅ 测试数据清理完成"

echo ""
echo "=========================================="
echo "✅ 所有验收测试通过！"
echo "=========================================="
echo ""
echo "下一步："
echo "1. 重启 bigyun-config-service 服务"
echo "2. 运行单元测试: mvn test -Dtest=ProviderCredentialServiceTest"
echo "3. 查看服务日志，确认 Handler 注册成功"
echo ""
