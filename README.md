# NetRetryInterceptor
流程图如下：
![NetRetryInterceptor流程图](https://github.com/ydslib/HttpRetryUtil/blob/master/uml.png)

## 3 使用
### 3.1 自动轮询模式
在SplashActivity中初始化：
//在原项目RetrofitClient初始化的基础上添加NetRetryInterceptor拦截器
RetrofitClient.setup(
            BuildConfig.HOST,
            listOf(CookieInterceptor(getCookieEnv(), getTokenKey()),NetRetryInterceptor())
        )

val array = intArrayOf(502,504)
RetryManager.initManager(this)
            .delayTime(1000)
            .isNeedDeDuplication(true)//是否需要去重
            .isAutoSchedule(true)
            .responseCodeSave(array)

### 3.2 手动轮询模式
在Application中初始化
val array = intArrayOf(502,504)
RetryManager.initManager(this)
            .delayTime(1000)
            .isNeedDeDuplication(true)//是否需要去重
            .responseCodeSave(array)
在SplashActivity中调用
RetryManager.startTask()

//在需要关闭的地方调用关闭方法
RetryManager.closeTask()

## 4 主要api
### Api-initManager
初始化重试库
**接口定义**
```kotlin
fun initManager(context: Context):RetryManager
```

### Api-delayTime
轮询任务间隔时间，单位毫秒
**接口定义**
```kotlin
fun delayTime(delayTime: Long):RetryManager
```
**参数说明**
轮询任务间隔时间，单位毫秒

### Api-maxFailCount
接口最大失败次数，当数据库中某个接口失败次数大于等于maxFailCount时，将会从数据库中删除该接口，默认大小为Integer.MAX_VALUE
**接口定义**
```kotlin
fun maxFailCount(maxFailCount: Int):RetryManager
```
**参数说明**
接口最大失败次数

### Api-maxScheduleCount
数据库最大空轮询次数，当连续maxScheduleCount次轮询数据库都为空时，则关闭轮询器，只在自动轮询中使用，非自动轮询无法使用
**接口定义**
```kotlin
fun maxScheduleCount(maxScheduleCount:Int):RetryManager
```
**参数说明**
数据库最大空轮询次数

### Api-startTask
开启轮询任务
**接口定义**
```kotlin
fun startTask()
```

### Api-startTaskWithDelay
延迟一段时间后开启轮询任务
**接口定义**
```kotlin
fun startTaskWithDelay(delayTime: Long)
```
**参数说明**
延迟一段时间后开启轮询任务，单位毫秒

### Api-closeTask
关闭轮询任务
**接口定义**
```kotlin
fun closeTask()
```

### Api-isNeedDeDuplication
是否需要去重，默认不去重
**接口定义**
```kotlin
fun isNeedDeDuplication(isNeedDeDuplication: Boolean):RetryManager
```
**参数说明**
是否需要去重，默认不去重


### Api-isAutoSchedule
是否是自动轮询模式，默认不是
**接口定义**
```kotlin
fun isAutoSchedule(isAutoSchedule: Boolean, scheduledMode: Int = DEFAULT_SCHEDULE_MODE):RetryManager
```
**参数说明**
- isAutoSchedule：是否是自动轮询模式，不需要用户手动开启，会在特定时候自己开启轮询器
- scheduledMode：轮询模式，分为三种，默认（DEFAULT_SCHEDULE_MODE），前台模式（FOREGROUND_SCHEDULE_MODE）和数据驱动模式（DATA_SCHEDULE_MODE）
> - 默认模式：在App启动和Activity切换以及应用切换前后台时，都会判断是否开启轮询器，如果没开启，则开启，如果应用进入后台或杀死应用，则会关闭轮询器。当有失败次数插入到数据库时，判断是否开启轮询器，> > 如果没开启，则开启。
> - 前台模式：在App启动和Activity切换以及应用切换前后台时，都会判断是否开启轮询器，如果没开启，则开启，如果应用进入后台或杀死应用，则会关闭轮询器。
> - 数据驱动模式：当有失败次数插入到数据库时，判断是否开启轮询器，如果没开启，则开启。

### Api-getOkHttpClient()
获取OkHttpClient对象
**接口定义**
```kotlin
fun getOkHttpClient(): OkHttpClient
```


### Api-retryImmediately
手动上报，立即上报，不走调度器
**接口定义**
```kotlin
fun retryImmediately()
```

### Api-responseCodeSave
后台返回哪些code码时需要存储到数据库，用于后续轮询重试
**接口定义**
```kotlin
fun responseCodeSave(codeArray: IntArray):RetryManager
```
**参数说明**
codeArray：需要存入数据库的接口返回码列表
