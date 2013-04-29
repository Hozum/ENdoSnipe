package jp.co.acroquest.endosnipe.web.dashboard.dto;

/**
 * シグナル定義のDTOクラス。
 * 
 * @author miyasaka
 * 
 */
public class SignalDefinitionDto
{
    /** シグナル定義テーブルのID。 */
    private int signalId;

    /** シグナル名。 */
    private String signalName;

    /** マッチングパターン。 */
    private String matchingPattern;

    /** 設定できる閾値の上限レベル。 */
    private int level;

    /** 各レベルの閾値。 */
    private String patternValue;

    /** エスカレーション期間。 */
    private double escalationPeriod;

    /** 閾値判定の結果。 */
    private Integer signalValue;

    /**
     * シグナル名を取得する。
     * 
     * @return シグナル名
     */
    public String getSignalName()
    {
        return signalName;
    }

    /**
     * シグナルIDを取得する。
     * 
     * @return シグナルID
     */
    public int getSignalId()
    {
        return signalId;
    }

    /**
     * シグナルIDを設定する。
     * 
     * @param signalId
     *            シグナルID
     */
    public void setSignalId(final int signalId)
    {
        this.signalId = signalId;
    }

    /**
     * シグナル名を設定する。
     * 
     * @param signalName
     *            シグナル名
     */
    public void setSignalName(final String signalName)
    {
        this.signalName = signalName;
    }

    /**
     * マッチングパターンを取得する。
     * 
     * @return マッチングパターン
     */
    public String getMatchingPattern()
    {
        return matchingPattern;
    }

    /**
     * マッチングパターンを設定する。
     * 
     * @param matchingPattern
     *            マッチングパターン
     */
    public void setMatchingPattern(final String matchingPattern)
    {
        this.matchingPattern = matchingPattern;
    }

    /**
     * 設定できる閾値の上限レベルを取得する。
     * 
     * @return 設定できる閾値の上限レベル
     */
    public int getLevel()
    {
        return level;
    }

    /**
     * 設定できる閾値の上限レベルを設定する。
     * 
     * @param level
     *            設定できる閾値の上限レベル
     */
    public void setLevel(final int level)
    {
        this.level = level;
    }

    /**
     * 各レベルの閾値を取得する。
     * 
     * @return 各レベルの閾値
     */
    public String getPatternValue()
    {
        return patternValue;
    }

    /**
     * 各レベルの閾値を設定する。
     * 
     * @param patternValue
     *            各レベルの閾値
     */
    public void setPatternValue(final String patternValue)
    {
        this.patternValue = patternValue;
    }

    /**
     * エスカレーション期間を取得する。
     * 
     * @return エスカレーション期間
     */
    public double getEscalationPeriod()
    {
        return escalationPeriod;
    }

    /**
     * エスカレーション期間を設定する。
     * 
     * @param escalationPeriod
     *            エスカレーション期間
     */
    public void setEscalationPeriod(final double escalationPeriod)
    {
        this.escalationPeriod = escalationPeriod;
    }

    /**
     * 閾値判定の結果を取得する。
     * 
     * @return 閾値判定の結果
     */
    public Integer getSignalValue()
    {
        return signalValue;
    }

    /**
     * 閾値判定の結果を設定する。
     * 
     * @param signalValue 閾値判定の結果
     */
    public void setSignalValue(final Integer signalValue)
    {
        this.signalValue = signalValue;
    }
}
