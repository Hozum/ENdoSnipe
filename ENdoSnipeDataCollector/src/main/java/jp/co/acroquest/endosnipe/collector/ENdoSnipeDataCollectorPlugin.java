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
package jp.co.acroquest.endosnipe.collector;

import org.eclipse.ui.plugin.AbstractUIPlugin;

/**
 * DataCollector��Eclipse�v���O�C���Ƃ��Ďg���邽�߂̃��C���N���X�ł��B
 * 
 * @author nagai
 *
 */
public class ENdoSnipeDataCollectorPlugin extends AbstractUIPlugin
{

    private static ENdoSnipeDataCollectorPlugin plugin__;

    /**
     * �R���X�g���N�^�B<br />
     * <br />
     * Eclipse���C���X�^���X���쐬���邽�߂�public�ɂ��Ă�����̂ŁA
     * �����new����v���O�����������̂͌��ł��B<br />
     * {@link #getDefault()}���\�b�h��p���āAEclipse�̍쐬�����C���X�^���X���擾���Ă��������B
     */
    public ENdoSnipeDataCollectorPlugin()
    {
        super();
        plugin__ = this;
    }

    /**
     * Eclipse�����������v���O�C���C���X�^���X��Ԃ��܂��B
     * 
     * @return DataCollector�̃v���O�C���C���X�^���X�B
     */
    public static ENdoSnipeDataCollectorPlugin getDefault()
    {
        return plugin__;
    }
}
