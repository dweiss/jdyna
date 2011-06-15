package org.jdyna.frontend.swing;

import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashSet;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;

import org.jdyna.frontend.swing.Configuration.KeyBinding;
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
    private final static Color ERROR_COLOR = new Color(255, 192, 192);
    private final static Color OK_COLOR = new Color(255, 255, 255);
    
    private Configuration configClone;
    private Configuration original;
    
    private final JTextField[] keyBindingFields;
    
    private JTextField udpBroadcastPort;
    private JTextField tcpControlPort;
    private JTextField udpFeedbackPort;

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
        
        keyBindingFields = new JTextField[configClone.keyBindings.length];
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
            public void actionPerformed(ActionEvent e)
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
        modeSpecificConfigLayout.show(modeSpecificConfig, configClone.viewType.toString());
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
        
        udpBroadcastPort = new JTextField(Integer.toString(configClone.UDPBroadcastPort), 3);
        udpBroadcastPort.addCaretListener(createPortCaretListener(udpBroadcastPort));
        builder.add(new JLabel("UDP broadcast port (server):"), cc.xy(5, 1));
        builder.add(udpBroadcastPort, cc.xy(7, 1));
        
        tcpControlPort = new JTextField(Integer.toString(configClone.TCPport), 3);
        tcpControlPort.addCaretListener(createPortCaretListener(tcpControlPort));
        builder.add(new JLabel("TCP control port (client):"), cc.xy(5, 3));
        builder.add(tcpControlPort, cc.xy(7, 3));
        
        udpFeedbackPort = new JTextField(Integer.toString(configClone.UDPport), 3);
        udpFeedbackPort.addCaretListener(createPortCaretListener(udpFeedbackPort));
        builder.add(new JLabel("UDP feedback port (client):"), cc.xy(5, 5));
        builder.add(udpFeedbackPort, cc.xy(7, 5));

        addVerticalSeparator(builder);
        return builder.getPanel();
    }
    
    private CaretListener createPortCaretListener(final JTextField field)
    {
        return new CaretListener()
        {
            public void caretUpdate(CaretEvent e)
            {
                if (field == udpBroadcastPort)
                {
                    try
                    {
                        configClone.UDPBroadcastPort = Integer.parseInt(udpBroadcastPort.getText());
                    }
                    catch (NumberFormatException ex)
                    {
                        configClone.UDPBroadcastPort = original.UDPBroadcastPort;
                    }
                }
                else if (field == udpFeedbackPort)
                {
                    try
                    {
                        configClone.UDPport = Integer.parseInt(udpFeedbackPort.getText());
                    }
                    catch (NumberFormatException ex)
                    {
                        configClone.UDPport = original.UDPport;
                    }
                }
                else if (field == tcpControlPort)
                {
                    try
                    {
                        configClone.TCPport = Integer.parseInt(tcpControlPort.getText());
                    }
                    catch (NumberFormatException ex)
                    {
                        configClone.TCPport = original.TCPport;
                    }
                }
                
                // set default field colors assuming no conflicts
                udpBroadcastPort.setBackground(OK_COLOR);
                udpFeedbackPort.setBackground(OK_COLOR);
                tcpControlPort.setBackground(OK_COLOR);
                // check for conflicts
                if (configClone.UDPBroadcastPort == configClone.UDPport)
                {
                    udpBroadcastPort.setBackground(ERROR_COLOR);
                    udpFeedbackPort.setBackground(ERROR_COLOR);
                }
                if (configClone.UDPBroadcastPort == configClone.TCPport)
                {
                    udpBroadcastPort.setBackground(ERROR_COLOR);
                    tcpControlPort.setBackground(ERROR_COLOR);
                }
            }
        };
    }

    private JPanel createControllerPanel()
    {
        final DefaultFormBuilder builder = createFormBuilder("30dlu, 3dlu, 60dlu, center:10dlu, right:30dlu, 3dlu, left:30dlu, 33dlu, right:30dlu, 3dlu, left:30dlu",
                                                             "top:pref, 3dlu, top:pref, 3dlu, top:pref, 3dlu, top:pref, 3dlu, top:pref, 3dlu, top:pref, 33dlu, " +
                                                                 "top:pref, 3dlu, top:pref, 3dlu, top:pref, 3dlu, top:pref, 3dlu, top:pref, 3dlu, top:pref, center:13dlu, top:pref");
        final CellConstraints cc = new CellConstraints();
        
        for (int player = 1; player <= 4; player++)
        {
            final int col = (player % 2 == 1 ? 5 : 9);
            final int row = (player <= 2 ? 1 : 13);
            builder.addSeparator("Player " + player, cc.xyw(col, row, 3));
            for (KeyBinding key: KeyBinding.values())
            {
                builder.add(new JLabel(key.toString()), cc.xy(col, row + 2 * (key.ordinal() + 1)));
                final JComponent keyPicker = createPlayerKeyPicker(player - 1, key);
                builder.add(keyPicker, cc.xy(col + 2, row + 2 * (key.ordinal() + 1)));
            }
        }
        
        builder.addSeparator("", cc.xyw(5, 24, 7));
        final JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        final JButton revert = new JButton("Revert");
        revert.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e)
            {
                revertKeyBindings(original);
            }
        });
        buttonsPanel.add(revert);
        final JButton defaults = new JButton("Defaults");
        defaults.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e)
            {
                revertKeyBindings(new Configuration());
            }
        });
        buttonsPanel.add(defaults);
        builder.add(buttonsPanel, cc.xyw(5, 25, 7));
        
        addVerticalSeparator(builder);
        return builder.getPanel();
    }
    
    private void revertKeyBindings(Configuration config)
    {
        for (int i = 0; i < keyBindingFields.length; i++)
        {
            bindAndFindConflicts(config.keyBindings[i], configClone.keyBindings[i], i);
        }
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
    
    private JComponent createPlayerKeyPicker(final int player, final KeyBinding key)
    {
        final JTextField field = new JTextField(8);
        final int keyOffset = player * KeyBinding.values().length + key.ordinal();
        final int code = configClone.keyBindings[keyOffset];
        field.addKeyListener(new KeyListener(){
            public void keyReleased(KeyEvent e)
            {
                e.consume();
            }

            public void keyTyped(KeyEvent e)
            {
                e.consume();
            }

            public void keyPressed(KeyEvent e)
            {
                final int prevCode = configClone.keyBindings[keyOffset];
                final int code = e.getKeyCode();
                e.consume();
                
                bindAndFindConflicts(code, prevCode, keyOffset);
            }
        });
        field.setBackground(OK_COLOR);
        keyBindingFields[keyOffset] = field;
        bindAndFindConflicts(code, KeyEvent.VK_UNDEFINED, keyOffset);
        return field;
    }
    
    private void bindAndFindConflicts(int code, int prevCode, int keyOffset)
    {
        // look for binding conflicts
        // there's no point in looking for conflicts, if no binding has changed
        if (prevCode != code)
        {
            final JTextField field = keyBindingFields[keyOffset];
            field.setText(KeyEvent.getKeyText(code));
            configClone.keyBindings[keyOffset] = code;

            JTextField previouslyConflicting = null;
            int previousConflicts = 0;
            boolean conflict = false;

            for (int i = 0; i < keyBindingFields.length; i++)
            {
                final JTextField f = keyBindingFields[i];
                // f may be null only while resolving conflicts when creating config
                // dialog
                if (f != null && f != field)
                {
                    final int fCode = configClone.keyBindings[i];
                    if (fCode == code)
                    {
                        conflict = true;
                        f.setBackground(ERROR_COLOR);
                    }
                    if (fCode == prevCode)
                    {
                        previouslyConflicting = f;
                        previousConflicts++;
                    }
                }
            }
            // set modified field's color relatively to conflicts found
            field.setBackground(conflict ? ERROR_COLOR : OK_COLOR);
            // if previous conflict has been resolved, reset previously
            // conflicting fields' colors
            if (previousConflicts == 1)
            {
                previouslyConflicting.setBackground(OK_COLOR);
            }
        }
    }
}
