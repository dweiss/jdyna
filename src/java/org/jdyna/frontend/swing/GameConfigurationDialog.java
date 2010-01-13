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
                    return config = GameConfiguration.CLASSIC_CONFIGURATION;
                case EXTENDED:
                    return config = new GameConfiguration();
                case CUSTOM:
                    return config;
            }
            throw new RuntimeException(
                "If you see this message, it means that author has forseen the opportunity of this error.");
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
                new FormLayout("40dlu, center:10dlu, right:150dlu, 3dlu, left:30dlu, 3dlu", "pref, fill:pref")
                );
        mainBuilder.setDefaultDialogBorder();

        final JComboBox schemeCombo = new JComboBox(GameConfigurationScheme.values());
        mainBuilder.append(schemeCombo);

        final JCheckBox replayCheckBox = new JCheckBox();
        replayCheckBox.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                // TODO set if replay data should be saved
            }
        });
        mainBuilder.append("Save for replay:", replayCheckBox);
        mainBuilder.nextLine();
        
        // add vertical separator on the left
        final JSeparator separator = new JSeparator(JSeparator.VERTICAL);
        mainBuilder.add(separator, new CellConstraints().rchw(1, 2, 2, 1));
        mainBuilder.nextColumn(2);
        
        // create options panel
        final DefaultFormBuilder builder =
            new DefaultFormBuilder(
                new FormLayout("right:150dlu, 3dlu, left:30dlu")
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
        builder.appendSeparator("Game options");        

        startingBombRange = new JTextField(3);
        startingBombRange.addCaretListener(createNumberListener(startingBombRange, 0, 1, 10));
        builder.append("starting bomb range:", startingBombRange);
        builder.nextLine();

        fuseFrames = new JTextField(3);
        fuseFrames.addCaretListener(createNumberListener(fuseFrames, 1, 1, 10, true));
        builder.append("fuse frames:", fuseFrames);
        builder.nextLine();

        startingBombCount = new JTextField(3);
        startingBombCount.addCaretListener(createNumberListener(startingBombCount, 2, 1, 10));
        builder.append("starting bomb count:", startingBombCount);
        builder.nextLine();

        bonusInterval = new JTextField(3);
        bonusInterval.addCaretListener(createNumberListener(bonusInterval, 3, 1, 60, true));
        builder.append("bonus interval:", bonusInterval);
        builder.nextLine();

        ressurectionDelay = new JTextField(3);
        ressurectionDelay.addCaretListener(createNumberListener(ressurectionDelay, 4, 1, 60, true));
        builder.append("resurrection delay:", ressurectionDelay);
        builder.nextLine();

        immortalityAfterRessurection = new JTextField(3);
        immortalityAfterRessurection.addCaretListener(createNumberListener(immortalityAfterRessurection, 5, 1, 60, true));
        builder.append("immortality after resurrection duration:", immortalityAfterRessurection);
        builder.nextLine();

        immortalityAfterJoin = new JTextField(3);
        immortalityAfterJoin.addCaretListener(createNumberListener(immortalityAfterJoin, 6, 1, 60, true));
        builder.append("immortality after joining game duration:", immortalityAfterJoin);
        builder.nextLine();

        lives = new JTextField(3);
        lives.addCaretListener(createNumberListener(lives, 7, 1, 10));
        builder.append("lives:", lives);
        builder.nextLine();

        addRandomCrates = new JCheckBox();
        addRandomCrates.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e)
            {
                config.ADD_RANDOM_CRATES = addRandomCrates.isSelected();
            }
        });
        builder.append("add random crates:", addRandomCrates);
        builder.nextLine();

        addingCratesInterval = new JTextField(3);
        addingCratesInterval.addCaretListener(createNumberListener(addingCratesInterval, 8, 1, 60, true));
        builder.append("adding crates interval:", addingCratesInterval);
        builder.nextLine();
    }

    private void addBonusOptions(final DefaultFormBuilder builder)
    {
        builder.appendSeparator("Bonus options");

        diarrheaDuration = new JTextField(3);
        diarrheaDuration.addCaretListener(createNumberListener(diarrheaDuration, 9, 1, 60, true));
        builder.append("diarrhea duration:", diarrheaDuration);
        builder.nextLine();

        noBombsDuration = new JTextField(3);
        noBombsDuration.addCaretListener(createNumberListener(noBombsDuration, 10, 1, 60, true));
        builder.append("no bombs duration:", noBombsDuration);
        builder.nextLine();

        maxRangeDuration = new JTextField(3);
        maxRangeDuration.addCaretListener(createNumberListener(maxRangeDuration, 11, 1, 60, true));
        builder.append("max range duration:", maxRangeDuration);
        builder.nextLine();

        speedChangeDuration = new JTextField(3);
        speedChangeDuration.addCaretListener(createNumberListener(speedChangeDuration, 12, 1, 60, true));
        builder.append("speed up/slow down duration:", speedChangeDuration);
        builder.nextLine();

        speedUpMultiplier = new JTextField(3);
        speedUpMultiplier.addCaretListener(createNumberListener(speedUpMultiplier, 13, 101, 500));
        builder.append("speed up multiplier (% of normal):", speedUpMultiplier);
        builder.nextLine();

        slowDownMultiplier = new JTextField(3);
        slowDownMultiplier.addCaretListener(createNumberListener(slowDownMultiplier, 14, 1, 99));
        builder.append("slow down multiplier (% of normal):", slowDownMultiplier);
        builder.nextLine();

        crateWalkingDuration = new JTextField(3);
        crateWalkingDuration.addCaretListener(createNumberListener(crateWalkingDuration, 15, 1, 60, true));
        builder.append("crate walking duration:", crateWalkingDuration);
        builder.nextLine();

        bombWalkingDuration = new JTextField(3);
        bombWalkingDuration.addCaretListener(createNumberListener(bombWalkingDuration, 16, 1, 60, true));
        builder.append("bomb walking duration:", bombWalkingDuration);
        builder.nextLine();

        controllerReverseDuration = new JTextField(3);
        controllerReverseDuration.addCaretListener(createNumberListener(controllerReverseDuration, 17, 1, 60, true));
        builder.append("controller reverse duration:", controllerReverseDuration);
        builder.nextLine();
    }

    private void addSchemesButtons(final DefaultFormBuilder builder)
    {
        builder.appendSeparator();
        
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
        builder.append(buttonsPanel, 3);
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

    public static void main(String [] args)
    {
        SwingUtilities.invokeLater(new Runnable()
        {
            public void run()
            {
                final GameConfigurationDialog configDialog = new GameConfigurationDialog(
                    new GameConfiguration(), GameConfigurationScheme.CLASSIC);
                configDialog.prompt(null);
            }
        });
    }

}
