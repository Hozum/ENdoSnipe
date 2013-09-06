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
package jp.co.acroquest.endosnipe.javelin.util;

/**
 * �X���b�h����ێ�����B
 * 
 * @author eriguchi
 */
public class DetailThreadInfo
{
    /** RUNNABLE�̃X���b�h�̐� */
    private int runnableCount_;

    /** BLOCKED�̃X���b�h�̐� */
    private int blockedCount_;

    /**
     * RUNNABLE�̃X���b�h�̐����擾����B
     * 
     * @return RUNNABLE�̃X���b�h�̐�
     */
    public int getRunnableCount()
    {
        return runnableCount_;
    }

    /**
     * RUNNABLE�̃X���b�h�̐���ݒ肷��B
     * 
     * @param runnableCount RUNNABLE�̃X���b�h�̐�
     */
    public void setRunnableCount(int runnableCount)
    {
        runnableCount_ = runnableCount;
    }

    /**
     * BLOCKED�̃X���b�h�̐����擾����B
     * 
     * @return BLOCKED�̃X���b�h�̐�
     */
    public int getBlockedCount()
    {
        return blockedCount_;
    }

    /**
     * BLOCKED�̃X���b�h�̐���ݒ肷��B
     * 
     * @param blockedCount BLOCKED�̃X���b�h�̐�
     */
    public void setBlockedCount(int blockedCount)
    {
        blockedCount_ = blockedCount;
    }

}
