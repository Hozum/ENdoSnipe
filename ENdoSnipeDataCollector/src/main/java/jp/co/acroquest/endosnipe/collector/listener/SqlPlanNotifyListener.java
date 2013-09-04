/*******************************************************************************
 * ENdoSnipe 5.0 - (https://github.com/endosnipe)
 * 
 * The MIT License (MIT)
 * 
 * Copyright (c) 2013 Acroquest Technology Co.,Ltd.
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 ******************************************************************************/
package jp.co.acroquest.endosnipe.collector.listener;

import java.sql.SQLException;

import jp.co.acroquest.endosnipe.collector.LogMessageCodes;
import jp.co.acroquest.endosnipe.collector.data.JavelinData;
import jp.co.acroquest.endosnipe.common.logger.ENdoSnipeLogger;
import jp.co.acroquest.endosnipe.communicator.AbstractTelegramListener;
import jp.co.acroquest.endosnipe.communicator.TelegramListener;
import jp.co.acroquest.endosnipe.communicator.accessor.SqlPlanNotifyAccessor;
import jp.co.acroquest.endosnipe.communicator.accessor.SqlPlanNotifyAccessor.SqlPlanEntry;
import jp.co.acroquest.endosnipe.communicator.entity.Telegram;
import jp.co.acroquest.endosnipe.communicator.entity.TelegramConstants;
import jp.co.acroquest.endosnipe.data.dao.SqlPlanDao;
import jp.co.acroquest.endosnipe.data.entity.SqlPlan;

/**
 * SQL���s�v��ʒm�d������M���邽�߂̃N���X�ł��B<br />
 * 
 * @author miyasaka
 *
 */
public class SqlPlanNotifyListener extends AbstractTelegramListener implements TelegramListener,
    LogMessageCodes, AgentNameListener
{
    private static final ENdoSnipeLogger LOGGER = ENdoSnipeLogger
        .getLogger(JvnFileNotifyListener.class);

    private String databaseName_ = "";

    private String hostName_ = null;

    private String agentName_ = null;

    private String ipAddress_ = "";

    private int port_ = -1;

    private String clientId_ = null;

    @Override
    protected Telegram doReceiveTelegram(final Telegram telegram)
    {
        if (LOGGER.isDebugEnabled())
        {
            LOGGER.log(JVN_FILE_NOTIFY_RECEIVED);
        }

        SqlPlanEntry[] entries = SqlPlanNotifyAccessor.getSqlPlanEntries(telegram);

        int entryLength = entries.length;

        for (int index = 0; index < entryLength; index++)
        {
            SqlPlanEntry sqlPlanEntry = entries[index];
            SqlPlan sqlPlan = convertSqlPlan(sqlPlanEntry);

            try
            {
                SqlPlanDao.insert(databaseName_, sqlPlan);
            }
            catch (SQLException ex)
            {
                LOGGER.log(DATABASE_ACCESS_ERROR, ex, ex.getMessage());
            }
        }

        return null;
    }

    @Override
    protected byte getByteRequestKind()
    {
        return TelegramConstants.BYTE_REQUEST_KIND_NOTIFY;
    }

    @Override
    protected byte getByteTelegramKind()
    {
        return TelegramConstants.BYTE_TELEGRAM_KIND_SQL_PLAN;
    }

    /**
     * Agent�����擾����B
     * 
     * @return Agent��
     */
    public String getAgentName()
    {
        return agentName_;
    }

    /**
     *  Agent����ݒ肷��B
     *  
     *  @param agentName Agent��
     */
    public void setAgentName(final String agentName)
    {
        this.agentName_ = agentName;
    }

    /**
     * {@link JavelinData} �p�̐ڑ��� IP �A�h���X��ݒ肵�܂��B<br />
     * 
     * @param ipAddress �ڑ��� IP �A�h���X
     */
    public void setIpAddress(final String ipAddress)
    {
        this.ipAddress_ = ipAddress;
    }

    /**
     * {@link JavelinData} �p�̐ڑ���|�[�g�ԍ���ݒ肵�܂��B<br />
     * 
     * @param port �ڑ���|�[�g�ԍ�
     */
    public void setPort(final int port)
    {
        this.port_ = port;
    }

    /**
     * {@link JavelinData} �p�̃N���C�A���gID��ݒ肵�܂��B
     * @param clientId �N���C�A���gID
     */
    public void setClientId(final String clientId)
    {
        clientId_ = clientId;
    }

    /**
     * �z�X�g����ݒ肷��B
     * 
     * @param hostName �z�X�g��
     */
    public void setHostName(final String hostName)
    {
        hostName_ = hostName;
    }

    /**
     * DB����ݒ肷��B
     * 
     * @param databaseName DB��
     */
    public void setDatabaseName(final String databaseName)
    {
        databaseName_ = databaseName;
    }

    /**
     * SqlPlanEntry�I�u�W�F�N�g��SqlPlan�I�u�W�F�N�g�ɕύX����B
     * 
     * @param entry �ϊ��Ώۂ�SqlPlanEntry�I�u�W�F�N�g
     * @return SqlPlan�I�u�W�F�N�g
     */
    private SqlPlan convertSqlPlan(final SqlPlanEntry entry)
    {
        SqlPlan sqlPlan = new SqlPlan();
        sqlPlan.measurementItemName = entry.measurementItemName;
        sqlPlan.executionPlan = entry.executionPlan;
        sqlPlan.sqlStatement = entry.sqlStatement;
        sqlPlan.gettingPlanTime = entry.gettingPlanTime;
        sqlPlan.stackTrace = entry.stackTrace;

        return sqlPlan;
    }
}
