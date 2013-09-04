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
package jp.co.acroquest.endosnipe.javelin.jdbc.stats;

import java.sql.Timestamp;

import jp.co.acroquest.endosnipe.common.logger.SystemLogger;
import jp.co.acroquest.endosnipe.communicator.TelegramCreator;
import jp.co.acroquest.endosnipe.communicator.entity.Telegram;
import jp.co.acroquest.endosnipe.javelin.communicate.JavelinAcceptThread;
import jp.co.acroquest.endosnipe.javelin.communicate.JavelinConnectThread;

/**
 * SQL���s�v��̒ʒm�d���𑗐M����N���X�B
 * 
 * @author miyasaka
 *
 */
public class SqlPlanTelegramSender
{
    /**
     * SQL���s�v��̒ʒm�d���𑗐M����B
     * 
     * @param measurementItemName ���ږ�
     * @param sqlStatement SQL��
     * @param executionPlan ���s�v��
     * @param gettingPlanTime ���s�v��擾����
     * @param stackTrace �X�^�b�N�g���[�X
     */
    public void execute(final String measurementItemName, final String sqlStatement,
        final String executionPlan, final Timestamp gettingPlanTime, final String stackTrace)
    {
        // �N���C�A���g�����Ȃ��ꍇ�͓d�����쐬���Ȃ��B
        if (JavelinAcceptThread.getInstance().hasClient() == false
            && JavelinConnectThread.getInstance().isConnected() == false)
        {
            return;
        }

        // �ʒm�d�����쐬����B
        Telegram telegram = null;
        try
        {
            telegram =
                TelegramCreator.createSqlPlanTelegram(measurementItemName, sqlStatement,
                                                      executionPlan, gettingPlanTime, stackTrace);
        }
        catch (IllegalArgumentException ex)
        {
            SystemLogger logger = SystemLogger.getInstance();
            logger.warn(ex);
        }

        if (telegram == null)
        {
            return;
        }

        if (JavelinAcceptThread.getInstance().hasClient())
        {
            JavelinAcceptThread.getInstance().sendTelegram(telegram);
        }
        else
        {
            JavelinConnectThread.getInstance().sendTelegram(telegram);
        }
    }
}
