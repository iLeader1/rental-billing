# Rental Billing System (租务计费系统)

这是一个基于 Spring Boot 的企业级租赁管理后端系统，旨在为房东或公寓管理者提供自动化、公平的水电煤气费分摊解决方案。

## 🌟 核心特性

- **房屋管理**：支持房源的录入、查询及安全删除校验。
- **租客管理**：
  - 支持租客入住（Check-in）与退房（Check-out）流程。
  - **高级列表查询**：支持基于插件的分页查询，并可根据姓名、入住日期、退房日期进行动态升降序排列。
  - **复合排序**：姓名排序时自动引入入住日期作为次要排序条件，确保数据展示稳定性。
- **自动化计费与分摊**：
  - **人·天 (Person-Days) 算法**：基于租客在账期内实际居住天数进行精确分摊。
  - **水费特殊逻辑**：自动提取 5 元固定垃圾费，由账期内去重后的租客均摊，剩余部分再按天数分摊。
  - **财务严谨性**：全流程使用 `BigDecimal` 计算，并包含“尾差补偿”逻辑，确保分摊总额与原始账单完全一致。
- **企业级架构**：
  - 统一 API 响应规范 (`ApiResponse`)。
  - 全局异常拦截处理。
  - 详细的分摊历史追踪（记录每位租客实际计费的起止日期）。

## 🛠 技术栈

- **后端框架**：Spring Boot 3.x
- **持久层**：MyBatis-Plus (支持 Lambda 构造器)
- **数据库**：MySQL 8.0+
- **工具库**：Lombok, Jackson
- **API 文档**：Swagger / OpenAPI 3.0
- **构建工具**：Maven

## 🚀 快速启动

### 1. 环境要求
- JDK 21+
- MySQL 8.0
- Maven 3.9+

### 2. 数据库配置
请在 MySQL 中创建数据库并执行以下初始化脚本：

```sql
-- 房屋表
CREATE TABLE house (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    house_name VARCHAR(50) NOT NULL,
    address VARCHAR(255),
    created_at DATETIME
);

-- 租客表
CREATE TABLE tenant (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    house_id BIGINT NOT NULL,
    name VARCHAR(50) NOT NULL,
    phone VARCHAR(20),
    check_in_date DATE NOT NULL,
    check_out_date DATE
);

-- 原始账单表
CREATE TABLE utility_bill (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    house_id BIGINT NOT NULL,
    utility_type VARCHAR(20) NOT NULL, -- WATER, ELECTRICITY, GAS
    billing_start_date DATE NOT NULL,
    billing_end_date DATE NOT NULL,
    total_amount DECIMAL(10,2) NOT NULL,
    total_usage DECIMAL(10,2),
    remark VARCHAR(255),
    created_at DATETIME
);

-- 分摊明细表
CREATE TABLE tenant_bill_allocation (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    bill_id BIGINT NOT NULL,
    tenant_id BIGINT NOT NULL,
    tenant_name VARCHAR(50),
    residence_days INT,
    stay_start_date DATE,
    stay_end_date DATE,
    total_person_days INT,
    allocated_amount DECIMAL(10,2)
);
```

### 3. 应用配置
修改 `src/main/resources/application.yaml` 中的数据库连接信息：
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/your_db_name
    username: your_username
    password: your_password
```

### 4. 运行
```bash
mvn clean compile
mvn spring-boot:run
```

## 📖 核心业务逻辑说明

### 1. 计费分摊公式
对于普通费用（电费、燃气费）：
$$每人分摊 = 总金额 	imes \frac{个人计费天数}{所有租客总计费天数}$$

### 2. 水费特殊分摊 (包含垃圾费)
- **固定部分**：从总额中提取 5.00 元作为垃圾费。
- **分摊逻辑**：
  1. 统计账期内去重后的租客总数 $N$。
  2. 每人承担垃圾费 = $5.00 / N$（仅在租客第一个居住周期扣除）。
  3. 剩余金额（总额 - 5.00）按“人·天”比例分摊。

### 3. 动态排序说明
在查询租客列表时，支持以下参数：
- `prop`: 排序字段 (`name`, `checkInDate`, `id`)。
- `order`: 排序方向 (`ascending`, `descending`)。
- **注意**：当使用 `name` 排序时，系统会自动追加 `checkInDate ASC` 作为次要排序规则。

## 接口文档
项目启动后访问：`http://localhost:8080/swagger-ui/index.html`
