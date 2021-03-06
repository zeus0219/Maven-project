//    uniCenta oPOS  - Touch Friendly Point Of Sale
//    Copyright (c) 2009-2018 uniCenta & previous Openbravo POS works
//    https://unicenta.com
//
//    This file is part of uniCenta oPOS
//
//    uniCenta oPOS is free software: you can redistribute it and/or modify
//    it under the terms of the GNU General Public License as published by
//    the Free Software Foundation, either version 3 of the License, or
//    (at your option) any later version.
//
//   uniCenta oPOS is distributed in the hope that it will be useful,
//    but WITHOUT ANY WARRANTY; without even the implied warranty of
//    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//    GNU General Public License for more details.
//
//    You should have received a copy of the GNU General Public License
//    along with uniCenta oPOS.  If not, see <http://www.gnu.org/licenses/>.
package com.openbravo.pos.sales;

import com.openbravo.data.loader.LocalRes;
import com.openbravo.pos.forms.AppLocal;
import com.openbravo.pos.scripting.ScriptEngine;
import com.openbravo.pos.scripting.ScriptException;
import com.openbravo.pos.scripting.ScriptFactory;
import com.openbravo.pos.ticket.TicketLineInfo;
import lombok.extern.slf4j.Slf4j;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.swing.*;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumnModel;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.awt.*;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;

/**
 *
 * @author JG uniCenta
 */
@Slf4j
public class JTicketLines extends javax.swing.JPanel {

    private static SAXParser m_sp = null;

    private final TicketTableModel m_jTableModel;
    private Boolean sendStatus;

    /**
     * Creates new form JLinesTicket
     *
     * @param ticketline
     */
    public JTicketLines(String ticketline) {

        initComponents();

        ColumnTicket[] acolumns = new ColumnTicket[0];

        if (ticketline != null) {
            try {
                if (m_sp == null) {
                    SAXParserFactory spf = SAXParserFactory.newInstance();
                    m_sp = spf.newSAXParser();
                }
                ColumnsHandler columnshandler = new ColumnsHandler();
                m_sp.parse(new InputSource(new StringReader(ticketline)), columnshandler);
                acolumns = columnshandler.getColumns();

            } catch (ParserConfigurationException ePC) {
                log.error(LocalRes.getIntString("exception.parserconfig"), ePC.getMessage());
            } catch (SAXException eSAX) {
                log.error(LocalRes.getIntString("exception.xmlfile"), eSAX.getMessage());
            } catch (IOException eIO) {
                log.error(LocalRes.getIntString("exception.iofile"), eIO);
            }
        }

        m_jTableModel = new TicketTableModel(acolumns);
        m_jTicketTable.setModel(m_jTableModel);
        TableColumnModel jColumns = m_jTicketTable.getColumnModel();
        for (int i = 0; i < acolumns.length; i++) {
            jColumns.getColumn(i).setPreferredWidth(acolumns[i].width);
            jColumns.getColumn(i).setResizable(false);
        }

        // set font for headers
        Font f = new Font("Arial", Font.BOLD, 14);
        JTableHeader header = m_jTicketTable.getTableHeader();
        header.setFont(f);
        header.setBorder(BorderFactory.createLineBorder(new Color(100, 100, 100)));
        /*
         * Starting point for SORTING the current table model & view
         * m_aLines + TicketLineInfo also has to be considered BECAUSE....
         * where ticket is open on another terminal it won't reflect change from 
         * other session once placed and opened from sharedticket table       
         *       
         *        m_jTicketTable.setRowSorter(new TableRowSorter(m_jTableModel));
         *        m_jTicketTable.getTableHeader().setReorderingAllowed(true); 
         *        m_jTicketTable.setAutoCreateRowSorter(true);
         *       m_jTicketTable.getTableHeader().addMouseListener(new MouseAdapter() {
         *   @Override
         *       public void mouseClicked(MouseEvent e) {
         *           int col = m_jTicketTable.columnAtPoint(e.getPoint());
         *           String name = m_jTicketTable.getColumnName(col);
         *           System.out.println("Column index selected " + col + " " + name);
         *       }
         *      });
         */

        m_jTicketTable.setDefaultRenderer(Object.class, new TicketCellRenderer(acolumns));

        m_jTicketTable.setRowHeight(40);
        m_jTicketTable.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        m_jTableModel.clear();
    }

    /**
     *
     * @param l
     */
    public void addListSelectionListener(ListSelectionListener l) {
        m_jTicketTable.getSelectionModel().addListSelectionListener(l);
    }

    /**
     *
     * @param l
     */
    public void removeListSelectionListener(ListSelectionListener l) {
        m_jTicketTable.getSelectionModel().removeListSelectionListener(l);
    }

    /**
     *
     */
    public void clearTicketLines() {
        m_jTableModel.clear();
    }

    /**
     *
     * @param index
     * @param oLine
     */
    public void setTicketLine(int index, TicketLineInfo oLine) {
        m_jTableModel.setRow(index, oLine);
    }

    /**
     *
     * @param oLine
     */
    public void addTicketLine(TicketLineInfo oLine) {
        m_jTableModel.addRow(oLine);
        setSelectedIndex(m_jTableModel.getRowCount() - 1);
    }

    /**
     *
     * @param index
     * @param oLine
     */
    public void insertTicketLine(int index, TicketLineInfo oLine) {
        m_jTableModel.insertRow(index, oLine);
        setSelectedIndex(index);
    }

    /**
     *
     * @param i
     */
    public void removeTicketLine(int i) {
        m_jTableModel.removeRow(i);

        if (i >= m_jTableModel.getRowCount()) {
            i = m_jTableModel.getRowCount() - 1;
        }

        if ((i >= 0) && (i < m_jTableModel.getRowCount())) {
            setSelectedIndex(i);
        }
    }

    /**
     *
     * @param i
     */
    public void setSelectedIndex(int i) {
        m_jTicketTable.getSelectionModel().setSelectionInterval(i, i);
        Rectangle oRect = m_jTicketTable.getCellRect(i, 0, true);
        m_jTicketTable.scrollRectToVisible(oRect);

    }

    /**
     *
     * @return
     */
    public int getSelectedIndex() {
        return m_jTicketTable.getSelectionModel().getMinSelectionIndex(); // solo sera uno, luego no importa...
    }

    public int sortIndex(int i) {
        int[] selection = m_jTicketTable.getSelectedRows();
        for (i = 0; i < selection.length; i++) {
            selection[i] = m_jTicketTable.convertRowIndexToModel(selection[i]);
        }
        return m_jTicketTable.getRowSorter().convertRowIndexToView(i);
    }

    /**
     *
     */
    public void selectionDown() {
        int i = m_jTicketTable.getSelectionModel().getMaxSelectionIndex();
        if (i < 0) {
            i = 0;
        } else {
            i++;
            if (i >= m_jTableModel.getRowCount()) {
                i = m_jTableModel.getRowCount() - 1;
            }
        }

        if ((i >= 0) && (i < m_jTableModel.getRowCount())) {
            setSelectedIndex(i);
        }
    }

    /**
     *
     */
    public void selectionUp() {
        int i = m_jTicketTable.getSelectionModel().getMinSelectionIndex();
        if (i < 0) {
            i = m_jTableModel.getRowCount() - 1; // No hay ninguna seleccionada
        } else {
            i--;
            if (i < 0) {
                i = 0;
            }
        }

        if ((i >= 0) && (i < m_jTableModel.getRowCount())) {
            setSelectedIndex(i);
        }
    }

    private static class TicketCellRenderer extends DefaultTableCellRenderer {

        private final ColumnTicket[] m_acolumns;

        public TicketCellRenderer(ColumnTicket[] acolumns) {
            m_acolumns = acolumns;
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {

            JLabel aux = (JLabel) super.getTableCellRendererComponent(table, value,
                    isSelected, hasFocus, row, column);
            aux.setVerticalAlignment(javax.swing.SwingConstants.TOP);
            aux.setHorizontalAlignment(m_acolumns[column].align);
            Font fName = aux.getFont();
            aux.setFont(new Font(fName.getName(), Font.PLAIN, 14));

            return aux;
        }
    }

    private static class TicketCellRendererSent extends DefaultTableCellRenderer {

        private final ColumnTicket[] m_acolumns;

        public TicketCellRendererSent(ColumnTicket[] acolumns) {
            m_acolumns = acolumns;
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {

            JLabel aux = (JLabel) super.getTableCellRendererComponent(table,
                    value, isSelected, hasFocus, row, column);

            aux.setVerticalAlignment(javax.swing.SwingConstants.TOP);
            aux.setHorizontalAlignment(m_acolumns[column].align);
            Font fName = aux.getFont();
            aux.setFont(new Font(fName.getName(), Font.PLAIN, 12));
            aux.setBackground(Color.yellow);
            return aux;
        }
    }

    private static class TicketTableModel extends AbstractTableModel {

        private final ColumnTicket[] m_acolumns;
        private final ArrayList m_rows = new ArrayList();

        public TicketTableModel(ColumnTicket[] acolumns) {
            m_acolumns = acolumns;
        }

        @Override
        public int getRowCount() {
            return m_rows.size();
        }

        @Override
        public int getColumnCount() {
            return m_acolumns.length;
        }

        @Override
        public String getColumnName(int column) {
            return AppLocal.getIntString(m_acolumns[column].name);
            // return m_acolumns[column].name;
        }

        @Override
        public Object getValueAt(int row, int column) {
            return ((String[]) m_rows.get(row))[column];
        }

        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }

        public void clear() {
            int old = getRowCount();
            if (old > 0) {
                m_rows.clear();
                fireTableRowsDeleted(0, old - 1);
            }
        }

        public void setRow(int index, TicketLineInfo oLine) {

            String[] row = (String[]) m_rows.get(index);
            for (int i = 0; i < m_acolumns.length; i++) {
                try {
                    ScriptEngine script = ScriptFactory.getScriptEngine(ScriptFactory.VELOCITY);
                    script.put("ticketline", oLine);
                    row[i] = script.eval(m_acolumns[i].value).toString();
                } catch (ScriptException e) {
                    row[i] = null;
                }
                fireTableCellUpdated(index, i);
            }
        }

        public void addRow(TicketLineInfo oLine) {

            insertRow(m_rows.size(), oLine);
        }

        public void insertRow(int index, TicketLineInfo oLine) {

            String[] row = new String[m_acolumns.length];
            for (int i = 0; i < m_acolumns.length; i++) {
                try {
                    ScriptEngine script = ScriptFactory.getScriptEngine(ScriptFactory.VELOCITY);
                    script.put("ticketline", oLine);
                    row[i] = script.eval(m_acolumns[i].value).toString();
                } catch (ScriptException e) {
                    row[i] = null;
                }
            }

            m_rows.add(index, row);
            fireTableRowsInserted(index, index);
        }

        public void removeRow(int row) {
            m_rows.remove(row);
            fireTableRowsDeleted(row, row);
        }
    }

    private static class ColumnsHandler extends DefaultHandler {

        private ArrayList m_columns = null;

        public ColumnTicket[] getColumns() {
            return (ColumnTicket[]) m_columns.toArray(new ColumnTicket[m_columns.size()]);
        }

        @Override
        public void startDocument() throws SAXException {
            m_columns = new ArrayList();
        }

        @Override
        public void endDocument() throws SAXException {
        }

        @Override
        public void startElement(String uri, String localName, String qName,
                Attributes attributes) throws SAXException {
            if ("column".equals(qName)) {
                ColumnTicket c = new ColumnTicket();
                c.name = attributes.getValue("name");
                c.width = Integer.parseInt(attributes.getValue("width"));
                String sAlign = attributes.getValue("align");
                switch (sAlign) {
                    case "right":
                        c.align = javax.swing.SwingConstants.RIGHT;
                        break;
                    case "center":
                        c.align = javax.swing.SwingConstants.CENTER;
                        break;
                    default:
                        c.align = javax.swing.SwingConstants.LEFT;
                        break;
                }
                c.value = attributes.getValue("value");
                m_columns.add(c);
            }
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
        }

        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
        }
    }

    /**
     *
     * @param state
     */
    public void setSendStatus(Boolean state) {
        sendStatus = state;
    }

    private static class ColumnTicket {

        public String name;
        public int width;
        public int align;
        public String value;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        m_jScrollTableTicket = new javax.swing.JScrollPane();
        m_jTicketTable = new javax.swing.JTable();
        jPanel1 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        btnTicketUp = new com.openbravo.beans.JImageButton();
        jPanel3 = new javax.swing.JPanel();
        btnTicketDown = new com.openbravo.beans.JImageButton();

        setOpaque(false);
        setLayout(new java.awt.BorderLayout());

        m_jScrollTableTicket.getVerticalScrollBar().setPreferredSize(new Dimension(0, 0));
        m_jScrollTableTicket.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        m_jScrollTableTicket.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        m_jScrollTableTicket.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        m_jScrollTableTicket.setOpaque(false);

        m_jTicketTable.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        m_jTicketTable.setFocusable(false);
        m_jTicketTable.setIntercellSpacing(new java.awt.Dimension(0, 1));
        m_jTicketTable.setRequestFocusEnabled(false);
        m_jTicketTable.setShowVerticalLines(false);
        m_jTicketTable.setFillsViewportHeight(true);
        m_jScrollTableTicket.setViewportView(m_jTicketTable);

        add(m_jScrollTableTicket, java.awt.BorderLayout.CENTER);

        jPanel1.setOpaque(false);
        jPanel1.setLayout(new java.awt.GridLayout(0, 1));

        jPanel2.setOpaque(false);
        jPanel2.setLayout(new javax.swing.BoxLayout(jPanel2, javax.swing.BoxLayout.LINE_AXIS));

        btnTicketUp.setToolTipText("Scroll Up");
        btnTicketUp.setFocusable(false);
        btnTicketUp.setIconPrefix("single-up-48");
        btnTicketUp.setPreferredSize(new java.awt.Dimension(50, 50));
        btnTicketUp.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnTicketUpActionPerformed(evt);
            }
        });
        jPanel2.add(btnTicketUp);

        jPanel1.add(jPanel2);

        jPanel3.setOpaque(false);
        jPanel3.setLayout(new javax.swing.BoxLayout(jPanel3, javax.swing.BoxLayout.LINE_AXIS));

        btnTicketDown.setToolTipText("Scroll Down");
        btnTicketDown.setFocusable(false);
        btnTicketDown.setIconPrefix("single-down-48");
        btnTicketDown.setPreferredSize(new java.awt.Dimension(50, 50));
        btnTicketDown.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnTicketDownActionPerformed(evt);
            }
        });
        jPanel3.add(btnTicketDown);

        jPanel1.add(jPanel3);

        add(jPanel1, java.awt.BorderLayout.LINE_END);
    }// </editor-fold>//GEN-END:initComponents

    private void btnTicketUpActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnTicketUpActionPerformed
        int scrollHeight = m_jScrollTableTicket.getSize().height;
        int scrollPos = m_jScrollTableTicket.getVerticalScrollBar().getValue();
        int contentHeight = m_jTicketTable.getSize().height;

        if (scrollHeight < contentHeight
                && scrollPos > 0) {
            int nextPos = scrollPos - 200;
            if (nextPos < 0) {
                nextPos = 0;
            }
            setTicketScrollPos(nextPos);
        }
    }//GEN-LAST:event_btnTicketUpActionPerformed

    private void btnTicketDownActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnTicketDownActionPerformed
        int scrollHeight = m_jScrollTableTicket.getSize().height;
        int scrollPos = m_jScrollTableTicket.getVerticalScrollBar().getValue();
        int contentHeight = m_jTicketTable.getSize().height;

        if (scrollHeight < contentHeight
                && scrollPos < contentHeight - scrollHeight + 10) {
            int nextPos = scrollPos + 200;
            if (nextPos + scrollHeight > contentHeight) {
                nextPos = contentHeight - scrollHeight;
            }
            setTicketScrollPos(nextPos);
        }
    }//GEN-LAST:event_btnTicketDownActionPerformed

    private void setTicketScrollPos(int value) {
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                m_jScrollTableTicket.getVerticalScrollBar().setValue(value);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private com.openbravo.beans.JImageButton btnTicketDown;
    private com.openbravo.beans.JImageButton btnTicketUp;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane m_jScrollTableTicket;
    private javax.swing.JTable m_jTicketTable;
    // End of variables declaration//GEN-END:variables

}
