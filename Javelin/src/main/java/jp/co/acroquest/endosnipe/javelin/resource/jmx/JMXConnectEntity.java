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
package jp.co.acroquest.endosnipe.javelin.resource.jmx;

import java.util.ArrayList;
import java.util.List;

/**
 * JMX�̐ڑ����s�����߂̃G���e�B�e�B�ł��B
 * @author fujii
 *
 */
public class JMXConnectEntity
{
    /** ID */
    private String                 id_;

    /** JMX�̐ڑ���URL */
    private String                 url_;

    /** ���[�U */
    private String                 user_;

    /** �p�X���[�h */
    private String                 password_;

    /** �ڑ��Ώۃ��X�g */
    private List<MBeanValueGetter> resourceList_ = new ArrayList<MBeanValueGetter>();

    /**
     * ID���擾����B
     * @return ID
     */
    public String getId()
    {
        return id_;
    }

    /**
     * ID��ݒ肷��B
     * @param id ID
     */
    public void setId(String id)
    {
        id_ = id;
    }

    /**
     * JMX�̐ڑ���URL���擾����B
     * @return JMX�̐ڑ���URL
     */
    public String getUrl()
    {
        return url_;
    }

    /**
     * JMX�̐ڑ���URL��ݒ肷��B
     * @param url JMX�̐ڑ���URL
     */
    public void setUrl(String url)
    {
        url_ = url;
    }

    /**
     * ���[�U���擾����B
     * @return ���[�U
     */
    public String getUser()
    {
        return user_;
    }

    /**
     * ���[�U��ݒ肷��B
     * @param user ���[�U
     */
    public void setUser(String user)
    {
        user_ = user;
    }

    /**
     * �p�X���[�h���擾����B
     * @return �p�X���[�h
     */
    public String getPassword()
    {
        return password_;
    }

    /**
     * �p�X���[�h��ݒ肷��B
     * @param password �p�X���[�h
     */
    public void setPassword(String password)
    {
        password_ = password;
    }

    /**
     * �ڑ��Ώۃ��X�g���擾����B
     * @return �ڑ��Ώۃ��X�g
     */
    public List<MBeanValueGetter> getResourceList()
    {
        return resourceList_;
    }

    /**
     * �ڑ��Ώۂ����X�g�ɒǉ�����B
     * @param resource �ڑ��Ώ�
     */
    public void addResource(MBeanValueGetter resource)
    {
        resourceList_.add(resource);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString()
    {
        return "JMXConnectEntity [id=" + id_ + ", url=" + url_ + ", user=" + user_
                + ", password=" + password_ + ", resourceList=" + resourceList_ + "]";
    }

}
