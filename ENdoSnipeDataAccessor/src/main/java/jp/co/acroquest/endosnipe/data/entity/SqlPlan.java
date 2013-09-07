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
package jp.co.acroquest.endosnipe.data.entity;

import java.sql.Timestamp;

/**
 * SQL_PLAN�e�[�u���ɑ΂���Entity�N���X�ł��B
 * 
 * @author miyasaka
 *
 */
public class SqlPlan
{
    /** �v�����B */
    public String measurementItemName;
    
    /** SQL���B */
    public String sqlStatement;
    
    /** SQL�̎��s�v��B */
    public String executionPlan;
    
    /** ���s�v�悪�擾�ł������ԁB */
    public Timestamp gettingPlanTime;
    
    /** �X�^�b�N�g���[�X�B */
    public String stackTrace;
}
