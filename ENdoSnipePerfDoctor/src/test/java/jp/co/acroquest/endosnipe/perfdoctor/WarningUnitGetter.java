package jp.co.acroquest.endosnipe.perfdoctor;

/**
 * WarningUnit���쐬���邽�߂̃N���X�B
 * @author fujii
 *
 */
public class WarningUnitGetter
{
    /**
     * WarningUnit���쐬����B
     * @param unitId �x����ID
     * @param id ���[����ID
     * @param description �x���̐����B
     * @param className �N���X���B
     * @param methodName ���\�b�h���B
     * @param level �d�v�x
     * @param logFileName ���O�t�@�C�����B
     * @param logFileLineNumber �s�ԍ��B
     * @param args 臒l�A���o�l�Ȃǂ̈����B
     * @return WarnignUnit WarningUnit
     */
    public static WarningUnit createWarningUnit(final String unitId, final String id,
        final String description, final String className, final String methodName,
        final String level, final String logFileName, final int logFileLineNumber,
        final long startTime, final long endTime, final Object[] args)
    {
        return new WarningUnit(unitId, id, description, className, methodName, level, logFileName,
                               logFileLineNumber, startTime, endTime, true, args);
    }

    /**
     * WarningUnit���쐬����B
     * @param unitId �x����ID
     * @param id ���[����ID
     * @param description �x���̐����B
     * @param className �N���X���B
     * @param methodName ���\�b�h���B
     * @param level �d�v�x
     * @param logFileName ���O�t�@�C�����B
     * @param logFileLineNumber �s�ԍ��B
     * @param isEvent �C�x���g�ł��邩�ǂ����B
     * @param stackTrace �X�^�b�N�g���[�X
     * @param args 臒l�A���o�l�Ȃǂ̈����B
     * @return WarnignUnit WarningUnit
     */
    public static WarningUnit createWarningUnit(final String unitId, final String id,
        final String description, final String className, final String methodName,
        final String level, final String logFileName, final int logFileLineNumber,
        final long startTime, final long endTime, final boolean isEvent, final String stackTrace,
        final Object[] args)
    {
        return new WarningUnit(unitId, id, description, className, methodName, level, logFileName,
                               logFileLineNumber, startTime, endTime, true, isEvent, stackTrace,
                               args, null);
    }

}
