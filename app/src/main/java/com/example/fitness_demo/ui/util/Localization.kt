package com.example.fitness_demo.ui.util

object Localization {
    private val exerciseNameMap: Map<String, String> = mapOf(
        // Chest
        "Bench Press" to "平板卧推",
        "Incline Bench Press" to "上斜卧推",
        "Decline Bench Press" to "下斜卧推",
        "Dumbbell Bench Press" to "哑铃卧推",
        "Dumbbell Incline Press" to "哑铃上斜卧推",
        "Chest Fly (Machine)" to "胸飞鸟（器械）",
        "Cable Crossover" to "绳索交叉",
        "Push Up" to "俯卧撑",
        "Weighted Push Up" to "负重俯卧撑",
        "Chest Dip" to "双杠臂屈伸（胸）",
        // Back
        "Deadlift" to "硬拉",
        "Barbell Row" to "杠铃划船",
        "Pendlay Row" to "潘德雷划船",
        "T-Bar Row" to "T 杠划船",
        "One-Arm Dumbbell Row" to "单臂哑铃划船",
        "Lat Pulldown" to "高位下拉",
        "Pull Up" to "引体向上",
        "Chin Up" to "反握引体",
        "Seated Cable Row" to "坐姿划船",
        "Face Pull" to "面拉",
        "Back Extension" to "背部伸展",
        // Legs
        "Back Squat" to "后蹲",
        "Front Squat" to "前蹲",
        "High-Bar Squat" to "高杠深蹲",
        "Low-Bar Squat" to "低杠深蹲",
        "Leg Press" to "推蹬",
        "Bulgarian Split Squat" to "保加利亚分腿蹲",
        "Lunge" to "弓步蹲",
        "Romanian Deadlift" to "罗马尼亚硬拉",
        "Stiff-Leg Deadlift" to "直腿硬拉",
        "Good Morning" to "早安式",
        "Leg Extension" to "腿部伸展",
        "Leg Curl" to "腿弯举",
        "Hip Thrust" to "杠铃臀桥",
        "Glute Bridge" to "臀桥",
        // Shoulders
        "Overhead Press" to "站姿推举",
        "Seated Barbell Press" to "坐姿杠铃推举",
        "Dumbbell Shoulder Press" to "哑铃肩推",
        "Arnold Press" to "阿诺德推举",
        "Lateral Raise" to "侧平举",
        "Cable Lateral Raise" to "绳索侧平举",
        "Rear Delt Fly" to "反向飞鸟",
        "Reverse Pec Deck" to "反向夹胸",
        "Upright Row" to "直立划船",
        // Arms
        "Barbell Curl" to "杠铃弯举",
        "EZ-Bar Curl" to "EZ 杠弯举",
        "Dumbbell Curl" to "哑铃弯举",
        "Incline Dumbbell Curl" to "上斜哑铃弯举",
        "Hammer Curl" to "锤式弯举",
        "Concentration Curl" to "集中弯举",
        "Preacher Curl" to "牧师椅弯举",
        "Tricep Pushdown" to "绳索下压",
        "Overhead Tricep Extension" to "头上臂屈伸",
        "Skull Crusher" to "仰卧臂屈伸",
        "Close-Grip Bench Press" to "窄握卧推",
        "Dips (Triceps)" to "双杠臂屈伸（三头）",
        "Wrist Curl" to "腕弯举",
        "Reverse Wrist Curl" to "反向腕弯举",
        // Core
        "Plank" to "平板支撑",
        "Side Plank" to "侧桥",
        "Crunch" to "仰卧卷腹",
        "Cable Crunch" to "绳索卷腹",
        "Hanging Leg Raise" to "悬垂举腿",
        "Captain's Chair" to "船长椅举腿",
        "Ab Wheel Rollout" to "腹肌轮前滚",
        "Cable Woodchop" to "绳索伐木",
        "Russian Twist" to "俄罗斯转体",
        // Calves / Traps
        "Standing Calf Raise" to "站姿提踵",
        "Seated Calf Raise" to "坐姿提踵",
        "Barbell Shrug" to "杠铃耸肩",
        "Dumbbell Shrug" to "哑铃耸肩",
        // Full body / Conditioning
        "Clean and Press" to "翻推",
        "Clean and Jerk" to "挺举",
        "Snatch" to "抓举",
        "Kettlebell Swing" to "壶铃摆动",
        "Burpee" to "波比跳",
        "Farmer's Walk" to "农夫行走"
    )

    fun exercise(name: String): String = exerciseNameMap[name] ?: name
    fun group(en: String): String = when (en.lowercase()) {
        "all" -> "全部"
        "chest" -> "胸部"
        "back" -> "背部"
        "legs" -> "腿部"
        "shoulders" -> "肩部"
        "arms" -> "手臂"
        "core" -> "核心"
        "full body" -> "全身"
        "other" -> "其它"
        else -> en
    }
}

