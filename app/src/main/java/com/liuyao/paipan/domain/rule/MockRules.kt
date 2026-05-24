package com.liuyao.paipan.domain.rule

import com.liuyao.paipan.domain.model.DivinationCategory
import com.liuyao.paipan.domain.model.SixGod
import com.liuyao.paipan.domain.model.SixKin

/**
 * 断语结构化示例(5 条),用于验证"断语能被结构化表达"。
 * 仅作样例数据,不参与生产逻辑;真实资料后续从导入流程入库。
 */
object MockRules {

    val source = RuleSource(
        id = "src-liu",
        author = "刘昌明",
        book = "象断六爻",
    )

    val rules: List<DivinationRule> = listOf(

        // 1) 求职:用神(官鬼)不上卦又被克 → 不利
        DivinationRule(
            id = "rule-001",
            sourceId = source.id,
            sourceName = source.displayName,
            category = DivinationCategory.CAREER,
            target = RuleTarget.kin(SixKin.OFFICIAL),
            originalText = "官鬼不上卦,伏而被克,谋职多不成。",
            plainExplanation = "代表职位的官鬼没出现在卦上、藏起来又被压制,求职多半难成。",
            conditionText = "官鬼不上卦,且伏神被飞神克",
            positiveMeaning = null,
            negativeMeaning = "求职难成,或久拖生变。",
            matchConditions = listOf(
                RuleCondition.KinAbsent(SixKin.OFFICIAL),
                RuleCondition.FlyingSuppressesHidden(hiddenKin = SixKin.OFFICIAL),
            ),
            excludeConditions = listOf(
                RuleCondition.UseGodSupportedByDay, // 若用神反得日生,则不作此凶断
            ),
            polarity = RulePolarity.INAUSPICIOUS,
            priority = RulePriority.HIGH.value,
            confidenceWeight = 0.8,
            tags = listOf(RuleTag.JOB.code, RuleTag.USE_GOD_HIDDEN.code),
            explanation = RuleExplanation(
                plain = "官鬼为职位,不现且受克,主事不成。",
                mechanism = "用神伏藏无力,飞神又克之,生路被断。",
                caveat = "若用神另得日月生扶,则当别论。",
            ),
            matchHint = RuleMatchHint(requireAllMatch = true, minScoreToShow = 60),
        ),

        // 2) 财运:妻财持世又得日辰生 → 财来就我
        DivinationRule(
            id = "rule-002",
            sourceId = source.id,
            sourceName = source.displayName,
            category = DivinationCategory.WEALTH,
            target = RuleTarget.World,
            originalText = "财爻持世,得日辰生扶,财来就我。",
            plainExplanation = "代表钱财的妻财爻就在世位上,又被日辰生助,主进财顺遂。",
            conditionText = "妻财临世爻,且用神得日辰生扶",
            positiveMeaning = "进财顺遂,财来就我。",
            negativeMeaning = null,
            matchConditions = listOf(
                RuleCondition.KinPresent(SixKin.WEALTH),
                RuleCondition.UseGodSupportedByDay,
            ),
            excludeConditions = listOf(
                RuleCondition.UseGodVoid, // 财爻若空,吉象打折,不作此断
            ),
            polarity = RulePolarity.AUSPICIOUS,
            priority = RulePriority.HIGH.value,
            confidenceWeight = 0.78,
            tags = listOf(RuleTag.WEALTH.code, RuleTag.WORLD_RESPONSE.code),
            explanation = RuleExplanation(
                plain = "财临世得生,财主动靠近自己。",
                mechanism = "用神临世为我所主,日辰生扶则力强。",
            ),
            matchHint = RuleMatchHint(minScoreToShow = 55),
        ),

        // 3) 文书:父母动化兄弟,又见相刑 → 通知旁落
        DivinationRule(
            id = "rule-003",
            sourceId = source.id,
            sourceName = source.displayName,
            category = DivinationCategory.FAME,
            target = RuleTarget.kin(SixKin.PARENT),
            originalText = "父母动而化兄弟,通知多旁落他人。",
            plainExplanation = "代表文书/录用的父母爻发动,却变出兄弟,录用通知常落到别人手里。",
            conditionText = "父母发动,化出兄弟(回头克),并见刑",
            positiveMeaning = null,
            negativeMeaning = "录用旁落他人,或反复不定。",
            matchConditions = listOf(
                RuleCondition.UseGodMoving,
                RuleCondition.BackRestrain,
            ),
            excludeConditions = emptyList(),
            polarity = RulePolarity.INAUSPICIOUS,
            priority = RulePriority.NORMAL.value,
            confidenceWeight = 0.65,
            tags = listOf(RuleTag.DOCUMENT.code, RuleTag.BACK_RESTRAIN.code),
            explanation = RuleExplanation(
                plain = "父母为文书,化兄弟则被劫夺。",
                mechanism = "兄弟为劫,文书向兄弟而去,故旁落。",
            ),
            matchHint = RuleMatchHint(minScoreToShow = 50),
        ),

        // 4) 官非:子孙旺动克官 → 官灾消解(但不利谋官)
        DivinationRule(
            id = "rule-004",
            sourceId = source.id,
            sourceName = source.displayName,
            category = DivinationCategory.LAWSUIT,
            target = RuleTarget.kin(SixKin.OFFSPRING),
            originalText = "子孙发动克官鬼,官灾自消。",
            plainExplanation = "代表解神的子孙发动去克官鬼,官非、灾病多可化解。",
            conditionText = "子孙发动(克官鬼)",
            positiveMeaning = "官非、灾病消解。",
            negativeMeaning = "若为谋官求职,则反主不利。",
            matchConditions = listOf(
                RuleCondition.KinPresent(SixKin.OFFSPRING),
                RuleCondition.UseGodMoving,
                RuleCondition.SixGodPresent(SixGod.WHITE_TIGER, onTarget = RuleTarget.kin(SixKin.OFFICIAL)),
            ),
            excludeConditions = listOf(
                RuleCondition.KinAbsent(SixKin.OFFICIAL), // 官鬼不现则无所谓克官
            ),
            polarity = RulePolarity.MIXED,
            priority = RulePriority.NORMAL.value,
            confidenceWeight = 0.6,
            tags = listOf(RuleTag.MOVING.code),
            explanation = RuleExplanation(
                plain = "子孙为福神制官,官灾得解。",
                caveat = "求官者忌之:子孙克官则官星受伤。",
            ),
            matchHint = RuleMatchHint(minScoreToShow = 50),
        ),

        // 5) 求名:用神临应又旬空 → 事在两可,待出空
        DivinationRule(
            id = "rule-005",
            sourceId = source.id,
            sourceName = source.displayName,
            category = DivinationCategory.FAME,
            target = RuleTarget.Response,
            originalText = "用神临应又逢旬空,事在两可,出空之日方有定音。",
            plainExplanation = "代表结果的用神落在对方(应)位且旬空,说明暂未定,要等出空那天才见分晓。",
            conditionText = "用神临应爻,且应爻旬空",
            positiveMeaning = null,
            negativeMeaning = null,
            matchConditions = listOf(
                RuleCondition.ResponseVoid,
                RuleCondition.UseGodVoid,
            ),
            excludeConditions = emptyList(),
            polarity = RulePolarity.NEUTRAL,
            priority = RulePriority.NORMAL.value,
            confidenceWeight = 0.7,
            tags = listOf(RuleTag.USE_GOD_VOID.code, RuleTag.APPLY_TIMING.code),
            explanation = RuleExplanation(
                plain = "用神临应而空,结果未定。",
                mechanism = "旬空待填,出空之日为应期。",
            ),
            matchHint = RuleMatchHint(
                minScoreToShow = 55,
                applyTimingNote = "以用神出空(填实/冲空)之日为应期。",
            ),
        ),
    )
}
