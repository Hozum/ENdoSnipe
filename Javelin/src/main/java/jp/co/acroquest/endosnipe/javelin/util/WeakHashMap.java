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

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.AbstractCollection;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * WeakHashMap�N���X
 * @author acroquest
 *
 * @param <K> �L�[
 * @param <V> �l
 */
public class WeakHashMap<K, V> extends AbstractMap<K, V> implements Map<K, V>
{

    /** �f�t�H���g�����e�� */
    private static final int               DEFAULT_INITIAL_CAPACITY = 16;

    /** �V�t�g�� */
    private static final int               SHIFT                    = 30;

    /** �ő�e�� */
    private static final int               MAXIMUM_CAPACITY         = 1 << SHIFT;

    /** �f�t�H���g�g�p�� */
    private static final float             DEFAULT_LOAD_FACTOR      = 0.75f;

    /** �G���g���[ */
    private Entry<K, V>[]                  table_;

    /** �T�C�Y */
    private int                            size_;

    /** �������l */
    private int                            threshold_;

    /** �g�p�� */
    private float                          loadFactor_;

    /** �Q�ƃL���[ */
    private final ReferenceQueue<K>        queue_                   = new ReferenceQueue<K>();

    private volatile int                   modCount_;

    /** null�L�[ */
    private static final Object            NULL_KEY                 = new Object();

    /** �G���g���[�̃Z�b�g */
    private transient Set<Map.Entry<K, V>> entrySet_                = null;

    /**
     * �R���X�g���N�^
     * @param initialCapacity �����e��
     * @param loadFactor �g�p��
     */
    @SuppressWarnings("unchecked")
    public WeakHashMap(int initialCapacity, float loadFactor)
    {
        if (initialCapacity < 0)
        {
            throw new IllegalArgumentException("Illegal Initial Capacity: " + initialCapacity);
        }

        if (initialCapacity > MAXIMUM_CAPACITY)
        {
            initialCapacity = MAXIMUM_CAPACITY;
        }

        if (loadFactor <= 0 || Float.isNaN(loadFactor))
        {
            throw new IllegalArgumentException("Illegal Load factor: " + loadFactor);
        }

        int capacity = 1;
        while (capacity < initialCapacity)
        {
            capacity <<= 1;
            table_ = new Entry[capacity];
            this.loadFactor_ = loadFactor;
            threshold_ = (int)(capacity * loadFactor);
        }
    }

    /**
     * �R���X�g���N�^
     * @param initialCapacity �����e��
     */
    public WeakHashMap(int initialCapacity)
    {
        this(initialCapacity, DEFAULT_LOAD_FACTOR);
    }

    /**
     * �R���X�g���N�^
     */
    @SuppressWarnings("unchecked")
    public WeakHashMap()
    {
        this.loadFactor_ = DEFAULT_LOAD_FACTOR;
        threshold_ = (int)(DEFAULT_INITIAL_CAPACITY);
        table_ = new Entry[DEFAULT_INITIAL_CAPACITY];
    }

    /**
     * �R���X�g���N�^
     * @param t �}�b�v
     */
    public WeakHashMap(Map<? extends K, ? extends V> t)
    {
        this(Math.max((int)(t.size() / DEFAULT_LOAD_FACTOR) + 1, DEFAULT_INITIAL_CAPACITY),
                DEFAULT_LOAD_FACTOR);
        putAll(t);
    }

    private static Object maskNull(Object key)
    {
        return (key == null ? NULL_KEY : key);
    }

    @SuppressWarnings("unchecked")
    private static <K> K unmaskNull(Object key)
    {
        return (K)(key == NULL_KEY ? null : key);
    }

    /**
     * �w�肵���p�����[�^�������������肷��
     * @param x ��r����I�u�W�F�N�g
     * @param y ��r����I�u�W�F�N�g
     * @return �������Ƃ�true/�������Ȃ��Ƃ�false
     */
    static boolean eq(Object x, Object y)
    {
        if (x == y || x.equals(y))
        {
            return true;
        }

        return false;
    }

    /**
     * �w�肵���p�����[�^�ƒ����̃T�C�Y��AND���Z���s���A���ʂ�Ԃ��B
     * @param h �p�����[�^
     * @param length ����
     * @return ���Z����
     */
    static int indexFor(int h, int length)
    {
        int result = h & (length - 1);

        return result;
    }

    /**
     * �Â��G���g�����폜����
     */
    @SuppressWarnings("unchecked")
    private void expungeStaleEntries()
    {
        Entry<K, V> entry;
        while ((entry = (Entry<K, V>)queue_.poll()) != null)
        {
            int h = entry.hash_;
            int i = indexFor(h, table_.length);

            Entry<K, V> prev = table_[i];
            Entry<K, V> previousEntry = prev;
            while (previousEntry != null)
            {
                Entry<K, V> next = previousEntry.next_;
                if (previousEntry == entry)
                {
                    if (prev == entry)
                    {
                        table_[i] = next;
                    }
                    else
                    {
                        prev.next_ = next;
                        entry.next_ = null;
                        entry.value_ = null;
                        size_--;
                        break;
                    }
                }
                prev = previousEntry;
                previousEntry = next;
            }
        }
    }

    /**
     * �e�[�u�����擾����B
     * @return �e�[�u��
     */
    private Entry<K, V>[] getTable()
    {
        expungeStaleEntries();
        return table_;
    }

    /**
     * {@inheritDoc}
     */
    public int size()
    {
        if (size_ == 0)
        {
            return 0;
        }
        expungeStaleEntries();
        return size_;
    }

    /**
     * �󂩂ǂ������肷��B
     * @return ��̂Ƃ�true/�����łȂ��Ƃ�false
     */
    public boolean isEmpty()
    {
        if (size() == 0)
        {
            return true;
        }

        return false;
    }

    /**
     * {@inheritDoc}
     */
    public V get(Object key)
    {
        Object k = maskNull(key);
        int h = HashMap.hash(k);
        Entry<K, V>[] tab = getTable();
        int index = indexFor(h, tab.length);
        Entry<K, V> e = tab[index];
        while (e != null)
        {
            if (e.hash_ == h && eq(k, e.get()))
            {
                return e.value_;
            }
            e = e.next_;
        }
        return null;
    }

    /**
     *�@�w�肵���L�[���܂�ł��邩���肷��B
     * @param key �܂�ł��邩�m�F����L�[
     * @return �܂�ł���Ƃ�true/�����łȂ��Ƃ�false
     */
    public boolean containsKey(Object key)
    {
        if (getEntry(key) != null)
        {
            return true;
        }

        return false;
    }

    /**
     * �G���g���[���擾����B
     * @param key �L�[
     * @return �G���g���[
     */
    public Entry<K, V> getEntry(Object key)
    {
        Object k = maskNull(key);
        int h = HashMap.hash(k);
        Entry<K, V>[] tab = getTable();
        int index = indexFor(h, tab.length);
        Entry<K, V> entory = tab[index];
        while (entory != null && !(entory.hash_ == h && eq(k, entory.get())))
        {
            entory = entory.next_;
        }

        return entory;
    }

    /**
     * �w�肵���L�[�ƒl���G���g���[�ɐݒ肷��B
     * @param key �L�[
     * @param value �l
     * @return �ݒ肵���G���g���[
     */
    @SuppressWarnings("unchecked")
    public V put(K key, V value)
    {
        K k = (K)maskNull(key);
        int h = HashMap.hash(k);
        Entry<K, V>[] tab = getTable();
        int i = indexFor(h, tab.length);

        for (Entry<K, V> e = tab[i]; e != null; e = e.next_)
        {
            if (h == e.hash_ && eq(k, e.get()))
            {
                V oldValue = e.value_;
                if (value != oldValue)
                {
                    e.value_ = value;
                }
                return oldValue;
            }
        }

        modCount_++;
        Entry<K, V> e = tab[i];
        tab[i] = new Entry<K, V>(k, value, queue_, h, e);
        if (++size_ >= threshold_)
        {
            resize(tab.length * 2);
        }
        return null;
    }

    /**
     * �w�肵���e�ʂŃ��T�C�Y���s���B
     * @param newCapacity �V�K�w�肷��e��
     */
    @SuppressWarnings("unchecked")
    void resize(int newCapacity)
    {
        Entry<K, V>[] oldTable = getTable();
        int oldCapacity = oldTable.length;
        if (oldCapacity == MAXIMUM_CAPACITY)
        {
            threshold_ = Integer.MAX_VALUE;
            return;
        }

        Entry<K, V>[] newTable = new Entry[newCapacity];
        transfer(oldTable, newTable);
        table_ = newTable;

        if (size_ >= threshold_ / 2)
        {
            threshold_ = (int)(newCapacity * loadFactor_);
        }
        else
        {
            expungeStaleEntries();
            transfer(newTable, oldTable);
            table_ = oldTable;
        }
    }

    /**
     * �G���g�����ڂ��ւ���
     * @param src �ړ����̃G���g��
     * @param dest �ړ���̃G���g��
     */
    private void transfer(Entry<K, V>[] src, Entry<K, V>[] dest)
    {
        for (int j = 0; j < src.length; ++j)
        {
            Entry<K, V> entry = src[j];
            src[j] = null;
            while (entry != null)
            {
                Entry<K, V> next = entry.next_;
                Object key = entry.get();
                if (key == null)
                {
                    entry.next_ = null;
                    entry.value_ = null;
                    size_--;
                }
                else
                {
                    int i = indexFor(entry.hash_, dest.length);
                    entry.next_ = dest[i];
                    dest[i] = entry;
                }
                entry = next;
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

        Iterator<? extends Map.Entry<? extends K, ? extends V>> i = m.entrySet().iterator();
        while (i.hasNext())
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
        Object k = maskNull(key);
        int h = HashMap.hash(k);
        Entry<K, V>[] tab = getTable();
        int i = indexFor(h, tab.length);
        Entry<K, V> prev = tab[i];
        Entry<K, V> e = prev;

        while (e != null)
        {
            Entry<K, V> next = e.next_;
            if (h == e.hash_ && eq(k, e.get()))
            {
                modCount_++;
                size_--;
                if (prev == e)
                {
                    tab[i] = next;
                }
                else
                {
                    prev.next_ = next;
                }
                return e.value_;
            }
            prev = e;
            e = next;
        }

        return null;
    }

    /**
     * Map���폜���܂��B
     * @param object �폜�Ώۂ�Map
     * @return �폜����Map/�폜���s����null
     */
    @SuppressWarnings("unchecked")
    public Entry<K, V> removeMapping(Object object)
    {
        if (!(object instanceof Map.Entry))
        {
            return null;
        }

        Entry<K, V>[] tab = getTable();
        Map.Entry<K, V> entry = (Map.Entry<K, V>)object;
        Object k = maskNull(entry.getKey());
        int h = HashMap.hash(k);
        int i = indexFor(h, tab.length);
        Entry<K, V> prev = tab[i];
        Entry<K, V> e = prev;

        while (e != null)
        {
            Entry<K, V> next = e.next_;
            if (h == e.hash_ && e.equals(entry))
            {
                modCount_++;
                size_--;
                if (prev == e)
                {
                    tab[i] = next;
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

        return null;
    }

    /**
     * {@inheritDoc}
     */
    public void clear()
    {

        while (queue_.poll() != null)
            ;

        modCount_++;
        Entry[] tab = table_;
        for (int i = 0; i < tab.length; ++i)
        {
            tab[i] = null;
        }
        size_ = 0;

        while (queue_.poll() != null)
            ;
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
        Entry<K, V>[] tab = getTable();
        for (int i = tab.length; i-- > 0;)
        {
            for (Entry<K, V> entry = tab[i]; entry != null; entry = entry.next_)
            {
                if (value.equals(entry.value_))
                {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean containsNullValue()
    {
        Entry<K, V>[] tab = getTable();
        for (int i = tab.length; i-- > 0;)
        {
            for (Entry<K, V> entry = tab[i]; entry != null; entry = entry.next_)
            {
                if (entry.value_ == null)
                {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * �G���g���[�N���X
     * @author acroquest
     *
     * @param <K> �L�[
     * @param <V> �l
     */
    private static class Entry<K, V> extends WeakReference<K> implements Map.Entry<K, V>
    {
        /** �l */
        private V           value_;

        /** �n�b�V�� */
        private final int   hash_;

        /** ���̃G���g�� */
        private Entry<K, V> next_;

        /**
         * �R���X�g���N�^
         * @param key �L�[
         * @param value �l
         * @param queue �L���[
         * @param hash �n�b�V��
         * @param next ���̃G���g��
         */
        public Entry(K key, V value, ReferenceQueue<K> queue, int hash, Entry<K, V> next)
        {
            super(key, queue);
            this.value_ = value;
            this.hash_ = hash;
            this.next_ = next;
        }

        /**
         * {@inheritDoc}
         */
        public K getKey()
        {
            return WeakHashMap.<K> unmaskNull(get());
        }

        /**
         * {@inheritDoc}
         */
        public V getValue()
        {
            return value_;
        }

        /**
         * {@inheritDoc}
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
            Map.Entry<K, V> e = (Map.Entry<K, V>)o;
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
            Object k = getKey();
            Object v = getValue();
            return ((k == null ? 0 : k.hashCode()) ^ (v == null ? 0 : v.hashCode()));
        }

        /**
         * {@inheritDoc}
         */
        public String toString()
        {
            return getKey() + "=" + getValue();
        }
    }

    /**
     * Hash�̃C�e���[�^�p���ۃN���X
     * @author acroquest
     *
     * @param <T>
     */
    private abstract class HashIterator<T> implements Iterator<T>
    {
        /** �C���f�b�N�X */
        int         index_;

        /** �G���g�� */
        Entry<K, V> entry_            = null;

        Entry<K, V> lastReturned_     = null;

        int         expectedModCount_ = modCount_;

        /** ���̃L�[ */
        Object      nextKey_          = null;

        /** ���݂̃L�[ */
        Object      currentKey_       = null;

        /**
         * �R���X�g���N�^
         */
        public HashIterator()
        {
            index_ = (size() != 0 ? table_.length : 0);
        }

        /**
         * {@inheritDoc}
         */
        public boolean hasNext()
        {
            Entry<K, V>[] table = table_;

            while (nextKey_ == null)
            {
                Entry<K, V> entry = entry_;
                int i = index_;
                while (entry == null && i > 0)
                {
                    entry = table[--i];
                }
                entry_ = entry;
                index_ = i;
                if (entry == null)
                {
                    currentKey_ = null;
                    return false;
                }
                nextKey_ = entry.get();
                if (nextKey_ == null)
                {
                    entry_ = entry_.next_;
                }
            }
            return true;
        }

        /**
         * ���̃G���g����Ԃ��܂��B
         * @return ���̃G���g��
         */
        protected Entry<K, V> nextEntry()
        {
            if (modCount_ != expectedModCount_)
            {
                throw new ConcurrentModificationException();
            }

            if (nextKey_ == null && !hasNext())
            {
                throw new NoSuchElementException();
            }

            lastReturned_ = entry_;
            entry_ = entry_.next_;
            currentKey_ = nextKey_;
            nextKey_ = null;
            return lastReturned_;
        }

        /**
         * {@inheritDoc}
         */
        public void remove()
        {
            if (lastReturned_ == null)
            {
                throw new IllegalStateException();
            }

            if (modCount_ != expectedModCount_)
            {
                throw new ConcurrentModificationException();
            }

            WeakHashMap.this.remove(currentKey_);
            expectedModCount_ = modCount_;
            lastReturned_ = null;
            currentKey_ = null;
        }

    }

    /**
     * �l�p�C�e���[�^
     * @author acroquest
     */
    private class ValueIterator extends HashIterator<V>
    {
        public V next()
        {
            return nextEntry().value_;
        }
    }

    /**
     * �L�[�p�C�e���[�^
     * @author acroquest
     */
    private class KeyIterator extends HashIterator<K>
    {
        public K next()
        {
            return nextEntry().getKey();
        }
    }

    /**
     * �G���g���[�p�C�e���[�^
     * @author acroquest
     */
    private class EntryIterator extends HashIterator<Map.Entry<K, V>>
    {
        public Map.Entry<K, V> next()
        {
            return nextEntry();
        }
    }

    /**
     * {@inheritDoc}
     */
    public Set<K> keySet()
    {
        Set<K> ks = keySet_;

        if (ks == null)
        {
            keySet_ = new KeySet();
            return keySet_;
        }

        return ks;
    }

    /**
     * �L�[�Z�b�g�̒��ۃN���X
     * @author acroquest
     *
     */
    private class KeySet extends AbstractSet<K>
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
            return WeakHashMap.this.size();
        }

        /**
         * {@inheritDoc}
         */
        public boolean contains(Object o)
        {
            return containsKey(o);
        }

        /**
         * {@inheritDoc}
         */
        public boolean remove(Object o)
        {
            if (containsKey(o))
            {
                WeakHashMap.this.remove(o);
                return true;
            }
            else
            {
                return false;
            }
        }

        /**
         * {@inheritDoc}
         */
        public void clear()
        {
            WeakHashMap.this.clear();
        }

        /**
         * {@inheritDoc}
         */
        public Object[] toArray()
        {
            Collection<K> c = new ArrayList<K>(size());
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
            Collection<K> c = new ArrayList<K>(size());
            for (Iterator<K> i = iterator(); i.hasNext();)
            {
                c.add(i.next());
            }
            return c.toArray(a);
        }
    }

    /**
     * {@inheritDoc}
     */
    public Collection<V> values()
    {
        Collection<V> vs = values_;
        if (vs == null)
        {
            values_ = new Values();
            return vs;
        }

        return vs;
    }

    /**
     * �l�̈ꗗ�̃N���X
     * @author acroquest
     *
     */
    private class Values extends AbstractCollection<V>
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
            return WeakHashMap.this.size();
        }

        /**
         * {@inheritDoc}
         */
        public boolean contains(Object o)
        {
            return containsValue(o);
        }

        /**
         * {@inheritDoc}
         */
        public void clear()
        {
            WeakHashMap.this.clear();
        }

        /**
         * {@inheritDoc}
         */
        public Object[] toArray()
        {
            Collection<V> c = new ArrayList<V>(size());
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
            Collection<V> c = new ArrayList<V>(size());
            for (Iterator<V> i = iterator(); i.hasNext();)
            {
                c.add(i.next());
            }
            return c.toArray(a);
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
            entrySet_ = new EntrySet();
            return entrySet_;
        }

        return es;
    }

    /**
     * Entry��Set�p�N���X
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
            return WeakHashMap.this.size();
        }

        /**
         * {@inheritDoc}
         */
        public void clear()
        {
            WeakHashMap.this.clear();
        }

        /**
         * {@inheritDoc}
         */
        public Object[] toArray()
        {
            Collection<Map.Entry<K, V>> c = new ArrayList<Map.Entry<K, V>>(size());
            for (Iterator<Map.Entry<K, V>> i = iterator(); i.hasNext();)
            {
                c.add(new AbstractMap.SimpleEntry<K, V>(i.next()));
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
                c.add(new AbstractMap.SimpleEntry<K, V>(i.next()));
            }
            return c.toArray(a);
        }
    }
}
