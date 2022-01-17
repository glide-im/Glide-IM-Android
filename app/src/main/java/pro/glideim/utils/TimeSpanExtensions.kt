package pro.glideim.utils

import java.util.*


private const val hourSec = 60 * 60
private const val daySec = hourSec * 24
private const val mothSec = daySec * 30

fun Long.secToTimeSpan(): String {

    val instance = Calendar.getInstance(Locale.getDefault())
    instance.time = Date(this * 1000)
    val today = Calendar.getInstance().get(Calendar.DAY_OF_MONTH)

    val m = instance.get(Calendar.MONTH) + 1
    val d = instance.get(Calendar.DAY_OF_MONTH)
    val h = instance.get(Calendar.HOUR_OF_DAY)
    val min = instance.get(Calendar.MINUTE)

    return when (today - d) {
        0 -> "$h:${if (min < 10) "0" else ""}$min"
        1 -> "昨天"
        2 -> "前天"
        else -> "$m-$d"
    }

//    return when {
//        span <= 10 -> "刚刚"
//        span <= 60 -> "$span 秒前"
//        span <= 30 * 60 -> "${span / 60} 分钟前"
//        span <= hourSec -> "半小时时前"
//        span <= daySec -> "${span / hourSec} 小时前"
//        span <= daySec * 2 -> "昨天"
//        span <= daySec * 3 -> "前天"
//        span <= mothSec -> "${span / daySec} 天前"
//        else -> "${span / mothSec} 个月前"
//    }
}