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
package jp.co.acroquest.endosnipe.javelin.util;

import java.util.Collection;
import java.util.Set;

/**
 * LinkedHashSet�N���X
 *
 * @author acroquest
 * @param <E> �G�������g
 */
public class LinkedHashSet<E> extends HashSet<E> implements Set<E>, Cloneable
{
    /** �V���A���o�[�W����ID */
    private static final long serialVersionUID = -2133387120094590387L;

    /** ���[�h�t�@�N�^�[�̃f�t�H���g�l*/
    private static final float DEFAULT_LOAD_FACTOR = .75f;
    
    /** �L���p�V�e�B�̃f�t�H���g�l�@*/
    private static final int DEFAULT_INITIAL_CAPACITY = 16;
    
    /** �L���p�V�e�B�̂������l�@*/
    private static final int CAPACITY_THRESHOLD_SIZE = 11;
    
    /**
     * �R���X�g���N�^
     * @param initialCapacity �����e��
     * @param loadFactor ���p��
     */
    public LinkedHashSet(int initialCapacity, float loadFactor)
    {
        super(initialCapacity, loadFactor, true);
    }

    /**
     * �R���X�g���N�^
     * @param initialCapacity �����e��
     */
    public LinkedHashSet(int initialCapacity)
    {
        super(initialCapacity, DEFAULT_LOAD_FACTOR, true);
    }

    /**
     * �R���X�g���N�^
     */
    public LinkedHashSet()
    {
        super(DEFAULT_INITIAL_CAPACITY, DEFAULT_LOAD_FACTOR, true);
    }

    /**
     * �R���X�g���N�^
     * @param collection �R���N�V����
     */
    public LinkedHashSet(Collection<? extends E> collection)
    {
        super(Math.max(2 * collection.size(), CAPACITY_THRESHOLD_SIZE), DEFAULT_LOAD_FACTOR, true);
        addAll(collection);
    }
}
