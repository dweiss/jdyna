package com.dawidweiss.dyna.view.swing;

import java.awt.Dimension;
import java.util.List;

import javax.swing.*;
import javax.swing.table.*;

import com.dawidweiss.dyna.*;

/**
 * Swing component displaying player status information.
 */
@SuppressWarnings("serial")
public final class ScorePanel extends JPanel implements IGameEventListener
{
    /**
     * Player stats model for JTable
     */
    private static class PlayersTableModel extends AbstractTableModel
    {
        private List<PlayerStatus> modelData;

        private Class<?> [] columns =
        {
            String.class, Integer.class, Integer.class, String.class
        };
        
        private String [] columnNames =
        {
            "Player", "Lives", "Enemies", "Status"
        };
        
        private int [] columnWeights = 
        {
            90, 10, 10, 10 
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
            final PlayerStatus status = modelData.get(rowIndex);
            switch (columnIndex)
            {
                case 0:
                    return status.getPlayerName();
                case 1:
                    return status.getLivesLeft();
                case 2:
                    return status.getKilledEnemies();
                case 3:
                    if (status.isDead()) return "DEAD";
                    if (status.isImmortal()) return "IMMORTAL";
                    return "";
            }
            return null;
        }

        public void setModelData(final List<PlayerStatus> modelData)
        {
            SwingUtilities.invokeLater(new Runnable()
            {
                public void run()
                {
                    PlayersTableModel.this.modelData = modelData;
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
    private TableRowSorter<PlayersTableModel> sorter;
    private PlayersTableModel model;

    /**
     * 
     */
    public ScorePanel()
    {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        this.model = new PlayersTableModel();
        sorter = new TableRowSorter<PlayersTableModel>(model);
        table = new JTable(model);
        table.setRowSorter(sorter);
        table.setPreferredScrollableViewportSize(new Dimension(50, 70));
        table.setFillsViewportHeight(true);
        table.setAutoCreateRowSorter(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
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
                model.setModelData(((GameStatusEvent) e).stats);
            }
        }
    }
}
