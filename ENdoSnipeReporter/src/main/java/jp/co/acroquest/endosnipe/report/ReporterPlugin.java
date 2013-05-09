package jp.co.acroquest.endosnipe.report;

import jp.co.acroquest.endosnipe.report.ReporterPlugin;

import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * ENdoSnipeReporter��ReportPlugin�Ƃ��Ďg�p���邽�߂̃N���X
 * 
 * @author kimura
 */
public class ReporterPlugin extends AbstractUIPlugin
{

    /** �v���O�C��ID */
    public static final String    PLUGIN_ID = "ENdoSnipeReport";

    /** �v���O�C���̎Q�Ɛ� */
    private static ReporterPlugin plugin__;

    /**
     * The constructor
     */
    public ReporterPlugin()
    {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void start(BundleContext context)
        throws Exception
    {
        super.start(context);
        plugin__ = this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void stop(BundleContext context)
        throws Exception
    {
        plugin__ = null;
        super.stop(context);
    }

    /**
     * Returns the shared instance
     *
     * @return the shared instance
     */
    public static ReporterPlugin getDefault()
    {
        return plugin__;
    }

}
