package open.dolphin.client;

import open.dolphin.infomodel.DocInfoModel;
import open.dolphin.project.Project;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 最終受診日計算.
 * 今日受診していたら今日, 今日受診していない場合は History の最終受診日になる.
 * 今日受診していても，History の最終受診日を知りたい場合は InHistory を使う.
 *
 * @author pns
 */
public class LastVisit {

    // Chart
    private Chart context;
    // 最終受診日
    private LocalDate lastVisit = null;
    // DocumentHistory の最終受診日
    private LocalDate lastVisitInHistory = null;
    // ロガー
    private Logger logger = Logger.getLogger(LastVisit.class);

    public LastVisit(Chart context) {
        this.context = context;
        //logger.setLevel(Level.DEBUG);
    }

    /**
     * DocumentHistory inspector の updateHistory から呼んでもらって update する.
     *
     * @param docInfoModels List of DocInfoModel
     */
    public void update(List<DocInfoModel> docInfoModels) {
        String pvtDate = context.getPatientVisit().getPvtDate();
        logger.debug("pvt date = " + pvtDate);

        // ISO_DATE 型式のリスト
        List<String> docList = docInfoModels.stream()
                .map(DocInfoModel::getFirstConfirmDateTrimTime)
                .sorted(Comparator.reverseOrder()).collect(Collectors.toList());
        logger.debug("doc list = " + docList);

        if (docList.isEmpty()) {
            // 新患でまだ保存していない状態 = pvtDate は null ではありえない
            lastVisit = LocalDate.parse(pvtDate, DateTimeFormatter.ISO_DATE_TIME);
            lastVisitInHistory = null;

        } else {
            LocalDate test = LocalDate.parse(docList.get(0), DateTimeFormatter.ISO_DATE);
            if (Objects.isNull(pvtDate)) {
                // 今日の受診がなくて docList が空ではない
                lastVisit = lastVisitInHistory = test;
            } else {
                // 今日の受診がある
                lastVisit = LocalDate.parse(pvtDate, DateTimeFormatter.ISO_DATE_TIME);
                lastVisitInHistory = !lastVisit.equals(test) ? test
                        : docList.size() == 1 ? null
                        : LocalDate.parse(docList.get(1), DateTimeFormatter.ISO_DATE);
            }
        }
    }

    /**
     * 前回受診から1ヶ月以下なら offset 日戻した日付.
     * 前回受診から2ヶ月以上なら, 前回受診から1ヶ月後の最終日.
     *
     * @return ISO_DATE 型式の outcome date
     */
    public String getDiagnosisOutcomeDate() {
        LocalDate startDate = Objects.nonNull(lastVisitInHistory) ? lastVisitInHistory : lastVisit;
        long monthBetween = ChronoUnit.MONTHS.between(startDate.withDayOfMonth(1), lastVisit.withDayOfMonth(1));
        logger.debug("monthBetween " + monthBetween);

        int offset = Project.getPreferences().getInt(Project.OFFSET_OUTCOME_DATE, -1);
        LocalDate endDate = monthBetween <= 1
                ? lastVisit.plusDays(offset)
                : startDate.plusMonths(1).withDayOfMonth(startDate.plusMonths(1).lengthOfMonth());

        logger.debug("lastVisit = " + lastVisit + ", start = " + startDate + ", endDate = " + endDate);
        return endDate.format(DateTimeFormatter.ISO_DATE);
    }

    /**
     * 最終受診日を返す.
     *
     * @return last visit in LocalDateTime
     */
    @Nullable
    public LocalDate getLastVisit() {
        return lastVisit;
    }

    /**
     * 今日を除いた最終受診日を返す.
     *
     * @return last visit in LocalDateTime
     */
    @Nullable
    public LocalDate getLastVisitInHistory() {
        return lastVisitInHistory;
    }

    /**
     * 最終受診日を，int 配列 int[0]=year, int[1]=month, int[2]=day で返す (month は 0-11)
     *
     * @return { year, month, day }
     */
    @Nullable
    public int[] getLastVisitYmd() {
        return Objects.isNull(lastVisit) ? null :
                new int[]{lastVisit.getYear(),
                        lastVisit.getMonthValue() - 1,
                        lastVisit.getDayOfMonth()};
    }

    /**
     * 今日を除いた最終受診日（int 配列形式 int[0]=year, int[1]=month(0～11), int[2]=day）
     *
     * @return { year, month, day }
     */
    @Nullable
    public int[] getLastVisitInHistoryYmd() {
        return Objects.isNull(lastVisitInHistory) ? null :
                new int[]{lastVisitInHistory.getYear(),
                        lastVisitInHistory.getMonthValue() - 1,
                        lastVisitInHistory.getDayOfMonth()};
    }

    /**
     * 最終受診日を GlegorianCalendar で返す
     *
     * @return GregorianCalendar
     */
    @Nullable
    public GregorianCalendar getLastVisitGc() {
        return Objects.isNull(lastVisit) ? null :
                GregorianCalendar.from(lastVisit.atStartOfDay(ZoneId.systemDefault()));
    }
}
