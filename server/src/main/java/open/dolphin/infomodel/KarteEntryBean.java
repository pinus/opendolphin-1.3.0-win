package open.dolphin.infomodel;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import open.dolphin.util.ModelUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.hibernate.search.annotations.IndexedEmbedded;

import javax.persistence.*;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * KarteEntryBean.
 *
 * @param <T> type of objects compared to
 * @author Minagawa, Kazushi
 */
@MappedSuperclass
@JsonIdentityInfo(generator = ObjectIdGenerators.UUIDGenerator.class)
public class KarteEntryBean<T extends KarteEntryBean<T>> extends InfoModel implements Comparable<T> {
    private static final long serialVersionUID = -9126237924533456842L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @Column(nullable = false)
    @Temporal(value = TemporalType.TIMESTAMP)
    private Date confirmed;

    @Column(nullable = false)
    @Temporal(value = TemporalType.TIMESTAMP)
    private Date started;

    @Temporal(value = TemporalType.TIMESTAMP)
    private Date ended;

    @Column(nullable = false)
    @Temporal(value = TemporalType.TIMESTAMP)
    private Date recorded;

    private long linkId;

    private String linkRelation;

    @Column(length = 1, nullable = false)
    private String status;

    @ManyToOne
    @JoinColumn(name = "creator_id", nullable = false)
    private UserModel creator;

    @IndexedEmbedded            // hibernate search
    @ManyToOne
    @JoinColumn(name = "karte_id", nullable = false)
    private KarteBean karte;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Date getConfirmed() {
        return confirmed;
    }

    public void setConfirmed(Date confirmed) {
        this.confirmed = confirmed;
    }

    public Date getStarted() {
        return started;
    }

    public void setStarted(Date started) {
        this.started = started;
    }

    public Date getEnded() {
        return ended;
    }

    public void setEnded(Date ended) {
        this.ended = ended;
    }

    public Date getRecorded() {
        return recorded;
    }

    public void setRecorded(Date recorded) {
        this.recorded = recorded;
    }

    public long getLinkId() {
        return linkId;
    }

    public void setLinkId(long linkId) {
        this.linkId = linkId;
    }

    public String getLinkRelation() {
        return linkRelation;
    }

    public void setLinkRelation(String linkRelation) {
        this.linkRelation = linkRelation;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public UserModel getCreator() {
        return creator;
    }

    public void setCreator(UserModel creator) {
        this.creator = creator;
    }

    public KarteBean getKarte() {
        return karte;
    }

    public void setKarte(KarteBean karte) {
        this.karte = karte;
    }

    @Override
    public int hashCode() {
        final int PRIME = 31;
        int result = 1;
        result = PRIME * result + (int) (id ^ (id >>> 32));
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final KarteEntryBean other = (KarteEntryBean) obj;
        return (id == other.getId());
    }

    /**
     * 適合開始日と確定日で比較する.
     *
     * @param other 比較対象
     * @return Comparable の比較値
     */
    @Override
    public int compareTo(T other) {
        if (other != null) {
            Date date1 = getStarted();
            Date date2 = other.getStarted();
            int result = compareDate(date1, date2);
            if (result == 0) {
                date1 = getConfirmed();
                date2 = other.getConfirmed();
                result = compareDate(date1, date2);
            }
            return result;
        }
        return -1;
    }

    private int compareDate(Date date1, Date date2) {
        if (date1 == null) {
            // 両方 null なら等しい
            if (date2 == null) {
                return 0;
            }
            // null は最上位
            else {
                return 1;
            }
        } else {
            // null は最上位
            if (date2 == null) {
                return -1;
            }
            // date1 != null && date2 != null の場合
            else {
                return date1.compareTo(date2);
            }
        }
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    //
    // 互換性用のプロキシコード
    //
    public Date getFirstConfirmed() {
        return getStarted();
    }

    public void setFirstConfirmed(Date firstConfirmed) {
        setStarted(firstConfirmed);
    }

    public String getFirstConfirmDate() {
        return ModelUtils.getDateTimeAsString(getFirstConfirmed());
    }

    public void setFirstConfirmDate(String timeStamp) {
        setFirstConfirmed(ModelUtils.getDateTimeAsObject(timeStamp));
    }

    public String getConfirmDate() {
        return ModelUtils.getDateTimeAsString(getConfirmed());
    }

    public void setConfirmDate(String timeStamp) {
        setConfirmed(ModelUtils.getDateTimeAsObject(timeStamp));
    }


    //
    // 足場コード  Date
    //
    public String firstConfirmDateAsString() {
        return dateAsString(getFirstConfirmed());
    }

    public String confirmDateAsString() {
        return dateAsString(getConfirmed());
    }

    public String startedDateAsString() {
        return dateAsString(getStarted());
    }

    public String endedDateAsString() {
        return dateAsString(getEnded());
    }

    public String recordedDateAsString() {
        return dateAsString(getRecorded());
    }

    private String dateAsString(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_WITHOUT_TIME);
        return sdf.format(date);
    }

    //
    // 足場コード  TimeStamp
    //
    public String confirmedTimeStampAsString() {
        return timeStampAsString(getConfirmed());
    }

    public String startedTimeStampAsString() {
        return timeStampAsString(getStarted());
    }

    public String endedTimeStampAsString() {
        return timeStampAsString(getEnded());
    }

    public String recordedTimeStampAsString() {
        return timeStampAsString(getRecorded());
    }

    private String timeStampAsString(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat(ISO_8601_DATE_FORMAT);
        return sdf.format(date);
    }
}
