自动任务包含CRUD操作，实现可以参考tech.wetech.flexmodel.infrastructure.persistence.DataFmRepository里面的方法，以下为DSL定义：
// 新增记录
{
"subType": "insert_record", // 必填
"datasourceName": "example_datasource", // 必填
"modelName": "example_model", // 必填
"document": [
{
"field": "field1",
"value":"value1${testaaa}"
}
],
"inputPath": "arrayData", // 非必填，新增多条记录时使用
"resultPath": "affectedRows" // 非必填，新增结果存放路径
}
// 更新记录
{
"subType": "update_record",
"datasourceName": "example_datasource",
"modelName": "example_model",
"document": [
{
"field": "field1",
"value":"value1${testaaa}"
}
],
"inputPath": "arrayData", // 非必填，更新多条记录时使用
"filter": "{ \"field\": { \"_eq\": \"value\" } }", // 过滤条件，非必填
"resultPath": "affectedRows"
}
// 删除记录
{
"subType": "delete_record", // 必填
"datasourceName": "example_datasource", // 必填
"modelName": "example_model",
"filter": "{ \"field\": { \"_eq\": \"value\" } }",// 过滤条件，非必填
"resultPath": "affectedRows" // 删除结果存放路径
}
// 查询记录
{
"subType": "query_record", // 必填
"datasourceName": "example_datasource", // 必填
"modelName": "example_model", // 必填
"filter": "{ \"field\": { \"_eq\": \"value\" } }", // 过滤条件，非必填
"sort": [ // 排序，非必填
{
"field": "createdAt",
"order": "desc"
}
],
"page":1, // 第几页，非必填，和size同时存在时生效
"size":10, // 每页大小，非必填，和page同时存在时生效
"resultPath": "records" // 查询结果存放路径
}

// 其中document需要支持路径语法，已经存在 tech.wetech.flexmodel.shared.utils.JsonPathUtils 工具类，可以直接调用
// 例如：
// 进行解析
// String value = JsonPathUtils.evaluateJsonPath("test.user", data);
// 判断是否为路径表达式
// boolean isJsonPath = JsonPathUtils.isJsonPath("test.user");

// 以下为完整的flow定义格式示例
{"flowElementList":[{"key":"start-node","type":2,"incoming":[],"outgoing":["SequenceFlow_mgoksf9skg7m1c"],"properties":{"positionX":-64,"positionY":16}},{"key":"end-node","type":3,"incoming":["SequenceFlow_mgolxly85affbh"],"outgoing":[],"properties":{"positionX":1584,"positionY":16}},{"key":"serviceTask_mgokolh6ottxgz","type":5,"incoming":["SequenceFlow_mgoksf9skg7m1c"],"outgoing":["SequenceFlow_mgokv2z5j6tju7"],"properties":{"subType":"insert_record","datasourceName":"system","modelName":"Teacher","document":[{"field":"teacherName","value":"张三丰"},{"field":"subject","value":"太极"}],"resultPath":"teacherAffectedRows","name":"新增教师张三丰","positionX":96,"positionY":16}},{"key":"serviceTask_mgokspzb8h72r0","type":5,"incoming":["SequenceFlow_mgokv2z5j6tju7"],"outgoing":["SequenceFlow_mgokxqixlnssvn"],"properties":{"subType":"update_record","datasourceName":"system","modelName":"Teacher","document":[{"field":"subject","value":"太极张三丰"}],"filter":"{\"teacherName\":{\"_eq\":\"张三丰\"}}","resultPath":"student","name":"更新教师张三丰","positionX":320,"positionY":16}},{"key":"serviceTask_mgokvj54kxi9pj","type":5,"incoming":["SequenceFlow_mgokxqixlnssvn"],"outgoing":["SequenceFlow_mgokyc4xwquno6"],"properties":{"subType":"query_record","datasourceName":"system","modelName":"Student","filter":"","sort":"[{\"field\":\"id\",order:\"desc\"}]","page":1,"size":1000,"resultPath":"students","name":"查询学生","positionX":528,"positionY":16}},{"key":"serviceTask_mgoky18vc65fea","type":5,"incoming":["SequenceFlow_mgokyc4xwquno6"],"outgoing":["SequenceFlow_mgolse1rjjms9t"],"properties":{"subType":"insert_record","inputPath":"students","datasourceName":"system","modelName":"Student","document":[{"field":"studentName","value":"${studentName}2"},{"field":"gender","value":"${gender}"},{"field":"interest","value":"${interest}"},{"field":"age","value":"${age}"},{"field":"classId","value":"${classId}"}],"resultPath":"sutndent2AffectedRows","name":"新增学生","positionX":768,"positionY":16}},{"key":"serviceTask_mgoli5cpsdyy77","type":5,"incoming":["SequenceFlow_mgolse1rjjms9t"],"outgoing":["SequenceFlow_mgolsrsrj4yfkj"],"properties":{"subType":"update_record","inputPath":"students2","datasourceName":"system","modelName":"Student","document":[{"field":"classId","value":"3"}],"filter":"{\"studentName\":{\"_in\":\"students2[*].studentName\"}}","resultPath":"sutndent3AffectedRows","name":"更新学生信息","positionX":1040,"positionY":16}},{"key":"serviceTask_mgolsqgfgrdmat","type":5,"incoming":["SequenceFlow_mgolsrsrj4yfkj"],"outgoing":["SequenceFlow_mgolxly85affbh"],"properties":{"subType":"delete_record","resultPath":"sutndent3AffectedRows","name":"删除学生信息","positionX":1312,"positionY":16,"datasourceName":"system","modelName":"Student","filter":"{\\n   \"studentName\":{\"_in\":\"students2[*].studentName\"}\\n}"}},{"key":"SequenceFlow_mgoksf9skg7m1c","type":1,"incoming":["start-node"],"outgoing":["serviceTask_mgokolh6ottxgz"],"properties":{"conditionsequenceflow":"","defaultConditions":"false"}},{"key":"SequenceFlow_mgokv2z5j6tju7","type":1,"incoming":["serviceTask_mgokolh6ottxgz"],"outgoing":["serviceTask_mgokspzb8h72r0"],"properties":{"conditionsequenceflow":"","defaultConditions":"false"}},{"key":"SequenceFlow_mgokxqixlnssvn","type":1,"incoming":["serviceTask_mgokspzb8h72r0"],"outgoing":["serviceTask_mgokvj54kxi9pj"],"properties":{"conditionsequenceflow":"","defaultConditions":"false"}},{"key":"SequenceFlow_mgokyc4xwquno6","type":1,"incoming":["serviceTask_mgokvj54kxi9pj"],"outgoing":["serviceTask_mgoky18vc65fea"],"properties":{"conditionsequenceflow":"","defaultConditions":"false"}},{"key":"SequenceFlow_mgolse1rjjms9t","type":1,"incoming":["serviceTask_mgoky18vc65fea"],"outgoing":["serviceTask_mgoli5cpsdyy77"],"properties":{"conditionsequenceflow":"","defaultConditions":"false"}},{"key":"SequenceFlow_mgolsrsrj4yfkj","type":1,"incoming":["serviceTask_mgoli5cpsdyy77"],"outgoing":["serviceTask_mgolsqgfgrdmat"],"properties":{"conditionsequenceflow":"","defaultConditions":"false"}},{"key":"SequenceFlow_mgolxly85affbh","type":1,"incoming":["serviceTask_mgolsqgfgrdmat"],"outgoing":["end-node"],"properties":{"conditionsequenceflow":"","defaultConditions":"false"}}]}
