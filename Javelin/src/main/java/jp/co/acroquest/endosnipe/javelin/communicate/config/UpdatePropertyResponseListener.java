package jp.co.acroquest.endosnipe.javelin.communicate.config;

import java.util.ArrayList;
import java.util.List;

import jp.co.acroquest.endosnipe.communicator.AbstractTelegramListener;
import jp.co.acroquest.endosnipe.communicator.entity.Body;
import jp.co.acroquest.endosnipe.communicator.entity.Telegram;
import jp.co.acroquest.endosnipe.communicator.entity.TelegramConstants;

/**
 *  �T�[�o�v���p�e�B�ݒ�X�V�̉�������M���āA���ʂ�ێ����郊�X�i�B
 * 
 * @author eriguchi
 */
public class UpdatePropertyResponseListener extends AbstractTelegramListener
{
    private List<PropertyEntry> propertyInfoList_;

    /** �^�C���A�E�g�Ɏg�p����I�u�W�F�N�g�B */
    private Object timeoutObject_;

    /**
     * �R���X�g���N�^�B
     * 
     * @param timeoutObject �Ăяo�����Ƃ̑҂����킹�Ɏg���I�u�W�F�N�g�B
     */
    public UpdatePropertyResponseListener(Object timeoutObject)
    {
        this.timeoutObject_ = timeoutObject;
    }

    /**
     * ��M�����v���p�e�B�ݒ�̃��X�g���擾����B
     * 
     * @return ��M�����v���p�e�B�ݒ�̃��X�g�B����M�̏ꍇ�Anull��Ԃ��B
     */
    public List<PropertyEntry> getPropertyInfoList()
    {
        return propertyInfoList_;
    }

    /**
     * �T�[�o�v���p�e�B�ݒ�X�V�̉�������M���āA���ʂ�ێ�����B
     *�@@param telegram �����d���B 
     * @return null��Ԃ��B
     */
    @Override
    protected Telegram doReceiveTelegram(Telegram telegram)
    {
        this.propertyInfoList_ = parsePropertyInfoList(telegram);

        synchronized (this.timeoutObject_)
        {
            this.timeoutObject_.notifyAll();
        }
        return null;
    }

    /**
     * �v��������ʁi�����j��Ԃ��B
     * @return �v��������ʁi�����j�B
     */
    @Override
    protected byte getByteRequestKind()
    {
        return TelegramConstants.BYTE_REQUEST_KIND_RESPONSE;
    }

    /**
     * �T�[�o�v���p�e�B�ݒ�X�V�̎�ʂ�Ԃ��B
     * @return �T�[�o�v���p�e�B�ݒ�X�V�̎�ʁB
     */
    @Override
    protected byte getByteTelegramKind()
    {
        return TelegramConstants.BYTE_TELEGRAM_KIND_UPDATE_PROPERTY;
    }

    /**
     * �d������e�[�u���ɕ\������v���p�e�B�����쐬����
     * 
     * @param telegram ��M�d��
     * @return �\������v���p�e�B���
     */
    List<PropertyEntry> parsePropertyInfoList(final Telegram telegram)
    {
        List<PropertyEntry> propertyInfoList = new ArrayList<PropertyEntry>();

        // ��M�d������e�[�u���ɕ\������f�[�^���쐬����
        Body[] bodyList = telegram.getObjBody();

        if (bodyList == null)
        {
            return propertyInfoList;
        }

        for (Body body : bodyList)
        {
            String propertyName = body.getStrObjName();
            String propertyDetail = "";
            String propertyValue = body.getStrItemName();

            if (propertyName == null || "".equals(propertyName))
            {
                continue;
            }

            PropertyEntry entry = new PropertyEntry();
            entry.setProperty(propertyName);
            entry.setPropertyDetail(propertyDetail);
            entry.setCurrentValue(propertyValue);
            propertyInfoList.add(entry);
        }
        return propertyInfoList;
    }
}
