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

import java.io.IOException;
import java.io.Serializable;
import java.util.AbstractCollection;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * HashMap�N���X
 * @author acroquest
 *
 * @param <K> �L�[
 * @param <V> �l
 */
public class HashMap<K, V> extends AbstractMap<K, V> implements Map<K, V>, Cloneable, Serializable
{

    /** �V���A���o�[�W����ID */
    private static final long              serialVersionUID              = 8761989745265438004L;

    private static final int               OLD_HASH_LEFT_SHIFT_FOUR      = 4;

    private static final int               OLD_HASH_LEFT_SHIFT_NINE      = 9;

    private static final int               OLD_HASH_RIGHT_SHIFT_FOURTEEN = 14;

    private static final int               OLD_HASH_RIGHT_SHIFT_TEN      = 10;

    private static final int               NEW_HASH_RIGHT_SHIFT_FOUR     = 4;

    private static final int               NEW_HASH_RIGHT_SHIFT_SEVEN    = 7;

    private static final int               NEW_HASH_RIGHT_SHIFT_TWELEVE  = 12;

    private static final int               NEW_HASH_RIGHT_SHIFT_TWENTY   = 20;

    /** �f�t�H���g�̃n�b�V���}�b�v�̏����e�� */
    static final int                       DEFAULT_INITIAL_CAPACITY      = 16;

    /** ���V�t�g�� */
    static final int                       CAPACITY_SHIFT_VALUE          = 30;

    /** �n�b�V���}�b�v�̍ő�e�� */
    static final int                       MAXIMUM_CAPACITY = 1 << CAPACITY_SHIFT_VALUE;

    /** �f�t�H���g�̃n�b�V���}�b�v�̕��׌W�� */
    static final float                     DEFAULT_LOAD_FACTOR           = 0.75f;

    /** null�������L�[ */
    static final Object                    NULL_KEY                      = new Object();

    /** �V�����n�b�V�����g�p���Ă��邩�̃t���O */
    private static final boolean           USER_NEW_HASH;

    /** �G���g���Z�b�g */
    private transient Set<Map.Entry<K, V>> entrySet_                     = null;

    /** �e�[�u�� */
    transient Entry<K, V>[]                table_;

    /** �T�C�Y */
    transient int                          size_;

    /** �������l */
    int                                    threshold_;

    /** �n�b�V���}�b�v�̕��׌W�� */
    final float                            loadFactor_;

    /** �C���� */
    transient volatile int                 modCount_;

    /**
     * �R���X�g���N�^
     * @param initialCapacity �n�b�V���}�b�v�̏����e��
     * @param loadFactor �n�b�V���}�b�v�̕��׌W��
     */
    @SuppressWarnings("unchecked")
    public HashMap(int initialCapacity, float loadFactor)
    {
        if (initialCapacity < 0)
        {
            throw new IllegalArgumentException("Illegal initial capacity: " + initialCapacity);
        }
        if (initialCapacity > MAXIMUM_CAPACITY)
        {
            initialCapacity = MAXIMUM_CAPACITY;
        }
        if (loadFactor <= 0 || Float.isNaN(loadFactor))
        {
            throw new IllegalArgumentException("Illegal load factor: " + loadFactor);
        }

        int capacity = 1;
        while (capacity < initialCapacity)
        {
            capacity <<= 1;
        }

        this.loadFactor_ = loadFactor;
        threshold_ = (int)(capacity * loadFactor);
        table_ = new Entry[capacity];
        init();
    }

    /**
     * �R���X�g���N�^
     * @param initialCapacity �n�b�V���}�b�v�̏����e��
     */
    public HashMap(int initialCapacity)
    {
        this(initialCapacity, DEFAULT_LOAD_FACTOR);
    }

    /**
     * �R���X�g���N�^
     */
    @SuppressWarnings("unchecked")
    public HashMap()
    {
        this.loadFactor_ = DEFAULT_LOAD_FACTOR;
        threshold_ = (int)(DEFAULT_INITIAL_CAPACITY * DEFAULT_LOAD_FACTOR);
        table_ = new Entry[DEFAULT_INITIAL_CAPACITY];
        init();
    }

    /**
     * �R���X�g���N�^
     * @param m �}�b�v
     */
    public HashMap(Map<? extends K, ? extends V> m)
    {
        this(Math.max((int)(m.size() / DEFAULT_LOAD_FACTOR) + 1, DEFAULT_INITIAL_CAPACITY),
                DEFAULT_LOAD_FACTOR);
        putAllForCreate(m);
    }

    /**
     * ���������s���܂��B
     */
    void init()
    {
    }

    /**
     * �w�肳�ꂽ�L�[��null�̂Ƃ�null�̃I�u�W�F�N�g��Ԃ��܂��B
     * @param <T> 
     * @param key �L�[
     * @return �w�肳�ꂽ�L�[��null�̂Ƃ�null�̃I�u�W�F�N�g/�����łȂ��Ƃ��w�肳�ꂽ�L�[
     */
    @SuppressWarnings("unchecked")
    static <T> T maskNull(T key)
    {
        return key == null ? (T)NULL_KEY : key;
    }

    /**
     * �w�肳�ꂽ�L�[��null�̃I�u�W�F�N�g�Ƃ�null��Ԃ��܂��B
     * @param <T> 
     * @param key �L�[
     * @return �w�肳�ꂽ�L�[��null�̂Ƃ�null�̃I�u�W�F�N�g/�����łȂ��Ƃ��w�肳�ꂽ�L�[
     */
    static <T> T unmaskNull(T key)
    {
        return (key == NULL_KEY ? null : key);
    }

    static
    {
        USER_NEW_HASH = false;
    }

    private static int oldHash(int h)
    {
        h += ~(h << OLD_HASH_LEFT_SHIFT_NINE);
        h ^= (h >>> OLD_HASH_RIGHT_SHIFT_FOURTEEN);
        h += (h << OLD_HASH_LEFT_SHIFT_FOUR);
        h ^= (h >>> OLD_HASH_RIGHT_SHIFT_TEN);
        return h;
    }

    private static int newHash(int h)
    {
        h ^= (h >>> NEW_HASH_RIGHT_SHIFT_TWENTY) ^ (h >>> NEW_HASH_RIGHT_SHIFT_TWELEVE);
        return h ^ (h >>> NEW_HASH_RIGHT_SHIFT_SEVEN) ^ (h >>> NEW_HASH_RIGHT_SHIFT_FOUR);
    }

    /**
     * �n�b�V����Ԃ��܂��B�B
     * @param h �n�b�V���l
     * @return �n�b�V��
     */
    static int hash(int h)
    {
        return USER_NEW_HASH ? newHash(h) : oldHash(h);
    }

    /**
     * �n�b�V����Ԃ��܂��B
     * @param key �L�[
     * @return �n�b�V��
     */
    static int hash(Object key)
    {
        return hash(key.hashCode());
    }

    /**
     * �I�u�W�F�N�g�������������肵�܂��B
     * @param x ��r�ΏۃI�u�W�F�N�g
     * @param y ��r�ΏۃI�u�W�F�N�g
     * @return �I�u�W�Fy��Ƃ��������Ƃ�true/�����łȂ��Ƃ�false
     */
    static boolean eq(Object x, Object y)
    {
        return x == y || x.equals(y);
    }

    /**
     * �w�肳�ꂽ�n�b�V���ƒ����̘_���a��Ԃ��܂��B
     * @param h �n�b�V��
     * @param length ����
     * @return �n�b�V���ƒ����̘_���a
     */
    static int indexFor(int h, int length)
    {
        return h & (length - 1);
    }

    /**
     * {@inheritDoc}
     */
    public int size()
    {
        return size_;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isEmpty()
    {
        return size_ == 0;
    }

    /**
     * {@inheritDoc}
     */
    public V get(Object key)
    {
        if (key == null)
        {
            return getForNullKey();
        }
        int hash = hash(key.hashCode());
        int index = indexFor(hash, table_.length);
        for (Entry<K, V> entry = table_[index]; entry != null; entry = entry.next_)
        {
            Object k = entry.key_;
            if (entry.hash_ == hash && (k == key || key.equals(k)))
            {
                return entry.value_;
            }
        }
        return null;
    }

    /**
     * Null�L�[���擾���܂��B
     * @return
     */
    private V getForNullKey()
    {
        int hash = hash(NULL_KEY.hashCode());
        int i = indexFor(hash, table_.length);
        Entry<K, V> e = table_[i];
        while (true)
        {
            if (e == null)
            {
                return null;
            }
            if (e.key_ == NULL_KEY)
            {
                return e.value_;
            }
            e = e.next_;
        }
    }

    /**
     * {@inheritDoc}
     */
    public boolean containsKey(Object key)
    {
        Object k = maskNull(key);
        int hash = hash(k.hashCode());
        int i = indexFor(hash, table_.length);
        Entry<K, V> e = table_[i];
        while (e != null)
        {
            if (e.hash_ == hash && eq(k, e.key_))
            {
                return true;
            }
            e = e.next_;
        }
        return false;
    }

    /**
     * �w�肵���L�[�����G���g�����擾���܂��B
     * @param key �L�[
     * @return �w�肵���L�[�����G���g��
     */
    Entry<K, V> getEntry(Object key)
    {
        Object k = maskNull(key);
        int hash = hash(k.hashCode());
        int i = indexFor(hash, table_.length);
        Entry<K, V> entry = table_[i];
        while (entry != null && !(entry.hash_ == hash && eq(k, entry.key_)))
        {
            entry = entry.next_;
        }
        return entry;
    }

    /**
     * {@inheritDoc}
     */
    public V put(K key, V value)
    {
        if (key == null)
        {
            return putForNullKey(value);
        }
        int hash = hash(key.hashCode());
        int i = indexFor(hash, table_.length);
        for (Entry<K, V> e = table_[i]; e != null; e = e.next_)
        {
            Object k;
            if (e.hash_ == hash && ((k = e.key_) == key || key.equals(k)))
            {
                V oldValue = e.value_;
                e.value_ = value;
                e.recordAccess(this);
                return oldValue;
            }
        }
        modCount_++;
        addEntry(hash, key, value, i);
        return null;
    }

    @SuppressWarnings("unchecked")
    private V putForNullKey(V value)
    {
        int hash = hash(NULL_KEY.hashCode());
        int i = indexFor(hash, table_.length);

        for (Entry<K, V> e = table_[i]; e != null; e = e.next_)
        {
            if (e.key_ == NULL_KEY)
            {
                V oldValue = e.value_;
                e.value_ = value;
                return oldValue;
            }
        }

        modCount_++;
        addEntry(hash, (K)NULL_KEY, value, i);
        return null;
    }

    /**
     * �V�K�̃G���g����ǉ����܂��B
     * @param key �L�[
     * @param value �l
     */
    private void putForCreate(K key, V value)
    {
        K k = maskNull(key);
        int hash = hash(k.hashCode());
        int i = indexFor(hash, table_.length);

        for (Entry<K, V> e = table_[i]; e != null; e = e.next_)
        {
            if (e.hash_ == hash && eq(k, e.key_))
            {
                e.value_ = value;
                return;
            }
        }

        createEntry(hash, k, value, i);
    }

    /**
     * �w�肵��Map�̑S�v�f����G���g���𐶐����܂��B
     * @param m �}�b�v
     */
    void putAllForCreate(Map<? extends K, ? extends V> m)
    {
        Iterator<? extends Map.Entry<? extends K, ? extends V>> iterator = m.entrySet().iterator();
        for (Iterator<? extends Map.Entry<? extends K, ? extends V>> i = iterator; i.hasNext();)
        {
            Map.Entry<? extends K, ? extends V> e = i.next();
            putForCreate(e.getKey(), e.getValue());
        }
    }

    /**
     * �e�[�u�����w�肵���e�ʂɃ��T�C�Y���܂��B
     * @param newCapacity �e�[�u���̗e��
     */
    @SuppressWarnings("unchecked")
    void resize(int newCapacity)
    {
        Entry<K, V>[] oldTable = table_;
        int oldCapacity = oldTable.length;
        if (oldCapacity == MAXIMUM_CAPACITY)
        {
            threshold_ = Integer.MAX_VALUE;
            return;
        }

        Entry<K, V>[] newTable = new Entry[newCapacity];
        transfer(newTable);
        table_ = newTable;
        threshold_ = (int)(newCapacity * loadFactor_);
    }

    /**
     * �G���g���̔z���V�����e�[�u���Ɉړ����܂��B
     * @param newTable �V�����e�[�u��
     */
    void transfer(Entry<K, V>[] newTable)
    {
        Entry<K, V>[] src = table_;
        int newCapacity = newTable.length;
        for (int j = 0; j < src.length; j++)
        {
            Entry<K, V> e = src[j];
            if (e != null)
            {
                src[j] = null;
                do
                {
                    Entry<K, V> next = e.next_;
                    int i = indexFor(e.hash_, newCapacity);
                    e.next_ = newTable[i];
                    newTable[i] = e;
                    e = next;
                }
                while (e != null);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public void putAll(Map<? extends K, ? extends V> m)
    {
        int numKeysToBeAdded = m.size();
        if (numKeysToBeAdded == 0)
        {
            return;
        }

        if (numKeysToBeAdded > threshold_)
        {
            int targetCapacity = (int)(numKeysToBeAdded / loadFactor_ + 1);
            if (targetCapacity > MAXIMUM_CAPACITY)
            {
                targetCapacity = MAXIMUM_CAPACITY;
            }
            int newCapacity = table_.length;
            while (newCapacity < targetCapacity)
            {
                newCapacity <<= 1;
            }
            if (newCapacity > table_.length)
            {
                resize(newCapacity);
            }
        }

        Iterator<? extends Map.Entry<? extends K, ? extends V>> iterator = m.entrySet().iterator();
        for (Iterator<? extends Map.Entry<? extends K, ? extends V>> i = iterator; i.hasNext();)
        {
            Map.Entry<? extends K, ? extends V> e = i.next();
            put(e.getKey(), e.getValue());
        }
    }

    /**
     * {@inheritDoc}
     */
    public V remove(Object key)
    {
        Entry<K, V> e = removeEntryForKey(key);
        return (e == null ? null : e.value_);
    }

    /**
     * �w�肵���L�[�����G���g�����폜���܂��B
     * @param key �폜����G���g���̃L�[
     * @return �폜�����G���g��
     */
    public Entry<K, V> removeEntryForKey(Object key)
    {
        Object k = maskNull(key);
        int hash = hash(k.hashCode());
        int i = indexFor(hash, table_.length);
        Entry<K, V> prev = table_[i];
        Entry<K, V> entry = prev;

        while (entry != null)
        {
            Entry<K, V> next = entry.next_;
            if (entry.hash_ == hash && eq(k, entry.key_))
            {
                modCount_++;
                size_--;
                if (prev == entry)
                {
                    table_[i] = next;
                }
                else
                {
                    prev.next_ = next;
                }
                return entry;
            }
            prev = entry;
            entry = next;
        }

        return entry;
    }

    /**
     * �w�肵���G���g����Mapping���폜���܂��B
     * @param o �G���g��
     * @return �폜�����G���g��
     */
    @SuppressWarnings("unchecked")
    Entry<K, V> removeMapping(Object o)
    {
        if (!(o instanceof Map.Entry))
        {
            return null;
        }

        Map.Entry<K, V> entry = (Map.Entry<K, V>)o;
        Object k = maskNull(entry.getKey());
        int hash = hash(k.hashCode());
        int i = indexFor(hash, table_.length);
        Entry<K, V> prev = table_[i];
        Entry<K, V> e = prev;

        while (e != null)
        {
            Entry<K, V> next = e.next_;
            if (e.hash_ == hash && e.equals(entry))
            {
                modCount_++;
                size_--;
                if (prev == e)
                {
                    table_[i] = next;
                }
                else
                {
                    prev.next_ = next;
                }
                return e;
            }
            prev = e;
            e = next;
        }

        return e;
    }

    /**
     * {@inheritDoc}
     */
    public void clear()
    {
        modCount_++;
        Entry<K, V>[] tab = table_;
        for (int i = 0; i < tab.length; i++)
        {
            tab[i] = null;
        }
        size_ = 0;
    }

    /**
     * {@inheritDoc}
     */
    public boolean containsValue(Object value)
    {
        if (value == null)
        {
            return containsNullValue();
        }

        Entry<K, V>[] tab = table_;
        for (int i = 0; i < tab.length; i++)
        {
            for (Entry<K, V> e = tab[i]; e != null; e = e.next_)
            {
                if (value.equals(e.value_))
                {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean containsNullValue()
    {
        Entry<K, V>[] tab = table_;
        for (int i = 0; i < tab.length; i++)
        {
            for (Entry<K, V> e = tab[i]; e != null; e = e.next_)
            {
                if (e.value_ == null)
                {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    public Object clone()
    {
        HashMap<K, V> result = null;
        try
        {
            result = (HashMap<K, V>)super.clone();
        }
        catch (CloneNotSupportedException e)
        {
            // assert false;
        }
        result.table_ = new Entry[table_.length];
        result.entrySet_ = null;
        result.modCount_ = 0;
        result.size_ = 0;
        result.init();
        result.putAllForCreate(this);

        return result;
    }

    /**
     * �G���g���N���X
     * @author acroquest
     *
     * @param <K> �L�[
     * @param <V> �l
     */
    static class Entry<K, V> implements Map.Entry<K, V>
    {
        /** �L�[ */
        final K     key_;

        /** �l */
        V           value_;

        /** �n�b�V�� */
        final int   hash_;

        /** ���̃G���g�� */
        Entry<K, V> next_;

        /** 
         * �R���X�g���N�^
         * @param hash �n�b�V��
         * @param key �L�[
         * @param value �l
         * @param next ���̃G���g��
         */
        Entry(int hash, K key, V value, Entry<K, V> next)
        {
            value_ = value;
            next_ = next;
            key_ = key;
            hash_ = hash;
        }

        /**
         * �L�[���擾���܂��B
         * @return �L�[
         */
        public K getKey()
        {
            return HashMap.<K> unmaskNull(key_);
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
         * @param newValue �V�����l
         * @return �Â��l
         */
        public V setValue(V newValue)
        {
            V oldValue = value_;
            value_ = newValue;
            return oldValue;
        }

        /**
         * {@inheritDoc}
         */
        @SuppressWarnings("unchecked")
        public boolean equals(Object o)
        {
            if (!(o instanceof Map.Entry))
            {
                return false;
            }
            Map.Entry<K, V> e = (Entry<K, V>)o;
            Object k1 = getKey();
            Object k2 = e.getKey();
            if (k1 == k2 || (k1 != null && k1.equals(k2)))
            {
                Object v1 = getValue();
                Object v2 = e.getValue();
                if (v1 == v2 || (v1 != null && v1.equals(v2)))
                {
                    return true;
                }
            }
            return false;
        }

        /**
         * {@inheritDoc}
         */
        public int hashCode()
        {
            int keyHash = 0;
            int valueHash = 0;

            if (key_ != NULL_KEY)
            {
                keyHash = key_.hashCode();
            }

            if (value_ != null)
            {
                valueHash = value_.hashCode();
            }

            int result = keyHash ^ valueHash;

            return result;
        }

        /**
         * {@inheritDoc}
         */
        public String toString()
        {
            return getKey() + "=" + getValue();
        }
        
        void recordAccess(HashMap<K, V> m)        
        {
            
        }
    }

    /**
     * �G���g����ǉ����܂��B
     * @param hash �n�b�V��
     * @param key �L�[
     * @param value �l
     * @param bucketIndex �i�[����C���f�b�N�X
     */
    void addEntry(int hash, K key, V value, int bucketIndex)
    {
        Entry<K, V> e = table_[bucketIndex];
        table_[bucketIndex] = new Entry<K, V>(hash, key, value, e);
        if (size_++ >= threshold_)
        {
            resize(2 * table_.length);
        }
    }

    /**
     * �G���g���𐶐����܂��B
     * @param hash �n�b�V��
     * @param key �L�[
     * @param value �l
     * @param bucketIndex �i�[����C���f�b�N�X
     */
    void createEntry(int hash, K key, V value, int bucketIndex)
    {
        Entry<K, V> e = table_[bucketIndex];
        table_[bucketIndex] = new Entry<K, V>(hash, key, value, e);
        size_++;
    }

    /**
     * �n�b�V���C�e���[�^�N���X
     * @author acorquest
     *
     * @param <E> element
     */
    private abstract class HashIterator<E> implements Iterator<E>
    {
        Entry<K, V> next_;            // next entry to return

        int         expectedModCount_; // For fast-fail 

        int         index_;           // current slot 

        Entry<K, V> current_;         // current entry

        HashIterator()
        {
            expectedModCount_ = modCount_;
            Entry<K, V>[] t = table_;
            int i = t.length;
            Entry<K, V> n = null;
            if (size_ != 0)
            { // advance to first entry
                while (i > 0 && (n = t[--i]) == null)
                    ;
            }
            next_ = n;
            index_ = i;
        }

        public boolean hasNext()
        {
            return next_ != null;
        }

        Entry<K, V> nextEntry()
        {
            if (modCount_ != expectedModCount_)
            {
                throw new ConcurrentModificationException();
            }
            Entry<K, V> e = next_;
            if (e == null)
            {
                throw new NoSuchElementException();
            }

            Entry<K, V> n = e.next_;
            Entry<K, V>[] t = table_;
            int i = index_;
            while (n == null && i > 0)
            {
                n = t[--i];
            }
            index_ = i;
            next_ = n;
            current_ = e;
            return current_;
        }

        public void remove()
        {
            if (current_ == null)
            {
                throw new IllegalStateException();
            }
            if (modCount_ != expectedModCount_)
            {
                throw new ConcurrentModificationException();
            }
            Object k = current_.key_;
            current_ = null;
            HashMap.this.removeEntryForKey(k);
            expectedModCount_ = modCount_;
        }

    }

    /**
     * �l�̃C�e���[�^�N���X
     * @author acroquest
     *
     */
    private class ValueIterator extends HashIterator<V>
    {
        public V next()
        {
            return nextEntry().value_;
        }
    }

    /**
     * �L�[�̃C�e���[�^�N���X
     * @author acroquest
     *
     */
    private class KeyIterator extends HashIterator<K>
    {
        public K next()
        {
            return nextEntry().getKey();
        }
    }

    /**
     * �G���g���̃C�e���[�^�N���X
     * @author acroquest
     *
     */
    private class EntryIterator extends HashIterator<Map.Entry<K, V>>
    {
        public Map.Entry<K, V> next()
        {
            return nextEntry();
        }
    }

    // Subclass overrides these to alter behavior of views' iterator() method

    /**
     * �V�K�̃L�[�̃C�e���[�^���擾���܂��B
     * @return �V�K�̃L�[�̃C�e���[�^
     */
    Iterator<K> newKeyIterator()
    {
        return new KeyIterator();
    }

    /**
     * �V�K�̒l�̃C�e���[�^���擾���܂��B
     * @return �V�K�̒l�̃C�e���[�^
     */
    Iterator<V> newValueIterator()
    {
        return new ValueIterator();
    }

    /**
     * �V�K�̃G���g���̃C�e���[�^���擾���܂��B
     * @return �V�K�̃G���g���̃C�e���[�^
     */
    Iterator<Map.Entry<K, V>> newEntryIterator()
    {
        return new EntryIterator();
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
     * �L�[�Z�b�g�N���X
     * @author acorquest
     *
     */
    private class KeySet extends AbstractSet<K>
    {
        public Iterator<K> iterator()
        {
            return newKeyIterator();
        }

        public int size()
        {
            return size_;
        }

        public boolean contains(Object o)
        {
            return containsKey(o);
        }

        public boolean remove(Object o)
        {
            return HashMap.this.removeEntryForKey(o) != null;
        }

        public void clear()
        {
            HashMap.this.clear();
        }
    }

    /**
     * {@inheritDoc}
     */
    public Collection<V> values()
    {
        Collection<V> vs = values_;

        if (vs != null)
        {
            return vs;
        }
        values_ = new Values();
        return values_;
    }

    /**
     * �l�̈ꗗ�N���X
     * @author acroquest
     *
     */
    private class Values extends AbstractCollection<V>
    {
        public Iterator<V> iterator()
        {
            return newValueIterator();
        }

        public int size()
        {
            return size_;
        }

        public boolean contains(Object o)
        {
            return containsValue(o);
        }

        public void clear()
        {
            HashMap.this.clear();
        }
    }

    /**
     * {@inheritDoc}
     */
    public Set<Map.Entry<K, V>> entrySet()
    {
        Set<Map.Entry<K, V>> es = entrySet_;

        if (es != null)
        {
            return es;
        }

        entrySet_ = (Set<Map.Entry<K, V>>)new EntrySet();

        return entrySet_;
    }

    /**
     * �G���g���[�Z�b�g�N���X
     * @author acroquest
     *
     */
    private class EntrySet extends AbstractSet<Map.Entry<K, V>>
    {
        /**
         * {@inheritDoc}
         */
        public Iterator<Map.Entry<K, V>> iterator()
        {
            return newEntryIterator();
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
            Entry<K, V> candidate = getEntry(e.getKey());
            return candidate != null && candidate.equals(e);
        }

        /**
         * {@inheritDoc}
         */
        public boolean remove(Object o)
        {
            return removeMapping(o) != null;
        }

        /**
         * {@inheritDoc}
         */
        public int size()
        {
            return size_;
        }

        /**
         * {@inheritDoc}
         */
        public void clear()
        {
            HashMap.this.clear();
        }
    }

    /**
     * writerObject�𐶐����܂��B
     * @param s �o�̓X�g���[��
     * @throws IOException ���o�͗�O
     */
    private void writeObject(java.io.ObjectOutputStream s)
        throws IOException
    {
        Iterator<Map.Entry<K, V>> i = entrySet().iterator();

        s.defaultWriteObject();

        s.writeInt(table_.length);

        s.writeInt(size_);

        while (i.hasNext())
        {
            Map.Entry<K, V> e = i.next();
            s.writeObject(e.getKey());
            s.writeObject(e.getValue());
        }
    }

    /**
     * readObject�𐶐����܂��B
     * @param s ���̓X�g���[��
     * @throws IOException ���o�͗�O
     * @throws ClassNotFoundException�@�N���X��������Ȃ��������̗�O
     */
    @SuppressWarnings("unchecked")
    private void readObject(java.io.ObjectInputStream s)
        throws IOException,
            ClassNotFoundException
    {
        s.defaultReadObject();

        int numBuckets = s.readInt();
        table_ = new Entry[numBuckets];

        init();

        int size = s.readInt();

        for (int i = 0; i < size; i++)
        {
            K key = (K)s.readObject();
            V value = (V)s.readObject();
            putForCreate(key, value);
        }
    }

    /**
     * �e�[�u���̒�����Ԃ��܂��B
     * @return �e�[�u���̒���
     */
    int getLength()
    {
        return table_.length;
    }

    /**
     * �n�b�V���}�b�v�̕��׌W�����擾���܂��B
     * @return �n�b�V���}�b�v�̕��׌W��
     */
    float loadFactor()
    {
        return loadFactor_;
    }
}
