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
package jp.co.acroquest.endosnipe.javelin.communicate;

import java.util.Arrays;
import java.util.List;

import jp.co.acroquest.endosnipe.common.entity.ItemType;
import jp.co.acroquest.endosnipe.communicator.TelegramUtil;
import jp.co.acroquest.endosnipe.communicator.entity.Header;
import jp.co.acroquest.endosnipe.communicator.entity.ResponseBody;
import jp.co.acroquest.endosnipe.communicator.entity.Telegram;
import jp.co.acroquest.endosnipe.communicator.entity.TelegramConstants;
import jp.co.acroquest.endosnipe.javelin.MBeanManager;
import jp.co.acroquest.endosnipe.javelin.bean.Component;
import jp.co.acroquest.endosnipe.javelin.bean.ExcludeMonitor;
import jp.co.acroquest.endosnipe.javelin.bean.Invocation;
import jp.co.acroquest.endosnipe.javelin.util.ArrayList;

/**
 *  Invocation����d�����쐬����N���X�ł��B<br />
 * @author acroquest
 *
 */
public class JavelinTelegramCreator implements TelegramConstants
{
    private static final int TELEGRAM_ITEM_COUNT = 26;

    private JavelinTelegramCreator()
    {
        // Do Nothing.
    }

    /***
     * �S�Ă�Invocation�̃��X�g���擾���A�d���ɕϊ����܂��B<br />
     * 
     * @return �SInvocation�̃��X�g��d���ɕϊ��������́B
     */
    public static List<byte[]> createAll()
    {
        // �d���f�[�^�����
        Component[] objComponentArr = MBeanManager.getAllComponents();
        List<Invocation> invocationList = new ArrayList<Invocation>();

        // �d�����𓝌v����
        for (int i = 0; i < objComponentArr.length; i++)
        {
            invocationList.addAll(Arrays.asList(objComponentArr[i].getAllInvocation()));
        }

        Telegram objTelegram =
                create(invocationList, BYTE_TELEGRAM_KIND_GET, BYTE_REQUEST_KIND_RESPONSE);

        // �d���́Aobject �� byte[] �ɕϊ�����
        List<byte[]> byteList = TelegramUtil.createTelegram(objTelegram);

        // �ԋp����
        return byteList;
    }

    /**
     * �w�肵��invocation�̃��X�g����A���̃I�u�W�F�N�g��\���d�����쐬���܂��B<br />
     * 
     * @param invocations inovocation�̃��X�g
     * @param telegramKind �d�����
     * @param requestKind �v���������
     * @return invocation�̃��X�g����쐬�����d��
     */
    public static Telegram create(final List<Invocation> invocations, final byte telegramKind,
            final byte requestKind)
    {
        return create(invocations, null, telegramKind, requestKind);
    }

    /**
     * �w�肵��invocation�̃��X�g����A���̃I�u�W�F�N�g��\���d�����쐬���܂��B<br />
     * 
     * @param invocations inovocation�̃��X�g
     * @param accumulatedTimes inovocation�ɑΉ�����A�ݐώ��Ԃ̃��X�g
     * @param telegramKind �d�����
     * @param requestKind �v���������
     * @return invocation�̃��X�g����쐬�����d��
     */
    public static Telegram create(final List<Invocation> invocations,
            final List<Long> accumulatedTimes, final byte telegramKind, final byte requestKind)
    {
        return create(invocations, accumulatedTimes, telegramKind, requestKind, 0);
    }

    /**
     * �w�肵��invocation�̃��X�g����A���̃I�u�W�F�N�g��\���d�����쐬���܂��B<br />
     * 
     * @param invocations inovocation�̃��X�g
     * @param accumulatedTimes inovocation�ɑΉ�����A�ݐώ��Ԃ̃��X�g
     * @param telegramKind �d�����
     * @param requestKind �v���������
     * @param telegramId �d�� ID
     * @return invocation�̃��X�g����쐬�����d��
     */
    public static Telegram create(final List<Invocation> invocations,
            final List<Long> accumulatedTimes, final byte telegramKind, final byte requestKind,
            final long telegramId)
    {
        // �d�����������y�Ƃ肠�����A�d������ݒ肵�Ȃ��z
        Header objHeader = new Header();
        objHeader.setId(telegramId);
        objHeader.setByteRequestKind(requestKind);
        objHeader.setByteTelegramKind(telegramKind);

        // �d���{�̂����
        ResponseBody[] bodies = new ResponseBody[invocations.size() * TELEGRAM_ITEM_COUNT];

        for (int index = 0; index < invocations.size(); index++)
        {
            Invocation invocation = invocations.get(index);

            // �I�u�W�F�N�g�����擾����
            StringBuffer strObjName = new StringBuffer();
            strObjName.append(invocation.getClassName());
            strObjName.append(CLASSMETHOD_SEPARATOR);
            strObjName.append(invocation.getMethodName());
            String objName = strObjName.toString();

            // ���ڐ�����u���郊�X�g
            Object[] objItemValueArr = null;
            int bodyIndex = index * TELEGRAM_ITEM_COUNT;

            // �Ăяo����
            objItemValueArr = new Long[1];
            objItemValueArr[0] = Long.valueOf(invocation.getCount());
            bodies[bodyIndex + 0] =
                    TelegramUtil.createResponseBody(objName, ITEMNAME_CALL_COUNT,
                                                    ItemType.ITEMTYPE_LONG, objItemValueArr);

            // 2�ڈȍ~�͂ǂ̃N���X�A���\�b�h�̏�񂩖��炩�̂��߁A�N���X���A���\�b�h������ɂ���
            objName = "";

            // ��������
            objItemValueArr = new Long[1];
            if (accumulatedTimes != null && index < accumulatedTimes.size())
            {
                objItemValueArr[0] = Long.valueOf(accumulatedTimes.get(index));
            }
            else
            {
                objItemValueArr[0] = Long.valueOf(0);
            }
            bodies[bodyIndex + 1] =
                    TelegramUtil.createResponseBody(objName, ITEMNAME_CURRENT_INTERVAL,
                                                    ItemType.ITEMTYPE_LONG, objItemValueArr);

            // �ώZ���v����
            objItemValueArr = new Long[1];
            objItemValueArr[0] = Long.valueOf(invocation.getAccumulatedTotal());
            bodies[bodyIndex + 2] =
                    TelegramUtil.createResponseBody(objName, ITEMNAME_ACCUMULATED_TOTAL_INTERVAL,
                                                    ItemType.ITEMTYPE_LONG, objItemValueArr);
            // �ώZ�ő厞��
            objItemValueArr = new Long[1];
            objItemValueArr[0] = Long.valueOf(invocation.getAccumulatedMaximum());
            bodies[bodyIndex + 3] =
                    TelegramUtil.createResponseBody(objName, ITEMNAME_ACCUMULATED_MAXIMUM_INTERVAL,
                                                    ItemType.ITEMTYPE_LONG, objItemValueArr);
            // �ώZ�ŏ�����
            objItemValueArr = new Long[1];
            objItemValueArr[0] = Long.valueOf(invocation.getAccumulatedMinimum());
            bodies[bodyIndex + 4] =
                    TelegramUtil.createResponseBody(objName, ITEMNAME_ACCUMULATED_MINIMUM_INTERVAL,
                                                    ItemType.ITEMTYPE_LONG, objItemValueArr);
            
            // �ώZCPU���v����
            objItemValueArr = new Long[1];
            objItemValueArr[0] = Long.valueOf(invocation.getAccumulatedCpuTotal());
            bodies[bodyIndex + 5] =
                    TelegramUtil.createResponseBody(objName, ITEMNAME_ACCUMULATED_TOTAL_CPU_INTERVAL,
                                                    ItemType.ITEMTYPE_LONG, objItemValueArr);
            // �ώZCPU�ő厞��
            objItemValueArr = new Long[1];
            objItemValueArr[0] = Long.valueOf(invocation.getAccumulatedCpuMaximum());
            bodies[bodyIndex + 6] =
                    TelegramUtil.createResponseBody(objName, ITEMNAME_ACCUMULATED_MAXIMUM_CPU_INTERVAL,
                                                    ItemType.ITEMTYPE_LONG, objItemValueArr);
            // �ώZCPU�ŏ�����
            objItemValueArr = new Long[1];
            objItemValueArr[0] = Long.valueOf(invocation.getAccumulatedCpuMinimum());
            bodies[bodyIndex + 7] =
                    TelegramUtil.createResponseBody(objName, ITEMNAME_ACCUMULATED_MINIMUM_CPU_INTERVAL,
                                                    ItemType.ITEMTYPE_LONG, objItemValueArr);
            
            // �ώZUSER���v����
            objItemValueArr = new Long[1];
            objItemValueArr[0] = Long.valueOf(invocation.getAccumulatedUserTotal());
            bodies[bodyIndex + 8] =
                    TelegramUtil.createResponseBody(objName, ITEMNAME_ACCUMULATED_TOTAL_USER_INTERVAL,
                                                    ItemType.ITEMTYPE_LONG, objItemValueArr);
            // �ώZUSER�ő厞��
            objItemValueArr = new Long[1];
            objItemValueArr[0] = Long.valueOf(invocation.getAccumulatedUserMaximum());
            bodies[bodyIndex + 9] =
                    TelegramUtil.createResponseBody(objName, ITEMNAME_ACCUMULATED_MAXIMUM_USER_INTERVAL,
                                                    ItemType.ITEMTYPE_LONG, objItemValueArr);
            // �ώZUSER�ŏ�����
            objItemValueArr = new Long[1];
            objItemValueArr[0] = Long.valueOf(invocation.getAccumulatedUserMinimum());
            bodies[bodyIndex + 10] =
                    TelegramUtil.createResponseBody(objName, ITEMNAME_ACCUMULATED_MINIMUM_USER_INTERVAL,
                                                    ItemType.ITEMTYPE_LONG, objItemValueArr);
            
            // ���v����
            objItemValueArr = new Long[1];
            objItemValueArr[0] = Long.valueOf(invocation.getTotal());
            bodies[bodyIndex + 11] =
                    TelegramUtil.createResponseBody(objName, ITEMNAME_TOTAL_INTERVAL,
                                                    ItemType.ITEMTYPE_LONG, objItemValueArr);

            // �ő又������
            objItemValueArr = new Long[1];
            objItemValueArr[0] = Long.valueOf(invocation.getMaximum());
            bodies[bodyIndex + 12] =
                    TelegramUtil.createResponseBody(objName, ITEMNAME_MAXIMUM_INTERVAL,
                                                    ItemType.ITEMTYPE_LONG, objItemValueArr);

            // �ŏ���������
            objItemValueArr = new Long[1];
            objItemValueArr[0] = Long.valueOf(invocation.getMinimum());
            bodies[bodyIndex + 13] =
                    TelegramUtil.createResponseBody(objName, ITEMNAME_MINIMUM_INTERVAL,
                                                    ItemType.ITEMTYPE_LONG, objItemValueArr);

            // CPU���v����
            objItemValueArr = new Long[1];
            objItemValueArr[0] = Long.valueOf(invocation.getCpuTotal());
            bodies[bodyIndex + 14] =
                    TelegramUtil.createResponseBody(objName, ITEMNAME_TOTAL_CPU_INTERVAL,
                                                    ItemType.ITEMTYPE_LONG, objItemValueArr);

            // CPU�ő又������
            objItemValueArr = new Long[1];
            objItemValueArr[0] = Long.valueOf(invocation.getCpuMaximum());
            bodies[bodyIndex + 15] =
                    TelegramUtil.createResponseBody(objName, ITEMNAME_MAXIMUM_CPU_INTERVAL,
                                                    ItemType.ITEMTYPE_LONG, objItemValueArr);

            // CPU�ŏ���������
            objItemValueArr = new Long[1];
            objItemValueArr[0] = Long.valueOf(invocation.getCpuMinimum());
            bodies[bodyIndex + 16] =
                    TelegramUtil.createResponseBody(objName, ITEMNAME_MINIMUM_CPU_INTERVAL,
                                                    ItemType.ITEMTYPE_LONG, objItemValueArr);

            // User���v����
            objItemValueArr = new Long[1];
            objItemValueArr[0] = Long.valueOf(invocation.getUserTotal());
            bodies[bodyIndex + 17] =
                    TelegramUtil.createResponseBody(objName, ITEMNAME_TOTAL_USER_INTERVAL,
                                                    ItemType.ITEMTYPE_LONG, objItemValueArr);

            // User�ő又������
            objItemValueArr = new Long[1];
            objItemValueArr[0] = Long.valueOf(invocation.getUserMaximum());
            bodies[bodyIndex + 18] =
                    TelegramUtil.createResponseBody(objName, ITEMNAME_MAXIMUM_USER_INTERVAL,
                                                    ItemType.ITEMTYPE_LONG, objItemValueArr);

            // User�ŏ���������
            objItemValueArr = new Long[1];
            objItemValueArr[0] = Long.valueOf(invocation.getUserMinimum());
            bodies[bodyIndex + 19] =
                    TelegramUtil.createResponseBody(objName, ITEMNAME_MINIMUM_USER_INTERVAL,
                                                    ItemType.ITEMTYPE_LONG, objItemValueArr);

            // ��O������
            objItemValueArr = new Long[1];
            objItemValueArr[0] = Long.valueOf(invocation.getThrowableCount());
            bodies[bodyIndex + 20] =
                    TelegramUtil.createResponseBody(objName, ITEMNAME_JAVAPROCESS_EXCEPTION_OCCURENCE_COUNT,
                                                    ItemType.ITEMTYPE_LONG, objItemValueArr);

            // ���\�b�h�̌Ăяo���� �N���X��
            Invocation[] callerInvocations = invocation.getAllCallerInvocation();
            String[] callerNames = new String[callerInvocations.length];
            for (int callerIndex = 0; callerIndex < callerInvocations.length; callerIndex++)
            {
                callerNames[callerIndex] = callerInvocations[callerIndex].getClassName();
            }
            bodies[bodyIndex + 21] =
                    TelegramUtil.createResponseBody(objName, ITEMNAME_ALL_CALLER_NAMES,
                                                    ItemType.ITEMTYPE_STRING, callerNames);

            // �v���Ώۂ��ۂ�
            objItemValueArr = new String[1];
            boolean isTarget = isTarget(invocation);
            objItemValueArr[0] = String.valueOf(isTarget);
            bodies[bodyIndex + 22] =
                    TelegramUtil.createResponseBody(objName, ITEMNAME_TARGET,
                                                    ItemType.ITEMTYPE_STRING, objItemValueArr);

            // �g�����U�N�V�����O���t�o�͑Ώۂ��ۂ�
            objItemValueArr = new String[1];
            objItemValueArr[0] = String.valueOf(invocation.isResponseGraphOutputTarget());
            bodies[bodyIndex + 23] =
                    TelegramUtil.createResponseBody(objName, ITEMNAME_TRANSACTION_GRAPH,
                                                    ItemType.ITEMTYPE_STRING, objItemValueArr);

            // TAT�A���[��臒l
            objItemValueArr = new Long[1];
            objItemValueArr[0] = Long.valueOf(invocation.getAlarmThreshold());
            bodies[bodyIndex + 24] =
                    TelegramUtil.createResponseBody(objName, ITEMNAME_ALARM_THRESHOLD,
                                                    ItemType.ITEMTYPE_LONG, objItemValueArr);

            // CPU�A���[��臒l
            objItemValueArr = new Long[1];
            objItemValueArr[0] = Long.valueOf(invocation.getAlarmCpuThreshold());
            bodies[bodyIndex + 25] =
                    TelegramUtil.createResponseBody(objName, ITEMNAME_ALARM_CPU_THRESHOLD,
                                                    ItemType.ITEMTYPE_LONG, objItemValueArr);
            
/*            // HTTP�X�e�[�^�X�G���[��
            objItemValueArr = new Long[1];
            objItemValueArr[0] = Long.valueOf(invocation.getHttpStatusCount());
            bodies[bodyIndex + 26] =
                    TelegramUtil.createResponseBody(objName, ITEMNAME_JAVAPROCESS_HTTP_EXCEPTION,
                                                    ItemType.ITEMTYPE_LONG, objItemValueArr);
*/
        }

        // �d���I�u�W�F�N�g��ݒ肷��
        Telegram objTelegram = new Telegram();
        objTelegram.setObjHeader(objHeader);
        objTelegram.setObjBody(bodies);
        return objTelegram;
    }

    /**
     * �w�肵���N���X���A���\�b�h������v���Ώۂ��ǂ�����Ԃ��܂��B<br />
     * 
     * @param className �N���X��
     * @param methodName ���\�b�h��
     * @return �v���Ώۂł���ꍇ�ɁA<code>ture</code>
     */
    private static boolean isTarget(Invocation invocation)
    {
        boolean isTarget = ExcludeMonitor.isTarget(invocation);
        boolean isExclude = ExcludeMonitor.isExclude(invocation);
        if (isExclude == true)
        {
            return false;
        }
        if (isTarget == true)
        {
            return true;
        }
        return !ExcludeMonitor.isExcludePreffered(invocation);
    }
}
