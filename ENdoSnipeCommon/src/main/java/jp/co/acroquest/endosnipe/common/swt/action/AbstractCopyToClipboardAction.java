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
package jp.co.acroquest.endosnipe.common.swt.action;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import jp.co.acroquest.endosnipe.common.Messages;
import jp.co.acroquest.endosnipe.common.logger.CommonLogMessageCodes;
import jp.co.acroquest.endosnipe.common.logger.ENdoSnipeCommonPluginProvider;
import jp.co.acroquest.endosnipe.common.logger.ENdoSnipeLogger;

import org.eclipse.jface.action.Action;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTError;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

/**
 * �u�N���b�v�{�[�h�ɃR�s�[�v�A�N�V�����̃x�[�X�N���X�B<br />
 *
 * @author sakamoto
 */
public abstract class AbstractCopyToClipboardAction extends Action implements CommonLogMessageCodes
{
    /** �u�N���b�v�{�[�h�ɃR�s�[�v�A�N�V������ ID */
    public static final String ID = "jp.co.acroquest.endosnipe.common.view.copyToClipboardAction";

    /** ���K�[ */
    private static ENdoSnipeLogger logger__;

    /** ImageTransfer#getInstanceMethod() ���\�b�h�iEclipse 3.3�ȑO�ł� <code>null</code> �j */
    private static Method getInstanceMethod__ = null;

    private static Clipboard clipboard__;

    private final Shell shell_;

    /**
     * �u�N���b�v�{�[�h�ɃR�s�[�v�A�N�V�������쐬���܂��B<br />
     *
     * @param text �A�N�V����������
     * @param shell {@link Shell}
     */
    public AbstractCopyToClipboardAction(final String text, final Shell shell)
    {
        super(text);
        if (logger__ == null)
        {
            init();
        }
        this.shell_ = shell;
        setId(ID);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isEnabled()
    {
        return (getInstanceMethod__ != null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void run()
    {
        if (getInstanceMethod__ == null)
        {
            logger__.log(ECLIPSE_3_4_IS_REQUIRED, getText());
            return;
        }

        // �N���b�v�{�[�h�ɃR�s�[����
        Image image = null;
        try
        {
            image = getImage();
            Transfer imageTransfer = (Transfer)getInstanceMethod__.invoke(null, new Object[0]);
            clipboard__.setContents(new Object[]{image.getImageData()},
                                    new Transfer[]{imageTransfer});
        }
        catch (IllegalArgumentException ex)
        {
            logger__.log(ECLIPSE_3_4_IS_REQUIRED, ex, getText());
        }
        catch (IllegalAccessException ex)
        {
            logger__.log(ECLIPSE_3_4_IS_REQUIRED, ex, getText());
        }
        catch (InvocationTargetException ex)
        {
            logger__.log(ECLIPSE_3_4_IS_REQUIRED, ex, getText());
        }
        catch (SWTError ex)
        {
            showErrorDialogBecauseOfTooLarge(ex);
        }
        catch (OutOfMemoryError ex)
        {
            showErrorDialogBecauseOfTooLarge(ex);
        }
        finally
        {
            if (image != null)
            {
                image.dispose();
            }
        }
    }

    /**
     * �摜�T�C�Y���傫�����߂ɃR�s�[�ł��Ȃ����Ƃ��_�C�A���O�\�����܂��B<br />
     *
     * @param ex ����������O
     */
    private void showErrorDialogBecauseOfTooLarge(final Throwable ex)
    {
        logger__.log(TOO_LARGE_COPY_TO_CLIPBOARD, ex);
        MessageBox messageBox = new MessageBox(this.shell_, SWT.OK);
        messageBox.setText(getDialogTitle());
        messageBox.setMessage(Messages.endoSnipeCommonTooFewMemoryToCopy__);
        messageBox.open();
    }

    /**
     * �R�s�[����摜��Ԃ��܂��B<br />
     *
     * @return �摜
     */
    protected abstract Image getImage();

    /**
     * �_�C�A���O�̃^�C�g���������Ԃ��܂��B<br />
     *
     * @return �_�C�A���O�̃^�C�g��������
     */
    protected abstract String getDialogTitle();

    /**
     * ���K�[�A�N���b�v�{�[�h�A���t���N�V�����̏��������s���܂��B<br />
     */
    private void init()
    {
        logger__ =
                ENdoSnipeLogger.getLogger(AbstractCopyToClipboardAction.class,
                                          ENdoSnipeCommonPluginProvider.INSTANCE);

        clipboard__ = new Clipboard(Display.getCurrent());

        try
        {
            Class<?> imageTransferClass = Class.forName("org.eclipse.swt.dnd.ImageTransfer");
            getInstanceMethod__ = imageTransferClass.getMethod("getInstance", new Class[0]);
        }
        catch (ClassNotFoundException ex)
        {
            logger__.log(ECLIPSE_3_4_IS_REQUIRED, ex, getText());
        }
        catch (SecurityException ex)
        {
            logger__.log(ECLIPSE_3_4_IS_REQUIRED, ex, getText());
        }
        catch (NoSuchMethodException ex)
        {
            logger__.log(ECLIPSE_3_4_IS_REQUIRED, ex, getText());
        }
    }

}
