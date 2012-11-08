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
package jp.co.acroquest.endosnipe.common.swt;

import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;

/**
 * TableViewer�̃J�������\�[�g����N���X�B
 * @author acroquest
 */
public abstract class ColumnViewerSorter extends ViewerComparator
{
    /** �����Ƀ\�[�g����B */
    public static final int ASC = 1;

    /** �\�[�g���s��Ȃ��B */
    public static final int NONE = 0;

    /** �~���Ƀ\�[�g����B */
    public static final int DESC = -1;

    /** ���݂̃\�[�g�����B */
    private int direction_ = 0;

    /** �\�[�g�Ώۂ̃J�����B */
    private final TableViewerColumn column_;

    /**  �J������ێ�����r���[���B */
    private final ColumnViewer viewer_;

    /**
     * �R���X�g���N�^�B�r���[���ƃJ������ێ�����B
     * @param viewer �r���[��
     * @param column �J����
     */
    public ColumnViewerSorter(final ColumnViewer viewer, final TableViewerColumn column)
    {
        this.column_ = column;
        this.viewer_ = viewer;
        this.column_.getColumn().addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent e)
            {
                int direction = calcDirection();
                setSorter(ColumnViewerSorter.this, direction);
            }

            private int calcDirection()
            {
                if (ColumnViewerSorter.this.viewer_.getComparator() == null)
                {
                    return ASC;
                }

                if (ColumnViewerSorter.this.viewer_.getComparator() != ColumnViewerSorter.this)
                {
                    return ASC;
                }

                int direction = ColumnViewerSorter.this.direction_;
                if (direction == ASC)
                {
                    return DESC;
                }
                else if (direction == DESC)
                {
                    return NONE;
                }

                return ASC;
            }
        });
    }

    /**
     * �\�[�g������ݒ肷��B
     * @param sorter �\�[�g�N���X
     * @param direction �\�[�g��
     */
    public void setSorter(final ColumnViewerSorter sorter, final int direction)
    {
        if (direction == NONE)
        {
            this.column_.getColumn().getParent().setSortColumn(null);
            this.column_.getColumn().getParent().setSortDirection(SWT.NONE);
            this.viewer_.setComparator(null);
        }
        else
        {
            this.column_.getColumn().getParent().setSortColumn(this.column_.getColumn());
            sorter.direction_ = direction;

            if (direction == ASC)
            {
                this.column_.getColumn().getParent().setSortDirection(SWT.UP);
            }
            else
            {
                this.column_.getColumn().getParent().setSortDirection(SWT.DOWN);
            }

            if (this.viewer_.getComparator() == sorter)
            {
                this.viewer_.refresh();
            }
            else
            {
                this.viewer_.setComparator(sorter);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compare(final Viewer viewer, final Object e1, final Object e2)
    {
        return this.direction_ * doCompare(e1, e2);
    }

    /**
     * ��r���s���B
     * @param e1 �v�f1
     * @param e2 �v�f2
     * @return e1�̕����傫���ꍇ��1�A�����ꍇ��0�Ae1�̕����������ꍇ��-1�B
     */
    protected abstract int doCompare(Object e1, Object e2);
}
