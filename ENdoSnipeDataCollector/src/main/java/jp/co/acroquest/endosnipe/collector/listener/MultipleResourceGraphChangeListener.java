/*******************************************************************************
 * ENdoSnipe 5.0 - (https://github.com/endosnipe)
 * 
 * The MIT License (MIT)
 * 
 * Copyright (c) 2012 Acroquest Technology Co.,Ltd.
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

import jp.co.acroquest.endosnipe.collector.LogMessageCodes;
import jp.co.acroquest.endosnipe.collector.manager.MultipleResourceGraphStateManager;
import jp.co.acroquest.endosnipe.common.logger.ENdoSnipeLogger;
import jp.co.acroquest.endosnipe.communicator.AbstractTelegramListener;
import jp.co.acroquest.endosnipe.communicator.TelegramListener;
import jp.co.acroquest.endosnipe.communicator.entity.Body;
import jp.co.acroquest.endosnipe.communicator.entity.Telegram;
import jp.co.acroquest.endosnipe.communicator.entity.TelegramConstants;
import jp.co.acroquest.endosnipe.data.dto.MultipleResourceGraphDefinitionDto;
import jp.co.acroquest.endosnipe.data.entity.MultipleResourceGraphDefinition;

/**
 * �V�O�i����`�ύX�v���d������M���邽�߂̃N���X�ł��B<br />
 * 
 * @author pin
 */
public class MultipleResourceGraphChangeListener extends AbstractTelegramListener implements
        TelegramListener, LogMessageCodes
{
    private static final ENdoSnipeLogger LOGGER =
                                                  ENdoSnipeLogger.getLogger(MultipleResourceGraphChangeListener.class);

    private static final int ITEM_VALUE_MUL_RES_GRAPH_ID = 0;

    private static final int ITEM_VALUE_MUL_RES_GRAPH_NAME = 1;

    private static final int ITEM_VALUE_MEASUREMENT_ITEM_LIST = 2;

    /**
     * {@inheritDoc}
     */
    @Override
    protected Telegram doReceiveTelegram(final Telegram telegram)
    {
        if (LOGGER.isDebugEnabled())
        {
            LOGGER.log(MULTIPLE_RESOURCE_GRAPH_DEFINITION_CHANGE_NOTIFY_RECEIVED);
        }

        // �d������͂��A�V�O�i����`�����X�V����B
        updateMultipleResourceGraphDefinition(telegram);

        return null;
    }

    /**
     * 臒l��`�����X�V����B
     * @param telegram 臒l��`���d���ꗗ
     */
    private void updateMultipleResourceGraphDefinition(final Telegram telegram)
    {
        Body[] bodys = telegram.getObjBody();

        for (Body body : bodys)
        {
            String itemName = body.getStrItemName();
            if (TelegramConstants.ITEMNAME_MUL_RES_GRAPH_ADD.equals(itemName)
                    || TelegramConstants.ITEMNAME_MUL_RES_GRAPH_UPDATE.equals(itemName))
            {
                setMultipleResourceGraphDefinition(body, itemName);
            }
            else if (TelegramConstants.ITEMNAME_MUL_RES_GRAPH_DELETE.equals(itemName))
            {
                deleteMultipleResourceGraphDefinition(body);
            }
        }
        return;
    }

    /**
     * 
     * @param body {@link Body}�I�u�W�F�N�g
     * @param itemName ���ږ�
     */
    private void setMultipleResourceGraphDefinition(final Body body, final String itemName)
    {
        MultipleResourceGraphStateManager multipleResourceGraphStateManager =
                                                                              MultipleResourceGraphStateManager.getInstance();

        Object[] itemValues = body.getObjItemValueArr();
        String multipleResourceGraphId = (String)itemValues[ITEM_VALUE_MUL_RES_GRAPH_ID];
        String multipleResourceGraphName = (String)itemValues[ITEM_VALUE_MUL_RES_GRAPH_NAME];
        String measurementItemList = (String)itemValues[ITEM_VALUE_MEASUREMENT_ITEM_LIST];

        try
        {

            MultipleResourceGraphDefinition multipleResourceGraphDefinition =
                                                                              new MultipleResourceGraphDefinition();
            multipleResourceGraphDefinition.multipleResourceGraphId =
                                                                      Long.parseLong(multipleResourceGraphId);
            multipleResourceGraphDefinition.multipleResourceGraphName = multipleResourceGraphName;
            multipleResourceGraphDefinition.measurementItemIdList = measurementItemList;

            MultipleResourceGraphDefinitionDto multipleResourceGraphDefinitionDto =
                                                                                    new MultipleResourceGraphDefinitionDto(
                                                                                                                           multipleResourceGraphDefinition);

            multipleResourceGraphStateManager.addMultipleResourceGraphDefinition(multipleResourceGraphDefinition.multipleResourceGraphId,
                                                                                 multipleResourceGraphDefinitionDto);

            if (TelegramConstants.ITEMNAME_MUL_RES_GRAPH_UPDATE.equals(itemName))
            {
                /* AlarmData alarmData =
                                       multipleResourceGraphStateManager.getAlarmData(multipleResourceGraphName);
                 if (alarmData != null)
                 {
                     alarmData.reset();
                 }*/
            }
        }
        catch (NumberFormatException ex)
        {
            // �V�O�i����`���̒ǉ��Ɏ��s
        }
    }

    /**
     * 
     * @param body {@link Body}�I�u�W�F�N�g
     * @param itemName ���ږ�
     */
    private void deleteMultipleResourceGraphDefinition(final Body body)
    {
        MultipleResourceGraphStateManager multipleResourceGraphStateManager =
                                                                              MultipleResourceGraphStateManager.getInstance();

        Object[] itemValues = body.getObjItemValueArr();
        String multipleResourceGraphIdStr = (String)itemValues[ITEM_VALUE_MUL_RES_GRAPH_ID];
        String multipleResourceGraphName = (String)itemValues[ITEM_VALUE_MUL_RES_GRAPH_NAME];

        try
        {
            long multipleResourceGraphId = Long.parseLong(multipleResourceGraphIdStr);
            multipleResourceGraphStateManager.removeMultipleResourceGraphDefinition(multipleResourceGraphId);
            // multipleResourceGraphStateManager.removeAlarmData(multipleResourceGraphName);
        }
        catch (NumberFormatException ex)
        {
            // �V�O�i����`���̒ǉ��Ɏ��s
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected byte getByteRequestKind()
    {
        return TelegramConstants.BYTE_REQUEST_KIND_NOTIFY;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected byte getByteTelegramKind()
    {
        return TelegramConstants.BYTE_TELEGRAM_KIND_MUL_RES_GRAPH_DEFINITION;
    }

}
