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
package jp.co.acroquest.endosnipe.javelin.converter.thread.monitor;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import jp.co.acroquest.endosnipe.common.config.JavelinConfig;
import jp.co.acroquest.endosnipe.common.event.EventConstants;
import jp.co.acroquest.endosnipe.common.logger.SystemLogger;
import jp.co.acroquest.endosnipe.communicator.entity.TelegramConstants;
import jp.co.acroquest.endosnipe.javelin.JavelinTransformer;
import jp.co.acroquest.endosnipe.javelin.StatsJavelinRecorder;
import jp.co.acroquest.endosnipe.javelin.event.CommonEvent;
import jp.co.acroquest.endosnipe.javelin.resource.ResourceCollector;
import jp.co.acroquest.endosnipe.javelin.util.ThreadUtil;

/**
 * �X���b�h�_���v���擾���邽�߂̃X���b�h�ł��B<br />
 * 
 * @author fujii
 *
 */
public class ThreadDumpMonitor implements Runnable
{
    /** Singleton�I�u�W�F�N�g */
    private static ThreadDumpMonitor instance__ = new ThreadDumpMonitor();

    /** Javelin�̐ݒ�B */
    private final JavelinConfig config_ = new JavelinConfig();

    /** �O���CPU���� */
    private long lastCpuTotalTime_ = 0;

    /** �O���CPU���� */
    private long lastCpuSystemTime_ = 0;

    /** �O���CPU���� */
    private long lastCpuIoWaitTime_ = 0;

    /** �O���JavaUp���� */
    private long lastUpTime_ = 0;

    /** �O��̒l */
    private Map<String, Double> prevValues_ = new ConcurrentHashMap<String, Double>();
    
    /** Java�A�b�v�^�C���̍��� */
    private long upTimeDif_ = 0;

    /** �v���Z�X�� */
    private int processorCount_;

    /** CPU�g�p�� */
    private CpuUsage cpuUsage_;

    /** �X���b�h�� */
    private int threadNum_;

    /** �e�X���b�h����CPU�g�p�� */
    private Map<Long, Double> threadCpuRateMap_;

    /** �O��̃X���b�h����CPU�g�p���� */
    private Map<Long, Long> lastThreadCpuMap_;

    /** ���\�[�X���擾�p�I�u�W�F�N�g */
    private static ResourceCollector collector__;

    /** CPU�g�p���ɕϊ����邽�߂̒萔 */
    private static final int CONVERT_RATIO = 10000;

    static
    {
        collector__ = ResourceCollector.getInstance();
    }

    /**
     * �C���X�^���X����j�~����v���C�x�[�g�R���X�g���N�^�B
     */
    private ThreadDumpMonitor()
    {
        // Do Nothing.
    }

    /**
     * {@link ThreadDumpMonitor}�I�u�W�F�N�g���擾���܂��B<br />
     * 
     * @return {@link ThreadDumpMonitor}�I�u�W�F�N�g
     */
    public static ThreadDumpMonitor getInstance()
    {
        return instance__;
    }

    /**
     * "javelin.thread.dump.interval"�̊Ԋu���ƂɁA
     * �X���b�h�_���v���o�͂��邩�ǂ������肵�܂��B
     */
    public void run()
    {
        try
        {
            Thread.sleep(JavelinTransformer.WAIT_FOR_THREAD_START);
        }
        catch (Exception ex)
        {
            SystemLogger.getInstance().debug(ex);
        }

        while (true)
        {
            try
            {
                int sleepTime = this.config_.getThreadDumpInterval();
                Thread.sleep(sleepTime);
                // �X���b�h����臒l���z���Ă���Ƃ��ACPU�g�p����臒l���z���Ă���Ƃ��ɁA
                // �t���X���b�h�_���v���o�͂���B
                synchronized (this)
                {
                    if (isThreadDump())
                    {
                        CommonEvent event = createThreadDumpEvent();
                        StatsJavelinRecorder.addEvent(event);
                    }
                }
            }
            catch (Throwable ex)
            {
                SystemLogger.getInstance().warn(ex);
            }
        }
    }

    /**
     * �X���b�h�_���v���o�͂��邩�ǂ�����Ԃ��܂��B<br />
     * �����͈ȉ���2��
     * <ol>
     * <li>�X���b�h�_���v�擾�t���O��<code>true</code></li>
     * <li>�X���b�h����臒l���z���Ă���A����CPU�g�p����臒l���z���Ă���</li>
     * </ol>
     * 
     * @return �X���b�h�_���v���o�͂���ꍇ�A<code>true</code>
     */
    private synchronized boolean isThreadDump()
    {
        // CPU�g�p���́A�����ɂ���Čv�Z���邽�߁A�ُ�l���o�Ȃ��悤�ɖ���v�Z����B
        this.cpuUsage_ = getCpuUssage();
        this.threadCpuRateMap_ = getThreadCpuRateMap(this.upTimeDif_);

        // �X���b�h�_���v�擾�t���O��OFF�̂Ƃ��ɂ́A�X���b�h�_���v���擾���Ȃ��B
        if (config_.isThreadDump() == false)
        {
            return false;
        }

        int threasholdCpuTotal = config_.getThreadDumpCpu();
        int threasholdCpuSystem = config_.getThreadDumpCpuSys();
        int threasholdCpuUser = config_.getThreadDumpCpuUser();
        if (this.cpuUsage_.getCpuTotal() > threasholdCpuTotal
            || this.cpuUsage_.getCpuSystem() > threasholdCpuSystem
            || this.cpuUsage_.getCpuTotal() - this.cpuUsage_.getCpuSystem()
                - this.cpuUsage_.getCpuIoWait() > threasholdCpuUser)
        {
            return true;
        }

        // �X���b�h����臒l���z���Ă���Ƃ��ɁA�X���b�h�_���v���o�͂���B
        int threasholdThread = config_.getThreadDumpThreadNum();
        this.threadNum_ = getThreadNum();
        if (this.threadNum_ > threasholdThread)
        {
            return true;
        }
        
        Map<String, Double> thresholdMap = config_.getThreadDumpResourceTreshold();

        for (Map.Entry<String, Double> entry : thresholdMap.entrySet())
        {
            String itemName = entry.getKey();
            double threshold = entry.getValue().doubleValue();
            boolean result =
                judgeThreshold(itemName, threshold);
            if (result)
            {
                return true;
            }
        }

        return false;
    }

    /**
     * �w�肵���n��臒l�𒴂��Ă��邩�ǂ����𔻒肷��B
     * 
     * @param itemName �n��
     * @param threshold 臒l
     * @return  �w�肵���n��臒l�𒴂��Ă��邩�ǂ����B
     */
    private boolean judgeThreshold(String itemName, double threshold)
    {
        double currentValue = 0;
        Number resource = collector__.getResource(itemName);
        if (resource != null)
        {
            currentValue = resource.doubleValue();
        }

        if (itemName.endsWith("(d)"))
        {
            Double prevValue = prevValues_.get(itemName);
            prevValues_.put(itemName, currentValue);
            if(prevValue != null)
            {
                currentValue -= prevValue.doubleValue();
            }
            
        }

        return threshold < currentValue;
    }

    /**
     * CPU�g�p�����擾���܂��B<br />
     * CPU�g�p��=(CPU���Ԃ̍���)/(Java��UP���� * �v���Z�b�T��)
     * 
     * @return CPU�g�p��
     */
    private synchronized CpuUsage getCpuUssage()
    {
        Number cpuTotal =
            collector__.getResource(TelegramConstants.ITEMNAME_PROCESS_CPU_TOTAL_TIME);
        Number cpuSystem =
            collector__.getResource(TelegramConstants.ITEMNAME_PROCESS_CPU_SYSTEM_TIME);
        Number cpuIoWait =
            collector__.getResource(TelegramConstants.ITEMNAME_PROCESS_CPU_IOWAIT_TIME);

        Number uptimeResource = collector__.getResource(TelegramConstants.ITEMNAME_JAVAUPTIME);
        Number processorResource =
            collector__.getResource(TelegramConstants.ITEMNAME_SYSTEM_CPU_PROCESSOR_COUNT);

        CpuUsage usage = new CpuUsage();
        if (cpuTotal == null || uptimeResource == null || processorResource == null)
        {
            return usage;
        }

        if (cpuSystem == null || cpuIoWait == null)
        {
            cpuSystem = 0;
            cpuIoWait = 0;
        }

        long cpuTotalTime = cpuTotal.longValue();
        long cpuSystemTime = cpuSystem.longValue();
        long cpuIoWaitTime = cpuIoWait.longValue();
        long upTime = uptimeResource.longValue();
        this.processorCount_ = processorResource.intValue();

        // CPU�g�p����臒l���z���Ă���Ƃ��ɁA�X���b�h�_���v���o�͂���B
        if (this.lastUpTime_ != 0)
        {
            this.upTimeDif_ = upTime - this.lastUpTime_;
            double cpuTotalUsage =
                (double)(cpuTotalTime - this.lastCpuTotalTime_)
                    / (this.upTimeDif_ * CONVERT_RATIO * this.processorCount_);
            double cpuSystemUsage =
                (double)(cpuSystemTime - this.lastCpuSystemTime_)
                    / (this.upTimeDif_ * CONVERT_RATIO * this.processorCount_);
            double cpuIoWaitUsage =
                (double)(cpuIoWaitTime - this.lastCpuIoWaitTime_)
                    / (this.upTimeDif_ * CONVERT_RATIO * this.processorCount_);

            usage.setCpuSystem(cpuSystemUsage);
            usage.setCpuUser(cpuTotalUsage - cpuSystemUsage - cpuIoWaitUsage);
            usage.setCpuSystem(cpuIoWaitUsage);
        }
        this.lastCpuTotalTime_ = cpuTotalTime;
        this.lastCpuSystemTime_ = cpuSystemTime;
        this.lastCpuIoWaitTime_ = cpuIoWaitTime;
        this.lastUpTime_ = upTime;

        return usage;
    }

    /**
     * �X���b�h�����擾���܂��B<br />
     * 
     * @return �X���b�h��
     */
    private synchronized int getThreadNum()
    {
        long[] threadIds = ThreadUtil.getAllThreadIds();
        return threadIds.length;
    }

    /**
     * �X���b�h����CPU���Ԃ�ۑ�����Map��Ԃ��܂��B<br />
     * 
     * @return �X���b�h����CPU�g�p���Ԃ�ۑ�����Map
     */
    private synchronized Map<Long, Long> getThreadCpuMap()
    {
        long[] threadIds = ThreadUtil.getAllThreadIds();

        Map<Long, Long> threadCpuMap = new LinkedHashMap<Long, Long>();
        long[] threadCpuTimes = ThreadUtil.getThreadCpuTime(threadIds);
        for (int num = 0; num < threadIds.length; num++)
        {
            long threadId = threadIds[num];
            threadCpuMap.put(threadId, threadCpuTimes[num]);
        }

        return threadCpuMap;

    }

    /**
     * �t���X���b�h�_���v�o�̓C�x���g���쐬���܂��B<br />
     * [�C�x���g�`��]<br />
     * javelin.thread.dump.threadNum=&lt;�X���b�h��&gt;<br />
     * javelin.thread.dump.cpu.total=&lt;CPU�g�p���̍��v�l&gt;<br />
     * javelin.thread.dump.cpu.&lt;�X���b�hID1&gt;=&lt;�X���b�h1��CPU�g�p��&gt;<br />
     * javelin.thread.dump.cpu.&lt;�X���b�hID2&gt;=&lt;�X���b�h2��CPU�g�p��&gt;<br />
     * javelin.thread.dump.cpu.&lt;�X���b�hID3&gt;=&lt;�X���b�h3��CPU�g�p��&gt;<br />
     * �E�E�E<br />
     * javelin.thread.dump=&lt;�t���X���b�h�_���v&gt;<br />
     * 
     * @return �t���X���b�h�_���v�o�́B
     */
    private synchronized CommonEvent createThreadDumpEvent()
    {
        CommonEvent event = new CommonEvent();

        String threadDump = ThreadUtil.getFullThreadDump();

        event.setName(EventConstants.NAME_THREAD_DUMP);
        event.addParam(EventConstants.PARAM_THREAD_DUMP_THREADNUM, String.valueOf(this.threadNum_));
        event.addParam(EventConstants.PARAM_THREAD_DUMP_CPU_TOTAL, String.valueOf(this.cpuUsage_));

        // �X���b�h����CPU�g�p�����o�͂���B
        for (Map.Entry<Long, Double> entry : this.threadCpuRateMap_.entrySet())
        {
            Long threadId = entry.getKey();
            Double cpuRate = entry.getValue();
            if (cpuRate != 0.0)
            {
                event.addParam(EventConstants.PARAM_THREAD_DUMP_CPU + '.' + threadId,
                               String.valueOf(cpuRate));
            }
        }

        event.addParam(EventConstants.PARAM_THREAD_DUMP, threadDump);

        return event;
    }

    /**
     * �X���b�h����CPU�g�p�����擾���܂��B<br />
     * 
     * @return �X���b�h����CPU�g�p��
     */
    private synchronized Map<Long, Double> getThreadCpuRateMap(long upTimeDif)
    {
        Map<Long, Long> threadCpuMap = getThreadCpuMap();
        Map<Long, Double> threadCpuRateMap = new LinkedHashMap<Long, Double>();
        for (Map.Entry<Long, Long> entry : threadCpuMap.entrySet())
        {
            Long threadId = entry.getKey();
            if (this.lastThreadCpuMap_ == null)
            {
                threadCpuRateMap.put(threadId, 0.0);
                continue;
            }
            Long cpuTime = entry.getValue();
            Long lastCpuTime = this.lastThreadCpuMap_.get(threadId);
            if (upTimeDif == 0)
            {
                threadCpuRateMap.put(threadId, 0.0);
            }
            else if (lastCpuTime != null)
            {
                double threadCpuRate =
                    (double)(cpuTime - lastCpuTime)
                        / (upTimeDif * CONVERT_RATIO * this.processorCount_);
                threadCpuRateMap.put(threadId, threadCpuRate);
            }
            else
            {
                double threadCpuRate =
                    (double)(cpuTime) / (upTimeDif * CONVERT_RATIO * this.processorCount_);
                threadCpuRateMap.put(threadId, threadCpuRate);
            }
        }
        this.lastThreadCpuMap_ = threadCpuMap;
        return threadCpuRateMap;
    }

    /**
     * �X���b�h�_���v�擾�C�x���g�𑗐M���܂��B<br />
     *
     * @param telegramId �d�� ID
     */
    public synchronized void sendThreadDumpEvent(final long telegramId)
    {
        this.cpuUsage_ = getCpuUssage();
        this.threadNum_ = getThreadNum();
        this.threadCpuRateMap_ = getThreadCpuRateMap(this.upTimeDif_);
        CommonEvent event = createThreadDumpEvent();
        StatsJavelinRecorder.addEvent(event, telegramId);
    }
}
