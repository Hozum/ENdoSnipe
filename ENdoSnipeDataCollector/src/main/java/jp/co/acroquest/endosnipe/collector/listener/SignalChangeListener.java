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

import jp.co.acroquest.endosnipe.collector.ENdoSnipeDataCollectorPluginProvider;
import jp.co.acroquest.endosnipe.collector.LogMessageCodes;
import jp.co.acroquest.endosnipe.collector.manager.SignalStateManager;
import jp.co.acroquest.endosnipe.common.logger.ENdoSnipeLogger;
import jp.co.acroquest.endosnipe.communicator.AbstractTelegramListener;
import jp.co.acroquest.endosnipe.communicator.TelegramListener;
import jp.co.acroquest.endosnipe.communicator.entity.Body;
import jp.co.acroquest.endosnipe.communicator.entity.Telegram;
import jp.co.acroquest.endosnipe.communicator.entity.TelegramConstants;
import jp.co.acroquest.endosnipe.data.dto.SignalDefinitionDto;
import jp.co.acroquest.endosnipe.data.entity.SignalDefinition;

/**
 * �V�O�i�����X�V���邽�߂̃N���X�ł��B<br />
 * 
 * @author fujii
 */
public class SignalChangeListener extends AbstractTelegramListener implements TelegramListener,
        LogMessageCodes
{
    private static final ENdoSnipeLogger LOGGER =
                                                  ENdoSnipeLogger.getLogger(SignalChangeListener.class,
                                                                            ENdoSnipeDataCollectorPluginProvider.INSTANCE);

    private static final int ITEM_VALUE_SIGNAL_ID = 0;

    private static final int ITEM_VALUE_SIGNAL_NAME = 1;

    private static final int ITEM_VALUE_SIGNAL_MATCHING_PATTERN = 2;

    private static final int ITEM_VALUE_LEVEL = 3;

    private static final int ITEM_VALUE_ESCALATION_PERIOD = 4;

    private static final int ITEM_VALUE_PATTERN_VALUE = 5;

    /**
     * {@inheritDoc}
     */
    @Override
    protected Telegram doReceiveTelegram(final Telegram telegram)
    {
        if (LOGGER.isDebugEnabled())
        {
            LOGGER.log(SIGNAL_DEFINITION_CHANGE_NOTIFY_RECEIVED);
        }

        // �d������͂��A�V�O�i����`�����X�V����B
        updateSignalDefinition(telegram);

        return null;
    }

    /**
     * 臒l��`�����X�V����B
     * @param telegram 臒l��`���d���ꗗ
     */
    private void updateSignalDefinition(final Telegram telegram)
    {
        SignalStateManager signalStateManager = SignalStateManager.getInstance();
        Body[] bodys = telegram.getObjBody();

        for (Body body : bodys)
        {
            String itemNameInTelegram = body.getStrItemName();
            if (TelegramConstants.ITEMNAME_SIGNAL_ADD.equals(itemNameInTelegram) == true)
            {
                Object[] itemValues = body.getObjItemValueArr();
                String signalId = (String)itemValues[ITEM_VALUE_SIGNAL_ID];
                String signalName = (String)itemValues[ITEM_VALUE_SIGNAL_NAME];
                String matchingPattern = (String)itemValues[ITEM_VALUE_SIGNAL_MATCHING_PATTERN];
                String level = (String)itemValues[ITEM_VALUE_LEVEL];
                String escalationPeriod = (String)itemValues[ITEM_VALUE_ESCALATION_PERIOD];
                String patternValue = (String)itemValues[ITEM_VALUE_PATTERN_VALUE];

                try
                {

                    SignalDefinition signalDefinition = new SignalDefinition();
                    signalDefinition.signalId = Long.parseLong(signalId);
                    signalDefinition.signalName = signalName;
                    signalDefinition.matchingPattern = matchingPattern;
                    signalDefinition.level = Integer.parseInt(level);
                    signalDefinition.escalationPeriod = Double.parseDouble(escalationPeriod);
                    signalDefinition.patternValue = patternValue;

                    SignalDefinitionDto signalDefinitionDto =
                                                              new SignalDefinitionDto(
                                                                                      signalDefinition);

                    signalStateManager.addSignalDefinition(signalDefinition.signalId,
                                                           signalDefinitionDto);
                }
                catch (NumberFormatException ex)
                {
                    // �V�O�i����`���̒ǉ��Ɏ��s
                }
            }
        }
        return;
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
        return TelegramConstants.BYTE_TELEGRAM_KIND_SIGNAL_DEFINITION;
    }

}
