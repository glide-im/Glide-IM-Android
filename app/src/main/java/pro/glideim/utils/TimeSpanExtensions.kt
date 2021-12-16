package pro.glideim.utils


private const val hourSec = 60 * 60
private const val daySec = hourSec * 24
private const val mothSec = daySec * 30

fun Long.secToTimeSpan(): String {
    val span = System.currentTimeMillis() / 1000 - this

    return when {
        span <= 10 -> "刚刚"
        span <= 60 -> "$span 秒前"
        span <= 30 * 60 -> "${span / 60} 分钟前"
        span <= hourSec -> "半小时时前"
        span <= daySec -> "${span / hourSec} 小时前"
        span <= daySec * 2 -> "昨天"
        span <= daySec * 3 -> "前天"
        span <= mothSec -> "${span / daySec} 天前"
        else -> "${span / mothSec} 个月前"
    }
}