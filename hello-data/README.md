# 数据库表数据生成器
目录结构
- config--配置目录，保存所有yml配置的映射类
- proxy--数据生成的代理实现，根据配置的策略动态生成列数据生成器
- strategy--数据生成策略，有6种(auto-inc,default,fixed-value,dict-value,rand-ele,rand-table-ele)

核心类：
- ColDataGenerator:列数据生成器，根据列名生成列数据
- DataGenerator:表数据生成器，组合列数据生成器，遍历配置信息生成表数据
- App：启动类

配置文件：
- db.setting:数据库连接配置
- generate.yml:表数据生成配置