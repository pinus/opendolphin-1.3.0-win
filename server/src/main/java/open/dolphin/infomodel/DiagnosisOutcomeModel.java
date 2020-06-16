package open.dolphin.infomodel;

import javax.persistence.Embeddable;

/**
 * Diagnosis のカテゴリーモデル.
 *
 * @author Kazushi Minagawa, Digital Globe, Inc.
 */
@Embeddable
public class DiagnosisOutcomeModel extends InfoModel {
    private static final long serialVersionUID = 632837158247035968L;

    private String outcome;
    private String outcomeDesc;
    private String outcomeCodeSys;

    @Override
    public String toString() {
        return getOutcomeDesc();
    }

    public String getOutcome() { return outcome; }

    public void setOutcome(String outcome) { this.outcome = outcome; }

    public String getOutcomeDesc() {
        return outcomeDesc;
    }

    public void setOutcomeDesc(String outcomeDesc) {
        this.outcomeDesc = outcomeDesc;
    }

    public String getOutcomeCodeSys() {
        return outcomeCodeSys;
    }

    public void setOutcomeCodeSys(String outcomeCodeSys) {
        this.outcomeCodeSys = outcomeCodeSys;
    }
}
