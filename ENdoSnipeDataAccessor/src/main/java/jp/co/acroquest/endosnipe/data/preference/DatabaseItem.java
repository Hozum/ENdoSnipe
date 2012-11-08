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
package jp.co.acroquest.endosnipe.data.preference;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;

import jp.co.acroquest.endosnipe.data.dao.JavelinLogDao;
import jp.co.acroquest.endosnipe.data.dao.MeasurementValueDao;
import jp.co.acroquest.endosnipe.data.entity.HostInfo;
import jp.co.acroquest.endosnipe.data.service.HostInfoManager;

/**
 * �v���t�@�����X�y�[�W�ɕ\������f�[�^�x�[�X�ꗗ�� 1 ���ڂ�\���N���X�B<br />
 *
 * @author sakamoto
 */
public class DatabaseItem
{
    private final String databaseName_;

    private HostInfo hostInfo_;

    private long startTime_;

    private long endTime_;

    /**
     * �v���t�@�����X�y�[�W�ɕ\������f�[�^�x�[�X�ꗗ�� 1 ���ڂ𐶐����܂��B<br />
     *
     * @param databaseName �f�[�^�x�[�X��
     */
    private DatabaseItem(final String databaseName)
        throws SQLException
    {
        this.databaseName_ = databaseName;
    }

    /**
     * �v���t�@�����X�y�[�W�ɕ\������f�[�^�x�[�X�ꗗ�� 1 ���ڂ𐶐����܂��B<br />
     *
     * @param databaseName �f�[�^�x�[�X�� 
     * @return {@link DatabaseItem} �I�u�W�F�N�g
     * @throws SQLException �f�[�^�x�[�X����l���擾�ł��Ȃ������ꍇ
     */
    public static DatabaseItem createDatabaseItem(final String databaseName)
        throws SQLException
    {
        DatabaseItem databaseItem = new DatabaseItem(databaseName);
        if (databaseItem.init())
        {
            return databaseItem;
        }
        return null;
    }

    /**
     * �t�B�[���h�̏��������s���܂��B<br />
     *
     * @return �������ɐ��������ꍇ�� <code>true</code> �A���s�����ꍇ�� <code>false</code>
     * @throws SQLException �f�[�^�x�[�X����l���擾�ł��Ȃ������ꍇ
     */
    private boolean init()
        throws SQLException
    {
        this.hostInfo_ = HostInfoManager.getHostInfo(this.databaseName_, true);
        if (this.hostInfo_ == null)
        {
            return false;
        }

        Timestamp[] javelinLogTerm = JavelinLogDao.getLogTerm(this.databaseName_);
        Timestamp[] measurementTerm = MeasurementValueDao.getTerm(this.databaseName_);
        if (javelinLogTerm.length == 2 && measurementTerm.length == 2)
        {
            // Javelin���O�ƃO���t�o�͗p�f�[�^�����ɑ��݂���ꍇ
            if (javelinLogTerm[0] != null && measurementTerm[0] != null)
            {
                this.startTime_ =
                        Math.min(javelinLogTerm[0].getTime(), measurementTerm[0].getTime());
            }
            if (javelinLogTerm[1] != null && measurementTerm[1] != null)
            {
                this.endTime_ = Math.min(javelinLogTerm[1].getTime(), measurementTerm[1].getTime());
            }
        }
        else if (javelinLogTerm.length == 2)
        {
            // Javelin���O�f�[�^�݂̂��i�[����Ă���ꍇ
            this.startTime_ = javelinLogTerm[0].getTime();
            this.endTime_ = javelinLogTerm[1].getTime();
        }
        else if (measurementTerm.length == 2)
        {
            // �O���t�o�͗p�Ńf�[�^�݂̂��i�[����Ă���ꍇ
            this.startTime_ = measurementTerm[0].getTime();
            this.endTime_ = measurementTerm[1].getTime();
        }
        else
        {
            // ���Ƀf�[�^���i�[����Ă��Ȃ��ꍇ
            this.startTime_ = 0;
            this.endTime_ = 0;
        }
        return true;
    }

    /**
     * �f�[�^�x�[�X����Ԃ��܂��B<br />
     *
     * @return �f�[�^�x�[�X��
     */
    public String getDatabaseName()
    {
        return this.databaseName_;
    }

    /**
     * �z�X�g����Ԃ��܂��B<br />
     *
     * @return �z�X�g���i�f�[�^�x�[�X���s���ȏꍇ�� <code>null</code> �j
     */
    public String getHostName()
    {
        if (this.hostInfo_ == null)
        {
            return null;
        }
        return this.hostInfo_.hostName;
    }

    /**
     * IP �A�h���X��Ԃ��܂��B<br />
     *
     * @return IP �A�h���X�i�f�[�^�x�[�X���s���ȏꍇ�� <code>null</code> �j
     */
    public String getIpAddress()
    {
        if (this.hostInfo_ == null)
        {
            return null;
        }
        return this.hostInfo_.ipAddress;
    }

    /**
     * �|�[�g�ԍ���Ԃ��܂��B<br />
     *
     * @return �|�[�g�ԍ��i�f�[�^�x�[�X���s���ȏꍇ�� <code>-1</code> �j
     */
    public int getPort()
    {
        if (this.hostInfo_ == null)
        {
            return -1;
        }
        return this.hostInfo_.port;
    }

    /**
     * �z�X�g�̐�����Ԃ��܂��B<br />
     *
     * @return �z�X�g�̐���
     */
    public String getDescription()
    {
        if (this.hostInfo_ == null)
        {
            return "";
        }
        return this.hostInfo_.description;
    }

    /**
     * �f�[�^�x�[�X�ɒ~�ς���Ă�����̊��Ԃ̍ŏ�������Ԃ��܂��B<br />
     *
     * @return �f�[�^�x�[�X�ɒ~�ς���Ă�����̊��Ԃ̍ŏ������i�~���b�j
     */
    public long getStartTime()
    {
        return this.startTime_;
    }

    /**
     * �f�[�^�x�[�X�ɒ~�ς���Ă�����̊��Ԃ̍ő������Ԃ��܂��B<br />
     *
     * @return �f�[�^�x�[�X�ɒ~�ς���Ă�����̊��Ԃ̍ő�����i�~���b�j
     */
    public long getEndTime()
    {
        return this.endTime_;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("{databaseName=");
        builder.append(this.databaseName_);
        builder.append(",hostInfo=");
        builder.append(this.hostInfo_);
        builder.append(",startTime=");
        builder.append((this.startTime_ != 0) ? new Date(this.startTime_) : "none");
        builder.append(",endTime=");
        builder.append((this.endTime_ != 0) ? new Date(this.endTime_) : "none");
        builder.append("}");
        return builder.toString();
    }
}
