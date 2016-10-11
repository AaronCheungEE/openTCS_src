/*
 * openTCS copyright information:
 * Copyright (c) 2005-2011 ifak e.V.
 * Copyright (c) 2012 Fraunhofer IML
 *
 * This program is free software and subject to the MIT license. (For details,
 * see the licensing information (LICENSE.txt) you should have received with
 * this copy of the software.)
 */

package org.opentcs.guing.components.properties.panel;

import javax.swing.JPanel;
import org.opentcs.guing.components.dialogs.DetailsDialogContent;
import org.opentcs.guing.components.properties.type.Property;
import org.opentcs.guing.components.properties.type.StringProperty;
import org.opentcs.guing.util.ResourceBundleUtil;

/**
 * Ein Panel, mit dem ein String editiert werden kann.
 *
 * @author Sebastian Naumann (ifak e.V. Magdeburg)
 */
public class StringPropertyEditorPanel
    extends JPanel
    implements DetailsDialogContent {

  /**
   * Das Property, auf das der Panel zugreift.
   */
  private StringProperty fProperty;

  /**
   * Creates new form StringPropertyPanel
   */
  public StringPropertyEditorPanel() {
    initComponents();
  }

  @Override // DetailsDialogContent
  public String getTitle() {
    return ResourceBundleUtil.getBundle().getString("StringPropertyEditorPanel.title");
  }

  /**
   * Richtet die Dialogelemente entsprechend den Eigenschaftswerten ein. Diese
   * Methode wird aufgerufen, wenn der Dialog ge�ffnet wird.
   */
  public void initFields() {
    textArea.setText(fProperty.getText());
    textArea.setCaretPosition(fProperty.getText().length());
  }

  @Override // DetailsDialogContent
  public void updateValues() {
    fProperty.setText(textArea.getText());
  }

  @Override // DetailsDialogContent
  public void setProperty(Property property) {
    fProperty = (StringProperty) property;
    initFields();
  }

  @Override // DetailsDialogContent
  public Property getProperty() {
    return fProperty;
  }

  // CHECKSTYLE:OFF
  /**
   * This method is called from within the constructor to initialize the form.
   * WARNING: Do NOT modify this code. The content of this method is always
   * regenerated by the Form Editor.
   */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        textArea = new javax.swing.JTextArea();

        setLayout(new java.awt.BorderLayout());

        textArea.setColumns(20);
        textArea.setFont(new java.awt.Font("Monospaced", 0, 12)); // NOI18N
        textArea.setLineWrap(true);
        textArea.setRows(15);
        textArea.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        add(textArea, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextArea textArea;
    // End of variables declaration//GEN-END:variables
  // CHECKSTYLE:ON
}
