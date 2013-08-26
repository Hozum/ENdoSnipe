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
package jp.co.acroquest.endosnipe.javelin.communicate;

import jp.co.acroquest.endosnipe.communicator.CommunicationClient;
import jp.co.acroquest.endosnipe.communicator.CommunicatorListener;
import jp.co.acroquest.endosnipe.communicator.impl.CommunicationClientImpl;

/**
 * Javelin/DataCollector�ɐڑ����ēd�����M����N���C�A���g�B
 * 
 * @author eriguchi
 */
public class SimpleENdoSnipeClient
{
    /** �^�C���A�E�g���� */
    protected static final int TIMEOUT_MILLIS = 5000;

    /** �ʐM�p�N���C�A���g�B */
    protected CommunicationClient client_;

    /** �^�C���A�E�g�Ɏg�p����I�u�W�F�N�g�B */
    protected Object timeoutObject_;

    /** �R���X�g���N�^ 
     * @param threadName �X���b�h��
     */
    public SimpleENdoSnipeClient(String threadName)
    {
        client_ = new CommunicationClientImpl(threadName);
        timeoutObject_ = new Object();
    }

    /**
     * �w�肵���z�X�g�A�|�[�g��Javelin�ɐڑ�����B
     * 
     * @param host �z�X�g���B
     * @param port �|�[�g�ԍ��B
     * 
     * @return �ڑ��ɐ����������A���s�������B
     */
    public boolean connect(String host, int port)
    {
    
        client_.init(host, port);
    

        client_.addCommunicatorListener(new CommunicatorListener() {
    
            public void clientDisconnected(boolean forceDisconnected)
            {
                // �������Ȃ��B
            }
    
            public void clientConnected(String hostName, String ipAddress, int port)
            {
                synchronized (SimpleENdoSnipeClient.this.timeoutObject_)
                {
                    SimpleENdoSnipeClient.this.timeoutObject_.notifyAll();
                }
            }
        });
    
        client_.connect(null);
    
        if (client_.isConnected() == false)
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
    
            if (client_.isConnected() == false)
            {
                System.out.println("connect timeout. ");
                return false;
            }
        }
    
        return true;
    }

    /**
     * �ؒf����B
     */
    public void disconnect()
    {
        client_.disconnect();
        client_.shutdown();
    }

}