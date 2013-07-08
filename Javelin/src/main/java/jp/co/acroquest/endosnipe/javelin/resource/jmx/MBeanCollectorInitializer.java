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
import java.lang.management.ManagementFactory;
import java.net.MalformedURLException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import javax.management.MBeanServer;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import jp.co.acroquest.endosnipe.common.config.JavelinConfigUtil;
import jp.co.acroquest.endosnipe.common.logger.SystemLogger;
import jp.co.acroquest.endosnipe.communicator.entity.TelegramConstants;
import jp.co.acroquest.endosnipe.javelin.resource.MultiResourceGetter;

/**
 * JMX�v���l���擾���邽�߂̃N���X���όn��p�}�b�v�ɓo�^���܂��B
 *
 * @author y_asazuma
 */
public class MBeanCollectorInitializer
{
    /** JMX�ڑ��p�X���b�h */
    private static JMXConnectThread              jmxConnectThread__;

    /** Javelin�ŊĎ�����JMX�̐ݒ�t�@�C�� */
    private static final String                  JMX_PROP                   =
                                                                              "../conf/jmx.properties";

    /** �ݒ�t�@�C����ObjectName��\���L�[ */
    private static final String                  PREFIX_OBJECTNAME          = "objectName.";

    /** �ݒ�t�@�C����attribute��\���L�[ */
    private static final String                  PREFIX_ATTRIBUTE           = "attribute.";

    /** JMX�̃����[�g�ڑ����s�����ǂ�����\���L�[ */
    private static final String                  PREFIX_JMX_ENABLE          = "jmx.remote.enable.";

    /** JMX�̃����[�g�ڑ���URL��\���L�[ */
    private static final String                  PREFIX_JMX_REMOTE_URL      = "jmx.remote.url.";

    /** JMX�̃����[�g���[�U��\���L�[ */
    private static final String                  PREFIX_JMX_REMOTE_USER     = "jmx.remote.user.";

    /** JMX�̃����[�g�p�X���[�h
     * ��\���L�[ */
    private static final String                  PREFIX_JMX_REMOTE_PASSWORD =
                                                                              "jmx.remote.password.";

    /** JMX�̃����[�g�ڑ��I�u�W�F�N�g��ێ�����Map */
    private static Map<String, JMXConnector>     jmxConnectorMap__          =
                                                                              new ConcurrentHashMap<String, JMXConnector>();

    /** JMX�̃����[�g�ڑ�����ێ�����Map */
    private static Map<String, JMXConnectEntity> jmxConnectEntityMap__      =
                                                                              new ConcurrentHashMap<String, JMXConnectEntity>();

    /**
     * �R���X�g���N�^
     */
    private MBeanCollectorInitializer()
    {

    };

    /**
     * ���\�[�X�擾�C���X�^���X���}�b�v�ɓo�^���܂��B
     *
     * @param multiResourceMap ���\�[�X�擾�C���X�^���X��o�^����}�b�v�i�όn��p�j
     */
    public static void init(Map<String, MultiResourceGetter> multiResourceMap)
    {
        Properties properties = JavelinConfigUtil.loadProperties(JMX_PROP);
        if (properties == null)
        {
            SystemLogger.getInstance().warn(JMX_PROP + " is null.");
            return;
        }

        jmxConnectThread__ = new JMXConnectThread();
        jmxConnectThread__.start();

        MBeanMultiResourceGetter getters = new MBeanMultiResourceGetter();
        Enumeration<?> enumetarion = properties.propertyNames();
        while (enumetarion.hasMoreElements())
        {
            String propKey = (String)enumetarion.nextElement();
            String objectStr = properties.getProperty(propKey);

            // PREFIX��"objectName."�łȂ��ꍇ�͓ǂݔ�΂�
            if (propKey.startsWith(PREFIX_OBJECTNAME) == false)
            {
                continue;
            }

            // �ݒ�t�@�C������I�u�W�F�N�g�̒�`���擾����
            String objectName = objectStr;

            String id = propKey.substring(PREFIX_OBJECTNAME.length());
            String attrListStr = properties.getProperty(PREFIX_ATTRIBUTE + id);
            String remoteEnableStr = properties.getProperty(PREFIX_JMX_ENABLE + id);
            boolean remoteEnable = Boolean.valueOf(remoteEnableStr);
            String remoteUrl = properties.getProperty(PREFIX_JMX_REMOTE_URL + id);
            String user = properties.getProperty(PREFIX_JMX_REMOTE_USER + id);
            String password = properties.getProperty(PREFIX_JMX_REMOTE_PASSWORD + id);

            MBeanServer mbeanServer = null;
            MBeanServerConnection mbeanServerConnection = null;
            JMXConnectEntity entity = null;
            if (remoteEnable)
            {
                entity = new JMXConnectEntity();
                entity.setId(id);
                entity.setUrl(remoteUrl);
                entity.setUser(user);
                entity.setPassword(password);
                jmxConnectEntityMap__.put(id, entity);

                try
                {
                    JMXServiceURL url = new JMXServiceURL(remoteUrl);
                    HashMap<String, String[]> env = new HashMap<String, String[]>();
                    String[] credentials = new String[]{user, password};
                    env.put(JMXConnector.CREDENTIALS, credentials);
                    JMXConnector connector = JMXConnectorFactory.connect(url, env);
                    jmxConnectorMap__.put(id, connector);
                    mbeanServerConnection = connector.getMBeanServerConnection();
                }
                catch (MalformedURLException muex)
                {
                    // JMX��URL�s���̏ꍇ�͍Đڑ��ł��Ȃ����߁A���̗v�f�ɏ���������B
                    SystemLogger.getInstance().warn(muex);
                    continue;
                }
                catch (IOException ioex)
                {
                    // �ڑ����s�̏ꍇ�͍Đڑ������������Ȃ����߁A�����͌p������B
                    reconnect(entity);
                    SystemLogger.getInstance().warn(ioex);
                }
            }
            else
            {
                mbeanServer = ManagementFactory.getPlatformMBeanServer();
            }
            // �ݒ�t�@�C�����瑮���̒�`���擾����
            // �ϐ�attrListStr�̒��g�͈ȉ��̌`���ɂȂ��Ă���
            //   <attribute n1>,<attribute n2>,...
            String[] attrList = attrListStr.split(",");
            for (String attrStr : attrList)
            {
                String attrName = attrStr;

                // �擾����JMX�̌v���l�̐ݒ�����o�͂���
                StringBuilder sb = new StringBuilder();
                sb.append("(JMX mesuerment) ");
                sb.append("ObjectName[").append(objectName).append("] ");
                sb.append("attribute[").append(attrName).append("] ");
                SystemLogger.getInstance().info(sb.toString());

                try
                {
                    // JMX�̌v���l���擾����N���X�����������Ēǉ�����
                    MBeanValueGetter getter =
                                              new MBeanValueGetter(mbeanServer,
                                                                   mbeanServerConnection,
                                                                   objectName, attrName,
                                                                   remoteEnable, id);
                    getters.addMBeanValueGetter(getter);
                    if (entity != null)
                    {
                        entity.addResource(getter);
                    }

                }
                catch (MalformedObjectNameException ex)
                {
                    SystemLogger.getInstance().warn(ex);
                }
            }
        }

        // �όn��p�̃��\�[�X�擾�Ƃ���JMX�̌v���l��o�^����
        multiResourceMap.put(TelegramConstants.ITEMNAME_JMX, getters);
    }

    /**
     * JMX�ڑ��I�u�W�F�N�g��ǉ�����B
     * @param id ID
     * @param connector {@link JMXConnector}�I�u�W�F�N�g
     */
    public static void addConnector(String id, JMXConnector connector)
    {
        jmxConnectorMap__.put(id, connector);
    }

    /**
     * �w�肵��ID�ɑΉ�����JMX�̍Đڑ��v�����s���B
     * @param id �Đڑ��v�����s��Entity��ID
     */
    public static void recconect(String id)
    {
        JMXConnectEntity entity = jmxConnectEntityMap__.get(id);
        if (entity != null)
        {
            reconnect(entity);
        }
    }

    /**
     * JMX�̍Đڑ��v�����s���B
     * @param entity JMX�̍Đڑ��v��
     */
    public static void reconnect(JMXConnectEntity entity)
    {
        synchronized (jmxConnectThread__)
        {
            jmxConnectThread__.addConnectEntity(entity);
            jmxConnectThread__.notifyAll();
        }
    }

    /**
     * JMX�̐ؒf�������s���B
     */
    public static void close()
    {
        for (Entry<String, JMXConnector> connectorEntry : jmxConnectorMap__.entrySet())
        {
            JMXConnector connector = connectorEntry.getValue();
            try
            {
                connector.close();
            }
            catch (IOException ex)
            {
                SystemLogger.getInstance().warn(ex);
            }
        }
    }
}
