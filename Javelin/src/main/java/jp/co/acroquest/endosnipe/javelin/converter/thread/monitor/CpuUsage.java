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
package jp.co.acroquest.endosnipe.javelin.converter.thread.monitor;

/**
 * CPU�g�p���B
 * 
 * @author eriguchi
 */
public class CpuUsage
{
    /** CPU���[�U */
    private double cpuUser_;

    /** CPU�V�X�e�� */
    private double cpuSystem_;

    /** CPU�@I/O�ҋ@���� */
    private double cpuIoWait_;

    /**
     * CPU�g�p���̍��v���擾����B
     * 
     * @return CPU�g�p���̍��v
     */
    public double getCpuTotal()
    {
        return this.cpuSystem_ + this.cpuUser_ + this.cpuIoWait_;
    }

    /**
     * CPU���[�U���擾����B
     * 
     * @return CPU���[�U�B
     */
    public double getCpuUser()
    {
        return cpuUser_;
    }

    /**
     * CPU���[�U��ݒ肷��B
     * 
     * @param cpuUser CPU���[�U�B
     */
    public void setCpuUser(double cpuUser)
    {
        cpuUser_ = cpuUser;
    }

    /**
     * CPU���[�U���擾����B
     * 
     * @return CPU���[�U�B
     */
    public double getCpuSystem()
    {
        return cpuSystem_;
    }

    /**
     * CPU���[�U��ݒ肷��B
     * 
     * @param cpuSystem CPU���[�U�B
     */
    public void setCpuSystem(double cpuSystem)
    {
        cpuSystem_ = cpuSystem;
    }

    /**
     * CPU���[�U���擾����B
     * 
     * @return CPU���[�U�B
     */
    public double getCpuIoWait()
    {
        return cpuIoWait_;
    }

    /**
     * CPU���[�U��ݒ肷��B
     * 
     * @param cpuIoWait CPU���[�U�B
     */
    public void setCpuIoWait(double cpuIoWait)
    {
        cpuIoWait_ = cpuIoWait;
    }

}
