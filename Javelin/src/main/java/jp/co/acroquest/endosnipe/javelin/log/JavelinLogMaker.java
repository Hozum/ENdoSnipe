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
package jp.co.acroquest.endosnipe.javelin.log;

import java.text.SimpleDateFormat;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jp.co.acroquest.endosnipe.common.config.JavelinConfig;
import jp.co.acroquest.endosnipe.common.parser.JavelinConstants;
import jp.co.acroquest.endosnipe.common.parser.JavelinLogConstants;
import jp.co.acroquest.endosnipe.javelin.CallTree;
import jp.co.acroquest.endosnipe.javelin.CallTreeNode;
import jp.co.acroquest.endosnipe.javelin.VMStatus;
import jp.co.acroquest.endosnipe.javelin.bean.Invocation;
import jp.co.acroquest.endosnipe.javelin.event.CommonEvent;
import jp.co.acroquest.endosnipe.javelin.helper.VMStatusHelper;
import jp.co.acroquest.endosnipe.javelin.util.StatsUtil;
import jp.co.acroquest.endosnipe.javelin.util.ThreadUtil;

/**
 * Javelin���O�̈�v�f�쐬����N���X�B
 *
 * @author eriguchi
 */
public class JavelinLogMaker implements JavelinConstants, JavelinLogConstants
{
    /** �_�u���N�H�[�e�[�V������\���p�^�[���B */
    private static final Pattern DOUBLE_QUOTATION_PATTERN = Pattern.compile("\"");

    /**
     * �C���X�^���X����j�~����v���C�x�[�g�R���X�g���N�^�B
     */
    protected JavelinLogMaker()
    {
        // Do Nothing.
    }

    /** �P�ʕϊ��萔(�i�m �� �~��) */
    private static final int NANO_TO_MILLI = 1000000;

    private static final String[] MESSAGE_TYPES =
            new String[]{MSG_CALL, MSG_RETURN, MSG_FIELD_READ, MSG_FIELD_WRITE, MSG_CATCH,
                    MSG_THROW, MSG_EVENT};

    private static final String NEW_LINE = "\r\n";

    /**
     * �C�x���g���O��������쐬���܂��B
     *
     * @param event �C�x���g�B
     * @param tree �R�[���c���[�B
     * @param node �m�[�h�B
     * @return �C�x���g���O������B
     */
    public static String createEventLog(final CommonEvent event, final CallTree tree,
            final CallTreeNode node)
    {
        if (event == null)
        {
            return null;
        }
        long time = event.getTime();
        if (time == 0L)
        {
            return null;
        }

        StringBuffer jvnBuffer = new StringBuffer();

        Invocation callee = node.getInvocation();
        if (callee == null)
        {
            return "";
        }

        jvnBuffer.append(MESSAGE_TYPES[ID_EVENT]);

        SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_PATTERN);
        jvnBuffer.append(",");
        jvnBuffer.append(dateFormat.format(time));

        // �C�x���g��
        addToJvnBuffer(event.getName(), jvnBuffer);

        // ���\�b�h��
        addToJvnBuffer(getValidMethodName(callee), jvnBuffer);

        // �N���X��
        addToJvnBuffer(callee.getClassName(), jvnBuffer);

        String levelStr = createLevelStr(event);

        // �x�����x��
        addToJvnBuffer(levelStr, jvnBuffer);

        // �X���b�hID
        addToJvnBuffer(tree.getThreadID(), jvnBuffer);
        jvnBuffer.append(NEW_LINE);

        jvnBuffer.append(JAVELIN_EVENTINFO_START);
        jvnBuffer.append(NEW_LINE);

        // �p�����[�^���o�͂���B
        for (Map.Entry<String, String> entry : event.getParamMap().entrySet())
        {
            String key = entry.getKey();
            Object value = entry.getValue();
            addParam(jvnBuffer, key, String.valueOf(value));
        }

        jvnBuffer.append(JAVELIN_EVENTINFO_END);
        jvnBuffer.append(NEW_LINE);

        String jvnMessage = jvnBuffer.toString();
        return jvnMessage;
    }

    /**
     * ���\�b�h���Ƀ_�u���N�H�[�e�[�V����������ꍇ�́A�����2�̃_�u���N�H�[�e�[�V�����ɒu�������A
     * ���s������ꍇ�͉��s���폜���ďo�͂���B
     *
     * @param invocation
     * @return
     */
    protected static String getValidMethodName(Invocation invocation)
    {
        String validMethodName = invocation.getMethodName();
        Matcher matcher = DOUBLE_QUOTATION_PATTERN.matcher(validMethodName);
        validMethodName = matcher.replaceAll("\"\"");
        return validMethodName.replaceAll("[\r\n]", " ");
    }

    protected static String createLevelStr(final CommonEvent event)
    {
        String levelStr;
        if (event.getLevel() == CommonEvent.LEVEL_ERROR)
        {
            levelStr = JavelinLogConstants.EVENT_ERROR;
        }
        else if (event.getLevel() == CommonEvent.LEVEL_WARN)
        {
            levelStr = JavelinLogConstants.EVENT_WARN;
        }
        else
        {
            levelStr = JavelinLogConstants.EVENT_INFO;
        }
        return levelStr;
    }

    /**
     *
     * @param messageType ���b�Z�[�W�^�C�v
     * @param time ����
     * @param tree {@link CallTree}�I�u�W�F�N�g
     * @param node {@link CallTreeNode}�I�u�W�F�N�g
     * @return Javelin���O�̓��e
     */
    public static String createJavelinLog(final int messageType, final long time,
            final CallTree tree, final CallTreeNode node)
    {
        if (time == 0L)
        {
            return null;
        }

        CallTreeNode parent = node.getParent();
        JavelinConfig config = new JavelinConfig();
        boolean isReturnDetail = config.isReturnDetail();

        StringBuffer jvnBuffer = new StringBuffer();

        Invocation callee = node.getInvocation();
        Invocation caller;
        if (parent == null)
        {
            String processName = VMStatusHelper.getProcessName();
            caller =
                    new Invocation(processName, tree.getRootCallerName(),
                                   JavelinLogConstants.DEFAULT_LOGMETHOD, 0);
        }
        else
        {
            caller = parent.getInvocation();
        }

        if (callee == null)
        {
            return "";
        }

        jvnBuffer.append(MESSAGE_TYPES[messageType]);

        SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_PATTERN);
        jvnBuffer.append(",");
        jvnBuffer.append(dateFormat.format(time));

        Throwable throwable = node.getThrowable();
        if (messageType == ID_THROW)
        {
            // ��O�N���X��
            addToJvnBuffer(throwable.getClass().getName(), jvnBuffer);

            // ��O�I�u�W�F�N�gID
            addToJvnBuffer(StatsUtil.getObjectID(throwable), jvnBuffer);
        }

        // �Ăяo���惁�\�b�h��
        addToJvnBuffer(getValidMethodName(callee), jvnBuffer);

        // �Ăяo����N���X��
        addToJvnBuffer(callee.getClassName(), jvnBuffer);

        // �Ăяo����I�u�W�F�N�gID
        addToJvnBuffer("unknown", jvnBuffer);

        if (messageType == ID_FIELD_READ || messageType == ID_FIELD_WRITE)
        {
            // �A�N�Z�X�����\�b�h��
            addToJvnBuffer("", jvnBuffer);

            // �A�N�Z�X���N���X��
            addToJvnBuffer("", jvnBuffer);

            // �A�N�Z�X���I�u�W�F�N�gID
            addToJvnBuffer("", jvnBuffer);

            // �A�N�Z�X��t�B�[���h�̌^
            addToJvnBuffer("", jvnBuffer);
        }
        else if (messageType == ID_CALL || messageType == ID_RETURN)
        {
            // �Ăяo�������\�b�h��
            addToJvnBuffer(getValidMethodName(caller), jvnBuffer);

            // �Ăяo�����N���X��
            addToJvnBuffer(caller.getClassName(), jvnBuffer);

            // �Ăяo�����I�u�W�F�N�gID
            addToJvnBuffer("unknown", jvnBuffer);

            // ���f�B�t�@�C�A
            addToJvnBuffer("", jvnBuffer);
        }

        // �X���b�hID
        addToJvnBuffer(tree.getThreadID(), jvnBuffer);
        jvnBuffer.append(NEW_LINE);

        int stringLimitLength = config.getStringLimitLength();
        String[] args = node.getArgs();
        if (messageType == ID_CALL && args != null && args.length > 0)
        {
            addArgs(jvnBuffer, stringLimitLength, args);
        }

        if (messageType == ID_RETURN)
        {
            String returnValue = node.getReturnValue();
            if (returnValue != null)
            {
                addReturn(jvnBuffer, returnValue, isReturnDetail, stringLimitLength);
            }
        }

        if (config.isLogMBeanInfo() || (config.isLogMBeanInfoRoot() && parent == null))
        {
            if (messageType == ID_CALL)
            {
                // VM���s���
                VMStatus startStatus = node.getStartVmStatus();
                VMStatus endStatus = node.getEndVmStatus();

                StringBuffer vmStatusBuffer = new StringBuffer();
                addVMStatusDiff(vmStatusBuffer, startStatus, endStatus);

                if (vmStatusBuffer.length() > 0)
                {
                    jvnBuffer.append(JAVELIN_JMXINFO_START);
                    jvnBuffer.append(NEW_LINE);

                    jvnBuffer.append(vmStatusBuffer);

                    jvnBuffer.append(JAVELIN_JMXINFO_END);
                    jvnBuffer.append(NEW_LINE);
                }
            }
        }

        if (messageType == ID_CALL)
        {
            jvnBuffer.append(JAVELIN_EXTRAINFO_START);
            jvnBuffer.append(NEW_LINE);

            long duration = node.getAccumulatedTime();
            if (duration >= 0)
            {
                addParam(jvnBuffer, EXTRAPARAM_DURATION, duration);
            }

            if (node.getParent() == null)
            {
                for (String key : tree.getLoggingKeys())
                {
                    Object value = tree.getLoggingValue(key);
                    addParam(jvnBuffer, key, String.valueOf(value));
                }
            }
            for (String key : node.getLoggingKeys())
            {
                Object value = node.getLoggingValue(key);
                addParam(jvnBuffer, key, value.toString());
            }

            jvnBuffer.append(JAVELIN_EXTRAINFO_END);
            jvnBuffer.append(NEW_LINE);
        }

        StackTraceElement[] stacktrace = node.getStacktrace();
        if (messageType == ID_CALL && stacktrace != null)
        {
            addStackTrace(jvnBuffer, stacktrace);
        }

        if (messageType == ID_THROW)
        {
            addThrowable(jvnBuffer, throwable);
        }
        return jvnBuffer.toString();
    }

    protected static void addStackTrace(final StringBuffer jvnBuffer,
            final StackTraceElement[] stacktrace)
    {
        jvnBuffer.append(JAVELIN_STACKTRACE_START);
        jvnBuffer.append(NEW_LINE);
        jvnBuffer.append(ThreadUtil.getStackTrace(stacktrace));
        jvnBuffer.append(JAVELIN_STACKTRACE_END);
        jvnBuffer.append(NEW_LINE);
    }

    protected static void addThrowable(final StringBuffer jvnBuffer, final Throwable throwable)
    {
        jvnBuffer.append(JAVELIN_STACKTRACE_START);
        jvnBuffer.append(NEW_LINE);
        jvnBuffer.append(throwable.getClass().getName() + ":" + throwable.getMessage());
        jvnBuffer.append(NEW_LINE);
        StackTraceElement[] stacktrace = throwable.getStackTrace();
        jvnBuffer.append(ThreadUtil.getStackTrace(stacktrace));
        jvnBuffer.append(JAVELIN_STACKTRACE_END);
        jvnBuffer.append(NEW_LINE);
    }

    protected static void addReturn(final StringBuffer jvnBuffer, final String returnValue,
            final boolean isReturnDetail, final int stringLimitLength)
    {
        jvnBuffer.append(JAVELIN_RETURN_START);
        jvnBuffer.append(NEW_LINE);
        jvnBuffer.append(StatsUtil.toStr(returnValue, stringLimitLength));
        jvnBuffer.append(NEW_LINE);
        jvnBuffer.append(JAVELIN_RETURN_END);
        jvnBuffer.append(NEW_LINE);
    }

    protected static void addArgs(final StringBuffer jvnBuffer, final int stringLimitLength,
            final String[] args)
    {
        jvnBuffer.append(JAVELIN_ARGS_START);
        jvnBuffer.append(NEW_LINE);
        for (int i = 0; i < args.length; i++)
        {
            jvnBuffer.append("args[");
            jvnBuffer.append(i);
            jvnBuffer.append("] = ");

            // ���s�v��i[PLAN] �Ŏn�܂�j�͌��ʂ̕������Z�k���Ȃ�
            if (args[i] != null && args[i].startsWith("[PLAN]"))
            {
                jvnBuffer.append(args[i]);
            }
            else
            {
                jvnBuffer.append(StatsUtil.toStr(args[i], stringLimitLength));
            }

            jvnBuffer.append(NEW_LINE);
        }
        jvnBuffer.append(JAVELIN_ARGS_END);
        jvnBuffer.append(NEW_LINE);
    }
    protected static void addVMStatusDiff(final StringBuffer jvnBuffer, final VMStatus startStatus,
            final VMStatus endStatus)
    {
        if (startStatus == null || endStatus == null)
        {
            return;
        }

        double cpuTimeDelta =
                (double)(endStatus.getCpuTime() - startStatus.getCpuTime()) / NANO_TO_MILLI;
        double userTimeDelta =
                (double)(endStatus.getUserTime() - startStatus.getUserTime()) / NANO_TO_MILLI;
        addParamDelta(jvnBuffer, JMXPARAM_THREAD_CURRENT_THREAD_CPU_TIME_DELTA,
                      String.valueOf(cpuTimeDelta));
        addParamDelta(jvnBuffer, JMXPARAM_THREAD_CURRENT_THREAD_USER_TIME_DELTA,
                      String.valueOf(userTimeDelta));
        addParamDelta(jvnBuffer, JMXPARAM_THREAD_THREADINFO_BLOCKED_COUNT_DELTA,
                      String.valueOf(endStatus.getBlockedCount() - startStatus.getBlockedCount()));
        addParamDelta(jvnBuffer, JMXPARAM_THREAD_THREADINFO_BLOCKED_TIME_DELTA,
                      String.valueOf(endStatus.getBlockedTime() - startStatus.getBlockedTime()));
        addParamDelta(jvnBuffer, JMXPARAM_THREAD_THREADINFO_WAITED_COUNT_DELTA,
                      String.valueOf(endStatus.getWaitedCount() - startStatus.getWaitedCount()));
        addParamDelta(jvnBuffer, JMXPARAM_THREAD_THREADINFO_WAITED_TIME_DELTA,
                      String.valueOf(endStatus.getWaitedTime() - startStatus.getWaitedTime()));
        addParamDelta(jvnBuffer, JMXPARAM_GARBAGECOLLECTOR_COLLECTION_COUNT_DELTA,
                      String.valueOf(endStatus.getCollectionCount()
                              - startStatus.getCollectionCount()));
        addParamDelta(jvnBuffer, JMXPARAM_GARBAGECOLLECTOR_COLLECTION_TIME_DELTA,
                      String.valueOf(endStatus.getCollectionTime()
                              - startStatus.getCollectionTime()));
    }

    protected static void addParamDelta(final StringBuffer jvnBuffer, final String paramName,
            final String paramValue)
    {
        if ("0.0".equals(paramValue) || "0".equals(paramValue))
        {
            return;
        }
        jvnBuffer.append(paramName);
        jvnBuffer.append(" = ");
        jvnBuffer.append(paramValue);
        jvnBuffer.append(NEW_LINE);
    }

    protected static void addParam(final StringBuffer jvnBuffer, final String paramName,
            final String paramValue)
    {
        jvnBuffer.append(paramName);
        jvnBuffer.append(" = ");
        jvnBuffer.append(paramValue);
        jvnBuffer.append(NEW_LINE);
    }

    protected static void addParam(final StringBuffer jvnBuffer, final String paramName,
            final long paramValue)
    {
        jvnBuffer.append(paramName);
        jvnBuffer.append(" = ");
        jvnBuffer.append(paramValue);
        jvnBuffer.append(NEW_LINE);
    }

    protected static void addToJvnBuffer(final String element, final StringBuffer jvnBuffer)
    {
        jvnBuffer.append(",\"");
        jvnBuffer.append(element);
        jvnBuffer.append("\"");
    }
}
