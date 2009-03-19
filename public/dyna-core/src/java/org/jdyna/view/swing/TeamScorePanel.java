package org.jdyna.view.swing;

import java.awt.Dimension;
import java.util.List;

import javax.swing.*;
import javax.swing.table.*;

import org.jdyna.*;


/**
 * Swing component displaying team status information.
 */
@SuppressWarnings("serial")
public final class TeamScorePanel extends JPanel implements IGameEventListener
{
    /**
     * Team stats model.
     */
    private static class TeamsTableModel extends AbstractTableModel
    {
        private List<TeamStatus> modelData;

        private Class<?> [] columns =
        {
            String.class, Integer.class, Integer.class, Integer.class, Integer.class
        };
        
        private String [] columnNames =
        {
            "Team", "Players", "Alive", "Kills", "Lives" 
        };
        
        private int [] columnWeights = 
        {
            90, 10, 10, 10, 10 
        };

        @Override
        public Class<?> getColumnClass(int columnIndex)
        {
            return columns[columnIndex];
        }

        @Override
        public int getColumnCount()
        {
            return columns.length;
        }

        @Override
        public String getColumnName(int columnIndex)
        {
            return columnNames[columnIndex];
        }

        @Override
        public int getRowCount()
        {
            return modelData == null ? 0 : modelData.size();
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex)
        {
            final TeamStatus status = modelData.get(rowIndex);
            switch (columnIndex)
            {
                case 0:
                    return status.getTeamName();
                case 1:
                    return status.getPlayersTotal();
                case 2:
                    return status.getPlayersLeft();
                case 3:
                    return status.getKilledEnemies();
                case 4:
                    return status.getLivesLeft();
            }
            return null;
        }

        public void setModelData(final List<TeamStatus> modelData)
        {
            SwingUtilities.invokeLater(new Runnable()
            {
                public void run()
                {
                    TeamsTableModel.this.modelData = modelData;
                    fireTableDataChanged();
                }
            });
        }

        public void updateColumns(TableColumnModel columnModel)
        {
            for (int i = 0; i < columnWeights.length; i++)
            {
                columnModel.getColumn(i).setPreferredWidth(columnWeights[i]);
            }
        }
    }

    private JTable table;
    private TableRowSorter<TeamsTableModel> sorter;
    private TeamsTableModel model;

    /**
     * 
     */
    public TeamScorePanel()
    {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        this.model = new TeamsTableModel();
        sorter = new TableRowSorter<TeamsTableModel>(model);
        table = new JTable(model);
        table.setRowSorter(sorter);
        table.setPreferredScrollableViewportSize(new Dimension(50, 70));
        table.setFillsViewportHeight(true);
        table.setAutoCreateRowSorter(true);
        SwingUtils.disableSelection(table);
        this.model.updateColumns(table.getColumnModel());

        final JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane);
    }

    /**
     * @see IGameEventListener
     */
    public void onFrame(int frame, final List<? extends GameEvent> events)
    {
        for (final GameEvent e : events)
        {
            if (e.type == GameEvent.Type.GAME_STATUS)
            {
                model.setModelData(((GameStatusEvent) e).teamStats);
            }
        }
    }
}
