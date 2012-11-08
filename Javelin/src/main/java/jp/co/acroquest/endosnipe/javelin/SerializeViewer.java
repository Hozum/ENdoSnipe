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
package jp.co.acroquest.endosnipe.javelin;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Map;

import jp.co.acroquest.endosnipe.javelin.bean.Component;
import jp.co.acroquest.endosnipe.javelin.bean.Invocation;

/**
 * �V���A���C�Y�r���[�A�[
 * �V���A���C�Y���ꂽ�t�@�C����ǂݍ��݁A���𓯂��t�H���_���ɏ����o���B
 * @author acroquest
 *
 */
public class SerializeViewer
{
    private static Map<String, Component> BeanMap_;

    private static final long MILLIS = 1000 * 1000;

    /**
     * �V���A���C�Y���ꂽ�t�@�C���̒��g��ʃt�@�C���ɏ����o�����ƂŁAView����B
     * @param args �V���A���C�Y���ꂽ�t�@�C��
     * 
     */
    public static void main(String[] args)
    {
        if (args.length == 0)
        {
            System.out.println("�t�@�C�����΃p�X�Ŏw�肵�Ă��������B");
            return;
        }
        File inputFilePath = new File(args[0]);
        if (!inputFilePath.getName().equals("serialize.dat"))
        {
            System.out.println("�t�@�C���̖��̂��Ԉ���Ă��܂��B�iserialize.dat�j");
            return;
        }
        else if (inputFilePath.isFile() == false)
        {
            System.out.println("�t�@�C�������݂��邩�A���邢�̓t�@�C�������Ԉ���Ă��Ȃ����m�F���Ă��������B");
            return;
        }

        FileWriter fw = null;
        File deserializeFile = new File(inputFilePath.getParent() + "/deserializedFile.csv");

        //�����Ɏw�肳�ꂽdat�t�@�C�����f�V���A���C�Y����B
        try
        {
            BeanMap_ = MBeanManagerSerializer.deserializeFile(args[0]);
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            return;
        }
        try
        {
            fw = new FileWriter(deserializeFile.getPath());
        }
        catch (IOException ioex)
        {
            ioex.printStackTrace();
            return;
        }
        BufferedWriter bw = new BufferedWriter(fw);
        try
        {
            String titles =
                    new String("���X�|���X," + "�v���Ώ�," + "�N���X��," + "���\�b�h��," + "TAT臒l," + "CPU臒l,"
                            + "�Ăяo����,"                    
                            +"���v��������(�ώZ),"+"���Ϗ�������(�ώZ),"+"�ő又������(�ώZ)," +
                            "�ŏ���������(�ώZ),"+"���vCPU����(�ώZ),"+" ����CPU����(�ώZ),"+
                            "�ő�CPU����(�ώZ),"+" �ŏ�CPU����(�ώZ),"+"���vUSER����(�ώZ),"+"����USER����(�ώZ),"
                            +"�ő�USER����(�ώZ),"+"�ŏ�USER����(�ώZ),"
                            +"���v��������," + "���Ϗ�������," + "�ő又������," + "�ŏ���������,"
                            + "���vCPU����," + "����CPU����," + "�ő�CPU����," + "�ŏ�CPU����," + "���vUSER����,"
                            + "����USER����," + "�ő�USER����," + "�ŏ�USER����," + "��O������\n");
            bw.write(titles);

            //�f�V���A���C�Y���ꂽ�t�@�C���ɕۑ�����Ă���SComponent�ɑ΂��āA�������s���B
            for (Component component : BeanMap_.values())
            {
                //�P��component���ɑ��݂���SInvocation�ɑ΂��āA�������s���B
                for (Invocation invocation : component.getAllInvocation())
                {
                    //Invocation����e�v�f��ǂݍ��ށB
                    String str = getContentsFromInvocation(invocation).toString();
                    bw.write(str);
                    bw.flush();
                }
            }
        }
        catch (IOException ioex)
        {
            ioex.printStackTrace();
            return;
        }
        try
        {
            bw.close();
            fw.close();
        }
        catch (IOException ioex)
        {
            ioex.printStackTrace();
            return;
        }
    }

    /**Invocation����e�v�f��ǂݍ��ށB*/
    private static StringBuilder getContentsFromInvocation(Invocation invocation)
    {
        StringBuilder sb = new StringBuilder();

        //���X�|���X�́Atrue��ON�Afalse��OFF�Ƃ��ďo�͂���B
        if (invocation.isResponseGraphOutputTarget() == true)
        {
            sb.append("ON,");
        }
        else
        {
            sb.append("OFF,");
        }
        //�v���Ώۂ́A�uON�v�uOFF�v�uNOT_SPECIFIED�v��3��ނ��o�͂���B
        sb.append(invocation.getMeasurementTarget() + ",");
        sb.append(invocation.getClassName() + ",");
        sb.append("\"" + invocation.getMethodName() + "\"" + ",");

        //�A���[�����������TAT��臒l�̒l��-1�̂Ƃ��́A�u���w��v�Əo�͂���B����ȊO�́A臒l�̒l���o�͂���B
        long TATThreshold = invocation.getAlarmThreshold();
        if (TATThreshold == -1)
        {
            sb.append("���w��,");
        }
        else
        {
            sb.append(TATThreshold + ",");
        }

        //�x���𔭐�������CPU���Ԃ�臒l�̒l��-1�̂Ƃ��́A�u���w��v�Əo�͂���B����ȊO�́A臒l�̒l���o�͂���B
        long CPUThreshold = invocation.getAlarmCpuThreshold();
        if (CPUThreshold == -1)
        {
            sb.append("���w��,");
        }
        else
        {
            sb.append(CPUThreshold + ",");
        }
        sb.append(invocation.getCount() + ",");
        
        //���Ϗ�������,����CPU����,����USER���Ԃ́A�����R�ʂ܂ŏo�͂���B����ȊO�͐����l���o�͂���B
        DecimalFormat decimalFormat = new DecimalFormat("0.000");
        
        sb.append(invocation.getAccumulatedTotal()+ ",");
       
        String accumulatedAverage = "0";
        if(invocation.getCount() == 0)
        {
            accumulatedAverage = decimalFormat.format(0);
        }
        else
        {
            accumulatedAverage = decimalFormat.format((double)invocation.getAccumulatedTotal()/invocation.getCount());
        }
        sb.append(accumulatedAverage+ ",");
        sb.append(invocation.getAccumulatedMaximum()+ ",");
        sb.append(invocation.getAccumulatedMinimum()+ ",");
        sb.append(invocation.getAccumulatedCpuTotal()/MILLIS+ ",");
        String accumulatedCpuAverage = decimalFormat.format((double)invocation.getAccumulatedCpuAverage()/MILLIS);
        sb.append(accumulatedCpuAverage+ ",");
        sb.append(invocation.getAccumulatedCpuMaximum()/MILLIS+ ",");
        sb.append(invocation.getAccumulatedCpuMinimum()/MILLIS+ ",");
        sb.append(invocation.getAccumulatedUserTotal()/MILLIS+ ",");
        String accumulatedUserAverage = decimalFormat.format((double)invocation.getAccumulatedUserAverage()/MILLIS);
        sb.append(accumulatedUserAverage + ",");
        sb.append(invocation.getAccumulatedUserMaximum()/MILLIS+ ",");
        sb.append(invocation.getAccumulatedUserMinimum()/MILLIS+ ",");
        
        sb.append(invocation.getTotal() + ",");

      
        //���Ϗ�������,����CPU����,����USER���Ԃ́A�����R�ʂ܂ŏo�͂���B
        String average = "0";
        if(invocation.getCount() == 0)
        {
            average = decimalFormat.format(0);
        }
        else
        {
            average = decimalFormat.format((double)invocation.getTotal() / invocation.getCount());
        }
        sb.append(average + ",");
        sb.append(invocation.getMaximum() + ",");
        sb.append(invocation.getMinimum() + ",");
        sb.append(invocation.getCpuTotal() / MILLIS + ",");
        //���Ϗ�������,����CPU����,����USER���Ԃ́A�����R�ʂ܂ŏo�͂���B
        String cpuAverage = decimalFormat.format((double)invocation.getCpuAverage() / MILLIS);
        sb.append(cpuAverage + ",");
        sb.append(invocation.getCpuMaximum() / MILLIS + ",");
        sb.append(invocation.getCpuMinimum() / MILLIS + ",");
        sb.append(invocation.getUserTotal() / MILLIS + ",");
        //���Ϗ�������,����CPU����,����USER���Ԃ́A�����R�ʂ܂ŏo�͂���B
        String userAverage = decimalFormat.format((double)invocation.getUserAverage() / MILLIS);
        sb.append(userAverage + ",");
        sb.append(invocation.getUserMaximum() / MILLIS + ",");
        sb.append(invocation.getUserMinimum() / MILLIS + ",");
        sb.append(invocation.getThrowableCount());
        sb.append("\n");
        return sb;
    }
}
