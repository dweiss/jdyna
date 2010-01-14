package org.jdyna.frontend.swing;

import java.awt.CardLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashSet;

import javax.swing.*;

import org.jdyna.frontend.swing.Configuration.SoundEngine;
import org.jdyna.frontend.swing.Configuration.ViewType;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.CellConstraints;
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
        final JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Graphics", createGraphicsPanel());
        tabs.addTab("Audio", createAudioPanel());
        tabs.addTab("Network", createNetworkPanel());
        tabs.addTab("Controller", createControllerPanel());
        return tabs;
    }
    
    private JPanel createGraphicsPanel()
    {
        final DefaultFormBuilder builder = createFormBuilder("30dlu, 3dlu, 60dlu, center:10dlu, 183dlu", "top:pref:grow, fill:pref");
        final CardLayout modeSpecificConfigLayout = new CardLayout();
        final JPanel modeSpecificConfig = new JPanel(modeSpecificConfigLayout);
        final HashSet<ViewType> availableModePanels = new HashSet<ViewType>();
        final JComboBox mode = new JComboBox(ViewType.values());
        mode.setSelectedItem(configClone.viewType);
        mode.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent arg0)
            {
                final ViewType viewType = (ViewType) mode.getSelectedItem();
                configClone.viewType = viewType;
                if (availableModePanels.contains(viewType));
                {
                    modeSpecificConfigLayout.show(modeSpecificConfig, viewType.toString());
                }
            }
        });
        builder.append("mode:", mode);
        
        final JPanel swingViewPanel = new JPanel();
        swingViewPanel.add(new JLabel("no further options available"));
        
        modeSpecificConfig.add(swingViewPanel, ViewType.SWING_VIEW.toString());
        modeSpecificConfig.add(createJMEConfigPanel(), ViewType.JME_VIEW.toString());
        builder.add(modeSpecificConfig, new CellConstraints().xywh(5, 1, 1, 2));
        
        addVerticalSeparator(builder);
        return builder.getPanel();
    }
    
    private JPanel createAudioPanel()
    {
        final DefaultFormBuilder builder = createFormBuilder();
        final JComboBox engine = new JComboBox(SoundEngine.values());
        engine.setSelectedItem(configClone.soundEngine);
        engine.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                configClone.soundEngine = (SoundEngine) engine.getSelectedItem();
            }
        });
        builder.append("engine:", engine);
        builder.append("no further options available");
        addVerticalSeparator(builder);
        return builder.getPanel();
    }
    
    private JPanel createNetworkPanel()
    {
        final DefaultFormBuilder builder = createFormBuilder("top:pref, 3dlu, top:pref, 3dlu, top:pref");
        final CellConstraints cc = new CellConstraints();
        
        final JTextField udpBroadcastPort = new JTextField(3);
        builder.add(new JLabel("UDP broadcast port:"), cc.xy(5, 1));
        builder.add(udpBroadcastPort, cc.xy(7, 1));
        
        final JTextField tcpControlPort = new JTextField(3);
        builder.add(new JLabel("TCP control port:"), cc.xy(5, 3));
        builder.add(tcpControlPort, cc.xy(7, 3));
        
        final JTextField udpFeedbackPort = new JTextField(3);
        builder.add(new JLabel("UDP feedback port:"), cc.xy(5, 5));
        builder.add(udpFeedbackPort, cc.xy(7, 5));

        addVerticalSeparator(builder);
        return builder.getPanel();
    }
    
    private JPanel createControllerPanel()
    {
        final DefaultFormBuilder builder = createFormBuilder("30dlu, 3dlu, 60dlu, center:10dlu, right:30dlu, 3dlu, left:30dlu, 33dlu, right:30dlu, 3dlu, left:30dlu",
                                                             "top:pref, 3dlu, top:pref, 3dlu, top:pref, 3dlu, top:pref, 3dlu, top:pref, 3dlu, top:pref, 33dlu, " +
                                                                 "top:pref, 3dlu, top:pref, 3dlu, top:pref, 3dlu, top:pref, 3dlu, top:pref, 3dlu, top:pref");
        final CellConstraints cc = new CellConstraints();
        final String[] keys = {"up", "down", "left", "right", "bomb"};
        
        for (int player = 1; player <= 4; player++)
        {
            final int col = (player % 2 == 1 ? 5 : 9);
            final int row = (player <= 2 ? 1 : 13);
            builder.addSeparator("Player " + player, cc.xyw(col, row, 3));
            for (int key = 0; key < keys.length; key++)
            {
                builder.add(new JLabel(keys[key]), cc.xy(col, row + 2 * (key + 1)));
                builder.add(createPlayerKeyPicker(player, keys[key]), cc.xy(col + 2, row + 2 * (key + 1)));
            }
        }
        
        addVerticalSeparator(builder);
        return builder.getPanel();
    }
    
    private DefaultFormBuilder createFormBuilder(String encodedColSpecs, String encodedRowSpecs)
    {
        final DefaultFormBuilder builder =
            new DefaultFormBuilder(
                new FormLayout(encodedColSpecs, encodedRowSpecs + ", fill:pref:grow")
                );
        builder.setDefaultDialogBorder();
        return builder;
    }
    
    private DefaultFormBuilder createFormBuilder(String encodedRowSpecs)
    {
        return createFormBuilder("30dlu, 3dlu, 60dlu, center:10dlu, right:150dlu, 3dlu, left:30dlu", encodedRowSpecs);
    }
    
    private DefaultFormBuilder createFormBuilder()
    {
        return createFormBuilder("top:pref, fill:pref");
    }
    
    private void addVerticalSeparator(DefaultFormBuilder builder)
    {
        // add vertical separator on the left
        final JSeparator separator = new JSeparator(JSeparator.VERTICAL);
        builder.add(separator, new CellConstraints().rchw(1, 4, builder.getRowCount(), 1));
    }
    
    private JPanel createJMEConfigPanel()
    {
        final DefaultFormBuilder builder = new DefaultFormBuilder(
            new FormLayout("right:50dlu, 3dlu, left:30dlu, 17dlu, right:50dlu, 3dlu, left:30dlu",
                           "top:pref, 3dlu, top:pref, 3dlu,  top:pref, 3dlu,  top:pref, 3dlu,  top:pref, 3dlu,  top:pref, 3dlu")
            );
        final CellConstraints cc = new CellConstraints();
        
        final JComboBox renderer = new JComboBox();
        builder.add(new JLabel("renderer:"), cc.xy(1, 1));
        builder.add(renderer, cc.xy(3, 1));
        
        final JComboBox music = new JComboBox();
        builder.add(new JLabel("music:"), cc.xy(5, 1));
        builder.add(music, cc.xy(7, 1));
        
        final JComboBox resolution = new JComboBox();
        builder.add(new JLabel("resolution:"), cc.xy(1, 3));
        builder.add(resolution, cc.xy(3, 3));
        
        final JComboBox soundEffects = new JComboBox();
        builder.add(new JLabel("sound effects:"), cc.xy(5, 3));
        builder.add(soundEffects, cc.xy(7, 3));
        
        final JComboBox depth = new JComboBox();
        builder.add(new JLabel("depth:"), cc.xy(1, 5));
        builder.add(depth, cc.xy(3, 5));
        
        final JComboBox depthBits = new JComboBox();
        builder.add(new JLabel("depth bits:"), cc.xy(5, 5));
        builder.add(depthBits, cc.xy(7, 5));
        
        final JComboBox frequency = new JComboBox();
        builder.add(new JLabel("frequency:"), cc.xy(1, 7));
        builder.add(frequency, cc.xy(3, 7));
        
        final JComboBox alphaBits = new JComboBox();
        builder.add(new JLabel("alpha bits:"), cc.xy(5, 7));
        builder.add(alphaBits, cc.xy(7, 7));
        
        final JComboBox verticalSync = new JComboBox();
        builder.add(new JLabel("vertical sync:"), cc.xy(1, 9));
        builder.add(verticalSync, cc.xy(3, 9));
        
        final JComboBox stencilBits = new JComboBox();
        builder.add(new JLabel("stencil bits:"), cc.xy(5, 9));
        builder.add(stencilBits, cc.xy(7, 9));
        
        final JComboBox fullscreen = new JComboBox();
        builder.add(new JLabel("fullscreen:"), cc.xy(1, 11));
        builder.add(fullscreen, cc.xy(3, 11));
        
        final JComboBox samples = new JComboBox();
        builder.add(new JLabel("samples:"), cc.xy(5, 11));
        builder.add(samples, cc.xy(7, 11));
        
        return builder.getPanel();
    }
    
    private JComboBox createPlayerKeyPicker(int player, String key)
    {
        final String[] keys = { "a", "b", "c", "d", "e" }; 
        final JComboBox combo = new JComboBox(keys);
        return combo;
    }
}
