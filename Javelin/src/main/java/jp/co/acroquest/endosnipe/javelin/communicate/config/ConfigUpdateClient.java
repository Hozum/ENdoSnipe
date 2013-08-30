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
package jp.co.acroquest.endosnipe.javelin.communicate.config;

import java.util.ArrayList;
import java.util.List;

import jp.co.acroquest.endosnipe.communicator.entity.Body;
import jp.co.acroquest.endosnipe.communicator.entity.Header;
import jp.co.acroquest.endosnipe.communicator.entity.Telegram;
import jp.co.acroquest.endosnipe.communicator.entity.TelegramConstants;
import jp.co.acroquest.endosnipe.javelin.communicate.SimpleENdoSnipeClient;

/**
 * �ݒ�ύX�N���C�A���g
 * 
 * @author eriguchi
 */
public class ConfigUpdateClient extends SimpleENdoSnipeClient
{

    /** �����̑҂��󂯃��X�i�B */
    protected UpdatePropertyResponseListener listener_;

    /** �R���X�g���N�^ 
     * @param threadName �X���b�h��
     */
    public ConfigUpdateClient(String threadName)
    {
        super(threadName);
        listener_ = new UpdatePropertyResponseListener(timeoutObject_);
        client_.addTelegramListener(listener_);
    }

    /**
     * �X�V�����s����B
     * ���s���ʂ͕W���o�͂ɏo�͂���B��������̃��X�g�̏ꍇ�́A
     * �S�v���p�e�B�����T�[�o����擾���ĕW���o�͂ɏo�͂���B
     * 
     * @param list �X�V�Ώۂ̃v���p�e�B�̃��X�g�B
     */
    public void update(List<PropertyEntry> list)
    {
        Telegram updateRequest = createUpdateTelegram(list);
        client_.sendTelegram(updateRequest);

        // ������҂B
        if (listener_.getPropertyInfoList() == null)
        {
            try
            {
                synchronized (this.timeoutObject_)
                {
                    this.timeoutObject_.wait(TIMEOUT_MILLIS);
                }
            }
            catch (InterruptedException ex)
            {
                ex.printStackTrace();
            }

            if (listener_.getPropertyInfoList() == null)
            {
                System.out.println("update timeout");
                return;
            }
        }

        List<PropertyEntry> propertyInfoList = listener_.getPropertyInfoList();

        if (list.size() == 0)
        {
            printResult(propertyInfoList);
        }
        else
        {
            checkResult(list, propertyInfoList);
        }
    }

    /**
     * �X�V�̎��s���ʂ�W���o�͂ɏo�͂���B
     * 
     * @param propertyInfoList ���s����
     */
    private void printResult(List<PropertyEntry> propertyInfoList)
    {
        for (PropertyEntry entry : propertyInfoList)
        {
            System.out.println(entry.getProperty() + "=" + entry.getCurrentValue());
        }
    }

    /**
     * ���s���ʂ��A���Ғʂ肩���m�F����B
     * 
     * @param expectedList ���Ҍ��� 
     * @param acturalList ���ۂ̌���
     */
    private void checkResult(List<PropertyEntry> expectedList, List<PropertyEntry> acturalList)
    {
        for (PropertyEntry entry : acturalList)
        {
            for (PropertyEntry expected : expectedList)
            {
                if (expected.getProperty().equals(entry.getProperty()))
                {
                    if (expected.getUpdateValue().equals(entry.getCurrentValue()))
                    {
                        System.out.println("" + entry.getProperty() + "=" + entry.getCurrentValue()
                            + "(succeeded)");
                    }
                    else
                    {
                        System.out.println("Update failed. : " + entry.getProperty() + "="
                            + entry.getCurrentValue() + "(failed)");
                    }
                    break;
                }
            }
        }
    }

    /**
     * �T�[�o�v���p�e�B�ݒ�X�V�d�����쐬����B
     * 
     * @param propertyList �ݒ�ύX�������v���p�e�B�̃��X�g�B
     * @return�@�T�[�o�v���p�e�B�ݒ�X�V�d���B
     */
    private static Telegram createUpdateTelegram(List<PropertyEntry> propertyList)
    {
        Telegram telegram = new Telegram();
        Header header = new Header();
        header.setByteTelegramKind(TelegramConstants.BYTE_TELEGRAM_KIND_UPDATE_PROPERTY);
        header.setByteRequestKind(TelegramConstants.BYTE_REQUEST_KIND_REQUEST);
        telegram.setObjHeader(header);

        List<Body> updatePropertyList = new ArrayList<Body>();

        for (PropertyEntry property : propertyList)
        {
            String updateProperty = property.getProperty();
            String updateValue = property.getUpdateValue();

            if (updateValue == null || "".equals(updateValue))
            {
                continue;
            }

            Body addParam = new Body();
            addParam.setStrObjName(updateProperty);
            addParam.setStrItemName(updateValue);

            updatePropertyList.add(addParam);
        }

        Body[] updatePropertyArray =
            updatePropertyList.toArray(new Body[updatePropertyList.size()]);
        telegram.setObjBody(updatePropertyArray);

        return telegram;
    }

    /**
     * �G���g���|�C���g�B
     * 
     * @param args <host> <port> [<propertyKey>=<propertyValue]
     */
    public static void main(String[] args)
    {
        if (args.length < 2)
        {
            System.out.println("usage : <Main Class> <host> <port> [<propertyKey>=<propertyValue]");
            return;
        }

        String hostName = args[0];
        int port;
        try
        {
            port = Integer.parseInt(args[1]);
        }
        catch (NumberFormatException nfe)
        {
            nfe.printStackTrace();
            System.out.println("illegal port number : " + args[1]);
            return;
        }

        ConfigUpdateClient client = new ConfigUpdateClient("ConfigUpdateClient");

        List<PropertyEntry> list = new ArrayList<PropertyEntry>();

        for (int index = 2; index < args.length; index++)
        {
            String[] paramStr = args[index].split("=");
            if (paramStr.length < 2)
            {
                continue;
            }

            PropertyEntry propertyEntry = new PropertyEntry();
            String propertyKey = paramStr[0];
            String propertyValue = paramStr[1];
            propertyEntry.setProperty(propertyKey);
            propertyEntry.setUpdateValue(propertyValue);
            list.add(propertyEntry);
        }

        boolean success = client.connect(hostName, port);
        try
        {
            if (success)
            {
                client.update(list);
            }
        }
        finally
        {
            client.disconnect();
        }
    }
}
