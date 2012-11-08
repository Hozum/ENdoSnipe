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
package jp.co.acroquest.endosnipe.common.entity;

/**
 * MBean�Ōv�������l��\���񋓑̂ł��B<br />
 * �f�[�^�^�͈ȉ��̃R�[�h�Ŏ����܂��B<br />
 * <br />
 * <table border="1" cellspacing="0">
 * <tr>
 *  <th>�f�[�^�^</th>
 *  <th>���l</th>
 * </tr>
 * <tr>
 *  <td>�s��</td>
 *  <td>-1</td>
 * </tr>
 * <tr>
 *  <td>byte</td>
 *  <td>0</td>
 * </tr>
 * <tr>
 *  <td>short</td>
 *  <td>1</td>
 * </tr>
 * <tr>
 *  <td>int</td>
 *  <td>2</td>
 * </tr>
 * <tr>
 *  <td>long</td>
 *  <td>3</td>
 * </tr> 
 * <tr>
 *  <td>float</td>
 *  <td>4</td>
 * </tr>
 * <tr>
 *  <td>double</td>
 *  <td>5</td>
 * </tr>
 * <tr>
 *  <td>String</td>
 *  <td>6</td>
 * </tr>
 * <tr>
 *  <td>JSON</td>
 *  <td>7</td>
 * </tr>
 * </table>
 * 
 * @author y_asazuma
 */
public enum ItemType
{
    /** �s�� */
    ITEMTYPE_UNKNOWN, // -1
    /** byte�^ */
    ITEMTYPE_BYTE, // 0
    /** short�^ */
    ITEMTYPE_SHORT, // 1
    /** int�^ */
    ITEMTYPE_INT, // 2
    /** long�^ */
    ITEMTYPE_LONG, // 3
    /** float�^ */
    ITEMTYPE_FLOAT, // 4
    /** double�^ */
    ITEMTYPE_DOUBLE, // 5
    /** String�^ */
    ITEMTYPE_STRING, // 6
    /** JSON�^ */
    ITEMTYPE_JMX; // 7

    /**
     * ���l����f�[�^�^��Ԃ��B
     * 
     * @param n ���l
     * @return �f�[�^�^
     */
    public static ItemType getItemType(final byte n)
        throws IllegalArgumentException
    {
        switch (n)
        {
        case -1:
            return ITEMTYPE_UNKNOWN;
        case 0:
            return ITEMTYPE_BYTE;
        case 1:
            return ITEMTYPE_SHORT;
        case 2:
            return ITEMTYPE_INT;
        case 3:
            return ITEMTYPE_LONG;
        case 4:
            return ITEMTYPE_FLOAT;
        case 5:
            return ITEMTYPE_DOUBLE;
        case 6:
            return ITEMTYPE_STRING;
        case 7:
            return ITEMTYPE_JMX;
        default:
            throw new IllegalArgumentException();
        }
    }

    /**
     * �f�[�^�^�ɉ��������l���擾���܂��B
     * 
     * @param type �f�[�^�^
     * @return ���l
     * @throws IllegalArgumentException IllegalArgumentException
     */
    public static byte getItemTypeNumber(final ItemType type)
        throws IllegalArgumentException
    {
        switch (type)
        {
        case ITEMTYPE_UNKNOWN:
            return -1;
        case ITEMTYPE_BYTE:
            return 0;
        case ITEMTYPE_SHORT:
            return 1;
        case ITEMTYPE_INT:
            return 2;
        case ITEMTYPE_LONG:
            return 3;
        case ITEMTYPE_FLOAT:
            return 4;
        case ITEMTYPE_DOUBLE:
            return 5;
        case ITEMTYPE_STRING:
            return 6;
        case ITEMTYPE_JMX:
            return 7;
        default:
            throw new IllegalArgumentException();
        }
    }
}
