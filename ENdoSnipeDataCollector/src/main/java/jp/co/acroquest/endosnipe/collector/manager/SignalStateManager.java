/*******************************************************************************
 * ENdoSnipe 5.0 - (https://github.com/endosnipe)
 * 
 * The MIT License (MIT)
 * 
 * Copyright (c) 2013 Acroquest Technology Co.,Ltd.
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
package jp.co.acroquest.endosnipe.collector.manager;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import jp.co.acroquest.endosnipe.collector.processor.AlarmData;

/**
 * �V�O�i���̏�Ԃ�ێ�����N���X
 * @author fujii
 *
 */
public class SignalStateManager
{
    /** SignalStateManager�C���X�^���X */
    public static SignalStateManager instance__ = new SignalStateManager();

    /** �A���[�����o�����ǂ������肷�邽�߂ɕێ���������f�[�^ */
    private final Map<String, AlarmData> alarmDataMap_ = new ConcurrentHashMap<String, AlarmData>();

    /**
     * �C���X�^���X����j�~����private�R���X�g���N�^�ł��B
     */
    private SignalStateManager()
    {
        // Do Nothing.
    }

    /**
     * {@link SignalStateManager}�C���X�^���X���擾����B
     * @return {@link SignalStateManager}�C���X�^���X
     */
    public static SignalStateManager getInstance()
    {
        return instance__;
    }

    /**
     * 臒l�����擾����B
     * @param signalId �V�O�i������ӂɂ��閼��
     * @return signalId�Ɉ�v����臒l���
     */
    public AlarmData getAlarmData(final String signalId)
    {
        return this.alarmDataMap_.get(signalId);
    }

    /**
     * 臒l����o�^����B
     * @param signalId �V�O�i������ӂɂ��閼��
     * @param alarmData 臒l���
     */
    public void addAlarmData(final String signalId, final AlarmData alarmData)
    {
        this.alarmDataMap_.put(signalId, alarmData);
    }

}
