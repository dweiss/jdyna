package org.jdyna.frontend.swing;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;

import org.jdyna.GameConfiguration;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

public final class GameConfigurationDialog
{
    /**
     * Configuration scheme to use.
     */
    public static enum GameConfigurationScheme
    {
        CLASSIC, EXTENDED, CUSTOM;

        public String toString()
        {
            switch (this)
            {
                case CLASSIC:
                    return "classic";
                case EXTENDED:
                    return "extended";
                case CUSTOM:
                    return "custom";
            }
            throw new RuntimeException("Unreachable.");
        }
    }

    private GameConfigurationScheme scheme;
    private GameConfiguration config;
    final private GameConfiguration originalConfig;

    private final static Color ERROR_COLOR = new Color(255, 192, 192);
    private final static Color OK_COLOR = new Color(255, 255, 255);
    
    // interface elements
    JTextField startingBombRange, fuseFrames, startingBombCount, bonusInterval,
        ressurectionDelay, immortalityAfterRessurection, immortalityAfterJoin, lives,
        addingCratesInterval, diarrheaDuration, noBombsDuration, maxRangeDuration,
        speedChangeDuration, speedUpMultiplier, slowDownMultiplier, crateWalkingDuration,
        bombWalkingDuration, controllerReverseDuration;
    private JCheckBox addRandomCrates;
    
    public GameConfigurationDialog(GameConfiguration config, GameConfigurationScheme scheme)
    {
        originalConfig = config;
        this.config = (GameConfiguration) config.clone();
        this.scheme = scheme;
    }

    /**
     * Create a modal dialog asking for preferences.
     * 
     * @return configuration chosen by user, or <code>null</code>, if user selected
     *         'cancel' button
     */
    public GameConfiguration prompt(Component parent)
    {
        final JComponent configPanel = createConfigPanel();

        if (JOptionPane.OK_OPTION == JOptionPane.showOptionDialog(parent, configPanel,
            "Edit game preferences", JOptionPane.OK_CANCEL_OPTION,
            JOptionPane.PLAIN_MESSAGE, null, null, null))
        {
            switch (scheme)
            {
                case CLASSIC:
                    return config = GameConfiguration.CLASSIC;
                case EXTENDED:
                    return config = new GameConfiguration();
                case CUSTOM:
                    return config;
            }

            throw new RuntimeException("Unreachable code.");
        }

        return config = null;
    }
    
    /**
     * Returns most recently generated config without showing a dialog.
     */
    public GameConfiguration getConfig()
    {
        return config;
    }

    /**
     * Create configuration panel
     */
    private JComponent createConfigPanel()
    {        
        final DefaultFormBuilder mainBuilder =
            new DefaultFormBuilder(
                    new FormLayout("40dlu, center:10dlu, right:349dlu, 3dlu, left:30dlu, 3dlu", "pref, fill:pref")
                );
        mainBuilder.setDefaultDialogBorder();

        final JComboBox schemeCombo = new JComboBox(GameConfigurationScheme.values());
        mainBuilder.append(schemeCombo);
        mainBuilder.nextLine();
        
        // add vertical separator on the left
        final JSeparator separator = new JSeparator(JSeparator.VERTICAL);
        mainBuilder.add(separator, new CellConstraints().rchw(1, 2, 2, 1));
        mainBuilder.nextColumn(2);
        
        // create options panel
        final DefaultFormBuilder builder =
            new DefaultFormBuilder(
                new FormLayout("right:130dlu, 3dlu, left:30dlu, 33dlu, right:110dlu, 3dlu, left:30dlu",
                               "top:pref, 3dlu, top:pref, 3dlu, top:pref, 3dlu, top:pref, 3dlu, top:pref, 3dlu, top:pref, 3dlu, top:pref, 3dlu, top:pref, 3dlu, top:pref, 3dlu, top:pref, 3dlu, top:pref, 3dlu, top:pref, 3dlu, top:pref")
                );
        
        // add options to panel
        addGameOptions(builder);
        addBonusOptions(builder);        
        addSchemesButtons(builder);
        
        // put config panel on main panel
        final JPanel customConfigPanel = getCustomConfigPanel(builder, mainBuilder);
        
        // add bottom separator
        mainBuilder.appendSeparator();
        
        // schemeCombo's action listener is set here, because we've got customConfigPanel
        // reference at last
        schemeCombo.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                scheme = (GameConfigurationScheme)schemeCombo.getSelectedItem(); 
                customConfigPanel.setVisible(scheme == GameConfigurationScheme.CUSTOM);
            }
        });
        
        // set config inputs for the first time
        setConfigInputs(config);
        
        return mainBuilder.getPanel();
    }

    private void addGameOptions(final DefaultFormBuilder builder)
    {
        final CellConstraints cc = new CellConstraints();
        
        builder.addSeparator("Game options", cc.xyw(1, 1, 3));        

        startingBombRange = new JTextField(3);
        startingBombRange.addCaretListener(createNumberListener(startingBombRange, 0, 1, 10));
        builder.add(new JLabel("starting bomb range:"), cc.xy(1, 3));
        builder.add(startingBombRange, cc.xy(3, 3));

        fuseFrames = new JTextField(3);
        fuseFrames.addCaretListener(createNumberListener(fuseFrames, 1, 1, 10, true));
        builder.add(new JLabel("fuse frames:"), cc.xy(1, 5));
        builder.add(fuseFrames, cc.xy(3, 5));

        startingBombCount = new JTextField(3);
        startingBombCount.addCaretListener(createNumberListener(startingBombCount, 2, 1, 10));
        builder.add(new JLabel("starting bomb count:"), cc.xy(1, 7));
        builder.add(startingBombCount, cc.xy(3, 7));

        bonusInterval = new JTextField(3);
        bonusInterval.addCaretListener(createNumberListener(bonusInterval, 3, 1, 60, true));
        builder.add(new JLabel("bonus interval:"), cc.xy(1, 9));
        builder.add(bonusInterval, cc.xy(3, 9));

        ressurectionDelay = new JTextField(3);
        ressurectionDelay.addCaretListener(createNumberListener(ressurectionDelay, 4, 1, 60, true));
        builder.add(new JLabel("resurrection delay:"), cc.xy(1, 11));
        builder.add(ressurectionDelay, cc.xy(3, 11));

        immortalityAfterRessurection = new JTextField(3);
        immortalityAfterRessurection.addCaretListener(createNumberListener(immortalityAfterRessurection, 5, 1, 60, true));
        builder.add(new JLabel("immortality after resurrection duration:"), cc.xy(1, 13));
        builder.add(immortalityAfterRessurection, cc.xy(3, 13));

        immortalityAfterJoin = new JTextField(3);
        immortalityAfterJoin.addCaretListener(createNumberListener(immortalityAfterJoin, 6, 1, 60, true));
        builder.add(new JLabel("immortality after joining game duration:"), cc.xy(1, 15));
        builder.add(immortalityAfterJoin, cc.xy(3, 15));

        lives = new JTextField(3);
        lives.addCaretListener(createNumberListener(lives, 7, 1, 10));
        builder.add(new JLabel("lives:"), cc.xy(1, 17));
        builder.add(lives, cc.xy(3, 17));

        addRandomCrates = new JCheckBox();
        addRandomCrates.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e)
            {
                config.ADD_RANDOM_CRATES = addRandomCrates.isSelected();
            }
        });
        builder.add(new JLabel("add random crates:"), cc.xy(1, 19));
        builder.add(addRandomCrates, cc.xy(3, 19));

        addingCratesInterval = new JTextField(3);
        addingCratesInterval.addCaretListener(createNumberListener(addingCratesInterval, 8, 1, 60, true));
        builder.add(new JLabel("adding crates interval:"), cc.xy(1, 21));
        builder.add(addingCratesInterval, cc.xy(3, 21));
    }

    private void addBonusOptions(final DefaultFormBuilder builder)
    {
        final CellConstraints cc = new CellConstraints();
        builder.addSeparator("Bonus options", cc.xyw(5, 1, 3));

        diarrheaDuration = new JTextField(3);
        diarrheaDuration.addCaretListener(createNumberListener(diarrheaDuration, 9, 1, 60, true));
        builder.add(new JLabel("diarrhea duration:"), cc.xy(5, 3));
        builder.add(diarrheaDuration, cc.xy(7, 3));

        noBombsDuration = new JTextField(3);
        noBombsDuration.addCaretListener(createNumberListener(noBombsDuration, 10, 1, 60, true));
        builder.add(new JLabel("no bombs duration:"), cc.xy(5, 5));
        builder.add(noBombsDuration, cc.xy(7, 5));

        maxRangeDuration = new JTextField(3);
        maxRangeDuration.addCaretListener(createNumberListener(maxRangeDuration, 11, 1, 60, true));
        builder.add(new JLabel("max range duration:"), cc.xy(5, 7));
        builder.add(maxRangeDuration, cc.xy(7, 7));

        speedChangeDuration = new JTextField(3);
        speedChangeDuration.addCaretListener(createNumberListener(speedChangeDuration, 12, 1, 60, true));
        builder.add(new JLabel("speed up/slow down duration:"), cc.xy(5, 9));
        builder.add(speedChangeDuration, cc.xy(7, 9));

        speedUpMultiplier = new JTextField(3);
        speedUpMultiplier.addCaretListener(createNumberListener(speedUpMultiplier, 13, 101, 500));
        builder.add(new JLabel("speed up multiplier (% of normal):"), cc.xy(5, 11));
        builder.add(speedUpMultiplier, cc.xy(7, 11));

        slowDownMultiplier = new JTextField(3);
        slowDownMultiplier.addCaretListener(createNumberListener(slowDownMultiplier, 14, 1, 99));
        builder.add(new JLabel("slow down multiplier (% of normal):"), cc.xy(5, 13));
        builder.add(slowDownMultiplier, cc.xy(7, 13));

        crateWalkingDuration = new JTextField(3);
        crateWalkingDuration.addCaretListener(createNumberListener(crateWalkingDuration, 15, 1, 60, true));
        builder.add(new JLabel("crate walking duration:"), cc.xy(5, 15));
        builder.add(crateWalkingDuration, cc.xy(7, 15));

        bombWalkingDuration = new JTextField(3);
        bombWalkingDuration.addCaretListener(createNumberListener(bombWalkingDuration, 16, 1, 60, true));
        builder.add(new JLabel("bomb walking duration:"), cc.xy(5, 17));
        builder.add(bombWalkingDuration, cc.xy(7, 17));

        controllerReverseDuration = new JTextField(3);
        controllerReverseDuration.addCaretListener(createNumberListener(controllerReverseDuration, 17, 1, 60, true));
        builder.add(new JLabel("controller reverse duration:"), cc.xy(5, 19));
        builder.add(controllerReverseDuration, cc.xy(7, 19));
    }

    private void addSchemesButtons(final DefaultFormBuilder builder)
    {
        final CellConstraints cc = new CellConstraints();
        builder.addSeparator("", cc.xyw(1, 23, 7));
        
        final JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        final JButton revertButton = new JButton("Revert");
        revertButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent arg0)
            {
                setConfigInputs(originalConfig);
            }
        });
        buttonsPanel.add(revertButton);
        final JButton defaultsButton = new JButton("Defaults");
        defaultsButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent arg0)
            {
                setConfigInputs(new GameConfiguration());
            }
        });
        buttonsPanel.add(defaultsButton);
        builder.add(buttonsPanel, cc.xyw(1, 25, 7));
    }
    
    private JPanel getCustomConfigPanel(final DefaultFormBuilder builder,
        final DefaultFormBuilder mainBuilder)
    {
        // add customConfigPanel to its own panel (which size is set on next lines), so
        // that whole config panel's size isn't depending on whether it is visible or not
        final JPanel customConfigPanel = builder.getPanel();
        final Dimension size = customConfigPanel.getPreferredSize();
        final JPanel customConfigPanelPanel = new JPanel();
        customConfigPanelPanel.add(customConfigPanel);
        customConfigPanelPanel.setPreferredSize(size);
        customConfigPanelPanel.setMinimumSize(size);
        customConfigPanelPanel.setSize(size);
        mainBuilder.append(customConfigPanelPanel, 3);
        customConfigPanel.setVisible(scheme == GameConfigurationScheme.CUSTOM);
        return customConfigPanel;
    }
    
    private CaretListener createNumberListener(final JTextField field, final int value,
        final int min, final int max, final boolean relativeToFPS)
    {
        field.setBackground(OK_COLOR);
        return new CaretListener()
        {
            public void caretUpdate(CaretEvent e)
            {
                boolean numberOK = true;
                int val = Integer.MIN_VALUE;
                try
                {
                    val = Integer.parseInt(field.getText());
                    if (relativeToFPS)
                    {
                        if (val < config.DEFAULT_FRAME_RATE * min
                            || val > config.DEFAULT_FRAME_RATE * max)
                            numberOK = false;
                    }
                    else if (val < min || val > max)
                        numberOK = false;
                }
                catch (NumberFormatException ex)
                {
                    numberOK = false;
                }
                if (numberOK)
                {
                    field.setBackground(OK_COLOR);
                    switch (value)
                    {
                        // GAME OPTIONS
                        case 0:
                            config.DEFAULT_BOMB_RANGE = val;
                            return;
                        case 1:
                            config.DEFAULT_FUSE_FRAMES = val;
                            return;
                        case 2:
                            config.DEFAULT_BOMB_COUNT = val;
                            return;
                        case 3:
                            config.DEFAULT_BONUS_PERIOD = val;
                            return;
                        case 4:
                            config.DEFAULT_RESURRECTION_FRAMES = val;
                            return;
                        case 5:
                            config.DEFAULT_IMMORTALITY_FRAMES = val;
                            return;
                        case 6:
                            config.DEFAULT_JOINING_IMMORTALITY_FRAMES = val;
                            return;
                        case 7:
                            config.DEFAULT_LIVES = val;
                            return;
                        case 8:
                            config.DEFAULT_CRATE_PERIOD = val;
                            return;
                        // BONUS OPTIONS
                        case 9:
                            config.DEFAULT_DIARRHEA_FRAMES = val;
                            return;
                        case 10:
                            config.DEFAULT_NO_BOMBS_FRAMES = val;
                            return;
                        case 11:
                            config.DEFAULT_MAXRANGE_FRAMES = val;
                            return;
                        case 12:
                            config.DEFAULT_SPEED_FRAMES = val;
                            return;
                        case 13:
                            config.SPEED_UP_MULTIPLIER = (float) val / (float) 100;
                            return;
                        case 14:
                            config.SLOW_DOWN_MULTIPLIER = (float) val / (float) 100;
                            return;
                        case 15:
                            config.DEFAULT_CRATE_WALKING_FRAMES = val;
                            return;
                        case 16:
                            config.DEFAULT_BOMB_WALKING_FRAMES = val;
                            return;
                        case 17:
                            config.DEFAULT_CONTROLLER_REVERSE_FRAMES = val;
                            return;
                    }
                    throw new RuntimeException("Unreachable.");
                }
                else
                {
                    field.setBackground(ERROR_COLOR);
                }
            }
        };
    }
    
    private void setConfigInputs(final GameConfiguration config)
    {
        assert SwingUtilities.isEventDispatchThread();
        this.config.DEFAULT_FRAME_RATE = config.DEFAULT_FRAME_RATE;
        // GAME OPTIONS
        startingBombRange.setText(Integer.toString(config.DEFAULT_BOMB_RANGE));
        fuseFrames.setText(Integer.toString(config.DEFAULT_FUSE_FRAMES));
        startingBombCount.setText(Integer.toString(config.DEFAULT_BOMB_COUNT));
        bonusInterval.setText(Integer.toString(config.DEFAULT_BONUS_PERIOD));
        ressurectionDelay.setText(Integer.toString(config.DEFAULT_RESURRECTION_FRAMES));
        immortalityAfterRessurection.setText(Integer.toString(config.DEFAULT_IMMORTALITY_FRAMES));
        immortalityAfterJoin.setText(Integer.toString(config.DEFAULT_JOINING_IMMORTALITY_FRAMES));
        lives.setText(Integer.toString(config.DEFAULT_LIVES));
        addRandomCrates.setSelected(config.ADD_RANDOM_CRATES);
        addingCratesInterval.setText(Integer.toString(config.DEFAULT_CRATE_PERIOD));
        // BONUS OPTIONS
        diarrheaDuration.setText(Integer.toString(config.DEFAULT_DIARRHEA_FRAMES));
        noBombsDuration.setText(Integer.toString(config.DEFAULT_NO_BOMBS_FRAMES));
        maxRangeDuration.setText(Integer.toString(config.DEFAULT_MAXRANGE_FRAMES));
        speedChangeDuration.setText(Integer.toString(config.DEFAULT_SPEED_FRAMES));
        speedUpMultiplier.setText(Integer.toString((int)(config.SPEED_UP_MULTIPLIER * 100)));
        slowDownMultiplier.setText(Integer.toString((int)(config.SLOW_DOWN_MULTIPLIER * 100)));
        crateWalkingDuration.setText(Integer.toString(config.DEFAULT_CRATE_WALKING_FRAMES));
        bombWalkingDuration.setText(Integer.toString(config.DEFAULT_BOMB_WALKING_FRAMES));
        controllerReverseDuration.setText(Integer.toString(config.DEFAULT_CONTROLLER_REVERSE_FRAMES));
    }

    private CaretListener createNumberListener(final JTextField field, final int value, final int min, final int max)
    {
        return createNumberListener(field, value, min, max, false);
    }
}
