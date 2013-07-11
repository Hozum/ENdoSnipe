/*
 * Copyright (c) 2004-2009 SMG Co., Ltd. All Rights Reserved.
 * Please read the associated COPYRIGHTS file for more details.
 *
 * THE  SOFTWARE IS  PROVIDED BY  SMG Co., Ltd., WITHOUT  WARRANTY  OF
 * ANY KIND,  EXPRESS  OR IMPLIED,  INCLUDING BUT  NOT LIMITED  TO THE
 * WARRANTIES OF  MERCHANTABILITY,  FITNESS FOR A  PARTICULAR  PURPOSE
 * AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDER BE LIABLE FOR ANY
 * CLAIM, DAMAGES SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING
 * OR DISTRIBUTING THIS SOFTWARE OR ITS DERIVATIVES.
 */
package jp.co.acroquest.endosnipe.report.entity;

import java.sql.Timestamp;

/**
 * �n�񂲂ƂɃO���t���o�͂���ꍇ�̃f�[�^��
 * 1�ێ�����N���X�ł��B
 * 
 * @author ochiai
 */
public class ItemRecord
{
	/** �v������ */
    private Timestamp measurementTime_;
    
    /** ��Ԋ��ԕ��ρ@*/
    private double      value_;
    
    /** ��Ԋ��ԍő� */
    private double      valueMax_;
    
    /** ��Ԋ��ԍŏ� */
    private double      valueMin_;
    
    /**
	 * @return the measurementTime_
	 */
	public Timestamp getMeasurementTime()
	{
		return measurementTime_;
	}

	/**
	 * @param measurementTime the measurementTime_ to set
	 */
	public void setMeasurementTime(Timestamp measurementTime)
	{
		this.measurementTime_ = measurementTime;
	}
	
	/**
	 * @return the value_
	 */
	public double getValue()
	{
		return value_;
	}

	/**
	 * @param value the value_ to set
	 */
	public void setValue(double value)
	{
		this.value_ = value;
	}

	/**
	 * @return the valueMax_
	 */
	public double getValueMax()
	{
		return valueMax_;
	}

	/**
	 * @param valueMax the valueMax_ to set
	 */
	public void setValueMax(double valueMax)
	{
		this.valueMax_ = valueMax;
	}

	/**
	 * @return the valueMin_
	 */
	public double getValueMin()
	{
		return valueMin_;
	}

	/**
	 * @param valueMin the valueMin_ to set
	 */
	public void setValueMin(double valueMin)
	{
		this.valueMin_ = valueMin;
	}
}
