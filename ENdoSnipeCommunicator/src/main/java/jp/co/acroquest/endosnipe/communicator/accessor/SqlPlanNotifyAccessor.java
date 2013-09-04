/*
 * Copyright (c) 2004-2013 Acroquest Technology Co., Ltd. All Rights Reserved.
 * Please read the associated COPYRIGHTS file for more details.
 *
 * THE  SOFTWARE IS  PROVIDED BY  Acroquest Technology Co., Ltd., WITHOUT  WARRANTY  OF
 * ANY KIND,  EXPRESS  OR IMPLIED,  INCLUDING BUT  NOT LIMITED  TO THE
 * WARRANTIES OF  MERCHANTABILITY,  FITNESS FOR A  PARTICULAR  PURPOSE
 * AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDER BE LIABLE FOR ANY
 * CLAIM, DAMAGES SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING
 * OR DISTRIBUTING THIS SOFTWARE OR ITS DERIVATIVES.
 */
package jp.co.acroquest.endosnipe.communicator.accessor;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import jp.co.acroquest.endosnipe.common.logger.ENdoSnipeLogger;
import jp.co.acroquest.endosnipe.communicator.entity.Body;
import jp.co.acroquest.endosnipe.communicator.entity.Header;
import jp.co.acroquest.endosnipe.communicator.entity.ResponseBody;
import jp.co.acroquest.endosnipe.communicator.entity.Telegram;
import jp.co.acroquest.endosnipe.communicator.entity.TelegramConstants;

/**
 * SQL���s�v��ʒm�d���̂��߂̃A�N�Z�T�N���X�ł��B<br />
 * 
 * @author miyasaka
 *
 */
public class SqlPlanNotifyAccessor implements TelegramConstants
{
    private static final ENdoSnipeLogger LOGGER = ENdoSnipeLogger
        .getLogger(SystemResourceGetter.class);

    /**
     * �v���C�x�[�g�R���X�g���N�^�ł��B<br />
     */
    private SqlPlanNotifyAccessor()
    {
        // Do nothing
    }

    /**
     * SQL���s�v��ʒm�d��������e�����o���܂��B<br />
     * �d����ʂ����O�ʒm�d���ł͂Ȃ��ꍇ��A���e���h���ł���ꍇ�� <code>null</code> ��Ԃ��܂��B<br />
     * 
     * @param telegram SQL���s�v��ʒm�d��
     * @return �d�����e
     */
    public static SqlPlanEntry[] getSqlPlanEntries(final Telegram telegram)
    {
        if (checkTelegram(telegram) == false)
        {
            return null;
        }

        Body[] bodies = telegram.getObjBody();
        List<String> measurmentItemNames = new ArrayList<String>(bodies.length);
        List<String> sqlStatements = new ArrayList<String>(bodies.length);
        List<String> executionPlans = new ArrayList<String>(bodies.length);
        List<Timestamp> gettingPlanTimes = new ArrayList<Timestamp>(bodies.length);
        List<String> stackTraces = new ArrayList<String>(bodies.length);

        for (Body body : bodies)
        {
            String objectName = body.getStrObjName();

            ResponseBody responseBody = (ResponseBody)body;
            if (OBJECTNAME_SQL_STATEMENT.equals(objectName) == true)
            {
                Object[] objItemValueArr = responseBody.getObjItemValueArr();
                if (objItemValueArr.length == 0)
                {
                    continue;
                }

                String sqlStatement = (String)objItemValueArr[0];
                sqlStatements.add(sqlStatement);

                String itemName = body.getStrItemName();
                measurmentItemNames.add(itemName);
            }
            else if (OBJECTNAME_SQL_EXECUTION_PLAN.equals(objectName) == true)
            {
                Object[] objItemValueArr = responseBody.getObjItemValueArr();
                if (objItemValueArr.length == 0)
                {
                    continue;
                }

                String sqlExecution = (String)objItemValueArr[0];
                executionPlans.add(sqlExecution);
            }
            else if (OBJECTNAME_GETTING_PLAN_TIME.equals(objectName) == true)
            {
                Object[] objItemValueArr = responseBody.getObjItemValueArr();
                if (objItemValueArr.length == 0)
                {
                    continue;
                }

                String gettingPlanTimeStr = (String)objItemValueArr[0];
                Timestamp timestamp;

                // TimeStamp�ւ̃p�[�X�����s�����ꍇ�́A���ݎ�����TimeStamp���쐬����
                try
                {
                    timestamp =
                        new Timestamp(new SimpleDateFormat("yyyy/MM/dd HH:mm:ss")
                            .parse(gettingPlanTimeStr).getTime());
                }
                catch (ParseException pEx)
                {
                    timestamp = new Timestamp(System.currentTimeMillis());

                    LOGGER.log("WECC0103", pEx);
                    pEx.printStackTrace();
                }
                gettingPlanTimes.add(timestamp);
            }
            else if (OBJECTNAME_STACK_TRACE.equals(objectName) == true)
            {
                Object[] objItemValueArr = responseBody.getObjItemValueArr();
                if (objItemValueArr.length == 0)
                {
                    continue;
                }

                String stackTrace = (String)objItemValueArr[0];
                stackTraces.add(stackTrace);
            }
        }

        int entriesSize = measurmentItemNames.size();
        if (entriesSize != sqlStatements.size() || entriesSize != executionPlans.size()
            || entriesSize != gettingPlanTimes.size() || entriesSize != stackTraces.size())
        {
            return null;
        }

        SqlPlanEntry[] entries = new SqlPlanEntry[entriesSize];
        for (int i = 0; i < measurmentItemNames.size(); i++)
        {
            entries[i] = new SqlPlanEntry();
            entries[i].measurementItemName = measurmentItemNames.get(i);
            entries[i].sqlStatement = sqlStatements.get(i);
            entries[i].executionPlan = executionPlans.get(i);
            entries[i].gettingPlanTime = gettingPlanTimes.get(i);
            entries[i].stackTrace = stackTraces.get(i);
        }

        return entries;
    }

    /**
     * �d����SQL���s�v��ʒm�d���ł��邱�Ƃ��m�F����B
     * 
     * @param telegram SQL���s�v��ʒm�d��
     * @return �d����SQL���s�v��ʒm�d���̏ꍇ��ture�A�����łȂ��ꍇ��false
     */
    private static boolean checkTelegram(final Telegram telegram)
    {
        Header header = telegram.getObjHeader();
        return BYTE_TELEGRAM_KIND_SQL_PLAN == header.getByteTelegramKind() ? true : false;
    }

    /**
     * SQL���s�v���ێ����邽�߂̃N���X�ł��B<br />
     * 
     * @author miyasaka
     */
    public static class SqlPlanEntry
    {
        /** �v�����B */
        public String measurementItemName;

        /** SQL���B */
        public String sqlStatement;

        /** SQL�̎��s�v��B */
        public String executionPlan;

        /** ���s�v�悪�擾�ł������ԁB */
        public Timestamp gettingPlanTime;
        
        /** �X�^�b�N�g���[�X�B */
        public String stackTrace;
    }
}
