package org.jdyna.frontend.swing;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayInputStream;
import java.io.IOException;

import javax.swing.*;

import org.jdyna.frontend.swing.Configuration.SoundEngine;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

/**
 * Configuration dialog.
 */
final class ConfigurationDialog
{
    private Configuration configClone;
    private Configuration original;

    public ConfigurationDialog(Configuration config)
    {
        this.original = config;

        // Clone by persisting and re-reading.
        try
        {
            this.configClone = Configuration.load(
                new ByteArrayInputStream(config.save().getBytes("UTF-8")));
        }
        catch (IOException e)
        {
            this.configClone = new Configuration();
        }
    }

    /**
     * Create a modal dialog asking for preferences.
     */
    public Configuration prompt(Component parent)
    {
        final JComponent configPanel = createConfigPanel();

        if (JOptionPane.OK_OPTION == JOptionPane.showOptionDialog(parent, configPanel,
            "Edit configuration", JOptionPane.OK_CANCEL_OPTION,
            JOptionPane.PLAIN_MESSAGE, null, null, null))
        {
            return configClone;
        }

        return original;
    }

    /*
     * 
     */
    private JComponent createConfigPanel()
    {
        final FormLayout layout = new FormLayout(
            "right:[40dlu,pref], 3dlu, 70dlu, 3dlu");
        
        final DefaultFormBuilder builder = new DefaultFormBuilder(layout);
        builder.setDefaultDialogBorder();

        builder.appendSeparator("Audio settings");

        final JComboBox engineCombo = new JComboBox(SoundEngine.values());
        engineCombo.setSelectedItem(configClone.soundEngine);
        engineCombo.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
                configClone.soundEngine = (SoundEngine) engineCombo.getSelectedItem();
            }
        });
        builder.append("Engine:", engineCombo);
        builder.nextLine();

        return builder.getPanel();        
    }
}
