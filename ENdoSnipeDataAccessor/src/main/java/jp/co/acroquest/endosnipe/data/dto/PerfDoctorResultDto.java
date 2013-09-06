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
package jp.co.acroquest.endosnipe.data.dto;

import java.sql.Timestamp;
import java.text.MessageFormat;

/**
 * PerfDoctor�f�f���ʗp��Dto�N���X�B
 * 
 * @author hiramatsu
 *
 */
public class PerfDoctorResultDto
{
    /** ���OID */
    private long logId_;

    /** �������� */
    private Timestamp occurrenceTime_;

    /** �T�v */
    private String description_;

    /** ���x�� */
    private String level_;

    /** �N���X�� */
    private String className_;

    /** ���\�b�h�� */
    private String methodName_;

    /** ���O�t�@�C���� */
    private String logFileName_;

    /** �v�����ږ� */
    private String measurementItemName_;

    /** 臒l���߁E�񕜎��̊T�v�t�H�[�}�b�g */
    private static final String DESCRIPTION_FORMAT =
        "Measurement value {0} the threshold.(Threshold:{1}, DetectedValue:{2})";

    /**
     * ���OID���擾����B
     * 
     * @return ���OID
     */
    public long getLogId()
    {
        return logId_;
    }

    /**
     * ���OID��ݒ肷��B
     * 
     * @param logId ���OID
     */
    public void setLogId(long logId)
    {
        logId_ = logId;
    }

    /**
     * �����������擾����B
     * 
     * @return ��������
     */
    public Timestamp getOccurrenceTime()
    {
        return occurrenceTime_;
    }

    /**
     * ����������ݒ肷��B
     * 
     * @param occurrenceTime ��������
     */
    public void setOccurrenceTime(Timestamp occurrenceTime)
    {
        occurrenceTime_ = occurrenceTime;
    }

    /**
     * �T�v���擾����B
     * 
     * @return �T�v
     */
    public String getDescription()
    {
        return description_;
    }

    /**
     * �T�v��ݒ肷��B
     * 
     * @param alarmType �A���[�����
     * @param threshold 臒l
     * @param detectedValue �v���l
     */
    public void setDescription(String alarmType, double threshold, double detectedValue)
    {
        String description =
            MessageFormat.format(DESCRIPTION_FORMAT, new Object[]{alarmType, threshold,
                detectedValue});
        description_ = description;
    }

    /**
     * �T�v��ݒ肷��B
     * 
     * @param description �T�v
     */
    public void setDescription(String description)
    {
        description_ = description;
    }

    /**
     * ���x�����擾����B
     * 
     * @return ���x��
     */
    public String getLevel()
    {
        return level_;
    }

    /**
     * ���x����ݒ肷��B
     * 
     * @param level ���x��
     */
    public void setLevel(String level)
    {
        level_ = level;
    }

    /**
     * �v�����ږ����擾����B
     * 
     * @return �v�����ږ�
     */
    public String getMeasurementItemName()
    {
        return measurementItemName_;
    }

    /**
     * �v�����ږ���ݒ肷��B
     * 
     * @param measurementItemName �v�����ږ�
     */
    public void setMeasurementItemName(String measurementItemName)
    {
        measurementItemName_ = measurementItemName;
    }

    /**
     * �N���X�����擾����B
     * 
     * @return �N���X��
     */
    public String getClassName()
    {
        return className_;
    }

    /**
     * �N���X����ݒ肷��B
     * 
     * @param className �N���X��
     */
    public void setClassName(String className)
    {
        className_ = className;
    }

    /**
     * ���\�b�h�����擾����B
     * 
     * @return ���\�b�h��
     */
    public String getMethodName()
    {
        return methodName_;
    }

    /**
     * ���\�b�h����ݒ肷��B
     * 
     * @param methodName ���\�b�h��
     */
    public void setMethodName(String methodName)
    {
        methodName_ = methodName;
    }
    
    /**
     * ���O�t�@�C�������擾����B
     * 
     * @return ���O�t�@�C����
     */
    public String getLogFileName()
    {
        return logFileName_;
    }

    /**
     * ���O�t�@�C������ݒ肷��B
     * 
     * @param logFileName ���O�t�@�C����
     */
    public void setLogFileName(String logFileName)
    {
        logFileName_ = logFileName;
    }
}
