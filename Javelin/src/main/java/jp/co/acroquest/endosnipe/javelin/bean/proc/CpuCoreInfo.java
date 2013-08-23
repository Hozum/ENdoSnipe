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
package jp.co.acroquest.endosnipe.javelin.bean.proc;

/**
 * �R�A���Ƃ�CPU�g�p���ԁB
 * 
 * @author eriguchi
 */
public class CpuCoreInfo
{
    /** CPU���[�U */
    private long cpuUser_;

    /** CPU�V�X�e�� */
    private long cpuSystem_;

    /** CPU�^�X�N */
    private long cpuTask_;

    /** CPU�@I/O�ҋ@���� */
    private long cpuIoWait_;

    /**
     * CPU���[�U���擾����B
     * 
     * @return CPU���[�U�B
     */
    public long getCpuUser()
    {
        return cpuUser_;
    }

    /**
     * CPU���[�U��ݒ肷��B
     * 
     * @param cpuUser CPU���[�U�B
     */
    public void setCpuUser(long cpuUser)
    {
        cpuUser_ = cpuUser;
    }

    /**
     * CPU���[�U���擾����B
     * 
     * @return CPU���[�U�B
     */
    public long getCpuSystem()
    {
        return cpuSystem_;
    }

    /**
     * CPU���[�U��ݒ肷��B
     * 
     * @param cpuSystem CPU���[�U�B
     */
    public void setCpuSystem(long cpuSystem)
    {
        cpuSystem_ = cpuSystem;
    }

    /**
     * CPU���[�U���擾����B
     * 
     * @return CPU���[�U�B
     */
    public long getCpuTask()
    {
        return cpuTask_;
    }

    /**
     * CPU���[�U��ݒ肷��B
     * 
     * @param cpuTask CPU���[�U�B
     */
    public void setCpuTask(long cpuTask)
    {
        cpuTask_ = cpuTask;
    }

    /**
     * CPU���[�U���擾����B
     * 
     * @return CPU���[�U�B
     */
    public long getCpuIoWait()
    {
        return cpuIoWait_;
    }

    /**
     * CPU���[�U��ݒ肷��B
     * 
     * @param cpuIoWait CPU���[�U�B
     */
    public void setCpuIoWait(long cpuIoWait)
    {
        cpuIoWait_ = cpuIoWait;
    }

}
