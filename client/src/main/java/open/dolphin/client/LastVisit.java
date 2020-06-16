package open.dolphin.client;

import open.dolphin.infomodel.DocInfoModel;
import open.dolphin.project.Project;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
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
    // lastVisit の時間情報
    private LocalTime lastVisitTime = null;
    // ロガー
    private Logger logger = LoggerFactory.getLogger(LastVisit.class);

    public LastVisit(Chart context) {
        this.context = context;
        //((ch.qos.logback.classic.Logger)logger).setLevel(ch.qos.logback.classic.Level.INFO);
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

        if (Objects.nonNull(pvtDate)) {
            // 時間情報を保存
            lastVisitTime = LocalTime.parse(pvtDate, DateTimeFormatter.ISO_DATE_TIME);
        }

        if (docList.isEmpty()) {
            // ここに入ってくるのは (1)新患受付の場合, (2)久しぶりの受診で DocumentHistory で最終受診をスキャンしている途中
            lastVisit = Objects.isNull(pvtDate) ? null : LocalDate.parse(pvtDate, DateTimeFormatter.ISO_DATE_TIME);
            lastVisitInHistory = null;

        } else {
            // docList がある
            LocalDate test = LocalDate.parse(docList.get(0), DateTimeFormatter.ISO_DATE);
            lastVisit = Objects.isNull(pvtDate) ? test
                    : LocalDate.parse(pvtDate, DateTimeFormatter.ISO_DATE_TIME);
            lastVisitInHistory = !lastVisit.equals(test) ? test
                    : docList.size() == 1 ? null // 今日の受診だけがあって保存されている状態
                    : LocalDate.parse(docList.get(1), DateTimeFormatter.ISO_DATE);
        }
        logger.debug("lastVisit = " + lastVisit + ", inHistory " + lastVisitInHistory);
    }

    /**
     * 最終受診日を返す.
     *
     * @return last visit in LocalDate
     */
    @Nullable
    public LocalDate getLastVisit() {
        return lastVisit;
    }


    /**
     * 最終受診日の受診時間を返す.
     *
     * @return last visit time in LocalTime
     */
    @Nullable
    public LocalTime getLastVisitTime() {
        return lastVisitTime;
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
}
