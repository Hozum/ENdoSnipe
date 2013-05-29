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
package jp.co.acroquest.endosnipe.javelin.util.concurrent;

import java.io.IOException;
import java.io.Serializable;
import java.util.AbstractCollection;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.ReentrantLock;

import jp.co.acroquest.endosnipe.javelin.util.AbstractMap;
import jp.co.acroquest.endosnipe.javelin.util.ArrayList;

/**
 * ConcurrentHashMap�N���X
 * @author acroquest
 *
 * @param <K> �L�[
 * @param <V> �l
 */
public class ConcurrentHashMap<K, V> extends AbstractMap<K, V> implements ConcurrentMap<K, V>,
        Serializable
{
    /** �V���A���o�[�W����ID */
    private static final long              serialVersionUID          = 7986208325563360065L;

    /** �f�t�H���g�̏����e�� */
    private static final int               DEFAULT_INITIAL_CAPACITY  = 16;

    private static final int               INITIAL_CAPACITY          = 11;

    /** �e�ʂ̃V�t�g�� */
    private static final int               CAPACITY_SHIFT            = 30;

    /** �ő�e�� */
    private static final int               MAXIMUM_CAPACITY          = 1 << CAPACITY_SHIFT;

    /** �f�t�H���g�̕��׌W�� */
    private static final float             DEFAULT_LOAD_FACTOR       = 0.75f;

    /** �f�t�H���g�̃Z�O�����g�� */
    private static final int               DEFAULT_SEGMENTS          = 16;

    /** �Z�O�����g�̃V�t�g�� */
    private static final int               DEFAULT_SEGMENTS_SHIFT    = 16;

    /** �Z�O�����g�̃V�t�g�� */
    private static final int               SEGMENTS_SHIFT            = 32;

    /** �Z�O�����g�̍ő吔 */
    private static final int               MAX_SEGMENTS              = 1 << DEFAULT_SEGMENTS_SHIFT;

    /** ���b�N�܂ł̃��g���C�� */
    private static final int               RETRIES_BEFORE_LOCK       = 2;

    private static final int               HASH_LEFT_SHIFT_FOUR      = 4;

    private static final int               HASH_LEFT_SHIFT_NINE      = 9;

    private static final int               HASH_RIGHT_SHIFT_FOURTEEN = 14;

    private static final int               HASH_RIGHT_SHIFT_TEN      = 10;

    /** �Z�O�����g�}�X�N */
    private final int                      segmentMask_;

    /** �Z�O�����g�V�t�g�� */
    private final int                      segmentShift_;

    /** �Z�O�����g */
    private final Segment<K, V>[]                segments_;

    /** �L�[�Z�b�g */
    private transient Set<K>               keySet_;

    /** �G���g���[�Z�b�g */
    private transient Set<Map.Entry<K, V>> entrySet_;

    /** �l */
    private transient Collection<V>        values_;

    /**
     * �n�b�V���R�[�h���擾����B
     * @param x �n�b�V���R�[�h���擾����I�u�W�F�N�g
     * @return ���������n�b�V���R�[�h
     */
    static int hash(Object x)
    {
        int h = x.hashCode();
        h += ~(h << HASH_LEFT_SHIFT_NINE);
        h ^= (h >>> HASH_RIGHT_SHIFT_FOURTEEN);
        h += (h << HASH_LEFT_SHIFT_FOUR);
        h ^= (h >>> HASH_RIGHT_SHIFT_TEN);
        return h;
    }

    /**
     * �w�肵���n�b�V���R�[�h�����Z�O�����g���擾����B
     * @param hash �n�b�V���R�[�h
     * @return �w�肵���n�b�V���R�[�h�����Z�O�����g
     */
    final Segment<K, V> segmentFor(int hash)
    {
        return (Segment<K, V>)segments_[(hash >>> segmentShift_) & segmentMask_];
    }

    /**
     * HashEntry�N���X
     * @author acroquest
     *
     * @param <K> �L�[
     * @param <V> �l
     */
    static final class HashEntry<K, V>
    {
        /**
         * �L�[
         */
        final K               key_;

        /**
         * �n�b�V���R�[�h
         */
        final int             hash_;

        /**
         * �l
         */
        volatile V            value_;

        /**
         * ���̃G���g��
         */
        final HashEntry<K, V> next_;

        /**
         * �R���X�g���N�^
         * @param key �L�[
         * @param hash �n�b�V��
         * @param next ���̃G���g��
         * @param value �l
         */
        HashEntry(K key, int hash, HashEntry<K, V> next, V value)
        {
            this.key_ = key;
            this.hash_ = hash;
            this.next_ = next;
            this.value_ = value;
        }
    }

    /**
     * Segment�N���X
     * @author acroquest
     *
     * @param <K> �L�[
     * @param <V> �l
     */
    static final class Segment<K, V> extends ReentrantLock implements Serializable
    {

        private static final long      serialVersionUID = 2249069246763182397L;

        /** �J�E���g */
        transient volatile int         count_;

        /** �C���� */
        transient int                  modCount_ = 0;

        /** �������l */
        transient int                  threshold_;

        /** �e�[�u�� */
        transient volatile HashEntry<K, V>[] table_;

        /** �e�[�u���̕��׌W�� */
        final float                    loadFactor_;

        /**
         * �R���X�g���N�^
         * @param initialCapacity �����e��
         * @param lf �Z�O�����g�̕��׌W��
         */
        @SuppressWarnings("unchecked")
        Segment(int initialCapacity, float lf)
        {
            loadFactor_ = lf;
            setTable(new HashEntry[initialCapacity]);
        }

        /**
         * �e�[�u����ݒ肵�܂��B
         * @param newTable �e�[�u��
         */
        void setTable(HashEntry<K, V>[] newTable)
        {
            threshold_ = (int)(newTable.length * loadFactor_);
            table_ = newTable;
        }

        /**
         * �ŏ��̃G���g�����擾���܂��B
         * @param hash �n�b�V���R�[�h
         * @return �ŏ��̃G���g��
         */
        HashEntry<K, V> getFirst(int hash)
        {
            HashEntry<K, V>[] tab = table_;
            return (HashEntry<K, V>)tab[hash & (tab.length - 1)];
        }

        /**
         * ���b�N���ăG���g������l��ǂݎ��
         * @param entry �l��ǂݎ��G���g��
         * @return �ǂݎ�����l
         */
        V readValueUnderLock(HashEntry<K, V> entry)
        {
            lock();
            try
            {
                return entry.value_;
            }
            finally
            {
                unlock();
            }
        }

        /**
         * �w�肵���L�[�����v�f����l���擾���܂��B
         * @param key �L�[
         * @param hash �n�b�V���R�[�h
         * @return �w�肵���L�[�����v�f�̒l
         */
        V get(Object key, int hash)
        {
            if (count_ != 0)
            {
                HashEntry<K, V> e = getFirst(hash);
                while (e != null)
                {
                    if (e.hash_ == hash && key.equals(e.key_))
                    {
                        V v = e.value_;
                        if (v != null)
                        {
                            return v;
                        }
                        return readValueUnderLock(e);
                    }
                    e = e.next_;
                }
            }
            return null;
        }

        /**
         * �w�肵���L�[���G���g�����������肵�܂��B
         * @param key �L�[
         * @param hash �n�b�V���R�[�h
         * @return �w�肵���L�[���G���g�������Ƃ�true/�����łȂ��Ƃ�false
         */
        boolean containsKey(Object key, int hash)
        {
            if (count_ != 0)
            {
                HashEntry<K, V> e = getFirst(hash);
                while (e != null)
                {
                    if (e.hash_ == hash && key.equals(e.key_))
                    {
                        return true;
                    }
                    e = e.next_;
                }
            }
            return false;
        }

        /**
         * �w�肵���l���G���g�����������肵�܂��B
         * @param value �l
         * @return �w�肵���l���G���g�������Ƃ�true/�����łȂ��Ƃ�false
         */
        boolean containsValue(Object value)
        {
            if (count_ != 0)
            {
                HashEntry<K, V>[] tab = table_;
                int len = tab.length;
                for (int i = 0; i < len; i++)
                {
                    for (HashEntry<K, V> e = (HashEntry<K, V>)tab[i]; e != null; e = e.next_)
                    {
                        V v = e.value_;
                        if (v == null)
                        {
                            v = readValueUnderLock(e);
                        }
                        if (value.equals(v))
                        {
                            return true;
                        }
                    }
                }
            }
            return false;
        }

        /**
         * �w�肵���l�����v�f�̒l��u�������܂��B
         * @param key �L�[
         * @param hash �n�b�V���R�[�h
         * @param oldValue �Â��l
         * @param newValue �V�����l
         * @return �l��u����������true/�����łȂ��Ƃ�false
         */
        boolean replace(K key, int hash, V oldValue, V newValue)
        {
            lock();
            try
            {
                HashEntry<K, V> entry = getFirst(hash);
                while (entry != null && (entry.hash_ != hash || !key.equals(entry.key_)))
                {
                    entry = entry.next_;
                }

                boolean replaced = false;
                if (entry != null && oldValue.equals(entry.value_))
                {
                    replaced = true;
                    entry.value_ = newValue;
                }
                return replaced;
            }
            finally
            {
                unlock();
            }
        }

        /**
         * �w�肵���l�����v�f�̒l��u�������܂��B
         * @param key �L�[
         * @param hash �n�b�V���R�[�h
         * @param newValue �V�����l
         * @return �u��������O�̒l
         */
        V replace(K key, int hash, V newValue)
        {
            lock();
            try
            {
                HashEntry<K, V> e = getFirst(hash);
                while (e != null && (e.hash_ != hash || !key.equals(e.key_)))
                {
                    e = e.next_;
                }

                V oldValue = null;
                if (e != null)
                {
                    oldValue = e.value_;
                    e.value_ = newValue;
                }
                return oldValue;
            }
            finally
            {
                unlock();
            }
        }

        /**
         * �w�肵���L�[�����G���g����ݒ肷��B
         * @param key �L�[
         * @param hash �n�b�V���R�[�h
         * @param value �l
         * @param onlyIfAbsent �v�f�����݂����ꍇ�ɒu�������邩�̃t���O
         * @return �ݒ肷��O�̒l
         */
        V put(K key, int hash, V value, boolean onlyIfAbsent)
        {
            lock();
            try
            {
                int c = count_;
                if (c++ > threshold_)
                {
                    rehash();
                }
                HashEntry<K, V>[] tab = table_;
                int index = hash & (tab.length - 1);
                HashEntry<K, V> first = (HashEntry<K, V>)tab[index];
                HashEntry<K, V> entry = first;

                while (entry != null && (entry.hash_ != hash || !key.equals(entry.key_)))
                {
                    entry = entry.next_;
                }

                V oldValue;
                if (entry != null)
                {
                    oldValue = entry.value_;
                    if (!onlyIfAbsent)
                    {
                        entry.value_ = value;
                    }
                }
                else
                {
                    oldValue = null;
                    ++modCount_;
                    tab[index] = new HashEntry<K, V>(key, hash, first, value);
                    count_ = c;
                }
                return oldValue;
            }
            finally
            {
                unlock();
            }
        }

        /**
         * �e�[�u����V�K�쐬����B
         */
        @SuppressWarnings("unchecked")
        void rehash()
        {
            HashEntry<K, V>[] oldTable = table_;
            int oldCapacity = oldTable.length;
            if (oldCapacity >= MAXIMUM_CAPACITY)
            {
                return;
            }

            HashEntry<K, V>[] newTable = new HashEntry[oldCapacity << 1];
            threshold_ = (int)(newTable.length * loadFactor_);
            int sizeMask = newTable.length - 1;
            for (int i = 0; i < oldCapacity; i++)
            {
                HashEntry<K, V> entry = (HashEntry<K, V>)oldTable[i];

                if (entry != null)
                {
                    HashEntry<K, V> next = entry.next_;
                    int idx = entry.hash_ & sizeMask;

                    if (next == null)
                    {
                        newTable[idx] = entry;
                    }
                    else
                    {

                        HashEntry<K, V> lastRun = entry;
                        int lastIdx = idx;
                        for (HashEntry<K, V> last = next; last != null; last = last.next_)
                        {
                            int k = last.hash_ & sizeMask;
                            if (k != lastIdx)
                            {
                                lastIdx = k;
                                lastRun = last;
                            }
                        }
                        newTable[lastIdx] = lastRun;

                        for (HashEntry<K, V> p = entry; p != lastRun; p = p.next_)
                        {
                            int k = p.hash_ & sizeMask;
                            HashEntry<K, V> n = (HashEntry<K, V>)newTable[k];
                            newTable[k] = new HashEntry<K, V>(p.key_, p.hash_, n, p.value_);
                        }
                    }
                }
            }
            table_ = newTable;
        }

        /**
         * �w�肵���L�[�����G���g�����폜����
         * @param key �L�[
         * @param hash �n�b�V���R�[�h
         * @param value �l
         * @return �폜�����G���g��
         */
        V remove(Object key, int hash, Object value)
        {
            lock();
            try
            {
                int c = count_ - 1;
                HashEntry<K, V>[] tab = table_;
                int index = hash & (tab.length - 1);
                HashEntry<K, V> first = (HashEntry<K, V>)tab[index];
                HashEntry<K, V> entry = first;
                while (entry != null && (entry.hash_ != hash || !key.equals(entry.key_)))
                {
                    entry = entry.next_;
                }

                V oldValue = null;
                if (entry != null)
                {
                    V v = entry.value_;
                    if (value == null || value.equals(v))
                    {
                        oldValue = v;

                        ++modCount_;
                        HashEntry<K, V> newFirst = entry.next_;
                        for (HashEntry<K, V> p = first; p != entry; p = p.next_)
                        {
                            newFirst = new HashEntry<K, V>(p.key_, p.hash_, newFirst, p.value_);
                        }
                        tab[index] = newFirst;
                        count_ = c;
                    }
                }
                return oldValue;
            }
            finally
            {
                unlock();
            }
        }

        /**
         * �e�[�u�����N���A����B
         */
        void clear()
        {
            if (count_ != 0)
            {
                lock();
                try
                {
                    HashEntry<K, V>[] tab = table_;
                    for (int i = 0; i < tab.length; i++)
                    {
                        tab[i] = null;
                    }
                    ++modCount_;
                    count_ = 0;
                }
                finally
                {
                    unlock();
                }
            }
        }
    }

    /**
     * �R���X�g���N�^
     * @param initialCapacity �n�b�V���}�b�v�̏����e��
     * @param loadFactor �n�b�V���}�b�v�̕��׌W��
     * @param concurrencyLevel ���s�������x��
     */
    @SuppressWarnings("unchecked")
    public ConcurrentHashMap(int initialCapacity, float loadFactor, int concurrencyLevel)
    {
        if (!(loadFactor > 0) || initialCapacity < 0 || concurrencyLevel <= 0)
        {
            throw new IllegalArgumentException();
        }

        if (concurrencyLevel > MAX_SEGMENTS)
        {
            concurrencyLevel = MAX_SEGMENTS;
        }

        int sshift = 0;
        int ssize = 1;
        while (ssize < concurrencyLevel)
        {
            ++sshift;
            ssize <<= 1;
        }
        segmentShift_ = SEGMENTS_SHIFT - sshift;
        segmentMask_ = ssize - 1;
        this.segments_ = new Segment[ssize];

        if (initialCapacity > MAXIMUM_CAPACITY)
        {
            initialCapacity = MAXIMUM_CAPACITY;
        }
        int c = initialCapacity / ssize;
        if (c * ssize < initialCapacity)
        {
            ++c;
        }
        int cap = 1;
        while (cap < c)
        {
            cap <<= 1;
        }

        for (int i = 0; i < this.segments_.length; ++i)
        {
            this.segments_[i] = new Segment<K, V>(cap, loadFactor);
        }
    }

    /**
     * �R���X�g���N�^
     * @param initialCapacity �n�b�V���}�b�v�̏����e��
     */
    public ConcurrentHashMap(int initialCapacity)
    {
        this(initialCapacity, DEFAULT_LOAD_FACTOR, DEFAULT_SEGMENTS);
    }

    /**
     * �R���X�g���N�^
     */
    public ConcurrentHashMap()
    {
        this(DEFAULT_INITIAL_CAPACITY, DEFAULT_LOAD_FACTOR, DEFAULT_SEGMENTS);
    }

    /**
     * �R���X�g���N�^
     * @param t Map
     */
    public ConcurrentHashMap(Map<? extends K, ? extends V> t)
    {
        this(Math.max((int)(t.size() / DEFAULT_LOAD_FACTOR) + 1, INITIAL_CAPACITY),
                DEFAULT_LOAD_FACTOR, DEFAULT_SEGMENTS);
        putAll(t);
    }

    /**
     * {@inheritDoc}
     */
    public boolean isEmpty()
    {
        final Segment<K, V>[] SEGMENTS = this.segments_;

        int[] mc = new int[SEGMENTS.length];
        int mcsum = 0;
        for (int i = 0; i < SEGMENTS.length; ++i)
        {
            if (SEGMENTS[i].count_ != 0)
            {
                return false;
            }
            else
            {
                mcsum += SEGMENTS[i].modCount_;
                mc[i] = SEGMENTS[i].modCount_;
            }
        }

        if (mcsum != 0)
        {
            for (int i = 0; i < SEGMENTS.length; ++i)
            {
                if (SEGMENTS[i].count_ != 0 || mc[i] != SEGMENTS[i].modCount_)
                {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public int size()
    {
        final Segment<K, V>[] SEGMENTS = this.segments_;
        long sum = 0;
        long check = 0;
        int[] mc = new int[SEGMENTS.length];

        for (int k = 0; k < RETRIES_BEFORE_LOCK; ++k)
        {
            check = 0;
            sum = 0;
            int mcsum = 0;
            for (int i = 0; i < SEGMENTS.length; ++i)
            {
                sum += SEGMENTS[i].count_;
                mcsum += SEGMENTS[i].modCount_;
                mc[i] = SEGMENTS[i].modCount_;
            }
            if (mcsum != 0)
            {
                for (int i = 0; i < SEGMENTS.length; ++i)
                {
                    check += SEGMENTS[i].count_;
                    if (mc[i] != SEGMENTS[i].modCount_)
                    {
                        check = -1;
                        break;
                    }
                }
            }
            if (check == sum)
            {
                break;
            }
        }
        if (check != sum)
        {
            sum = 0;
            for (int i = 0; i < SEGMENTS.length; ++i)
            {
                SEGMENTS[i].lock();
            }
            for (int i = 0; i < SEGMENTS.length; ++i)
            {
                sum += SEGMENTS[i].count_;
            }
            for (int i = 0; i < SEGMENTS.length; ++i)
            {
                SEGMENTS[i].unlock();
            }
        }
        if (sum > Integer.MAX_VALUE)
        {
            return Integer.MAX_VALUE;
        }
        else
        {
            return (int)sum;
        }
    }

    /**
     * {@inheritDoc}
     */
    public V get(Object key)
    {
        int hash = hash(key);
        return segmentFor(hash).get(key, hash);
    }

    /**
     * {@inheritDoc}
     */
    public boolean containsKey(Object key)
    {
        int hash = hash(key);
        return segmentFor(hash).containsKey(key, hash);
    }

    /**
     * {@inheritDoc}
     */
    public boolean containsValue(Object value)
    {
        if (value == null)
        {
            throw new NullPointerException();
        }

        final Segment<K, V>[] SEGMENTS = this.segments_;
        int[] mc = new int[SEGMENTS.length];

        for (int k = 0; k < RETRIES_BEFORE_LOCK; ++k)
        {
            int mcsum = 0;
            for (int i = 0; i < SEGMENTS.length; ++i)
            {
                mcsum += SEGMENTS[i].modCount_;
                mc[i] = SEGMENTS[i].modCount_;
                if (SEGMENTS[i].containsValue(value))
                {
                    return true;
                }
            }
            boolean cleanSweep = true;
            if (mcsum != 0)
            {
                for (int i = 0; i < SEGMENTS.length; ++i)
                {
                    if (mc[i] != SEGMENTS[i].modCount_)
                    {
                        cleanSweep = false;
                        break;
                    }
                }
            }
            if (cleanSweep)
            {
                return false;
            }
        }

        for (int i = 0; i < SEGMENTS.length; ++i)
        {
            SEGMENTS[i].lock();
        }
        boolean found = false;
        try
        {
            for (int i = 0; i < SEGMENTS.length; ++i)
            {
                if (SEGMENTS[i].containsValue(value))
                {
                    found = true;
                    break;
                }
            }
        }
        finally
        {
            for (int i = 0; i < SEGMENTS.length; ++i)
            {
                SEGMENTS[i].unlock();
            }
        }
        return found;
    }

    /**
     * �w�肵���l���Z�O�����g���������肷��
     * @param value �l
     * @return �w�肵���l���Z�O�����g�����Ƃ�true/�����łȂ��Ƃ�false
     */
    public boolean contains(Object value)
    {
        return containsValue(value);
    }

    /**
     * {@inheritDoc}
     */
    public V put(K key, V value)
    {
        if (value == null)
        {
            throw new NullPointerException();
        }
        int hash = hash(key);
        return segmentFor(hash).put(key, hash, value, false);
    }

    /**
     * {@inheritDoc}
     */
    public V putIfAbsent(K key, V value)
    {
        if (value == null)
        {
            throw new NullPointerException();
        }
        int hash = hash(key);
        return segmentFor(hash).put(key, hash, value, true);
    }

    /**
     * {@inheritDoc}
     */
    public void putAll(Map<? extends K, ? extends V> t)
    {
        Iterator<? extends Map.Entry<? extends K, ? extends V>> itetator = t.entrySet().iterator();
        for (Iterator<? extends Map.Entry<? extends K, ? extends V>> it = itetator; it.hasNext();)
        {
            Entry<? extends K, ? extends V> e = it.next();
            put(e.getKey(), e.getValue());
        }
    }

    /**
     * {@inheritDoc}
     */
    public V remove(Object key)
    {
        int hash = hash(key);
        return segmentFor(hash).remove(key, hash, null);
    }

    /**
     * {@inheritDoc}
     */
    public boolean remove(Object key, Object value)
    {
        int hash = hash(key);
        return segmentFor(hash).remove(key, hash, value) != null;
    }

    /**
     * {@inheritDoc}
     */
    public boolean replace(K key, V oldValue, V newValue)
    {
        if (oldValue == null || newValue == null)
        {
            throw new NullPointerException();
        }
        int hash = hash(key);
        return segmentFor(hash).replace(key, hash, oldValue, newValue);
    }

    /**
     * {@inheritDoc}
     */
    public V replace(K key, V value)
    {
        if (value == null)
        {
            throw new NullPointerException();
        }
        int hash = hash(key);
        return segmentFor(hash).replace(key, hash, value);
    }

    /**
     * {@inheritDoc}
     */
    public void clear()
    {
        for (int i = 0; i < segments_.length; ++i)
        {
            segments_[i].clear();
        }
    }

    /**
     * {@inheritDoc}
     */
    public Set<K> keySet()
    {
        Set<K> ks = keySet_;

        if (ks != null)
        {
            return ks;
        }
        
        keySet_ = new KeySet();

        return keySet_;
    }

    /**
     * {@inheritDoc}
     */
    public Collection<V> values()
    {
        Collection<V> vs = values_;
        
        if(vs != null)
        {
            return vs;
        }
        
        values_ = new Values();
        return values_;
    }

    /**
     * {@inheritDoc}
     */
    public Set<Map.Entry<K, V>> entrySet()
    {
        Set<Map.Entry<K, V>> es = entrySet_;
        
        if(es != null)
        {
            return es;
        }
        
        entrySet_ = (Set<Map.Entry<K, V>>)new EntrySet();
        
        return entrySet_;
    }

    /**
     * �V�K�ɃL�[���擾���܂��B
     * @return �V�K�̃L�[ 
     */
    public Enumeration<K> keys()
    {
        return new KeyIterator();
    }

    /**
     * �V�K�̗v�f���擾���܂��B
     * @return �V�K�̗v�f
     */
    public Enumeration<V> elements()
    {
        return new ValueIterator();
    }

    /**
     * HashIterator���ۃN���X
     * @author acroquest
     *
     */
    abstract class HashIterator
    {
        /** ���̃Z�O�����g�̃C���f�b�N�X */
        int             nextSegmentIndex_;

        /** ���̃e�[�u���̃C���f�b�N�X */
        int             nextTableIndex_;

        /** ���݂̃e�[�u�� */
        HashEntry<K, V>[]     currentTable_;

        /** ���̃G���g�� */
        HashEntry<K, V> nextEntry_;

        /** �Ō�ɕԂ����G���g�� */
        HashEntry<K, V> lastReturned_;

        /**
         * �R���X�g���N�^
         */
        HashIterator()
        {
            nextSegmentIndex_ = segments_.length - 1;
            nextTableIndex_ = -1;
            advance();
        }

        /**
         * ���̗v�f���������肷��B
         * @return ���̗v�f�����Ƃ�true/�����łȂ��Ƃ�false
         */
        public boolean hasMoreElements()
        {
            return hasNext();
        }

        /**
         * ���̗v�f�ɐi�݂܂��B
         */
        final void advance()
        {
            if (nextEntry_ != null && (nextEntry_.next_) != null)
            {
                nextEntry_ = nextEntry_.next_;
                return;
            }

            while (nextTableIndex_ >= 0)
            {
                nextEntry_ = (HashEntry<K, V>)currentTable_[nextTableIndex_--];
                if (nextEntry_ != null)
                {
                    return;
                }
            }

            while (nextSegmentIndex_ >= 0)
            {
                Segment<K, V> seg = (Segment<K, V>)segments_[nextSegmentIndex_--];
                if (seg.count_ != 0)
                {
                    currentTable_ = seg.table_;
                    for (int j = currentTable_.length - 1; j >= 0; --j)
                    {
                        nextEntry_ = (HashEntry<K, V>)currentTable_[j];
                        if (nextEntry_ != null)
                        {
                            nextTableIndex_ = j - 1;
                            return;
                        }
                    }
                }
            }
        }

        /**
         * ���̗v�f���������肷��B
         * @return ���̗v�f�����Ƃ�true/�����łȂ��Ƃ�false
         */
        public boolean hasNext()
        {
            return nextEntry_ != null;
        }

        /**
         * ���̗v�f���擾����B
         * @return ���̗v�f
         */
        HashEntry<K, V> nextEntry()
        {
            if (nextEntry_ == null)
            {
                throw new NoSuchElementException();
            }
            lastReturned_ = nextEntry_;
            advance();
            return lastReturned_;
        }

        /**
         * �Ō�ɕԂ����G���g�����폜����B
         */
        public void remove()
        {
            if (lastReturned_ == null)
            {
                throw new IllegalStateException();
            }
            ConcurrentHashMap.this.remove(lastReturned_.key_);
            lastReturned_ = null;
        }
    }

    /**
     * KeyIterator�N���X
     * @author acroquest
     *
     */
    final class KeyIterator extends HashIterator implements Iterator<K>, Enumeration<K>
    {
        /**
         * {@inheritDoc}
         */
        public K next()
        {
            return super.nextEntry().key_;
        }

        /**
         * {@inheritDoc}
         */
        public K nextElement()
        {
            return super.nextEntry().key_;
        }
    }

    /**
     * �l�̃C�e���[�^�N���X
     * @author acroquest
     *
     */
    final class ValueIterator extends HashIterator implements Iterator<V>, Enumeration<V>
    {
        /**
         * {@inheritDoc}
         */
        public V next()
        {
            return super.nextEntry().value_;
        }

        /**
         * {@inheritDoc}
         */
        public V nextElement()
        {
            return super.nextEntry().value_;
        }
    }

    /**
     * �G���g���̃C�e���[�^�N���X
     * @author acroquest
     *
     */
    final class EntryIterator extends HashIterator implements Map.Entry<K, V>,
            Iterator<Entry<K, V>>
    {
        /**
         * {@inheritDoc}
         */
        public Map.Entry<K, V> next()
        {
            nextEntry();
            return this;
        }

        /**
         * {@inheritDoc}
         */
        public K getKey()
        {
            if (lastReturned_ == null)
            {
                throw new IllegalStateException("Entry was removed");
            }
            return lastReturned_.key_;
        }

        /**
         * {@inheritDoc}
         */
        public V getValue()
        {
            if (lastReturned_ == null)
            {
                throw new IllegalStateException("Entry was removed");
            }
            return ConcurrentHashMap.this.get(lastReturned_.key_);
        }

        /**
         * {@inheritDoc}
         */
        public V setValue(V value)
        {
            if (lastReturned_ == null)
            {
                throw new IllegalStateException("Entry was removed");
            }
            return ConcurrentHashMap.this.put(lastReturned_.key_, value);
        }

        /**
         * {@inheritDoc}
         */
        @SuppressWarnings("unchecked")
        public boolean equals(Object o)
        {

            if (lastReturned_ == null)
            {
                return super.equals(o);
            }
            if (!(o instanceof Map.Entry))
            {
                return false;
            }
            Map.Entry<K, V> e = (Map.Entry<K, V>)o;
            return eq(getKey(), e.getKey()) && eq(getValue(), e.getValue());
        }

        /**
         * {@inheritDoc}
         */
        public int hashCode()
        {

            if (lastReturned_ == null)
            {
                return super.hashCode();
            }

            Object k = getKey();
            Object v = getValue();
            return ((k == null) ? 0 : k.hashCode()) ^ ((v == null) ? 0 : v.hashCode());
        }

        /**
         * {@inheritDoc}
         */
        public String toString()
        {

            if (lastReturned_ == null)
            {
                return super.toString();
            }
            else
            {
                return getKey() + "=" + getValue();
            }
        }

        /**
         * �w�肵���I�u�W�F�N�g�������������萔r
         * @param o1 ��r��
         * @param o2 ��r��
         * @return �w�肵���I�u�W�F�N�g���������Ƃ�true/�����łȂ��Ƃ�false
         */
        boolean eq(Object o1, Object o2)
        {
            return (o1 == null ? o2 == null : o1.equals(o2));
        }

    }

    /**
     * KeySet�N���X
     * @author acroquest
     *
     */
    final class KeySet extends AbstractSet<K>
    {
        /**
         * {@inheritDoc}
         */
        public Iterator<K> iterator()
        {
            return new KeyIterator();
        }

        /**
         * {@inheritDoc}
         */
        public int size()
        {
            return ConcurrentHashMap.this.size();
        }

        /**
         * {@inheritDoc}
         */
        public boolean contains(Object o)
        {
            return ConcurrentHashMap.this.containsKey(o);
        }

        /**
         * {@inheritDoc}
         */
        public boolean remove(Object o)
        {
            return ConcurrentHashMap.this.remove(o) != null;
        }

        /**
         * {@inheritDoc}
         */
        public void clear()
        {
            ConcurrentHashMap.this.clear();
        }

        /**
         * {@inheritDoc}
         */
        public Object[] toArray()
        {
            Collection<K> c = new ArrayList<K>();
            for (Iterator<K> i = iterator(); i.hasNext();)
            {
                c.add(i.next());
            }
            return c.toArray();
        }

        /**
         * {@inheritDoc}
         */
        public <T> T[] toArray(T[] a)
        {
            Collection<K> c = new ArrayList<K>();
            for (Iterator<K> i = iterator(); i.hasNext();)
            {
                c.add(i.next());
            }
            return c.toArray(a);
        }
    }

    /**
     * Values�N���X
     * @author acroquest
     *
     */
    final class Values extends AbstractCollection<V>
    {
        /**
         * {@inheritDoc}
         */
        public Iterator<V> iterator()
        {
            return new ValueIterator();
        }

        /**
         * {@inheritDoc}
         */
        public int size()
        {
            return ConcurrentHashMap.this.size();
        }

        /**
         * {@inheritDoc}
         */
        public boolean contains(Object o)
        {
            return ConcurrentHashMap.this.containsValue(o);
        }

        /**
         * {@inheritDoc}
         */
        public void clear()
        {
            ConcurrentHashMap.this.clear();
        }

        /**
         * {@inheritDoc}
         */
        public Object[] toArray()
        {
            Collection<V> c = new ArrayList<V>();
            for (Iterator<V> i = iterator(); i.hasNext();)
            {
                c.add(i.next());
            }
            return c.toArray();
        }

        /**
         * {@inheritDoc}
         */
        public <T> T[] toArray(T[] a)
        {
            Collection<V> c = new ArrayList<V>();
            for (Iterator<V> i = iterator(); i.hasNext();)
            {
                c.add(i.next());
            }
            return c.toArray(a);
        }
    }

    /**
     * EntrySet�N���X
     * @author acroquest
     *
     */
    final class EntrySet extends AbstractSet<Map.Entry<K, V>>
    {
        /**
         * {@inheritDoc}
         */
        public Iterator<Map.Entry<K, V>> iterator()
        {
            return new EntryIterator();
        }

        /**
         * {@inheritDoc}
         */
        @SuppressWarnings("unchecked")
        public boolean contains(Object o)
        {
            if (!(o instanceof Map.Entry))
            {
                return false;
            }
            Map.Entry<K, V> e = (Map.Entry<K, V>)o;
            V v = ConcurrentHashMap.this.get(e.getKey());
            return v != null && v.equals(e.getValue());
        }

        /**
         * {@inheritDoc}
         */
        @SuppressWarnings("unchecked")
        public boolean remove(Object o)
        {
            if (!(o instanceof Map.Entry))
            {
                return false;
            }
            Map.Entry<K, V> e = (Map.Entry<K, V>)o;
            return ConcurrentHashMap.this.remove(e.getKey(), e.getValue());
        }

        /**
         * {@inheritDoc}
         */
        public int size()
        {
            return ConcurrentHashMap.this.size();
        }

        /**
         * {@inheritDoc}
         */
        public void clear()
        {
            ConcurrentHashMap.this.clear();
        }

        /**
         * {@inheritDoc}
         */
        public Object[] toArray()
        {

            Collection<Map.Entry<K, V>> c = new ArrayList<Map.Entry<K, V>>(size());
            for (Iterator<Map.Entry<K, V>> i = iterator(); i.hasNext();)
            {
                c.add(new SimpleEntry<K, V>(i.next()));
            }
            return c.toArray();
        }

        /**
         * {@inheritDoc}
         */
        public <T> T[] toArray(T[] a)
        {
            Collection<Map.Entry<K, V>> c = new ArrayList<Map.Entry<K, V>>(size());
            for (Iterator<Map.Entry<K, V>> i = iterator(); i.hasNext();)
            {
                c.add(new SimpleEntry<K, V>(i.next()));
            }
            return c.toArray(a);
        }

    }

    /**
     * SimpleEntry�N���X
     * @author acroquest
     *
     * @param <K> �L�[
     * @param <V> �l
     */
    static final class SimpleEntry<K, V> implements Entry<K, V>
    {
        /** �L�[ */
        K key_;

        /** �l */
        V value_;

        /**
         * �R���X�g���N�^
         * @param key �L�[
         * @param value �l
         */
        public SimpleEntry(K key, V value)
        {
            this.key_ = key;
            this.value_ = value;
        }

        /**
         * �R���X�g���N�^
         * @param e �G���g��
         */
        public SimpleEntry(Entry<K, V> e)
        {
            this.key_ = e.getKey();
            this.value_ = e.getValue();
        }

        /**
         * �L�[���擾���܂��B
         * @return �L�[
         */
        public K getKey()
        {
            return key_;
        }

        /**
         * �l���擾���܂��B
         * @return �l
         */
        public V getValue()
        {
            return value_;
        }

        /**
         * �l��ݒ肵�܂��B
         * @param value �l
         * @return �ݒ肷��O�̒l
         */
        public V setValue(V value)
        {
            V oldValue = this.value_;
            this.value_ = value;
            return oldValue;
        }

        /**
         * ���̃C���X�^���X�Ǝw�肵���I�u�W�F�N�g�̃L�[�ƒl�������������肵�܂��B
         * @param o ��r�Ώ�
         * @return �������Ƃ�true/�����łȂƂ�false
         */
        @SuppressWarnings("unchecked")
        public boolean equals(Object o)
        {
            if (!(o instanceof Map.Entry))
            {
                return false;
            }
            Map.Entry<K, V> e = (Map.Entry<K, V>)o;
            return eq(key_, e.getKey()) && eq(value_, e.getValue());
        }

        /**
         * �n�b�V���R�[�h�𐶐����܂��B
         * @return �n�b�V���R�[�h
         */
        public int hashCode()
        {
            return ((key_ == null) ? 0 : key_.hashCode())
                    ^ ((value_ == null) ? 0 : value_.hashCode());
        }

        /**
         * �L�[�ƒl�𕶎���ɂ��Ď擾���܂��B
         * @return �L�[�ƒl�̕�����
         */
        public String toString()
        {
            return key_ + "=" + value_;
        }

        /**
         * �w�肵���I�u�W�F�N�g�������������肵�܂��B
         * @param o1 ��r�Ώ�
         * @param o2 ��r�Ώ�
         * @return �w�肵���I�u�W�F�N�g���������Ƃ�true/�����łȂ��Ƃ�false
         */
        static boolean eq(Object o1, Object o2)
        {
            return (o1 == null ? o2 == null : o1.equals(o2));
        }
    }

    private void writeObject(java.io.ObjectOutputStream s)
        throws IOException
    {
        s.defaultWriteObject();

        for (int k = 0; k < segments_.length; ++k)
        {
            Segment<K, V> seg = (Segment<K, V>)segments_[k];
            seg.lock();
            try
            {
                HashEntry<K, V>[] tab = seg.table_;
                for (int i = 0; i < tab.length; ++i)
                {
                    for (HashEntry<K, V> e = (HashEntry<K, V>)tab[i]; e != null; e = e.next_)
                    {
                        s.writeObject(e.key_);
                        s.writeObject(e.value_);
                    }
                }
            }
            finally
            {
                seg.unlock();
            }
        }
        s.writeObject(null);
        s.writeObject(null);
    }

    @SuppressWarnings("unchecked")
    private void readObject(java.io.ObjectInputStream s)
        throws IOException,
            ClassNotFoundException
    {
        s.defaultReadObject();

        for (int i = 0; i < segments_.length; ++i)
        {
            segments_[i].setTable(new HashEntry[1]);
        }

        for (;;)
        {
            K key = (K)s.readObject();
            V value = (V)s.readObject();
            if (key == null)
            {
                break;
            }
            put(key, value);
        }
    }

}
