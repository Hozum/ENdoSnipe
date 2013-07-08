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
package jp.co.acroquest.endosnipe.javelin.resource.jmx;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.management.MBeanServerConnection;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import jp.co.acroquest.endosnipe.common.logger.SystemLogger;

/**
 * JMX�̐ڑ����s�����߂̃X���b�h�N���X�ł��B
 * @author fujii
 *
 */
public class JMXConnectThread extends Thread
{
    /** �Đڑ��������s���܂ł̃X���[�v���� */
    private static final long             SLEEP_TIME    = 60 * 1000;

    private Map<String, JMXConnectEntity> resourceMap_  =
                                                          new ConcurrentHashMap<String, JMXConnectEntity>();

    /** ���[�v���p�����邩�ǂ�����\���t���O */
    private boolean                       continueFlag_ = true;

    /**
     * �R���X�g���N�^�ł��B
     */
    public JMXConnectThread()
    {
        super();
        setName("JMXConnectThread-" + getId());
        setDaemon(true);
    }

    /**
     * 
     */
    public void run()
    {
        while (continueFlag_)
        {
            try
            {
                synchronized (this)
                {
                    if (this.resourceMap_.size() == 0)
                    {
                        wait();
                    }
                }
                Set<Entry<String, JMXConnectEntity>> entitySet = resourceMap_.entrySet();
                Iterator<Entry<String, JMXConnectEntity>> it = entitySet.iterator();
                while (it.hasNext())
                {
                    Entry<String, JMXConnectEntity> entry = it.next();
                    JMXConnectEntity entity = entry.getValue();
                    boolean result = reconnect(entity);
                    if (result)
                    {
                        it.remove();
                    }
                }
                Thread.sleep(SLEEP_TIME);
            }
            catch (InterruptedException ex)
            {
                SystemLogger.getInstance().debug(ex);
            }
        }
    }

    /**
     * �Đڑ��������s���B
     * @param entity {@link JMXConnectEntity}�I�u�W�F�N�g
     * @return �Đڑ������ɐ��������ꍇ��true�A���s�����ꍇ��false��Ԃ��B
     */
    private boolean reconnect(JMXConnectEntity entity)
    {
        try
        {
            String id = entity.getId();
            String remoteUrl = entity.getUrl();
            String user = entity.getUser();
            String password = entity.getPassword();
            JMXServiceURL url = new JMXServiceURL(remoteUrl);
            HashMap<String, String[]> env = new HashMap<String, String[]>();
            String[] credentials = new String[]{user, password};
            env.put(JMXConnector.CREDENTIALS, credentials);
            JMXConnector connector = JMXConnectorFactory.connect(url, env);
            MBeanServerConnection connection = connector.getMBeanServerConnection();
            List<MBeanValueGetter> resourceList = entity.getResourceList();
            for (MBeanValueGetter resource : resourceList)
            {
                resource.setConnection(connection);
            }
            MBeanCollectorInitializer.addConnector(id, connector);
        }
        catch (MalformedURLException ex)
        {
            SystemLogger.getInstance().warn(ex);
            // URL���s���ȏꍇ�͍Đڑ��ł��Ȃ����߁A�ȍ~�̏������s��Ȃ��悤��true��Ԃ��B
            return true;
        }
        catch (IOException ex)
        {
            SystemLogger.getInstance().warn(ex);
            return false;
        }
        return true;
    }

    /**
     * �ڑ��Ώۂ�ǉ�����B
     * @param entity JMX�ڑ��Ώ�
     */
    public void addConnectEntity(JMXConnectEntity entity)
    {
        String id = entity.getId();
        if (resourceMap_.get(id) == null)
        {
            resourceMap_.put(id, entity);
        }
    }
}
