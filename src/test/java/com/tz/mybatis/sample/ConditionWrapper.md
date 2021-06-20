> **警告:**
>
> **不支持以及不赞成在 RPC 调用中把 Wrapper 进行传输**
>
> 1. **wrapper 很重**
> 2. **传输 wrapper 可以类比为你的 controller 用 map 接收值(开发一时爽,维护火葬场)**
> 3. **正确的 RPC 调用姿势是写一个 DTO 进行传输,被调用方再根据 DTO 执行相应的操作**
> 4. **我们拒绝接受任何关于 RPC 传输 Wrapper 报错相关的 issue 甚至 pr**



说明

> - 以下出现的第一个入参boolean condition表示该条件是否加入最后生成的sql中，例如：query.like(StringUtils.isNotBlank(name), Entity::getName, name) .eq(age!=null && age >= 0, Entity::getAge, age)
> - 以下代码块内的多个方法均为从上往下补全个别boolean类型的入参,默认为true
> - 以下出现的泛型Param均为Wrapper的子类实例(均具有AbstractWrapper的所有方法)
> - 以下方法在入参中出现的R为泛型,在普通wrapper中是String,在LambdaWrapper中是函数(例:Entity::getId,Entity为实体类,getId为字段id的getMethod)
> - 以下方法入参中的R column均表示数据库字段,当R具体类型为String时则为数据库字段名(字段名是数据库关键字的自己用转义符包裹!)!而不是实体类数据字段名!!!,另当R具体类型为SFunction时项目runtime不支持eclipse自家的编译器!!!
> - 以下举例均为使用普通wrapper,入参为Map和List的均以json形式表现!
> - 使用中如果入参的Map或者List为空,则不会加入最后生成的sql中!!!



# 条件构造包装类结构

```
- AbstractWrapper
- | - QueryWrapper
- | - | - LambdaQueryWrapper
- | - UpdateWrapper
- | - | - LambdaUpdateWrapper
```

## AbstractWrapper

### allEq 全部相等

- allEq(Map<R, V> params) 
- allEq(Map<R, V> params, boolean null2IsNull)
- allEq(boolean condition, Map<R, V> params, boolean null2IsNull)
  - `condition`：表示该条件是否加入最后生成的sql中
  - `params` : `key`为数据库字段名,`value`为字段值
  - `null2IsNull` : 为`true`则在`map`的`value`为`null`时调用 `isNull` 方法,为`false`时则忽略`value`为`null`的
- 示例
  - `allEq({id:1,name:"老王",age:null})--->id = 1 and name = '老王' and age is null`
  - `allEq({id:1,name:"老王",age:null}, false)--->id = 1 and name = '老王'`



- allEq(BiPredicate<R, V> filter, Map<R, V> params)

- allEq(BiPredicate<R, V> filter, Map<R, V> params, boolean null2IsNull)

- allEq(boolean condition, BiPredicate<R, V> filter, Map<R, V> params, boolean null2IsNull) 

  - `filter` : 过滤函数,是否允许字段传入比对条件中
  - `codition`， `params` 与 `null2IsNull` : 同上

- 示例

  - `allEq((k,v) -> k.indexOf("a") >= 0, {id:1,name:"老王",age:null})--->name = '老王' and age is null`

  - `allEq((k,v) -> k.indexOf("a") >= 0, {id:1,name:"老王",age:null}, false)--->name = '老王'`

    

### eq 相等

- eq(R column, Object val)
- eq(boolean condition, R column, Object val)
  - `condition`：条件
  - `column`：列名
  - `val`：值

### ne 不等

- ne(R column, Object val)
- ne(boolean condition, R column, Object val)
  - 参数与 eq 相同

### gt 大于

- gt(R column, Object val)
- gt(boolean condition, R column, Object val)

### ge 大于等于

- ge(R column, Object val)
- ge(boolean condition, R column, Object val)

### lt 小于

- lt(R column, Object val)
- lt(boolean condition, R column, Object val)

### le 小于等于

- le(R column, Object val)
- le(boolean condition, R column, Object val)

### between 之间

- between(R column, Object val1, Object val2)
- between(boolean condition, R column, Object val1, Object val2)

### notBetween 不在 之间

- notBetween(R column, Object val1, Object val2)
- notBetween(boolean condition, R column, Object val1, Object val2)

### like 模糊 （like '%abc%'）

- like(R column, Object val)
- like(boolean condition, R column, Object val)
- 示例：
  - `like("name", "王")--->name like '%王%'`

### notLike  NOT LIKE

- notLike(R column, Object val)
- notLike(boolean condition, R column, Object val)

### likeLeft 左模糊 like '%abc'

- likeLeft(R column, Object val)
- likeLeft(boolean condition, R column, Object val)

### likeRight 右模糊  like 'abc%'

- likeRight(R column, Object val)
- likeRight(boolean condition, R column, Object val)

### isNull 为空

- isNull(R column)
- isNull(boolean condition, R column)

####  isNotNull 不为空

- isNotNull(R column)
- isNotNull(boolean condition, R column)

### in 在某些值内

- in(R column, Collection<?> value)
- in(boolean condition, R column, Collection<?> value)
- in(R column, Object... values)
- in(boolean condition, R column, Object... values)

### notIn 不在某些值内

- notIn(R column, Collection<?> value)
- notIn(boolean condition, R column, Collection<?> value)
- notIn(R column, Object... values)
- notIn(boolean condition, R column, Object... values)

### inSql 在sql查找的值内

- inSql(R column, String inValue)
- inSql(boolean condition, R column, String inValue)
- 示例
  - 字段 IN ( sql语句 )
  -  `inSql("age", "1,2,3,4,5,6")`--->`age in (1,2,3,4,5,6)`
  -  `inSql("id", "select id from table where id < 3")`--->`id in (select id from table where id < 3)`

### notInSql 不在sql查找的值内

- notInSql(R column, String inValue)
- notInSql(boolean condition, R column, String inValue)
- 示例
  - 等同于 inSql

### groupBy 分组

- groupBy(R... columns)
- groupBy(boolean condition, R... columns)
- 示例
  -  `groupBy("id", "name")`--->`group by id,name`

### orderByAsc 正序排序

- orderByAsc(R... columns)
- orderByAsc(boolean condition, R... columns)
- 示例
  -  `orderByAsc("id", "name")`--->`order by id ASC,name ASC`

### orderByDesc 倒序排序

- orderByDesc(R... columns)
- orderByDesc(boolean condition, R... columns)
- 示例
  -  `orderByDesc("id", "name")`--->`order by id DESC,name DESC`

### orderBy 排序

- orderBy(boolean condition, boolean isAsc, R... columns)
- 示例
  -  `orderBy(true, true, "id", "name")`--->`order by id ASC,name ASC`

### having

- having(String sqlHaving, Object... params)
- having(boolean condition, String sqlHaving, Object... params)
- 示例
  - `having("sum(age) > 10")`--->`having sum(age) > 10`
  -  `having("sum(age) > {0}", 11)`--->`having sum(age) > 11`

### func （出现分支条件下，方便链式调用完整）

- func(Consumer<Children> consumer)
- func(boolean condition, Consumer<Children> consumer)
- 示例
  - `func(i -> if(true) {i.eq("id", 1)} else {i.ne("id", 1)})`

### or 拼接Or条件

- or()

- or(boolean condition)

- > 注意事项:
  >
  > 主动调用or表示紧接着下一个方法不是用and连接!(不调用or则默认为使用and连接)

- 示例

  - `eq("id",1).or().eq("name","老王")--->id = 1 or name = '老王'`

- or(Consumer<Param> consumer)

- or(boolean condition, Consumer<Param> consumer)

- 示例

  - `or(i -> i.eq("name", "李白").ne("status", "活着"))`--->`or (name = '李白' and status <> '活着')`

### and 拼接and 条件

- and(Consumer<Param> consumer)
- and(boolean condition, Consumer<Param> consumer)

### nested 嵌套

- nested(Consumer<Param> consumer)
- nested(boolean condition, Consumer<Param> consumer)
- 注意：前后不会添加 and 或者 or 连接关键字
- 示例
  - `nested(i -> i.eq("name", "李白").ne("status", "活着"))--->(name = '李白' and status <> '活着')`

### apply 拼接sql

- apply(String applySql, Object... params)

- apply(boolean condition, String applySql, Object... params)

- > 注意事项:
  >
  > 该方法可用于数据库**函数** 动态入参的`params`对应前面`applySql`内部的`{index}`部分.这样是不会有sql注入风险的,反之会有!

- 示例

  -  `apply("id = 1")`--->`id = 1`
  -  `apply("date_format(dateColumn,'%Y-%m-%d') = '2008-08-08'")`--->`date_format(dateColumn,'%Y-%m-%d') = '2008-08-08'")`
  -  `apply("date_format(dateColumn,'%Y-%m-%d') = {0}", "2008-08-08")`--->`date_format(dateColumn,'%Y-%m-%d') = '2008-08-08'")`

### exists 是否存在

- exists(String existsSql)
- exists(boolean condition, String existsSql)

### notExists 是否不存在

- notExists(String notExistsSql)
- notExists(boolean condition, String notExistsSql)

### last 直接把sql放到语句最后

- last(String lastSql)

- last(boolean condition, String lastSql)

- > 注意事项:
  >
  > 只能调用一次,多次调用以最后一次为准 有sql注入的风险,请谨慎使用

- 示例

  - `last("limit 1")`

## QueryWrapper 查询包装器

### select 设置查询的字段

- select(String... sqlSelect)

- select(Predicate<TableFieldInfo> predicate)

- select(Class<T> entityClass, Predicate<TableFieldInfo> predicate)

- > 说明:
  >
  > 以上方法分为两类.
  > 第二类方法为:过滤查询字段(主键除外),入参不包含 class 的调用前需要wrapper内的entity属性有值! 这两类方法重复调用以最后一次为准

- 示例

  -  `select("id", "name", "age")`
  -  `select(i -> i.getProperty().startsWith("test"))`

## UpdateWrapper 更新包装器

### set 设置值

- set(String column, Object val)
- set(boolean condition, String column, Object val)
- 示例
  -  `set("name", "老李头")`
  -  `set("name", "")`--->数据库字段值变为**空字符串**
  -  `set("name", null)`--->数据库字段值变为`null`

### setSql 以自定义sql设置值

- setSql(String sql)
- 示例
  - `setSql("name = '老李头'")`

