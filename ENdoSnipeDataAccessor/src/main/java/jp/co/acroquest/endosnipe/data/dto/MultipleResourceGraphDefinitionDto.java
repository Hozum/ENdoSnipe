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
package jp.co.acroquest.endosnipe.data.dto;

import java.util.regex.Pattern;

import jp.co.acroquest.endosnipe.data.entity.MultipleResourceGraphDefinition;

/**
 * 臒l���������`���Dto�ł��B<br />
 * 
 * @author fujii
 *
 */
public class MultipleResourceGraphDefinitionDto
{
    /** �V�O�i����`�e�[�u����ID */
    private long                 multipleResourceGraphId_;

    /** �V�O�i���� */
    private String               multipleResourceGraphName_;

    /** �}�b�`���O�p�^�[�� */
    private String               measurementItemIdList_;

    /** 臒l�̃X�v���b�g�p�^�[�� */
    private static final Pattern SPLIT_PATERN = Pattern.compile(",");

    /**
     * �V�O�i����`�e�[�u����ID���擾����B
     * @return �V�O�i����`�e�[�u����ID
     */
    public long getMultipleResourceGraphId()
    {
        return multipleResourceGraphId_;
    }

    /**
     * �V�O�i����`�e�[�u����ID��ݒ肷��B
     * @param multipleResourceGraphId �V�O�i����`�e�[�u����ID
     */
    public void setMultipleResourceGraphId(long multipleResourceGraphId)
    {
        this.multipleResourceGraphId_ = multipleResourceGraphId;
    }

    /**
     * �V�O�i�������擾����B
     * @return �V�O�i����
     */
    public String getMultipleResourceGraphName()
    {
        return multipleResourceGraphName_;
    }

    /**
     * �V�O�i������ݒ肷��B
     * @param multipleResourceGraphName �V�O�i����
     */
    public void setMultipleResourceGraphName(String multipleResourceGraphName)
    {
        this.multipleResourceGraphName_ = multipleResourceGraphName;
    }

    /**
     * �}�b�`���O�p�^�[�����擾����B
     * @return �}�b�`���O�p�^�[����
     */
    public String getMeasurementItemIdList()
    {
        return measurementItemIdList_;
    }

    /**
     * �}�b�`���O�p�^�[����ݒ肷��B
     * @param measurementItemIdList �}�b�`���O�p�^�[����
     */
    public void setMeasurementItemIdList(String measurementItemIdList)
    {
        this.measurementItemIdList_ = measurementItemIdList;
    }

    /**
     * {@link MultipleResourceGraphDefinitionDto} �I�u�W�F�N�g�𐶐����܂��B
     * @param MultipleResourceGraphDefinition {@link MultipleResourceGraphDefinition}�I�u�W�F�N�g
     */
    public MultipleResourceGraphDefinitionDto(MultipleResourceGraphDefinition MultipleResourceGraphDefinition)
    {
        this.multipleResourceGraphId_ = MultipleResourceGraphDefinition.multipleResourceGraphId;
        this.multipleResourceGraphName_ = MultipleResourceGraphDefinition.multipleResourceGraphName;
        this.measurementItemIdList_ = MultipleResourceGraphDefinition.measurementItemIdList;
       
    }

    @Override
    public String toString()
    {
        return "MultipleResourceGraphInfoDto [multipleResourceGraphId=" + multipleResourceGraphId_ + ", multipleResourceGraphName=" + multipleResourceGraphName_
            + ", measurementItemIdList=" + measurementItemIdList_ +"]";
    }
}
