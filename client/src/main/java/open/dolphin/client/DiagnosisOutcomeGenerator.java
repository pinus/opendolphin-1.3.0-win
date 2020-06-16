package open.dolphin.client;

import open.dolphin.infomodel.RegisteredDiagnosisModel;
import open.dolphin.project.Project;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * 転帰日を計算.
 *
 * @author pns
 */
public class DiagnosisOutcomeGenerator {
    private static int OFFSET = Project.getPreferences().getInt(Project.OFFSET_OUTCOME_DATE, -1);
    private static String[] ACUTE_DISEASE = { "ヘルペス", "単純疱疹" };

    private Logger logger = LoggerFactory.getLogger(DiagnosisOutcomeGenerator.class);
    private RegisteredDiagnosisModel rd;
    private LastVisit lv;

    public DiagnosisOutcomeGenerator() { }

    public void setParams(RegisteredDiagnosisModel registeredDiagnosisModel, LastVisit lastVisit) {
        rd = registeredDiagnosisModel;
        lv = lastVisit;
    }

    /**
     * 前回受診から1ヶ月以下なら offset 日戻した日付.
     * 前回受診から2ヶ月以上なら, 前回受診から1ヶ月後の最終日.
     *
     * @return ISO_DATE 型式の outcome date
     */
    public String standard() {
        LocalDate startDate = Objects.nonNull(lv.getLastVisitInHistory()) ? lv.getLastVisitInHistory() : lv.getLastVisit();
        long monthBetween = ChronoUnit.MONTHS.between(startDate.withDayOfMonth(1), lv.getLastVisit().withDayOfMonth(1));
        logger.info("monthBetween " + monthBetween);

        int n = 1; // month interval

        LocalDate endDate = monthBetween <= n
            ? lv.getLastVisit().plusDays(OFFSET)
            : startDate.plusMonths(n).withDayOfMonth(startDate.plusMonths(n).lengthOfMonth());

        return endDate.format(DateTimeFormatter.ISO_DATE);
    }

    /**
     * 急性疾患は started 月の末日. それ以外は最終受診日の月の末日.
     *
     * @return ISO_DATE
     */
    public String special() {
        LocalDate lastVisit = lv.getLastVisit();
        LocalDate endDate = Stream.of(ACUTE_DISEASE).anyMatch(rd.getDiagnosis()::contains)
            // 急性病名. plusDays(1) は started が末日だった場合対応
            ? rd.getStarted().toInstant().atZone(ZoneId.systemDefault()).toLocalDate().plusDays(1)
            // その他
            : Objects.nonNull(lv.getLastVisitInHistory()) ? lv.getLastVisitInHistory() : lv.getLastVisit();

        // 月末に設定.  started が当月の場合とか面倒くさいので無視.
        endDate = endDate.withDayOfMonth(endDate.lengthOfMonth());

        return endDate.format(DateTimeFormatter.ISO_DATE);
    }
}
