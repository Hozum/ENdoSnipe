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
package jp.co.acroquest.endosnipe.common;

import org.eclipse.osgi.util.NLS;

/**
 * ���b�Z�[�W�O�����̂��߂̃N���X�ł��B<br />
 * 
 * @author y-komori
 */
public class Messages extends NLS
{
	private static final String BUNDLE_NAME
		= "jp.co.acroquest.endosnipe.common.messages"; //$NON-NLS-1$

	/** Preference �y�[�W�Őݒ肷�郍�O���x���̍��ڃ��x�� */
	public static String endoSnipeCommonLogLevel__;

	/** Preference �y�[�W�Őݒ肷�郍�O���x���̍��ڒl�i�f�o�b�O�j */
	public static String endoSnipeCommonLogLevelDebug__;

	/** Preference �y�[�W�Őݒ肷�郍�O���x���̍��ڒl�i�G���[�j */
	public static String endoSnipeCommonLogLevelError__;

	/** Preference �y�[�W�Őݒ肷�郍�O���x���̍��ڒl�i���j */
	public static String endoSnipeCommonLogLevelInfo__;

	/** Preference �y�[�W�Őݒ肷�郍�O���x���̍��ڒl�i�x���j */
	public static String endoSnipeCommonLogLevelWarning__;

	/** �������s���ŃR�s�[�ł��Ȃ��Ƃ��ɕ\�����郁�b�Z�[�W */
	public static String endoSnipeCommonTooFewMemoryToCopy__;

	static
	{
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages()
	{
		// Do nothing.
	}
}
