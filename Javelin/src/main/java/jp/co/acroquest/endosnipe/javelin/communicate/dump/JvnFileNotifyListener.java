package jp.co.acroquest.endosnipe.javelin.communicate.dump;

import jp.co.acroquest.endosnipe.communicator.AbstractTelegramListener;
import jp.co.acroquest.endosnipe.communicator.TelegramListener;
import jp.co.acroquest.endosnipe.communicator.accessor.JvnFileNotifyAccessor;
import jp.co.acroquest.endosnipe.communicator.accessor.JvnFileNotifyAccessor.JvnFileEntry;
import jp.co.acroquest.endosnipe.communicator.entity.Telegram;
import jp.co.acroquest.endosnipe.communicator.entity.TelegramConstants;

/**
 * Jvn���O�ʒm���󂯎��B
 * @author eriguchi
 */
public class JvnFileNotifyListener extends AbstractTelegramListener implements TelegramListener
{
    /** �^�C���A�E�g�Ɏg�p����I�u�W�F�N�g�B */
    private Object timeoutObject_;

    private JvnFileEntry[] entries_;

    /**
     * ���X�i���쐬����B
     * @param timeoutObject �^�C���A�E�g�҂�������I�u�W�F�N�g�B
     */
    public JvnFileNotifyListener(Object timeoutObject)
    {
        this.timeoutObject_ = timeoutObject;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Telegram doReceiveTelegram(final Telegram telegram)
    {
        this.entries_ = JvnFileNotifyAccessor.getJvnFileEntries(telegram);

        synchronized (this.timeoutObject_)
        {
            this.timeoutObject_.notifyAll();
        }
        return null;
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
        return TelegramConstants.BYTE_TELEGRAM_KIND_JVN_FILE;
    }

    /**
     * ���ʂ��擾����B
     * 
     * @return ���ʂ�Jvn���O�B
     */
    public JvnFileEntry[] getEntries()
    {
        return entries_;
    }

}
